/*****************************************************************************
 *						Shapeways, Inc Copyright (c) 2012
 *							   Java Source
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
 * BlockArrayTwoBitGrid, a grid using ConstantBlocks for homogeneous
 * blocks and TwoBitBlocks for heterogeneous blocks.
 * 
 * Compared to non-blocked grids, lookup operations are slightly slower
 * as the 1D index of both the block and the voxel inside the block must
 * be calculated. However, this provides significantly improved scalability
 * with regard to memory requirements. Accessing voxels via find() is also
 * significantly faster.
 * 
 * Homogeneous constant blocks are not checked for aggressively, but
 * an attempt to reclaim blocks (GC allowing) can be made via clean().
 * 
 * @author James Gray
 */
public class BlockArrayTwoBitGrid extends BaseGrid {
	// block array
	protected Block[] blocks;
	
	// temp objects
	protected byte byt;
	protected int idx;
	
	// size of grid & block as power of two
	public final int[] GRID_TWOS_ORDER;
	protected final int[] BLOCK_TWOS_ORDER;
	
	// for index lookup calculations
	protected final int GRIDORDER_XPLUSZ;
	protected final int GRIDORDER_Z;
	protected final int BLOCKORDER_XPLUSZ;
	protected final int BLOCKORDER_Z;
	
	// width of grid in blocks, width of block in voxels
	protected final int[] GRID_SIZE_IN_BLOCKS;
	protected final int[] BLOCK_SIZE_IN_VOXELS;
	
	// last valid index on block = 2^BLOCK_TWOS_ORDER-1
	protected final int[] BLOCK_LAST_INDEX;
	
	// number of voxels per block
	public final int VOXELS_PER_BLOCK;
	
	// declare constant blocks
	static final Block OUTSIDE_BLOCK = new ConstantBlock(Grid.OUTSIDE);
	static final Block EXTERIOR_BLOCK = new ConstantBlock(Grid.EXTERIOR);
	static final Block INTERIOR_BLOCK = new ConstantBlock(Grid.INTERIOR);
	static final Block USERDEF_BLOCK = new ConstantBlock(Grid.USER_DEFINED);
	
	// preallocated return VoxelDataBytes
	protected final VoxelDataByte VOXEL_OUTSIDE = new VoxelDataByte(Grid.OUTSIDE, Grid.NO_MATERIAL);
	protected final VoxelDataByte VOXEL_EXTERIOR = new VoxelDataByte(Grid.EXTERIOR, Grid.NO_MATERIAL);
	protected final VoxelDataByte VOXEL_INTERIOR = new VoxelDataByte(Grid.INTERIOR, Grid.NO_MATERIAL);
	protected final VoxelDataByte VOXEL_USER_DEFINED = new VoxelDataByte(Grid.USER_DEFINED, Grid.NO_MATERIAL);
	
	// how many times a block has received a set() command since last clear
	protected int[] activity;
	
	/**
	 * Constructor, grid size in voxels.
	 * 
	 * @param w, the desired width along the x axis in voxels
	 * @param h, the desired height along the y axis in voxels
	 * @param d, the deisred depth along the z axis in voxels
	 * @param pixel, the x-z scale of a voxel
	 * @param sheight, the y height of a voxel
	 * @param blockTwosOrder, the [x,y,z] scale of a block in voxels as 2^n
	 */
	public BlockArrayTwoBitGrid(int w, int h, int d, double pixel, double sheight, int[] blockTwosOrder) {
		// satisfy BaseGrid
		super(((1<<blockTwosOrder[0]) * (int) Math.ceil(((double)w)/((double)(1<<blockTwosOrder[0])))),
			  ((1<<blockTwosOrder[1]) * (int) Math.ceil(((double)h)/((double)(1<<blockTwosOrder[1])))),
			  ((1<<blockTwosOrder[2]) * (int) Math.ceil(((double)d)/((double)(1<<blockTwosOrder[2])))),pixel,sheight);
		
		// set up the grid
		BLOCK_TWOS_ORDER = blockTwosOrder;
		GRID_TWOS_ORDER = new int[] {f.nextpow2((double) w / (double) (1<<BLOCK_TWOS_ORDER[0])),
									 f.nextpow2((double) h / (double) (1<<BLOCK_TWOS_ORDER[1])),
									 f.nextpow2((double) d / (double) (1<<BLOCK_TWOS_ORDER[2]))};
		
		GRID_SIZE_IN_BLOCKS = new int[] {1<<BLOCK_TWOS_ORDER[0],1<<BLOCK_TWOS_ORDER[1],1<<BLOCK_TWOS_ORDER[2]};
		BLOCK_SIZE_IN_VOXELS = new int[] {1<<BLOCK_TWOS_ORDER[0],1<<BLOCK_TWOS_ORDER[1],1<<BLOCK_TWOS_ORDER[2]};
		VOXELS_PER_BLOCK = BLOCK_SIZE_IN_VOXELS[0]*BLOCK_SIZE_IN_VOXELS[1]*BLOCK_SIZE_IN_VOXELS[2];
		BLOCK_LAST_INDEX = new int[] {GRID_SIZE_IN_BLOCKS[0]-1,GRID_SIZE_IN_BLOCKS[1]-1,GRID_SIZE_IN_BLOCKS[2]-1};
		
		GRIDORDER_XPLUSZ = GRID_TWOS_ORDER[0]+GRID_TWOS_ORDER[2];
		GRIDORDER_Z = GRID_TWOS_ORDER[2];
		BLOCKORDER_XPLUSZ = BLOCK_TWOS_ORDER[0]+BLOCK_TWOS_ORDER[2];
		BLOCKORDER_Z = BLOCK_TWOS_ORDER[2];
		
		// initialize blocks array
		blocks = new Block[(1<<GRID_TWOS_ORDER[0])*(1<<GRID_TWOS_ORDER[1])*(1<<GRID_TWOS_ORDER[2])];
		for (int i = 0; i < blocks.length; i++) {
			blocks[i] = OUTSIDE_BLOCK;
		}
		activity = new int[blocks.length];
	}
	
