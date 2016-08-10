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


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.abs;

import abfab3d.core.AttributeGrid;

import abfab3d.core.Bounds;
import abfab3d.util.SliceManager;
import abfab3d.util.Slice;
import abfab3d.core.DataSource;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;

import static abfab3d.grid.op.ClosestPointIndexer.DT3sweepX_bounded;
import static abfab3d.grid.op.ClosestPointIndexer.DT3sweepY_bounded;
import static abfab3d.grid.op.ClosestPointIndexer.DT3sweepZ_bounded;

import static abfab3d.grid.op.ClosestPointIndexer.combineGridsSlice;
import static abfab3d.grid.op.ClosestPointIndexer.makeDistanceGridSlice;
import static abfab3d.grid.op.ClosestPointIndexer.makeAttributedDistanceGridSlice;

/**
   MT version of closest point indexer
   
   works based on ClosesPointIndexer 

   different threads work in paralel on different layers of the grid 
   
   

   @author Vladimir Bulatov
 */
public class ClosestPointIndexerMT {


    static final boolean DEBUG_TIMING = true;
    static final boolean DEBUG = false;
    //static final double DEF_LAYER_THICKNESS = 1.8;

    
    /**
       calculates closest Point Index for each cell of the index grid 
       the indexGrid should be initialized with indices of point in close proximity to the center of grid voxels 
       
     *  @param coord  3 arrays of physical coordinates of points. coord[0] x-coordinates, coord[1] y-coordinates, coord[2] z-coordinates, 
     *  element with index 0 of each array is ignored 
     *  @param maxDistance max distance to calculate (in physical units) 
     *  @param firstLayerThickiness thickness of first layer in grid units (good values are in range (1,4)) 
     *  @param indexGrid - on input has indices of closest points in thin layer around the point cloud 
     *                   - on output has indices of closest point for each grid point 
     *                   valid indices start from 1, index value 0 means "undefined" 
     * @param threadCount count of thread to be used for calculation 
     */
    public static void getClosestPoints(double coord[][], double maxDistance, double firstLayerThickness, AttributeGrid indexGrid, int threadCount){
        
        double vs = indexGrid.getVoxelSize();
        double pntx[] = coord[0];
        double pnty[] = coord[1];
        double pntz[] = coord[2];

        ClosestPointIndexer.getPointsInGridUnits(indexGrid, pntx, pnty, pntz);
        ClosestPointIndexer.initFirstLayer(indexGrid, pntx,pnty,pntz, firstLayerThickness);

        PI3_MT(pntx,pnty,pntz, maxDistance/vs, indexGrid, threadCount);

        ClosestPointIndexer.getPointsInWorldUnits(indexGrid, coord[0],coord[1],coord[2]);
        
    }


    /**
       calculates closest Point Indexer for each cell of the index grid 
       the indexGrid should be initialized with indices of point in close proximity to the center of grid voxels 
       
     *  @param coordx  array of x coordinates. coordx[0] is unused 
     *  @param coordy  array of y coordinates. coordy[0] is unused  
     *  @param coordz  array of y coordinates. coordz[0] is unused 
     *  @param indexGrid - on input has indices of closest points in thin layer around the point cloud 
     *                   - on output has indices of closest point for each grid point 
     *                   valid indices start from 1, index value 0 means "undefined" 
     * @param threadCount count of thread to be used for calculation 
     */
    public static void PI3_MT(double coordx[], double coordy[], double coordz[], AttributeGrid indexGrid, int threadCount){
        PI3_MT(coordx, coordy, coordz, 0., indexGrid, threadCount);        
    }

    


