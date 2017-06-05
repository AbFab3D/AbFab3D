/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2017
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.shapejs;

import abfab3d.param.BaseParameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.EnumParameter;
import abfab3d.param.Parameter;

/**
 * Surface Tracing Parameters
 *
 * @author Alan Hudson
 */
public class TracingParams extends BaseParameterizable {
    public enum ModeType {
        DRAFT(4), NORMAL(5), FINE(7), SUPER_FINE(10);

        private int id;

        ModeType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
        
        public static String[] getStringValues() {
            ModeType[] states = values();
            String[] names = new String[states.length];

            for (int i = 0; i < states.length; i++) {
                names[i] = states[i].name();
            }

            return names;
        }
    }

    private EnumParameter mp_mode = new EnumParameter("mode", ModeType.getStringValues(), ModeType.NORMAL.toString());
    private DoubleParameter mp_precision = new DoubleParameter("precision", "How close to the surface", 3e-4);
    private DoubleParameter mp_factor = new DoubleParameter("factor", "Percent of distance to jump", 0.95);

    private Parameter m_aparam[] = new Parameter[]{
        mp_mode,
        mp_precision,
        mp_factor
    };

    public TracingParams() {
        initParams();
    }

    public TracingParams(ModeType mode, double precision, double factor) {
        initParams();

        setMode(mode);
        setPrecision(precision);
        setFactor(factor);
    }

    protected void initParams() {
        super.addParams(m_aparam);
    }

    public ModeType getMode() {
        return ModeType.valueOf(mp_mode.getValue());
    }

    public void setMode(ModeType mode) {
        mp_mode.setValue(mode.toString());
    }

    public double getPrecision() {
        return mp_precision.getValue();
    }

    public void setPrecision(double val) {
        mp_precision.setValue(val);
    }

    public double getFactor() {
        return mp_factor.getValue();
    }

    public void setFactor(double val) {
        mp_factor.setValue(val);
    }
}
