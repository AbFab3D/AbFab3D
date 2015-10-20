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
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;

import static abfab3d.grid.Grid.OUTSIDE;

/**
 * Erode an object with given custom shape
 * 
 * 
 *  1) find outside surface voxels of the grid 
 *  2) erode grid with VoxelShape running over surface voxels 
 *
 * @author Vladimir Bulatov
 */
public class ErosionShape implements Operation, AttributeOperation {

    public static int sm_debug = 0;
	
    VoxelChecker m_voxelChecker; // external tester which checks if voxel needs to be processed
    VoxelShape m_voxelShape;  // shape used to perform dilation     
    
    public ErosionShape() {
        
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
        throw new IllegalArgumentException(fmt("ErosionShape.execute(%d) not implemented!\n", grid));  

    }

    
    public AttributeGrid execute(AttributeGrid grid) {

        printf("ErosionShape.execute(%s)\n", grid); 
        
        GridBitIntervals m_surface = new GridBitIntervals(grid.getWidth(), grid.getHeight(), grid.getDepth());
        long t0 = time();
        grid.find(VoxelClasses.INSIDE, new SurfaceFinder(grid, m_surface));
        printf("surface: %d ms\n", (time()-t0));
        t0 = time();
        m_surface.find(VoxelClasses.INSIDE, new ShapeEroder(grid, m_voxelShape, m_voxelChecker));
        printf("erosion: %d ms\n", (time()-t0));
        
        m_surface.release();
        m_surface = null;
        return grid;
    }

    /**
       dilation of surface voxels with given shape
     */
    static class ShapeEroder implements ClassTraverser {
        
        Grid grid;
        VoxelShape shape; 
        int neighbors[];
        int neighborsIncremented[];
        VoxelChecker voxelChecker;
        int nx, ny, nz;
        int x1 = -1, y1 = -1, z1 = -1;// coordinate of previous processed voxel 

        ShapeEroder(Grid grid, VoxelShape shape, VoxelChecker checker){

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
    } //class ShapeEroder


    /**
       checks each of 6-neighbour voxels in gridIn and if it is empty turn ON that voxel in the mask
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

            //TODO - add grid bounds checker 

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
    } // class SurfaceFinder

}
