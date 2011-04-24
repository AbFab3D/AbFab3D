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

package abfab3d.grid.op;

// External Imports

/**
 * Subtraction operation.
 *
 * Subtracts one grid from another.  Grid A is the base grid.  B is
 * the subtracted grid.  EXTERIOR voxels of grid B will become
 * new EXTERIOR points on grid A.
 *
 * @author Alan Hudson
 */
public class Subtract implements Operation, ClassTraverser {
    /** The grid to subtract */
    private Grid gridB;

    /** The x translation of gridB */
    private double x;

    /** The y translation of gridB */
    private double y;

    /** The z translation of gridB */
    private double z;

    /** The material for new exterior voxels */
    private byte material;

    public Subtract(Grid b, double x, double y, double z, byte material) {
        gridB = b;
        this.x = x;
        this.y = y;
        this.z = z;
        this.material = material;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid A.
     * @return The new grid
     */
    public Grid execute(Grid grid) {
        int width = grid.getWidth();
        int depth = grid.getDepth();
        int height = grid.getHeight();


        // TODO: Make sure the grids are the same size

        gridB.find(grid.VoxelClass.MARKED);

        return grid;
    }

    /**
     * A voxel of the class requested has been found.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public void found(int x, int y, int z, VoxelData vd) {
        byte bstate = vd.getState();
        byte astate = grid.getState(x,y,z);

        if (bstate == Grid.EXTERIOR) {
            if (astate == Grid.INTERIOR) {
                grid.setData(Grid.EXTERIOR, material);
            }
        } else {
            // must be interior

            if (astate == Grid.INTERIOR) {
                grid.setData(Grid.OUTSIDE, 0);
            }
        }
    }
}
