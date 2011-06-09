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

package abfab3d.path;

/**
 * A path in a straight direction to the last voxel of the grid.
 *
 * @author Alan Hudson
 */
public class StraightPath implements Path {
    /** The direction */
    private int[] dir;

    /** The number of voxels in the grid */
    private int numVoxels;

    /** The current position */
    private int[] currPos;

    public StraightPath(int[] dir) {
        this.dir = dir.clone();

    }

    /**
     * Initial the path to the beginning.
     *
     * @param pos The initial pos
     * @param numVoxels The number of voxels in grid.
     */
    public void init(int[] pos, int numVoxels) {
        this.currPos = pos.clone();
        this.numVoxels = numVoxels;
    }

    /**
     * Find the next position along the path
     *
     * @param pos The new position
     * @return True if there are more positions
     */
    public boolean next(int[] pos) {
        pos[0] = currPos[0] + dir[0];
        pos[1] = currPos[1] + dir[1];
        pos[2] = currPos[2] + dir[2];

        if (pos[0] >= numVoxels || pos[1] >= numVoxels ||
            pos[2] >= numVoxels) {

            return false;
        }

        return true;
    }
}