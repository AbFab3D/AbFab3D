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

package abfab3d.util;

/**
   
   interface to implements general source of data at given Vec point 
   
 */
public interface DataSource {

    public static int 
        RESULT_OK = 0,     // success
        RESULT_ERROR = 1,  // error occurs 
        RESULT_OUTSIDE = 2; // argument is outside of domain of definition 

    /**
       data value at given point 
       return result code 
     */
    public int getDataValue(Vec pnt, Vec dataValue);
        
}
