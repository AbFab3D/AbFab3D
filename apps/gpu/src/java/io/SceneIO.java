package io;

import static abfab3d.util.Output.printf;
import abfab3d.grid.Bounds;

import abfab3d.param.DoubleParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.LocationParameter;
import abfab3d.param.Parameter;
import abfab3d.param.ParameterType;
import abfab3d.param.StringParameter;
import abfab3d.param.URIParameter;

import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;

import shapejs.EvalResult;
import shapejs.JSWrapper;
import shapejs.ShapeJSEvaluator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.vecmath.Vector3d;

/**
 * Creates images from ShapeJS
 *
 * @author Alan Hudson
 */
public class SceneIO {
    private static String RESULTS_DIR_PUBLIC = "http://localhost:8080/creator-kernels/results";
    private static String RESULTS_DIR = "/var/www/html/creator-kernels/results";
    private static String TMP_DIR = "/tmp";
    private static int TEMP_DIR_ATTEMPTS = 1000;


    public static Map<String, Object> loadScene(Map<String, String[]> params) throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();

        String workingDirName = createTempDir(RESULTS_DIR);
        String resultDirPath = RESULTS_DIR + "/" + workingDirName;

        String zipFile = params.get("sceneFile")[0];
        Map<String, String> sceneFiles = new HashMap<String, String>();
        extractZip(zipFile, resultDirPath, sceneFiles);

        String scriptFilePath = sceneFiles.get("scriptFile");
        String paramFilePath = sceneFiles.get("paramFile");
        System.out.println("scriptFilePath: " + scriptFilePath);
        System.out.println("paramFilePath: " + paramFilePath);

        if (scriptFilePath == null) {
            result.put("success",false);
            result.put("errorLog","Missing script file");
        } else {
            File scriptFile = new File(scriptFilePath);
            String script = FileUtils.readFileToString(scriptFile, "UTF-8");
            result.put("script", script);

            if (paramFilePath != null) {
                Gson gson = new Gson();
                String paramsJson = FileUtils.readFileToString(new File(paramFilePath), "UTF-8");
                Map<String, Object> scriptParams = gson.fromJson(paramsJson, Map.class);

                Bounds bounds = new Bounds();
                ShapeJSEvaluator evaluator = new ShapeJSEvaluator();
                EvalResult evalResult = evaluator.evalScript(script, null,bounds, null);
                Map<String, Parameter> evalParams = evalResult.getUIParams();

                // For parameters of type "uri", make it a fully qualified url
//                System.out.println("*** Loaded params");
                for (Map.Entry<String, Object> entry : scriptParams.entrySet()) {
                    String name = entry.getKey();
                    Object val = entry.getValue();
//                    System.out.println(    name + "=" + val);
                    ParameterType type = evalParams.get(name).getType();
//                    System.out.println(    "  type: " + type);
                    if (type == ParameterType.URI) {
                        scriptParams.put(name, resolveURN(resultDirPath) + "/" + (String)val);
                    }
                }
                
                result.put("params", scriptParams);
            }

            result.put("success", true);
        }

