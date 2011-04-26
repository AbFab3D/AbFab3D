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

// External Imports
import java.util.*;
import java.io.*;

/**
 * A slice backed by a Map.
 *
 * Uses much less memory then a SliceArray.  Likely a bit slower.
 *
 * @author Alan Hudson
 */
public class SliceMap implements Slice {
    /** Should we range check grid coord values */
    private static final boolean VERIFY_RANGE = true;

    /** The number of pixels in the x/width direction */
    protected int width;

    /** The number of pixels in the z/depth direction */
    protected int depth;

    /** Data, 0 = nothing.  > 0 materialID */
    protected HashMap<SliceCoordinate, Byte> data;

    public SliceMap(int w, int d) {
        width = w;
        depth = d;

        data = new HashMap<SliceCoordinate, Byte>();
    }

    /**
     * Get a pixel data directly.
     *
     * @param x The x value
     * @param z The z value
     * @return The data value
     */
    public VoxelData getData(int x, int z) {
        verifyRange(x,z);

        byte state = Grid.OUTSIDE;
        byte mat = Grid.OUTSIDE;

        SliceCoordinate coord = new SliceCoordinate(x,z);
        Byte b = data.get(coord);

        if (b != null) {
            state = (byte) ((b & 0xFF) >> 6);
            mat = (byte) (0x3F & b);
        }

        VoxelData vd = new VoxelData(state,mat);

        return vd;
    }

    /**
     * Get a pixel state directly.
     *
     * @param x The x value
     * @param z The z value
     */
    public byte getState(int x, int z) {
        verifyRange(x,z);

        byte ret_val = Grid.OUTSIDE;

        SliceCoordinate coord = new SliceCoordinate(x,z);
        Byte b = data.get(coord);

        if (b != null)
            ret_val = (byte) ((b & 0xFF) >> 6);

        return ret_val;
    }

    /**
     * Get a pixel state directly.
     *
     * @param x The x value
     * @param z The z value
     */
    public byte getMaterial(int x, int z) {
        verifyRange(x,z);

        byte ret_val = Grid.OUTSIDE;

        SliceCoordinate coord = new SliceCoordinate(x,z);
        Byte b = data.get(coord);

        if (b != null)
            ret_val = (byte) (0x3F & b);

        return ret_val;
    }

    /**
     * Set a pixel directly.
     *
     * @param x The x value
     * @param z The z value
     */
    public void setData(int x, int z, byte state, byte material) {
        verifyRange(x,z);

        SliceCoordinate sc = new SliceCoordinate(x,z);

        if (state == Grid.OUTSIDE) {
            // Optimize for sparse arrays
            // TODO: Should this be configurable?

            data.remove(sc);
            return;
        }

        data.put(new SliceCoordinate(x,z), new Byte((byte) (0xFF & (state << 6 | material))));
    }

    public String toStringSlice() {
        StringBuilder sb = new StringBuilder();
        Byte val;

        for(int i=0; i < depth; i++) {
            for(int j=0; j < width; j++) {
                val = data.get(new SliceCoordinate(i,j));

                if (val == null) {
                    sb.append("0");
                } else {
                    sb.append(val);
                }
                sb.append(" ");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }

    /**
     * Range check grid coord values.  If outside range then throw
     * an IllegalArgumentException.
     *
     * @param x The x value
     * @param z The z value
     */
    private void verifyRange(int x, int z) {
        if (!VERIFY_RANGE)
            return;

        if (x < 0 || x > width - 1) {
            throw new IllegalArgumentException("x value invalid: " + x);
        }

        if (z < 0 || z > depth - 1) {
            throw new IllegalArgumentException("z value invalid: " + z);
        }
    }
}

class SliceCoordinate {
    public int x,z;

    public SliceCoordinate(int x, int z) {
        this.x = x;
        this.z = z;
    }

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    public boolean equals(Object o) {
        if(!(o instanceof SliceCoordinate))
            return false;
        else
            return equals((SliceCoordinate)o);
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ta The geometry instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(SliceCoordinate ta) {
        return (ta.x == this.x && ta.z == this.z);
    }

    /**
     * Get the hashcode for this.  Expected values for x and y < 10K
     * so multiplying by 31 will not overflow.
     */
    public int hashCode() {
        int ret_val = 31 * x + z;

        return ret_val;
    }
}