/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package rectsidedpopper;

// External Imports

import abfab3d.core.Grid;
import abfab3d.creator.GeometryKernel;
import abfab3d.creator.KernelResults;
import abfab3d.creator.Parameter;
import abfab3d.creator.shapeways.HostedKernel;
import abfab3d.creator.util.ParameterUtil;
import abfab3d.grid.*;
import abfab3d.grid.op.GridMaker;
import abfab3d.transforms.*;
import abfab3d.io.output.BoxesX3DExporter;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.SAVExporter;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.AreaCalculator;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import abfab3d.io.output.GridSaver;
import abfab3d.io.output.ShellResults;
import app.common.RegionPrunner;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.export.X3DXMLRetainedExporter;
import org.web3d.vrml.sav.BinaryContentHandler;

import javax.vecmath.Vector3d;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static abfab3d.core.MathUtil.TORAD;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static java.lang.System.currentTimeMillis;

//import java.awt.*;

/**
 * Geometry Kernel for a side image popper
 * <p/>
 *
 * @author Alan Hudson
 */
public class RectSidedPopperKernel extends HostedKernel {
    public enum Shape {CUBE}
    private static final boolean DEBUG = false;
    private static final boolean USE_MIP_MAPPING = false;
    private final boolean USE_MESH_MAKER_MT = true;

    /**
     * Debugging level.  0-5.  0 is none
     */
    private static final int DEBUG_LEVEL = 0;

    // High = print resolution.  Medium = print * 1.5, LOW = print * 2
    enum PreviewQuality {LOW(2.0), MEDIUM(1.5), HIGH(1.0);

        private double factor;

        PreviewQuality(double f) {
            factor = f;
        }

        public double getFactor() {
            return factor;
        }
    };

    /** The horizontal and vertical resolution */
    private double resolution;
    private PreviewQuality previewQuality;
    private int smoothSteps;
    private double maxDecimationError;

    /** How many regions to keep */
    private RegionPrunner.Regions regions;

    /** The height of the sides */
    private double sideHeight;

    /** The thickness of the sides */
    private double sideThickness;

    /** The radius of the inscribed circle of the shape */
    private double radius;

    /** The side images */
    private String sideImage1;
    private String sideImage2;
    private String sideImage3;
    private String sideImage4;
    private String sideImage5;
    private String sideImage6;

    private String topImage;
    private String bottomImage;

    /** Top starting location expressed as percentage */
    private double topLocation;

    /** Top thickness */
    private double topThickness;

    /** Bottom starting location expressed as percentage */
    private double bottomLocation;

    /** Bottom thickness */
    private double bottomThickness;

    /** Shape to create */
    private Shape shape;

    private String material;
    private boolean useGrayscale;
    private boolean visRemovedRegions;
    private int threads;

    private String[] availableMaterials = new String[]{"White Strong & Flexible", "White Strong & Flexible Polished",
            "Silver", "Silver Glossy", "Stainless Steel", "Gold Plated Matte", "Gold Plated Glossy", "Antique Bronze Matte",
            "Antique Bronze Glossy", "Alumide", "Polished Alumide"};

