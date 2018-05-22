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

import abfab3d.param.Parameter;
import com.google.gson.Gson;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static abfab3d.core.Output.printf;

/**
 * Parses ShapeJS Variant files and returns a Javascript object
 *
 * Handles some of the features of shapevar files such as
 *    unit conversion
 *    change relative uri to project dir form instead of from variant dir
 *
 * @author Alan Hudson
 */
public class ShapeVariantHandler implements URLHandler {
    public Object parse(Reader r, String basedir, List<String> libDirs, Scriptable scope) throws IOException {
        Variant var = new Variant();

        // Relative pathing needs a file in the variant dir but it doesn't really have to exist
        String vardir = basedir + File.separator + "variants" + File.separator + "fake.shapevar";

        ArrayList<String> libs = new ArrayList<String>();
        libs.add(basedir);
        libs.addAll(libDirs);

        try {
            var.readDesign(libs, vardir, r);
        } catch(Exception e) {

            e.printStackTrace();
            return null;
        }

        // Strategy is to parse with Variant and then save parameters into map

        EvaluatedScript escript = var.getEvaluatedScript();
        Map<String,Parameter> params = escript.getScriptParams();

        HashMap<String,Object> ret = new HashMap<>();

        ret.put("scriptPath",var.getScriptPath());
        HashMap<String,Object> scriptParams = new HashMap<>();
        for(Parameter p : params.values()) {
            //printf("val: %s --> %s\n",p.getName(),p.getValue());
            Object jo = ShapeJSEvaluator.convParameterToJSObject(scope,p);

            scriptParams.put(p.getName(), jo);
        }

        ret.put("scriptParams",scriptParams);

        return ret;
    }
}
