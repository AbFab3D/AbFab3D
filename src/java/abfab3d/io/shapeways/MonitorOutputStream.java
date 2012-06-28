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

//External Imports
import java.io.IOException;
import java.io.OutputStream;


//Internal Imports

/**
 * Watch an IOStream and send messages about status
 * <p>
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class MonitorOutputStream extends OutputStream  {

    /** The stream to monitor */
    private OutputStream ioStream;

    /** The size of the stream */
    private int contentLength;

    /** What is the type of message be watched */
    private IORequestType requestType;

    /** What is the user message to send with the progress */
    private String message;

    /** How far through are we */
    private int count;

    /** The communications manager */
    private IOManager ioManager;

    /** The rounded percentage complete working variable */
    private int percentage;

    /**
     * Constructs an object to monitor the progress of an input stream.
     *
     * @param parentComponent The component triggering the operation
     */
    public MonitorOutputStream(
            OutputStream ioStream,
            int contentLength,
            IORequestType requestType,
            String message) {

        this.ioStream = ioStream;
        this.contentLength = contentLength;
        this.requestType = requestType;
        this.message = message;

        count = 0;
        percentage = 0;

        ioManager = IOManager.getIOManager();;

    }

    //----------------------------------------------------------
    // Overrides OutputStream methods
    //----------------------------------------------------------

    /**
     * Overrides <code>OutputStream.read</code>
     */
    public void write(int b) throws IOException {

        // perform the read
        ioStream.write(b);

        // send an update of status
        updateStatus(1);

    }


    /**
     * Overrides <code>OutputStream.read</code>
     */
    public void write(byte b[]) throws IOException {

        // perform the read
        ioStream.write(b);

        // send an update of status
        updateStatus(b.length);

    }


    /**
     * Overrides <code>OutputStream.read</code>
     */
    public void write(byte b[],
                    int off,
                    int len) throws IOException {

        // perform the read
        ioStream.write(b, off, len);

        // send an update of status
        updateStatus(len);

    }

    /**
     * Overrides <code>OutputStream.close</code>
     */
    public void close() throws IOException {
        ioStream.close();
    }

    /**
     * Overrides <code>OutputStream.flush</code>
     */
    public void flush() throws IOException {
        ioStream.flush();
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Calculate the new percentage and send the notification out
     *
     * @param increment - The total bytes read this time
     */
    private void updateStatus(int increment) {

        // add to the count from the read
        count += increment;

        // calculate the percentage
        percentage = Math.round(((float)count / (float)contentLength) * 100.0f);

        // fire the notification
        ioManager.fireUpdateRequest(requestType, message, percentage);

    }

}
