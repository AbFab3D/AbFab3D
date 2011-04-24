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
 * A slice backed by a Map.  Values are packed to reduce space further.
 *
 * Uses much less memory then a SliceArray.  Likely a lot slower.
 *
 * @author Alan Hudson
 */
public class SliceMapPacked implements Slice {
    /** The number of pixels in the x/width direction */
    protected int width;

    /** The number of pixels in the z/depth direction */
    protected int depth;

    /** Data, 0 = nothing.  > 0 materialID */
    protected HashMap<SliceCoordinate, Byte> data;

    /** The number of bits used for each voxel */
    protected int bits;

    /** Divisor for coordinates */
    protected int divisor;

    public SliceMapPacked(int w, int d, int bits) {
        width = w;
        depth = d;
        this.bits = bits;

        data = new HashMap<SliceCoordinate, Byte>();

        if (bits == 1) {
            divisor = 8;
        } else if (bits == 2) {
            divisor = 4;
        } else if (bits == 3) {
            throw new IllegalArgumentException("Unsupported number of bits: " + bits);
        } else if (bits == 4) {
            divisor = 2;
        } else {
            throw new IllegalArgumentException("Unsupported number of bits: " + bits);
        }
    }

    /**
     * Get a pixel data directly.
     *
     * @param x The x value
     * @param z The z value
     * @return The data value
     */
    public VoxelData getData(int x, int z) {
    /*
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
*/
        return null;
    }

    /**
     * Get a pixel directly.
     *
     * @param x The x value
     * @param z The z value
     */
/*
    public byte getData(int x, int z) {
        byte ret_val = 0;

        SliceCoordinate coord = new SliceCoordinate(x,z);
        Byte b = data.get(coord);

        if (b != null)
            ret_val = (byte) b;

        return ret_val;
    }
*/

    /**
     * Set a pixel directly.
     *
     * @param x The x value
     * @param z The z value
     */
    public void setData(int x, int z, byte state, byte material) {
        int px = (int) Math.floor(x / divisor);
        int pos = x % divisor;

        SliceCoordinate sc = new SliceCoordinate(px,z);
        Byte oval = data.get(sc);

        if (oval == null) {
            data.put(sc, new Byte((byte) (0xFF & (state << 6 | material))));
        } else {
System.out.println("TODO: need to finish impl");
/*
            // need a mask such as(divisor 8, pos = 6) 10111111
            byte nval = val;
            byte mask = 0;

            // TODO: Need to deal with sign problems

            nval = (byte) ((oval & mask) | (val << pos));
            data.put(new SliceCoordinate(x,z), new Byte(nval));
*/
        }
    }

    /**
     * Get a pixel state directly.
     *
     * @param x The x value
     * @param z The z value
     */
    public byte getState(int x, int z) {
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
        byte ret_val = Grid.OUTSIDE;

        SliceCoordinate coord = new SliceCoordinate(x,z);
        Byte b = data.get(coord);

        if (b != null)
            ret_val = (byte) (0x3F & b);

        return ret_val;
    }

    public String toStringSlice() {
        StringBuilder sb = new StringBuilder();

        for(int i=0; i < depth; i++) {
            for(int j=0; j < width; j++) {
                sb.append(data.get(new SliceCoordinate(i,j)));
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
}

class SliceCoordinatePacked {
    public int x,z;

    public SliceCoordinatePacked(int x, int z) {
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