/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.shapejs;

import org.mozilla.javascript.Scriptable;

import java.io.*;
import java.util.List;

/**
 * Parses a file and returns a Javascript object
 *
 * TODO: This concept is still experimental and likely will change
 *
 * @author Alan Hudson
 */
public interface URLHandler {
    public Object parse(Reader r, String path,String basedir, List<String> libDirs, Scriptable scope) throws IOException;
}
