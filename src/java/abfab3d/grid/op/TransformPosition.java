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
import javax.vecmath.*;

// Internal Imports
import abfab3d.grid.*;

/**
 * Transform the position of MARKED elements.
 *
 * Applies a 4x4 matrix to each MARKED element.  Scales are supported
 * but this can lead to voxels outside the frid.
 *
 * @author Alan Hudson
 */
public class TransformPosition implements Operation, ClassTraverser {
    /** The matrix to use */
    private Matrix4d matrix;

    /** The material for new exterior voxels */
    private int material;

    /** Scratch point */
    private Point3d p;

    /** The grid */
    private Grid grid;

    public TransformPosition(Matrix4d matrix) {
        this.matrix = matrix;
        p = new Point3d();

    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid A.
     * @return The new grid
     */
    public Grid execute(Grid grid) {
        this.grid = grid;

        grid.find(Grid.VoxelClasses.MARKED, this);

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
        double[] wcoords = new double[3];

        grid.getWorldCoords(x,y,z,wcoords);

        p.set(wcoords);
//System.out.println("coords: " + x + " " + y + " " + z);

        matrix.transform(p);
//System.out.println("wc: " + java.util.Arrays.toString(wcoords) + " to: " + p);
        byte state = vd.getState();
        int mat = vd.getMaterial();

        grid.setData(x,y,z,Grid.OUTSIDE, 0);
        grid.setData(p.x, p.y, p.z, state, mat);
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
