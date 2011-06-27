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

// External imports
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

// Local imports

/**
 * Interface adapter between XML input from a SAX source to the Java source.
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class SOAPAdapter extends DefaultHandler {

    /** The Encodeable object to save to */
    private Encodeable encodableObject;

    /** Flag to skip unwanted elements */
    private boolean isElement;

    /** The name of the root element to convert */
    private String elementName;;

    /** A temporary buffer for character data */
    private StringBuffer textBuffer;

    /** A stack of objects */
    private Stack<Object> nodeStack;

    /** A stack of object types */
    private Stack<String> typeStack;

    /**
     * Constructor.  Will save to a new DefaultObject.
     */
    public SOAPAdapter() {
        this(new DefaultObject());
    }

    /**
     * Constructor.  Will save to the soapObject.
     *
     * @param soapObject
     */
    public SOAPAdapter(Encodeable soapObject) {

        this.encodableObject = soapObject;

        nodeStack = new Stack<Object>();
        typeStack = new Stack<String>();

        isElement = false;
    }

    //----------------------------------------------------------
    // SAX ContentHandler methods
    //----------------------------------------------------------

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument() throws SAXException {

        elementName = encodableObject.getSoapElementName();

        // push the base item onto the stack
        nodeStack.push(encodableObject);
        typeStack.push(encodableObject.getClass().getName());

    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument() throws SAXException {

        // pop the stack???
        encodableObject = (Encodeable)nodeStack.pop();
        typeStack.pop();

    }

    /**
     * Handles characters event.
     *
     * @param ch The characters.
     * @param start The start position in the character array.
     * @param length The number of characters .
     */
    public void characters(char[] ch, int start, int length) throws SAXException {

        // Buffer the characters
        String s = new String(ch, start, length);

        if (textBuffer == null) {
            textBuffer = new StringBuffer(s);
        } else {
            textBuffer.append(s);
        }

    }

    /**
     * Handles startElement event.
     *
     * In this case we are looking at the attributes to know what the variable type is.
     * If it is an array of complex types then we have to deal with it.  Otherwise just
     * update the propertyTypes and add a property for this element.  Default is String
     * if nothing can be determined.
     *
     * @param namespaceURI The namespace URI.
     * @param localName The local name, or the empty string if Namespace processing is not being performed.
     * @param qName The qualified name, or the empty string if qualified names are not available.
     * @param attributes The specified or defaulted attributes.
     */
    public void startElement(
            String namespaceURI,
            String localName,
            String qName,
            Attributes attributes) throws SAXException {

        flushText();

        if (localName.equals(elementName)) {
            isElement = true;
        }

        // we have reached the part of the envelop that contains the desired
        if (isElement) {

            // check to see if it is an array
            if (attributes.getValue("SOAP-ENC:arrayType") != null) {

                // grab the current object
                encodableObject = (Encodeable)nodeStack.peek();

                //find out how many we are going to have
                String type = attributes.getValue("SOAP-ENC:arrayType").trim();
                String parameterType =
                    "abfab3d.io.shapeways." +
                    type.substring(
                            type.indexOf(":") + 1,
                            type.indexOf("[")) +
                    "Type";

                // Add the property to the list
                encodableObject.addProperty(localName);

                // set the type of array object
                encodableObject.setPropertyType(localName, parameterType + "[]");

                // create the list to assign values to
                ArrayList<Encodeable> list = new ArrayList<Encodeable>();

                // push the list onto the stack
                nodeStack.push(list);
                typeStack.push(parameterType);

            }
            // check to see if it is an iten of an array
            else if (localName.equals("item")) {

                // grab the current object
                ArrayList<Encodeable> list = (ArrayList<Encodeable>)nodeStack.peek();

                // create the instance and append to the end of the list
                try {
                    String classname = typeStack.peek();

                    Class cls = Class.forName(classname);
                    Encodeable item = (Encodeable)cls.newInstance();

                    //Encodeable item = new DefaultObject();
                    list.add(item);

                    // push the list onto the stack
                    nodeStack.push(item);
                } catch(Exception e) {
                }

            }
            // otherwise we assume it is an encodeable and should process
            else if (attributes.getValue("xsi:type") != null) {

                // grab the current object
                encodableObject = (Encodeable)nodeStack.peek();

                // Add the property to the list
                encodableObject.addProperty(localName);

                String type = attributes.getValue("xsi:type").trim().toLowerCase();
                String parameterType = type.substring(type.indexOf(":") + 1);

                // set the type of the object
                encodableObject.setPropertyType(localName, parameterType);


            }
            // fallback to a string just in case
            else {

                // grab the current object
                encodableObject = (Encodeable)nodeStack.peek();

                // Add the property to the list
                encodableObject.addProperty(localName);

                // set the type of the object
                encodableObject.setPropertyType(localName, "string");

            }

        }

    }

    /**
     * Handles endElement event.
     *
     * Now we should have the value of the element.  We associate the value with
     * corresponding type in the return object.
     *
     * @param namespaceURI namespace URI
     * @param localName The local name, or the empty string if Namespace processing is not being performed.
     * @param qName The qualified name, or the empty string if qualified names are not available.
     */
    public void endElement(
            String namespaceURI,
            String localName,
            String qName) throws SAXException {

        if (isElement) {

            // grab the current object
            Object obj = nodeStack.peek();

            if (obj instanceof ArrayList) {

                // we are done with this list
                ArrayList<Encodeable> list = (ArrayList<Encodeable>)nodeStack.pop();

                encodableObject = (Encodeable)nodeStack.peek();
                encodableObject.setProperty(localName, list);

                typeStack.pop();

            } else {

                // if this ends an array item
                if (localName.equals("item")) {

                    // we are done with this item
                    Encodeable item = (Encodeable)nodeStack.pop();

                }
                // otherwise treat as a incoming value
                else {

                    encodableObject = (Encodeable)nodeStack.peek();

                    // Get the property type
                    String parameterType = encodableObject.getPropertyType(localName);

                    String value = null;
                    if (textBuffer != null) {
                        value = textBuffer.toString();
                        flushText();
                    }

                    if (value == null) {

                        encodableObject.setProperty(localName, null);

                    } else if (parameterType.equals("string")) {

                        encodableObject.setProperty(localName, new String(value));

                    } else if (parameterType.equals("byte")) {

                        encodableObject.setProperty(localName, new Byte(Byte.parseByte(value)));

                    } else if (parameterType.equals("short"))  {

                        encodableObject.setProperty(localName, new Short(Short.parseShort(value)));

                    } else if (parameterType.equals("int") ||
                           parameterType.equals("integer")) {

                        encodableObject.setProperty(localName, new Integer(Integer.parseInt(value)));

                    } else if (parameterType.equals("long"))  {

                        encodableObject.setProperty(localName, new Long(Long.parseLong(value)));

                    } else if ( parameterType.equals("float"))  {

                        encodableObject.setProperty(localName, new Float(Float.parseFloat(value)));

                    } else if (parameterType.equals("date"))  {

                        encodableObject.setProperty(localName, new Date(Long.parseLong(value)));

                    } else if (parameterType.equals("boolean"))  {

                        encodableObject.setProperty(localName,
                                (value.equals("true") || value.equals("1")) ?
                                        new Boolean(true) :
                                        new Boolean(false));

                    } else if (parameterType.equals("base64") ||
                           parameterType.equals("base64binary")) {

                        try {

                            encodableObject.setProperty(localName, Base64.decode(value));

                        } catch (Exception se) {

                            throw new SAXException("Error encoding Base64 object");

                        }

                    } else if (parameterType.equals("object")) {

                        encodableObject.setProperty(localName, value);

                    } else if (encodableObject.getClass().getName().toLowerCase().endsWith(parameterType)) {

                        encodableObject.setProperty(localName, encodableObject);

                    } else {

                        throw new SAXException("Unsupported data type:" + parameterType );

                    }

                }

            }

        }

        if (localName.equals(elementName)) {
            isElement = false;
        }

    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Flush the text accumulated in the character buffer.
     */
    private void flushText() throws SAXException {
        if (textBuffer == null) {
            return;
        }
        textBuffer = null;
    }


}
