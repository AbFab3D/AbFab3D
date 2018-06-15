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

package abfab3d.core;

/**
   
   interface to implements calculation of general multidimensional data at the given point 

   @author Vladimir Bulatov 
 */
public interface DataSource {

    // compilation flag to make shapes data source to use signed distance or density 
    public static final int DATA_TYPE_DISTANCE = 1;
    public static final int DATA_TYPE_DENSITY = 0;
    
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
    
    /**
       @return bounds of this data source. It may be null for data sources without bounds 
       
     */
    public Bounds getBounds();


}