        return result;
    }

    public static void saveScene(String script, Map<String, Object> sceneParams, OutputStream os) throws IOException {
        Bounds bounds = new Bounds();
        ShapeJSEvaluator evaluator = new ShapeJSEvaluator();
        EvalResult result = evaluator.evalScript(script, null,bounds, sceneParams);
        Map<String, Parameter> evalParams = result.getUIParams();
    	Gson gson = new Gson();
    	
        System.out.println("*** Script:\n" + script);
        System.out.println("*** Params:");
        String workingDirName = createTempDir(TMP_DIR);
        String workingDirPath = TMP_DIR + "/" + workingDirName;
        Map<String, Object> params = new HashMap<String, Object>();

        // Write the script to file
        File scriptFile = new File(workingDirPath + "/script.js");
        FileUtils.writeStringToFile(scriptFile, script, "UTF-8");

        // Loop through params and create key/pair entries
        for (Map.Entry<String, Object> entry : sceneParams.entrySet()) {
            String name = entry.getKey();
            Object val = entry.getValue();
            ParameterType type = evalParams.get(name).getType();
            
            Object pval = null;

            if (val instanceof JSWrapper) {
            	pval = ((JSWrapper) val).getParameter();
            } else if (val instanceof NativeJavaObject) {
            	pval = ((NativeJavaObject) val).unwrap();
            } else if (val instanceof NativeArray) {
            	pval = ((NativeArray) val);
            } else {
            	pval = val;
            }

            switch(type) {
                case URI:
                	URIParameter urip = (URIParameter) pval;
                	String u = (String) urip.getValue();
                	System.out.println("*** uri: " + u);
                    File f = new File(u);
                    String fileName = f.getName();
                    params.put(name, fileName);

                    // Copy the file to working directory
                    FileUtils.copyFile(f, new File(workingDirPath + "/" + fileName), true);
                    break;
                case LOCATION:
                	LocationParameter lp = (LocationParameter) pval;
                	Vector3d p = lp.getPoint();
                	Vector3d n = lp.getNormal();
                	double[] point = {p.x, p.y, p.z};
                	double[] normal = {n.x, n.y, n.z};
                	System.out.println("*** lp: " + java.util.Arrays.toString(point) + ", " + java.util.Arrays.toString(normal));
                	Map<String, double[]> loc = new HashMap<String, double[]>();
                	loc.put("point", point);
                	loc.put("normal", normal);
                    params.put(name, loc);
                    break;
                case DOUBLE:
                	DoubleParameter dp = (DoubleParameter) pval;
                	Double d = (Double) dp.getValue();
                	System.out.println("*** double: " + d);
                    params.put(name, d);
                    break;
                case INTEGER:
                	IntParameter ip = (IntParameter) pval;
                	Integer i = ip.getValue();
                	System.out.println("*** int: " + pval);
                    params.put(name, i);
                    break;
                case STRING:
                	StringParameter sp = (StringParameter) pval;
                	String s = sp.getValue();
                	System.out.println("*** string: " + s);
                	params.put(name, s);
                    break;
                default:
                	params.put(name, pval);
            }
        }

        if (params.size() > 0) {
            String paramsJson = gson.toJson(params);
            File paramFile = new File(workingDirPath + "/" + "params.json");
            FileUtils.writeStringToFile(paramFile, paramsJson, "UTF-8");
        }

        File[] files = (new File(workingDirPath)).listFiles();
        ZipOutputStream zos = new ZipOutputStream(os);
        System.out.println("*** Num files to zip: " + files.length);

        try {
            byte[] buffer = new byte[1024];
            
            for (int i=0; i<files.length; i++) {
                if (files[i].getName().endsWith(".zip")) continue;

                System.out.println("*** Adding file: " + files[i].getName());
                FileInputStream fis = new FileInputStream(files[i]);
                ZipEntry ze = new ZipEntry(files[i].getName());
                zos.putNextEntry(ze);

                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                fis.close();
            }
        } finally {
            zos.closeEntry();
            zos.close();
        }
    }

    public static void extractZip(String zipFile, String outputFolder, Map<String, String> sceneFiles) {
        byte[] buffer = new byte[1024];

        try{
            //create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {
                // Ignore directories
                if (ze.isDirectory()) continue;

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                System.out.println("file unzip : "+ newFile.getAbsoluteFile());

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                // Save path to the script and parameters files
                if (fileName.endsWith(".json")) {
                    sceneFiles.put("paramFile", newFile.getAbsolutePath());
                } else if (fileName.endsWith(".js")) {
                    sceneFiles.put("scriptFile", newFile.getAbsolutePath());
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
    
    public static void downloadURI(Map<String, Parameter> evalParams, Map<String, Object> namedParams) {
		String workingDirName = null;
		String workingDirPath = null;
		
        for (Map.Entry<String, Object> entry : namedParams.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            Parameter param = evalParams.get(key);
            String json = (String) entry.getValue();
            
            try {

                if (param.getType() == ParameterType.URI) {
                	Parameter p = ((JSWrapper) val).getParameter();
                	URIParameter up = (URIParameter) p;
                	String urlStr = up.getValue();
                	System.out.println("*** uri, " + key + " : " + urlStr);
                	if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
                		if (workingDirName == null) {
                			workingDirName = createTempDir(TMP_DIR);
                			workingDirPath = TMP_DIR + "/" + workingDirName;
                		}
                		
                		String localPath = writeUrlToFile(key, urlStr, workingDirPath);
                		up.setValue(localPath);
                		System.out.println("*** uri, " + key + " : " + up.getValue());
                	}
                } else if (param.getType() == ParameterType.URI_LIST) {
                	// TODO: Handle uri list
                }
                
            } catch (Exception e) {
            	printf("Error resolving uri: " + json);
            	e.printStackTrace();
            }
        }
    }
    
    public static String downloadURI(String paramName, String urlStr) throws IOException {
		String workingDirName = createTempDir(TMP_DIR);
		String workingDirPath = TMP_DIR + "/" + workingDirName;
		return writeUrlToFile(paramName, urlStr, workingDirPath);
    }

    private static String writeUrlToFile(String paramName, String urlStr, String destDir) throws IOException {
		String filename = paramName + "_" + FilenameUtils.getName(urlStr);

		File file = new File(destDir + "/" + filename);
		FileUtils.copyURLToFile(new URL(urlStr), file, 10000, 60000);

		if (file.exists()) {
			return file.getAbsolutePath();
		}
		
		return null;
    }
    
    /**
    * Atomically creates a new directory somewhere beneath the system's
    * temporary directory (as defined by the {@code java.io.tmpdir} system
    * property), and returns its name.
    *
    * <p>Use this method instead of {@link File#createTempFile(String, String)}
    * when you wish to create a directory, not a regular file.  A common pitfall
    * is to call {@code createTempFile}, delete the file and create a
    * directory in its place, but this leads a race condition which can be
    * exploited to create security vulnerabilities, especially when executable
    * files are to be written into the directory.
    *
    * <p>This method assumes that the temporary volume is writable, has free
    * inodes and free blocks, and that it will not be called thousands of times
    * per second.
    *
    * @return the newly-created directory
    * @throws IllegalStateException if the directory could not be created
    */
    public static String createTempDir(String baseDir) {
        String baseName = System.currentTimeMillis() + "-";

        for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir()) {
                return baseName + counter;
            }
        }

        throw new IllegalStateException("Failed to create directory within "
            + TEMP_DIR_ATTEMPTS + " attempts (tried "
            + baseName + "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')' + " baseDir: " + baseDir);
    }
    
    public static File createTempFile(String baseDir, String fieldName, String fileName, String ext) {
        String baseName = fieldName + "_" + fileName + "-";//System.currentTimeMillis() + "-";
        
        String extension = ext;
        if (!extension.startsWith(".")) {
        	extension = "." + extension;
        }

        try {
            for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
//                File tempFile = new File(baseDir, fileName + baseName + counter + ext);
                File tempFile = new File(baseDir, baseName + counter + extension);
                if (tempFile.createNewFile()) {
                    return tempFile;
                }
            }
        } catch (Exception e) {}

        throw new IllegalStateException("Failed to create file within "
            + TEMP_DIR_ATTEMPTS + " attempts (tried " + baseName + "0" + extension
            + " to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + extension + ')' + " baseDir: " + baseDir);
    }
    
    private static String resolveURN(String resultDir) {
    	String ret_val = resultDir.replace(RESULTS_DIR, RESULTS_DIR_PUBLIC);
    	return ret_val;
    }

}

