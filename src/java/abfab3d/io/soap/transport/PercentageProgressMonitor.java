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
 * <p>
 * This abstract implementation of a {@link TransportProgressMonitor} watches
 * all the notifications of bytes transferred and triggers
 * {@link #updatePercentage(int)} when the number of bytes transferred covers a
 * percentile of the total bytes to transfer. This way, when transferring large
 * numbers of bytes, notifications are confined to segments.
 *
 * <p>
 * This class also maintains state information to enforce the following call
 * order: (1) {@link #setBytesToTransfer(long)}, (2) zero or more calls to
 * {@link #bytesTransferred(long)} which may trigger calls to
 * {@link #updatePercentage(int)}, and (3) a call to {@link #transferComplete()}
 * . Calling (2) or (3) before (1) or calling anything after calling (3) will
 * throw {@link IllegalStateException}.
 *
 * @author D. Julian
 * @version $Revision: 1.4 $
 */
public abstract class PercentageProgressMonitor implements
        TransportProgressMonitor {

    private enum State {
        NEW, INITIALIZED, COMPLETED
    };

    private State state = State.NEW;

    private int totalBytes = 0;
    private int percentile = 0;
    private int threshold = 0;

    /**
     * Set the total number of bytes which will be sent.  This is the initializing
     * method and must be called before any of the others.
     *
     * @param bytes the number of bytes which will be sent
     * @see TransportProgressMonitor#setBytesToTransfer(long)
     */
    public final void setBytesToTransfer(final int bytes) {
        if (this.state != State.NEW) {
            throw new IllegalStateException(
                    "Monitor has already been initialized");
        }

        if (bytes < 1) {
            throw new IllegalArgumentException(
                    "may not set bytes to transfer to less than 1");
        }

        this.totalBytes = bytes;

        if (bytes < 100) {
            this.percentile = 1;
        } else {
            this.percentile = totalBytes / 100;
        }

        this.state = State.INITIALIZED;
    }

    /**
     * Called as bytes are being transferred. If the number of bytes has passed
     * another percentile, the current percentage is passed to
     * {@link #updatePercentage(int)}.
     *
     * @see TransportProgressMonitor#bytesTransferred(long)
     */
    public final void bytesTransferred(final int bytes) {
        if (this.state != State.INITIALIZED) {
            throw new IllegalStateException(
                    "Monitor in wrong state to receive updates: " + this.state);
        }

        if (bytes > this.totalBytes) {
            throw new IllegalArgumentException(
                    "may not report bytes transferred greater than total");
        }

        if (bytes > this.threshold) {
            int percentage = bytes / this.percentile;

            updatePercentage(percentage);

            if (percentage >= 99) {
                this.threshold = this.totalBytes;
            } else {
                this.threshold = (percentage + 1) * this.percentile;
            }
        }
    }

    /**
     * This method is called as the percentage of bytes transferred increases.
     * There is no guarantee it is called for every percentage (i.e., 100 times)
     * or at all.
     *
     * @param percentage the current percentage of the transfer which has been
     *        completed
     */
    public abstract void updatePercentage(int percentage);

    /**
     * Called when the transfer is complete. This calls
     * {@link #updatePercentage(int)}, providing 100 as the percentage.
     *
     * @see TransportProgressMonitor#transferComplete()
     */
    public final void transferComplete() {
        if (this.state != State.INITIALIZED) {
            throw new IllegalStateException(
                    "Monitor in wrong state to be set complete: " + this.state);
        }

        // we're done
        updatePercentage(100);

        this.state = State.COMPLETED;
    }
}
