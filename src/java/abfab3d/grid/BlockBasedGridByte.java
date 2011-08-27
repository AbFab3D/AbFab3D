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
 * A grid backed by blocked tiles.  Inspired by Field3D.  Later may
 * include optimizations done by db+grid
 *
 * Uses the X3D coordinate system.  Y-up.  Grid is located
 * on positive right side octant.
 *
 * TODO: support a clear that defines the empty value.  Would
 * help with files with lots of INSIDE and little OUTSIDE
 *
 * @author Alan Hudson
 */
public class BlockBasedGridByte extends BaseGrid {
    private static final int DEFAULT_BLOCK_ORDER = 4;

    /** Block order (size = 2^blockOrder) */
    protected int blockOrder;

    protected Block[] data;

    protected int blockResX;
    protected int blockResY;
    protected int blockResZ;
    protected int blockXZSize;

    /** preallocated outside return value */
    protected VoxelDataByte outside;

    /** Scratch block coord */
    private int[] bcoord;

    /** Scratch voxel coord */
    private int[] vcoord;

    /**
     * Constructor.
     *
     * @param w The width in world coords
     * @param h The height in world coords
     * @param d The depth in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public BlockBasedGridByte(double w, double h, double d, double pixel, double sheight) {
        this((int) (w / pixel) + 1, (int) (h / sheight) + 1,
           (int) (d / pixel) + 1, pixel, sheight, DEFAULT_BLOCK_ORDER);
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
    public BlockBasedGridByte(int w, int h, int d, double pixel, double sheight) {
        this(w,h,d,pixel,sheight,DEFAULT_BLOCK_ORDER);
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
    public BlockBasedGridByte(double w, double h, double d, double pixel, double sheight, int blockOrder) {
        this((int) (w / pixel) + 1, (int) (h / sheight) + 1,
           (int) (d / pixel) + 1, pixel, sheight, blockOrder);
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
    public BlockBasedGridByte(int w, int h, int d, double pixel, double sheight, int blockOrder) {
        super(w,h,d,pixel,sheight);

/*
        if (pixel != sheight)
            throw new IllegalArgumentException("BlockBasedGrid must be have equal voxel sizes");
*/

/*
        // TODO: Revisit whether we can relax this.  Looks possible
        int res = w;
        if(h > size)
            size = h;
        if(d > size)
            size = d;

        if (size % 2 != 0)
            size++;

        width = size;
        height = size;
        depth = size;
*/

        this.blockOrder = blockOrder;

//System.out.println("Req size: " + width + " " + height + " " + depth + " blockOrder: " + blockOrder);
        if (width < (int) Math.pow(2,blockOrder))
            width = (int) Math.pow(2,blockOrder);

        if (height < (int) Math.pow(2,blockOrder))
            height = (int) Math.pow(2,blockOrder);

        if (depth < (int) Math.pow(2,blockOrder))
            depth = (int) Math.pow(2,blockOrder);

        bcoord = new int[3];
        vcoord = new int[3];

//        data = new Block[(height >> blockOrder) * (width >> blockOrder) * (depth >> blockOrder)];

        blockResX = (int) Math.ceil((float)width / (1 << blockOrder));
        blockResY = (int) Math.ceil((float)height / (1 << blockOrder));
        blockResZ = (int) Math.ceil((float)depth / (1 << blockOrder));

//System.out.println("blockResX: " + blockResX + " " + blockResY + " " + blockResZ);
        blockXZSize = blockResX * blockResZ;
        data = new Block[blockResX * blockResY * blockResZ];

        outside = new VoxelDataByte(OUTSIDE, 0);
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

        // TODO: what to do about block order?
        Grid ret_val = new BlockBasedGridByte(w,h,d,pixel,sheight,4);

