/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package shapejs;

import abfab3d.util.DataSource;

/**
 * Result of evaluating a ShapeJS script from Javascript.
 *
 * @author Alan Hudson
 */
public class EvalResult {
    /** The result datasource tree */
    private DataSource datasource;

    /** Output from any prints in the script */
    private String printLog;

    /** Execution errors */
    private String errorLog;

    /** How long did the script take */
    private long execTime;

    /** Was the execution successful */
    private boolean success;

    public EvalResult(boolean success,DataSource datasource, String printLog, String errorLog, long execTime) {
        this.datasource = datasource;
        this.printLog = printLog;
        this.errorLog = errorLog;
        this.execTime = execTime;
        this.success = success;
    }

    public DataSource getDataSource() {
        return datasource;
    }

    public String getPrintLog() {
        return printLog;
    }

    public String getErrorLog() {
        return errorLog;
    }

    public long getExecTime() {
        return execTime;
    }

    public boolean isSuccess() {
        return success;
    }
}
