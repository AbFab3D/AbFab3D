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
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.RequireBuilder;
import org.mozilla.javascript.commonjs.module.provider.SoftCachingModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
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

    public GlobalScope(){
    }
    
    //public GlobalScope(Context cx) {        
    //    initShapeJS(cx,null);
    // }

    public boolean isInitialized() {
        return initialized;
    }

    public void initShapeJS(ContextFactory factory, final ArrayList<String> libDirs, final boolean sandboxed) {

        factory.call(new ContextAction() {
                public Object run(Context cx){
                    initShapeJS(cx,libDirs, sandboxed);
                    return null;
                }
            });
    }

    public void initShapeJS(Context cx, ArrayList<String> libDirs, boolean sandboxed) {
        // Define some global functions particular to the shell. Note
        // that these functions are not part of ECMA.
        initStandardObjects(cx, sealedStdLib);

        if (globals == null) {
            globals = new ShapeJSGlobal(this);
        }

        // Initialize AbFab3D specific globals
        defineFunctionProperties(globals.getFunctions(), ShapeJSGlobal.class,
                ScriptableObject.DONTENUM);

        Map<String,Object> props = globals.getProperties();
        for(Map.Entry<String,Object> e : props.entrySet()) {
            defineProperty(e.getKey(), e.getValue(),
                    ScriptableObject.DONTENUM);
        }

        if (libDirs != null) {
            
            ArrayList<String> modules = new ArrayList<>();
            for(int i = 0; i < libDirs.size(); i++){
                String dir = libDirs.get(i);
                URI uri = new File(dir).toURI();
                modules.add(uri.toASCIIString());
            }

            installRequire(cx, modules, sandboxed);  // TODO: Review sandbox rules and follow
        } else {
            printf("No basedir for global scope\n");
        }
        initialized = true;
    }

    public ShapeJSGlobal getGlobals() {
        return globals;
    }

    public Require installRequire(Context cx, List<String> modulePath, boolean sandboxed) {
        RequireBuilder rb = new RequireBuilder();
        rb.setSandboxed(sandboxed);
        List<URI> uris = new ArrayList<URI>();
        if (modulePath != null) {
            for (String path : modulePath) {
                try {
                    URI uri = new URI(path);
                    if (!uri.isAbsolute()) {
                        // call resolve("") to canonify the path
                        uri = new File(path).toURI().resolve("");
                    }
                    if (!uri.toString().endsWith("/")) {
                        // make sure URI always terminates with slash to
                        // avoid loading from unintended locations
                        uri = new URI(uri + "/");
                    }
                    uris.add(uri);
                } catch (URISyntaxException usx) {
                    throw new RuntimeException(usx);
                }
            }
        }
        rb.setModuleScriptProvider(
                new SoftCachingModuleScriptProvider(
                        new UrlModuleSourceProvider(uris, null)));
        Require require = rb.createRequire(cx, this);
        require.install(this);
        return require;
    }
}


