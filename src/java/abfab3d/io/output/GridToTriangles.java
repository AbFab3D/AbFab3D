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

package abfab3d.io.output;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector3d;

import abfab3d.grid.Grid;
import abfab3d.grid.ClassTraverser;
import abfab3d.grid.VoxelClasses;
import abfab3d.util.TriangleCollector;


/**
   
   convert voxels of the grid into triagles (2 per exposed square face) 
   
   uses TriangleCollector interface to pass triangles to 

   @author Vladimir Bulatov
   

 */
public class GridToTriangles implements ClassTraverser {
    
    Grid grid;
    int triCount = 0;
    int nx1, ny1, nz1;
    
    Vector3d // normals to the sides 
        wx0 = new Vector3d(-1,0,0),
        wx1 = new Vector3d(1,0,0),
        wy0 = new Vector3d(0,-1,0),
        wy1 = new Vector3d(0,1,0),
        wz0 = new Vector3d(0,0,-1),
        wz1 = new Vector3d(0,0,1);
    
    Vector3d v[] = new Vector3d[8];
    //double scale = 1000;// to convert to millimeters 
    double offset = 0.;
    double x0, y0, z0, dx, dy, dz;
    TriangleCollector out;
    
    /**
       
     */
    public GridToTriangles(){            
        
        for(int i = 0; i < v.length; i++){
            v[i] = new Vector3d(0,0,0);
        }        
        
    }

    public void execute(Grid grid, double bounds[], TriangleCollector out){
        
        this.grid = grid;            
        nx1 = grid.getWidth()-1;
        ny1 = grid.getHeight()-1;
        nz1 = grid.getDepth()-1;

        x0 = bounds[0];
        y0 = bounds[2];
        z0 = bounds[4];
        dx = (bounds[1] - bounds[0])/grid.getWidth();
        dy = (bounds[3] - bounds[2])/grid.getHeight();
        dz = (bounds[5] - bounds[4])/grid.getDepth();
        this.out = out;
                
        // traverse the grid with triMaker 
        grid.findInterruptible(VoxelClasses.INSIDE, this);
        
    }
    

    void addTri(Vector3d normal, Vector3d v[], int ind0, int ind1, int ind2 ){
        
        out.addTri(v[ind0],v[ind1],v[ind2]);
        
    }
    
    
    public void found(int x, int y, int z, byte _state){
        
        foundInterruptible(x,y,z,_state);
        
    }
    
    public boolean foundInterruptible(int x, int y, int z, byte _state) {
        
        //  
        //       4+-------4----------+5
        //       /|                 /|                                     
        //      7 |                5 |                                      
        //     /  |               /  |                                      
        //   7+---------6--------+6  9                                                         
        //    |   |8             |   |                                     
        //   11   |             10   |                                     
        //    |  0+-------0------|---+1
        //    |  / x,y,z         |  /                                     
        //    | 3                | 1                                        
        //    |/                 |/                                        
        //   3+--------2---------+2
        //              
        double yy, yy1, xx, xx1, zz, zz1;
        
        yy = (y0 + (y - offset)* dy);
        yy1 = (yy + dy);
        
        xx = (x0 + (x-offset)*dx);
        xx1 = (xx + dx);
        
        zz = (z0 + (z-offset)*dz);
        zz1 = (zz + dz);
        
        v[0].set(xx,yy,zz);
        v[1].set(xx1,yy,zz);
        v[2].set(xx1,yy,zz1);
        v[3].set(xx,yy,zz1);
        v[4].set(xx,yy1,zz);
        v[5].set(xx1,yy1,zz);
        v[6].set(xx1,yy1,zz1);
        v[7].set(xx,yy1,zz1);
        
        try {
            if(x < nx1 && grid.getState(x+1,y,z) == Grid.OUTSIDE){
                addTri(wx1, v, 6,2,1);
                addTri(wx1, v, 6,1,5);
            }
            if(x > 0 && grid.getState(x-1,y,z) == Grid.OUTSIDE){
                addTri(wx0, v, 7,4,0);
                addTri(wx0, v, 7,0,3);                        
            }
            if(z < nz1 && grid.getState(x,y,z+1) == Grid.OUTSIDE){
                addTri(wz1, v, 7,3,2);                        
                addTri(wz1, v, 7,2,6);                        
                
            }
            if(z > 0 && grid.getState(x,y,z-1) == Grid.OUTSIDE){
                addTri(wz0, v, 4,5,1);                        
                addTri(wz0, v, 4,1,0);                        
            }
            if(y < ny1 && grid.getState(x,y+1,z) == Grid.OUTSIDE){
                addTri(wy1, v, 4,7,6);                        
                addTri(wy1, v, 4,6,5);                                                
            }
            if(y > 0 && grid.getState(x,y-1,z) == Grid.OUTSIDE){
                addTri(wy0, v, 0,1,2);                        
                addTri(wy0, v, 0,2,3);                        
            }
            
            return true;
            
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }

    }                    
    
} // class GridToTrinagles
