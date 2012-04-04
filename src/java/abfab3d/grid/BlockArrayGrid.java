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


/**
 * A BlockArrayGrid, inspired by DBGrid.
 * 
 * Each grid is composed of blocks, each block contains a set of points.
 * 
 * Memory is conserved by only instantiating one copy of the "outside" block
 * containing only inactive external voxels. However, they can be addressed
 * as if it were a fully populated explicit voxel space. It may be helpful to create
 * additional unique blocks where their states are homogeneous (interior regions).
 * 
 * This implementation should approach a 3D array of voxels w.r.t factors including
 * access time, speed and accessibility of operations, and within-block coherency.
 * However, the memory used may be MUCH less, depending on what data is loaded.
 * 
 * Uses the X3D coordinate system. Y-up. Grid is located on positive right side octant.
 * Voxels are assumed to be square in xz.
 * 
 * @author James Gray
 */
public class BlockArrayGrid extends BaseGrid {
    // dimensions of grid in blocks
// dimensions of blocks in voxels
// values given in 2^n
protected final int[] GRID_ORDER;
    protected final int[] BLOCK_ORDER;
    protected final int[] GRID_SIZE_IN_BLOCKS;
    protected final int[] BLOCK_SIZE_IN_VOXELS;
    protected final int[] GRID_SIZE_IN_VOXELS;
    
    // unified description of grid's dimensionality
    // { gridOrder[3]
    //   blockOrder[3]
    //   GRID_SIZE_IN_BLOCKS[3]
    //   BLOCK_SIZE_IN_VOXELS[3] }
    protected final int[] SIZER;
    
    // the outside block - the core of the DBGrid memory optimization is reusing this everywhere
    // 
    // support for other homogeneous blocks types be nice at some point, but mind the cost
    // due to multi-material & other attributes, the "inside" block from DBGrid is insufficient
    // using parallel voxel data may be a way to reclaim this? if we want to? is it needed?
    protected final Block OUTSIDE;
    
    // offset of the grid's origin in number of voxels
    protected double[] offset;
    
    // for converting grid units to world dimensions
    // world*scale = grid
    protected double[] scale;
    
    // storage for blocks, x is LSB & z is MSB, use c2i() to convert
    protected Block[] blocks;
    
    /**
     * Constructor.
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels (world size of voxels in xz)
     * @param sheight The slice height in meters (world size of voxels in y)
     * @param blockOrder The dimensionality of blocks in voxels, value as 2^n
     * @param gridOrigin The position of the origin voxel in world coords
     */
    public BlockArrayGrid(int w, int h, int d, double pixel, double sheight, int[] blockOrder, double[] gridOrigin) {
        // satisfy BaseGrid
    super(w,h,d,pixel,sheight);
        
        // set up the BlockArrayGrid...
    
    // record the block order and offset
    BLOCK_ORDER = blockOrder;
    offset = gridOrigin;
    
    // find invscale values
    scale = new double[] {1.0/pixel, 1.0/sheight, 1.0/pixel}; 
    
    // what are the dims of the blocks in voxels
    int[] blockWidth = {1 << blockOrder[0], 1 << blockOrder[1], 1 << blockOrder[2]};
    
    // what is the size of the grid in 2^n
    GRID_ORDER = new int[] {f.nextpow2((double) w / (double) blockWidth[0]),
    f.nextpow2((double) h / (double) blockWidth[1]),
    f.nextpow2((double) d / (double) blockWidth[2])};
    
    GRID_SIZE_IN_BLOCKS = new int[] {f.p2(GRID_ORDER[0]),
   f.p2(GRID_ORDER[1]),
   f.p2(GRID_ORDER[2])};
    BLOCK_SIZE_IN_VOXELS = new int[] {f.p2(BLOCK_ORDER[0]),
  f.p2(BLOCK_ORDER[1]),
  f.p2(BLOCK_ORDER[2])};
    GRID_SIZE_IN_VOXELS = new int[] {f.p2(BLOCK_ORDER[0]) * f.p2(GRID_ORDER[0]),
  f.p2(BLOCK_ORDER[1]) * f.p2(GRID_ORDER[1]),
  f.p2(BLOCK_ORDER[2]) * f.p2(GRID_ORDER[2])};
SIZER = new int[] { GRID_ORDER[0],
GRID_ORDER[1],
GRID_ORDER[2],
BLOCK_ORDER[0],
BLOCK_ORDER[1],
BLOCK_ORDER[2],
GRID_SIZE_IN_BLOCKS[0],
GRID_SIZE_IN_BLOCKS[1],
GRID_SIZE_IN_BLOCKS[2],
BLOCK_SIZE_IN_VOXELS[0],
BLOCK_SIZE_IN_VOXELS[1],
BLOCK_SIZE_IN_VOXELS[2]};
    
    // initialize the outside block
    OUTSIDE = new Block(SIZER,(byte)0);
    
    // initialize blocks with all OUTSIDE blocks (i.e. an empty grid)
    blocks = new Block[1 << GRID_ORDER[0] << GRID_ORDER[1] << GRID_ORDER[2]];
        for (int i = 0; i < blocks.length; i++) {
        blocks[i] = OUTSIDE;
        }
    }
    
