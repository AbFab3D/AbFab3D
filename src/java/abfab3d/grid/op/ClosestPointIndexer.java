/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
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

import javax.vecmath.Vector3d;

import abfab3d.core.AttributeGrid;
import abfab3d.core.AttributePacker;
import abfab3d.core.GridDataChannel;
import abfab3d.core.GridDataDesc;

import abfab3d.grid.ArrayAttributeGridShort;
import abfab3d.core.Grid2D;

import abfab3d.core.Vec;
import abfab3d.core.DataSource;
import abfab3d.core.Bounds;
import abfab3d.util.PointSet;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.MathUtil.step10;

/**
   methods to find index of closest point for each point on grid 
   points are given as array of coordinates
   result is grid which contains indices of the closest point to the center of voxel
   the algorithm is originated from 
   Felzenszwalb P. Huttenlocher D. (2004) Distance Transforms of Sampled Functions
   in original algorithm the points were located in the centers of voxels
   the result of the original algorithm was exact. 
   Here the algorithm is generalized to arbitrary locations of points 
   
   most direct generalization of original algorithm is in PI3() 

   Unfortunate result of arbitrary points locations is that the algorithm is not exact anymore.
   Some percentage of voxels have errors. The points found for those voxels are located by fraction of voxel further then optimal point. 

   Partial workaround is to use PI3_multiPass which performs several passes of the algorithm in different order. This significantly reduces errors, but increases timing.
   
   The algorithm takes partially initialized grid of closest point indexes. Used point indices start from 1. Index 0 is used to represent non initialized point.    

   Units or measurement is size of voxel.

   @author Vladimir Bulatov
 */
public class ClosestPointIndexer {
    

    static final double EPS = 1.e-5;  // tolerance for parabolas intersection 
    static public final double INF = 1.e10;  // infinity 
    static final double HALF = 0.5;
    static final boolean DEBUG = false;
    static boolean DEBUG1 = false;
    static final boolean DEBUG_TIMING = true;
    static final int sm_iterationNeig[] = Neighborhood.makeBall(1.5); // neighborhood for iterations

    // neighbors to collect points for 1D pass 
    //static final int[] sm_neig2x = new int[]{0,0,0, 0, 1,0, 0,-1,0};

    private static long processed = 0;
    private static long changed = 0;

    /**
       Indexed Coord Distance Transform 
       calculates 1D distance transform on one dimensional grid of voxels 
       closest distance is calculated to the array of sorted points located at arbitrary positions 
       each point has cordinate and value

       @param gridSize grid size
       @param pointCount count of points       
       @param index  array of point indices 
       @param coord  array of point coordinates. Coordinate of point i is coord[index[i]]
       @param value  array of points distance values 
       @param gpindex  output array of closest point indices
       @param v work array of length (pointCount+1) to store indices parabolas of the envelope
       @param w work array of length (pointCount+1) to store coord of intersections between parabolas
     */
    public static int PI1(int gridSize, 
                             int pointCount, 
                             int index[], 
                             double coord[], 
                             double value[], 
                             int gpindex[], 
                             int v[], 
                             double w[]){
        int ecount = 0;
        //if(true) ecount = sortCoordinates(pointCount, index, coord, value);

        //if(DEBUG) printD("coord:", coord, pointCount);
        //if(DEBUG) printD("value: ", value, pointCount);
        int k = 0; // index of current active parabola in the envelope 
        v[0] = 0;  // initial parabola is originaly  lowest in the envelope         
        w[0] = -INF; // boundaries of the first parabola 
        w[1] = INF;
        //if(DEBUG1)printD("w:", w, k+2);
        double s = 0;
        for (int p = 1; p < pointCount; p++) {
            // checking next parabola 
            double x1 = coord[index[p]]; // vertex of next parabola
            //if(DEBUG1)printf("q:%2d x1:%4.1f \n", p, x1);
            while ( k >=0) {
                // vertex of parabola in the envelope 
                double x0 = coord[index[v[k]]];
                //if(DEBUG1)printf("  k:%2d x0:%4.1f \n", k, x0);
                if(abs(x0 - x1) > EPS){ // parabolas have intersection
                    s = (sqr(x1) - sqr(x0) + value[p] - value[v[k]])/(2*(x1-x0));
                    //if(DEBUG1)printf("     s: %7.1f\n", s);
                    if (s > w[k]) {
                        // found place for new parabola in envelope 
                        break;
                    }
                } else { 
                    if(false) {
                        if(x0 != x1 && abs(value[p] - value[v[k]]) > EPS)  
                            printf("no intersection: x0:%7.3f x1:%7.3f v0:%7.3f v1:%7.3f\n", x0, x1, value[p],value[v[k]]);
                        //TODO process the case if parabolas have no intersection
                        // in that case the parabola with smaller value replaces other in the envelope 
                    }
                }
                k--;
            }   
            k++;
            
            v[k] = p;
            w[k] = s;
            w[k + 1] = INF;
            
            if(false) {
                printf("v:");
                printI(v, k+1);
                printf("w:");
                printD(w, k+2);
            }            
        }
        
        k = 0;
        for (int q = 0; q < gridSize; q++) {            
            while (w[k + 1] < (q+HALF)) { // half pixel shift 
                // these parabolas are ignored 
                k++;
            }
            gpindex[q] = index[v[k]];
        }
/*
        int num_changed = (gridSize-k);
        int[] gpnt = new int[num_changed];
        System.arraycopy(gpindex,0,gpnt,0,num_changed);
        printf("Running: pcnt: %d value: %s changed: %d gpnt: %s\n", pointCount, Arrays.toString(value),(gridSize - k), Arrays.toString(gpnt));
*/
        changed++;
        return (gridSize - k);
    }

    
    /**
       Indexed Coord Distance Transform 
       calculates 1D distance transform on one dimensional grid of voxels        
       closest distance is calculated to the array of sorted points located at arbitrary positions 
       each point has cordinate and value 
       only points with distances up to maxDistance are stored 

       @param gridSize grid size
       @param pointCount count of points       
       @param index  array of point indices 
       @param coord  array of point coordinates. Coordinate of point i is coord[index[i]]
       @param value  array of points distance values 
       @param maxDistance - maximal distance value to be stored back if point has distance > maxDistance then index 0 is stored in the corresponding element of gpindex
       @param gpindex  output array of closest point indices
       @param v work array of length (pointCount+1) to store indices parabolas of the envelope
       @param w work array of length (pointCount+1) to store coord of intersections between parabolas
     */
    public static void PI1_bounded(int gridSize, 
                                   int pointCount, 
                                   int index[], 
                                   double coord[], 
                                   double value[], 
                                   double maxDistance, 
                                   int gpindex[], 
                                   int v[], 
                                   double w[]){
        int k = 0; // index of current active parabola in the envelope 
        v[0] = 0;  // initial parabola is originaly  lowest in the envelope         
        w[0] = -INF; // boundaries of the first parabola 
        w[1] = INF;
        double maxDist2 = maxDistance*maxDistance;
        double s = 0;
        for (int p = 1; p < pointCount; p++) {
            // checking next parabola 
            double x1 = coord[index[p]]; // vertex of next parabola
            while ( k >=0) {
                // vertex of parabola in the envelope 
                double x0 = coord[index[v[k]]];
                //if(DEBUG1)printf("  k:%2d x0:%4.1f \n", k, x0);
                if(abs(x0 - x1) > EPS){ // parabolas have intersection
                    s = (sqr(x1) - sqr(x0) + value[p] - value[v[k]])/(2*(x1-x0));
                    if (s > w[k]) {
                        // found place for new parabola in envelope 
                        break;
                    }
                } else { 
                    if(false) {
                        if(x0 != x1 && abs(value[p] - value[v[k]]) > EPS)  
                            printf("no intersection: x0:%7.3f x1:%7.3f v0:%7.3f v1:%7.3f\n", x0, x1, value[p],value[v[k]]);
                        //TODO process the case if parabolas have no intersection
                        // in that case the parabola with smaller value replaces other in the envelope 
                    }
                }
                k--;
            }   
            k++;
            
            v[k] = p;
            w[k] = s;
            w[k + 1] = INF;
            
        }
        
        k = 0;
        for (int q = 0; q < gridSize; q++) { 
            double x = (q+HALF);
            while (w[k + 1] < x) { // half pixel shift 
                // these parabolas are ignored 
                k++;
            }
            // test the distance value 
            int ind = index[v[k]];
            x -= coord[ind]; 
            double dist2 = x*x + value[v[k]];
            if(dist2 < maxDist2){
                // distance is in the range 
                gpindex[q] = ind;
            }else {
                // distance is out of range 
                gpindex[q] = 0;
            }
        }
    }

