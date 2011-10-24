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
public class VoxelDataInt implements VoxelData {
    /** The voxel state */
    private byte state;

    /** The material */
    private int material;

    public VoxelDataInt(byte state, int material) {
        this.state = state;
        this.material = material;
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
    public int getMaterial() {
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
    public void setMaterial(int mat) {
        this.material = mat;
    }

    /**
     * Set the state and material
     *
     * @param state The state
     * @param mat The material
     */
    public void setData(byte state, int mat) {
        this.state = state;
        this.material = mat;
    }

    /**
     * Clone this object.
     *
     * @return The cloned object
     */
    public Object clone() {
        return new VoxelDataInt(state, material);
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
     * @param ta The geometry instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(VoxelData vd) {
        return (vd.getState() == this.state && vd.getMaterial() == this.material);
    }
}