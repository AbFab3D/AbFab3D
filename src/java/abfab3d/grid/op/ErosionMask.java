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

// External Imports
import abfab3d.grid.*;

// Internal Imports

import static java.lang.System.currentTimeMillis;
import static abfab3d.util.Output.printf;
import static abfab3d.grid.Grid.OUTSIDE;
import static abfab3d.grid.Grid.INTERIOR;

/**
 * Erode an object one layer per iteration. Repeat given numbers of iterations.
 * 
 *  Erosion is done via removing voxels, which have empty 6-neighbor
 * 
 *  next iteration runs over newly removed voxels only. This speeds up the process a lot. 
 *  surface voxels are stored as mask in GridBitIntervals
 * 
 * @author Vladimir Bulatov
 */
public class ErosionMask implements Operation, AttributeOperation {

    public static int sm_debug = 0;
	    
    /** The distance from a voxel to erode */
    private int m_iterCount;
    
    AttributeGrid m_grid; // grid we are working on 

    VoxelChecker m_voxelChecker = null; // user suppiued checker if voxel can be erroded
    GridBitIntervals m_surfaceMask; // mask of current surface voxels

    int m_nnCount = 6; // count of nearest neigbors to use in erosion 

    public ErosionMask(int iterCount) {
        this.m_iterCount = iterCount;
   } 

    public ErosionMask(int iterCount, int nnCount) {
        this.m_iterCount = iterCount;
        this.m_nnCount  = nnCount;
    }

    public void setVoxelChecker(VoxelChecker voxelChecker){
        m_voxelChecker = voxelChecker;
    }
        

   
    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid A.
     * @return original grid modified
     */
    public Grid execute(Grid grid) {

        printf("ErosionSurface.execute(Grid) not implemented!\n");
        
        return grid;
        
    }


    public AttributeGrid execute(AttributeGrid grid) {

        printf("ErosionMask.execute()\n");

        m_grid = grid;

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        m_surfaceMask = new GridBitIntervals(nx, ny, nz);
        
        if(m_nnCount == 0){
            
            // spherical erosion 
            m_grid.find(Grid.VoxelClasses.INTERIOR, new CustomVoxelsCollector(m_grid, m_surfaceMask, MaskFactory.makeBall(m_iterCount), m_voxelChecker));
            //m_grid.find(Grid.VoxelClasses.INTERIOR, new SphericalVoxelsCollector(m_grid, m_surfaceMask, m_iterCount, m_voxelChecker));
            // set marked voxels as OUTSIDE
            m_surfaceMask.find(Grid.VoxelClasses.INTERIOR, new VoxelStateSetter(m_grid, Grid.OUTSIDE));
            m_surfaceMask.clear();
            
        } else {

            for(int i = 0; i < m_iterCount; i++){
                
                makeOneIteration(getCount(i));
                
            }            
            
        } 

        m_grid = null;
        m_surfaceMask = null;

        return grid;
    }

    int getCount(int index){
        switch(m_nnCount){
        default: 
            return m_nnCount;
        case 618:         
            if( (index & 1) != 0 ) // reduce asymmetry ? 
                return 6;
            else 
                return 18;            
        }
    }

    /**
       removes one layer of surface voxels 
     */
    public void makeOneIteration(int nnCount) {
        
        //m_markedCount = 0;
        
        //long t0 = currentTimeMillis();
        //if(m_surfaceMask != null){
            // we have surface voxels stored on previous step 
            // scan only surface voxels             
            //processSurfaceVoxel(x,y,z);                 
            
        //} else {
            // no surface calculated yet. Scan the whole grid to find marked voxels     

        m_grid.find(Grid.VoxelClasses.INTERIOR, new SurfaceVoxelsCollector(m_grid, m_surfaceMask, nnCount));
        
        //}

        // set marked voxels as OUTSIDE
        m_surfaceMask.find(Grid.VoxelClasses.INTERIOR, new VoxelStateSetter(m_grid, Grid.OUTSIDE));

        m_surfaceMask.clear();

    }
    
    /**
       runs over custom set of neigbors
     */
    static class CustomVoxelsCollector implements ClassTraverser {

        GridBitIntervals surfaceMask;
        AttributeGrid grid;
        VoxelChecker voxelChecker;
        int neighbors[];
        int nx, ny, nz;
        
