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

package abfab3d.distance;

import javax.vecmath.Vector3d;

import static java.lang.Math.sqrt;
import static java.lang.Math.abs;


/**
   returns the signed distance to half space 
   half space is defined via point on plane and external normal 
   distance inside of half space is negative 
   distance outside of half space is positive 
   
   @author Vladimir Bulatov
 */
public class DistanceDataHalfSpace implements DistanceData {
        
    Vector3d normal; // external normal 
    double distance; // distance from origin to the plane 

    public DistanceDataHalfSpace(Vector3d normal, Vector3d pointOnPlane){
        this.normal = new Vector3d(normal);
        this.normal.normalize();
        this.distance = this.normal.dot(pointOnPlane);
    }

    //
    // return distance to the half space
    //
    public double getDistance(double x, double y, double z){

        return (x*normal.x + y*normal.y + z*normal.z) - distance;

    }
}
