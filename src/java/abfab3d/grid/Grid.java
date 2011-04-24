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
import org.web3d.vrml.sav.BinaryContentHandler;

/**
 * A grid composed of slices.
 *
 * Uses the X3D coordinate system.  Y-up.  Grid is located
 * on positive right side octant.
 *
 * @author Alan Hudson
 */
public interface Grid {
    // The voxel is outside any object
    public static final byte OUTSIDE = 0;

    // The voxel is an exterior voxel
    public static final byte EXTERIOR = 1;

    // The voxel is an interior voxel
    public static final byte INTERIOR = 2;

    public enum VoxelClasses {ALL, MARKED, EXTERIOR, INTERIOR, OUTSIDE};

    /**
     * Get the data for a voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param The voxel data
     */
    public VoxelData getData(double x, double y, double z);

    /**
     * Get the state of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param The voxel state
     */
    public VoxelData getData(int x, int y, int z);

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param The voxel state
     */
    public byte getState(double x, double y, double z);

    /**
     * Get the state of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param The voxel state
     */
    public byte getState(int x, int y, int z);

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param The voxel material
     */
    public byte getMaterial(double x, double y, double z);

    /**
     * Get the material of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param The voxel material
     */
    public byte getMaterial(int x, int y, int z);

    /**
     * Set the value of a voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 materialID
     * @param material The materialID
     */
    public void setData(double x, double y, double z, byte state, byte material);

    /**
     * Set the value of a voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param val The value.  0 = nothing. > 0 materialID
     */
    public void setData(int x, int y, int z, byte state, byte material);

    /**
     * Get the grid coordinates for a world coordinate.
     *
     * @param x The x value in world coords
     * @param y The y value in world coords
     * @param z The z value in world coords
     * @param coords The ans is placed into this preallocated array(3).
     */
    public void getGridCoords(double x, double y, double z, int[] coords);

    /**
     * Get the world coordinates for a grid coordinate.
     *
     * @param x The x value in grid coords
     * @param y The y value in grid coords
     * @param z The z value in grid coords
     * @param coords The ans is placed into this preallocated array(3).
     */
    public void getWorldCoords(int x, int y, int z, double[] coords);

    /**
     * Get the grid bounds in world coordinates.
     *
     * @param min The min coordinate
     * @param max The max coordinate
     */
    public void getGridBounds(double[] min, double[] max);

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void find(VoxelClasses vc, ClassTraverser t);

    /**
     * Create an X3D file from the grid.  This should be an exact
     * repsentation of the grid values.
     *
     * @param stream The stream to write too
     * @param colors Maps materialID's to colors
     */
    public void toX3D(BinaryContentHandler stream, Map<Byte, float[]> colors);

    /**
     * Get the number of height cells.
     *
     * @return the val
     */
    public int getHeight();

    /**
     * Get the number of width cells.
     *
     * @return the val
     */
    public int getWidth();

    /**
     * Get the number of depth cells.
     *
     * @return the val
     */
    public int getDepth();

    /**
     * Get the voxel size in xz.
     *
     * @return The value
     */
    public double getVoxelSize();

    /**
     * Get the slice height.
     *
     * @return The value
     */
    public double getSliceHeight();

    /**
     * Print out a slice of data.
     */
    public String toStringSlice(int s);

    public String toStringAll();
}

