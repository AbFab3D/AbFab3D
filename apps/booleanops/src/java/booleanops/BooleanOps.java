/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package booleanops;

// External Imports

import abfab3d.geom.MeshVoxelizer;
import abfab3d.grid.ArrayGridByte;
import abfab3d.core.Grid;
import abfab3d.grid.GridBitIntervals;
import abfab3d.grid.Operation;
import abfab3d.grid.op.SubtractOpMT;
import abfab3d.io.output.BoxesX3DExporter;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.SAVExporter;
import abfab3d.mesh.AreaCalculator;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import abfab3d.util.BoundingBoxUtilsFloat;
import abfab3d.io.output.GridSaver;
import org.j3d.geom.BoxGenerator;
import org.j3d.geom.CylinderGenerator;
import org.j3d.geom.GeometryData;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;

// Internal Imports


/**
 * Example of boolean operations.  Voxels may not be the best way to
 * evaluate this but it's nice its so easy to code.
 * <p/>
 * This example creates a cube and then subtracts 3 rotated cylinders from it.
 *
 * @author Alan Hudson
 */
public class BooleanOps {
    private long maxRamUsage = 1200 * 1024 * 1000l;

    private static final boolean DEBUG = false;

    /**
     * Resolution of the printer in meters.
     */
//    public static final double RESOLUTION = 0.00015;
    public static final double RESOLUTION = 0.00015;

