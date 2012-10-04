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

import java.util.HashMap; 
import java.util.Iterator;

import abfab3d.grid.*;

import static java.lang.System.currentTimeMillis;
import static abfab3d.util.Output.printf;

import static abfab3d.grid.Grid.OUTSIDE;
import static abfab3d.grid.Grid.INTERIOR;

/**
 * Dilate an object one layer per iteration. Repeat given numbers of iterations.
 * For each filled voxel, check 6-neigbours and add whose, which are empty
 *  
 * Iteration after first runs over newly added voxels only. This speeds up the process a lot. 
 * The surface voxels are stored in a binary mask (GridBitIntervals) for memory efficiency 
 *  
 * Works with very large grids. 
 * 
 * @author Vladimir Bulatov
 */
public class DilationMask implements Operation, AttributeOperation {

    public static int sm_debug = 0;
	
    // count of iterations to dilate
    private int m_iterCount;

    GridBitIntervals m_surface; // voxels turned ON on previous step
    GridBitIntervals m_marked;  // voxels to be turned ON after current scan

    AttributeGrid m_grid; // grid we are working on 
    
    int m_nx, m_ny, m_nz; 

    public DilationMask(int iterCount) {
        this.m_iterCount = iterCount;
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
        printf("DilationSurface.execute(Grid) not implemented!\n");        
        return grid;
    }


    public AttributeGrid execute(AttributeGrid grid) {

        m_grid = grid;
        m_nx = grid.getWidth();
        m_ny = m_grid.getHeight();
        m_nz = m_grid.getDepth();

        for(int i = 0; i < m_iterCount; i++){
            makeOneIteration();
        }
        
        m_grid = null;
        if(m_surface != null){

            m_surface.release();
            m_surface = null;
            m_marked.release();
            m_marked = null;

        }

        return grid;
    }

    /**
       adds one layer of surface voxels 
     */
    public void makeOneIteration() {
                
        if(m_surface != null){
            
            // we have surface voxels calculated on previous step 
            // scan only surface voxels 
            m_marked.clear();
            m_surface.findInterruptible(Grid.VoxelClasses.INTERIOR, new BodyVoxelProcesser(m_grid, m_marked));

        } else {
            
            // no surface calculated yet. Scan the whole grid to find marked voxels 
            m_surface = new GridBitIntervals(m_nx, m_ny, m_nz);
            m_marked =  new GridBitIntervals(m_nx, m_ny, m_nz);
            m_grid.findInterruptible(Grid.VoxelClasses.INTERIOR, new BodyVoxelProcesser(m_grid, m_marked));
            
        }

        m_marked.findInterruptible(Grid.VoxelClasses.INTERIOR, new VoxelStateSetter(m_grid, Grid.INTERIOR));
        
        // swap pointers surface <-> marked
        GridBitIntervals t = m_surface;
        m_surface = m_marked;
        m_marked = t;
        
    }
    
    /**
       checks 6 neighbours of each incoming voxel 
       and if neightbour is empty it turns ON the corresponding voxel in the mask 
       
    */ 
    static class BodyVoxelProcesser implements ClassTraverser {

        Grid grid;
        GridBit mask; 

        BodyVoxelProcesser(Grid grid, GridBit mask){
            this.grid = grid; 
            this.mask = mask;
        }
        public void found(int x, int y, int z, byte state){
            foundInterruptible(x,y,z,state);            
        }
        
        public boolean foundInterruptible(int x, int y, int z, byte state){

            processVoxel(x,y,z);
            return true;

        }

        void processVoxel(int x,int y,int z){
            
            if(grid.getState(x+1,y,z) == OUTSIDE){
                mask.set(x+1,y,z,1);
            }
            if(grid.getState(x-1,y,z) == OUTSIDE){
                mask.set(x-1,y,z,1);
            }
            if(grid.getState(x,y+1,z) == OUTSIDE){
                mask.set(x,y+1,z,1);
            }        
            if(grid.getState(x,y-1,z) == OUTSIDE){
                mask.set(x,y-1,z,1);
            }        
            if(grid.getState(x,y,z+1) == OUTSIDE){
                mask.set(x,y,z+1,1);
            }
            if(grid.getState(x,y,z-1) == OUTSIDE){
                mask.set(x,y,z-1,1);
            }                        
        }        
    } // class BodyVoxelProcesser              
}


