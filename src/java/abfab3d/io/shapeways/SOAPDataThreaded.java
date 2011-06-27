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

// External imports
// None

// Local imports
import abfab3d.io.soap.transport.HTTPTransport;

/**
 * Thread local version of HTTPTransport to make IOManager calls thread safe.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class SOAPDataThreaded extends ThreadLocal<HTTPTransport> {
    /**
     * Initialize the thread specific instance.
     */
    protected synchronized HTTPTransport initialValue() {
        return new HTTPTransport();
    }
}
