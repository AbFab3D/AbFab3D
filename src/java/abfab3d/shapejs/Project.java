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

import abfab3d.core.Color;
import abfab3d.param.*;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.ietf.uri.IllegalActionException;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Vector3d;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A ShapeJS project contains its main script, resources and any parameter overrides.
 * <p/>
 * The serialization format is a zip container
 * main.js - The main script file
 * params.json - The script params
 * scene.json - The scene params
 * resources/* - All other resources
 *
 * @author Alan Hudson
 */
public class Project {
    private static final boolean DEBUG = false;

    private List<String> m_resources;
    private Script m_script;

    public Project() {
    }

    public Project(Script script) {
        m_script = script;
    }

    public void setScript(Script script) {
        m_script = script;
    }

    public Script getScript() {
        return m_script;
    }

    public void save(String file) throws IOException {
        EvaluatedScript escript = m_script.getEvaluatedScript();
        Map<String, Parameter> scriptParams = escript.getResult().getParamMap();
        Gson gson = JSONParsing.getJSONParser();

        String code = escript.getCode();

        Path workingDirName = Files.createTempDirectory("saveScript");
        String workingDirPath = workingDirName.toAbsolutePath().toString();
        Map<String, Object> params = new HashMap<String, Object>();

        // Write the script to file
        File scriptFile = new File(workingDirPath + "/main.js");
        FileUtils.writeStringToFile(scriptFile, code, "UTF-8");

        // Loop through params and create key/pair entries
        for (Map.Entry<String, Parameter> entry : scriptParams.entrySet()) {
            String name = entry.getKey();
            Parameter pval = entry.getValue();

            if (pval.isDefaultValue()) continue;

            ParameterType type = pval.getType();

            switch (type) {
                case URI:
                    URIParameter urip = (URIParameter) pval;
                    String u = (String) urip.getValue();

//                	System.out.println("*** uri: " + u);
                    File f = new File(u);

                    String fileName = null;

                    // TODO: This is hacky. If the parameter value is a directory, then assume it was
                    //       originally a zip file, and its contents were extracted in the directory.
                    //       Search for the zip file in the directory and copy that to the working dir.
                    if (f.isDirectory()) {
                        File[] files = f.listFiles();
                        for (int i = 0; i < files.length; i++) {
                            String fname = files[i].getName();
                            if (fname.endsWith(".zip")) {
                                fileName = fname;
                                f = files[i];
                            }
                        }
                    } else {
                        fileName = f.getName();
                    }

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
//                	System.out.println("*** lp: " + java.util.Arrays.toString(point) + ", " + java.util.Arrays.toString(normal));
                    Map<String, double[]> loc = new HashMap<String, double[]>();
                    loc.put("point", point);
                    loc.put("normal", normal);
                    params.put(name, loc);
                    break;
                case AXIS_ANGLE_4D:
                    AxisAngle4dParameter aap = (AxisAngle4dParameter) pval;
                    AxisAngle4d a = (AxisAngle4d) aap.getValue();
                    params.put(name, a);
                    break;
                case DOUBLE:
                    DoubleParameter dp = (DoubleParameter) pval;
                    Double d = (Double) dp.getValue();
//                	System.out.println("*** double: " + d);

                    params.put(name, d);
                    break;
                case INTEGER:
                    IntParameter ip = (IntParameter) pval;
                    Integer i = ip.getValue();
//                	System.out.println("*** int: " + pval);
                    params.put(name, i);
                    break;
                case STRING:
                    StringParameter sp = (StringParameter) pval;
                    String s = sp.getValue();
//                	System.out.println("*** string: " + s);
                    params.put(name, s);
                    break;
                case COLOR:
                    ColorParameter cp = (ColorParameter) pval;
                    Color c = cp.getValue();
//                	System.out.println("*** string: " + s);
                    params.put(name, c.toHEX());
                    break;
                case ENUM:
                    EnumParameter ep = (EnumParameter) pval;
                    String e = ep.getValue();
//                	System.out.println("*** string: " + s);
                    params.put(name, e);
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

        FileOutputStream fos = new FileOutputStream(file);
        ZipOutputStream zos = new ZipOutputStream(fos);
        System.out.println("*** Num files to zip: " + files.length);

        try {
            byte[] buffer = new byte[1024];

            for (int i = 0; i < files.length; i++) {
//                if (files[i].getName().endsWith(".zip")) continue;

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

    /**
     * Load a project
     *
     * @param file
     * @throws IOException
     */
    public static Project load(String file) throws IOException {
        Path workingDirName = Files.createTempDirectory("loadScript");
        String resultDirPath = workingDirName.toAbsolutePath().toString();

        Project ret_val = new Project();
        Map<String, String> sceneFiles = new HashMap<String, String>();
        List<String> m_resources = new ArrayList<String>();


        if (file.endsWith(".zip")) {
            extractZip(file, resultDirPath, sceneFiles, m_resources);
        } else if (file.endsWith(".js")) {
            sceneFiles.put("scriptFile", file);
        } else {
            throw new IllegalArgumentException("File type must be .js or .zip");
        }

        String scriptFilePath = sceneFiles.get("scriptFile");
        String paramFilePath = sceneFiles.get("paramFile");
        System.out.println("scriptFilePath: " + scriptFilePath);
        System.out.println("paramFilePath: " + paramFilePath);

        if (scriptFilePath == null) {
            throw new IllegalArgumentException("Missing script file");
        }

        File scriptFile = new File(scriptFilePath);
        String code = FileUtils.readFileToString(scriptFile, "UTF-8");
        EvaluatedScript evalResult = null;
        ShapeJSEvaluator evaluator = new ShapeJSEvaluator();

        if (paramFilePath != null) {
            Gson gson = JSONParsing.getJSONParser();
            String paramsJson = FileUtils.readFileToString(new File(paramFilePath), "UTF-8");
            Map<String, Object> scriptParams = gson.fromJson(paramsJson, Map.class);

            // TODO: Not used right now, need to think through how this might work
            // JSON String will become a map of string:object which is not what's expected downstream
            throw new IllegalActionException("Fix me");
            /*
            evaluator.prepareScript(code,scriptParams);
            evalResult = evaluator.executeScript(null);
            */
        } else {
            evaluator.prepareScript(code,null);
            evalResult = evaluator.executeScript(null);
        }

        URI uri = new File(file).toURI();
        Script script = new Script(uri, evalResult);

        ret_val.setScript(script);

        return ret_val;
    }

    private static void extractZip(String zipFile, String outputFolder, Map<String, String> sceneFiles, List<String> resources) {
        byte[] buffer = new byte[1024];

        try {
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
                System.out.println("file unzip : " + newFile.getAbsoluteFile());

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
                } else {
                    resources.add(newFile.getAbsolutePath());
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}