    /**
     * Constructor.
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels (world size of voxels in xz)
     * @param sheight The slice height in meters (world size of voxels in y)
     * @param blockOrder The dimensionality of blocks in voxels, value as 2^n
     * @param gridOrigin The position of the origin voxel in world coords
     */
    public BlockArrayGrid(int w, int h, int d, double pixel, double sheight, int[] blockOrder) {
    this(w,h,d,pixel,sheight,blockOrder,new double[] {0,0,0});
    }
    
    /**
     * Constructor.
     *
     * @param w The width in world coords
     * @param h The height in world coords
     * @param d The depth in world coords
     * @param pixel The size of the pixels (world size of voxels in xz)
     * @param sheight The slice height in meters (world size of voxels in y)
     * @param blockOrder The dimensionality of blocks in voxels, value as 2^n
     * @param gridOrigin The position of the origin voxel in world coords
     */
    public BlockArrayGrid(double w, double h, double d, double pixel, double sheight, int[] blockOrder) {
    this(w,h,d,pixel,sheight,blockOrder,new double[] {0,0,0});
    }
    
    /**
     * Constructor using world coordinates.
     *
     * @param w The width in world coords
     * @param h The height in world coords
     * @param d The depth in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     * @param blockOrder The dimensionality of blocks in voxels, value as 2^n
     * @param gridOrigin The position of the origin voxel in world coords
     */
    public BlockArrayGrid(double w, double h, double d, double pixel, double sheight, int[] blockOrder, double[] gridOrigin) {
    this((int) (w / pixel) + 1, (int) (h / sheight) + 1, 
    (int) (d / pixel) + 1, pixel, sheight, blockOrder, gridOrigin);
    }
    
    /**
     * Constructor using some default values.
     * 
     * Defaults should be updated pending testing
     * of what constitutes good default values.
     * 
     * Grid size 4096^3, Block size 16^3.
     */
    public BlockArrayGrid() {
    this(4096.0,4096.0,4096.0,1.0,1.0,new int[] {4, 4, 4},new double[] {0.0,0.0,0.0});
    }
    
    /**
     * Create an empty grid.
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels (world size of voxels in xz)
     * @param sheight The slice height in meters (world size of voxels in y)
     * 
     * other params will be per the current object
     */
    public Grid createEmpty(int w, int h, int d, double pixel, double sheight) {
    return new BlockArrayGrid(w,h,d,pixel,sheight,BLOCK_ORDER,offset);
    }
    
