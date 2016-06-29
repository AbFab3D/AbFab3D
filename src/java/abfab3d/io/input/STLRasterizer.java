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

import abfab3d.core.AttributeGrid;
import abfab3d.core.Grid;
//import abfab3d.grid.GridShortIntervals;
import abfab3d.grid.ArrayAttributeGridByte;


import static java.lang.Math.floor;
import static java.lang.Math.ceil;

import static abfab3d.core.Output.printf;

/**
   class to load STL file and return rasterized grid
 */
public class STLRasterizer {
    public static final int RASTER_METHOD_ZBUFFER = 0;
    public static final int RASTER_METHOD_WAVELET = 1;

    static final double MM = 0.001;
    
    //padding to put around the voxelized model     
    int m_padding = 2;

    double m_voxelSize = 0.1*MM;
    
    Grid m_gridType = new ArrayAttributeGridByte(1,1,1,0.1,0.1);
    private int method = RASTER_METHOD_ZBUFFER;

    // bound of grid to rasterize to 
    double m_gridBounds[] = new double[6];

    public STLRasterizer(){        
        
    }

    public void setVoxelSize(double voxelSize){
        m_voxelSize = voxelSize;
    }

    /**
       set the grid used to generate new grid 
     */
    public void setGridType(Grid grid){
        m_gridType = grid;
    }

    public void setRasterMethod(int method) {
        this.method = method;
    }

    /**
       set padding around the voxelized geometry 
     */
    public void setPadding(int padding){
        m_padding = padding;
    }

    public void setGridBounds(double bounds[]){

        m_gridBounds = new double[bounds.length];
        System.arraycopy(bounds, 0, m_gridBounds, 0, bounds.length);

    }
    

    public Grid rasterizeFile(String path) throws IOException {
        
        printf("STLRasterizer.rasterizeFile(%s)\n",path );
        
        double mbounds[] = new double[6];
        STLReader reader = new STLReader();
        BoundsCalculator bc = new BoundsCalculator();
        reader.read(path, bc);
        bc.getBounds(mbounds);

        printf(" bounds:[(%7.2f %7.2f) (%7.2f %7.2f) (%7.2f %7.2f)] MM \n", mbounds[0]/MM,mbounds[1]/MM,mbounds[2]/MM,mbounds[3]/MM,mbounds[4]/MM,mbounds[5]/MM);

        //getModelBounds(path, mbounds);

        double 
            xmin = mbounds[0], 
            xmax = mbounds[1],
            ymin = mbounds[2], 
            ymax = mbounds[3], 
            zmin = mbounds[4], 
            zmax = mbounds[5];

        // do nice rounding off the model boundaries 
        int 
            nxmin = (int)floor(xmin/m_voxelSize) - m_padding,
            nxmax = (int)ceil(xmax/m_voxelSize) + m_padding,
            nymin = (int)floor(ymin/m_voxelSize) - m_padding,
            nymax = (int)ceil(ymax/m_voxelSize) + m_padding,
            nzmin = (int)floor(zmin/m_voxelSize) - m_padding,
            nzmax = (int)ceil(zmax/m_voxelSize) + m_padding;            
        
        int 
            voxelsX = (nxmax - nxmin),
            voxelsY = (nymax - nymin),
            voxelsZ = (nzmax - nzmin);

        printf("grid: [%d x %d x %d]\n", voxelsX,voxelsY,voxelsZ);
        
        double gbounds[] = m_gridBounds;

        gbounds[0] = nxmin* m_voxelSize;
        gbounds[1] = nxmax* m_voxelSize;
        gbounds[2] = nymin* m_voxelSize;
        gbounds[3] = nymax* m_voxelSize;
        gbounds[4] = nzmin* m_voxelSize;
        gbounds[5] = nzmax* m_voxelSize;

        printf("grid bounds: [%10.7f, %10.7f, %10.7f, %10.7f, %10.7f, %10.7f]\n",gbounds[0],gbounds[1],gbounds[2],gbounds[3],gbounds[4],gbounds[5]);
        printf("grid size: [%10.7f, %10.7f, %10.7f]\n",(gbounds[1]-gbounds[0]),(gbounds[3]-gbounds[2]),(gbounds[5]-gbounds[4]));

        Grid grid;

        if (method == RASTER_METHOD_ZBUFFER) {
            MeshRasterizer mr = new MeshRasterizer(gbounds, voxelsX, voxelsY, voxelsZ);

            reader.read(path, mr);

            grid = m_gridType.createEmpty(voxelsX, voxelsY, voxelsZ, m_voxelSize, m_voxelSize);

            printf("grid: %s \n",grid.getClass().getName());

            grid.setGridBounds(gbounds);

            mr.getRaster(grid);
        } else {

            WaveletRasterizer mr = new WaveletRasterizer(gbounds, voxelsX, voxelsY, voxelsZ);

            // TODO: Do we need to expose this max?
            mr.setMaxAttributeValue(63);

            reader.read(path, mr);

            grid = m_gridType.createEmpty(voxelsX, voxelsY, voxelsZ, m_voxelSize, m_voxelSize);

            printf("grid: %s \n",grid.getClass().getName());

            grid.setGridBounds(gbounds);

            printf("grid bounds: %s \n",grid.getGridBounds());

            mr.getRaster((AttributeGrid)grid);
        }
        return grid;
        
    }
}
