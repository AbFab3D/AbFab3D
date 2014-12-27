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

package abfab3d.geomutil;


/**
   interface to represetn abstract point 
   
 */
public interface APnt {
    /**
       @returns midpoint between pnt and this point 
       @param pnt point at another end of interval 
    */
    public APnt midpoint(APnt pnt);
    
    /**
       @returns distance from pnt to this 
     */
    public double distance(APnt pnt);
    

}