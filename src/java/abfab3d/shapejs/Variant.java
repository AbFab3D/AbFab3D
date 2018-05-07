/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2018
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.shapejs;

import abfab3d.core.ResultCodes;
import abfab3d.param.ParamJson;
import abfab3d.param.Parameter;
import abfab3d.param.ParameterType;
import abfab3d.param.Parameterizable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.corba.se.impl.ior.NewObjectKeyTemplateBase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.*;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;


/**
 * A ShapeJS main script + parameters.  A variant on the design such a different ring sizes, styles etc.
 *
 * @author Alan Hudson
 */
public class Variant  {

    static final boolean DEBUG = false;

    public static final String EXT_PNG = ".png";
    public static final String EXT_SHAPE_JS = ".shapejs";
    public static final String EXT_JS = ".js";
    public static final String EXT_JSON = ".json";
    public static final String EXT_JSON_PNG = ".json.png";
    public static final String SCRIPT_PATH = "scriptPath";
    public static final String SCRIPT_PARAMS = "scriptParams";
    public static final String NEW_DESIGN = "new design";
    public static final String UNDEFINED = "undefined";
    ScriptManager m_sm = ScriptManager.getInstance();

    private String m_jobID = UUID.randomUUID().toString();

    private String m_scriptPath = UNDEFINED;
    private String m_designPath = NEW_DESIGN;

    private Parameterizable m_scene;

    EvaluatedScript m_evaluatedScript;

    String m_errorMessages[] = new String[0];
    
    private String script;
    
    private Map<String,Object> variantParams;


    /**
     * read design from specified path
     */
    public int read(String basedir,String path) throws IOException, NotCachedException {

        if (path.toLowerCase().endsWith(EXT_JS) || path.toLowerCase().endsWith(EXT_SHAPE_JS)) {
            return readScript(path);
        } else if (path.toLowerCase().endsWith(EXT_JSON)) {
            return readDesign(basedir,path);
        } else if (path.toLowerCase().endsWith(EXT_JSON_PNG)) {
            // thumbnail?
            return readDesign(basedir,path.substring(0, path.toLowerCase().lastIndexOf(EXT_PNG)));
        } else {
            throw new RuntimeException(fmt("unknown file type:%s", path));
        }
    }

    /**
     * read design file (in JSON format)
     *
     * @return Result.SUCCESS
     */
    public int readDesign(String basedir,String path) throws IOException, NotCachedException {

        if (DEBUG) printf("ShapeJSDesign.readDesign(%s)\n", path);
        clearMessages();

        File file = new File(path);
        String design = FileUtils.readFileToString(file);

        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(design).getAsJsonObject();
        Object spathObj = obj.get(SCRIPT_PATH);
        if (spathObj == null) throw new IllegalArgumentException("Variant missing scriptPath: " + path);
        String spath = obj.get(SCRIPT_PATH).getAsString();
        JsonObject oparams = obj.get(SCRIPT_PARAMS).getAsJsonObject();
        if (DEBUG) printf("script path: %s\n", spath);
        if (DEBUG) printf("params: %s\n", oparams);
        if (spath == null) {
            m_scriptPath = null;
            throw new RuntimeException("scriptPath is undefined");
        }
        if (oparams == null) {
            throw new RuntimeException("script params undefined");
        }

        String aspath = resolvePath(file, new File(spath));

        // load fresh script, to reset params default values

        if (DEBUG) printf("reading new script:%s\n", aspath);

        // empty param map
        LinkedHashMap<String, Object> paramMap = new LinkedHashMap<>();

        script = FileUtils.readFileToString(new File(aspath));
        ScriptResources sr;

        sr = m_sm.prepareScript(m_jobID, basedir,script, paramMap);
        
        if (!sr.evaluatedScript.isSuccess()) {
            printScriptError(sr);
            throw new RuntimeException(fmt("failed to prepare script", aspath));
        }

        try {
            // Reset the params to their default value
            m_sm.resetParams(m_jobID);
        } catch(NotCachedException nce) {
            // ignore
        }


        if (DEBUG) printParamsMap("after first prepareScript", paramMap);
        Map<String, Parameter> scriptParams = sr.getParams();
        ParamJson.getParamValuesFromJson(oparams, scriptParams);
        Map<String, Object> uriParams = resolveURIParams(file, sr.getParams());
        //

        // this needed for params conversion
        
        // Don't reprocess uri parameters with relative path
        boolean skipRelativePath = true;

        sr = m_sm.updateParams(m_jobID, uriParams, skipRelativePath);
        sr = m_sm.executeScript(sr);

        if (!sr.evaluatedScript.isSuccess()) {
            printScriptError(sr);
            throw new RuntimeException(fmt("failed to execute script", aspath));
        }
        m_designPath = path;
        m_scriptPath = aspath;
        m_evaluatedScript = sr.evaluatedScript;
        m_scene = m_evaluatedScript.getResult();
        return ResultCodes.RESULT_OK;

    }

