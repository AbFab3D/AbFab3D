package abfab3d.grid.op;

import abfab3d.datasources.*;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Grid;
import abfab3d.grid.GridShortIntervals;
import abfab3d.io.input.STLReader;
import abfab3d.io.input.WaveletRasterizer;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.STLWriter;
import abfab3d.util.BoundingBoxCalculator;
import abfab3d.util.DataSource;
import abfab3d.util.MathUtil;

import javax.vecmath.Vector3d;
import java.io.IOException;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;
import static java.lang.Math.round;

/**
 * Test the DensityGridExtractor class
 *
 * @author Alan Hudson
 */
public class TestDensityGridExtractor extends BaseTestDistanceTransform {
    private static final boolean DEBUG = true;
    double surfaceThickness = Math.sqrt(3)/2;
    //int maxAttribute = 100;
    double voxelSize = 0.1*MM;

    public void testSphere(){

        int max_attribute = 100;
        int nx = 128;
        double sphereRadius = 5 * MM;
        AttributeGrid grid = makeSphere(nx, sphereRadius, voxelSize, max_attribute, surfaceThickness);


        MyGridWriter gw = new MyGridWriter(8,8);
        //if (DEBUG) gw.writeSlices(grid, maxAttribute, "/tmp/slices/sphere_%03d.png",0, nx/2, null);

        double[] bounds = new double[6];
        grid.getGridBounds(bounds);

        double maxInDistance = 1*MM + voxelSize;
        double maxOutDistance = voxelSize;

        long t0 = time();
        DistanceTransformExact dt_exact = new DistanceTransformExact(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_exact = dt_exact.execute(grid);
        printf("DistanceTransformExact done: %d ms\n", time() - t0);

        DensityGridExtractor dge = new DensityGridExtractor(1*MM, 0,dg_exact,maxInDistance,maxOutDistance, max_attribute);
        AttributeGrid subsurface = (AttributeGrid) grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getSliceHeight(), grid.getVoxelSize());
        subsurface.setGridBounds(bounds);

        subsurface = dge.execute(subsurface);

        AttributeGrid dest = (AttributeGrid) grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getSliceHeight(), grid.getVoxelSize());
        dest.setGridBounds(bounds);

        GridMaker gm = new GridMaker();
        gm.setMaxAttributeValue(max_attribute);

        gm.setSource(new DataSourceGrid(subsurface,max_attribute));
        gm.makeGrid(dest);

        if (DEBUG) gw.writeSlices(subsurface, max_attribute, "/tmp/slices/subsurface_%03d.png",0, nx/2, null);

        try {
            writeGrid(grid, "/tmp/sphere_orig.stl", max_attribute);
            writeGrid(dest, "/tmp/sphere_hollow.stl", max_attribute);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        printf("Original grid\n");
        printRow(grid, 0, nx, 62, 64, false, 50);
        printf("\n");

        printf("Distance grid\n");
        printRow(dg_exact, 0, nx, 62, 64, false, 50);
        printf("\n");



        printf("Hollow grid\n");
        printRow(subsurface, 0, nx, 62, nx / 2, false, 50);
    }

    public void testTorusBumpy(){

        int max_attribute = 127;
        int nx = 400;
        double sphereRadius = 17.0 * MM;
        AttributeGrid grid = makeTorus(nx, sphereRadius, 2 * MM, voxelSize, max_attribute, surfaceThickness);
        double[] bounds = new double[6];
        grid.getGridBounds(bounds);

        double maxInDistance = 0*MM;
        double maxOutDistance = 0.5*MM + voxelSize;

        long t0 = time();
        DistanceTransformExact dt_exact = new DistanceTransformExact(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_exact = dt_exact.execute(grid);
        printf("DistanceTransformExact done: %d ms\n", time() - t0);

        DensityGridExtractor dge = new DensityGridExtractor(0, maxOutDistance - voxelSize,dg_exact,maxInDistance,maxOutDistance, max_attribute);
        AttributeGrid supersurface = (AttributeGrid) grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getSliceHeight(), grid.getVoxelSize());
        supersurface.setGridBounds(bounds);

        supersurface = dge.execute(supersurface);

        AttributeGrid dest = (AttributeGrid) grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getSliceHeight(), grid.getVoxelSize());
        dest.setGridBounds(bounds);

        GridMaker gm = new GridMaker();
        gm.setMaxAttributeValue(max_attribute);

        DataSourceGrid dsg1 = new DataSourceGrid(grid,max_attribute);

        DataSourceGrid dsg2 = new DataSourceGrid(supersurface,max_attribute);
        DataSource gyroid = new VolumePatterns.Gyroid(3*MM,0.4*MM);
        Intersection pattern = new Intersection(dsg2, gyroid);

        Union result = new Union(dsg1,pattern);
        Subtraction subtract = new Subtraction(result, new Plane(new Vector3d(0,1,0), new Vector3d(0,0,0)));

        //gm.setSource(dsg2);
        gm.setSource(subtract);
//        gm.setSource(pattern);
        gm.makeGrid(dest);

