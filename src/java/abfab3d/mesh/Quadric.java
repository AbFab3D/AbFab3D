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

import abfab3d.util.MathUtil;

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

        init(a,b,c,d);
    }
    
    public Quadric(Vector4d plane) {

        init(plane.x, plane.y, plane.z, plane.w);
    }

    /**
     return quadric for plane via 3 points
     */
    public Quadric(Point3d p0,Point3d p1,Point3d p2){

        Vector4d plane = new Vector4d();

        Vector3d v0 = new Vector3d(p0);
        Vector3d v1 = new Vector3d(p1);
        Vector3d v2 = new Vector3d(p2);

        Vector3d normal = new Vector3d();

        makePlane(v0, v1, v2, normal, plane);
        init(plane.x, plane.y, plane.z, plane.w);
    }

    private void init(double a, double b, double c, double d) {

        m00 = a*a;  m01 = a*b;  m02 = a*c;  m03 = a*d;
        m11 = b*b;  m12 = b*c;  m13 = b*d;
        m22 = c*c;  m23 = c*d;
        m33 = d*d;

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

    public void getMinimum(Point3d pnt, Matrix3d m, double result[], int[] row_perm, double[] row_scale, double[] tmp) {
        m.m00 = m00;
        m.m01 = m01;
        m.m02 = m02;
        m.m10 = m01;
        m.m11 = m11;
        m.m12 = m12;
        m.m20 = m02;
        m.m21 = m12;
        m.m22 = m22;

        // Non garbage way
        MathUtil.invertGeneral(m, result,row_perm, row_scale, tmp);
/*
        // Faster way?
        Matrix3d dest = new Matrix3d();
        MathUtil.invertAffine2(m, dest);
        m.set(dest);
  */

        // Old way
        //m.invert();
        pnt.set(m03, m13, m23);
        m.transform(pnt);
        pnt.scale(-1);
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

    /**
     * Make a plane from 3 points.
     *
     * @param v0
     * @param v1
     * @param v2
     * @param plane
     * @return true if successful, false if bad triangle
     */
    public static boolean makePlane(Vector3d v0, Vector3d v1, Vector3d v2, Vector3d normal, Vector4d plane){

        v1.sub(v0);
        v2.sub(v0);

        normal.cross(v1, v2);
        normal.normalize();

        if(Double.isNaN(normal.x)){
            printf("****BAD triangle\n");
            printf("v1: (%18.15g, %18.15g, %18.15g) \n", v1.x,v1.y,v1.z);
            printf("v2: (%18.15g, %18.15g, %18.15g) \n", v2.x,v2.y,v2.z);
            printf("p0: (%18.15g, %18.15g, %18.15g) \n", v0.x,v0.y,v0.z);
            return false;
        }

        if(plane == null)
            plane = new Vector4d();

        plane.x = normal.x;
        plane.y = normal.y;
        plane.z = normal.z;
        plane.w = -normal.dot(v0);

        return true;

    }

    
    public static double planePointDistance2(Vector4d plane, Tuple3d point){
        
        double d = plane.x * point.x + plane.y * point.y + plane.z * point.z + plane.w;
        return d*d;
        
    }

    // Methods on quadric data, class with no state

    public static final int DATA_SIZE = 10;
    public static final int POS_M00 = 0;
    public static final int POS_M01 = 1;
    public static final int POS_M02 = 2;
    public static final int POS_M03 = 3;
    public static final int POS_M11 = 4;
    public static final int POS_M12 = 5;
    public static final int POS_M13 = 6;
    public static final int POS_M22 = 7;
    public static final int POS_M23 = 8;
    public static final int POS_M33 = 9;

    public static int getDataSize() {
        return DATA_SIZE;
    }

    public static void setZero(double[] dest, int destPos){
        dest[destPos + POS_M00] = 0;
        dest[destPos + POS_M01] = 0;
        dest[destPos + POS_M02] = 0;
        dest[destPos + POS_M03] = 0;
        dest[destPos + POS_M11] = 0;
        dest[destPos + POS_M12] = 0;
        dest[destPos + POS_M13] = 0;
        dest[destPos + POS_M22] = 0;
        dest[destPos + POS_M23] = 0;
        dest[destPos + POS_M33] = 0;
    }

    private static void init(double a, double b, double c, double d, double[] dest, int destPos) {

        dest[destPos + POS_M00] = a*a;  dest[destPos + POS_M01] = a*b;  dest[destPos + POS_M02] = a*c;  dest[destPos + POS_M03] = a*d;
        dest[destPos + POS_M11] = b*b;  dest[destPos + POS_M12] = b*c;  dest[destPos + POS_M13] = b*d;
        dest[destPos + POS_M22] = c*c;  dest[destPos + POS_M23] = c*d;
        dest[destPos + POS_M33] = d*d;
    }
    
    public static void createQuadric(Vector4d plane, double[] dest, int destPos) {
        init(plane.x, plane.y, plane.z, plane.w, dest, destPos);
    }
    
    public static void addSet(double[] src, int srcPos, double[] dest, int destPos) {

        dest[destPos + POS_M00] += src[srcPos + POS_M00];
        dest[destPos + POS_M01] += src[srcPos + POS_M01];
        dest[destPos + POS_M02] += src[srcPos + POS_M02];
        dest[destPos + POS_M03] += src[srcPos + POS_M03];
        dest[destPos + POS_M11] += src[srcPos + POS_M11];
        dest[destPos + POS_M12] += src[srcPos + POS_M12];
        dest[destPos + POS_M13] += src[srcPos + POS_M13];
        dest[destPos + POS_M22] += src[srcPos + POS_M22];
        dest[destPos + POS_M23] += src[srcPos + POS_M23];
        dest[destPos + POS_M33] += src[srcPos + POS_M33];
    }

    public static void set(double[] src, int srcPos, double[] dest, int destPos){
        dest[destPos + POS_M00] = src[srcPos + POS_M00];
        dest[destPos + POS_M01] = src[srcPos + POS_M01];
        dest[destPos + POS_M02] = src[srcPos + POS_M02];
        dest[destPos + POS_M03] = src[srcPos + POS_M03];
        dest[destPos + POS_M11] = src[srcPos + POS_M11];
        dest[destPos + POS_M12] = src[srcPos + POS_M12];
        dest[destPos + POS_M13] = src[srcPos + POS_M13];
        dest[destPos + POS_M22] = src[srcPos + POS_M22];
        dest[destPos + POS_M23] = src[srcPos + POS_M23];
        dest[destPos + POS_M33] = src[srcPos + POS_M33];
    }

} // Quadric 
