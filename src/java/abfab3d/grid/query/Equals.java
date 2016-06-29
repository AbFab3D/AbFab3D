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

package abfab3d.grid.query;

// External Imports

// Internal Imports

import abfab3d.core.Grid;
import abfab3d.core.VoxelData;

/**
 * Determines whether 2 grids are equal.  Checks each voxel and makes sure
 * that all state and material values are the same.
 *
 * @author Alan Hudson
 */
public class Equals {
    /** The grid we are using */
    private Grid gridB;

    /** The reason */
    private String reason;

    public Equals(Grid b) {
        gridB = b;
    }

    /**
     * Can the specified material move along the path
     * to exit the voxel space.  Any intersection with
     * another materialID will cause failure.
     *
     * @param grid The grid to use for grid src
     * @return true if it can move to an exit.
     */
    public boolean execute(Grid grid) {
        int width = grid.getWidth();
        int depth = grid.getDepth();
        int height = grid.getHeight();

        if (gridB.getWidth() != width ||
            gridB.getHeight() != height ||
            gridB.getDepth() != depth) {

            reason = "Grid's not the same size";

            return false;
        }

        VoxelData a = grid.getVoxelData();
        VoxelData b = grid.getVoxelData();
        for(int i=0; i < width; i++) {
            for(int j=0; j < height; j++) {
                for(int k=0; k < depth; k++) {
                    grid.getData(i,j,k,a);
                    gridB.getData(i,j,k,b);

                    if (!a.equals(b)) {
                        reason = "Pos: " + i + " " + j + " " + k + " not equal.  A: " + a.getState() + " B: " + b.getState();
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * If the equality fails a reason string will be generated.
     *
     * @return The first reason
     */
    public String getReason() {
        return reason;
    }
}
