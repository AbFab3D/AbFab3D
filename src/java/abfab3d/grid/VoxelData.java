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
 * Contains the data portion of a voxel.
 *
 * @author Alan Hudson
 */
public interface VoxelData {
    /**
     * Get the state.
     *
     * @return The state
     */
    public byte getState();

    /**
     * Get the material.
     *
     * @return The material
     */
    public int getMaterial();

    /**
     * Set the state.
     *
     * @param state The new state
     */
    public void setState(byte state);

    /**
     * Set the material.
     *
     * @param mat The material
     */
    public void setMaterial(int mat);

    /**
     * Set the state and material
     *
     * @param state The state
     * @param mat The material
     */
    public void setData(byte state, int mat);
}