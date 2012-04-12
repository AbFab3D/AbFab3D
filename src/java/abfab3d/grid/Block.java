package abfab3d.grid;

/**
 * Interface for Blocks.
 * 
 * All Blocks are assumed to allow linear indexing
 * such that 3D x,y,z voxels are reduced to 1D,
 * with x being the least significant value.
 */
interface Block {
	
	// Get a voxel's state.
	public byte get(int index);
	
	// Set a voxel's state.
	public void set(int index, byte state);
	
	// Set all voxels in block to one state.
	public void setAll(byte state);
	
	// Find whether all voxels in block have the same state.
	public boolean allEqual(int voxelsPerBlock);
	
	// Return a copy of this block.
	public Block clone();
}