    /**
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public BlockArrayGrid(BlockArrayGrid grid) {
        super(grid.getWidth(), grid.getHeight(), grid.getDepth(),
            grid.getVoxelSize(), grid.getSliceHeight());
        
        // clone stuff
    BLOCK_ORDER = grid.BLOCK_ORDER.clone();
    GRID_ORDER = grid.GRID_ORDER.clone();
    GRID_SIZE_IN_BLOCKS = grid.GRID_SIZE_IN_BLOCKS.clone();
        BLOCK_SIZE_IN_VOXELS = grid.BLOCK_SIZE_IN_VOXELS.clone();
        GRID_SIZE_IN_VOXELS = grid.GRID_SIZE_IN_VOXELS.clone();
        SIZER = grid.SIZER.clone();
    OUTSIDE = new Block(SIZER,(byte)0); // voxels are non-final, don't share
    offset = grid.offset.clone();
    scale = grid.scale.clone();
    blocks = grid.blocks.clone();
    }
    
    /**
     * Get size of blocks in voxels
     */
    public int[] blockSizeInVoxels() {
    return BLOCK_SIZE_IN_VOXELS;
    }
    
    /**
     * Get size of grid in blocks
     */
    public int[] gridSizeInBlocks() {
    return GRID_SIZE_IN_BLOCKS;
    }
    
    /**
     * Get size of grid in voxels
     */
    public int[] gridSizeInVoxels() {
    return GRID_SIZE_IN_VOXELS;
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
    return getVoxelData(x,y,z);
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
    return getVoxelData(x,y,z);
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
    return get(x,y,z);
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
    return get(x,y,z);
    }

    /**
     * Get the material of the voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @return The voxel material
     */
    public int getMaterial(double x, double y, double z) {
    return 0; // this is the no-material version
    }

    /**
     * Get the material of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @return The voxel material
     */
    public int getMaterial(int x, int y, int z) {
    return 0; // this is the no-material version
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
    int[] coord = worldToGrid(new double[] {x,y,z});
    set(coord[0],coord[1],coord[2],state);
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
    set(x,y,z,state);
    }

    /**
     * Set the material value of a voxel.
 * This is the no material version, so it actually does NOTHING!
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param material The materialID
     */
    public void setMaterial(int x, int y, int z, int material) {
        return; // this is the "no material" version
    }
    
    /**
     * Set the material value of a voxel.
     * This is the no material version, so it actually does NOTHING!
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param material The materialID
     */
    public void setMaterial(double x, double y, double z, int material) {
        return; // this is the "no material" version
    }

    /**
     * Set the state value of a voxel.  Leaves the material unchanged.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param state The value.
     */
    public void setState(int x, int y, int z, byte state) {
        set(x,y,z,state);
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
        set(x,y,z,state);
    }
    
    /**
     * Read a voxel to VoxelData by grid coordinate.
     * Currently assumes all active voxels are EXTERNAL.
     * This is "no material" version to mat = 0.
     * 
     * @param coord, the coord to read
     * @return a VoxelData object describing the voxel
     */
    protected VoxelData getVoxelData(int x, int y, int z) {
    return new VoxelDataByte((byte) get(x,y,z), 0);
    }
    
    /**
     * Read a voxel to VoxelData by world coordinate.
     * Currently assumes all active voxels are EXTERNAL.
     * This is "no material" version to mat = 0.
     * 
     * @param coord, the coord to read
     * @return a VoxelData object describing the voxel
     */
    protected VoxelData getVoxelData(double x, double y, double z) {
    int[] gc = worldToGrid(new double[] {x,y,z});
    return getVoxelData(gc[0],gc[1],gc[2]);
    }
    
    /**
     * Get a voxel's value by index.
     * 
     * @param index, the {blockIndex, voxelIndex} to get
     * @return the voxel's value
     */
    protected byte get(int[] index) {
    return blocks[index[0]].get(index[1]);
    }
    
    /**
     * Get voxel data by grid coords.
     * 
     * @param xyz, the voxel to read
     * @return the voxel's value
     */
    protected byte get(int x, int y, int z) {
    int[] gc = {x,y,z};
    return get(new int[] {f.blockIndex(gc,SIZER), f.voxelIndex(gc,SIZER)});
    }
    
    /**
     * Get voxel data by world coords.
     * 
     * @param xyz, the voxel to read
     * @return the voxel's value
     */
    protected byte get(double x, double y, double z) {
    int[] gc = worldToGrid(new double[] {x,y,z});
    return get(gc[0], gc[1], gc[2]);
    }
    
