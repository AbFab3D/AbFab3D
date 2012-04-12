package abfab3d.grid;

/**
 * A block whose state can't be changed.
 * 
 * All gets will get the initial state, all sets will throw an error.
 */
class ConstantBlock implements Block {
	final byte state;
	
	/**
	 * Construct a new block.
	 * 
	 * State is expected to be Grid.OUTSIDE, Grid.INSIDE,
	 * or Grid.EXTERIOR. But any byte would be accepted.
	 * 
	 * @param state
	 */
	public ConstantBlock(byte state) {
		this.state = state;
	}
	
	/**
	 * Get the state of the block. Constant block,
	 * so any get method will return this value.
	 */
	public byte get() {
		return state;
	}
	
	/**
	 * Get the value of a voxel within the block.
	 */
	public byte get(int index) {
		return state;
	}
	
	/**
	 * Set the value. Constant block - throws exception!
	 */
	public void set(int index, byte state) {
		throw new UnsupportedOperationException("Cannot set elements of a ConstantBlock.");
	}
	
	/**
	 * Set the value. Constant block - throws exception!
	 */
	public void setAll(byte state) {
		throw new UnsupportedOperationException("Cannot set elements of a ConstantBlock.");
	}
	
	/**
	 * Returns whether all voxels in block have equal states.
	 */
	public boolean allEqual() {
		return true;
	}
	
	/**
	 * Returns a copy of this block.
	 */
	public ConstantBlock clone() {
		return new ConstantBlock(state);
	}
}
