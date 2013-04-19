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
import abfab3d.grid.*;

/**
 * Copy a grid to another grid.  Assumes the grid fits into the
 * destination.
 *
 * TODO: Optimize using iterator for MARKED only copies
 * TODO: Add a param for wether to copy outside.  Really just a union then
 *
 * @author Alan Hudson
 */
public class Copy implements Operation, AttributeOperation {
    /** The x location */
    private int x;

    /** The y location */
    private int y;

    /** The z location */
    private int z;

    /** The src grid */
    private Grid src;

    public Copy(Grid src, int x, int y, int z) {
        this.src = src;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid src
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid dest) {
        int width = src.getWidth();
        int height = src.getHeight();
        int depth = src.getDepth();

        int origin_x = x;
        int origin_y = y;
        int origin_z = z;
        VoxelData vd = dest.getVoxelData();


        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                for(int z=0; z < depth; z++) {
                    dest.getData(x,y,z,vd);

                    if (vd.getState() != Grid.OUTSIDE) {
                        // TODO: really only works on empty
                        dest.setData(origin_x + x, origin_y + y, origin_z + z,
                            vd.getState(), vd.getMaterial());
                    }
                }
            }
        }

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
        int width = src.getWidth();
        int height = src.getHeight();
        int depth = src.getDepth();

        int origin_x = x;
        int origin_y = y;
        int origin_z = z;
        byte vd;

        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                for(int z=0; z < depth; z++) {
                    vd = src.getState(x,y,z);

                    if (vd != Grid.OUTSIDE) {
                        // TODO: really only works on empty
                        dest.setState(origin_x + x, origin_y + y, origin_z + z,
                                vd);
                    }
                }
            }
        }

        return dest;
    }
}
