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

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.AttributeChannel;
import abfab3d.grid.AttributeDesc;

import abfab3d.grid.ArrayAttributeGridShort;
import abfab3d.grid.Grid2D;

import abfab3d.grid.op.Neighborhood;
import abfab3d.util.Bounds;
import abfab3d.util.SliceManager;
import abfab3d.util.Slice;

import java.util.Arrays;

import static java.lang.Math.sqrt;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;

import static abfab3d.grid.op.ClosestPointIndexer.DT3sweepX_bounded;
import static abfab3d.grid.op.ClosestPointIndexer.DT3sweepY_bounded;
import static abfab3d.grid.op.ClosestPointIndexer.DT3sweepZ_bounded;

import static abfab3d.grid.op.ClosestPointIndexer.makeDistanceGridSlice;

/**
   MT version of closest point indexer
   
   works based on ClosesPointIndexer 

   different threads work in paralel on different layers of the grid 
   
   

   @author Vladimir Bulatov
 */
public class ClosestPointIndexerMT {


    static final boolean DEBUG_TIMING = false;
    static final boolean DEBUG = false;


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
     *  @param maxDistance maximal distance to calculate distances. if maxDistance == 0, the full range is calculated 
     *  @param indexGrid - on input has indices of closest points in thin layer around the point cloud 
     *                   - on output has indices of closest point for each grid point 
     *                   valid indices start from 1, index value 0 means "undefined" 
     * @param threadCount count of thread to be used for calculation 
     */
    public static void PI3_MT(double coordx[], double coordy[], double coordz[], double maxDistance, AttributeGrid indexGrid, int threadCount){

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



    public static void makeDistanceGrid_MT(AttributeGrid indexGrid, 
                                           double pntx[], double pnty[], double pntz[], 
                                           AttributeGrid interiorGrid, 
                                           AttributeGrid distanceGrid,
                                           double maxInDistance,
                                           double maxOutDistance,
                                           int threadCount
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
                                                                               interiorGrid, distanceGrid, maxInDistance,maxOutDistance);
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
        double maxInDistance, maxOutDistance;
        Bounds gridBounds;

        SliceProcessorDistance(int id, SliceManager slicer, 
                               AttributeGrid indexGrid, double coordx[], double coordy[], double coordz[], AttributeGrid interiorGrid, AttributeGrid distanceGrid, 
                               double maxInDistance,double maxOutDistance){

            this.id = id;
            this.slicer = slicer;
            this.coordx = coordx;
            this.coordy = coordy;
            this.coordz = coordz;
            this.indexGrid = indexGrid;
            this.distanceGrid = distanceGrid;
            this.interiorGrid = interiorGrid;
            this.maxInDistance = maxInDistance;
            this.maxOutDistance = maxOutDistance;

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
                                      indexGrid, coordx, coordy, coordz, interiorGrid, distanceGrid, maxInDistance, maxOutDistance);
                //if(DEBUG)printf("thread: %d slice: %d %d done\n", id, slice.smin, slice.smax);
            }
            
            //if(DEBUG)printf("thread: %d done\n", id);
        }        
    } // static class SliceProcessorDistance 

}
