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

import abfab3d.core.AttributeGrid;
import abfab3d.core.ClassAttributeTraverser;
import abfab3d.core.ClassTraverser;
import abfab3d.core.Grid;
import abfab3d.core.GridDataChannel;
import abfab3d.core.GridDataDesc;
import abfab3d.core.VoxelClasses;
import abfab3d.core.VoxelData;
import abfab3d.core.Bounds;

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
 * @author Vladimir Bulatov
 */
public abstract class BaseAttributeWrapper extends BaseWrapper implements AttributeGridWrapper {
    public BaseAttributeWrapper() {
        super();
    }

    /**
     * Constructor.
     *
     * @param grid The grid to wrap
     */
    public BaseAttributeWrapper(AttributeGrid grid) {
        super(grid);
    }

    /**
     * Copy Constructor.
     *
     * @param wrap The wrapper to copy
     */
    public BaseAttributeWrapper(BaseAttributeWrapper wrap) {
        super(wrap);
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
        return grid.createEmpty(w, h, d, pixel, sheight);
    }

    /**
     * Get a new instance of voxel data.  Returns this grids specific sized voxel data.
     *
     * @return The voxel data
     */
    public VoxelData getVoxelData() {
        return grid.getVoxelData();
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
        grid.getData(x, y, z, vd);
    }

    /**
     * Get the data for a voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @return The voxel data
     */
    public void getDataWorld(double x, double y, double z, VoxelData vd) {
        grid.getDataWorld(x, y, z, vd);
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @return The voxel state
     */
    public byte getStateWorld(double x, double y, double z) {
        return grid.getStateWorld(x, y, z);
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
        return grid.getState(x, y, z);
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @return The voxel material
     */
    public long getAttributeWorld(double x, double y, double z) {
        return ((AttributeGrid)grid).getAttributeWorld(x, y, z);
    }

    public void setAttributeWorld(double x, double y, double z, long attribute) {
        ((AttributeGrid)grid).setAttributeWorld(x, y, z, attribute);
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
        return ((AttributeGrid)grid).getAttribute(x, y, z);
    }

    /**
     * Set the value of a voxel.
     *  @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 materialID
     * @param material The materialID
     */
    public void setDataWorld(double x, double y, double z, byte state, long material) {
/*
        // Not sure why this was here, doesn't seem to make sense.

        if (vd.getState() != Grid.OUTSIDE && state != Grid.OUTSIDE
            && vd.getAttribute() != material ) {
            throw new IllegalArgumentException("Invalid state change at pos: " + x + " " + y + " " + z);
        }
  */
        ((AttributeGrid)grid).setDataWorld(x, y, z, state, material);
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
        // Not sure why this was here, doesn't seem to make sense.
        if (vd.getState() != Grid.OUTSIDE && state != Grid.OUTSIDE
            && vd.getAttribute() != material ) {

            System.out.println("curr state: " + vd.getState() + " new state: " + state);
            System.out.println("old material: " + vd.getAttribute() + " new mat: " + material);
            throw new IllegalArgumentException("Invalid state change at index: " + x + " " + y + " " + z);
        }
  */
        ((AttributeGrid)grid).setData(x, y, z, state, material);
    }

    /**
     * Set the attribute value of a voxel.  Leaves the state unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param attribute The attribute
     */
    public void setAttributes(int x, int y, long[] attribute) {
        int nz = attribute.length;
        for(int z=0; z < nz; z++) {
            ((AttributeGrid)grid).setAttribute(x, y, z, attribute[z]);
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
        ((AttributeGrid)grid).setAttribute(x, y, z, material);
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
        grid.setState(x, y, z, state);
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
    public void setStateWorld(double x, double y, double z, byte state) {
        grid.setStateWorld(x, y, z, state);
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
        grid.getGridCoords(x, y, z, coords);
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
        grid.getWorldCoords(x, y, z, coords);
    }

    /**
     * Get the grid bounds in world coordinates.
     *
     * @param min The min coordinate
     * @param max The max coordinate
     */
    public void getGridBounds(double[] min, double[] max) {
        grid.getGridBounds(min, max);
    }


    /**
     * Get the grid bounds in world coordinates.
     *  @param bounds array {xmin, xmax, ymin, ymax, zmin, zmax}
     */
    public void getGridBounds(double[] bounds){

        grid.getGridBounds(bounds);

    }

    /**
     * Get the grid bounds in world coordinates.
     */
    public Bounds getGridBounds(){

        return grid.getGridBounds();

    }

    /**
     * Set the grid bounds in world coordinates.
     *  @param bounds array {xmin, xmax, ymin, ymax, zmin, zmax}
     */
    public void setGridBounds(double[] bounds){

        grid.setGridBounds(bounds);

    }

    public void setGridBounds(Bounds bounds){

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
        ((AttributeGrid)grid).findAttribute(mat, t);
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
        ((AttributeGrid)grid).findAttribute(vc, t);
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
        ((AttributeGrid)grid).findAttribute(vc, mat, t);
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
        ((AttributeGrid)grid).findAttribute(vc, t, xmin, xmax, ymin, ymax);
    }

    /**
     * Traverse a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttributeInterruptible(long mat, ClassAttributeTraverser t) {
        ((AttributeGrid)grid).findAttributeInterruptible(mat, t);
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
        ((AttributeGrid)grid).findAttributeInterruptible(vc, t);
    }

    /**
     * Traverse a class of voxel and material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttributeInterruptible(VoxelClasses vc, long mat, ClassAttributeTraverser t) {
        ((AttributeGrid)grid).findAttributeInterruptible(vc, mat, t);
    }

    /**
     * Count a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The class of material to traverse
     * @return The number
     */
    public int findCount(long mat) {
        return ((AttributeGrid)grid).findCount(mat);
    }

    /**
     * Remove all voxels associated with the Material.
     *
     * @param mat The aterialID
     */
    public void removeAttribute(long mat) {
        ((AttributeGrid)grid).removeAttribute(mat);
    }

    /**
     * Reassign a group of materials to a new materialID
     *
     * @param materials The new list of materials
     * @param matID The new materialID
     */
    public void reassignAttribute(long[] materials, long matID) {
        ((AttributeGrid)grid).reassignAttribute(materials, matID);
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
        throw new RuntimeException("Not implemented");
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
    public boolean insideGridWorld(double wx, double wy, double wz) {
        return grid.insideGridWorld(wx, wy, wz);
    }

    /**
       assign to the grid a description of a voxel attributes
       @param description The attirbute description 
       @override 
    */
    public void setDataDesc(GridDataDesc description){
        ((AttributeGrid)grid).setDataDesc(description);
    }

    /**
       @return voxel attribute description assigned to the grid
       @override 
    */
    public GridDataDesc getDataDesc(){
        return ((AttributeGrid)grid).getDataDesc();
    }

    /**
       copy data from fromGrid into this grid 
     */
    public void copyData(AttributeGrid fromGrid){
        ((AttributeGrid)grid).copyData(fromGrid);
    }

    public GridDataChannel getDataChannel() {
        return getDataChannel(0);
    }

    public GridDataChannel getDataChannel(int idx) {
        return ((AttributeGrid)grid).getDataChannel(idx);
    }



}
