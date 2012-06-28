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
 * SOAPException class extends Exception
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class SOAPException extends Exception
{

    /**
     * Creates instance of SOAPException.
     *
     * @param message the message to encapsulate as an Exception.
     */
    public SOAPException(String message) {
        super(message);
    }

}
