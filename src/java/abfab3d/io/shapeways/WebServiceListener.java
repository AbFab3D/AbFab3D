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

// Standard Imports

// Application specific imports

/**
 * A listener for Session tasks.
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public interface WebServiceListener {

    /**
     * The session has timed out, relogin needed.
     *
     * @param message The message returned by the web service
     * @return username and password
     */
    public Object[] sessionTimeout(String message);

    /**
     * The web service request failed, display warning
     *
     * @param message The message to display to the user
     */
    public void requestFailed(String message);

}
