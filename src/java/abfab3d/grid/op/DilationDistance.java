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

package abfab3d.grid.op;

// External Imports

// Internal Imports

import abfab3d.datasources.DataSourceGrid;
import abfab3d.datasources.Subtraction;
import abfab3d.datasources.Union;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.AttributeOperation;
import abfab3d.grid.Grid;
import abfab3d.grid.Operation;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;

/**
 * Dilate an object based on using a DistanceTransform.
 *
 * Alternate idea, calculate distance transform from -voxelSize to distance
 *
 * This version is density aware.
 *
 * @author Alan Hudson
 */
public class DilationDistance implements Operation, AttributeOperation {
    private static final boolean DEBUG = false;

    /** The dilation distance in meters */
    private double distance;
    private int subvoxelResolution;

    public DilationDistance(double distance, int subvoxelResolution) {
        this.distance = distance;
        this.subvoxelResolution = subvoxelResolution;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid A.
     * @return The new grid
     */
    public Grid execute(Grid dest) {
        throw new IllegalArgumentException("Not implemented.");
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for dest
     * @return The new grid
     */
    public AttributeGrid executeOld(AttributeGrid dest) {

        // Nothing to do if distance is 0
        if (distance <= 0) {
            return dest;
        }

        int height = dest.getHeight();
        int width = dest.getWidth();
        int depth = dest.getDepth();

        // Calculate DistanceTransform

        double maxInDistance = dest.getVoxelSize();
        double maxOutDistance = distance + dest.getVoxelSize();

        long t0 = time();

        // TODO:  allow user specified DistanceTransform class
        DistanceTransformMultiStep dt = new DistanceTransformMultiStep(subvoxelResolution, maxInDistance, maxOutDistance);
        AttributeGrid dg = dt.execute(dest);

        // All current surface voxels should be interior now
        int nx = dest.getWidth();
        int ny = dest.getHeight();
        int nz = dest.getDepth();
/*
        for(int y=0; y < ny; y++) {
            for(int x=0; x < nx; x++) {
                for(int z=0; z < nz; z++) {
                    if (dest.getAttribute(x,y,z) > 0) {
                        dest.setAttribute(x,y,z,subvoxelResolution);
                    }
                }
            }
        }
*/
        // Dilate grid by adding surface voxels

        double vs = dg.getVoxelSize();

        nx = dg.getWidth();
        ny = dg.getHeight();
        nz = dg.getDepth();

        // 5 intervals for distance values

        int inDistanceMinus = (int) (-maxInDistance * subvoxelResolution / vs - subvoxelResolution / 2);
        int inDistancePlus = (int) (-maxInDistance * subvoxelResolution / vs + subvoxelResolution / 2);
        int outDistanceMinus = (int) (maxOutDistance * subvoxelResolution / vs - subvoxelResolution / 2);
        int outDistancePlus = (int) (maxOutDistance * subvoxelResolution / vs + subvoxelResolution / 2);

        // TODO: remove me
        long in_cnt = 0;

        for(int y=0; y < ny; y++) {
            for(int x=0; x < nx; x++) {
                for(int z=0; z < nz; z++) {
                    long att = (long) (short) dg.getAttribute(x,y,z);

                    short dest_att;

                    if (att < inDistanceMinus) {
                        // ignore
                        //dest.setState(x,y,z,Grid.OUTSIDE);
                    } else if (att >= inDistanceMinus && att < inDistancePlus) {
                        // ignore
                        //dest_att = (short) (att - inDistanceMinus);
                        //dest.setData(x,y,z, Grid.INSIDE,dest_att);
                    } else if (att >= inDistancePlus && att < outDistanceMinus /*|| att == -Short.MAX_VALUE*/) {
                        dest_att = (short) subvoxelResolution;
                        dest.setData(x,y,z, Grid.INSIDE,dest_att);
                        in_cnt++;
                    } else if (att >= outDistanceMinus && att <= outDistancePlus) {
                        dest_att = (short) (outDistancePlus - att);
                        dest.setData(x,y,z, Grid.INSIDE,dest_att);
                        in_cnt++;
                    } else {
                       // dest.setState(x,y,z,Grid.OUTSIDE);
                    }
                }
            }
        }

        printf("in cnt: %d\n",in_cnt);

        return dest;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for dest
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid dest) {
        printf("Using new code\n");

        // Nothing to do if distance is 0
        if (distance <= 0) {
            return dest;
        }

        // Calculate DistanceTransform

        long t0 = time();

        // TODO:  allow user specified DistanceTransform class

        double maxInDistance = dest.getVoxelSize();
        double maxOutDistance = distance + dest.getVoxelSize();

        DistanceTransformMultiStep dt_exact = new DistanceTransformMultiStep(subvoxelResolution, maxInDistance, maxOutDistance);
        AttributeGrid dg = dt_exact.execute(dest);
        printf("DistanceTransformMultiStep done: %d ms\n", time() - t0);

        double[] bounds = new double[6];
        dest.getGridBounds(bounds);

        DensityGridExtractor dge = new DensityGridExtractor(0*MM, distance,dg,maxInDistance,maxOutDistance, subvoxelResolution);
        AttributeGrid subsurface = (AttributeGrid) dest.createEmpty(dest.getWidth(), dest.getHeight(), dest.getDepth(), dest.getVoxelSize(), dest.getSliceHeight());
        subsurface.setGridBounds(bounds);

        subsurface = dge.execute(subsurface);

        printf("Done extracting subsurface");
        AttributeGrid new_dest = (AttributeGrid) dest.createEmpty(dest.getWidth(), dest.getHeight(), dest.getDepth(), dest.getVoxelSize(), dest.getSliceHeight());
        new_dest.setGridBounds(bounds);

        GridMaker gm = new GridMaker();
        gm.setMaxAttributeValue(subvoxelResolution);

        DataSourceGrid dsg1 = new DataSourceGrid(dest,subvoxelResolution);
        DataSourceGrid dsg2 = new DataSourceGrid(subsurface,subvoxelResolution);

        Union result = new Union(dsg1,dsg2);

        gm.setSource(result);
        gm.makeGrid(new_dest);

        printf("Done making grid\n");
        return new_dest;
    }

}
