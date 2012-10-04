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

import java.io.IOException;

import javax.vecmath.Vector3d;

import abfab3d.grid.Grid;
import abfab3d.grid.GridShortIntervals;


import abfab3d.util.TriangleCollector;

import static abfab3d.util.Output.printf;

/**
   class to load STL file and return rasterized grid
 */
public class STLRasterizer {
    
    static final double TOMM = 1000;
    
    //padding to put around the voxelized model     
    int m_padding = 2;

    double m_voxelSize = 1.e-4;     

    public STLRasterizer(){
        
        
    }

    public void setVoxelSize(double voxelSize){
        m_voxelSize = voxelSize;
    }

    public void setPadding(int padding){
        m_padding = padding;
    }

    public Grid rasterizeFile(String path) throws IOException {
        
        printf("STLRasterizer.rasterizeFile(%s)\n",path );

        STLReader reader = new STLReader();
        BoundsCalculator bc = new BoundsCalculator();
        reader.read(path, bc);
        // model bounds 
        double mbounds[] = bc.getBounds();
        printf("bounds: [%7.2f, %7.2f; %7.2f,%7.2f; %7.2f,%7.2f]mm\n",
               mbounds[0]*TOMM,mbounds[1]*TOMM,mbounds[2]*TOMM,mbounds[3]*TOMM,mbounds[4]*TOMM,mbounds[5]*TOMM);
        
        int gridX = (int)Math.ceil((mbounds[1] - mbounds[0])/m_voxelSize);
        int gridY = (int)Math.ceil((mbounds[3] - mbounds[2])/m_voxelSize);
        int gridZ = (int)Math.ceil((mbounds[5] - mbounds[4])/m_voxelSize);

        printf("model Grid: [%d x %d x %d]\n",gridX, gridY, gridZ);

        gridX += 2*m_padding;
        gridY += 2*m_padding;
        gridZ += 2*m_padding;

        printf("voxels Grid: [%d x %d x %d]\n",gridX, gridY, gridZ);

        double gbounds[] = new double[]{mbounds[0] - m_padding*m_voxelSize,
                                        mbounds[0] + gridX*m_voxelSize,
                                        mbounds[2] - m_padding*m_voxelSize,
                                        mbounds[2] + gridY*m_voxelSize,
                                        mbounds[4] - m_padding*m_voxelSize,
                                        mbounds[4] + gridZ*m_voxelSize};
                                                        
        MeshRasterizer mr = new MeshRasterizer(gbounds, gridX, gridY, gridZ);
        // read file again 
        reader.read(path, mr);
        
        GridShortIntervals grid = new GridShortIntervals(gridX, gridY, gridZ, m_voxelSize, m_voxelSize);
        
        mr.getRaster(grid);
        
        return grid;
        
    }



    static class BoundsCalculator implements TriangleCollector{
        
        double 
            xmin = Double.MAX_VALUE, xmax = Double.MIN_VALUE, 
            ymin = Double.MAX_VALUE, ymax = Double.MIN_VALUE, 
            zmin = Double.MAX_VALUE, zmax = Double.MIN_VALUE;
 
        public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2){

            addVert(v0);
            addVert(v1);
            addVert(v2);
            return true;
        }

        void addVert(Vector3d v){
            
            if(v.x < xmin) xmin = v.x;
            if(v.x > xmax) xmax = v.x;

            if(v.y < ymin) ymin = v.y;
            if(v.y > ymax) ymax = v.y;

            if(v.z < zmin) zmin = v.z;
            if(v.z > zmax) zmax = v.z;
            
        }

        public double[] getBounds(){

            return new double[]{xmin, xmax, ymin, ymax, zmin, zmax};

        }
        
        
    }
}
