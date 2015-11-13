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
   represents single slice to be processed by thread 
*/
public class Slice {

    public int smin;
    public int smax;
    
    Slice(int smin, int smax){
        this.smin = smin;
        this.smax = smax;
    }


}
