/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
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

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridShort;

import abfab3d.grid.GridShortIntervals;
import abfab3d.grid.GridDataDesc;
import abfab3d.grid.GridDataChannel;


/**
   base class of various distance transforms algorithms
 */
public class DistanceTransform {
    
    // temlate to be used for distance grid creation 
    protected AttributeGrid m_distanceGridTemplate; 
    protected int m_subvoxelResolution = 100; // distance presision per voxel 

    protected double m_inDistance = 0; // max interior distance 
    protected double m_outDistance = 0; // max exterior distance

    /**
       set template to be used for distance grid creation 
     */
    public void setDistanceGridTemplate(AttributeGrid gridTemplate){
        m_distanceGridTemplate = gridTemplate;
    }

    /**
       create distance grid based on dimensions of densityGrid 
     */
    protected AttributeGrid createDistanceGrid(AttributeGrid densityGrid){

        int nx = densityGrid.getWidth();
        int ny = densityGrid.getHeight();
        int nz = densityGrid.getDepth();

        double bounds[] = new double[6];
                
        densityGrid.getGridBounds(bounds);
        double vs = densityGrid.getVoxelSize();
        AttributeGrid distGrid = null;
        if(m_distanceGridTemplate != null) {
            distGrid = (AttributeGrid) m_distanceGridTemplate.createEmpty(nx, ny, nz, vs, vs);
        } else {
            long dataLength = (long)nx * ny * nz;

            if(dataLength >= Integer.MAX_VALUE) {
                distGrid = new GridShortIntervals(nx, ny, nz, vs, vs);
            } else {
                distGrid = new ArrayAttributeGridShort(nx, ny, nz, vs, vs);
            }
        }
        distGrid.setGridBounds(bounds);
        distGrid.setDataDesc(new GridDataDesc(new GridDataChannel(GridDataChannel.DISTANCE, "distance", vs / m_subvoxelResolution, -m_inDistance, m_outDistance)));

        return distGrid;
    }   

}