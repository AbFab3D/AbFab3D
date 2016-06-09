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

package abfab3d.io.input;


import javax.vecmath.Vector3d;

import abfab3d.geom.ZBuffer;
import abfab3d.util.TriangleCollector;
import abfab3d.util.Bounds;

import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;

import static abfab3d.util.Output.printf;


/**
   class to convert collection of triangles into a voxel grid

   it uses ZBuffer technique to rasterize each incoming triangle 
   
   it is supposed to be used as follows 

   rasterizer = new MeshRasterizer(bounds, nx, ny, nz);
   for(each triangle in collection){
      rasterizer.addTri();
   }

   AttributeGrid grid = new AttributeGrid(...);
   rasterizer.getRaster(grid);    

   @author Vladimir Bulatov
 */
public class MeshRasterizer implements TriangleCollector {

    int exceptionCount = 100;
    static final boolean DEBUG = false;
    // shift to break possible exact symmetry 
    static final double EPSILON_SHIFT = 1.2345e-10;

    // transformation to transform from world coordinates to voxel cordinates 
    // p_voxel.x = sx * p_world.x + tx    
    double m_sx, m_sy, m_sz;  // scale 
    double m_tx, m_ty, m_tz;  // translation 
    
    // grid size 
    int m_nx, m_ny, m_nz;

    long m_attributeValue=1;

    ZBuffer m_zbuffer; // z-buffer to render trianges to
    

    /**
       construct rasterizer 
       @param bounds rasterization grid bounds
       @param gridX width of grid 
       @param gridY height of grid 
       @param gridZ depth of grid 
     */
    public MeshRasterizer(Bounds bounds, int gridX, int gridY, int gridZ){

        m_zbuffer = new ZBuffer(gridX, gridY, gridZ);
        
        m_nx = gridX;
        m_ny = gridY;
        m_nz = gridZ;
        
        m_sx = gridX/bounds.getSizeX();
        m_sy = gridY/bounds.getSizeY();
        m_sz = gridZ/bounds.getSizeZ();
        
        m_tx = -m_sx*bounds.xmin;
        m_ty = -m_sy*bounds.ymin;
        m_tz = -m_sz*bounds.zmin; 
        
        // break possble symmetry (dirty hack)
        m_tx += EPSILON_SHIFT;
        if(DEBUG){
            printf("MeshRasterizer()\n");
            printf("grid: [%d %d %d]\n", m_nx, m_ny, m_nz);
            printf("sx: [%10.7f, %10.7f, %10.7f]\n", m_sx, m_sy, m_sz);
            printf("tx: [%10.7f, %10.7f, %10.7f]\n", m_tx, m_ty, m_tz);
        }
    }
    
    public MeshRasterizer(double bounds[], int gridX, int gridY, int gridZ){

        this(new Bounds(bounds), gridX, gridY, gridZ);
    }

    /**
       set value used for shape interior
     */
    public void setInteriorValue(long value){
        m_attributeValue = value;

    }
            


    /**
       method of TriangleCollector interface 
       it is called for each triangle in the collection
    */
    public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){

        double 
            x0, y0, z0, 
            x1, y1, z1, 
            x2, y2, z2;

        x0 = m_sx*v0.x+m_tx;
        y0 = m_sy*v0.y+m_ty;
        z0 = m_sz*v0.z+m_tz;

        x1 = m_sx*v1.x+m_tx;
        y1 = m_sy*v1.y+m_ty;
        z1 = m_sz*v1.z+m_tz;

        x2 = m_sx*v2.x+m_tx;
        y2 = m_sy*v2.y+m_ty;
        z2 = m_sz*v2.z+m_tz;
        
        //printf("fillTriangle(%6.1f,%6.1f,%6.1f; %6.1f,%6.1f,%6.1f; %6.1f,%6.1f,%6.1f)\n",x0, y0, z0, x1, y1, z1, x2, y2, z2);
        m_zbuffer.fillTriangle(x0, y0, z0, x1, y1, z1, x2, y2, z2);
        

        return true;

    }

    /**
       the final mandatory step after all rasterization is done
       it stores data from ZBuffer into supplied grid
     */
    public void getRaster(Grid grid){
        
        fillGrid(grid);

    }

    public void getRaster(AttributeGrid grid){
        
        fillGridAttribute(grid);
        
    }
   
    protected void fillGridAttribute(AttributeGrid grid){

        m_zbuffer.sort();
                        
        for(int y = 0; y < m_ny; y++){
            
            for(int x = 0; x < m_nx; x++){
                
                int len = m_zbuffer.getCount(x,y);
                //printf("len: %d %d %d\n", x,y, len);

                if(len < 2)
                    continue;
                
                float zray[] = m_zbuffer.getRay(x,y);
                
                len = (len & 0xFFFE); // make it even 
                
                for(int c = len-2; c >= 0; c-=2 ){
                    // half voxel shift 
                    int z1 = (int)Math.ceil(zray[c] - 0.5);   
                    int z2 = (int)Math.floor(zray[c+1] - 0.5); 
                    fillSegmentAttribute(grid, x,y,z1,z2);
                }
                // release ray memory 
                m_zbuffer.setRay(x,y, null);
            }            
        }        
    }
    
    protected void fillGrid(Grid grid){

        m_zbuffer.sort();
        
        //printf("MeshRasterizer.fillGrid()\n");
                
        for(int y = 0; y < m_ny; y++){
            
            for(int x = 0; x < m_nx; x++){
                
                int len = m_zbuffer.getCount(x,y);
                //printf("len: %d %d %d\n", x,y, len);

                if(len < 2)
                    continue;
                
                float zray[] = m_zbuffer.getRay(x,y);
                
                len = (len & 0xFFFE); // make it even 
                
                for(int c = len-2; c >= 0; c-=2 ){
                    // half voxel shift 
                    int z1 = (int)Math.ceil(zray[c] - 0.5);   
                    int z2 = (int)Math.floor(zray[c+1] - 0.5); 
                    fillSegment(grid, x,y,z1,z2);
                }
                // release ray memory 
                m_zbuffer.setRay(x,y, null);
            }            
        }
    }    
    
    void fillSegment(Grid grid, int x, int y, int z1, int z2){
        int z = 0;
        try {
            for(z = z2; z >= z1; z--){
                grid.setState(x, y, z, Grid.INSIDE);
            }         
        } catch(Exception e){
            if(exceptionCount > 0){
                exceptionCount--;
                printf("index out of bounds: (x: %d, y:%d, z: %d)\n", x,y,z);
            }
        }
    }

    void fillSegmentAttribute(AttributeGrid grid, int x, int y, int z1, int z2){
        int z = 0;
        try {
            for(z = z2; z >= z1; z--){
                grid.setAttribute(x, y, z, m_attributeValue);
            }         
        } catch(Exception e){
            if(exceptionCount > 0){
                exceptionCount--;
                printf("index out of bounds: (x: %d, y:%d, z: %d)\n", x,y,z);
            }
        }
    }
}