    /**
     * Get the parameters for this editor.
     *
     * @return The parameters.
     */
    public Map<String, Parameter> getParams() {
        HashMap<String, Parameter> params = new HashMap<String, Parameter>();

        int seq = 0;
        int step = 0;

        params.put("sideHeight", new Parameter("sideHeight", "Side Height", "The height of the sides", "0.02", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        params.put("sideThickness", new Parameter("sideThickness", "Side Thickness", "The thickness of the sides", "0.0015", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        params.put("radius", new Parameter("radius", "Radius", "The radius of the inscribed circle", "0.01", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        params.put("sideImage1", new Parameter("sideImage1", "Side Image 1", "The image to use for side 1", "images/bside1.png", 1,
                Parameter.DataType.URI, Parameter.EditorType.FILE_DIALOG,
                step, seq++, false, 0, 0.1, null, null)
        );
        params.put("sideImage2", new Parameter("sideImage2", "Side Image 2", "The image to use for side 2", "images/bside2.png", 1,
                Parameter.DataType.URI, Parameter.EditorType.FILE_DIALOG,
                step, seq++, false, 0, 0.1, null, null)
        );
        params.put("sideImage3", new Parameter("sideImage3", "Side Image 3", "The image to use for side 3", "images/bside3.png", 1,
                Parameter.DataType.URI, Parameter.EditorType.FILE_DIALOG,
                step, seq++, false, 0, 0.1, null, null)
        );
        params.put("sideImage4", new Parameter("sideImage4", "Side Image 4", "The image to use for side 4", "images/bside4.png", 1,
                Parameter.DataType.URI, Parameter.EditorType.FILE_DIALOG,
                step, seq++, false, 0, 0.1, null, null)
        );
        params.put("sideImage5", new Parameter("sideImage5", "Side Image 5", "The image to use for side 5", "NONE", 1,
                Parameter.DataType.URI, Parameter.EditorType.FILE_DIALOG,
                step, seq++, false, 0, 0.1, null, null)
        );
        params.put("sideImage6", new Parameter("sideImage6", "Side Image 6", "The image to use for side 6", "NONE", 1,
                Parameter.DataType.URI, Parameter.EditorType.FILE_DIALOG,
                step, seq++, false, 0, 0.1, null, null)
        );

        params.put("topImage", new Parameter("topImage", "Top Image", "Top Image", "images/bsidet.png", 1,
                Parameter.DataType.URI, Parameter.EditorType.FILE_DIALOG,
                step, seq++, false, 0, 0.1, null, null)
        );
        params.put("topLocation", new Parameter("topLocation", "Top Location", "Top location as percentage of side height", "1", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );
        params.put("topThickness", new Parameter("topThickness", "Top Thickness", "Top thickness above sides", "0.0015", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        params.put("bottomImage", new Parameter("bottomImage", "Bottom Image", "Bottom Image", "images/bsideb.png", 1,
                Parameter.DataType.URI, Parameter.EditorType.FILE_DIALOG,
                step, seq++, false, 0, 0.1, null, null)
        );

        params.put("bottomLocation", new Parameter("bottomLocation", "Bottom Location", "Bottom location as percentage of side height", "0", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        params.put("bottomThickness", new Parameter("bottomThickness", "Bottom Thickness", "Bottom thickness below sides", "0.0015", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        params.put("useGrayscale", new Parameter("useGrayscale", "Use Grayscale", "Should we use grayscale", "false", 1,
                Parameter.DataType.BOOLEAN, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 100, null, null)
        );

        params.put("shape", new Parameter("shape", "Shape", "What shape to use", "CUBE", 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, new String[] {Shape.CUBE.toString()})
        );

        step++;
        seq = 0;

        params.put("material", new Parameter("material", "Material", "What material to design for", "Silver Glossy", 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, availableMaterials)
        );

        step++;
        seq = 0;

        // Advanced Params
        params.put("resolution", new Parameter("resolution", "Resolution", "How accurate to model the object", "0.0001", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 0.1, null, null)
        );

        params.put("threads", new Parameter("threads", "Threads", "Threads to use for operations", "0", 1,
                Parameter.DataType.INTEGER, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 50, null, null)
        );

        params.put("previewQuality", new Parameter("previewQuality", "PreviewQuality", "How rough is the preview", PreviewQuality.MEDIUM.toString(), 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, enumToStringArray(PreviewQuality.values()))
        );

        params.put("maxDecimationError", new Parameter("maxDecimationError", "MaxDecimationError", "Maximum error during decimation", "1e-9", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 1, null, null)
        );

        params.put("regions", new Parameter("regions", "Regions", "How many regions to keep", RegionPrunner.Regions.ALL.toString(), 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, enumToStringArray(RegionPrunner.Regions.values()))
        );

        params.put("smoothSteps", new Parameter("smoothSteps", "Smooth Steps", "How smooth to make the object", "3", 1,
                Parameter.DataType.INTEGER, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 100, null, null)
        );

        params.put("visRemovedRegions", new Parameter("visRemovedRegions", "Vis Removed Regions", "Visualize removed regions", "false", 1,
                Parameter.DataType.BOOLEAN, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 100, null, null)
        );


        return params;
    }

    /**
     * @param params  The parameters
     * @param acc     The accuracy to generate the model
     * @param handler The X3D content handler to use
     */
    public KernelResults generate(Map<String, Object> params, Accuracy acc, BinaryContentHandler handler) throws IOException {

        System.out.println("Generating RectSidedPopper on thread: " + Thread.currentThread().getName());
        long start = currentTimeMillis();

        pullParams(params);

        Grid grid = null;

        if (acc == Accuracy.VISUAL) {
            resolution = resolution * previewQuality.getFactor();
        }

        if (maxDecimationError > 0) {
            maxDecimationError = 0.1*resolution*resolution;
        }

        double voxelSize = resolution;
        double margin = 5 * voxelSize;
        // HARD CODED params to play with
        // width of Gaussian smoothing of grid, may be 0. - no smoothing
        double smoothingWidth = 0;
        // size of grid block for MT calculatins
        // (larger values reduce processor cache performance)
        int blockSize = 50;
        // max number to use for surface transitions. Should be ODD number
        // set it to 1 to have binary grid
        int maxGridAttributeValue = 63;
        // width of surface transition area relative to voxel size
        // optimal value sqrt(3)/2. Larger value causes rounding of sharp edges
        // sreyt it to 0. to make no surface transitions
        double surfaceTransitionWidth = Math.sqrt(3)/2; // 0.866
        double imagesBlurWidth = surfaceTransitionWidth*voxelSize;

        double baseWidth = radius;
        double shapeCenter[] = new double[]{0,0,sideHeight/2}; // center of the shape

        double gridWidth = baseWidth + 2 * margin;
        double gridHeight = baseWidth + 2 * margin;
        double gridDepth = sideHeight + 2 * margin;

        double bounds[] = new double[]{-gridWidth / 2, gridWidth / 2, -gridHeight / 2, gridHeight / 2, -margin, sideHeight + margin};

        int nx = (int) ((bounds[1] - bounds[0]) / voxelSize);
        int ny = (int) ((bounds[3] - bounds[2]) / voxelSize);
        int nz = (int) ((bounds[5] - bounds[4]) / voxelSize);

        printf("grid: [%d x %d x %d]\n", nx, ny, nz);
        //printf("bodyDepth: %f mm\n", bodyDepth*1000);
        //printf("sideThickness: %fmm, sideHeight: %f mm\n", sideThickness*1000, sideHeight*1000);

        // TODO: Change to use BlockBased for some size
        grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        popImage(grid, baseWidth, baseWidth, bottomThickness, bottomImage, bounds, new double[]{1,0,0,180*TORAD},null,new double[] {0,0,bottomThickness});
        popImage(grid, baseWidth, baseWidth, topThickness, topImage, bounds, null,null, new double[] {0,0,topLocation * sideHeight-topThickness});

        popImage(grid, baseWidth, sideHeight, sideThickness, sideImage1, bounds, new double[]{1,0,0,90*TORAD}, null, new double[] {0,-baseWidth/2+sideThickness,sideHeight/2});
        popImage(grid, baseWidth, sideHeight, sideThickness, sideImage2, bounds,
                 new double[]{0,0,1,180*TORAD}, new double[]{1,0,0,-90*TORAD}, new double[] {0,baseWidth/2-sideThickness,sideHeight/2});
        popImage(grid, baseWidth, sideHeight, sideThickness, sideImage3, bounds,
                 new double[]{0,0,1,90*TORAD}, new double[]{0,1,0,90*TORAD}, new double[] {baseWidth/2-sideThickness,0,sideHeight/2});
        popImage(grid, baseWidth, sideHeight, sideThickness, sideImage4, bounds,
                 new double[]{0,0,1,-90*TORAD}, new double[]{0,1,0,-90*TORAD}, new double[] {-baseWidth/2+sideThickness,0,sideHeight/2});

        //popImage(grid, sideHeight, baseWidth, sideThickness, sideImage3, bounds, new double[]{0,1,0,90*TORAD}, null, new double[] {-baseWidth/2,0,sideHeight/2});
        //popImage(grid, sideHeight, baseWidth, sideThickness, sideImage4, bounds, new double[]{0,1,0,-90*TORAD}, null, new double[] {baseWidth/2,0,sideHeight/2});


        if (regions != RegionPrunner.Regions.ALL) {
//            System.out.println("Regions Counter: " + RegionCounter.countComponents(grid, Grid.INSIDE, Integer.MAX_VALUE, true, ConnectedComponentState.DEFAULT_ALGORITHM));
            if (visRemovedRegions) {
                RegionPrunner.reduceToOneRegion(grid, handler, bounds);
            } else {
                RegionPrunner.reduceToOneRegion(grid);
            }
        }

        int min_volume = 10;
        int regions_removed = 0;

        System.out.println("Writing grid");

        HashMap<String, Object> exp_params = new HashMap<String, Object>();
        exp_params.put(SAVExporter.EXPORT_NORMALS, false);   // Required now for ITS?
        if (acc == Accuracy.VISUAL) {
            params.put(SAVExporter.GEOMETRY_TYPE, SAVExporter.GeometryType.INDEXEDTRIANGLESET);
            params.put(SAVExporter.VERTEX_NORMALS, true);
        } else {
            params.put(SAVExporter.GEOMETRY_TYPE, SAVExporter.GeometryType.INDEXEDTRIANGLESET);
        }

        long t0;

        WingedEdgeTriangleMesh mesh;

        double gbounds[] = new double[6];
        grid.getGridBounds(gbounds);

        // place of default viewpoint
        double viewDistance = GridSaver.getViewDistance(grid);

        if(USE_MESH_MAKER_MT){

            MeshMakerMT meshmaker = new MeshMakerMT();

            t0 = time();
            meshmaker.setBlockSize(blockSize);
            meshmaker.setThreadCount(threads);
            meshmaker.setSmoothingWidth(smoothingWidth);
            meshmaker.setMaxDecimationError(maxDecimationError);
            meshmaker.setSmoothingWidth(smoothingWidth);
            meshmaker.setMaxAttributeValue(maxGridAttributeValue);

            // TODO: Need to get a better way to estimate this number
            IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
            meshmaker.makeMesh(grid, its);
            mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

            printf("MeshMakerMT.makeMesh(): %d ms\n", (time()-t0));

            // extra decimation to get rid of seams
            if(maxDecimationError > 0){
                t0 = time();
                mesh = GridSaver.decimateMesh(mesh, maxDecimationError);
                printf("final decimation: %d ms\n", (time()-t0));
            }

        } else {

            mesh = GridSaver.createIsosurface(grid, smoothSteps);
            // Release grid to lower total memory requirements
            if(maxDecimationError > 0)
                mesh = GridSaver.decimateMesh(mesh, maxDecimationError);
        }

        // Release grid to save memory
        grid = null;

        if(regions != RegionPrunner.Regions.ALL) {
            t0 = time();
            ShellResults sr = GridSaver.getLargestShell(mesh, min_volume);
            mesh = sr.getLargestShell();
            regions_removed = sr.getShellsRemoved();
            printf("GridSaver.getLargestShell(): %d ms\n", (time()-t0));
        }

        GridSaver.writeMesh(mesh, viewDistance, handler, params, true);

        AreaCalculator ac = new AreaCalculator();
        mesh.getTriangles(ac);
        double volume = ac.getVolume();
        double surface_area = ac.getArea();

        // Do not shorten the accuracy of these prints they need to be high
        printf("final surface area: %7.8f cm^2\n", surface_area * 1.e4);
        printf("final volume: %7.8f cm^3\n", volume * 1.e6);

        printf("Total time: %d ms\n", (time() - start));
        printf("-------------------------------------------------\n");

        double min_bounds[] = new double[]{gbounds[0],gbounds[2],gbounds[4]};
        double max_bounds[] = new double[]{gbounds[1],gbounds[3],gbounds[5]};
        return new KernelResults(true, min_bounds, max_bounds, volume, surface_area, regions_removed);

    }

    private void popImage(Grid grid, double bodyWidth, double bodyHeight, double bodyDepth,
                          String filename, double[] bounds,
                          double rot[], double rot2[], double translation[]) {

        ImageBox layer = new ImageBox();
        layer.setSize(bodyWidth, bodyHeight, bodyDepth);
        layer.setCenter(0, 0, bodyDepth/2); // move up halfthickness to align bottom of the image with xy plane
        layer.setBaseThickness(0.0);
        layer.setImageType(ImageBox.IMAGE_TYPE_EMBOSSED);
        layer.setTiles(1, 1);
        layer.setImagePath(filename);
        layer.setUseGrayscale(useGrayscale);

        if (USE_MIP_MAPPING) {
            layer.setInterpolationType(ImageBox.INTERPOLATION_MIPMAP);
            layer.setPixelWeightNonlinearity(1.0);  // 0 - linear, 1. - black pixels get more weight
            //layer.setProbeSize(resolution * 2.);
        }

        GridMaker gm = new GridMaker();

        CompositeTransform transform = new CompositeTransform();

        // do rotation if needed
        if(rot != null){
            Rotation r = new Rotation();
            r.setRotation(new Vector3d(rot[0],rot[1],rot[2]), rot[3]);
            transform.add(r);
        }
        // secont rotation if needed
        if(rot2 != null){
            Rotation r = new Rotation();
            r.setRotation(new Vector3d(rot2[0],rot2[1],rot2[2]), rot2[3]);
            transform.add(r);
        }

        // do translation
        if(translation != null){
            Translation tr = new Translation();
            tr.setTranslation(translation[0],translation[1],translation[2]);
            transform.add(tr);
        }

        gm.setTransform(transform);
        gm.setBounds(bounds);
        gm.setSource(layer);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);
        printf("gm.makeGrid() done\n");

    }
/*
    private void popImageYUP(Grid grid, double bodyWidth1, double bodyDepth1, double bodyHeight1, double margin, String filename, double[] trans, double[] rot, double[] bounds) {
        ImageBitmapYUP layer1 = new ImageBitmapYUP();
        layer1.setSize(bodyWidth1, bodyHeight1, bodyDepth1);
        layer1.setCenter(0, 0, bodyDepth1/2);
        layer1.setBaseThickness(0.0);
        layer1.setImageType(ImageBitmapYUP.IMAGE_POSITIVE);
        layer1.setTiles(1, 1);
        layer1.setImagePath(filename);
        layer1.setUseGrayscale(useGrayscale);

        if (USE_MIP_MAPPING) {
            layer1.setInterpolationType(ImageBitmapYUP.INTERPOLATION_MIPMAP);
            layer1.setPixelWeightNonlinearity(1.0);  // 0 - linear, 1. - black pixels get more weight
            layer1.setProbeSize(resolution * 2.);
        }

        GridMaker gm = new GridMaker();
        if (trans != null && rot != null) {
            System.out.println("**Need to implement combined");
        } else if (trans != null) {
            layer1.setCenter(trans[0],trans[1],bodyDepth1/2 + trans[2]);
        } else if (rot != null) {
            Rotation rotation = new Rotation();
            rotation.m_axis = new Vector3d(rot[0],rot[1],rot[2]);
            rotation.m_angle = rot[3];
            gm.setTransform(rotation);
        }

        gm.setBounds(bounds);
        gm.setDataSource(layer1);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);
        printf("gm.makeGrid() done\n");

    }
  */
    /**
     * Pull the params into local variables
     *
     * @param params The parameters
     */
    private void pullParams(Map<String, Object> params) {
        String pname = null;

        if (DEBUG) {
            System.out.println("RectSidedPopperKernel Params: " + params);
        }
        try {
            pname = "resolution";
            resolution = ((Double) params.get(pname)).doubleValue();

            pname = "previewQuality";
            previewQuality = PreviewQuality.valueOf((String) params.get(pname));

            pname = "maxDecimationError";
            maxDecimationError = ((Double) params.get(pname)).doubleValue();

            pname = "sideHeight";
            sideHeight = ((Double) params.get(pname)).doubleValue();

            pname = "sideThickness";
            sideThickness = ((Double) params.get(pname)).doubleValue();

            pname = "radius";
            radius = ((Double) params.get(pname)).doubleValue();

            pname = "sideImage1";
            sideImage1 = (String) params.get(pname);

            pname = "sideImage2";
            sideImage2 = (String) params.get(pname);

            pname = "sideImage3";
            sideImage3 = (String) params.get(pname);

            pname = "sideImage4";
            sideImage4 = (String) params.get(pname);

            pname = "sideImage5";
            sideImage5 = (String) params.get(pname);

            pname = "sideImage6";
            sideImage6 = (String) params.get(pname);

            pname = "topImage";
            topImage = (String) params.get(pname);

            pname = "topLocation";
            topLocation = ((Double) params.get(pname)).doubleValue();

            pname = "topThickness";
            topThickness = ((Double) params.get(pname)).doubleValue();

            pname = "bottomImage";
            bottomImage = (String) params.get(pname);

            pname = "bottomLocation";
            bottomLocation = ((Double) params.get(pname)).doubleValue();

            pname = "bottomThickness";
            bottomThickness = ((Double) params.get(pname)).doubleValue();

            pname = "smoothSteps";
            smoothSteps = ((Integer) params.get(pname)).intValue();

            pname = "regions";
            regions = RegionPrunner.Regions.valueOf((String) params.get(pname));

            pname = "useGrayscale";
            useGrayscale = (Boolean) params.get(pname);

            pname = "visRemovedRegions";
            visRemovedRegions = (Boolean) params.get(pname);

            pname = "shape";
            shape = Shape.valueOf((String) params.get(pname));

            pname = "threads";
            threads = ((Integer) params.get(pname)).intValue();

            if (threads == 0) {
                int cores = Runtime.getRuntime().availableProcessors();

                threads = cores;

                // scales well to 4 threads, stop there.
                if (threads > 4) {
                    threads = 4;
                }

                System.out.println("Number of cores:" + threads);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error parsing: " + pname + " val: " + params.get(pname));
        }
    }

    /**
     * return bounds extended by given margin
     */
    static double[] extendBounds(double bounds[], double margin) {
        return new double[]{
                bounds[0] - margin,
                bounds[1] + margin,
                bounds[2] - margin,
                bounds[3] + margin,
                bounds[4] - margin,
                bounds[5] + margin,
        };
    }

    private void writeDebug(Grid grid, BinaryContentHandler handler, ErrorReporter console) {
        // Output File

        BoxesX3DExporter exporter = new BoxesX3DExporter(handler, console, true);

        HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
        colors.put(new Integer(Grid.INSIDE), new float[]{1, 0, 0});
        colors.put(new Integer(Grid.OUTSIDE), new float[]{0, 0, 1});

        HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
        transparency.put(new Integer(Grid.INSIDE), new Float(0));
        transparency.put(new Integer(Grid.OUTSIDE), new Float(0.98));

        exporter.writeDebug(grid, colors, transparency);
        exporter.close();
    }

    public static <T extends Enum<T>> String[] enumToStringArray(T[] values) {
        int i = 0;
        String[] result = new String[values.length];
        for (T value : values) {
            result[i++] = value.name();
        }
        return result;
    }

    public static void main(String[] args) {
        int loops = 1;

        for(int n=0; n < loops; n++) {
        int threads = 1;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        long stime = System.currentTimeMillis();

        for(int i=0; i < threads; i++) {
            HostedKernel kernel = new RectSidedPopperKernel();
          Runnable runner = new KernelRunner(kernel);
//            Runnable runner = new FakeRunner(kernel);
            executor.submit(runner);
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Total Runtime: " + (System.currentTimeMillis() - stime));
        }
    }
}

class KernelRunner implements Runnable {
    private HostedKernel kernel;

    public KernelRunner(HostedKernel kernel) {
        this.kernel = kernel;
    }

    @Override
    public void run() {
        HashMap<String,String> params = new HashMap<String,String>();
/*
        // params for garbage gen, originally 100 million objects
        params.put("bodyWidth1","0.1016");
        params.put("bodyHeight1","0.1016");
        params.put("bodyDepth1","0.012");

        params.put("regions","ALL");
        params.put("previewQuality","LOW");
        params.put("bodyImage","C:\\cygwin\\home\\giles\\projs\\abfab3d\\code\\trunk\\apps\\ringpopper\\images\\Tile_dilate8_unedged.png");
*/
        // params for regions test
        params.put("bodyWidth1","0.0216");
        params.put("bodyHeight1","0.0216");
        params.put("bodyDepth1","0.012");

        params.put("regions","ONE");
        params.put("previewQuality","LOW");
        params.put("visRemovedRegions","true");
        params.put("bodyImage","C:\\cygwin\\home\\giles\\projs\\abfab3d\\code\\trunk\\apps\\imagepopper\\images\\leaf\\5_cleaned.png");

        Map<String,Object> parsed_params = ParameterUtil.parseParams(kernel.getParams(), params);

        try {
            FileOutputStream fos = new FileOutputStream("/tmp/thread" + Thread.currentThread().getName() + ".x3d");
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            PlainTextErrorReporter console = new PlainTextErrorReporter();

            BinaryContentHandler writer = new X3DXMLRetainedExporter(bos,3,2,console);
            writer.startDocument("","","utf8","#X3D", "V3.2", "");
            writer.profileDecl("Immersive");


            kernel.generate(parsed_params, GeometryKernel.Accuracy.VISUAL, writer);

            writer.endDocument();
            bos.close();
            fos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}

class FakeRunner implements Runnable {
    private HostedKernel kernel;

    static class EdgeArray {

        Random m_rnd = new Random(101);

        public int nextInt() {
            return m_rnd.nextInt(10000);
        }
    }

    public FakeRunner(HostedKernel kernel) {
        this.kernel = kernel;
    }

    @Override
    public void run() {
        System.out.println("Running Fake");
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("bodyWidth1","0.1016");
        params.put("bodyHeight1","0.1016");
        params.put("bodyDepth1","0.03");
        params.put("regions","ALL");
        params.put("previewQuality","LOW");
        params.put("bodyImage","C:\\cygwin\\home\\giles\\projs\\abfab3d\\code\\trunk\\apps\\ringpopper\\images\\Tile_dilate8_unedged.png");

        Map<String,Object> parsed_params = ParameterUtil.parseParams(kernel.getParams(), params);

        try {
            FileOutputStream fos = new FileOutputStream("/tmp/thread" + Thread.currentThread().getName());
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            PlainTextErrorReporter console = new PlainTextErrorReporter();

            BinaryContentHandler writer = new X3DXMLRetainedExporter(bos,3,2,console);
            writer.startDocument("","","utf8","#X3D", "V3.2", "");
            writer.profileDecl("Immersive");


            //kernel.generate(parsed_params, GeometryKernel.Accuracy.VISUAL, writer);
            long TIMES = (long) 2e7;
            long tot = 0;

            EdgeArray m_edgeArray = new EdgeArray();

            System.out.println("Times: " + TIMES);
            for(int i=0; i < TIMES; i++) {
                tot += Math.ceil(Math.sqrt(i) * Math.sin(i) + Math.cos(i));
                tot -= Math.ceil(Math.sqrt(i) * Math.sin(i) + Math.cos(i) + Math.asin(i));

                tot += m_edgeArray.nextInt();
            }

            System.out.println("Total Count: " + tot);
            fos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}


