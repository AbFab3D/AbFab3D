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

package abfab3d.util;

// External Imports

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Random;
import javax.vecmath.Matrix3d;

import static java.lang.Math.abs;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;

/**
 */
public class TestMathUtil extends TestCase {

    static final double EPS = 1.e-9;
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestMathUtil.class);
    }

    public void testInversion3() {

        printf("testInversion3\n");

        //double m[] = new double[]{1,0,0,0,2,0,0,0,3};
        double m[] = new double[]{1,2,3,4,5,6,7,8,10};
        double n[] = MathUtil.copyMatrix3(m, new double[9]); 
        
        if(MathUtil.invertMatrix3(m) == 0)
            throw new RuntimeException(fmt("non invertible matrix:(%7.5f %7.5f %7.5f;%7.5f %7.5f %7.5f; %7.5f, %7.5f, %7.5f)",
                                           m[0],m[1],m[2],m[3],m[4],m[5],m[6],m[7],m[8] ));
        double r[] = new double[9];
        MathUtil.multMM3(m,n,r);
        printf("n:\n(%7.5f, %7.5f, %7.5f)\n(%7.5f, %7.5f, %7.5f)\n(%7.5f, %7.5f, %7.5f)\n",n[0],n[1],n[2],n[3],n[4],n[5],n[6],n[7],n[8]);
        printf("m:\n(%7.5f, %7.5f, %7.5f)\n(%7.5f, %7.5f, %7.5f)\n(%7.5f, %7.5f, %7.5f)\n",m[0],m[1],m[2],m[3],m[4],m[5],m[6],m[7],m[8]);
        printf("n*m:\n(%7.5f, %7.5f, %7.5f)\n(%7.5f, %7.5f, %7.5f)\n(%7.5f, %7.5f, %7.5f)\n",r[0],r[1],r[2],r[3],r[4],r[5],r[6],r[7],r[8]);
        double dd = MathUtil.maxDistance(r, MathUtil.getUnitMatrix3(null));
        printf("max distance from unit maxtix: %9.5g\n",dd );                                       
               assertTrue(fmt("failed invertMatrix maxDIstance: %9.5g", dd), dd < EPS );
                
    }

    public void testInversion3a() {

        printf("testInversion3a\n");

        double m[] = new double[9];
        double n[] = new double[9];
        double r[] = new double[9];

        Random rnd = new Random(101);
        int N = 10000000;
        long t0 = time();
        double maxDist = 0;
        for(int t = 0; t < N; t++){
            for(int k = 0; k < 9; k++){
                m[k] = rnd.nextDouble();
            }
            MathUtil.copyMatrix3(m, n);         
            if(MathUtil.invertMatrix3(m) == 0){
                printf("uninvertible\n");                                                   
            } else {
                MathUtil.multMM3(m,n,r);
                double dd = MathUtil.maxDistance(r, MathUtil.getUnitMatrix3(null));
                if(dd > maxDist) 
                    maxDist = dd;
            }
        }                
        printf("count: %d time: %d ms max error: %9.5g\n",N, (time() - t0),maxDist);                                       
    }

    
    public void testInversion3b() {

        printf("testInversion3a\n");

        double m[] = new double[9];
        double n[] = new double[9];
        double r[] = new double[9];

        double result[] = new double[9];
        int row_perm[] = new int[3];
        double row_scale[] = new double[3];
        double tmp [] = new double[9];
        Matrix3d mm = new Matrix3d();


        Random rnd = new Random(101);
        int N = 10000000;
        long t0 = time();
        double maxDist = 0;
        for(int t = 0; t < N; t++){
            for(int k = 0; k < 9; k++){
                m[k] = rnd.nextDouble();
            }
            MathUtil.copyMatrix3(m, mm);
            MathUtil.copyMatrix3(m, n);

            MathUtil.invertGeneral(mm, result, row_perm, row_scale, tmp);

            MathUtil.copyMatrix3(mm, m);
                    
            MathUtil.multMM3(m,n,r);

            //printf("n:\n(%7.5f, %7.5f, %7.5f)\n(%7.5f, %7.5f, %7.5f)\n(%7.5f, %7.5f, %7.5f)\n",n[0],n[1],n[2],n[3],n[4],n[5],n[6],n[7],n[8]);
            //printf("m:\n(%7.5f, %7.5f, %7.5f)\n(%7.5f, %7.5f, %7.5f)\n(%7.5f, %7.5f, %7.5f)\n",m[0],m[1],m[2],m[3],m[4],m[5],m[6],m[7],m[8]);
            //printf("n*m:\n(%7.5f, %7.5f, %7.5f)\n(%7.5f, %7.5f, %7.5f)\n(%7.5f, %7.5f, %7.5f)\n",r[0],r[1],r[2],r[3],r[4],r[5],r[6],r[7],r[8]);

            double dd = MathUtil.maxDistance(r, MathUtil.getUnitMatrix3(null));
            if(dd > maxDist) 
                maxDist = dd;        
        }                
        printf("count: %d time: %d ms max error: %9.5g\n",N, (time() - t0),maxDist);                                       
    }


    public void testSolveLinear3() {

        printf("testSolveLinear3()\n");

        double m[] = new double[]{3,2,-1,2,-1,-3,1,3,-2};
        double c[] = new double[]{2,13,1};
        double x[] = new double[3];
        MathUtil.solveLinear3(m,c,x);
        printf("x: (%15.12f,%15.12f,%15.12f)\n",x[0],x[1],x[2]);
        double cc[] = new double[3];
        MathUtil.multMV3(m,x,cc);
        printf("cc: (%15.12f,%15.12f,%15.12f)\n",cc[0],cc[1],cc[2]);

        assertTrue(fmt("failed solveLinear3()"), abs(cc[0]-c[0]) < EPS && abs(cc[1]-c[1]) < EPS && abs(cc[2]-c[2]) < EPS);
        
        
    }

    public void testSolveLinear3a() {
        
        printf("testSolveLinear3a()\n");

        int N = 100000;
        long t0 = time();
        double m[] = new double[]{3,2,-1,2,-1,-3,1,3,-2};
        double c[] = new double[]{2,13,1};
        double x[] = new double[3];
        double cc[] = new double[3];
        Random rnd = new Random(101);
        double maxDelta = 0;
        int failed = 0;
        double sum = 0;

        for(int k = 0; k < N; k++){
            for(int i = 0; i < 9; i++)                
                m[i] = rnd.nextDouble();
            for(int i = 0; i < 3; i++)
                c[i] = rnd.nextDouble();
            
            if(MathUtil.solveLinear3(m,c,x)){
                //printf("x: (%15.12f,%15.12f,%15.12f)\n",x[0],x[1],x[2]);
                MathUtil.multMV3(m,x,cc);
                double delta = MathUtil.maxDistance(cc,c);
                if(delta > maxDelta)
                    maxDelta = delta;
                sum += delta;
            } else {
                failed++;
            }
        }
        double avrgDelta  = sum/N;
        printf("tested: %d failed = %d max error: %10.3e  avrg error: %10.3e timing: %d ms\n", N, failed, maxDelta, avrgDelta,(time() - t0));
        assertTrue(fmt("failed solveLinear3a() maxDelta:%10.3e", maxDelta), maxDelta < 1.e-9);        
        
    }
    
    public void testGetBestPlane(){
        
        printf("testGetBestPlane()\n");
        
        double coords[][] = new double[][]{
            {1,1,0, 1,0,1, 0,1,1, 2./3,2./3, 2./3 },
            {-0.377964473009227, -0.377964473009227, -0.377964473009227, 0.755928946018454},//{-0.5, -0.5, -0.5, 1.}, // plane 
            {1,1,-2, 1,-2,1, -2,1,1.0},
            {0.577350269189626,  0.577350269189626,  0.577350269189626,-0.000000000000000},//{1.0, 1., 1., 0}, // plane
            {1,1,0, 1,2,0, 2,1,0},
            {0,0,1,0}, // pane 
            {0,1,1, 0,1,2, 0,2,1}, 
            {1,0,0,0}, // plane 
            {1,0,1, 2,0,1, 1,0.,2}, 
            {0,1,0,0}, // plane 
        };
        
        double m[] = new double[9];
        double c[] = new double[3];
        double plane[] = new double[4];
        
        for(int k = 0; k < coords.length/2; k++){
            
            double coord[] = coords[2*k];
            double exact[] = coords[2*k+1];
            
            if(MathUtil.getBestPlane(coord, m, c, plane)){
                printf("plane: (%18.15f, %18.15f, %18.15f,%18.15f)\n", plane[0],plane[1],plane[2],plane[3]);
            } else {
                printf("no plane found\n"); 
            }
            assertTrue(fmt("failed getBestPlane() plane: (%18.15f, %18.15f, %18.15f, %18.15f)\n", plane[0],plane[1],plane[2],plane[3]), (MathUtil.maxDistance(plane, exact) < 1.e-6));
        }
    }


    public void testGetBestPlane2(){

        printf("testGetBestPlane2()\n");
        double coord[] = new double[9];        
        double plane[] = new double[4];        
        double m[] = new double[9];
        double c[] = new double[3];

        int N = 1000000;
        Random rnd = new Random(121);
        double maxDist = 0;
        long t0 = time();
        int failed = 0;
        for(int k = 0; k < N; k++){
            for(int i = 0; i < 9; i++){
                coord[i] = rnd.nextDouble();
            }
            if(MathUtil.getBestPlane(coord, m, c, plane)){

                // test point 0
                double d = abs(coord[0] * plane[0] + coord[1] * plane[1] + coord[2] * plane[2] +  plane[3]);
                if(d > maxDist){
                    maxDist = d;
                }
                // test point 1
                d = abs(coord[3] * plane[0] + coord[4] * plane[1] + coord[5] * plane[2] +  plane[3]);
                if(d > maxDist){
                    maxDist = d;
                }
                // test point 2
                d = abs(coord[6] * plane[0] + coord[7] * plane[1] + coord[8] * plane[2] +  plane[3]);
                if(d > maxDist){
                    maxDist = d;
                }
            } else {
                failed++;
            }
            //printf("d: %10.4e \n",d);
        }
        printf("count: %d failed: %d maxDist: %10.4e time: %d\n",N, failed, maxDist, (time() - t0));
        assertTrue(fmt("failed getBestPlane2() maxDist: %10.4e)", maxDist), (maxDist < 1.e-7));
        
    }


    public static void main(String arg[]){

        new TestMathUtil().testInversion3b();
        //new TestMathUtil().testInversion3a();
        //new TestMathUtil().testSolveLinear3();
        //new TestMathUtil().testSolveLinear3a();
        //new TestMathUtil().testGetBestPlane();
        //new TestMathUtil().testGetBestPlane();
        //new TestMathUtil().testGetBestPlane2();

        
    }
}