        CustomVoxelsCollector(AttributeGrid grid, GridBitIntervals surfaceMask, int neighbors[], VoxelChecker voxelChecker){

            this.grid = grid; 
            this.surfaceMask = surfaceMask;
            this.neighbors = neighbors;
            this.voxelChecker = voxelChecker;
            this.nx = grid.getWidth();
            this.ny = grid.getHeight();
            this.nz = grid.getDepth();
        }

        public void found(int x, int y, int z, byte _state){

            processModelVoxel(x,y,z);
            
        }

        public boolean foundInterruptible(int x, int y, int z, byte _state){

            processModelVoxel(x,y,z);
            return true;
        }
                
        
        void processModelVoxel(int x,int y,int z){
            if(voxelChecker != null){
                if(!voxelChecker.canProcess(x,y,z)){
                    return; 
                }
            }
            int nlength = neighbors.length;
            int index = 0;
            while(index < nlength){
                int ix = neighbors[index++];
                int iy = neighbors[index++];
                int iz = neighbors[index++];
                int xx = x + ix; 
                int yy = y + iy; 
                int zz = z + iz; 
                if(xx >= 0 && xx < nx && 
                   yy >= 0 && yy < ny && 
                   zz >= 0 && zz < nz ){
                    
                    if(grid.getState(xx,yy,zz) == OUTSIDE){
                        // we have outside neighbor, set mask to 
                        surfaceMask.set(x,y,z,1); 
                        return;              
                    }      
                }
            }
        }        
    } // class CustomVoxelCollector 

    static class SphericalVoxelsCollector implements ClassTraverser {

        GridBitIntervals surfaceMask;
        AttributeGrid grid;
        int ballSize;
        int ballSize2;
        VoxelChecker voxelChecker;

        SphericalVoxelsCollector(AttributeGrid grid, GridBitIntervals surfaceMask, int size, VoxelChecker voxelChecker){
            this.grid = grid;
            this.surfaceMask = surfaceMask;
            this.ballSize = size;
            this.ballSize2 = size*size;
            this.voxelChecker = voxelChecker; 
        }

        public void found(int x, int y, int z, byte _state){

            processModelVoxel(x,y,z);
            
        }

        public boolean foundInterruptible(int x, int y, int z, byte _state){

            processModelVoxel(x,y,z);
            return true;
        }
                
        
        void processModelVoxel(int x,int y,int z){
            if(voxelChecker != null){
                if(!voxelChecker.canProcess(x,y,z)){
                    return; 
                }
            }
            for(int iy = -ballSize; iy <= ballSize; iy++){
                for(int ix = -ballSize; ix <= ballSize; ix++){
                    for(int iz = -ballSize; iz <= ballSize; iz++){
                        int r2 = (ix*ix + iy*iy + iz*iz);
                        if(r2 <= ballSize2){
                            //printf("%d \n", r2, );
                            if(grid.getState(x+ix,y+iy,z+iz) == OUTSIDE){
                                // 
                                surfaceMask.set(x,y,z,1); 
                                return;
                            }                                             
                        }
                    }
                }
            }
        }        
    } // class SphericalVoxelCollector 
    
    
    static class SurfaceVoxelsCollector implements ClassTraverser {

        GridBitIntervals surfaceMask;
        AttributeGrid grid;
        int nnCount;

        SurfaceVoxelsCollector(AttributeGrid grid, GridBitIntervals surfaceMask, int nnCount){
            this.grid = grid;
            this.surfaceMask = surfaceMask;
            this.nnCount = nnCount;
        }

        public void found(int x, int y, int z, byte _state){

            processModelVoxel(x,y,z);
            
        }

        public boolean foundInterruptible(int x, int y, int z, byte _state){

            processModelVoxel(x,y,z);
            return true;
        }
        
        /**
           checks 6 neighbours of this interior voxel and if any is empty, 
           adds this voxel to marked[]
           and updates m_markedCount
        */ 
        void processModelVoxel(int x,int y,int z){
            
            switch(nnCount){
            default: 
            case 6:  
                processModelVoxel6(x,y,z); 
                break;
            case 18:
                processModelVoxel18(x,y,z); 
                break;
            case 26:
                processModelVoxel26(x,y,z); 
                break;
            }
        }

