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
 * Union operation.
 *
 * Adds one grid to another.  Grid A is the base grid.  B is
 * the union grid.  EXTERIOR voxels of grid B will become
 * be added on grid A.
 *
 * @author Alan Hudson
 */
public class Union implements Operation, AttributeOperation {
    /** The grid used for A */
    private Grid gridA;

    /** The grid used for A */
    private Grid gridAAtt;

    /** The grid to subtract */
    private Grid gridB;

    /** The grid to subtract */
    private AttributeGrid gridBAtt;

    /** The x translation of gridB */
    private double x;

    /** The y translation of gridB */
    private double y;

    /** The z translation of gridB */
    private double z;

    /** The material for new exterior voxels */
    private long material;


    public Union(Grid b, double x, double y, double z, long material) {
        if (b instanceof AttributeGrid) {
            gridBAtt = (AttributeGrid) b;
        }

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

        if (grid instanceof AttributeGrid) {
            gridAAtt = (AttributeGrid) grid;
        }
        gridA = grid;

        // TODO: Make sure the grids are the same size

        gridB.find(Grid.VoxelClasses.INSIDE, new Handler(gridA));

        return grid;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid A.
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid grid) {
        int width = grid.getWidth();
        int depth = grid.getDepth();
        int height = grid.getHeight();
        gridA = grid;

        // TODO: Make sure the grids are the same size

        gridBAtt.findAttribute(Grid.VoxelClasses.INSIDE, new AttributeHandler((AttributeGrid)gridA,material));

        return grid;
    }

}

class Handler implements ClassTraverser {
    private Grid gridA;

    public Handler(Grid grid) {
        gridA = grid;
    }

    /**
     * A voxel of the class requested has been found.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public void found(int x, int y, int z, byte vd) {
        if (vd == Grid.INSIDE) {
            gridA.setState(x,y,z,vd);
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
    public boolean foundInterruptible(int x, int y, int z, byte vd) {
        // ignore
        return true;
    }
}

class AttributeHandler implements ClassAttributeTraverser {
    private AttributeGrid gridAAtt;
    private long mat;

    public AttributeHandler(AttributeGrid grid, long material) {
        gridAAtt = grid;
        this.mat = material;
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
        byte state = vd.getState();

        if (state == Grid.INSIDE) {
            // TODO: this is not using the passed in material, should it?
//            gridAAtt.setData(x,y,z,state, mat);
            gridAAtt.setData(x,y,z,state, vd.getMaterial());
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