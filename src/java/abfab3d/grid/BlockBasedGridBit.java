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

import java.util.BitSet;

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
public class BlockBasedGridBit extends BaseGrid {
    private static final int DEFAULT_BLOCK_ORDER = 4;

    /** BlockOrder (size = 2^blockOrder) */
    protected int blockOrder;

    protected BlockBit[] data;

    protected int blockResX;
    protected int blockResY;
    protected int blockResZ;
    protected int blockXZSize;
    protected int blockMax;

    /** preallocated outside return value */
    protected final VoxelDataByte outside;

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
    public BlockBasedGridBit(double w, double h, double d, double pixel, double sheight) {
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
    public BlockBasedGridBit(int w, int h, int d, double pixel, double sheight) {
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
    public BlockBasedGridBit(double w, double h, double d, double pixel, double sheight, int blockOrder) {
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
    public BlockBasedGridBit(int w, int h, int d, double pixel, double sheight, int blockOrder) {
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
        this.blockMax = (1 << blockOrder) - 1;

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

//System.out.println("blockRes: " + blockResX + " " + blockResY + " " + blockResZ);
        blockXZSize = blockResX * blockResZ;
        data = new BlockBit[blockResX * blockResY * blockResZ];

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
        Grid ret_val = new BlockBasedGridBit(w,h,d,pixel,sheight,4);

        return ret_val;
    }

    /**
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public BlockBasedGridBit(BlockBasedGridBit grid) {
        // TODO: what to do about block order?
        super(grid.getWidth(), grid.getHeight(), grid.getDepth(),
                grid.getVoxelSize(), grid.getSliceHeight());
        
        this.blockOrder = grid.blockOrder;
        this.blockResX = grid.blockResX;
        this.blockResY = grid.blockResY;
        this.blockResZ = grid.blockResZ;
        this.blockXZSize = grid.blockXZSize;
        this.blockMax = grid.blockMax;
        this.outside = (VoxelDataByte) grid.outside.clone();
        this.bcoord = grid.bcoord.clone();
        this.vcoord = grid.vcoord.clone();
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
        // Find block coord
        getBlockCoord(x, y, z, bcoord);

        // Inline getBlockID call, confirm faster?
        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        BlockBit block = data[id];

//System.out.println("gd: " + x + " " + y + " " + z + " id: " + id + " block: " + block);
        if (block != null) {
            // Find coord in block
            getVoxelInBlock(x, y, z, vcoord);

            byte val = block.getValue(vcoord, blockOrder);

            byte state = val;

            VoxelDataByte vd = new VoxelDataByte(state, Grid.NO_MATERIAL);
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
     * @return The voxel state
     */
    public VoxelData getData(double x, double y, double z) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        // Find block coord
        getBlockCoord(s_x, slice, s_z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        BlockBit block = data[id];

        if (block != null) {
            // Find coord in block
            getVoxelInBlock(s_x, slice, s_z, vcoord);

            byte val = block.getValue(vcoord, blockOrder);

            byte state = val;

            VoxelDataByte vd = new VoxelDataByte(state, Grid.NO_MATERIAL);
            return vd;
        } else {
            return outside;
        }
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
        // Find block coord
        getBlockCoord(x, y, z, bcoord);

        // Inline getBlockID call, confirm faster?
        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        BlockBit block = data[id];

//System.out.println("gd: " + x + " " + y + " " + z + " id: " + id + " block: " + block);
        if (block != null) {
            // Find coord in block
            getVoxelInBlock(x, y, z, vcoord);

            byte val = block.getValue(vcoord, blockOrder);

            vd.setData(val, Grid.NO_MATERIAL);
        } else {
            vd.setData(Grid.OUTSIDE, Grid.NO_MATERIAL);
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
    public void getData(double x, double y, double z, VoxelData vd) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        // Find block coord
        getBlockCoord(s_x, slice, s_z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        BlockBit block = data[id];

        if (block != null) {
            // Find coord in block
            getVoxelInBlock(s_x, slice, s_z, vcoord);

            byte val = block.getValue(vcoord, blockOrder);

            vd.setData(val, Grid.NO_MATERIAL);
        } else {
            vd.setData(Grid.OUTSIDE, Grid.NO_MATERIAL);
        }
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

        // Find block coord
        getBlockCoord(s_x, slice, s_z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        BlockBit block = data[id];

        if (block != null) {
            // Find coord in block
            getVoxelInBlock(s_x, slice, s_z, vcoord);

            return block.getValue(vcoord, blockOrder);
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
     * @return The voxel state
     */
    public byte getState(int x, int y, int z) {
        // Find block coord
        getBlockCoord(x, y, z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];
        BlockBit block = data[id];

        if (block != null) {
            // Find coord in block
            getVoxelInBlock(x, y, z, vcoord);

            return block.getValue(vcoord, blockOrder);
        } else {
            return OUTSIDE;
        }
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

        BlockBit block = data[id];

        // Find coord in block
        getVoxelInBlock(s_x, slice, s_z, vcoord);

        if (block != null) {

            block.setValue(state, vcoord, blockOrder);
        } else {
            block = new BlockBit(blockOrder);
            data[id] = block;
            block.setValue(state, vcoord, blockOrder);
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

        BlockBit block = data[id];
//System.out.println("id: " + id + " block: " + block);

        // Find coord in block
        getVoxelInBlock(x, y, z, vcoord);

        if (block != null) {
            block.setValue(state, vcoord, blockOrder);
        } else {
            block = new BlockBit(blockOrder);
            data[id] = block;
            block.setValue(state, vcoord, blockOrder);
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
        // Find block coord
        getBlockCoord(x, y, z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        BlockBit block = data[id];

        // Find coord in block
        getVoxelInBlock(x, y, z, vcoord);

        if (block != null) {
            block.setValue(state, vcoord, blockOrder);
        } else {
            block = new BlockBit(blockOrder);
            data[id] = block;
            byte mat = 0;

            block.setValue(state, vcoord, blockOrder);
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
    public void setState(double x, double y, double z, byte state) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        // Find block coord
        getBlockCoord(s_x, slice, s_z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        BlockBit block = data[id];

        // Find coord in block
        getVoxelInBlock(s_x, slice, s_z, vcoord);

        if (block != null) {
            block.setValue(state, vcoord, blockOrder);
        } else {
            block = new BlockBit(blockOrder);
            data[id] = block;
            byte mat = 0;

            block.setValue(state, vcoord, blockOrder);
        }
    }

    /**
     * Clone the object.
     */
    public Object clone() {
        BlockBasedGridBit ret_val = new BlockBasedGridBit(this);

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
//        super.find(vc, t);

        // Sadly this is slower, why?
        // Its faster when sparse, slower when not.  I suspect its all
        // that math to get the x,y,z

        // Traverse blocks instead

        int len = data.length;

        if (vc == VoxelClasses.OUTSIDE || vc == VoxelClasses.ALL) {
            // Use slow route
            super.find(vc, t);

            return;
        }

        int[] coord = new int[3];

        for(int i=0; i < len; i++) {
            BlockBit block = data[i];

            if (block == null) {
                continue;
            }


            BitSet data = block.getData();
            int len2 = data.size() / 2;
            for(int j=0; j < len2; j++) {
                int loc = j << 1;
                boolean b1 = data.get(loc);
                boolean b2 = data.get(loc+1);

                byte state;

                if (b1) {
                    state = 1;
                } else {
                    state = 0;
                }

                if (b2) {
                    state += 2;
                }

                getVoxelCoord(i,j, coord);

                switch(vc) {
                    case ALL:
                        t.found(coord[0],coord[1],coord[2],state);
                        break;
                    case MARKED:
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            t.found(coord[0],coord[1],coord[2],state);
                        }
                        break;
                    case EXTERIOR:
                        if (state == Grid.EXTERIOR) {
                            t.found(coord[0],coord[1],coord[2],state);
                        }
                        break;
                    case INTERIOR:
                        if (state == Grid.INTERIOR) {
                            t.found(coord[0],coord[1],coord[2],state);
                        }
                        break;
                }
            }
        }
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttribute(VoxelClasses vc, ClassTraverser t) {
//        super.find(vc, t);


        // Sadly this is slower, why?
        // Its faster when sparse, slower when not.  I suspect its all
        // that math to get the x,y,z

        // Traverse blocks instead

        int len = data.length;

        if (vc == VoxelClasses.OUTSIDE || vc == VoxelClasses.ALL) {
            // Use slow route
            super.find(vc, t);

            return;
        }

        int[] coord = new int[3];

        for(int i=0; i < len; i++) {
            BlockBit block = data[i];

            if (block == null) {
                continue;
            }


            BitSet data = block.getData();
            int len2 = data.size() / 2;
            for(int j=0; j < len2; j++) {
                int loc = j << 1;
                boolean b1 = data.get(loc);
                boolean b2 = data.get(loc+1);

                byte state;

                if (b1) {
                    state = 1;
                } else {
                    state = 0;
                }

                if (b2) {
                    state += 2;
                }

                getVoxelCoord(i,j, coord);

                switch(vc) {
                    case ALL:
                        t.found(coord[0],coord[1],coord[2],state);
                        break;
                    case MARKED:
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            t.found(coord[0],coord[1],coord[2],state);
                        }
                        break;
                    case EXTERIOR:
                        if (state == Grid.EXTERIOR) {
                            t.found(coord[0],coord[1],coord[2],state);
                        }
                        break;
                    case INTERIOR:
                        if (state == Grid.INTERIOR) {
                            t.found(coord[0],coord[1],coord[2],state);
                        }
                        break;
                }
            }
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
//        super.find(vc, t);

        // Sadly this is slower, why?
        // Its faster when sparse, slower when not.  I suspect its all
        // that math to get the x,y,z

        // Traverse blocks instead

        int len = data.length;

        if (vc == VoxelClasses.OUTSIDE || vc == VoxelClasses.ALL) {
            // Use slow route
            super.findInterruptible(vc, t);

            return;
        }

        int[] coord = new int[3];

        loop:
        for(int i=0; i < len; i++) {
            BlockBit block = data[i];

            if (block == null) {
                continue;
            }


            BitSet data = block.getData();
            int len2 = data.size() / 2;
            for(int j=0; j < len2; j++) {
                int loc = j << 1;
                boolean b1 = data.get(loc);
                boolean b2 = data.get(loc+1);

                byte state;

                if (b1) {
                    state = 1;
                } else {
                    state = 0;
                }

                if (b2) {
                    state += 2;
                }

                getVoxelCoord(i,j, coord);

                switch(vc) {
                    case ALL:
                        if(!t.foundInterruptible(coord[0],coord[1],coord[2],state))
                            break loop;
                        break;
                    case MARKED:
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            if (!t.foundInterruptible(coord[0],coord[1],coord[2],state))
                                break loop;
                        }
                        break;
                    case EXTERIOR:
                        if (state == Grid.EXTERIOR) {
                            if (!t.foundInterruptible(coord[0],coord[1],coord[2],state))
                                break loop;
                        }
                        break;
                    case INTERIOR:
                        if (state == Grid.INTERIOR) {
                            if (!t.foundInterruptible(coord[0],coord[1],coord[2],state))
                                break loop;
                        }
                        break;
                }
            }
        }
    }

    /**
     * Get the voxel coordinates from the blockID and position.
     *
     * @param blockID The blockID
     * @param pos The block position
     */
    protected void getVoxelCoord(int blockID, int pos, int[] vcoord) {

        // Convert blockID to blockCoord
    //    int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];
        bcoord[1] = (int) (blockID / blockXZSize);
        int subID = blockID - bcoord[1] * blockXZSize;
        bcoord[0] = subID / blockResZ;
        bcoord[2] = subID % blockResZ;

        // convert blockCoord to voxel coordinate
        bcoord[0] = bcoord[0] << blockOrder;
        bcoord[1] = bcoord[1] << blockOrder;
        bcoord[2] = bcoord[2] << blockOrder;

//System.out.println("bcoord: " + java.util.Arrays.toString(bcoord));
        // We have the upper left corner

        //(vcoord[1] << blockOrder << blockOrder) + (vcoord[0] << blockOrder) + vcoord[2]];
        vcoord[1] = (pos >> blockOrder) >> blockOrder;
        subID = pos - (vcoord[1] << blockOrder << blockOrder);
//System.out.println("pos: " + pos + " vc1: " + vcoord[1] + " subID: " + subID);
        vcoord[0] = subID >> blockOrder;
//        vcoord[2] = subID % ((int)Math.pow(2, blockOrder));
        vcoord[2] = subID % (1 << blockOrder);
//System.out.println("mod: " + ((Math.pow(2, blockOrder))));
        // Reverse this
        //coord[0] = x & ((1 << blockOrder) - 1);
        //coord[1] = y & ((1 << blockOrder) - 1);
        //coord[2] = z & ((1 << blockOrder) - 1);

//        System.out.println("vcoord: " + java.util.Arrays.toString(vcoord));

        vcoord[0] += bcoord[0];
        vcoord[1] += bcoord[1];
        vcoord[2] += bcoord[2];

        //System.out.println("fin vcoord: " + java.util.Arrays.toString(vcoord));
    }

    /**
     * Get the position of the voxel coordinates in the data array.
     */
    protected int getBlockID(int[] bcoord) {
        return bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];
    }

    /**
     * Get the position of the voxel coordinates in block data array.
     */
    protected int getID(int[] vcoord) {
        return (vcoord[1] << blockOrder << blockOrder) + (vcoord[0] << blockOrder) + vcoord[2];
    }


    /**
     * Get the block coordinates.
     *
     * @param x The x voxel coordinate
     * @param y The y voxel coordinate
     * @param z The z voxel coordinate
     * @coord The prealloacted block coordinate to return
     */
    protected void getBlockCoord(int x, int y, int z, int[] coord) {
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
    protected void getVoxelInBlock(int x, int y, int z, int[] coord) {

        coord[0] = x & blockMax;
        coord[1] = y & blockMax;
        coord[2] = z & blockMax;

//System.out.println("gvc: " + x + " " + y + " " + z + " val: " + java.util.Arrays.toString(coord));
/*
        0 & 0 = 0
        0 & 1 = 0
        1 & 0 = 0
        1 & 1 = 1
*/
    }

}

/**
 * A 2 bit block storage method.
 */

class BlockBit {
    protected BitSet data;

    public BlockBit(int blockOrder) {
        data = new BitSet(1 << blockOrder << blockOrder << blockOrder * 2 * 8);
        //System.out.println("Created block: " + data.length + " this: " + this);
    }

    public byte getValue(int[] vcoord, int blockOrder) {
        int loc = ((vcoord[1] << blockOrder << blockOrder) + (vcoord[0] << blockOrder) + vcoord[2]) << 1;
        boolean b1 = data.get(loc);
        boolean b2 = data.get(loc+1);

        byte ret_val;
        if (b1) {
            ret_val = 1;
        } else {
            ret_val = 0;
        }

        if (b2) {
            ret_val += 2;
        }
//        System.out.println("bgv: " + java.util.Arrays.toString(vcoord) + " ret: " + ret_val);

        return ret_val;
    }

    public void setValue(byte val, int[] vcoord, int blockOrder) {
//System.out.println("sv: " + val + " vcoord: " + java.util.Arrays.toString(vcoord));
        int loc = ((vcoord[1] << blockOrder << blockOrder) + (vcoord[0] << blockOrder) + vcoord[2]) << 1;

        boolean b1 = (val & 1) == 1;
        boolean b2 = (val & 2) == 2;
//System.out.println("b1: " + b1 + " b2: " + b2);
        data.set(loc,b1);
        data.set(loc+1,b2);
    }

    public BitSet getData() {
        return data;
    }
}
