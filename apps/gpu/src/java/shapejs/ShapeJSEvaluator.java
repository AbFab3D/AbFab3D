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
import abfab3d.param.*;
import abfab3d.util.DataSource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.ToolErrorReporter;

import javax.vecmath.Vector3d;
import java.lang.reflect.Type;
import java.util.*;

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
    private HashMap<String,Parameter> defs;
    private Shape shape;

    /** Should we run this in a sandbox, default it true */
    private boolean sandboxed;

    private static Type stringListType = new TypeToken<List<String>>() {}.getType();
    private static Type doubleListType = new TypeToken<List<Double>>() {}.getType();

    // scratch variables
    private double[] dArray1 = new double[3];
    private double[] dArray2 = new double[3];
    private Vector3d v3d1 = new Vector3d();
    private Vector3d v3d2 = new Vector3d();
    private Gson gson = new GsonBuilder().create();

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

    public ShapeJSEvaluator() {
        this.sandboxed = true;

        printf("TODO: Security hole, overriding sandboxed");
        sandboxed = false;
    }

    public ShapeJSEvaluator(boolean sandboxed) {
        this.sandboxed = sandboxed;
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

        if (DEBUG) printf("reevalScript(script, %s, namedParams)\n", bounds);
        try {
            if (scope == null) {
                throw new IllegalArgumentException("Cannot reeval as scope is null");
            }

            script = addImports(script);

            if (namedParams != null) {
                mungeParams(defs,namedParams);

                for (Map.Entry<String, Object> entry : namedParams.entrySet()) {
                    printf("Changing arg: %s -> %s\n", entry.getKey(), entry.getValue().toString());
                    argsMap.defineProperty(entry.getKey(), entry.getValue(), 0);
                }

                boolean main_called = false;

                for (Map.Entry<String, Object> entry : namedParams.entrySet()) {

                    Parameter pd = defs.get(entry.getKey());

                    if (pd == null) {
                        return new EvalResult("Cannot find parameter: " + entry.getKey(),System.currentTimeMillis() - t0);
                    }

                    String onChange = pd.getOnChange();

                    if (onChange == null) {
                        return new EvalResult("Cannot find onChange property: " + pd.getName(), System.currentTimeMillis() - t0);
                    }
                    if (main_called && onChange.equals("main")) {
                        continue;
                    }
                    Object o = scope.get(onChange, scope);
                    if (o == null) {
                        return new EvalResult("Cannot find onChange function: " + pd.getOnChange(), System.currentTimeMillis() - t0);
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

                    if (onChange.equals("main")) {
                        // We updated the who thing
                        main_called = true;

                        if (result2 instanceof NativeJavaObject) {
                            Object no = ((NativeJavaObject) result2).unwrap();

                            shape = (Shape) no;
                            bounds.set(shape.getBounds());
                        }
                    }

                }
            }

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

            return new EvalResult(true,shape.getDataSource(),print_msg,err_msg, System.currentTimeMillis() - t0);

        } finally {
            Context.exit();
        }
    }


    /**
     * Convert JSON encoded params into real params based on types
     * @param params The parameter definitions
     * @param namedParams
     */
    private void mungeParams(Map<String,Parameter> params, Map<String, Object> namedParams) {
        for (Map.Entry<String, Object> entry : namedParams.entrySet()) {
            String key = entry.getKey();
            Object no = entry.getValue();
            Parameter param = params.get(key);
            Object wrapped =  null;

            if (no instanceof ScriptableObject) {
                // Already in the correct form
                continue;
            }

            String json = (String) entry.getValue();

            if (param == null) {
                printf("Unknown param: %s\nparams: %s",key,params);
                continue;
            }

            printf("Munging: %s  type: %s\n",param.getName(), param.getType());
            try {

                switch (param.getType()) {
                    case DOUBLE:
                        Double dv = gson.fromJson(json, Double.class);
                        DoubleParameter dp = (DoubleParameter) param;
                        dp.setValue(dv);
                        wrapped = new ParameterJSWrapper(scope,dp);
                        break;
                    case DOUBLE_LIST:
                        DoubleListParameter dlp = (DoubleListParameter) param;
                        try {
                            List<Double> dlv = gson.fromJson(json, doubleListType);
                            dlp.setValue(dlv);
                        } catch(JsonSyntaxException jse) {
                            // try single number form
                            Number dlv = gson.fromJson(json, Number.class);
                            ArrayList<Double> dlv2 = new ArrayList<Double>();
                            dlv2.add(new Double(dlv.doubleValue()));
                            dlp.setValue(dlv2);
                        }
                        wrapped = new ArrayJSWrapper(scope,dlp);
                        break;
                    case STRING:
                        String sv = null;
                        try {
                            sv = gson.fromJson(json, String.class);
                        } catch(JsonSyntaxException jse) {
                            sv = json;
                        }
                        StringParameter sp = (StringParameter) param;
                        sp.setValue(sv);
                        wrapped = new ParameterJSWrapper(scope,sp);
                        break;
                    case URI:
                        // TODO: not JSON encoded decide if we want this
                       //String uv = gson.fromJson(json, String.class);
                        String uv = json;
                        URIParameter up = (URIParameter) param;
                        up.setValue(uv);
                        wrapped = new ParameterJSWrapper(scope,up);
                        break;
                    case URI_LIST:
                        String[] ulv = gson.fromJson(json, String[].class);
                        URIListParameter ulp = (URIListParameter) param;
                        ulp.setValue(ulv);
                        wrapped = new ArrayJSWrapper(scope,ulp);
                        break;
                    case STRING_LIST:
                        List<String> slv = gson.fromJson(json, stringListType);
                        StringListParameter slp = (StringListParameter) param;
                        slp.setValue(slv);
                        wrapped = new ArrayJSWrapper(scope,slp);
                        break;
                    case LOCATION:
                        Map<String, Object> map = gson.fromJson(json, Map.class);
                        Vector3d point = null;
                        Vector3d normal = null;
                        Object o = map.get("point");
                        if (o != null) {
                            mungeToVector3d(o, v3d1);
                            point = v3d1;
                        }
                        o = map.get("normal");
                        if (o != null) {
                            mungeToVector3d(o, v3d2);
                            normal = v3d2;
                        }

                        LocationParameter lp = (LocationParameter) param;
                        if (point != null) lp.setPoint(point);
                        if (normal != null) lp.setNormal(normal);
                        wrapped = new ComplexJSWrapper(scope,param);
                        //wrapped =  Context.javaToJS(lp, scope);

                        break;
                }
                namedParams.put(key, wrapped);
            } catch(Exception e) {
                printf("Error parsing: " + json);
                e.printStackTrace();
            }
        }
    }

    public EvalResult evalScript(String script, String method, Bounds bounds, Map<String, Object> namedParams) {
        long t0 = System.currentTimeMillis();

        if (DEBUG) printf("evalScript(script, sandbox: %b namedParams)\n", sandboxed,bounds);
        Context cx = Context.enter();
        Context.ClassShutterSetter setter = cx.getClassShutterSetter();

        if (sandboxed && setter != null) {
            setter.setClassShutter(new ClassShutter() {
                public boolean visibleToScripts(String className) {
                    // Do not allow recreation of this class ever
                    if (className.equals("ShapeJSEvaluator")) {
                        return false;
                    }

                    for (String pack : packageWhitelist) {
                        if (className.startsWith(pack)) {
                            return true;
                        }

                    }

                    for (String specific : classImports) {
                        if (className.equals(specific)) {
                            return true;
                        }

                    }

                    printf("Rejecting class: %s\n", className);
                    return false;
                }
            });
        }

        try {
            if (scope == null) {
                scope = new GlobalScope();
                ContextFactory contextFactory = null;

                if (sandboxed) {
                    contextFactory = new SandboxContextFactory();
                } else {
                    contextFactory = new ContextFactory();
                }
                ToolErrorReporter errorReporter = new ToolErrorReporter(false, System.err);
                errors = new ErrorReporterWrapper(errorReporter);
                contextFactory.setErrorReporter(errors);

                scope.initShapeJS(contextFactory);
                argsMap = new NativeObject();
            }

            script = addImports(script);


            printf("Final script:\n%s\n",script);
            try {
                Object result1 = cx.evaluateString(scope, script, "<cmd>", 1, null);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                printf("Script failed: %s\nScript:\n%s", e.getMessage(),script);
                return new EvalResult("Script failed to evaluate: " + e.getMessage(), System.currentTimeMillis() - t0);
            }

            Object uiParams = scope.get("uiParams", scope);
            printf("uiParams: %s\n",uiParams);
            parseDefinition(uiParams);

            if (namedParams != null) {
                mungeParams(defs,namedParams);
                for (Map.Entry<String, Object> entry : namedParams.entrySet()) {
                    printf("Adding arg: %s -> %s\n", entry.getKey(), entry.getValue().toString() + " class: " + entry.getValue().getClass());

                    argsMap.defineProperty(entry.getKey(), entry.getValue(), 0);
                }
            }

            if (method == null) {
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

                return new EvalResult(true,null,null,err_msg,defs,(System.currentTimeMillis() - t0));
            }

            Object o = scope.get(method, scope);

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

                return new EvalResult(true,dataSource,print_msg,err_msg, defs,System.currentTimeMillis() - t0);
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
        if (defs == null) {
            defs = new HashMap<String, Parameter>();
        } else {
            defs.clear();
        }

        if (uiParams == null) {
            return;
        }
        if (!(uiParams instanceof NativeArray)) return;
        NativeArray arr = (NativeArray) uiParams;
        int len = (int) arr.getLength();
        printf("Params length: %d\n",len);
        for(int i=0; i < len; i++) {
            Object po = arr.get(i);
            printf("po: %s\n",po);
            NativeObject no = (NativeObject) po;
            String name = (String) no.get("name");
            String desc = (String) no.get("desc");
            String type = ((String) no.get("type")).toUpperCase();
            String onChange = (String) no.get("onChange");

            Object defaultValue = no.get("defaultVal");
            if (onChange == null) onChange = "main";

            if (type.endsWith("[]")) {
                type = type.substring(0,type.length()-2) + "_LIST";
            }
            ParameterType ptype = ParameterType.valueOf(type);
            Parameter pd = null;
            Object val = null;

            printf("Creating definition:  %s  type: %s\n",name,type);
            switch(ptype) {
                case DOUBLE:
                    double rangeMin = Double.NEGATIVE_INFINITY;
                    double rangeMax = Double.POSITIVE_INFINITY;
                    double step = 1.0;
                    double def = 0;

                    val = no.get("rangeMin");
                    if (val != null) {
                        rangeMin = ((Number) val).doubleValue();
                    }
                    val = no.get("rangeMax");
                    if (val != null) {
                        rangeMax = ((Number) val).doubleValue();
                    }
                    val = no.get("step");
                    if (val != null) {
                        step = ((Number) val).doubleValue();
                    }
                    val = no.get("defaultVal");
                    if (val != null) {
                        def = ((Number) val).doubleValue();
                    }

                    pd = new DoubleParameter(name,desc,def, rangeMin, rangeMax,step);
                    break;
                case STRING:
                    pd = new StringParameter(name,desc,(String) defaultValue);
                    break;
                case URI:
                    pd = new URIParameter(name,desc,(String) defaultValue);
                    break;
                case URI_LIST:
                    NativeArray ula = (NativeArray) defaultValue;
                    ArrayList<URIParameter> ul = new ArrayList<URIParameter>();
                    for(int j=0; j < ula.size(); j++) {
                        ul.add(new URIParameter(name,desc,(String)ula.get(j)));
                    }
                    pd = new URIListParameter(name,desc,ul);
                    break;
                case STRING_LIST:
                    NativeArray sla = (NativeArray) defaultValue;
                    ArrayList<StringParameter> sl = new ArrayList<StringParameter>();
                    for(int j=0; j < sla.size(); j++) {
                        sl.add(new StringParameter(name,desc,(String)sla.get(j)));
                    }
                    pd = new StringListParameter(name,desc,sl);
                    break;
                case DOUBLE_LIST:
                    double dlRangeMin = Double.NEGATIVE_INFINITY;
                    double dlRangeMax = Double.POSITIVE_INFINITY;
                    double dlStep = 1.0;
                    ArrayList<DoubleParameter> dll = new ArrayList<DoubleParameter>();
                    double dlDef = 0;

                    val = no.get("rangeMin");
                    if (val != null) {
                        dlRangeMin = ((Number) val).doubleValue();
                    }
                    val = no.get("rangeMax");
                    if (val != null) {
                        dlRangeMax = ((Number) val).doubleValue();
                    }
                    val = no.get("step");
                    if (val != null) {
                        dlStep = ((Number) val).doubleValue();
                    }
                    val = no.get("defaultVal");
                    if (val != null) {
                        if (val instanceof Number) {
                            dlDef = ((Number) val).doubleValue();
                            dll.add(new DoubleParameter(name,desc,dlDef,dlRangeMin,dlRangeMax,dlStep));
                        } else if (val instanceof NativeArray) {
                            NativeArray dla = (NativeArray) defaultValue;
                            for(int j=0; j < dla.size(); j++) {
                                dll.add(new DoubleParameter(name,desc,((Number)dla.get(j)).doubleValue(),dlRangeMin,dlRangeMax,dlStep));
                            }

                        }
                    }

                    pd = new DoubleListParameter(name,desc,dll,dlRangeMin,dlRangeMax,dlStep);
                    break;

                case LOCATION:
                    // TODO: garbage

                    // default should be a map of point and normal
                    Map<String,Object> defmap = (Map<String,Object>) defaultValue;
                    Vector3d p = null;
                    Vector3d n = null;
                    double[] point = null;
                    double[] normal = null;

                    if (defmap != null) {
                        Object o = defmap.get("point");
                        printf("point: %s\n", o);
                        if (o != null) {
                            mungeToDoubleArray(o, dArray1);
                            point = dArray1;
                        }
                        o = defmap.get("normal");
                        printf("normal: %s\n", o);
                        if (o != null) {
                            mungeToDoubleArray(o, dArray2);
                            normal = dArray2;
                        }

                        if (point != null) p = new Vector3d(point);
                        if (normal != null) n = new Vector3d(normal);
                    }

                    pd = new LocationParameter(name,desc,p,n);
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled parameter type: " + ptype);
            }
            pd.setOnChange(onChange);
            printf("Creating pd: id: %s name: %s type: %s onChange: %s\n",name,desc,type,onChange);
            defs.put(name,pd);
        }
    }

    private void mungeToDoubleArray(Object in, double[] out) {
        if (in == null) return;

        if (in instanceof NativeArray) {
            out[0] = ((Number)((NativeArray)in).get(0)).doubleValue();
            out[1] = ((Number)((NativeArray)in).get(1)).doubleValue();
            out[2] = ((Number)((NativeArray)in).get(2)).doubleValue();
        } else if (in instanceof double[]) {
            out[0] = ((double[])in)[0];
            out[1] = ((double[])in)[1];
            out[2] = ((double[])in)[2];
        } else if (in instanceof int[]) {
            out[0] = ((int[]) in)[0];
            out[1] = ((int[]) in)[1];
            out[2] = ((int[]) in)[2];
        } else if (in instanceof List) {
            List list = (List) in;
            out[0] = ((Number)list.get(0)).doubleValue();
            out[1] = ((Number)list.get(1)).doubleValue();
            out[2] = ((Number)list.get(2)).doubleValue();
        } else {
            throw new IllegalArgumentException("Unhandled type: " + in + " class: " + in.getClass());
        }
    }

    private void mungeToVector3d(Object in, Vector3d out) {
        if (in == null) return;

        if (in instanceof NativeArray) {
            out.x = ((Number)((NativeArray)in).get(0)).doubleValue();
            out.y = ((Number)((NativeArray)in).get(1)).doubleValue();
            out.z = ((Number)((NativeArray)in).get(2)).doubleValue();
        } else if (in instanceof double[]) {
            out.x = ((double[])in)[0];
            out.y = ((double[])in)[1];
            out.z = ((double[])in)[2];
        } else if (in instanceof int[]) {
            out.x = ((int[]) in)[0];
            out.y = ((int[]) in)[1];
            out.z = ((int[]) in)[2];
        } else if (in instanceof List) {
            List list = (List) in;
            out.x = ((Number)list.get(0)).doubleValue();
            out.y = ((Number)list.get(1)).doubleValue();
            out.z = ((Number)list.get(2)).doubleValue();
        } else {
            throw new IllegalArgumentException("Unhandled type: " + in + " class: " + in.getClass());
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