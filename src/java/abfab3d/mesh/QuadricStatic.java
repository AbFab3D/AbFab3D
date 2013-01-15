/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
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
import abfab3d.util.StructDataDefinition;
import abfab3d.util.StructMixedData;

import javax.vecmath.*;

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;

/**
 * Reimpl of Quadric.
 *
 * @author Alan Hudson
 */
public class QuadricStatic extends StructDataDefinition {
    public static final int DOUBLE_DATA_SIZE = 10;
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

    /**
     unit quadric centered at origin
     */
    public static int createQuadric(StructMixedData dest) {
        int destIdx = dest.addItem();
        int dest_double_pos = destIdx * DOUBLE_DATA_SIZE;

        double[] dest_double = dest.getDoubleData();

        dest_double[dest_double_pos + POS_M00] = 1;
        dest_double[dest_double_pos + POS_M11] = 1;
        dest_double[dest_double_pos + POS_M22] = 1;

        // the rest is 0;

        return destIdx;
    }

    /**
     copy constructor
     */

    public static int createQuadric(StructMixedData src, int srcIdx, StructMixedData dest) {

        int destIdx = dest.addItem();

        set(src, srcIdx, dest, destIdx);

        return destIdx;
    }

    public static int createQuadric(double e00, double e01, double e02,  double e03, double e11, double e12, double e13,
                             double e22, double e23,double e33, StructMixedData dest) {

        int destIdx = dest.addItem();
        int dest_double_pos = destIdx * DOUBLE_DATA_SIZE;
        double[] dest_double = dest.getDoubleData();

        dest_double[dest_double_pos + POS_M00] = e00;
        dest_double[dest_double_pos + POS_M01] = e01;
        dest_double[dest_double_pos + POS_M02] = e02;
        dest_double[dest_double_pos + POS_M03] = e03;
        dest_double[dest_double_pos + POS_M11] = e11;
        dest_double[dest_double_pos + POS_M12] = e12;
        dest_double[dest_double_pos + POS_M13] = e13;
        dest_double[dest_double_pos + POS_M22] = e22;
        dest_double[dest_double_pos + POS_M23] = e23;
        dest_double[dest_double_pos + POS_M33] = e33;

        return destIdx;
    }

    /**
     quadric centered at midpoint of p0, p1 and scaled by scale
     */

    public static int createQuadric(Point3d p0, Point3d p1, double scale, StructMixedData dest) {

        int destIdx = dest.addItem();

        getMidEdgeQuadric(p0, p1, scale, dest,destIdx);

        return destIdx;
    }

    /**
     Construct a quadric to evaluate the squared distance of any point
     to the given plane [ax+by+cz+d = 0].  This is the "fundamental error
     quadric" discussed in the Garland's paper
     */

    public static int createQuadric(double a, double b, double c, double d, StructMixedData dest) {

        int destIdx = dest.addItem();

        init(a,b,c,d, dest, destIdx);

        return destIdx;
    }


    public static int createQuadric(Vector4d plane, StructMixedData dest) {

        int destIdx = dest.addItem();

        init(plane.x, plane.y, plane.z, plane.w, dest, destIdx);

        return destIdx;
    }

    /**
     return quadric for plane via 3 points
     */

    public static int createQuadric(Point3d p0,Point3d p1,Point3d p2, StructMixedData dest){

        Vector4d plane = new Vector4d();

        Vector3d v0 = new Vector3d(p0);
        Vector3d v1 = new Vector3d(p1);
        Vector3d v2 = new Vector3d(p2);

        Vector3d normal = new Vector3d();

        makePlane(v0, v1, v2, normal, plane);

        int destIdx = dest.addItem();

        init(plane.x, plane.y, plane.z, plane.w, dest, destIdx);

        return destIdx;
    }

