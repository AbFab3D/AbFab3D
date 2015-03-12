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
    private long execTime;

    /** Was the execution successful */
    private boolean success;

    /** The number of instructions the script compiled too */
    private int instructions;

    /** The parsed uiParams */
    private Map<String,ParameterDefinition> uiParams;

    public EvalResult(boolean success,DataSource datasource, String printLog, String errorLog, long execTime) {
        this.datasource = datasource;
        this.printLog = printLog;
        this.errorLog = errorLog;
        this.execTime = execTime;
        this.success = success;
    }

    public EvalResult(boolean success,DataSource datasource, String printLog, String errorLog, Map<String,ParameterDefinition> uiParams, long execTime) {
        this.datasource = datasource;
        this.printLog = printLog;
        this.errorLog = errorLog;
        this.execTime = execTime;
        this.success = success;
        setUIParams(uiParams);
    }

    public EvalResult(String errorLog, long execTime) {
        this.errorLog = errorLog;
        this.execTime = execTime;
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

    public long getExecTime() {
        return execTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setInstructions(int inst) {
        instructions = inst;
    }

    public int getInstructions() {
        return instructions;
    }

    public void setUIParams(Map<String,ParameterDefinition> params) {
        if (uiParams == null) uiParams = new HashMap<String,ParameterDefinition>();

        uiParams.putAll(params);
    }

    public Map<String,ParameterDefinition> getUIParams() {
        return uiParams;
    }
}
