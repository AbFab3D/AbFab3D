/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
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

import java.util.concurrent.atomic.AtomicInteger;

import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Operation;
import abfab3d.grid.AttributeOperation;
import abfab3d.grid.GridBitIntervals;
import abfab3d.grid.ClassTraverser;
import abfab3d.grid.GridBit;
import abfab3d.util.AbFab3DGlobals;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;

import static abfab3d.grid.Grid.OUTSIDE;

/**
 * Dilate an object with given custom shape. Multithreaded version
 * 
 * 
 *  1) find surface voxels of the grid 
 *  2) dilate surface voxels with the VoxelShape
 *
 * @author Vladimir Bulatov
 */
public class DilationShapeMT implements Operation, AttributeOperation {

    public static int sm_debug = 0;
	
    VoxelChecker m_voxelChecker; // external tester which checks if voxel needs to be processed
    VoxelShape m_voxelShape;  // shape used to perform dilation 

    int m_nx, m_ny, m_nz; 
    int m_threadCount = 1;
    int m_sliceSize = 1;

    private Slice[] m_slices;
    private AtomicInteger m_slicesIdx;
    
    public DilationShapeMT() {
        
    }
    
    public void setThreadCount(int count){
        if (count < 1) {
            count = Runtime.getRuntime().availableProcessors();
        }

        int max_threads = ((Number) AbFab3DGlobals.get(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY)).intValue();
        if (count > max_threads)
            count = max_threads;

        m_threadCount = count;
    }

    public void setSliceSize(int size){

        m_sliceSize = size;

    }


    public void setVoxelChecker(VoxelChecker voxelChecker){

        m_voxelChecker = voxelChecker;

    }
        