    /**
     *  calculates indexed distance transform on 2D grid for a set of points 
     *  @param npnt points count 
     *  @param coordx  array of x coordinates. atrray should have one unused coord at the beginning 
     *  @param coordy  array of y coordinates 
     *  @param indexGrid - on input has indices of closest points in thin layer around the surface, 
     *                   - on output has indices of closest point for each grid point 
     */
    public static void PI2(int npnt, double coordx[], double coordy[], Grid2D indexGrid){
        
        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
            
        // maximal dimension 
        int nm = max(nx, ny);
        // work arrays
        int v[] = new int[nm];
        double w[] = new double[nm+1];
        int ipnt[] = new int[nm+1];
        double value1[] = new double[nm];
        int gpnt[] = new int[nm];

        // make 1D x transforms for each y row 
        for(int iy = 0; iy < ny; iy++){
            int pcnt = 0;
            // prepare 1D chain of points 
            for(int ix = 0; ix < nx; ix++){
                int ind = (int)indexGrid.getAttribute(ix, iy);
                if(ind > 0){
                    ipnt[pcnt] = ind;
                    double y = coordy[ind]-(iy+HALF);
                    value1[pcnt] = y*y;
                    pcnt++;
                }
            }
            if(pcnt > 0){ 
                PI1(nx,pcnt, ipnt, coordx, value1, gpnt, v, w);
                // write chain of indices back into 2D grid 
                for(int ix = 0; ix < nx; ix++){
                    indexGrid.setAttribute(ix, iy, gpnt[ix]);
                }            
            }
        }
        //if(true) return;
        // make 1D y transforms for each x column 
        for(int ix = 0; ix < nx; ix++){
            int pcnt = 0;
            // prepare 1D chain of points 
            for(int iy = 0; iy < ny; iy++){
                int ind = (int)indexGrid.getAttribute(ix, iy);
                if(ind > 0){
                    ipnt[pcnt] = ind;
                    double x = coordx[ind]-(ix+HALF);
                    value1[pcnt] = x*x;
                    pcnt++;
                }
            }
            if(pcnt > 0){ 
                PI1(ny, pcnt, ipnt, coordy, value1, gpnt, v, w);
                // write chain of indices back into 2D grid 
                for(int iy = 0; iy < ny; iy++){
                    indexGrid.setAttribute(ix, iy, gpnt[iy]);
                }            
            }
        }
    }


    public static void PI2_sorted(int npnt, double coordx[], double coordy[], Grid2D indexGrid){
        
        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
            
        // maximal dimension 
        int nm = max(nx, ny);
        // work arrays
        int v[] = new int[nm];
        double w[] = new double[nm+1];
        int ipnt[] = new int[nm+1];
        double values[] = new double[nm];
        int gpnt[] = new int[nm];

        // make 1D x transforms for each y row 
        for(int iy = 0; iy < ny; iy++){
            int pcnt = 0;
            double vy = (iy+HALF);
            // prepare 1D chain of points 
            for(int ix = 0; ix < nx; ix++){
                int ind = (int)indexGrid.getAttribute(ix, iy);
                if(ind > 0){
                    // non empty voxel 
                    double dist = sqr(coordy[ind]-vy);
                    pcnt = addPointSorted(coordx, ipnt, values, ind, dist, pcnt);
                }
            }
            if(pcnt > 0){ 
                PI1(nx,pcnt, ipnt, coordx, values, gpnt, v, w);
                // write chain of indices back into 2D grid 
                for(int ix = 0; ix < nx; ix++){
                    int ind = gpnt[ix];
                    if(ind != 0) indexGrid.setAttribute(ix, iy, ind);
                }            
            }
        }
        //if(true) return;
        // make 1D y transforms for each x column 
        for(int ix = 0; ix < nx; ix++){
            int pcnt = 0;
            double vx = ix + HALF;
            // prepare 1D chain of points 
            for(int iy = 0; iy < ny; iy++){
                int ind = (int)indexGrid.getAttribute(ix, iy);
                if(ind > 0){
                    // non empty voxel 
                    double dist = sqr(coordx[ind]-vx);
                    pcnt = addPointSorted(coordy, ipnt, values, ind, dist, pcnt);

                }
            }
            if(pcnt > 0){ 
                PI1(ny, pcnt, ipnt, coordy, values, gpnt, v, w);
                // write chain of indices back into 2D grid 
                for(int iy = 0; iy < ny; iy++){
                    int ind = gpnt[iy];
                    if(ind != 0) indexGrid.setAttribute(ix, iy, ind);
                }            
            }
        }
    }


    /**
     *  calculates Indexed of closes point on 3D grid for a set of points 
     *  @param coordx  array of x coordinates. coordx[0] is unused 
     *  @param coordy  array of y coordinates. coordy[0] is unused  
     *  @param coordz  array of y coordinates. coordz[0] is unused 
     *  @param indexGrid - on input has indices of closest points in thin layer around the surface,
     *                   - on output has indices of closest point for each grid point 
     *                   valid indices start from 1, value 0 means "undefined" 
     */
    public static void PI3(double coordx[], double coordy[], double coordz[], AttributeGrid indexGrid){

        long t0 = time(), t1 = t0;

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();

        // maximal dimension 
        int nm = max(max(nx, ny),nz); 

        // work arrays
        int v[] = new int[nm];
        double w[] = new double[nm+1];
        int ipnt[] = new int[nm+1];
        double value1[] = new double[nm];
        int gpnt[] = new int[nm];
        DT3sweepX(coordx, coordy, coordz, indexGrid, v, w, ipnt, value1, gpnt);
        DT3sweepY(coordx, coordy, coordz, indexGrid, v, w, ipnt, value1, gpnt);        
        DT3sweepZ(coordx, coordy, coordz, indexGrid, v, w, ipnt, value1, gpnt);        
        
    } // PI3()

    /**
       makes trasformation and does correct sorting of 1D arrays
     */
    public static void PI3_sorted(double coordx[], double coordy[], double coordz[], AttributeGrid indexGrid){

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        int neigCount = 1;
        // work array size 
        int nm = max(max(nx, ny),nz)*neigCount; 
        // work arrays
        int v[] = new int[nm];
        double w[] = new double[nm+1];
        int ipnt[] = new int[nm+1];
        double value[] = new double[nm];
        int gpnt[] = new int[nm];
        DT3sweepX_sorted(0, nz, coordx, coordy, coordz, indexGrid, v, w, ipnt, value, gpnt);
        DT3sweepY_sorted(0, nz, coordx, coordy, coordz, indexGrid, v, w, ipnt, value, gpnt);        
        DT3sweepZ_sorted(0, ny, coordx, coordy, coordz, indexGrid, v, w, ipnt, value, gpnt);        

    } // PI3_sorted()


    /**
       find index of closes point for to each voxel in the index grid 
       only stores indices if closes point is close than given maxDistance 
       @param coordx  x-cordinates in grid units
       @param coordy  y-cordinates in grid units
       @param coordz  z-cordinates in grid units
       @param maxDistance maximal distance to compute in grid units (voxels)
       @param indexGrid on input contains indices of closes point in thin layer around points on output contains indices of closes point up to maxDistance
       
     */
    public static void PI3_bounded(double coordx[], double coordy[], double coordz[], double maxDistance, AttributeGrid indexGrid){

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        int neigCount = 1;
        // work array size 
        int nm = max(max(nx, ny),nz)*neigCount; 
        // work arrays
        int v[] = new int[nm];
        double w[] = new double[nm+1];
        int ipnt[] = new int[nm+1];
        double value[] = new double[nm];
        int gpnt[] = new int[nm];
        DT3sweepX_bounded(0, nz, coordx, coordy, coordz, maxDistance, indexGrid, v, w, ipnt, value, gpnt);
        DT3sweepY_bounded(0, nz, coordx, coordy, coordz, maxDistance, indexGrid, v, w, ipnt, value, gpnt);        
        DT3sweepZ_bounded(0, ny, coordx, coordy, coordz, maxDistance, indexGrid, v, w, ipnt, value, gpnt);        

    } // PI3_bounded()

