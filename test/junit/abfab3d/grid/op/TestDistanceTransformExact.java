package abfab3d.grid.op;

import abfab3d.grid.AttributeGrid;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;
import static java.lang.Math.round;

/**
 * Test the DistanceTransformExact class.
 *
 * @author Alan Hudson
 */
public class TestDistanceTransformExact extends BaseTestDistanceTransform {
    private static final boolean DEBUG = true;
    double surfaceThickness = Math.sqrt(3)/2;
    int maxAttribute = 100;
    double voxelSize = 0.1*MM;

    public void testBoxInside(){

        int max_attribute = 100;
        int nx = 128;
        double boxWidth = 2.0 * MM;
        AttributeGrid grid = makeBox(nx, boxWidth, voxelSize, max_attribute, surfaceThickness);
        double maxInDistance = 1.2*MM;
        double maxOutDistance = 0;

        MyGridWriter gw = new MyGridWriter(8,8);
        if (DEBUG) gw.writeSlices(grid, maxAttribute, "/tmp/slices/box_%03d.png",nx/2, nx/2+1, new DistanceColorizer(max_attribute,0,0,255));

        long t0 = time();
        DistanceTransformExact dt_exact = new DistanceTransformExact(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_exact = dt_exact.execute(grid);
        printf("DistanceTransformExact done: %d ms\n", time() - t0);

        int norm = (int)round(maxAttribute*maxInDistance/voxelSize);

        if (DEBUG) {
            gw.writeSlices(dg_exact,norm , "/tmp/slices/distance/exact_%03d.png",0, nx/2, new DistanceColorizer(norm,0,0,255));
            for(int i=0; i < 27; i++) {
                printRow(dg_exact, 50, 90, i, nx / 2, false, 50);
                printf("\n");
            }
        }

        // check that the distance never exceeds half the box size
        int max = (int)(Math.ceil(boxWidth/2.0*max_attribute/grid.getVoxelSize() + 0.5));

        printf("Max att is: %d",max);
        long not_calced_inside = dt_exact.getInsideDefault();
        long not_calced_outside = dt_exact.getOutsideDefault();

        checkMaxValue(max, not_calced_inside, not_calced_outside, dg_exact);
    }

    public void testBoxOutside(){

        int max_attribute = 100;
        int nx = 128;
        double boxWidth = 2.0 * MM;
        AttributeGrid grid = makeBox(nx, boxWidth, voxelSize, max_attribute, surfaceThickness);
        double maxInDistance = 0*MM;
        double maxOutDistance = 0.5 * MM;

        MyGridWriter gw = new MyGridWriter(8,8);
        if (DEBUG) gw.writeSlices(grid, maxAttribute, "/tmp/slices/box_%03d.png",nx/2, nx/2+1, new DistanceColorizer(max_attribute,255,0,0));

        long t0 = time();
        DistanceTransformExact dt_exact = new DistanceTransformExact(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_exact = dt_exact.execute(grid);
        printf("DistanceTransformExact done: %d ms\n", time() - t0);

        int norm = (int)round(maxAttribute*maxOutDistance/voxelSize);

        if (DEBUG) {
            gw.writeSlices(dg_exact,norm , "/tmp/slices/distance/exact_%03d.png",0, 17, new DistanceColorizer(norm,255,0,0));
            for(int i=0; i < 18; i++) {
                printRow(dg_exact, 50, 90, i, 15, false, 50);
                printf("\n");
            }
        }

        // check that the distance never exceeds half the box size
        int min = -(int)(Math.ceil(boxWidth/2.0*max_attribute/grid.getVoxelSize() + 0.5));

        long not_calced_inside = dt_exact.getInsideDefault();
        long not_calced_outside = dt_exact.getOutsideDefault();

        checkMinValue(min, not_calced_inside, not_calced_outside, dg_exact);
    }

    public void testBoxBoth(){

        int max_attribute = 100;
        int nx = 128;
        double boxWidth = 2.0 * MM;
        AttributeGrid grid = makeBox(nx, boxWidth, voxelSize, max_attribute, surfaceThickness);
//        AttributeGrid grid = makeSphere(nx, 4.0 * MM, voxelSize, max_attribute, surfaceThickness);
//        AttributeGrid grid = makeTorus(nx, 4.0 * MM, 2.0 * MM, voxelSize, max_attribute, surfaceThickness);
        double maxInDistance = 0.5*MM;
        double maxOutDistance = 0.5*MM;

        int max_grid_att = (int) (Math.ceil(max_attribute * (maxInDistance + maxOutDistance) / voxelSize / 2.0));
        MyGridWriter gw = new MyGridWriter(8,8);
        DistanceColorizer colorizer =new DistanceColorizer(max_grid_att,0,0,0);
        if (DEBUG) gw.writeSlices(grid, maxAttribute, "/tmp/slices/box_%03d.png",nx/2, nx/2+1, colorizer);

        long t0 = time();
        DistanceTransformExact dt_exact = new DistanceTransformExact(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_exact = dt_exact.execute(grid);
        printf("DistanceTransformExact done: %d ms\n", time() - t0);

        int norm = (int)round(maxAttribute*maxOutDistance/voxelSize);

        if (DEBUG) {
            gw.writeSlices(dg_exact,norm , "/tmp/slices/distance/exact_%03d.png",0, 27, colorizer);
            for(int i=70; i < nx; i++) {
                printRow(dg_exact, 50, 90, i, 20, false, 50);
                printf("\n");
            }
        }

        // check that the distance never exceeds half the box size
        int min = -(int)(Math.ceil(boxWidth/2.0*max_attribute/grid.getVoxelSize() + 0.5));

        long not_calced_inside = dt_exact.getInsideDefault();
        long not_calced_outside = dt_exact.getOutsideDefault();

        checkMinValue(min, not_calced_inside, not_calced_outside, dg_exact);
        checkLowToHighToLow(nx/2, nx/2, nx/2, not_calced_inside, not_calced_outside, dg_exact);
    }

    public void testSphereBoth(){

        int max_attribute = 100;
        int nx = 128;
        double sphereRadius = 5.0 * MM;
        AttributeGrid grid = makeSphere(nx, sphereRadius, voxelSize, max_attribute, surfaceThickness);
        double maxInDistance = 0.5*MM;
        double maxOutDistance = 0.5*MM;

        int max_grid_att = (int) (Math.ceil(max_attribute * (maxInDistance + maxOutDistance) / voxelSize / 2.0));
        MyGridWriter gw = new MyGridWriter(8,8);
        DistanceColorizer colorizer =new DistanceColorizer(max_grid_att,0,0,0);
        gw.writeSlices(grid, maxAttribute, "/tmp/slices/sphere_%03d.png",nx/2, nx/2+1, colorizer);
//        gw.writeSlices(grid, maxAttribute, "/tmp/slices/box_%03d.png",0, nx, new DistanceColorizer(max_attribute));

        long t0 = time();
        DistanceTransformExact dt_exact = new DistanceTransformExact(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_exact = dt_exact.execute(grid);
        printf("DistanceTransformExact done: %d ms\n", time() - t0);

        int norm = (int)round(maxAttribute*maxOutDistance/voxelSize);

        gw.writeSlices(dg_exact,norm , "/tmp/slices/distance/exact_%03d.png",0, 27, colorizer);
        for(int i=nx / 2 - 20; i < nx / 2 + 20; i++) {
            printRow(dg_exact, 50, 90, i, 20, false, 50);
            printf("\n");
        }
        // check that the distance never exceeds half the box size
        int min = -(int)(Math.ceil(sphereRadius*max_attribute/grid.getVoxelSize() + 0.5));

        long not_calced_inside = dt_exact.getInsideDefault();
        long not_calced_outside = dt_exact.getOutsideDefault();

        checkMinValue(min, not_calced_inside, not_calced_outside, dg_exact);
        checkLowToHighToLow(nx/2, nx/2, nx/2, not_calced_inside, not_calced_outside, dg_exact);
    }

    public void testTorusBoth(){

        int max_attribute = 100;
        int nx = 128;
        double sphereRadius = 5.0 * MM;
        AttributeGrid grid = makeTorus(nx, sphereRadius, 1*MM, voxelSize, max_attribute, surfaceThickness);
        double maxInDistance = 2*MM;
        double maxOutDistance = 0*MM;

        int max_grid_att = (int) (Math.ceil(max_attribute * (maxInDistance + maxOutDistance) / voxelSize / 2.0));
        MyGridWriter gw = new MyGridWriter(8,8);
        DistanceColorizer colorizer =new DistanceColorizer(max_grid_att,0,0,0);
        gw.writeSlices(grid, maxAttribute, "/tmp/slices/torus_%03d.png",nx/2, nx/2+1, colorizer);
//        gw.writeSlices(grid, maxAttribute, "/tmp/slices/box_%03d.png",0, nx, new DistanceColorizer(max_attribute));

        long t0 = time();
        DistanceTransformExact dt_exact = new DistanceTransformExact(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_exact = dt_exact.execute(grid);
        printf("DistanceTransformExact done: %d ms\n", time() - t0);

        int norm = (int)round(maxAttribute*(maxInDistance + maxOutDistance)/voxelSize);

        gw.writeSlices(dg_exact,norm , "/tmp/slices/distance/exact_%03d.png",0, 128, colorizer);
        for(int i=50; i < 78; i++) {
            printRow(dg_exact, 0, 40, i, 55, false, 50);
            printf("\n");
        }
        // check that the distance never exceeds half the box size
        int min = -(int)(Math.ceil(sphereRadius*max_attribute/grid.getVoxelSize() + 0.5));

        long not_calced_inside = dt_exact.getInsideDefault();
        long not_calced_outside = dt_exact.getOutsideDefault();

        checkMinValue(min, not_calced_inside, not_calced_outside, dg_exact);
        //checkLowToHighToLow(nx/2, nx/2, nx/2, not_calced_inside, not_calced_outside, dg_exact);
    }

    public void _testGyroidBoth(){

        int max_attribute = 100;
        int nx = 384;
        double sphereRadius = 5.0 * MM;
        AttributeGrid grid = makeGyroid(nx, sphereRadius, voxelSize, max_attribute, surfaceThickness, nx * voxelSize / 3.0, 0.1);
        double maxInDistance = 0.5*MM;
        double maxOutDistance = 0.5*MM;

        int max_grid_att = (int) (Math.ceil(max_attribute * (maxInDistance + maxOutDistance) / voxelSize / 2.0));
        MyGridWriter gw = new MyGridWriter(1,1,2);
        DistanceColorizer colorizer =new DistanceColorizer(max_grid_att,0,0,0);
        gw.writeSlices(grid, maxAttribute, "/tmp/slices/gyroid_exact_%03d.png",0, nx/2+1, colorizer);
//        gw.writeSlices(grid, maxAttribute, "/tmp/slices/box_%03d.png",0, nx, new DistanceColorizer(max_attribute));

        long t0 = time();
        DistanceTransformExact dt_exact = new DistanceTransformExact(max_attribute, maxInDistance, maxOutDistance);
//        DistanceTransformFM dt_exact = new DistanceTransformFM(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_exact = dt_exact.execute(grid);
        printf("DistanceTransformExact done: %d ms\n", time() - t0);

        int norm = (int)round(maxAttribute*maxOutDistance/voxelSize);

        gw.writeSlices(dg_exact,norm , "/tmp/slices/distance/exact_%03d.png",0, nx, colorizer);
        /*
        for(int i=nx / 2 - 20; i < nx / 2 + 20; i++) {
            printRow(dg_exact, 50, 90, i, 20, false, 50);
            printf("\n");
        }
        */
        // check that the distance never exceeds half the box size
        int min = -(int)(Math.ceil(sphereRadius*max_attribute/grid.getVoxelSize() + 0.5));

        long not_calced_inside = dt_exact.getInsideDefault();
        long not_calced_outside = dt_exact.getOutsideDefault();

        checkMinValue(min, not_calced_inside, not_calced_outside, dg_exact);
    }

}
