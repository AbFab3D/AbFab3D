/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.io.soap.encoders;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * A default object for structures returned by the SOAP service
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class DefaultObject implements Encodeable {

    /** The name of the root XML tag */
    protected String soapElementName;

    /** The data type of the root XML tag */
    protected String soapElementType;

    /** The set of properties */
    private HashMap<String, Object> properties = new HashMap<String, Object>();

    /** The set of property type */
    private HashMap<String, String> propertyTypes = new HashMap<String, String>();

    /** The ordered set of properties */
    private ArrayList<String> propertyNames = new ArrayList<String>();

    //---------------------------------------------------------------
    // Methods required by Encodeable
    //---------------------------------------------------------------

    /**
     * Returns the property count
     *
     * @return the current count
     */
    public int getPropertyCount() {

        return propertyNames.size();

    }

    /**
     * Return the property name for the given index
     *
     * @param index the index number
     * @return the property name as String
     */
    public Object getProperty(String name) {

        return properties.get(name);

    }

    /**
     * Return all the properties
     *
     * @return the property list
     */
    public HashMap<String, Object> getProperties() {

        return properties;

    }


    /**
     * Return the property type
     *
     * @param name
     * @return the property type as String
     */
    public String getPropertyType(String name) {

        return propertyTypes.get(name);

    }

    /**
     * Return the property name for the given index
     *
     * @param index the index number
     * @return the property name as String
     */
    public String getPropertyName(int index) {

        return propertyNames.get(index);

    }

    /**
     * Return the property value for the given index
     *
     * @param index the index number.
     * @return the property value as Object.
     */
    public Object getPropertyValue(int index) {

        String key = propertyNames.get(index);
        return properties.get(key);

    }

    /**
     * Add/Replace the name and value of a property
     *
     * @param name the name of the property
     * @param the value of the property.
     */
    public void setProperty(String name, Object value) {

//System.out.println("DefaultObject.setProperty: " + this);
//System.out.println("    name: " + name);
//System.out.println("    value: " + value);

        if (!propertyNames.contains(name)) {
            propertyNames.add(name);
        }

        properties.put(name, value);

        if (value != null) {
            propertyTypes.put(name, value.getClass().getName());
        }

    }

    /**
     * Replaces the existing property at a specified index with the new value.
     *
     * @param value - the new Object
     * @param index - the index
     */
    public void setPropertyAt(Object value, int index) {

        String key = propertyNames.get(index);

        if (key != null) {
            properties.put(key, value);
            propertyTypes.put(key, value.getClass().getName());
        }

    }

    /**
     * Add a new property to the list
     * @param name
     */
    public void addProperty(String name) {

//System.out.println("DefaultObject.addProperty");
//System.out.println("    name: " + name);

        propertyNames.add(name);
        properties.put(name, null);

    }

    /**
     * Add a new property to the list
     * @param name
     */
    public void removeProperty(String name) {

        propertyNames.remove(name);
        properties.remove(name);
        propertyTypes.remove(name);

    }

    /**
     * Add/Replace the name and type of a property
     *
     * @param name the name of the property
     * @param the value of the property.
     */
    public void setPropertyType(String name, String type) {

//System.out.println("DefaultObject.setPropertyType");
//System.out.println("    name: " + name);
//System.out.println("    type: " + type);

        propertyTypes.put(name, type);
    }

    /**
     * Get the name of the root soap element
     * @return
     */
    public String getSoapElementName() {
        return soapElementName;
    }

    /**
     * Set the name of the root soap element
     * @return
     */
    public void setSoapElementName(String soapElementName) {
        this.soapElementName = soapElementName;
    }

    /**
     * Get the type of the root soap element
     * @return
     */
    public String getSoapElementType() {
        return soapElementType;
    }

    /**
     * Set the type of the root soap element
     * @return
     */
    public void setSoapElementType(String soapElementType) {
        this.soapElementType = soapElementType;
    }

}
