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
import javax.vecmath.Vector3d;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4d;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.TODEGREE;

/**
 */
public class TestMathUtil extends TestCase {

    static final double EPS = 1.e-9;
    static final double SEPS = 1.e-20;
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

    public void devTestIntersectIF(){
        Vector3d p0 = new Vector3d(0,0,0);
        Vector3d p1 = new Vector3d(1,0,0);
        //double v0 = 0;
        //double v1 = 1;
        Vector3d p2 = new Vector3d(0,0,0);

        //MathUtil.intersectIF(p0, p1, v0, v1, p2);
        
        //printf("p0: (%4.2f,%4.2f,%4.2f) p1:(%4.2f,%4.2f,%4.2f),v0: %4.2f v1: %4.2f p2:(%4.2f,%4.2f,%4.2f)\n", p0.x,p0.y,p0.z, p1.x,p1.y,p1.z,v0,v1, p2.x,p2.y,p2.z);
        int N = 10;
        for(int y = 0; y <= N; y++){
            for(int x = 0; x <= N; x++){
                double v0 = -0.1*x-0.01;
                double v1 = 0.1*y;                
                MathUtil.intersectIF(p0, p1, v0, v1, p2);
                printf("%5.2f ", p2.x);
            }
            printf("\n");
        }
    }

    public void devTestTripleProduct(){
        Vector3d v1 = new Vector3d(1,1,0);
        Vector3d v2 = new Vector3d(0,1,1);
        Vector3d v3 = new Vector3d(1,0,1);
        //double tp = MathUtil.tripleProduct(v1, v2, v3);
        double tp = MathUtil.tripleProduct(v1, v3, v2);
        printf("tp:%7.5f\n", tp);
    }

    public void devTestAxisAngle1(){
    
        Vector3d v1 = new Vector3d(-1,0,0);
        Vector3d v2 = new Vector3d(0,-1,0);
        Vector3d v3 = new Vector3d(0,0,1);
        AxisAngle4d aa = MathUtil.getAxisAngle(v1,v2,v3);
        printf("aa: %9.6f,%9.6f,%9.6f, %9.6f deg\n", aa.x, aa.y, aa.z, aa.angle*180/Math.PI);
    }

    public void devTestAxisAngle2(){
        
        Random rnd = new Random(101);
        int N = 100000;
        Vector3d vmax[] = new Vector3d[]{new Vector3d(),new Vector3d(),new Vector3d()};
        double dmax = 0;
        for(int i = 0; i < N; i++){
            Vector3d v1 = new Vector3d(2*rnd.nextDouble()-1,2*rnd.nextDouble()-1,2*rnd.nextDouble()-1);
            v1.normalize();
            Vector3d v3 = new Vector3d(2*rnd.nextDouble()-1,2*rnd.nextDouble()-1,2*rnd.nextDouble()-1);
            v3.normalize();            
            Vector3d v2 = new Vector3d();
            v2.cross(v3, v1);
            v2.normalize();
            v3.cross(v1,v2);
            v3.normalize();
            //printf("v1: (%7.5f,%7.5f,%7.5f) v2:  (%7.5f,%7.5f,%7.5f) v3:  (%7.5f,%7.5f,%7.5f)\n", v1.x,v1.y,v1.z, v2.x,v2.y,v2.z, v3.x,v3.y,v3.z);        
            Matrix3d m = new Matrix3d(v1.x,v2.x, v3.x,v1.y,v2.y,v3.y,v1.z,v2.z,v3.z);
            AxisAngle4d aa = MathUtil.getAxisAngle(m);
            //aa.set(m);
            //printf("aa: %7.5f,%7.5f,%7.5f,%7.5f deg\n", aa.x, aa.y, aa.z, aa.angle*180/Math.PI);
            Matrix3d r = new Matrix3d();
            r.set(aa);
            Vector3d u1 = new Vector3d(1,0,0);
            Vector3d u2 = new Vector3d(0,1,0);
            Vector3d u3 = new Vector3d(0,0,1);
            r.transform(u1);
            r.transform(u2);
            r.transform(u3);           
            //printf("u1: (%7.5f,%7.5f,%7.5f) u2:  (%7.5f,%7.5f,%7.5f) u3:  (%7.5f,%7.5f,%7.5f)\n", u1.x,u1.y,u1.z, u2.x,u2.y,u2.z, u3.x,u3.y,u3.z); 
            double d1 = MathUtil.getDistance(v1, u1);
            double d2 = MathUtil.getDistance(v2, u2);
            double d3 = MathUtil.getDistance(v3, u3);
            double d = max(max(d1, d2),d3);
            if(d > dmax){
                vmax[0].set(v1);
                vmax[1].set(v2);
                vmax[2].set(v3);
                dmax = d;
                //printf("dmax: %14.7e\n", dmax); 
                //printf("v1max: %9.6f %9.6f %9.6f\n", vmax[0].x,vmax[0].y,vmax[0].z); 
                //printf("v1max: %9.6f %9.6f %9.6f\n", vmax[1].x,vmax[1].y,vmax[1].z); 
                //printf("v1max: %9.6f %9.6f %9.6f\n", vmax[2].x,vmax[2].y,vmax[2].z); 
            }
        }
        printf("dmax: %14.7e\n", dmax); 
        printf("v1max: %9.6f %9.6f %9.6f\n", vmax[0].x,vmax[0].y,vmax[0].z); 
        printf("v1max: %9.6f %9.6f %9.6f\n", vmax[1].x,vmax[1].y,vmax[1].z); 
        printf("v1max: %9.6f %9.6f %9.6f\n", vmax[2].x,vmax[2].y,vmax[2].z); 
        printf("dot\n");
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                printf("%9.7f ", vmax[i].dot(vmax[j]));
            }
            printf("\n");
        }
    }

    public void devTestAxisAngle3(){

        AxisAngle4d aa = new AxisAngle4d();
        
        Random rnd = new Random(101);
        int N = 10;
        Vector3d vmax[] = new Vector3d[]{new Vector3d(),new Vector3d(),new Vector3d()};
        Vector3d axis = new Vector3d();
        double dmax = 0;
        Matrix3d rot = new Matrix3d();
        for(int i = 0; i < N; i++){
            axis.set(2*rnd.nextDouble()-1,2*rnd.nextDouble()-1,2*rnd.nextDouble()-1);
            axis.normalize();
            //double angle = rnd.nextDouble()*2*Math.PI;
            double angle = Math.PI;
            aa.set(axis.x,axis.y,axis.z,angle);
            rot.set(aa);
            AxisAngle4d aa1 = MathUtil.getAxisAngle(rot);
            printf("aa:(%8.5f,%8.5f,%8.5f;%10.5f), aa1:(%8.5f,%8.5f,%8.5f;%10.5f)\n", aa.x,aa.y,aa.z,aa.angle*TODEGREE,aa1.x,aa1.y,aa1.z,aa1.angle*TODEGREE);
        }
    }


    public static void main(String arg[]){

        //new TestMathUtil().testInversion3b();
        //new TestMathUtil().testInversion3a();
        //new TestMathUtil().testSolveLinear3();
        //new TestMathUtil().testSolveLinear3a();
        //new TestMathUtil().testGetBestPlane();
        //new TestMathUtil().testGetBestPlane();
        //new TestMathUtil().testGetBestPlane2();
        //new TestMathUtil().testIntersectIF();
        //new TestMathUtil().testTripleProduct();
        //new TestMathUtil().testAxisAngle();
        new TestMathUtil().devTestAxisAngle3();
        
    }
}
