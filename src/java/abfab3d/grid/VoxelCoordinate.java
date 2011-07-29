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
 * Voxel coordinate holder.  Used to reference a voxels position.
 * Typically stored in a Java collection object.
 *
 * This structure will be optimized for memory savings in the future.
 *
 * @author Alan Hudson
 */

public class VoxelCoordinate implements Cloneable {
    /** The x coordinate */
    protected int x;

    /** The y coordinate */
    protected int y;

    /** The z coordinate */
    protected int z;

    public VoxelCoordinate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    public Object clone() {
        Object ret_val = new VoxelCoordinate(x,y,z);

        return ret_val;
    }
}
