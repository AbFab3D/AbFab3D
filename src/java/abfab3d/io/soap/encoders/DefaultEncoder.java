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

import abfab3d.io.soap.*;

import java.util.*;
import java.io.*;

import org.xml.sax.helpers.AttributesImpl;

/**
 * Reads XML and assigns the properties to the Java Object representation.
 * Writes XML from the Java Object.
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class DefaultEncoder extends BaseEncoder implements Encoder  {

    /**
     * Default Constructor.  If used then you must set the reader
     * and writer and registry (if needed).
     */
    public DefaultEncoder() {
        super();
    }

    /**
     * Constructor
     */
    public DefaultEncoder(
            XMLWriter xmlWriter,
            Encodeable returnObject) {

        super();
        this.xmlWriter = xmlWriter;
        this.returnObject = returnObject;

    }

    //----------------------------------------------------------
    // Methods required by Encoder
    //----------------------------------------------------------

    /**
     * Converts a parameter represented as Java objects to XML.
     *
     * @param parameterName the name of the parameter
     * @param parameterValue the value of the parameter.
     *
     * @throws Exception if any error occurs during writing XML
     */
    public void objectToXML(
            String parameterName,
            Object parameterObject) throws Exception {

        if (parameterObject == null) {

            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(getSchemaInstance(), "null", "xsi:null", "boolean", "true");

            xmlWriter.startElement("", parameterName, "", attributes);
            xmlWriter.endElement("", parameterName, "");

            return;

        }

        if (parameterObject instanceof Encodeable) {

            encodeableToXML(parameterName, (Encodeable)parameterObject);

        } else if (parameterObject instanceof java.util.Vector) {

            vectorToXML(parameterName, parameterObject);

        } else if (parameterObject instanceof java.util.Hashtable) {

            hashtableToXML(parameterName, parameterObject);

        } else if (parameterObject instanceof java.util.Date) {

            dateToXML(parameterName, parameterObject);

        } else if (parameterObject.getClass().isArray()) {

            arrayToXML(parameterName, parameterObject);

        } else {

            primitiveToXML(parameterName, parameterObject);

        }

    }

    /**
     * Parse an unknown data type
     *
     * @throws IOException if any error occurs in parsing
     * the XML.
     * @throws Exception if any other error occurs.
     */
    public void xmlToObject(byte[] response)
        throws IOException, Exception {

        // Create an adaptor
        SOAPAdapter adaptor = new SOAPAdapter(returnObject);

        // Create the parser and call it
        XMLParser xmlParser = new XMLParser();
        xmlParser.setContentHandler(adaptor);
        xmlParser.parse(new ByteArrayInputStream(response));

    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Converts a parameter that is an instance of Date to XML.
     *
     * @param parameterName the name of the parameter
     * @param parameterObject the value of the parameter represented
     *
     * @throws Exception if any error occurs during writing XML
     */
    private void dateToXML(
            String parameterName,
            Object parameterObject) throws Exception {

        Date d = (Date) parameterObject;
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        TimeZone tz = cal.getTimeZone();

        int offset = tz.getOffset(
                1,
                Calendar.YEAR,
                Calendar.MONTH+1,
                Calendar.DAY_OF_MONTH,
                Calendar.DAY_OF_WEEK,
                01);

        d.setTime(d.getTime() - offset);
        cal = Calendar.getInstance();
        cal.setTime(d);

        StringBuffer sb = new StringBuffer();
        sb.append(cal.get(Calendar.YEAR));
        sb.append("-");
        sb.append(padInteger((cal.get(Calendar.MONTH))+1));
        sb.append("-");
        sb.append(padInteger(cal.get(Calendar.DAY_OF_MONTH)));
        sb.append("T");
        sb.append(padInteger(cal.get(Calendar.HOUR_OF_DAY)));
        sb.append(":");
        sb.append(padInteger(cal.get(Calendar.MINUTE)));
        sb.append(":");
        sb.append(padInteger(cal.get(Calendar.SECOND)));
        sb.append(".");
        sb.append(padInteger(cal.get(Calendar.MILLISECOND)));
        sb.append("Z");

        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(getSchemaInstance(), "", "xsi:type", "dateTime", "xsd:dateTime");

        xmlWriter.startElement("", parameterName, "", attributes);
        xmlWriter.bodyElement(sb.toString());
        xmlWriter.endElement("", parameterName, "");

    }

    /**
     * Converts a parameter that is an instance of
     * an Encodeable Object to XML.
     *
     * @param parameterName the name of the parameter
     * @param data the value of the parameter represented
     *
     * @throws Exception if any error occurs during writing XML
     */
    private void encodeableToXML(
          String parameterName,
          Encodeable parameterObject) throws Exception {

        // create the wrapper element
        String elementName = parameterObject.getSoapElementName();
        String elementType = parameterObject.getSoapElementType();

        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(getSchemaInstance(), "", "xsi:type", elementType, "Shapeways:" + elementType);
        xmlWriter.startElement("Shapeways", elementName, "Shapeways:" + elementName, attributes);

        // write all the child properties
        HashMap<String, Object> properties = parameterObject.getProperties();

        Iterator<Map.Entry<String, Object>> index =
            properties.entrySet().iterator();

        while (index.hasNext()) {

            attributes = new AttributesImpl();

            Map.Entry<String, Object> mapEntry = index.next();

            // get the key, value pairing
            String name = mapEntry.getKey();
            Object value = mapEntry.getValue();

            if (value instanceof Integer) {

                attributes.addAttribute(getSchemaInstance(), "", "xsi:type", "int", "xsd:int");

            } else if (value instanceof byte[]) {

                attributes.addAttribute(getSchemaInstance(), "", "xsi:type", "base64Binary", "xsd:base64Binary");
                value = Base64.encode((byte[])value);

            } else if (value != null) {

                String className = value.getClass().getName();
                int i = className.lastIndexOf('.');

                attributes.addAttribute(getSchemaInstance(), "", "xsi:type",
                        className.substring(i + 1).toLowerCase(),
                        "xsd:" + className.substring(i + 1).toLowerCase());

            } else if (value == null) {

                attributes.addAttribute(getSchemaInstance(), "", "xsi:nil", "true", "true");

            }

            xmlWriter.startElement("", name, "", attributes);
            if (value != null) {
                xmlWriter.bodyElement(value.toString());
            }
            xmlWriter.endElement("", name, "");

        }

        xmlWriter.endElement("Shapeways", elementName, "Shapeways:" + elementName);

    }

    /**
     * Converts a parameter that is an instance of
     * primitive wrapper instance to XML.
     *
     * @param parameterName the name of the parameter
     * @param data the value of the parameter represented
     *
     * @throws Exception if any error occurs during writing XML
     */
    private void primitiveToXML(
          String parameterName,
          Object parameterObject) throws Exception {

        AttributesImpl attributes = new AttributesImpl();

        if (parameterObject instanceof Integer) {

            attributes.addAttribute(getSchemaInstance(), "", "xsi:type", "integer", "xsd:integer");

        } else if (parameterObject instanceof Base64) {

            //System.out.println("Encoding of Base64 not supported");

            if (schema.equals(SOAPConstants.SOAP_SCHEMA)) {

                attributes.addAttribute(getSchemaInstance(), "", "xsi:type", "base64Binary", "xsd:base64Binary");

            } else {

                attributes.addAttribute(getSchemaInstance(), "", "xsi:type", "base64", "xsd:base64");

            }


        } else {

            String className = parameterObject.getClass().getName();
            int index = className.lastIndexOf('.');

            attributes.addAttribute(getSchemaInstance(), "", "xsi:type",
                    className.substring(index + 1).toLowerCase(),
                    "xsd:" + className.substring(index + 1).toLowerCase());

        }

        xmlWriter.startElement("", parameterName, "", attributes);
        xmlWriter.bodyElement(parameterObject.toString());
        xmlWriter.endElement("", parameterName, "");

    }

    /**
     * Converts a parameter that is an instance of Vector to
     * XML.
     *
     * @param parameterName the name of the parameter
     * @param parameterObject the value of the parameter represented
     *
     * @throws Exception if any error occurs during writing XML
     */
    private void vectorToXML(
            String parameterName,
            Object parameterObject) throws Exception {

        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(getSchemaInstance(), "", "xsi:type", "Vector", "xsd:Vector");

        xmlWriter.startElement("", parameterName, "", attributes);

        Vector vector = (Vector)parameterObject;

        for (int i=0; i<vector.size(); i++) {
            Object vectorElement = vector.elementAt(i);
            objectToXML("vectoritem", vectorElement);
        }

        //Close the element tag
        xmlWriter.endElement("", parameterName, "");
    }

    /**
     * Converts a parameter that is an instance of Hashtable to XML.
     *
     * @param parameterName the name of the parameter
     * @param parameterObject the value of the parameter represented
     *
     * @throws Exception if any error occurs during writing XML
     */
    private void hashtableToXML(
            String parameterName,
            Object parameterObject) throws Exception {

        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(getSchemaInstance(), "", "xsi:type", "Map", "xsd:Map");

        xmlWriter.startElement("", parameterName, "", attributes);

        Hashtable hashtable = (Hashtable)parameterObject;
        Enumeration keys = hashtable.keys();

        while (keys.hasMoreElements()) {

             Object theKey = keys.nextElement();

             xmlWriter.startElement("", "mapitem", "", null);
             objectToXML("key", theKey);
             objectToXML("value", hashtable.get(theKey));
             xmlWriter.endElement("", "mapitem", "");

        }

        //Close the element tag
        xmlWriter.endElement("", parameterName, "");
    }

    /**
     * Converts a parameter that is an instance of a primitive wrapper array to XML.
     *
     * @param parameterName the name of the parameter
     * @param parameterObject the value of the parameter represented
     *
     * @throws Exception if any error occurs during writing XML
     */
    private void arrayToXML(
            String parameterName,
            Object parameterObject) throws Exception {

        throw new Exception("Array to XML not supported!");

        /*
        if(parameterObject instanceof byte[]) {
            // catch the base64 object
            //Base64 b64 = new Base64((byte[])parameterObject);
            //objectToXML(parameterName, b64);

        } else {

            String theNamespace = this.schema;
            Object[] objectArray = (Object[]) parameterObject;
            String arrayType="";

            // check if it is a homogeneous array
            if (isArrayHetrogeneous(objectArray)) {

                arrayType="ur-type[";

            } else {

                // This might be an array of custom objects.
                // Not supported at this time


            }

            // Write the array type. Retrieve each element of the array and serialize it.
            //xmlWriter.startTag(parameterName);
            //xmlWriter.attribute("type", schemaInstance, "Array", encodingStyle);
            //xmlWriter.attribute("arrayType", encodingStyle, arrayType+objectArray.length+"]", theNamespace);

            for (int i=0; i<objectArray.length; i++) {
                objectToXML("item", objectArray[i]);
            }
            //xmlWriter.endTag();

        }
        */

    }

    /**
     * Determines if a given array is hetrogeneous.  An
     * Array is hetrogeneous if any element of the array
     * belongs to a different class that any other
     * element of the array.
     *
     * @param array the array to interrogate.
     * @return true if the array is hetrogeneous, false
     * if not.
     */
    /*
    private boolean isArrayHetrogeneous(Object[] array) {

        boolean returnValue=false;
        Object previousElement=null;

        for (int i=0; i<array.length; i++) {
            if (i!=0 && array[i]!=null &&
               (! array[i].getClass().getName().equals(
                       previousElement.getClass().getName()))) {

                returnValue=true;
                break;
            } //else
            previousElement=array[i];
        } //for

        return returnValue;

    }
    */

    /**
     * Utility method to return a two digited string
     * representation for a int.
     *
     * @param value the integer for which a two digited
     * string representation is needed.
     * @return String representation of the int.
     */
    private String padInteger(int value) {
        return value < 10 ? "0" + value : value + "";
    }

}
