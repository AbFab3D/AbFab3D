package shapejs;

import org.mozilla.javascript.*;
import org.mozilla.javascript.serialize.ScriptableInputStream;
import org.mozilla.javascript.serialize.ScriptableOutputStream;
import org.mozilla.javascript.tools.ToolErrorReporter;

import java.io.*;
import java.util.Map;

import shapejs.ShapeJSGlobal;

import static abfab3d.util.Output.printf;

/**
 * Global context for ecmascript
 *
 * @author Alan Hudson
 */
public class GlobalScope extends ImporterTopLevel
{
    static final long serialVersionUID = 4029130780977538005L;

    private boolean sealedStdLib = false;
    boolean initialized;

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

        ShapeJSGlobal globals = new ShapeJSGlobal();

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

}


