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

import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Operation;
import abfab3d.grid.AttributeOperation;
import abfab3d.grid.GridBitIntervals;
import abfab3d.grid.ClassTraverser;
import abfab3d.grid.GridBit;
import abfab3d.grid.VoxelStateSetter;

import static abfab3d.util.Output.printf;

import static abfab3d.grid.Grid.OUTSIDE;
import static abfab3d.grid.Grid.INSIDE;

/**
 * Dilate an object one layer per iteration. Repeat given numbers of iterations.
 * For each filled voxel, check neigbours and add whose, which are empty
 * 
 *  next iteration runs over newly added voxels only. This speeds up the process a lot. 
 *  Works with very large grids. 
 * 
 * @author Vladimir Bulatov
 */
public class DilationMask implements Operation, AttributeOperation {

    public static int sm_debug = 0;
	
    // count of iterations to dilate
    private int m_iterCount;

    GridBitIntervals m_surface; // voxels turned ON on previus step
    GridBitIntervals m_marked;  // voxels to be turned ON after current scan

    AttributeGrid m_grid; // grid we are working on 
    int m_nnCount = 6; // count of nearest neighbors tpo use in operation 
    VoxelChecker m_voxelChecker;

    int m_nx, m_ny, m_nz; 

    public DilationMask(int iterCount) {
        this.m_iterCount = iterCount;
    }
    public DilationMask(int iterCount, int nnCount) {
        this.m_iterCount = iterCount;
        this.m_nnCount = nnCount;
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
        //TODO - not implemented 
        printf("DilationSurface.execute(Grid) not implemented!\n");        
        return grid;
    }


