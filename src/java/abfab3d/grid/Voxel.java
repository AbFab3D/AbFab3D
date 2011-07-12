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

public class Voxel implements Cloneable {
    /** The voxel coordinate */
    protected VoxelCoordinate vc;

    /** The voxel data */
    protected VoxelData vd;

    public Voxel(VoxelCoordinate vc, VoxelData vd) {
        this.vc = vc;
        this.vd = vd;
    }

    /**
     * Get the voxel coordinate
     *
     * @return The value
     */
    public VoxelCoordinate getCoordinate() {
        return vc;
    }

    /**
     * Get the data.
     *
     * @return The value
     */
    public VoxelData getData() {
        return vd;
    }

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    public boolean equals(Object o) {
        if(!(o instanceof Voxel))
            return false;
        else
            return equals((Voxel)o);
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ta The geometry instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(Voxel ta) {
        return (ta.vc.equals(this.vc));
    }

    /**
     * Get the hashcode for this.  Expected values for x and y < 10K
     * so multiplying by 31 will not overflow.
     */
    public int hashCode() {
        int ret_val = vc.hashCode();

        return ret_val;
    }

    public Object clone() {
        Object ret_val = new Voxel((VoxelCoordinate)vc.clone(), (VoxelData)vd.clone());

        return ret_val;
    }
}
