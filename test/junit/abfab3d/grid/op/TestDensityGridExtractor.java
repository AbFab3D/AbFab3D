package abfab3d.grid.op;

import abfab3d.datasources.*;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Grid;
import abfab3d.grid.GridShortIntervals;
import abfab3d.io.input.STLRasterizer;
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

    public void _testTorusBumpy(){

        int max_attribute = 127;
        int nx = 400;
        double sphereRadius = 16.0 * MM;
        AttributeGrid grid = makeTorus(nx, sphereRadius, 2 * MM, voxelSize, max_attribute, surfaceThickness);
        double[] bounds = new double[6];
        grid.getGridBounds(bounds);

        double maxInDistance = 0*MM;
        double maxOutDistance = 2.1*MM + voxelSize;

        long t0 = time();
        DistanceTransformMultiStep dt_exact = new DistanceTransformMultiStep(max_attribute, maxInDistance, maxOutDistance);
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
//        DataSource gyroid = new VolumePatterns.Gyroid(3*MM,0.4*MM);
//        DataSource cubes = new VolumePatterns.CubicGrid(2*MM,0.4*MM);
        DataSource schwarz = new VolumePatterns.SchwarzPrimitive(2*MM,0.5*MM);
        Intersection pattern = new Intersection(dsg2, schwarz);

//        Union result = new Union(dsg1,pattern);
        Union result = new Union();
        result.add(pattern);
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

    public void _testDeerHollow(){

        int max_attribute = 127;
        int nx = 400;
        double sphereRadius = 17.0 * MM;
        AttributeGrid grid = loadSTL("test/models/Deer.stl",0.2*MM,max_attribute,0);
        double[] bounds = new double[6];
        grid.getGridBounds(bounds);

        double maxInDistance = 2*MM + voxelSize;
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

    public void _testDeerHollow2(){

        int max_attribute = 127;
        int nx = 400;
        double sphereRadius = 17.0 * MM;
        AttributeGrid grid = loadSTL("test/models/Deer.stl",0.2*MM,max_attribute,0);
        double[] bounds = new double[6];
        grid.getGridBounds(bounds);

        double maxInDistance = 16*MM + voxelSize;
        double maxOutDistance = voxelSize;

        long t0 = time();
        DistanceTransformMultiStep dt_exact = new DistanceTransformMultiStep(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_exact = dt_exact.execute(grid);
        printf("DistanceTransformExact done: %d ms\n", time() - t0);

        DensityGridExtractor dge = new DensityGridExtractor(16*MM, -1*MM,dg_exact,maxInDistance,maxOutDistance, max_attribute);
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
        Subtraction subtract = new Subtraction(result, new Plane(new Vector3d(0,1,0), new Vector3d(0,-0.025,0)));

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

    public void _testDilate(){

        int max_attribute = 255;
//        double distance = 25.4*MM / 2;
        double distance = 30*MM / 2;
        double voxelSize = 0.4*MM;
        AttributeGrid grid = loadSTL("test/models/holes.stl",voxelSize,max_attribute,(int) (distance / voxelSize) + 2);
        double[] bounds = new double[6];
        grid.getGridBounds(bounds);

        double maxInDistance = voxelSize;
        double maxOutDistance = distance;

        long t0 = time();
        DistanceTransformMultiStep dt_exact = new DistanceTransformMultiStep(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_exact = dt_exact.execute(grid);
        printf("DistanceTransformExact done: %d ms\n", time() - t0);

        DensityGridExtractor dge = new DensityGridExtractor(0*MM,distance,dg_exact,maxInDistance,maxOutDistance, max_attribute);
        AttributeGrid subsurface = (AttributeGrid) grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getSliceHeight(), grid.getVoxelSize());
        subsurface.setGridBounds(bounds);

        subsurface = dge.execute(subsurface);

        AttributeGrid dest = (AttributeGrid) grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getSliceHeight(), grid.getVoxelSize());
        dest.setGridBounds(bounds);

        GridMaker gm = new GridMaker();
        gm.setMaxAttributeValue(max_attribute);

        DataSourceGrid dsg1 = new DataSourceGrid(grid,max_attribute);
        DataSourceGrid dsg2 = new DataSourceGrid(subsurface,max_attribute);

        Union result = new Union(dsg1,dsg2);

        gm.setSource(result);
        gm.makeGrid(dest);

        try {
            writeGrid(dest, "/tmp/holes_dilate.stl", max_attribute);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void _testDilate2(){

        int max_attribute = 1;
//        double distance = 25.4*MM / 2;
        double distance = 25.4*MM;

        printf("Old distance: %f",distance);
        // correct for cube distance
        distance = distance * 1.5 / 1.732;
        printf("New distance: %f",distance);

        double voxelSize = 0.3*MM;
        STLRasterizer rasterizer = new STLRasterizer();
        rasterizer.setPadding((int) (distance / voxelSize) + 2);
        rasterizer.setVoxelSize(voxelSize);
        Grid grid = null;
        try {
            grid = (Grid) rasterizer.rasterizeFile("test/models/holes.stl");
//            grid = (Grid) rasterizer.rasterizeFile("test/models/sphere_30mm.stl");
            //writeGrid(grid, "/tmp/holes_orig.stl", 1);
        } catch(IOException ioe) {}


        printf("Old distance: %f\n",distance);
        // correct for cube distance
        distance = distance * 1.5 / 1.732;
        printf("New distance: %f\n",distance);

        int dv = (int) ((distance / voxelSize));
        /*
        DilationCube ds = new DilationCube(dv / 2);
        grid = ds.execute(grid);
        */

        DilationShapeMT dsn = new DilationShapeMT();
//        dsn.setVoxelShape(VoxelShapeFactory.getBall(dv / 2,0,0));
        dsn.setVoxelShape(VoxelShapeFactory.getCube(dv / 2));
        dsn.setThreadCount(8);
        grid = dsn.execute(grid);

        try {
            writeGrid(grid, "/tmp/holes_dilate2.stl", 1);
//            writeGrid(grid, "/tmp/sphere_dilate2.stl", 1);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void _testErode2(){

        double distance = 25.4*MM;
        double voxelSize = 0.3*MM;
        STLRasterizer rasterizer = new STLRasterizer();
        rasterizer.setPadding(2);
        rasterizer.setVoxelSize(voxelSize);
        Grid grid = null;
        try {
            grid = (Grid) rasterizer.rasterizeFile("/tmp/holes_dilate2.stl");
//            grid = (Grid) rasterizer.rasterizeFile("/tmp/sphere_dilate2.stl");
        } catch(IOException ioe) {}


        printf("Old distance: %f",distance);
        // correct for cube distance
        distance = distance * 1.5 / 1.732 * 0.825;   // TODO:  fudge factor why needed
        printf("New distance: %f",distance);
        int dv = (int) ((distance / voxelSize));
        //ErosionCube ds = new DilationCube(dv / 2);
        //grid = ds.execute(grid);


        ErosionShapeMT dsn = new ErosionShapeMT();
//        dsn.setVoxelShape(VoxelShapeFactory.getBall(dv / 4,0,0));
        dsn.setThreadCount(8);
        dsn.setVoxelShape(VoxelShapeFactory.getCube(dv / 2));

        grid = dsn.execute(grid);


        try {
            writeGrid(grid, "/tmp/holes_erode2.stl", 1);
//            writeGrid(grid, "/tmp/sphere_erode2.stl", 1);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void _testClosing2(){

        double distance = 25.4*MM;

        printf("Old distance: %f",distance);
        // correct for cube distance
        distance = distance * 1.5 / 1.732;
        printf("New distance: %f",distance);
        int dv = (int) ((distance / voxelSize));

        double voxelSize = 0.3*MM;
        STLRasterizer rasterizer = new STLRasterizer();
        rasterizer.setPadding((int) (distance / voxelSize) + 2);
        rasterizer.setVoxelSize(voxelSize);
        Grid grid = null;
        try {
            grid = (Grid) rasterizer.rasterizeFile("test/models/holes.stl");
//            grid = (Grid) rasterizer.rasterizeFile("test/models/sphere_30mm.stl");
            //writeGrid(grid, "/tmp/holes_orig.stl", 1);
        } catch(IOException ioe) {}


        DilationShapeMT dsn = new DilationShapeMT();
//        dsn.setVoxelShape(VoxelShapeFactory.getBall(dv / 2,0,0));
        dsn.setVoxelShape(VoxelShapeFactory.getCube(dv / 2));
        dsn.setThreadCount(8);
        grid = dsn.execute(grid);

        // TODO: horrible fudge factor no idea why
        distance = distance * 0.825;
        dv = (int) ((distance / voxelSize));

        ErosionShapeMT esn = new ErosionShapeMT();
//        dsn.setVoxelShape(VoxelShapeFactory.getBall(dv / 4,0,0));
        esn.setThreadCount(8);
        esn.setVoxelShape(VoxelShapeFactory.getCube(dv / 2));

        grid = esn.execute(grid);


        try {
            writeGrid(grid, "/tmp/holes_closing2.stl", 1);
//            writeGrid(grid, "/tmp/sphere_erode2.stl", 1);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

    }

    public void _testShapes() {
        printf("Cube\n");
        VoxelShape cube = VoxelShapeFactory.getCube(1);
        int[] coords = cube.getCoords();

        for(int i=0; i < coords.length / 3; i++) {
            printf("%d %d %d\n",coords[i*3],coords[i*3+1],coords[i*3+2]);
        }

        printf("Sphere\n");
        VoxelShape sphere = VoxelShapeFactory.getBall(1,0,0);
        coords = sphere.getCoords();

        for(int i=0; i < coords.length / 3; i++) {
            printf("%d %d %d\n",coords[i*3],coords[i*3+1],coords[i*3+2]);
        }


    }
    public void _testErode(){

        int max_attribute = 255;
        double distance = 25.4*MM / 2;
        double voxelSize = 0.3*MM;
//        AttributeGrid grid = loadSTL("test/models/Deer.stl",0.2*MM,max_attribute,0);
        AttributeGrid grid = loadSTL("/tmp/holes_dilate.stl",voxelSize,max_attribute,0);
        double[] bounds = new double[6];
        grid.getGridBounds(bounds);

        double maxInDistance = distance + voxelSize;
        double maxOutDistance = voxelSize;

        long t0 = time();
        DistanceTransformMultiStep dt_exact = new DistanceTransformMultiStep(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_exact = dt_exact.execute(grid);
        printf("DistanceTransformExact done: %d ms\n", time() - t0);

        DensityGridExtractor dge = new DensityGridExtractor(-distance, 0*MM,dg_exact,maxInDistance,maxOutDistance, max_attribute);
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

        gm.setSource(result);
        gm.makeGrid(dest);

        try {
            writeGrid(dest, "/tmp/holes_erode.stl", max_attribute);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }


    public void _testClosing(){

        int max_attribute = 127;
        double distance = 25.4*MM / 2;
        double voxelSize = 0.2 * MM;
        AttributeGrid grid = loadSTL("test/models/holes.stl",voxelSize,max_attribute,(int) (2 * distance / voxelSize) + 2);
        double[] bounds = new double[6];
        grid.getGridBounds(bounds);

        double maxInDistance = voxelSize;
        double maxOutDistance = distance;

        long t0 = time();
        DistanceTransformMultiStep dt_exact = new DistanceTransformMultiStep(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_exact = dt_exact.execute(grid);
        printf("DistanceTransformExact done: %d ms\n", time() - t0);

        DensityGridExtractor dge = new DensityGridExtractor(0*MM, distance,dg_exact,maxInDistance,maxOutDistance, max_attribute);
        AttributeGrid dilate_surface = (AttributeGrid) grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getSliceHeight(), grid.getVoxelSize());
        dilate_surface.setGridBounds(bounds);

        dilate_surface = dge.execute(dilate_surface);

        AttributeGrid dilate_dest = (AttributeGrid) grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getSliceHeight(), grid.getVoxelSize());
        dilate_dest.setGridBounds(bounds);

        GridMaker gm = new GridMaker();
        gm.setMaxAttributeValue(max_attribute);

        DataSourceGrid dsg1 = new DataSourceGrid(grid,max_attribute);
        DataSourceGrid dsg2 = new DataSourceGrid(dilate_surface,max_attribute);

        Union result = new Union(dsg1,dsg2);

        gm.setSource(result);
        gm.makeGrid(dilate_dest);

        try {
            writeGrid(dilate_dest, "/tmp/holes_dilate.stl", max_attribute);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        // Erode back down
        maxInDistance = distance + voxelSize;
        maxOutDistance = voxelSize;

        t0 = time();
        dt_exact = new DistanceTransformMultiStep(max_attribute, maxInDistance, maxOutDistance);
        dg_exact = dt_exact.execute(dilate_dest);
        printf("DistanceTransformExact done: %d ms\n", time() - t0);

        dge = new DensityGridExtractor(-distance, voxelSize,dg_exact,maxInDistance,maxOutDistance, max_attribute);
        AttributeGrid erode_surface = (AttributeGrid) grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getSliceHeight(), grid.getVoxelSize());
        erode_surface.setGridBounds(bounds);

        erode_surface = dge.execute(erode_surface);

        try {
            writeGrid(erode_surface, "/tmp/holes_erode.stl", max_attribute);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        AttributeGrid erode_dest = (AttributeGrid) grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getSliceHeight(), grid.getVoxelSize());
        erode_dest.setGridBounds(bounds);

        gm = new GridMaker();
        gm.setMaxAttributeValue(max_attribute);

        dsg1 = new DataSourceGrid(dilate_dest,max_attribute);
        dsg2 = new DataSourceGrid(erode_surface,max_attribute);

        Subtraction erode_result = new Subtraction(dsg1,dsg2);

        gm.setSource(erode_result);
        gm.makeGrid(erode_dest);

        try {
            writeGrid(erode_dest, "/tmp/holes_closed.stl", max_attribute);
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

    AttributeGrid loadSTL(String filename, double vs, int maxAttribute, int margin) {
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
            bounds = MathUtil.extendBounds(bounds, margin * vs);
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

    public static void main(String arg[]){
//        new TestDensityGridExtractor()._testShapes();
        new TestDensityGridExtractor()._testErode2();

        //new TestDensityGridExtractor().testTorusBumpy();
        //new TestDensityGridExtractor().testDeerHollow();
    }
}
