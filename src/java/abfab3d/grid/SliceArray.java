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
 * A slice backed by an array.
 *
 * Internal represation is a flat array for speed.
 *
 * @author Alan Hudson
 */
public class SliceArray implements Slice {
    /** Should we range check grid coord values */
    private static final boolean VERIFY_RANGE = true;

    /** The number of pixels in the x/width direction */
    protected int width;

    /** The number of pixels in the z/depth direction */
    protected int depth;

    /** Data, 0 = nothing.  > 0 materialID */
    protected byte[] data;

    public SliceArray(int w, int d) {
        width = w;
        depth = d;

        data = new byte[width * depth];
    }

    /**
     * Set the data directly
     *
     * @param data The data
     */
    public void setData(byte[] newData) {
        data = new byte[newData.length];

        System.arraycopy(data, 0, newData, 0, newData.length);
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

        // TODO: Should we use an object cache for these?
        int idx = z * width + x;
        byte state = (byte) ((data[idx] & 0xFF) >> 6);
        byte mat = (byte) (0x3F & data[idx]);

        return new VoxelData(state,mat);
    }

    /**
     * Get the data.
     *
     * @param data A reference to the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Get a pixel state directly.
     *
     * @param x The x value
     * @param z The z value
     */
    public byte getState(int x, int z) {
        verifyRange(x,z);

        return (byte) ((data[z * width + x] & 0xFF) >> 6);
    }

    /**
     * Get a pixel material directly.
     *
     * @param x The x value
     * @param z The z value
     */
    public byte getMaterial(int x, int z) {
        verifyRange(x,z);

        return (byte) (0x3F & data[z * width + x]);
    }

    /**
     * Set a pixel directly.
     *
     * @param x The x value
     * @param z The z value
     * @param state The voxel state
     * @param material The materialID
     */
    public void setData(int x, int z, byte state, byte material) {
        verifyRange(x,z);

        data[z * width + x] = (byte) (0xFF & (state << 6 | material));
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

    public String toStringSlice() {
        StringBuilder sb = new StringBuilder();

        for(int i=0; i < depth; i++) {
            for(int j=0; j < width; j++) {
                sb.append(data[i * width + j]);
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