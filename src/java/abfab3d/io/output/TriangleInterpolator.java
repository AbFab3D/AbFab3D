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
package abfab3d.io.output;

import abfab3d.util.MathUtil;
/**
   implements linear transformation which maps 2D triangle into 3D triangle 
*/
public class TriangleInterpolator {


    static final int // elements of 3x3 matrix stored in array 
        M00 = 0, M01 = 1, M02 = 2, 
        M10 = 3, M11 = 4, M12 = 5, 
        M20 = 6, M21 = 7, M22 = 8; 


    
    double  m0[] = new double[3]; // matrix of x-transforms 
    double  m1[] = new double[3]; // matrix of y-transforms 
    double  m2[] = new double[3]; // matrix of z-transforms 
    double X[] = new double[3];       // work vector 
   
 
    /**
       get 3D point from 2D 
    */
    void interpolate(double u, double v, double p[]){
        
        p[0] = m0[0]*u + m0[1]*v + m0[2];
        p[1] = m1[0]*u + m1[1]*v + m1[2];
        p[2] = m2[0]*u + m2[1]*v + m2[2];
    }
    
    /**
       creates matrix of linear transform interpolates maps 3 points of 2D triangle t[3][2] into 3 points of 3D triangle p[3][3]
       @param t - 2D triangle 
       @param p - 3D triangle 
    */
    void init(double t[][], double p[][]){
        //
        // we need linear transform which maps triple of 2D coordinates (u,v) into triple of 3D coordinates (x,y,z) 
        // for x coordinate we have set of linear equations for (m00 m01 m02) 
        // m0[0] * u0 + m0[1] * v0 + m0[2] = x0 
        // m0[0] * u1 + m0[1] * v1 + m0[2] = x1
        // m0[0] * u2 + m0[1] * v2 + m0[2] = x2 
        // these equation can be written in matrix form 
        //  UV * m0 = X (1) 
        // where UV is 3x3 matrix 
        //  (u0  v0  1 )  
        //  (u1  v1  1 ) 
        //  (u2  v2  1 ) 
        // M is column (m0[0] m0[1] m0[2])^T
        // X is column (x0, x1, x2)^T 
        // solution of equation (1) is  m0 = UV^(-1) * X
        // the equations for Y and Z components looks as (1) with replacement X -> Y and X -> Z
        // and using m1 and m2 for unknown 
        
        double 
            u0 = t[0][0], u1 = t[1][0], u2 = t[2][0],
            v0 = t[0][1], v1 = t[1][1], v2 = t[2][1];            
        // determinant of the matrix 
        double det = 
            (u0*v1 - u1*v0) + 
            (u1*v2 - u2*v1) + 
            (u2*v0 - u0*v2);
        if(det == 0 ) {
            // degenerate triangle 
            return;// RES_DEGENERATE_TRIANGLE; 
        }
        
        // elements of the inverse matrix UV^(-1) 
        //        (    v1-v2         v2-v0         v0-v1     )
        // 1/det  (    u2-u1         u0-u2         u1-u0     )
        //        (  u1*v2-u2*v1   u2*v0-u0*v2   u0*v1-u1*v0 )
        
        // invert determinant to save on divisions 
        det = 1./det;
        
        double in[] = new double[9];
        
        in[M00] = (v1 - v2)*det;
        in[M01] = (v2 - v0)*det;
        in[M02] = (v0 - v1)*det;
        in[M10] = (u2 - u1)*det;
        in[M11] = (u0 - u2)*det;
        in[M12] = (u1 - u0)*det;
        in[M20] = (u1*v2-u2*v1)*det;
        in[M21] = (u2*v0-u0*v2)*det;
        in[M22] = (u0*v1-u1*v0)*det;
        // interpolate x-component 
        X[0] = p[0][0]; 
        X[1] = p[1][0];
        X[2] = p[2][0];
        MathUtil.multMV3(in, X, m0);
        // interpolate y-component 
        X[0] = p[0][1];
        X[1] = p[1][1];
        X[2] = p[2][1];
        MathUtil.multMV3(in, X, m1);
        // interpolate z-component 
        X[0] = p[0][2];
        X[1] = p[1][2];
        X[2] = p[2][2];
        MathUtil.multMV3(in, X, m2);            
    }
}