    /**
     * read design file (in JSON format)
     *
     * @return Result.SUCCESS
     */
    public int readDesign(String basedir,String variantdir,Reader design) throws IOException, NotCachedException {

        clearMessages();

        File file = new File(variantdir);

        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(design).getAsJsonObject();
        Object spathObj = obj.get(SCRIPT_PATH);
        if (spathObj == null) throw new IllegalArgumentException("Variant missing scriptPath");
        String spath = obj.get(SCRIPT_PATH).getAsString();
        JsonObject oparams = obj.get(SCRIPT_PARAMS).getAsJsonObject();
        if (DEBUG) printf("script path: %s\n", spath);
        if (DEBUG) printf("params: %s\n", oparams);
        if (spath == null) {
            m_scriptPath = null;
            throw new RuntimeException("scriptPath is undefined");
        }
        if (oparams == null) {
            throw new RuntimeException("script params undefined");
        }

        String aspath = resolvePath(file, new File(spath));

        // load fresh script, to reset params default values

        if (DEBUG) printf("reading new script:%s\n", aspath);

        // empty param map
        LinkedHashMap<String, Object> paramMap = new LinkedHashMap<>();

        script = FileUtils.readFileToString(new File(aspath));
        ScriptResources sr;

        sr = m_sm.prepareScript(m_jobID, basedir,script, paramMap);

        if (!sr.evaluatedScript.isSuccess()) {
            printScriptError(sr);
            throw new RuntimeException(fmt("failed to prepare script", aspath));
        }

        try {
            // Reset the params to their default value
            m_sm.resetParams(m_jobID);
        } catch(NotCachedException nce) {
            // ignore
        }

        if (DEBUG) printParamsMap("after first prepareScript", paramMap);
        Map<String, Parameter> scriptParams = sr.getParams();
        ParamJson.getParamValuesFromJson(oparams, scriptParams);
        Map<String, Object> uriParams = resolveURIParams(file, sr.getParams());
        //

        // this needed for params conversion

        // Don't reprocess uri parameters with relative path
        boolean skipRelativePath = true;
        sr = m_sm.updateParams(m_jobID, uriParams, skipRelativePath);
        sr = m_sm.executeScript(sr);

        if (!sr.evaluatedScript.isSuccess()) {
            printScriptError(sr);
            throw new RuntimeException(fmt("failed to execute script", aspath));
        }
        m_designPath = NEW_DESIGN;
        m_scriptPath = aspath;
        m_evaluatedScript = sr.evaluatedScript;
        m_scene = m_evaluatedScript.getResult();
        return ResultCodes.RESULT_OK;

    }

    /**
     * read design file (in JSON format)
     *
     * @return Result.SUCCESS
     */
    /*
    public int readDesign(String basedir,String path, Map<String,Object> params) throws IOException, NotCachedException {

        if (DEBUG) printf("ShapeJSDesingn.readDesign(%s)\n", path);
        clearMessages();
        File file = new File(path);
        String design = FileUtils.readFileToString(file);

        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(design).getAsJsonObject();
        Object spathObj = obj.get(SCRIPT_PATH);
        if (spathObj == null) throw new IllegalArgumentException("Variant missing scriptPath: " + path);
        String spath = obj.get(SCRIPT_PATH).getAsString();
        JsonObject oparams = obj.get(SCRIPT_PARAMS).getAsJsonObject();
        if (DEBUG) printf("script path: %s\n", spath);
        if (DEBUG) printf("params: %s\n", oparams);
        if (spath == null) {
            m_scriptPath = null;
            throw new RuntimeException("scriptPath is undefined");
        }
        if (oparams == null) {
            throw new RuntimeException("script params undefined");
        }

        String aspath = resolvePath(file, new File(spath));

        // load fresh script, to reset params default values

        if (DEBUG) printf("reading new script:%s\n", aspath);

        // empty param map
        LinkedHashMap<String, Object> paramMap = new LinkedHashMap<>();

        script = FileUtils.readFileToString(new File(aspath));
        ScriptResources sr;
        sr = m_sm.prepareScript(m_jobID, basedir,script, paramMap);

        if (!sr.evaluatedScript.isSuccess()) {
            printScriptError(sr);
            throw new RuntimeException(fmt("failed to prepare script", aspath));
        }

        if (DEBUG) printParamsMap("after first prepareScript", paramMap);
        Map<String, Parameter> scriptParams = sr.getParams();
        ParamJson.getParamValuesFromJson(oparams, scriptParams);
        Map<String, Object> uriParams = resolveURIParams(file, sr.getParams());
        //

        // this needed for params conversion

        // Don't reprocess uri parameters with relative path
        boolean skipRelativePath = true;
        sr = m_sm.updateParams(m_jobID, uriParams, skipRelativePath);
        sr = m_sm.executeScript(sr);

        if (!sr.evaluatedScript.isSuccess()) {
            printScriptError(sr);
            throw new RuntimeException(fmt("failed to execute script", aspath));
        }

        ParamJson.getParamValueFromJson(params,scriptParams);
        m_designPath = path;
        m_scriptPath = aspath;
        m_evaluatedScript = sr.evaluatedScript;
        m_scene = m_evaluatedScript.getResult();
        return ResultCodes.RESULT_OK;

    }
*/

