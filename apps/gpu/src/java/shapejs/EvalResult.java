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

import abfab3d.param.Parameter;
import abfab3d.util.DataSource;

import java.util.HashMap;
import java.util.Map;

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
    private long evalTime;

    /** Was the execution successful */
    private boolean success;

    /** Script compile information */
    private int opCount;
    private int opSize;
    private int dataSize;
    

    /** The parsed uiParams */
    private Map<String,Parameter> uiParams;

    public EvalResult(boolean success,DataSource datasource, String printLog, String errorLog, long evalTime) {
        this.datasource = datasource;
        this.printLog = printLog;
        this.errorLog = errorLog;
        this.evalTime = evalTime;
        this.success = success;
    }

    public EvalResult(boolean success,DataSource datasource, String printLog, String errorLog, Map<String,Parameter> uiParams, long evalTime) {
        this.datasource = datasource;
        this.printLog = printLog;
        this.errorLog = errorLog;
        this.evalTime = evalTime;
        this.success = success;
        setUIParams(uiParams);
    }

    public EvalResult(String errorLog, long evalTime) {
        this.errorLog = errorLog;
        this.evalTime = evalTime;
        this.success = false;
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

    public long getEvalTime() {
        return evalTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getOpCount() {
        return opCount;
    }

    public void setOpCount(int opCount) {
        this.opCount = opCount;
    }

    public int getOpSize() {
        return opSize;
    }

    public void setOpSize(int opSize) {
        this.opSize = opSize;
    }

    public int getDataSize() {
        return dataSize;
    }

    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }

    public void setUIParams(Map<String,Parameter> params) {
        if (uiParams == null) uiParams = new HashMap<String,Parameter>();

        uiParams.putAll(params);
    }

    public Map<String,Parameter> getUIParams() {
        return uiParams;
    }
}
