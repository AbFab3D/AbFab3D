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

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;

/**
 * Subtraction operation.
 *
 * Subtracts one grid from another.  Grid A is the base grid.  B is
 * the subtracting grid.  MARKED voxels of grid B will become
 * OUTSIDE voxels of A.
 *
 * Would like a mode that preserves EXTERIOR/INTERRIOR difference.
 *
 * @author Alan Hudson
 */
public class Subtract implements Operation, ClassTraverser {
    /** The grid used for subtraction */
    private Grid src;

    /** The dest grid */
    private Grid dest;

    /** The x translation of gridB */
    private double x;

    /** The y translation of gridB */
    private double y;

    /** The z translation of gridB */
    private double z;

    /** The material for new exterior voxels */
    private long material;


    public Subtract(Grid src, double x, double y, double z, long material) {
        this.src = src;
        this.x = x;
        this.y = y;
        this.z = z;
        this.material = material;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid A.
     * @return The new grid
     */
    public Grid execute(Grid dest) {
        long t0 = time();

        this.dest = dest;

        // TODO: Make sure the grids are the same size

        src.find(Grid.VoxelClasses.MARKED, this);

        printf("subtract: %d ms\n", (time() - t0));

        return dest;
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
        dest.setState(x,y,z, Grid.OUTSIDE);

/*
        if (bstate == Grid.EXTERIOR) {
//System.out.println("found EXT: " + x + " " + y + " a: " + astate);
            if (astate == Grid.INTERIOR) {
                gridA.setData(x,y,z,Grid.EXTERIOR, material);
            } else if (astate == Grid.EXTERIOR) {
                // TODO: not so sure about this
                gridA.setData(x,y,z,Grid.OUTSIDE, (byte)0);
            }
        } else {
            // must be interior
//System.out.println("found INT: " + x + " " + y);

            if (astate == Grid.INTERIOR) {
                gridA.setData(x,y,z,Grid.OUTSIDE, (byte)0);
            } else if (astate == Grid.EXTERIOR) {
                gridA.setData(x,y,z,Grid.OUTSIDE, (byte)0);
            }
        }
*/
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
