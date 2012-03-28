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

package abfab3d.grid;

/**
 * Grid wrappers perform extra checks on top of the base grid operations.
 *
 * These operations typically have a runtime cost that not all grids want.
 * A wrapper implements Grid so they can be chained as needed to get the
 * checks needed. An example Wrapper is StateChangeWrapper which detects if
 * the state of a voxel changes and issues a RuntimeException when detected.
 *
 * @author Alan Hudson
 */
public interface AttributeGridWrapper extends AttributeGrid {
    /**
     * Sets the underlying grid to use.
     *
     * @param grid The grid or null to clear.
     */
    public void setGrid(AttributeGrid grid);
}