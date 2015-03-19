package io;

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
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;

import shapejs.EvalResult;
import shapejs.JSWrapper;
import shapejs.ShapeJSEvaluator;
import utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.vecmath.Vector3d;

/**
 * Common IO methods for scene.
 *
 * @author Tony Wong
 */
public class SceneIO {
    private static String RESULTS_DIR_PUBLIC = "http://localhost:8080/creator-kernels/results";
    private static String RESULTS_DIR = "/var/www/html/creator-kernels/results";
    private static String TMP_DIR = "/tmp";


    public static Map<String, Object> loadScene(Map<String, String[]> params) throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();

        String workingDirName = Utils.createTempDir(RESULTS_DIR);
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
        String workingDirName = Utils.createTempDir(TMP_DIR);
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
    
    private static String resolveURN(String resultDir) {
    	String ret_val = resultDir.replace(RESULTS_DIR, RESULTS_DIR_PUBLIC);
    	return ret_val;
    }

}

