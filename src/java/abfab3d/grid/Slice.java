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
import abfab3d.core.VoxelData;

/**
 * A slice of a grid.  Uses X3D coordinate system.
 *
 *    ------->  +X(width)
 *    |
 *    |
 *    |
 *    V +Z(depth)
 *
 * Internal represation is a flat array for speed.
 *
 * @author Alan Hudson
 */
public interface Slice {
    /**
     * Get a pixel data directly.
     *
     * @param x The x value
     * @param z The z value
     * @return The data value
     */
    public VoxelData getData(int x, int z);

    /**
     * Get a pixel state directly.
     *
     * @param x The x value
     * @param z The z value
     * @return The state value
     */
    public byte getState(int x, int z);

    /**
     * Get a pixel material directly.
     *
     * @param x The x value
     * @param z The z value
     * @return The material value
     */
    public long getMaterial(int x, int z);

    /**
     * Set a pixel directly.
     *
     * @param x The x value
     * @param z The z value
     */
    public void setData(int x, int z, byte state, long material);

    /**
     * Convert the slice into a printable string.
     *
     * @return The string
     */
    public String toStringSlice();

    /**
     * Get the width of the slice.
     *
     * @return The width
     */
    public int getWidth();

    /**
     * Get the depth of the slice.
     *
     * @return The depth
     */
    public int getDepth();
}