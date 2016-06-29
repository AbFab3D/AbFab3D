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
import abfab3d.core.Grid;
import abfab3d.grid.*;

/**
 * Downsample a grid to half its size.  Works best on power of 2 grids.
 *
 * Currently uses 2x2x2 box filtering.  Lots of room for better methods.
 *
 * @author Alan Hudson
 */
public class DownsampleOp implements Operation, AttributeOperation {
    /** Width of the grid under operation */
    private int width;

    /** Height of the grid under operation */
    private int height;

    /** Depth of the grid under operation */
    private int depth;

    /** Should we prefer marked voxels when downsampling */
    private boolean preferMarked;

    public DownsampleOp() {
    }

    public DownsampleOp(boolean preferMarked) {
        this.preferMarked = preferMarked;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid A.
     * @return The new grid
     */
    public Grid execute(Grid dest) {
        if (preferMarked) {
            return executePreferMarked(dest);
        } else {
            return executeBoxAverage(dest);
        }
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid A.
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid dest) {
        if (preferMarked) {
            return executePreferMarked(dest);
        } else {
            return executeBoxAverage(dest);
        }
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid A.
     * @return The new grid
     */
    public Grid executeBoxAverage(Grid dest) {
        width = dest.getWidth();
        depth = dest.getDepth();
        height = dest.getHeight();

        Grid ret_val = dest.createEmpty(width / 2,height / 2,depth / 2,
            dest.getVoxelSize() * 2.0, dest.getSliceHeight() * 2.0);


        int len_x = width / 2;
        int len_y = height / 2;
        int len_z = depth / 2;
        int state;

        System.out.println("Downsample to: " + len_x + " " + len_y + " " + len_z);
        for(int y=0; y < len_y; y++) {
            for(int x=0; x < len_x; x++) {
                for(int z=0; z < len_z; z++) {
                    ret_val.setState(x,y,z,
                        avgState(dest, x*2, y*2,z*2));
                }
            }
        }

        return ret_val;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid A.
     * @return The new grid
     */
    public Grid executePreferMarked(Grid dest) {
        width = dest.getWidth();
        depth = dest.getDepth();
        height = dest.getHeight();

        Grid ret_val = dest.createEmpty(width / 2,height / 2,depth / 2,
                dest.getVoxelSize() * 2.0, dest.getSliceHeight() * 2.0);


        int len_x = width / 2;
        int len_y = height / 2;
        int len_z = depth / 2;
        int state;

        byte[][][] tmp = new byte[2][2][2];

        for(int y=0; y < len_y; y++) {
            for(int x=0; x < len_x; x++) {
                for(int z=0; z < len_z; z++) {
                    ret_val.setState(x,y,z,
                            preferMarked(dest, x*2, y*2,z*2, tmp));
                }
            }
        }

        return ret_val;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid A.
     * @return The new grid
     */
    public AttributeGrid executePreferMarked(AttributeGrid dest) {
        width = dest.getWidth();
        depth = dest.getDepth();
        height = dest.getHeight();

        AttributeGrid ret_val = (AttributeGrid) dest.createEmpty(width / 2,height / 2,depth / 2,
                dest.getVoxelSize() * 2.0, dest.getSliceHeight() * 2.0);


        int len_x = width / 2;
        int len_y = height / 2;
        int len_z = depth / 2;
        int state;

        byte[][][] tmp = new byte[2][2][2];

        for(int y=0; y < len_y; y++) {
            for(int x=0; x < len_x; x++) {
                for(int z=0; z < len_z; z++) {
                    ret_val.setState(x,y,z,
                            preferMarked(dest, x*2, y*2,z*2, tmp));
                }
            }
        }

        return ret_val;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid A.
     * @return The new grid
     */
    public AttributeGrid executeBoxAverage(AttributeGrid dest) {
        width = dest.getWidth();
        depth = dest.getDepth();
        height = dest.getHeight();

        AttributeGrid ret_val = (AttributeGrid) dest.createEmpty(width / 2,height / 2,depth / 2,
                dest.getVoxelSize() * 2.0, dest.getSliceHeight() * 2.0);


        int len_x = width / 2;
        int len_y = height / 2;
        int len_z = depth / 2;
        int state;

        System.out.println("Downsample to: " + len_x + " " + len_y + " " + len_z);
        for(int y=0; y < len_y; y++) {
            for(int x=0; x < len_x; x++) {
                for(int z=0; z < len_z; z++) {
                    ret_val.setData(x,y,z,
                            avgState(dest, x*2, y*2,z*2), avgMaterial(dest, x*2, y*2, z*2));
                }
            }
        }

        return ret_val;
    }

    /**
     * Avg the state of 2x2x2 box.
     *
     * @param x The upper left x
     * @param y The upper left y
     * @param z The upper left z
     */
    private byte avgState(Grid grid, int x, int y, int z) {
        byte ret_val;

        byte[][][] state = new byte[2][2][2];

        // Init to no value
        for(int i=0; i < 2; i++) {
            for(int j=0; j < 2; j++) {
                for(int k=0; k < 2; k++) {
                    state[i][j][k] = -1;
                }
            }
        }

        state[0][0][0] = grid.getState(x,y,z);

        if (z+1 < depth)
            state[0][0][1] = grid.getState(x,y,z+1);
        if (y+1 < height)
            state[0][1][0] = grid.getState(x,y+1,z);
        if (z+1 < depth && y+1 < height)
            state[0][1][1] = grid.getState(x,y+1,z+1);

        if (x+1 < width) {
            state[1][0][0] = grid.getState(x+1,y,z);
            state[1][0][1] = grid.getState(x+1,y,z+1);

            if (y+1 < height) {
                state[1][1][0] = grid.getState(x+1,y+1,z);

                if (z+1 < depth) {
                    state[1][1][1] = grid.getState(x+1,y+1,z+1);
                }
            }
        }

        int cnt_outside = 0;
        int cnt_interior = 0;

        byte val;

        for(int i=0; i < 2; i++) {
            for(int j=0; j < 2; j++) {
                for(int k=0; k < 2; k++) {
                    val = state[i][j][k];
                    if (val == Grid.OUTSIDE) {
                        cnt_outside++;
                    } else if (val == Grid.INSIDE) {
                        cnt_interior++;
                    }
                }
            }
        }

        if (cnt_interior >= cnt_outside) {

            ret_val = Grid.INSIDE;
        } else {
            ret_val = Grid.OUTSIDE;
        }

        return ret_val;
    }

    /**
     * Avg a 2x2 box with preference to marked state.
     *
     * @param x The upper left x
     * @param y The upper left y
     * @param z The upper left z
     * @param state Scratch var for state to avoid alloc
     */
    private byte preferMarked(Grid grid, int x, int y, int z, byte[][][] state) {
        byte ret_val;

        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        // Init to no value
        for(int i=0; i < 2; i++) {
            for(int j=0; j < 2; j++) {
                for(int k=0; k < 2; k++) {
                    state[i][j][k] = -1;
                }
            }
        }

        state[0][0][0] = grid.getState(x,y,z);

        if (z+1 < depth)
            state[0][0][1] = grid.getState(x,y,z+1);
        if (y+1 < height)
            state[0][1][0] = grid.getState(x,y+1,z);
        if (z+1 < depth && y+1 < height)
            state[0][1][1] = grid.getState(x,y+1,z+1);

        if (x+1 < width) {
            state[1][0][0] = grid.getState(x+1,y,z);
            state[1][0][1] = grid.getState(x+1,y,z+1);

            if (y+1 < height) {
                state[1][1][0] = grid.getState(x+1,y+1,z);

                if (z+1 < depth) {
                    state[1][1][1] = grid.getState(x+1,y+1,z+1);
                }
            }
        }

        int cnt_exterior = 0;
        int cnt_interior = 0;

        byte val;

        for(int i=0; i < 2; i++) {
            for(int j=0; j < 2; j++) {
                for(int k=0; k < 2; k++) {
                    val = state[i][j][k];
                    if (val == Grid.INSIDE) {
                        cnt_interior++;
                    }
                }
            }
        }

        if (cnt_interior > 0) {
            return Grid.INSIDE;
        }

        return Grid.OUTSIDE;

    }

    private int avgMaterial(Grid grid, int x, int y, int z) {
// TODO: need to implement
        int ret_val = 0;

        return ret_val;
    }
}
