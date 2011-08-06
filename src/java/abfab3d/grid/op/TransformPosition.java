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

    /** The grid */
    private Grid grid;

    /** Scratch point */
    private Point3d p;

    /** Scratch coords */
    private double[] wcoords;

    public TransformPosition(Matrix4d matrix) {
        this.matrix = matrix;
        p = new Point3d();
        wcoords = new double[3];
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

System.out.println("marked: " + cnt + " overwritten: " + overwriteCnt);
        return grid;
    }

int cnt = 0;
int overwriteCnt = 0;

    /**
     * A voxel of the class requested has been found.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public void found(int x, int y, int z, VoxelData vd) {
cnt++;
        grid.getWorldCoords(x,y,z,wcoords);

        p.set(wcoords);
//System.out.println("coords: " + x + " " + y + " " + z);

        matrix.transform(p);
//System.out.println("wc: " + java.util.Arrays.toString(wcoords) + " to: " + p);
        byte state = vd.getState();
        int mat = vd.getMaterial();

        grid.setData(x,y,z,Grid.OUTSIDE, 0);

        if (grid.getState(p.x, p.y, p.z) != Grid.OUTSIDE) {
            int[] gcoords = new int[3];

            grid.getGridCoords(p.x, p.y, p.z, gcoords);
System.out.println("overwrite: " + p + " orig: " + java.util.Arrays.toString(wcoords));
System.out.println("   Moved from: " + x + " " + y + " " + z + " to: " + java.util.Arrays.toString(gcoords));
            overwriteCnt++;
        } else {
            int[] gcoords = new int[3];
            grid.getGridCoords(p.x, p.y, p.z, gcoords);
System.out.println("place: " + p + " orig: " + java.util.Arrays.toString(wcoords));
System.out.println("   Moved from: " + x + " " + y + " " + z + " to: " + java.util.Arrays.toString(gcoords));
        }

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
