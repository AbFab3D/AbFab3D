/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.mesh;

import java.util.Random;
import java.util.Set;
import java.util.HashSet;


import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Vector3d;

import static abfab3d.util.Output.printf; 
import static abfab3d.util.Output.fmt; 


/**
   Quadric calculation 
   
   quadric is symmetrical 4x4 quadratic form sum_ij(x_i*m_ij*x_j)
   because of symmetry only upper triangle is stored 
   
   4x4 quadratic form act on 3-vectors assuming the 4-component is 1 

   @author Vladimir Bulatov
   
*/
public class Quadric {
    
    
    // holder of quadric matrix 
    //public Matrix4d M;
    double 
        m00, m01, m02, m03,
        m11, m12, m13,
        m22, m23,
        m33;
    
    /**
       unit quadric centered at origin 
     */
    public Quadric() {
        m00 = m11 = m22 = 1;
        // the rest is 0; 
    }
    
    /**
       copy constructor 
    */
    public Quadric(Quadric q) {
        
        m00 = q.m00;
        m01 = q.m01;
        m02 = q.m02;
        m03 = q.m03;
        m11 = q.m11;
        m12 = q.m12;
        m13 = q.m13;
        m22 = q.m22;
        m23 = q.m23;
        m33 = q.m33;
        
    }
    
    
    public Quadric(double e00, double e01, double e02,  double e03, double e11, double e12, double e13, double e22, double e23,double e33) {
        
        m00 = e00;
        m01 = e01;
        m02 = e02;
        m03 = e03;
        m11 = e11;
        m12 = e12;
        m13 = e13;
        m22 = e22;
        m23 = e23;
        m33 = e33;
        
    }
    
    /**
       quadric centered at midpoint of p0, p1 and scaled by scale
    */
    public Quadric(Point3d p0, Point3d p1, double scale) {
        
        getMidEdgeQuadric(p0, p1, scale, this);

    }
    
    /**
        Construct a quadric to evaluate the squared distance of any point
        to the given plane [ax+by+cz+d = 0].  This is the "fundamental error
        quadric" discussed in the Garland's paper
    */
    public Quadric(double a, double b, double c, double d) {
        
        m00 = a*a;  m01 = a*b;  m02 = a*c;  m03 = a*d;
        m11 = b*b;  m12 = b*c;  m13 = b*d;
        m22 = c*c;  m23 = c*d;
        m33 = d*d;            
        
    }
    
    public Quadric(Vector4d plane) {

        this(plane.x, plane.y, plane.z, plane.w);

    }
    
    
    /**
       return quadric for plane via 3 points 
    */
    public Quadric(Point3d p0,Point3d p1,Point3d p2){
        
        this(makePlane(p0, p1, p2, new Vector4d()));
        
    }
    
    /**
       evalueate value of the form at given point 
    */
    public double evaluate(Point3d p){
        double 
            x = p.x,
            y = p.y,
            z = p.z;
        
        return 
            x*x*m00 + 2*x*y*m01 + 2*x*z*m02 + 2*x*m03 
            +           y*y*m11 + 2*y*z*m12 + 2*y*m13  
            +                       z*z*m22 + 2*z*m23 
            +                                     m33;        
    }
    
    public Quadric set(Quadric q){

        m00 = q.m00;
        m01 = q.m01;
        m02 = q.m02;
        m03 = q.m03;
        m11 = q.m11;
        m12 = q.m12;
        m13 = q.m13;
        m22 = q.m22;
        m23 = q.m23;
        m33 = q.m33;        
        return this;        
    }

    public Quadric addSet(Quadric q){
        
        m00 += q.m00;
        m01 += q.m01;
        m02 += q.m02;
        m03 += q.m03;
        m11 += q.m11;
        m12 += q.m12;
        m13 += q.m13;
        m22 += q.m22;
        m23 += q.m23;
        m33 += q.m33;
        
        return this;
    }

    public Quadric setZero(){
        
        m00 = 0;
        m01 = 0;
        m02 = 0;
        m03 = 0;
        m11 = 0;
        m12 = 0;
        m13 = 0;
        m22 = 0;
        m23 = 0;
        m33 = 0;
        
        return this;

    }
    
    public String toString(){
        return fmt("[%8.5e, %8.5e,%8.5e,%8.5e;%8.5e,%8.5e,%8.5e;%8.5e,%8.5e;%8.5e]", 
                   m00, m01, m02, m03, m11, m12, m13, m22, m23, m33);
    }
    
    public double determinant(){
        
        Matrix3d m = new Matrix3d(m00, m01, m02,
                                  m01, m11, m12,
                                  m02, m12, m22
                                  );
        //printf("m: %s\n", m);
        
        return m.determinant();
    }
    
    public Point3d getMinimum(Point3d pnt){
        
        Matrix3d m = new Matrix3d(m00, m01, m02,
                                  m01, m11, m12,
                                  m02, m12, m22
                                  );
        m.invert();
        pnt.set(m03, m13, m23);
        m.transform(pnt);
        pnt.scale(-1);
        return pnt;
    }
    
    /**
       returns quadric centered in the middle between points p0 and p1
     */
    public static Quadric getMidEdgeQuadric(Point3d p0, Point3d p1, double scale, Quadric outQ){
        double 
            x = 0.5*(p0.x + p1.x),
            y = 0.5*(p0.y + p1.y),
            z = 0.5*(p0.z + p1.z);
        
        outQ.m00 = scale;
        outQ.m01 = 0;
        outQ.m02 = 0;
        outQ.m03 = -x*scale;
        outQ.m11 = scale;
        outQ.m12 = 0;
        outQ.m13 = -y*scale;
        outQ.m22 = scale;
        outQ.m23 = -z*scale;;
        outQ.m33 = (x*x + y*y + z*z)*scale;

        return outQ;

    }

    public Object clone(){
        return new Quadric(this);
    }
    
    public static Vector4d makePlane(Point3d p0, Point3d p1, Point3d p2, Vector4d plane){
        
        Vector3d v0 = new Vector3d(p0);
        Vector3d v1 = new Vector3d(p1);
        Vector3d v2 = new Vector3d(p2);
        
        v1.sub(p0);
        v2.sub(p0);
        
        Vector3d normal = new Vector3d();
        normal.cross(v1, v2);
        normal.normalize();

        if(normal.x == Double.NaN || 
           normal.y == Double.NaN || 
           normal.z == Double.NaN ){
            printf("****BAD normal: %s %s %s\n", normal.x,normal.y,normal.z);
            printf("v0: %s\n", v0);
            printf("v1: %s\n", v1);
            printf("v2: %s\n", v2);
        }

        if(plane == null)
            plane = new Vector4d();
        
        plane.x = normal.x;
        plane.y = normal.y;
        plane.z = normal.z;
        plane.w = -normal.dot(v0);
        
        return plane;
        
    }
    
    
    public static double planePointDistance2(Vector4d plane, Tuple3d point){
        
        double d = plane.x * point.x + plane.y * point.y + plane.z * point.z + plane.w;
        return d*d;
        
    }                
    
} // Quadric 
