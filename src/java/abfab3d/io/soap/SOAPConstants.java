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

package abfab3d.io.soap;

/**
 * SOAP Constants
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class SOAPConstants {

    /**
     * This class should not be instantiated
     */
    private SOAPConstants(){}

    /**
     * Constant representing the namespace for the
     * elements of a SOAP message.
     */
    public final static String SOAP_NAMESPACE =
        "http://schemas.xmlsoap.org/soap/envelope/";

    /**
     * Constant representing the default schema instance
     * Used if no specific schema instance is provided
     * in the Envelope
     */
    public final static String SOAP_SCHEMA_INSTANCE =
        "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * Constant representing the default schema.  Used
     * if no specific schema is provided in the Envelope.
     */
    public final static String SOAP_SCHEMA =
        "http://www.w3.org/2001/XMLSchema";

    /**
     * Constant representing the default encoding style.
     */
    public final static String SOAP_ENCODING_STYLE =
        "http://schemas.xmlsoap.org/soap/encoding/";

    /**
     * The namespace used, for serializing and deserializing
     * java.lang.Vector and java.lang.Hashtable.
     */
    public final static String DEFAULT_NAMESPACE =
        "http://xml.apache.org/xml-soap";

}