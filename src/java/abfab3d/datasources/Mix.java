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
import abfab3d.param.Parameter;
import abfab3d.param.SNodeParameter;

import abfab3d.core.Vec;
import abfab3d.core.DataSource;
import abfab3d.core.Initializable;

import static java.lang.Math.abs;

import static abfab3d.core.Output.printf;


import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.step10;
import static java.lang.Math.min;


/**

   Linear mix of two data sources  source1 + (source2 - source1)*mixer
   <br/>
   
   @author Vladimir Bulatov

 */
public class Mix extends TransformableDataSource{
    
    static final boolean DEBUG = true;
    static final String dFmt = "%8.4f";

    DataSource m_dataSource1;
    DataSource m_dataSource2;
    DataSource m_mixer;

    int m_dimSource1; // dimension of source1
    int m_dimSource2; // dimension of source2
    int m_dimMix;     // dimension of mix 

    SNodeParameter mp_d1 = new SNodeParameter("source1");
    SNodeParameter mp_d2 = new SNodeParameter("source2");
    SNodeParameter mp_mixer = new SNodeParameter("mix");

    Parameter m_aparam[] = new Parameter[]{
        mp_d1,
        mp_d2,
        mp_mixer,
    };    

    public Mix(){
        super.addParams(m_aparam);        
    }

    public Mix(DataSource source1, DataSource source2, DataSource mix){

        super.addParams(m_aparam);
        mp_d1.setValue(source1);
        mp_d2.setValue(source2);
        mp_mixer.setValue(mix);

    }

    public Mix(DataSource source1, DataSource source2, double mix){

        super.addParams(m_aparam);
        mp_d1.setValue(source1);
        mp_d2.setValue(source2);
        mp_mixer.setValue(new Constant(mix));

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
     *  Set source2
     *
     * @param ds  The data source
     */
    public void setSource2(DataSource ds) {
        mp_d2.setValue(ds);
    }

    /**
     * Set source2 to a constant value
     * @param val The constant value
     */
    public void setSource2(double val) {
        mp_d2.setValue(new Constant(val));
    }

    /**
     * Get the second source
     * @return
     */
    public Object getSource2() {
        return mp_d2.getValue();
    }

    /**
     *  Set mixer
     *
     * @param ds  The data source
     */
    public void setMixer(DataSource ds) {
        mp_mixer.setValue(ds);
    }

    /**
     * Set mixer to a constant value
     * @param val The constant value
     */
    public void setMixer(double val) {
        mp_mixer.setValue(new Constant(val));
    }

    /**
     * Get the mixer
     * @return
     */
    public Object getMixer() {
        return mp_mixer.getValue();
    }

    /**
       @noRefGuide
     */
    public int initialize(){

        super.initialize();

        m_dataSource1 = (DataSource)mp_d1.getValue();
        m_dataSource2 = (DataSource)mp_d2.getValue();
        m_mixer = (DataSource)mp_mixer.getValue();

        if(m_dataSource1 instanceof Initializable){
            ((Initializable)m_dataSource1).initialize();
        }
        if(m_dataSource2 instanceof Initializable){
            ((Initializable)m_dataSource2).initialize();
        }
        if(m_mixer instanceof Initializable){
            ((Initializable)m_mixer).initialize();
        }

        m_dimSource1 = m_dataSource1.getChannelsCount();
        m_dimSource2 = m_dataSource2.getChannelsCount();
        m_dimMix = m_mixer.getChannelsCount();
        if(DEBUG) printf("Mix.initialize() m_dimSource1: %d m_dimSource1:%d m_dimMix:%d\n", m_dimSource1, m_dimSource2, m_dimMix);
        m_channelsCount = Math.max(m_dimSource1, m_dimSource2);
        
        return ResultCodes.RESULT_OK;
        
    }
    
    /**
     * @noRefGuide
       
     * calculates values of all data sources and return maximal value
     * can be used to make union of few shapes
     */
    public int getBaseValue(Vec pnt, Vec data){

        if(m_channelsCount == 1){
            // single channel case 
            m_mixer.getDataValue(new Vec(pnt), data);
            double t = data.v[0];
            
            m_dataSource2.getDataValue(new Vec(pnt), data);
            double d2 = data.v[0];
            
            m_dataSource1.getDataValue(new Vec(pnt), data);
            double d1 = data.v[0];
            
            data.v[0] = d1 + (d2-d1)*t;
            
        } else {
            // general channel count case 
            Vec data1 = new Vec(m_dimSource1);
            Vec data2 = new Vec(m_dimSource2);
            Vec dMix = new Vec(m_dimMix);
            
            m_mixer.getDataValue(new Vec(pnt), dMix);
            m_dataSource1.getDataValue(new Vec(pnt), data1);
            m_dataSource2.getDataValue(new Vec(pnt), data2);

            for(int i = 0; i < m_channelsCount; i++){
                int imix = min(i,m_dimMix-1);
                double t = dMix.v[imix];
                double d1 = (i < m_dimSource1)? data1.v[i]: 0.;
                double d2 = (i < m_dimSource2)? data2.v[i]: 0.;
                data.v[i] = d1 + (d2-d1)*t;
            }
            //if(DEBUG) printf("Mix.getBaseValue() %s -> %s\n", pnt.toString(dFmt), data.toString(dFmt));
        }
        
        return ResultCodes.RESULT_OK;

    }

} // class Mix
