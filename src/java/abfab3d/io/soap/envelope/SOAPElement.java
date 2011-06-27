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

package abfab3d.io.soap.envelope;

import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;
import abfab3d.io.soap.*;

/**
 * Base class for all SOAP message elements.
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public abstract class SOAPElement {

    /** Hashtable of SOAPElement attributes */
    private Hashtable attributes;

    /**
     * Creates a new instance of SOAPElement.
     */
    protected SOAPElement() {}

    /**
     * Adds an attribute
     *
     * @param attributeName  Attribute name
     * @param attributeValue Attribute value
     */
    public void addAttribute(
            String attributeName,
            String attributeValue) {

        if (attributes==null) {
            attributes=new Hashtable();
        }
        attributes.put(attributeName, attributeValue);

    }

    /**
     * Removes an attribute
     *
     * @param attributeName the key to remove from the
     * Hashtable.
     */
    public void removeAttribute(String attributeName) {
        attributes.remove(attributeName);
    }

    /**
     * Returns a (previously set) SOAPElement attribute
     *
     * @return the value of the attribute.
     */
    public String getAttribute(String attributeName) {

        return (attributes != null) ?
                (String) attributes.get (attributeName) :
                null;

    }

    /**
     * Returns all the SOAPElement attributes
     *
     * @return Hashtable of name/value pairs;
     *  null if no attribute is set.
     */
    public Hashtable getAttributes() {
        return attributes;
    }

    /**
     * Converts the attributes in the Hashtable to attributes of an element.
     * Called by subclasses when serializing iteself.
     *
     * @param xmlWriter instance of XMLWriter to aid in writing XML.
     */
    protected void objectToXML(AttributesImpl xmlAttributes) throws SAXException {

        if (attributes==null) {
            return;
        }

        Enumeration keys = attributes.keys();
        while (keys.hasMoreElements()) {

            String key = (String) keys.nextElement();

            xmlAttributes.addAttribute(
                    SOAPConstants.SOAP_SCHEMA_INSTANCE,
                    key, "xsi:" + key, "string", getAttribute(key));

        }

    }

    /**
     * Encapsulates  attributes in XML form in a Hashtable
     * of name-value pairs.
     *
     * @param theAttributes Vector, with each element an
     * instance of org.kxml.Attribute.
     */
    protected void xmltoObject(Vector theAttributes) {

        if (theAttributes == null) {
            return;
        }

        //for (int i=0; i<theAttributes.size(); i++) {
        //    AttributesImpl attribute = (AttributesImpl)theAttributes.elementAt(i);
        //    addAttribute(attribute.getName(), attribute.getValue());
        //}

    }

}
