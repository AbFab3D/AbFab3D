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

/**
   base class of various distance transforms algorithms
 */
public class DistanceTransform {
    
    // temlate to be used for distance grid creation 
    protected AttributeGrid m_distanceGridTemplate; 

    /**
       set template to be used for distance grid creation 
     */
    public void setDistanceGridTemplate(AttributeGrid gridTemplate){
        m_distanceGridTemplate = gridTemplate;
    }

    protected AttributeGrid createDistanceGrid(AttributeGrid densityGrid){

        int nx = densityGrid.getWidth();
        int ny = densityGrid.getHeight();
        int nz = densityGrid.getDepth();
        double bounds[] = new double[6];

        densityGrid.getGridBounds(bounds);
        double vs = densityGrid.getVoxelSize();
        AttributeGrid distGrid = null;
        if(m_distanceGridTemplate != null)
            distGrid = (AttributeGrid)m_distanceGridTemplate.createEmpty(nx, ny, nz, vs, vs);
        else 
            distGrid = new ArrayAttributeGridShort(nx, ny, nz, vs, vs);
        distGrid.setGridBounds(bounds);
        return distGrid;
    }   

}