        return ret_val;
    }

    /**
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public BlockBasedGridByte(BlockBasedGridByte grid) {
        // TODO: what to do about block order?
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
     * @param The voxel state
     */
    public VoxelData getData(int x, int y, int z) {
        // Find block coord
        getBlockCoord(x, y, z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        Block block = data[id];

//System.out.println("gd: " + x + " " + y + " " + z + " id: " + id + " block: " + block);
        if (block != null) {
            // Find coord in block
            getVoxelInBlock(x, y, z, vcoord);

            byte val = block.getValue(vcoord, blockOrder);

            byte state = (byte) ((val & 0xFF) >> 6);
            byte mat = (byte) (0x3F & val);

            VoxelDataByte vd = new VoxelDataByte(state, mat);
            return vd;
        } else {
            return outside;
        }
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

        // Find block coord
        getBlockCoord(s_x, slice, s_z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        Block block = data[id];

        if (block != null) {
            // Find coord in block
            getVoxelInBlock(s_x, slice, s_z, vcoord);

            byte val = block.getValue(vcoord, blockOrder);

            byte state = (byte) ((val & 0xFF) >> 6);
            byte mat = (byte) (0x3F & val);

            VoxelDataByte vd = new VoxelDataByte(state, mat);
            return vd;
        } else {
            return outside;
        }
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

        // Find block coord
        getBlockCoord(s_x, slice, s_z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        Block block = data[id];

        if (block != null) {
            // Find coord in block
            getVoxelInBlock(s_x, slice, s_z, vcoord);

            byte val = block.getValue(vcoord, blockOrder);

            byte state = (byte) ((val & 0xFF) >> 6);

            return state;
        } else {
            return OUTSIDE;
        }
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
        // Find block coord
        getBlockCoord(x, y, z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        Block block = data[id];

        if (block != null) {
            // Find coord in block
            getVoxelInBlock(x, y, z, vcoord);

            byte val = block.getValue(vcoord, blockOrder);

            byte state = (byte) ((val & 0xFF) >> 6);

            return state;
        } else {
            return OUTSIDE;
        }
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

        // Find block coord
        getBlockCoord(s_x, slice, s_z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        Block block = data[id];

        if (block != null) {
            // Find coord in block
            getVoxelInBlock(s_x, slice, s_z, vcoord);

            byte val = block.getValue(vcoord, blockOrder);
            byte mat = (byte) (0x3F & val);

            return (int) mat;
        }

        return 0;
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
        // Find block coord
        getBlockCoord(x, y, z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        Block block = data[id];

        if (block != null) {
            // Find coord in block
            getVoxelInBlock(x, y, z, vcoord);

            byte val = block.getValue(vcoord, blockOrder);

            byte mat = (byte) (0x3F & val);

            return (int) mat;
        }

        return (int) OUTSIDE;
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

        // Find block coord
        getBlockCoord(s_x, slice, s_z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        Block block = data[id];

        // Find coord in block
        getVoxelInBlock(s_x, slice, s_z, vcoord);

        if (block != null) {

            block.setValue((byte) (0xFF & (state << 6 | ((byte)material))), vcoord, blockOrder);
        } else {
            block = new Block(blockOrder);
            data[id] = block;
            block.setValue((byte) (0xFF & (state << 6 | ((byte)material))), vcoord, blockOrder);
        }
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
        // Find block coord
        getBlockCoord(x, y, z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        Block block = data[id];
//System.out.println("id: " + id + " block: " + block);

        // Find coord in block
        getVoxelInBlock(x, y, z, vcoord);

        if (block != null) {
            block.setValue((byte) (0xFF & (state << 6 | ((byte)material))), vcoord, blockOrder);
        } else {
            block = new Block(blockOrder);
            data[id] = block;
            block.setValue((byte) (0xFF & (state << 6 | ((byte)material))), vcoord, blockOrder);
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
    public void setMaterial(int x, int y, int z, int material) {
        // Find block coord
        getBlockCoord(x, y, z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        Block block = data[id];

        // Find coord in block
        getVoxelInBlock(x, y, z, vcoord);

        if (block != null) {
            // TODO: can we do better then this?
            byte val = block.getValue(vcoord, blockOrder);
            byte state = (byte) ((val & 0xFF) >> 6);

            block.setValue((byte) (0xFF & (state << 6 | ((byte)material))), vcoord, blockOrder);
        } else {
            block = new Block(blockOrder);
            data[id] = block;
            byte state = OUTSIDE;

            block.setValue((byte) (0xFF & (state << 6 | ((byte)material))), vcoord, blockOrder);
        }
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
        // Find block coord
        getBlockCoord(x, y, z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        Block block = data[id];

        // Find coord in block
        getVoxelInBlock(x, y, z, vcoord);

        if (block != null) {
            // TODO: can we do better then this?
            byte val = block.getValue(vcoord, blockOrder);
            byte mat = (byte) (0x3F & val);

            block.setValue((byte) (0xFF & (state << 6 | ((byte)mat))), vcoord, blockOrder);
        } else {
            block = new Block(blockOrder);
            data[id] = block;
            byte mat = 0;

            block.setValue((byte) (0xFF & (state << 6 | ((byte)mat))), vcoord, blockOrder);
        }
    }

    /**
     * Clone the object.
     */
    public Object clone() {
        BlockBasedGridByte ret_val = new BlockBasedGridByte(this);

        return ret_val;
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void find(VoxelClasses vc, ClassTraverser t) {
        super.find(vc, t);
    /*
        // Traverse blocks instead

        int len = data.length;

        if (vc == VoxelClasses.OUTSIDE) {
            // Use slow route
            super.find(vc, t);
        }

        int[] coord = new int[3];

        for(int i=0; i < len; i++) {
            Block block = data[i];

            if (block == null) {
                continue;
            }


            byte[] data = block.getData();
            int len2 = data.length;
            for(int j=0; j < len2; j++) {
                byte state = (byte) ((data[j] & 0xFF) >> 6);
                byte mat = (byte) (0x3F & data[j]);

                VoxelDataByte vd = new VoxelDataByte(state,mat);

                getVoxelCoord(i,j, coord);

                switch(vc) {
                    case ALL:
                        t.found(coord[0],coord[1],coord[2],vd);
                        break;
                    case MARKED:
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            t.found(coord[0],coord[1],coord[2],vd);
                        }
                        break;
                    case EXTERIOR:
                        state = vd.getState();
                        if (state == Grid.EXTERIOR) {
                            t.found(coord[0],coord[1],coord[2],vd);
                        }
                        break;
                    case INTERIOR:
                        state = vd.getState();
                        if (state == Grid.INTERIOR) {
                            t.found(coord[0],coord[1],coord[2],vd);
                        }
                        break;
                }
            }
        }
*/
    }

    /**
     * Get the voxel coordinates from the blockID and position.
     *
     * @param blockID The blockID
     * @param pos The block position
     */
    private void getVoxelCoord(int blockID, int pos, int[] vcoord) {

// TODO: This needs work

    //    int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];
        vcoord[1] = (int) (blockID / blockXZSize);
        int subID = blockID - vcoord[1] * blockXZSize;
        vcoord[0] = subID / blockResZ;
        vcoord[2] = subID % blockResX;  // Not sure about this one

        // We have the upper left corner

        int y  = (int) (pos / blockXZSize);
        vcoord[1] += y;
        subID = pos - y * blockXZSize;
        vcoord[0] += subID / blockResZ;
        vcoord[1] += subID % blockResX;
    }

    /**
     * Get the block coordinates.
     *
     * @param x The x voxel coordinate
     * @param y The y voxel coordinate
     * @param z The z voxel coordinate
     * @coord The prealloacted block coordinate to return
     */
    private void getBlockCoord(int x, int y, int z, int[] coord) {
        coord[0] = x >> blockOrder;
        coord[1] = y >> blockOrder;
        coord[2] = z >> blockOrder;

//System.out.println("gbc: " + x + " " + y + " " + z + " val: " + java.util.Arrays.toString(coord));
    }

    /**
     * Get the voxel coordinate in the block
     *
     * @param x The x voxel coordinate
     * @param y The y voxel coordinate
     * @param z The z voxel coordinate
     * @coord The prealloacted block coordinate to return
     */
    private void getVoxelInBlock(int x, int y, int z, int[] coord) {

        // Bit shift should be ok, indices are always positive.

        coord[0] = x & ((1 << blockOrder) - 1);
        coord[1] = y & ((1 << blockOrder) - 1);
        coord[2] = z & ((1 << blockOrder) - 1);

//System.out.println("gvc: " + x + " " + y + " " + z + " val: " + java.util.Arrays.toString(coord));
    }

}

class Block {
    protected byte[] data;

    public Block(int blockOrder) {
        data = new byte[1 << blockOrder * 1 << blockOrder * 1 << blockOrder];
//        System.out.println("Created block: " + data.length + " this: " + this);
    }

    public byte getValue(int[] vcoord, int blockOrder) {
//System.out.println("bgv: " + java.util.Arrays.toString(vcoord));
        return data[(vcoord[2] << blockOrder << blockOrder) + (vcoord[1] << blockOrder) + vcoord[0]];

    }

    public void setValue(byte val, int[] vcoord, int blockOrder) {
//System.out.println("sv: " + val + " vcoord: " + java.util.Arrays.toString(vcoord));
        data[(vcoord[2] << blockOrder << blockOrder) + (vcoord[1] << blockOrder) + vcoord[0]] = val;
    }

    public byte[] getData() {
        return data;
    }
}