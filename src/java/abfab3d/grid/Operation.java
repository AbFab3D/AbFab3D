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


import abfab3d.core.Grid;

/**
 * An operation that can be performed on a grid.
 *
 * @author Alan Hudson
 */
public interface Operation {
    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * TODO: add region specification to allow subsets operation.
     *
     * @param dest The grid to use or null to create one
     * @return The new grid
     */
    public Grid execute(Grid dest);
}
