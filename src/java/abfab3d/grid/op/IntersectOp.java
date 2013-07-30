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

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.AttributeOperation;
import abfab3d.grid.Grid;
import abfab3d.grid.Operation;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;

// Internal Imports

/**
 * Intersection operation.
 * <p/>
 * Intersects two grids.  A voxel which is INSIDE in both
 * grids will be in the destination.
 *
 * @author Alan Hudson
 */
public class IntersectOp implements Operation, AttributeOperation {
    /**
     * The source grid, A
     */
    private Grid src;

    /**
     * The dest grid, B
     */
    private Grid dest;

    public IntersectOp(Grid src) {
        this.src = src;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The destination grid
     * @return The new grid
     */
    public Grid execute(Grid dest) {
        long t0 = time();

        this.dest = dest;

        int width = dest.getWidth();
        int depth = dest.getDepth();
        int height = dest.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    byte src_state = src.getState(x, y, z);
                    if (src_state == Grid.OUTSIDE) {
                        dest.setState(x, y, z, Grid.OUTSIDE);
                    }
                }
            }
        }

        printf("intersect: %d ms\n", (time() - t0));

        return dest;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use or null to create one
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid dest) {
        return (AttributeGrid) execute((Grid)dest);
    }
}
