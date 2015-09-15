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
import static java.lang.Math.min;
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
    static final double INF = DistanceToPointSetIndexed.INF;

    int subvoxelResolution = 100;
    

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDistanceToTriangleSet.class);
    }

    void testNothing()throws Exception{
        // to make tester happy 
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
        double w = 5*MM;
        double xmin = -w, xmax = w, ymin = -w, ymax = w, zmin = -w, zmax = w;
        //double vs = 0.02*MM;
        //double vs = 1*MM;
        double vs = 0.2*MM;
        double maxErrorVoxels = 0.05;
        int subvoxelResolution = 100;
        double firstLayerThickness = 1.5; // 0.5
        printf("firstLayerThickness: %5.2f\n",firstLayerThickness);
        ArrayAttributeGridInt indexGrid = new ArrayAttributeGridInt(new Bounds(xmin,xmax,ymin,ymax,zmin,zmax), vs, vs);
        printf("grid: [%d x %d x %d]\n",indexGrid.getWidth(),indexGrid.getHeight(), indexGrid.getDepth());
        DistanceToTriangleSet dts = new DistanceToTriangleSet(indexGrid, subvoxelResolution);
        dts.setMaxDistanceVoxels(firstLayerThickness);

        dts.initialize();
        //double cx = 4.75*MM, cy = 4.75*MM, cz = 4.75*MM, radius = 3*MM;
        double cx = 0.5*vs, cy = 0.5*vs, cz = 0.5*vs, radius = 3*MM;
        
        //  8  0.5M triangles 
        //  9 -> 2M triangles, 
        // 10 -> 8M triangles
        TriangulatedModels.Sphere sphere = new TriangulatedModels.Sphere(radius, new Vector3d(cx, cy, cz ), 4); 
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
        int ucount = getUsedPoints( indexGrid, pntx, pnty, pntz);
        printf("getUsedPoints time: %d ms\n",(time()-t0));
        printf("usedPointsCount: %d\n", ucount);
                
        STLWriter stl = new STLWriter("/tmp/testDTSSphere.stl");
        
        sphere.getTriangles(stl);
                
        //printDistances(pntx,pnty,pntz,indexGrid);

        //distribute distances to the whole grid 
        //DistanceToPointSetIndexed.DT3(pntx, pnty, pntz, indexGrid);
        DistanceToPointSetIndexed.DT3_multiPass(pntx, pnty, pntz, indexGrid);

        convertPointsToWorldUnits(indexGrid, pntx, pnty, pntz);

        calculateErrorsHistogram(indexGrid, pntx, pnty, pntz, 100, 0.1);
        if(true)writePoints(indexGrid, pntx,pnty,pntz, vs*0.2, stl);        
        //if(false)writeDistanceCenters(indexGrid, stl, vs*0.2);
        //if(true)writeFrame(indexGrid, 0.5*vs, stl);
        //if(true)writeErrors(indexGrid, pntx, pnty, pntz, maxErrorVoxels,  0.5*vs, stl);

        //writeDistanceCenters(indexGrid, stl, vs*0.2);

        //printDistances(pntx,pnty,pntz,indexGrid);

        compareSphereDistances(indexGrid, pntx,pnty,pntz,cx, cy, cz, radius, 0.1);

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

    static int getUsedPoints(AttributeGrid indexGrid, double px[], double py[], double pz[]){

        int used[] = new int[px.length];

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    int ind = (int)indexGrid.getAttribute(x,y,z);
                    if(ind != 0) {
                        used[ind] = 1;
                    }
                }
            }
        }
        int count = 0;
        for(int i = 0; i < used.length; i++){
            if(used[i] != 0) {
                count++;
            } else {
                px[i] = INF;
                py[i] = INF;
                pz[i] = INF;
            }
        }
        return count;        
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
    

    static void convertPointsToWorldUnits(AttributeGrid indexGrid, double pntx[], double pnty[], double pntz[]){
        
        Bounds bounds = indexGrid.getGridBounds();
        Vector3d pnt = new Vector3d();
        for(int i = 0; i < pntx.length; i++){
            //printf("(%7.2f, %7.2f, %7.2f) mm \n", upnts[i]/MM, upnts[i+1]/MM, upnts[i+2]/MM);       
            if(pntx[i] != INF){
                pnt.set(pntx[i],pnty[i], pntz[i]);
                bounds.toWorldCoord(pnt);
                pntx[i] = pnt.x;
                pnty[i] = pnt.y;
                pntz[i] = pnt.z;
            }
        }                
    }


    static void writePoints(AttributeGrid indexGrid, double pntx[], double pnty[], double pntz[], double pointSize, TriangleCollector tc){

        double coord[] = new double[3];

        Octahedron octa = new Octahedron(pointSize);
        Bounds bounds = indexGrid.getGridBounds();
        Vector3d pnt = new Vector3d();
        for(int i = 0; i < pntx.length; i++){
            //printf("(%7.2f, %7.2f, %7.2f) mm \n", upnts[i]/MM, upnts[i+1]/MM, upnts[i+2]/MM);       
            if(pntx[i] != INF){
                octa.setCenter(pntx[i], pnty[i], pntz[i]);
                octa.getTriangles(tc);            
            }
        }        
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

    static int[] calculateErrorsHistogram(AttributeGrid indexGrid, double pntx[],double pnty[],double pntz[], int binCount, double errorToCount){
       
        printf("calculateErrorsHistogram()\n");
        int errorCounts[] = new int[binCount];
        double binSize = 1./binCount;
        double vs = indexGrid.getVoxelSize();

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();

        double coord[] = new double[3];
        int errorCount=0;
        double maxError = 0;
        double sumError = 0;
        double sumError2 = 0;
        for(int z = 0; z < nz; z++){
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    int ind = (int)indexGrid.getAttribute(x,y,z);
                    if(ind != 0) {
                        // voxel has point accociated with it 
                        indexGrid.getWorldCoords(x, y, z, coord);                                                
                        double 
                            dx = coord[0] - pntx[ind],
                            dy = coord[1] - pnty[ind],
                            dz = coord[2] - pntz[ind];
                        double dist = sqrt(dx*dx + dy*dy + dz*dz); 
                        double minDist = minDistance( coord[0],coord[1], coord[2], pntx, pnty, pntz);
                        double error = abs(dist - minDist)/vs;
                        int bin = (int)(error/binSize);
                        if(bin > binCount-1)bin = binCount-1;
                        errorCounts[bin]++;
                        if(error >= maxError) {
                            maxError = error;
                        }
                        sumError2 += error*error;
                        sumError += error;
                        if(error > errorToCount) errorCount++;
                    }                                
                }
            }
        }

        int voxelCount = nx*ny*nz;
        printf("voxelCount: %d\n", voxelCount); 
        printf("error above %7.2f count: %d\n", errorToCount, errorCount); 
        printf("maxError %7.3f vs\n", maxError); 
        double avrgError = (sumError/voxelCount);
        double avrgError2 = sqrt(sumError2/voxelCount);
        printf("avrg error %7.5f vs\n", avrgError); 
        printf("avrg quad error %7.5f vs\n", avrgError2);             
        
        printf("**error histogram**\n");
        printf("  interval  | count\n");
        for(int i = 0; i < binCount; i++){
            if(errorCounts[i] != 0)
                printf("(%4.2f %4.2f) | %3d\n", binSize*(i), binSize*(i+1), errorCounts[i]);
        }                
        printf("--------end of **error histogram**\n");
        return errorCounts;
    }

    static void writeErrors(AttributeGrid indexGrid, double pntx[],double pnty[],double pntz[],double maxErrorVoxels,  double pointSize, TriangleCollector tc){
        double vs = indexGrid.getVoxelSize();
        Bounds bounds = indexGrid.getGridBounds();
        double xmin = bounds.xmin;
        double ymin = bounds.ymin;
        double zmin = bounds.zmin;

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();

        double coord[] = new double[3];
        Octahedron octa = new Octahedron(pointSize);
        int errorCount=0;

        for(int z = 0; z < nz; z++){
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    int ind = (int)indexGrid.getAttribute(x,y,z);
                    if(ind != 0) {
                        // voxel has point accociated with it 
                        indexGrid.getWorldCoords(x, y, z, coord);                                                
                        double 
                            dx = coord[0] - pntx[ind],
                            dy = coord[1] - pnty[ind],
                            dz = coord[2] - pntz[ind];

                        double dist = sqrt(dx*dx + dy*dy + dz*dz); 
                        double minDist = minDistance( coord[0],coord[1], coord[2], pntx, pnty, pntz);
                        double error = abs(dist - minDist)/vs;
                        if(error >= maxErrorVoxels) {
                            octa.setSize(pointSize*min(1,(error/maxErrorVoxels)));
                            octa.setCenter(coord[0],coord[1],coord[2]);
                            octa.getTriangles(tc);  
                            //printf("[%2d %2d %2d] (%7.2f,%7.2f,%7.2f): %7.2f %7.2f \n", x,y,z, coord[0]/vs,coord[1]/vs,coord[2]/vs,dist/vs, minDist/vs);
                            errorCount++;
                        }
                    }                                
                }
            }
        }
        printf("error count above %4.2f : %d\n", maxErrorVoxels, errorCount); 
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

    static void compareSphereDistances(AttributeGrid indexGrid, double px[],double py[],double pz[],double cx, double cy, double cz, double radius, double errorToCount){
        printf("compareSphereDistances()\n");
        double vs = indexGrid.getVoxelSize();
        Bounds bounds = indexGrid.getGridBounds();

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        double coord[] = new double[3];
        double 
            maxError = 0, 
            sumError = 0,
            sumError2 = 0;
        
        int errorCount = 0;

        for(int z = 0; z < nz; z++){
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
                        double dist = sqrt(dx*dx + dy*dy + dz*dz); 
                        
                        double 
                            dxs = coord[0] - cx,
                            dys = coord[1] - cy,
                            dzs = coord[2] - cz;
                        // exact distance to sphere 
                        double ds = abs(sqrt(dxs*dxs + dys*dys + dzs*dzs) - radius);
                        double error = abs(dist - ds)/vs;
                        if(error >= maxError) {
                            maxError = error;
                        }
                        sumError2 += error*error;
                        sumError += error;
                        if(error > errorToCount) errorCount++;                        
                    }
                }
            }
            
        }

        long voxelCount = (long)nx*ny*nz;

        printf("maxError %7.3f vs\n", maxError); 
        double avrgError = (sumError/voxelCount);
        double avrgError2 = sqrt(sumError2/voxelCount);
        printf("avrg error %7.5f vs\n", avrgError); 
        printf("avrg quad error %7.5f vs\n", avrgError2);       
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

    static double minDistance(double x, double y, double z, double px[],double py[],double pz[]){
        double minDist = 1.e10;
        for(int i = 1; i < px.length; i++){
            double 
                dx = x - px[i],
                dy = y - py[i],
                dz = z - pz[i];
            double dist = sqrt(dx*dx + dy*dy + dz*dz);
            if(dist < minDist) 
                minDist = dist; 
        }
        return minDist;
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
