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

package abfab3d.geom;

// External Imports
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.Vector3d;
import java.util.Random;


// Internal Imports
import abfab3d.distance.DistanceDataHalfSpace;

import abfab3d.util.PointMap;
import abfab3d.util.PointMap2;
import abfab3d.util.TrianglePrinter;

import abfab3d.io.output.STLWriter;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;
import static abfab3d.core.MathUtil.str;
import static java.lang.Math.*;


/**
 * Tests the functionality of TriangleSlicer
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestTriangleSlicer extends TestCase {


    

    /**+
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestParametricSurfaceMaker.class);
    }

    public void testNothing(){
        //this test here is to make Test happy. 
    }


    public void devTestRandom(){

        TriangleSlicer slicer = new TriangleSlicer();
        Vector3d q0 = new Vector3d();
        Vector3d q1 = new Vector3d();
        Random rnd = new Random(151);
        int N = 100000000;
        printf("devTestRandom() N: %d\n", N);
        double maxDist = 0;
        Vector3d 
            mp0 = new Vector3d(), 
            mp1 = new Vector3d(), 
            mp2 = new Vector3d(), 
            mNormal = new Vector3d(), 
            mpp = new Vector3d();
        double md0 = 0., md1 = 0., md2 = 0.;

        for(int i = 0; i < N; i++){
            Vector3d 
                p0 = getRandomVect(rnd),
                p1 = getRandomVect(rnd),
                p2 = getRandomVect(rnd);
            
            Vector3d normal = getRandomVect(rnd);
            Vector3d pp = getRandomVect(rnd);
            
            
            DistanceDataHalfSpace plane = new DistanceDataHalfSpace(normal, pp);
            double d0 = plane.getDistance(p0.x,p0.y,p0.z);
            double d1 = plane.getDistance(p1.x,p1.y,p1.z);
            double d2 = plane.getDistance(p2.x,p2.y,p2.z);
            int res = slicer.getIntersection(p0, p1, p2, d0, d1, d2, q0, q1);
            String format = "%7.5f";
            
            switch(res){
            case TriangleSlicer.INTERSECT: 
                double dq0 = abs(plane.getDistance(q0.x,q0.y,q0.z));
                double dq1 = abs(plane.getDistance(q1.x,q1.y,q1.z));
                //printf("intersect q0: %s, q1: %s, dq0:%18.15e dq1:%18.15e\n", str(format, q0),str(format, q1), dq0, dq1);
                if(dq0 > maxDist || dq1 > maxDist) {
                    maxDist = max(dq0,dq1);
                    mp0 = p0;
                    mp1 = p1;
                    mp2 = p2;
                    mNormal = normal;
                    mpp = pp;
                    md0 = d0;
                    md1 = d1;
                    md2 = d2;
                }
                break;
            case TriangleSlicer.INSIDE:                 
                //printf("inside\n");                
                break;
            case TriangleSlicer.OUTSIDE: 
                //printf("outside\n");                
                break;
            case TriangleSlicer.ERROR: 
                //printf("error\n");                
                break;
            }
        }
        
        printf("maxDist: %6.2e\n", maxDist);
        String f = "%18.15f";
        printf("mp0:   %s\n", str(f, mp0));
        printf("mp1:   %s\n", str(f, mp1));
        printf("mp2:   %s\n", str(f, mp2));
        printf("mNorm: %s\n", str(f, mNormal));
        printf("mpp:   %s\n", str(f, mpp));
        printf("md0: %17.15f, md1: %17.15f, md2: %17.15f\n", md0, md1, md2);

    }
    
    static Vector3d getRandomVect(Random rnd){
        return new Vector3d(2*rnd.nextDouble()-1, 2*rnd.nextDouble()-1, 2*rnd.nextDouble()-1 );
    }

    static Vector3d getRandomVect(Random rnd, Vector3d v){
        v.set(2*rnd.nextDouble()-1, 2*rnd.nextDouble()-1, 2*rnd.nextDouble()-1 );
        return v;
    }
    
    /**
     * 
     */
    public static void devTestTorus() throws IOException {
	
        double rin = 4*MM; 
        double rout = 6*MM; 
        
        //ParametricSurfaces.Sphere ps = new ParametricSurfaces.Sphere(rin + rout);
        ParametricSurfaces.Torus ps = new ParametricSurfaces.Torus(rin, rout);

        ParametricSurfaceMaker maker = new ParametricSurfaceMaker(ps, 0.01*MM);

        //TrianglePrinter printer = new TrianglePrinter();
        //STLWriter stl = new STLWriter("/tmp/torus_4mm_6mm_01.stl");
        //stl.close();              

        TriangleMeshSlicer meshSlicer = new TriangleMeshSlicer();
        meshSlicer.setSliceNormal(new Vector3d(0,0,1));
        meshSlicer.setSliceStep(0.1*MM);
        long t0 = time();
        meshSlicer.makeSlices(maker);        

        printf("meshSlicer.makeSlices(maker): %d ms\n", (time() - t0));
        printf("triangles count: %d \n", meshSlicer.getTriCount());
        printf("slices count: %d \n", meshSlicer.getSliceCount());        
        for(int i = 0; i < meshSlicer.getSliceCount(); i++){
            Slice slice = meshSlicer.getSlice(i);
            printf("slice[%4d]: segments: %5d  open contours: %d\n", i, slice.getSegmentCount(), slice.getOpenContourCount()); 
        
        }

    }

    void devTestSlice(){

        int N = 1000003;
        int K = 2;
        printf("devTestSlice({N:%d, K:%d})\n", N,K);
        double r = 1000*MM;
        Slice slice = new Slice(new Vector3d(0,0,1), new Vector3d(0,0,0),1.e-8);

        Vector3d 
            v0 = new Vector3d(),
            v1 = new Vector3d();
        long t0 = time();
        printf("start making contour %d poins\n", N);
        double da = 2*PI/N;
        for(int i = 0; i < N-10; i++){
            int j = (K*i) % N;
            double a0 = j*da;
            double a1 = (j+1)*da;
            
            v0.set(r*cos(a0),r*sin(a0),0);
            v1.set(r*cos(a1),r*sin(a1),0);
            slice.addSegment(v0,v1);
        }
        printf("making contour time: %d ms\n",(time() - t0));
        slice.printStat();
        printf("pointCount:%d\n",slice.getPointCount());

        printf("segmentCount:%d\n",slice.getSegmentCount());
        printf("openContourCount:%d\n",slice.getOpenContourCount());
        slice.getPoints();        
    }


    void devTestContour(){
        int N = 10;

        
        {
            Contour c = new Contour(0,1);
            long t0 = time();
            printf("test append: {N:%d}\n", N);
            for(int i = 0; i < N; i++){
                c.append(i+10);
            }
            printf("append done: %d ms\n", (time()-t0));
            //printf("contour:%s\n", c);
        }
        
        {
            Contour c = new Contour(0,1);
            printf("test prepend: {N:%d}\n", N);
            long t0 = time();
            for(int i = 0; i < N; i++){
                c.prepend(i+10);
            }
            printf("prepend done: %d ms\n", (time()-t0));
            //printf("contour:%s\n", c);
        }
    
        {
            Contour c0 = new Contour();
            c0.append(1);
            c0.prepend(0);            
            Contour c1 = new Contour();
            c1.prepend(10);
            c1.prepend(11);
            c1.prepend(12);
            
            printf("test append(contour) {N:%d}\n", N);
            long t0 = time();
            for(int i = 0; i < N; i++){
                c0.append(c1);
            }
            printf("test append(contour) done: %d ms\n", (time()-t0));
            //printf("contour:%s\n", c0);
        }

        {
            Contour c0 = new Contour();
            c0.append(1);
            c0.prepend(0);            
            Contour c1 = new Contour();
            c1.prepend(10);
            c1.prepend(11);
            
            printf("test append/prepend(contour) {N:%d}\n", N);
            long t0 = time();
            for(int i = 0; i < N; i++){
                c0.prepend(c1);
                c0.append(c1);
                c0.append(30+i);
                c0.prepend(30+i);

            }
            printf("test append/prepend(contour) done: %d ms\n", (time()-t0));
            printf("contour:%s\n", c0);
        }

    }

    /**
     * 
     */
    public static void devTestSphere() throws IOException {
	
        double rad = 10*MM; 
        
        ParametricSurfaces.Sphere sphere = new ParametricSurfaces.Sphere(rad);

        ParametricSurfaceMaker maker = new ParametricSurfaceMaker(sphere, 0.01*MM);
        TriangleMeshSlicer meshSlicer = new TriangleMeshSlicer();
        meshSlicer.makeSlices(maker);
        //STLWriter stl = new STLWriter("/tmp/sphere_10mm_1.stl");
        //maker.getTriangles(stl);
        //stl.close();              

    }

    public static void devTestPointMap() throws IOException {

        printf("devTestPointMap()\n");
        double epsilon  = 1.e-6;
        double delta = epsilon;
        
        int N = 100000;
        int M = 100;
        boolean debug = false;
        printf("epsilon:%10.3e\n",epsilon);  
        printf("N:%d\n",N);        
        printf("M:%d\n",M);        
        
        //PointMap2 map = new PointMap2(N, 0.75, epsilon);
        PointMap map = new PointMap(N, 0.75, epsilon);

        Random rnd = new Random(125);
        int errorCount = 0;
        long t0 = time();
        Vector3d 
            v = new Vector3d(), 
            u = new Vector3d();

        for(int i = 0; i < N; i++){
            getRandomVect(rnd, v);
            int iv = map.add(v.x,v.y,v.z);
            if(debug)printf("(%6.4f,%6.4f,%6.4f):%d\n",v.x, v.y, v.z, iv);
            for(int k = 0; k < M; k++){
                getRandomVect(rnd, u); 
                u.scale(delta);
                u.add(v);
                int iu = map.get(u.x,u.y,u.z);
                if(debug)printf("  -> (%6.4f,%6.4f,%6.4f):%d\n", u.x,u.y,u.z, iu);
                //if(debug)printf(" %2d",iu);
                if(iu != iv)errorCount++;
            }
            if(debug)printf("\n",iv);
        }
        printf("time:%d ms\n",(time() - t0));        
        printf("point count:%d\n",map.getPointCount());        
        printf("errorCount:%d\n",errorCount);        
        double p[] = map.getPoints();
        
    }



    public static void main(String[] arg) throws Exception {

        //new TestTriangleSlicer().devTestRandom();
        //new TestTriangleSlicer().devTestTorus();
        //new TestTriangleSlicer().devTestSphere();
        //new TestTriangleSlicer().devTestSlice();
        //new TestTriangleSlicer().devTestContour();
        for(int i = 0; i < 1; i++)
            new TestTriangleSlicer().devTestPointMap();        
        
    }
}
