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
import abfab3d.param.Parameter;
import abfab3d.param.SNodeParameter;

import abfab3d.core.Vec;
import abfab3d.core.DataSource;
import abfab3d.core.Initializable;

import static java.lang.Math.abs;

import static abfab3d.core.Output.printf;


import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.step10;


/**

   Return the absolute value of a data source.
   <br/>
   
   @author Vladimir Bulatov

 */
public class Abs extends TransformableDataSource {
    
    DataSource m_data;

    SNodeParameter mp_data = new SNodeParameter("source", ShapesFactory.getInstance());

    Parameter m_aparam[] = new Parameter[]{
        mp_data,
    };    

    public Abs(){
        super.addParams(m_aparam);        
    }

    /**
     * Create an Abs of a data source
     */
    public Abs(DataSource source){

        super.addParams(m_aparam);
        mp_data.setValue(source);

    }

    /**
     *  Set the source
     *
     * @param ds  The data source
     */
    public void setSource(DataSource ds) {
        mp_data.setValue(ds);
    }

    /**
     * Get the source
     * @return
     */
    public Object getSource() {
        return mp_data.getValue();
    }

    /**
       @noRefGuide
     */
    public int initialize(){

        super.initialize();
        m_data = (DataSource)mp_data.getValue();

        if(m_data instanceof Initializable){
            ((Initializable)m_data).initialize();
        }

        return ResultCodes.RESULT_OK;
        
    }
    
    /**
     * @noRefGuide
       
     * calculates values of all data sources and return maximal value
     * can be used to make union of few shapes
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);
        
        m_data.getDataValue(new Vec(pnt), data);
        data.v[0] = Math.abs(data.v[0]);

        return ResultCodes.RESULT_OK;

    }

} // class Abs
