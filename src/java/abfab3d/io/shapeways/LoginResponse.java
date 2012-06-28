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

package abfab3d.io.shapeways;

// External Imports
// none

// Internal Imports
import abfab3d.io.soap.encoders.DefaultObject;

/**
 * Encapsulates the Login data retrieved from the web service
 *
 * @author Russell Dodds
 * @version $Revision 1.1$
 */
public class LoginResponse extends DefaultObject {

    /**
     * Constructor
     */
    public LoginResponse() {
        soapElementName = "loginReturn";
        soapElementType = "loginReturn";
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Gets the sessionId value.
     *
     * @return sessionId
     */
    public String getSessionId() {
        return (String)getProperty("loginReturn");
    }

    /**
     * Sets the sessionId value.
     *
     * @param sessionId
     */
    public void setSessionId(String sessionId) {
        setProperty("loginReturn", sessionId);
    }

}