    /**
       point indexer with several passes 
       has smaller errors, but x3 slower 
     */
    public static void PI3_multiPass(double coordx[], double coordy[], double coordz[], AttributeGrid indexGrid, int iterationCount){

        long t0 = time(), t1 = t0;

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();

        // maximal dimension 
        int nm = max(max(nx, ny),nz);//*sm_neig2.length;

        // work arrays
        int v[] = new int[nm];
        double w[] = new double[nm+1];
        int ipnt[] = new int[nm+1];
        double value1[] = new double[nm];
        int gpnt[] = new int[nm];
        AttributeGrid origGrid = (AttributeGrid)indexGrid.clone();        
        AttributeGrid workGrid = (AttributeGrid)origGrid.clone();

        // do 3 series of sweeps in 3 different order 
        // this eliminates most of errors in calculations
        // XYZ 
        DT3sweepX_sorted(0, nz, coordx, coordy, coordz, indexGrid, v, w, ipnt, value1, gpnt);        
        DT3sweepY_sorted(0, nz, coordx, coordy, coordz, indexGrid, v, w, ipnt, value1, gpnt);        
        DT3sweepZ_sorted(0, ny, coordx, coordy, coordz, indexGrid, v, w, ipnt, value1, gpnt);        
        
        // YZX 
        DT3sweepY_sorted(0, nz, coordx, coordy, coordz, workGrid, v, w, ipnt, value1, gpnt);        
        DT3sweepZ_sorted(0, ny, coordx, coordy, coordz, workGrid, v, w, ipnt, value1, gpnt); 
        DT3sweepX_sorted(0, nz, coordx, coordy, coordz, workGrid, v, w, ipnt, value1, gpnt);                
        combineGrids(indexGrid, workGrid, coordx, coordy, coordz);

        // ZXY 
        workGrid.copyData(origGrid);
        DT3sweepZ_sorted(0, ny, coordx, coordy, coordz, workGrid, v, w, ipnt, value1, gpnt);        
        DT3sweepX_sorted(0, nz, coordx, coordy, coordz, workGrid, v, w, ipnt, value1, gpnt); 
        DT3sweepY_sorted(0, nz, coordx, coordy, coordz, workGrid, v, w, ipnt, value1, gpnt);        
        combineGrids(indexGrid, workGrid, coordx, coordy, coordz);
        
        /*
        // other 3 permutations 
        // XZY 
        workGrid.copyData(origGrid);
        DT3sweepX(coordx, coordy, coordz, indexGrid, v, w, ipnt, value1, gpnt);        
        DT3sweepZ(coordx, coordy, coordz, indexGrid, v, w, ipnt, value1, gpnt);        
        DT3sweepY(coordx, coordy, coordz, indexGrid, v, w, ipnt, value1, gpnt);        
        combineGrids(indexGrid, workGrid, coordx, coordy, coordz);

        // YXZ 
        workGrid.copyData(origGrid);
        DT3sweepY(coordx, coordy, coordz, workGrid, v, w, ipnt, value1, gpnt);        
        DT3sweepX(coordx, coordy, coordz, workGrid, v, w, ipnt, value1, gpnt); 
        DT3sweepZ(coordx, coordy, coordz, workGrid, v, w, ipnt, value1, gpnt);                
        combineGrids(indexGrid, workGrid, coordx, coordy, coordz);

        // ZYX 
        workGrid.copyData(origGrid);
        DT3sweepZ(coordx, coordy, coordz, workGrid, v, w, ipnt, value1, gpnt);        
        DT3sweepY(coordx, coordy, coordz, workGrid, v, w, ipnt, value1, gpnt); 
        DT3sweepX(coordx, coordy, coordz, workGrid, v, w, ipnt, value1, gpnt);        
        combineGrids(indexGrid, workGrid, coordx, coordy, coordz);
        */
        for(int i = 0; i < iterationCount; i++){
            workGrid.copyData(indexGrid);
            long cnt = makeIteration(indexGrid, workGrid, coordx, coordy, coordz, sm_iterationNeig);
            if(DEBUG)printf("iteration %2d  cnt: %d\n", i, cnt);
            if(cnt == 0) 
                break;
        }

        printf("processed: %d  changed: %d\n",processed,changed);
    } // PI3_multiPass()


    
    static int DT3sweepX(double coordx[], double coordy[], double coordz[], AttributeGrid indexGrid, 
                         int v[], double w[], int ipnt[], double value[], int gpnt[]){
        return DT3sweepX(0, indexGrid.getDepth(),coordx, coordy, coordz, indexGrid, v, w, ipnt, value, gpnt);
    }
    
    static int DT3sweepX(int zmin, int zmax, double coordx[], double coordy[], double coordz[], AttributeGrid indexGrid, 
                         int v[], double w[], int ipnt[], double value[], int gpnt[]){
        if(false) printf("DT3sweepX(%d, %d)\n", zmin, zmax);
        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        int ecount = 0;
        // make 1D X-transforms 
        //for(int iz = 0; iz < nz; iz++){
        for(int iz = zmin; iz < zmax; iz++){
            double vz = (iz+HALF);
            for(int iy = 0; iy < ny; iy++){
                int pcnt = 0;
                double vy = (iy+HALF);
                // prepare 1D chain of points 
                for(int ix = 0; ix < nx; ix++){
                    int ind = (int)indexGrid.getAttribute(ix, iy, iz);
                    if(ind > 0){
                        ipnt[pcnt] = ind;
                        double y = coordy[ind]-vy;
                        double z = coordz[ind]-vz;
                        value[pcnt] = z*z + y*y;
                        pcnt++;
                    }

                }
                processed++;
                if(pcnt > 0){ 
                    //TODO remove sorting 
                    //sortCoordinates(pcnt, ipnt, coordx);
                    int changed = PI1(nx,pcnt, ipnt, coordx, value, gpnt, v, w);
                    // write chain of indices back into 3D grid 
                    for(int ix = 0; ix < nx; ix++){
                        indexGrid.setAttribute(ix, iy, iz, gpnt[ix]);
                    }            
                }
            }
        }
        if(DEBUG && ecount > 0) printf("sorting errors: %d\n", ecount);
        return ecount;
    }

    // 
    // does sorting of data in each row before passing it to PI1()
    //
    static int DT3sweepX_sorted(int zmin, int zmax, double coordx[], double coordy[], double coordz[], AttributeGrid indexGrid, 
                         int v[], double w[], int ipnt[], double value[], int gpnt[]){
        if(false) printf("DT3sweepX(%d, %d)\n", zmin, zmax);
        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        int ecount = 0;
        // make 1D X-transforms 
        //for(int iz = 0; iz < nz; iz++){
        for(int iz = zmin; iz < zmax; iz++){
            double vz = (iz+HALF);
            for(int iy = 0; iy < ny; iy++){
                int pcnt = 0;
                double vy = (iy+HALF);
                // prepare 1D chain of points 
                for(int ix = 0; ix < nx; ix++){
                    int ind = (int)indexGrid.getAttribute(ix, iy, iz);
                    if(ind > 0){
                        // non empty voxel 
                        double dist = length2(coordy[ind]-vy, coordz[ind]-vz);
                        pcnt = addPointSorted(coordx, ipnt, value, ind, dist, pcnt);
                    }

                }
                if(pcnt > 0){ 
                    PI1(nx,pcnt, ipnt, coordx, value, gpnt, v, w);
                    // write chain of indices back into 3D grid 
                    for(int ix = 0; ix < nx; ix++){                        
                        indexGrid.setAttribute(ix, iy, iz, gpnt[ix]);
                    }            
                }
            }
        }
        if(DEBUG && ecount > 0) printf("sorting errors: %d\n", ecount);
        return ecount;
    }

