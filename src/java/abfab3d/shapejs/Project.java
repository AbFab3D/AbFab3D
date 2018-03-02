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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static abfab3d.core.Output.printf;

/**
 * A self-contained ShapeJS Container
 *
 * @author Alan Hudson
 */
public class Project {
    private static final boolean DEBUG = false;

    public static final String EXT_PROJECT = ".shapeprj";
    public static final String EXT_VARIANT = ".shapevar";
    public static final String EXT_SCRIPT = ".shapejs";
    public static final String EXT_MANIFEST = ".json";
    public static final String NAME_PROP = "name";
    public static final String AUTHOR_PROP = "author";
    public static final String LICENSE_PROP = "license";

    private String parentDir;
    private ArrayList<ProjectItem> scripts = new ArrayList<>();
    private ArrayList<ProjectItem> resources = new ArrayList<>();
    private ArrayList<VariantItem> variants = new ArrayList<>();
    private LinkedHashMap<String, String> dependencies = new LinkedHashMap<>();
    private String name;
    private String author;
    private String license;  // Follow NPM Rules(https://spdx.org/licenses/ using (A OR B) for multiple
    private String thumbnail;

    public Project() {
    }

    public List<ProjectItem> getScripts() {
        return scripts;
    }

    public void setScripts(List<ProjectItem> scripts) {
        this.scripts = new ArrayList(scripts);
    }

    public void addScript(ProjectItem script) {
        this.scripts.add(script);
    }

    public void removeScript(ProjectItem script) {
        this.scripts.remove(script);
    }

    public List<ProjectItem> getResources() {
        return resources;
    }

    public void setResources(List<ProjectItem> resources) {
        this.resources = new ArrayList(resources);
    }

    public void addResource(ProjectItem resource) {
        this.resources.add(resource);
    }

    public void removeResource(ProjectItem resource) {
        this.resources.remove(resource);
    }

    public List<VariantItem> getVariants() {
        return variants;
    }

    public void setVariants(List<VariantItem> variants) {
        this.variants = new ArrayList(variants);
    }

    public void addVariant(VariantItem variant) {
        this.variants.add(variant);
    }

    public void removeVariant(VariantItem variant) {
        this.variants.remove(variant);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public void addDependency(String name, String version) {
        dependencies.put(name, version);
    }

    public void removeDependency(String name, String version) {
        dependencies.put(name, version);
    }

    public Map<String, String> getDependencies() {
        return new HashMap<>(dependencies);
    }

    public void setThumbnail(String path) {
        thumbnail = path;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getParentDir() {
        return parentDir;
    }

    public void setParentDir(String parentDir) {
        this.parentDir = parentDir;
    }

    /**
     * Save this project to a directory
     *
     * @param dir
     * @throws IOException
     */
    public void save(String dir) throws IOException {
        File f = new File(dir);

        f.mkdirs();

        LinkedHashMap<String,Object> manifest = new LinkedHashMap<>();
        ArrayList<String> scriptList = new ArrayList<>();
        for(ProjectItem script : scripts) {
            String fname = FilenameUtils.separatorsToUnix(script.getPath());
            scriptList.add(fname);
        }

        ArrayList<String> variantList = new ArrayList<>();
        for(ProjectItem variant : variants) {
            String fname = FilenameUtils.separatorsToUnix(variant.getPath());
            variantList.add(fname);
        }

        ArrayList<String> resourceList = new ArrayList<>();
        for(ProjectItem resource : resources) {
            String fname = FilenameUtils.separatorsToUnix(resource.getPath());
            resourceList.add(fname);
        }

        manifest.put(NAME_PROP,name);
        manifest.put(AUTHOR_PROP,author);
        manifest.put(LICENSE_PROP,license);
        manifest.put("scripts",scriptList);
        manifest.put("variants",variantList);
        manifest.put("resources",resourceList);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String manifestSt = gson.toJson(manifest);
        File manifestFile = new File(f.getAbsolutePath() + File.separator + "manifest" + EXT_MANIFEST);
        FileUtils.writeStringToFile(manifestFile,manifestSt);

        // TODO: Write out real stuff

        File scriptsDir = new File(f.getAbsolutePath() + File.separator + "scripts");
        scriptsDir.mkdirs();

        File variantsDir = new File(f.getAbsolutePath() + File.separator + "variants");
        variantsDir.mkdirs();

        File resourcesDir = new File(f.getAbsolutePath() + File.separator + "resources");
        resourcesDir.mkdirs();
    }


    public void saveOld(String file) throws IOException {
        /*
        EvaluatedScript escript = script.getEvaluatedScript();
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

        */
    }

    /**
     * Load a project
     *
     * @param file
     * @throws IOException
     */
    public static Project loadOld(String file) throws IOException {
        /*
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

            //evaluator.prepareScript(code,scriptParams);
            //evalResult = evaluator.executeScript(null);

        } else {
            evaluator.prepareScript(code,null);
            evalResult = evaluator.executeScript(null);
        }

        URI uri = new File(file).toURI();
        Script script = new Script(uri, evalResult);

        ret_val.setScript(script);

        return ret_val;
        */

        return null;
    }

    /**
     * Load a project.
     *
     * @param file - Either a .shapejsprj file or the unzipped manifest.json
     * @return
     * @throws IOException
     */
    public static Project load(String file) throws IOException {
        Path workingDirName = Files.createTempDirectory("loadProject");
        String resultDirPath = workingDirName.toAbsolutePath().toString();

        String manifest = null;
        if (file.endsWith(EXT_PROJECT)) {  // This a zipped container
            extractZip(file,resultDirPath);

            File dir = workingDirName.toFile();
            String[] fname = dir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.equals("manifest" + EXT_MANIFEST)) return true;
                    else return false;
                }
            });

