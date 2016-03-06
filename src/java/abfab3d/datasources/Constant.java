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


import abfab3d.param.DoubleParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.Parameter;
import abfab3d.param.BaseParameterizable;

import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Bounds;


/**
 * Return constant data value with up to 4 components 
 *
 * @author Vladimir Bulatov
 */
public class Constant extends BaseParameterizable implements DataSource {

    private double m_value0,m_value1,m_value2,m_value3;
    private int m_dimension = 1;

    private DoubleParameter mp_value0 = new DoubleParameter("value0", "value of the component 0", 0);
    private DoubleParameter mp_value1 = new DoubleParameter("value1", "value of the component 1", 0);
    private DoubleParameter mp_value2 = new DoubleParameter("value2", "value of the component 2", 0);
    private DoubleParameter mp_value3 = new DoubleParameter("value3", "value of the component 3", 0);
    private IntParameter mp_dimension = new IntParameter("dimension", "dimension of constant", 1);

    Parameter m_aparam[] = new Parameter[]{
            mp_dimension,
            mp_value0,
            mp_value1,
            mp_value2,
            mp_value3,
    };

    public Constant() {
        super.addParams(m_aparam);        
    }

    /**
     * Constant with one component
     */
    public Constant(double value) {
        super.addParams(m_aparam);
        mp_value0.setValue(value);
        mp_dimension.setValue(1);
    }

    /**
       constant with 2 components 
     */
    public Constant(double value0,double value1) {
        addParams(m_aparam);
        mp_value0.setValue(value0);
        mp_value1.setValue(value1);
        mp_dimension.setValue(2);     
    }

    /**
       constant with 3 components 
     */
    public Constant(double value0,double value1,double value2) {
        addParams(m_aparam);
        mp_value0.setValue(value0);
        mp_value1.setValue(value1);
        mp_value2.setValue(value2);
        mp_dimension.setValue(3);
    }

    /**
       constant with 4 components 
     */
    public Constant(double value0,double value1,double value2, double value3) {
        addParams(m_aparam);
        mp_value0.setValue(value0);
        mp_value1.setValue(value1);
        mp_value2.setValue(value2);
        mp_value3.setValue(value3);
        mp_dimension.setValue(4);
    }

    /**
     * Set the constant value
     * @param val
     */
    public void setValue(double val) {
        mp_value0.setValue(val);
    }

    /**
     * Get the constant value
     * @return
     */
    public double getValue() {
        return mp_value0.getValue();
    }

    /**
     * @noRefGuide
     */
    public int initialize() {

        m_value0 = mp_value0.getValue();
        m_value1 = mp_value1.getValue();
        m_value2 = mp_value2.getValue();
        m_value3 = mp_value3.getValue();
        m_dimension = mp_dimension.getValue();
        return RESULT_OK;
    }

    /**
     * @noRefGuide 
     */
    public int getDataValue(Vec pnt, Vec data) {
        switch(m_dimension){
        case 4:
            data.v[3] = m_value3;
        case 3:
            data.v[2] = m_value2;
        case 2:
            data.v[1] = m_value1;
        case 1:
            data.v[0] = m_value0;
        }

        return RESULT_OK;
    }

    public Bounds getBounds(){
        return null;
    }

    public void setBounds(Bounds bounds){        
    }
    public int getChannelsCount(){
        return m_dimension;
    }

}  // class Constant