    public AttributeGrid execute(AttributeGrid grid) {

        printf("DilationMask.execute()\n");        
        m_grid = grid;
        m_nx = grid.getWidth();
        m_ny = m_grid.getHeight();
        m_nz = m_grid.getDepth();

        //m_gridCallCount = 0;
        //m_maskCallCount = 0;
        //m_processVoxelCount = 0;
        
        if( m_nnCount == 0){

            // spherical dilation 
            m_marked =  new GridBitIntervals(m_nx, m_ny, m_nz);
            
            //m_grid.find(Grid.VoxelClasses.OUTSIDE, new CustomOutsideVoxelProcesser(m_grid, m_marked, makeBall(m_iterCount), m_voxelChecker));
            m_grid.find(Grid.VoxelClasses.INSIDE, new CustomInsideVoxelProcesser(m_grid, m_marked, MaskFactory.makeBall(m_iterCount), m_voxelChecker));
            //m_grid.find(Grid.VoxelClasses.INSIDE, new SphericalVoxelProcesser(m_grid, m_marked, m_iterCount, m_voxelChecker));
            m_marked.find(Grid.VoxelClasses.INSIDE, new VoxelStateSetter(m_grid, Grid.INSIDE));
           
        } else {
            
            // iterative dilation 
            for(int i = 0; i < m_iterCount; i++){
                makeOneIteration(getCount(i));
            }
            
        }

        m_grid = null;
        
        if(m_surface != null){
            
            m_surface.release();
            m_surface = null;
        }
        if(m_marked != null){
            m_marked.release();
            m_marked = null;                
        }

        
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
       adds one layer of surface voxels 
     */
    public void makeOneIteration(int nnCount) {
                
        if(m_surface != null){
            
            // we have surface voxels calculated on previous step 
            // scan only surface voxels 
            m_marked.clear();
            m_surface.find(Grid.VoxelClasses.INSIDE, new BodyVoxelProcesser(m_grid, m_marked, nnCount));

        } else {
            
            // no surface calculated yet. Scan the whole grid to find marked voxels 
            m_surface = new GridBitIntervals(m_nx, m_ny, m_nz);
            m_marked =  new GridBitIntervals(m_nx, m_ny, m_nz);
            m_grid.find(Grid.VoxelClasses.INSIDE, new BodyVoxelProcesser(m_grid, m_marked, m_nnCount));
            
        }

        m_marked.find(Grid.VoxelClasses.INSIDE, new VoxelStateSetter(m_grid, Grid.INSIDE));
        
        // swap pointers surface <-> marked
        GridBitIntervals t = m_surface;
        m_surface = m_marked;
        m_marked = t;
        
    }


    /**
       dilation based on internal voxels 
     */
    static class CustomInsideVoxelProcesser implements ClassTraverser {
        
        Grid grid;
        GridBit mask; 
        int neighbors[];
        VoxelChecker voxelChecker;
        int nx, ny, nz;
        
        CustomInsideVoxelProcesser(Grid grid, GridBit mask, int neighbors[], VoxelChecker voxelChecker){

            this.grid = grid; 
            this.mask = mask;
            this.neighbors = neighbors;
            this.voxelChecker = voxelChecker;
            this.nx = grid.getWidth();
            this.ny = grid.getHeight();
            this.nz = grid.getDepth();

        }

        public void found(int x, int y, int z, byte state){
            foundInterruptible(x,y,z,state);            
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
            //m_processVoxelCount++;
            int index = 0;
            int nlength = neighbors.length;
            
            while(index < nlength){
                int ix = neighbors[index++];
                int iy = neighbors[index++];
                int iz = neighbors[index++];
                int xx = x + ix; 
                int yy = y + iy; 
                int zz = z + iz; 
                if(xx >= 0 && xx < nx && yy >= 0 && yy < ny && zz >= 0 && zz < nz ){
                    //m_gridCallCount++;
                    if(grid.getState(xx,yy,zz) == OUTSIDE){
                        //m_maskCallCount++;
                        mask.set(xx,yy,zz,1);              
                    }               
                }                    
            }
        }        
    } //class CustomInsideVoxelProcesser
    

    /**
       dilation based on outside voxels 
     */
    static class CustomOutsideVoxelProcesser implements ClassTraverser {
        
        Grid grid;
        GridBit mask; 
        int neighbors[];
        VoxelChecker voxelChecker;
        int nx, ny, nz;

        CustomOutsideVoxelProcesser(Grid grid, GridBit mask, int neighbors[], VoxelChecker voxelChecker){

            this.grid = grid; 
            this.mask = mask;
            this.neighbors = neighbors;
            this.voxelChecker = voxelChecker;

            nx = grid.getWidth();
            ny = grid.getHeight();
            nz = grid.getDepth();
            
        }

        public void found(int x, int y, int z, byte state){
            foundInterruptible(x,y,z,state);            
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

            //m_processVoxelCount++;

            int index = 0;

            while(index < neighbors.length){

                int ix = neighbors[index++];
                int iy = neighbors[index++];
                int iz = neighbors[index++];
                int xx = x + ix;
                int yy = y + iy;
                int zz = z + iz;
                if(xx >= 0 && xx < nx && 
                   yy >= 0 && yy < ny && 
                   zz >= 0 && zz < nz 
                   ){
                    //m_gridCallCount++;
                    if(grid.getState(xx,yy,zz) == INSIDE){
                        // the voxel has filled neighbor 
                        //m_maskCallCount++;
                        mask.set(x,y,z,1); 
                        break;
                    }             
                }            
            }
        }        
    } //class CustomOutsideVoxelProcesser
    

    static class SphericalVoxelProcesser implements ClassTraverser {
        
        Grid grid;
        GridBit mask; 
        int ballSize, ballSize2;
        VoxelChecker voxelChecker;

        SphericalVoxelProcesser(Grid grid, GridBit mask, int size, VoxelChecker voxelChecker){

            this.grid = grid; 
            this.mask = mask;
            this.ballSize = size;
            this.ballSize2 = size*size;
            this.voxelChecker = voxelChecker;

        }

        public void found(int x, int y, int z, byte state){
            foundInterruptible(x,y,z,state);            
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

            //
            // for each empty voxel we check if there is filled voxel in the bal neihborhood
            // and mark that voxel in the grid 
            //
            for(int iz = -ballSize; iz <= ballSize; iz++){
                for(int iy = -ballSize; iy <= ballSize; iy++){
                    for(int ix = -ballSize; ix <= ballSize; ix++){
                        int r2 = (ix*ix + iy*iy + iz*iz);
                        if(r2 <= ballSize2){
                            //printf("%d \n", r2, );
                            if(grid.getState(x+ix,y+iy,z+iz) == OUTSIDE){
                                // this voxel is empty and has grid voxel within distance 
                                mask.set(x+ix,y+iy,z+iz,1);                                 
                            }                                             
                        }
                    }
                }
            }
        }        
    } //class SphericalVoxelProcesser


    /**
       checks each of 6-neighbour voxels in gridIn and if it is empty turns ON corresponding voxel in mask
     */
    static class BodyVoxelProcesser implements ClassTraverser {

        Grid grid;
        GridBit mask; 
        int nnCount;
        
        BodyVoxelProcesser(Grid grid, GridBit mask, int nnCount){
            this.grid = grid; 
            this.mask = mask;
            this.nnCount = nnCount;
        }

        public void found(int x, int y, int z, byte state){
            foundInterruptible(x,y,z,state);            
        }
        
        public boolean foundInterruptible(int x, int y, int z, byte state){

            processVoxel(x,y,z);
            return true;

        }

        /**
           checks 6 neighbours of this model voxel 
           turn ON empty voxels in mask 
        */ 
        void processVoxel(int x,int y,int z){
            //m_processVoxelCount++;

            switch(nnCount){
            default: 
            case 18:
                processVoxel18(x,y,z);
                break;
            case 6:
                processVoxel6(x,y,z);
                break;
            case 26:
                processVoxel26(x,y,z);
                break;                
            }
        }
        
        void processVoxel6(int x,int y,int z){

            //m_gridCallCount+=6;
            
            if(grid.getState(x+1,y,z) == OUTSIDE) { mask.set(x+1,y,z,1); }           
            if(grid.getState(x-1,y,z) == OUTSIDE) { mask.set(x-1,y,z,1); }
            if(grid.getState(x,y+1,z) == OUTSIDE) { mask.set(x,y+1,z,1); }
            if(grid.getState(x,y-1,z) == OUTSIDE) { mask.set(x,y-1,z,1); }
            if(grid.getState(x,y,z+1) == OUTSIDE) { mask.set(x,y,z+1,1); }
            if(grid.getState(x,y,z-1) == OUTSIDE) { mask.set(x,y,z-1,1); }

        }        

        void processVoxel18(int x,int y,int z){
            
            if(grid.getState(x+1,y,z) == OUTSIDE) mask.set(x+1,y,z,1);            
            if(grid.getState(x-1,y,z) == OUTSIDE) mask.set(x-1,y,z,1);
            if(grid.getState(x,y+1,z) == OUTSIDE) mask.set(x,y+1,z,1);
            if(grid.getState(x,y-1,z) == OUTSIDE) mask.set(x,y-1,z,1);
            if(grid.getState(x,y,z+1) == OUTSIDE) mask.set(x,y,z+1,1);            
            if(grid.getState(x,y,z-1) == OUTSIDE) mask.set(x,y,z-1,1);

            if(grid.getState(x+1,y+1,z) == OUTSIDE) mask.set(x+1,y+1,z,1);
            if(grid.getState(x-1,y+1,z) == OUTSIDE) mask.set(x-1,y+1,z,1);
            if(grid.getState(x+1,y-1,z) == OUTSIDE) mask.set(x+1,y-1,z,1);
            if(grid.getState(x-1,y-1,z) == OUTSIDE) mask.set(x-1,y-1,z,1);
            if(grid.getState(x+1,y,z+1) == OUTSIDE) mask.set(x+1,y,z+1,1);
            if(grid.getState(x-1,y,z+1) == OUTSIDE) mask.set(x-1,y,z+1,1);
            if(grid.getState(x+1,y,z-1) == OUTSIDE) mask.set(x+1,y,z-1,1);
            if(grid.getState(x-1,y,z-1) == OUTSIDE) mask.set(x-1,y,z-1,1);
            if(grid.getState(x,y+1,z+1) == OUTSIDE) mask.set(x,y+1,z+1,1);
            if(grid.getState(x,y-1,z+1) == OUTSIDE) mask.set(x,y-1,z+1,1);
            if(grid.getState(x,y+1,z-1) == OUTSIDE) mask.set(x,y+1,z-1,1);
            if(grid.getState(x,y-1,z-1) == OUTSIDE) mask.set(x,y-1,z-1,1);

        }        

        void processVoxel26(int x,int y,int z){
            
            if(grid.getState(x+1,y,z) == OUTSIDE) mask.set(x+1,y,z,1);            
            if(grid.getState(x-1,y,z) == OUTSIDE) mask.set(x-1,y,z,1);
            if(grid.getState(x,y+1,z) == OUTSIDE) mask.set(x,y+1,z,1);
            if(grid.getState(x,y-1,z) == OUTSIDE) mask.set(x,y-1,z,1);
            if(grid.getState(x,y,z+1) == OUTSIDE) mask.set(x,y,z+1,1);            
            if(grid.getState(x,y,z-1) == OUTSIDE) mask.set(x,y,z-1,1);

            if(grid.getState(x+1,y+1,z) == OUTSIDE) mask.set(x+1,y+1,z,1);
            if(grid.getState(x-1,y+1,z) == OUTSIDE) mask.set(x-1,y+1,z,1);
            if(grid.getState(x+1,y-1,z) == OUTSIDE) mask.set(x+1,y-1,z,1);
            if(grid.getState(x-1,y-1,z) == OUTSIDE) mask.set(x-1,y-1,z,1);
            if(grid.getState(x+1,y,z+1) == OUTSIDE) mask.set(x+1,y,z+1,1);
            if(grid.getState(x-1,y,z+1) == OUTSIDE) mask.set(x-1,y,z+1,1);
            if(grid.getState(x+1,y,z-1) == OUTSIDE) mask.set(x+1,y,z-1,1);
            if(grid.getState(x-1,y,z-1) == OUTSIDE) mask.set(x-1,y,z-1,1);
            if(grid.getState(x,y+1,z+1) == OUTSIDE) mask.set(x,y+1,z+1,1);
            if(grid.getState(x,y-1,z+1) == OUTSIDE) mask.set(x,y-1,z+1,1);
            if(grid.getState(x,y+1,z-1) == OUTSIDE) mask.set(x,y+1,z-1,1);
            if(grid.getState(x,y-1,z-1) == OUTSIDE) mask.set(x,y-1,z-1,1);

            if(grid.getState(x+1,y+1,z+1) == OUTSIDE) mask.set(x+1,y+1,z+1,1);
            if(grid.getState(x-1,y+1,z+1) == OUTSIDE) mask.set(x-1,y+1,z+1,1);
            if(grid.getState(x+1,y-1,z+1) == OUTSIDE) mask.set(x+1,y-1,z+1,1);
            if(grid.getState(x-1,y-1,z+1) == OUTSIDE) mask.set(x-1,y-1,z+1,1);
            if(grid.getState(x+1,y+1,z-1) == OUTSIDE) mask.set(x+1,y+1,z-1,1);
            if(grid.getState(x-1,y+1,z-1) == OUTSIDE) mask.set(x-1,y+1,z-1,1);
            if(grid.getState(x+1,y-1,z-1) == OUTSIDE) mask.set(x+1,y-1,z-1,1);
            if(grid.getState(x-1,y-1,z-1) == OUTSIDE) mask.set(x-1,y-1,z-1,1);

        }        


    }
            
    static int[] reallocArray(int array[], int newsize){
        if(sm_debug > 1)
            printf("reallocArray(%d)\n", newsize);
        int newarray[] = new int[newsize];
        System.arraycopy(array, 0, newarray, 0, array.length);
        return newarray;
    }
    
}
