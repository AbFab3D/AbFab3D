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

/**
 * Each structured object should implement these methods.  These methods
 * provide the ability to get/set properties during the encoding process.
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public interface Encodeable {

    /**
     * Returns the property count
     *
     * @return the current count
     */
    public int getPropertyCount();

    /**
     * Return the property name for the given index
     *
     * @param index the index number
     * @return the property name as String
     */
    public Object getProperty(String name);

    /**
     * Return all the properties
     *
     * @return the property list
     */
    public HashMap<String, Object> getProperties();

    /**
     * Return the property name for the given index
     *
     * @param index the index number
     * @return the property name as String
     */
    public String getPropertyName(int index);

    /**
     * Return the property value for the given index
     *
     * @param index the index number.
     * @return the property value as Object.
     */
    public Object getPropertyValue(int index);

    /**
     * Add/Replace the name and value of a property
     *
     * @param name the name of the property
     * @param the value of the property.
     */
    public void setProperty(String name, Object value);

    /**
     * Add a new property to the list
     * @param name
     */
    public void addProperty(String name);

    /**
     * Add a new property to the list
     * @param name
     */
    public void removeProperty(String name);

    /**
     * Add/Replace the name and type of a property
     *
     * @param name the name of the property
     * @param the value of the property.
     */
    public void setPropertyType(String name, String type);

    /**
     * Get the type of a property
     *
     * @param name the name of the property
     */
    public String getPropertyType(String name);

    /**
     * Get the name of the root soap element
     * @return
     */
    public String getSoapElementName();

    /**
     * Set the name of the root soap element
     * @return
     */
    public void setSoapElementName(String soapElementName);

    /**
     * Get the type of the root soap element
     * @return
     */
    public String getSoapElementType();

    /**
     * Set the type of the root soap element
     * @return
     */
    public void setSoapElementType(String soapElementType);

}

