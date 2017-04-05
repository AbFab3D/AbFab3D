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

/**
 * A parameter to a datasource.
 *
 * @author Alan Hudson
 */
public interface Parameter extends Cloneable {
    /**
     * Get the parameter type enum.
     * @return The type
     */
    public ParameterType getType();

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public void validate(Object val);

    /**
     * @return the name
     */
    public String getName();

    /**
     * @param name the name to set
     */
    public void setName(String name);

    /**
     * @return the desc
     */
    public String getDesc();

    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc);

    /**
     * Get the parameters value.
     * @return
     */
    public Object getValue();

    /**
     * Set the parameters value
     * @param value
     */
    public void setDefaultValue(Object value);

    /**
     * Get the parameters value.
     * @return
     */
    public Object getDefaultValue();

    /**
     * Is this parameter set at its default value
     * @return
     */
    public boolean isDefaultValue();

    /**
     * Set the parameters value
     * @param value
     */
    public void setValue(Object value);

    public String getOnChange();

    public void setOnChange(String onChange);
    
    public String getGroup();

    public void setGroup(String group);
    
    public void setLabel(String label);
    
    public String getLabel();

    /**
     * Get the string value to use for parameter hashes
     * @return
     */
    public String getParamString();

    /**
     * Get the string value to use for parameter hashes, append value to existing string builder to lower garbage
     * @return
     */
    public void getParamString(StringBuilder sb);

    /**
     * Has the value changed since the last call.  This method will clear the changed state.
     * @return
     */
    public boolean hasChanged();


    /**
       adds editor to the parameter
       used to update editor UI in case of param change 
     */
    public void addEditor(Editor editor);

    /**
       @return String representation of this Parametetr to be used for serialization 
       it is expected to be representation in simple JSON form 
     */
    public String getStringValue();

    /**
       set value of this parameter as string 
       @param str string it is expected to be representation in simple JSON form 
     */
    public void setStringValue(String str);

}
