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

import org.web3d.util.DoubleToString;

import javax.vecmath.Vector3d;

/**
 * A Vector3d parameter  
 *
 * @author Alan Hudson
 */
public class Vector3dParameter extends NumberParameter {
    public Vector3dParameter(String name) {
        this(name, name, new Vector3d(0, 0, 0));
    }

    public Vector3dParameter(String name, String desc) {
        this(name, desc, new Vector3d(0, 0, 0));
    }
    public Vector3dParameter(String name, Vector3d initialValue) {
        this(name, name, initialValue);
    }

    public Vector3dParameter(String name, String desc, Vector3d initialValue) {
        super(name, desc);
        if(initialValue != null) {
            setValue(initialValue.clone());
            defaultValue = value;
        } else
            setValue(null);            
    }

    @Override
    public Vector3d getValue() {
        return (Vector3d)value;
    }

    public void setValue(Vector3d vec) {
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
        return ParameterType.VECTOR_3D;
    }

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
        if (val == null) return;

        if (!(val instanceof Vector3d)) {
            throw new IllegalArgumentException("Unsupported type for Vector3D: " + val + " in param: " + getName());
        }
    }

    public Vector3dParameter clone() {
        return (Vector3dParameter) super.clone();
    }

    /**
     * Get the string value to use for parameter hashes, append value to existing string builder to lower garbage
     * @return
     */
    public void getParamString(StringBuilder sb) {
        if (value == null) {
            sb.append("[]");
            return;
        }

        Vector3d val = (Vector3d) value;
        sb.append("[");
        DoubleToString.appendFormatted(sb, (Double) val.x, 6);
        sb.append(",");
        DoubleToString.appendFormatted(sb, (Double) val.y, 6);
        sb.append(",");
        DoubleToString.appendFormatted(sb, (Double) val.z, 6);
        sb.append("]");
    }

    public String getParamString() {
        
        StringBuilder sb = new StringBuilder();
        getParamString(sb);
        return sb.toString();
    }
}
