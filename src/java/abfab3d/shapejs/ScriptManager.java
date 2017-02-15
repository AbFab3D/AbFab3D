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
package abfab3d.shapejs;

import abfab3d.core.Material;
import abfab3d.io.input.URIMapper;
import abfab3d.util.URIUtils;
import abfab3d.param.Parameter;
import abfab3d.param.ParameterType;
import abfab3d.param.Parameterizable;
import abfab3d.param.URIParameter;

import abfab3d.core.Initializable;
import com.google.common.cache.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;

/**
 * Manages scripts and their resources.  This class is thread safe.
 *
 * @author Alan Hudson
 */
public class ScriptManager {
    private static final boolean DEBUG = false;
    private static final boolean STOP_CACHING = false;
    private static final int JOB_RETAIN_MS = 60 * 60 * 1000;
    
    // File type that contains base64 data to be saved to disk as mime-type specified in the data
    private static final String BASE64_FILE_EXTENSION = ".base64";

    private LoadingCache<String, ScriptResources> cache;
    private static ScriptManager instance;

    private static final String TMP_DIR = "/tmp";
    private static final String IMAGES_DIR = "/stock/media/images";
    private static final String MODELS_DIR = "/stock/media/models";
    
    private static final Map<String, String> media;

    private static MaterialMapper matMapper;
    private static URIMapper uriMapper = null;

    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put("urn:shapeways:stockImage:shapeways_logo", IMAGES_DIR + "/shapeways_logo.png");
        aMap.put("urn:shapeways:stockImage:allblack", IMAGES_DIR + "/allblack.jpg");
        aMap.put("urn:shapeways:stockImage:allwhite", IMAGES_DIR + "/allwhite.jpg");
        aMap.put("urn:shapeways:stockImage:gradient", IMAGES_DIR + "/gradient.jpg");
        aMap.put("urn:shapeways:stockImage:envmap_rays", IMAGES_DIR + "/envmap_rays.png");
        aMap.put("urn:shapeways:stockModel:smallbox", MODELS_DIR + "/box_10mm.x3db");
        aMap.put("urn:shapeways:stockModel:box", MODELS_DIR + "/box_20mm.x3db");
        aMap.put("urn:shapeways:stockModel:smallsphere", MODELS_DIR + "/sphere_10mm.x3db");
        aMap.put("urn:shapeways:stockModel:sphere", MODELS_DIR + "/sphere_20mm.x3db");
        media = Collections.unmodifiableMap(aMap);

