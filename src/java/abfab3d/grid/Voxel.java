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
 * Voxel coordinate and data holder.
 *
 * @author Alan Hudson
 */

public class Voxel {
    protected int x;
    protected int y;
    protected int z;

    /** The voxel state */
    private byte state;

    /** The material */
    private byte material;

    public Voxel(int x, int y, int z, byte state, byte material) {
        this.x = x;
        this.y = y;
        this.z = z;
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
    public byte getMaterial() {
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
    public void setMaterial(byte mat) {
        this.material = mat;
    }

    /**
     * Set the state and material
     *
     * @param state The state
     * @param mat The material
     */
    public void setData(byte state, byte mat) {
        this.state = state;
        this.material = mat;
    }

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    public boolean equals(Object o) {
        if(!(o instanceof VoxelCoordinate))
            return false;
        else
            return equals((VoxelCoordinate)o);
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ta The geometry instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(VoxelCoordinate ta) {
        return (ta.x == this.x && ta.y == this.y && this.z == ta.z);
    }

    /**
     * Get the hashcode for this.  Expected values for x and y < 10K
     * so multiplying by 31 will not overflow.
     */
    public int hashCode() {
        int ret_val = 31 * 31 * x + 31 * y + z;

        return ret_val;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

}