    /**
     * read design file (in JSON format) with an assigned job ID
     *
     * @return Result.SUCCESS
     */
    public int readDesign(String basedir,String path, String jobID) throws IOException, NotCachedException {
    	m_jobID = jobID;
    	return readDesign(basedir, path);
    }

    /**
     * read new script file
     */
    public int readScript(String path) throws IOException {

        clearMessages();
        File fpath = new File(path);
        script = FileUtils.readFileToString(fpath);

        String basedir = FilenameUtils.getPath(path);
        ScriptResources sr = m_sm.prepareScript(m_jobID, basedir,script, null);

        if (!sr.evaluatedScript.isSuccess()) {
            if (DEBUG) printf("failed to prepareScript()\n");
            printScriptError(sr);
            return ResultCodes.RESULT_ERROR;
        }

        Map<String, Parameter> params = sr.getParams();

        Map<String, Object> uriParams = resolveURIParams(fpath, params);

        if (uriParams.size() > 0) {
            sr = m_sm.prepareScript(m_jobID, null,(String) null, uriParams);
        }
        m_sm.executeScript(sr);

        if (!sr.evaluatedScript.isSuccess()) {
            printScriptError(sr);
            //throw new RuntimeException(fmt("failed to execute script", path));
            return ResultCodes.RESULT_ERROR;
        }

        m_evaluatedScript = sr.evaluatedScript;
        m_scene = m_evaluatedScript.getResult();
        m_scriptPath = path;
        m_designPath = NEW_DESIGN;
        return ResultCodes.RESULT_OK;
    }

    /**
     * read new script file
     */
    public int readScript(String path, String jobID) throws IOException {
    	m_jobID = jobID;
    	return readScript(path);
    }
    
    /**
     * reload script file which was changed
     */
    public int reloadScript() {

        clearMessages();
        if (DEBUG) printf("ShapeJSDesign.reloadScript(%s)\n", m_scriptPath);
        script = null;
        try {
            script = FileUtils.readFileToString(new File(m_scriptPath));
        } catch (Exception e) {
            throw new RuntimeException(fmt("error reading file: %s\n", m_scriptPath));
        }

        ScriptResources sr = null;
        try {
            sr = m_sm.getResources(m_jobID);
            Map<String, Parameter> oldParams = sr.getParams();
            Map<String, Object> oldValues = convParamsToObjects(oldParams);

            sr = m_sm.prepareScript(m_jobID, null,script, null);

            // reapply old values.  Removed values will be ignored
            m_sm.updateParams(m_jobID, oldValues);
        } catch (NotCachedException nce) {
            // should not happen
            nce.printStackTrace();
        }

        if (!sr.evaluatedScript.isSuccess()) {
            printf("failed to prepareScript()\n");
            printScriptError(sr);
            return ResultCodes.RESULT_ERROR;
        }

        m_sm.executeScript(sr);
        if (!sr.evaluatedScript.isSuccess()) {
            printf("failed to executeScript()\n");
            printScriptError(sr);
            return ResultCodes.RESULT_ERROR;
        }
        m_evaluatedScript = sr.evaluatedScript;
        m_scene = m_evaluatedScript.getResult();
        return ResultCodes.RESULT_OK;
    }


