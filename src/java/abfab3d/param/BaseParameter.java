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
public abstract class BaseParameter implements Parameter {
    /** The name of the parameter. */
    private String name;

    /** The description */
    private String desc;

    /** The value */
    protected Object value;

    /** The default value */
    protected Object defaultValue;

    /** Method to run on change */
    protected String onChange;
    
    protected String group;
    

	public BaseParameter(String name, String desc) {

        this.name = name;
        this.desc = desc;
    }
	
	public BaseParameter(String name, String desc, String group) {

        this.name = name;
        this.desc = desc;
        this.group = group;
    }

    /**
     * Get the parameter type enum.
     * @return The type
     */
    public abstract ParameterType getType();

    /**
     * Validate that the object's value meets the parameters requirements.  Throws InvalidArgumentException on
     * error.
     *
     * @param val The proposed value
     */
    public abstract void validate(Object val);

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * Get the parameters value.
     * @return
     */
    public Object getValue() {
        return value;
    }

    /**
     * Set the parameters value
     * @param value
     */
    public void setDefaultValue(Object value) {

        validate(value);
        
        this.defaultValue = value;
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
    public void setValue(Object value) {

        validate(value);

        this.value = value;
    }

    public String getOnChange() {
        return onChange;
    }

    public void setOnChange(String onChange) {
        this.onChange = onChange;
    }
    
    public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

    public BaseParameter clone() {
        try {
            return (BaseParameter) super.clone();
        } catch(CloneNotSupportedException cnse) { cnse.printStackTrace(); }

        return null;
    }
}
