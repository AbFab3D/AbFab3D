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

package abfab3d.util;

// External imports
// None

// Local imports
import org.j3d.util.ErrorReporter;

/**
 * An implementation of the ErrorReporter interface that just writes everything
 * to System.err.
 * <p>
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class SysErrorReporter implements ErrorReporter {

    /** The log level needed to get all messages printed out. */
    public static final int PRINT_ALL = 0;

    /** The log level needed to print out warnings and worse. */
    public static final int PRINT_WARNINGS = 1;

    /** The log level needed to print out errors and worse. */
    public static final int PRINT_ERRORS = 2;

    /** The log level needed to print out only fatal errors. */
    public static final int PRINT_FATAL_ERRORS = 3;

    /** Don't print out any messages */
    public static final int PRINT_NONE = 1000000;

    /**
     * The current log level of the reporter. Higher number means only the more
     * severe messages are printed out. A level of 0 prints all messages.
     */
    private int logLevel;

    /**
     * Creates a new, default instance of the reporter that will print all
     * messages to the output.
     */
    public SysErrorReporter() {
        this(PRINT_ALL);
    }

    /**
     * Creates a new, that will print messages of the given level to the output.
     *
     * @param level One of the error level constants
     */
    public SysErrorReporter(int level) {
        logLevel = level;
    }

    //-----------------------------------------------------------------------
    // Methods defined by ErrorReporter
    //-----------------------------------------------------------------------

    /**
     * Notification of an partial message from the system.  When being written
     * out to a display device, a partial message does not have a line
     * termination character appended to it, allowing for further text to
     * appended on that same line.
     *
     * @param msg The text of the message to be displayed
     */
    public void partialReport(String msg) {
        if(logLevel < PRINT_WARNINGS)
            System.err.print(msg);
    }

    /**
     * Notification of an informational message from the system. For example,
     * it may issue a message when a URL cannot be resolved.
     *
     * @param msg The text of the message to be displayed
     */
    public void messageReport(String msg) {
        if(logLevel < PRINT_WARNINGS) {
            System.err.print("Message: ");
            System.err.println(msg);
        }
    }

    /**
     * Notification of a warning in the way the system is currently operating.
     * This is a non-fatal, non-serious error. For example you will get an
     * warning when a value has been set that is out of range.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     */
    public void warningReport(String msg, Exception e) {
        warningReport(msg, (Throwable) e);
    }

    /**
     * Notification of a warning in the way the system is currently operating.
     * This is a non-fatal, non-serious error. For example you will get an
     * warning when a value has been set that is out of range.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     */
    public void warningReport(String msg, Throwable e) {
        if(logLevel < PRINT_ERRORS) {
            System.err.print("Warning: ");
            System.err.println(msg);

            if(e != null)
                e.printStackTrace(System.err);
        }
    }

    /**
     * Notification of a recoverable error. This is a serious, but non-fatal
     * error, for example trying to add a route to a non-existent node or the
     * use of a node that the system cannot find the definition of.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     */
    public void errorReport(String msg, Exception e) {
        errorReport(msg, (Throwable) e);
    }

    /**
     * Notification of a recoverable error. This is a serious, but non-fatal
     * error, for example trying to add a route to a non-existent node or the
     * use of a node that the system cannot find the definition of.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     */
    public void errorReport(String msg, Throwable e) {
        if(logLevel < PRINT_FATAL_ERRORS) {
            System.err.print("Error: ");
            System.err.println(msg);

            if(e != null)
                e.printStackTrace(System.err);
        }
    }

    /**
     * Notification of a non-recoverable error that halts the entire system.
     * After you recieve this report the runtime system will no longer
     * function - for example a non-recoverable parsing error. The best way
     * out is to reload the file or restart the application internals.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     */
    public void fatalErrorReport(String msg, Exception e) {
        fatalErrorReport(msg, (Throwable) e);
    }

    /**
     * Notification of a non-recoverable error that halts the entire system.
     * After you recieve this report the runtime system will no longer
     * function - for example a non-recoverable parsing error. The best way
     * out is to reload the file or restart the application internals.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     */
    public void fatalErrorReport(String msg, Throwable e) {
        if(logLevel < PRINT_NONE) {
            System.err.print("Fatal Error: ");
            System.err.println(msg);

            if(e != null)
                e.printStackTrace(System.err);
        }
    }

    //-----------------------------------------------------------------------
    // Local Methods
    //-----------------------------------------------------------------------

    /**
     * Change the current output level to the new value.
     *
     * @param level The new level to use for output
     */
    public void setLogLevel(int level) {
        logLevel = level;
    }

    /**
     * Fetch the currently set log level.
     *
     * @return Whatever the currently set log level is
     */
    public int getLogLevel() {
        return logLevel;
    }
}
