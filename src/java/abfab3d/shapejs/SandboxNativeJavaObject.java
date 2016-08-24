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

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

import java.util.HashSet;

/**
 * Wrapper for NativeJava objects to stop getClass.  This avoids scripts using reflection to defeat security.
 *
 * @author Alan Hudson
 */
public class SandboxNativeJavaObject extends NativeJavaObject {
    private static final HashSet<String> forbidden;

    static {
        forbidden = new HashSet<String>();
        forbidden.add("getClass");
        forbidden.add("forName");
    }

    public SandboxNativeJavaObject(Scriptable scope, Object javaObject, Class staticType) {
        super(scope, javaObject, staticType);
    }

    @Override
    public Object get(String name, Scriptable start) {
        if (forbidden.contains(name)) {
            return NOT_FOUND;
        }

        return super.get(name, start);
    }
}