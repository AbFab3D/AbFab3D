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
public class ArrayAttributeGridByte extends BaseAttributeGrid {
    protected byte[] data;

    /**
     * Constructor.
     *
     * @param w The width in world coords
     * @param h The height in world coords
     * @param d The depth in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public ArrayAttributeGridByte(double w, double h, double d, double pixel, double sheight) {
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
    public ArrayAttributeGridByte(int w, int h, int d, double pixel, double sheight) {
        super(w,h,d,pixel,sheight);
        long dataLength = (long)height * width * depth;
        if(dataLength >= Integer.MAX_VALUE){
            System.out.printf("Out of memory");
            Thread.currentThread().dumpStack();
            return;
        }
        data = new byte[height * width * depth];
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
        Grid ret_val = new ArrayAttributeGridByte(w,h,d,pixel,sheight);

        return ret_val;
    }

    /**
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public ArrayAttributeGridByte(ArrayAttributeGridByte grid) {
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
     * @return The voxel state
     */
    public VoxelData getData(int x, int y, int z) {
        int idx = y * sliceSize + x * depth + z;

        byte state = (byte) ((data[idx] & 0xFF) >> 6);
        byte mat = (byte) (0x3F & data[idx]);

        VoxelDataByte vd = new VoxelDataByte(state, mat);

        return vd;
    }

    /**
     * Get the data of the voxel
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @return The voxel state
     */
    public void getData(int x, int y, int z, VoxelData vd) {
        int idx = y * sliceSize + x * depth + z;

        byte state = (byte) ((data[idx] & 0xFF) >> 6);
        byte mat = (byte) (0x3F & data[idx]);

        vd.setData(state,mat);
    }

    /**
     * Get the state of the voxels specified in the area.
     *
     * @param x1 The starting x grid coordinate
     * @param x2 The ending x grid coordinate
     * @param y1 The starting y grid coordinate
     * @param y2 The ending y grid coordinate
     * @param z1 The starting z grid coordinate
     * @param z2 The ending z grid coordinate
     *
     * @return Returns the data at each position.  3 dim array represented as flat, must be preallocated
     */
    public void getData(int x1, int x2, int y1, int y2, int z1, int z2, VoxelData[] ret) {

        int idx;
        byte state;
        byte mat;

        int ridx = 0;

// TODO: check whether array order matters for cache coherence
//System.out.println("x1: " + x1 + " x2: " + x2);
//System.out.println("y1: " + y1 + " y2: " + y2);
//System.out.println("z1: " + z1 + " z2: " + z2);

        int x_len = x2 - x1 + 1;
        int y_len = y2 - y1 + 1;
        int z_len = z2 - z1 + 1;

        for(int i=0; i < x_len; i++) {
            for(int j=0; j < y_len; j++) {
                for(int k=0; k < z_len; k++) {
//System.out.println("i: " + i + " j: " + j + " k: " + k);
                    idx = (j + y1) * sliceSize + (i + x1) * depth + (k + z1);
                    state = (byte) ((data[idx] & 0xFF) >> 6);
                    mat = (byte) (0x3F & data[idx]);

//System.out.println("Setting ridx: " + ridx);
                    ret[ridx++] = new VoxelDataByte(state, mat);
                }
            }
        }
    }

    /**
     * Get the data of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @return The voxel state
     */
    public VoxelData getData(double x, double y, double z) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        int idx = slice * sliceSize + s_x * depth + s_z;

        byte state = (byte) ((data[idx] & 0xFF) >> 6);
        byte mat = (byte) (0x3F & data[idx]);

        return new VoxelDataByte(state, mat);
    }

    /**
     * Get the data of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @return The voxel state
     */
    public void getData(double x, double y, double z, VoxelData vd) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        int idx = slice * sliceSize + s_x * depth + s_z;

        byte state = (byte) ((data[idx] & 0xFF) >> 6);
        byte mat = (byte) (0x3F & data[idx]);

        vd.setData(state, mat);
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
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        int idx = slice * sliceSize + s_x * depth + s_z;

        byte state = (byte) ((data[idx] & 0xFF) >> 6);

        return state;
    }

    /**
     * Get the state of the voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @return The voxel state
     */
    public byte getState(int x, int y, int z) {
        int idx = y * sliceSize + x * depth + z;

        byte state = (byte) ((data[idx] & 0xFF) >> 6);

        return state;
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @return The voxel material
     */
    public int getAttribute(double x, double y, double z) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        int idx = slice * sliceSize + s_x * depth + s_z;

        byte mat = (byte) (0x3F & data[idx]);

        return (int) mat;
    }

    /**
     * Get the material of the voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @return The voxel material
     */
    public int getAttribute(int x, int y, int z) {
        int idx = y * sliceSize + x * depth + z;

        byte mat = (byte) (0x3F & data[idx]);

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

        int idx = slice * sliceSize + s_x * depth + s_z;

        data[idx] = (byte) (0xFF & (state << 6 | ((byte)material)));
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

        data[idx] = (byte) (0xFF & (state << 6 | ((byte)material)));
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
        int idx = y * sliceSize + x * depth + z;

        byte state = (byte) ((data[idx] & 0xFF) >> 6);

        data[idx] = (byte) (0xFF & (state << 6 | ((byte)material)));
    }

    /**
     * Set the state value of a voxel.  Leaves the material unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.
     */
    public void setState(int x, int y, int z, byte state) {
        int idx = y * sliceSize + x * depth + z;

        byte mat = (byte) (0x3F & data[idx]);

        data[idx] = (byte) (0xFF & (state << 6 | ((byte)mat)));
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

        int idx = slice * sliceSize + s_x * depth + s_z;
        byte mat = (byte) (0x3F & data[idx]);

        data[idx] = (byte) (0xFF & (state << 6 | ((byte)mat)));
    }

    /**
     * Clone the object.
     */
    public Object clone() {
        ArrayAttributeGridByte ret_val = new ArrayAttributeGridByte(this);

        return ret_val;
    }
}

