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
        double x0 = 4.5*MM;
        Vector3d tris[] = new Vector3d[]{
            //new Vector3d(0*MM, 0*MM, 5*MM), new Vector3d(10*MM,0*MM, 5*MM), new Vector3d(10*MM, 10*MM, 5*MM),
            //new Vector3d(1.1*MM, 1*MM, 5.5*MM), new Vector3d(9*MM,1*MM, 6.5*MM),  new Vector3d(9*MM, 9*MM, 7.5*MM),
            new Vector3d(x0, 1*MM, 1*MM), new Vector3d(x0,1*MM, 9*MM),  new Vector3d(x0, 9*MM, 9*MM),
            new Vector3d(x0, 1*MM, 1*MM), new Vector3d(x0,9*MM, 1*MM),  new Vector3d(x0, 9*MM, 9*MM),
            //new Vector3d(1*MM, 1*MM, 5.5*MM), new Vector3d(9*MM,1*MM, 5.5*MM),  new Vector3d(9*MM, 9*MM, 5.5*MM),
            //new Vector3d(1*MM, 1*MM, 5.5*MM), new Vector3d(9*MM, 9*MM, 5.5*MM), new Vector3d(1*MM,9*MM, 5.5*MM), 
        };

        DistanceToTriangleSet dts = new DistanceToTriangleSet(indexGrid, subvoxelResolution);
        dts.setMaxDistanceVoxels(1.9);

        dts.initialize();

        for(int i = 0; i < tris.length; i+= 3){
            dts.addTri(tris[i],tris[i+1],tris[i+2]);            
        }

        int pcount = dts.getPointCount();
        printf("pcount: %d\n", pcount);
        double pntx[] = new double[pcount];
        double pnty[] = new double[pcount];
        double pntz[] = new double[pcount];
        dts.getPointsInGridUnits(pntx, pnty, pntz);
        
        STLWriter stl = new STLWriter("/tmp/testDTS.stl");
        
        for(int i = 0; i < tris.length; i+= 3){                        
            stl.addTri(tris[i],tris[i+1],tris[i+2]);

        }
        
        if(true){
            Octahedron octa = new Octahedron(0.2*vs);
            double vs2 = vs/2; // half voxel 
            for(int i = 0; i < pntx.length; i++){
                //printf("(%7.2f, %7.2f, %7.2f) mm \n", pnts[i]/MM, pnts[i+1]/MM, pnts[i+2]/MM);            
                octa.setCenter(pntx[i]*vs + xmin,pnty[i]*vs + ymin,pntz[i]*vs+zmin);
                octa.getTriangles(stl);            
            }
        }

        if(true) 
            printDistances(pntx,pnty,pntz,indexGrid);

        // distribute distances to the whole grid 
        DistanceToPointSetIndexed.DT3(pntx, pnty, pntz, indexGrid);

        if(false) 
            writeDistanceCenters(indexGrid, stl, vs*0.2);

        if(true)
            printDistances(pntx,pnty,pntz,indexGrid);

        stl.close();

        

    }

    /**
       testing distance to sphere 
     */
    void makeTestSphere()throws Exception{
        
        if(DEBUG) printf("makeTestSphere()\n");
        double xmin = 0*MM, xmax = 10*MM, ymin = 0*MM, ymax = 10*MM, zmin = 0*MM, zmax = 10*MM;
        //double vs = 0.02*MM;
        //double vs = 1*MM;
        double vs = 0.1*MM;
        int subvoxelResolution = 100;
        double firstLayerThickness = 1.7;
        printf("firstLayerThickness: %5.2f\n",firstLayerThickness);
        ArrayAttributeGridInt indexGrid = new ArrayAttributeGridInt(new Bounds(xmin,xmax,ymin,ymax,zmin,zmax), vs, vs);
        printf("grid: [%d x %d x %d]\n",indexGrid.getWidth(),indexGrid.getHeight(), indexGrid.getDepth());
        DistanceToTriangleSet dts = new DistanceToTriangleSet(indexGrid, subvoxelResolution);
        dts.setMaxDistanceVoxels(firstLayerThickness);

        dts.initialize();
        //double cx = 4.75*MM, cy = 4.75*MM, cz = 4.75*MM, radius = 3*MM;
        double cx = 4.5*MM, cy = 4.5*MM, cz = 4.5*MM, radius = 3*MM;
        
        //  8  0.5M triangles 
        //  9 -> 2M triangles, 
        // 10 -> 8M triangles
        TriangulatedModels.Sphere sphere = new TriangulatedModels.Sphere(radius, new Vector3d(cx, cy, cz ), 8); 
        long t0 = time();
        sphere.getTriangles(dts);
        printf("first layer time: %d ms\n",(time()-t0));
        int triCount = dts.getTriCount();
        printf("triCount: %d\n", triCount);        

        int pcount = dts.getPointCount();

        printf("pntCount: %d\n", pcount);

        double pnts[] = new double[pcount*3];
        dts.getPoints(pnts);
        t0 = time();

        double pntx[] = new double[pcount];
        double pnty[] = new double[pcount];
        double pntz[] = new double[pcount];
        dts.getPointsInGridUnits(pntx, pnty, pntz);

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
        
        //printDistances(pntx,pnty,pntz,indexGrid);

        // distribute distances to the whole grid 
        //DistanceToPointSetIndexed.DT3(pntx, pnty, pntz, indexGrid);
        DistanceToPointSetIndexed.DT3_multiPass(pntx, pnty, pntz, indexGrid);
        
        //writeDistanceCenters(indexGrid, stl, vs*0.2);

        //printDistances(pntx,pnty,pntz,indexGrid);

        //compareSphereDistances(pntx,pnty,pntz,indexGrid, cx, cy, cz, radius);
        calculateSphereError(pntx,pnty,pntz,indexGrid, cx, cy, cz, radius);

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

    static void printDistances(double pntx[],double pnty[],double pntz[],AttributeGrid indexGrid){
        double vs = indexGrid.getVoxelSize();
        Bounds bounds = indexGrid.getGridBounds();
        double xmin = bounds.xmin;
        double ymin = bounds.ymin;
        double zmin = bounds.zmin;

        double px[] = new double[pntx.length];
        double py[] = new double[pnty.length];
        double pz[] = new double[pntz.length];

        // convert points into world units 
        for(int i = 0; i < pntx.length; i++){
            px[i] = pntx[i]*vs + xmin;
            py[i] = pnty[i]*vs + ymin;
            pz[i] = pntz[i]*vs + zmin;
        }        
        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        double coord[] = new double[3];

        for(int z = 0; z < nz; z++){
            printf("z: %d\n", z);
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    int ind = (int)indexGrid.getAttribute(x,y,z);
                    if(ind != 0) {
                        // voxel has point accociated with it 
                        indexGrid.getWorldCoords(x, y, z, coord);
                        coord[0] -= px[ind];
                        coord[1] -= py[ind];
                        coord[2] -= pz[ind];
                        double d = sqrt(coord[0]*coord[0] + coord[1]*coord[1] + coord[2]*coord[2]);                        
                        printf("%4d ", (int)(100*d/vs+0.5));
                    }  else {
                        printf("  .  ");
                    }                                
                }
                printf("\n");
            }
            printf("--\n");
        }
       
    }

    static void compareSphereDistances(double pntx[],double pnty[],double pntz[],AttributeGrid indexGrid, double cx, double cy, double cz, double radius){
        printf("compareSphereDistances()\n");
        double vs = indexGrid.getVoxelSize();
        Bounds bounds = indexGrid.getGridBounds();
        double xmin = bounds.xmin;
        double ymin = bounds.ymin;
        double zmin = bounds.zmin;

        double px[] = new double[pntx.length];
        double py[] = new double[pnty.length];
        double pz[] = new double[pntz.length];

        // convert points into world units 
        for(int i = 0; i < pntx.length; i++){
            px[i] = pntx[i]*vs + xmin;
            py[i] = pnty[i]*vs + ymin;
            pz[i] = pntz[i]*vs + zmin;
        }        
        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        double coord[] = new double[3];

        for(int z = 0; z < nz; z++){
            printf("z: %d\n", z);
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    int ind = (int)indexGrid.getAttribute(x,y,z);
                    if(ind != 0) {
                        // voxel has point accociated with it 
                        indexGrid.getWorldCoords(x, y, z, coord);
                        double 
                            dx = coord[0] - px[ind],
                            dy = coord[1] - py[ind],
                            dz = coord[2] - pz[ind];
                        double d = sqrt(dx*dx + dy*dy + dz*dz); 
                        
                        double 
                            dxs = coord[0] - cx,
                            dys = coord[1] - cy,
                            dzs = coord[2] - cz;
                        // exact distance to sphere 
                        double ds = abs(sqrt(dxs*dxs + dys*dys + dzs*dzs) - radius);

                        printf("%4d ", (int)(100*(d - ds)/vs+0.5));
                    }  else {
                        printf("  .  ");
                    }                                
                }
                printf("\n");
            }
            printf("--\n");
        }
       
    }

    static void calculateSphereError(double pntx[],double pnty[],double pntz[],AttributeGrid indexGrid, double cx, double cy, double cz, double radius){
        printf("calculateSphereError()\n");
        double vs = indexGrid.getVoxelSize();
        Bounds bounds = indexGrid.getGridBounds();
        double xmin = bounds.xmin;
        double ymin = bounds.ymin;
        double zmin = bounds.zmin;

        printf("sphereRadius: %7.2f voxels\n", radius/vs);

        double px[] = new double[pntx.length];
        double py[] = new double[pnty.length];
        double pz[] = new double[pntz.length];

        // convert points into world units 
        for(int i = 0; i < pntx.length; i++){
            px[i] = pntx[i]*vs + xmin;
            py[i] = pnty[i]*vs + ymin;
            pz[i] = pntz[i]*vs + zmin;
        }        
        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        double coord[] = new double[3];
        int count = 0;
        double 
            maxDelta = 0,
            maxRelDelta = 0,
            deltaSum = 0,
            maxDist = 0;
        int printCount = 10;

        for(int z = 0; z < nz; z++){
            //printf("z: %d\n", z);
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    int ind = (int)indexGrid.getAttribute(x,y,z);
                    if(ind != 0) {
                        // voxel has point accociated with it 
                        indexGrid.getWorldCoords(x, y, z, coord);
                        double 
                            dx = (coord[0] - px[ind]),
                            dy = (coord[1] - py[ind]),
                            dz = (coord[2] - pz[ind]);
                        double d = sqrt(dx*dx + dy*dy + dz*dz); 
                        
                        double 
                            dxs = (coord[0] - cx),
                            dys = (coord[1] - cy),
                            dzs = (coord[2] - cz);
                        // exact distance to sphere 
                        double ds = abs(sqrt(dxs*dxs + dys*dys + dzs*dzs) - radius);
                        double delta = abs((d - ds));
                        // exact point on sphere 
                        double ns = sqrt(dxs*dxs + dys*dys + dzs*dzs);
                        double 
                            psx = radius*dxs/ns + cx,
                            psy = radius*dys/ns + cy,
                            psz = radius*dzs/ns + cz;
                        if(d > maxDist) maxDist = d;
                        if(delta > maxDelta) maxDelta = delta;
                        deltaSum += delta;
                        if(delta > 0.3*vs && printCount-- > 0) {
                            printf("delta: %4.2f,  dist: %5.2f coord:(%5.2f,%5.2f,%5.2f) pnt[ind]: (%5.2f,%5.2f,%5.2f): spnt:(%5.2f,%5.2f,%5.2f)\n", 
                                   delta/vs, coord[0]/vs, coord[1]/vs, coord[2]/vs, d/vs,px[ind]/vs,py[ind]/vs,pz[ind]/vs,psx/vs,psy/vs,psz/vs);
                        }
                        count++;
                    }                                
                }
            }
            
        }
        printf("sphereRadius: %5.2f voxels\n", radius/vs);
        printf("maxDistance: %5.1f voxels\n", maxDist/vs);
        printf("maxError: %5.2f voxels\n", maxDelta/vs);
        printf("avrgError: %5.2f voxels\n", deltaSum/vs/count);

    }


    public static void main(String arg[]) throws Exception {

        for(int i = 0; i < 1; i++){
            //new TestDistanceToTriangleSet().makeTestDT3();
            //new TestDistanceToTriangleSet().makeTestOneTriangle();
            new TestDistanceToTriangleSet().makeTestSphere();
            //new TestDistanceToTriangleSet().makeTestPointMap();
            //new TestDistanceToTriangleSet().makeTestPointMapRehash();
        }        
    }
}
