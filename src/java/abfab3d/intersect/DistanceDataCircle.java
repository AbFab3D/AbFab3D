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

package abfab3d.intersect;

import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4d;

import static java.lang.Math.sqrt;


/**
   returns distance to a 3D circle 
   @author Vladimir Bulatov
 */
public class DistanceDataCircle implements DistanceData {
        
    Vector3d center; 
    Vector3d axis; 
    double radius;
    Matrix3d mat = new Matrix3d();

    public DistanceDataCircle(double radius){
        this(radius, 0,0,0,  0,0,1);
    }

    public DistanceDataCircle(double radius, 
                              double centerx, double centery, double centerz){
        this(radius, centerx, centery, centerz,  0,0,1);
    }

    public DistanceDataCircle(double radius, 
                              double centerx, double centery, double centerz, 
                              double axisx, double axisy, double axisz ){

        this.radius = radius;
        center = new Vector3d(centerx, centery, centerz);
        axis = new Vector3d(axisx, axisy,axisz);
        axis.normalize();
        
        makeRotation(axis, new Vector3d(0,0,1), mat);
        
    }   

    /**
       creates rotation matrix for given rotation 
     */
    static void makeRotation(Vector3d from, Vector3d to, Matrix3d m){

        double EPS = 1.e-8;

        Vector3d cp = new Vector3d();
        cp.cross(from, to);
        double sina = cp.length();
        if(Math.abs(sina) < EPS){
            // no rotation 
            m.setIdentity();
            return;
        }
            
        double cosa = from.dot(to);
        double angle = Math.atan2(sina, cosa);
        cp.normalize();

        Quat4d q = new Quat4d(); 
        q.set(new AxisAngle4d(cp.x,cp.y,cp.z, angle));
        m.set(q);
    }
 
    //
    // return distance to the circle in 3D 
    //
    public double get(double x, double y, double z){

        // move center to origin 
        x -= center.x;
        y -= center.y;
        z -= center.z;        
        
        // rotate into canonical orientation

        double px = mat.m00* x + mat.m01*y + mat.m02*z;
        double py = mat.m10* x + mat.m11*y + mat.m12*z;
        double pz = mat.m20* x + mat.m21*y + mat.m22*z;


        double xy = sqrt(px*px + py*py) - radius;
        double dist = sqrt(xy*xy + pz*pz);
        return dist;
    }

    /**
       return point representation of that circle 
     */
    public Vector3d[] getSpine(int count){

        Vector3d spine[] = new Vector3d[count];
        double df = Math.PI*2/count;
        
        for(int k = 0; k < count; k++){
            double a = k*df;
            double sina = Math.sin(a);
            double cosa = Math.cos(a);

            double x = radius*(mat.m00*cosa + mat.m10*sina) + center.x;
            double y = radius*(mat.m01*cosa + mat.m11*sina) + center.y;
            double z = radius*(mat.m02*cosa + mat.m12*sina) + center.z;

            spine[k] = new Vector3d(x,y,z);
            
        }
        return spine;
    }

}

