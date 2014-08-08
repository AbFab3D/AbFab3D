/** 
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

package abfab3d.grid.op;

import abfab3d.grid.AttributeGrid;

import static java.lang.Math.round;
import static java.lang.Math.abs;

import static java.lang.Math.min;
import static java.lang.Math.max;
import static java.lang.Math.abs;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;

import static abfab3d.util.MathUtil.L2S;

/**
 * Test the DistanceTransformLayered class.
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov

 */
public class TestDistanceTransformLayered extends BaseTestDistanceTransform {

    private static final boolean DEBUG = false;
    private static final boolean DEBUG_TIMING = true;
    private static final boolean DEBUG_SLICES = false;

    double surfaceThickness = Math.sqrt(3)/2;
    int subvoxelResolution = 100;
    double voxelSize = 0.1*MM;

    public void testAccuracy(){

        int nx = 100;
 
        int test_margin_factor = 2;

        AttributeGrid[] grids = new AttributeGrid[1];
        //grids[0] = makeBox(nx, 5.0 * MM, voxelSize, subvoxelResolution, surfaceThickness);
        //grids[0] = makeSphere(nx, 2.5 * MM, voxelSize, subvoxelResolution, surfaceThickness);
        //grids[0] = makeTorus(nx, 2.7 * MM, 2.2 * MM, voxelSize, subvoxelResolution, surfaceThickness);
        grids[0] = makeGyroid(nx, 4.0 * MM, voxelSize, subvoxelResolution, 10*MM, nx * voxelSize / 2.0, 0.1);

        double maxInDistance = 2*MM;
        double maxOutDistance = 0.*MM;

        for(int i=0; i < grids.length; i++) {

            AttributeGrid grid = grids[i];

            int ny = grid.getHeight();
            int nz = grid.getDepth();

            MyGridWriter gw = new MyGridWriter(8,8);

            if (DEBUG_SLICES) gw.writeSlices(grid, subvoxelResolution, "/tmp/slices/grid_%03d.png",nx/2, nx/2+1, new DensityColorizer(subvoxelResolution));

            int dist_norm  = (int)(subvoxelResolution * maxInDistance/voxelSize);


            long t0 = time();
            DistanceTransformLayered dt_fm = new DistanceTransformLayered(subvoxelResolution, maxInDistance, maxOutDistance);

            AttributeGrid dg_fm = dt_fm.execute(grid);
            printf("DistanceTransformLayered done: %d ms\n", time() - t0);
            if (DEBUG_SLICES) gw.writeSlices(dg_fm, dist_norm , "/tmp/slices/layered/dist_layered_%03d.png", 0, nx,new DistanceColorizer(dist_norm));
            if (false) printSlice(dg_fm, 0, nx/2, 0, nx/2, nx/2);

            if(false)return;

            t0 = time();
            DistanceTransformExact dt_exact = new DistanceTransformExact(subvoxelResolution, maxInDistance, maxOutDistance);
            AttributeGrid dg_exact = dt_exact.execute(grid);
            printf("DistanceTransformExact done: %d ms\n", time() - t0);
            if (DEBUG_SLICES) gw.writeSlices(dg_exact, dist_norm , "/tmp/slices/exact/dist_exact_%03d.png", 0, nx,new DistanceColorizer(dist_norm));


            long errors[] = getDiffHistogram( dg_exact, dg_fm);
            printDiffHistogram(errors);

        }
    }


