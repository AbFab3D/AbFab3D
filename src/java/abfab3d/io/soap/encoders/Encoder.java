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

import java.io.IOException;

import abfab3d.io.soap.*;

/**
 * Each encoder should implement this methods
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public interface Encoder {

    /**
     * Set XML writer.
     *
     * @param xmlWriter The writer to use
     */
    public void setXmlWriter(XMLWriter xmlWriter);

    /**
     * Set the return object.
     *
     * @param returnObject The object to return
     */
    public void setReturnObject(Encodeable returnObject);

    /**
     * Converts a parameter specified by the user using Envelope.setBody(String, Object) to XML.
     *
     * @param paramName the name of the parameter.
     * @param paramObject the value of the parameter.
     *
     * @throws Exception if any other error occurs.
     */
    public void objectToXML(String paramName, Object paramObject) throws Exception;

    /**
     * Parse a SOAP packet to produce an Encodeable Object.
     *
     * @param response The byte array that is the SOAP packet
     *
     * @throws IOException if any error occurs in parsing the XML.
     * @throws Exception if any other error occurs.
     */
    public void xmlToObject(byte[] response) throws IOException, Exception;

}
