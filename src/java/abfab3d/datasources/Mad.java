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

   Multiply-accumulate operation.  Calculates: source * a + b
   <br/>
   
   @author Vladimir Bulatov

 */
public class Mad extends TransformableDataSource{
    
    DataSource m_data;
    DataSource m_a;
    DataSource m_b;

    SNodeParameter mp_data = new SNodeParameter("source");
    SNodeParameter mp_a = new SNodeParameter("a");
    SNodeParameter mp_b = new SNodeParameter("b");

    Parameter m_aparam[] = new Parameter[]{
        mp_data,
        mp_a,
        mp_b,
    };    

    public Mad(DataSource source, DataSource a, DataSource b){

        super.addParams(m_aparam);
        mp_data.setValue(source);
        mp_a.setValue(a);
        mp_b.setValue(b);

    }

    public Mad(DataSource source, double a, double b){

        super.addParams(m_aparam);
        mp_data.setValue(source);
        mp_a.setValue(new Constant(a));
        mp_b.setValue(new Constant(b));

    }

    /**
     * Set the value of the A param
     */
    public void setA(DataSource val) {
        mp_a.setValue(val);
    }

    /**
     * Set the value of the A param to a constant
     * @param val The value
     */
    public void setA(double val) {
        mp_a.setValue(new Constant(val));
    }

    /**
     * Set the value of the B param
     */
    public void setB(DataSource val) {
        mp_b.setValue(val);
    }

    /**
     * Set the value of the B param to a constant
     * @param val The value
     */
    public void setB(double val) {
        mp_a.setValue(new Constant(val));
    }

    /**
       @noRefGuide
     */
    public int initialize(){

        super.initialize();
        m_data = (DataSource)mp_data.getValue();
        m_a = (DataSource)mp_a.getValue();
        m_b = (DataSource)mp_b.getValue();

        if(m_data instanceof Initializable){
            ((Initializable)m_data).initialize();
        }
        if(m_a instanceof Initializable){
            ((Initializable)m_a).initialize();
        }
        if(m_b instanceof Initializable){
            ((Initializable)m_b).initialize();
        }

        return ResultCodes.RESULT_OK;
        
    }
    
    /**
     * @noRefGuide
       
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);
        
        m_a.getDataValue(new Vec(pnt), data);
        double a = data.v[0];
        m_b.getDataValue(new Vec(pnt), data);
        double b = data.v[0];

        m_data.getDataValue(pnt, data);
        double d = data.v[0];
        
        data.v[0] = d*a + b;

        //TODO - material 
        return ResultCodes.RESULT_OK;

    }

} // class Mad
