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

   linear mix of two data sources  source1 + (source2 - source1)*mixer
   <br/>
   
   @author Vladimir Bulatov

 */
public class Mix extends TransformableDataSource{
    
    DataSource m_dataSource1;
    DataSource m_dataSource2;
    DataSource m_mixer;

    SNodeParameter mp_d1 = new SNodeParameter("source1");
    SNodeParameter mp_d2 = new SNodeParameter("source2");
    SNodeParameter mp_mixer = new SNodeParameter("mix");

    Parameter m_aparam[] = new Parameter[]{
        mp_d1,
        mp_d2,
        mp_mixer,
    };    

    /**
       
     */
    public Mix(DataSource source1, DataSource source2, DataSource mix){

        super.addParams(m_aparam);
        mp_d1.setValue(source1);
        mp_d2.setValue(source2);
        mp_mixer.setValue(mix);

    }

    /**
       
     */
    public Mix(DataSource source1, DataSource source2, double mix){

        super.addParams(m_aparam);
        mp_d1.setValue(source1);
        mp_d2.setValue(source2);
        mp_mixer.setValue(new Constant(mix));

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
        return RESULT_OK;
        
    }
    
    /**
     * @noRefGuide
       
     * calculates values of all data sources and return maximal value
     * can be used to make union of few shapes
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);
        
        m_mixer.getDataValue(pnt, data);
        double t = data.v[0];

        m_dataSource2.getDataValue(new Vec(pnt), data);
        double d2 = data.v[0];

        m_dataSource1.getDataValue(new Vec(pnt), data);
        double d1 = data.v[0];
        
        data.v[0] *= d1 + (d2-d1)*t;

        //TODO - material 
        return RESULT_OK;

    }

} // class Mix
