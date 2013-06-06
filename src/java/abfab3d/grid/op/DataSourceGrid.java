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

package abfab3d.grid.op;


import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;

import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;

import abfab3d.util.Output;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;


/**
   
   DataSourceGrid
   
   @author Vladimir Bulatov
   
*/

/**
   
   DataSource interface to Grid
   
*/
public class DataSourceGrid implements DataSource, Initializable {

    static final boolean DEBUG = false;
    static int debugCount = 0;
    
    AttributeGrid m_grid;
    int m_maxAttributeValue;
    double m_bounds[] = new double[6];
    int m_nx, m_ny, m_nz;
    double xmin, ymin, zmin, xscale, yscale, zscale;
        

    public DataSourceGrid(AttributeGrid grid, double bounds[], int maxAttributeValue){
        
        m_maxAttributeValue = maxAttributeValue;
        m_grid = grid;
        if(bounds == null){
            m_grid.getGridBounds(m_bounds);
        } else {
            System.arraycopy(bounds, 0, m_bounds, 0, m_bounds.length);
        }
        
        m_nx = grid.getWidth();
        m_ny = grid.getHeight();
        m_nz = grid.getDepth();
        
        xmin = m_bounds[0];
        ymin = m_bounds[2];
        zmin = m_bounds[4];

        xscale = m_nx / (m_bounds[1] - m_bounds[0]);
        yscale = m_ny / (m_bounds[3] - m_bounds[2]);
        zscale = m_nz / (m_bounds[5] - m_bounds[4]);


        if(DEBUG && debugCount > 0){
            printf("DataSourceGrid()\n");
            printf("nx: (%d x %d x %d) \n", grid.getWidth(),grid.getHeight(),grid.getDepth());
            printf("xmin: (%10.7f,%10.7f,%10.7f) \n", xmin, ymin, zmin);
            printf("xscale: (%10.7f,%10.7f,%10.7f) \n", xscale,yscale,zscale);
        }
    }
    
    public int initialize(){
        
        return RESULT_OK;

    }

    /**
     * returns 1 if pnt is inside of grid
     * returns 0 if pont is poutsid eof grid
     * returns interpolared value near the boundary 
     */
    public int getDataValue(Vec pnt, Vec data) {

        double 
            x = pnt.v[0],
            y = pnt.v[1],
            z = pnt.v[2];
            
        int ix = (int)((x - xmin)*xscale);
        int iy = (int)((y - ymin)*yscale);
        int iz = (int)((z - zmin)*zscale);

        if(DEBUG && debugCount > 0){
            debugCount--;
            //printf("x:[%8.5f %8.5f %8.5f ] -> [%3d, %3d %3d]\n", x,y,z,ix, iy, iz);
        }

        if(ix < 0 || ix >= m_nx){
            data.v[0] = 0.;
            return RESULT_OUTSIDE; 
        }
        if(iy < 0 || iy >= m_ny){
            data.v[0] = 0.;
            return RESULT_OUTSIDE; 
        }
        if(iz < 0 || iz >= m_nz){
            data.v[0] = 0.;
            return RESULT_OUTSIDE; 
        }

        //if(true){
        //    data.v[0] = 1.;
        //    return RESULT_OK;                                
        //}

        switch(m_maxAttributeValue){
            
        case 0:  // grid value is in state 
            byte state = m_grid.getState(ix, iy, iz);
            
            switch(state){
            case Grid.OUTSIDE:
                data.v[0] = 0.;
                return RESULT_OK;                                
            default:
                data.v[0] = 1.;
                return RESULT_OK;                                
            }             

        default:  
            // grid value is in attribute 
            long att = m_grid.getAttribute(ix, iy, iz);
            data.v[0] = ((double)att)/m_maxAttributeValue;
            return RESULT_OK;                
        }

    }
    
}
