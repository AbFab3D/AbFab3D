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
import java.util.*;

/**
 * A region of a voxel space using a list of each voxel coordinate.
 *
 *
 * @author Alan Hudson
 */
public class ListRegion implements Region {
    /** This coordinates */
    private List<VoxelCoordinate> coords;

    public ListRegion() {
        this(100);
    }

    /**
     * Constructor.
     *
     * @param sizeGuess a guess at the maximum size.  Will preallocate list to this size.
     */
    public ListRegion(int sizeGuess) {
        coords = new ArrayList<VoxelCoordinate>(sizeGuess);
    }

    /**
     * Add a coordinate to this region.
     *
     * @param vc The coordinate
     */
    public void add(VoxelCoordinate vc) {
        coords.add(vc);
    }

    /**
     * Remove a coordinate to this region.
     *
     * @param vc The coordinate
     */
    public void remove(VoxelCoordinate vc) {
        coords.remove(vc);
    }

    /**
     * Get the number of coordinates in the region.
     *
     * @return The number of coordinates
     */
    public int getNumCoords() {
        return coords.size();
    }

    /**
     * Get a list of coordinates.  This is the raw list for performance, do not modify directly.
     *
     * @return The list
     */
    public List<VoxelCoordinate> getList() {
        return coords;
    }

    /*
     * Traverse a region and call the RegionTraverser per voxel coordinate.
     *
     * @param t The traverer to call for each voxel
     */
    public void traverse(RegionTraverser t) {
        int len = coords.size();

        for(int i=0; i < len; i++) {
            VoxelCoordinate vc = coords.get(i);

            t.found(vc.getX(), vc.getY(), vc.getZ());
        }
    }

    /*
     * Traverse a region and call the RegionTraverser per voxel coordinate.  Can be
     * interupted.
     *
     * @param vc The class of voxels to traverse
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void traverseInterruptible(RegionTraverser t) {
        int len = coords.size();

        for(int i=0; i < len; i++) {
            VoxelCoordinate vc = coords.get(i);

            if (!t.foundInterruptible(vc.getX(), vc.getY(), vc.getZ()))
                break;
        }
    }
}