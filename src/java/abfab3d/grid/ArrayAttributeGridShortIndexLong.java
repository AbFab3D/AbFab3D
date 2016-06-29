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

import abfab3d.core.Grid;
import abfab3d.core.VoxelData;
import abfab3d.core.Bounds;

/**
 * A grid backed by arrays.
 *
 * Likely better performance for memory access that is not slice aligned.
 *
 * Uses the X3D coordinate system.  Y-up.  Grid is located
 * on positive right side octant.
 *
 * @author Alan Hudson
 */
public class ArrayAttributeGridShortIndexLong extends BaseAttributeGrid {
    protected short[][][] data;

    /**
     * Constructor.
     *
     * @param w The width in world coords
     * @param h The height in world coords
     * @param d The depth in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public ArrayAttributeGridShortIndexLong(double w, double h, double d, double pixel, double sheight) {
        this(roundSize(w / pixel),roundSize(h / sheight),roundSize(d / pixel),
                pixel,
                sheight, null);
    }

    /**
     * Constructor.
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public ArrayAttributeGridShortIndexLong(int w, int h, int d, double pixel, double sheight) {
        this(w,h,d,pixel,sheight,null);
    }

    /**
     * Constructor.
     *
     * @param w The width in world coords
     * @param h The height in world coords
     * @param d The depth in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public ArrayAttributeGridShortIndexLong(double w, double h, double d, double pixel, double sheight, InsideOutsideFunc ioFunc) {
        this(roundSize(w / pixel),roundSize(h / sheight),roundSize(d / pixel),
                pixel,
                sheight, ioFunc);
    }

    /**
     * Constructor.
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public ArrayAttributeGridShortIndexLong(int w, int h, int d, double pixel, double sheight, InsideOutsideFunc ioFunc) {
        super(w,h,d,pixel,sheight,ioFunc);

        data = new short[height][width][depth];
    }

    /**
     * Constructor.
     *
     * @param bounds The grid bounds in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public ArrayAttributeGridShortIndexLong(Bounds bounds, double pixel, double sheight) {

        super(bounds, pixel,sheight);
        data = new short[height][width][depth];
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
        Grid ret_val = new ArrayAttributeGridShortIndexLong(w,h,d,pixel,sheight, ioFunc);

        return ret_val;
    }

    /**
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public ArrayAttributeGridShortIndexLong(ArrayAttributeGridShortIndexLong grid) {
        super(grid.getWidth(), grid.getHeight(), grid.getDepth(),
                grid.getVoxelSize(), grid.getSliceHeight(),grid.ioFunc);
        this.data = grid.data.clone();
    }

    /**
     * Get a new instance of voxel data.  Returns this grids specific sized voxel data.
     *
     * @return The voxel data
     */
    public VoxelData getVoxelData() {
        return new VoxelDataShort();
    }

    /**
     * Get the data of the voxel
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public void getData(int x, int y, int z, VoxelData vd) {
        long encoded = data[y][x][z] & 0xFFFF;
        long att = ioFunc.getAttribute(encoded);
        byte state = ioFunc.getState(encoded);

        vd.setData(state,att);
    }

    /**
     * Get the data of the voxel
     *  @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public void getDataWorld(double x, double y, double z, VoxelData vd) {
        int slice = (int)((y-yorig) / sheight);
        int s_x =   (int)((x-xorig) / pixelSize);
        int s_z =   (int)((z-zorig) / pixelSize);

        long d = data[slice][s_x][s_z] & 0xFFFF;
        long att = ioFunc.getAttribute(d);
        byte state = ioFunc.getState(d);

        vd.setData(state, att);
    }

    /**
     * Get the state of the voxel
     *  @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public byte getStateWorld(double x, double y, double z) {
        int slice = (int)((y-yorig) / sheight);
        int s_x =   (int)((x-xorig) / pixelSize);
        int s_z =   (int)((z-zorig) / pixelSize);

        return ioFunc.getState(data[slice][s_x][s_z] & 0xFFFF);
    }

    /**
     * Get the state of the voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public byte getState(int x, int y, int z) {
        return ioFunc.getState(data[y][x][z] & 0xFFFF);
    }

    /**
     * Get the state of the voxel
     *  @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public long getAttributeWorld(double x, double y, double z) {
        int slice = (int)((y-yorig) / sheight);
        int s_x =   (int)((x-xorig) / pixelSize);
        int s_z =   (int)((z-zorig) / pixelSize);

        return ioFunc.getAttribute(data[slice][s_x][s_z] & 0xFFFF);
    }

    /**
     * Get the material of the voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public long getAttribute(int x, int y, int z) {
        return ioFunc.getAttribute(data[y][x][z] & 0xFFFF);
    }

    /**
     * Set the value of a voxel.
     *  @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The voxel state
     * @param material The material
     */
    public void setDataWorld(double x, double y, double z, byte state, long material) {
        int slice = (int)((y-yorig) / sheight);
        int s_x =   (int)((x-xorig) / pixelSize);
        int s_z =   (int)((z-zorig) / pixelSize);

        data[slice][s_x][s_z] = (short) ioFunc.combineStateAndAttribute(state,material);
    }

    /**
     * Set the value of a voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param state The voxel state
     * @param material The material
     */
    public void setData(int x, int y, int z, byte state, long material) {
        data[y][x][z] = (short) ioFunc.combineStateAndAttribute(state,material);
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
        data[y][x][z] = (short) ioFunc.updateAttribute(data[y][x][z] & 0xFFFF, material);
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
        long att = ioFunc.getAttribute(data[y][x][z]);
        data[y][x][z] = (short) ioFunc.combineStateAndAttribute(state,att);
    }

    /**
     * Set the state value of a voxel.  Leaves the material unchanged.
     *  @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 materialID
     */
    public void setStateWorld(double x, double y, double z, byte state) {
        int slice = (int)((y-yorig) / sheight);
        int s_x =   (int)((x-xorig) / pixelSize);
        int s_z =   (int)((z-zorig) / pixelSize);

        long att = ioFunc.getAttribute(data[slice][s_x][s_z] & 0xFFFF);
        data[slice][s_x][s_z] = (short) ioFunc.combineStateAndAttribute(state,att);
    }

    /**
     * Clone the object.
     */
    public Object clone() {
        ArrayAttributeGridShortIndexLong ret_val = new ArrayAttributeGridShortIndexLong(this);

        BaseGrid.copyBounds(this, ret_val);
        return ret_val;
    }
}