	/**
	 * Constructor, grid size in world coordinates.
	 * 
	 * @param w, the desired width along the x axis in world scale
	 * @param h, the desired height along the y axis in world scale
	 * @param d, the deisred depth along the z axis in world scale
	 * @param pixel, the x-z scale of a voxel
	 * @param sheight, the y height of a voxel
	 * @param blockTwosOrder, the [x,y,z] scale of a block in voxels as 2^n
	 */
	public BlockArrayTwoBitGrid(double w, double h, double d, double pixel, double sheight, int[] blockTwosOrder) {
		this((int) (w/pixel)+1, (int) (h/sheight)+1, (int) (d/pixel)+1, pixel, sheight, blockTwosOrder);
	}
	
	/**
	 * Constructor, grid size in voxels. Use blocks of 16x16x16 voxels.
	 * 
	 * @param w, the desired width along the x axis in voxels
	 * @param h, the desired height along the y axis in voxels
	 * @param d, the deisred depth along the z axis in voxels
	 * @param pixel, the x-z scale of a voxel
	 * @param sheight, the y height of a voxel
	 */
	public BlockArrayTwoBitGrid(int w, int h, int d, double pixel, double sheight) {
		this(w, h, d, pixel, sheight, new int[] {4,4,4});
	}
	
	/**
	 * Constructor, grid size in world coordinates. Use blocks of 16x16x16 voxels.
	 * 
	 * @param w, the desired width along the x axis in world scale
	 * @param h, the desired height along the y axis in world scale
	 * @param d, the deisred depth along the z axis in world scale
	 * @param pixel, the x-z scale of a voxel
	 * @param sheight, the y height of a voxel
	 */
	public BlockArrayTwoBitGrid(double w, double h, double d, double pixel, double sheight) {
		this((int) (w/pixel)+1, (int) (h/sheight)+1, (int) (d/pixel)+1, pixel, sheight, new int[] {4,4,4});
	}
	
	/**
	 * Copy constructor.
	 */
	public BlockArrayTwoBitGrid(BlockArrayTwoBitGrid grid) {
		this(grid.width, grid.height, grid.depth, grid.pixelSize, grid.sheight, grid.BLOCK_TWOS_ORDER);
		blocks = grid.blocks.clone();
	}
	
	/**
	 * Get an empty grid of the same type as this grid, inheriting block size.
	 */
	public Grid createEmpty(int w, int h, int d, double pixel, double sheight) {
		return new BlockArrayTwoBitGrid(w,h,d,pixel,sheight,BLOCK_TWOS_ORDER);
	}
	
    /**
     * Get the VoxelData of a voxel given grid coordinates.
     */
    public void getData(int x, int y, int z, VoxelData vd) {
        switch(getState(x,y,z)) {
            case Grid.OUTSIDE:
                vd.setState(Grid.OUTSIDE);
                break;
            case Grid.EXTERIOR:
                vd.setState(Grid.EXTERIOR);
                break;
            case Grid.INTERIOR:
                vd.setState(Grid.INTERIOR);
                break;
            default:
                vd.setState(Grid.USER_DEFINED);
                break;
        }
    }

