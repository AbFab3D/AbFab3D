/*****************************************************************************
 *                        Alan Hudson Copyright (c) 2011
 *                               Java Source
 *
 * This source is private and not licensed for any use.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid;


import abfab3d.core.Grid2D;

/**
 * An operation that can be performed on a 2D grid.
 *
 * @author Alan Hudson
 */
public interface Operation2D {
    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use or null to create one
     * @return The new grid
     */
    public Grid2D execute(Grid2D dest);
}
