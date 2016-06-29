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

import abfab3d.core.VoxelData;

/**
 * Contains the data portion of a voxel.
 *
 * @author Alan Hudson
 */
public class VoxelDataShort implements VoxelData {
    /** The voxel state */
    private byte state;

    /** The material */
    private short material;

    public VoxelDataShort() {
    }

    public VoxelDataShort(byte state, long material) {
        this.state = state;
        this.material = (short) material;
    }

    /**
     * Get the state.
     *
     * @return The state
     */
    public byte getState() {
        return state;
    }

    /**
     * Get the material.
     *
     * @return The material
     */
    public long getMaterial() {
        return material;
    }

    /**
     * Set the state.
     *
     * @param state The new state
     */
    public void setState(byte state) {
        this.state = state;
    }

    /**
     * Set the material.
     *
     * @param mat The material
     */
    public void setAttribute(long mat) {
        this.material = (short) mat;
    }

    /**
     * Set the state and material
     *
     * @param state The state
     * @param mat The material
     */
    public void setData(byte state, long mat) {
        this.state = state;
        this.material = (short) mat;
    }

    /**
     * Clone this object.
     *
     * @return The cloned object
     */
    public Object clone() {
        return new VoxelDataShort(state, material);
    }

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    public boolean equals(Object o) {
        if(!(o instanceof VoxelData))
            return false;
        else
            return equals((VoxelData)o);
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param vd The voxel data instance to compare
     * @return true if the objects represent identical values
     */
    public boolean equals(VoxelData vd) {
        return (vd.getState() == this.state && vd.getMaterial() == this.material);
    }
}