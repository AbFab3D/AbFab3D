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

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import javax.vecmath.Vector3d;

import abfab3d.core.AttributeGrid;
import abfab3d.grid.op.ClosestPointIndexer;

import abfab3d.core.Bounds;
import abfab3d.core.TriangleCollector;

import abfab3d.io.output.STLWriter;

import abfab3d.geom.Octahedron;
import abfab3d.geom.TriangulatedModels;


import static java.lang.Math.round;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;

/**
 * Test the TriangleMeshSurfaceBuilder class.
 *
 * @author Vladimir Bulatov
 */
public class TestTriangleMeshSurfaceBuilder extends TestCase {

    private static final boolean DEBUG = false;
    static final double INF = ClosestPointIndexer.INF;

    int subvoxelResolution = 100;
    

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTriangleMeshSurfaceBuilder.class);
    }

    public void testNothing()throws Exception{
        // to make tester happy 
    }


    void devTestOneTriangle()throws Exception{
        
        if(DEBUG) printf("makeTestOneTriangle()\n");
        double xmin = 0*MM, xmax = 10*MM, ymin = 0*MM, ymax = 10*MM, zmin = 0*MM, zmax = 10*MM;
        double vs = 1*MM;

        Bounds bounds = new Bounds(xmin,xmax,ymin,ymax,zmin,zmax, vs);

        double x0 = 4.5*MM;
        Vector3d tris[] = new Vector3d[]{
            new Vector3d(0*MM, 0*MM, 5*MM), new Vector3d(10*MM,0*MM, 5*MM), new Vector3d(10*MM, 10*MM, 5*MM),
            new Vector3d(1.1*MM, 1*MM, 5.5*MM), new Vector3d(9*MM,1*MM, 6.5*MM),  new Vector3d(9*MM, 9*MM, 7.5*MM),
            new Vector3d(x0, 1*MM, 1*MM), new Vector3d(x0,1*MM, 9*MM),  new Vector3d(x0, 9*MM, 9*MM),
            new Vector3d(x0, 1*MM, 1*MM), new Vector3d(x0,9*MM, 1*MM),  new Vector3d(x0, 9*MM, 9*MM),
            new Vector3d(1*MM, 1*MM, 5.5*MM), new Vector3d(9*MM,1*MM, 5.5*MM),  new Vector3d(9*MM, 9*MM, 5.5*MM),
            new Vector3d(1*MM, 1*MM, 5.5*MM), new Vector3d(9*MM, 9*MM, 5.5*MM), new Vector3d(1*MM,9*MM, 5.5*MM), 
        };

        TriangleMeshSurfaceBuilder dts = new TriangleMeshSurfaceBuilder(bounds);
        
        dts.initialize();

        for(int i = 0; i < tris.length; i+= 3){
            dts.addTri(tris[i],tris[i+1],tris[i+2]);            
        }

        int pcount = dts.getPointCount();
        printf("pcount: %d\n", pcount);
        double pntx[] = new double[pcount];
        double pnty[] = new double[pcount];
        double pntz[] = new double[pcount];
        dts.getPoints(pntx, pnty, pntz);
        
        STLWriter stl = new STLWriter("/tmp/testTMSB.stl");
        
        for(int i = 0; i < tris.length; i+= 3){                        
            stl.addTri(tris[i],tris[i+1],tris[i+2]);

        }
        
        if(true){
            Octahedron octa = new Octahedron(0.2*vs);
            double vs2 = vs/2; // half voxel 
            for(int i = 0; i < pntx.length; i++){
                //printf("(%7.2f, %7.2f, %7.2f) mm \n", pnts[i]/MM, pnts[i+1]/MM, pnts[i+2]/MM);            
                octa.setCenter(pntx[i],pnty[i],pntz[i]);
                octa.getTriangles(stl);            
            }
        }

        stl.close();
       
    }
    
    /**
       testing surface of a sphere
     */
    
    void devTestSphere()throws Exception{
        
        if(DEBUG) printf("devTestSphere()\n");
        double w = 25*MM;
        double xmin = -w, xmax = w, ymin = -w, ymax = w, zmin = -w, zmax = w;
        double vs = 1*MM;

        Bounds bounds = new Bounds(xmin,xmax,ymin,ymax,zmin,zmax, vs);
        printf("grid: [%d x %d x %d]\n", bounds.getGridWidth(), bounds.getGridHeight(), bounds.getGridDepth());
        TriangleMeshSurfaceBuilder dts = new TriangleMeshSurfaceBuilder(bounds);

        dts.initialize();

        double cx = 0.5*vs, cy = 0.5*vs, cz = 0.5*vs, radius = 20.5*MM;
        
        //  8  0.5M triangles 
        //  9 -> 2M triangles, 
        // 10 -> 8M triangles
        TriangulatedModels.Sphere sphere = new TriangulatedModels.Sphere(radius, new Vector3d(cx, cy, cz ), 6);
        long t0 = time();
        sphere.getTriangles(dts);
        printf("TriangleMeshSurfaceBuilder time: %d ms\n",(time()-t0));
        int triCount = dts.getTriCount();
        printf("triCount: %d\n", triCount);        
        int pcount = dts.getPointCount();

        printf("pntCount: %d\n", pcount);

        double pntx[] = new double[pcount];
        double pnty[] = new double[pcount];
        double pntz[] = new double[pcount];
        dts.getPoints(pntx, pnty, pntz);

        printf("dts.getPoints(pnts) time: %d ms\n",(time()-t0));

        t0 = time();

        STLWriter stl = new STLWriter("/tmp/testDTSSphere.stl");
        
        sphere.getTriangles(stl);

        if(true){
            Octahedron octa = new Octahedron(0.2*vs);
            double vs2 = vs/2; // half voxel 
            for(int i = 0; i < pntx.length; i++){
                //printf("(%7.2f, %7.2f, %7.2f) mm \n", pnts[i]/MM, pnts[i+1]/MM, pnts[i+2]/MM);            
                octa.setCenter(pntx[i],pnty[i],pntz[i]);
                octa.getTriangles(stl);            
            }
        }
                
        stl.close();
    }    


    public void testSpherePrecision()throws Exception{
        
        if(DEBUG) printf("testSpherePrecision()\n");
        double w = 25*MM;
        double xmin = -w, xmax = w, ymin = -w, ymax = w, zmin = -w, zmax = w;
        double vs = 1*MM;

        Bounds bounds = new Bounds(xmin,xmax,ymin,ymax,zmin,zmax, vs);
        printf("grid: [%d x %d x %d]\n", bounds.getGridWidth(), bounds.getGridHeight(), bounds.getGridDepth());
        TriangleMeshSurfaceBuilder dts = new TriangleMeshSurfaceBuilder(bounds);

        dts.initialize();

        double cx = 0.5*vs, cy = 0.5*vs, cz = 0.5*vs, radius = 20*MM;
        
        //  8  0.5M triangles 
        //  9 -> 2M triangles, 
        // 10 -> 8M triangles
        TriangulatedModels.Sphere sphere = new TriangulatedModels.Sphere(radius, new Vector3d(cx, cy, cz ), 5);
        long t0 = time();
        sphere.getTriangles(dts);
        printf("TriangleMeshSurfaceBuilder time: %d ms\n",(time()-t0));
        int triCount = dts.getTriCount();
        printf("triCount: %d\n", triCount);        
        int pcount = dts.getPointCount();

        printf("pntCount: %d\n", pcount);

        double pntx[] = new double[pcount];
        double pnty[] = new double[pcount];
        double pntz[] = new double[pcount];
        dts.getPoints(pntx, pnty, pntz);

        printf("dts.getPoints(pnts) time: %d ms\n",(time()-t0));

        double maxError = 0;
        for(int i = 1; i < pcount; i++){
            double x = pntx[i]-cx;
            double y = pnty[i]-cy;
            double z = pntz[i]-cz;
            double r = sqrt(x*x + y*y + z*z);
            double error = abs(r - radius);
            if(error > maxError){
                maxError = error;
            }
        }
        printf("maxError: %7.4f mm\n", maxError/MM);
        assertTrue("((maxError < 0.02 ) failed)\n", (maxError/MM < 0.02));
    }    


    static void writeFrame(AttributeGrid indexGrid, double thickness, TriangleCollector tc){
        
        Bounds bounds = indexGrid.getGridBounds();
        double t = thickness;
        double 
            xmin = bounds.xmin+t/2,
            xmax = bounds.xmax-t/2,
            ymin = bounds.ymin-t/2,
            ymax = bounds.ymax+t/2,
            zmin = bounds.zmin-t/2,
            zmax = bounds.zmax+t/2;
        double 
            zc = (zmax+zmin)/2,
            sz = (zmax-zmin) + t,
            yc = (ymax+ymin)/2,
            sy = (ymax-ymin) + t,
            xc = (xmax+xmin)/2,
            sx = (xmax-xmin) + t;


        new TriangulatedModels.Box(xmin, ymin, zc, t, t, sz).getTriangles(tc);
        new TriangulatedModels.Box(xmax, ymin, zc, t, t, sz).getTriangles(tc);
        new TriangulatedModels.Box(xmax, ymax, zc, t, t, sz).getTriangles(tc);
        new TriangulatedModels.Box(xmin, ymax, zc, t, t, sz).getTriangles(tc);
        
        new TriangulatedModels.Box(xmin, yc, zmin, t, sy, t).getTriangles(tc);
        new TriangulatedModels.Box(xmax, yc, zmin, t, sy, t).getTriangles(tc);
        new TriangulatedModels.Box(xmin, yc, zmax, t, sy, t).getTriangles(tc);
        new TriangulatedModels.Box(xmax, yc, zmax, t, sy, t).getTriangles(tc);

        new TriangulatedModels.Box(xc, ymin, zmin, sx, t, t).getTriangles(tc);
        new TriangulatedModels.Box(xc, ymax, zmin, sx, t, t).getTriangles(tc);
        new TriangulatedModels.Box(xc, ymin, zmax, sx, t, t).getTriangles(tc);
        new TriangulatedModels.Box(xc, ymax, zmax, sx, t, t).getTriangles(tc);

    }


    public static void main(String arg[]) throws Exception {

        for(int i = 0; i < 1; i++){
            //new TestTriangleMeshSurfaceBuilder().devTestOneTriangle();
            new TestTriangleMeshSurfaceBuilder().devTestSphere();
            //new TestTriangleMeshSurfaceBuilder().testSpherePrecision();
        }
    }
}
