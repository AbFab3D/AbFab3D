/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package svxconv;

import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.GridShortIntervals;
import abfab3d.io.input.*;
import abfab3d.io.output.GridSaver;
import abfab3d.io.output.ShellResults;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.STLWriter;
import abfab3d.io.output.SVXWriter;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.util.TriangleMesh;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import abfab3d.util.*;
import org.apache.commons.io.FilenameUtils;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Map;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;

/**
 * SVX format conversion tools.
 *
 * Converts between voxel and triangle formats.
 *
 * @author Alan Hudson
 */
public class SVXConv {
    private final static String USAGE_MSG =
            "SVX format converter \n" +
                    "usage: svxconv -input foo.svx -output bar.stl \n" +
                    " -input <input file path>\n" +
                    " -output string (output + extension(svx,stl,x3d*))\n" +
                    " -voxelSize float, voxel size in meters\n" +
                    " -meshSmoothingWidth float[0.5], width of output smooth in voxel size\n" +
                    " -meshErrorFactor float [0.1], max decimation error factor\n" +
                    " -meshMaxPartsCount int [MAX], maximum number of parts\n" +
                    " -meshMinPartVolume float [0], minimum volume per part\n" +
                    " -maxTriangles int [1800000], maximum triangle count\n" +
                    " -threadCount int [0]  number of threads to use. 0 - number of available cores\n";

    static final String
            OUTPUT = "-output",
            VOXELSIZE = "-voxelSize",
            INPUT = "-input",
            MESH_SMOOTHING_WIDTH = "-meshSmoothingWidth",
            MESH_ERROR_FACTOR = "-meshErrorFactor",
            MESH_MAX_PARTS_COUNT = "-meshMaxPartsCount",
            MESH_MIN_PART_VOLUME = "-meshMinPartVolume",
            MAX_TRIANGLES = "-maxTriangles",
            THREAD_COUNT = "-threadCount";

    private String input;
    private String output;
    private double voxelSize = 0.1 * Units.MM;
    private double meshSmoothingWidth = 0.5;
    private double meshErrorFactor = 0.1;
    private double meshMinVolume = 0;
    private int meshMaxPartsCount = Integer.MAX_VALUE;
    private int maxTriangles = 1800000;
    private int maxRunTime;
    private int threadCount;


