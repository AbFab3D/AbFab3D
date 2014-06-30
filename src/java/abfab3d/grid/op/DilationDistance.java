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

//        DistanceTransformMultiStep dt_exact = new DistanceTransformMultiStep(subvoxelResolution, maxInDistance, maxOutDistance);
        DistanceTransformExact dt_exact = new DistanceTransformExact(subvoxelResolution, maxInDistance, maxOutDistance);
        AttributeGrid dg = dt_exact.execute(dest);
        printf("DistanceTransformMultiStep done: %d ms\n", time() - t0);

        double[] bounds = new double[6];
        dest.getGridBounds(bounds);

        DensityGridExtractor dge = new DensityGridExtractor(-maxInDistance * 2, distance,dg,-maxInDistance,maxOutDistance, subvoxelResolution);
        AttributeGrid subsurface = (AttributeGrid) dest.createEmpty(dest.getWidth(), dest.getHeight(), dest.getDepth(), dest.getVoxelSize(), dest.getSliceHeight());
        subsurface.setGridBounds(bounds);

        AttributeGrid new_dest = (AttributeGrid) dest.createEmpty(dest.getWidth(), dest.getHeight(), dest.getDepth(), dest.getVoxelSize(), dest.getSliceHeight());
        new_dest.setGridBounds(bounds);

        new_dest = dge.execute(new_dest);

        return new_dest;
    }

}