        void processModelVoxel6(int x,int y,int z){
        
            if(grid.getState(x+1,y,z) == OUTSIDE || 
               grid.getState(x-1,y,z) == OUTSIDE || 
               grid.getState(x,y+1,z) == OUTSIDE || 
               grid.getState(x,y-1,z) == OUTSIDE || 
               grid.getState(x,y,z+1) == OUTSIDE || 
               grid.getState(x,y,z-1) == OUTSIDE 
               ){                
                surfaceMask.set(x,y,z,1);                
            }
        }
        void processModelVoxel18(int x,int y,int z){
        
            if(
               grid.getState(x+1,y+1,z) == OUTSIDE ||
               grid.getState(x-1,y+1,z) == OUTSIDE ||
               grid.getState(x+1,y-1,z) == OUTSIDE ||
               grid.getState(x-1,y-1,z) == OUTSIDE ||
               grid.getState(x+1,y,z+1) == OUTSIDE ||
               grid.getState(x-1,y,z+1) == OUTSIDE ||
               grid.getState(x+1,y,z-1) == OUTSIDE ||
               grid.getState(x-1,y,z-1) == OUTSIDE ||
               grid.getState(x,y+1,z+1) == OUTSIDE ||
               grid.getState(x,y-1,z+1) == OUTSIDE ||
               grid.getState(x,y+1,z-1) == OUTSIDE ||
               grid.getState(x,y-1,z-1) == OUTSIDE ||

               grid.getState(x+1,y,z) == OUTSIDE || 
               grid.getState(x-1,y,z) == OUTSIDE || 
               grid.getState(x,y+1,z) == OUTSIDE || 
               grid.getState(x,y-1,z) == OUTSIDE || 
               grid.getState(x,y,z+1) == OUTSIDE || 
               grid.getState(x,y,z-1) == OUTSIDE 
               ){                
                surfaceMask.set(x,y,z,1);                
            }
        }
        void processModelVoxel26(int x,int y,int z){
        
            if(
               grid.getState(x+1,y+1,z+1) == OUTSIDE ||
               grid.getState(x-1,y+1,z+1) == OUTSIDE ||
               grid.getState(x+1,y-1,z+1) == OUTSIDE ||
               grid.getState(x-1,y-1,z+1) == OUTSIDE ||
               grid.getState(x+1,y+1,z-1) == OUTSIDE ||
               grid.getState(x-1,y+1,z-1) == OUTSIDE ||
               grid.getState(x+1,y-1,z-1) == OUTSIDE ||
               grid.getState(x-1,y-1,z-1) == OUTSIDE ||

               grid.getState(x+1,y+1,z) == OUTSIDE ||
               grid.getState(x-1,y+1,z) == OUTSIDE ||
               grid.getState(x+1,y-1,z) == OUTSIDE ||
               grid.getState(x-1,y-1,z) == OUTSIDE ||
               grid.getState(x+1,y,z+1) == OUTSIDE ||
               grid.getState(x-1,y,z+1) == OUTSIDE ||
               grid.getState(x+1,y,z-1) == OUTSIDE ||
               grid.getState(x-1,y,z-1) == OUTSIDE ||
               grid.getState(x,y+1,z+1) == OUTSIDE ||
               grid.getState(x,y-1,z+1) == OUTSIDE ||
               grid.getState(x,y+1,z-1) == OUTSIDE ||
               grid.getState(x,y-1,z-1) == OUTSIDE ||

               grid.getState(x+1,y,z) == OUTSIDE || 
               grid.getState(x-1,y,z) == OUTSIDE || 
               grid.getState(x,y+1,z) == OUTSIDE || 
               grid.getState(x,y-1,z) == OUTSIDE || 
               grid.getState(x,y,z+1) == OUTSIDE || 
               grid.getState(x,y,z-1) == OUTSIDE 

               ){                
                surfaceMask.set(x,y,z,1);                
            }
        }        
    }  //class SurfaceVoxelsCollector
    
    static class VoxelStateSetter implements ClassTraverser {

        AttributeGrid grid;
        byte state;

        VoxelStateSetter(AttributeGrid grid, byte state){
            this.grid = grid;
            this.state = state;
        }

        public void found(int x, int y, int z, byte _state){
            grid.setState(x,y,z,state);
        }

        public boolean foundInterruptible(int x, int y, int z, byte _state){
            grid.setState(x,y,z,state);
            return true;
        }        
    }        
}


