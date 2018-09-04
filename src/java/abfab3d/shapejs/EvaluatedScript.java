/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2016
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/
package abfab3d.shapejs;

import abfab3d.param.BaseParameterizable;
import abfab3d.param.Parameter;
import abfab3d.param.Parameterizable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static abfab3d.core.Output.printf;

/**
 * A ShapeJS script that has been evaluated
 *
 * @author Alan Hudson
 */
public class EvaluatedScript {
    
    final boolean DEBUG = true;

    /** Result of executing the scripts main */
    private Parameterizable m_result;

    /** Parameters defined in the script */
    private Map<String, Parameter> scriptParams;

    /** Output from any prints in the script */
    private String[] printLogs;

    /** Execution errors */
    private List<Map<String, String>> errorLogs;

    /** Execution warnings */
    private List<Map<String, String>> warningLogs;

    /** How long did the script take */
    private long evalTime;

    /** Was the execution successful */
    private boolean success;

    /** The javascript code */
    private String code;

    /** Script compile information */
    private int opCount;
    private int opSize;
    private int dataSize;

    public EvaluatedScript(boolean success, String code, Parameterizable scene, String[] printLogs, List<Map<String, String>> warningLogs,
                           List<Map<String, String>> errorLogs, long evalTime) {
        m_result = scene;
        this.code = code;
        this.printLogs = printLogs;
        this.warningLogs = warningLogs;
        this.errorLogs = errorLogs;
        this.evalTime = evalTime;
        this.success = success;
    }

    public EvaluatedScript(boolean success, String code, Parameterizable scene, String[] printLogs, List<Map<String, String>> warningLogs,
                           List<Map<String, String>> errorLogs, Map<String, Parameter> scriptParams, long evalTime) {

        this(success, code, scene, printLogs, warningLogs, errorLogs, evalTime);

        this.scriptParams = scriptParams;
    }

    public EvaluatedScript(List<Map<String, String>> errorLogs, long evalTime) {
        this.errorLogs = errorLogs;
        this.evalTime = evalTime;
        this.success = false;
    }

    public EvaluatedScript(ShapeJSErrors.ErrorType type) {
        this.evalTime = 0;
        this.success = false;
        this.errorLogs = new ArrayList<Map<String, String>>();
        addErrorLog(type);
    }

    public EvaluatedScript(ShapeJSErrors.ErrorType type, String msg) {
        this.evalTime = 0;
        this.success = false;
        this.errorLogs = new ArrayList<Map<String, String>>();
        addErrorLog(type, msg);
    }

    public EvaluatedScript(ShapeJSErrors.ErrorType type, long evalTime) {
        this.evalTime = evalTime;
        this.success = false;
        this.errorLogs = new ArrayList<Map<String, String>>();
        addErrorLog(type);
    }

    public EvaluatedScript(ShapeJSErrors.ErrorType type, String[] args, long evalTime) {
        this.evalTime = evalTime;
        this.success = false;
        this.errorLogs = new ArrayList<Map<String, String>>();
        addErrorLog(type, args);
    }

    public EvaluatedScript(ShapeJSErrors.ErrorType type, String msg, String[] printLogs, long evalTime) {
        this.evalTime = evalTime;
        this.success = false;
        this.errorLogs = new ArrayList<Map<String, String>>();
        this.printLogs = printLogs;

        addErrorLog(type, msg);
    }

    public Map<String, Parameter> getScriptParams() {
        return scriptParams;
    }

    public String getCode() {
        return code;
    }

    public Parameterizable getResult() {
        return m_result;
    }

    public String[] getPrintLogs() {
        return printLogs;
    }


    public long getEvalTime() {
        return evalTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean value) {
        this.success = value;;
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


    public List<Map<String, String>> getWarningLogs() {
        return warningLogs;
    }

    public List<Map<String, String>> getErrorLogs() {
        return errorLogs;
    }

    /**
     * Add an error log with the specified type.
     *
     * @param type The error type
     */
    public void addErrorLog(ShapeJSErrors.ErrorType type) {
        if (errorLogs == null) {
            errorLogs = new ArrayList<Map<String, String>>();
        }

        String log = ShapeJSErrors.getErrorMsg(type, null);
        if (log != null) {
            Map<String, String> errorMap = new HashMap<String, String>();
            errorMap.put("type", type.toString());
            errorMap.put("msg", log);
            errorLogs.add(errorMap);
        }
    }

    /**
     * Add an error log with the specified type and arguments for error message.
     *
     * @param type The error type
     * @param args The arguments for the error message
     */
    public void addErrorLog(ShapeJSErrors.ErrorType type, String[] args) {
        if (errorLogs == null) {
            errorLogs = new ArrayList<Map<String, String>>();
        }

        String log = ShapeJSErrors.getErrorMsg(type, args);
            
        if (log != null) {
            Map<String, String> errorMap = new HashMap<String, String>();
            errorMap.put("type", type.toString());
            errorMap.put("msg", log);
            errorLogs.add(errorMap);
        }
    }

    /**
     * Add an error log with the specified type and error message.
     *
     * @param type The error type
     * @param msg The message
     */
    public void addErrorLog(ShapeJSErrors.ErrorType type, String msg) {
        if (errorLogs == null) {
            errorLogs = new ArrayList<Map<String, String>>();
        }
        if(DEBUG) printf("EvaluatedScript.addErrorLog(%s, %s)\n", type, msg);
        Map<String, String> errorMap = new HashMap<String, String>();
        errorMap.put("type", type.toString());
        errorMap.put("msg", msg);
        errorLogs.add(errorMap);
    }

}
