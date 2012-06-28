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

import abfab3d.io.soap.*;
import abfab3d.io.soap.encoders.*;
import abfab3d.io.soap.transport.Transport;

/**
 * Entry point for making a SOAP call. This class is used to send a
 * SOAP packet to the SOAP server.
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class Call {

    /** The SOAP Envelope to send in the RPC call */
    private Envelope envelope;

    /** The name of the soap method currently being called */
    private String methodName;

    /** URI to post the SOAP call */
    private String targetURI;

    /** display SOAP packets */
    private boolean debug = false;

    /**
     * Default constructor.  Creates a default Envelope that is used to
     * build up the SOAP packet
     */
    public Call () {
        envelope = new Envelope();
    }

    /**
     * Constructor using a custom SOAP Envelope
     */
    public Call(Envelope envelope) {
        this.envelope = envelope;
    }

    /**
     * Enable or disable on the fly printing of the debugging messages.
     *
     * @param state True to turn on the raw message printing
     */
    public void enableMessageDebug(boolean state) {
        debug = state;
    }

    /**
     * Allows the user to specify a Parameter name and value.
     * This parameter name and value is sent to the service as part of the
     * <SOAP-ENV:Body> element of a SOAP message.
     *
     * @param paramName the name of the parameter.
     * @param value the value of the parameter represented as a Java object.
     */
    public void addParameter(String paramName, Object value) {
        envelope.setBody(paramName, value);
    }

    /**
     * Sets the URI of the service on the server.
     *
     * @param uri URI for the service on the server.
     */
    public void setTargetObjectURI(String uri) {
        this.targetURI=uri;
    }

    /**
     * Sets the soap method to be requested
     *
     * @param methodName the name of the method
     */
    public void setMethodName(String methodName) {
        this.methodName=methodName;
    }

    /**
     * Send the soap packet to the server and get back the response.
     * The response is encapsulated in the Envelope
     *
     * @param transport The transport type (default is HTTP)
     * @param returnObject the response from the service
     *  encapsulated as an Encodeable object.
     */
    public void invoke(Transport transport, Encodeable returnObject)
        throws SOAPFault, Exception {

        if (envelope == null) {
            throw new Exception("Soap Envelope cannot be null");
        }

        // Create the soap XML to send
        byte[] request = envelope.objectToXML(methodName, targetURI);

        // DEBUG!!!
        printDebug("REQUEST", request);

        // Make the request
        try {

            byte[] response = transport.call(request);

            if (response == null)
                return;

            // DEBUG!!!
            printDebug("RESPONSE", response);

            // Process the response, create java objects as required
            Envelope responseEnvelope = new Envelope();
            responseEnvelope.xmlToObject(response, returnObject);

        } catch (SOAPFault sf) {

            printDebug("FAULT RESPONSE", sf.getResponse());

            // Process the response, create java objects as required
            Envelope responseEnvelope = new Envelope();
            FaultObject faultObject = new FaultObject();
            responseEnvelope.xmlToObject(sf.getResponse(), faultObject);

            sf.setFaultCode(faultObject.getFaultCode());
            sf.setFaultString(faultObject.getFaultString());

            if (debug) {
                System.out.println("faultObject.getFaultCode(): " + faultObject.getFaultCode());
                System.out.println("faultObject.getFaultString(): " + faultObject.getFaultString());
            }

            throw sf;

        }
    }

    /**
     * Print byte streams to output stream when debug is on.  If the amount of
     * data is truly huge, only the start and end will be printed.
     *
     * @param type the type of message this is
     * @param data the data to spool out
     */
    private void printDebug(String type, byte[] data) {
        if (this.debug) {
            // where to print e.g., System.out or System.err
            final java.io.PrintStream ps = System.out;
            // how much beginning and end of data is printed
            // if data.length <= bufflen * 2 then the whole thing is printed
            final int bufflen = 1024;

            ps.print('[');
            ps.print(new java.util.Date().toString());
            ps.print("] ");
            ps.println(type);

            int pos = 0;
            int count = Math.min(bufflen, data.length);
            ps.write(data, 0, count);
            pos += count;

            if (pos < data.length) {
                pos = Math.max(bufflen, data.length - bufflen);
                int skipped = pos - count;
                if (skipped > 0) {
                    ps.println("");
                    ps.println("... skipping " + skipped + " bytes ...");
                }
                ps.write(data, pos, data.length - pos);
            }

            ps.println("");
        }
    }

}
