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

// Internal Imports

/**
 * Detects whether a set operation is changing a voxel that
 * is already occupied.  This will throw an IllegalArgumentException if
 * a voxel is already !OUTSIDE when set is called.
 *
 * @author Alan Hudson
 */
public class OccupiedWrapper implements GridWrapper {
    /** The wrapper grid */
    private Grid grid;

    /**
     * Constructor.
     *
     * @param grid The grid to wrap
     */
    public OccupiedWrapper(Grid grid) {
        this.grid = grid;
    }

    /**
     * Sets the underlying grid to use.
     *
     * @param grid The grid or null to clear.
     */
    public void setGrid(Grid grid) {
        this.grid = grid;
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
     * @param The voxel data
     */
    public VoxelData getData(double x, double y, double z) {
        return grid.getData(x,y,z);
    }

    /**
     * Get the state of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param The voxel state
     */
    public VoxelData getData(int x, int y, int z) {
        return grid.getData(x,y,z);
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param The voxel state
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
     * @param The voxel state
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
     * @param The voxel material
     */
    public byte getMaterial(double x, double y, double z) {
        return grid.getMaterial(x,y,z);
    }

    /**
     * Get the material of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param The voxel material
     */
    public byte getMaterial(int x, int y, int z) {
        return grid.getMaterial(x,y,z);
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
    public void setData(double x, double y, double z, byte state, byte material) {
        byte old_state = grid.getState(x,y,z);

        if (old_state != Grid.OUTSIDE && state != Grid.OUTSIDE) {
            throw new IllegalArgumentException("Invalid state change");
        }

        grid.setData(x,y,z,state,material);
    }

    /**
     * Set the value of a voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param val The value.  0 = nothing. > 0 materialID
     */
    public void setData(int x, int y, int z, byte state, byte material) {
        byte old_state = grid.getState(x,y,z);

        if (old_state != Grid.OUTSIDE && state != Grid.OUTSIDE) {
            throw new IllegalArgumentException("Invalid state change");
        }

        grid.setData(x,y,z,state,material);
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
        grid.getGridBounds(min,max);
    }

    /**
     * Get an iterator for voxel state.  The returned object
     * Voxel may be reused so clone if you to keep it.  For speed
     * this iterator does not check for comodification, don't do
     * that.
     *
     * @param vc The voxel state
     * @return The voxels matching the state specified
     */
    public Iterator<Voxel> getStateIterator(VoxelClasses vc) {
        return grid.getStateIterator(vc);
    }

    /**
     * Get an iterator for materialID.  The returned object
     * Voxel may be reused so clone if you to keep it.  For speed
     * this iterator does not check for comodification, don't do
     * that.
     *
     * @param mat The materialID
     * @return The voxels matching the materialID
     */
    public Iterator<Voxel> getMaterialIterator(byte mat) {
        return grid.getMaterialIterator(mat);
    }

    /**
     * Get an iterator for state and materialID.  The returned object
     * Voxel may be reused so clone if you to keep it.  For speed
     * this iterator does not check for comodification, don't do
     * that.
     *
     * @param vc The voxel class
     * @param mat The materialID
     * @return The voxels that are the same state and materialID
     */
    public Iterator<Voxel> getIterator(VoxelClasses vc, byte mat) {
        return grid.getIterator(vc,mat);
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
     * Count a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The class of material to traverse
     * @return The number
     */
    public int findCount(byte mat) {
        return grid.findCount(mat);
    }

    /**
     * Get the number of height cells.
     *
     * @return the val
     */
    public int getHeight() {
        return grid.getHeight();
    }

    /**
     * Get the number of width cells.
     *
     * @return the val
     */
    public int getWidth() {
        return grid.getWidth();
    }

    /**
     * Get the number of depth cells.
     *
     * @return the val
     */
    public int getDepth() {
        return grid.getDepth();
    }

    /**
     * Get the voxel size in xz.
     *
     * @return The value
     */
    public double getVoxelSize() {
        return grid.getVoxelSize();
    }

    /**
     * Get the slice height.
     *
     * @return The value
     */
    public double getSliceHeight() {
        return grid.getSliceHeight();
    }

    /**
     * Print out a slice of data.
     */
    public String toStringSlice(int s) {
        return grid.toStringSlice(s);
    }

    /**
     * Print out all slices.
     */
    public String toStringAll() {
        return grid.toStringAll();
    }
}