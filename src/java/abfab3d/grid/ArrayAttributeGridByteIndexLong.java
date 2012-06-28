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
public class ArrayAttributeGridByteIndexLong extends BaseAttributeGrid {
    protected byte[][][] data;

    /**
     * Constructor.
     *
     * @param w The width in world coords
     * @param h The height in world coords
     * @param d The depth in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public ArrayAttributeGridByteIndexLong(double w, double h, double d, double pixel, double sheight) {
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
    public ArrayAttributeGridByteIndexLong(int w, int h, int d, double pixel, double sheight) {
        super(w,h,d,pixel,sheight);

        // TODO: Align memory with slice access patterns.  Need to verify
        data = new byte[height][width][depth];
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
        Grid ret_val = new ArrayAttributeGridByteIndexLong(w,h,d,pixel,sheight);

        return ret_val;
    }

    /**
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public ArrayAttributeGridByteIndexLong(ArrayAttributeGridByteIndexLong grid) {
        super(grid.getWidth(), grid.getHeight(), grid.getDepth(),
            grid.getVoxelSize(), grid.getSliceHeight());
        this.data = grid.data.clone();
    }

    /**
     * Get the data of the voxel
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public VoxelData getData(int x, int y, int z) {
        byte datum = data[y][x][z];

        byte state = (byte) ((datum & 0xFF) >> 6);
        byte mat = (byte) (0x3F & datum);

        VoxelDataByte vd = new VoxelDataByte(state, mat);

        return vd;
    }

    /**
     * Get the data of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public VoxelData getData(double x, double y, double z) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        byte datum = data[slice][s_x][s_z];

        byte state = (byte) ((datum & 0xFF) >> 6);
        byte mat = (byte) (0x3F & datum);

        return new VoxelDataByte(state, mat);
    }

    /**
     * Get the data of the voxel
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public void getData(int x, int y, int z, VoxelData vd) {
        byte datum = data[y][x][z];

        byte state = (byte) ((datum & 0xFF) >> 6);
        byte mat = (byte) (0x3F & datum);

        vd.setData(state,mat);
    }

    /**
     * Get the data of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public void getData(double x, double y, double z, VoxelData vd) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        byte datum = data[slice][s_x][s_z];

        byte state = (byte) ((datum & 0xFF) >> 6);
        byte mat = (byte) (0x3F & datum);

        vd.setData(state,mat);
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public byte getState(double x, double y, double z) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        byte datum = data[slice][s_x][s_z];

        byte state = (byte) ((datum & 0xFF) >> 6);

        return state;
    }

    /**
     * Get the state of the voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public byte getState(int x, int y, int z) {
        byte datum = data[y][x][z];

        byte state = (byte) ((datum & 0xFF) >> 6);

        return state;
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public int getAttribute(double x, double y, double z) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        byte datum = data[slice][s_x][s_z];

        byte mat = (byte) (0x3F & datum);

        return (int) mat;
    }

    /**
     * Get the material of the voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public int getAttribute(int x, int y, int z) {
        int idx = y * sliceSize + x * depth + z;

        byte datum = data[y][x][z];

        byte mat = (byte) (0x3F & datum);

        return (int) mat;
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

        data[slice][s_x][s_z] = (byte) (0xFF & (state << 6 | ((byte)material)));
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
        data[y][x][z] = (byte) (0xFF & (state << 6 | ((byte)material)));
    }

    /**
     * Set the material value of a voxel.  Leaves the state unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param material The materialID
     */
    public void setAttribute(int x, int y, int z, int material) {
        byte state = (byte) ((data[y][x][z] & 0xFF) >> 6);

        data[y][x][z] = (byte) (0xFF & (state << 6 | ((byte)material)));
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
        byte mat = (byte) (0x3F & data[y][x][z]);

        data[y][x][z] = (byte) (0xFF & (state << 6 | ((byte)mat)));
    }

    /**
     * Set the state value of a voxel.  Leaves the material unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.
     */
    public void setState(double x, double y, double z, byte state) {

        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        byte mat = (byte) (0x3F & data[slice][s_x][s_z]);

        data[slice][s_x][s_z] = (byte) (0xFF & (state << 6 | ((byte)mat)));
    }

    /**
     * Clone the object.
     */
    public Object clone() {
        ArrayAttributeGridByteIndexLong ret_val = new ArrayAttributeGridByteIndexLong(this);

        return ret_val;
    }
}