    /**
     * Set a voxel's value by index.
     * 
     * @param index, the {blockIndex, voxelIndex} to set
     * @param value, the data to set
     */
    protected void set(int[] index, byte value) {
    if (value != 0 & blocks[index[0]] == OUTSIDE) {
    blocks[index[0]] = new Block(SIZER);
    }
    if (blocks[index[0]].set(index[1], value)) {
    blocks[index[0]] = OUTSIDE;
    }
    }
    
    /**
     * Set a voxel's value by grid coords.
     * 
     * @param xyz, the coords of the voxel to set
     * @param value, the data to set
     */
    protected void set(int x, int y, int z, byte value) {
    int[] gc = {x,y,z};
    set(new int[] {f.blockIndex(gc,SIZER), f.voxelIndex(gc,SIZER)},value);
    }
    
    /**
     * Set a voxel's value by world coords.
     * 
     * @param xyz, the coords of the voxel to set
     * @param value, the data to set
     */
    protected void set(double x, double y, double z, byte value) {
    int[] gc = worldToGrid(new double[] {x,y,z});
    set(gc[0],gc[1],gc[2],value);
    }
    
    /**
     * Set all voxels within a block to a given value.
     * 
     * @param coord, a grid coordinate in the block
     * @param value, the data to set
     */
    protected void setBlock(int[] coord, byte value) {
    if (value == 0) {
    blocks[blockIndex(coord)] = OUTSIDE;
    } else {
    blocks[blockIndex(coord)].setAll(value);
    }
    }
    
    /**
     * Set all voxels within a block to a given value.
     * 
     * @param coord, a world coordinate in the block
     * @param value, the data to set
     */
    protected void setBlock(double[] coord, byte value) {
    setBlock(worldToGrid(coord),value);
    }
    
    /**
     * Convert world coordinates to grid coordinates.
     * 
     * @param coord, the world coordinate
     * @return the grid coordinate
     */
    protected int[] worldToGrid(double[] coord) {
    return new int[] {(int) Math.floor(coord[0]*scale[0]),
      (int) Math.floor(coord[1]*scale[1]),
      (int) Math.floor(coord[2]*scale[2])}; 
    }
    
    /**
     * Given a grid coord, return the containing block's index.
     * 
     * @param coord, the grid coordinate
     * @return the block index within grid's block array
     */
    protected int blockIndex(int[] coord) {
    return f.blockIndex(coord, SIZER);
    }
    
    /**
     * Given a world coord, return the containing block's index.
     * 
     * @param coord, the world coordinate
     * @return the block index within grid's block array
     */
    protected int blockIndex(double[] coord) {
    return blockIndex(worldToGrid(coord));
    }
    
    /**
     * Clone factory.
     */
    public Object clone() {
return new BlockArrayGrid(this);
}
}



/**
 * A block contains a PointSet and describes a contiguous subset
 * of the BlockArrayGrid's voxels. All blocks on the grid are assumed
 * to have the same general characteristics. Blocks and their PointSet
 * are preallocated upon creation. Two static blocks are expected,
 * one all-outside and one all-inside. Blocks which don't meet these
 * criteria are "dirty" blocks.
 * 
 * Optimizing memory usage by block attributes may be desirable, but
 * is assumed to be minor in comparison to optimizing voxel memory.
 * Types for block attributes (e.g. blockType) are currently chosen for
 * convenience of use, not memory overhead.
 * 
 * blockType: 0 = outside, 1 = dirty
 * 
 * In simple cases, this is possibly a redundant layer of abstraction.
 * However, it allows for tricks like multiple PointSet implementations
 * like different voxel data types or voxels of non-singular length.
 * 
 */
class Block {
    PointSet points;
    byte blockType;
    
    /**
     * Construct a new block.
     * 
     * @param sizer, the grid sizer
     * @param type, the type of block
     */
    public Block(int[] sizer, byte type) {
        blockType = type;
        points = new PointSet(sizer, type);
    }
    
