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
import javax.vecmath.Matrix3d;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4d;

import static java.lang.Math.sqrt;
import static java.lang.Math.abs;


/**
   returns the distance to 3D spherical shell
   @author Vladimir Bulatov
 */
public class DistanceDataSphereShell implements DistanceData {
        
    double cx,cy, cz; 
    double radius;
    double thickness2;


    public DistanceDataSphereShell(double radius){
        this(radius, 0,0,0);
    }

    public DistanceDataSphereShell(double radius, 
                                   double centerx, double centery, double centerz){
        this(radius, centerx, centery, centerz, 0.);
    }
    public DistanceDataSphereShell(double radius, 
                                   double centerx, double centery, double centerz, double thickness){
        this.cx = centerx;
        this.cy = centery;
        this.cz = centerz;
        this.radius = radius;
        this.thickness2 = thickness/2;

    }

    //
    // return distance to the spherical shell in 3D 
    //
    public double getDistance(double x, double y, double z){

        // move center to origin 
        x -= cx;
        y -= cy;
        z -= cz;        
        
        double dist = abs(sqrt(x*x + y*y + z*z) - radius) - thickness2;

        return dist;
    }
}

