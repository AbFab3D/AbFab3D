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


import abfab3d.param.Parameter;
import abfab3d.param.SNode;
import abfab3d.param.SNodeParameter;
import abfab3d.param.DoubleParameter;

import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;

import static java.lang.Math.abs;

import static abfab3d.util.Output.printf;


import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.step10;

import static abfab3d.util.Units.MM;


/**

   makes mask out of given data source 
   mask has values in the range (0,1) 
   returned value is calculated from data value v as follows:
   <pre>
   if(v < threshod - thickness/2) 
      return 1;
   else if(v < threshod + thickness/2) 
      return 0;
   else 
       return (threshod + thickness/2 - v)/thickness;
   </pre>
   
   the mask can be used to converet distance function into density 
   
   @author Vladimir Bulatov

 */
public class Mask extends TransformableDataSource {
    
    private DataSource m_dataSource;
    private double m_threshold;
    private double m_thickness2; // half thickness 

    DataSource dataSource2;

    SNodeParameter mp_data = new SNodeParameter("data");
    DoubleParameter mp_threshold = new DoubleParameter("threshold", "mask surface threshold", 0.);
    DoubleParameter mp_thickness = new DoubleParameter("thickness", "mask surface thickness", 0.1*MM);

    Parameter m_aparam[] = new Parameter[]{
        mp_data,
        mp_threshold,
        mp_thickness,
    };    

    /**
       
     */
    public Mask(DataSource data, double threshold, double thickness){

        super.addParams(m_aparam);
        mp_data.setValue(data);
        mp_threshold.setValue(threshold);
        mp_thickness.setValue(thickness);
    }

    /**
       @noRefGuide
     */
    public int initialize(){

        super.initialize();
        m_dataSource = (DataSource)mp_data.getValue();

        if(m_dataSource instanceof Initializable){
            ((Initializable)m_dataSource).initialize();
        }
        m_threshold = mp_threshold.getValue();
        m_thickness2 = (mp_thickness.getValue())/2;
        
        
        return RESULT_OK;
        
    }
    
    /**
     * @noRefGuide
       
     * calculates values of all data sources and return maximal value
     * can be used to make union of few shapes
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);
        
        m_dataSource.getDataValue(new Vec(pnt), data);

        data.v[0] = step10(data.v[0], m_threshold, m_thickness2);

        return RESULT_OK;

    }

} // class Mul