    /**
     * Construct a new block. Assumed to be a "dirty" block.
     * 
     * @param sizer, the grid sizer
     */
    public Block(int[] sizer) {
        blockType = 1;  // dirty block
        points = new PointSet(sizer);
    }
    
    /**
     * Get the value of a voxel by index within block.
     * 
     * @param index, the index of the voxel within the block
     * @return the value of the voxel
     */
    public byte get(int index) {
        return points.get(index);
    }
    
    /**
     * Get the value of a voxel by coord within block.
     * 
     * @param coord, the coord of the voxel within the block
     * @param sizer, the grid sizer
     * @return the value of the voxel
     */
    public byte get(int[] coord, int[] sizer) {
        return points.get(coord,sizer);
    }
    
    /**
     * Set the value of a given voxel by index within block.
     * 
     * @param index, the index within the block to modify
     * @param value, the data to set
     * @return TRUE if the block should be replaced with OUTSIDE
     */
    public boolean set(int index, byte value) {
        return points.set(index, value);
    }
    
    /**
     * Set the value of a given voxel by coord within block.
     * 
     * @param coord, the x,y,z within the block to modify
     * @param sizer, the grid sizer
     * @param value, the data to set
     * @return TRUE if the block should be replaced with OUTSIDE
     */
    public boolean set(int[] coord, int[] sizer, byte value) {
        return points.set(coord, sizer, value);
    }
    
    /**
     * Set all voxels in block to the same value.
     * 
     * @param value, the data to set
     */
    public void setAll(byte value) {
        points.setAll(value);
    }
    
    /**
     * @return TRUE if all voxels in block have equal values
     */
    public boolean allEqual() {
    return points.allEqual();
    }
    
}


/**
 * A PointSet contains an array of point data and corresponds to
 * the contents of one block on the BlockArrayGrid.
 * 
 * The points in the PointSet are flattened 3D data. Ordering is such
 * that index = x + y*(xsize-1) + z*(xsize-1)*(ysize-1).
 * 
 * Points are stored as primitives for memory efficiency. Options in
 * Java are byte, short, int, long (8, 16, 32, and 64-bit). If more is
 * needed, multiples of the above would work.
 * 
 * If value == 0, the point is an OUTSIDE (inactive) point.
 */
class PointSet {
    protected byte[] data;
    protected int ndirty;
    
    /**
     * Construct a PointSet given a grid sizer.
     * 
     * @param sizer
     */
    public PointSet(int[] sizer) {
        data = new byte[1 << sizer[3] << sizer[4] << sizer[5]]; // xsize*ysize*zsize
        ndirty = 0;
    }
    
/**
 * 
 * @param sizer
 * @param value
 */
    public PointSet(int[] sizer, byte value) {
        data = new byte[1 << sizer[3] << sizer[4] << sizer[5]]; // xsize*ysize*zsize
        
        // is this a dirty block?
        if (value > 0) {
            ndirty = data.length;
        } else {
        ndirty = 0;
        }
        
        // set all the values
        setAll(value);
    }
    
    /**
     * Get voxel data by index within block.
     * 
     * @param index
     * @return
     */
    public byte get(int index) {
        return data[index];
    }
    
    /**
     * Get voxel data by coord within block.
     * 
     * @param coord, an (x,y,z) location within a block
     * @return data of voxel at coord
     */
    public byte get(int[] coord, int[] sizer) {
        return get(f.coordToIndex(coord, new int[] {sizer[3], sizer[4], sizer[5]}));
    }
    
    /**
     * Set voxel data by index within block.
     * 
     * @param index, the index of the voxel to modify
     * @param value, the data to set
     * @return TRUE if the block should become OUTSIDE
     */
    public boolean set(int index, byte value) {
    boolean r = false;
    
    // check whether we need to change ndirty
    //
    // TODO - remove this to gain speed?
    // 
    // there's some cost here
    // it may be worth omitting this in favor
    // of being less efficient at reclaiming
    // memory when blocks become clean
    // 
    // dirty blocks are expected to be orders
    // of magnitude less common compared to
    // clean OUTSIDE blocks, so periodically
    // doing a check against all dirty blocks
    // when memory becomes problematic may be
    // a more efficient method
    byte oldVal = data[index];
    if (oldVal != value) {
            if (oldVal == (byte) 0) {
                ndirty += 1;
            } else if (value == 0) {
                ndirty -= 1;
                if (ndirty <= 0) {
                    r = true;
                }
            }
        }
    
    // set the value
        data[index] = value;
    return r;
    }
    
