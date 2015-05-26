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

   makes sum of 2 data sources
   <br/>
   
   @author Vladimir Bulatov

 */
public class Add extends TransformableDataSource {
    
    DataSource dataSource1;
    DataSource dataSource2;

    SNodeParameter mp_d1 = new SNodeParameter("data1");
    SNodeParameter mp_d2 = new SNodeParameter("data2");

    Parameter m_aparam[] = new Parameter[]{
        mp_d1,
        mp_d2,
    };    

    /**
       
     */
    public Add(DataSource d1, DataSource d2){

        super.addParams(m_aparam);
        mp_d1.setValue(d1);
        mp_d2.setValue(d2);

    }

    /**
       
     */
    public Add(DataSource d1, double d2){

        super.addParams(m_aparam);
        mp_d1.setValue(d1);
        mp_d2.setValue(new Constant(d2));

    }

    /**
       
     */
    public Add(double d1, DataSource d2){

        super.addParams(m_aparam);
        mp_d1.setValue(new Constant(d1));
        mp_d2.setValue(d2);

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
        return RESULT_OK;
        
    }
    
    /**
     * @noRefGuide
       
     * calculates values of all data sources and return maximal value
     * can be used to make union of few shapes
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);
        
        dataSource2.getDataValue(new Vec(pnt), data);
        double d2 = data.v[0];

        dataSource1.getDataValue(pnt, data);
        
        data.v[0] += d2;

        return RESULT_OK;

    }

} // class Add
