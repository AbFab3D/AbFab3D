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

// Internal Imports

/**
 * Masks the usage of setData calls with an attribute to an underlying grid that may not
 * contain attributes.  This adds overhead to each call but avoid code duplication.  Use this
 * for non-critical code where code maintenance if more important then speed.
 *
 * @author Alan Hudson
 */
public class DualWrapper implements AttributeGridWrapper {
    private int width;
    private int height;
    private int depth;

    /** The wrapper grid */
    private AttributeGrid gridAtt;

    /** The wrapper grid */
    private Grid grid;

    /**
     * Constructor.
     *
     * @param grid The grid to wrap
     */
    public DualWrapper(Grid grid) {
        setGrid(grid);
    }

    /**
     * Copy Constructor.
     *
     * @param wrap The wrapper to copy
     */
    public DualWrapper(DualWrapper wrap) {
        if (wrap == null) {
            setGrid(wrap);

            return;
        }

        if (wrap.grid != null)
            this.grid = (AttributeGrid) wrap.grid.clone();
        this.width = wrap.width;
        this.height = wrap.height;
        this.depth = wrap.depth;
    }

    /**
     * Create an empty grid of the specified size.  Reuses
     * the grid type and material type(byte, short, int).
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public Grid createEmpty(int w, int h, int d, double pixel, double sheight) {
        return grid.createEmpty(w,h,d,pixel,sheight);
    }

    /**
     * Get a new instance of voxel data.  Returns this grids specific sized voxel data.
     *
     * @return The voxel data
     */
    public VoxelData getVoxelData() {
        return gridAtt.getVoxelData();
    }

    /**
     * Sets the underlying grid to use.
     *
     * @param grid The grid or null to clear.
     */
    public void setGrid(Grid grid) {
        if (grid instanceof AttributeGrid) {
            gridAtt = (AttributeGrid) grid;
        }

        this.grid = grid;

        if (grid != null) {
            width = grid.getWidth();
            height = grid.getHeight();
            depth = grid.getDepth();
        } else {
            width = 0;
            height = 0;
            depth = 0;
        }
    }

    /**
     * Sets the underlying grid to use.
     *
     * @param grid The grid or null to clear.
     */
    public void setGrid(AttributeGrid grid) {
        gridAtt = (AttributeGrid) grid;
        this.grid = grid;

        if (grid != null) {
            width = grid.getWidth();
            height = grid.getHeight();
            depth = grid.getDepth();
        } else {
            width = 0;
            height = 0;
            depth = 0;
        }
    }

    //----------------------------------------------------------
    // Grid methods
    //----------------------------------------------------------

    /**
     * Get the data for a voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @return The voxel data
     */
    public void getData(double x, double y, double z,VoxelData vd) {
        grid.getData(x,y,z,vd);
    }

