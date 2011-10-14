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
import java.io.*;

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
public class ArrayGridInt extends BaseGrid {
    protected int[] data;

    /**
     * Constructor.
     *
     * @param w The width in world coords
     * @param h The height in world coords
     * @param d The depth in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public ArrayGridInt(double w, double h, double d, double pixel, double sheight) {
        this((int) (Math.ceil(w / pixel)) + 1,
        	 (int) (Math.ceil(h / sheight)) + 1,
             (int) (Math.ceil(d / pixel)) + 1, 
             pixel, 
             sheight);
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
    public ArrayGridInt(int w, int h, int d, double pixel, double sheight) {
        super(w,h,d,pixel,sheight);

        data = new int[height * width * depth];
    }

    /**
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public ArrayGridInt(ArrayGridInt grid) {
        super(grid.getWidth(), grid.getHeight(), grid.getDepth(),
            grid.getVoxelSize(), grid.getSliceHeight());
        this.data = grid.data.clone();
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
        Grid ret_val = new ArrayGridInt(w,h,d,pixel,sheight);

        return ret_val;
    }

    /**
     * Get the data of the voxel
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param The voxel state
     */
    public VoxelData getData(int x, int y, int z) {
        int idx = y * sliceSize + x * depth + z;

        byte state = (byte) ((data[idx] & 0xFFFFFFFF) >> 30);
        int mat = (0x3FFFFFFF & data[idx]);

        VoxelDataInt vd = new VoxelDataInt(state, mat);

        return vd;
    }

    /**
     * Get the data of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param The voxel state
     */
    public VoxelData getData(double x, double y, double z) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        int idx = slice * sliceSize + s_x * depth + s_z;

        byte state = (byte) ((data[idx] & 0xFFFFFFFF) >> 30);
        int mat = (0x3FFFFFFF & data[idx]);

        return new VoxelDataInt(state, mat);
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
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        int idx = slice * sliceSize + s_x * depth + s_z;

        byte state = (byte) ((data[idx] & 0xFFFFFFFF) >> 30);

        return state;
    }

    /**
     * Get the state of the voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param The voxel state
     */
    public byte getState(int x, int y, int z) {
        int idx = y * sliceSize + x * depth + z;

        byte state = (byte) ((data[idx] & 0xFFFFFFFF) >> 30);

        return state;
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param The voxel material
     */
    public int getMaterial(double x, double y, double z) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        int idx = slice * sliceSize + s_x * depth + s_z;

        int mat = (0x3FFFFFFF & data[idx]);

        return mat;
    }

    /**
     * Get the material of the voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param The voxel material
     */
    public int getMaterial(int x, int y, int z) {
        int idx = y * sliceSize + x * depth + z;

        int mat = (0x3FFFFFFF & data[idx]);

        return mat;
    }

    /**
     * Set the value of a voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The voxel state
     * @param material The material
     */
    public void setData(double x, double y, double z, byte state, int material) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        int idx = slice * sliceSize + s_x * depth + s_z;

        data[idx] = (0xFFFFFFFF & (state << 30 | material));
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
    public void setData(int x, int y, int z, byte state, int material) {
        int idx = y * sliceSize + x * depth + z;

        data[idx] = (0xFFFFFFFF & (state << 30 | material));
    }

    /**
     * Set the material value of a voxel.  Leaves the state unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param material The materialID
     */
    public void setMaterial(int x, int y, int z, int material) {
        int idx = y * sliceSize + x * depth + z;

        byte state = (byte) ((data[idx] & 0xFFFFFFFF) >> 30);

        data[idx] = (0xFFFFFFFF & (state << 30 | material));
    }

    /**
     * Set the state value of a voxel.  Leaves the material unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 materialID
     * @param material The materialID
     */
    public void setState(int x, int y, int z, byte state) {
        int idx = y * sliceSize + x * depth + z;

        int mat = (0x3FFFFFFF & data[idx]);

        data[idx] = (0xFFFFFFFF & (state << 30 | mat));
    }

    /**
     * Clone the object.
     */
    public Object clone() {
        ArrayGridInt ret_val = new ArrayGridInt(this);

        return ret_val;
    }
}

