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
 * Encapsulates a Fault element in a SOAP message.
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class SOAPFault extends Exception {

    /** The xml response */
    private byte[] response;

    /** The fault code */
    private String faultString;

    /** The fault string */
    private int faultCode;

    /**
     * Creates a new instance of SOAPFault.
     *
     * @param fault instance of a FaultObject
     */
    public SOAPFault(String message, byte[] response) {

        super(message);
        this.response = response;

    }

    /**
     *
     * @return
     */
    public byte[] getResponse() {
        return response;
    }

    /**
     * Retrieves the <faultcode> element in the Fault element of a SOAP message.
     *
     * @return String with the faultcode, null if no faultcode.
     */
    public int getFaultCode() {
        return faultCode;
    }

    /**
     * Sets the faultCode
     *
     * @return String with the faultcode, null if no faultcode.
     */
    public void setFaultCode(int faultCode) {
        this.faultCode = faultCode;
    }

    /**
     * Gets the faultString
     *
     * @return String with the faultstring, null if no faultstring.
     */
    public String getFaultString() {
        return faultString;
    }

    /**
     * Sets the faultString
     *
     * @param String with the faultString
     */
    public void setFaultString(String faultString) {
        this.faultString = faultString;
    }

}

