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

package abfab3d.grid;



/**
   interface for 2D grid 
   is is used to set/get attribute values for 2 dimensional grid 
   all other functionality of 2D grid is shared with 3 dimensional grids Grid and AttributeGrid 
   
   @author Vladimir Bulatov 
 */
public interface Grid2D {

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
    
}
