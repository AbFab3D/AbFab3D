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
import java.util.Map;

import static abfab3d.util.Output.printf;

/**
 * Evaluate a ShapeJS script to get its graph
 *
 * @author Alan Hudson
 */
public class ShapeJSEvaluator {

    final static boolean DEBUG = true;

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

        bldr.append("importClass(Packages.abfab3d.util.PointSetArray);\n");
        bldr.append("importClass(Packages.abfab3d.util.Complex);\n");
        bldr.append("importClass(Packages.abfab3d.grid.ArrayAttributeGridShort);\n");
        bldr.append("importClass(Packages.abfab3d.grid.ArrayAttributeGridByte);\n");
        bldr.append("importClass(Packages.abfab3d.grid.AttributeDesc);\n");
        bldr.append("importClass(Packages.abfab3d.grid.Bounds);\n");
        bldr.append("importClass(Packages.abfab3d.grid.AttributeDesc);\n");
        bldr.append("importClass(Packages.abfab3d.grid.AttributeChannel);\n");
        bldr.append("importClass(Packages.abfab3d.geomutil.Subdivider);\n");
        bldr.append("importClass(Packages.java.util.Vector);\n");
        bldr.append("importClass(Packages.shapejs.Shape);\n");

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

    public DataSource runScript(File file, Bounds bounds) {

        if(DEBUG)printf("runScript(%s, %s)\n", file, bounds);
        Context cx = Context.enter();
        try {
            GlobalScope scope = new GlobalScope();
            ContextFactory contextFactory = new ContextFactory();
            //.setErrorReporter(errorReporter);

            scope.initShapeJS(contextFactory);

            String script = FileUtils.readFileToString(file);
            script = addImports(script);
            // don't replace from now on 
            //script = replaceMakeGrid(script);

            //printf("Final script:\n%s\n",script);
            Object result1 = cx.evaluateString(scope, script, "<cmd>", 1, null);

            Object o = scope.get("main", scope);

            if (o == org.mozilla.javascript.Scriptable.NOT_FOUND) {
                System.out.println("Cannot find function main");
            }
            Function main = (Function) o;

            Object[] args = new Object[0];
            Object result2 = main.call(cx, scope, scope, new Object[] {args});

            if(DEBUG)printf("result of JS evaluation: %s\n", result2);

            if (result2 instanceof NativeJavaObject) {
                Object no = ((NativeJavaObject)result2).unwrap();
                
                Shape shape = (Shape) no;
                bounds.set(shape.getBounds());
                DataSource dataSource = shape.getDataSource();

                if(DEBUG)printf("end of runScript() dataSource: %s, bounds:%s\n", dataSource, bounds);

                return dataSource;

                /*
                ShapeJSStore store = (ShapeJSStore) no;

                if(DEBUG)printf("store: %s\n", store);

                List<DataSource> source_list = store.getDataSources();
                List<Bounds> bounds_list = store.getBounds();
                for(int i=0; i < source_list.size(); i++) {
                    // TODO: Handle lists
                    DataSource source = source_list.get(i);
                    bounds.set(bounds_list.get(i));
                    return source;
                }
                */
            }

        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            Context.exit();
        }

        return null;
    }

    public DataSource runScript(String script, Bounds bounds, Map<String,Object> namedParams) {

        if(DEBUG)printf("runScript(script, %s, namedParams)\n", bounds);
        Context cx = Context.enter();
        try {
            GlobalScope scope = new GlobalScope();
            ContextFactory contextFactory = new ContextFactory();
            //.setErrorReporter(errorReporter);

            scope.initShapeJS(contextFactory);

            script = addImports(script);

            // don't replace from now on
            //script = replaceMakeGrid(script);

            NativeObject argsMap = new NativeObject();

            for(Map.Entry<String, Object> entry : namedParams.entrySet()){
                printf("Adding arg: %s -> %s\n",entry.getKey(),entry.getValue().toString());
                argsMap.defineProperty(entry.getKey(), entry.getValue().toString(), NativeObject.READONLY);
            }
/*
            Object argForMain = argsMap;
            try {
                argForMain = new JsonParser(cx,scope).parseValue(new Gson().toJson(argsMap));
            } catch (Exception e) {e.printStackTrace();}
*/

            //printf("Final script:\n%s\n",script);
            try {
                Object result1 = cx.evaluateString(scope, script, "<cmd>", 1, null);
            } catch(Exception e) {
                printf("Script failed: %s\n",script);
            }
            Object o = scope.get("main", scope);

            if (o == org.mozilla.javascript.Scriptable.NOT_FOUND) {
                System.out.println("Cannot find function main");
            }
            Function main = (Function) o;

            Object[] args = new Object[] { argsMap};
            Object result2 = main.call(cx, scope, scope, args);

            if(DEBUG)printf("result of JS evaluation: %s\n", result2);

            if (result2 instanceof NativeJavaObject) {
                Object no = ((NativeJavaObject)result2).unwrap();
                
                Shape shape = (Shape) no;
                bounds.set(shape.getBounds());
                DataSource dataSource = shape.getDataSource();

                if(DEBUG)printf("end of runScript() dataSource: %s, bounds:%s\n", dataSource, bounds);

                return dataSource;
                /*
                ShapeJSStore store = (ShapeJSStore) no;
                if(DEBUG)printf("store: %s\n", store);
                List<DataSource> source_list = store.getDataSources();
                List<Bounds> bounds_list = store.getBounds();
                for(int i=0; i < source_list.size(); i++) {
                    // TODO: Handle lists
                    DataSource source = source_list.get(i);
                    bounds.set(bounds_list.get(i));
                    return source;
                }
                */
            }

        } finally {
            Context.exit();
        }

        return null;
    }
}