    /**
     * Get the state of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @return The voxel state
     */
    public void getData(int x, int y, int z,VoxelData vd) {
        grid.getData(x,y,z,vd);
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @return The voxel state
     */
    public byte getState(double x, double y, double z) {
        return grid.getState(x,y,z);
    }

    /**
     * Get the state of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @return The voxel state
     */
    public byte getState(int x, int y, int z) {
        return grid.getState(x,y,z);
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @return The voxel material
     */
    public long getAttribute(double x, double y, double z) {
        if (gridAtt != null) {
            return gridAtt.getAttribute(x, y, z);
        } else {
            return Grid.NO_MATERIAL;
        }
    }

    /**
     * Get the material of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @return The voxel material
     */
    public long getAttribute(int x, int y, int z) {
        if (gridAtt != null) {
            return gridAtt.getAttribute(x, y, z);
        } else {
            return Grid.NO_MATERIAL;
        }
    }

    /**
     * Set the value of a voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 materialID
     * @param material The materialID
     */
    public void setData(double x, double y, double z, byte state, long material) {

/*
        VoxelData vd = grid.getData(x,y,z);
        // Not sure why this was here, doesn't seem to make sense.

        if (vd.getState() != Grid.OUTSIDE && state != Grid.OUTSIDE
            && vd.getAttribute() != material ) {
            throw new IllegalArgumentException("Invalid state change at pos: " + x + " " + y + " " + z);
        }
  */
        if (gridAtt != null) {
            gridAtt.setData(x,y,z,state,material);
        } else {
            grid.setState(x,y,z,state);
        }
    }

    /**
     * Set the value of a voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The new state
     * @param material The new material value.  0 = nothing. > 0 materialID
     */
    public void setData(int x, int y, int z, byte state, long material) {

/*
        VoxelData vd = grid.getData(x,y,z);

        // Not sure why this was here, doesn't seem to make sense.
        if (vd.getState() != Grid.OUTSIDE && state != Grid.OUTSIDE
            && vd.getAttribute() != material ) {

            System.out.println("curr state: " + vd.getState() + " new state: " + state);
            System.out.println("old material: " + vd.getAttribute() + " new mat: " + material);
            throw new IllegalArgumentException("Invalid state change at index: " + x + " " + y + " " + z);
        }
  */
        if (gridAtt != null) {
            gridAtt.setData(x,y,z,state,material);
        } else {
            grid.setState(x,y,z,state);
        }
    }

    /**
     * Set the material value of a voxel.  Leaves the state unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param material The materialID
     */
    public void setAttribute(int x, int y, int z, long material) {
        if (gridAtt != null) {
            gridAtt.setAttribute(x, y, z, material);
        }
    }

    /**
     * Set the state value of a voxel.  Leaves the material unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 materialID
     */
    public void setState(int x, int y, int z, byte state) {
        grid.setState(x,y,z,state);
    }

    /**
     * Set the state value of a voxel.  Leaves the material unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 materialID
     * @return material The materialID
     */
    public void setState(double x, double y, double z, byte state) {
        grid.setState(x,y,z,state);
    }

    /**
     * Get the grid coordinates for a world coordinate.
     *
     * @param x The x value in world coords
     * @param y The y value in world coords
     * @param z The z value in world coords
     * @param coords The ans is placed into this preallocated array(3).
     */
    public void getGridCoords(double x, double y, double z, int[] coords) {
        grid.getGridCoords(x,y,z,coords);
    }

    /**
     * Get the world coordinates for a grid coordinate.
     *
     * @param x The x value in grid coords
     * @param y The y value in grid coords
     * @param z The z value in grid coords
     * @param coords The ans is placed into this preallocated array(3).
     */
    public void getWorldCoords(int x, int y, int z, double[] coords) {
        grid.getWorldCoords(x,y,z,coords);
    }

    /**
     * Get the grid bounds in world coordinates.
     *
     * @param min The min coordinate
     * @param max The max coordinate
     */
    public void getGridBounds(double[] min, double[] max) {
        verifyGrid();

        grid.getGridBounds(min,max);
    }

    /**
     * Get the grid bounds in world coordinates.
     *  @param bounds array {xmin, xmax, ymin, ymax, zmin, zmax}
     */
    public void getGridBounds(double[] bounds){

        verifyGrid();
        grid.getGridBounds(bounds);

    }

    /**
     * Set the grid bounds in world coordinates.
     *  @param bounds array {xmin, xmax, ymin, ymax, zmin, zmax}
     */
    public void setGridBounds(double[] bounds){

        verifyGrid();
        grid.setGridBounds(bounds);

    }


    /**
     * Count a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @return The number
     */
    public int findCount(VoxelClasses vc) {
        verifyGrid();

        return grid.findCount(vc);
    }

    /**
     * Traverse a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttribute(long mat, ClassAttributeTraverser t) {
        if (gridAtt != null) {
            gridAtt.findAttribute(mat,t);
        } else {
            // error
            throw new IllegalArgumentException("findAttribute not available on grid");
        }
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void find(VoxelClasses vc, ClassTraverser t) {
        grid.find(vc, t);
    }

    /**
     * Traverse a class of voxels types over given rectangle in xy plane.
     * May be much faster then full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     * @param xmin - minimal x - coordinate of voxels
     * @param xmax - maximal x - coordinate of voxels
     * @param ymin - minimal y - coordinate of voxels
     * @param ymax - maximal y - coordinate of voxels
     */
    public void find(VoxelClasses vc, ClassTraverser t, int xmin, int xmax, int ymin, int ymax){

        grid.find(vc, t, xmin, xmax, ymin, ymax);

    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttribute(VoxelClasses vc, ClassAttributeTraverser t) {
        if (gridAtt != null) {
            gridAtt.findAttribute(vc, t);
        } else {
            // error
            throw new IllegalArgumentException("findAttribute not available on grid");
        }
    }

    /**
     * Traverse a class of voxel and material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttribute(VoxelClasses vc, long mat, ClassAttributeTraverser t) {
        if (gridAtt != null) {
            gridAtt.findAttribute(vc, mat, t);
        } else {
            // error
            throw new IllegalArgumentException("findAttribute not available on grid");
        }
    }

    /**
     * Traverse a class of voxels types over given rectangle in xy plane.
     * May be much faster then full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     * @param xmin - minimal x - coordinate of voxels
     * @param xmax - maximal x - coordinate of voxels
     * @param ymin - minimal y - coordinate of voxels
     * @param ymax - maximal y - coordinate of voxels
     */
    public void findAttribute(VoxelClasses vc, ClassAttributeTraverser t, int xmin, int xmax, int ymin, int ymax) {
        if (gridAtt != null) {
            gridAtt.findAttribute(vc,t,xmin,xmax,ymin,ymax);
        } else {
            // error
            throw new IllegalArgumentException("findAttribute not available on grid");
        }
    }

    /**
     * Traverse a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttributeInterruptible(long mat, ClassAttributeTraverser t) {
        if (gridAtt != null) {
            gridAtt.findAttributeInterruptible(mat,t);
        } else {
            // error
            throw new IllegalArgumentException("findAttribute not available on grid");
        }
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findInterruptible(VoxelClasses vc, ClassTraverser t) {
        grid.findInterruptible(vc, t);
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttributeInterruptible(VoxelClasses vc, ClassAttributeTraverser t) {
        if (gridAtt != null) {
            gridAtt.findAttributeInterruptible(vc, t);
        } else {
            // error
            throw new IllegalArgumentException("findAttribute not available on grid");
        }
    }

    /**
     * Traverse a class of voxel and material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param mat The material to traverse
     * @param t The traverser to call for each voxel
     */
    public void findAttributeInterruptible(VoxelClasses vc, long mat, ClassAttributeTraverser t) {
        if (gridAtt != null) {
            gridAtt.findAttributeInterruptible(vc, mat, t);
        } else {
            // error
            throw new IllegalArgumentException("findAttribute not available on grid");
        }
    }

    /**
     * Count a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The class of material to traverse
     * @return The number
     */
    public int findCount(long mat) {
        if (gridAtt != null) {
            return gridAtt.findCount(mat);
        } else {
            // error
            throw new IllegalArgumentException("findCount not available on grid");
        }
    }

    /**
     * Remove all voxels associated with the Material.
     *
     * @param mat The aterialID
     */
    public void removeAttribute(long mat) {
        if (gridAtt != null) {
            gridAtt.removeAttribute(mat);
        } else {
            // error
            throw new IllegalArgumentException("removeAttribute not available on grid");
        }
    }

    /**
     * Reassign a group of materials to a new materialID
     *
     * @param materials The new list of materials
     * @param matID The new materialID
     */
    public void reassignAttribute(long[] materials, long matID) {
        if (gridAtt != null) {
            gridAtt.reassignAttribute(materials, matID);
        } else {
            // error
            throw new IllegalArgumentException("removeAttribute not available on grid");
        }

    }

    /**
     * Get the number of height cells.
     *
     * @return the val
     */
    public int getHeight() {
        verifyGrid();

        return grid.getHeight();
    }

    /**
     * Get the number of width cells.
     *
     * @return the val
     */
    public int getWidth() {
        verifyGrid();

        return grid.getWidth();
    }

    /**
     * Get the number of depth cells.
     *
     * @return the val
     */
    public int getDepth() {
        verifyGrid();

        return grid.getDepth();
    }

    /**
     * Get the voxel size in xz.
     *
     * @return The value
     */
    public double getVoxelSize() {
        verifyGrid();

        return grid.getVoxelSize();
    }

    /**
     * Get the slice height.
     *
     * @return The value
     */
    public double getSliceHeight() {
        verifyGrid();

        return grid.getSliceHeight();
    }

    /**
     * Print out a slice of data.
     */
    public String toStringSlice(int s) {
        verifyGrid();

        return grid.toStringSlice(s);
    }

    /**
     * Print out all slices.
     */
    public String toStringAll() {
        verifyGrid();

        return grid.toStringAll();
    }

    /**
     * Clone this object.
     */
    public Object clone() {
        DualWrapper new_wrapper = new DualWrapper(this);

        return new_wrapper;
    }

    /**
     * Check whether the grid is null and throws an IllegalArgumentException is so
     */
    private void verifyGrid() {
        if (grid == null) {
            throw new NullPointerException("Grid has not been set");
        }
    }

    /**
     * Determine if a voxel coordinate is inside the grid space.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return True if the coordinate is inside the grid space
     */
    public boolean insideGrid(int x, int y, int z) {
        return grid.insideGrid(x,y,z);
    }

    /**
     * Determine if a voxel coordinate is inside the grid space.
     *
     * @param wx The x world coordinate
     * @param wy The y world coordinate
     * @param wz The z world coordinate
     * @return True if the coordinate is inside the grid space
     */
    public boolean insideGrid(double wx, double wy, double wz) {
        return grid.insideGrid(wx,wy,wz);
    }

}