    /**
     *  does 1D X-sweep for each x-row 
     *  does sorting of 1D row 
     *  only saves points with distance below maxDistance
     *
     */
    static int DT3sweepX_bounded(int zmin, int zmax, double coordx[], double coordy[], double coordz[], double maxDistance, AttributeGrid indexGrid, 
                                 int v[], double w[], int ipnt[], double value[], int gpnt[]){
        if(maxDistance <= 0.) {
            // make full range calculations
            return DT3sweepX_sorted(zmin, zmax, coordx, coordy, coordz, indexGrid, v, w, ipnt, value, gpnt);
        }
        if(false) printf("DT3sweepX(%d, %d)\n", zmin, zmax);
        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        int ecount = 0;
        // make 1D X-transforms 
        //for(int iz = 0; iz < nz; iz++){
        for(int iz = zmin; iz < zmax; iz++){
            double vz = (iz+HALF);
            for(int iy = 0; iy < ny; iy++){
                int pcnt = 0;
                double vy = (iy+HALF);
                // prepare 1D chain of points 
                for(int ix = 0; ix < nx; ix++){
                    int ind = (int)indexGrid.getAttribute(ix, iy, iz);
                    if(ind > 0){
                        // non empty voxel 
                        double dist = length2(coordy[ind]-vy, coordz[ind]-vz);
                        pcnt = addPointSorted(coordx, ipnt, value, ind, dist, pcnt);
                    }
                }
                if(pcnt > 0){ 
                    PI1_bounded(nx,pcnt, ipnt, coordx, value, maxDistance, gpnt, v, w);
                    // write chain of indices back into 3D grid 
                    for(int ix = 0; ix < nx; ix++){                        
                        if(gpnt[ix] != 0) indexGrid.setAttribute(ix, iy, iz, gpnt[ix]);
                    }            
                }
            }
        }
        return ecount;
    }

    static int DT3sweepY(double coordx[], double coordy[], double coordz[], 
                            AttributeGrid indexGrid, int v[], double w[], int ipnt[], double value[], int gpnt[]){

        return DT3sweepY(0, indexGrid.getDepth(), coordx, coordy, coordz, indexGrid, v, w, ipnt, value, gpnt);

    }

    static int DT3sweepY(int zmin, int zmax, double coordx[], double coordy[], double coordz[], 
                            AttributeGrid indexGrid, int v[], double w[], int ipnt[], double value[], int gpnt[]){
        
        if(false) printf("DT3sweepY(%d, %d)\n", zmin, zmax);
        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        int ecount = 0;

        // make 1D Y-transforms 
        for(int iz = zmin; iz < zmax; iz++){
            double vz = (iz+HALF);
            for(int ix = 0; ix < nx; ix++){
                double vx = (ix+HALF);
                int pcnt = 0;
                // prepare 1D chain of points 
                for(int iy = 0; iy < ny; iy++){
                    int ind = (int)indexGrid.getAttribute(ix, iy, iz);
                    if(ind > 0){
                        ipnt[pcnt] = ind;
                        double x = coordx[ind]-vx;
                        double z = coordz[ind]-vz;
                        value[pcnt] = x*x + z*z;
                        pcnt++;
                        
                    }
                }
                if(pcnt > 0){ 
                    // sortCoordinates(pcnt, ipnt, coordy);
                    PI1(ny, pcnt, ipnt, coordy, value, gpnt, v, w);
                    // write chain of indices back into 3D grid 
                    for(int iy = 0; iy < ny; iy++){
                      indexGrid.setAttribute(ix, iy, iz, gpnt[iy]);
                    }            
                }
            }
        }
        if(DEBUG && ecount > 0) printf("sorting errors: %d\n", ecount);
        return ecount;
    }

    static int DT3sweepY_sorted(int zmin, int zmax, double coordx[], double coordy[], double coordz[], 
                                AttributeGrid indexGrid, int v[], double w[], int ipnt[], double value[], int gpnt[]){
        
        if(false) printf("DT3sweepY(%d, %d)\n", zmin, zmax);
        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();

        // make 1D Y-transforms 
        for(int iz = zmin; iz < zmax; iz++){
            double vz = (iz+HALF);
            for(int ix = 0; ix < nx; ix++){
                double vx = (ix+HALF);
                int pcnt = 0;
                // prepare 1D chain of points 
                for(int iy = 0; iy < ny; iy++){
                    int ind = (int)indexGrid.getAttribute(ix, iy, iz);
                    if(ind > 0){
                        // non empty voxel 
                        double dist = length2(coordx[ind]-vx, coordz[ind]-vz);
                        pcnt = addPointSorted(coordy, ipnt, value, ind, dist, pcnt);
                        
                    }
                }
                if(pcnt > 0){ 
                    PI1(ny, pcnt, ipnt, coordy, value, gpnt, v, w);
                    // write chain of indices back into 3D grid 
                    for(int iy = 0; iy < ny; iy++){
                      indexGrid.setAttribute(ix, iy, iz, gpnt[iy]);
                    }            
                }
            }
        }
        return 0;
    }

    /**
     *  does 1D Y-sweep for each x-row 
     *  does sorting of 1D row 
     *  only saves points with distance below maxDistance
     *
     */
    static int DT3sweepY_bounded(int zmin, int zmax, double coordx[], double coordy[], double coordz[], double maxDistance,
                                 AttributeGrid indexGrid, int v[], double w[], int ipnt[], double value[], int gpnt[]){
        if(maxDistance <= 0.) {
            // make full range calculations
            return DT3sweepY_sorted(zmin, zmax, coordx, coordy, coordz, indexGrid, v, w, ipnt, value, gpnt);
        }

        if(false) printf("DT3sweepY(%d, %d)\n", zmin, zmax);
        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();

        // make 1D Y-transforms 
        for(int iz = zmin; iz < zmax; iz++){
            double vz = (iz+HALF);
            for(int ix = 0; ix < nx; ix++){
                double vx = (ix+HALF);
                int pcnt = 0;
                // prepare 1D chain of points 
                for(int iy = 0; iy < ny; iy++){
                    int ind = (int)indexGrid.getAttribute(ix, iy, iz);
                    if(ind > 0){
                        // non empty voxel 
                        double dist = length2(coordx[ind]-vx, coordz[ind]-vz);
                        pcnt = addPointSorted(coordy, ipnt, value, ind, dist, pcnt);
                        
                    }
                }
                if(pcnt > 0){ 
                    PI1_bounded(ny, pcnt, ipnt, coordy, value, maxDistance, gpnt, v, w);
                    // write chain of indices back into 3D grid 
                    for(int iy = 0; iy < ny; iy++){
                        if(gpnt[iy] != 0) indexGrid.setAttribute(ix, iy, iz, gpnt[iy]);
                    }            
                }
            }
        }
        return 0;
    }


    static int DT3sweepZ(double coordx[], double coordy[], double coordz[], 
                         AttributeGrid indexGrid, int v[], double w[], int ipnt[], double value[], int gpnt[]){

        return DT3sweepZ(0, indexGrid.getHeight(), coordx, coordy, coordz, indexGrid, v, w, ipnt, value, gpnt);

    }

    static int DT3sweepZ(int ymin, int ymax, double coordx[], double coordy[], double coordz[], 
                            AttributeGrid indexGrid, int v[], double w[], int ipnt[], double value[], int gpnt[]){

        if(false) printf("DT3sweepZ(%d, %d)\n", ymin, ymax);
        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();

        // make 1D Z-transforms 
        for(int iy = ymin; iy < ymax; iy++){
            double vy = (iy+HALF);
            for(int ix = 0; ix < nx; ix++){
                double vx = (ix+HALF);
                int pcnt = 0;
                // prepare 1D chain of points 
                for(int iz = 0; iz < nz; iz++){
                    int ind = (int)indexGrid.getAttribute(ix, iy, iz);
                    if(ind > 0){
                        ipnt[pcnt] = ind;
                        double x = coordx[ind]-vx;
                        double y = coordy[ind]-vy;
                        value[pcnt] = x*x + y*y;
                        pcnt++;
                    }
                }
                if(pcnt > 0){ 
                    PI1(nz, pcnt, ipnt, coordz, value, gpnt, v, w);
                    // write chain of indices back into 3D grid 
                    for(int iz = 0; iz < nz; iz++){
                        indexGrid.setAttribute(ix, iy, iz, gpnt[iz]);
                    }            
                }
            }
        }
        return 0;
    }

