package shapejs;

import abfab3d.grid.Bounds;
import abfab3d.util.DataSource;
import org.apache.commons.io.FileUtils;
import org.mozilla.javascript.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static abfab3d.util.Output.printf;

/**
 * Evaluate a ShapeJS script to get its graph
 *
 * @author Alan Hudson
 */
public class ShapeJSEvaluator {
    /** Packages allowed to be imported.  Security mechanism */
    private static final ArrayList<String> packageWhitelist;

    /** Default imports to add to scripts */
    private static final ArrayList<String> scriptImports;
    private static final ArrayList<String> classImports;

    /** Remap error messages to something readable */
    private static final HashMap<String,String> errorRemap;

    static {
        packageWhitelist = new ArrayList();
        packageWhitelist.add("abfab3d.");
        packageWhitelist.add("javax.vecmath");
        packageWhitelist.add("java.lang");
        packageWhitelist.add("app.common");
        packageWhitelist.add("shapejs");

        scriptImports = new ArrayList<String>();

        scriptImports.add("abfab3d.datasources");
        scriptImports.add("abfab3d.transforms");
        scriptImports.add("abfab3d.grid.op");
        scriptImports.add("javax.vecmath");

        classImports = new ArrayList<String>();
        classImports.add("abfab3d.grid.Model");

        // Do not make abfab3d.io.output exposed as a package big security hole
        classImports.add("abfab3d.io.output.SingleMaterialModelWriter");
        classImports.add("abfab3d.io.output.VoxelModelWriter");
        classImports.add("shapejs.ShapeJSStore");

        errorRemap = new HashMap<String,String>();
        errorRemap.put("Wrapped abfab3d.grid.util.ExecutionStoppedException","Execution time exceeded.");
    }

    /**
     * Add default imports to a script
     * @return
     */
    private String addImports(String script) {
        StringBuilder bldr = new StringBuilder();

        for(String pack : scriptImports) {
            bldr.append("importPackage(Packages.");
            bldr.append(pack);
            bldr.append(");\n");
        }

        for(String pack : classImports) {
            bldr.append("importClass(Packages.");
            bldr.append(pack);
            bldr.append(");\n");
        }

        bldr.append(script);

        return bldr.toString();
    }

    /**
     * Replace any makeGrid call to convert to a ShapeJS Graph.
     *
     * @param script
     * @return
     */
    private String replaceMakeGrid(String script) {
       // TODO: be janky right now just string replace "maker.makeGrid(grid)"
        return script.replace("maker.makeGrid(grid);","var ret_ss = new ShapeJSStore(); ret_ss.addMaker(maker,grid); return ret_ss;");
    }

    public DataSource runScript(String filename, Bounds bounds) {

        Context cx = Context.enter();
        try {
            GlobalScope scope = new GlobalScope();
            ContextFactory contextFactory = new ContextFactory();
            //.setErrorReporter(errorReporter);

            scope.initShapeJS(contextFactory);

            String script = FileUtils.readFileToString(new File(filename));
            script = addImports(script);
            script = replaceMakeGrid(script);

            printf("Final script:\n%s\n",script);
            Object result1 = cx.evaluateString(scope, script, "<cmd>", 1, null);

            Object o = scope.get("main", scope);

            if (o == org.mozilla.javascript.Scriptable.NOT_FOUND) {
                System.out.println("Cannot find function main");
            }
            Function main = (Function) o;

            System.out.println("Main is: " + main.getClass());

            Object[] args = new Object[0];
            Object result2 = main.call(cx, scope, scope, new Object[] {args});

            if (result2 instanceof NativeJavaObject) {
                Object no = ((NativeJavaObject)result2).unwrap();

                ShapeJSStore store = (ShapeJSStore) no;
                List<DataSource> source_list = store.getDataSources();
                List<Bounds> bounds_list = store.getBounds();
                for(int i=0; i < source_list.size(); i++) {
                    // TODO: Handle lists
                    DataSource source = source_list.get(i);
                    bounds.set(bounds_list.get(i));
                    return source;
                }
            }
            System.err.println("Result:" + result2.getClass().getName());
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            Context.exit();
        }

        return null;
    }
}

