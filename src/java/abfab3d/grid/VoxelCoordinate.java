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
 * Pass in max w,h,d in new constructor, use bit operations for lower storage costs
 *
 * @author Alan Hudson
 */

public class VoxelCoordinate implements Cloneable, Comparable {
    /** Maximum number of voxels per side of a grid we support */
    private static final long MAX_VOXELS = 30000;
    private static final long MAX_VOXELS_SQ = MAX_VOXELS * MAX_VOXELS;

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

    public VoxelCoordinate(int[] vcoord) {
        this.x = vcoord[0];
        this.y = vcoord[1];
        this.z = vcoord[2];
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

    public int compareTo(Object o2) {
        // TODO: Decide whether to keep this here

        VoxelCoordinate vc1 = this;
        VoxelCoordinate vc2 = (VoxelCoordinate) o2;

        
        long v1 = vc1.getX() * MAX_VOXELS_SQ + vc1.getY() * MAX_VOXELS + vc1.getZ();
        long v2 = vc2.getX() * MAX_VOXELS_SQ + vc2.getY() * MAX_VOXELS + vc2.getZ();
        
System.out.println(this + " to: " + o2 + " v1: " + v1 + " v2: " + v2);
        return ((v1 < v2) ? -1 : ((v1 == v2) ? 0 : 1));
/*        
        if (vc1.getX() < vc2.getX()) {
            return -1;
        }

        if (vc1.getY() < vc2.getY()) {
            return -1;
        }

        if (vc1.getZ() < vc2.getZ()) {
            return -1;
        }

        if (vc1.getX() == vc2.getX() &&
                vc1.getY() == vc2.getY() && vc1.getZ() == vc2.getZ()) {

            return 0;
        }

        return 1;
*/        
    }

    public Object clone() {
        Object ret_val = new VoxelCoordinate(x,y,z);

        return ret_val;
    }

    public String toString() {
        return "VoxelCoordinate hc: " + hashCode() + " x: " + x + " y: " + y + " z: " + z;
    }
}