    static int DT3sweepZ_sorted(int ymin, int ymax, double coordx[], double coordy[], double coordz[], 
                                AttributeGrid indexGrid, int v[], double w[], int ipnt[], double value[], int gpnt[]){

        if(false) printf("DT3sweepZ(%d, %d)\n", ymin, ymax);

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        int ecount = 0;

        // make 1D Z-transforms 
        for(int iy = ymin; iy < ymax; iy++){
            double vy = (iy+HALF);
            for(int ix = 0; ix < nx; ix++){
                double vx = (ix+HALF);
                int pcnt = 0;
                // prepare 1D chain of points 
                for(int iz = 0; iz < nz; iz++){
                    int ind = (int)indexGrid.getAttribute(ix, iy, iz);
                    if(ind > 0){
                        // non empty voxel 
                        double dist = length2(coordx[ind]-vx, coordy[ind]-vy);
                        pcnt = addPointSorted(coordz, ipnt, value, ind, dist, pcnt);
                    }
                }
                if(pcnt > 0){ 
                    PI1(nz, pcnt, ipnt, coordz, value, gpnt, v, w);
                    // write chain of indices back into 3D grid 
                    for(int iz = 0; iz < nz; iz++){
                        indexGrid.setAttribute(ix, iy, iz, gpnt[iz]);
                    }            
                }
            }
        }
        return 0;
    }

    static int DT3sweepZ_bounded(int ymin, int ymax, double coordx[], double coordy[], double coordz[], double maxDistance, 
                                 AttributeGrid indexGrid, int v[], double w[], int ipnt[], double value[], int gpnt[]){
        if(maxDistance <= 0.) {
            // make full range calculations
            return DT3sweepZ_sorted(ymin, ymax, coordx, coordy, coordz, indexGrid, v, w, ipnt, value, gpnt);
        }
        if(false) printf("DT3sweepZ(%d, %d)\n", ymin, ymax);

        int nx = indexGrid.getWidth();
        int ny = indexGrid.getHeight();
        int nz = indexGrid.getDepth();
        int ecount = 0;

        // make 1D Z-transforms 
        for(int iy = ymin; iy < ymax; iy++){
            double vy = (iy+HALF);
            for(int ix = 0; ix < nx; ix++){
                double vx = (ix+HALF);
                int pcnt = 0;
                // prepare 1D chain of points 
                for(int iz = 0; iz < nz; iz++){
                    int ind = (int)indexGrid.getAttribute(ix, iy, iz);
                    if(ind > 0){
                        // non empty voxel 
                        double dist = length2(coordx[ind]-vx, coordy[ind]-vy);
                        pcnt = addPointSorted(coordz, ipnt, value, ind, dist, pcnt);
                    }
                }
                if(pcnt > 0){ 
                    PI1_bounded(nz, pcnt, ipnt, coordz, value, maxDistance, gpnt, v, w);
                    // write chain of indices back into 3D grid 
                    for(int iz = 0; iz < nz; iz++){
                        if(gpnt[iz] != 0)indexGrid.setAttribute(ix, iy, iz, gpnt[iz]);
                    }            
                }
            }
        }
        return 0;
    }

    
    /**
       add new point to 1D array of point in sorted order 
       
       points with equal coordx are treated as follows:
       the point with smaller distance replaces point with larger distances 
       
       @param coordx array of all points coordinates 
       @param ipnt  array of indices in sorted array. New point index is inserted into this array 
       @param values array of point values. New point value is inserted into this array 
       @param ind index of point being added 
       @param value value of point being added 
       @param pcnt  count of points stored in the arrays ipnt and values
       @return new count of ponts in he array 
     */
    static final int addPointSorted(double coordx[], int ipnt[], double values[], int ind, double value, int pcnt){

        double x = coordx[ind];
        
        if(pcnt == 0){
            // first point is added (no sorting needed) 
            ipnt[0] = ind;
            values[0] = value;                            
            pcnt = 1;
        } else {
            // array is non empty 
            int k = pcnt; // place to add new point 
            // find place to insert new point. the x-coord should be in accenting order 
            while(k > 0 && x < coordx[ipnt[k-1]] - EPS ){
                k--;
            }
            
            if(k > 0 && abs(coordx[ipnt[k-1]] - x) < EPS){
                // x-coord are equal 
                if(value < values[k-1]) {
                    // new point is better, it replaces old point at index k 
                    ipnt[k-1] = ind;
                    values[k-1] = value;
                }
            } else {
                // shift old points and insert new point at index k 
                for(int i = pcnt; i > k; i--){
                    ipnt[i] = ipnt[i-1];
                    values[i] = values[i-1]; 
                }
                ipnt[k] = ind;
                values[k] = value;                 
                pcnt++;
            } 
        }                        
        return pcnt;
    }

    /**
       sort coordinates and values in accending order 
       return permutations count 
       Note. sorting is disabled for now 
     */
    public static final int sortCoordinates(int count, int index[], double coord[]){
        
        if(true) return 0;
        int ecount = 0;
        for(int i = 1; i < count; i++){
            if(coord[index[i]] < coord[index[i-1]] + EPS) {
                //printf("%7.5f %7.5f\n ", coord[index[i]], coord[index[i-1]]);
                ecount++;
                if(false){
                    //printArray("bad order", count, index, coord);
                    //int t = index[i-1];
                    //index[i-1] = index[i];
                    //index[i] = t;
                    //double tv = value[i-1];
                    //value[i-1] = value[i];
                    //value[i] = tv;
                    //printArray("corrected:",count, index, coord);
                }
            }
        }
        if(ecount > 0) printf("sorting errors: %d\n", ecount);
        return ecount;
    }

    
    /**
       convert single array of points into 3 arrays in grid units 
     */
    public static void getPointsInGridUnits(AttributeGrid grid, double pnts[], double pntx[], double pnty[], double pntz[]){

        Bounds bounds = grid.getGridBounds();
        double xmin = bounds.xmin;
        double ymin = bounds.ymin;
        double zmin = bounds.zmin;

        double vs = grid.getVoxelSize();
        int count = pnts.length/3;

        printf("Count of points: %d\n",pnts.length);
        // first point is not used 
        for(int i = 1; i < count; i++){
            pntx[i] = (pnts[3*i  ] - xmin)/vs;
            pnty[i] = (pnts[3*i+1] - ymin)/vs;
            pntz[i] = (pnts[3*i+2] - zmin)/vs;

        }
    }

    /**
       convert 3 arrays of points into grid units 
     */
    public static void getPointsInGridUnits(AttributeGrid grid, double pntx[], double pnty[], double pntz[]){

        Bounds bounds = grid.getGridBounds();
        double xmin = bounds.xmin;
        double ymin = bounds.ymin;
        double zmin = bounds.zmin;

        double vs = grid.getVoxelSize();
        int count = pntx.length;
        double scale = 1./vs;
        // first point is not used 
        for(int i = 1; i < count; i++){
            pntx[i] = (pntx[i] - xmin)*scale;
            pnty[i] = (pnty[i] - ymin)*scale;
            pntz[i] = (pntz[i] - zmin)*scale;

        }
    }


    /**
       convert points in grid coordinates into world coordinates
     */
    public static void getPointsInWorldUnits(AttributeGrid grid, double pntx[], double pnty[], double pntz[]){

        Bounds bounds = grid.getGridBounds();
        double xmin = bounds.xmin;
        double ymin = bounds.ymin;
        double zmin = bounds.zmin;

        double vs = grid.getVoxelSize();
        int count = pntx.length;

        // first point is not used 
        for(int i = 1; i < count; i++){
            pntx[i] = pntx[i]*vs + xmin;
            pnty[i] = pnty[i]*vs + ymin;
            pntz[i] = pntz[i]*vs + zmin;
        }
    }

    /**
       convert points in grid coordinates into world coordinates
     */
    public static void getPointsInWorldUnits(Grid2D grid, double pntx[], double pnty[]){

        Bounds bounds = grid.getGridBounds();
        double xmin = bounds.xmin;
        double ymin = bounds.ymin;

        double vs = grid.getVoxelSize();
        int count = pntx.length;

        // first point is not used 
        for(int i = 1; i < count; i++){
            pntx[i] = pntx[i]*vs + xmin;
            pnty[i] = pnty[i]*vs + ymin;
        }
    }

    public static void snapToVoxels(double pnts[]){

        for(int i = 0; i < pnts.length; i++){
            pnts[i] = ((int)pnts[i]+HALF);
        }
    }

