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
import java.util.Iterator;

// Internal Imports
import abfab3d.grid.*;

/**
 * Union operation.
 *
 * Adds one grid to another.  Grid A is the base grid.  B is
 * the union grid.  EXTERIOR voxels of grid B will become
 * be added on grid A.
 *
 * @author Alan Hudson
 */
public class Union implements Operation, ClassTraverser {
    /** The grid to subtract */
    private Grid gridB;

    /** The x translation of gridB */
    private double x;

    /** The y translation of gridB */
    private double y;

    /** The z translation of gridB */
    private double z;

    /** The material for new exterior voxels */
    private int material;

    /** The grid used for A */
    private Grid gridA;

    public Union(Grid b, double x, double y, double z, int material) {
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
        gridA = grid;

        // TODO: Make sure the grids are the same size

        gridB.find(Grid.VoxelClasses.MARKED, this);

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
        byte astate = gridA.getState(x,y,z);
        int mat = vd.getMaterial();

        if (bstate == Grid.EXTERIOR || bstate == Grid.INTERIOR) {
            gridA.setData(x,y,z,bstate, mat);
        }
     }

    /**
     * A voxel of the class requested has been found.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public boolean foundInterruptible(int x, int y, int z, VoxelData vd) {
        // ignore
        return true;
    }
}
