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

/**
 * Classes implementing this interface will be notified by a {@link Transport}
 * how far along it is while its shipping bytes from here to wherever. For
 * example, in the case of {@link HTTPTransport}, while the bytes are being sent
 * to the foreign HTTP server.
 *
 * @author D. Julian
 * @version $Revision: 1.4 $
 */
public interface TransportProgressMonitor {

    /**
     * Before any bytes are sent, this method is called to set the number of
     * bytes that are intended to be sent.
     *
     * @param bytes the number of bytes which a particular call will send
     */
    public void setBytesToTransfer(int bytes);

    /**
     * This method is called while bytes are being sent to update how many bytes
     * have thus far been transferred. How often this is called is transport
     * dependent and there is no guarantee it will be called at all (e.g., all
     * bytes are sent in a single batch). Likewise, while this should never be
     * larger than the number of bytes specified in
     * {@link #setBytesToTransfer(long)}, it may not ever be called with the
     * number of bytes equal to that number. That's what
     * {@link #transferComplete(long)} is for.
     *
     * @param bytes the number of bytes
     */
    public void bytesTransferred(int bytes);

    /**
     * This is called by the {@link Transport} when all the bytes which can be
     * sent have been sent.
     */
    public void transferComplete();
}