    /**
     * Set voxel data by coord within block.
     * 
     * @param coord, the x,y,z within the block to modify
     * @param sizer, the grid sizer
     * @param value, the data to set
     * @return TRUE if block should be replaced with OUTSIDE
     */
    public boolean set(int[] coord, int[] sizer, byte value) {
    return set(f.coordToIndex(coord, new int[] {sizer[3], sizer[4], sizer[5]}),
       value);
    }
    
    /**
     * Set every voxel in the block to the same value.
     * 
     * @param value, the data to set
     */
    public void setAll(byte value) {
        for (int i = 0; i < data.length; i++) {
            data[i] = value;
        }
    }
    
    /**
     * Check if all voxels in the block have equal values.
     * 
     * @return TRUE if all voxels in block are equal-valued
     */
    public boolean allEqual() {
    for (int i = 1; i < data.length; i++) {
            if (data[i] != data[0]) return false;
        }
    return true;
    }
}

/**
 * Various helper functions.
 */
class f {
/**
 * Find the index of a block containing a given voxel coord within grid.
 * 
 * @param coord, the (x,y,z) of a voxel in the grid
 * @param sizer, {gridOrder[3]
 *    blockOrder[3]
 *  GRID_SIZE_IN_BLOCKS[3]
 *   BLOCK_SIZE_IN_VOXELS[3]}
 * @return index within grid's array of blocks
 */
static int blockIndex(int coord[], int[] sizer) {
return coordToIndex(new int[] {coord[0] >> sizer[3],
   coord[1] >> sizer[4],
   coord[2] >> sizer[5]},
new int[] {sizer[0],sizer[1],sizer[2]});
}

/**
 * Find the index of a voxel within a block, given voxel coord within grid.
 * 
 * @param coord, the (x,y,z) of a voxel in the grid
 * @param sizer, {gridOrder[0..2]
 *    blockOrder[3..5]
 *  GRID_SIZE_IN_BLOCKS[6..8]
 *   BLOCK_SIZE_IN_VOXELS[9..11]}
 * @return index within block's array of voxels
 */
static int voxelIndex(int coord[], int[] sizer) {
// TODO - can this be more efficient?
return coordToIndex( new int[] {coord[0] % sizer[9],
    coord[1] % sizer[10],
    coord[2] % sizer[11]},
     new int[] {sizer[3], sizer[4], sizer[5]});
}

/**
 * Quickly calculate an index within a flattened 3D grid.
 * 
 * The first value is assumed to be the least significant bit.
 * For example:
 * x y z | i
 * 0 0 0 | 0
 *      1 0 0 | 1
 *      0 1 0 | 2
 *      1 1 0 | 3  ...and so forth.
 * 
 * @param c, the xyz
 * @param ord, the order of each grid dimension as 2^n
 * @return the index
 */
static int coordToIndex(int[] c, int[] ord) {
return c[0] + (c[1] << ord[0]) + (c[2] << ord[0] << ord[1]);
}

/**
 * Find the order of the next power of two.
 * 
 * If already a power of two, return the order of value.
 * 
 * @param value
 * @return 2^n >= value
 */
static int nextpow2(int value) {
int p2 = 1;
int r = 0;
// check higher powers while not high enough
while (p2 < value) {
p2 = p2 << 1;
r += 1;
}
return r;
}

/**
 * Find the order of the next power of two.
 * 
 * If already a power of two, return the order of value.
 * 
 * @param value
 * @return 2^n >= value
 */
static int nextpow2(double value) {
return nextpow2((int) Math.ceil(value));
}

/**
 * Find 2^n.
 * 
 * @param value
 * @return 2^value
 */
static int p2(int value) {
// return (int) Math.pow(2,value);
return 1 << value;
}
}


