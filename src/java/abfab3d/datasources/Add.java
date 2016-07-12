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

import static abfab3d.core.Output.printf;

/**

   Makes sum of 2 data sources, source1 + source2.
   <br/>
   
   @author Vladimir Bulatov

 */
public class Add extends TransformableDataSource {
    
    static final boolean DEBUG = false;
    int debugCount = 100;
    
    DataSource dataSource1;
    DataSource dataSource2;

    SNodeParameter mp_d1 = new SNodeParameter("source1");
    SNodeParameter mp_d2 = new SNodeParameter("source2");

    Parameter m_aparam[] = new Parameter[]{
        mp_d1,
        mp_d2,
    };    

    public Add(){
        super.addParams(m_aparam);
    }
    /**
     * Addition of two data sources.
     */
    public Add(DataSource d1, DataSource d2){

        super.addParams(m_aparam);
        mp_d1.setValue(d1);
        mp_d2.setValue(d2);

    }

    /**
     * Addition of source1 and a constant value.
     */
    public Add(DataSource d1, double d2){

        super.addParams(m_aparam);
        mp_d1.setValue(d1);
        mp_d2.setValue(new Constant(d2));

    }

    /**
     * Addition of a constant value and source2.
     */
    public Add(double d1, DataSource d2){

        super.addParams(m_aparam);
        mp_d1.setValue(new Constant(d1));
        mp_d2.setValue(d2);

    }

    /**
     *  Set source1
     *
     * @param ds  The data source
     */
    public void setSource1(DataSource ds) {
        mp_d1.setValue(ds);
    }

    /**
     * Set source1 to a constant value
     * @param val The constant value
     */
    public void setSource1(double val) {
        mp_d1.setValue(new Constant(val));
    }

    /**
     * Get the first source
     * @return
     */
    public Object getSource1() {
        return mp_d1.getValue();
    }

    /**
     * Set source2 to a constant value
     * @param val The constant value
     */
    public void setSource2(double val) {
        mp_d2.setValue(new Constant(val));
    }

    /**
     *  Set the second source
     *
     * @param ds  data source
     */
    public void setSource2(DataSource ds) {
        mp_d2.setValue(ds);
    }

    /**
     * Get the second source
     * @return
     */
    public Object getSource2() {
        return mp_d2.getValue();
    }

    /**
       @noRefGuide
     */
    public int initialize(){

        super.initialize();
        dataSource1 = (DataSource)mp_d1.getValue();
        dataSource2 = (DataSource)mp_d2.getValue();

        if(dataSource1 instanceof Initializable){
            ((Initializable)dataSource1).initialize();
        }
        if(dataSource2 instanceof Initializable){
            ((Initializable)dataSource2).initialize();
        }
        return ResultCodes.RESULT_OK;
        
    }
    
    /**
     * @noRefGuide
       
     * calculates values of all data sources and return maximal value
     * can be used to make union of few shapes
     */
    public int getBaseValue(Vec pnt, Vec data) {
                
        dataSource2.getDataValue(new Vec(pnt), data);
        double d2 = data.v[0];

        dataSource1.getDataValue(pnt, data);
        
        //if(DEBUG && debugCount-- > 0 )printf("d1: %7.5f d2: %7.5f\n",data.v[0], d2);

        data.v[0] += d2;
        return ResultCodes.RESULT_OK;

    }

} // class Add
