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

import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Handle references to project resources
 *
 * @author Alan Hudson
 */
public class ProjectResourceHandler implements URLHandler {
    public Object parse(Reader r, String path, String basedir, List<String> libDirs, Scriptable scope) throws IOException {
        return path;
    }
}
