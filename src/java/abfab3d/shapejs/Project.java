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

import abfab3d.util.Zip;
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

    public void remove(ProjectItem pi) {
        if (pi instanceof VariantItem) {
            variants.remove(pi);
        }

        scripts.remove(pi);
        resources.remove(pi);
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

        Path pbase = Paths.get(dir);

        LinkedHashMap<String,Object> manifest = new LinkedHashMap<>();
        ArrayList<String> scriptList = new ArrayList<>();
        for(ProjectItem script : scripts) {
            scriptList.add(FilenameUtils.separatorsToUnix(script.getOrigPath()));
        }

        ArrayList<String> variantList = new ArrayList<>();
        for(ProjectItem variant : variants) {
            variantList.add(FilenameUtils.separatorsToUnix(variant.getOrigPath()));
        }

        ArrayList<String> resourceList = new ArrayList<>();
        for(ProjectItem resource : resources) {
            resourceList.add(FilenameUtils.separatorsToUnix(resource.getOrigPath()));
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

    // Resolve a resource to a local directory
    private String resolveResource(String origPath, String origFile,String destDir) throws IOException {
        // Copy the resource into the dest dir
        // Create a relative url for the resource


        // TODO: This logic is ugly.  Stems from weirdness around projectItem and what the path should
        // be relative too.

        File srcFile = new File(origPath);
        // remove all ../
        origFile = origFile.replace("../","");
        //printf("Dest Dir is: %s\n",destDir);
        //printf("Non relative path is: %s\n",origFile);
        File destFile = new File(destDir + File.separator + origFile);

        //printf("Resolving resource: %s  -> %s\n",srcFile,destFile);
        destFile.getParentFile().mkdirs();

        FileUtils.copyFile(srcFile,destFile);

        File parent = new File(new File(destDir).getParent());
        Path ppath = parent.toPath();
        Path rel = ppath.relativize(destFile.toPath());

        String ret = normalizeSlashes(rel.toString());

        return ret;
    }

    /**
     * Copy a resource and update a reference
     * @param origPath
     * @param origFile
     * @param destDir
     * @throws IOException
     */
    private void copyVariantResource(String origPath, String origFile,String destDir) throws IOException {
        // Copy the resource into the dest dir
        // Create a relative url for the resource


        File srcFile = new File(origPath);

        // Paths are relative to variant directory, make it from parent directory
        origFile = origFile.replace("../","");
        File destFile = new File(destDir + File.separator + origFile);

        destFile.getParentFile().mkdirs();

        FileUtils.copyFile(srcFile,destFile);
    }

    public void exportProjectOld(File targetDir) {
        // TODO: Lots of weirdness related to relative pathing.  Fix later.  For now
        // assume standard dirs for scripts,variants,etc

        Project resolved = new Project();

        try {
            Path tmpd = Files.createTempDirectory("exportProject");
            String tmpdSt = tmpd.toFile().getAbsolutePath();

            printf("Exporting to: %s\n",tmpdSt);
            for (VariantItem vi : getVariants()) {
                Map<String, Object> params = vi.getParams();
                HashMap<String, Object> resolvedParams = new HashMap<>();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    // TODO: How to know the datatype, need to parse the script
                    // Just check for strings with ../.. for now
                    if (entry.getValue() instanceof String) {
                        String sval = (String) entry.getValue();
                        if (sval.startsWith("../..")) {
                            // this is relative to the variant file location...
                            // TODO: For now assume that proj parent dir / variants
                            String path = resolveResource(getParentDir()+
                                            File.separator+"variants" + File.separator + sval,sval,
                                    tmpdSt + File.separator + "resolved");

                            path = normalizeSlashes("../" + path);
                            resolvedParams.put(entry.getKey(), path);
                        } else {
                            // If this file exists copy it over to dest
                            // More horribleness, assume a string with resources in it might need to be copied
                            if (sval.contains("resources")) {
                                String path = getParentDir() + File.separator + "variants" + File.separator + sval;
                                File f = new File(path);
                                if (f.exists()) {
                                    copyVariantResource(path, sval, tmpdSt);
                                }
                            }
                            resolvedParams.put(entry.getKey(),sval);
                        }
                    } else {
                        //printf("resolved params.  key: %s   class: %s\n",entry.getKey(),entry.getValue().getClass());
                        resolvedParams.put(entry.getKey(), entry.getValue());
                    }
                }

                // Assume any items in the resources dir need to be copied
                // Otherwise the manifest file must contain a valid list of resources...
                File rpath = new File(getParentDir() +
                        File.separator + "resources");
                File drpath = new File(tmpdSt +
                        File.separator + "resources");
                if (rpath.exists()) FileUtils.copyDirectory(rpath, drpath);

                // TODO: Signature line needs this, remove once fixed
                File fpath = new File(getParentDir() +
                        File.separator + "fonts");
                File frpath = new File(tmpdSt +
                        File.separator + "fonts");
                if (fpath.exists()) FileUtils.copyDirectory(fpath, frpath);

                VariantItem nvi = new VariantItem(vi.getOrigPath(),vi.getPath(),null);

                Path base = Paths.get(getParentDir() + File.separator + "variants");
                Path abs = Paths.get(vi.getMainScript());

                String ms = base.relativize(abs).toString();

                FilenameUtils.separatorsToUnix(ms);
                nvi.setMainScript(ms);
                nvi.setParams(resolvedParams);

                String vpath = tmpdSt + File.separator + vi.getOrigPath();
                new File(vpath).getParentFile().mkdirs();

                nvi.save(vpath);
                resolved.addVariant(nvi);
            }

            String projDirNormalized = FilenameUtils.normalize(getParentDir());

            for (ProjectItem script : getScripts()) {

                String tpath = script.getThumbnail();

                String origPath = FilenameUtils.separatorsToSystem(script.getOrigPath());
                String fullPath = FilenameUtils.separatorsToSystem(FilenameUtils.normalize(script.getPath()));

                if (fullPath.startsWith(projDirNormalized)) {
                    // Script was in the local dir
                    ProjectItem pi = new ProjectItem(script.getOrigPath(),script.getPath(),tpath);

                    String vpath = tmpdSt + File.separator + script.getOrigPath();
                    new File(vpath).getParentFile().mkdirs();

                    FileUtils.copyFile(new File(script.getPath()),new File(vpath));
                    resolved.addScript(pi);
                } else {
                    // Script was resolved on the path

                    int idx = fullPath.indexOf(origPath);
                    String libDir = fullPath.substring(0,idx);
                    printf("FullPath: %s  origPath: %s  libDir: %s\n",fullPath,origPath,libDir);
                    // TODO: Assume one level of dir is enough to make unique
                    String path = FilenameUtils.getPathNoEndSeparator(libDir);
                    idx = path.lastIndexOf(File.separator);
                    String shortName = "resolved" + File.separator + path.substring(idx+1) + File.separator + origPath;
                    String fullName = tmpdSt + File.separator + shortName;

                    printf("libdir: %s  shortName: %s  fullName: %s\n",libDir,shortName,fullName);
                    ProjectItem pi = new ProjectItem(shortName,fullName,tpath);

                    new File(fullName).getParentFile().mkdirs();

                    FileUtils.copyFile(new File(script.getPath()),new File(fullName));
                    resolved.addScript(pi);
                }
            }

            resolved.save(tmpdSt);

            String parentDir = getParentDir();
            int idx = parentDir.lastIndexOf(File.separator);
            if (idx > -1) {
                parentDir = parentDir.substring(idx+1);
            }
            String zipname = targetDir.getAbsolutePath() + File.separator + parentDir + ".zip";
            printf("Creating zip: %s\n",zipname);
            Zip.createZip(tmpdSt,zipname,false);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Export a project into a transmital zip
     *
     * TODO: This code is horrible because of pathing questions.  Fix later, work up an angle to blame Tony for it.
     */
    public void exportProject(File targetDir) {
        // TODO: Lots of weirdness related to relative pathing.  Fix later.  For now
        // assume standard dirs for scripts,variants,etc

        Project resolved = new Project();

        try {
            Path tmpd = Files.createTempDirectory("exportProject");
            String tmpdSt = tmpd.toFile().getAbsolutePath();

            printf("Exporting to: %s\n",tmpdSt);
            for (VariantItem vi : getVariants()) {
                Map<String, Object> params = vi.getParams();
                HashMap<String, Object> resolvedParams = new HashMap<>();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    // TODO: How to know the datatype, need to parse the script
                    // Just check for strings with ../.. for now
                    if (entry.getValue() instanceof String) {
                        String sval = (String) entry.getValue();
                        if (sval.startsWith("../..")) {
                            // this is relative to the variant file location...
                            // TODO: For now assume that proj parent dir / variants
                            String path = resolveResource(getParentDir()+
                                            File.separator+"variants" + File.separator + sval,sval,
                                    tmpdSt + File.separator + "resolved");

                            path = normalizeSlashes("../" + path);
                            resolvedParams.put(entry.getKey(), path);
                        } else {
                            resolvedParams.put(entry.getKey(), entry.getValue());
                        }
                    } else {
                        //printf("resolved params.  key: %s   class: %s\n",entry.getKey(),entry.getValue().getClass());
                        resolvedParams.put(entry.getKey(), entry.getValue());
                    }
                }

                // Assume any items in the resources dir need to be copied
                // Otherwise the manifest file must contain a valid list of resources...
                File rpath = new File(getParentDir() +
                        File.separator + "resources");
                File drpath = new File(tmpdSt +
                        File.separator + "resources");
                if (rpath.exists()) FileUtils.copyDirectory(rpath, drpath);


                VariantItem nvi = new VariantItem(vi.getOrigPath(),vi.getPath(),null);

                Path base = Paths.get(getParentDir() + File.separator + "variants");
                Path abs = Paths.get(vi.getMainScript());

                String ms = base.relativize(abs).toString();

                FilenameUtils.separatorsToUnix(ms);
                nvi.setMainScript(ms);
                nvi.setParams(resolvedParams);

                String vpath = tmpdSt + File.separator + vi.getOrigPath();
                new File(vpath).getParentFile().mkdirs();

                nvi.save(vpath);
                resolved.addVariant(nvi);
            }

            String projDirNormalized = FilenameUtils.normalize(getParentDir());

            for (ProjectItem script : getScripts()) {

                String tpath = script.getThumbnail();

                String origPath = FilenameUtils.separatorsToSystem(script.getOrigPath());
                String fullPath = FilenameUtils.separatorsToSystem(FilenameUtils.normalize(script.getPath()));

                if (fullPath.startsWith(projDirNormalized)) {
                    // Script was in the local dir
                    ProjectItem pi = new ProjectItem(script.getOrigPath(),script.getPath(),tpath);

                    String vpath = tmpdSt + File.separator + script.getOrigPath();
                    new File(vpath).getParentFile().mkdirs();

                    FileUtils.copyFile(new File(script.getPath()),new File(vpath));
                    resolved.addScript(pi);
                } else {
                    // Script was resolved on the path

                    int idx = fullPath.indexOf(origPath);
                    String libDir = fullPath.substring(0,idx);
                    printf("FullPath: %s  origPath: %s  libDir: %s\n",fullPath,origPath,libDir);
                    // TODO: Assume one level of dir is enough to make unique
                    String path = FilenameUtils.getPathNoEndSeparator(libDir);
                    idx = path.lastIndexOf(File.separator);
                    String shortName = "resolved" + File.separator + path.substring(idx+1) + File.separator + origPath;
                    String fullName = tmpdSt + File.separator + shortName;

                    printf("libdir: %s  shortName: %s  fullName: %s\n",libDir,shortName,fullName);
                    ProjectItem pi = new ProjectItem(shortName,fullName,tpath);

                    new File(fullName).getParentFile().mkdirs();

                    FileUtils.copyFile(new File(script.getPath()),new File(fullName));
                    resolved.addScript(pi);
                }
            }

            printf("Handling resources\n");
            List<ProjectItem> resources = getResources();
            for(ProjectItem res: resources) {
                File rpath;
                File drpath;

                String path = res.getOrigPath();
                if (path.endsWith("*")) {
                    printf("Dir Entry: %s\n",path);

                    String bare = path.substring(0,path.length()-1);
                    // copy direct
                    rpath = new File(getParentDir() +
                            File.separator + "resources");
                    drpath = new File(tmpdSt +
                            File.separator + bare);
                    if (rpath.exists()) FileUtils.copyDirectory(rpath, drpath);
                } else {
                    rpath = new File(res.getPath());
                    drpath = new File(tmpdSt +
                            File.separator + res.getOrigPath());
                    drpath.mkdirs();

                    printf("Copying resource: %s -> %s\n",rpath,drpath);
                    FileUtils.copyFile(rpath,drpath);
                }
            }

            resolved.save(tmpdSt);

            String parentDir = getParentDir();
            int idx = parentDir.lastIndexOf(File.separator);
            if (idx > -1) {
                parentDir = parentDir.substring(idx+1);
            }
            String zipname = targetDir.getAbsolutePath() + File.separator + parentDir + ".zip";
            printf("Creating zip: %s\n",zipname);
            Zip.createZip(tmpdSt,zipname,false);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Normalize the slashes in a path
     * @param st
     * @return
     */
    private String normalizeSlashes(String st) {
        return st.replace("\\","/");
    }

    /**
     * Load a project.
     *
     * @param file - Either a .shapejsprj file or the unzipped manifest.json
     * @return
     * @throws IOException
     */
    public static Project load(String file, List<String> libDirs) throws IOException {
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
            Project ret = createProject(resultDirPath,libDirs,manifest);

            // Who is responsible for cleaning up the project afterwards?
            return ret;
        } else if (file.endsWith(EXT_MANIFEST)) {  // Assume its already unzipped
            String wd = new File(FilenameUtils.getFullPath(file)).getCanonicalPath();
            manifest = FilenameUtils.getName(file);

            Project ret = createProject(wd,libDirs,manifest);
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
    private static Project createProject(String dir, List<String> libDirs, String manifest) {
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
            String projDir = new File(dir).getCanonicalPath();

            List<String> uscripts = (List<String>) props.get("scripts");
            processItem(uscripts,projDir,libDirs,scripts);
            ret.setScripts(scripts);

            List<String> uvariants = (List<String>) props.get("variants");
            processVariant(uvariants,projDir,libDirs,variants);
            ret.setVariants(variants);

            List<String> uresources = (List<String>) props.get("resources");
            processItem(uresources,projDir,libDirs,resources);
            ret.setResources(resources);

            ret.setParentDir(dir);
            return ret;
        } catch(IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    /**
     * Strip nulls from a list.  Trailing
     * @param list
     */
    private static List stripNull(List list) {
        Iterator itr = list.iterator();

        while(itr.hasNext()) {
            Object p = itr.next();

            if (p == null) {
                itr.remove();
            }
        }

        return list;
    }

    /**
     * Resolves a resource given by searching the project and libDirs.  Supports a single wildcard ending
     * to denote a directory resource
     *
     * @param res
     * @param projDir
     * @param libDirs
     * @return The absolute path of the item or null if not found
     */
    private static String resolveResource(String res, String projDir, List<String> libDirs) {
        File f = new File(projDir + File.separator + res);
        if (f.exists()) {
            return f.getAbsolutePath();
        }

        for(String dir : libDirs) {
            f = new File(dir + File.separator + res);
            if (f.exists()) {
                return f.getAbsolutePath();
            }
        }

        // handle wildcard case
        if (!res.endsWith("*")) return null;

        String baseRes = res.substring(0,res.length()-1);

        f = new File(projDir + File.separator + baseRes);
        if (f.exists()) {
            return f.getAbsolutePath() + File.separator + "*";
        }

        for(String dir : libDirs) {
            f = new File(dir + File.separator + baseRes);
            if (f.exists()) {
                return f.getAbsolutePath() + File.separator + "*";
            }
        }

        return null;
    }

    private static void processItem(List<String> uscripts,String projDir,List<String> libDirs,List<ProjectItem> scripts) throws IOException {
        if (uscripts == null) return;

        for (String script : uscripts) {
            if (script == null) continue;


            String rscript = resolveResource(script,projDir,libDirs);

            if (rscript == null) {
                printf("Cannot resolve ProjectItem: %s\n",script);
                rscript = "Resource not found";
            }
            String thumbnail = null;

            // check for thumbnail
            File tf = new File(rscript + ".png");
            if (tf.exists()) {
                thumbnail = tf.getAbsolutePath();
            }

            ProjectItem pi = new ProjectItem(script,rscript,thumbnail);
            scripts.add(pi);
        }
    }

    private static void processVariant(List<String> uvariants,String projDir,List<String> libDirs,List<VariantItem> variants) throws IOException {
        if (uvariants != null) {

            for (String script : uvariants) {
                if (script == null) continue;

                String rscript = resolveResource(script,projDir,libDirs);

                String thumbnail = null;

                // check for thumbnail
                File tf = new File(rscript + ".png");
                if (tf.exists()) {
                    thumbnail = tf.getAbsolutePath();
                }

                VariantItem pi = new VariantItem(projDir,script,rscript,thumbnail);
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
