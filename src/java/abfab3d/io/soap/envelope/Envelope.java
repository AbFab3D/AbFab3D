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
import java.io.*;

import org.xml.sax.helpers.AttributesImpl;

import abfab3d.io.soap.*;
import abfab3d.io.soap.encoders.*;

/**
 * Encapsulates the SOAP Envelope element.
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class Envelope extends SOAPElement {

    /** Store the parameters, index 0 = name, index 1 = value */
    private Vector<Object[]> body;

    /** Was a SOAP Fault generated */
    private boolean faultGenerated;

    /** The SOAP Fault Object */
    private SOAPFault faultObject;

    /** The name of the web service method being called */
    private String methodName;

    /** The target URI */
    private String targetURI;

    /** The encoding style to use */
    private String encoding;

    /**
     * Constructor for the SOAP Envelope. This sets:
     *
     *  Default namespace - http://schemas.xmlsoap.org/soap/envelope
     *  Default Schema Instance - http://www.w3.org/2001/XMLSchema-instance
     *  Default Schema - http://www.w3.org/2001/XMLSchema
     *  Defualt Encoding Style - http://schemas.xmlsoap.org/soap/encoding
     *
     */
    public Envelope() {

        super.addAttribute("xmlns:SOAP-ENV", SOAPConstants.SOAP_NAMESPACE);
        setSchema(SOAPConstants.SOAP_SCHEMA);
        setSchemaInstance(SOAPConstants.SOAP_SCHEMA_INSTANCE);
        setEncodingStyle(SOAPConstants.SOAP_ENCODING_STYLE);

    }

    /**
     * Sets the SOAP body with parameters.
     *
     * @param parameterName String with the name of the parameter.
     * @param value Object encapsulating the value assigned to the parameter.
     */
    public void setBody(String parameterName, Object value) {

        if (body==null) {
            body=new Vector<Object[]>();
        }

        body.addElement(new Object[] {parameterName, value});

    }

    /**
     * Get the schemaInstance
     *
     * @return String with the schema instance.
     */
    public String getSchemaInstance() {

        String str =  super.getAttribute("xmlns:xsi");

        if (str == null) {
            return SOAPConstants.SOAP_SCHEMA_INSTANCE;
        }  else {
            return str;
        }

    }

    /**
     * Set the schemaInstance
     *
     * @param schemaInstance URI of Schema Instance
     */
    public void setSchemaInstance(String schemaInstance) {
        super.addAttribute("xmlns:xsi", schemaInstance);
    }

    /**
     * Get the schema
     *
     * @return String with the schema.
     */
    public String getSchema() {

        String str =  super.getAttribute("xmlns:xsd");

        if (str == null) {
            return SOAPConstants.SOAP_SCHEMA;
        } else {
            return str;
        }

    }

    /**
     * Set the schema
     *
     * @param schema URI of Schema
     */
    public void setSchema(String schema) {
        super.addAttribute("xmlns:xsd", schema);
    }

    /**
     * Get the encodingStyle
     *
     * @return String with the encoding style.
     */
    public String getEncodingStyle () {

        String str =  super.getAttribute("SOAP-ENV:encodingStyle");

        if (str == null) {
            return SOAPConstants.SOAP_ENCODING_STYLE;
        } else {
            return str;
        }

    }

    /**
     * Set the encodingStyle
     *
     * @param schema URI of encodingStyle
     */
    public void setEncodingStyle(String encodingStyle) {
        super.addAttribute("SOAP-ENV:encodingStyle", encodingStyle);
    }

    /**
     * Returns the number of parameters in the the SOAP Body.
     *
     * @return int the number of parameters; 0
     * if no parameter is present
     */
    public int getParameterCount() {

        return body==null ?
                0 :
                body.size();

    }

    /**
     * Returns the parameter name at the given index.
     *
     * @param index the parameter number.
     * @return String with the parameter name.
     */
    public String getParameterName(int index) {

        return body==null ?
                null :
                (String)(((Object[])body.elementAt(index))[0]);

    }

    /**
     * Returns the parameter at the given index.
     *
     * @param index the parameter number.
     * @return Object The parameter as an object.
     */
    public Object getParameter(int index) {

        return body == null ?
                null:
                ((Object[])body.elementAt(index))[1];

    }

    /**
     * Determines if a fault element was present in
     * the response from a service.
     *
     * @return true If a fault element was present
     */
    public boolean isFaultGenerated() {
        return faultGenerated;
    }

    /**
     * Returns the fault generated by the service
     *
     * @return fault generated
     */
    public SOAPFault getFault() {
        return faultObject;
    }

    /**
     * Get the encoding being used.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Method to specify the encoding to use while sending
     * and parsing the SOAP payload (example UTF-8).
     *
     * @param enc the encoding to use.
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Create a XML based SOAP compliant envelope.
     *
     * @param registry TypeMappingRegistry
     * @param methodName the name of the method to invoke on the on the service
     * @param targetURI URI of the service to invoke
     * @return An array of bytes representing the soap message
     *
     * @throws Exception if problems during serialization
     */
    public byte[] objectToXML(
            String methodName,
            String targetURI) throws Exception {

        // create the XMLWriter
        ByteArrayOutputStream bais = new ByteArrayOutputStream(1024 * 4);
        OutputStreamWriter osw = new OutputStreamWriter(bais);

        XMLWriter xmlWriter = new XMLWriter(osw);
        xmlWriter.startDocument();

        // get the Envelope attributes
        AttributesImpl attributes = new AttributesImpl();
        super.objectToXML(attributes);

        // write the envelope
        xmlWriter.startElement(SOAPConstants.SOAP_NAMESPACE, "", "SOAP-ENV:Envelope", attributes);

        // write the body
        writeBody(xmlWriter, methodName, targetURI);

        // End the Body and Envelope element
        xmlWriter.endElement(SOAPConstants.SOAP_NAMESPACE, "", "SOAP-ENV:Envelope");
        xmlWriter.endDocument();

        osw.flush();
        return bais.toByteArray();
    }

    /**
     * Use the XMLWriter to create the Body element.
     * The body is stored in the body Vector.  The elements
     * of the body vector is Object[] indicating name/value pairs.
     *
     * @param xmlWriter aids in writing the XML.
     * @param methodName the name of the method to invoke on the server
     * @param targetURI the name of the service to invoke
     *
     * throws Exception if any errors occur in encoding
     */
    private void writeBody(
            XMLWriter xmlWriter,
            String methodName,
            String targetURI) throws Exception {

        boolean endMethod=false;

        //write the Body element
        xmlWriter.startElement(SOAPConstants.SOAP_NAMESPACE, "", "SOAP-ENV:Body", null);

        if (methodName != null) {

            endMethod=true;

            if (targetURI != null) {
                xmlWriter.startElement(targetURI, methodName, methodName, null);
            } else {
                xmlWriter.startElement(SOAPConstants.SOAP_NAMESPACE, methodName, methodName, null);
            }

        }

        // Create the BaseEncoder to use
        DefaultEncoder encoder = new DefaultEncoder();
        encoder.setSchemas(getSchema(), getSchemaInstance(), getEncodingStyle());
        encoder.setXmlWriter(xmlWriter);

        for (int i = 0; body != null && i < body.size(); i++) {

            if (body.elementAt(i) instanceof Object[]) {

                String name = (String)((Object[]) body.elementAt(i))[0];
                Object value = (Object)((Object[]) body.elementAt(i))[1];

                encoder.objectToXML(name, value);

            } else {
                // unsupported body type
            }

        }

        //End the methodName, if necessary
        if (endMethod) {
            xmlWriter.endElement(targetURI, methodName, methodName);
        }

        // End the Body element
        xmlWriter.endElement(SOAPConstants.SOAP_NAMESPACE, "", "SOAP-ENV:Body");

        if (encoder != null) {
            encoder = null;
        }

    } //writeBody

    /**
     *  Takes the response from the service as a byte[] and converts
     *   it into Java representation
     *
     * @param response The response from the service
     * @param registry TypeMappingRegistry mapping
     *
     * @throws IOException if any error occurs in parsing
     * @throws SOAPException if any error occurs during encoding
     * @throws Exception any other unusual exceptions.
     */
    public void xmlToObject(byte[] response, Encodeable soapObject)
            throws IOException, Exception {

        if (response == null) {
            return;
        }

        // Create the BaseEncoder to use
        DefaultEncoder encoder = new DefaultEncoder();
        encoder.setSchemas(getSchema(), getSchemaInstance(), getEncodingStyle());
        encoder.setReturnObject(soapObject);

        encoder.xmlToObject(response);

    }

    /**
     * For RPC style response, the parameter is wrapped with an element.
     *
     * @return String the element name that is wrapped around the parameters
     */
    public String getReturnOperationName() {
        return methodName;
    }

    /**
     * Returns the namespace associated with the return operation.
     *
     * @return String the namespace associated with the return operation name
     */
    public String getReturnNamespace() {
        return targetURI;
    }

}