    /**
     * Get the VoxelData of a voxel given world coordinates.
     */
    public void getData(double x, double y, double z, VoxelData vd) {
        switch(getState(func.w2v(x,pixelSize),func.w2v(y,sheight),func.w2v(z,pixelSize))) {
            case Grid.OUTSIDE:
                vd.setState(Grid.OUTSIDE);
                break;
            case Grid.EXTERIOR:
                vd.setState(Grid.EXTERIOR);
                break;
            case Grid.INTERIOR:
                vd.setState(Grid.INTERIOR);
                break;
            default:
                vd.setState(Grid.USER_DEFINED);
                break;
        }
    }

	/**
	 * Get the state of a voxel using grid coordinates.
	 */
	public byte getState(int x, int y, int z) {
		return blocks[func.blockIdx(x,y,z,BLOCK_TWOS_ORDER,GRIDORDER_Z,GRIDORDER_XPLUSZ)].get(
				func.voxelIdx(x,y,z,BLOCK_LAST_INDEX,BLOCKORDER_Z,BLOCKORDER_XPLUSZ));
	}
	
	/**
	 * Get the state of a voxel using world coordinates.
	 */
	public byte getState(double x, double y, double z) {
		return getState(func.w2v(x,pixelSize),func.w2v(y,sheight),func.w2v(z,pixelSize));
	}
	
	/**
	 * Set the state of a voxel using grid coordinates.
	 */
	public void setState(int x, int y, int z, byte state) {
		idx = func.blockIdx(x,y,z,BLOCK_TWOS_ORDER,GRIDORDER_Z,GRIDORDER_XPLUSZ);
		if (blocks[idx] instanceof TwoBitBlock) {
			// set the voxel state and increment the block activity counter
			blocks[idx].set(func.voxelIdx(x,y,z,BLOCK_LAST_INDEX,BLOCKORDER_Z,BLOCKORDER_XPLUSZ),state);
			activity[idx] += 1;
		} else {
			// only replace a ConstantBlock if we need a conflicting voxel state
			byt = blocks[idx].get(0);
			if (state != byt) {
				// create a new block, filled with state of the ConstantBlock it replaces
				if (byt != Grid.OUTSIDE) {
					blocks[idx] = new TwoBitBlock(VOXELS_PER_BLOCK,byt);
				} else {
					blocks[idx] = new TwoBitBlock(VOXELS_PER_BLOCK);
				}
				// set the voxel state and increment the block activity counter
				blocks[idx].set(func.voxelIdx(x,y,z,BLOCK_LAST_INDEX,BLOCKORDER_Z,BLOCKORDER_XPLUSZ),state);
				activity[idx] += 1;
			}
		}
	}
	
	/**
	 * Set the state of a voxel using world coordinates.
	 */
	public void setState(double x, double y, double z, byte state) {
		setState(func.w2v(x,pixelSize),func.w2v(y,sheight),func.w2v(z,pixelSize),state);
	}
	
