package abfab3d.grid;

/**
 * A grid that can contain per-voxel attributes.  The state of the grid is INSIDE if the attribute
 * is > 0 and is OUTSIDE if it equals 0.
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov
 */
public interface AttributeGrid extends Grid {
    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @return The voxel attribute
     */
    public long getAttributeWorld(double x, double y, double z);

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @return The voxel attribute
     */
    public void setAttributeWorld(double x, double y, double z, long attribute);

    /**
     * Get the attribute of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @return The voxel attribute
     */
    public long getAttribute(int x, int y, int z);

    /**
     * Set the value of a voxel.
     *  @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 attributeID
     * @param attribute The attributeID
     */
    public void setDataWorld(double x, double y, double z, byte state, long attribute);

    /**
     * Set the value of a voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param state The value.  0 = nothing. > 0 attributeID
     * @param attribute The attributeID
     */
    public void setData(int x, int y, int z, byte state, long attribute);

    /**
     * Set the attribute value of a voxel.  Leaves the state unchanged.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param attribute The attributeID
     */
    public void setAttribute(int x, int y, int z, long attribute);

    /**
     * Set the attribute value of a voxel.  Leaves the state unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param attribute The attributeID
     */
/*   // Need to add for completeness
    public void setAttribute(double x, double y, double z, long attribute);
*/
    /**
     * Count a class of attribute types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The class of attribute to traverse
     * @return The number
     */
    public int findCount(long mat);

    /**
     * Count a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttribute(VoxelClasses vc, ClassAttributeTraverser t);

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttributeInterruptible(VoxelClasses vc, ClassAttributeTraverser t);

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The attribute to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttribute(long mat, ClassAttributeTraverser t);

    /*
     * Traverse a class of voxel and attribute types.  May be much faster then
     * full grid traversal for some implementations.  Allows interruption
     * of the find stream.
     *
     * @param vc The class of voxels to traverse
     * @param mat The attribute to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttribute(VoxelClasses vc, long mat, ClassAttributeTraverser t);

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.  Allows interruption
     * of the find stream.
     *
     * @param mat The attribute to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttributeInterruptible(long mat, ClassAttributeTraverser t);

    /*
     * Traverse a class of voxel and attribute types.  May be much faster then
     * full grid traversal for some implementations.  Allows interruption
     * of the find stream.
     *
     * @param vc The class of voxels to traverse
     * @param mat The attribute to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttributeInterruptible(VoxelClasses vc, long mat, ClassAttributeTraverser t);

    /**
     * Traverse a class of voxels types over given rectangle in xy plane.
     * May be much faster then full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     * @param xmin - minimal x - coordinate of voxels
     * @param xmax - maximal x - coordinate of voxels
     * @param ymin - minimal y - coordinate of voxels
     * @param ymax - maximal y - coordinate of voxels
     */
    public void findAttribute(VoxelClasses vc, ClassAttributeTraverser t, int xmin, int xmax, int ymin, int ymax);

    /**
     * Remove all voxels associated with the Attribute.
     *
     * @param mat The attributeID
     */
    public void removeAttribute(long mat);

    /**
     * Reassign a group of attributes to a new attributeID
     *
     * @param attributes The new list of attributes
     * @param matID The new attributeID
     */
    public void reassignAttribute(long[] attributes, long matID);

    /**
       assign to the grid a description of a voxel attributes
       @param description The attirbute description 
     */
    public void setAttributeDesc(AttributeDesc description);

    /**
       @return voxel attribute description assigned to the grid
    */
    public AttributeDesc getAttributeDesc();
        

}
