/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.datasources;


import abfab3d.core.ResultCodes;
import abfab3d.core.Vec;

import abfab3d.core.AttributeGrid;

import static abfab3d.core.Output.printf;


/**
   
   
   DataSource interface to GridMask. 
   This object should be uses as mask to intersect with smooth grid. 
   it returns interior density for any non zero attribute in voxel or it's neighbors

   @author Vladimir Bulatov
   
   
*/
public class DataSourceGridMask extends TransformableDataSource {

    static final boolean DEBUG = false;
    static int debugCount = 100;

    AttributeGrid m_grid;
    double m_bounds[] = new double[6];
    int m_nx, m_ny, m_nz;
    double xmin, ymin, zmin, xscale, yscale, zscale;

    /**
       makes grid with given bounds and max attribute value
    */
    public DataSourceGridMask(AttributeGrid grid){
        
        m_grid = grid;
        m_grid.getGridBounds(m_bounds);
        
        m_nx = grid.getWidth();
        m_ny = grid.getHeight();
        m_nz = grid.getDepth();
        
        xmin = m_bounds[0];
        ymin = m_bounds[2];
        zmin = m_bounds[4];

        xscale = m_nx / (m_bounds[1] - m_bounds[0]);
        yscale = m_ny / (m_bounds[3] - m_bounds[2]);
        zscale = m_nz / (m_bounds[5] - m_bounds[4]);

    }

    /**
     * returns 1 if pnt is inside of grid
     * returns 0 if pont is poutsid eof grid
     * returns interpolared value near the boundary 
      @noRefGuide            
     */
    public int getDataValue(Vec pnt, Vec data) {

        super.transform(pnt);
                                                                
        getBoxInterpolatedValue(pnt, data);
        
        super.getMaterialDataValue(pnt, data);        
        return ResultCodes.RESULT_OK;
        
    }

    private int getBoxInterpolatedValue(Vec pnt, Vec data){

        double v[] = pnt.v;
        // values normalized to voxel size 
        double 
            x = (v[0]-xmin)*xscale,
            y = (v[1]-ymin)*yscale,
            z = (v[2]-zmin)*zscale;
        int ix = (int)x;
        int iy = (int)y;
        int iz = (int)z;

        data.v[0] = getGridValue(ix,  iy,  iz );
        return ResultCodes.RESULT_OK;
            
    }


    private final double getGridValue(int x, int y, int z){

        
        if(m_grid.getAttribute(x, y, z) != 0) return 1.;
        if(x+1 < m_nx && m_grid.getAttribute(x+1, y, z) != 0) return 1.;
        if(x-1 >=0 && m_grid.getAttribute(x-1, y, z) != 0) return 1.;
        if(y+1 < m_ny && m_grid.getAttribute(x, y+1, z) != 0) return 1.;
        if(y-1 >=0 && m_grid.getAttribute(x, y-1, z) != 0) return 1.;
        if(z+1 < m_nz && m_grid.getAttribute(x, y, z+1) != 0) return 1.;
        if(z-1 >=0 && m_grid.getAttribute(x, y, z-1) != 0) return 1.;

        return 0.;

    }
    
}
