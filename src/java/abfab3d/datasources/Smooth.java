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
import abfab3d.param.DoubleParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.Parameter;
import abfab3d.param.SNodeParameter;
import abfab3d.core.DataSource;
import abfab3d.core.Initializable;
import abfab3d.core.Units;
import abfab3d.core.Vec;

import javax.vecmath.Vector3d;


/**
   Smoothing operation
 
 * @author Alan Hudson
 */
public class Smooth extends TransformableDataSource {
    public static final int PATTERN_1 = 1,PATTERN_4 = 4, PATTERN_6 = 6, PATTERN_7 = 7;
    private static final double SQRT_12 = Math.sqrt(12);

    private DataSource dataSource = null;

    private SNodeParameter mp_data = new SNodeParameter("source");
    private DoubleParameter mp_smoothWidth = new DoubleParameter("smoothWidth","Smoothing width", 1* Units.MM);
    private IntParameter mp_pattern = new IntParameter("pattern","Pattern of the sampling", 0, 0, PATTERN_6);

    Parameter m_aparam[] = new Parameter[]{
        mp_data, mp_smoothWidth,mp_pattern
    };

    private Vector3d[] neigh;

    /**
     * Smoothing of the given datasource.
     * @param source  object to which the complement is generated
     */
    public Smooth(DataSource source, double width, int pattern) {
        super.addParams(m_aparam);
        mp_data.setValue(source);
        dataSource = source;

        setSmoothWidth(width);
        setPattern(pattern);
    }

    /**
     * Sets the width of the smoothing kernel
     */
    public void setSmoothWidth(double sw) {
        if (sw <= 0) throw new IllegalArgumentException("Invalid smooth width: " + sw);
        mp_smoothWidth.setValue(new Double(sw));
    }

    /**
     * Get the width of the smoothing kernel
     */
    public double getSmoothWidth() {
        return mp_smoothWidth.getValue();
    }

    /**
     * Sets the sampling pattern
     */
    public void setPattern(int pattern) {
        if (pattern != PATTERN_1 && pattern != PATTERN_4 && pattern != PATTERN_6 && pattern != PATTERN_7) {
            throw new IllegalArgumentException("Invalid pattern value: " + pattern);
        }
        mp_pattern.setValue(pattern);
    }

    /**
     * Get the width of the smoothing kernel
     */
    public int getPattern() {
        return mp_pattern.getValue();
    }

    /**
     * Get the calculated neighbors array from the pattern
     *
     * @noRefGuide
     * @return
     */
    public Vector3d[] getNeighbors() {
        return neigh;
    }

    /**
     * @noRefGuide
     */
    public int initialize() {

        super.initialize();

        if (dataSource instanceof Initializable) {
            ((Initializable) dataSource).initialize();
        }
        double a;

        switch(mp_pattern.getValue()) {
            case PATTERN_1:
                neigh = new Vector3d[] {
                        new Vector3d(0,0,0)
                };
                break;
            case PATTERN_4:
                a = mp_smoothWidth.getValue() / SQRT_12;

                neigh = new Vector3d[] {
                    new Vector3d(a,a,a),new Vector3d(-a,-a,a), new Vector3d(a,-a,-a), new Vector3d(-a,a,-a)
                };
                break;
            case PATTERN_6:
                a = mp_smoothWidth.getValue() / 2;

                neigh = new Vector3d[] {
                        new Vector3d(-a,0,0),new Vector3d(a,0,0),
                        new Vector3d(0,-a,0), new Vector3d(0,a,0),
                        new Vector3d(0,0,-a), new Vector3d(0,0,a),
                };
                break;
            case PATTERN_7:
                a = mp_smoothWidth.getValue() / 2;

                neigh = new Vector3d[] {
                        new Vector3d(-a,0,0),new Vector3d(a,0,0),
                        new Vector3d(0,-a,0), new Vector3d(0,a,0),
                        new Vector3d(0,0,-a), new Vector3d(0,0,a),
                        new Vector3d(0,0,0)
                };
                break;
            default:
                throw new IllegalArgumentException("Not implemented pattern: " + mp_pattern.getValue());
        }
        return ResultCodes.RESULT_OK;
    }

    /**
     * Get the data value for a pnt
     *
     * @noRefGuide
     */
    public int getDataValue(Vec pnt, Vec data) {

        super.transform(pnt);

        // TODO: garbage, revisit if we care about CPU performance
        Vec spnt = new Vec(3);
        Vec sdata = new Vec(3);
        double d = 0;

        int len = neigh.length;
        for(int i=0; i < len; i++) {
            spnt.set(pnt.v[0]+neigh[i].x,pnt.v[1]+neigh[i].y,pnt.v[2]+neigh[i].z);
            int res = dataSource.getDataValue(spnt, sdata);
            if (res != ResultCodes.RESULT_OK) {
                // bad result in source
                data.v[0] = 1;
                return res;
            }
            d += sdata.v[0];
        }
        data.v[0] = d / len;
        return ResultCodes.RESULT_OK;
    }
} // class Smooth

