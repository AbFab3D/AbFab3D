/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011-2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.core;

import javax.vecmath.Tuple3d;

/**
   interface for 2D grid 
   is is used to set/get attribute values for 2 dimensional grid 
   all other functionality of 2D grid is shared with 3 dimensional grids Grid and AttributeGrid 
   
   @author Vladimir Bulatov 
 */
public interface Grid2D {
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
     * Get the grid bounds in world coordinates.
     *
     */
    public Bounds getGridBounds();

    /**
     * Set the grid bounds in world coordinates.
     *
     */
    public void setGridBounds(Bounds bounds);


    /**
     * Get the attribute of the voxel, 2D version
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @return The voxel attribute 
     */
    public long getAttribute(int x, int y);

    /**
     * Set the attribute of a voxel, 2D version 
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param attribute value of attribute 
     */
    public void setAttribute(int x, int y, long attribute);

    /**
     * Create an empty grid of the specified size.  Reuses
     * the grid type and material type(byte, short, int).
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param pixel The pixel size in meters
     */
    public Grid2D createEmpty(int w, int h, double pixel);

    /**
     * Get the voxel size in xz.
     *
     * @return The value
     */
    public double getVoxelSize();

    /**
     * Get the world coordinates for a grid coordinate.
     *
     * @param x The x value in grid coords
     * @param y The y value in grid coords
     * @param coords The result is placed into this preallocated array(3).
     */
    public void getWorldCoords(int x, int y, Tuple3d coords);

    /**
     * Get the grid coordinates for a world coordinate.
     * 
     * @param x The x value in world coords
     * @param y The y value in world coords
     * @param coords The ans is placed here
     */
    public void getGridCoords(double x, double y, Tuple3d coords);


    /**
     * Determine if a voxel coordinate is inside the grid space.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return True if the coordinate is inside the grid space
     */
    public boolean insideGrid(int x,int y);

    /**
       assign to the grid a description of a voxel attributes
       @param description The attirbute description
    */
    public void setDataDesc(GridDataDesc description);

    /**
       @return voxel attribute description assigned to the grid
     */
    public GridDataDesc getDataDesc();

}