    public void testMT(){

        int nx = 200;
        int WARMUP = 2;
        //int test_margin_factor = 2;

        AttributeGrid[] grids = new AttributeGrid[1];
        grids[0] = makeSphere(nx, nx*voxelSize/2, voxelSize, subvoxelResolution, surfaceThickness);

        //double maxInDistance = 2*MM;
        double maxInDistance = 2*MM;
        double maxOutDistance = 0.*MM;
        long st_time = 0;
        long mt_time = 0;
        int max_threads = 12;

        for(int n=0; n < WARMUP; n++) {
            for(int i=0; i < grids.length; i++) {

                AttributeGrid grid = grids[i];

                int ny = grid.getHeight();
                int nz = grid.getDepth();

                if(DEBUG_TIMING)printf("processing grid: [%d x %d X %d]\n",nx, ny, nz);
                MyGridWriter gw = new MyGridWriter(8,8);

                if (DEBUG_SLICES) gw.writeSlices(grid, subvoxelResolution, "/tmp/slices/grid_%03d.png",nx/2, nx/2+1, new DensityColorizer(subvoxelResolution));

                int dist_norm  = (int)(subvoxelResolution * maxInDistance/voxelSize);

                long t0 = time();
                if(false){
                    DistanceTransformExact dt_exact = new DistanceTransformExact(subvoxelResolution, maxInDistance, maxOutDistance);
                    AttributeGrid dg_exact = dt_exact.execute(grid);
                    if(DEBUG_TIMING)printf("DistanceTransformExact done: %d ms\n", time() - t0);
                    //if (DEBUG_SLICES) gw.writeSlices(dg_exact, dist_norm , "/tmp/slices/exact/dist_exact_%03d.png", 0, nx,new DistanceColorizer(dist_norm));
                }

                t0 = time();
                DistanceTransformLayered dt_st = new DistanceTransformLayered(subvoxelResolution, maxInDistance, maxOutDistance);
                dt_st.setThreadCount(1);
                AttributeGrid dg_st = dt_st.execute(grid);
                st_time = (time() - t0);
                if(DEBUG_TIMING)printf("DistanceTransformLayered ST done: %d ms\n", st_time);


                DistanceTransformLayered dt_mt = new DistanceTransformLayered(subvoxelResolution, maxInDistance, maxOutDistance);
                dt_mt.setThreadCount(max_threads);
                t0 = time();
                AttributeGrid dg_mt = dt_mt.execute(grid);
                mt_time = time() - t0;
                printf("DistanceTransformLayered MT done: %d ms  multi: %4.2f\n", mt_time,((float)st_time / mt_time));

                if (DEBUG_SLICES) gw.writeSlices(dg_mt, dist_norm , "/tmp/slices/layered/dist_layered_%03d.png", 0, nx,new DistanceColorizer(dist_norm));
                if (false) printSlice(dg_mt, 0, nx/2, 0, nx/2, nx/2);

                if(false)return;


                long errors[] = getDiffHistogram( dg_st, dg_mt);
                //printDiffHistogram(errors);
            }
        }

        for(int i=0; i < grids.length; i++) {

            AttributeGrid grid = grids[i];

            int ny = grid.getHeight();
            int nz = grid.getDepth();

            if(DEBUG_TIMING)printf("processing grid: [%d x %d X %d]\n",nx, ny, nz);
            MyGridWriter gw = new MyGridWriter(8,8);

            if (DEBUG_SLICES) gw.writeSlices(grid, subvoxelResolution, "/tmp/slices/grid_%03d.png",nx/2, nx/2+1, new DensityColorizer(subvoxelResolution));

            int dist_norm  = (int)(subvoxelResolution * maxInDistance/voxelSize);

            long t0 = time();
            if(false){
                DistanceTransformExact dt_exact = new DistanceTransformExact(subvoxelResolution, maxInDistance, maxOutDistance);
                AttributeGrid dg_exact = dt_exact.execute(grid);
                if(DEBUG_TIMING)printf("DistanceTransformExact done: %d ms\n", time() - t0);
                //if (DEBUG_SLICES) gw.writeSlices(dg_exact, dist_norm , "/tmp/slices/exact/dist_exact_%03d.png", 0, nx,new DistanceColorizer(dist_norm));
            }

            t0 = time();
            DistanceTransformLayered dt_st = new DistanceTransformLayered(subvoxelResolution, maxInDistance, maxOutDistance);
            dt_st.setThreadCount(1);
            AttributeGrid dg_st = dt_st.execute(grid);
            st_time = (time() - t0);
            if(DEBUG_TIMING)printf("DistanceTransformLayered ST done: %d ms\n", st_time);

            DistanceTransformLayered dt_mt = new DistanceTransformLayered(subvoxelResolution, maxInDistance, maxOutDistance);
            dt_mt.setThreadCount(max_threads);
            t0 = time();
            AttributeGrid dg_mt = dt_mt.execute(grid);
            mt_time = time() - t0;
            printf("DistanceTransformLayered MT done: %d ms  multi: %4.2f\n", mt_time,((float)st_time / mt_time));

            if (DEBUG_SLICES) gw.writeSlices(dg_mt, dist_norm , "/tmp/slices/layered/dist_layered_%03d.png", 0, nx,new DistanceColorizer(dist_norm));
            if (false) printSlice(dg_mt, 0, nx/2, 0, nx/2, nx/2);

            if(false)return;


            long errors[] = getDiffHistogram( dg_st, dg_mt);
            //printDiffHistogram(errors);
        }
    }


    public static void main(String arg[]){

        //new TestDistanceTransformLayered().testAccuracy();
        new TestDistanceTransformLayered().testMT();
                    

    }
    
}