	/**
	 * Find voxels of the requested class and report their coordinates.
	 *
	 * @param vc The class of voxels to traverse
	 * @param t The traverser to call for each voxel
	 */
	public void find(VoxelClasses vc, ClassTraverser t) {
		for (int x0 = 0; x0 < width; x0+=BLOCK_SIZE_IN_VOXELS[0]) {
			for (int y0 = 0; y0 < height; y0+=BLOCK_SIZE_IN_VOXELS[1]) {
				for (int z0 = 0; z0 < depth; z0+=BLOCK_SIZE_IN_VOXELS[2]) {
					idx = func.blockIdx(x0,y0,z0,BLOCK_TWOS_ORDER,GRIDORDER_Z,GRIDORDER_XPLUSZ);
					if (blocks[idx] instanceof ConstantBlock) {
						// same state for all contained voxels
						byt = blocks[idx].get(0);
						// check if block meets requested type
						switch (vc) {
							case OUTSIDE:
								if (byt != Grid.OUTSIDE) continue;
								break;
							case EXTERIOR:
								if (byt != Grid.EXTERIOR) continue;
								break;
							case INTERIOR:
								if (byt != Grid.INTERIOR) continue;
								break;
							case MARKED:
								if ((byt != Grid.EXTERIOR) & (byt != Grid.INTERIOR)) continue;
								break;
						}
						// set all voxels within this homogeneous block
						for (int x = x0; x < x0+BLOCK_SIZE_IN_VOXELS[0]; x++) {
							for (int y = y0; y < y0+BLOCK_SIZE_IN_VOXELS[1]; y++) {
								for (int z = z0; z < z0+BLOCK_SIZE_IN_VOXELS[2]; z++) {
									// push coords & state
									t.found(x,y,z,byt);
								}
							}
						}
					} else {
						// check voxel states individually
						for (int x = x0; x < x0+BLOCK_SIZE_IN_VOXELS[0]; x++) {
							for (int y = y0; y < y0+BLOCK_SIZE_IN_VOXELS[1]; y++) {
								for (int z = z0; z < z0+BLOCK_SIZE_IN_VOXELS[2]; z++) {
									byt = blocks[idx].get(func.voxelIdx(x,y,z,BLOCK_LAST_INDEX,BLOCKORDER_Z,BLOCKORDER_XPLUSZ));
									// check if block meets requested type
									switch (vc) {
										case OUTSIDE:
											if (byt != Grid.OUTSIDE) continue;
											break;
										case EXTERIOR:
											if (byt != Grid.EXTERIOR) continue;
											break;
										case INTERIOR:
											if (byt != Grid.INTERIOR) continue;
											break;
										case MARKED:
											if (byt != Grid.EXTERIOR & byt != Grid.INTERIOR) continue;
											break;
										// if requested ALL, no need to break out of block
									}
									// push coords & state
									t.found(x,y,z,byt);
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Attempt to reclaim blocks with homogeneous state and
	 * replace them with constant blocks. Invoke the GC and
	 * pray for an increase in free memory.
	 * 
	 */
	public void clean() {
		for (int i = 0; i < blocks.length; i++) {
			// skip block if we haven't used it enough to fill it
			// activity is nominally zero for constant blocks
			if (activity[i] < VOXELS_PER_BLOCK) continue;
			
			// is it homogeneous?
			if (blocks[i].allEqual(VOXELS_PER_BLOCK)) {
				// set it to the appropriate constant block
				switch (blocks[i].get(0)) {
					case Grid.OUTSIDE:
						blocks[i] = OUTSIDE_BLOCK;
						break;
					case Grid.EXTERIOR:
						blocks[i] = EXTERIOR_BLOCK;
						break;
					case Grid.INTERIOR:
						blocks[i] = INTERIOR_BLOCK;
						break;
					default:
						blocks[i] = USERDEF_BLOCK;
				}
				// reset activity upon clean
				activity[i] = 0;
			}
		}
		// we hopefully orphaned some stuff
		System.gc();
	}
	
	/**
	 * Clone factory.
	 */
	public BlockArrayTwoBitGrid clone() {
		return new BlockArrayTwoBitGrid(this);
	}
}



/**
 * A block using two bits per voxel. Should afford minimal memory
 * cost at this data complexity, short of spending significantly
 * more CPU time to compress the block. CPU efficiency should be
 * high, although it could be improved by using one primitive per
 * voxel instead of four voxels per primitive.
 *
 * @author James Gray
 */
class TwoBitBlock implements Block {
	protected TwoBitArray voxels;
	
	/**
	 * Constructor.
	 * 
	 * @param voxelsPerBlock = xSize*ySize*zSize of block in voxels
	 */
	public TwoBitBlock(int voxelsPerBlock) {
		voxels = new TwoBitArray(voxelsPerBlock);
	}
	
	/**
	 * Constructor for non-outside homogeneous blocks.
	 * 
	 * @param voxelsPerBlock = xSize*ySize*zSize of block in voxels
	 */
	public TwoBitBlock(int voxelsPerBlock, byte state) {
		voxels = new TwoBitArray(voxelsPerBlock);
		voxels.setAll(state);
	}
	
	/**
	 * Get a state.
	 * 
	 * @param i, the 1D index of the bin inside the block
	 * @return the two-bit state stored in the bin
	 */
	public byte get(int i) {
		return voxels.get(i);
	}
	
	/**
	 * Set a state.
	 * 
	 * @param i, the bin index within the block to set
	 * @param state, the state to set, assumed two-bit
	 */
	public void set(int i, byte state) {
		voxels.set(i, state);
	}
	
	/**
	 * Set all states to state.
	 * 
	 * @param state, the state to set, assumed two-bit
	 */
	public void setAll(byte state) {
		voxels.setAll(state);
	}
	
	/**
	 * Check whether all states are equal.
	 */
	public boolean allEqual(int voxelsPerBlock) {
		return voxels.allEqual(voxelsPerBlock);
	}
	
	/**
	 * Clone factory.
	 */
	public TwoBitBlock clone() {
		TwoBitBlock r = new TwoBitBlock(voxels.size());
		r.voxels = (TwoBitArray) voxels.clone();
		return r;
	}
}



/**
 * An array of two-bit entries, stored as byte[] in memory.
 * 
 * Additional memory overhead vs. byte[] should be minimal,
 * while the methods available use efficient bitwise ops.
 * 
 * @author James Gray
 */
class TwoBitArray {
	protected byte[] data;
	
	/**
	 * Constructor.
	 * 
	 * @param nbins, the number of two-bit bins to construct an array of
	 */
	public TwoBitArray(int nbins) {
		int nbytes = nbins >> 2;
		if ((nbytes&0x3) > 0 || nbytes == 0) {
			nbytes += 1;
		}
		data = new byte[nbytes];
	}
	
	/**
	 * Get the state of a bin.
	 * 
	 * @param i, the ith two-bit bin to get the state of.
	 * @return the bin's state, as the first two bits of a byte.
	 */
	public byte get(int i) {
		return (byte) ((data[i>>2] >>> ((i&3)<<1)) & 0x3);
	}
	
	/**
	 * Set the state of a bin.
	 * 
	 * @param i, the ith two-bit bin to get the state of.
	 * @param b, first two bits are the state, following bits are zeros.
	 */
	public void set(int i, byte b) {
		data[i>>2] = (byte) ( (data[i>>2] & ~(0x3<<((i&0x3)<<1))) | (b<<((i&0x3)<<1)) );
	}
	
	/**
	 * Clone factory.
	 */
	public TwoBitArray clone() {
		TwoBitArray r = new TwoBitArray(data.length >> 2);
		r.data = data.clone();
		return r;
	}
	
	/**
	 * @return true if all bins have equal state.
	 */
	public boolean allEqual(int voxelsPerBlock) {
		if ( (data[0]&0x3) == ((data[0]&0x0C)>>>2) && 
			 (data[0]&0x3) == ((data[0]&0x30)>>>4) &&
			 (data[0]&0x3) == ((data[0]&0xC0)>>>6) ) {
			for (int i = 1; i < (data.length-1); i++) {
				if (data[i] != data[0]) {
					return false;
				}
			}
			// check last byte
			for (int i = 0; i < (data.length << 2) - voxelsPerBlock; i++) {
				if (((data[data.length-1]>>>(i<<1)) & 0x3) != (data[0] & 0x3)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Analogous to array.length, but without spending another int. Value
	 * represents the storage capacity of data[] in two-bit units, which
	 * may be greater than voxelsPerBlock by up to three.
	 * 
	 * @return
	 */
	public int size() {
		return data.length << 2;
	}
	
	/**
	 * Set all bins to the provided state.
	 * 
	 * @param b, first two bits are the state, following bits are zeros.
	 */
	public void setAll(byte b) {
		b = (byte) (b | (b<<2) | (b<<4) | (b<<6));
		for (int i = 0; i < data.length; i++) {
			data[i] = b;
		}
	}
}


/**
 * Helper functions.
 * 
 * @author James Gray
 */
class func {
	
	/**
	 * Convert world coordinate to voxel coordinate.
	 */
	static int w2v (double w, double size) {
		return (int) (w/size);
	}
	
	/**
	 * Find the 1D block index given voxel coord. Order is y-up: from LSB, count z,x,y.
	 * 
	 * @param x,y,z coordinates to translate
	 * @return equivalent index value
	 */
	static int blockIdx(int x, int y, int z, int[] blockTwosOrder, int gridTwosOrderZ, int gridTwosOrderXPlusZ) {
		return (z >> blockTwosOrder[2])
			+ ((x >> blockTwosOrder[0]) << gridTwosOrderZ)
			+ ((y >> blockTwosOrder[1]) << gridTwosOrderXPlusZ);
	}
	
	/**
	 * Find the 1D voxel index given voxel coord. Order is y-up: from LSB, count z,x,y.
	 * 
	 * @param x,y,z coordinates to translate
	 * @return equivalent index value
	 */
	static int voxelIdx(int x, int y, int z, int[] blockLastIndex, int blockTwosOrderZ, int blockTwosOrderXPlusZ) {
		return (z & blockLastIndex[2])
			+ ((x & blockLastIndex[0]) << blockTwosOrderZ)
			+ ((y & blockLastIndex[1]) << blockTwosOrderXPlusZ);
	}
}


