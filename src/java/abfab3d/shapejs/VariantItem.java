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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static abfab3d.core.Output.printf;

/**
 * An item that can be included in a Project
 *
 * @author Alan Hudson
 */
public class VariantItem extends ProjectItem {
    protected String mainScript;
    protected Map<String,Object> params;

    public VariantItem(String pdir,String origPath, String fullPath, String thumbnail) {
        super(origPath,fullPath,thumbnail);

        parse(pdir,origPath,fullPath);
    }

    public VariantItem(String origPath,String fullPath,String thumbnail) {
        super(origPath,fullPath,thumbnail);

        params = new HashMap<>();
    }

    private void parse(String pdir,String origPath,String fullPath) {
        Gson gson = new GsonBuilder().create();

        try {
            String mst = FileUtils.readFileToString(new File(fullPath), Charset.defaultCharset());
            Map<String, Object> props = gson.fromJson(mst, Map.class);
            mainScript = (String) props.get("scriptPath");
            params = (Map<String,Object>) props.get("scriptParams");  // TODO: This loses datatyping

            File ms = new File(pdir + File.separator + "variants" + File.separator + mainScript);
            mainScript = FilenameUtils.normalize(ms.getAbsolutePath());
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void save(String file) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            HashMap<String, Object> obj = new HashMap<>();

            // make the mainScript value be relative to the variant file
            obj.put("scriptPath", FilenameUtils.separatorsToUnix(mainScript));
            obj.put("scriptParams", params);
            String st = gson.toJson(obj);
            FileUtils.writeStringToFile(new File(file), st);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public String getMainScript() {
        return mainScript;
    }

    public void setMainScript(String mainScript) {
        this.mainScript = mainScript;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
