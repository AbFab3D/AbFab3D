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

        //new TestMathUtil().testSolveLinear3();
        //new TestMathUtil().testSolveLinear3a();
        //new TestMathUtil().testGetBestPlane();
        //new TestMathUtil().testGetBestPlane();
        //new TestMathUtil().testGetBestPlane2();

        
    }
}
