/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package shapejs.viewer;

import java.io.File;

import java.util.HashMap;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.ArrayList;

import java.nio.file.Path;

import abfab3d.shapejs.NotCachedException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import abfab3d.core.ResultCodes;

import abfab3d.param.StringListParameter;
import abfab3d.param.Parameterizable;
import abfab3d.param.ParamJson;
import abfab3d.param.Parameter;
import abfab3d.param.ParameterType;

import abfab3d.shapejs.EvaluatedScript;
import abfab3d.shapejs.ScriptManager;
import abfab3d.shapejs.ScriptResources;
import abfab3d.shapejs.Scene;


import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;


/**
   container to hold data for ShapeJS design 
   responsible for loading json file and associated scripts and other resources

   @author Vladimir Bulatov 
 */
public class ShapeJSDesign {

    static final boolean DEBUG = false;

    public static final String EXT_PNG = ".png"; 
    public static final String EXT_JS = ".js";
    public static final String EXT_SHAPEJS = ".shapejs"; 
    public static final String EXT_SHAPEVAR = ".shapevar"; 
    public static final String EXT_SHAPEVAR_PNG = ".shapevar.png"; 
    public static final String EXT_JSON = ".json"; 
    public static final String EXT_JSON_PNG = ".json.png";

    public static final String SCRIPT_PATH = "scriptPath";
    public static final String SCRIPT_PARAMS = "scriptParams";
    public static final String NEW_DESIGN = "new design";
    public static final String UNDEFINED = "undefined";
    
    ScriptManager m_sm = ScriptManager.getInstance();

    private String m_jobID =  UUID.randomUUID().toString();

    private String m_scriptPath = UNDEFINED;
    private String m_designPath = NEW_DESIGN;

    private Parameterizable m_scene;

    EvaluatedScript m_evaluatedScript;

    String  m_errorMessages[] = new String[0];
    
    boolean m_sandboxed = true;

    public ShapeJSDesign(boolean sandboxed){
        m_sandboxed = sandboxed;
    }

    /**
       @return true if path is desing file 
     */
    static boolean isDesignPath(String path){

        String low = path.toLowerCase();
        return low.endsWith(EXT_JSON) || low.endsWith(EXT_SHAPEVAR);
    }

    /**
       @return true if path is thumbnail file 
     */
    static boolean isThumbnailPath(String path){
        String low = path.toLowerCase();
        return low.endsWith(EXT_JSON_PNG) || low.endsWith(EXT_SHAPEVAR_PNG);
    }

    static boolean isScriptPath(String path){
        String low = path.toLowerCase();
        return low.endsWith(EXT_JS) || low.endsWith(EXT_SHAPEJS);
    }


    /**
       read design from specified path 
     */
    public int read(String path)throws Exception {
        String lowPath = path.toLowerCase();

        if(isScriptPath(lowPath)){

            return readScript(path);

        } else if(isDesignPath(lowPath)){

            return readDesign(path);            

        } else if(isThumbnailPath(lowPath)){

            // thumbnail ? 
            return readDesign(path.substring(0, lowPath.lastIndexOf(EXT_PNG)));            

        } else {

            throw new RuntimeException(fmt("unknown file type:%s", path));

        }        
    }
    