        try {
            writeGrid(dest, "/tmp/bumpy_torus.stl", max_attribute);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void testTorusHollow(){

        int max_attribute = 127;
        int nx = 400;
        double sphereRadius = 17.0 * MM;
        AttributeGrid grid = makeTorus(nx, sphereRadius, 2 * MM, voxelSize, max_attribute, surfaceThickness);
        double[] bounds = new double[6];
        grid.getGridBounds(bounds);

        double maxInDistance = 2*MM;
        double maxOutDistance = 0*MM;

        long t0 = time();
        DistanceTransformExact dt_exact = new DistanceTransformExact(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_exact = dt_exact.execute(grid);
        printf("DistanceTransformExact done: %d ms\n", time() - t0);

        DensityGridExtractor dge = new DensityGridExtractor(2*MM, -1*MM,dg_exact,maxInDistance,maxOutDistance, max_attribute);
        AttributeGrid subsurface = (AttributeGrid) grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getSliceHeight(), grid.getVoxelSize());
        subsurface.setGridBounds(bounds);

        subsurface = dge.execute(subsurface);

        AttributeGrid dest = (AttributeGrid) grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getSliceHeight(), grid.getVoxelSize());
        dest.setGridBounds(bounds);

        GridMaker gm = new GridMaker();
        gm.setMaxAttributeValue(max_attribute);

        DataSourceGrid dsg1 = new DataSourceGrid(grid,max_attribute);

        DataSourceGrid dsg2 = new DataSourceGrid(subsurface,max_attribute);

        Subtraction result = new Subtraction(dsg1,dsg2);
        Subtraction subtract = new Subtraction(result, new Plane(new Vector3d(0,1,0), new Vector3d(0,0,0)));

        gm.setSource(subtract);
//        gm.setSource(pattern);
        gm.makeGrid(dest);

        try {
            writeGrid(dest, "/tmp/torus_hollow.stl", max_attribute);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void testDeerHollow(){

        int max_attribute = 127;
        int nx = 400;
        double sphereRadius = 17.0 * MM;
        AttributeGrid grid = loadSTL("test/models/Deer.stl",0.2*MM,max_attribute);
        double[] bounds = new double[6];
        grid.getGridBounds(bounds);

        double maxInDistance = 2*MM;
        double maxOutDistance = voxelSize;

        long t0 = time();
        DistanceTransformExact dt_exact = new DistanceTransformExact(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_exact = dt_exact.execute(grid);
        printf("DistanceTransformExact done: %d ms\n", time() - t0);

        DensityGridExtractor dge = new DensityGridExtractor(2*MM, voxelSize,dg_exact,maxInDistance,maxOutDistance, max_attribute);
        AttributeGrid subsurface = (AttributeGrid) grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getSliceHeight(), grid.getVoxelSize());
        subsurface.setGridBounds(bounds);

        subsurface = dge.execute(subsurface);

        AttributeGrid dest = (AttributeGrid) grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getSliceHeight(), grid.getVoxelSize());
        dest.setGridBounds(bounds);

        GridMaker gm = new GridMaker();
        gm.setMaxAttributeValue(max_attribute);

        DataSourceGrid dsg1 = new DataSourceGrid(grid,max_attribute);
        DataSourceGrid dsg2 = new DataSourceGrid(subsurface,max_attribute);

        Intersection result = new Intersection(dsg1,dsg2);
        Subtraction subtract = new Subtraction(result, new Plane(new Vector3d(0,1,0), new Vector3d(0,0.01,0)));

        //gm.setSource(new DataSourceGrid(subsurface,max_attribute));
        gm.setSource(subtract);
//        gm.setSource(pattern);
        gm.makeGrid(dest);

        try {
            writeGrid(dest, "/tmp/deer_hollow.stl", max_attribute);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    void writeGrid(Grid grid, String path, int gridMaxAttributeValue) throws IOException {

        MeshMakerMT mmaker = new MeshMakerMT();
        mmaker.setMaxAttributeValue(gridMaxAttributeValue);
        mmaker.setSmoothingWidth(0.5);
        mmaker.setBlockSize(50);
        mmaker.setMaxDecimationError(3.e-10);

        STLWriter stl = new STLWriter(path);
        mmaker.makeMesh(grid, stl);
        stl.close();

    }

    AttributeGrid loadSTL(String filename, double vs, int maxAttribute) {
        try {
            STLReader stl = new STLReader();
            BoundingBoxCalculator bb = new BoundingBoxCalculator();
            stl.read(filename, bb);

            double bounds[] = new double[6];
            bb.getBounds(bounds);

            // if any measurement is over 1M then the file "must" be in m instead of mm.  God I hate unspecified units
            if(false){
                // guessing units is bad and may be wrong
                double sx = bounds[1] - bounds[0];
                double sy = bounds[3] - bounds[2];
                double sz = bounds[5] - bounds[4];
                if (sx > 1 || sy > 1 | sz > 1) {
                    stl.setScale(1);

                    double factor = 1.0 / 1000;
                    for(int i=0; i < 6; i++) {
                        bounds[i] *= factor;
                    }
                }
            }
            //
            // round up to the nearest voxel
            //
            MathUtil.roundBounds(bounds, vs);
            // Add a 1 voxel margin around the model to get some space
            bounds = MathUtil.extendBounds(bounds, 1 * vs);
            int nx = (int) Math.round((bounds[1] - bounds[0]) / vs);
            int ny = (int) Math.round((bounds[3] - bounds[2]) / vs);
            int nz = (int) Math.round((bounds[5] - bounds[4]) / vs);
            printf("   grid bounds: [ %7.3f, %7.3f], [%7.3f, %7.3f], [%7.3f, %7.3f] mm; vs: %7.3f mm\n",
                    bounds[0]/MM, bounds[1]/MM, bounds[2]/MM, bounds[3]/MM, bounds[4]/MM, bounds[5]/MM, vs/MM);
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

            AttributeGrid dest = new ArrayAttributeGridByte(nx,ny,nz,vs,vs);
            dest.setGridBounds(bounds);

            WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, nx, ny, nz);
            rasterizer.setMaxAttributeValue(maxAttribute);

            stl.read(filename, rasterizer);

            rasterizer.getRaster(dest);

            System.out.println("Loaded: " + filename);

            return dest;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch(Throwable t) {
            t.printStackTrace();
        }

        return null;
    }

}
