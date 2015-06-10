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
 * A enum parameter.
 *
 *
 * @author Alan Hudson
 */
public class EnumParameter extends Parameter {

    String m_values[] = new String[]{"value"};
    int m_index = 0;

    /*
    public EnumParameter(String name, String desc, String initialValue) {
        super(name, desc);
        setValue(initialValue);
    }
    */

    public EnumParameter(String name, String desc, String values[], String initialValue) {
        super(name, desc);
        m_values = values;
        setValue(initialValue);
    }

    public EnumParameter(EnumParameter def, String initialValue) {

        super(def.getName(), def.getDesc());
        
        defaultValue = initialValue;
        setValue(initialValue);
    }

    @Override
    public String getValue() {
        return value.toString();
    }

    public Object getDefaultValue(Class hint) {
        return getValue();
    }

    public String[] getValues() {
        return m_values;
    }

    public int getIndex() {
        return m_index;
    }


    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType() {
        return ParameterType.ENUM;
    }

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val) {
        if (val == null) return;

        if (val instanceof Enum) {
            val = val.toString();
        }
        if (!(val instanceof String)) {
            throw new IllegalArgumentException("Unsupported type for String: " + val.getClass() + " in param: " + getName());
        }

        for(int i = 0; i < m_values.length; i++){
            if(val.equals(m_values[i])){
                m_index = i;
                return;
            }
        }

        m_index = 0;

        throw new IllegalArgumentException("Unsupported value: " + val + " in param: " + getName());
        
    }

    public EnumParameter clone() {

        return (EnumParameter) super.clone();
        
    }

    public static <T extends Enum<T>> String[] enumArray(T[] values) {
        int i = 0;
        String[] result = new String[values.length];
        Enum[] arr$ = values;
        int len$ = values.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Enum value = arr$[i$];
            result[i++] = value.name();
        }

        return result;
    }
}
