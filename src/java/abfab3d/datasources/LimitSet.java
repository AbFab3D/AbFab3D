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


//import java.awt.image.Raster;


import abfab3d.core.ResultCodes;
import abfab3d.core.Vec;

import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;

import static java.lang.Math.abs;

import static abfab3d.core.Output.printf;


import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.step10;


/**

   class to return neighborhood of a limit set of symmetry group. 
   These are area where 1/scaleFactor is close to 0. 

   @author Vladimir Bulatov
   
*/

public class LimitSet  extends TransformableDataSource {
    
    final boolean DEBUG = false;
    int debugCount = 100;

    private DoubleParameter  mp_offset = new DoubleParameter("offset","distance offset", 0.);
    private DoubleParameter  mp_factor = new DoubleParameter("factor","scaling factor", 1);

    Parameter aparam[] = new Parameter[]{
        mp_offset,
        mp_factor,
    };

    private double m_offset = 0;
    private double m_factor = 1;
    


    public LimitSet(double offset, double factor){

        super.addParams(aparam);

        mp_offset.setValue(offset);
        mp_factor.setValue(factor);
        
    }

    public int initialize(){

        super.initialize();
        m_offset = mp_offset.getValue();
        m_factor = mp_factor.getValue();
        return ResultCodes.RESULT_OK;

    }
    
    /**
     limit set distance is calculated as   m_factor/pnt.scaleFactor - m_offset 
    */
    public int getBaseValue(Vec pnt, Vec data) {

        double dist = m_factor/pnt.getScaleFactor()-m_offset;

        data.v[0] = getShapeValue(dist, pnt);
        
        return ResultCodes.RESULT_OK;
        
    }
    
}  // class LimitSet