    /**
       calculates closest Point Indexer for each cell of the index grid 
       the indexGrid should be initialized with indices of point in close proximity to the center of grid voxels 
       
     *  @param coordx  array of x coordinates. coordx[0] is unused 
     *  @param coordy  array of y coordinates. coordy[0] is unused  
     *  @param coordz  array of y coordinates. coordz[0] is unused 
     *  @param maxDistance maximal value to calculate and store distances. if maxDistance == 0, the full range is calculated 
     *  @param indexGrid - on input has indices of closest points in thin layer around the point cloud 
     *                   - on output has indices of closest point for each grid point 
     *                   valid indices start from 1, index value 0 means "undefined" 
     * @param threadCount count of thread to be used for calculation 
     */
    public static void PI3_MT(double coordx[], double coordy[], double coordz[], double maxDistance, AttributeGrid indexGrid, int threadCount){

        if(threadCount <= 1) {
            // use ST path for single thread
            ClosestPointIndexer.PI3_bounded(coordx, coordy, coordz, maxDistance, indexGrid);
            return;
        }
        //if(DEBUG){printf("PI3_MT(threads: %d)\n", threadCount);}
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


        DT3sweep_MT(0, indexGrid.getDepth(), coordx, coordy, coordz, maxDistance, indexGrid, threadCount);
        DT3sweep_MT(1, indexGrid.getDepth(), coordx, coordy, coordz, maxDistance, indexGrid, threadCount);
        DT3sweep_MT(2, indexGrid.getHeight(), coordx, coordy, coordz, maxDistance, indexGrid, threadCount);

        if(DEBUG_TIMING){t1 = time();printf("PI3_MT() done %d ms\n", t1 - t0);t0 = t1;}
        
    }


    /**
       calculates closest Point Indexer for each cell of the index grid using multi pass algorithm 
       the indexGrid should be initialized with indices of point in close proximity to the center of grid voxels 
       
     *  @param coordx  array of x coordinates. coordx[0] is unused 
     *  @param coordy  array of y coordinates. coordy[0] is unused  
     *  @param coordz  array of y coordinates. coordz[0] is unused 
     *  @param maxDistance maximal value to calculate and store distances. if maxDistance == 0, the full range is calculated 
     *  @param indexGrid - on input has indices of closest points in thin layer around the point cloud 
     *                   - on output has indices of closest point for each grid point 
     *                   valid indices start from 1, index value 0 means "undefined" 
     * @param threadCount count of thread to be used for calculation 
     */
    public static void PI3_multiPass_MT(double coordx[], double coordy[], double coordz[], double maxDistance, AttributeGrid indexGrid, int threadCount){

        if(DEBUG){printf("PI3_multiPass_MT(threads: %d)\n", threadCount);}
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
        AttributeGrid origGrid = (AttributeGrid)indexGrid.clone();        
        AttributeGrid workGrid = (AttributeGrid)indexGrid.clone();
        if(DEBUG_TIMING) {
            printf("PI3_multiPass_MT() grid cloning: %d ms\n", time() - t1);
            t1 = time();
        }
        //XYZ
        DT3sweep_MT(0, nz, coordx, coordy, coordz, maxDistance, indexGrid, threadCount);
        DT3sweep_MT(1, nz, coordx, coordy, coordz, maxDistance, indexGrid, threadCount);
        DT3sweep_MT(2, ny, coordx, coordy, coordz, maxDistance, indexGrid, threadCount);
        if(DEBUG_TIMING) {
            printf("PI3_multiPass_MT() first pass: %d ms\n", time() - t1);
            t1 = time();
        }

        //YZX
        DT3sweep_MT(1, nz, coordx, coordy, coordz, maxDistance, workGrid, threadCount);
        DT3sweep_MT(2, ny, coordx, coordy, coordz, maxDistance, workGrid, threadCount);
        DT3sweep_MT(0, nz, coordx, coordy, coordz, maxDistance, workGrid, threadCount);

        if(DEBUG_TIMING) {
            printf("PI3_multiPass_MT() second pass: %d ms\n", time() - t1);
            t1 = time();
        }

        combineGrids_MT(indexGrid, workGrid, coordx, coordy, coordz, threadCount);
        
        if(DEBUG) {
            printf("PI3_multiPass_MT() combine grids: %d ms\n", time() - t1);
            t1 = time();
        }
        workGrid.copyData(origGrid);

        if(DEBUG_TIMING) {
            printf("PI3_multiPass_MT() copy data: %d ms\n", time() - t1);
            t1 = time();
        }

        //ZXY
        DT3sweep_MT(2, ny, coordx, coordy, coordz, maxDistance, workGrid, threadCount);
        DT3sweep_MT(0, nz, coordx, coordy, coordz, maxDistance, workGrid, threadCount);
        DT3sweep_MT(1, nz, coordx, coordy, coordz, maxDistance, workGrid, threadCount);

        if(DEBUG_TIMING) {
            printf("PI3_multiPass_MT() third pass: %d ms\n", time() - t1);
            t1 = time();
        }
        combineGrids_MT(indexGrid, workGrid, coordx, coordy, coordz, threadCount);
        if(DEBUG_TIMING) {
            printf("PI3_multiPass_MT() combine grids: %d ms\n", time() - t1);
            t1 = time();
        }

        if(DEBUG_TIMING){printf("PI3_multiPass_MT() done %d ms\n", time() - t0);}
        
    }


