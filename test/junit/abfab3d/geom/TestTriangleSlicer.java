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

import java.io.ByteArrayOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

// Internal Imports
import abfab3d.distance.DistanceDataHalfSpace;

import abfab3d.util.PointMap;
import abfab3d.util.PointMap2;
import abfab3d.util.TrianglePrinter;

import abfab3d.io.input.MeshReader;
import abfab3d.io.output.STLWriter;
import abfab3d.io.cli.CLISliceWriter;
import abfab3d.io.cli.SliceLayer;
import abfab3d.io.cli.PolyLine;

import abfab3d.util.PointMap;

import abfab3d.core.TriangleCollector;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;
import static abfab3d.core.MathUtil.str;
import static abfab3d.core.MathUtil.getDistance;
import static abfab3d.geom.TriangleSlicer.getTriangleNormal;
import static abfab3d.geom.TriangleSlicer.getIntersectionDirection;
import static java.lang.Math.*;


/**
 * Tests the functionality of TriangleSlicer
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestTriangleSlicer extends TestCase {


    static final boolean DEBUG = true;

    /**+
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestParametricSurfaceMaker.class);
    }

    public void testNothing(){
        //this test here is to make Test happy. 
    }

    public void devTestTriangleNormal(){

        printf("devTestTriangleNormal()\n");

        int N = 10;
        Random rnd = new Random(151);

        Vector3d normal = new Vector3d();
        for(int i = 0; i < N; i++){
            Vector3d 
                p0 = getRandomVect(rnd),
                p1 = getRandomVect(rnd),
                p2 = getRandomVect(rnd);
            getTriangleNormal(p0, p1, p2, normal);
            p1.sub(p0);
            p2.sub(p0);
            double d1 = p1.dot(normal);
            double d2 = p2.dot(normal);
            String f = "%7.4f";
            printf("p0:%s, p1:%s, p2:%s, n:%s d1:%18.15f, d2:%18.15f\n", str(f,p0),str(f,p1),str(f,p2),str(f,normal),d1, d2);
        }
    }

    public void devTestTriangleSlice(){

        printf("devTestTriangleSlice()\n");
        TriangleSlicer slicer = new TriangleSlicer();
        Vector3d 
            q0 = new Vector3d(),
            q1 = new Vector3d();
        /*
        double vert[][] = new double[][]{
            {0,0,-1,-1},{1,0,-1,-1},{0,1,1,1},
            {0,1,1,1},{0,0,-1,-1},{1,0,-1,-1},
            {1,0,-1,-1},{0,1,1,1},{0,0,-1,-1},
            {0,0,-1,-1},{0,1,1,1},{1,0,-1,-1},
            {1,0,-1,-1},{0,0,-1,-1},{0,1,1,1},
            {0,1,1,1},{1,0,-1,-1},{0,0,-1,-1},
        };
        */

        double vert[][] = new double[][]{
            {0,0,-1,-1},{1,0,-1,-1},{0,0.01,1,1},
            {0,0.01,1,1},{0,0,-1,-1},{1,0,-1,-1},
            {1,0,-1,-1},{0,0.01,1,1},{0,0,-1,-1},

            {0,0,-1,-1},{0,0.01,1,1},{1,0,-1,-1},
            {1,0,-1,-1},{0,0,-1,-1},{0,0.01,1,1},
            {0,0.01,1,1},{1,0,-1,-1},{0,0,-1,-1},

            //{0,0,-1,-1},{1,0,-1,-1},{0,-0.01,1,1},
        };

        Vector3d planeNormal = new Vector3d(0,0,1);
        Vector3d triNormal =  new Vector3d();
        Vector3d direction =  new Vector3d();
        
        for(int i = 0; i < vert.length; i+=3){

            double v0[] = vert[i];
            double v1[] = vert[i+1];
            double v2[] = vert[i+2];
            Vector3d 
                p0 = new Vector3d(v0[0],v0[1],v0[2]),
                p1 = new Vector3d(v1[0],v1[1],v1[2]),
                p2 = new Vector3d(v2[0],v2[1],v2[2]);
            
            slicer.getTriangleNormal(p0, p1, p2, triNormal);
            getIntersectionDirection(planeNormal, triNormal, direction);

            String f = "%7.5f";
            printf("triNormal:%s, direction:%s\n", str(f, triNormal), str(f, direction));

            int res = slicer.getIntersection(p0, p1, p2, v0[3],v1[3],v2[3],q0, q1);
            
            printf("getIntersection\n   p0: %s;%s \n   p1: %s;%s \n   p2: %s;%s\n", str(f, p0),fmt(f,v0[3]),str(f, p1),fmt(f,v1[3]), str(f, p2),fmt(f,v2[3]));
            switch(res){
            case TriangleSlicer.INTERSECT: 
                printf("intersect\n   q0: %s\n   q1: %s\n", str(f, q0),str(f, q1));
                break;
            case TriangleSlicer.INSIDE:                 
                printf("inside\n");                
                break;
            case TriangleSlicer.OUTSIDE: 
                printf("outside\n");                
                break;
            case TriangleSlicer.ERROR: 
                printf("error\n");                
                break;
            }
        }
        
    }

    
    public void devTestRandomTriangles(){

        TriangleSlicer slicer = new TriangleSlicer();
        Vector3d 
            q0 = new Vector3d(),
            q1 = new Vector3d();

        Random rnd = new Random(151);
        int N = 1000000;

        printf("devTestRandomTriangles() N: %d\n", N);
        double maxDist = 0;
        Vector3d 
            mp0 = new Vector3d(), 
            mp1 = new Vector3d(), 
            mp2 = new Vector3d(), 
            mNormal = new Vector3d(), 
            mpp = new Vector3d();
        double md0 = 0., md1 = 0., md2 = 0., minDot = 1.;

        Vector3d direction = new Vector3d();
        Vector3d triNormal = new Vector3d();

        for(int i = 0; i < N; i++){
            Vector3d 
                p0 = getRandomVect(rnd),
                p1 = getRandomVect(rnd),
                p2 = getRandomVect(rnd);
            
            Vector3d planeNormal = getRandomVect(rnd);
            Vector3d planePoint = getRandomVect(rnd);
                        
            DistanceDataHalfSpace plane = new DistanceDataHalfSpace(planeNormal, planePoint);
            double d0 = plane.getDistance(p0.x,p0.y,p0.z);
            double d1 = plane.getDistance(p1.x,p1.y,p1.z);
            double d2 = plane.getDistance(p2.x,p2.y,p2.z);

            getTriangleNormal(p0, p1, p2, triNormal);

            getIntersectionDirection(planeNormal,triNormal, direction);
            direction.normalize();
            int res = slicer.getIntersection(p0, p1, p2, d0, d1, d2, q0, q1);
            
            String format = "%7.5f";
            
            switch(res){
            case TriangleSlicer.INTERSECT: 
                double dq0 = abs(plane.getDistance(q0.x,q0.y,q0.z));
                double dq1 = abs(plane.getDistance(q1.x,q1.y,q1.z));
                Vector3d dir = new Vector3d(q1);
                dir.sub(q0);
                dir.normalize();
                double dot = direction.dot(dir);
                if(dot < minDot) minDot = dot;
                if(abs(dot - 1.) > 1.e-8){
                    printf("***wrong direction***");
                    printf("***wrong direction: q0:%s, q1:%s, predicted:%s actual:%s\n", str(format, q0),str(format, q1), str(format, direction), str(format,dir));                    
                }
                if(dq0 > maxDist || dq1 > maxDist) {
                    maxDist = max(dq0,dq1);
                    mp0 = p0;
                    mp1 = p1;
                    mp2 = p2;
                    mNormal = planeNormal;
                    mpp = planePoint;
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
        printf("minDot: %17.15f\n", minDot);

        slicer.printStat();
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
	
        double rin = 3*MM; 
        double rout = 7*MM; 
        
        //ParametricSurfaces.Sphere ps = new ParametricSurfaces.Sphere(rin + rout);
        ParametricSurfaces.Torus ps = new ParametricSurfaces.Torus(rin, rout);

        ParametricSurfaceMaker maker = new ParametricSurfaceMaker(ps, 1*MM);

        //TrianglePrinter printer = new TrianglePrinter();
        //STLWriter stl = new STLWriter("/tmp/torus_4mm_6mm_01.stl");
        //stl.close();              

        Vector3d normal = new Vector3d(0,0,1);
        Vector3d firstSlice = new Vector3d(0,0,0.0*MM);
        double sliceStep = 1*MM;
        int sliceCount = 10;

        //SlicingParam sp = new SlicingParam(normal, firstSlice, sliceStep, sliceCount);
        SlicingParam sp = new SlicingParam(normal, sliceStep);

        TriangleMeshSlicer meshSlicer = new TriangleMeshSlicer(sp);

        long t0 = time();
        meshSlicer.makeSlices(maker);        

        printf("meshSlicer.makeSlices(maker): %d ms\n", (time() - t0));
        printf("triangles count: %d (empty: %d, inter: %d)\n", meshSlicer.getTriCount(),meshSlicer.getEmptyTriCount(),meshSlicer.getInterTriCount());
        printf("slices count: %d \n", meshSlicer.getSliceCount()); 
        writeCLISlices("/tmp/torusSlices_6.cli", meshSlicer);

        STLWriter stl = new STLWriter("/tmp/torus_3_7_1.stl");
        maker.getTriangles(stl);
        stl.close();              

    }

    public static void devTestSphere() throws IOException {
	
        double R = 10*MM; 
        
        ParametricSurfaces.Sphere ps = new ParametricSurfaces.Sphere(R);
        ParametricSurfaceMaker maker = new ParametricSurfaceMaker(ps, 1*MM);

        Vector3d normal = new Vector3d(0,0,1);
        Vector3d firstSlice = new Vector3d(0,0,-10.000*MM);
        double sliceStep = 1*MM;
        int sliceCount = 21;

        SlicingParam sp = new SlicingParam(normal, firstSlice, sliceStep, sliceCount, 0., 0.);
        //SlicingParam sp = new SlicingParam(normal, sliceStep);

        TriangleMeshSlicer meshSlicer = new TriangleMeshSlicer(sp);

        long t0 = time();
        meshSlicer.makeSlices(maker);        

        printf("meshSlicer.makeSlices(maker): %d ms\n", (time() - t0));
        printf("triangles count: %d (empty: %d, inter: %d)\n", meshSlicer.getTriCount(),meshSlicer.getEmptyTriCount(),meshSlicer.getInterTriCount());
        printf("slices count: %d \n", meshSlicer.getSliceCount());        
        writeCLISlices("/tmp/sphereSlices.cli", meshSlicer);
    }

    /**
     * 
     */
    public static void devTestSphereSTL() throws IOException {
	
        double rad = 10*MM; 
        
        ParametricSurfaces.Sphere sphere = new ParametricSurfaces.Sphere(rad);

        ParametricSurfaceMaker maker = new ParametricSurfaceMaker(sphere, 1*MM);
        //TriangleMeshSlicer meshSlicer = new TriangleMeshSlicer();
        //meshSlicer.makeSlices(maker);
        STLWriter stl = new STLWriter("/tmp/sphere_10mm_1.stl");
        maker.getTriangles(stl);
        stl.close();              

    }

    public static void devTestOctahedron() throws IOException {
	
        double R = 10*MM; 
        
        Octahedron octa = new Octahedron(R);

        Vector3d normal = new Vector3d(0,0,1);
        Vector3d firstSlice = new Vector3d(0,0,-10*MM);
        double sliceStep = 1*MM;
        int sliceCount = 21;

        SlicingParam sp = new SlicingParam(normal, firstSlice, sliceStep, sliceCount, 0., 0.);
        //SlicingParam sp = new SlicingParam(normal, sliceStep);

        TriangleMeshSlicer meshSlicer = new TriangleMeshSlicer(sp);

        long t0 = time();
        meshSlicer.makeSlices(octa);        

        printf("meshSlicer.makeSlices(maker): %d ms\n", (time() - t0));
        printf("triangles count: %d (empty: %d, inter: %d)\n", meshSlicer.getTriCount(),meshSlicer.getEmptyTriCount(),meshSlicer.getInterTriCount());
        printf("slices count: %d \n", meshSlicer.getSliceCount());        
        writeCLISlices("/tmp/slicingTestModels/octaSlices.cli", meshSlicer);

    }


    public static void devTestFilter() throws IOException {
	        
        String filePath = "/tmp/slicingTestModels/8240505_6587068.v2.analytical_mesh_converter.sh.x3db";
        MeshReader reader = new MeshReader(filePath);
        TriangleFilter filter = new TriangleFilter("/tmp/slicingTestModels/8240505_6587068.bad_tri.stl");
        reader.getTriangles(filter);

        filter.close();
    }
    
    public static void devTestFile() throws IOException {
	        
        //String filePath = "test/models/gyrosphere.stl";
        //String slicesPath = "/tmp/gyrosphere_slices.cli";

        String folder = "/tmp/slicingTestModels/";
        //String filePath = "/8240505_6587068.v2.analytical_mesh_converter.sh.x3db";
        //String filePath = "/8240505_6587068.bad_tri.stl";
        //String fileName = "8310663_6799866.v0.x3db";
        //String fileName = "1527142_5307597.v0.x3db";
        String fileName = "1677655_5534876.v0.x3db";  // 10 failed slices at 0.0001mm
        //String fileName = "8288771_6787679.v0.x3db";  // 
        //String fileName = "8196147_5861575.v2.x3db";  // 
        //String fileName = "3665693_5905400.v0.x3db";    // 
        //String fileName = "5757986_5905406.v0.x3db";    // 
        //String fileName = "8871781_7087703.v0.x3db";    // 
        //String fileName = "1272568_4868304.v0.x3db";    // 

        String filePath = folder + fileName;
        String slicesFile = folder + "slices/" + fileName;
        
        Vector3d normal = new Vector3d(0,0,1);
        double sliceStep = 0.1*MM;
        int sliceCount = 1;
        //double sliceOffset =  -12.9*MM; double precision = 0.0001*MM; double sliceShift = 0.0001*MM; boolean auto = true;  // closable contours
        double sliceOffset =  -12.9*MM; double precision = 0.000*MM; double sliceShift = 0.000*MM; boolean auto = true;  // 

        
        Vector3d firstSlice = new Vector3d(0,0,sliceOffset);

        MeshReader reader = new MeshReader(filePath);
        
        SlicingParam sp;
        String slicesPath,openSlicesPath;
        if(auto) {
            sp = new SlicingParam(normal, sliceStep, sliceShift, precision);
            slicesPath = slicesFile+ fmt(",shift.%10.8f,prec.%10.8f.cli",sliceShift/MM, precision/MM);
            openSlicesPath = slicesFile + fmt(",shift.%10.8f,prec.%10.8f_open.cli", sliceShift/MM,precision/MM);
        } else {
            sp = new SlicingParam(normal, firstSlice, sliceStep, sliceCount, sliceShift, precision);
            slicesPath = slicesFile + fmt(",off.%10.8f,shift.%10.8f,prec.%10.8f.cli", firstSlice.z/MM, sliceShift/MM, precision/MM);
            openSlicesPath = slicesFile + fmt(",off.%10.8f,shift.%10.8f,prec.%10.8f_open.cli", firstSlice.z/MM, sliceShift/MM, precision/MM);
        }


        TriangleMeshSlicer meshSlicer = new TriangleMeshSlicer(sp);

        long t0 = time();
        meshSlicer.makeSlices(reader);        

        printf("meshSlicer.makeSlices(maker): %d ms\n", (time() - t0));
        printf("triangles count: %d (empty: %d, inter: %d)\n", meshSlicer.getTriCount(),meshSlicer.getEmptyTriCount(),meshSlicer.getInterTriCount());
        printf("slices count: %d \n", meshSlicer.getSliceCount());        

        writeCLISlices(slicesPath, meshSlicer);
        
        if(meshSlicer.getSuccess()){

            printf("***Slicing success***\n");            
            //meshSlicer.printStat();

        } else {

            printf("***Slicing failure***\n");            
            printf("***TriangleMeshSlicerSTAT***\n");
            meshSlicer.printStat();
            writeCLISlices(openSlicesPath, meshSlicer, true);
            
        }

    }

    public static void devTestManySlices() throws IOException {
	        
        String filePath = "/tmp/slicingTestModels/8240505_6587068.v2.analytical_mesh_converter.sh.x3db";

        Vector3d normal = new Vector3d(0,0,1);
        double sliceStep = 0.1*MM;        
        double sliceOffset = -5.9*MM;
        int sliceCount = 139;
        Vector3d firstSlice = new Vector3d(0,0,sliceOffset);
        //double tolerance = 0.000001*MM;
        //double tolerance = 0.00001*MM; // worst  slice -1.170000 mm length:   2 ends:[ -0.079989, 75.428668], [ -0.079989, 75.430001] dist:  0.0013331 mm
        //double tolerance = 0.0001*MM; // slice:  -1.153000 mm open:1  length:1753 ends:[ 15.276650, 74.746388], [ -0.079989, 75.379003] dist: 15.3696645 mm
        double sliceShift = 0.001*MM; // BAD  slice:   4.902500 mm open:2
        double precision = 0.;
        //                                              length:1771 ends:[ 36.405749, 28.040650], [-34.638515, -2.444935] dist: 77.3088503 mm
        //                                             length:2086 ends:[-34.637457, -2.444400], [ 36.404983, 28.038929] dist: 77.3062842 mm
        MeshReader reader = new MeshReader(filePath);
        double sliceIncrement = 0.0001*MM;
        int incCount = (int)(sliceStep/sliceIncrement);

        long t0 = time();
        printf("start slicing: %d cycles\n", incCount);
        for(int i = 0; i < incCount;i++){

            Vector3d startSlice = new Vector3d(0,0, sliceOffset + i*sliceIncrement);
            SlicingParam sp = new SlicingParam(normal, startSlice, sliceStep, sliceCount, sliceShift, precision);
            TriangleMeshSlicer meshSlicer = new TriangleMeshSlicer(sp);
            meshSlicer.makeSlices(reader);                    
            meshSlicer.printProblems();
        }
        printf("done: %d ms\n", (time() - t0));
        /*
        String slicesPath = filePath + fmt(".tol.%10.8fmm.cli",tolerance/MM);
        String openSlicesPath = filePath + fmt(".tol.%10.8fmm_open.cli",tolerance/MM);


        meshSlicer.makeSlices(reader);        

        printf("meshSlicer.makeSlices(maker): %d ms\n", (time() - t0));
        printf("triangles count: %d (empty: %d, inter: %d)\n", meshSlicer.getTriCount(),meshSlicer.getEmptyTriCount(),meshSlicer.getInterTriCount());
        printf("slices count: %d \n", meshSlicer.getSliceCount());        
        printf("***TriangleMeshSlicerSTAT***\n");
        meshSlicer.printStat();
        writeCLISlices(slicesPath, meshSlicer, false);
        writeCLISlices(openSlicesPath, meshSlicer, true);
        */
    }

    /**
       writeFlags = 1 - closed contours 
       writeFlags = 2 - open contours 
       writeFlags = 3 - both contours        
     */
    static void writeCLISlices(String outPath, Slice slices[], int writeFlags) throws IOException{

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        BufferedOutputStream baos = new BufferedOutputStream(bytes);

        CLISliceWriter writer = new CLISliceWriter(baos,false,MM);

        int dir = 0;
        int id = 1;
                        
        for(int i = 0; i < slices.length; i++){
            
            //if(DEBUG)printf("writing layer: %d\n",i);
            
            Slice slice = slices[i];
            
            double z = slice.getSliceDistance();  
            SliceLayer layer = new SliceLayer(z);            
            if((writeFlags & 1) != 0) {
                // write closed contours 
                int ccount= slice.getClosedContourCount(); 
                for(int k = 0; k < ccount; k++){                    
                    double pnt[];
                    pnt = slice.getClosedContourPoints(k);
                    PolyLine line = new PolyLine(id, dir, pnt);  
                    layer.addPolyLine(line);
                }                
            }
            if((writeFlags & 2) != 0) {
                // write open contours 
                int ccount= slice.getOpenContourCount();
                for(int k = 0; k < ccount; k++){                    
                    double pnt[];
                    pnt = slice.getOpenContourPoints(k);
                    PolyLine line = new PolyLine(id, dir, pnt);  
                    layer.addPolyLine(line);
                }

            }

            writer.addLayer(layer);
            
        }
        
        writer.close();
        
        FileOutputStream fos = new FileOutputStream(outPath);
        baos.close();
        byte[] ba = bytes.toByteArray();
        printf("writing %d bytes into CLI file:%s\n", ba.length, outPath);
        fos.write(ba,0,ba.length);
        fos.close();
        
    }

    static void writeCLISlices(String outPath, TriangleMeshSlicer slicer) throws IOException{

        writeCLISlices(outPath, slicer, false);

    }

    static void writeCLISlices(String outPath, TriangleMeshSlicer slicer, boolean writeOpenContours) throws IOException{
        
        printf("writeCLISlices(%s)\n",outPath);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        BufferedOutputStream baos = new BufferedOutputStream(bytes);

        CLISliceWriter writer = new CLISliceWriter(baos,false,MM);

        int dir = 0;
        int id = 1;
                        
        for(int i = 0; i < slicer.getSliceCount(); i++){
            
            //if(DEBUG)printf("writing layer: %d\n",i);
            
            Slice slice = slicer.getSlice(i);
            
            int ccount;
            if(writeOpenContours) 
                ccount= slice.getOpenContourCount();
            else
                ccount= slice.getClosedContourCount(); 

            double z = slice.getSliceDistance();  
            SliceLayer layer = new SliceLayer(z);            

            for(int k = 0; k < ccount; k++){
                
                double pnt[];
                if(writeOpenContours){ 
                    pnt = slice.getOpenContourPoints(k);
                    //String f = "%20.18f";
                    //printf("open contour: [%s,%s]->[%s,%s]\n", fmt(f, pnt[0]), fmt(f, pnt[1]),fmt(f, pnt[pnt.length-2]),fmt(f, pnt[pnt.length-1]));
                } else {
                    pnt = slice.getClosedContourPoints(k);
                }
                PolyLine line = new PolyLine(id, dir, pnt);  
                layer.addPolyLine(line);
            }

            writer.addLayer(layer);
            
        }
        
        writer.close();
        
        FileOutputStream fos = new FileOutputStream(outPath);
        baos.close();
        byte[] ba = bytes.toByteArray();
        printf("writing %d bytes into CLI file:%s\n", ba.length, outPath);
        fos.write(ba,0,ba.length);
        fos.close();
        
    }

    void devTestSliceV1() throws IOException {

        int N = 103;
        int K = 1;
        printf("devTestSlice({N:%d, K:%d})\n", N,K);
        double r = 10*MM;
        Vector3d normal = new Vector3d(0,0,1);
        double tolerance = 1.e-8;

        Vector3d 
            v0 = new Vector3d(),
            v1 = new Vector3d();
        double da = 2*PI/N;
        int NZ = 10;
        double sliceStep = r/ NZ;
        String outPath = "/tmp/slicingTestModels/testSlice.cli";

        Slice slices[] = new Slice[NZ];

        printf("start making contour %d poins\n", N);
        for(int iz = 0; iz < NZ; iz++){
            double z = truncate(iz * sliceStep, 1.e-16);
            Vector3d pointOnPlane = new Vector3d(0,0,z);
            Slice slice = new SliceV1(normal,pointOnPlane, tolerance);
            slices[iz] = slice;

            long t0 = time();
            for(int i = 0; i < N; i++){
                int j = (i+1) % N;
                double a0 = i*da;
                double a1 = j*da;       
                double rr = Math.sqrt(r*r - z*z);
                v0.set(rr*cos(a0),rr*sin(a0),z);
                v1.set(rr*cos(a1),rr*sin(a1),z);
                slice.addSegment(v0,v1);
            }
            slice.buildContours();
            printf("making contour time: %d ms\n",(time() - t0));
            slice.printStat();
            printf("closedContours:%d openContours:%d \n",slice.getClosedContourCount(), slice.getOpenContourCount());
        }
        
        writeCLISlices(outPath, slices, 3);

    }

    void devTestSliceV2() throws IOException {

        int N = 4;
        int K = 1;
        printf("devTestSlice({N:%d, K:%d})\n", N,K);
        double r = 10*MM;
        Vector3d normal = new Vector3d(0,0,1);
        double tolerance = 1.e-8;

        Vector3d 
            v0 = new Vector3d(),
            v1 = new Vector3d();
        double da = 2*PI/N;
        int NZ = 1;
        double sliceStep = r/ NZ;
        String outPath = "/tmp/slicingTestModels/testSliceV2.cli";

        Slice slices[] = new Slice[NZ];

        printf("start making contour %d poins\n", N);
        for(int iz = 0; iz < NZ; iz++){
            double z = truncate(iz * sliceStep, 1.e-12);
            Vector3d pointOnPlane = new Vector3d(0,0,z);
            Slice slice = new SliceV2(normal,pointOnPlane, tolerance);
            slices[iz] = slice;

            long t0 = time();
            for(int i = 0; i < N-1; i++){
                int j = (i+1) % N;
                double a0 = i*da;
                double a1 = j*da;       
                double rr = Math.sqrt(r*r - z*z);
                v0.set(rr*cos(a0),rr*sin(a0),z);
                v1.set(rr*cos(a1),rr*sin(a1),z);
                slice.addSegment(v0,v1);
            }

            for(int i = 0; i < N; i++){
                int j = (i+1) % N;
                double a0 = -i*da;
                double a1 = -j*da;       
                double rr = Math.sqrt(r*r - z*z);
                v0.set(rr*cos(a0),0.5*rr*sin(a0),z);
                v1.set(rr*cos(a1),0.5*rr*sin(a1),z);
                slice.addSegment(v0,v1);                
            }

            boolean manifold = slice.testManifold();
            if(!manifold) printf("!!! non-manifold slice: %12.10f !!!\n", slice.getPointOnPlane().z);
            slice.printStat();
            slice.buildContours();
            printf("making contour time: %d ms\n",(time() - t0));
            slice.printStat();
            printf("closedContours:%d openContours:%d \n",slice.getClosedContourCount(), slice.getOpenContourCount());
        }
        
        writeCLISlices(outPath, slices, 3);

    }


    /**
       makes contour consisting of array of tangent circles 
     */
    void devTestSliceV2_test2() throws IOException {

        int N = 4; // count of point in single circle 
        printf("devTestSliceV2_test2(%d)\n", N);
        double r = 10*MM;
        Vector3d normal = new Vector3d(0,0,1);
        double tolerance = 1.e-8;

        Vector3d 
            v0 = new Vector3d(),
            v1 = new Vector3d();
        double da = 2*PI/N;
        int NZ = 10;
        double sliceStep = 0.2*MM;
        double scale = 0.9;
        String outPath = "/tmp/slicingTestModels/testSliceV2_test2.cli";

        Slice slices[] = new Slice[NZ];

        printf("start making contour %d poins\n", N);

        for(int iz = 0; iz < NZ; iz++){
            double z = iz * sliceStep;//truncate(iz * sliceStep, 1.e-12);
            printf("sliceZ: %7.3f mm\n", z/MM);
            Vector3d pointOnPlane = new Vector3d(0,0,z);
            Slice slice = new SliceV2(normal,pointOnPlane, tolerance);
            slices[iz] = slice;
            
            //long t0 = time();
            for(int k = 0; k < 4; k++){

                double xc = k*r*2;
                double yc = 0;

                for(int i = 0; i < N; i++){
                    int j = (i+1) % N;
                    double a0 = i*da;
                    double a1 = j*da;       
                    double rr = Math.sqrt(r*r - z*z);
                    v0.set(xc + rr*cos(a0),yc + rr*sin(a0),z);
                    v1.set(xc+ rr*cos(a1),yc + rr*sin(a1),z);
                    slice.addSegment(v0,v1);
                }
            }
            
            boolean manifold = slice.testManifold();
            if(!manifold) printf("!!! non-manifold slice: %12.10f !!!\n", slice.getPointOnPlane().z);
            //slice.printStat();
            slice.buildContours();
            //printf("making contour time: %d ms\n",(time() - t0));
            //slice.printStat();
            printf("   closedContours:%d openContours:%d \n",slice.getClosedContourCount(), slice.getOpenContourCount());
        }
        
        writeCLISlices(outPath, slices, 3);

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

    static class TriangleFilter implements TriangleCollector {

        //Vector3d pnt = new Vector3d(24.604016*MM,-74.274*MM, -0.002*MM);
        Vector3d pnt = new Vector3d(24.604022502899170*MM,-74.274003505706790*MM, -0.002000480890274*MM);

        //[24.604022502899170,-74.274003505706790,-0.002000480890274
        double tolerance  = 0.01*MM;
        STLWriter writer;
        TriangleFilter(String outPath) throws IOException {
            writer = new STLWriter(outPath);
        }
        public boolean addTri(Vector3d p0,Vector3d p1,Vector3d p2){
            String f = "%12.8f";
            if((getDistance(p0, pnt) < tolerance) || 
               (getDistance(p1, pnt) < tolerance) ||
               (getDistance(p2, pnt) < tolerance)) {
                //printf("%s, %s %s\n", str(f, p0),str(f, p1),str(f, p2));
                Vector3d pp0 = new Vector3d(p0);
                Vector3d pp1 = new Vector3d(p1);
                Vector3d pp2 = new Vector3d(p2);
                double ss = 1;
                pp0.sub(pnt); 
                pp0.scale(ss);
                pp1.sub(pnt); 
                pp1.scale(ss);
                pp2.sub(pnt); 
                pp2.scale(ss);

                printf("%s, %s %s\n", str(f, pp0),str(f, pp1),str(f, pp2));
                //writer.addTri(pp0, pp1, pp2);
                writer.addTri(p0, p1, p2);

            }
            return true;
        }
        void close() throws IOException {
            writer.close();
        }
    }

    static double truncate(double v, double eps){
        return eps*Math.round((v)/eps);
    }
    
    public static void main(String[] arg) throws Exception {
        
        //new TestTriangleSlicer().devTestTriangleNormal();
        //new TestTriangleSlicer().devTestRandomTriangles();
        //new TestTriangleSlicer().devTestTriangleSlice();
        //new TestTriangleSlicer().devTestTorus();
        //new TestTriangleSlicer().devTestOctahedron();
        //new TestTriangleSlicer().devTestSliceV1();
        //new TestTriangleSlicer().devTestSliceV2();
        //new TestTriangleSlicer().devTestSliceV2_test2();
        //new TestTriangleSlicer().devTestSphere();
        //new TestTriangleSlicer().devTestSphereSTL();
        new TestTriangleSlicer().devTestFile();
        //new TestTriangleSlicer().devTestManySlices();
        //new TestTriangleSlicer().devTestFilter();
        //new TestTriangleSlicer().devTestSlice();
        //new TestTriangleSlicer().devTestContour();
        //for(int i = 0; i < 1; i++)
        //     new TestTriangleSlicer().devTestPointMap();        
        
    }
}
