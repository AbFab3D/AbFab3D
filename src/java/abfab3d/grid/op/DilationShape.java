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

import abfab3d.grid.*;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;

import static abfab3d.grid.Grid.OUTSIDE;

/**
 * Dilate an object with given custom shape
 * 
 * 
 *  1) find surface voxels of the grid 
 *  2) dilate surface voxels with the VoxelShape
 *
 * @author Vladimir Bulatov
 */
public class DilationShape implements Operation, AttributeOperation {

    public static int sm_debug = 0;
	
    //static long m_gridCallCount = 0;
    //static long m_maskCallCount = 0;
    //static long m_processVoxelCount = 0;

    //GridBitIntervals m_surface; // voxels turned ON on previus step
    //GridBitIntervals m_marked;  // voxels to be turned ON after current scan

    //AttributeGrid m_grid; // grid we are working on 
    VoxelChecker m_voxelChecker; // external tester which checks if voxel needs to be processed
    VoxelShape m_voxelShape;  // shape used to perform dilation 

    int m_nx, m_ny, m_nz; 
    
    
    public DilationShape() {
        
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
        printf("DilationShape.execute(Grid) not implemented!\n");        
        return grid;
    }

    
    public AttributeGrid execute(AttributeGrid grid) {

        printf("DilationShape.execute()\n"); 
        
        ///m_grid = grid;

        m_nx = grid.getWidth();
        m_ny = grid.getHeight();
        m_nz = grid.getDepth();

        GridBitIntervals m_surface = new GridBitIntervals(m_nx, m_ny, m_nz);
        long t0 = time();
        grid.find(VoxelClasses.INSIDE, new SurfaceFinder(grid, m_surface));
        //printf("surface: %d ms\n", (time()-t0));
        t0 = time();
        m_surface.find(VoxelClasses.INSIDE, new ShapeDilater(grid, m_voxelShape, m_voxelChecker));
        //printf("dilation: %d ms\n", (time()-t0));
        
        m_surface.release();
        m_surface = null;
        return grid;
    }

    /**
       dilation of surface voxels with given shape
     */
    static class ShapeDilater implements ClassTraverser {
        
        Grid grid;
        VoxelShape shape; 
        int neighbors[];
        int neighborsIncremented[];
        VoxelChecker voxelChecker;
        int nx, ny, nz;
        int x1 = -1, y1 = -1, z1 = -1;// coordinate of previous processed voxel 

        ShapeDilater(Grid grid, VoxelShape shape, VoxelChecker checker){

            this.grid = grid; 
            this.shape = shape;
            this.voxelChecker = voxelChecker;


            nx = grid.getWidth();
            ny = grid.getHeight();
            nz = grid.getDepth();
            neighbors = shape.getCoords();
            neighborsIncremented = shape.getCoordsIncremented();
            
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
    } //class ShapeDilater


    /**
       checks each of 6-neighbour voxels in gridIn and if it is empty turns ON corresponding voxel in mask
     */
    static class SurfaceFinder implements ClassTraverser {
        
        Grid grid;
        GridBit surface; 
        
        SurfaceFinder(Grid grid, GridBit surface){
            this.grid = grid; 
            this.surface = surface;
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
           turn ON empty voxels in mask 
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
    } // class SurfaceFinder

}