    protected static void DT3sweep_MT(int direction, int gridSize, double coordx[], double coordy[], double coordz[], double maxDistance, AttributeGrid indexGrid, int threadCount){
        
        //if(DEBUG) printf("DT3sweep_MT(%d)\n", direction);
        long t0 = time();
        int sliceThickness = 1;
        SliceManager slicer = new SliceManager(gridSize,sliceThickness);
        
        //if(DEBUG) printf("threads: %d slices: %d \n", threadCount, slicer.getSliceCount());

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for(int i = 0; i < threadCount; i++){
            SliceProcessorSweeper sliceProcessor = new SliceProcessorSweeper(i, direction, slicer,coordx,coordy,coordz, maxDistance, indexGrid);
            executor.submit(sliceProcessor);
        }
        executor.shutdown();
        
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
                
        if(DEBUG_TIMING) printf("DT3sweep_MT(%d) done %d ms\n", direction, (time() - t0));

    }

   

    /**
       class to calculate distance transform for single slice
    */
    static class SliceProcessorSweeper implements Runnable {
        
        SliceManager slicer;
        int id;
        double coordx[];
        double coordy[];
        double coordz[];
        AttributeGrid indexGrid;
        int direction; // 0,1,2 
        int nx;
        int ny;
        int nz;
        // work arrays
        int v[];
        double w[];
        int ipnt[];
        double value[];
        int gpnt[];
        double maxDistance = 0.;

        SliceProcessorSweeper(int id, int direction, SliceManager slicer, double coordx[], double coordy[], double coordz[], double maxDistance, AttributeGrid indexGrid){

            this.id = id;
            this.slicer = slicer;
            this.coordx = coordx;
            this.coordy = coordy;
            this.coordz = coordz;
            this.indexGrid = indexGrid;
            this.direction = direction;
            this.maxDistance = maxDistance;

            this.nx = indexGrid.getWidth();
            this.ny = indexGrid.getHeight();
            this.nz = indexGrid.getDepth();
            
            // maximal dimension 
            int nm = max(max(nx, ny),nz); 
            
            // work arrays
            this.v = new int[nm];
            this.w = new double[nm+1];
            this.ipnt = new int[nm+1];
            this.value = new double[nm];
            this.gpnt = new int[nm];
            
        }

