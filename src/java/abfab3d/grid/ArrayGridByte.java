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
public class ArrayGridByte extends BaseGrid {
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
    public ArrayGridByte(double w, double h, double d, double pixel, double sheight) {
        this(roundSize(w / pixel),roundSize(h / sheight),roundSize(d / pixel),
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
    public ArrayGridByte(int w, int h, int d, double pixel, double sheight) {
        super(w,h,d,pixel,sheight);

        long dataLength = (long)height * width * depth;

        if(dataLength >= Integer.MAX_VALUE){
            throw new IllegalArgumentException("Size exceeds integer, use ArrayGridByteLongIndex");
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
        return new ArrayGridByte(w,h,d,pixel,sheight);
    }

    /**
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public ArrayGridByte(ArrayGridByte grid) {
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
     * @param vd The voxel data
     */
    public void getData(int x, int y, int z, VoxelData vd) {
        int idx = y * sliceSize + x * depth + z;

        byte state = data[idx];

        vd.setData(state,0);
    }

    /**
     * Get the data of the voxel
     *  @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param vd The voxel data
     */
    public void getDataWorld(double x, double y, double z, VoxelData vd) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        int idx = slice * sliceSize + s_x * depth + s_z;

        byte state = data[idx];

        vd.setData(state,0);
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
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        int idx = slice * sliceSize + s_x * depth + s_z;

        return data[idx];
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

        return data[idx];
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

        data[idx] = state;
    }

    /**
     * Set the state value of a voxel.  Leaves the material unchanged.
     *  @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.
     */
    public void setStateWorld(double x, double y, double z, byte state) {

        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        int idx = slice * sliceSize + s_x * depth + s_z;

        data[idx] = state;
    }

    /**
     * Clone the object.
     */
    public Object clone() {
        ArrayGridByte ret_val = new ArrayGridByte(this);
        BaseGrid.copyBounds(this, ret_val);
        return ret_val;
    }

    //-----PROTOTYPE ---

    // Possible 2 styles, keep x,y,z or idx
    // Grid density will affect which approach is faster likely
    private int tIdx;
    private int minx;
    private int maxx;
    private int miny;
    private int maxy;
    private int tx;
    private int ty;
    private int tz;

    private void startFindInside(int minx, int maxx, int miny, int maxy) {
        this.minx = minx;
        this.maxx = maxx;
        this.miny = miny;
        this.maxy = maxy;
        tIdx = 0;
    }

    /**
     *
     */
    public boolean getNextIdx(VoxelCoordinate vc) {
        while(data[tIdx] != Grid.INSIDE) {
            tIdx++;
            if (tIdx > data.length) {
                return false;
            }
        }

        int x = tIdx;
        int y = tIdx / sliceSize;
        int z = tIdx;

        vc.setValue(x,y,z);
        return true;
    }

    /**
     *
     */
    public boolean getNextXYZ(VoxelCoordinate vc) {
        if (getState(tx,ty,tz) == Grid.INSIDE) {
            vc.setValue(tx,ty,tz);
            return true;
        }

        while(true) {
            tz++;
            if (tz >= depth) {
                tz = 0;
                tx++;
                if (tx >= width) {
                    tx = 0;
                    ty++;

                    if (ty >= height) {
                        return false;
                    }
                }
            }

            if (getState(tx,ty,tz) == Grid.INSIDE) {
                vc.setValue(tx,ty,tz);
                return true;
            }
        }
    }
}

