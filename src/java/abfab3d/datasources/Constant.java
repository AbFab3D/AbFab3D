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
import abfab3d.param.Parameter;
import abfab3d.util.Vec;


/**
 * Return constant data value with up to 4 components 
 *
 * @author Vladimir Bulatov
 */
public class Constant extends TransformableDataSource {

    private double m_value0,m_value1,m_value2,m_value3;

    private DoubleParameter mp_value0 = new DoubleParameter("value0", "value of the component 0", 0);
    private DoubleParameter mp_value1 = new DoubleParameter("value1", "value of the component 1", 0);
    private DoubleParameter mp_value2 = new DoubleParameter("value2", "value of the component 2", 0);
    private DoubleParameter mp_value3 = new DoubleParameter("value3", "value of the component 3", 0);

    Parameter m_aparam[] = new Parameter[]{
            mp_value0,
            mp_value1,
            mp_value2,
            mp_value3,
    };

    /**
     * Constant with one component
     */
    public Constant(double value) {
        addParams(m_aparam);
        mp_value0.setValue(value);
        m_channelsCount = 1;        
    }

    /**
       constant with 2 components 
     */
    public Constant(double value0,double value1) {
        addParams(m_aparam);
        mp_value0.setValue(value0);
        mp_value1.setValue(value1);
        m_channelsCount = 2;        
    }

    /**
       constant with 3 components 
     */
    public Constant(double value0,double value1,double value2) {
        addParams(m_aparam);
        mp_value0.setValue(value0);
        mp_value1.setValue(value1);
        mp_value2.setValue(value2);
        m_channelsCount = 3;        
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
        m_channelsCount = 4;        
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
        super.initialize();
        m_value0 = mp_value0.getValue();

        return RESULT_OK;
    }

    /**
     * @noRefGuide 
     */
    public int getDataValue(Vec pnt, Vec data) {

        data.v[0] = m_value0;
        data.v[1] = m_value1;
        data.v[2] = m_value2;
        data.v[3] = m_value3;
        super.getMaterialDataValue(pnt, data);
        return RESULT_OK;
    }

}  // class Constant

