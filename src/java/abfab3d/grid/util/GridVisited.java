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

 package abfab3d.grid.util;

// External Imports
import java.util.*;

// Internal Imports
import abfab3d.grid.*;

 /**
  * Tracks whether a voxel has been visited.
  *
  * Internally this class switches representations from a map to
  * an array when the memory usage of the map exceeds the flat
  * array.
  *
  * Memory usage of VoxelCoordinate is a guess.  I assume its this:
  *    hashMapPtr, Java Object Overhead, 3 ints
  *
  * @author Alan Hudson
  */
 public class GridVisited implements ClassTraverser {
    /** Memory usage for VoxelCoordinate.  Guess Java memory usage */
    private static final int VC_MEMORY = 4 + 8 + 3 * 4;

    /** Multiplier of
    /** The width of the grid */
    protected int width;

    /** The height of the grid */
    protected int height;

    /** The depth of the grid */
    protected int depth;

    /** Visited Map */
    protected HashSet<VoxelCoordinate> visitedSet;

    /** Visited Array */
    protected boolean[][][] visitedArray;

    /** The maximum entries before swapping representations */
    protected int maxEntries;

    /** Grid for findUnvisited calc. */
    protected Grid grid;

    /** The unvisited coordinate found */
    protected VoxelCoordinate unvisited;

    /**
     * Constructor.  maxEntriesMultiplier is set to 1X
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     */
    public GridVisited(int w, int h, int d) {

        this(w,h,d,1);
    }

    /**
     * Constructor.
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     * @param maxEntries The maximum entries multiplier before switching to an array
     */
    public GridVisited(int w, int h, int d, int maxEntries) {
        width = w;
        height = h;
        depth = d;

        // Start with a set
        visitedSet = new HashSet<VoxelCoordinate>();

        this.maxEntries = maxEntries * width * depth * height / VC_MEMORY;
    }

    /**
     * Has this voxel been visited.
     *
     * @param x The x voxel coordinate
     * @param y The y voxel coordinate
     * @param z The z voxel coordinate
     * @return Whether its been visited
     */
    public boolean getVisited(int x,int y,int z) {
        if (visitedSet == null) {
            if (visitedArray[x][y][z]) {
                return true;
            } else {
                return false;
            }
        }

        VoxelCoordinate vc = new VoxelCoordinate(x,y,z);

        return visitedSet.contains(vc);
    }

    /**
     * Has this voxel been visited.
     *
     * @param x The x voxel coordinate
     * @param y The y voxel coordinate
     * @param z The z voxel coordinate
     * @return Whether its been visited
     */
    public boolean getVisited(VoxelCoordinate vc) {
        if (visitedSet == null) {
            if (visitedArray[vc.getX()][vc.getY()][vc.getZ()]) {
                return true;
            } else {
                return false;
            }
        }

        return visitedSet.contains(vc);
    }

    /**
     * Set whether this voxel has been visited.
     *
     * @param vc The voxel coordinate
     */
    public void setVisited(VoxelCoordinate vc, boolean state) {
        if (visitedSet == null) {
            visitedArray[vc.getX()][vc.getY()][vc.getZ()] = state;

            return;
        }

        if (state == true) {
            visitedSet.add(vc);
        } else {
            visitedSet.remove(vc);
        }

        if (visitedSet.size() >= maxEntries) {
            changeRepresentation();
        }
    }

    /**
     * Set whether this voxel has been visited.
     *
     * @param x The x voxel coordinate
     * @param y The y voxel coordinate
     * @param z The z voxel coordinate
     */
    public void setVisited(int x, int y, int z, boolean state) {
        if (visitedSet == null) {
            visitedArray[x][y][z] = state;

            return;
        }

        if (state == true) {
            visitedSet.add(new VoxelCoordinate(x,y,z));
        } else {
            visitedSet.remove(new VoxelCoordinate(x,y,z));
        }

        if (visitedSet.size() >= maxEntries) {
            changeRepresentation();
        }
    }

    /**
     * Find an unvisited voxel from the specified grid.
     *
     * @param grid The grid
     */
    public VoxelCoordinate findUnvisited(Grid grid) {
        this.grid = grid;
        unvisited = null;

        grid.findInterruptible(Grid.VoxelClasses.MARKED, this);

        grid = null;

        return unvisited;
    }

    /**
     * Clear the visited status.
     */
    public void clear() {
        visitedArray = null;
        visitedSet = new HashSet<VoxelCoordinate>();
    }

    /**
     * Change the internal representation.  Changes only from
     * Set to array.
     */
    private void changeRepresentation() {
        if (visitedSet == null)
            return;

        visitedArray = new boolean[width][height][depth];

        Iterator<VoxelCoordinate> itr = visitedSet.iterator();
        while(itr.hasNext()) {
            VoxelCoordinate vc = itr.next();

            visitedArray[vc.getX()][vc.getY()][vc.getZ()] = true;
        }

        visitedSet = null;
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public void found(int x, int y, int z, VoxelData vd) {
        // ignore
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     *
     * @return True to continue, false stops the traversal.
     */
    public boolean foundInterruptible(int x, int y, int z, VoxelData vd) {
        if (visitedSet != null) {
            VoxelCoordinate vc = new VoxelCoordinate(x,y,z);
            if (visitedSet.contains(vc)) {
                return true;
            }

            unvisited = vc;

            return false;
        } else {
            if (visitedArray[x][y][z] == true) {
                // continue search
                return true;
            } else {
                unvisited = new VoxelCoordinate(x,y,z);
                return false;
            }
        }
    }

 }