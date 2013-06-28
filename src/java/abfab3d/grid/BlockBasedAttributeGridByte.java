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
public class BlockBasedAttributeGridByte extends BaseAttributeGrid {
    private static final int DEFAULT_BLOCK_ORDER = 4;

    /** BlockByteorder (size = 2^blockOrder) */
    protected int blockOrder;

    protected BlockByte[] data;

    protected int blockResX;
    protected int blockResY;
    protected int blockResZ;
    protected int blockXZSize;
    protected int blockMax;

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
    public BlockBasedAttributeGridByte(double w, double h, double d, double pixel, double sheight) {
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
    public BlockBasedAttributeGridByte(int w, int h, int d, double pixel, double sheight) {
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
    public BlockBasedAttributeGridByte(double w, double h, double d, double pixel, double sheight, int blockOrder) {
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
    public BlockBasedAttributeGridByte(int w, int h, int d, double pixel, double sheight, int blockOrder) {
        this(w,h,d,pixel,sheight,blockOrder,null);
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
    public BlockBasedAttributeGridByte(int w, int h, int d, double pixel, double sheight, int blockOrder, InsideOutsideFunc ioFunc) {
        super(w,h,d,pixel,sheight,ioFunc);

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
        data = new BlockByte[blockResX * blockResY * blockResZ];

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

        Grid ret_val = new BlockBasedAttributeGridByte(w,h,d,pixel,sheight,blockOrder, ioFunc);

        return ret_val;
    }

    /**
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public BlockBasedAttributeGridByte(BlockBasedAttributeGridByte grid) {
        // TODO: what to do about block order?
        super(grid.getWidth(), grid.getHeight(), grid.getDepth(),
            grid.getVoxelSize(), grid.getSliceHeight(), grid.ioFunc);

        this.blockOrder = grid.blockOrder;
        this.blockResX = grid.blockResX;
        this.blockResY = grid.blockResY;
        this.blockResZ = grid.blockResZ;
        this.blockXZSize = grid.blockXZSize;
        this.blockMax = grid.blockMax;
        this.outside = (VoxelDataByte) grid.outside.clone();
        this.bcoord = grid.bcoord.clone();
        this.vcoord = grid.vcoord.clone();

        data = new BlockByte[blockResX * blockResY * blockResZ];
        for(int b =0; b < data.length; b++){
            BlockByte bd = grid.data[b];
            if(bd != null){
                data[b] = (BlockByte)bd.clone();
            }
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

        BlockByte block = data[id];

//System.out.println("gd: " + x + " " + y + " " + z + " id: " + id + " block: " + block);
        if (block != null) {
            // Find coord in block
            getVoxelInBlock(x, y, z, vcoord);

            long encoded = block.getValue(vcoord, blockOrder) & 0xFF;
            long att = ioFunc.getAttribute(encoded);
            byte state = ioFunc.getState(encoded);

            vd.setData(state, att);
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

        BlockByte block = data[id];

        if (block != null) {
            // Find coord in block
            getVoxelInBlock(s_x, slice, s_z, vcoord);

            long encoded = block.getValue(vcoord, blockOrder) & 0xFF;
            long att = ioFunc.getAttribute(encoded);
            byte state = ioFunc.getState(encoded);

            vd.setData(state,att);
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

        BlockByte block = data[id];

        if (block != null) {
            // Find coord in block
            getVoxelInBlock(s_x, slice, s_z, vcoord);

            long encoded = block.getValue(vcoord, blockOrder) & 0xFF;
            return ioFunc.getState(encoded);
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
        BlockByte block = data[id];

        if (block != null) {
            // Find coord in block
            getVoxelInBlock(x, y, z, vcoord);

            long encoded = block.getValue(vcoord, blockOrder) & 0xFF;
            return ioFunc.getState(encoded);
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
     * @return The voxel material
     */
    public long getAttribute(double x, double y, double z) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        // Find block coord
        getBlockCoord(s_x, slice, s_z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        BlockByte block = data[id];

        if (block != null) {
            // Find coord in block
            getVoxelInBlock(s_x, slice, s_z, vcoord);

            long encoded = block.getValue(vcoord, blockOrder) & 0xFF;
            return ioFunc.getAttribute(encoded);
        }

        return Grid.NO_MATERIAL;
    }

    /**
     * Get the material of the voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @return The voxel material
     */
    public long getAttribute(int x, int y, int z) {
        // Find block coord
        getBlockCoord(x, y, z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        BlockByte block = data[id];

        if (block != null) {
            // Find coord in block
            getVoxelInBlock(x, y, z, vcoord);

            long encoded = block.getValue(vcoord, blockOrder) & 0xFF;
            return ioFunc.getAttribute(encoded);
        }

        return Grid.NO_MATERIAL;
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
    public void setData(double x, double y, double z, byte state, long material) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        // Find block coord
        getBlockCoord(s_x, slice, s_z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        BlockByte block = data[id];

        // Find coord in block
        getVoxelInBlock(s_x, slice, s_z, vcoord);

        if (block != null) {

            byte encoded = (byte) ioFunc.combineStateAndAttribute(state,material);

            block.setValue(encoded, vcoord, blockOrder);
        } else {
            block = new BlockByte(blockOrder);
            data[id] = block;
            byte encoded = (byte) ioFunc.combineStateAndAttribute(state,material);
            block.setValue(encoded, vcoord, blockOrder);
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
    public void setData(int x, int y, int z, byte state, long material) {
        // Find block coord
        getBlockCoord(x, y, z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        BlockByte block = data[id];
//System.out.println("id: " + id + " block: " + block);

        // Find coord in block
        getVoxelInBlock(x, y, z, vcoord);

        if (block != null) {
            byte encoded = (byte) ioFunc.combineStateAndAttribute(state,material);

            block.setValue(encoded, vcoord, blockOrder);
        } else {
            block = new BlockByte(blockOrder);
            data[id] = block;

            byte encoded = (byte) ioFunc.combineStateAndAttribute(state,material);

            block.setValue(encoded, vcoord, blockOrder);
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
        // Find block coord
        getBlockCoord(x, y, z, bcoord);

        int id = bcoord[1] * blockXZSize + bcoord[0] * blockResZ + bcoord[2];

        BlockByte block = data[id];

        // Find coord in block
        getVoxelInBlock(x, y, z, vcoord);

        if (block != null) {
            long encoded = block.getValue(vcoord, blockOrder) & 0xFF;
            long att = ioFunc.getAttribute(encoded);

            block.setValue((byte) ioFunc.updateAttribute(att, material), vcoord, blockOrder);
        } else {
            block = new BlockByte(blockOrder);
            data[id] = block;

            // OUTSIDE can't have a material
            block.setValue((byte)Grid.NO_MATERIAL, vcoord, blockOrder);
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

        BlockByte block = data[id];

        // Find coord in block
        getVoxelInBlock(x, y, z, vcoord);

        if (block != null) {

            switch (state) {
                case INSIDE:
                    long att = block.getValue(vcoord, blockOrder) & 0xFF;
                    block.setValue((byte)ioFunc.combineStateAndAttribute(state,att), vcoord, blockOrder);
                    break;
                case OUTSIDE:
                    block.setValue((byte)Grid.NO_MATERIAL, vcoord, blockOrder);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled state");
            }
        } else {
            block = new BlockByte(blockOrder);
            data[id] = block;

            block.setValue((byte)Grid.NO_MATERIAL, vcoord, blockOrder);
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

        BlockByte block = data[id];

        // Find coord in block
        getVoxelInBlock(s_x, slice, s_z, vcoord);

        if (block != null) {
            switch (state) {
                case INSIDE:
                    long att = block.getValue(vcoord, blockOrder) & 0xFF;
                    block.setValue((byte)ioFunc.combineStateAndAttribute(state,att), vcoord, blockOrder);
                    break;
                case OUTSIDE:
                    block.setValue((byte)Grid.NO_MATERIAL, vcoord, blockOrder);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled state");
            }
        } else {
            block = new BlockByte(blockOrder);
            data[id] = block;

            block.setValue((byte)Grid.NO_MATERIAL, vcoord, blockOrder);
        }
    }

    /**
     * Clone the object.
     */
    public Object clone() {
        BlockBasedAttributeGridByte ret_val = new BlockBasedAttributeGridByte(this);

        return ret_val;
    }

    /**
     * Get a new instance of voxel data.  Returns this grids specific sized voxel data.
     *
     * @return The voxel data
     */
    public VoxelData getVoxelData() {
        return new VoxelDataByte();
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

        if (vc == VoxelClasses.OUTSIDE) {
            // Use slow route
            super.find(vc, t);

            return;
        }

        int[] coord = new int[3];

        for(int i=0; i < len; i++) {
            BlockByte block = data[i];

            if (block == null) {
                continue;
            }


            byte[] data = block.getData();
            int len2 = data.length;
            for(int j=0; j < len2; j++) {
                byte state = ioFunc.getState(data[j] & 0xFF);

                getVoxelCoord(i,j, coord);

                switch(vc) {
                    case ALL:
                        t.found(coord[0],coord[1],coord[2],state);
                        break;
                    case INSIDE:
                        if (state > 0) {
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
    public void findAttribute(VoxelClasses vc, ClassAttributeTraverser t) {
        // Sadly this is slower, why?
        // Its faster when sparse, slower when not.  I suspect its all
        // that math to get the x,y,z

        // Traverse blocks instead

        int len = data.length;

        if (vc == VoxelClasses.OUTSIDE || vc == VoxelClasses.ALL) {
            // Use slow route
            super.findAttribute(vc, t);

            return;
        }

        int[] coord = new int[3];
        VoxelDataByte vd = new VoxelDataByte();

        for(int i=0; i < len; i++) {
            BlockByte block = data[i];

            if (block == null) {
                continue;
            }


            byte[] data = block.getData();
            int len2 = data.length;
            for(int j=0; j < len2; j++) {
                long d = data[j] & 0xFF;
                byte state = ioFunc.getState(d);
                byte mat = (byte) ioFunc.getAttribute(d);

                vd.setData(state,mat);

                getVoxelCoord(i,j, coord);

                switch(vc) {
                    case ALL:
                        t.found(coord[0],coord[1],coord[2],vd);
                        break;
                    case INSIDE:
                        if (state == Grid.INSIDE) {
                            t.found(coord[0],coord[1],coord[2],vd);
                        }
                        break;
                }
            }
        }
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
        // Sadly this is slower, why?
        // Its faster when sparse, slower when not.  I suspect its all
        // that math to get the x,y,z

        // Traverse blocks instead

        int len = data.length;

        if (vc == VoxelClasses.OUTSIDE || vc == VoxelClasses.ALL) {
            // Use slow route
            super.findAttribute(vc, t);

            return;
        }

        int[] coord = new int[3];
        VoxelDataByte vd = new VoxelDataByte();

        for(int i=0; i < len; i++) {
            BlockByte block = data[i];

            if (block == null) {
                continue;
            }


            byte[] data = block.getData();
            int len2 = data.length;
            for(int j=0; j < len2; j++) {
                byte material = (byte) ioFunc.getAttribute(data[j] & 0xFF);

                if (mat != material) {
                    continue;
                }

                byte state = ioFunc.getState(data[j]);
                vd.setData(state,material);

                getVoxelCoord(i,j, coord);

                switch(vc) {
                    case INSIDE:
                        if (state == Grid.INSIDE) {
                            t.found(coord[0],coord[1],coord[2],vd);
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

        if (vc == VoxelClasses.OUTSIDE) {
            // Use slow route
            super.findInterruptible(vc, t);

            return;
        }

        int[] coord = new int[3];

        loop:
        for(int i=0; i < len; i++) {
            BlockByte block = data[i];

            if (block == null) {
                continue;
            }


            byte[] data = block.getData();
            int len2 = data.length;
            for(int j=0; j < len2; j++) {
                byte state = ioFunc.getState(data[j] & 0xFF);

                getVoxelCoord(i,j, coord);

                switch(vc) {
                    case ALL:
                        if(!t.foundInterruptible(coord[0],coord[1],coord[2],state))
                            break loop;
                        break;
                    case INSIDE:
                        if (state == Grid.INSIDE) {
                            if (!t.foundInterruptible(coord[0],coord[1],coord[2],state))
                                break loop;
                        }
                        break;
                }
            }
        }
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
        // Sadly this is slower, why?
        // Its faster when sparse, slower when not.  I suspect its all
        // that math to get the x,y,z

        // Traverse blocks instead

        int len = data.length;

        if (vc == VoxelClasses.OUTSIDE) {
            // Use slow route
            super.findAttributeInterruptible(vc, t);

            return;
        }

        int[] coord = new int[3];
        VoxelDataByte vd = new VoxelDataByte();

        loop:
        for(int i=0; i < len; i++) {
            BlockByte block = data[i];

            if (block == null) {
                continue;
            }

            byte[] data = block.getData();
            int len2 = data.length;
            for(int j=0; j < len2; j++) {
                byte material = (byte) ioFunc.getAttribute(data[j] & 0xFF);

                if (mat != material) {
                    continue;
                }

                byte state = ioFunc.getState(data[j]);
                vd.setData(state,mat);

                getVoxelCoord(i,j, coord);

                switch(vc) {
                    case INSIDE:
                        if (state == Grid.INSIDE) {
                            if (!t.foundInterruptible(coord[0],coord[1],coord[2],vd))
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

class BlockByte {
    protected byte[] data;

    /**
       copyu contstructor
     */
    public BlockByte(BlockByte bb) {

        if(bb != null)
            data = bb.data.clone();
    }

    public BlockByte(int blockOrder) {
        data = new byte[1 << blockOrder << blockOrder << blockOrder];
        //System.out.println("Created block: " + data.length + " this: " + this);
    }

    public byte getValue(int[] vcoord, int blockOrder) {
//System.out.println("bgv: " + java.util.Arrays.toString(vcoord));
        return data[(vcoord[1] << blockOrder << blockOrder) + (vcoord[0] << blockOrder) + vcoord[2]];

    }

    public void setValue(byte val, int[] vcoord, int blockOrder) {
//System.out.println("sv: " + val + " vcoord: " + java.util.Arrays.toString(vcoord));
        data[(vcoord[1] << blockOrder << blockOrder) + (vcoord[0] << blockOrder) + vcoord[2]] = val;
    }

    public byte[] getData() {
        return data;
    }

    public Object clone(){

        return new BlockByte(this);

    }

}
