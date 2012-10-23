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

package app.common;

import java.util.*;
import java.io.*;

/**
 * Log the output of a stream to another stream.
 *
 * @author Alan Hudson
 */
public class StreamLogger extends Thread {
    /** The input stream to read */
    private InputStream is;

    /** The stream to write results to */
    private PrintStream os;

    /** Should the stream be duplicated to another */
    private PrintStream dup;

    StreamLogger(InputStream is, PrintStream os) {
        this.is = is;
        this.os = os;
        dup = null;
    }

    /**
     * Constructor.
     *
     * @param is The inputstream
     * @param os Location to redirect to
     * @param dup Stream to duplicate out too or null to not
     */
    StreamLogger(InputStream is, PrintStream os, PrintStream dup) {
        this.is = is;
        this.os = os;
        this.dup = dup;
    }

    /**
     * Listen for changes on the stream.
     */
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ( (line = br.readLine()) != null) {
                os.println(line);
                if (dup != null)
                    dup.println(line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(os);
        }
    }
}
