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

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridShort;

import abfab3d.intersect.DistanceData;
import abfab3d.intersect.DistanceDataSphere;


import abfab3d.geom.PointCloud;

import static java.lang.Math.round;
import static java.lang.Math.ceil;
import static java.lang.Math.abs;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;
import static abfab3d.util.MathUtil.L2S;

/**
 * Test the DistanceToPointSet class.
 *
 * @author Vladimir Bulatov
 */
public class TestDistanceToPointSet extends TestCase {

    private static final boolean DEBUG = true;

    int subvoxelResolution = 100;


    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDistanceToPointSet.class);
    }

    public void testPoints(){

        double vs = 0.005*MM;
        double x0 = -1*MM,y0 = -1*MM, z0 = -1*MM;
        double x1 = 1*MM,y1 = 1*MM, z1 = 1*MM;

        int nx = (int)((x1-x0)/vs), ny = (int)((y1-y0)/vs), nz = (int)((z1-z0)/vs); 
        // recalculate bounds to voxels boundary 
        x1 = x0 + nx*vs;
        y1 = y0 + ny*vs;
        z1 = z0 + nz*vs;
        printf("grid size: [%d x %d x %d]\n",nx, ny, nz);

        PointCloud pnts = new PointCloud(1);

        // center of a voxel near grid center 
        double xc = x0 + ((nx/2) + 0.5)*vs;
        double yc = y0 + ((ny/2) + 0.5)*vs;
        double zc = z0 + ((nz/2) + 0.5)*vs;

        //int iterCount = 1000;
        int iterCount = 500;
        for(int k = 0; k < iterCount; k++){
            pnts.addPoint(xc+2*vs, yc+vs, zc);
            pnts.addPoint(xc-3*vs, yc+vs, zc);
            pnts.addPoint(xc+2*vs, yc-2*vs, zc);
            pnts.addPoint(xc-3*vs, yc-2*vs, zc);
        } 
        if(DEBUG) printf("points count: %d\n", pnts.size());
        
        DistanceToPointSet dps = new DistanceToPointSet(pnts, 0, 30*vs, subvoxelResolution);
        //dps.setAlgorithm(DistanceToPointSet.ALG_EXACT);
        dps.setAlgorithm(DistanceToPointSet.ALG_LAYERED);
        AttributeGrid grid = new ArrayAttributeGridShort(nx, ny, nz, vs, vs);        
        grid.setGridBounds(new double[]{x0,x1, y0, y1, z0, z1});
        long t0 = time();
        dps.execute(grid);
        printf("DistanceToPointSet done %d ms\n", time() - t0);
        if(false){
            // print slices 
            printSlice(grid, nz/2-1);
            printSlice(grid, nz/2);
            printSlice(grid, nz/2+1);
        }
    }

    public void testAnisotropy(){

        printf("testAnisotropy()\n");
        double vs = 0.1*MM;
        double x0 = -2*MM,y0 = -2*MM, z0 = -2*MM;
        double x1 = 2*MM,y1 = 2*MM, z1 = 2*MM;

        int nx = (int)((x1-x0)/vs), ny = (int)((y1-y0)/vs), nz = (int)((z1-z0)/vs); 
        // recalculate bounds to voxels boundary 
        x1 = x0 + nx*vs;
        y1 = y0 + ny*vs;
        z1 = z0 + nz*vs;
        printf("grid size: [%d x %d x %d]\n",nx, ny, nz);


        PointCloud pnts = new PointCloud(1);

        // center of a voxel near grid center 
        double xc = x0 + ((nx/2) + 0.5)*vs;
        double yc = y0 + ((ny/2) + 0.5)*vs;
        double zc = z0 + ((nz/2) + 0.5)*vs;

        int m = 19;

        pnts.addPoint(xc+m*vs, yc-m*vs, zc);
        pnts.addPoint(xc, yc-m*vs, zc);
        pnts.addPoint(xc-m*vs/2, yc, zc);
        //pnts.addPoint(xc+m*vs, yc+m*vs, zc);
         
        if(DEBUG) printf("points count: %d\n", pnts.size());
        
        DistanceToPointSet dps = new DistanceToPointSet(pnts, 0, 50*vs, subvoxelResolution);
        AttributeGrid gride = new ArrayAttributeGridShort(nx, ny, nz, vs, vs);        
        gride.setGridBounds(new double[]{x0,x1, y0, y1, z0, z1});
        long t0 = time();
        dps.setAlgorithm(DistanceToPointSet.ALG_EXACT);
        dps.execute(gride);
        printf("exact DistanceToPointSet done %d ms\n", time() - t0);
        if(true){
            printf("exact\n");
            printSlice(gride, nz/2);
        }

        AttributeGrid gridl = new ArrayAttributeGridShort(nx, ny, nz, vs, vs);   
        gridl.setGridBounds(new double[]{x0,x1, y0, y1, z0, z1});
        t0 = time();

        dps.setAlgorithm(DistanceToPointSet.ALG_LAYERED);
        dps.execute(gridl);
        printf("layered DistanceToPointSet done %d ms\n", time() - t0);
        if(true){
            // print slices 
            //printSlice(grid, nz/2-1);
            printf("layered\n");
            printSlice(gridl, nz/2);
            printf("diff\n");
            printDiff(gride,gridl, nz/2);
        }
    }


    public void testCompare(){

        printf("testCompare()\n");
        double vs = 0.025*MM;
        double x0 = -2*MM,y0 = -2*MM, z0 = -2*MM;
        double x1 = 2*MM,y1 = 2*MM, z1 = 2*MM;

        int nx = (int)((x1-x0)/vs), ny = (int)((y1-y0)/vs), nz = (int)((z1-z0)/vs); 
        // recalculate bounds to voxels boundary 
        x1 = x0 + nx*vs;
        y1 = y0 + ny*vs;
        z1 = z0 + nz*vs;
        printf("grid size: [%d x %d x %d]\n",nx, ny, nz);
        int maxDistVoxels = 30;
        // center of a voxel near grid center 
        double xc = x0 + ((nx/2) + 0.5)*vs;
        double yc = y0 + ((ny/2) + 0.5)*vs;
        double zc = z0 + ((nz/2) + 0.5)*vs;
        int totalError = 0;
        int pntCount = 1000;
        int iterCount = 10;
        Random rnd = new Random(101);
        for( int m = 0; m < iterCount; m++){
            PointCloud pnts = new PointCloud(1);
            printf("try: %d\n",m);
            for(int k = 0; k < pntCount; k++){
                double x = x0 + rnd.nextDouble()*(x1-x0);
                double y = y0 + rnd.nextDouble()*(y1-y0);
                double z = z0 + rnd.nextDouble()*(y1-y0);
                pnts.addPoint(x,y, z);
            }         
            //if(DEBUG) printf("points count: %d\n", pnts.size());
            
            DistanceToPointSet dps = new DistanceToPointSet(pnts, 0, maxDistVoxels*vs, subvoxelResolution);
            AttributeGrid gride = new ArrayAttributeGridShort(nx, ny, nz, vs, vs);        
            gride.setGridBounds(new double[]{x0,x1, y0, y1, z0, z1});
            long t0 = time();
            dps.setAlgorithm(DistanceToPointSet.ALG_EXACT);
            dps.execute(gride);
            printf("DistanceToPointSet EXACT done %d ms\n", time() - t0);
            AttributeGrid gridl = new ArrayAttributeGridShort(nx, ny, nz, vs, vs);   
            gridl.setGridBounds(new double[]{x0,x1, y0, y1, z0, z1});
            t0 = time();
            
            dps.setAlgorithm(DistanceToPointSet.ALG_LAYERED);
            dps.execute(gridl);
            printf("DistanceToPointSet LAYERED done %d ms\n", time() - t0);
            if(true){
                int err = compareGrids(gride, gridl);
                printf("err: %d\n",err);
                totalError += err;
            }
            if(false)
                printSlice(gridl, 18);
        }
        printf("totalError: %d\n", totalError);
    }

    /**
       makes point set distributed on a sphere and test the distance calculations 
     */
    public void testSphere(){
        double vs = 0.1*MM;
        double x0 = -2*MM,y0 = -2*MM, z0 = -2*MM;
        double x1 = 2*MM,y1 = 2*MM, z1 = 2*MM;
        int nx = (int)ceil((x1 - x0)/vs); 
        int ny = (int)ceil((y1 - y0)/vs); 
        int nz = (int)ceil((z1 - z0)/vs); 
        printf("grid: [%d x %d  x %d]\n", nx, ny, nz);
        int maxDistVoxels = 20;

        x1 = x0 + nx*vs;
        y1 = y0 + ny*vs;
        z1 = z0 + nz*vs;


        double cx = 0.0*MM,cy = 0.0*MM, cz= 0.0*MM;        
        double radius = 1.9*MM;
        DistanceData dd = new DistanceDataSphere(radius, cx, cy, cz);
        double dmax = 0;
        PointCloud pnts = makePointCloud(x0, y0, z0, nx, ny, nz, vs, dd);
        
        printf("point count: %d\n", pnts.size());
        Point3d pnt = new Point3d();
        for(int k = 0; k < pnts.size(); k++){

            pnts.getPoint(k, pnt);
            double d = Math.abs(dd.get(pnt.x, pnt.y, pnt.z)/vs);            
            if(d > dmax) {
                dmax = d; 
                //printf("dmax: %7.4f vs pnt: (%7.3f, %7.3f, %7.3f)\n", dmax, pnt.x/vs, pnt.y/vs, pnt.z/vs );
                
            }
        }
        //printf("max error: %7.4f vs\n", dmax);

        DistanceToPointSet dps = new DistanceToPointSet(pnts, 0, maxDistVoxels*vs, subvoxelResolution);
        AttributeGrid gride = new ArrayAttributeGridShort(nx, ny, nz, vs, vs);        
        gride.setGridBounds(new double[]{x0,x1, y0, y1, z0, z1});
        long t0 = time();
        dps.setAlgorithm(DistanceToPointSet.ALG_EXACT);
        dps.execute(gride);
        printf("DistanceToPointSet EXACT done %d ms\n", time() - t0);
        AttributeGrid gridl = new ArrayAttributeGridShort(nx, ny, nz, vs, vs);   
        gridl.setGridBounds(new double[]{x0,x1, y0, y1, z0, z1});
        t0 = time();
        
        dps.setAlgorithm(DistanceToPointSet.ALG_LAYERED);
        dps.execute(gridl);
        printf("DistanceToPointSet LAYERED done %d ms\n", time() - t0);
        if(true){
            long errors[] = getDiffHistogram(gride, gridl);
            printf("err cnt\n");
            for(int k = 0; k < errors.length; k++){
                if(errors[k] != 0)
                    printf("%3d %3d\n",k, errors[k]);
            }
        }        
    }


    static PointCloud makePointCloud(double x0, double y0, double z0, int nx, int ny, int nz, double vs, DistanceData dd){
                
        PointCloud pnts = new PointCloud(2*(nx*ny + ny*nz + nz*nx));
        
        for(int iy = 0; iy < ny; iy++){
            for(int ix = 0; ix < nx; ix++){
                for(int iz = 0; iz < ny; iz++){
                    double x = x0 + ix*vs;
                    double y = y0 + iy*vs;
                    double z = x0 + iz*vs;
                    double d0 = dd.get(x,y,z);
                    double dx = dd.get(x+vs,y,z);
                    double dy = dd.get(x,y+vs,z);
                    double dz = dd.get(x,y,z+vs);
                    if(dx * d0 <= 0) pnts.addPoint(x + vs * root(d0, dx), y,z); 
                    if(dy * d0 <= 0) pnts.addPoint(x, y + vs * root(d0, dy), z); 
                    if(dz * d0 <= 0) pnts.addPoint(x, y, z + vs * root(d0, dz)); 
                }
            }
        }

        return pnts;
    }
    
    /**
       return a solution of linear equation (v1-v0)*x + v0 = 0. 
     */
    static double root(double v0, double v1){
        return (- v0 / (v1 - v0));
    }

    static void printSlice(AttributeGrid grid, int z){
        int 
            nx = grid.getWidth(), 
            ny = grid.getHeight(),
            nz = grid.getDepth();
        printf("grid:[ %d x %d x %d] slice %d\n",nx,ny,nz,z);

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                int d = L2S(grid.getAttribute(x,y,z));
                switch(d){
                case Short.MAX_VALUE: printf("    +"); break;
                case -Short.MAX_VALUE: printf("    -"); break;
                default:printf("%5d", d); break;
                }
            }
            printf("\n");
        }
    }

    static void printDiff(AttributeGrid grid, AttributeGrid grid1, int z){
        int 
            nx = grid.getWidth(), 
            ny = grid.getHeight(),
            nz = grid.getDepth();
        printf("grid:[ %d x %d x %d] slice %d\n",nx,ny,nz,z);

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                int d = L2S(grid.getAttribute(x,y,z));
                int d1 = L2S(grid1.getAttribute(x,y,z));
                if(d == d1){
                    printf("  .  ");
                    continue;
                } else {
                    printf("%5d", (d1-d));
                }
                //switch(d){
                //case Short.MAX_VALUE: printf("    +"); break;
                //case -Short.MAX_VALUE: printf("    -"); break;
                //default:printf("%5d", d); break;
                //}
            }
            printf("\n");
        }
    }

    static long[] getDiffHistogram(AttributeGrid grid, AttributeGrid grid1){
        int 
            nx = grid.getWidth(), 
            ny = grid.getHeight(),
            nz = grid.getDepth();
        int maxDiff = 0;
        int diff = 0;
        long hist[] = new long[100];
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    int d = L2S(grid.getAttribute(x,y,z));
                    int d1 = L2S(grid1.getAttribute(x,y,z));
                    if(d != d1){
                        diff = Math.abs(d - d1);
                        if(diff >= hist.length)
                            hist[hist.length-1]++;
                        else 
                            hist[diff]++;
                        if(diff > maxDiff)
                            maxDiff = diff;
                    } else {
                        hist[0]++;
                    }
                }
            }
        }
        printf("maxDiff: %3d\n", maxDiff);
        return hist;
    }

    static int compareGrids(AttributeGrid grid, AttributeGrid grid1){
        int 
            nx = grid.getWidth(), 
            ny = grid.getHeight(),
            nz = grid.getDepth();
        //printf("compare grids:[ %d x %d x %d]\n",nx,ny,nz);
        int errorCount  = 0;
        
        for(int z = 0; z < nz; z++){
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    int d = L2S(grid.getAttribute(x,y,z));
                    int d1 = L2S(grid1.getAttribute(x,y,z));
                    if(d != d1 ){
                        if(d != Short.MAX_VALUE && d1 != Short.MAX_VALUE){
                            printf("(%3d %3d %3d]: %4d - %4d\n", x,y,z,d,d1);
                            errorCount++;
                        }
                    }
                }
            }
        }

        return errorCount;

    }

    // simulation of using SliceManager by multiple threads 
    void testSliceManager(){
        
        DistanceToPointSet.SliceManager sm = new DistanceToPointSet.SliceManager(201,2);
        DistanceToPointSet.Slice slices[] = new  DistanceToPointSet.Slice[8];
        
        printf("start\n");
        int treadCount = slices.length;        
        for(int k = 0; k < slices.length; k++){
            slices[k] = sm.getNextSlice(slices[k]);
            if(slices[k] == null){
                treadCount--;
                printf("%d finished\n", k);
            } else {
                printf("%d %s\n", k, slices[k]);
            }
        }

        printf("continue\n");

        Random rnd = new Random(101);

        int m = rnd.nextInt(slices.length); 
        // simulate several threads doing processing 
        while(true){
            if(slices[m] == null){
                m = rnd.nextInt(slices.length); 
                // dead thread 
                continue;
            }
            slices[m] = sm.getNextSlice(slices[m]);
            if(slices[m] != null){
                printf("%2d %s\n", m, slices[m]);
            } else {
                printf("%2d finished\n", m);                
                treadCount--;
                if(treadCount <= 0)
                    break;
            }                
            m = rnd.nextInt(slices.length); 
        }
        
        printf("result\n");        
        sm.printSlices();
        
        int count = sm.getUnprocessedCount();
        printf("unprocessed slices count: %d\n", count);

    }

    public static void main(String arg[]){

        //new TestDistanceToPointSet().testPoints();
        //new TestDistanceToPointSet().testAnisotropy();
        //new TestDistanceToPointSet().testCompare();
        //new TestDistanceToPointSet().testSphere();
        new TestDistanceToPointSet().testSliceManager();
        
    }

}
