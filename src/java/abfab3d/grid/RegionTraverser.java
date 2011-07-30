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

package abfab3d.grid;

// External Imports

/**
 * Notification of a region voxel.
 *
 * @author Alan Hudson
 */
public interface RegionTraverser {
    /**
     * The following voxel coordinate is part of the region.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public void found(int x, int y, int z);

    /**
     * The following voxel coordinate is part of the region.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     *
     * @return True to continue, false stops the traversal.
     */
    public boolean foundInterruptible(int x, int y, int z);
}