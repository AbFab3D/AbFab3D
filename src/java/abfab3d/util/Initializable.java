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

   interface for objects, which require some kind of one time initialization before doing computationally intensive job 
   
 */
public interface Initializable {

    //public static final int 
    //    RESULT_OK = DataSource.RESULT_OK,     // success
    //    RESULT_ERROR  = DataSource.RESULT_ERROR;  // error

    /**
       
     */
    public int initialize();
        
}
