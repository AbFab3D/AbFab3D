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
import abfab3d.util.AbFab3DGlobals;

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
    public static final int
            DISTANCE_TRANSFORM_EXACT = 0,
            DISTANCE_TRANSFORM_MULTI_STEP = 1,
            DISTANCE_TRANSFORM_FAST_MARCHING = 2,
            DISTANCE_TRANSFORM_LAYERED = 3;

    /** The dilation distance in meters */
    private double distance;
    private int subvoxelResolution;
    private int threadCount = 1;
    int m_distanceTransformAlgorithm = DISTANCE_TRANSFORM_LAYERED;
    // template to be used for distance grid creation
    protected AttributeGrid m_distanceGridTemplate;

    public DilationDistance(double distance, int subvoxelResolution) {
        this.distance = distance;
        this.subvoxelResolution = subvoxelResolution;
    }

    /**
      * Set template to be used for distance grid creation
     */
    public void setDistanceGridTemplate(AttributeGrid gridTemplate){
        m_distanceGridTemplate = gridTemplate;
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
        // Nothing to do if distance is 0
        if (distance <= 0) {
            return dest;
        }

        // Calculate DistanceTransform

        long t0 = time();

        double maxInDistance = dest.getVoxelSize();
        double maxOutDistance = distance + dest.getVoxelSize();

        printf("Dilation Distance  in: %5.2f mm  out: %5.2f mm\n",maxInDistance / MM, maxOutDistance / MM);
        DistanceTransformLayered dt = new DistanceTransformLayered(subvoxelResolution, maxInDistance, maxOutDistance);
        dt.setDistanceGridTemplate(m_distanceGridTemplate);
        dt.setThreadCount(threadCount);
        AttributeGrid dg = dt.execute(dest);
        printf("Dilation Distance done: %d ms  threads: %d\n", time() - t0,threadCount);

        double[] bounds = new double[6];
        dest.getGridBounds(bounds);

        DensityGridExtractor dge = new DensityGridExtractor(-maxInDistance * 2, distance,dg,-maxInDistance,maxOutDistance, subvoxelResolution);

        //AttributeGrid new_dest = (AttributeGrid) dest.createEmpty(dest.getWidth(), dest.getHeight(), dest.getDepth(), dest.getVoxelSize(), dest.getSliceHeight());
        //new_dest.setGridBounds(bounds);

        return dge.execute(dest);
    }

    public void setThreadCount(int count){
        if (count < 1) {
            count = Runtime.getRuntime().availableProcessors();
        }

        int max_threads = ((Number) AbFab3DGlobals.get(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY)).intValue();
        if (count > max_threads)
            count = max_threads;

        threadCount = count;
    }

    /**
     * Set the distance transform algorithm to use.
     *
     * @param distanceTransformAlgorithm Possible values: DISTANCE_TRANSFORM_EXACT, DISTANCE_TRANSFORM_MULTI_STEP,
     *      DISTANCE_TRANSFORM_FAST_MARCHING, DISTANCE_TRANSFORM_LAYERED
     */
    public void setDistanceTransformAlgorithm(int distanceTransformAlgorithm){

        m_distanceTransformAlgorithm = distanceTransformAlgorithm;
    }
}
