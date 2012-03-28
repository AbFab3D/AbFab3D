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
  * Optimized for sparse usage
  *
  * TODO:  Consider either storing this state directly onto VoxelData or
  *        having a block based rep instead of this map/array
  *        1)  Are visitor patterns usually on sparse data?
  *        2)  Do we ever need multiple visited state per voxel?
  *        3)  visited, materialID etc are attributes to a voxel.
  *        4)     maybe have an addAttribute(int id, int numBits), getAttribute(int id)
  *
  * @author Alan Hudson
  */
 public class GridVisitedIndexed implements ClassTraverser, ClassAttributeTraverser {
    /** The width of the grid */
    protected int width;

    /** The height of the grid */
    protected int height;

    /** The depth of the grid */
    protected int depth;

    /** UnVisited Map */
    protected HashSet<VoxelCoordinate> unvisitedSet;

    /** Visited status */
    protected boolean[] visited;

    /** The number of voxels in a slice */
    protected int sliceSize;

    /** Grid for findUnvisited calc. */
    protected Grid grid;

     /** Grid for findUnvisited calc. */
     protected AttributeGrid gridAtt;
     
     /**
      * Constructor
      *
      * @param grid
      * @param target
      */
    public GridVisitedIndexed(Grid grid, Grid.VoxelClasses target) {
        if (grid instanceof AttributeGrid) {
            this.gridAtt = (AttributeGrid) grid;
        }
        this.grid = grid;
        width = grid.getWidth();
        height = grid.getHeight();
        depth = grid.getDepth();
        sliceSize = width * depth;

        visited = new boolean[width * height * depth];

        // Initialize set, assume 1% used
        unvisitedSet = new HashSet<VoxelCoordinate>((int)Math.ceil(width * height * depth * 0.01f));

        grid.find(target, this);
    }

     /**
      * Constructor
      *
      * @param grid
      * @param target
      * @param mat
      */
    public GridVisitedIndexed(AttributeGrid grid, Grid.VoxelClasses target, int mat) {
        if (grid instanceof AttributeGrid) {
            this.gridAtt = (AttributeGrid) grid;
        }
        this.grid = grid;
        width = grid.getWidth();
        height = grid.getHeight();
        depth = grid.getDepth();
        sliceSize = width * depth;

        visited = new boolean[width * height * depth];

        // Initialize set, assume 1% used
        unvisitedSet = new HashSet<VoxelCoordinate>((int)Math.ceil(width * height * depth * 0.01f));

        gridAtt.findAttribute(target, mat, this);
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
        int idx = y * sliceSize + x * depth + z;

        return visited[idx];
    }

    /**
     * Has this voxel been visited.
     *
     * @param vc The voxel coordinate
     * @return Whether its been visited
     */
    public boolean getVisited(VoxelCoordinate vc) {
        int idx = vc.getY() * sliceSize + vc.getX() * depth + vc.getZ();

        return visited[idx];
    }

    /**
     * Set whether this voxel has been visited.
     *
     * @param vc The voxel coordinate
     */
    public void setVisited(VoxelCoordinate vc) {
        int idx = vc.getY() * sliceSize + vc.getX() * depth + vc.getZ();

        visited[idx] = true;
        unvisitedSet.remove(vc);
    }

    /**
     * Set whether this voxel has been visited.
     *
     * @param x The x voxel coordinate
     * @param y The y voxel coordinate
     * @param z The z voxel coordinate
     */
    public void setVisited(int x, int y, int z) {
        int idx = y * sliceSize + x * depth + z;

        visited[idx] = true;

        unvisitedSet.remove(new VoxelCoordinate(x,y,z));
    }

    /**
     * Find an unvisited voxel from the specified grid.
     *
     * @return An unvisited voxel or null if none left
     */
    public VoxelCoordinate findUnvisited() {
        Iterator<VoxelCoordinate> itr = unvisitedSet.iterator();

        VoxelCoordinate vc = null;

        if (itr.hasNext()) {
            vc = itr.next();

//System.out.println("removed visited2: " + vc);
            unvisitedSet.remove(vc);
        }

        if (vc != null) {
            int idx = vc.getY() * sliceSize + vc.getX() * depth + vc.getZ();
            visited[idx] = true;
        }

        return vc;
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
    public void found(int x, int y, int z, byte vd) {
        VoxelCoordinate vc = new VoxelCoordinate(x,y,z);

//System.out.println("Add to unvisited: " + vc);
        unvisitedSet.add(vc);
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
    public boolean foundInterruptible(int x, int y, int z, byte vd) {
        // not used
        return false;
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
         VoxelCoordinate vc = new VoxelCoordinate(x,y,z);

//System.out.println("Add to unvisited: " + vc);
         unvisitedSet.add(vc);
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
         // not used
         return false;
     }

 }