    public static long makeIteration(AttributeGrid indexGrid, AttributeGrid workGrid, double pntx[], double pnty[], double pntz[], int neig[]){
        int ncount = neig.length;

        int 
            nx = indexGrid.getWidth(),
            ny = indexGrid.getHeight(),
            nz = indexGrid.getDepth();
        long cnt = 0;

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    int ind = (int)indexGrid.getAttribute(x,y,z);
                    int bestIndex = ind;
                    double bestDist = distance2(x,y,z,pntx,pnty,pntz, ind);                    
                    for(int k = 0; k < ncount; k+=3){
                        int 
                            vx = x + neig[k],
                            vy = y + neig[k+1],
                            vz = z + neig[k+2];                    
                        if( vx >= 0 && vy >= 0 & vz >= 0 && vx < nx && vy < ny && vz < nz){
                            int indn = (int)workGrid.getAttribute(vx,vy,vz);
                            double distn = distance2(x,y,z,pntx,pnty,pntz, indn);                    
                            if(distn < bestDist) {
                                bestDist = distn;
                                bestIndex = indn;
                            }
                        }
                    }
                    if(bestIndex != ind) {
                        cnt++;
                        indexGrid.setAttribute(x,y,z, bestIndex);
                    }                        
                }
            }
        }
        return cnt;
    }

    /**
       
       initializes thin layer of voxle around points
       
       @param indexGrid on output contains indices of closes point in thin layer around points 
       @param pntx x-cordiates of points in grid units , pntx[0] is unused
       @param pnty y-cordiates of points in grid units , pnty[0] is unused
       @param pntz z-cordiates of points in grid units , pntz[0] is unused
       @param layerThickness thicknes of thin layer in voxels units 
     */
    public static void initFirstLayer(AttributeGrid indexGrid, double pntx[], double pnty[], double pntz[], double layerThickness){

        int neig[] = Neighborhood.makeBall(layerThickness+1);
        //printf("neig.length: %d\n", neig.length);
        int subvoxelResolution = 100;
        double tol = 1./subvoxelResolution;  // tolerance to compare distances 
        int 
            nx = indexGrid.getWidth(),
            ny = indexGrid.getHeight(),
            nz = indexGrid.getDepth();

        layerThickness += tol;

        int pcnt = pntx.length;
        Bounds bounds = indexGrid.getGridBounds();
        double vs = indexGrid.getVoxelSize();
        // temp storage for distance values  
        ArrayAttributeGridShort distanceGrid = new ArrayAttributeGridShort(bounds, vs, vs);
        distanceGrid.fill((int)(subvoxelResolution*(layerThickness+0.5)));

        for(int index = 1; index < pcnt; index++){
            //if(true)printf("index: %d\n", index);
            
            double 
                x = pntx[index],
                y = pnty[index],
                z = pntz[index];
            int 
                ix = (int)x,
                iy = (int)y,
                iz = (int)z;
            //if(true)printf("%d (%2d %2d %2d)\n", index, ix, iy, iz);

           int ncount = neig.length;
            
            for(int k = 0; k < ncount; k+=3){
                int 
                    vx = ix + neig[k],
                    vy = iy + neig[k+1],
                    vz = iz + neig[k+2];                    
                if( vx >= 0 && vy >= 0 & vz >= 0 && vx < nx && vy < ny && vz < nz){
                    double 
                        dx = x - (vx+HALF),
                        dy = y - (vy+HALF),
                        dz = z - (vz+HALF);
                    double dist = sqrt(dx*dx + dy*dy + dz*dz);
                    //double dist = max(dx, max(dy,dz));
                    //printf("[%2d,%2d,%2d]: dist: %5.2f \n", neig[k],neig[k+1],neig[k+2],dist);
                    if(dist <= layerThickness) {
                        int newdist = iround(dist*subvoxelResolution);
                        int olddist = (int)distanceGrid.getAttribute(vx, vy, vz);
                        if(newdist < olddist){
                            // better point found
                            distanceGrid.setAttribute(vx, vy, vz, newdist);
                            indexGrid.setAttribute(vx, vy, vz, index);
                        }
                    }
                }
            }                    
        }
    }

    /**
       compares distances to points stored in two grids and select shortest distance and stores result in first grid
     */
    static void combineGrids(AttributeGrid grid1, AttributeGrid grid2, double pntx[], double pnty[], double pntz[]){

        int 
            nx = grid1.getWidth(),
            ny = grid1.getHeight(),
            nz = grid1.getDepth();

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    int ind1 = (int)grid1.getAttribute(x,y,z);
                    int ind2 = (int)grid2.getAttribute(x,y,z);
                    if(ind1 != ind2){
                        double dist1 = distance2(x,y,z,pntx,pnty,pntz, ind1);
                        double dist2 = distance2(x,y,z,pntx,pnty,pntz, ind2); 
                        if(dist2 < dist1) 
                            grid1.setAttribute(x,y,z, ind2);
                    }
                }
            }
        }
    }

    /**
     * scan the index grid for used indices and assign INF to points which are not presented in the grid 
     * 
     */
    public static int removeUnusedPoints(AttributeGrid indexGrid, double pntx[], double pnty[], double pntz[]){
        int indices[] = new int[pntx.length];
        int 
            nx = indexGrid.getWidth(),
            ny = indexGrid.getHeight(),
            nz = indexGrid.getDepth();
        
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    // set this index as used 
                    indices[(int)indexGrid.getAttribute(x,y,z)] = 1;
                }
            }
        }
        int usedcount = 0;
        for(int i = 0; i < indices.length; i++){
            if(indices[i] == 0) {
                pntx[i] = INF;
                pnty[i] = INF;
                pntz[i] = INF;
            } else {
                usedcount++;
            }    
        }        
        return usedcount;
    }


    /**
     * calculated distance grid from given set of indices and pont coordinates 
     *  accept points as PointSet 
     * 
     */
    public static void makeDistanceGrid(AttributeGrid indexGrid, 
                                        PointSet pnts,
                                        AttributeGrid interiorGrid, 
                                        AttributeGrid distanceGrid,
                                        double minDistance,
                                        double maxDistance
                                        ){
        int npnt = pnts.size();
        double px[] = new double[npnt];
        double py[] = new double[npnt];
        double pz[] = new double[npnt];
        Vector3d pnt = new Vector3d();
        for(int i = 0; i < npnt; i++){
            pnts.getPoint(i, pnt);
            px[i] = pnt.x;
            py[i] = pnt.y;
            pz[i] = pnt.z;
        }
        makeDistanceGrid(indexGrid, px, py, pz, interiorGrid, minDistance, maxDistance, distanceGrid);

    }

    /**
       calculates distance grid with optional multichannel data originated from array of attributed points 
       
       on output each voxel of outGrid contains distance to closest point and (possible transformed) attribute values of that point
       @param indexGrid contain indices of closes point to each voxel 
       @param pnts  coordinates and attributed of each point used in 
       pnts[0] - x-coordinates 
       pnts[1] - y-coordinates 
       pnts[2] - z-coordinates 
       pnts[3] - attribute channel 0
       pnts[4] - attribute channel 1
       ..............
       @param interiorGrid if not null contain non-zero value for interior voxels. It is used to assign negative distance value to interior voxels 
       @param minDistance  minimal signed distance value to store in the grid. Values smaller than  minDistance are assigned value minDistance.
       @param maxDistance  maximal signed distance value to store in the grid. Voxels with distances larger than maxDistance are
       @param colorizer optional converter from points attributes into grid channel attributes.
       Particular use case when points attributes contain 2D textre coordinates and they need to be converted into 3D color components. 
       If attributeColorizer is null, identity DataSource is used instead.

       @param outGrid output grid which contains calculated attributes 
       outGrid data description, obtained via outGrid.gridDataDesc() method shall return appropriate GridDataDesc which can be used to convert (distance, att0, att1, att2, ...) 
       into grid attribute value.              
       
    */
    public static void makeAttributedDistanceGrid(AttributeGrid indexGrid, 
                                                  double pnts[][],
                                                  AttributeGrid interiorGrid, 
                                                  double minDistance,
                                                  double maxDistance,
                                                  DataSource colorizer,
                                                  AttributeGrid outGrid
                                                  ){
        int 
            ny = indexGrid.getHeight(),
            nz = indexGrid.getDepth();
        if(DEBUG)printf("makeAttributedDistanceGrid() doing slices\n");
        Bounds bounds = indexGrid.getGridBounds();
        long att[] = new long[nz];
        boolean interior[] = new boolean[nz];

        for(int y = 0; y < ny; y++)
            makeAttributedDistanceGridSlice(y, y+1, bounds, att, interior,
                                            indexGrid, pnts, interiorGrid,minDistance,maxDistance,colorizer, outGrid);
        
    }

    public static void makeAttributedDistanceGridSlice(int yStart, 
                                                       int yEnd, 
                                                       Bounds bounds, 
                                                       long att[], 
                                                       boolean interior[],
                                                       AttributeGrid indexGrid, 
                                                       double pnts[][],
                                                       AttributeGrid interiorGrid, 
                                                       double minDistance,
                                                       double maxDistance,
                                                       DataSource colorizer,
                                                       AttributeGrid outGrid
                                                       ){
        if(DEBUG)printf("makeAttributedDistanceGridSlice(%d, %d)\n", yStart, yEnd);        
        int 
            nx = indexGrid.getWidth(),
            nz = indexGrid.getDepth();

        GridDataDesc dataDesc =  outGrid.getDataDesc();        
        AttributePacker attMaker = dataDesc.getAttributePacker();

        double vs = bounds.getVoxelSize();
        double vs2 = vs/2;
        double xmin = bounds.xmin+vs2;
        double ymin = bounds.ymin+vs2;
        double zmin = bounds.zmin+vs2;

        int maxDataInd = pnts.length-1;

        double pntx[] = pnts[0];
        double pnty[] = pnts[1];
        double pntz[] = pnts[2];
        // TODO 
        // what do to if there is less data 
        double pntu[] = pnts[Math.min(3,maxDataInd)];
        double pntv[] = pnts[Math.min(4,maxDataInd)];
        double pntw[] = pnts[Math.min(5,maxDataInd)];

        // TODO use variable channel dimension 
        Vec pntData = new Vec(3);        
        Vec colorData = new Vec(3);        
        Vec voxelData = new Vec(4);        
        voxelData.v[0] = minDistance;
        long inAtt = attMaker.makeAttribute(voxelData);
        voxelData.v[0] = maxDistance;
        long outAtt = attMaker.makeAttribute(voxelData);

        for(int y = yStart; y < yEnd; y++){
            double coordy = ymin + vs*y;
            for(int x = 0; x < nx; x++){
                double coordx = xmin + vs*x;
                // read input grid data into 1D arrays
                for(int z = 0; z < nz; z++){
                    att[z] = indexGrid.getAttribute(x,y,z);
                }   
                if(interiorGrid != null){
                    for(int z = 0; z < nz; z++){
                        interior[z] = (interiorGrid.getAttribute(x,y,z) != 0);
                    }                
                }
                for(int z = 0; z < nz; z++){
                    int ind = (int)att[z];
                    if(ind > 0) {
                        // point has closest point 
                        double coordz = zmin + vs*z;
                        double dist2 = distance2(coordx,coordy,coordz, pntx[ind],pnty[ind],pntz[ind]);
                        double dist = sqrt(dist2);
                        
                        pntData.v[0] = pntu[ind];
                        pntData.v[1] = pntv[ind];
                        pntData.v[2] = pntw[ind];

                        if (colorizer != null) {
                            colorizer.getDataValue(pntData, colorData);
                        }
                        //TODO - use different channels dimension 
                        if(interior[z]) {
                            dist = -dist;
                        }
                        voxelData.v[0] = dist;
                        voxelData.v[1] = colorData.v[0];
                        voxelData.v[2] = colorData.v[1];
                        voxelData.v[3] = colorData.v[2];
                        att[z] = attMaker.makeAttribute(voxelData);
                    }  else {
                        // point is undefined 
                        if(interior[z]){
                            // interior 
                            att[z] = inAtt;
                        } else {
                            // exterior 
                            att[z] = outAtt;
                        }
                    }
                } // for(int z
                // write distances into output grid                
                outGrid.setAttributes(x,y,att);                                
            }
        }
        
    }

    
    /**
       calculates distance grid from given closest point grid and interior grid 
       distanceGrid value are mapped and clamped to the interval [-maxInDistance, maxOutDistance]
       points are in given in world units
       makes calculations in slices 
       
       @param indexGrid contains indices of closest point. index = 0 meand closesnt point is undefined
       @param pnts  pnts[0] x-coordinates, pnts[1] y-coordinates, 
       @param interiorGrid contains non zero value for voxels in the shape interior 
       
     */
    public static void makeDistanceGrid(AttributeGrid indexGrid, 
                                        double pnts[][],
                                        AttributeGrid interiorGrid, 
                                        double minDistance,
                                        double maxDistance,
                                        AttributeGrid distanceGrid
                                        ){
        makeDistanceGrid(indexGrid, pnts[0], pnts[1], pnts[2], interiorGrid, minDistance, maxDistance, distanceGrid);        
    }

    /**
       calculates distance grid from given closest point grid and interior grid 
       distanceGrid value are mapped and clamped to the interval [-maxInDistance, maxOutDistance]
       points are in given in world units
       makes calculations in slices 
       
       @param indexGrid contains indices of closest point. index = 0 meand closesnt point is undefined
       @param pntx x-coordinates of points in world units
       @param pnty y-coordinates of points in world units
       @param pnty z-coordinates of points in world units
       @param interiorGrid grid of voxles whcuh are in the shape interior 
       
     */
    public static void makeDistanceGrid(AttributeGrid indexGrid, 
                                        double pntx[], double pnty[], double pntz[], 
                                        AttributeGrid interiorGrid, 
                                        double minDistance,
                                        double maxDistance,
                                        AttributeGrid distanceGrid
                                        ){
        
        int 
            ny = indexGrid.getHeight(),
            nz = indexGrid.getDepth();
        if(DEBUG)printf("makeDistanceGrid() slices \n");
        Bounds bounds = indexGrid.getGridBounds();
        long att[] = new long[nz];
        boolean interior[] = new boolean[nz];

        for(int y = 0; y < ny; y++)
            makeDistanceGridSlice(y, y+1, bounds, att, interior,
                                  indexGrid, pntx,pnty, pntz, interiorGrid,minDistance,maxDistance,distanceGrid);
    }

    /**
       calculate distance values in grid y-slice (yStart <= y < yEnd) 
       
       @param yStart first slice to make 
       @param yEnd last slice to make (exclusive) 
       @param bounds grid bounds used to control the 3D positions where distance is calculated. Bounds have to have correct voxel size as well 
       bounds of other grid are ignored                
       @param indexGrid  contains index of closes point to each voxel. value 0 means undefined, noclosest point was assigned
       @param pntx  x-coordinates of array of points (point index 0 is not used) 
       @param pnty  y-coordinates of array of points (point index 0 is not used) 
       @param pntz  z-coordinates of array of points (point index 0 is not used) 
       @param interiorGrid if not null contain non-zero value for interior voxels. It is used to assign negative distance value to interior voxels 
       @param distanceGrid on output contains calculated distance values to closest point
       @param minDistance  minimal signed distance value to store in the grid. Values smaller than  minDistance are assigned value minDistance.
       @param maxDistance  maximal signed distance value to store in the grid. Voxels with distacnes larger than maxDistance are 
       @param att work array of length grid depth
       @param interior work array of length grid depth
     */
    public static void makeDistanceGridSlice(int yStart, int yEnd, 
                                             Bounds bounds, 
                                             long att[], 
                                             boolean interior[],
                                             AttributeGrid indexGrid, 
                                             double pntx[], double pnty[], double pntz[], 
                                             AttributeGrid interiorGrid, 
                                             double minDistance,
                                             double maxDistance,
                                             AttributeGrid distanceGrid
                                             ){
        
        int 
            nx = indexGrid.getWidth(),
            nz = indexGrid.getDepth();

        GridDataChannel distanceDataChannel = distanceGrid.getDataDesc().getChannel(0);
        double vs = bounds.getVoxelSize();
        double vs2 = vs/2;
        double xmin = bounds.xmin+vs2;
        double ymin = bounds.ymin+vs2;
        double zmin = bounds.zmin+vs2;
        
        long inAtt = distanceDataChannel.makeAtt(minDistance);
        long outAtt = distanceDataChannel.makeAtt(maxDistance);
//        printf("minDistance: %7.5f maxDistance: %7.5f minAtt: 0x%x  maxAtt: 0x%x \n", minDistance, maxDistance, inAtt, outAtt);
        for(int y = yStart; y < yEnd; y++){
            double coordy = ymin + vs*y;
            for(int x = 0; x < nx; x++){
                double coordx = xmin + vs*x;
                // read input grid data into 1D arrays
                for(int z = 0; z < nz; z++){
                    att[z] = indexGrid.getAttribute(x,y,z);
                }   
                if(interiorGrid != null){
                    for(int z = 0; z < nz; z++){
                        interior[z] = (interiorGrid.getAttribute(x,y,z) != 0);
                    }                
                }
                for(int z = 0; z < nz; z++){
                    int ind = (int)att[z];
                    if(ind > 0) {
                        // point has closest point 
                        double coordz = zmin + vs*z;
                        double dist2 = distance2(coordx,coordy,coordz, pntx[ind],pnty[ind],pntz[ind]);
                        double dist = sqrt(dist2);
                        //xbprintf("%d\n", dist);
                        if(interior[z]) {
                            dist = -dist;
                        }
                        att[z] = distanceDataChannel.makeAtt(dist);                         
                    }  else {
                        // point is undefined 
                        if(interior[z]){
                            // interior 
                            att[z] = inAtt;
                        } else {
                            // exterior 
                            att[z] = outAtt;
                        }
                    }
                } // for(int z
                // write distances into output grid
                for(int z = 0; z < nz; z++){
                    distanceGrid.setAttribute(x,y,z,att[z]);
                }                
            }
        }
    }

    /**
       calculates distance grid from given closest point grid and interior grid 
       distanceGrid value are mapped and clamped to the interval [-maxInDistance, maxOutDistance]
       points are in given in world units
       
       @param indexGrid contains indices of closest point. index = 0 meand closesnt point is undefined
       @param pntx x-coordinates of points in world units
       @param pnty y-coordinates of points in world units
       @param interiorGrid grid of voxles whcuh are in the shape interior 
     */
    public static void makeDistanceGrid2D(Grid2D indexGrid, 
                                          double pntx[], double pnty[],
                                          Grid2D interiorGrid, 
                                          Grid2D distanceGrid, 
                                          double maxInDistance, 
                                          double maxOutDistance){

        int 
            nx = indexGrid.getWidth(),
            ny = indexGrid.getHeight();
        
        GridDataChannel distChannel = distanceGrid.getAttributeDesc().getChannel(0);

        long intAtt = distChannel.makeAtt(maxInDistance);
        long extAtt = distChannel.makeAtt(maxOutDistance);


        double coord[] = new double[3];
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                int ind = (int)indexGrid.getAttribute(x,y);
                if(ind > 0) {
                    indexGrid.getWorldCoords(x, y, coord);
                    double dist = distance(coord[0],coord[1], pntx[ind],pnty[ind]);
                    if(interiorGrid != null && interiorGrid.getAttribute(x,y) != 0)
                        dist = -dist;
                    distanceGrid.setAttribute(x,y,distChannel.makeAtt(dist));
                }  else {
                    // point is undefined 
                    if(interiorGrid != null && interiorGrid.getAttribute(x,y) != 0){
                        // interior 
                        distanceGrid.setAttribute(x,y,intAtt);
                    } else {
                        // exterior 
                        distanceGrid.setAttribute(x,y,extAtt);
                    }
                }
            } // for(x
        } // for(y 
    }


    /**
       makes density grid from index grid and coordinates of points 

       @param indexGrid contaisn indiecs of closest point 
     */
    public static void makeDensityGrid(AttributeGrid indexGrid, 
                                       double pntx[], double pnty[], double pntz[], 
                                       AttributeGrid interiorGrid, 
                                       AttributeGrid densityGrid,
                                       GridDataChannel dataChannel){
        int 
            nx = indexGrid.getWidth(),
            ny = indexGrid.getHeight(),
            nz = indexGrid.getDepth();
        //printf("ClosestPointIndexer.makeDensityGrid(): [%d x %d x %d]\n", nx, ny, nz);
        long interiorAtt = dataChannel.makeAtt(1.);
        long exteriorAtt = dataChannel.makeAtt(0.);

        double voxelSize = densityGrid.getVoxelSize();        
        double coord[] = new double[3];
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    int ind = (int)indexGrid.getAttribute(x,y,z);                    
                    if(ind > 0) {
                        indexGrid.getWorldCoords(x, y, z, coord);
                        double dist2 = distance2(coord[0],coord[1],coord[2], pntx[ind],pnty[ind],pntz[ind]);
                        double dist = sqrt(dist2);
                        if(interiorGrid != null && interiorGrid.getAttribute(x,y,z) != 0){
                            dist = -dist;
                        }
                        double density = step10(dist, voxelSize);
                        long att = dataChannel.makeAtt(density);
                        densityGrid.setAttribute(x,y,z,att);
                    }  else {
                        // point is undefined 
                        if(interiorGrid != null && interiorGrid.getAttribute(x,y,z) != 0){
                            // interior 
                            densityGrid.setAttribute(x,y,z,interiorAtt);
                        } else {
                            // exterior 
                            densityGrid.setAttribute(x,y,z,exteriorAtt);
                        }
                    }
                }
            }
        }
    }


    static void printArray(String title, int count, int index[], double coord[]){

        printf(title);
        for(int k = 0; k < count; k++){
            printf("%7.3f ", coord[index[k]]);
        }
        printf("\n");
    }

    /**
       distance between 2D points 
     */
    static final double distance(double vx, double vy, double px, double py){
        vx -= px;
        vy -= py;
        return sqrt(vx*vx + vy*vy);
    }

    /**
       distance between 3D points 
    */
    static final double distance(double vx, double vy, double vz, double px, double py, double pz){
        vx -= px;
        vy -= py;
        vz -= pz;
        return sqrt(vx*vx + vy*vy + vz*vz);
    }

    static final double distance2(double vx, double vy, double vz, double px, double py, double pz){
        vx -= px;
        vy -= py;
        vz -= pz;
        return (vx*vx + vy*vy + vz*vz);
    }

    static final double distance2(int vx,int vy, double pntx[],double pnty[],int ind){
        double 
            x = vx + HALF,
            y = vy + HALF;
        x -= pntx[ind];
        y -= pnty[ind];
        return x*x + y*y;        
    }

    static final double distance2(int vx,int vy,int vz, double pntx[],double pnty[],double pntz[], int ind){
        double 
            x = vx + HALF,
            y = vy + HALF,
            z = vz + HALF;
        x -= pntx[ind];
        y -= pnty[ind];
        z -= pntz[ind];
        return x*x + y*y + z*z;

    }

    static final double length2(double dx, double dy){
        return dx*dx + dy*dy;
    }


    
    static void printI(int v[], int n){
        for(int i = 0; i < n; i++){
            printf("%3d ", v[i]);
        }
        printf("\n");
    }

    static void printD(double w[], int n){
        for(int i = 0; i < n; i++){
            double d = w[i];
            if(abs(d + INF) < EPS)
                printf("-INF ");
            else if (abs(d - INF) < EPS)
                printf("+INF ");
            else 
                printf("%4.1f ", d);
        }
        printf("\n");
    }

    static final double sqr(double x){
        return x*x;
    }

    static final int iround(double x){
        return (int)(x + 0.5);
    }    

    static void printD(String s, double w[], int n){
        printf(s);
        printD(w,n);
    }

    static void printTitle(String str, int n){
        printf(str);
        for(int i = 0; i < n; i++){
            printf("%4d ", i);
        }
        printf("\n");        
    }
    static void printRow(String str, int n, double coord[], int gpnt[]){
        printf(str);
        for(int i = 0; i < n; i++){
            int k = gpnt[i];
            if(k == 0) printf("  .  ");
            else printf("%4.2f ", coord[k]);
        }
        printf("\n");
    }
 
    static void printInd(String str, int iy, int iz, AttributeGrid indexGrid){
        printf(str);
        int nx = indexGrid.getWidth();
        for(int ix = 0; ix < nx; ix++){
            int k = (int)indexGrid.getAttribute(ix, iy, iz);
            if(k == 0) printf("  .  ");
            else printf("%4d ", k);
        }
        printf("\n");        
    }   
    
    static void printGrid(AttributeGrid grid){
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        for(int iy = 0; iy < ny; iy++){
            for(int ix = 0; ix < nx; ix++){
                printf("%d ",(int)grid.getAttribute(ix, iy, 0));
            }
            printf("\n");
        }
    }
}