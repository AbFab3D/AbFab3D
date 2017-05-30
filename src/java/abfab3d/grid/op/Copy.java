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
import abfab3d.core.AttributeGrid;
import abfab3d.core.ClassAttributeTraverser;
import abfab3d.core.ClassTraverser;
import abfab3d.core.Grid;
import abfab3d.core.Grid2D;
import abfab3d.core.VoxelClasses;
import abfab3d.core.VoxelData;
import abfab3d.grid.*;

/**
 * Copy a grid to another grid.  Assumes the grid fits into the
 * destination.
 *
 * TODO: Optimize using iterator for INSIDE only copies
 * TODO: Add a param for wether to copy outside.  Really just a union then
 *
 * @author Alan Hudson
 */
public class Copy implements Operation, AttributeOperation, Operation2D, ClassTraverser, ClassAttributeTraverser {
    /** The x location */
    private int x0;

    /** The y location */
    private int y0;

    /** The z location */
    private int z0;

    /** The src grid */
    private Grid src;
    private Grid2D src2d;

    /** The dest grid */
    private Grid destGrid;
    private AttributeGrid destGridAtt;

    public Copy(Grid src, int x, int y, int z) {
        this.src = src;
        this.x0 = x;
        this.y0 = y;
        this.z0 = z;
    }

    public Copy(Grid2D src, int x, int y) {
        this.src2d = src;
        this.x0 = x;
        this.y0 = y;
    }

    public Copy(Grid src) {
        this.src = src;
    }
    public Copy(Grid2D src) {
        this.src2d = src;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid src
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid dest) {
        destGridAtt = dest;
        ((AttributeGrid)src).findAttribute(VoxelClasses.INSIDE, this);

        destGridAtt = null;
        return dest;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid src
     * @return The new grid
     */
    public Grid execute(Grid dest) {
        destGrid = dest;
        src.find(VoxelClasses.INSIDE, this);

        destGrid = null;
        return dest;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid dest
     * @return The new grid
     */
    public Grid2D execute(Grid2D dest) {
        int w = src2d.getWidth();
        int h = src2d.getHeight();

        for(int y=0; y < h; y++) {
            for(int x=0; x < w; x++) {
                dest.setAttribute(x0 + x, y0 + y, src2d.getAttribute(x,y));
            }
        }

        return dest;
    }

    @Override
    public void found(int x, int y, int z, VoxelData vd) {
        if (vd.getState() != Grid.OUTSIDE) {
            destGridAtt.setData(x0 + x, y0 + y, z0 + z,
                    vd.getState(), vd.getMaterial());
        }
    }

    @Override
    public boolean foundInterruptible(int x, int y, int z, VoxelData vd) {
        // not used
        return true;
    }

    @Override
    public void found(int x, int y, int z, byte state) {
        if (state != Grid.OUTSIDE) {
            destGrid.setState(x0 + x, y0 + y, z0 + z, state);
        }
    }

    @Override
    public boolean foundInterruptible(int x, int y, int z, byte state) {
        // not used
        return false;
    }

    /**
       convenience method to copy grids
     */
    public static void copy(Grid2D srcGrid, Grid2D destGrid){

        int w = srcGrid.getWidth();
        int h = srcGrid.getHeight();

        for(int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){
                destGrid.setAttribute(x,y,srcGrid.getAttribute(x,y));
            }
        }
    }

    /**
       convenience method to copy grid into new one
     */
    public static Grid2D createCopy(Grid2D grid){

        Grid2D dest = grid.createEmpty(grid.getWidth(),grid.getHeight(),grid.getVoxelSize());
        dest.setDataDesc(grid.getDataDesc());
        copy(grid, dest);
        return dest;
    }
}
