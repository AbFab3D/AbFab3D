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

import java.util.Random;

import abfab3d.io.input.MeshReader;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import javax.vecmath.Vector3d;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.op.ClosestPointIndexer;

import abfab3d.util.Bounds;
import abfab3d.util.PointMap;
import abfab3d.util.TriangleCollector;
import abfab3d.util.TriangleProducerConverter;
import abfab3d.util.Vec;

import abfab3d.io.output.STLWriter;


import static java.lang.Math.round;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;

/**
 * Test the AttributedTriangleMeshSurfaceBuilder class.
 *
 * @author Vladimir Bulatov
 */
public class TestAttributedTriangleMeshSurfaceBuilder extends TestCase {

    private static final boolean DEBUG = true;
    
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestAttributedTriangleMeshSurfaceBuilder.class);
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
            //new Vector3d(0*MM, 0*MM, 5*MM), new Vector3d(10*MM,0*MM, 5*MM), new Vector3d(10*MM, 10*MM, 5*MM),
            new Vector3d(0*MM, 5*MM, 0*MM), new Vector3d(0*MM, 5*MM, 10*MM), new Vector3d(10*MM, 5*MM, 10*MM),
            //new Vector3d(1.1*MM, 1*MM, 5.5*MM), new Vector3d(9*MM,1*MM, 6.5*MM),  new Vector3d(9*MM, 9*MM, 7.5*MM),
            //new Vector3d(x0, 1*MM, 1*MM), new Vector3d(x0,1*MM, 9*MM),  new Vector3d(x0, 9*MM, 9*MM),
            //new Vector3d(x0, 1*MM, 1*MM), new Vector3d(x0,9*MM, 1*MM),  new Vector3d(x0, 9*MM, 9*MM),
            //new Vector3d(1*MM, 1*MM, 5.5*MM), new Vector3d(9*MM,1*MM, 5.5*MM),  new Vector3d(9*MM, 9*MM, 5.5*MM),
            //new Vector3d(1*MM, 1*MM, 5.5*MM), new Vector3d(9*MM, 9*MM, 5.5*MM), new Vector3d(1*MM,9*MM, 5.5*MM), 
        };

        AttributedTriangleMeshSurfaceBuilder dts = new AttributedTriangleMeshSurfaceBuilder(bounds);
        dts.setDataDimension(3);
        dts.initialize();
        
        for(int i = 0; i < tris.length; i+= 3){
            dts.addAttTri(new Vec(tris[i]),new Vec(tris[i+1]),new Vec(tris[i+2]));            
        }

        int pcount = dts.getPointCount();
        printf("pcount: %d\n", pcount);
        double pnt[][] = new double[3][pcount];
        dts.getPoints(pnt);
        
        STLWriter stl = new STLWriter("/tmp/testTri.stl");
        
        for(int i = 0; i < tris.length; i+= 3){                        
            stl.addTri(tris[i],tris[i+1],tris[i+2]);
        }
        
        if(true){
            Octahedron octa = new Octahedron(0.2*vs);
            double vs2 = vs/2; // half voxel 
            for(int i = 0; i < pcount; i++){
                printf("(%7.2f, %7.2f, %7.2f) mm \n", pnt[0][i]/MM, pnt[1][i]/MM, pnt[2][i]/MM);            
                octa.setCenter(pnt[0][i],pnt[1][i],pnt[2][i]);
                octa.getTriangles(stl);            
            }
        }

        stl.close();
       
    }

    void devTestOneColorTriangle()throws Exception{
        
        if(DEBUG) printf("makeTestOneTriangle()\n");
        double xmin = 0*MM, xmax = 10*MM, ymin = 0*MM, ymax = 10*MM, zmin = 0*MM, zmax = 10*MM;
        double vs = 2*MM;

        Bounds bounds = new Bounds(xmin,xmax,ymin,ymax,zmin,zmax, vs);
        int dataDimension = 6;
        double x0 = 4.5*MM;
        Vec tris[] = new Vec[]{
            new Vec(0*MM, 5*MM, 0*MM, 0, 5, 0), new Vec(0*MM, 5*MM, 10*MM, 0, 5, 10), new Vec(10*MM, 5*MM, 10*MM, 10, 10, 10),
        };

        AttributedTriangleMeshSurfaceBuilder dts = new AttributedTriangleMeshSurfaceBuilder(bounds);
        dts.setDataDimension(dataDimension);
        dts.initialize();
        
        for(int i = 0; i < tris.length; i+= 3){
            dts.addAttTri(tris[i],tris[i+1],tris[i+2]);
        }

        int pcount = dts.getPointCount();
        printf("pcount: %d\n", pcount);
        double pnt[][] = new double[dataDimension][pcount];
        dts.getPoints(pnt);
        
        STLWriter stl = new STLWriter("/tmp/testTri.stl");  
        //for(int i = 0; i < tris.length; i+= 3){                        
        //    stl.addTri(tris[i],tris[i+1],tris[i+2]);
        //}
        
        if(true){
            Octahedron octa = new Octahedron(0.2*vs);
            double vs2 = vs/2; // half voxel 
            for(int i = 0; i < pcount; i++){
                printf("coord:(%5.2f,%5.2f,%5.2f)mm att:(%5.2f,%5.2f,%5.2f) \n", 
                       pnt[0][i]/MM, pnt[1][i]/MM, pnt[2][i]/MM, pnt[3][i], pnt[4][i], pnt[5][i]);            
                octa.setCenter(pnt[0][i],pnt[1][i],pnt[2][i]);
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
        AttributedTriangleMeshSurfaceBuilder dts = new AttributedTriangleMeshSurfaceBuilder(bounds);
        dts.setDataDimension(3);
        dts.initialize();

        double cx = 0.5*vs, cy = 0.5*vs, cz = 0.5*vs, radius = 20.5*MM;
        
        //  8  0.5M triangles 
        //  9 -> 2M triangles, 
        // 10 -> 8M triangles
        TriangulatedModels.Sphere sphere = new TriangulatedModels.Sphere(radius, new Vector3d(cx, cy, cz ), 5);
        long t0 = time();
        TriangleProducerConverter tp2 = new TriangleProducerConverter(sphere);
        tp2.getAttTriangles(dts);
        printf("TriangleMeshSurfaceBuilder time: %d ms\n",(time()-t0));
        int triCount = dts.getTriCount();
        printf("triCount: %d\n", triCount);        
        int pcount = dts.getPointCount();

        printf("pntCount: %d\n", pcount);

        double pnt[][] = new double[3][pcount];
        dts.getPoints(pnt);

        printf("dts.getPoints(pnts) time: %d ms\n",(time()-t0));

        t0 = time();

        STLWriter stl = new STLWriter("/tmp/testDTSSphere.stl");
        
        sphere.getTriangles(stl);

        if(true){
            Octahedron octa = new Octahedron(0.2*vs);
            double vs2 = vs/2; // half voxel 
            int debugCount = 100;
            for(int i = 0; i < pcount; i++){
                if(debugCount-- > 0) printf("(%7.2f, %7.2f, %7.2f) mm \n", pnt[0][i]/MM, pnt[1][i]/MM, pnt[2][i]/MM);            
                octa.setCenter(pnt[0][i],pnt[1][i],pnt[2][i]);
                octa.getTriangles(stl);            
            }
        }
                
        stl.close();
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
            new TestAttributedTriangleMeshSurfaceBuilder().devTestOneColorTriangle();
            //new TestAttributedTriangleMeshSurfaceBuilder().devTestOneTriangle();
            //new TestAttributedTriangleMeshSurfaceBuilder().devTestSphere();
            //new TestTriangleMeshSurfaceBuilder().testSpherePrecision();
        }
    }
}
