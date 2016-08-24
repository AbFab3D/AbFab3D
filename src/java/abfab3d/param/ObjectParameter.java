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
import static abfab3d.core.Output.printf;

/**
 * A ObjectParameter parameter  to hold generic object
 *
 * @author Vladimir Bulatov
 */
public class ObjectParameter extends BaseParameter {

    public ObjectParameter(String name, String desc, Object initialValue) {
        super(name, desc);
        setValue(initialValue);
    }

    @Override
    public Object getValue() {
        return value;
    }

    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType() {
        return ParameterType.OBJECT;
    }

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
        //anything good, can be subclassed to be more selective 
    }

    public ObjectParameter clone() {
        return (ObjectParameter) super.clone();
    }

    @Override
    public String getParamString() {

        if (value instanceof SourceWrapper) {
            return ((SourceWrapper)value).getParamString();
        }

        if (value instanceof ExpensiveInitializable) {
            return ((ExpensiveInitializable)value).getParamString();
        }

        if (value == null) return "null";

        if (value instanceof String) {
            return (String) value;
        }

        //return value.toString();
        return value.getClass().getSimpleName() + "@" + Integer.toHexString(value.hashCode());
    }
}