    private static void init(double a, double b, double c, double d, StructMixedData dest, int destIdx) {

        int dest_double_pos = destIdx * DOUBLE_DATA_SIZE;
        double[] dest_double = dest.getDoubleData();

        dest_double[dest_double_pos + POS_M00] = a*a;
        dest_double[dest_double_pos + POS_M01] = a*b;
        dest_double[dest_double_pos + POS_M02] = a*c;
        dest_double[dest_double_pos + POS_M03] = a*d;

        dest_double[dest_double_pos + POS_M11] = b*b;
        dest_double[dest_double_pos + POS_M12] = b*c;
        dest_double[dest_double_pos + POS_M13] = b*d;

        dest_double[dest_double_pos + POS_M22] = c*c;
        dest_double[dest_double_pos + POS_M23] = c*d;

        dest_double[dest_double_pos + POS_M33] = d*d;
    }

    /**
     evaluate value of the form at given point
     */

    public static double evaluate(Point3d p, StructMixedData src, int srcIdx){
        double
                x = p.x,
                y = p.y,
                z = p.z;

        int src_double_pos = srcIdx * DOUBLE_DATA_SIZE;
        double[] src_double = src.getDoubleData();

        double m00 = src_double[src_double_pos + POS_M00];
        double m01 = src_double[src_double_pos + POS_M01];
        double m02 = src_double[src_double_pos + POS_M02];
        double m03 = src_double[src_double_pos + POS_M03];
        double m11 = src_double[src_double_pos + POS_M11];
        double m12 = src_double[src_double_pos + POS_M12];
        double m13 = src_double[src_double_pos + POS_M13];
        double m22 = src_double[src_double_pos + POS_M22];
        double m23 = src_double[src_double_pos + POS_M23];
        double m33 = src_double[src_double_pos + POS_M33];

        return
                x*x*m00 + 2*x*y*m01 + 2*x*z*m02 + 2*x*m03
                        +           y*y*m11 + 2*y*z*m12 + 2*y*m13
                        +                       z*z*m22 + 2*z*m23
                        +                                     m33;
    }

    public static void set(StructMixedData src, int srcIdx, StructMixedData dest, int destIdx){

        int dest_double_pos = destIdx * DOUBLE_DATA_SIZE;
        double[] dest_double = dest.getDoubleData();

        int src_double_pos = srcIdx * DOUBLE_DATA_SIZE;
        double[] src_double = src.getDoubleData();

        dest_double[dest_double_pos + POS_M00] = src_double[src_double_pos + POS_M00];
        dest_double[dest_double_pos + POS_M01] = src_double[src_double_pos + POS_M01];
        dest_double[dest_double_pos + POS_M02] = src_double[src_double_pos + POS_M02];
        dest_double[dest_double_pos + POS_M03] = src_double[src_double_pos + POS_M03];
        dest_double[dest_double_pos + POS_M11] = src_double[src_double_pos + POS_M11];
        dest_double[dest_double_pos + POS_M12] = src_double[src_double_pos + POS_M12];
        dest_double[dest_double_pos + POS_M13] = src_double[src_double_pos + POS_M13];
        dest_double[dest_double_pos + POS_M22] = src_double[src_double_pos + POS_M22];
        dest_double[dest_double_pos + POS_M23] = src_double[src_double_pos + POS_M23];
        dest_double[dest_double_pos + POS_M33] = src_double[src_double_pos + POS_M33];
    }

    public static void addSet(StructMixedData src, int srcIdx, StructMixedData dest, int destIdx) {

        int dest_double_pos = destIdx * DOUBLE_DATA_SIZE;
        double[] dest_double = dest.getDoubleData();

        int src_double_pos = srcIdx * DOUBLE_DATA_SIZE;
        double[] src_double = src.getDoubleData();

        dest_double[dest_double_pos + POS_M00] += src_double[src_double_pos + POS_M00];
        dest_double[dest_double_pos + POS_M01] += src_double[src_double_pos + POS_M01];
        dest_double[dest_double_pos + POS_M02] += src_double[src_double_pos + POS_M02];
        dest_double[dest_double_pos + POS_M03] += src_double[src_double_pos + POS_M03];
        dest_double[dest_double_pos + POS_M11] += src_double[src_double_pos + POS_M11];
        dest_double[dest_double_pos + POS_M12] += src_double[src_double_pos + POS_M12];
        dest_double[dest_double_pos + POS_M13] += src_double[src_double_pos + POS_M13];
        dest_double[dest_double_pos + POS_M22] += src_double[src_double_pos + POS_M22];
        dest_double[dest_double_pos + POS_M23] += src_double[src_double_pos + POS_M23];
        dest_double[dest_double_pos + POS_M33] += src_double[src_double_pos + POS_M33];
    }

