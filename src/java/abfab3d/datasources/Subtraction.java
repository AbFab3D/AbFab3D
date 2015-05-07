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


/**

   Boolean difference between two data sources 
   <br/>

   <embed src="doc-files/Subtraction.svg" type="image/svg+xml"/> 

   
   @author Vladimir Bulatov

 */
public class Subtraction extends TransformableDataSource implements SNode {
    
    DataSource dataSource1;
    DataSource dataSource2;

    SNodeParameter mp_shape1 = new SNodeParameter("shape1");
    SNodeParameter mp_shape2 = new SNodeParameter("shape2");
    DoubleParameter mp_blendWidth = new DoubleParameter("blend", "blend width", 0.);

    Parameter m_aparam[] = new Parameter[]{
        mp_shape1,
        mp_shape2,
        mp_blendWidth,
    };    

    /**
       shape which is result of subtracting shape2 from shape1
     */
    public Subtraction(DataSource shape1, DataSource shape2){
        super.addParams(m_aparam);

        setShape1(shape1);
        setShape2(shape2);
    }

    /**
     * Set the blending width
     */
    public void setBlend(double r){
        mp_blendWidth.setValue(r);
    }

    public void setShape1(DataSource shape1) {
        mp_shape1.setValue(shape1);
    }

    public void setShape2(DataSource shape2) {
        mp_shape2.setValue(shape2);
    }

    /**
       @noRefGuide
     */
    public int initialize(){

        super.initialize();

        dataSource1 = (DataSource)mp_shape1.getValue();
        dataSource2 = (DataSource)mp_shape2.getValue();

        if(dataSource1 != null && dataSource1 instanceof Initializable){
            ((Initializable)dataSource1).initialize();
        }
        if(dataSource2 != null && dataSource2 instanceof Initializable){
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
        double v1 = 0, v2 = 0;
        
        int res = dataSource1.getDataValue(new Vec(pnt), data);
        if(res != RESULT_OK){
            data.v[0] = 0.0;
            return res;
        }
        
        v1 = data.v[0];
        
        if(v1 <= 0.){
            data.v[0] = 0.0;
            return RESULT_OK;
        }
        
        // we are here if v1 > 0
        
        res = dataSource2.getDataValue(new Vec(pnt), data);
        
        if(res != RESULT_OK){
            data.v[0] = v1;
            return RESULT_OK;
        }
        
        v2 = data.v[0];
        if(v2 >= 1.){
            data.v[0] = 0.;
            return RESULT_OK;
        }
        data.v[0] = v1*(1-v2);
        
        return RESULT_OK;
    }

    @Override
    public SNode[] getChildren() {
        return new SNode[] {(SNode)mp_shape1.getValue(),(SNode)mp_shape2.getValue()};
    }
} // class Subtraction
