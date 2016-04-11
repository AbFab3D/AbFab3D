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

package abfab3d.grid.util;

import abfab3d.param.Parameter;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.IntParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.StringParameter;


/**
   class to write grid into bunch of individual image files 
   using given data channel and colorizer 
 */
public class GridDataWriter extends BaseParameterizable {
    
    public static final int 
        TYPE_DENSITY = 1, 
        TYPE_DISTANCE = 2;
       
    protected IntParameter mp_type = new IntParameter("type", TYPE_DISTANCE);
    protected IntParameter mp_magnification = new IntParameter("magnification", 1);
    protected DoubleParameter mp_distanceStep = new DoubleParameter("distanceStep",0.001);
    protected DoubleParameter mp_densityStep = new DoubleParameter("densityStep",0.5);
    protected StringParameter mp_pathFormat = new StringParameter("pathFormat","/tmp/slice%03d.png");
    
    Parameter m_params[] = new Parameter[]{
        mp_type,
        mp_distanceStep,
        mp_densityStep,
        mp_magnification,
        mp_pathFormat,
        
        
    };

    public GridDataWriter(){
        super.addParams(m_params);
    }

    
    
}
