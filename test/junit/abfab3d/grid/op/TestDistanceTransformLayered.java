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
    private static final boolean DEBUG_SLICES = false;

    double surfaceThickness = Math.sqrt(3)/2;
    int subvoxelResolution = 100;
    double voxelSize = 0.1*MM;

    public void testAccuracy(){

        int nx = 50;
 
        int test_margin_factor = 2;

        AttributeGrid[] grids = new AttributeGrid[1];
        //grids[0] = makeBox(nx, 4.0 * MM, voxelSize, subvoxelResolution, surfaceThickness);
        grids[0] = makeSphere(nx, 2.5 * MM, voxelSize, subvoxelResolution, surfaceThickness);
        //grids[2] = makeTorus(nx, 4.0 * MM, 2.0 * MM, voxelSize, subvoxelResolution, surfaceThickness);
        //grids[3] = makeGyroid(nx, 4.0 * MM, voxelSize, max_attribute, subvoxelResolution, nx * voxelSize / 3.0, 0.1);

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
            //DistanceTransformLayered dt_fm = new DistanceTransformLayered(subvoxelResolution, maxInDistance, maxOutDistance);
            DistanceTransformMultiStep dt_fm = new DistanceTransformMultiStep(subvoxelResolution, maxInDistance, maxOutDistance);

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


            int diffs_in_exact = 0;
            int diffs_in_fm = 0;
            
            long defaultInside = dt_exact.getInsideDefault();
            long defaultOutside = dt_exact.getOutsideDefault();

            long total_error = 0;
            long max_error = 0;
            long err_cnt = 0;

            long ma = (long) ((double) subvoxelResolution * maxInDistance / voxelSize);
            
            long errors[] = getDiffHistogram( dg_exact, dg_fm);
            printf("err cnt\n");
            for(int k = 0; k < errors.length; k++){
                if(errors[k] != 0)
                    printf("%3d %3d\n",k, errors[k]);
            }
            // viz:  difference grayscale.  blue for not in exact but in compare, red for in exact but not in compare

            //if (DEBUG_SLICES) gw.writeSlices(diff_grid,dist_norm, "/tmp/slices/diff/layered_diff_%03d.png",0, nx/2, new ErrorColorizer(dist_norm));

        }
    }

    public static void main(String arg[]){
        
        new TestDistanceTransformLayered().testAccuracy();

    }
    
}