    private Map<String, Object> convParamsToObjects(Map<String, Parameter> params) {
        HashMap<String, Object> ret_val = new HashMap<>();

        for (Parameter p : params.values()) {
            ret_val.put(p.getName(), p.getValue());
        }

        return ret_val;
    }


    /**
     * writes design into specified path
     */
    public int write(String path) {

        if (DEBUG) printf("writeDesign(%s)\n", path);
        File file = new File(path);
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            ScriptResources sr = m_sm.getResources(m_jobID);
            Map<String, Parameter> ap = sr.getParams();

            Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
            for (Parameter par : ap.values()) {
                String name = par.getName();
                Object val = par.getValue();
                if (par.getType() == ParameterType.URI && val != null) {
                    val = getRelativePath(file, new File((String) val));
                }

                Object jobj = ParamJson.getJsonValue(val, par.getType());
                paramMap.put(name, jobj);
            }

            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put(SCRIPT_PATH, getRelativePath(file, new File(m_scriptPath)));
            map.put(SCRIPT_PARAMS, paramMap);

            String str = gson.toJson(map);
            FileUtils.writeStringToFile(file, str, "UTF-8");
            m_designPath = path;
            return ResultCodes.RESULT_OK;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResultCodes.RESULT_ERROR;
    }

    public int save() {
        if (m_designPath == NEW_DESIGN)
            throw new RuntimeException("no designPath is given. Use \"Save As\"");
        return write(m_designPath);
    }


    static void printParams(String title, Parameter[] aparam) {
        printf("printParams(%s, count:%d)\n", title, aparam.length);
        for (int i = 0; i < aparam.length; i++) {
            Parameter par = aparam[i];
            printf("%s (%s) = %s\n", par.getName(), par.getType(), par.getValue());
        }

    }

    static void printParamsMap(String title, Map<String, Object> map) {
        printf("printParamsMap(%s, count: %d)\n", title, map.size());
        for (String s : map.keySet()) {
            Object value = map.get(s);
            if (value != null) printf("%s: %s(%s)\n", s, value.getClass().getSimpleName(), value);
            else printf("%s: null\n", s);
        }
    }

    void clearMessages() {
        m_errorMessages = new String[0];
    }

    void printScriptError(ScriptResources sr) {

        if (DEBUG) printf("printScriptError()\n");
        java.util.List<Map<String, String>> log = sr.evaluatedScript.getErrorLogs();
        if (DEBUG) printf(" log: %s\n", log);
        if (log == null) {
            m_errorMessages = new String[]{"error log is null"};
            return;
        }
        int i = 0;
        Vector<String> msg = new Vector<String>();
        for (Map<String, String> entry : log) {

            Map<String, String> map = entry;
            for (String key : map.keySet()) {
                //printf(" map:%d key: %s value: {%s}\n",i, key, map.get(key));
                msg.add(key + ":" + map.get(key));
            }
            i++;
            //printf("item: %d\n%s\n",i++, entry);
        }

        m_errorMessages = msg.toArray(new String[msg.size()]);

        printf(" end of printScriptError()\n");
    }

    public String[] getErrorMessages() {

        return m_errorMessages;
    }

