package abfab3d.grid;

/**
 * Determines whether a voxel value is inside or outside.
 * Handles encoding state and attribute values.
 *
 * @author Alan Hudson
 */
public interface InsideOutsideFunc {
    /**
     * Get the INSIDE/OUTSIDE state of a voxel based on its attribute.
     *
     * @param encoded The encoded value
     * @return
     */
    public byte getState(long encoded);

    /**
     * Get the INSIDE/OUTSIDE state of a voxel based on its attribute.
     *
     * @param encoded The encoded value
     * @return
     */
    public long getAttribute(long encoded);

    /**
     * Combine state to an attribute value.
     * Must be consistent with the getState(attribute) call.
     *
     * @param state
     * @return The encoded value
     */
    public long combineStateAndAttribute(byte state, long attribute);

    /**
     * Update the attribute value without changing the state.
     *
     * @param encoded
     * @return The encoded value
     */
    public long updateAttribute(long encoded, long attribute);
}
