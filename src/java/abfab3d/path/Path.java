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
 * A path through voxel space.
 *
 * @author Alan Hudson
 * @version
 */
public interface Path {
    /**
     * Initial the path to the beginning.
     *
     * @param pos The initial pos
     * @param numVoxels The number of voxels in grid.
     */
    public void init(int[] pos, int numVoxels);

    /**
     * Find the next position along the path
     *
     * @param pos The new position
     * @return True if there are more positions
     */
    public boolean next(int[] pos);
}