    public void setInput(String input){
        this.input = input;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setVoxelSize(double voxelSize) {
        this.voxelSize = voxelSize;
    }

    public void setMeshSmoothingWidth(double smoothingWidth) {
        this.meshSmoothingWidth = smoothingWidth;
    }

    public void setMeshErrorFactor(double errorFactor) {
        printf("Setting error factor: %f\n",errorFactor);
        this.meshErrorFactor = errorFactor;
    }

    public void setMaxRunTime(int maxRunTime) {
        this.maxRunTime = maxRunTime;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public void setMaxTriangles(int maxTriangles) {
        this.maxTriangles = maxTriangles;
    }

    public double getMeshMinVolume() {
        return meshMinVolume;
    }

    public void setMeshMinVolume(double meshMinVolume) {
        this.meshMinVolume = meshMinVolume;
    }

    public int getMeshMaxPartsCount() {
        return meshMaxPartsCount;
    }

    public void setMeshMaxPartsCount(int meshMaxPartsCount) {
        this.meshMaxPartsCount = meshMaxPartsCount;
    }

    public void execute() throws IOException {
        int processors = Runtime.getRuntime().availableProcessors();
        if (threadCount == 0) {
            threadCount = processors;
        } else if (threadCount > processors) {
            printf("Capping threads to max processors: %d", processors);
            threadCount = processors;
        }
        AbFab3DGlobals.put(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY, threadCount);

        // Load input into a grid
        String ext = FilenameUtils.getExtension(input);

        AttributeGrid grid = null;
        int subvoxelResolution = 255;

        if (ext.equalsIgnoreCase("svx")) {
            SVXReader reader = new SVXReader();
            grid = reader.load(input);

            SVXManifest manifest = reader.getManifest();
            Map<String,String> map = manifest.getMetadata();

            String meshErrorFactor_st = map.get("meshErrorFactor");
            String meshSmoothingWidth_st = map.get("meshSmoothingWidth");

            if (meshErrorFactor_st != null) {
                meshErrorFactor = Double.parseDouble(meshErrorFactor_st);

                printf("Using user specified meshErrorFactor: %4.3f\n",meshErrorFactor);
            }

            if (meshSmoothingWidth_st != null) {
                meshSmoothingWidth = Double.parseDouble(meshSmoothingWidth_st);
                printf("Using user specified meshSmoothingWidth: %4.3f\n",meshSmoothingWidth);
            }
        } else if (ext.equalsIgnoreCase("stl") || ext.startsWith("x3d") || ext.startsWith("X3D")) {
            BoundingBoxCalculator bb = new BoundingBoxCalculator();
            TriangleProducer tp = null;

            if (ext.startsWith("x3d") || ext.startsWith("X3D")) {
                tp = new X3DReader(input);
                tp.getTriangles(bb);
            } else {
                tp = new STLReader(input);
                tp.getTriangles(bb);
            }

            double bounds[] = new double[6];
            bb.getBounds(bounds);

            printf("   orig bounds: [ %7.3f, %7.3f], [%7.3f, %7.3f], [%7.3f, %7.3f] mm; voxelSize: %7.3f mm\n",
                    bounds[0]/MM, bounds[1]/MM, bounds[2]/MM, bounds[3]/MM, bounds[4]/MM, bounds[5]/MM, voxelSize/MM);

            // Add a margin around the model to get some space
            bounds = MathUtil.extendBounds(bounds, 1 * voxelSize);
            //
            // round up to the nearest voxel
            //
            MathUtil.roundBounds(bounds, voxelSize);
            int nx = (int) Math.round((bounds[1] - bounds[0]) / voxelSize);
            int ny = (int) Math.round((bounds[3] - bounds[2]) / voxelSize);
            int nz = (int) Math.round((bounds[5] - bounds[4]) / voxelSize);
            printf("   grid bounds: [ %7.3f, %7.3f], [%7.3f, %7.3f], [%7.3f, %7.3f] mm; voxelSize: %7.3f mm\n",
                    bounds[0]/MM, bounds[1]/MM, bounds[2]/MM, bounds[3]/MM, bounds[4]/MM, bounds[5]/MM, voxelSize/MM);
            printf("  grid size: [%d x %d x %d]\n", nx, ny, nz);

            // range check bounds and voxelSized
            for(int i = 0; i < bounds.length; i++) {
                Float f = new Float(bounds[i]);
                if (f.isNaN()) {
                    throw new IllegalArgumentException("Grid size[" + i + "] is Not a Number.");
                }

            }

            if (nx <= 0) {
                throw new IllegalArgumentException("Grid x size <= 0: " + nx);
            }
            if (ny <= 0) {
                throw new IllegalArgumentException("Grid y size <= 0" + ny);
            }
            if (nz <= 0) {
                throw new IllegalArgumentException("Grid z size <= 0" + nz);
            }

            long t0 = time();
            grid = makeEmptyGrid(new int[] {nx,ny,nz},voxelSize);

            grid.setGridBounds(bounds);

            WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, nx, ny, nz);
            //rasterizer.setMaxAttributeValue(subvoxelResolution);

            tp.getTriangles(rasterizer);

            rasterizer.getRaster(grid);
            printf("Rasterize time: %d ms\n",(time() - t0));
        }

        // Write output to a file
        ext = FilenameUtils.getExtension(output);

        if (ext.equalsIgnoreCase("svx")) {
            SVXWriter writer = new SVXWriter();
            writer.write(grid, output);
        } else if (ext.equalsIgnoreCase("stl")) {
            TriangleMesh mesh = getMesh(grid,subvoxelResolution,meshMinVolume,meshMaxPartsCount);

            STLWriter stl = new STLWriter(output);
            mesh.getTriangles(stl);
            stl.close();
        } else if (ext.startsWith("x3d") || ext.startsWith("X3D")) {
            WingedEdgeTriangleMesh mesh = (WingedEdgeTriangleMesh) getMesh(grid,subvoxelResolution,meshMinVolume,meshMaxPartsCount);
            GridSaver.writeMesh(mesh, output);
        }
    }

    private AttributeGrid makeEmptyGrid(int[] gs, double vs) {
        AttributeGrid grid = null;

        long voxels = ((long) (gs[0])) * gs[1] * gs[2];

        long MAX_MEMORY = Integer.MAX_VALUE;
        if (voxels > MAX_MEMORY) {
            grid = new GridShortIntervals(gs[0], gs[1], gs[2], vs, vs);
        } else {
            grid = new ArrayAttributeGridByte(gs[0], gs[1], gs[2], vs, vs);
        }

        return grid;
    }

    private TriangleMesh getMesh(AttributeGrid grid, int subvoxelResolution, double mv, int mp) {
        double voxelSize = grid.getVoxelSize();

        printf("error factor: %f\n",meshErrorFactor);
        double maxDecimationError = meshErrorFactor * voxelSize * voxelSize;
        // Write out the grid to an STL file
        MeshMakerMT meshmaker = new MeshMakerMT();
        int max_threads = 8;

        meshmaker.setThreadCount(max_threads);
        meshmaker.setSmoothingWidth(meshSmoothingWidth);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(10);
        meshmaker.setMaxAttributeValue(subvoxelResolution);
        meshmaker.setMaxTriangles(maxTriangles);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
        meshmaker.makeMesh(grid, its);

        System.out.println("Vertices: " + its.getVertexCount() + " faces: " + its.getFaceCount());

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        if (mv > 0 || mp < Integer.MAX_VALUE) {
            ShellResults sr = GridSaver.getLargestShells(mesh, mp, mv);
            mesh = sr.getLargestShell();
            int regions_removed = sr.getShellsRemoved();
            System.out.println("Regions removed: " + regions_removed);
        }

        return mesh;
    }

	public static final void main(String[] args) {

        for(int i=0; i < args.length; i++) {
            System.out.println("   " + args[i]);
        }
        if( args.length == 0){
            printf(USAGE_MSG);
            System.exit(6);
        }

		ImageIO.setUseCache(false);
        SVXConv conv = new SVXConv();

        try {
            for(int i = 0; i < args.length; i++){
                String arg = args[i];

                if(arg.charAt(0) != '-'){
                    printf("invalid key:%s\n",arg);
                    printf(USAGE_MSG);
                    System.exit(6);
                }

                if(arg.equals(VOXELSIZE)){
                    conv.setVoxelSize(Double.parseDouble(args[++i]));
                } else if(arg.equals(INPUT)){
                    conv.setInput(args[++i]);
                } else if(arg.equals(OUTPUT)){
                    conv.setOutput(args[++i]);
                } else if (arg.equals(MESH_ERROR_FACTOR)){
                    conv.setMeshErrorFactor(Double.parseDouble(args[++i]));
                } else if (arg.equals(MESH_MIN_PART_VOLUME)){
                    conv.setMeshMinVolume(Double.parseDouble(args[++i]));
                } else if (arg.equals(MESH_MAX_PARTS_COUNT)){
                    conv.setMeshMaxPartsCount(Integer.parseInt(args[++i]));
                } else if (arg.equals(MAX_TRIANGLES)){
                    conv.setMaxTriangles(Integer.parseInt(args[++i]));
                } else if (arg.equals(THREAD_COUNT)){
                    conv.setThreadCount(Integer.parseInt(args[++i]));
                } else if (arg.equals(MESH_SMOOTHING_WIDTH)){
                    conv.setMeshSmoothingWidth(Double.parseDouble(args[++i]));
                } else {

                    System.out.println("Unknown parameter: " + arg);
                    printf(USAGE_MSG);
                    System.exit(6);
                }
            }
        } catch(Exception e){
            e.printStackTrace(System.out);
            printf(USAGE_MSG);
            System.exit(6);
        }

        try {
            conv.execute();
        } catch (IOException ioe) {
            System.out.println("Input file not found.");
            ioe.printStackTrace();
            System.exit(1);
        } catch (OutOfMemoryError oom) {
            System.out.println("Out of memory error.");
            oom.printStackTrace();
            System.exit(102);
        } catch(Exception e) {
            // something unexpected...
            System.out.println("Software crash.");
            e.printStackTrace();
            System.exit(101);
        }
    }
}
