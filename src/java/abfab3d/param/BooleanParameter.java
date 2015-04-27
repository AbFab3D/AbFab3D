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

/**
 * A BooleanParameter parameter 
 *
 * @author Vladimir Bulatov
 */
public class BooleanParameter extends NumberParameter {

    public BooleanParameter(String name, String desc, boolean initialValue) {
        super(name, desc);
        defaultValue = initialValue;
        setValue(initialValue);
    }

    @Override
    public Boolean getValue() {
        return (Boolean) value;
    }

    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType() {
        return ParameterType.BOOLEAN;
    }

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
        if (val == null) return;

        if (!(val instanceof Boolean)) {
            throw new IllegalArgumentException("Unsupported type for Boolean: " + val + " in param: " + getName());
        }
    }

    public BooleanParameter clone() {
        return (BooleanParameter) super.clone();
    }
}
