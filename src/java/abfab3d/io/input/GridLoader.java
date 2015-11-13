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

import abfab3d.util.BoundingBoxCalculator;
import abfab3d.util.Bounds;


import abfab3d.grid.AttributeGrid;
import abfab3d.grid.AttributeChannel;
import abfab3d.grid.AttributeDesc;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.ArrayAttributeGridShort;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;
import static abfab3d.util.MathUtil.getMaxValue;

/**
   class to load mesh or grid from a file into a grid 

   @author Vladiir Bulatov
 */
public class GridLoader {

    static final boolean DEBUG = true;
    

    protected double m_preferredVoxelSize = 0.2*MM;

    protected int m_densityBitCount = 8;
    protected int m_distanceGridBitCount = 16;
    protected double m_margins = 0.2*MM;

    protected long m_maxGridSize = 1000L*1000L*1000L;

    protected AttributeGrid m_densityGridTemplate = new ArrayAttributeGridByte(1,1,1, 10*MM, 10*MM);
    protected AttributeGrid m_distanceGridTemplate = new ArrayAttributeGridShort(1,1,1, 10*MM, 10*MM);

    public static final int RASTERIZER_WAVELET = 1, RASTERIZER_DISTANCE = 2, RASTERIZER_ZBUFFER = 3;
    
    protected int m_densityAlgorithm = RASTERIZER_WAVELET;
    protected int m_distanceAlgorithm = RASTERIZER_DISTANCE;
    protected double m_maxOutDistance = 2*MM;
    protected double m_maxInDistance = 2*MM;

    public GridLoader(){
        
    }

    
    public void setMaxGridSize(long maxGridSize){

        m_maxGridSize = maxGridSize;

    }

    public void setPreferredVoxelSize(double voxelSize){
        m_preferredVoxelSize = voxelSize;
    }

    public void setMaxOutsideDistance(double value){
        m_maxOutDistance = value;
    }

    public void setMaxInsideDistance(double value){
        m_maxInDistance = value;
    }

    public void setDensityAlgorithm(int algorithm){
        switch(algorithm){
        default: throw new IllegalArgumentException(fmt("unknown Density Rasterization Algorithm: %d",algorithm));
        case RASTERIZER_ZBUFFER:
        case RASTERIZER_WAVELET:
        case RASTERIZER_DISTANCE:
            m_densityAlgorithm = algorithm;
            break;
        }
    }

    public void setDistanceAlgorithm(int algorithm){
        switch(algorithm){
        default: throw new IllegalArgumentException(fmt("unknown Density Rasterization Algorithm: %d",algorithm));
        case RASTERIZER_ZBUFFER:
        case RASTERIZER_WAVELET:
        case RASTERIZER_DISTANCE:
            m_distanceAlgorithm = algorithm;
            break;
        }
    }

    public void setDensityBitCount(int bitCount){
        
        m_densityBitCount = bitCount;

    }

    public AttributeGrid loadDistanceGrid(String fileName){
        
        return null;
    }

    public AttributeGrid loadDensityGrid(String filePath){
                
        printf("loadDensityGrid(%s)\n",filePath);

        MeshReader reader = new MeshReader(filePath);
        Bounds bounds = getModelBounds(reader);
        bounds.expand(m_margins);

        double voxelSize = m_preferredVoxelSize;

        bounds.setVoxelSize(voxelSize);
        bounds.roundBounds();
        int ng[] = bounds.getGridSize(voxelSize);
        
        while((long) ng[0] * ng[1]*ng[2] > m_maxGridSize) {                
            //voxelSize = Math.pow(bounds.getVolume()/m_maxGridSize, 1./3);
            voxelSize *= 1.01;
            bounds.setVoxelSize(voxelSize);
            bounds.roundBounds();
            // round up to the nearest voxel
            ng = bounds.getGridSize(voxelSize);
        } 
        printf("actual voxelSize: %7.3fmm\n",voxelSize/MM);

        int nx = ng[0], ny = ng[1], nz = ng[2];

        printf("  grid size: [%d x %d x %d] = %d\n", nx, ny, nz, (long) ng[0] * ng[1]*ng[2]);
        printf("  bounds: [ %8.3f, %8.3f], [%8.3f, %8.3f], [%8.3f, %8.3f] mm; vs: %5.3f mm\n",
               bounds.xmin/MM, bounds.xmax/MM, bounds.ymin/MM, bounds.ymax/MM, bounds.zmin/MM, bounds.zmax/MM, voxelSize/MM);
        
        if(nx < 2 || ny < 2 || nz < 2) throw new IllegalArgumentException(fmt("bad grid size (%d x %d x %d)\n", nx, ny, nz));

        AttributeGrid densityGrid = (AttributeGrid)m_densityGridTemplate.createEmpty(nx, ny, nz, voxelSize, voxelSize);
        densityGrid.setGridBounds(bounds);
        AttributeChannel densityChannel = new AttributeChannel(AttributeChannel.DENSITY, "dens", m_densityBitCount, 0, 0., 1.);
        densityGrid.setAttributeDesc(new AttributeDesc(densityChannel));


        switch(m_densityAlgorithm){
        default: 
            throw new IllegalArgumentException(fmt("unknown Density Rasterization Algorithm: %d",m_densityAlgorithm));
            
        case RASTERIZER_DISTANCE:
            {
                DistanceRasterizer rasterizer = new DistanceRasterizer(bounds, nx, ny, nz);
                rasterizer.setInteriorValue(getMaxValue(m_densityBitCount)); 
                rasterizer.initialize();
                reader.getTriangles(rasterizer);        
                rasterizer.getRaster(densityGrid);                
            }
            break;

        case RASTERIZER_WAVELET:
            {
                WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, nx, ny, nz);
                rasterizer.setInteriorValue(getMaxValue(m_densityBitCount));        
                reader.getTriangles(rasterizer);        
                rasterizer.getRaster(densityGrid);
            }
            break;

        case RASTERIZER_ZBUFFER:
            {
                MeshRasterizer rasterizer = new MeshRasterizer(bounds, nx, ny, nz);
                rasterizer.setInteriorValue(getMaxValue(m_densityBitCount));        
                reader.getTriangles(rasterizer);        
                rasterizer.getRaster(densityGrid);
            }            
        }
        
        return densityGrid;
                
    }

    Bounds getModelBounds(MeshReader meshReader){

        long t0 = time();
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        meshReader.getTriangles(bb);

        if(DEBUG)printf("model read time time: %d ms\n", (time() - t0));
        return bb.getBounds(); 
        
    }

    protected AttributeGrid createDistanceGrid(Bounds bounds){

        
        //int size[] = bounds.getGridSize(m_voxelSize);
        //if(size[0] * size[1] * size[2] > 

        return null;

    }

}