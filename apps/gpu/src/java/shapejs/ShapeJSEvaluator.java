/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package shapejs;

import abfab3d.grid.Bounds;
import abfab3d.util.DataSource;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.ToolErrorReporter;

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
    private static final HashMap<String, String> errorRemap;

    /** How many header lines did we add? */
    private int headerLines;

    private GlobalScope scope;
    private ErrorReporterWrapper errors;
    private NativeObject argsMap;
    private HashMap<String,ParameterDefinition> defs;
    private Shape shape;

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

        classImports.add("abfab3d.util.PointSetArray");
        classImports.add("abfab3d.grid.Bounds");
        classImports.add("abfab3d.util.Complex");
        classImports.add("abfab3d.grid.ArrayAttributeGridShort");
        classImports.add("abfab3d.grid.ArrayAttributeGridByte");
        classImports.add("abfab3d.grid.AttributeChannel");
        classImports.add("abfab3d.geomutil.Subdivider");
        classImports.add("java.util.Vector");
        classImports.add("shapejs.Shape");
        classImports.add("abfab3d.grid.AttributeDesc");


        errorRemap = new HashMap<String, String>();
        errorRemap.put("Wrapped abfab3d.grid.util.ExecutionStoppedException", "Execution time exceeded.");
    }

    /**
     * Add default imports to a script
     *
     * @return
     */
    private String addImports(String script) {
        StringBuilder bldr = new StringBuilder();

        for (String pack : scriptImports) {
            headerLines++;
            bldr.append("importPackage(Packages.");
            bldr.append(pack);
            bldr.append(");\n");
        }

        for (String pack : classImports) {
            headerLines++;
            bldr.append("importClass(Packages.");
            bldr.append(pack);
            bldr.append(");\n");
        }


        bldr.append(script);

        return bldr.toString();
    }

    /**
     * Clear out the resources used for a job
     * @param jobID
     */
    public void clearJob(String jobID) {

    }

    /**
     * Reevaluate the script using the initial context.
     *
     * @param bounds
     * @return
     */
    public EvalResult reevalScript(String script, Bounds bounds, Map<String, Object> namedParams) {
        Context cx = Context.enter();
        DebugLogger.clearLog(cx);
        long t0 = System.currentTimeMillis();

        if (DEBUG) printf("runScript(script, %s, namedParams)\n", bounds);
        try {
            if (scope == null) {
                throw new IllegalArgumentException("Cannot reeval as scope is null");
            }

            script = addImports(script);

            if (namedParams != null) {
                for (Map.Entry<String, Object> entry : namedParams.entrySet()) {
                    printf("Changing arg: %s -> %s\n", entry.getKey(), entry.getValue().toString());
                    argsMap.defineProperty(entry.getKey(), entry.getValue().toString(), 0);

                    ParameterDefinition pd = defs.get(entry.getKey());

                    if (pd == null) {
                        return new EvalResult("Cannot find parameter: " + entry.getKey(),System.currentTimeMillis() - t0);
                    }
                    Object o = scope.get(pd.getOnChange(), scope);
                    if (o == null) {
                        return new EvalResult("Cannot find onChange function: " + pd.getOnChange(),System.currentTimeMillis() - t0);
                    }

                    Function main = (Function) o;

                    Object[] args = new Object[]{argsMap};
                    Object result2 = null;

                    try {
                        result2 = main.call(cx, scope, scope, args);
                    } catch(Exception e) {
                        e.printStackTrace();
                        String err_msg = addErrorLine(e.getMessage(), script, headerLines);
                        return new EvalResult(false,null,null,err_msg, System.currentTimeMillis() - t0);
                    }
                    if (DEBUG) printf("result of JS evaluation: %s\n", result2);

                    StringBuilder bldr = new StringBuilder();
                    for(JsError error : errors.getErrors()) {
                        String err_st = error.toString();
                        String remap = errorRemap.get(err_st);
                        if (remap != null) {
                            err_st = remap;
                        }
                        bldr.append(err_st);
                        bldr.append("\n");
                    }

                    String err_msg = bldr.toString();

                    List<String> prints = DebugLogger.getLog(cx);

                    String print_msg = "";
                    if (prints != null) {
                        for(String print : prints) {
                            bldr.append(print);
                        }
                        print_msg = bldr.toString();
                    }

                    if (pd.getOnChange().equals("main")) {
                        // We updated the who thing

                        if (result2 instanceof NativeJavaObject) {
                            Object no = ((NativeJavaObject) result2).unwrap();

                            shape = (Shape) no;
                            bounds.set(shape.getBounds());
                        }
                    }
                    return new EvalResult(true,shape.getDataSource(),print_msg,err_msg, System.currentTimeMillis() - t0);

                }
            }

        } finally {
            Context.exit();
        }

        return null;
    }


    public EvalResult evalScript(String script, Bounds bounds, Map<String, Object> namedParams) {
        long t0 = System.currentTimeMillis();

        if (DEBUG) printf("runScript(script, %s, namedParams)\n", bounds);
        Context cx = Context.enter();
        try {
            if (scope == null) {
                scope = new GlobalScope();
                ContextFactory contextFactory = new ContextFactory();
                ToolErrorReporter errorReporter = new ToolErrorReporter(false, System.err);
                errors = new ErrorReporterWrapper(errorReporter);
                contextFactory.setErrorReporter(errors);

                scope.initShapeJS(contextFactory);
                argsMap = new NativeObject();
            }

            script = addImports(script);


            if (namedParams != null) {
                for (Map.Entry<String, Object> entry : namedParams.entrySet()) {
                    printf("Adding arg: %s -> %s\n", entry.getKey(), entry.getValue().toString());
//                    argsMap.defineProperty(entry.getKey(), entry.getValue().toString(), NativeObject.READONLY);
                    argsMap.defineProperty(entry.getKey(), entry.getValue().toString(), 0);
                }
            }
            //printf("Final script:\n%s\n",script);
            try {
                Object result1 = cx.evaluateString(scope, script, "<cmd>", 1, null);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                printf("Script failed: %s\nScript:\n%s", e.getMessage(),script);
                return new EvalResult("Script failed to evaluate: " + e.getMessage(), System.currentTimeMillis() - t0);
            }

            Object uiParams = scope.get("uiParams", scope);
            parseDefinition(uiParams);

            Object o = scope.get("main", scope);

            if (o == org.mozilla.javascript.Scriptable.NOT_FOUND) {
                System.out.println("Cannot find function main");
                return new EvalResult("Cannot find main function", System.currentTimeMillis() - t0);
            }
            Function main = (Function) o;

            Object[] args = new Object[]{argsMap};
            Object result2 = null;

            try {
                result2 = main.call(cx, scope, scope, args);
            } catch(Exception e) {
                e.printStackTrace();
                String err_msg = addErrorLine(e.getMessage(), script, headerLines);
                return new EvalResult(false,null,null,err_msg, System.currentTimeMillis() - t0);
            }
            if (DEBUG) printf("result of JS evaluation: %s\n", result2);

            if (result2 instanceof NativeJavaObject) {
                Object no = ((NativeJavaObject) result2).unwrap();

                shape = (Shape) no;
                bounds.set(shape.getBounds());
                DataSource dataSource = shape.getDataSource();

                if (DEBUG) printf("end of runScript() dataSource: %s, bounds:%s\n", dataSource, bounds);

                StringBuilder bldr = new StringBuilder();
                for(JsError error : errors.getErrors()) {
                    String err_st = error.toString();
                    String remap = errorRemap.get(err_st);
                    if (remap != null) {
                        err_st = remap;
                    }
                    bldr.append(err_st);
                    bldr.append("\n");
                }

                String err_msg = bldr.toString();
                if (err_msg.length() == 0) err_msg = null;

                List<String> prints = DebugLogger.getLog(cx);

                String print_msg = "";
                if (prints != null) {
                    for(String print : prints) {
                        bldr.append(print);
                    }
                    print_msg = bldr.toString();
                    if (print_msg.length() == 0) print_msg = null;
                }

                return new EvalResult(true,dataSource,print_msg,err_msg, System.currentTimeMillis() - t0);
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

    private void parseDefinition(Object uiParams) {
        printf("Parsing uiParams: " + uiParams);
        if (defs == null) {
            defs = new HashMap<String, ParameterDefinition>();
        } else {
            defs.clear();
        }

        if (uiParams == null) return;

        if (uiParams instanceof NativeArray) {
            NativeArray arr = (NativeArray) uiParams;
            int len = (int) arr.getLength();
            printf("Params length: %d\n",len);
            for(int i=0; i < len; i++) {
                Object po = arr.get(i);
                printf("po: %s\n",po);
                NativeObject no = (NativeObject) po;
                String id = (String) no.get("id");
                String displayName = (String) no.get("displayName");
                String type = (String) no.get("type");
                String onChange = (String) no.get("onChange");
                if (onChange == null) onChange = "main";

                printf("Creating pd: id: %s name: %s type: %s onChange: %s\n",id,displayName,type,onChange);
                ParameterDefinition pd = new ParameterDefinition(id,displayName,type,onChange);
                defs.put(id,pd);
            }
        }
    }

    private String addErrorLine(String msg, String script, int header) {
        // line number is <cmd>#23 form
        int idx = msg.indexOf("<cmd>#");
        if (idx == -1) {
            return msg;
        }

        String line_st = msg.substring(idx+6);
        int idx2 = line_st.indexOf(")");
        line_st = line_st.substring(0,idx2);

        String[] lines = script.split("\r\n|\r|\n");
        int line = Integer.parseInt(line_st);

        int val = line - header;
        if (val > 0) {
            msg = msg.substring(0, idx - 1) + "\nScript Line(" + (line - header) + "): " + lines[line - 1];
        } else {
            msg = msg.substring(0, idx - 1);
        }
        return msg;
    }
}