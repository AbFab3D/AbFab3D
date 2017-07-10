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

import java.util.Arrays;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;

/**
 * A enum parameter.
 *
 *
 * @author Alan Hudson
 */
public class EnumParameter extends BaseParameter {

    String m_values[] = new String[]{"value"};
    String m_labels[] = new String[]{"label"};
    int m_index = 0;


    public EnumParameter(String name, String values[], String initialValue) {
        this(name, name, values, initialValue);
    }

    public EnumParameter(String name, String desc, String values[], String initialValue) {
        this(name,desc,values,null,initialValue);
    }

    public EnumParameter(String name, String label, String desc, String values[], String labels[], String initialValue) {
        super(name, desc);
        setLabel(label);

        m_values = values.clone();
        if (labels != null) {
            m_labels = labels.clone();
        } else {
            m_labels = values.clone();
        }

        if (initialValue == null) {
            // An enum must have a value, pick the first one
            initialValue = m_values[0];
        }
        defaultValue = initialValue;
        setValue(initialValue);
    }

    public EnumParameter(String name, String desc, String values[], String labels[], String initialValue) {
        super(name, desc);
        m_values = values.clone();
        if (labels != null) {
            m_labels = labels.clone();
            printf("Labels: %s\n", Arrays.toString(labels));
        } else {
            m_labels = m_values.clone();
        }

        if (initialValue == null) {
            // An enum must have a value, pick the first one
            initialValue = m_values[0];
        }
        defaultValue = initialValue;
        setValue(initialValue);
    }

    public EnumParameter(String name, String label, String desc, String values[], String initialValue) {
        this(name,label,desc,values,null,initialValue);
    }

    public EnumParameter(EnumParameter def, String initialValue) {

        super(def.getName(), def.getDesc());

        if (initialValue == null) {
            // An enum must have a value, pick the first one
            initialValue = m_values[0];
        }
        defaultValue = initialValue;
        setValue(initialValue);
    }

    public void setValues(String[] vals) {
        m_values = vals.clone();
    }

    @Override
    public String getValue() {
        if (value == null) {
            printf("Enum is null: name: %s\n",getName());
            return "null";
        }
        return value.toString();
    }

    public String getLabel() {
        if (label != null) return label;

        return getValue();
    }
    
    public Object getDefaultValue(Class hint) {
        return getValue();
    }

    public String[] getValues() {
        return m_values;
    }

    public String[] getLabels() {
        return m_labels;
    }

    public void setSelectedIndex(int index) {
        if(index < 0 || index >= m_values.length)
            throw new RuntimeException(fmt("invalid index value: %d", index));
        m_index = index;
        value = m_values[index];
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

        int len = m_values.length;
        for(int i = 0; i < len; i++){
            String st = (String) val;
            if(st.equalsIgnoreCase(m_values[i])){
                m_index = i;
                return;
            }
        }

        m_index = 0;

        String vv = "";
        for(int i = 0; i < len; i++){
            vv += m_values[i];

            if (i < len - 1) vv += ",";
        }

        throw new IllegalArgumentException("Unsupported value: " + val + " in param: " + getName() + " values: " + vv);
        
    }

    public EnumParameter clone() {

        return (EnumParameter) super.clone();
        
    }

    public static <T extends Enum<T>> String[] enumArray(T[] values) {
        int len = values.length;
        String[] result = new String[len];
        Enum[] arr = values;

        for(int i = 0; i < len; i++) {
            Enum value = arr[i];
            result[i] = value.name();
        }

        return result;
    }
}
