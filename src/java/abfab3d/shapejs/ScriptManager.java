/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2015
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/
package abfab3d.shapejs;

import abfab3d.core.Initializable;
import abfab3d.io.input.URIMapper;
import abfab3d.param.Parameter;
import abfab3d.param.ParameterType;
import abfab3d.param.Parameterizable;
import abfab3d.param.URIParameter;
import abfab3d.util.URIUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;

/**
 * Manages scripts and their resources.  This class is thread safe.
 *
 * @author Alan Hudson
 */
public class ScriptManager {
    private static final boolean DEBUG = true;
    private static final boolean STOP_CACHING = false;
    private static final int DEFAULT_CACHE_TIMEOUT_MS = 60 * 60 * 1000;
    private static int sm_cacheTimeout = DEFAULT_CACHE_TIMEOUT_MS; // time-out for cache items

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
        aMap.put("urn:shapeways:stockImage:wt_colormap", IMAGES_DIR + "/colormap_08.png");
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
            .expireAfterAccess(sm_cacheTimeout, TimeUnit.MILLISECONDS)
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

        if(DEBUG)printf("ScriptManager() creating cache, timeout:%d ms\n", sm_cacheTimeout);

    }


    /**
       sets cache timeout in milliseconds
     */
    public static void setCacheTimeout(int timeoutMS){
        
        sm_cacheTimeout = timeoutMS;
        if(DEBUG)printf("ScriptManager.setCacheTimeout(%d ms)\n",timeoutMS);
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
     *
     * @param jobID
     * @param script
     * @param params -  Must be Java native objects expected for the parameter type
     * @return
     * @throws NotCachedException
     */
    public ScriptResources prepareScript(String jobID, String basedir,String script, Map<String, Object> params) {
        ScriptResources sr = null;

        long t0 = time();
        if (params == null) {
            params = new HashMap<String, Object>(1);
        }

        try {
            sr = cache.get(jobID);
        } catch (ExecutionException ee) {
            // ignore
        }

        if (sr == null && script == null) {
            throw new IllegalArgumentException("Script cannot be null for new script");
        }

        try {
            if (sr == null) {
                sr = new ScriptResources();
                sr.jobID = jobID;
                sr.eval = new ShapeJSEvaluator();
                sr.eval.setBaseDir(basedir);
                sr.eval.prepareScript(script, params);
                sr.evaluatedScript = sr.eval.getResult();
                sr.script = script;
                if (!sr.evaluatedScript.isSuccess()) {
                    return sr;
                }

                // Apply all values in first pass
                sr.eval.updateParams(params);

                Map<String, Object> downloadedParams = downloadURI(sr, null);

                //Reapply the ones changed from downloading
                sr.eval.updateParams(downloadedParams);
            } else {
                Map<String, Object> changedParams = params;
                if (script != null) {
                    sr.eval.prepareScript(script, params);
                    sr.evaluatedScript = sr.eval.getResult();
                    sr.script = script;
                    changedParams = null;
                    if (!sr.evaluatedScript.isSuccess()) {
                        return sr;
                    }
                } else {
                	// If evaluatedScript is currently false, it means parsing failed previously
                	// Re-evaluate the script before continuing
                	// TODO: Better way to do this?
                	if (!sr.evaluatedScript.isSuccess()) {
	                    sr.eval.prepareScript(sr.script, params);
	                    sr.evaluatedScript = sr.eval.getResult();
	                    if (!sr.evaluatedScript.isSuccess()) {
	                        return sr;
	                    }
                	}
                }

                // Apply all values in first pass
                sr.eval.updateParams(params);

                Map<String, Object> downloadedParams = downloadURI(sr, changedParams);

                //Reapply the ones changed from downloading
                sr.eval.updateParams(downloadedParams);
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                sr.evaluatedScript = new EvaluatedScript(ShapeJSErrors.ErrorType.INVALID_PARAMETER_VALUE, e.getMessage());
                return sr;
            }

            e.printStackTrace();
            sr.evaluatedScript = new EvaluatedScript(ShapeJSErrors.ErrorType.UNKNOWN_CRASH, e.getMessage());
            return sr;
        }

        if (DEBUG) printf("ScriptManager.update parse: %d ms\n", time() - t0);
        t0 = time();

        // Cache the job only if script eval is a success
        if (sr.evaluatedScript.isSuccess()) {
            if (!STOP_CACHING) {
                cache.put(jobID, sr);
            }
        }

        if (DEBUG) printf("ScriptManager.prepareScript download: %d ms\n", time() - t0);

        return sr;
    }

    /**
     * Prepare a script for execution.  Evaluates the javascript and downloads any parameters
     *
     * @param jobID
     * @param script
     * @param params -  Must be Java native objects expected for the parameter type
     * @return
     * @throws NotCachedException
     */
    public ScriptResources prepareScript(String jobID, String basedir,Script script, Map<String, Object> params) {
        return prepareScript(jobID, basedir,script.getCode(), params);
    }

    /**
     * Updates parameter values for a script.  Evaluates the javascript and downloads any parameters
     *
     * @param jobID
     * @param params - Must be Java native objects expected for the parameter type
     * @return
     * @throws NotCachedException
     */
    public ScriptResources updateParams(String jobID, Map<String, Object> params) throws NotCachedException {
        ScriptResources sr = null;

        long t0 = time();
        if (params == null) {
            params = new HashMap<String, Object>(1);
        }

        try {
            sr = cache.get(jobID);
        } catch (ExecutionException ee) {
            throw new NotCachedException();
        }

        try {
            // Apply all values in first pass
            sr.eval.updateParams(params);
            Map<String, Object> downloadedParams = downloadURI(sr, params);
            //Reapply the ones changed from downloading
            sr.eval.updateParams(downloadedParams);
        } catch(IllegalArgumentException iae) {
            sr.evaluatedScript = new EvaluatedScript(ShapeJSErrors.ErrorType.INVALID_PARAMETER_VALUE, iae.getMessage());
            return sr;
        }


        if (DEBUG) printf("ScriptManager.updateParams parse: %d ms\n", time() - t0);
        t0 = time();

        if (DEBUG) printf("ScriptManager.updateParams: %d ms\n", time() - t0);

        return sr;
    }

    /**
     * Execute the script.  This calls the main method and returns the Scene
     *
     * @return
     * @throws NotCachedException
     */
    public ScriptResources executeScript(ScriptResources sr) {
        return executeScript(sr, null);
    }

    /**
     * Execute the script.  This calls the main method and returns the Scene
     *
     * @param sr The current script resources.  Used to insure jobID caching can't fail
     * @return
     * @throws NotCachedException
     */
    public ScriptResources executeScript(ScriptResources sr, String method) {
        long t0 = time();

        String methodToCall = "main";
        if (method != null) methodToCall = method;

        if (DEBUG) printf("ScriptManager Execute script.");
        sr.evaluatedScript = sr.eval.executeScript(methodToCall);
        if (DEBUG) printf("ScriptManager eval.executeScript() done time: %d ms\n", time() - t0);

        if (sr.evaluatedScript.isSuccess()) {
            Parameterizable scene = sr.evaluatedScript.getResult();
            if (scene == null) {
                // somehow this happens 
                sr.evaluatedScript.setSuccess(false);
                return sr;
            }


            if (scene instanceof Initializable) {
                ((Initializable) scene).initialize();
            }
        }

        if (DEBUG) {
            printf("ScriptManager init: %d ms\n", time() - t0);
        }
        return sr;

    }

    /**
     * Reset the parameter values to their default state
     *
     * @param jobID
     */
    public void resetParams(String jobID) throws NotCachedException {
        ScriptResources sr = null;

        try {
            sr = cache.get(jobID);
        } catch (ExecutionException ee) {
            throw new NotCachedException();
        }

        sr.eval.resetParams();

    }


    public void cleanupJob(String jobID) {
        cache.invalidate(jobID);
    }

    public void clear() {
        cache.invalidateAll();
    }


    /**
     * Download any uri parameters containing a fully qualified url.  Updates sensitiveData flags if urls are sensitive
     *
     * @param resources
     * @param changedParams The changed params or null for all
     * @return The resolved parameters, feed these through updateParams
     */
    private Map<String, Object> downloadURI(ScriptResources resources, Map<String, Object> changedParams) {
        Map<String, Object> ret_val = new HashMap<>();

        Map<String, Parameter> evalParams = resources.getParams();
        String workingDirName = null;
        String workingDirPath = null;
        String urlStr = null;

        for (Map.Entry<String, Parameter> entry : evalParams.entrySet()) {
            String key = entry.getKey();
            Parameter param = entry.getValue();

            if (changedParams != null && !changedParams.containsKey(key)) continue;

            try {
                if (param.getType() == ParameterType.URI) {
                    if (DEBUG) printf("ScriptManager downloading param: %s   mapper: %s\n", param.getName(),uriMapper);

                    URIParameter up = (URIParameter) param;
                    urlStr = up.getValue();

                    // Null value indicates removal of param from scene
                    if (urlStr == null) continue;

                    if (uriMapper != null) {
                        URIMapper.MapResult mr = uriMapper.mapURI(urlStr);
                        urlStr = mr.uri;

                        if (DEBUG) printf("Mapped url: %s  sensitive: %b\n",urlStr,mr.sensitiveData);
                        if (mr.sensitiveData) {
                            resources.sensitiveData = true;
                        }
                    }

                    String file = ShapeJSGlobal.getURL(urlStr);

                    // If urlStr is in cache, make sure cached file exists
                    if (file != null && (new File(file)).exists()) {
                        ret_val.put(key, file);

                        continue;
                    }

                    String localPath = null;
                    boolean cache = false;
                    // TODO: We should really be parsing the URI into components instead of using starts and ends with
//                    printf("*** uri, %s : %s\n", key, urlStr);
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
                                localPath = URIUtils.getUrlFilename(key, urlStr, workingDirPath, true);
                                printf("Found local copy, localPath is: %s\n", localPath);
                            } else {
                                printf("Can't find local copy.  url: %s  path: %s\n", urlStr, workingDirPath);

                                long t0 = System.currentTimeMillis();
                                localPath = URIUtils.writeUrlToFile(key, urlStr, workingDirPath, true);
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
                            localPath = URIUtils.writeUrlToFile(key, urlStr, workingDirPath, false);
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
                                printf("Failed to parse base64: %s  from file: %s\n", base64, localPath);
                            }
                            localPath = URIUtils.writeDataURIToFile(key, base64, workingDirPath);
                        } else {
                            cache = true;
                        }

                        ret_val.put(key, localPath);
//                		System.out.println("*** uri, " + key + " : " + up.getValue());
                    } else if (urlStr.startsWith("data:")) {
                        if (workingDirName == null) {
                            workingDirPath = Files.createTempDirectory("downloaddata").toAbsolutePath().toString();
                        }
                        localPath = URIUtils.writeDataURIToFile(key, urlStr, workingDirPath);
                        ret_val.put(key, localPath);
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

                        ret_val.put(key, localPath);
                        cache = true;
                    }

                    // Do not cache data URI
                    if (localPath != null && !urlStr.startsWith("data:") && !localPath.endsWith(BASE64_FILE_EXTENSION)) {
                        ret_val.put(key, localPath);
                    }

                    if (cache) {
                        localPath = ShapeJSGlobal.putURL(urlStr, localPath);
                        ret_val.put(key, localPath);
                    }

                } else if (param.getType() == ParameterType.URI_LIST) {
                    // TODO: Handle uri list
                }

            } catch (Exception e) {
                printf("Error resolving uri: %s  msg: %s\n", urlStr, e.getMessage());
                e.printStackTrace();
            }
        }

        return ret_val;
    }

    public ScriptResources getResources(String jobID) throws NotCachedException {
        try {
            return cache.get(jobID);
        } catch (ExecutionException ee) {
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

        for (Map.Entry<String, String> entry : media.entrySet()) {
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
