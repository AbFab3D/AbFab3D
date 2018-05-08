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
import org.mozilla.javascript.Scriptable;

import java.io.*;

import static abfab3d.core.Output.printf;

/**
 * Parses JSON files and returns a Javascript object
 *
 * @author Alan Hudson
 */
public class JSONHandler implements URLHandler {
    public Object parse(Reader r, String basedir, Scriptable scope) throws IOException {
        Gson gson = new Gson();

        return gson.fromJson(r, Object.class);
    }
}
