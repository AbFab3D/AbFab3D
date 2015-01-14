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

/**
   returns the distance to a line segment 
   @author Vladimir Bulatov
 */
public class DistanceDataSegment implements DistanceData {
        
    Vector3d p0; // start of segment 
    Vector3d p1; // start of segment 
    Vector3d p01; // segment tangencial vector 
    double length2; 

    public DistanceDataSegment(double x0, double y0, double z0, double x1, double y1, double z1){
        
        p0 = new Vector3d(x0,y0,z0);
        p1 = new Vector3d(x1,y1,z1);
        
        p01 = new Vector3d(x1, y1, z1);
        p01.sub(p0);
        length2 =  p01.dot(p01);
        
    }    
    public double getDistance(double x, double y, double z){
        
        Vector3d p = new Vector3d(x,y,z);
        
        p.sub(p0);

        double t = p01.dot(p)/length2;
        Vector3d cp = null;

        if(t <= 0.) {
            // closest point is p0;
            cp = p0;
        } else if(t >= 1.){
            cp = p1;            
        } else {
            cp = new Vector3d(p0);
            cp.interpolate(p1, t);
        }
        p.set(x,y,z);
        p.sub(cp);

        return Math.sqrt(p.dot(p));
    }
}