    /**
       set shape to use for dilation 
     */
    public void setVoxelShape(VoxelShape voxelShape){

        m_voxelShape = voxelShape;

    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for dilation 
     * @return original grid modified
     */
    public Grid execute(Grid grid) {
        //TODO - not implemented 
        printf("DilationShapeMT.execute(Grid) not implemented!\n");        
        return grid;
    }

    
    public AttributeGrid execute(AttributeGrid grid) {

        printf("DilationShapeMT.execute()\n"); 

        long t0 = time();

        m_nx = grid.getWidth();
        m_ny = grid.getHeight();
        m_nz = grid.getDepth();

        GridBitIntervals surface = new GridBitIntervals(m_nx, m_ny, m_nz);
        //GridShortIntervals surface = new GridShortIntervals(m_nx, m_ny, m_nz, 1., 1.);

        m_slices = new Slice[(int) Math.ceil(m_ny / m_sliceSize) + 1];
        int idx = 0;
        int sliceHeight = m_sliceSize; 
        
        for(int y = 0; y < m_ny; y+= sliceHeight){
            int ymax = y + sliceHeight;
            if(ymax > m_ny)
                ymax = m_ny;
            
            if(ymax > y){
                // non zero slice 
                m_slices[idx++] = new Slice(y, ymax-1);
            }
        }

        m_slicesIdx = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(m_threadCount);
        for(int i = 0; i < m_threadCount; i++){

            Runnable runner = new SurfaceFinderRunner(grid, surface);
            executor.submit(runner);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        printf("surface: %d ms\n", (time()-t0));
        t0 = time();

        m_dilationSlices = new Slice[(int) Math.ceil(m_ny / m_sliceSize) + 1];
        idx = 0;

        for(int y = 0; y < m_ny; y+= sliceHeight){
            int ymax = y + sliceHeight;
            if(ymax > m_ny)
                ymax = m_ny;
            if(ymax > y){
                // non zero slice 
                m_dilationSlices[idx++] = new Slice(y, ymax-1);
            }
        }

        m_dsIdx = new AtomicInteger(0);

        executor = Executors.newFixedThreadPool(m_threadCount);
        for(int i = 0; i < m_threadCount; i++){

            Runnable runner = new ShapeDilaterRunner(surface, grid, m_voxelShape, m_voxelChecker);
            executor.submit(runner);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        

        printf("dilation: %d ms\n", (time()-t0));
        
        surface.release();
        surface = null;
        return grid;
    }

    
    Slice getNextSlice(){
        if(m_slicesIdx.intValue() >= m_slices.length)
            return null;
        
        return m_slices[m_slicesIdx.getAndIncrement()];
        
    }
    
    Slice[] m_dilationSlices;
    AtomicInteger m_dsIdx;

    final static int RESULT_OK = 0, RESULT_BUSY = -1, RESULT_EMPTY = 1;

    int getNextDilationSlice(Slice slice){
        
        if(m_dsIdx.intValue() >= m_dilationSlices.length)
            return RESULT_EMPTY;

        Slice s = m_dilationSlices[m_dsIdx.getAndIncrement()];

        slice.ymin = s.ymin;
        slice.ymax = s.ymax;
        
        return RESULT_OK; 

    }

    /**
       class processes one slice of grid from the array of slices
     */
    class SurfaceFinderRunner implements Runnable, ClassTraverser {

        Grid grid;
        GridBit surface; 

        SurfaceFinderRunner(Grid grid, GridBit surface){
            this.grid = grid; 
            this.surface = surface; 
        }

        public void run(){  

            while(true){
                Slice slice = getNextSlice();
                if(slice == null){
                    // end of processing 
                    break;
                }
                grid.find(Grid.VoxelClasses.INSIDE, this, 0, m_nx-1, slice.ymin, slice.ymax);
            }
        }
            
        public void found(int x, int y, int z, byte state){
            processVoxel(x,y,z);
        }
        
        public boolean foundInterruptible(int x, int y, int z, byte state){
            processVoxel(x,y,z);
            return true;
        }

        /**
           checks 6 neighbours of this model voxel 
           turn ON tyhe voxel if any neighbours are empty
        */ 
        void processVoxel(int x,int y,int z){

            //TODO add grid bounds checker             
            if(grid.getState(x+1,y,z) == OUTSIDE) {surface.set(x,y,z,1);return;}
            if(grid.getState(x-1,y,z) == OUTSIDE) {surface.set(x,y,z,1);return;}
            if(grid.getState(x,y+1,z) == OUTSIDE) {surface.set(x,y,z,1);return;}
            if(grid.getState(x,y-1,z) == OUTSIDE) {surface.set(x,y,z,1);return;}
            if(grid.getState(x,y,z+1) == OUTSIDE) {surface.set(x,y,z,1);return;}
            if(grid.getState(x,y,z-1) == OUTSIDE) {surface.set(x,y,z,1);return;}

        }   

    } // SurfaceFinderRunner

    /**
       dilation of surface voxels with given shape
     */
    class ShapeDilaterRunner implements Runnable, ClassTraverser {
        
        Grid surface;
        Grid grid;
        VoxelShape shape; 

        int neighbors[];
        int neighborsIncremented[];
        VoxelChecker voxelChecker;
        int nx, ny, nz;

        int x1 = -1, y1 = -1, z1 = -1;// coordinate of previous processed voxel 

        ShapeDilaterRunner(Grid surface, Grid grid, VoxelShape shape, VoxelChecker checker){

            this.grid = grid; 
            this.surface = surface; 

            this.shape = shape;
            this.voxelChecker = checker;


            nx = grid.getWidth();
            ny = grid.getHeight();
            nz = grid.getDepth();
            neighbors = shape.getCoords();
            neighborsIncremented = shape.getCoordsIncremented();
            
        }

        public void run(){

            //printf("%s:.run()\n", Thread.currentThread());
            
            Slice slice = new Slice();
            
            while(true){

                int res = getNextDilationSlice(slice);
                if(res == RESULT_EMPTY){
                    return;
                } else if(res == RESULT_OK){

                    //printf("%s: [%d,%d]\n", Thread.currentThread(), slice.ymin, slice.ymax);
                    surface.find(Grid.VoxelClasses.INSIDE, this, 0, m_nx-1, slice.ymin, slice.ymax);

                } if(res == RESULT_BUSY){
                    try {Thread.sleep(1);} catch(Exception e){}
                }
            }                        
        }

        public void found(int x, int y, int z, byte state){
            processVoxel(x, y, z);
        }
        
        public boolean foundInterruptible(int x, int y, int z, byte state){

            processVoxel(x,y,z);
            return true;
        }

        void processVoxel(int x, int y, int z){

            if(voxelChecker != null){
                if(!voxelChecker.canProcess(x,y,z))
                    return;
            }
            
            int neig[];
            if(x == x1 && y == y1 && z == z1+1){

                neig = neighborsIncremented;
                z1 = z;

            } else {

                neig = neighbors;
                x1 = x;
                y1 = y;
                z1 = z;
            }
            
            int index = 0;
            int nlength = neig.length;
            
            while(index < nlength){
                int ix = neig[index++];
                int iy = neig[index++];
                int iz = neig[index++];
                int xx = x + ix; 
                int yy = y + iy; 
                int zz = z + iz; 
                if(xx >= 0 && xx < nx && yy >= 0 && yy < ny && zz >= 0 && zz < nz ){
                    grid.setState(xx,yy,zz, Grid.INSIDE);
                }                    
            }
        }        
    } //class ShapeDilaterRunner 


    static class Slice {

        int ymin;
        int ymax;
        Slice(){
            ymin = 0;
            ymax = -1;

        }
        Slice(int ymin, int ymax){

            this.ymin = ymin;
            this.ymax = ymax;
            
        }
        
    }

}
