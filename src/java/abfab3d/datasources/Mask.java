/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2015
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package abfab3d.datasources;


import abfab3d.core.ResultCodes;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.SNodeParameter;
import abfab3d.core.DataSource;
import abfab3d.core.Initializable;
import abfab3d.core.Vec;

import static abfab3d.core.MathUtil.step10;
import static abfab3d.core.Units.MM;


/**

 Makes a mask out of given data source.
 mask has values in the range (0,1)
 returned value is calculated from source value v as follows:
 <pre>
 if(v < threshold - thickness/2)
 return 1;
 else if(v > threshold + thickness/2)
 return 0;
 else // inside of the transition area
 return (threshold + thickness/2 - v)/thickness;
 </pre>

 The mask is typically used to convert distance functions into density.

 @author Vladimir Bulatov

 */
public class Mask extends TransformableDataSource {

    private DataSource m_dataSource;
    private double m_threshold;
    private double m_thickness2; // half thickness 

    SNodeParameter mp_data = new SNodeParameter("source");
    DoubleParameter mp_threshold = new DoubleParameter("threshold", "mask surface threshold", 0.);
    DoubleParameter mp_thickness = new DoubleParameter("thickness", "mask surface thickness", 0.1 * MM);

    Parameter m_aparam[] = new Parameter[]{
            mp_data,
            mp_threshold,
            mp_thickness,
    };

    public Mask(DataSource source) {
        this(source, 0., 0.1 * MM);
    }

    public Mask(DataSource source, double threshold, double thickness) {

        super.addParams(m_aparam);
        mp_data.setValue(source);
        mp_threshold.setValue(threshold);
        mp_thickness.setValue(thickness);
    }

    /**
     * Set the source mask
     * @param ds  data source
     */
    public void setSource(DataSource ds) {
        mp_data.setValue(ds);
    }

    /**
     * Get the data source
     */
    public DataSource getSource() {
        return (DataSource) mp_data.getValue();
    }

    /**
     * Set the threshold
     * @param val The threshold.  Default is 0.
     */
    public void setThreshold(double val) {
        mp_threshold.setValue(new Double(val));
    }

    /**
     * Get the threshold
     */
    public double getThreshold() {
        return mp_threshold.getValue();
    }

    /**
     * Set the thickness.  Default is 0.1 mm.
     * @param val The value in meters
     */
    public void setThickness(double val) {
        mp_thickness.setValue(new Double(val));
    }

    /**
     * Get the thickness
     */
    public double getThickness() {
        return mp_thickness.getValue();
    }

    /**
     @noRefGuide
     */
    public int initialize() {

        super.initialize();
        m_dataSource = (DataSource) mp_data.getValue();

        if (m_dataSource instanceof Initializable) {
            ((Initializable) m_dataSource).initialize();
        }
        m_threshold = mp_threshold.getValue();
        m_thickness2 = (mp_thickness.getValue()) / 2;


        return ResultCodes.RESULT_OK;

    }

    /**
     * @noRefGuide

     * calculates values of all data sources and return maximal value
     * can be used to make union of few shapes
     */
    public int getDataValue(Vec pnt, Vec data) {

        super.transform(pnt);

        m_dataSource.getDataValue(new Vec(pnt), data);

        data.v[0] = step10(data.v[0], m_threshold, m_thickness2);

        return ResultCodes.RESULT_OK;

    }

} // class Mask
