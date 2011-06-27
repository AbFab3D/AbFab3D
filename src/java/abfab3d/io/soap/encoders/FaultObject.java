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

import abfab3d.io.soap.encoders.*;

public class FaultObject extends DefaultObject {

    public FaultObject() {
        soapElementName = "Fault";
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Gets the faultcode value.
     *
     * @return faultcode
     */
    public int getFaultCode() {
        return Integer.parseInt((String)getProperty("faultcode"));
    }

    /**
     * Sets the faultcode value.
     *
     * @param faultcode
     */
    public void setFaultCode(String faultCode) {
        setProperty("faultcode", faultCode);
    }

    /**
     * Gets the faultstring value.
     *
     * @return faultstring
     */
    public String getFaultString() {
        return (String)getProperty("faultstring");
    }


    /**
     * Sets the faultstring value.
     *
     * @param faultstring
     */
    public void setFaultString(String faultString) {
        setProperty("faultstring", faultString);
    }

}
