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

import java.io.File;


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

    private static final boolean DEBUG = true;
    private static final boolean DEBUG_TIMING = true;
    private static final boolean DEBUG_SLICES = false;

    double surfaceThickness = Math.sqrt(3)/2;
    static int subvoxelResolution = 100;
    double voxelSize = 0.1*MM;

    public void testAccuracy(){

        int nx = 50;
 
        int test_margin_factor = 2;

        AttributeGrid[] grids = new AttributeGrid[1];
        //grids[0] = makeBox(nx, 5.0 * MM, voxelSize, subvoxelResolution, surfaceThickness);
        grids[0] = makeSphere(nx, 2.5 * MM, voxelSize, subvoxelResolution, surfaceThickness);
        //grids[0] = makeTorus(nx, 2.7 * MM, 2.2 * MM, voxelSize, subvoxelResolution, surfaceThickness);
        //grids[0] = makeGyroid(nx, 4.0 * MM, voxelSize, subvoxelResolution, 10*MM, nx * voxelSize / 2.0, 0.1);

        double maxInDistance = 1*MM;
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
            for(int k = 9; k < errors.length-1; k++){
                assertTrue(fmt("error[%d] = %d (but should be 0)\n", k, errors[k]), (errors[k] == 0));
            }
        }
    }


    public void testMT(){

        printf("%s.testMT()\n",getClass().getSimpleName());
        
        String 
            dir_ex = "/tmp/slices/exact",
            dir_st = "/tmp/slices/layered_st/",
            dir_mt = "/tmp/slices/layered_mt/",
            dir_orig = "/tmp/slices/orig/",
            dir_diff = "/tmp/slices/diff/";

        if(DEBUG_SLICES){
            String dirs[] = new String[]{dir_orig, dir_ex, dir_mt, dir_st, dir_diff};
            for(int i = 0; i < dirs.length; i++)
                    new File(dirs[i]).mkdirs();
        }

        int nx = 50;
        //int test_margin_factor = 2;

        AttributeGrid[] grids = new AttributeGrid[1];
        grids[0] = makeSphere(nx, 0.9*nx*voxelSize/2, voxelSize, subvoxelResolution, surfaceThickness);

        //double maxInDistance = 2*MM;
        double maxInDistance = 2*MM;
        double maxOutDistance = 0.*MM;
        long st_time = 0;
        long mt_time = 0;
        int max_threads = 4;


        for(int i=0; i < grids.length; i++) {

            AttributeGrid grid = grids[i];

            int ny = grid.getHeight();
            int nz = grid.getDepth();

            if(DEBUG_TIMING)printf("processing grid: [%d x %d X %d]\n",nx, ny, nz);
            MyGridWriter gw = new MyGridWriter(8,8);

            if (DEBUG_SLICES) gw.writeSlices(grid, subvoxelResolution, dir_orig + "dens_orig%03d.png",0, nx, new DensityColorizer(subvoxelResolution));

            int dist_norm  = (int)(subvoxelResolution * maxInDistance/voxelSize);

            long t0 = time();

            if(false){
                DistanceTransformExact dt_exact = new DistanceTransformExact(subvoxelResolution, maxInDistance, maxOutDistance);
                AttributeGrid dg_exact = dt_exact.execute(grid);
                if(DEBUG_TIMING)printf("DistanceTransformExact done: %d ms\n", time() - t0);
                if (DEBUG_SLICES) gw.writeSlices(dg_exact, dist_norm , dir_ex + "dist_ex%03d.png", 0, nx,new DistanceColorizer(dist_norm));
                if (DEBUG_SLICES) printf("EXACT\n");
                if (DEBUG_SLICES) printSlice(dg_exact, 10, nx-10, 10, nx-10, 3);
            }


            t0 = time();
            DistanceTransformLayered dt_st = new DistanceTransformLayered(subvoxelResolution, maxInDistance, maxOutDistance);
            dt_st.setThreadCount(1);
            AttributeGrid dg_st = dt_st.execute(grid);
            st_time = (time() - t0);
            if(DEBUG_TIMING)printf("DistanceTransformLayered ST done: %d ms\n", st_time);

            if (DEBUG_SLICES) gw.writeSlices(dg_st, dist_norm , dir_st+"dist_st%03d.png", 0, nx,new DistanceColorizer(dist_norm));
            
            DistanceTransformLayered dt_mt = new DistanceTransformLayered(subvoxelResolution, maxInDistance, maxOutDistance);
            dt_mt.setThreadCount(max_threads);
            t0 = time();
            AttributeGrid dg_mt = dt_mt.execute(grid);
            mt_time = time() - t0;
            printf("DistanceTransformLayered MT done: %d ms  multi: %4.2f\n", mt_time,((float)st_time / mt_time));
            if (DEBUG_SLICES) gw.writeSlices(dg_mt, dist_norm , dir_mt+"dist_mt%03d.png", 0, nx,new DistanceColorizer(dist_norm));
            if (false) printSlice(dg_mt, 0, nx/2, 0, nx/2, nx/2);

            AttributeGrid dg_diff = (AttributeGrid)dg_mt.clone();
            subtract(dg_diff, dg_st);
            if (DEBUG_SLICES) gw.writeSlices(dg_diff, subvoxelResolution , dir_diff + "dist_diff%03d.png", 0, nx,new DensityColorizer(subvoxelResolution));
            if (DEBUG_SLICES) printf("DIFF\n");
            if (DEBUG_SLICES) printSlice(dg_diff, 10, nx-10, 10, nx-10, 3);
            if (DEBUG_SLICES) printf("ST\n");
            if (DEBUG_SLICES) printSlice(dg_st, 10, nx-10, 10, nx-10, 3);
            if (DEBUG_SLICES) printf("MT\n");
            if (DEBUG_SLICES) printSlice(dg_mt, 10, nx-10, 10, nx-10, 3);

            
            if(false)return;

            
            long errors[] = getDiffHistogram( dg_st, dg_mt);
            printDiffHistogram(errors);
            for(int k = 8; k < errors.length-1; k++){
                assertTrue(fmt("error[%d] = %d (but should be 0)\n", k, errors[k]), (errors[k] == 0));
            }
        }
    }

    static void subtract(AttributeGrid g1, AttributeGrid g2){
        int nx = g1.getWidth();
        int ny = g1.getHeight();
        int nz = g1.getDepth();
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    long a1 = g1.getAttribute(x,y,z);
                    long a2 = g2.getAttribute(x,y,z);
                    long d = Math.abs(a1-a2);
                    //d  = Math.min(d,subvoxelResolution);
                    g1.setAttribute(x,y,z,d);                    
                }
            }            
        }
    }

    public static void main(String arg[]){

        //new TestDistanceTransformLayered().dtestAccuracy();
        new TestDistanceTransformLayered().testMT();
                    

    }
    
}
