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

import javax.vecmath.Vector3d;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;

import static java.lang.Math.sqrt;

/**
   calculates distance between point and traingles in 3D 

   @author Vladimir Bulatov 

   based on code from Geometric Tools by David Eberly 
 */
public class PointToTriangleDistance {
    
    static final boolean DEBUG = false;

    public static double get(Vector3d point, Vector3d v0, Vector3d v1, Vector3d v2){
        return sqrt(getSquared(point, v0, v1, v2));
    }
    public static double get(Vector3d point, Vector3d triangle[]){

        return sqrt(getSquared(point, triangle[0],triangle[1],triangle[2]));

    }

    public static double getSquared(Vector3d point, Vector3d triangle[]){
        return getSquared(point, triangle[0],triangle[1],triangle[2]);
    }

    public static double getSquared(Vector3d point, Vector3d v0, Vector3d v1, Vector3d v2){
        
        Vector3d diff = new Vector3d();  diff.sub(v0, point);
        Vector3d edge0 = new Vector3d(); edge0.sub(v1,v0);
        Vector3d edge1 = new Vector3d(); edge1.sub(v2,v0);
        double a00 = edge0.lengthSquared();
        double a01 = edge0.dot(edge1);
        double a11 = edge1.lengthSquared();
        double b0 = diff.dot(edge0);
        double b1 = diff.dot(edge1);
        double c = diff.lengthSquared();
        double det = Math.abs(a00*a11 - a01*a01);
        double s = a01*b1 - a11*b0;
        double t = a01*b0 - a00*b1;
        double sqrDistance=-1.;        

        if(det == 0.){
            throw new IllegalArgumentException(fmt("degenerate triangle:{\n\t(%12.7e,%12.7e,%12.7e),\n\t(%12.7e,%12.7e,%12.7e),\n\t(%12.7e,%12.7e,%12.7e)}]",
                                               v0.x, v0.y, v0.z, v1.x, v1.y, v1.z, v2.x, v2.y, v2.z));
                    
        }
            
        if (s + t <= det){            
            if (s < 0) {
                if (t < 0) {  // region 4 
                    if(DEBUG)printf("region 4\n");
                    if (b0 < 0) {
                        t = 0;
                        if (-b0 >= a00) {
                            s = 1;
                            sqrDistance = a00 + 2*b0 + c;
                        } else {
                            s = -b0/a00;
                            sqrDistance = b0*s + c;
                        }
                    } else {
                        s = 0;
                        if (b1 >= 0) {
                            t = 0;
                            sqrDistance = c;
                        } else if (-b1 >= a11) {
                            t = 1;
                            sqrDistance = a11 + 2*b1 + c;
                        } else {
                            t = -b1/a11;
                            sqrDistance = b1*t + c;
                        }
                    }
                }  else { // region 3
                    if(DEBUG)printf("region 3\n");                    
                    s = 0;
                    if (b1 >= 0) {
                        t = 0;
                        sqrDistance = c;
                    }  else if (-b1 >= a11) {
                        t = 1;
                        sqrDistance = a11 + 2*b1 + c;
                    } else {
                        t = -b1/a11;
                        sqrDistance = b1*t + c;
                    }
                }
            } else if (t < 0) {  // region 5
                    if(DEBUG)printf("region 5\n");                                    
                t = 0;
                if (b0 >= 0) {
                    s = 0;
                    sqrDistance = c;
                } else if (-b0 >= a00){
                    s = 1;
                    sqrDistance = a00 + 2*b0 + c;
                } else {
                    s = -b0/a00;
                    sqrDistance = b0*s + c;
                }
            } else { // region 0                
                if(DEBUG)printf("region 0\n");                                    
                // minimum at interior point
                double invDet = 1/det;
                s *= invDet;
                t *= invDet;
                sqrDistance = s*(a00*s + a01*t + 2*b0) +
                    t*(a01*s + a11*t + 2*b1) + c;
            }
        } else {
            double tmp0, tmp1, numer, denom;                
            if (s < 0) { // region 2                
                if(DEBUG)printf("region 2\n");                                    
                tmp0 = a01 + b0;
                tmp1 = a11 + b1;
                if (tmp1 > tmp0) {
                    numer = tmp1 - tmp0;
                    denom = a00 - 2*a01 + a11;
                    if (numer >= denom) {
                        s = 1;
                        t = 0;
                        sqrDistance = a00 + 2*b0 + c;
                    } else {
                        s = numer/denom;
                        t = 1 - s;
                        sqrDistance = s*(a00*s + a01*t + 2*b0) +
                            t*(a01*s + a11*t + 2*b1) + c;
                    }
                } else {
                    s = 0;
                    if (tmp1 <= 0){
                        t = 1;
                        sqrDistance = a11 + 2*b1 + c;
                    } else if (b1 >= 0) {
                        t = 0;
                        sqrDistance = c;
                    } else {
                        t = -b1/a11;
                        sqrDistance = b1*t + c;
                    }
                }
            } else if (t < 0) {  // region 6
                if(DEBUG)printf("region 6\n");                                                    
                tmp0 = a01 + b1;
                tmp1 = a00 + b0;
                if (tmp1 > tmp0){                        
                    numer = tmp1 - tmp0;
                    denom = a00 - 2*a01 + a11;
                    if (numer >= denom) {                            
                        t = 1;
                        s = 0;
                        sqrDistance = a11 + 2*b1 + c;
                    } else {
                        t = numer/denom;
                        s = 1 - t;
                        sqrDistance = s*(a00*s + a01*t + 2*b0) +
                            t*(a01*s + a11*t + 2*b1) + c;
                    }
                } else {                        
                    t = 0;
                    if (tmp1 <= 0) {
                        s = 1;
                        sqrDistance = a00 + 2*b0 + c;
                    } else if (b0 >= 0) {
                        s = 0;
                        sqrDistance = c;
                    } else {                            
                        s = -b0/a00;
                        sqrDistance = b0*s + c;
                    }
                }
            } else { // region 1
                if(DEBUG)printf("region 1\n");                   
                numer = a11 + b1 - a01 - b0;
                if (numer <= 0) {                        
                    s = 0;
                    t = 1;
                    sqrDistance = a11 + 2*b1 + c;
                } else {
                    denom = a00 - 2*a01 + a11;
                    if (numer >= denom) {                            
                        s = 1;
                        t = 0;
                        sqrDistance = a00 + 2*b0 + c;
                    } else {                
                        s = numer/denom;
                        t = 1 - s;
                        sqrDistance = s*(a00*s + a01*t + 2*b0) +
                            t*(a01*s + a11*t + 2*b1) + c;
                    }
                }
            }
        }
        if(DEBUG)printf("sqrDistance: %10.8f dist: %10.8f\n",sqrDistance, sqrt(sqrDistance));
            
        return sqrDistance;
    }
        
}