        if (STOP_CACHING) {
            printf("**** Caching disabled on ScriptManager ***\n");
            new Exception().printStackTrace();
        }
    }
    
    private ScriptManager() {
        cache = CacheBuilder.newBuilder()
                .softValues()
                .expireAfterAccess(JOB_RETAIN_MS, TimeUnit.MILLISECONDS)
                .removalListener(new RemovalListener<String, ScriptResources>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, ScriptResources> removal) {
                        // ignore replacements
                        if (removal.getCause() == RemovalCause.REPLACED) return;

                        ScriptResources ce = removal.getValue();
                        if (ce != null)
                            ce.clear();
                    }
                })
                .build(
                        new CacheLoader<String, ScriptResources>() {
                            public ScriptResources load(String key) throws ExecutionException {
                                throw new ExecutionException(new IllegalArgumentException("Can't load key: " + key));
                            }
                        }
                );

    }

    public static ScriptManager getInstance() {
        if (instance == null) {
            instance = new ScriptManager();
        }

        return instance;
    }

    public static void setURIMapper(URIMapper mapper) {
        uriMapper = mapper;
    }

    public static void setMaterialMapper(MaterialMapper mm) {
        matMapper = mm;
    }

    /**
     * Prepare a script for execution.  Evaluates the javascript and downloads any parameters
     * @param jobID
     * @param delta
     * @param script
     * @param params
     * @return
     * @throws NotCachedException
     */
    public ScriptResources prepareScript(String jobID, boolean delta, String script, Map<String,Object> params) throws NotCachedException {
        ScriptResources sr = null;

        long t0 = time();
        if (params == null) {
            params = new HashMap<String, Object>(1);
        }

        if (delta == true) {
            try {
                sr = cache.get(jobID);

                if (sr != null) {
                    // update existing values
                    if (params.size() > 0) {
                        updateParams(params, sr);
                    }

                    if (script != null) {
                        // new script
                        sr.script = script;
                    }

                    if (!sr.result.isSuccess()) {
                        printf("Script in a bad state, trying to reparse\n");
                        if (script!= null) {
                            sr.eval.parseScript(script);
                            sr.script = script;
                        } else {
                            sr.eval.parseScript(sr.script);
                        }
                        sr.result = sr.eval.getResult();
                    }
                }
            } catch (ExecutionException ee) {
                // ignore
                if (delta) throw new NotCachedException();
            }
        }

        if (sr == null) {
            sr = new ScriptResources();
            sr.jobID = jobID;
            sr.eval = new ShapeJSEvaluator();
            sr.eval.parseScript(script);
            sr.result = sr.eval.getResult();
            sr.script = script;
            if (!sr.result.isSuccess()) {
                return sr;
            }
            sr.firstCreate = true;
        } else {
            sr.firstCreate = false;
        }

        if (DEBUG) printf("ScriptManager.update parse: %d ms\n",time() - t0);
        t0 = time();

        // convert JSON to objects
        try {
            sr.eval.mungeParams(params, sr.firstCreate);
            if (DEBUG) printf("ScriptManager.update munge: %d ms\n",time() - t0);
        } catch (IllegalArgumentException iae) {
            sr.result = new EvaluatedScript(ShapeJSErrors.ErrorType.INVALID_PARAMETER_VALUE, iae.getMessage(), null,time() - t0);
            return sr;
        }

        // download URLs
        downloadURI(sr.result.getParamMap(), params);
        sr.params.putAll(params);

        // Cache the job only if script eval is a success
        if (sr.result.isSuccess()) {
            if (!STOP_CACHING) {
                cache.put(jobID, sr);
            }
        }

        if (DEBUG) printf("ScriptManager.update download: %d ms\n",time() - t0);

        return sr;
    }

    /**
     * Prepare a script for execution.  Evaluates the javascript and downloads any parameters
     * @param jobID
     * @param delta
     * @param script
     * @param params
     * @return
     * @throws NotCachedException
     */
    public ScriptResources prepareScript(String jobID, boolean delta, Script script, Map<String,Object> params) throws NotCachedException {
        return prepareScript(jobID,delta,script.getCode(),params);
    }

    /**
     * Prepare a script for execution.  Evaluates the javascript and downloads any parameters
     * @param jobID
     * @param script
     * @param params
     * @return
     * @throws NotCachedException
     */
    public ScriptResources prepareScript(String jobID, Script script, Map<String,Object> params) {
        try {
            return prepareScript(jobID, false, script.getCode(), params);
        } catch(NotCachedException nce) {
            // Should never happen
            printf("Unhandled case.");
            nce.printStackTrace();
            return null;
        }
    }

    /**
     * Prepare a script for execution.  Evaluates the javascript and downloads any parameters
     * @param jobID
     * @param script
     * @param params
     * @return
     * @throws NotCachedException
     */
    public ScriptResources prepareScript(String jobID, String script, Map<String,Object> params) {
        try {
            return prepareScript(jobID, false, script, params);
        } catch(NotCachedException nce) {
            // Should never happen
            printf("Unhandled case.");
            nce.printStackTrace();
            return null;
        }
    }

    /**
     * Execute the script.  This calls the main method or specific listener and returns the Scene
     * @param params
     * @return
     * @throws NotCachedException
     */
    public ScriptResources executeScript(ScriptResources sr, Map<String,Object> params){
        // lost the difference between eval and reval
        long t0;

        if (params == null) params = new HashMap<String,Object>(1);

        if (sr.firstCreate) {
            t0 = time();
            if (DEBUG) printf("ScriptManager Execute script.  params: %s\n",params);
            sr.result = sr.eval.executeScript("main", params);
            if (DEBUG) printf("ScriptManager Done eval time: %d ms\n",time() - t0);
        } else {
            t0 = time();
            if (DEBUG) printf("ScriptManager Reeval script.  params: %s\n",params);
            sr.result = sr.eval.reevalScript(sr.script,params);
            if (DEBUG) printf("ScriptManager Done Reeval script.  %d ms\n",time() - t0);
        }

        t0 = time();
        if (sr.result.isSuccess()) {
            Object material = params.get("material");

            if (material == null) {
                material = sr.eval.getParameter("material");
            }

            if (material != null) {
                Parameter mat = (Parameter) material;
                String matSt = (String) mat.getValue();

                EvaluatedScript result = sr.result;

                if (result != null && result.getScene() != null) {
                    if (matSt.equals("None")) {
                        result.getScene().setMaterial(0,DefaultMaterial.getInstance());
                        result.getScene().setLightingRig(Scene.LightingRig.THREE_POINT_COLORED);
                    } else if (matMapper != null) {
                        Material rm = matMapper.getImplementation(matSt);
                        if (rm == null) rm = sr.eval.getImplementation(matSt);

                        if (rm != null) {
                            result.getScene().setMaterial(0,rm);
                            result.getScene().setLightingRig(Scene.LightingRig.THREE_POINT);
                        }

                    }
                }
            }
        }

        if (sr.result.isSuccess()) {

            // I think this is the correct place to call initialize.  Might call it too often?
            List<Parameterizable> list = sr.result.getScene().getSource();
            for(Parameterizable ds: list) {
                if (ds instanceof Initializable) {
                    ((Initializable) ds).initialize();
                }
            }
        }

        if (DEBUG) {
            printf("ScriptManager init: %d ms\n",time() - t0);
        }
        return sr;

    }

    public void cleanupJob(String jobID) {
        cache.invalidate(jobID);
    }

    public void clear() {
        cache.invalidateAll();
    }



    /**
     * Download any uri parameters containing a fully qualified url.
     *
     * @param evalParams
     * @param namedParams
     */
    private void downloadURI(Map<String, Parameter> evalParams, Map<String, Object> namedParams) {
        String workingDirName = null;
        String workingDirPath = null;
        String urlStr = null;

        for (Map.Entry<String, Object> entry : namedParams.entrySet()) {
            String key = entry.getKey();
            Parameter param = evalParams.get(key);

            if (param == null) {
                printf("Cannot find definition for: %s, ignoring.\n",key);
                continue;
            }

            try {
                if (param.getType() == ParameterType.URI) {
                    URIParameter up = (URIParameter) param;
                    urlStr = up.getValue();

                    // Null value indicates removal of param from scene
                    if (urlStr == null) continue;

                    if (uriMapper != null) {
                        urlStr = uriMapper.mapURI(urlStr);
                    }

                    String file = ShapeJSGlobal.getURL(urlStr);

                    // If urlStr is in cache, make sure cached file exists
                    if (file != null && (new File(file)).exists()) {
                        up.setValue(file);
                        continue;
                    }

                    String localPath = null;
                    boolean cache = false;
                    // TODO: We should really be parsing the URI into components instead of using starts and ends with
//                	System.out.println("*** uri, " + key + " : " + urlStr);
                    if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
                        if (urlStr.contains("www.shapeways.com/models/get-base")) {
                            URL yourl = new URL(urlStr);
                            // Remove query params
                            URI uri = new URI(yourl.getProtocol(), yourl.getUserInfo(), yourl.getHost(), yourl.getPort(), yourl.getPath(), "", yourl.getRef());

                            // TODO: this will get cleaned regularly?  not sure what todo here
                            String basedir = System.getProperty("java.io.tmpdir") + "shapeways";
                            File f = new File(basedir);
                            f.mkdirs();
                            String filename = uri.toString().replaceAll("[:\\\\/*\"?|<>'.;]", "");

                            workingDirPath = basedir + File.separator + filename;

                            f = new File(workingDirPath);
                            if (f.exists()) {
                                // already downloaded, assume its all good
                                localPath = URIUtils.getUrlFilename(key,urlStr,workingDirPath,true);
                                printf("Found local copy, localPath is: %s\n",localPath);
                            } else {
                                printf("Can't find local copy.  url: %s  path: %s\n", urlStr, workingDirPath);

                                long t0 = System.currentTimeMillis();
                                localPath = URIUtils.writeUrlToFile(key, urlStr, workingDirPath,true);
                                printf("Download of: %s took: %s ms\n", urlStr, (System.currentTimeMillis() - t0));
                                if (localPath == null) {
                                    printf("Could not save url.  key: %s  url: %s  dir: %s\n", key, urlStr, workingDirPath);
                                    throw new IllegalArgumentException("Could not resolve uri: %s to disk: " + urlStr);
                                }
                            }
                        }

                        if (localPath == null) {
                            workingDirPath = Files.createTempDirectory("downloaduri").toAbsolutePath().toString();
                            long t0 = System.currentTimeMillis();
                            localPath = URIUtils.writeUrlToFile(key, urlStr, workingDirPath,false);
                            printf("Download of: %s took: %s ms\n", urlStr, (System.currentTimeMillis() - t0));
                            if (localPath == null) {
                                printf("Could not save url.  key: %s  url: %s  dir: %s\n", key, urlStr, workingDirPath);
                                throw new IllegalArgumentException("Could not resolve uri: %s to disk: " + urlStr);
                            }
                        }

                        // TODO: This handles a case with portal needing to write base64 file data to a
                        // .base64 file. Will want to rethink this in the future.
                        if (localPath.endsWith(BASE64_FILE_EXTENSION)) {
                        	String base64 = FileUtils.readFileToString(new File(localPath), "UTF-8");

                            if (base64 == null || base64.length() == 0) {
                                printf("Failed to parse base64: %s  from file: %s\n",base64,localPath);
                            }
                        	localPath = URIUtils.writeDataURIToFile(key, base64, workingDirPath);
                        } else {
                            cache = true;
                        }
                        
                        up.setValue(localPath);
//                		System.out.println("*** uri, " + key + " : " + up.getValue());
                    } else if (urlStr.startsWith("data:")) {
                        if (workingDirName == null) {
                            workingDirPath = Files.createTempDirectory("downloaddata").toAbsolutePath().toString();
                        }
                        localPath = URIUtils.writeDataURIToFile(key, urlStr, workingDirPath);
                        up.setValue(localPath);
                    } else if (urlStr.startsWith("urn:shapeways:")) {
                    	if (media.get(urlStr) == null) {
                    		throw new Exception("Invalid media resource: " + urlStr);
                    	}
                    	
                        localPath = TMP_DIR + media.get(urlStr);
                        File f = new File(localPath);
                        if (!f.exists()) {
                        	exportMediaResources();
                            if (!f.exists()) {
                            	throw new Exception("Error exporting media resource: " + urlStr);
                            }
                        }

                        up.setValue(localPath);
                        cache = true;
                    }

                    // Do not cache data URI
                    if (localPath != null && !urlStr.startsWith("data:") && !localPath.endsWith(BASE64_FILE_EXTENSION)) {
                        up.setValue(localPath);
                    }

                    if (cache) {
                        localPath = ShapeJSGlobal.putURL(urlStr, localPath);
                        up.setValue(localPath);
                    }

                } else if (param.getType() == ParameterType.URI_LIST) {
                    // TODO: Handle uri list
                }

            } catch (Exception e) {
                printf("Error resolving uri: %s  msg: %s\n",urlStr,e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Update parameters from an incoming delta
     * @param params
     * @param sr
     */
    private void updateParams(Map<String, Object> params, ScriptResources sr) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String name = entry.getKey();
            Object val = entry.getValue();

            // null value indicates removal of param
            if (val != null) {
                sr.params.put(name, val);
            } else {
                sr.params.remove(name);
            }
        }
    }
    
    public ScriptResources getResources(String jobID) throws NotCachedException {
        try {
            return cache.get(jobID);
        } catch(ExecutionException ee) {
            throw new NotCachedException();
        }
    }

    private void exportMediaResources() throws Exception {
    	File imagesDir = new File(TMP_DIR + IMAGES_DIR);
    	File modelsDir = new File(TMP_DIR + MODELS_DIR);
    	if (!imagesDir.exists()) {
    		imagesDir.mkdirs();
    	}
    	if (!modelsDir.exists()) {
    		modelsDir.mkdirs();
    	}
    	
    	for(Map.Entry<String, String> entry : media.entrySet()) { 
//    		System.out.printf("Key : %s and Value: %s %n", entry.getKey(), entry.getValue());
    		String file = entry.getValue();
    		String name = FilenameUtils.getName(file);
    		if (file.contains("media/images")) {
    			exportResource(entry.getValue(), new File(imagesDir.getAbsolutePath() + "/" + name));
    		} else if (file.contains("media/models")) {
    			exportResource(entry.getValue(), new File(modelsDir.getAbsolutePath() + "/" + name));
    		}
    	}
    }
    
    /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceFile ie.: "/SmartLibrary.dll"
     * @return The path to the exported resource
     * @throws Exception
     */
    public void exportResource(String resourceFile, File destFile) throws Exception {
    	InputStream is = null;
        try {
            is = ScriptManager.class.getResourceAsStream(resourceFile);

            if (is == null) {
                File file = new File("classes/" + resourceFile);

                if (!file.exists()) {
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    is = classLoader.getResourceAsStream(resourceFile);
                } else {
                    is = new FileInputStream(file);
                }
            }
            if (is == null) {
                throw new Exception("Cannot load resource: " + resourceFile);
            }
            FileUtils.copyInputStreamToFile(is, destFile);
        } finally {
        	if (is != null) is.close();
        }
    }
}
