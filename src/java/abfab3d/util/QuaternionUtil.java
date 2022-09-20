/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2018
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

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.TODEGREE;
import static java.lang.Math.*;


/**
   util to work with unit quaternions in stereographic projection 
   stereographic projection centrer is at (-1,0,0,0) 
   this makes most symetrical arrangement 
   
   IMPORTANT quaternion cordinates are (w; x,y,z) 

   @author Vladimir Bulatov 
 */
public class QuaternionUtil {

    /**
       quaternion -> stereographic projection 
       center of projection sphere is at(-1,0,0,0)
       this maps (1,0,0,0) -> (0,0,0)       
     */    
    public static double[] q2s(double q[]){ 
        double a = 1+q[0];
        return new double[]{q[1]/a,q[2]/a,q[3]/a};
    }


    /**
       convert point (x,y,z) in stereographic projection into unit quaternion 
       center of projection sphere is at(-1,0,0,0)
       this maps (0,0,0) ->(1,0,0,0) 
     */
    public static double[] s2q(double s[]){        

        double a2 = s[0]*s[0] + s[1]*s[1] + s[2]*s[2];
        double b = 1/(1+a2);
        double b2 = 2*b;
        return new double[]{(1-a2)*b, b2*s[0],b2*s[1],b2*s[2]};

    }


    /**
       convert rotation from axis angle representation {ax, ay, az, angle} iinto quaternion 

    */
    public static double[] r2q(double r[]){ 
        
        double a = r[3];
        double ca = cos(a/2);
        double sa = sin(a/2);
        
        double n = sqrt(r[0]*r[0] + r[1]*r[1] + r[2]*r[2]);

        return new double[]{ca, sa*r[0]/n,sa*r[1]/n,sa*r[2]/n};

    }

    /**
       convert rotation into quaternion in stereographic projection 
     */
    public static double[] r2s(double r[]){ 
        return q2s(r2q(r));
    }

    /**
       scalar product of 2 unit quaternions on S^3
     */
    public static double qdot(double p[], double q[]){
        return q[0]*p[0] + q[1]*p[1] + q[2]*p[2] + q[3]*p[3];
    }

    /**
       product of two quaternions 
     */
    public static double[] qmul(double p[], double q[]){
        return new double[]{
            p[0]*q[0] - p[1]*q[1] - p[2]*q[2]- p[3]*q[3],
            p[1]*q[0] + p[0]*q[1] + p[2]*q[3]- p[3]*q[2],
            p[2]*q[0] + p[0]*q[2] + p[3]*q[1]- p[1]*q[3],
            p[3]*q[0] + p[0]*q[3] + p[1]*q[2]- p[2]*q[1],
        };
    }
    
    /**
       return quaternion with non-negative 0-component
     */
    public static double[] q2positive(double p[]){
        if(p[0] < 0) 
            return new double[]{-p[0], -p[1], -p[2], -p[3]};
        else 
            return p;
    }


    /**
       interpolation between two quaternions p and q 
       normaly parameter t is in [0,1]
    */
    public static double[] qlerp(double q[], double p[], double t){
        double cosa = qdot(q, p);
        double phi = acos(cosa);
        double sina = sin(phi);
        double a = sin(phi*(1-t))/sina;
        double b = sin(phi*t)/sina;
        return new double[]{a*q[0] + b*p[0], a*q[1] + b*p[1], a*q[2] + b*p[2],  a*q[3] + b*p[3]};
        
    }

    /**
     * interpolation between 2 unit quaternions represented in stereographic coordinates 
     *
     */
    public static double[] qlerp_s(double s0[], double s1[], double t){
        // convert quaternions srom stereographic coord to into 
        double q0[] = s2q(s0);
        double q1[] = s2q(s1);
        double qt[] = qlerp(q0, q1, t);
        return q2s(qt);

    }

}