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

package abfab3d.grid.op;

import java.util.Random;

import abfab3d.grid.VectorIndexerStructMap;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridShort;
import abfab3d.grid.ArrayAttributeGridInt;

import abfab3d.util.Bounds;
import abfab3d.util.PointMap;
import abfab3d.util.TriangleCollector;

import abfab3d.distance.DistanceData;
import abfab3d.distance.DistanceDataSphere;
import abfab3d.io.output.STLWriter;


import abfab3d.geom.TriangulatedModels;
import abfab3d.geom.PointCloud;
import abfab3d.geom.Octahedron;

import static java.lang.Math.round;
import static java.lang.Math.ceil;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;
import static abfab3d.util.MathUtil.L2S;

/**
 * Test the DistanceToTriangleSet class.
 *
 * @author Vladimir Bulatov
 */
public class TestDistanceToTriangleSet extends TestCase {

    private static final boolean DEBUG = true;

    int subvoxelResolution = 100;


    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDistanceToTriangleSet.class);
    }


    void makeTestOneTriangle()throws Exception{
        
        if(DEBUG) printf("makeTestOneTriangle()\n");
        double xmin = 0*MM, xmax = 10*MM, ymin = 0*MM, ymax = 10*MM, zmin = 0*MM, zmax = 10*MM;
        double vs = 1*MM;
        int subvoxelResolution = 100;

        ArrayAttributeGridInt indexGrid = new ArrayAttributeGridInt(new Bounds(xmin,xmax,ymin,ymax,zmin,zmax), vs, vs);

        Vector3d tris[] = new Vector3d[]{
            //new Vector3d(0*MM, 0*MM, 5*MM), new Vector3d(10*MM,0*MM, 5*MM), new Vector3d(10*MM, 10*MM, 5*MM),
            new Vector3d(1.1*MM, 1*MM, 5.5*MM), new Vector3d(9*MM,1*MM, 6.5*MM),  new Vector3d(9*MM, 9*MM, 7.5*MM),
            //new Vector3d(1*MM, 1*MM, 5.5*MM), new Vector3d(9*MM,1*MM, 5.5*MM),  new Vector3d(9*MM, 9*MM, 5.5*MM),
            //new Vector3d(1*MM, 1*MM, 5.5*MM), new Vector3d(9*MM, 9*MM, 5.5*MM), new Vector3d(1*MM,9*MM, 5.5*MM), 
        };

        DistanceToTriangleSet dts = new DistanceToTriangleSet(indexGrid, subvoxelResolution);
        dts.setMaxDistanceVoxels(1.);

        dts.initialize();

        for(int i = 0; i < tris.length; i+= 3){
            dts.addTri(tris[i],tris[i+1],tris[i+2]);            
        }

        int pcount = dts.getPointCount();

        printf("pcount: %d\n", pcount);
        double pnts[] = new double[pcount*3];
        dts.getPoints(pnts);
        
        STLWriter stl = new STLWriter("/tmp/testDTS.stl");
        
        for(int i = 0; i < tris.length; i+= 3){                        
            stl.addTri(tris[i],tris[i+1],tris[i+2]);

        }
        
        Octahedron octa = new Octahedron(0.2*vs);
        double vs2 = vs/2; // half voxel 
        
        for(int i = 0; i < pnts.length; i+= 3){
            //printf("(%7.2f, %7.2f, %7.2f) mm \n", pnts[i]/MM, pnts[i+1]/MM, pnts[i+2]/MM);            
            octa.setCenter(pnts[i],pnts[i+1],pnts[i+2]);
            octa.getTriangles(stl);            
        }
        
        writeDistanceCenters(indexGrid, stl, vs*0.2);

        stl.close();
    }

    /**
       testing distance to sphere 
     */
    void makeTestSphere()throws Exception{
        
        if(DEBUG) printf("makeTestSphere()\n");
        double xmin = 0*MM, xmax = 10*MM, ymin = 0*MM, ymax = 10*MM, zmin = 0*MM, zmax = 10*MM;
        //double vs = 0.02*MM;
        double vs = 0.02*MM;
        int subvoxelResolution = 50;

        ArrayAttributeGridInt indexGrid = new ArrayAttributeGridInt(new Bounds(xmin,xmax,ymin,ymax,zmin,zmax), vs, vs);
        printf("grid: [%d x %d x %d]\n",indexGrid.getWidth(),indexGrid.getHeight(), indexGrid.getDepth());
        DistanceToTriangleSet dts = new DistanceToTriangleSet(indexGrid, subvoxelResolution);
        dts.setMaxDistanceVoxels(1.1);

        dts.initialize();

        TriangulatedModels.Sphere sphere = new TriangulatedModels.Sphere(3*MM, new Vector3d(5.5*MM,5.5*MM,5.5*MM), 5);
        long t0 = time();
        sphere.getTriangles(dts);
        printf("first layer time: %d ms\n",(time()-t0));

        int pcount = dts.getPointCount();

        printf("pcount: %d\n", pcount);
        t0 = time();

        double pnts[] = new double[pcount*3];
        dts.getPoints(pnts);
        printf("dts.getPoints(pnts) time: %d ms\n",(time()-t0));
        t0 = time();
        PointMap usedPoints = getUsedPoints( indexGrid, pnts, vs/subvoxelResolution);
        double upnts[] = new double[usedPoints.getPointCount()*3];
        usedPoints.getPoints(upnts);
        printf("getUsedPoints time: %d ms\n",(time()-t0));
        printf("usedPointsCount: %d\n", usedPoints.getPointCount());
                
        STLWriter stl = new STLWriter("/tmp/testDTSSphere.stl");
        
        sphere.getTriangles(stl);
        
        if(false){
            Octahedron octa = new Octahedron(0.2*vs);
            double vs2 = vs/2; // half voxel 
            for(int i = 0; i < upnts.length; i+= 3){
                //printf("(%7.2f, %7.2f, %7.2f) mm \n", upnts[i]/MM, upnts[i+1]/MM, upnts[i+2]/MM);            
                octa.setCenter(upnts[i],upnts[i+1],upnts[i+2]);
                octa.getTriangles(stl);            
            }
        }
        
        if(false)writeDistanceCenters(indexGrid, stl, vs*0.2);

        stl.close();
    }

    
    void makeTestPointMap(){

        PointMap points = new PointMap(10, 0.75, 1.e-1);
        int n = 10000000;
        Random rnd = new Random(101);

        long t0 = time();
        for(int i = 0; i < n; i++){

            double x = rnd.nextDouble();
            double y = rnd.nextDouble();
            //double z = rnd.nextDouble();
            double z = (x+y);
            int ind = points.add(x,y,z);
        }
        int diff = n - points.getPointCount();
        printf("n: %d points: %d, diff: %d time: %d ms, \n", n, points.getPointCount(), diff, (time() - t0));
    }

    void makeTestPointMapRehash(){

        PointMap points = new PointMap(10, 0.75, 1.e-1);
        int n = 1000000;
        Random rnd = new Random(101);

        long t0 = time();
        for(int i = 0; i < n; i++){

            double x = rnd.nextDouble();
            double y = rnd.nextDouble();
            double z = rnd.nextDouble();
            int ind = points.add(x,y,z);
        }
        int diff = n - points.getPointCount();
        printf("n: %d points: %d, diff: %d time: %d ms, \n", n, points.getPointCount(), diff, (time() - t0));
        
        double coord[] = points.getPoints();
        for(int i = 0; i < coord.length; i+= 3){
            int ind = points.get(coord[i],coord[i+1],coord[i+2]);
            // index of point should be the same as order ? 
            int d = (ind - i/3);
            if(d != 0) printf("ind: %d coord: %d d: %d\n", ind, i/3, d);
        }
        
    }

    static PointMap getUsedPoints(AttributeGrid indexGrid, double coord[], double tolerance){
        
        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        PointMap points = new PointMap(tolerance);
        printf("tolerance: %7.6f\n", tolerance);
        
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    int ind = (int)indexGrid.getAttribute(x,y,z);
                    if(ind != 0) {
                        double 
                            px = coord[ind*3],
                            py = coord[ind*3+1],
                            pz = coord[ind*3+2];

                        int i = points.add(px,py,pz);
                        //printf("usedPoint: %3d (%7.2f, %7.2f, %7.2f)mm: idx: %d\n", ind, px/MM,py/MM,pz/MM, i);
                    }
                }
            }
        }
        return points;
    }
    
    static void writeDistanceCenters(AttributeGrid indexGrid, TriangleCollector tc, double size){

        Octahedron octa = new Octahedron(size);

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        double coord[] = new double[3];

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    int ind = (int)indexGrid.getAttribute(x,y,z);
                    if(ind != 0) {
                        // voxel has point accociated with it 
                        indexGrid.getWorldCoords(x, y, z, coord);
                        //printf("(%5.2f,%5.2f,%5.2f)\n", coord[0]/vs,coord[1]/vs,coord[2]/vs);
                        octa.setCenter(coord[0],coord[1],coord[2]);
                        octa.getTriangles(tc);
                    }                        
                }
            }
        }
    }

    public static void main(String arg[]) throws Exception {

        for(int i = 0; i < 4; i++){
            //new TestDistanceToTriangleSet().makeTestOneTriangle();
            new TestDistanceToTriangleSet().makeTestSphere();
            //new TestDistanceToTriangleSet().makeTestPointMap();
            //new TestDistanceToTriangleSet().makeTestPointMapRehash();
        }        
    }
}
