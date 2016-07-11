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

import abfab3d.core.Color;

import javax.vecmath.Vector3d;

/**
 * A Color parameter
 *
 * @author Alan Hudson
 */
public class ColorParameter extends BaseParameter {
    public ColorParameter(String name) {
        this(name, name, new Color(0, 0, 0));
    }

    public ColorParameter(String name, String desc) {
        this(name, desc, new Color(0, 0, 0));
    }
    public ColorParameter(String name, Color initialValue) {
        this(name, name, initialValue);
    }
    public ColorParameter(String name, String desc, String initialValue) {
        super(name, desc);
        Color c = Color.fromHEX(initialValue);
        setValue(c);
        defaultValue = value;
    }

    public ColorParameter(String name, String desc, Color initialValue) {
        super(name, desc);
        if(initialValue != null) {
            setValue(initialValue.clone());
            defaultValue = value;
        } else
            setValue(null);            
    }

    @Override
    public Color getValue() {
        return (Color)value;
    }

    public double getr() {
        return ((Color)value).getr();
    }

    public double getg() {
        return ((Color)value).getg();
    }

    public double getBlue() {
        return ((Color)value).getb();
    }

    public void setValue(Color vec) {
        if (vec != null) {
            super.setValue(vec.clone());
        } else {
            super.setValue(vec);
        }
    }

    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType() {
        return ParameterType.COLOR;
    }

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
        if (val == null) return;

        if (!(val instanceof Color)) {
            throw new IllegalArgumentException("Unsupported type for Color: " + val + " in param: " + getName());
        }
    }

    public ColorParameter clone() {
        return (ColorParameter) super.clone();
    }
}
