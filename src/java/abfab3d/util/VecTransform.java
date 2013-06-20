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
   interface to implements general transformation of one vector into another vector
   
 */
public interface VecTransform {

    public static int 
        RESULT_OK = ResultCodes.RESULT_OK,     // transform was successfull 
        RESULT_ERROR = ResultCodes.RESULT_ERROR,  // error occurs during transform 
        RESULT_OUTSIDE = ResultCodes.RESULT_OUTSIDE; // argument is outside of domain of definition 

    /**
       direct transform from vin to vout        
       return 
     */
    public int transform(Vec vin,Vec vout);
    

    /**
       inverse transform from vin to vout        
     */
    public int inverse_transform(Vec vin,Vec vout);
    
}