    public static void addSet(Vector4d plane, StructMixedData dest, int destIdx) {

        int dest_double_pos = destIdx * DOUBLE_DATA_SIZE;
        double[] dest_double = dest.getDoubleData();

        double a = plane.x;
        double b = plane.y;
        double c = plane.z;
        double d = plane.w;

        dest_double[dest_double_pos + POS_M00] += a*a;
        dest_double[dest_double_pos + POS_M01] += a*b;
        dest_double[dest_double_pos + POS_M02] += a*c;
        dest_double[dest_double_pos + POS_M03] += a*d;

        dest_double[dest_double_pos + POS_M11] += b*b;
        dest_double[dest_double_pos + POS_M12] += b*c;
        dest_double[dest_double_pos + POS_M13] += b*d;

        dest_double[dest_double_pos + POS_M22] += c*c;
        dest_double[dest_double_pos + POS_M23] += c*d;

        dest_double[dest_double_pos + POS_M33] += d*d;
    }

    public static void setZero(StructMixedData dest, int destIdx){

        int dest_double_pos = destIdx * DOUBLE_DATA_SIZE;
        double[] dest_double = dest.getDoubleData();

        dest_double[dest_double_pos + POS_M00] = 0;
        dest_double[dest_double_pos + POS_M01] = 0;
        dest_double[dest_double_pos + POS_M02] = 0;
        dest_double[dest_double_pos + POS_M03] = 0;
        dest_double[dest_double_pos + POS_M11] = 0;
        dest_double[dest_double_pos + POS_M12] = 0;
        dest_double[dest_double_pos + POS_M13] = 0;
        dest_double[dest_double_pos + POS_M22] = 0;
        dest_double[dest_double_pos + POS_M23] = 0;
        dest_double[dest_double_pos + POS_M33] = 0;

    }

    public static String toString(StructMixedData src, int srcIdx){
        int src_double_pos = srcIdx * DOUBLE_DATA_SIZE;
        double[] src_double = src.getDoubleData();

        double m00 = src_double[src_double_pos + POS_M00];
        double m01 = src_double[src_double_pos + POS_M01];
        double m02 = src_double[src_double_pos + POS_M02];
        double m03 = src_double[src_double_pos + POS_M03];
        double m11 = src_double[src_double_pos + POS_M11];
        double m12 = src_double[src_double_pos + POS_M12];
        double m13 = src_double[src_double_pos + POS_M13];
        double m22 = src_double[src_double_pos + POS_M22];
        double m23 = src_double[src_double_pos + POS_M23];
        double m33 = src_double[src_double_pos + POS_M33];

        return fmt("[%8.5e, %8.5e,%8.5e,%8.5e;%8.5e,%8.5e,%8.5e;%8.5e,%8.5e;%8.5e]",
                m00, m01, m02, m03, m11, m12, m13, m22, m23, m33);
    }

    public static double determinant(StructMixedData src, int srcIdx){

        int src_double_pos = srcIdx * DOUBLE_DATA_SIZE;
        double[] src_double = src.getDoubleData();

        double m00 = src_double[src_double_pos + POS_M00];
        double m01 = src_double[src_double_pos + POS_M01];
        double m02 = src_double[src_double_pos + POS_M02];
        double m11 = src_double[src_double_pos + POS_M11];
        double m12 = src_double[src_double_pos + POS_M12];
        double m22 = src_double[src_double_pos + POS_M22];

        return
                +m00*(m11*m22 - m12*m12)
                        -m01*(m01*m22 - m12*m02)
                        +m02*(m01*m12 - m11*m02);
    }

