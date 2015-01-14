/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
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
   class provides functionality to check attribute for value and for location in space 
   used in ConnectedComponent for building component limited by possible values and optionally bounded by some shape 

   @author Vladimir Bulatov 
 */
public interface AttributeTester {
    
    /**
       @param x - coodinate of voxel 
       @param y - coodinate of voxel 
       @param z - coodinate of voxel 
       @param attribute value to test  
       @return true if voxel with coordinate (x,y,z) and attribute value is permitted 
     */
    public boolean test(int x, int y, int z, long attribute);
}