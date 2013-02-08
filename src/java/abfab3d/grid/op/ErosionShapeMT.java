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

import java.util.Stack; 
import java.util.HashMap; 
import java.util.Iterator;

import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Operation;
import abfab3d.grid.AttributeOperation;
import abfab3d.grid.GridBitIntervals;
import abfab3d.grid.ClassTraverser;
import abfab3d.grid.GridBit;
import abfab3d.grid.VoxelStateSetter;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;

import static abfab3d.grid.Grid.OUTSIDE;
import static abfab3d.grid.Grid.INTERIOR;

/**
 * Erode an object with given custom shape
 *
 *  Multithreaded version 
 * 
 *  1) find outside surface voxels of the grid 
 *  2) erode grid with VoxelShape running over surface voxels 
 *
 * @author Vladimir Bulatov
 */
public class ErosionShapeMT implements Operation, AttributeOperation {

    public static int sm_debug = 0;
	
    VoxelChecker m_voxelChecker; // external tester which checks if voxel needs to be processed
    VoxelShape m_voxelShape;  // shape used to perform dilation     
    
    int m_threadCount = 1;
    int m_sliceSize = 1;
    int m_nx, m_ny, m_nz;
    Stack<Slice> m_surfaceSlices; 
    Stack<Slice> m_erosionSlices; 

    public ErosionShapeMT() {
        
    }
    
    public void setThreadCount(int count){

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
     * @param grid The grid to use for grid A.
     * @return original grid modified
     */
    public Grid execute(Grid grid) {

        //TODO - not implemented 
        throw new IllegalArgumentException(fmt("ErosionShapeMT.execute(%d) not implemented!\n", grid));  

    }

    
    public AttributeGrid execute(AttributeGrid grid) {

        printf("ErosionShapeMT.execute(%s)\n", grid); 
        
        m_nx = grid.getWidth();
        m_ny = grid.getHeight();
        m_nz = grid.getDepth();

        long t0 = time();

        GridBitIntervals surface = new GridBitIntervals(m_nx, m_ny, m_nz);
        //GridShortIntervals surface = new GridShortIntervals(m_nx, m_ny, m_nz, 1., 1.);
        m_surfaceSlices = new Stack<Slice>();

        int sliceHeight = m_sliceSize; 
        
        for(int y = 0; y < m_ny; y+= sliceHeight){
            int ymax = y + sliceHeight;
            if(ymax > m_ny)
                ymax = m_ny;
            
            if(ymax > y){
                // non zero slice 
                m_surfaceSlices.push(new Slice(y, ymax-1));
            }
        }
        
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

        //grid.find(Grid.VoxelClasses.INTERIOR, new SurfaceFinder(grid, m_surface));
        printf("surface: %d ms\n", (time()-t0));

        t0 = time();

        m_erosionSlices = new Stack<Slice>();

        for(int y = 0; y < m_ny; y+= sliceHeight){
            int ymax = y + sliceHeight;
            if(ymax > m_ny)
                ymax = m_ny;
            if(ymax > y){
                // non zero slice 
                m_erosionSlices.push(new Slice(y, ymax-1));
            }
        }

        executor = Executors.newFixedThreadPool(m_threadCount);
        for(int i = 0; i < m_threadCount; i++){

            Runnable runner = new ShapeEroderRunner(surface, grid, m_voxelShape, m_voxelChecker);
            executor.submit(runner);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //surface.find(Grid.VoxelClasses.INTERIOR, new ShapeEroder(grid, m_voxelShape, m_voxelChecker));

        printf("erosion: %d ms\n", (time()-t0));
        
        surface.release();
        surface = null;

        return grid;
    }

    
    synchronized Slice getNextSurfaceSlice(){

        if(m_surfaceSlices.empty())
            return null;
        
        return m_surfaceSlices.pop();
        
    }
    
    synchronized Slice getNextErosionSlice(){

        if(m_erosionSlices.empty())
            return null;
        
        return m_erosionSlices.pop();
        
    }

    /**
       erosion of surface voxels with given shape
     */
    class ShapeEroderRunner implements Runnable, ClassTraverser {
        
        Grid surface;
        Grid grid;
        VoxelShape shape; 

        int neighbors[];
        int neighborsIncremented[];
        VoxelChecker voxelChecker;
        int nx, ny, nz;

        int x1 = -1, y1 = -1, z1 = -1;// coordinate of previous processed voxel 

        ShapeEroderRunner(Grid surface, Grid grid, VoxelShape shape, VoxelChecker checker){

            this.grid = grid; 
            this.surface = surface; 

            this.shape = shape;
            this.voxelChecker = voxelChecker;


            nx = grid.getWidth();
            ny = grid.getHeight();
            nz = grid.getDepth();
            neighbors = shape.getCoords();
            neighborsIncremented = shape.getCoordsIncremented();
            
        }

        public void run(){

            //printf("%s:.run()\n", Thread.currentThread());
            
            Slice slice;
            
            while(true){

                slice = getNextErosionSlice();
                if(slice == null)
                    break;
                
                surface.find(Grid.VoxelClasses.INTERIOR, this, 0, m_nx-1, slice.ymin, slice.ymax); 
                
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
                // we are next to last point 
                neig = neighborsIncremented;
                z1 = z;

            } else {

                neig = neighbors;
                // remember last point 
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
                    grid.setState(xx,yy,zz, Grid.OUTSIDE);
                }                    
            }
        }   
    } //class ShapeEroderRunner 

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
                Slice slice = getNextSurfaceSlice();
                if(slice == null){
                    // end of processing 
                    break;
                }
                grid.find(Grid.VoxelClasses.INTERIOR, this, 0, m_nx-1, slice.ymin, slice.ymax);
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
            int x1, y1, z1;

            x1 = x+1;
            if(grid.getState(x1,y,z) == OUTSIDE) surface.set(x1,y,z,1);

            x1 = x-1;
            if(grid.getState(x1,y,z) == OUTSIDE) surface.set(x1,y,z,1);

            y1 = y+1;
            if(grid.getState(x,y1,z) == OUTSIDE) surface.set(x,y1,z,1);

            y1 = y-1;
            if(grid.getState(x,y1,z) == OUTSIDE) surface.set(x,y1,z,1);

            z1 = z+1;
            if(grid.getState(x,y,z1) == OUTSIDE) surface.set(x,y,z1,1);

            z1 = z-1;
            if(grid.getState(x,y,z1) == OUTSIDE) surface.set(x,y,z1,1);

        }   

    } // SurfaceFinderRunner


    //
    //  class to represent one slice of grid 
    //
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
