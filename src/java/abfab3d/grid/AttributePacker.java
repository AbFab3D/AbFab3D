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

import abfab3d.util.Vec;

/**
   interface to convert vector of double into long voxel attribute 

   @author Vladimir Bulatov
 */

public interface AttributePacker {
    
    /**
       convert vector of double into long voxel attribute 
     */
    public long makeAttribute(Vec data);

    /**
       converts attribute into vector of double data 
       @param attribute 
       @param data values of data stored in attribute 
     */
    public void getData(long attribute, Vec data);
}