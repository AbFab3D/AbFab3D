/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.symmetry;

import abfab3d.util.Vec;

/**

   class to represent sphere or plane for uniform treatment of spheres and planes 
   @author Vladimir Bulatov
*/
public abstract class SPlane implements PairingTransform, FDPlane {
    
    /**
     * return signed distance to that object
     * exterior points have positive distance, interior points have negative distance
     * @param pnt coordinates of point 
     */ 
    public abstract double distance(Vec pnt);
    
    /**
     *  reflect the point in this splane 
     *  
     * @param pnt coordinates of point 
     */
    public abstract void transform(Vec pnt);
    // public abstract double getCosAngle(SPlane sp);



} // class SPlane 


