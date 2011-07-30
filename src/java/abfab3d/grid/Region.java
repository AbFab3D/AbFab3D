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
 * A region of a voxel space.
 *
 * Different implementations may save on space for descriptions.
 *
 * @author Alan Hudson
 */
public interface Region {
    /*
     * Traverse a region and call the RegionTraverser per voxel coordinate.
     *
     * @param t The traverer to call for each voxel
     */
    public void traverse(RegionTraverser t);

    /*
     * Traverse a region and call the RegionTraverser per voxel coordinate.  Can be
     * interupted.
     *
     * @param vc The class of voxels to traverse
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void traverseInterruptible(RegionTraverser t);
}