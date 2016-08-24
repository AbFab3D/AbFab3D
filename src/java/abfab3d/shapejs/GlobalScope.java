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

import org.mozilla.javascript.*;

import java.util.Map;

import static abfab3d.core.Output.printf;

/**
 * Global context for ecmascript
 *
 * @author Alan Hudson
 */
public class GlobalScope extends ImporterTopLevel
{
    static final long serialVersionUID = 4029130780977538005L;

    private boolean sealedStdLib = false;
    private boolean initialized;
    private static ShapeJSGlobal globals;

    public GlobalScope()
    {
    }

    public GlobalScope(Context cx)
    {
        initShapeJS(cx);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void initShapeJS(ContextFactory factory)
    {
        factory.call(new ContextAction() {
            public Object run(Context cx)
            {
                initShapeJS(cx);
                return null;
            }
        });
    }

    public void initShapeJS(Context cx) {
        // Define some global functions particular to the shell. Note
        // that these functions are not part of ECMA.
        initStandardObjects(cx, sealedStdLib);

        if (globals == null) {
            globals = new ShapeJSGlobal();
        }

        // Initialize AbFab3D specific globals
        defineFunctionProperties(globals.getFunctions(), ShapeJSGlobal.class,
                ScriptableObject.DONTENUM);

        Map<String,Object> props = globals.getProperties();
        for(Map.Entry<String,Object> e : props.entrySet()) {
            defineProperty(e.getKey(), e.getValue(),
                    ScriptableObject.DONTENUM);
        }

        initialized = true;
    }

    public ShapeJSGlobal getGlobals() {
        return globals;
    }

}


