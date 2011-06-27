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

package abfab3d.io.soap.transport;

import java.io.*;

import abfab3d.io.soap.*;

/**
 * Interface for all the transport implementations.
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public interface Transport {

    /**
     * Initialize the transport to the given URL and action
     *
     * @param soapURL the URL to connect to.
     * @param soapAction the value for SOAPAction HTTP header, may be null
     */
    public void initialize(String soapURL, String soapAction);

    /**
     * Attach a {@link TransportProgressMonitor} to this Transport. This monitor
     * will be notified with updates as to call progress.
     *
     * @param monitor The monitor to be notified; this monitor replaces any
     *        current monitor; setting this to <code>null</code> effectively
     *        clears the monitor
     */
    public void setProgressMonitor(TransportProgressMonitor monitor);

    /**
     * Method to open a connection to a server and send the request.
     *
     * @param request The XML SOAP message converted to a byte array
     * @return A byte stream from the SOAP server representing the response;
     * null if no response is returned.
     *
     * @throws SOAPFault if any faults are returned by the web service.
     * @throws SOAPException if response is not XML or other SOAP parsing issues.
     * @throws IOException if any error occurs while connecting to the server.
     */
    public byte[] call(byte[] request) throws SOAPFault, SOAPException, IOException;

}
