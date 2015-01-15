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

import abfab3d.param.Parameter;

/**
   
   interface to implements calculation of general multidimensional data at the given point 

   @author Vladimir Bulatov 
 */
public interface DataSource {

    public static int 
        RESULT_OK = 0,     // success
        RESULT_ERROR = 1,  // error occurs 
        RESULT_OUTSIDE = 2; // argument is outside of the domain of data definition 

    /**
       data value at the given point 
       @param pnt Point where the data is calculated 
       @param dataValue - storage for returned calculated data 
       @return result code 
     */
    public int getDataValue(Vec pnt, Vec dataValue);

    /**
       @returns count of data channels, 
       it is the count of data values returned in  getDataValue()        
     */
    public int getChannelsCount();

}
