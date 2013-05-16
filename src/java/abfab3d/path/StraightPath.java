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

    /** Is this path axis aligned */
    private boolean axisAligned;

    public StraightPath(int[] dir) {
        if (dir[0] == 0 && dir[1] == 0 && dir[2] == 0) {
            throw new IllegalArgumentException("zero direction");
        }

        this.dir = dir.clone();

        int dir_count = 0;

        if (dir[0] != 0)
            dir_count++;

        if (dir[1] != 0)
            dir_count++;

        if (dir[2] != 0)
            dir_count++;

        if (dir_count == 1)
            axisAligned = true;

        currPos = new int[3];
    }

    public StraightPath(int x,int y,int z) {
        if (x == 0 && y == 0 && z == 0) {
            throw new IllegalArgumentException("zero direction");
        }

        this.dir = new int[] {x,y,z};

        int dir_count = 0;

        if (dir[0] != 0)
            dir_count++;

        if (dir[1] != 0)
            dir_count++;

        if (dir[2] != 0)
            dir_count++;

        if (dir_count == 1)
            axisAligned = true;

        currPos = new int[3];
    }

    /**
     * Initial the path to the beginning.
     *
     * @param pos The initial pos
     */
    public void init(int[] pos, int width, int height, int depth) {
        this.currPos[0] = pos[0];
        this.currPos[1] = pos[1];
        this.currPos[2] = pos[2];
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    /**
     * Initial the path to the beginning.
     *
     */
    public void init(int x, int y, int z, int width, int height, int depth) {
        this.currPos[0] = x;
        this.currPos[1] = y;
        this.currPos[2] = z;
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
        if ( (dir[0] != 0 && (pos[0] >= width || pos[0] < 0)) ||
             (dir[1] != 0 && (pos[1] >= height || pos[1] < 0)) ||
             (dir[2] != 0 && (pos[2] >= depth || pos[2] < 0)) ) {

            return false;
        }

        currPos[0] = pos[0];
        currPos[1] = pos[1];
        currPos[2] = pos[2];

        return true;
    }

    /**
     * Create an inverted path.
     *
     * @return The inverted path
     */
    public Path invertPath() {
        return new StraightPath(-dir[0],-dir[1],-dir[2]);
    }

    /**
     * Get the extents of this path.
     *
     * @param ret The extent, x1,x2,y1,y2,z1,z2.  Preallocate
     */
    public void getExtents(int[] ret) {
        ret[0] = currPos[0];
        ret[1] = ((dir[0] == 0) ? currPos[0] : width - 1);
        ret[2] = currPos[1];
        ret[3] = ((dir[1] == 0) ? currPos[1] : height - 1);
        ret[4] = currPos[2];
        ret[5] = ((dir[2] == 0) ? currPos[2] : depth - 1);
    }

    /**
     * Is this path aligned on an axis?
     *
     * @return true if aligned
     */
    public boolean isAxisAligned() {
        return axisAligned;
    }

    /**
     * Return the direction of the path.
     */
    public int[] getDir() {
    	return dir;
    }
    
    public String toString() {
        return "StraightPath@" + hashCode() + " dir: " + java.util.Arrays.toString(dir);
    }
}