    public void generate(String filename) throws IOException {
        long start = System.currentTimeMillis();

        ErrorReporter console = new PlainTextErrorReporter();

        float bsize = 0.0254f;

        int threads = Runtime.getRuntime().availableProcessors();
        BoxGenerator tg = new BoxGenerator(bsize, bsize, bsize);
        GeometryData geom = new GeometryData();
        geom.geometryType = GeometryData.TRIANGLES;
        tg.generate(geom);

        double[] trans = new double[3];
        double[] maxsize = new double[3];

        BoundingBoxUtilsFloat bc = new BoundingBoxUtilsFloat();

        findGridParams(geom, RESOLUTION, RESOLUTION, trans, maxsize);

        // account for larger cylinder size
        maxsize[0] *= 1.2;
        maxsize[1] *= 1.2;
        maxsize[2] *= 1.2;

        Grid grid = getGrid(maxsize[0], maxsize[1], maxsize[2], RESOLUTION, RESOLUTION, maxRamUsage);

        System.out.println("Grid size: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());
        MeshVoxelizer tmc2 = null;

        double rx = 0, ry = 1, rz = 0, rangle = 0;

        float[] minmax = new float[6];
        bc.computeMinMax(geom.coordinates, geom.coordinates.length / 3, minmax);

        double
                min_x = minmax[0],
                max_x = minmax[1],
                min_y = minmax[2],
                max_y = minmax[3],
                min_z = minmax[4],
                max_z = minmax[5];

        int padding = 1;
        // transformation from model coordinates to grid coordinates
        //  sx * xmin + tx = pad;
        //  sx * xmax + tx = nx - pad;
        //  sx*(xmax-xmin) = nx-2*pad;
        double
                sx = ((max_x - min_x) / grid.getVoxelSize() - 2 * padding) / (max_x - min_x), // scale to transform from model space into grid space
                sy = ((max_y - min_y) / grid.getSliceHeight() - 2 * padding) / (max_y - min_y),
                sz = ((max_z - min_z) / grid.getVoxelSize() - 2 * padding) / (max_z - min_z),
                tx = padding - sx * min_x,
                ty = padding - sy * min_y,
                tz = padding - sz * min_z - 0.5;

        tmc2 = new MeshVoxelizer(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getVoxelSize(),
                grid.getSliceHeight(), tx, ty, tz);
/*
        tmc2 = new TriangleModelCreatorMT(grid.getWidth(), grid.getHeight(), grid.getDepth(),grid.getVoxelSize(),
                grid.getSliceHeight(),tx,ty,tz);

        ((TriangleModelCreatorMT)tmc2).setThreadCount(threads);
  */
        tmc2.rasterize(geom, grid);

        System.out.println("Generated Box: " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();

        double height = bsize * 1.1;
        double radius = bsize / 2.5f;
        int facets = 64;

        CylinderGenerator cg = new CylinderGenerator((float) height, (float) radius, facets);
        geom = new GeometryData();
        geom.geometryType = GeometryData.TRIANGLES;
        cg.generate(geom);

        Grid grid2 = getGrid(maxsize[0], maxsize[1], maxsize[2], RESOLUTION, RESOLUTION, maxRamUsage);

        tmc2 = new MeshVoxelizer(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getVoxelSize(),
                grid.getSliceHeight(), tx, ty, tz);
        tmc2.rasterize(geom, grid2);

        System.out.println("Generated Cylinder1: " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();


        Operation op = new SubtractOpMT(grid2,threads);

//        Operation op = new Union(grid2, 0, 0, 0, 1);
        grid = op.execute(grid);
        System.out.println("Subtract Cylinder1: " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();


        rx = 1;
        ry = 0;
        rz = 0;
        rangle = 1.57075;

        grid2 = getGrid(maxsize[0], maxsize[1], maxsize[2], RESOLUTION, RESOLUTION, maxRamUsage);

        tmc2 = new MeshVoxelizer(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getVoxelSize(),
                grid.getSliceHeight(), tx, ty, tz, rx, ry, rz, rangle);
        tmc2.rasterize(geom, grid2);


        System.out.println("Generated Cylinder2: " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();

        op = new SubtractOpMT(grid2,threads);
//        op = new Union(grid2, 0, 0, 0, 1);
        grid = op.execute(grid);
        System.out.println("Subtract Cylinder2: " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();

        rx = 0;
        ry = 0;
        rz = 1;
        rangle = 1.57075;

        grid2 = getGrid(maxsize[0], maxsize[1], maxsize[2], RESOLUTION, RESOLUTION, maxRamUsage);

        tmc2 = new MeshVoxelizer(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getVoxelSize(),
                grid.getSliceHeight(), tx, ty, tz, rx, ry, rz, rangle);
        tmc2.rasterize(geom, grid2);

        System.out.println("Generated Cylinder3: " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();

        op = new SubtractOpMT(grid2,threads);
//        op = new Union(grid2, 0, 0, 0, 1);
        grid = op.execute(grid);
        System.out.println("Subtract Cylinder2: " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();


        grid2 = null;  // Free this to save memory before export

        boolean oldway = false;
        if (DEBUG || oldway) {
            if (DEBUG) {
                try {
                    FileOutputStream fos = new FileOutputStream(filename);
                    String encoding = filename.substring(filename.lastIndexOf(".") + 1);
                    BoxesX3DExporter exporter = new BoxesX3DExporter(encoding, fos, console);

                    HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
                    colors.put(new Integer(Grid.INSIDE), new float[]{0, 1, 0});
                    colors.put(new Integer(Grid.OUTSIDE), new float[]{0, 0, 1});

                    HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
                    transparency.put(new Integer(Grid.INSIDE), new Float(0));
                    //transparency.put(new Integer(Grid.OUTSIDE), new Float(0.98));
                    transparency.put(new Integer(Grid.OUTSIDE), new Float(0.9));


                    exporter.writeDebug(grid, colors, transparency);
                    exporter.close();

                    fos.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            } else {
                try {
                    FileOutputStream fos = new FileOutputStream(filename);
                    String encoding = filename.substring(filename.lastIndexOf(".") + 1);
                    BoxesX3DExporter exporter = new BoxesX3DExporter(encoding, fos, console);

                    exporter.write(grid, null);
                    exporter.close();

                    fos.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

            }
            return;
        }

        int min_volume = 10;
        int regions_removed = 0;

        System.out.println("Writing grid");


        double maxDecimationError = 0.1 * RESOLUTION * RESOLUTION;
        int smoothSteps = 0;

        boolean USE_MESH_MAKER_MT = true;
        // HARD CODED params to play with
        // width of Gaussian smoothing of grid, may be 0. - no smoothing
        double smoothingWidth = 0;
        // size of grid block for MT calculatins
        // (larger values reduce processor cache performance)
        int blockSize = 50;
        // max number to use for surface transitions. Should be ODD number
        // set it to 0 to have binary grid
        int maxGridAttributeValue = 0;
        // width of surface transition area relative to voxel size
        // optimal value sqrt(3)/2. Larger value causes rounding of sharp edges
        // sreyt it to 0. to make no surface transitions
        double surfaceTransitionWidth = Math.sqrt(3) / 2; // 0.866

        HashMap<String, Object> exp_params = new HashMap<String, Object>();
        exp_params.put(SAVExporter.EXPORT_NORMALS, false);   // Required now for ITS?
        exp_params.put(SAVExporter.GEOMETRY_TYPE, SAVExporter.GeometryType.INDEXEDTRIANGLESET);

        long t0;

        WingedEdgeTriangleMesh mesh;

        double gbounds[] = new double[6];
        grid.getGridBounds(gbounds);

        // place of default viewpoint
        double viewDistance = GridSaver.getViewDistance(grid);

        if (USE_MESH_MAKER_MT) {

            MeshMakerMT meshmaker = new MeshMakerMT();

            t0 = time();
            meshmaker.setBlockSize(blockSize);
            meshmaker.setThreadCount(threads);
            meshmaker.setSmoothingWidth(smoothingWidth);
            meshmaker.setMaxDecimationError(maxDecimationError);
            meshmaker.setMaxAttributeValue(maxGridAttributeValue);

            // TODO: Need to get a better way to estimate this number
            IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
            meshmaker.makeMesh(grid, its);

            // Release meshmaker and grid to lower total memory requirements
            meshmaker = null;
            grid = null;

            mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

            printf("MeshMakerMT.makeMesh(): %d ms\n", (time() - t0));

            // extra decimation to get rid of seams
            if (maxDecimationError > 0) {
                t0 = time();
                mesh = GridSaver.decimateMesh(mesh, maxDecimationError);
                printf("final decimation: %d ms\n", (time() - t0));
            }

        } else {
            mesh = GridSaver.createIsosurface(grid, smoothSteps);
            // Release grid to lower total memory requirements
            grid = null;
            if (maxDecimationError > 0)
                mesh = GridSaver.decimateMesh(mesh, maxDecimationError);
        }

        GridSaver.writeMesh(mesh, filename);

        AreaCalculator ac = new AreaCalculator();
        mesh.getTriangles(ac);
        double volume = ac.getVolume();
        double surface_area = ac.getArea();

        // Do not shorten the accuracy of these prints they need to be high
        printf("final surface area: %7.8f cm^2\n", surface_area * 1.e4);
        printf("final volume: %7.8f cm^3\n", volume * 1.e6);

        printf("Total time: %d ms\n", (time() - start));
        printf("-------------------------------------------------\n");
    }


    /**
     * Find the params needed to place a model in the grid.
     *
     * @param geom  The geometry
     * @param horiz The horiz voxel size
     * @param vert  The vertical voxel size
     * @param trans The translation to use.  Preallocate to 3.
     * @param size  The minimum size the grid needs to be
     */
    private void findGridParams(GeometryData geom, double horiz, double vert, double[] trans, double[] size) {
        double[] min = new double[3];
        double[] max = new double[3];

        min[0] = Double.POSITIVE_INFINITY;
        min[1] = Double.POSITIVE_INFINITY;
        min[2] = Double.POSITIVE_INFINITY;
        max[0] = Double.NEGATIVE_INFINITY;
        max[1] = Double.NEGATIVE_INFINITY;
        max[2] = Double.NEGATIVE_INFINITY;

        int len = geom.coordinates.length / 3;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            if (geom.coordinates[idx] > max[0]) {
                max[0] = geom.coordinates[idx];
            }
            if (geom.coordinates[idx] < min[0]) {
                min[0] = geom.coordinates[idx];
            }

            idx++;

            if (geom.coordinates[idx] > max[1]) {
                max[1] = geom.coordinates[idx];
            }

            if (geom.coordinates[idx] < min[1]) {
                min[1] = geom.coordinates[idx];
            }

            idx++;

            if (geom.coordinates[idx] > max[2]) {
                max[2] = geom.coordinates[idx];
            }

            if (geom.coordinates[idx] < min[2]) {
                min[2] = geom.coordinates[idx];
            }

            idx++;
        }

        // Leave one ring of voxels around the item

        int numVoxels = 1;

        size[0] = (max[0] - min[0]) + (numVoxels * 2 * horiz);
        size[1] = (max[1] - min[1]) + (numVoxels * 2 * vert);
        size[2] = (max[2] - min[2]) + (numVoxels * 2 * horiz);

        trans[0] = -min[0] + numVoxels * horiz;
        trans[1] = -min[1] + numVoxels * vert;
        trans[2] = -min[2] + numVoxels * horiz;
    }

    /**
     * return bounds from indexed data
     */
    static double[] getBounds(double minmax[], float coords[], int coordIndex[]) {

        if (minmax == null) {
            minmax = new double[]{
                    Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
            };
        }

        double cx, cy, cz;

        int len = coordIndex.length;
        // gets max and min bounds
        for (int i = 0; i < len; i++) {
            int index = 3 * coordIndex[i];
            // get coords
            cx = coords[index++];
            cy = coords[index++];
            cz = coords[index];

            // gets max and min bounds
            if (cx < minmax[0])
                minmax[0] = cx;
            if (cx > minmax[1])
                minmax[1] = cx;

            if (cy < minmax[2])
                minmax[2] = cy;
            if (cy > minmax[3])
                minmax[3] = cy;

            if (cz < minmax[4])
                minmax[4] = cz;
            if (cz > minmax[5])
                minmax[5] = cz;

        }
        return minmax;
    }

    /**
     * return bounds from indexed data
     */
    static double[] getBounds(double minmax[], float coords[]) {

        if (minmax == null) {
            minmax = new double[]{
                    Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
            };
        }

        double cx, cy, cz;

        int len = coords.length / 3;
        // gets max and min bounds
        for (int i = 0; i < len; i++) {
            int index = 3 * i;
            // get coords
            cx = coords[index++];
            cy = coords[index++];
            cz = coords[index];

            // gets max and min bounds
            if (cx < minmax[0])
                minmax[0] = cx;
            if (cx > minmax[1])
                minmax[1] = cx;

            if (cy < minmax[2])
                minmax[2] = cy;
            if (cy > minmax[3])
                minmax[3] = cy;

            if (cz < minmax[4])
                minmax[4] = cz;
            if (cz > minmax[5])
                minmax[5] = cz;

        }
        return minmax;
    }

    private Grid getGrid(double width, double height, double depth, double pixelSize, double sliceHeight, long maxRam) {
/*
        if(1==1) {
            System.out.println("Hardcoding to bit intervals");
            return new GridBitIntervals(width, height, depth, GridBitIntervals.ORIENTATION_Z, RESOLUTION, RESOLUTION);
        }
*/
/*
        if(1==1) {
            System.out.println("Hardcoding to array grid");
            return new ArrayGridByte(width, height, depth, RESOLUTION, RESOLUTION);
        }
*/
        long voxels = (long) (width / pixelSize * height / sliceHeight * depth / pixelSize);

        // assume ArrayGridByte uses 1 byte per voxel.

        if (voxels > maxRam) {
            int w = (int) (width/pixelSize) + 1;
            int h = (int) (height/sliceHeight) + 1;
            int d = (int) (depth/pixelSize) + 1;
            return new GridBitIntervals(w, h, d, GridBitIntervals.ORIENTATION_Z, RESOLUTION, RESOLUTION);
        } else {
            return new ArrayGridByte(width, height, depth, RESOLUTION, RESOLUTION);
        }
    }

    public static void main(String[] args) {
        BooleanOps c = new BooleanOps();

        try {
            c.generate("out.x3db");
/*
            c.generate("out.x3db");
            c.generate("out.x3db");
            c.generate("out.x3db");
            */
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}