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
 * A voxel grid.  Grids can either be uniform or
 * have the same XZ with a different Y size.
 *
 * Uses the X3D coordinate system.  Y-up.  Grid is located
 * on positive right side octant.
 *
 * @author Alan Hudson
 */
public interface Grid extends Cloneable {
    // The voxel is outside any object
    public static final byte OUTSIDE = 0;

    // The voxel is an exterior voxel
    public static final byte EXTERIOR = 1;

    // The voxel is an interior voxel
    public static final byte INTERIOR = 2;

    // The voxel is a user defined state.  Consider it outside if its not your value.
    public static final byte USER_DEFINED = 3;

    // Marked is EXTERIOR | INTERIOR
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
     * Get the state of the voxels specified in the area.
     *
     * @param x1 The starting x grid coordinate
     * @param x2 The ending x grid coordinate
     * @param y1 The starting y grid coordinate
     * @param y2 The ending y grid coordinate
     * @param z1 The starting z grid coordinate
     * @param z2 The ending z grid coordinate
     *
     * @param ret Returns the data at each position.  3 dim array represented as flat, must be preallocated
     */
     // Proposed addition but not implemented yet
/*
    public void getData(int x1, int x2, int y1, int y2, int z1, int z2, VoxelData[] ret);
*/
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
    public int getMaterial(double x, double y, double z);

    /**
     * Get the material of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param The voxel material
     */
    public int getMaterial(int x, int y, int z);

    /**
     * Set the value of a voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 materialID
     * @param material The materialID
     */
    public void setData(double x, double y, double z, byte state, int material);

    /**
     * Set the value of a voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param val The value.  0 = nothing. > 0 materialID
     */
    public void setData(int x, int y, int z, byte state, int material);

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
     * Count a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @return The number
     */
    public int findCount(VoxelClasses vc);

    /**
     * Count a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The class of material to traverse
     * @return The number
     */
    public int findCount(int mat);

    /**
     * Count a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void find(VoxelClasses vc, ClassTraverser t);

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void find(int mat, ClassTraverser t);

    /*
     * Traverse a class of voxel and material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void find(VoxelClasses vc, int mat, ClassTraverser t);

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findInterruptible(VoxelClasses vc, ClassTraverser t);

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.  Allows interruption
     * of the find stream.
     *
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findInterruptible(int mat, ClassTraverser t);

    /*
     * Traverse a class of voxel and material types.  May be much faster then
     * full grid traversal for some implementations.  Allows interruption
     * of the find stream.
     *
     * @param vc The class of voxels to traverse
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findInterruptible(VoxelClasses vc, int mat, ClassTraverser t);

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

    /**
     * Print out all slices.
     */
    public String toStringAll();

    /**
     * Clone the grid.
     */
    public Object clone();

    /**
     * Create an empty grid of the specified size.  Reuses
     * the grid type and material type(byte, short, int).
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public Grid createEmpty(int w, int h, int d, double pixel, double sheight);

    /**
     * Remove all voxels associated with the Material.
     *
     * @param mat The materialID
     */
    public void removeMaterial(int mat);

    /**
     * Determine if a voxel coordinate is inside the grid space.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return True if the coordinate is inside the grid space
     */
    public boolean insideGrid(int x,int y, int z);
}