            if (fname.length < 1) {
                throw new IllegalArgumentException("Project missing manifest" + EXT_MANIFEST);
            }

            manifest = fname[0];
            Project ret = createProject(resultDirPath,manifest);

            // Who is responsible for cleaning up the project afterwards?
            return ret;
        } else if (file.endsWith(EXT_MANIFEST)) {  // Assume its already unzipped
            String wd = new File(FilenameUtils.getFullPath(file)).getCanonicalPath();
            manifest = FilenameUtils.getName(file);

            Project ret = createProject(wd,manifest);
            return ret;
        } else {
            throw new IllegalArgumentException("Unknown file type: " + FilenameUtils.getExtension(file));
        }
    }

    /**
     * Create a project from a directory
     *
     * @param dir      The directory of the unzipped project
     * @param manifest The project manifest
     * @return
     */
    private static Project createProject(String dir, String manifest) {
        try {
            Gson gson = new GsonBuilder().create();

            String mst = FileUtils.readFileToString(new File(dir + File.separator + manifest), Charset.defaultCharset());
            Map<String, Object> props = gson.fromJson(mst, Map.class);

            Project ret = new Project();

            ret.setName((String) props.get(NAME_PROP));
            ret.setAuthor((String) props.get(AUTHOR_PROP));
            ret.setLicense((String) props.get(LICENSE_PROP));

            ArrayList<ProjectItem> scripts = new ArrayList<>();
            ArrayList<VariantItem> variants = new ArrayList<>();
            ArrayList<ProjectItem> resources = new ArrayList<>();
            String cdir = new File(dir).getCanonicalPath();

            List<String> uscripts = (List<String>) props.get("scripts");
            processItem(uscripts,cdir,scripts);
            ret.setScripts(scripts);

            List<String> uvariants = (List<String>) props.get("variants");
            processVariant(uvariants,cdir,variants);
            ret.setVariants(variants);

            List<String> uresources = (List<String>) props.get("resources");
            processItem(uresources,cdir,resources);
            ret.setResources(resources);

            ret.setParentDir(dir);
            return ret;
        } catch(IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    private static void processItem(List<String> uscripts,String cdir,ArrayList<ProjectItem> scripts) throws IOException {
        if (uscripts != null) {

            for (String script : uscripts) {
                String cscript = sanitizeUserFilename(cdir + File.separator + script,cdir);

                String thumbnail = null;

                // check for thumbnail
                File tf = new File(cscript + ".png");
                if (tf.exists()) {
                    thumbnail = tf.getAbsolutePath();
                }

                String relativeScript = makeRelative(cdir,cscript);
                String relativeThumb = null;
                if (thumbnail != null) makeRelative(cdir,thumbnail);

                ProjectItem pi = new ProjectItem(relativeScript,relativeThumb);
                scripts.add(pi);
            }
        }
    }

    private static void processVariant(List<String> uvariants,String cdir,ArrayList<VariantItem> variants) throws IOException {
        if (uvariants != null) {

            for (String script : uvariants) {
                String cscript = sanitizeUserFilename(cdir + File.separator + script,cdir);

                String thumbnail = null;

                // check for thumbnail
                String tf = script + ".png";
                thumbnail = sanitizeUserFilename(cdir + File.separator + tf,cdir);
                if (!(new File(thumbnail).exists())) {
                    thumbnail = null;
                }

                String relativeScript = makeRelative(cdir,cscript);
                String relativeThumb = null;
                if (thumbnail != null) relativeThumb = makeRelative(cdir,thumbnail);

                VariantItem pi = new VariantItem(cdir,relativeScript,relativeThumb);
                variants.add(pi);
            }
        }
    }

    /**
     * Make a path relative to another
     * @param basedir
     * @param other
     * @return
     */
    public static String makeRelative(String basedir, String other) {
        try {
            File fbase = new File(basedir);
            Path base = Paths.get(fbase.getCanonicalPath());

            File fother = new File(other);

            Path relative = Paths.get(fother.getCanonicalPath());

            relative = base.relativize(relative);

            return relative.toString();
        } catch(IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    /**
     * Insure the user provided filename is safe
     * @param filename
     * @return
     */
    private static String sanitizeUserFilename(String filename, String dir) throws IOException {
        String cfile = new File(filename).getCanonicalPath();

        if (!cfile.startsWith(dir)) {
            System.out.printf("Dir: %s  cfile: %s\n",cfile,dir);
            throw new IllegalArgumentException("Project contains invalid path reference: " + cfile);
        }

        return filename;
    }

    private static void extractZipOld(String zipFile, String outputFolder, Map<String, String> sceneFiles, List<String> resources) {
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

    private static void extractZip(String zipFile, String extractFolder) {
        try {
            int BUFFER = 2048;
            File file = new File(zipFile);

            ZipFile zip = new ZipFile(file);
            String newPath = extractFolder;

            new File(newPath).mkdir();
            Enumeration zipFileEntries = zip.entries();

            // Process each entry
            while (zipFileEntries.hasMoreElements()) {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();

                File destFile = new File(newPath, currentEntry);
                //destFile = new File(newPath, destFile.getName());
                File destinationParent = destFile.getParentFile();

                // create the parent directory structure if needed
                destinationParent.mkdirs();

                if (!entry.isDirectory()) {
                    BufferedInputStream is = new BufferedInputStream(zip
                            .getInputStream(entry));
                    int currentByte;
                    // establish buffer for writing file
                    byte data[] = new byte[BUFFER];

                    // write the current file to disk
                    FileOutputStream fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos,
                            BUFFER);

                    // read and write until last byte is encountered
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, currentByte);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }


            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

}
