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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A user defined parameter type.  Native representation is a map.
 *
 * @author Alan Hudson
 */
public class UserDefinedParameter extends BaseParameter {

    private HashMap<String,ParameterType> types;
    private HashMap<String, Parameter> props;

    public UserDefinedParameter(String name, String desc, Object initialValue) {
        super(name, desc);
        setDefaultValue(initialValue);
        setValue(initialValue);

        types = new HashMap<String, ParameterType>();
        props = new HashMap<String, Parameter>();
    }

    public UserDefinedParameter(String name, String desc) {
        super(name, desc);

        types = new HashMap<String, ParameterType>();
        props = new HashMap<String, Parameter>();

        this.value = new LinkedHashMap();
    }

    /**
     * Set the parameters value
     * @param value
     */
    public void setDefaultValue(Map value) {

        validate(value);

        LinkedHashMap lhm = new LinkedHashMap(value);
        this.defaultValue = lhm;
    }

    /**
     * Get the parameters value.
     * @return
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the parameters value
     * @param value
     */
    public void setValue(Map value) {

        validate(value);

        LinkedHashMap lhm = new LinkedHashMap(value);
        this.value = lhm;
    }

    /**
     * Set one specific properties value.
     * @param name
     * @param val
     */
    public void setPropertyValue(String name, Parameter val) {
        ((Map)this.value).put(name,val);
    }

    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType() {
        return ParameterType.USERDEFINED;
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

    public UserDefinedParameter clone() {
        return (UserDefinedParameter) super.clone();
    }

    public void addProperty(String name, Parameter val) {

        props.put(name,val);
        ((Map)value).put(name, val);
    }

    public Parameter getProperty(String name) {
        return props.get(name);
    }

    public Map<String,ParameterType> getTypes() {
        return types;
    }

    public Map<String, Parameter> getProperties() {
        return props;
    }
}