    /**
       read design file (in JSON format)
       @return Result.SUCCESS
     */
    public int readDesign(String path) throws Exception {

        if(DEBUG)printf("ShapeJSDesign.readDesign(%s)\n", path);
        clearMessages();
        File file = new File(path);
        String design = FileUtils.readFileToString(file);
        JsonParser parser = new JsonParser();        
        JsonObject obj = parser.parse(design).getAsJsonObject();
        String spath = obj.get(SCRIPT_PATH).getAsString();
        JsonObject oparams = obj.get(SCRIPT_PARAMS).getAsJsonObject();
        if(DEBUG)printf("script path: %s\n", spath);
        if(DEBUG)printf("params: %s\n", oparams);
        if(spath == null) {
            m_scriptPath = null;
            throw new RuntimeException("scriptPath is undefined");
        } 
        if(oparams == null) {
            throw new RuntimeException("script params undefined");
        }
        
        String aspath = resolvePath(file, new File(spath));
        
        // load fresh script, to reset params default values 

        if(DEBUG) printf("reading new script:%s\n", aspath);
        
        // empty param map 
        LinkedHashMap<String, Object> paramMap = new LinkedHashMap<>();
        
        String script = FileUtils.readFileToString(new File(aspath));
        ScriptResources sr;
        sr = m_sm.prepareScript(m_jobID, getLibDirs(), script, paramMap, m_sandboxed);     
        if(DEBUG)printParamsMap("after first prepareScript",paramMap);
        Map<String,Parameter> scriptParams = sr.getParams();
        ParamJson.getParamValuesFromJson(oparams, scriptParams);

        Map<String,Object> uriParams = resolveURIParams(file, sr.getParams());
        // 

        // this needed for params conversion
        sr = m_sm.updateParams(m_jobID, uriParams);
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
       read new script file 
     */
    public int readScript(String path) throws Exception {

        clearMessages();
        File fpath = new File(path);
        String script = FileUtils.readFileToString(fpath);
        
        m_jobID = UUID.randomUUID().toString();
        
        ScriptResources sr = m_sm.prepareScript(m_jobID, getLibDirs(), script, null, m_sandboxed);

        if(!sr.evaluatedScript.isSuccess()){
            if(DEBUG)printf("failed to prepareScript()\n");
            printScriptError(sr);
            return ResultCodes.RESULT_ERROR;            
        }

        Map<String,Parameter> params = sr.getParams();

        Map<String,Object> uriParams = resolveURIParams(fpath, params);

        if (uriParams.size() > 0) {
            sr = m_sm.prepareScript(m_jobID, getLibDirs(), (String) null, uriParams, m_sandboxed);
        }
        m_sm.executeScript(sr);

        if(!sr.evaluatedScript.isSuccess()){
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
       reload script file which was changed 
     */
    public int reloadScript() {

        clearMessages();
        if(DEBUG) printf("ShapeJSDesign.reloadScript(%s)\n", m_scriptPath);
        String script = null;
        try {
            script = FileUtils.readFileToString(new File(m_scriptPath));
        } catch(Exception e){
            throw new RuntimeException(fmt("error reading file: %s\n", m_scriptPath));
        }

        ScriptResources sr = null;
        try {
            sr = m_sm.getResources(m_jobID);
            Map<String, Parameter> oldParams = sr.getParams();
            Map<String, Object> oldValues = convParamsToObjects(oldParams);

            sr = m_sm.prepareScript(m_jobID, getLibDirs(), script, null, m_sandboxed);

            // reapply old values.  Removed values will be ignored
            m_sm.updateParams(m_jobID, oldValues);
        } catch(NotCachedException nce) {
            // should not happen
            nce.printStackTrace();
        }

        if(!sr.evaluatedScript.isSuccess()){
            printf("failed to prepareScript()\n");
            printScriptError(sr);
            return ResultCodes.RESULT_ERROR;            
        }
        
        m_sm.executeScript(sr);
        if(!sr.evaluatedScript.isSuccess()) {
            printf("failed to executeScript()\n");
            printScriptError(sr);
            return ResultCodes.RESULT_ERROR;
        }  
        m_evaluatedScript = sr.evaluatedScript;                
        m_scene = m_evaluatedScript.getResult();
        return ResultCodes.RESULT_OK;
    }


    private Map<String,Object> convParamsToObjects(Map<String,Parameter> params) {
        HashMap<String,Object> ret_val = new HashMap<>();

        for(Parameter p : params.values()) {
            ret_val.put(p.getName(),p.getValue());
        }

        return ret_val;
    }


    /**
       writes design into specified path
     */
    public int write(String path){
        
        if(DEBUG)printf("writeDesign(%s)\n", path);
        File file = new File(path);
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            ScriptResources sr = m_sm.getResources(m_jobID);
            Map<String, Parameter> ap = sr.getParams();

            Map<String, Object> paramMap = new  LinkedHashMap<String, Object>();
            for (Parameter par : ap.values()) {
                String name = par.getName();
                Object val = par.getValue();
                if(par.getType() == ParameterType.URI && val != null){
                    val = getRelativePath(file, new File((String)val));
                }
                paramMap.put(name, ParamJson.getJsonValue(val,par.getType()));
            }
            
            Map<String, Object> map = new  LinkedHashMap<String, Object>();
            map.put(SCRIPT_PATH, getRelativePath(file, new File(m_scriptPath)));
            map.put(SCRIPT_PARAMS, paramMap);
            
            String str = gson.toJson(map);
            FileUtils.writeStringToFile(file, str, "UTF-8");        
            m_designPath = path;
            return ResultCodes.RESULT_OK;

        } catch(Exception e){
            e.printStackTrace();
        }

        return ResultCodes.RESULT_ERROR;
    }

    public int save(){
        if(m_designPath == NEW_DESIGN)
            throw new RuntimeException("no designPath is given. Use \"Save As\"");
        return write(m_designPath);
    }


    static void printParams(String title, Parameter[] aparam){
        printf("printParams(%s, count:%d)\n", title, aparam.length);
        for(int i = 0; i < aparam.length; i++){            
            Parameter par = aparam[i];
            printf("%s (%s) = %s\n", par.getName(), par.getType(), par.getValue());
        }
        
    }
    static void printParamsMap(String title, Map<String, Object> map){
        printf("printParamsMap(%s, count: %d)\n", title, map.size());
        for(String s: map.keySet()) {
            Object value = map.get(s);
            if(value != null) printf("%s: %s(%s)\n", s, value.getClass().getSimpleName(), value);
            else printf("%s: null\n", s);
        }
    }

    void clearMessages(){
        m_errorMessages = new String[0];
    }
    
    void printScriptError(ScriptResources sr){

        if(DEBUG)printf("printScriptError()\n"); 
        java.util.List<Map<String, String>> log = sr.evaluatedScript.getErrorLogs();
        if(DEBUG)printf(" log: %s\n", log); 
        if(log == null) {
            m_errorMessages = new String[] {"error log is null"};
            return;
        }
        int i = 0;
        Vector<String> msg = new Vector<String>();
        for (Map<String, String> entry : log) {
            
            Map<String,String> map = entry;
            for (String key: map.keySet()) {
                //printf(" map:%d key: %s value: {%s}\n",i, key, map.get(key)); 
                msg.add(key + ":" + map.get(key));
            }
            i++;
            //printf("item: %d\n%s\n",i++, entry);
        }

        m_errorMessages = msg.toArray(new String[msg.size()]);

        printf(" end of printScriptError()\n");         
    }

    public String[] getErrorMessages(){

        return m_errorMessages;
    }
            
    /**
       should be called to update scene when bunch of parameters is changed via UI 
       @params - a set of name, value pairs 
    */
    public int updateScriptParams(LinkedHashMap<String,Object> params){

        if(params == null) 
            return ResultCodes.RESULT_ERROR;
        try {

            // repack parameters into name, value pairs 
            //LinkedHashMap<String, Object> parValues = new LinkedHashMap<String, Object>();

            for(String name: params.keySet()){
                if(DEBUG)printf("ShapeJSDesign.updateScriptParams(param:%s, value:%s)\n", name, params.get(name));
                //parValues.put(p.getName(), p.getValue());                
            }
                        
            ScriptResources sr = null;
            sr = m_sm.updateParams(m_jobID, params);
            m_sm.executeScript(sr);
            
            if (sr.evaluatedScript.isSuccess()) {
                m_evaluatedScript = sr.evaluatedScript;
                m_scene = m_evaluatedScript.getResult();
                
            } else {
                java.util.List<Map<String, String>> error = sr.evaluatedScript.getErrorLogs();
                for (Map<String, String> entry : error) {
                    printf("%s\n", entry);
                }
            }
            return ResultCodes.RESULT_OK;

        } catch(Exception e){
            printf("EXCEPTION IN SCRIPT EXECUTION\n");
            //e.printStackTrace();
        }
        return ResultCodes.RESULT_ERROR;
            
    }

    /**
       convert array of params into LinkedHashMap
    */
    public static LinkedHashMap<String, Object> getParamMap(Parameter aparam[]){
        
        LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
        for(int i = 0; i < aparam.length; i++){
            params.put(aparam[i].getName(), aparam[i].getValue());
        }        
        return params;
    }

    public Scene getScene(){
        return (Scene) m_scene;
    }

    public String getPath(){
        return m_designPath;
    }

    public String getPathNoExt(){
        int dotIndex = m_designPath.lastIndexOf(".");
        if(dotIndex > 0){
            return m_designPath.substring(0,dotIndex);
        }
        return m_designPath;
    }
 
    /**
       @return path which can be used for history for re-loading 
     */
    public String getHistoryPath(){
        if(hasPath()) 
            return m_designPath;
        else 
            return m_scriptPath;           
    }

    public String getScriptPath(){
        return m_scriptPath;
    }

    public String getScriptName(){
        return FilenameUtils.getBaseName(m_scriptPath);
    }

    public String getFileName(){
        return FilenameUtils.getBaseName(m_designPath);
    }

    public EvaluatedScript getEvaluatedScript(){
        return m_evaluatedScript;
    }

    public java.util.List<Parameterizable> getSource(){
        return ((Scene) m_scene).getSource();
    }

    public boolean hasPath(){
        return (m_designPath != NEW_DESIGN);
    }


   /**
       enforce script update from script file 
     */
    public void onScriptChanged(){
        
    } 

    public static String getRelativePath(File f1, File f2){
        Path p1 = f1.getAbsoluteFile().getParentFile().toPath();
        Path p2 = f2.getAbsoluteFile().toPath();
        if(DEBUG)printf("getRelativePath(\'%s\', \'%s\')\n", f1, f2);
        Path prel = p1.relativize(p2);
        //File f = prel.toFile();
        //return f.getCanonicalName();
        String str = prel.toString();
        str = str.replace('\\','/');
        if(DEBUG)printf("path1: %s, path2: %s rel: %s\n", f1, f2, str);
        return str; 

    }
    
    /**
       makes absolute path from file f1 to f2 
       f2 may be absolute or relative 
     */
    public static String resolvePath(File f1, File f2){

        if(f2.isAbsolute())
            return f2.getAbsolutePath();
        if(f2.exists()){
            // relative to current working folder
            return f2.getAbsolutePath();            
        }
        Path p1 = f1.getAbsoluteFile().getParentFile().toPath();
        Path p2 = f2.toPath();
        Path ap =  p1.resolve(p2);
        File f = ap.toFile();
        try {             
            String str = f.getCanonicalPath();            
            if(DEBUG) printf("path1: %s, path2: %s resolved: %s\n", f1, f2, str);
            return str; 
        } catch(Exception e){           
        }
        return ap.toString();

    }

    //
    // resolve relative pathes to absolute 
    //
    static Map<String,Object> resolveURIParams(File parentFile, Map<String,Parameter> params){

        HashMap<String,Object> ret_val = new HashMap<>();

        for(Parameter par : params.values()){
            if(par.getType() == ParameterType.URI){
                String parPath = (String)par.getValue();

                if(parPath != null) {
                    String newPath  = resolvePath(parentFile, new File(parPath));

                    if (!newPath.equals(parPath)) {
                        ret_val.put(par.getName(), newPath);
                    }
                }
            }
        }

        return ret_val;
    }

    
    ArrayList<String> getLibDirs(){

        return (ArrayList<String>)ViewerConfig.getInstance().get(ViewerConfig.LIB);
        
    }
    

}