    public static void getMinimum(StructMixedData src, int srcIdx, Point3d pnt, Matrix3d m, double result[], int[] row_perm, double[] row_scale, double[] tmp) {
        int src_double_pos = srcIdx * DOUBLE_DATA_SIZE;
        double[] src_double = src.getDoubleData();

        double m00 = src_double[src_double_pos + POS_M00];
        double m01 = src_double[src_double_pos + POS_M01];
        double m02 = src_double[src_double_pos + POS_M02];
        double m03 = src_double[src_double_pos + POS_M03];
        double m11 = src_double[src_double_pos + POS_M11];
        double m12 = src_double[src_double_pos + POS_M12];
        double m13 = src_double[src_double_pos + POS_M13];
        double m22 = src_double[src_double_pos + POS_M22];
        double m23 = src_double[src_double_pos + POS_M23];
        double m33 = src_double[src_double_pos + POS_M33];

        m.m00 = m00;
        m.m01 = m01;
        m.m02 = m02;
        m.m10 = m01;
        m.m11 = m11;
        m.m12 = m12;
        m.m20 = m02;
        m.m21 = m12;
        m.m22 = m22;
/*
        // Non garbage way
        MathUtil.invertGeneral(m, result, row_perm, row_scale, tmp);


        // Old way
        //m.invert();
        pnt.set(m03, m13, m23);
        m.transform(pnt);
        pnt.scale(-1);
        */

        double det =
                m00*(m11*m22 - m12*m12)
                        -m01*(m01*m22 - m12*m02)
                        +m02*(m01*m12 - m11*m02);

        double invdet = 1./det;
        double
                r00 =  (m11*m22 - m12*m12)*invdet,
                r01 = -(m01*m22 - m02*m12)*invdet,
                r02 =  (m01*m12 - m02*m11)*invdet,
                r11 =  (m00*m22 - m02*m02)*invdet,
                r12 = -(m00*m12 - m01*m02)*invdet,
                r22 =  (m00*m11 - m01*m01)*invdet;

        double p0 = m03, p1 = m13, p2 = m23;
        pnt.x = -(r00*p0 + r01*p1 + r02*p2);
        pnt.y = -(r01*p0 + r11*p1 + r12*p2);
        pnt.z = -(r02*p0 + r12*p1 + r22*p2);

        //return pnt;
    }

    /**
     returns quadric centered in the middle between points p0 and p1
     */
    public static void getMidEdgeQuadric(Point3d p0, Point3d p1, double scale, StructMixedData dest, int destIdx){
        int dest_double_pos = destIdx * DOUBLE_DATA_SIZE;
        double[] dest_double = dest.getDoubleData();


        double
                x = 0.5*(p0.x + p1.x),
                y = 0.5*(p0.y + p1.y),
                z = 0.5*(p0.z + p1.z);

        dest_double[dest_double_pos + POS_M00] = scale;
        dest_double[dest_double_pos + POS_M01] = 0;
        dest_double[dest_double_pos + POS_M02] = 0;
        dest_double[dest_double_pos + POS_M03] = -x*scale;
        dest_double[dest_double_pos + POS_M11] = scale;
        dest_double[dest_double_pos + POS_M12] = 0;
        dest_double[dest_double_pos + POS_M13] = -y*scale;
        dest_double[dest_double_pos + POS_M22] = scale;
        dest_double[dest_double_pos + POS_M23] = -z*scale;
        dest_double[dest_double_pos + POS_M33] = (x*x + y*y + z*z)*scale;;
    }
         /*
    public Object clone(){
        return new Quadric(this);
    }
           */
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

    public static double getM00(StructMixedData src, int srcIdx) {
        int src_double_pos = srcIdx * DOUBLE_DATA_SIZE;
        double[] src_double = src.getDoubleData();

        return src_double[src_double_pos + POS_M00];
    }

    public int getDoubleDataSize() {
        return DOUBLE_DATA_SIZE;
    }
}
