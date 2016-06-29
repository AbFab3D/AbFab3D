/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
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
import abfab3d.core.Vec;
import abfab3d.core.DataSource;
import abfab3d.core.MathUtil;

import abfab3d.param.Parameter;
import abfab3d.param.SNode;
import abfab3d.param.SNodeParameter;
import abfab3d.param.DoubleParameter;

import static java.lang.Math.abs;

import static abfab3d.core.Output.printf;
import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.step10;

import static abfab3d.core.Units.MM;


/**

   Makes an embossing on a surface of the base shape with another source.  This is similar to displacement maps in other
   packages.  In general this technique only works well close to the surface, but can be good anywhere. 

   <br/>

   

   @author Vladimir Bulatov

 */
public class Embossing extends TransformableDataSource implements SNode {
    

    SNodeParameter mp_shape = new SNodeParameter("baseShape");
    SNodeParameter mp_embosser = new SNodeParameter("embosser");
    DoubleParameter mp_blendWidth = new DoubleParameter("blend", "blend width", 0.);
    DoubleParameter mp_minValue = new DoubleParameter("minValue", "min value to emboss", -1*MM);
    DoubleParameter mp_maxValue = new DoubleParameter("maxValue", "max value to emboss", 1*MM);
    DoubleParameter mp_factor = new DoubleParameter("factor", "multipier for embosser value", 1.);
    DoubleParameter mp_offset = new DoubleParameter("offset", "offset for embosser value", 0.);
    DoubleParameter mp_mixThreshold = new DoubleParameter("mixThreshold", "threshold for embosser value for attributes mixing", 0.1);
    DoubleParameter mp_mixAmount = new DoubleParameter("mixAmount", "amount of attributes mixing", 0);
    DoubleParameter mp_mixBlend = new DoubleParameter("mixBlend", "width of attributes mixing transition", 0.1);

    Parameter m_aparam[] = new Parameter[]{
        mp_shape,
        mp_embosser,
        mp_blendWidth,
        mp_minValue,
        mp_maxValue,
        mp_factor,
        mp_offset,
        mp_mixAmount,
        mp_mixThreshold,
        mp_mixBlend,
    };    


    /**
       constructor for shape and embosser
     */
    public Embossing(DataSource shape, DataSource embosser){

        super.addParams(m_aparam);
        mp_shape.setValue(shape);
        mp_embosser.setValue(embosser);

    }

    /**
     * Set the base shape to emboss onto
     * @param shape
     */
    public void setBaseShape(DataSource shape) {
        mp_shape.setValue(shape);
    }

    /**
     * Get the base shape to emboss onto
     * @return
     */
    public DataSource getBaseShape() {
        return (DataSource) mp_shape.getValue();
    }

    /**
     * Set the data source to emboss onto the base shape
     * @param embosser The source
     */
    public void setEmbosser(DataSource embosser) {
        mp_embosser.setValue(embosser);
    }

    /**
     * Get the data source to emboss onto the base shape
     */
    public DataSource getEmbosser() {
        return (DataSource) mp_embosser.getValue();
    }

    /**
     * Set the minimum value from the base shape that the embossing will go
     * @param val The value
     */
    public void setMinValue(double val) {
        mp_minValue.setValue(val);
    }

    /**
     * Get the minimum value from the base shape that the embossing will go
     */
    public double getMinValue() {
        return mp_minValue.getValue();
    }

    /**
     * Set the minimum value from the base shape that the embossing will go
     * @param val The value
     */
    public void setMaxValue(double val) {
        mp_maxValue.setValue(val);
    }

    /**
     * Get the minimum value from the base shape that the embossing will go
     */
    public double getMaxValue() {
        return mp_maxValue.getValue();
    }

    /**
     * Set the multiplication factor for the embossing.
     * @param val The value
     */
    public void setFactor(double val) {
        mp_factor.setValue(val);
    }

    /**
     * Get the multiplication factor for the embossing.
     */
    public double getFactor() {
        return mp_factor.getValue();
    }

    /**
     * Set the offset for the embossing.
     * @param val The value
     */
    public void setOffset(double val) {
        mp_offset.setValue(val);
    }

    /**
     * Get the offset for the embossing.
     */
    public double getOffset() {
        return mp_offset.getValue();
    }

    /**
     * Set the blending value of the base shape with the embossing
     * @param val The value in meters
     */
    public void setBlend(double val) {
        mp_blendWidth.setValue(val);
    }

    /**
     * Get the blending value of the base shape with the embossing
     */
    public double getBlend() {
        return mp_blendWidth.getValue();
    }

    // private variables for used in calculation 
    
    private TransformableDataSource m_baseShape;
    private TransformableDataSource m_embosser;
    private double m_minValue; 
    private double m_maxValue; 
    private double m_factor; 
    private double m_offset; 
    private int m_embosserChannelsCount;
    private int m_baseChannelsCount;
    private double m_mixFactor;
    private double m_mixThreshold;
    private double m_mixAmount;
    /**
       @noRefGuide
     */
    public int initialize(){

        super.initialize();
        m_baseShape = (TransformableDataSource)mp_shape.getValue();
        m_embosser = (TransformableDataSource)mp_embosser.getValue();
        super.initializeChild(m_baseShape);
        super.initializeChild(m_embosser);

        m_minValue = mp_minValue.getValue();
        m_maxValue = mp_maxValue.getValue();
        m_factor = mp_factor.getValue();
        m_offset = mp_offset.getValue();


        m_embosserChannelsCount = m_embosser.getChannelsCount();
        m_baseChannelsCount = m_baseShape.getChannelsCount();

        m_channelsCount = m_baseChannelsCount;
        m_mixThreshold = (m_maxValue - m_minValue)*mp_mixThreshold.getValue();
        m_mixFactor = 1/(mp_mixBlend.getValue()*(m_maxValue - m_minValue));
        m_mixAmount = mp_mixAmount.getValue();
        //printf("th: %10.5f mf: %10.5f\n", th, mf);
        return ResultCodes.RESULT_OK;
        
    }
    
    /**
     * @noRefGuide
       
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);
                
        Vec embData = new Vec(m_embosserChannelsCount);

        m_baseShape.getDataValue(pnt, data);
        m_embosser.getDataValue(pnt, embData);
        // embossing distance 
        double dist = embData.v[0]*m_factor + m_offset;
        dist = clamp(dist, m_minValue, m_maxValue);
        data.v[0] -= dist; // emboss 
        double mix = ((dist-m_minValue) - m_mixThreshold)*m_mixFactor;
        mix = m_mixAmount*clamp(mix,0.,1.);
        lerp(data.v,embData.v, mix, data.v, 1,m_baseChannelsCount-1);                       
        return ResultCodes.RESULT_OK;
    }

    static void lerp(double v0[], double v1[], double t, double vout[], int startIndex, int count){
        for(int i = 0; i < count; i++){
            int index = i + startIndex;
            vout[index] = MathUtil.lerp(v0[index], v1[index], t); 
        }
    }

} // class Embossing
