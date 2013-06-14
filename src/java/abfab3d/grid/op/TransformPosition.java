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
 * Transform the position of INSIDE elements.
 *
 * Applies a 4x4 matrix to each INSIDE element.  Scales are supported
 * but this can lead to voxels outside the grid.
 *
 * @author Alan Hudson
 */
public class TransformPosition implements Operation, ClassTraverser {
    private static final boolean DEBUG = true;

    /** The matrix to use */
    private Matrix4d matrix;

    /** The source grid */
    private Grid src;

    /** The dest grid */
    private Grid dest;

    /** Scratch point */
    private Point3d p;

    /** Scratch coords */
    private double[] wcoords;

    public TransformPosition(Matrix4d matrix, Grid src) {
        this.src = src;
        this.matrix = matrix;
        p = new Point3d();
        wcoords = new double[3];
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * TODO: add region specification to allow subsets operation.
     *
     * @param dest The grid to use or null to create one
     * @return The new grid
     */
    public Grid execute(Grid dest) {
        this.dest = dest;

        src.find(Grid.VoxelClasses.INSIDE, this);

        return dest;
    }

    /**
     * A voxel of the class requested has been found.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param state The voxel data
     */
    public void found(int x, int y, int z, byte state) {
        src.getWorldCoords(x,y,z,wcoords);

        p.set(wcoords);

        matrix.transform(p);

        dest.setState(x,y,z,Grid.OUTSIDE);

        if (DEBUG && !dest.insideGrid(p.x,p.y,p.z)) {
            System.out.println("Point outside grid: " + x + " " + y + " " + z);
            System.out.println("   dest: " + p);
            int[] pos = new int[3];
            dest.getGridCoords(p.x,p.y,p.z, pos);
            System.out.println("   dest: " + java.util.Arrays.toString(pos));
            return;
        }

        dest.setState(p.x, p.y, p.z, state);
     }

    /**
     * A voxel of the class requested has been found.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param state The voxel data
     */
    public boolean foundInterruptible(int x, int y, int z, byte state) {
        // ignore
        return true;
    }
}
