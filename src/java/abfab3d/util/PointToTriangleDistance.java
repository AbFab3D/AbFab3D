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
import static java.lang.Math.abs;

/**
   calculates distance between point and triangles in 3D 

   @author Vladimir Bulatov 

   based on code from Geometric Tools by David Eberly 

triangle is represented as points   T (s,t) = B + s*E0 + t*E1; s > 0, t > 0, s+t < 1
       
        | t
\    2  |  
  \     |  
    \   |  
      \ |  
        \  
        | \
        |   \           reg 1
        |     \
   3    |       \
        |   0     \
        |           \                    s
--------+-------------\-------------------  
        |               \
    4   |                 \       6
        |      5            \
        |                     \
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
                if(DEBUG){
                    printf("det: %21.17e\n invDet: %21.17e\ns: %21.17e\n t: %21.17e\n", det, invDet, s, t);
                    printf("a00: %21.17e\n", a00);
                    printf("a01: %21.17e\n", a01);
                    printf("a11: %21.17e\n", a11);
                    printf("b0: %21.17e\n", b0);
                    printf("b1: %21.17e\n", b1);
                    printf("c: %21.17e\n", c);
                }
                sqrDistance = s*(a00*s + a01*t + 2*b0) + t*(a01*s + a11*t + 2*b1) + c;
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
                        sqrDistance = s*(a00*s + a01*t + 2*b0) + t*(a01*s + a11*t + 2*b1) + c;
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
        
        if(sqrDistance < 0.) // this may happen as result of round off errors 
            sqrDistance = 0.;
        return sqrDistance;
    }
    
    /**
       squared distance to triangle with no garbage generation        
     */
    public static double getSquared(double pointx,double pointy, double pointz, 
                                    double v0x, double v0y, double v0z, 
                                    double v1x, double v1y, double v1z, 
                                    double v2x, double v2y, double v2z, 
                                    double pointInTriangle[]) {
        
        double 
            diffx = v0x - pointx,
            diffy = v0y - pointy,
            diffz = v0z - pointz,            
            edge0x = v1x - v0x,
            edge0y = v1y - v0y,
            edge0z = v1z - v0z,
            edge1x = v2x - v0x,
            edge1y = v2y - v0y,
            edge1z = v2z - v0z;
        double 
            a00 = length2(edge0x,edge0y, edge0z),
        a11 = length2(edge1x,edge1y, edge1z),
            a01 = dot(edge0x,edge0y,edge0z,edge1x,edge1y,edge1z),
            b0 = dot(diffx, diffy, diffz, edge0x,edge0y,edge0z),
            b1 = dot(diffx, diffy, diffz, edge1x,edge1y,edge1z),
            c = length2(diffx,diffy,diffz);
        double det = abs(a00*a11 - a01*a01);
        double s = a01*b1 - a11*b0;
        double t = a01*b0 - a00*b1;
        double sqrDistance=-1.;        

        if(det == 0.){
            throw new IllegalArgumentException(fmt("degenerate triangle:{\n\t(%12.7e,%12.7e,%12.7e),\n\t(%12.7e,%12.7e,%12.7e),\n\t(%12.7e,%12.7e,%12.7e)}]",
                                               v0x, v0y, v0z, v1x, v1y, v1z, v2x, v2y, v2z));
                    
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
                if(DEBUG){
                    printf("det: %21.17e\n invDet: %21.17e\ns: %21.17e\n t: %21.17e\n", det, invDet, s, t);
                    printf("a00: %21.17e\n", a00);
                    printf("a01: %21.17e\n", a01);
                    printf("a11: %21.17e\n", a11);
                    printf("b0: %21.17e\n", b0);
                    printf("b1: %21.17e\n", b1);
                    printf("c: %21.17e\n", c);
                }
                sqrDistance = s*(a00*s + a01*t + 2*b0) + t*(a01*s + a11*t + 2*b1) + c;
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
                        sqrDistance = s*(a00*s + a01*t + 2*b0) + t*(a01*s + a11*t + 2*b1) + c;
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
        
        if(sqrDistance < 0.) // this may happen as result of round off errors 
            sqrDistance = 0.;
        
        if(pointInTriangle != null){
            pointInTriangle[0] = v0x + edge0x * s + edge1x * t;
            pointInTriangle[1] = v0y + edge0y * s + edge1y * t;
            pointInTriangle[2] = v0z + edge0z * s + edge1z * t;
        }

        return sqrDistance;

    }
        
    public static final double dot(double ax,double ay,double az,double bx,double by,double bz){
        return ax*bx + ay*by + az*bz;
    }
    public static final double length2(double ax,double ay,double az){
        return ax*ax + ay*ay + az*az;
    }
    

}
