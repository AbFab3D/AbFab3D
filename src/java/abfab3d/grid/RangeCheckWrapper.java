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
 * Detects whether any operations exceed the dimensions specified by
 * the grid.  Useful for debugging code with boundary issues.
 * Not all grid implementations will issue ArrayOutOfBounds exceptions
 * so this class is useful for detecting those.
 *
 * This class is not designed to be performant.  Don't leave it in
 * place for production code.
 *
 * @author Alan Hudson
 */
public class RangeCheckWrapper implements GridWrapper {
    private int width;
    private int height;
    private int depth;

    /** The wrapper grid */
    private Grid grid;

    /** Should we check conversion like getGridCoords */
    private boolean checkConversion;

    /** Should we expand the grid if out of range */
    private boolean expandGrid;

    /**
     * Constructor.
     *
     * @param grid The grid to wrap
     */
    public RangeCheckWrapper(Grid grid) {
        this(grid,true,false);
    }

    /**
     * Constructor.
     *
     * @param grid The grid to wrap
     * @param checkConversions Should we check read accesses.
     */
    public RangeCheckWrapper(Grid grid, boolean checkConversions) {
        this(grid,checkConversions, false);
    }

    /**
     * Constructor.
     *
     * @param grid The grid to wrap
     * @param checkConversions Should we check read accesses.
     */
    public RangeCheckWrapper(Grid grid, boolean checkConversions, boolean expandGrid) {
        setGrid(grid);

        checkConversion = checkConversions;
        this.expandGrid = expandGrid;
    }

    /**
     * Copy Constructor.
     *
     * @param wrap The wrapper to copy
     */
    public RangeCheckWrapper(RangeCheckWrapper wrap) {
        if (wrap == null) {
            setGrid(wrap);

            return;
        }

        if (wrap.grid != null)
            this.grid = (Grid) wrap.grid.clone();
        this.width = wrap.width;
        this.height = wrap.height;
        this.depth = wrap.depth;
        this.checkConversion = wrap.checkConversion;
        this.expandGrid = wrap.expandGrid;
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
        return grid.getVoxelData();
    }

    /**
     * Sets the underlying grid to use.
     *
     * @param grid The grid or null to clear.
     */
    public void setGrid(Grid grid) {
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
     * Get the state of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @return The voxel state
     */
    public void getData(int x, int y, int z, VoxelData vd) {
        verifyRange(x,y,z);

        grid.getData(x,y,z,vd);
    }

    /**
     * Get the data for a voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @return The voxel data
     */
    public void getData(double x, double y, double z, VoxelData vd) {
        verifyRange(x,y,z);

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
        verifyRange(x,y,z);

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
        verifyRange(x,y,z);

        return grid.getState(x,y,z);
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
        verifyRange(x,y,z);

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
        verifyRange(x,y,z);

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
        if (checkConversion) verifyRange(x,y,z);

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
        if (checkConversion) verifyRange(x,y,z);

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

        grid.getGridBounds(bounds);

    }

    /**
     * Set the grid bounds in world coordinates.
     *  @param bounds array {xmin, xmax, ymin, ymax, zmin, zmax}
     */
    public void setGridBounds(double[] bounds){

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
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void find(VoxelClasses vc, ClassTraverser t) {
        verifyGrid();

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
        
        verifyGrid();
        grid.find(vc, t, xmin, xmax, ymin, ymax);

    }
    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findInterruptible(VoxelClasses vc, ClassTraverser t) {
        verifyGrid();

        grid.findInterruptible(vc, t);
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
        RangeCheckWrapper new_wrapper = new RangeCheckWrapper(this);

        return new_wrapper;
    }

    /**
     * Range check grid coord values.  If outside range then throw
     * an IllegalArgumentException.
     *
     * @param x The x value
     * @param y The y value
     * @param z The z value
     */
    private void verifyRange(int x, int y, int z) {
        verifyGrid();

        if (x < 0 || x > width - 1) {
            if (expandGrid) {
                //expand(x,y,z);
            }
            throw new IllegalArgumentException("x value invalid: " + x);
        }

        if (y < 0 || y > height - 1) {
            throw new IllegalArgumentException("y value invalid: " + y);
        }

        if (z < 0 || z > depth - 1) {
            throw new IllegalArgumentException("z value invalid: " + z);
        }
    }

    /**
     * Range check grid coord values.  If outside range then throw
     * an IllegalArgumentException.
     *
     * @param x The x value
     * @param y The y value
     * @param z The z value
     */
    private void verifyRange(double x, double y, double z) {
        verifyGrid();

        int[] pos = new int[3];

        grid.getGridCoords(x,y,z,pos);

        if (pos[0] < 0 || pos[0] > width - 1) {
            throw new IllegalArgumentException("x value invalid: " + pos[0]);
        }

        if (pos[1] < 0 || pos[1] > height - 1) {
            throw new IllegalArgumentException("y value invalid: " + pos[1]);
        }

        if (pos[2] < 0 || pos[2] > depth - 1) {
            throw new IllegalArgumentException("z value invalid: " + pos[2]);
        }
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
     * Expand the grid to contain at least this point.  Centers the existing grid inside the new grid
     * and expands the bounds of the grid.
     *
     * @param x
     * @param y
     * @param z
     */
    private void expandGrid(int x, int y, int z) {
        int w;
        int h;
        int d;

        if (x < 0) {

        }
        //Grid new_grid = grid.createEmpty(w,h,d,grid.getVoxelSize(),grid.getSliceHeight());
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
