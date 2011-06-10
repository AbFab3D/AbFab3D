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

    /** The grid width */
    private int width;

    /** The grid height */
    private int height;

    /** The grid depth */
    private int depth;

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
    public void init(int[] pos, int width, int height, int depth) {
        this.currPos = pos.clone();
        this.width = width;
        this.height = height;
        this.depth = depth;
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

        // if path on its edges of travel
        if ((dir[0] != 0 && pos[0] >= width) ||
            (dir[1] != 0 && pos[1] >= height) ||
            (dir[2] != 0 && pos[2] >= depth)) {

            return false;
        }

        currPos[0] = pos[0];
        currPos[1] = pos[1];
        currPos[2] = pos[2];

        return true;
    }
}