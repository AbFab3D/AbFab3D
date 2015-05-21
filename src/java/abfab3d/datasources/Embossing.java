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

   makes embossing of an surface of the base object with another object 
   <br/>

   <embed src="doc-files/engraving.svg" type="image/svg+xml"/> 

   
   @author Vladimir Bulatov

 */
public class Embossing extends TransformableDataSource implements SNode {
    
    DataSource dataSource1;
    DataSource dataSource2;

    SNodeParameter mp_shape = new SNodeParameter("baseShape");
    SNodeParameter mp_embosser = new SNodeParameter("embosser");
    DoubleParameter mp_blendWidth = new DoubleParameter("blend", "blend width", 0.);
    DoubleParameter mp_minValue = new DoubleParameter("minValue", "min value to emboss", -1*MM);
    DoubleParameter mp_maxValue = new DoubleParameter("maxValue", "max value to emboss", 1*MM);
    DoubleParameter mp_factor = new DoubleParameter("factor", "embosser factor", 1.);
    DoubleParameter mp_offset = new DoubleParameter("offset", "embosser offset", 0.);

    Parameter m_aparam[] = new Parameter[]{
        mp_shape,
        mp_embosser,
        mp_blendWidth,
        mp_minValue,
        mp_maxValue,
        mp_factor,
        mp_offset,
    };    

    /**
       shape which is result of subtracting shape2 from shape1
     */
    public Embossing(DataSource shape, DataSource engraver){

        super.addParams(m_aparam);
        mp_shape.setValue(shape);
        mp_embosser.setValue(engraver);

    }

    public void setBaseShape(DataSource shape) {
        mp_shape.setValue(shape);
    }

    public DataSource getBaseShape() {
        return (DataSource) mp_shape.getValue();
    }

    public void setEmbosser(DataSource engraver) {
        mp_embosser.setValue(engraver);
    }

    public DataSource getEmbosser() {
        return (DataSource) mp_embosser.getValue();
    }

    public void setMinValue(double val) {
        mp_minValue.setValue(val);
    }

    public double getMinValue() {
        return mp_minValue.getValue();
    }

    public void setMaxValue(double val) {
        mp_maxValue.setValue(val);
    }

    public double getMaxValue() {
        return mp_maxValue.getValue();
    }

    public void setFactor(double val) {
        mp_factor.setValue(val);
    }

    public double getFactor() {
        return mp_factor.getValue();
    }

    public void setOffset(double val) {
        mp_offset.setValue(val);
    }

    public double getOffset() {
        return mp_offset.getValue();
    }

    public void setBlend(double val) {
        mp_blendWidth.setValue(val);
    }

    public double getBlend() {
        return mp_blendWidth.getValue();
    }

    /**
       @noRefGuide
     */
    public int initialize(){

        super.initialize();
        dataSource1 = (DataSource)mp_shape.getValue();
        dataSource2 = (DataSource)mp_embosser.getValue();

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
        // TODO implement properly 
        // this currently works as pure subtraction 
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
        return new SNode[] {(SNode)mp_shape.getValue(),(SNode)mp_embosser.getValue()};
    }
} // class Engraving
