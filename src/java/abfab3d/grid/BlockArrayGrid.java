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

import java.util.ArrayList;
import java.util.BitSet;



// TODO: Many comments are out-of-date due to refactoring. 

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
	// declare constant blocks
	static final Block OUTSIDE_BLOCK = new ConstantBlock(Grid.OUTSIDE);
	static final Block EXTERIOR_BLOCK = new ConstantBlock(Grid.EXTERIOR);
	static final Block INTERIOR_BLOCK = new ConstantBlock(Grid.INTERIOR);
	static final Block USERDEF_BLOCK = new ConstantBlock(Grid.USER_DEFINED);
	
	// type of blocks to use for this grid
	// options are:
	// 		0: ArrayBlock, a block where voxels are represented as an array of bytes
	// 		1: RLEBlock, a block where voxels are stored via run-length encoding
	public enum BlockType {Array, BitArray, BitSet, RLE, RLEArrayList, KeyValue};
	public BlockType GRID_BLOCK_TYPE;
	
	// dimensions of the 1D containers
	public final int BLOCKS_PER_GRID; 
	public final int VOXELS_PER_BLOCK;
	
	// corresponding 3D dimensions
	public final int[] GRID_WIDTH_IN_BLOCKS;
	public final int[] BLOCK_WIDTH_IN_VOXELS;
	
	// corresponding 3D dimensions in 2^n form
	public final int[] GRID_TWOS_ORDER;
	public final int[] BLOCK_TWOS_ORDER;
	
	// largest dimensional coordinate of voxel in block
	// used to enable remainder by bitwise and in min ops
	protected final int[] BLOCK_LAST_INDEX;
	
	// offset of the grid's origin in number of voxels
	protected double[] offset = {0.0, 0.0, 0.0};
	
	// for converting grid units to world dimensions
	// world*scale = grid
	protected double[] scale = {1.0, 1.0, 1.0};
	
	// storage for blocks, x is LSB & z is MSB, use c2i() to convert
	protected Block[] blocks;
	
	// preallocated index tuples
	protected int[] idx = {0,0};
	protected int[] coord = {0,0,0};
	
	// log activity for each block
	// if activity[i] >= VOXELS_PER_BLOCK, clean() may recover memory
	// dealing with setAll() is another issue...
	// however, it is not yet exposed at the grid level, and may never need
	// to be since any grid optimization to exploit setAll() would likely
	// benefit more from using ConstantBlock objects.
	protected int[] activity;
	
	/**
	 * Constructor.
	 *
	 * @param w The number of voxels in width
	 * @param h The number of voxels in height
	 * @param d The number of voxels in depth
	 * @param pixel The size of the pixels (world size of voxels in xz)
	 * @param sheight The slice height in meters (world size of voxels in y)
	 * @param blockTwosOrder The dimensionality of blocks in voxels, value as 2^n
	 * @param blockType, the type of blocks to use.
	 * 				options are:
	 * 					0: ArrayBlock, a block where voxels are represented as an array of bytes
	 * 					1: RLEBlock, a block where voxels are stored via run-length encoding
	 */
	public BlockArrayGrid(int w, int h, int d, double pixel, double sheight, int[] blockTwosOrder, BlockType blockType) {
		// satisfy BaseGrid
		super(w,h,d,pixel,sheight);
		
		// set up the BlockArrayGrid constants
		GRID_BLOCK_TYPE = blockType;
		BLOCK_TWOS_ORDER = blockTwosOrder;
		BLOCK_WIDTH_IN_VOXELS = new int[] {(int) Math.pow(2,BLOCK_TWOS_ORDER[0]),
										   (int) Math.pow(2,BLOCK_TWOS_ORDER[1]),
										   (int) Math.pow(2,BLOCK_TWOS_ORDER[2])};
		
		GRID_TWOS_ORDER = new int[] {f.nextpow2((double) w / (double) BLOCK_WIDTH_IN_VOXELS[0]),
									 f.nextpow2((double) h / (double) BLOCK_WIDTH_IN_VOXELS[1]),
									 f.nextpow2((double) d / (double) BLOCK_WIDTH_IN_VOXELS[2])};
		
		GRID_WIDTH_IN_BLOCKS = new int[] {(int) Math.pow(2,GRID_TWOS_ORDER[0]),
										  (int) Math.pow(2,GRID_TWOS_ORDER[1]),
										  (int) Math.pow(2,GRID_TWOS_ORDER[2])};
		
		BLOCKS_PER_GRID = GRID_WIDTH_IN_BLOCKS[0]*GRID_WIDTH_IN_BLOCKS[1]*GRID_WIDTH_IN_BLOCKS[2];
		VOXELS_PER_BLOCK = BLOCK_WIDTH_IN_VOXELS[0]*BLOCK_WIDTH_IN_VOXELS[1]*BLOCK_WIDTH_IN_VOXELS[2];
		BLOCK_LAST_INDEX = new int[] { (1 << BLOCK_TWOS_ORDER[0])-1 ,
									   (1 << BLOCK_TWOS_ORDER[1])-1 ,
									   (1 << BLOCK_TWOS_ORDER[2])-1 };
		
		// set world scales
		scale[0] = 1.0/pixel;
		scale[1] = 1.0/sheight;
		scale[2] = scale[0];
		
		// initialize blocks array
		blocks = new Block[BLOCKS_PER_GRID];
		for (int i = 0; i < blocks.length; i++) {
			blocks[i] = OUTSIDE_BLOCK;
		}
		
		activity = new int[BLOCKS_PER_GRID];
	}
	
	/**
	 * Constructor using world coordinates.
	 *
	 * @param w The width in world coords
	 * @param h The height in world coords
	 * @param d The depth in world coords
	 * @param pixel The size of the pixels
	 * @param sheight The slice height in meters
	 * @param blockTwosOrder The dimensionality of blocks in voxels, value as 2^n
	 * @param blockType, the type of blocks to use.
	 * 				options are:
	 * 					0: ArrayBlock, a block where voxels are represented as an array of bytes
	 * 					1: RLEBlock, a block where voxels are stored via run-length encoding
	 */
	public BlockArrayGrid(double w, double h, double d, double pixel, double sheight, int[] blockTwosOrder, BlockType blockType) {
		this((int) (w/pixel)+1, (int) (h/sheight)+1, (int) (d/pixel)+1, pixel, sheight, blockTwosOrder, blockType);
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
		return new BlockArrayGrid(w,h,d,pixel,sheight,BLOCK_TWOS_ORDER,GRID_BLOCK_TYPE);
	}
	
	/**
	 * Copy Constructor.
	 *
	 * @param grid The grid
	 */
	public BlockArrayGrid(BlockArrayGrid grid) {
		this(grid.GRID_WIDTH_IN_BLOCKS[0]*grid.BLOCK_WIDTH_IN_VOXELS[0],
			 grid.GRID_WIDTH_IN_BLOCKS[1]*grid.BLOCK_WIDTH_IN_VOXELS[1],
			 grid.GRID_WIDTH_IN_BLOCKS[2]*grid.BLOCK_WIDTH_IN_VOXELS[2],
			 1.0/grid.scale[0],
			 1.0/grid.scale[1],
			 grid.BLOCK_TWOS_ORDER,
			 grid.GRID_BLOCK_TYPE);
		offset = grid.offset.clone();
		blocks = grid.blocks.clone();
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
	public VoxelData getData(double wx, double wy, double wz) {
		return getVoxelData(wx,wy,wz);
	}

	/**
	 * Get the state of the voxel
	 *
	 * @param x The x world coordinate
	 * @param y The y world coordinate
	 * @param z The z world coordinate
	 * @return The voxel state
	 */
	public byte getState(double wx, double wy, double wz) {
		return get(wx,wy,wz);
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
	public int getMaterial(double wx, double wy, double wz) {
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
	 * @param wx,wy,yz, The world coordinates
	 * @param state, the voxel state
	 * @param material, the material
	 */
	public void setData(double wx, double wy, double wz, byte state, int material) {
		coord = worldToGrid(wx,wy,wz);
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
	public void setMaterial(double wx, double wy, double wz, int material) {
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
	public void setState(double wx, double wy, double wz, byte state) {
		set(wx,wy,wz,state);
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
	protected VoxelData getVoxelData(double wx, double wy, double wz) {
		coord = worldToGrid(wx,wy,wz);
		return getVoxelData(coord[0],coord[1],coord[2]);
	}
	
	/**
	 * Get a voxel's value by index.
	 * 
	 * @param index, the {blockIndex, voxelIndex} to get
	 * @return the voxel's value
	 */
	protected byte get(int idxBlockInGrid, int idxVoxelInBlock) {
		return blocks[idxBlockInGrid].get(idxVoxelInBlock);
	}
	
	/**
	 * Get a voxel's value by index.
	 * 
	 * @param index, the [idxBlockInGrid, idxVoxelInBlock] to get
	 * @return the voxel's state
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
		return get( f.blockIndex(x, y, z, GRID_TWOS_ORDER, BLOCK_TWOS_ORDER),
					f.voxelIndex(x, y, z, BLOCK_LAST_INDEX, BLOCK_TWOS_ORDER) );
	}
	
	/**
	 * Get voxel data by world coords.
	 * 
	 * @param xyz, the voxel to read
	 * @return the voxel's value
	 */
	protected byte get(double wx, double wy, double wz) {
		coord = worldToGrid(wx,wy,wz);
		return get(coord[0],coord[1],coord[2]);
	}
	
	/**
	 * Set a voxel's value by index.
	 * 
	 * @param idxBlockInGrid, idxVoxelInBlock, the location of the voxel
	 * @param state, the data to set
	 */
	protected void set(int idxBlockInGrid, int idxVoxelInBlock, byte state) {
		if (blocks[idxBlockInGrid] instanceof ConstantBlock) {
			if (state != blocks[idxBlockInGrid].get(0)) {
				switch(GRID_BLOCK_TYPE) {
					case Array:
						blocks[idxBlockInGrid] = new ArrayBlock(VOXELS_PER_BLOCK);
						break;
					case BitArray:
						blocks[idxBlockInGrid] = new BitArrayBlock(VOXELS_PER_BLOCK);
						break;
					case BitSet:
						blocks[idxBlockInGrid] = new BitSetBlock(VOXELS_PER_BLOCK);
						break;
					case RLE:
						blocks[idxBlockInGrid] = new RLEBlock(VOXELS_PER_BLOCK);
						break;
					case RLEArrayList:
						blocks[idxBlockInGrid] = new RLEArrayListBlock(VOXELS_PER_BLOCK);
						break;
					case KeyValue:
						blocks[idxBlockInGrid] = new KeyValueBlock();
						break;
					default:
						throw new IllegalArgumentException("Specified block type not a defined type.");
				}
				blocks[idxBlockInGrid].set(idxVoxelInBlock,state);
			} else {
				return;
			}
		} else {
			blocks[idxBlockInGrid].set(idxVoxelInBlock,state);
		}
		
		// increment activity counter
		activity[idxBlockInGrid] += 1;
	}
	
	/**
	 * Set a voxel's value by index.
	 * 
	 * @param index, the [idxBlockInGrid, idxVoxelInBlock] to set
	 * @param state, the data to set
	 */
	protected void set(int[] index, byte state) {
		set(index[0],index[1],state);
	}
	
	/**
	 * Set a voxel's value by grid coords.
	 * 
	 * @param xyz, the coords of the voxel to set
	 * @param state, the data to set
	 */
	protected void set(int x, int y, int z, byte state) {
		set(f.blockIndex(x, y, z, GRID_TWOS_ORDER, BLOCK_TWOS_ORDER),
			f.voxelIndex(x, y, z, BLOCK_LAST_INDEX, BLOCK_TWOS_ORDER), state);
	}
	
	/**
	 * Set a voxel's state by world coords.
	 * 
	 * @param xyz, the coords of the voxel to set
	 * @param state, the data to set
	 */
	protected void set(double wx, double wy, double wz, byte state) {
		coord = worldToGrid(wx,wy,wz);
		set(coord[0],coord[1],coord[2],state);
	}
	
	/**
	 * Set all voxels within a block to a given value.
	 * Exploit existing constant blocks where possible.
	 * 
	 * @param coord, a grid coordinate in the block
	 * @param state, the state to set
	 */
	protected void setBlock(int x, int y, int z, byte state) {
		switch(state) {
			case Grid.OUTSIDE:
				blocks[f.blockIndex(x, y, z, GRID_TWOS_ORDER, BLOCK_TWOS_ORDER)] = OUTSIDE_BLOCK;
				break;
			case Grid.EXTERIOR:
				blocks[f.blockIndex(x, y, z, GRID_TWOS_ORDER, BLOCK_TWOS_ORDER)] = EXTERIOR_BLOCK;
				break;
			case Grid.INTERIOR:
				blocks[f.blockIndex(x, y, z, GRID_TWOS_ORDER, BLOCK_TWOS_ORDER)] = INTERIOR_BLOCK;
				break;
			default:
				blocks[f.blockIndex(x, y, z, GRID_TWOS_ORDER, BLOCK_TWOS_ORDER)].setAll(state);
		}
	}
	
	/**
	 * Set all voxels within a block to a given value.
	 * 
	 * @param coord, a world coordinate in the block
	 * @param value, the data to set
	 */
	protected void setBlock(double wx, double wy, double wz, byte value) {
		coord = worldToGrid(wx,wy,wz);
		setBlock(coord[0],coord[1],coord[2],value);
	}
	
	/**
	 * Convert world coordinates to grid coordinates.
	 * 
	 * @param coord, the world coordinate
	 * @return the grid coordinate
	 */
	protected int[] worldToGrid(double wx, double wy, double wz) {
		coord[0] = (int) Math.floor(wx*scale[0]);
		coord[1] = (int) Math.floor(wy*scale[1]);
		coord[2] = (int) Math.floor(wz*scale[2]);
		return coord;
	}
	
	/**
	 * Clone factory.
	 */
	public Object clone() {
		return new BlockArrayGrid(this);
	}
	
	/**
	 * Attempt to clean the BlockGrid by reclaiming blocks which have
	 * homogeneous voxel state.
	 * 
	 * This currently only looks for blocks which can be replaced with
	 * one of the grid's constant blocks, representing Grid.OUTSIDE,
	 * Grid.EXTERIOR, and Grid.INTERIOR states.
	 */
	public void clean() {
		if (GRID_BLOCK_TYPE == BlockType.KeyValue) {
			for (int i = 0; i < BLOCKS_PER_GRID; i++) {
				// TODO: change this over to a class comparison
				// Or just nuke it, since KeyValueBlock seems to waste too much time resizing
				if (blocks[i] != OUTSIDE_BLOCK & 
					blocks[i] != EXTERIOR_BLOCK &
					blocks[i] != INTERIOR_BLOCK) {
					if (((KeyValueBlock) blocks[i]).allEqual(VOXELS_PER_BLOCK)) {
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
						}
						activity[i] = 0;
					}
				}
			}
		} else {
			for (int i = 0; i < BLOCKS_PER_GRID; i++) {
				// skip if we haven't written to block enough to fully populate it
				if (activity[i] < VOXELS_PER_BLOCK) continue;
				
				// is it homogeneous? cost varies by block type
				if (blocks[i].allEqual(VOXELS_PER_BLOCK)) { 
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
					}
					activity[i] = 0;
				}
			}
		}
		System.gc();
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
class ArrayBlock implements Block {
	PointSet points;
	
	/**
	 * Construct a new block.
	 * 
	 * Voxels are initially set to the Grid.OUTSIDE state.
	 * 
	 * @param voxelsPerBlock, xSize*ySize*zSize in voxels
	 */
	public ArrayBlock(int voxelsPerBlock) {
		points = new PointSet(voxelsPerBlock);
	}
	
	/**
	 * Construct a new block.
	 * 
	 * @param voxelsPerBlock, xSize*ySize*zSize in voxels
	 * @param state, initialization state of the voxels
	 */
	public ArrayBlock(int voxelsPerBlock, byte state) {
		points = new PointSet(voxelsPerBlock, state);
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
	 * Set the state of voxel at index.
	 * 
	 * @param index
	 * @param state
	 */
	public void set(int index, byte state) {
		points.set(index, state);
	}
	
	/**
	 * Set all voxels in block to the same state.
	 * 
	 * @param state
	 */
	public void setAll(byte state) {
		points = new PointSet(points.size(), state);
	}
	
	/**
	 * Check if all voxels in the block have equal values.
	 * 
	 * @return true if all voxels in block are equal-valued
	 */
	public boolean allEqual(int voxelsPerBlock) {
		return points.allEqual();
	}
	
	/**
	 * Returns a clone of this block.
	 */
	public ArrayBlock clone() {
		ArrayBlock dupe = new ArrayBlock(points.size());
		dupe.points = points.clone();
		return dupe;
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
	
	/**
	 * Construct a PointSet given the size of the set.
	 * 
	 * @param voxelsPerBlock
	 */
	public PointSet(int voxelsPerBlock) {
		data = new byte[voxelsPerBlock];
	}
	
	/**
	 * 
	 * @param voxelsPerBlock
	 * @param state
	 */
	public PointSet(int voxelsPerBlock, byte state) {
		data = new byte[voxelsPerBlock];
		setAll(state);
	}
	
	/**
	 * Get voxel data by index within block.
	 * 
	 * @param index
	 * @return state of referenced voxel
	 */
	public byte get(int index) {
		return data[index];
	}
	
	/**
	 * Set voxel data by index within block.
	 * 
	 * @param index, the index of the voxel to modify
	 * @param state, the state to set
	 */
	public void set(int index, byte state) {
		data[index] = state;
	}
	
	/**
	 * Gets the size of the PointSet, equivalent to the
	 * voxelsPerBlock value it was created with.
	 */
	public int size() {
		return data.length;
	}
	
	/**
	 * Set all blocks to the same state.
	 * 
	 * @param state
	 * @param voxelsPerBlock
	 */
	public void setAll(byte state) {
		for (int i = 0; i < data.length; i++) {
			data[i] = state;
		}
	}
	
	/**
	 * Check if all voxels in the block have equal values.
	 * 
	 * @return true if all voxels in block are equal-valued
	 */
	public boolean allEqual() {
		for (int i = 1; i < data.length; i++) {
			if (data[i] != data[0]) return false;
		}
		return true;
	}
	
	/**
	 * Return a clone of this PointSet.
	 */
	public PointSet clone() {
		PointSet dupe = new PointSet(data.length);
		for (int i = 0; i < data.length; i++) {
			dupe.set(i,data[i]);
		}
		return dupe;
	}
}



/**
 * A block implementation using Run Length Encoding.
 * 
 * Code elements are stored as a linked list since they only
 * make sense when read from start anyway.
 * 
 * This should give reasonably efficient memory utilization -
 * provided that the code remains reasonably short.
 * 
 * Filling a large grid with data which oscillates at the 
 * resolution would give very poor memory performance. However,
 * this would be bad for any RLE implementation, and more so
 * given Java's object overhead. So don't use RLE for that case!
 * 
 */
class RLEBlock implements Block {
	// block attributes
	RLENode head;
	
	// pointers
	protected static RLENode node;
	protected static RLENode prevNode;
	protected static int n;
	protected static int start;
	protected static int r0;
	protected static int r1;
	
	/**
	 * Construct a block with a size. Voxels are inactive.
	 * 
	 * @param blockSize
	 */
	public RLEBlock(int voxelsPerBlock) {
		head = new RLENode(voxelsPerBlock, Grid.OUTSIDE);
	}
	
	/**
	 * Construct a block with a size. Voxels are given state.
	 * 
	 * @param blockSize
	 */
	public RLEBlock(int voxelsPerBlock, byte state) {
		head = new RLENode(voxelsPerBlock, state);
	}
	
	/**
	 * Construct a block with dimensional sizes.
	 * 
	 * The data is stored 1D, so this is for convenience.
	 * 
	 * @param xSize
	 * @param ySize
	 * @param zSize
	 */
	public RLEBlock(int xSize, int ySize, int zSize) {
		this(xSize*ySize*zSize);
	}
	
	/**
	 * Construct a block with dimensional sizes and a voxel state.
	 * 
	 * @param xSize
	 * @param ySize
	 * @param zSize
	 * @param state
	 */
	public RLEBlock(int xSize, int ySize, int zSize, byte state) {
		this(xSize*ySize*zSize, state);
	}
	
	/**
	 * Get the state of a voxel.
	 * 
	 * @param index, the index of the block inside
	 * 		the voxel, following the same xyz<-->i
	 * 		schema observed for Block. Method does
	 * 		not check that index < blockSize, and
	 * 		will throw null pointers if you try it.
	 * @return the activation state of the voxel.
	 * 		may be Grid.OUTSIDE or Grid.EXTERIOR.
	 */
	public byte get(int index) {
		node = head;
		n = node.length;
		
		// find the node containing index
		while (n <= index) {
			node = node.next;
			n += node.length;
		}
		return node.state;
	}
	
	/**
	 * Set the state of a voxel.
	 * 
	 * @param index, the index of the block inside
	 * 		the voxel, following the same xyz<-->i
	 * 		schema observed for Block. Method does
	 * 		not check that index < blockSize, and
	 * 		will throw null pointers if you try it.
	 * @param state, the desired voxel state. the
	 * 		supported values are Grid.OUTSIDE and
	 * 		Grid.EXTERIOR. If another value is used,
	 * 		state will be assumed as Grid.EXTERIOR.
	 */
	public void set(int index, byte state) {
		node = head;
		prevNode = null;
		n = node.length;
		start = 0;
		
		// find the node containing index & the offset
		while (n <= index) {
			start = n;
			prevNode = node;
			node = node.next;
			n += node.length;
		}
		
		// only make changes if changes are needed
		if (state != node.state) {
			// case: node has unit length
			if (node.length == 1) {
				node.state = state;
			} else {
				// find segment lengths
				r0 = index - start;
				r1 = node.length - r0;
				
				if (r0 == 0) {
					// Changing first element within node
					if (prevNode == null) {
						// changing head
						prevNode = new RLENode(1,state);
						prevNode.next = node;
						head = prevNode;
					} else if (prevNode.state == state) {
						prevNode.length += 1;
					} else {
						RLENode newNode = new RLENode(1,state);
						prevNode.next = newNode;
						newNode.next = node;
					}
					node.length -= 1;
				} else if (r1 == 1) {
					// Changing last element of run
					if (node.next == null) {
						node.next = new RLENode(1,state);
					} else if (node.next.state == state){
						node.next.length += 1;
					} else {
						RLENode newNode = new RLENode(1,state);
						newNode.next = node.next;
						node.next = newNode;
					}
					node.length -= 1;
				} else {
					// Changing intermediate element of run
					// Known not at an end (i.e. node.length != 2)
					// Need insert two new nodes
					RLENode newNode = new RLENode(r1-1,node.state);
					newNode.next = node.next;
					node.next = newNode;
					
					newNode = new RLENode(1,state);
					newNode.next = node.next;
					node.next = newNode;
					
					node.length = r0;
				}
			}
			// try to merge with neighbors
			if (node.next != null) {
				if (node.next.state == node.state) {
					node.next.length += node.length;
					node = node.next;
				}
			}
			if (prevNode != null) {
				if (prevNode.state == node.state) {
					prevNode.length += node.length;
					if (node.next != null) {
						prevNode.next = node.next;
					}
				}
			}
		}
	}
	
	/**
	 * Set all voxels in block to the same value.
	 * 
	 * @param state, the desired voxel state. the
	 * 		supported values are Grid.OUTSIDE and
	 * 		Grid.EXTERIOR. If another value is used,
	 * 		state will be assumed as Grid.EXTERIOR.
	 * @param voxelsPerBlock
	 */
	public void setAll(byte state) {
		head = new RLENode(size(),state);
	}
	
	/**
	 * Get total length of runs.
	 */
	private int size() {
		node = head;
		int length = node.length;
		while (node.next != null) {
			node = node.next;
			length += node.length;
		}
		return length;
	}
	
	/**
	 * Check whether all voxels in block have the same states.
	 * 
	 * @return true if all voxels in block have equal states.
	 */
	public boolean allEqual(int voxelsPerBlock) {
		return (head.next == null);
	}
	
	/**
	 * Return a clone of this block.
	 */
	public RLEBlock clone() {
		RLEBlock dupe = new RLEBlock(0);
		RLENode dupenode = dupe.head;
		node = head;
		
		dupe.head = node.clone();
		while (node.next != null) {
			dupenode.next = node.next.clone();
			dupenode = dupenode.next;
			node = node.next;
		}
		return dupe;
	}
	
	/**
	 * Node object for linked list.
	 */
	class RLENode {
		public RLENode next;
		public int length;
		public byte state;
		
		/**
		 * Constructor.
		 * 
		 * @param length, the run length for this state
		 */
		public RLENode(int length, byte state) {
			this.length = length;
			this.state = state;
		}
		
		/**
		 * Clone factory. Returns a new node with the
		 * same length as this node. However, the new
		 * new node's next attribute will remain null.
		 */
		public RLENode clone() {
			return new RLENode(length,state);
		}
	}
}



/**
 * A block implementation using Run Length Encoding.
 * 
 * Uses ArrayList to store the run lengths and states. This
 * should provide a baseline for this style of encoding.
 * 
 * Filling a large grid with data which oscillates at the 
 * resolution would give poor memory performance. However,
 * this would be bad for any RLE implementation, so don't
 * use RLE for that case!
 * 
 */
class RLEArrayListBlock implements Block {
	protected ArrayList<Integer> runLength;
	protected ArrayList<Byte> runState;
	
	/**
	 * Construct a block with a size. Voxels are inactive.
	 * 
	 * @param blockSize
	 */
	public RLEArrayListBlock(int voxelsPerBlock) {
		this(voxelsPerBlock,Grid.OUTSIDE);
	}
	
	/**
	 * Construct a block with a size. Voxels are given state.
	 * 
	 * @param blockSize
	 */
	public RLEArrayListBlock(int voxelsPerBlock, byte state) {
		runLength = new ArrayList<Integer>();
		runState = new ArrayList<Byte>();
		runLength.add(voxelsPerBlock);
		runState.add(state);
	}
	
	/**
	 * Construct a block with dimensional sizes.
	 * 
	 * The data is stored 1D, so this is for convenience.
	 * 
	 * @param xSize
	 * @param ySize
	 * @param zSize
	 */
	public RLEArrayListBlock(int xSize, int ySize, int zSize) {
		this(xSize*ySize*zSize);
	}
	
	/**
	 * Construct a block with dimensional sizes and a voxel state.
	 * 
	 * @param xSize
	 * @param ySize
	 * @param zSize
	 * @param state
	 */
	public RLEArrayListBlock(int xSize, int ySize, int zSize, byte state) {
		this(xSize*ySize*zSize, state);
	}
	
	/**
	 * Get the state of a voxel.
	 * 
	 * @param index, the index of the block inside
	 * 		the voxel, following the same xyz<-->i
	 * 		schema observed for Block. Method does
	 * 		not check that index < blockSize, and
	 * 		will throw null pointers if you try it.
	 * @return the activation state of the voxel.
	 */
	public byte get(int index) {
		int length = 0;
		for (int i = 0; i < runLength.size(); i++) {
			length += runLength.get(i);
			if (index < length) {
				return runState.get(i).byteValue();
			}
		}
		throw new IllegalArgumentException("Index exceeds length of stored code.");
	}
	
	/**
	 * Set the state of a voxel.
	 * 
	 * Code not collapsed in all cases. Could be optimized to
	 * cost more CPU in order to save some RAM, but the gain
	 * is unlikely to be very large in most usage patterns.
	 * 
	 * @param index, the index of the block inside
	 * 		the voxel, following the same xyz<-->i
	 * 		schema observed for Block. Method does
	 * 		not check that index < blockSize, and
	 * 		will throw null pointers if you try it.
	 * @param state, the desired voxel state.
	 */
	public void set(int index, byte state) {
		
		// length of run through end of node containing index
		int length = 0;
		for (int i = 0; i < runLength.size(); i++) {
			length += runLength.get(i).intValue();
			if (index < length) {
				// check if a modification is needed
				if (state == runState.get(i).byteValue()) {
					return;
				}
				
				// log the unmodified run length
				int oldLength = runLength.get(i).intValue();
				
				if (oldLength == 1) {
					// case: unit-length run
					runState.set(i,state);
				} else if (index == length - 1) {
					// case: target at end of run
					runLength.set(i,oldLength-1);
					if (i < runLength.size()-1) {
						// case: following run exists
						if (state == runState.get(i+1).byteValue()) {
							// if neighbor matches state, contribute length to neighbor
							runLength.set(i+1, runLength.get(i+1)+1);
						} else {
							// else insert a node at following position
							runLength.add(i+1,1);
							runState.add(i+1,state);
						}
						
					} else {
						// case: no following run exists
						// insert a node at following position
						runLength.add(i+1,1);
						runState.add(i+1,state);
					}
					
				} else if (index == length - oldLength) {
					// case: target at beginning of run
					runLength.set(i,oldLength-1);
					if (i>0) {
						// case: prior run exists
						if (state == runState.get(i-1).byteValue()) {
							// if neighbor matches state, contribute length to neighbor
							runLength.set(i-1,runLength.get(i-1)+1);
						} else {
							// else insert a node at current position
							runLength.add(i,1);
							runState.add(i,state);
						}
					} else {
						// case: no prior run exists
						// insert a node at current position
						runLength.add(i,1);
						runState.add(i,state);
					}
					
				} else {
					// case: target in middle of run
					// insert two nodes following
					runLength.set(i, index - (length - oldLength));
					
					// insert outer node with matching state
					runLength.add(i+1,oldLength - runLength.get(i).intValue() - 1);
					runState.add(i+1,runState.get(i).byteValue());
					
					// insert inner node with new state
					runLength.add(i+1,1);
					runState.add(i+1,state);
				}
				return;
			}
		}
		throw new IllegalArgumentException("Index exceeded length of code.");
	}
	
	/**
	 * Set all voxels in block to the same value.
	 * 
	 * @param state, the desired voxel state. the
	 * 		supported values are Grid.OUTSIDE and
	 * 		Grid.EXTERIOR. If another value is used,
	 * 		state will be assumed as Grid.EXTERIOR.
	 * @param voxelsPerBlock
	 */
	public void setAll(byte state) {
		runLength = new ArrayList<Integer>();
		runState = new ArrayList<Byte>();
		runLength.add(size());
		runState.add(state);
	}
	
	/**
	 * Get total length of runs.
	 */
	private int size() {
		int length = runLength.get(0).intValue();
		for (int i = 1; i < runLength.size(); i++) {
			length += runLength.get(i).intValue();
		}
		return length;
	}
	
	/**
	 * Check whether all voxels in block have the same states.
	 * 
	 * @return true if all voxels in block have equal states.
	 */
	public boolean allEqual(int voxelsPerBlock) {
		return (runLength.get(1) == 0);
	}
	
	/**
	 * Return a clone of this block.
	 */
	public RLEArrayListBlock clone() {
		RLEArrayListBlock newBlock = new RLEArrayListBlock(0);
		newBlock.runLength = new ArrayList<Integer>(this.runLength.size());
		newBlock.runState = new ArrayList<Byte>(this.runState.size());
		for (int i = 0; i < runLength.size(); i++) {
			newBlock.runLength.set(i,this.runLength.get(i).intValue());
			newBlock.runState.set(i,this.runState.get(i).byteValue());
		}
		return newBlock;
	}
}



/**
 * A Block implementation using BitSet. Each voxel is two bits.
 * 
 * States are:
 * 		00: Grid.OUTSIDE
 * 		01: Grid.EXTERIOR
 * 		10: Grid.INTERIOR
 * 		11: Grid.USER_DEFINED
 * 
 */
class BitSetBlock implements Block {
	protected BitSet states;
	protected boolean b0;
	protected boolean b1;
	
	/**
	 * Constructor.
	 * 
	 * @param voxelsPerBlock, the size of the block to construct
	 */
	BitSetBlock(int voxelsPerBlock) {
		states = new BitSet(voxelsPerBlock << 1);
	}
	
	/**
	 * Get a state.
	 */
	public byte get(int index) {
		return pack(states.get(index<<1),states.get((index<<1)+1));
	}
	
	/**
	 * Set a state.
	 */
	public void set(int index, byte state) {
		unpack(state);
		states.set(index<<1,b0);
		states.set((index<<1)+1,b1);
	}
	
	/**
	 * Set all states to state.
	 */
	public void setAll(byte state) {
		unpack(state);
		for (int i = 2; i < states.size(); i+=2) {
			states.set(i<<1,b0);
			states.set((i<<1)+1,b1);
		}
	}
	
	/**
	 * Check whether all states are equal.
	 */
	public boolean allEqual(int voxelsPerBlock) {
		for (int i = 2; i < states.size(); i+=2) {
			if (states.get(i) != states.get(0) || states.get(i+1) != states.get(1)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Clone factory.
	 */
	public BitSetBlock clone() {
		BitSetBlock newBlock = new BitSetBlock(states.size());
		newBlock.states = (BitSet) states.clone();
		return newBlock;
	}
	
	/**
	 * Use state to set protected vars b1,b0.
	 * 
	 * @param state, get bits of the two LSB
	 */
	private void unpack(byte state) {
		b0 = (state & 1) == 1;
		b1 = (state & 2) == 2;
	}
	
	/**
	 * Get the byte representation of two bits.
	 * 
	 * @param bit0, bit1 the data to pack
	 * @return the byte representation of the bits
	 */
	private byte pack(boolean bit0, boolean bit1) {
		if (bit1) {
			if(bit0) {
				return 3;
			}
			return 2;
		}
		if (bit0) {
			return 1;
		}
		return 0;
	}
}



/**
 * A Block implementation using BitArray. Each voxel is two bits.
 * 
 * States are:
 * 		00: Grid.OUTSIDE
 * 		01: Grid.EXTERIOR
 * 		10: Grid.INTERIOR
 * 		11: Grid.USER_DEFINED
 * 
 */
class BitArrayBlock implements Block {
	protected BitArray states;
	protected boolean b0;
	protected boolean b1;
	
	/**
	 * Constructor.
	 * 
	 * @param voxelsPerBlock, the size of the block to construct
	 */
	BitArrayBlock(int voxelsPerBlock) {
		states = new BitArray(voxelsPerBlock << 1);
	}
	
	/**
	 * Get a state.
	 */
	public byte get(int index) {
		return pack(states.get(index<<1),states.get((index<<1)+1));
	}
	
	/**
	 * Set a state.
	 */
	public void set(int index, byte state) {
		unpack(state);
		states.set(index<<1,b0);
		states.set((index<<1)+1,b1);
	}
	
	/**
	 * Set all states to state.
	 */
	public void setAll(byte state) {
		unpack(state);
		for (int i = 2; i < states.size(); i+=2) {
			states.set(i<<1,b0);
			states.set((i<<1)+1,b1);
		}
	}
	
	/**
	 * Check whether all states are equal.
	 */
	public boolean allEqual(int voxelsPerBlock) {
		for (int i = 2; i < states.size(); i+=2) {
			if (states.get(i) != states.get(0) || states.get(i+1) != states.get(1)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Clone factory.
	 */
	public BitArrayBlock clone() {
		BitArrayBlock newBlock = new BitArrayBlock(states.size());
		newBlock.states = (BitArray) states.clone();
		return newBlock;
	}
	
	/**
	 * Use state to set protected vars b1,b0.
	 * 
	 * @param state, get bits of the two LSB
	 */
	private void unpack(byte state) {
		b0 = (state & 1) == 1;
		b1 = (state & 2) == 2;
	}
	
	/**
	 * Get the byte representation of two bits.
	 * 
	 * @param bit0, bit1 the data to pack
	 * @return the byte representation of the bits
	 */
	private byte pack(boolean bit0, boolean bit1) {
		if (bit1) {
			if(bit0) {
				return 3;
			}
			return 2;
		}
		if (bit0) {
			return 1;
		}
		return 0;
	}
}



/**
 * A Block implementation based on a simple key-value store,
 * where the linear index result for "voxel in block" is
 * taken to be the key. ArrayList is used for the key and
 * value stores.
 * 
 * Allowed states are:
 * 		00: Grid.OUTSIDE
 * 		01: Grid.EXTERIOR
 * 		10: Grid.INTERIOR
 * 		11: Grid.USER_DEFINED
 *
 */
class KeyValueBlock implements Block {
	protected ArrayList<Integer> keys;
	protected ArrayList<Byte> values;
	protected boolean b0;
	protected boolean b1;
	protected int key;
	
	/**
	 * Constructor.
	 */
	KeyValueBlock() {
		keys = new ArrayList<Integer>();
		values = new ArrayList<Byte>();
	}
	
	/**
	 * Get a voxel state.
	 */
	public byte get(int index) {
		for (int i = 0; i < keys.size(); i++) {
			key = keys.get(i);
			if (key > index) {
				break;
			} else if (key == index) {
				return values.get(i);
			}
		}
		return Grid.OUTSIDE;
	}
	
	/**
	 * Set a voxel state.
	 */
	public void set(int index, byte state) {
		for (int i = 0; i < keys.size(); i++) {
			key = keys.get(i);
			// insert before
			if (key > index) {
				// anything unpopulated is already implicitly outside
				if (state == Grid.OUTSIDE) return;
				// add new entry, maintaining key ordering
				keys.add(i,index);
				values.add(i,state);
				return;
			// change state of existing entry
			} else if (key == index) {
				values.set(i, state);
				return;
			}
		}
		// anything unpopulated is already implicitly outside
		if (state == Grid.OUTSIDE) return;
		
		// new entry after list
		keys.add(index);
		values.add(state);
	}
	
	/**
	 * Set all states. This will implicitly set the full
	 * range of voxels which can possibly be in the block,
	 * which is probably wasteful and should be replaced
	 * with a ConstantBlock on the grid level.
	 */
	public void setAll(byte state) {
		keys = new ArrayList<Integer>(keys.size());
		values = new ArrayList<Byte>(keys.size());
		for (int i = 0; i < keys.size(); i++) {
			keys.set(i, i);
			values.set(i,state);
		}
	}
	
	/**
	 * Test whether all *stored* states are equal.
	 * 
	 * Warning: Not all states are known by the block.
	 * 			Any Grid.OUTSIDE states are implicit.
	 * 			To get a useful answer, you also need
	 * 			to check the size of the value store.
	 * 			Using allEqual(int) incorporates this.
	 */
	public boolean allEqual() {
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i) != values.get(0)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Test whether all voxel states in block are equal. 
	 */
	public boolean allEqual(int voxelsInBlock) {
		if (keys.size() != voxelsInBlock) {
			return false;
		}
		return allEqual();
	}
	
	/**
	 * Clone factory.
	 */
	@SuppressWarnings("unchecked")
	public KeyValueBlock clone() {
		KeyValueBlock newBlock = new KeyValueBlock();
		newBlock.keys = (ArrayList<Integer>) keys.clone();
		newBlock.values = (ArrayList<Byte>) values.clone();
		return newBlock;
	}
}



/**
 * Various helper functions.
 */
class f {
	
	/**
	 * Convert from x,y,z voxel coordinates to block index value.
	 * 
	 * Less efficient if you also need voxel index, saves computation if you only
	 * need the block index.
	 * 
	 * @param x,y,z the voxel coordinate
	 * @param gridTwosOrder, the size of the grid in blocks using twos order
	 * @return idxBlockInGrid, index of the block containing the voxel coordinate
	 */
	static int blockIndex(int x, int y, int z, int[] gridTwosOrder, int[] blockTwosOrder) {
		// find the index of the block within the grid
		return c2i(x >> blockTwosOrder[0], y >> blockTwosOrder[1], z >> blockTwosOrder[2], gridTwosOrder);
	}
	
	/**
	 * Convert from x,y,z voxel coordinates to block index value.
	 * 
	 * Less efficient if you also need voxel index, saves computation if you only
	 * need the block index.
	 * 
	 * @param x,y,z the voxel coordinate
	 * @param gridTwosOrder, the size of the grid in blocks using twos order
	 * @return idxBlockInGrid, index of the block containing the voxel coordinate
	 */
	static int voxelIndex(int x, int y, int z, int[] blockLastIndex, int[] blockTwosOrder) {
		return c2i(x & blockLastIndex[0], y & blockLastIndex[1], z & blockLastIndex[2], blockTwosOrder);
	}
	
	/**
	 * Calculate an index within a 1D representation of 3D integer locations.
	 * 
	 * Bit order is as follows:
	 * 		z x y | i
	 * 		0 0 0 | 0
	 * 		1 0 0 | 1
	 * 		0 1 0 | 2
	 * 		1 1 0 | 3
	 * 		0 0 1 | 4
	 * 
	 * This order should be observed to maintain consistency with other 
	 * AbFab3D classes. It may bias some comparisons between grids according
	 * to the structure of the test data used. However, this method should
	 * be the only place where a (x,y,z) ==> (i0,i1) mapping occurs; all
	 * other methods in this class and its sub-classes should refer to this
	 * method when such a calculation is required.
	 * 
	 * @param cx, cy, cz, the coordinates 
	 * @param twosOrder, the order of the cooordinate space 2^n
	 * @return the 1D index value
	 */
	static int c2i(int cx, int cy, int cz, int[] twosOrder) {
		return cz + (cx << twosOrder[2]) + (cy << twosOrder[2] << twosOrder[0]);
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
}


/**
 * Affords bit[] using byte[] in memory. Should be somewhat
 * smaller and more efficient than the equivalent BitSet if
 * you don't need dynamic size. 
 * 
 */
class BitArray {
	protected byte[] data;
	
	/**
	 * Construct a bit array using byte[].
	 * 
	 * @param nbits, desired length in bits
	 */
	public BitArray(int nbits) {
		int nbytes = nbits >> 3;
		if ((nbits&0x7) > 0) {
			nbytes += 1;
		}
		data = new byte[nbytes];
	}
	
	/**
	 * Get a bit's value.
	 * 
	 * @param i, the index to get the value of
	 * @return the stored value
	 */
	public boolean get(int i) {
		return (data[i>>3] & (1<<(i&0x7))) == (1<<(i&0x7));
	}
	
	/**
	 * Set a bit's value.
	 * 
	 * @param i, the index to set the value of
	 * @param b, the value to store
	 */
	public void set(int i, boolean b) {
		if (b) {
			data[i>>3] |= (0x1<<(i&0x7));
		} else {
			data[i>>3] &= ~(0x1<<(i&0x7));
		}
	}
	
	/**
	 * 
	 * @return the length of the bit array
	 */
	public int size() {
		return data.length << 3;
	}
	
	/**
	 * Clone factory.
	 */
	public BitArray clone() {
		BitArray r = new BitArray(data.length<<3);
		r.data = data.clone();
		return r;
	}
}



