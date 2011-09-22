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
     * Checks whether a coordinate is in the region.  O(n) operation.
     *
     * @param vc The coordinate
     */
    public boolean contains(VoxelCoordinate vc) {
        return coords.contains(vc);
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
     * Get the volume covered by this region.
     *
     * @return The volume
     */
    public long getVolume() {
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

    /**
     * Can this region be merged with another.  The region type must remain
     * the same.
     *
     * @param r The region to merge
     */
    public boolean canMerge(Region r) {
        return false;
    }

    /**
     * Merge this region with another.
     *
     * @return true if successful.  If false no changes will be made
     */
    public boolean merge(Region r) {
        return false;
    }

    /**
     * Print out the values in the list.
     */
    public void printValues() {
        Iterator<VoxelCoordinate> itr = coords.iterator();

        while(itr.hasNext()) {
            System.out.println(itr.next());
        }
    }

    /**
     * Get the extents of the region
     *
     * @param min The preallocated min
     * @param max The preallocated max
     */
    public void getExtents(double[] min, double[] max) {
        double min_x;
        double max_x;
        double min_y;
        double max_y;
        double min_z;
        double max_z;

        min_x = Double.POSITIVE_INFINITY;
        min_y = Double.POSITIVE_INFINITY;
        min_z = Double.POSITIVE_INFINITY;
        max_x = Double.NEGATIVE_INFINITY;
        max_y = Double.NEGATIVE_INFINITY;
        max_z = Double.NEGATIVE_INFINITY;

        double cx,cy,cz;

        Iterator<VoxelCoordinate> itr = coords.iterator();
        while(itr.hasNext()) {
            VoxelCoordinate coord = itr.next();

            cx = coord.getX();
            cy = coord.getY();
            cz = coord.getZ();

            // gets max and min bounds
            if (cx > max_x)
                max_x = cx;

            if (cy > max_y)
                max_y = cy;

            if (cz > max_z)
                max_z = cz;

            if (cx < min_x)
                min_x = cx;

            if (cy < min_y)
                min_y = cy;

            if (cz < min_z)
                min_z = cz;
        }

        min[0] = min_x;
        min[1] = min_y;
        min[2] = min_z;

        max[0] = max_x;
        max[1] = max_y;
        max[2] = max_z;

    }
}