    /**
     * should be called to update scene when single parameter is changed via UI
     */
    public int onScriptParamChanged(Parameter parameter) {

        if (false) printf("onScriptParamChanged(%s:%s)\n", parameter.getName(), parameter.getValue());
        try {
            LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
            params.put(parameter.getName(), parameter.getValue());

            ScriptResources sr = null;
            sr = m_sm.updateParams(m_jobID, params);
            m_sm.executeScript(sr);

            if (sr.evaluatedScript.isSuccess()) {
                m_evaluatedScript = sr.evaluatedScript;
                m_scene = m_evaluatedScript.getResult();
                return ResultCodes.RESULT_OK;
            } else {
                java.util.List<Map<String, String>> error = sr.evaluatedScript.getErrorLogs();
                for (Map<String, String> entry : error) {
                    printf("%s\n", entry);
                }
                printScriptError(sr);
                return ResultCodes.RESULT_ERROR;
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
        return ResultCodes.RESULT_ERROR;

    }
    
    /**
     * should be called to update scene when multiple parameters are changed
     */
    public int onScriptParamChanged(Map<String, Parameter> newParams) {
        try {
            Map<String, Object> params = convParamsToObjects(newParams);

            ScriptResources sr = null;
            sr = m_sm.updateParams(m_jobID, params);
            m_sm.executeScript(sr);

            if (sr.evaluatedScript.isSuccess()) {
                m_evaluatedScript = sr.evaluatedScript;
                m_scene = m_evaluatedScript.getResult();
                return ResultCodes.RESULT_OK;
            } else {
                java.util.List<Map<String, String>> error = sr.evaluatedScript.getErrorLogs();
                for (Map<String, String> entry : error) {
                    printf("%s\n", entry);
                }
                return ResultCodes.RESULT_ERROR;
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
        
        return ResultCodes.RESULT_ERROR;
    }
    
    public void setVariantParams(Map<String, Object> vParams) {
    	this.variantParams = vParams;
    }
    
    public Map<String, Object> getVariantParams() {
    	return this.variantParams;
    }

    /**
     * convert array of params into LinkedHashMap
     */
    public static LinkedHashMap<String, Object> getParamMap(Parameter aparam[]) {

        LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
        for (int i = 0; i < aparam.length; i++) {
            params.put(aparam[i].getName(), aparam[i].getValue());
        }
        return params;
    }

    public Parameterizable getResult() {
        return (Scene) m_scene;
    }

    public String getPath() {
        return m_designPath;
    }

    /**
     * @return path which can be used for history for re-loading
     */
    public String getHistoryPath() {
        if (hasPath())
            return m_designPath;
        else
            return m_scriptPath;
    }

    public String getScriptPath() {
        return m_scriptPath;
    }
    
    public String getScript() {
    	return script;
    }

    public String getScriptName() {
        return FilenameUtils.getBaseName(m_scriptPath);
    }

    public String getFileName() {
        return FilenameUtils.getBaseName(m_designPath);
    }

    public EvaluatedScript getEvaluatedScript() {
        return m_evaluatedScript;
    }

    /**
     * Not sure about this method yet, but most things requires Scene not a Parameterizable
     * @return
     */
    public Scene getScene() {
        return (Scene) m_scene;
    }

    public java.util.List<Parameterizable> getSource() {
        return ((Scene) m_scene).getSource();
    }

    public boolean hasPath() {
        return (m_designPath != NEW_DESIGN);
    }


    /**
     * enforce script update from script file
     */
    public void onScriptChanged() {

    }

    public static String getRelativePath(File f1, File f2) {
        Path p1 = f1.getAbsoluteFile().getParentFile().toPath();
        Path p2 = f2.getAbsoluteFile().toPath();
        if (DEBUG) printf("getRelativePath(\'%s\', \'%s\')\n", f1, f2);
        Path prel = p1.relativize(p2);
        //File f = prel.toFile();
        //return f.getCanonicalName();
        String str = prel.toString();
        str = str.replace('\\', '/');
        printf("path1: %s, path2: %s rel: %s\n", f1, f2, str);
        return str;

    }

    /**
     * makes absolute path from file f1 to f2
     * f2 may be absolute or relative
     */
    public static String resolvePath(File f1, File f2) {

        if (f2.isAbsolute())
        	try {
        		return f2.getCanonicalPath();
        	} catch (Exception e) {
        		return f2.getAbsolutePath();
        	}
        if (f2.exists()) {
            // relative to current working folder
        	try {
        		return f2.getCanonicalPath();
        	} catch (Exception e) {
        		return f2.getAbsolutePath();
        	}
        }
        Path p1 = f1.getAbsoluteFile().getParentFile().toPath();
        Path p2 = f2.toPath();
        Path ap = p1.resolve(p2);
        File f = ap.toFile();
        try {
            String str = f.getCanonicalPath();
            if (DEBUG) printf("path1: %s, path2: %s resolved: %s\n", f1, f2, str);
            return str;
        } catch (Exception e) {
        }
        return ap.toString();

    }

    //
    // resolve relative paths to absolute
    //
    static Map<String, Object> resolveURIParams(File parentFile, Map<String, Parameter> params) {

        printf("Resolvong URI params.  parent: %s\n",parentFile.getAbsoluteFile());

        HashMap<String, Object> ret_val = new HashMap<>();

        for (Parameter par : params.values()) {
            if (par.getType() == ParameterType.URI) {
                String parPath = (String) par.getValue();

                if (parPath != null) {
                    String newPath = resolvePath(parentFile, new File(parPath));
                    if (!newPath.equals(parPath)) {
                        ret_val.put(par.getName(), newPath);
                    }
                }
            }
        }

        return ret_val;
    }
}
