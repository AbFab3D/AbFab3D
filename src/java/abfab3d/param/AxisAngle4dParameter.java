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
package abfab3d.param;

// External Imports

import javax.vecmath.AxisAngle4d;

/**
 * A AxisAngle4d parameter to a service.
 *
 * @author Alan Hudson
 */
public class AxisAngle4dParameter extends NumberParameter {
    public AxisAngle4dParameter(String name) {
        this(name, name, new AxisAngle4d(0, 1, 0,0));
    }

    public AxisAngle4dParameter(String name, String desc) {
        this(name, desc, new AxisAngle4d(0, 1, 0,0));
    }

    public AxisAngle4dParameter(String name, String desc, AxisAngle4d initialValue) {
        super(name, desc);
        defaultValue = initialValue;
        setValue(initialValue);
    }

    @Override
    public AxisAngle4d getValue() {
        return (AxisAngle4d) value;
    }

    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType() {
        return ParameterType.AXIS_ANGLE_4D;
    }

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
        if (val == null) return;

        if (!(val instanceof AxisAngle4d)) {
            throw new IllegalArgumentException("Unsupported type for Vector3D: " + val + " in param: " + getName());
        }
    }

    public AxisAngle4dParameter clone() {
        return (AxisAngle4dParameter) super.clone();
    }
}