        public void run(){

            while(true){

                Slice slice = slicer.getNextSlice();
                if(slice == null)
                    break;
                // process slice X 
                try {
                    switch(direction){
                    default: 
                    case 0: 
                        DT3sweepX_bounded(slice.smin, slice.smax, coordx, coordy, coordz, maxDistance, indexGrid, v, w, ipnt, value, gpnt);
                        break;
                    case 1: 
                        DT3sweepY_bounded(slice.smin, slice.smax, coordx, coordy, coordz, maxDistance, indexGrid, v, w, ipnt, value, gpnt);
                        break;
                    case 2: 
                        DT3sweepZ_bounded(slice.smin, slice.smax, coordx, coordy, coordz, maxDistance, indexGrid, v, w, ipnt, value, gpnt);
                        break;
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }            
        }        
    } // static class SliceProcessorSweeper



    /**
       compares distances to points stored in two grids and select shortest distance and stores result in first grid
     */
    public static void combineGrids_MT(AttributeGrid grid1, AttributeGrid grid2, double pntx[], double pnty[], double pntz[], int threadCount){
        if(threadCount <= 1) {
            ClosestPointIndexer.combineGrids(grid1, grid2, pntx,pnty, pntz);
            return;
        }

        if(DEBUG) printf("combineGrids_MT(%d)\n", threadCount);
        long t0 = time();
        int sliceThickness = 1;
        SliceManager slicer = new SliceManager(grid1.getHeight(),sliceThickness);        
        //if(DEBUG) printf("threads: %d slices: %d \n", threadCount, slicer.getSliceCount());
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for(int i = 0; i < threadCount; i++){
            SliceProcessorCombine sliceProcessor = new SliceProcessorCombine(i, slicer, grid1, grid2, pntx, pnty, pntz);
            executor.submit(sliceProcessor);
        }
        executor.shutdown();
        
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
                
    }

    public static void makeDistanceGrid_MT(AttributeGrid indexGrid, 
                                           double pntx[], double pnty[], double pntz[], 
                                           AttributeGrid interiorGrid, 
                                           double minDistance,
                                           double maxDistance,
                                           int threadCount,
                                           AttributeGrid distanceGrid
                                        ){


        //if(DEBUG) printf("makeDistanceGrid_MT()\n");
        long t0 = time();
        int sliceThickness = 1;
        SliceManager slicer = new SliceManager(distanceGrid.getHeight(),sliceThickness);
        
        if(DEBUG) printf("threads: %d slices: %d \n", threadCount, slicer.getSliceCount());
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for(int i = 0; i < threadCount; i++){
            SliceProcessorDistance sliceProcessor = new SliceProcessorDistance(i, slicer,
                                                                               indexGrid, pntx, pnty, pntz, 
                                                                               interiorGrid, distanceGrid, minDistance,maxDistance);
            executor.submit(sliceProcessor);
        }
        executor.shutdown();
        
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        //if(DEBUG_TIMING) printf("makeDistanceGrid_MT() done %d ms\n", (time() - t0));

    }
    
    public static void makeAttributedDistanceGrid_MT(AttributeGrid indexGrid, 
                                                     double [][] pnts, 
                                                     AttributeGrid interiorGrid, 
                                                     double minDistance, 
                                                     double maxDistance, 
                                                     DataSource attColorizer,
                                                     int threadCount, 
                                                     AttributeGrid outGrid){

        if(DEBUG) printf("makeAttributedDistanceGrid_MT() threadCount: %d\n", threadCount);
        if(threadCount <= 1){            
            ClosestPointIndexer.makeAttributedDistanceGrid(indexGrid, pnts, interiorGrid, minDistance, maxDistance, attColorizer, outGrid);
            return;
        }
        int sliceThickness = 1;
        SliceManager slicer = new SliceManager(outGrid.getHeight(),sliceThickness);
        
        if(DEBUG) printf("threads: %d slices: %d \n", threadCount, slicer.getSliceCount());
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for(int ind = 0; ind < threadCount; ind++){
            SliceProcessorAttributedDistance sliceProcessor = new SliceProcessorAttributedDistance(ind, slicer,indexGrid, pnts, interiorGrid, minDistance,maxDistance, attColorizer, outGrid);
            executor.submit(sliceProcessor);
        }
        executor.shutdown();
        
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(DEBUG) printf("makeAttributedDistanceGrid_MT() done\n");
        
    }



    /**
       class to calculate distance values on the grid
     */
    static class SliceProcessorDistance implements Runnable {

        SliceManager slicer;
        int id;
        double coordx[];
        double coordy[];
        double coordz[];
        AttributeGrid indexGrid;
        AttributeGrid interiorGrid;
        AttributeGrid distanceGrid; 
        // work arrays
        long att[];
        boolean interior[];
        double minDistance, maxDistance;
        Bounds gridBounds;

        SliceProcessorDistance(int id, SliceManager slicer, 
                               AttributeGrid indexGrid, double coordx[], double coordy[], double coordz[], AttributeGrid interiorGrid, AttributeGrid distanceGrid, 
                               double minDistance,double maxDistance){

            this.id = id;
            this.slicer = slicer;
            this.coordx = coordx;
            this.coordy = coordy;
            this.coordz = coordz;
            this.indexGrid = indexGrid;
            this.distanceGrid = distanceGrid;
            this.interiorGrid = interiorGrid;
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;

            this.gridBounds = indexGrid.getGridBounds();

            int nz = indexGrid.getDepth();
                        
            // work arrays
            this.att = new long[nz];
            this.interior = new boolean[nz];
            
        }

        public void run(){

            //if(DEBUG)printf("thread: %d run\n", id);
            while(true){
                
                Slice slice = slicer.getNextSlice();
                //if(DEBUG)printf("thread: %d slice: %s\n", id, slice);
                if(slice == null)
                    break;

                makeDistanceGridSlice(slice.smin, slice.smax, gridBounds, att, interior,
                                      indexGrid, coordx, coordy, coordz, interiorGrid, minDistance, maxDistance, distanceGrid);
                //if(DEBUG)printf("thread: %d slice: %d %d done\n", id, slice.smin, slice.smax);
            }
            
            //if(DEBUG)printf("thread: %d done\n", id);
        }        
    } // static class SliceProcessorDistance 


    /**
       class to calculate attributed distance slice values on the grid
     */
    static class SliceProcessorAttributedDistance implements Runnable {

        SliceManager slicer;
        int id;
        double pnts[][];
        AttributeGrid indexGrid;
        AttributeGrid interiorGrid;
        AttributeGrid outGrid; 
        // work arrays
        long att[];
        boolean interior[];
        double minDistance, maxDistance;
        Bounds gridBounds;
        DataSource colorizer;

        SliceProcessorAttributedDistance(int id, 
                                         SliceManager slicer,                                          
                                         AttributeGrid indexGrid, 
                                         double pnts[][], 
                                         AttributeGrid interiorGrid, 
                                         double minDistance,
                                         double maxDistance, 
                                         DataSource colorizer,
                                         AttributeGrid outGrid){
            
            this.id = id;
            this.slicer = slicer;
            this.pnts = pnts;
            this.indexGrid = indexGrid;
            this.interiorGrid = interiorGrid;
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            this.colorizer = colorizer;
            this.outGrid = outGrid;
            this.gridBounds = indexGrid.getGridBounds();

            int nz = indexGrid.getDepth();
                        
            // work arrays
            this.att = new long[nz];
            this.interior = new boolean[nz];
            
        }

        public void run(){

            if(DEBUG)printf("thread: %d run\n", id);
            while(true){
                
                Slice slice = slicer.getNextSlice();
                //if(DEBUG)printf("thread: %d slice: %s\n", id, slice);
                if(slice == null)
                    break;
                try {
                    makeAttributedDistanceGridSlice(slice.smin, slice.smax, gridBounds,  att, interior,indexGrid, pnts,interiorGrid, minDistance,maxDistance,colorizer,outGrid);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }            
        }        
    } // static class SliceProcessorAttributedDistance 


    /**
       class to combine slices of two grids
     */
    static class SliceProcessorCombine implements Runnable {

        SliceManager slicer;
        int id;
        double coordx[];
        double coordy[];
        double coordz[];
        AttributeGrid grid1;
        AttributeGrid grid2;

        SliceProcessorCombine(int id, SliceManager slicer, 
                              AttributeGrid grid1, 
                              AttributeGrid grid2, 
                              double coordx[], double coordy[], double coordz[]){

            this.id = id;
            this.slicer = slicer;
            this.coordx = coordx;
            this.coordy = coordy;
            this.coordz = coordz;
            this.grid1 = grid1;
            this.grid2 = grid2;            
        }

        public void run(){

            //if(DEBUG)printf("thread: %d run\n", id);
            while(true){
                
                Slice slice = slicer.getNextSlice();
                //if(DEBUG)printf("thread: %d slice: %s\n", id, slice);
                if(slice == null)
                    break;                
                combineGridsSlice(slice.smin, slice.smax, grid1, grid2, coordx, coordy, coordz);
            }            
            //if(DEBUG)printf("thread: %d done\n", id);
        }        
    } // static class SliceProcessorCombine

} // class ClosestPointIndexer_MT
