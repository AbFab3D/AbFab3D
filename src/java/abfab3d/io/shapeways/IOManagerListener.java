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

// Internal Imports

/**
 * A listener for IO tasks.
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public interface IOManagerListener {

    /**
     * A request had begun.
     *
     * @param type The type of request being processed
     * @param data The data to send to the end user
     */
    public void startRequest(IORequestType type, Object data);

    /**
     * A request had been updated but the percentage is to remain.
     *
     * @param type The type of request being processed
     * @param data The data to send to the end user
     */
    public void updateRequest(IORequestType type, Object data);

    /**
     * A request had been updated.
     *
     * @param type The type of request being processed
     * @param data The data to send to the end user
     * @param percentage The percent complete
     */
    public void updateRequest(IORequestType type, Object data, int precentage);

    /**
     * A request has completed.
     *
     * @param type The type of request being processed
     * @param data The data to send to the end user
     */
    public void endRequest(IORequestType type, Object data);

}
