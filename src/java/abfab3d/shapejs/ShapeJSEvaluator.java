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

import abfab3d.core.Color;
import abfab3d.core.Material;
import abfab3d.param.*;
import abfab3d.util.Unit;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.ToolErrorReporter;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Vector3d;

import java.io.File;
import java.lang.reflect.Type;
import java.util.*;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;

/**
 * Evaluate a ShapeJS script to get its graph.
 *
 * General security mechanism is a classes package must be listed in the packageWhitelist.
 * If not, then it must be in classWhitelist otherwise its not allowed.
 *
 * @author Alan Hudson
 */
public class ShapeJSEvaluator implements MaterialMapper {

    final static boolean DEBUG = false;

    /** Packages allowed to be imported.  Security mechanism */
    private static ArrayList<String> packageWhitelist = new ArrayList<String>();
    /** Specific whitelisted classes allowed even when the general package is not */
    private static HashSet<String> classWhiteList = new HashSet<String>();

    /** Default imports to add to scripts */
    private static ArrayList<String> scriptImports = new ArrayList<String>();
    private static ArrayList<String> classImports = new ArrayList<String>();
    private static boolean securitySetup = false;

    /** Remap error messages to something readable */
    private static final HashMap<String, String> errorRemap;

    /** How many header lines did we add? */
    private static int headerLines;

    /** Default parameter definitions */
    private static Map<String,Parameter> defaultParams;
    private GlobalScope scope;
    private ErrorReporterWrapper errors;
    private NativeObject argsMap;
    private LinkedHashMap<String,Parameter> types;
    private LinkedHashMap<String,Parameter> defs;
    private HashSet<String> defaultProvided;
    private Scene scene;

    /** Should we run this in a sandbox, default it true */
    private boolean sandboxed;

    private static Type stringListType = new TypeToken<List<String>>() {}.getType();
    private static Type doubleListType = new TypeToken<List<Double>>() {}.getType();
    private static Type axisAngle4DType = new TypeToken<AxisAngle4d>() {}.getType();

    // Import list baked down to a string for speed
    private static String imports;

    private EvaluatedScript result = null;
    private String script = null;

    // scratch variables
    private double[] dArray1 = new double[3];
    private double[] dArray2 = new double[3];
    private double[] dArray3 = new double[3];
    private double[] dArray4 = new double[3];
    private Vector3d v3d1 = new Vector3d();
    private Vector3d v3d2 = new Vector3d();
    private Gson gson = JSONParsing.getJSONParser();

    private ClassShutter shutter = null;

    /** Common error fields on definitions to error out on */
    private static HashSet<String> errorFields;

    private static final HashSet<String> restrictedPackages;
    private static MaterialMapper matMapper;
    private static LinkedHashMap<String,Material> materials = new LinkedHashMap<String,Material>();

    static {

        restrictedPackages = new HashSet();
        restrictedPackages.add("java.lang");

        errorFields = new HashSet<String>();
        errorFields.add("defaultValue");
        errorFields.add("units");

        errorRemap = new HashMap<String, String>();
        errorRemap.put("Wrapped abfab3d.grid.util.ExecutionStoppedException", "Execution time exceeded.");

        // Create imports

        defaultParams = new HashMap<String,Parameter>();
/*
        EnumParameter matParam = new EnumParameter("material","Physical Material",new String[] {"None","White"},"None");
        matParam.setLabel("Material");
        matParam.setOnChange("main");
        defaultParams.put("material",matParam);
*/
        materials = new LinkedHashMap<String,Material>();
        materials.put(SingleColorMaterial.getInstance().getName(),SingleColorMaterial.getInstance());
        materials.put(FullColorMaterial.getInstance().getName(),FullColorMaterial.getInstance());
        Materials.add(SingleColorMaterial.getInstance().getName(),SingleColorMaterial.getInstance());
        Materials.add(FullColorMaterial.getInstance().getName(),FullColorMaterial.getInstance());

        setupSecurity();
    }

    public ShapeJSEvaluator() {
        this.sandboxed = true;
        types = new LinkedHashMap<String,Parameter>();
        defs = new LinkedHashMap<String,Parameter>();
        defaultProvided = new HashSet<String>();

        initHeader();
    }

    public ShapeJSEvaluator(boolean sandboxed) {
        this.sandboxed = sandboxed;
        defaultProvided = new HashSet<String>();

        initHeader();
    }

    private static void setupSecurity() {
        // Make sure you add a the packages for all classes you want
        packageWhitelist.add("javax.vecmath");
        packageWhitelist.add("abfab3d.datasources");
        packageWhitelist.add("abfab3d.transforms");
        packageWhitelist.add("abfab3d.shapejs");
        packageWhitelist.add("abfab3d.grid.op");
        packageWhitelist.add("abfab3d.grid");
        packageWhitelist.add("abfab3d.geomutil");
        packageWhitelist.add("abfab3d.param");
        packageWhitelist.add("abfab3d.util");

        // Packages we want imported to script by default
        scriptImports.add("abfab3d.datasources");
        scriptImports.add("abfab3d.transforms");
        scriptImports.add("abfab3d.grid.op");
        scriptImports.add("abfab3d.core");
        scriptImports.add("javax.vecmath");

        // Classes we want imported by default
        classImports.add("java.util.Vector");

        // Do not make abfab3d.io.output exposed as a package big security hole
        classImports.add("abfab3d.io.output.SingleMaterialModelWriter");
        classImports.add("abfab3d.io.output.VoxelModelWriter");

        // Be explicit about io.input to stop reading of disk contents
        classImports.add("abfab3d.io.input.AttributedMesh");
        classImports.add("abfab3d.util.MeshRasterizer");

        classImports.add("abfab3d.shapejs.Scene");
        classImports.add("abfab3d.shapejs.Light");
        classImports.add("abfab3d.shapejs.Viewpoint");
        classImports.add("abfab3d.shapejs.Background");
        classImports.add("abfab3d.shapejs.Materials");
        classImports.add("abfab3d.shapejs.SingleColorMaterial");
        classImports.add("abfab3d.shapejs.FullColorMaterial");

        classImports.add("abfab3d.io.input.ModelLoader");

        classImports.add("abfab3d.core.MathUtil");
        classImports.add("abfab3d.core.Color");
        classImports.add("abfab3d.core.Bounds");
        classImports.add("abfab3d.core.Vec");
        classImports.add("abfab3d.param.Shape");
        classImports.add("abfab3d.util.PointSetArray");
        classImports.add("abfab3d.util.Complex");
        classImports.add("abfab3d.util.ShapeProducer");

        classImports.add("abfab3d.core.MaterialType");

        classImports.add("abfab3d.grid.ArrayAttributeGridShort");
        classImports.add("abfab3d.grid.ArrayAttributeGridByte");
        classImports.add("abfab3d.core.GridDataChannel");
        classImports.add("abfab3d.grid.Model");
        classImports.add("abfab3d.core.GridDataDesc");

        classImports.add("abfab3d.geomutil.Subdivider");

/*
        // TODO: This shouldnt be needed but it seems to allow anything from material once in
        classImports.add("material.DefaultMaterial");
        classImports.add("material.WSFMaterial");
        classImports.add("material.WSFPolishedMaterial");
        classImports.add("material.WhiteMaterial");
        classImports.add("material.StainlessMaterial");
        classImports.add("material.CeramicsMaterial");
*/
        classImports.add("material.BlueSFPMaterial");
        classImports.add("material.RedSFPMaterial");
        classImports.add("material.PurpleSFPMaterial");
        classImports.add("material.BlueGemMaterial");

        classWhiteList = new HashSet<String>();
        classWhiteList.add("java.lang.Boolean");
        classWhiteList.add("java.lang.Byte");
        classWhiteList.add("java.lang.Character");
        classWhiteList.add("java.lang.Class");
        classWhiteList.add("java.lang.Double");
        classWhiteList.add("java.lang.Enum");
        classWhiteList.add("java.lang.Float");
        classWhiteList.add("java.lang.Object");
        classWhiteList.add("java.lang.String");
        classWhiteList.add("java.lang.reflect.Array");
        classWhiteList.add("java.util.Vector");
        classWhiteList.add("java.util.ArrayList");
        classWhiteList.add("java.util.Map");
        classWhiteList.add("java.util.HashMap");
        classWhiteList.add("java.util.LinkedHashMap");
        classWhiteList.add("java.awt.image.BufferedImage");
        classWhiteList.add("java.awt.Color");
        classWhiteList.add("sun.java2d.SunGraphics2D");  // Needed for image creation

    }
    private static void initHeader() {
        StringBuilder bldr = new StringBuilder();
        headerLines = 0;

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


        imports = bldr.toString();
        //printf("Initializing Header.  len: %d   imports: %d\n",headerLines,imports.length());
    }

    public static void setMaterialMapper(MaterialMapper mm) {
        matMapper = mm;
    }

    public static void configureSecurity(List<String> pwl, List<String> cwl, List<String> ci, List<String> si) {
        if (securitySetup) {
            printf("SECURITY: Attempt to configure security twice.");
            throw new IllegalArgumentException("Security cannot be configured twice");
        }
        for(String entry : pwl) {
            if (restrictedPackages.contains(entry)) {
                printf("SECURITY: Attempt to add restricted package: %s\n",entry);
                continue;
            }
            packageWhitelist.add(entry);
        }

        classWhiteList.addAll(cwl);
        classImports.addAll(ci);
        scriptImports.addAll(si);

        initHeader();

        securitySetup = true;
    }

    @Override
    public Material getImplementation(String mat) {
        return materials.get(mat);
    }

    @Override
    public Map<String, Material> getMaterials() {
        return materials;
    }

    /**
     * Add default imports to a script
     *
     * @return
     */
    private String addImports(String script) {
        return imports + script;
    }

    /**
     * Clear out the resources used for a job
     */
    public void clear() {
        result = null;
        types = null;
        defs = null;
        scope = null;
        argsMap = null;
        scene = null;
    }

    /**
     * Parse the script.  Stores its parameter definitions.
     *
     * @param val Thr script value
     * @return
     */
    public void parseScript(String val) {
        long t0 = System.currentTimeMillis();

        if (sandboxed && !ContextFactory.hasExplicitGlobal()) {
            org.mozilla.javascript.ContextFactory.GlobalSetter gsetter = ContextFactory.getGlobalSetter();

            if (gsetter != null) {
                gsetter.setContextFactoryGlobal(new SandboxContextFactory());
            }
        }

        if (DEBUG) printf("parseScript(this: %s script, sandbox: %b)\n", this,sandboxed);
        Context cx = Context.enter();

        try {
            Context.ClassShutterSetter setter = cx.getClassShutterSetter();
            DebugLogger.clearLog(cx);

            if (sandboxed && setter != null) {
                setter.setClassShutter(getShutter());
            }
            if (scope == null) {
                ContextFactory contextFactory = null;
                contextFactory = new ContextFactory();

                ToolErrorReporter errorReporter = new ToolErrorReporter(false, System.err);
                errors = new ErrorReporterWrapper(errorReporter);
                contextFactory.setErrorReporter(errors);

                scope = new GlobalScope();
                scope.initShapeJS(contextFactory);
                argsMap = new NativeObject();
            }

            script = addImports(val);


            //printf("Final script:\n%s\n",script);
            Object scene = null;
            try {
                scene = cx.evaluateString(scope, script, "<cmd>", 1, null);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                if(DEBUG)printf("Script failed: %s\nScript:\n%s", e.getMessage(),script);
                result = new EvaluatedScript(ShapeJSErrors.ErrorType.PARSING_ERROR, e.getMessage(), getPrintLogs(cx),System.currentTimeMillis() - t0);
                return;
            }

            // Set parameter types
            try {
                Object ptypes = scope.get("types", scope);
                types = parseDefinition(ptypes, true);
            } catch (ClassCastException cce) {
                result =  new EvaluatedScript(ShapeJSErrors.ErrorType.INVALID_PARAMETER_VALUE, cce.getMessage(), getPrintLogs(cx),System.currentTimeMillis() - t0);
                return;
            }

            // Set parameter definitions
            try {
                Object params = scope.get("params", scope);
                if (params == null || params == UniqueTag.NOT_FOUND) params = scope.get("uiParams", scope);

                defs = parseDefinition(params, false);

                if (params == null && scene != null) {
                    printf("Scene: %s\n", scene);
                    //addDataSourceParams(result1)
                }
            } catch (ClassCastException cce) {
                result =  new EvaluatedScript(ShapeJSErrors.ErrorType.INVALID_PARAMETER_VALUE, cce.getMessage(), getPrintLogs(cx),System.currentTimeMillis() - t0);
                return;
            }

        } finally {
            Context.exit();
        }

        result = new EvaluatedScript(true,val,null,null,null,null,defs,(System.currentTimeMillis() - t0));
    }

    private ClassShutter getShutter() {
        if (shutter == null) {
            shutter = new ClassShutter() {
                public boolean visibleToScripts(String className) {
                    if (classWhiteList.contains(className)) {
                        //printf("Allowing: %s\n",className);
                        return true;
                    }

                    // Do not allow recreation of this class ever
                    if (className.equals("ShapeJSEvaluator")) {
                        return false;
                    }

                    for (String pack : packageWhitelist) {
                        if (className.startsWith(pack)) {
                            //printf("Allowing: %s\n",className);
                            return true;
                        }

                    }

                    for (String specific : classImports) {
                        if (className.equals(specific)) {
                            //printf("Allowing: %s\n",className);
                            return true;
                        }

                    }

                    //printf("Rejecting class: %s\n", className);
                    return false;
                }
            };}

        return shutter;
    }

    /**
     * Get the parameter type for a param.  The current script must be parsed first.
     * @param param
     * @return The type or null if not found
     */
    public ParameterType getType(String param) {

        Parameter p = defs.get(param);

        if (p == null) return null;

        if (types.get(param) != null) {
            return (types.get(param)).getType();
        } else {
            return p.getType();
        }
    }

    /**
     * Get the parameter type for a param.  The current script must be parsed first.
     * @param param
     * @return The type or null if not found
     */
    public Parameter getParameter(String param) {

        return defs.get(param);
    }

    public Map<String,Parameter> getDefinitions() {
        return defs;
    }

    public Map<String,Parameter> getTypes() {
        return types;
    }

    /**
     * Returns the result of executing this script.  A null result means no errors yet.
     * @return
     */
    public EvaluatedScript getResult() {
        return result;
    }

    /**
     * Reevaluate the script using the initial context.
     *
     * @return
     */
    public EvaluatedScript reevalScript(String script, Map<String, Object> namedParams) {
        long t0 = System.currentTimeMillis();
        Context cx = Context.enter();
        DebugLogger.clearLog(cx);

        try {
            if (scope == null) {
                throw new IllegalArgumentException("Cannot reeval as scope is null");
            }

            if (DEBUG) printf("reevalScript(this: %s , script, namedParams)\n",this);
            if (namedParams != null) {
/*
				// ScripManager update() already calls mungeParams before calling this method
            	try {
            		mungeParams(namedParams,false);
            	} catch (IllegalArgumentException iae) {
            		// TODO: Use INVALID_PARAMETER_VALUE error string and include parameter name and value
            		return new EvalResult(ErrorType.INVALID_PARAMETER_VALUE, iae.getMessage(), System.currentTimeMillis() - t0);
            	}
*/
                for (Map.Entry<String, Object> entry : namedParams.entrySet()) {
                    if (entry.getValue() == null) {
                        if (DEBUG) printf("Removing arg: %s\n", entry.getKey());
                        argsMap.remove(entry.getKey());
                    } else {
                        if (DEBUG) printf("Changing arg: %s -> %s\n", entry.getKey(), entry.getValue().toString());
                        Object argVal = entry.getValue();

                        if (argVal instanceof SandboxNativeJavaObject) {
                            SandboxNativeJavaObject wrapper = (SandboxNativeJavaObject) argVal;
                            Object no = wrapper.unwrap();
                            if (no instanceof LocationParameter) {
                                LocationParameter lp = (LocationParameter) no;
                                if (DEBUG) printf("---> param: %s point: %s normal: %s\n",no,lp.getPoint(),lp.getNormal());
                            } else if (no instanceof UserDefinedParameter) {
                                UserDefinedParameter udp = (UserDefinedParameter) no;
                                Map<String, Parameter> udpvals = udp.getValue();
                                NativeObject udpArgs = new NativeObject();
                                if (DEBUG) printf("---> param: %s vals: %s \n",udp.getName(),udpvals);
                                for (Map.Entry<String, Parameter> vals : udpvals.entrySet()) {
                                    Parameter val = vals.getValue();
                                    udpArgs.defineProperty(val.getName(), val.getValue(), 0);
                                }
                                argVal = udpArgs;
                            } else {
                                if (DEBUG) printf("---> param: %s\n",entry.getKey());
                            }
                        } else {
                            try {
                                ParameterJSWrapper wrapper = (ParameterJSWrapper) argVal;
                                Parameter p = wrapper.getParameter();
                                if (p instanceof DoubleParameter) {
                                    DoubleParameter dp = (DoubleParameter) p;
                                    argVal = dp.getUnit().getConversionVal(dp.getValue());
                                    if (DEBUG) printf("---> param: %s defValue: %s\n", wrapper.getParameter(), argVal);
                                } else {
                                    if (DEBUG)
                                        printf("---> param: %s defValue: %s\n", wrapper.getParameter(), wrapper.getDefaultValue(null));
                                }
                            } catch(ClassCastException cce) {
                                cce.printStackTrace();
                            }
                        }

                        argsMap.defineProperty(entry.getKey(), argVal, 0);
                    }
                }

                //printf("args map: %s\n",((NativeObject)argsMap).entrySet());
                boolean main_called = false;

                for (Map.Entry<String, Object> entry : namedParams.entrySet()) {

                    Parameter pd = defs.get(entry.getKey());

                    if (pd == null) {
                        if (DEBUG) printf("Cannot find parameter: %s\n",entry.getKey());
                        continue;
                    }

                    String onChange = pd.getOnChange();

                    if (onChange == null) {
                        String[] args = new String[] {pd.getName()};
                        result = new EvaluatedScript(ShapeJSErrors.ErrorType.ONCHANGE_PROPERTY_NOT_FOUND, args, System.currentTimeMillis() - t0);
                        return result;
                    }
                    if (main_called && onChange.equals("main")) {
                        continue;
                    }
                    Object o = scope.get(onChange, scope);
                    if (o == null) {
                        String[] args = new String[] {pd.getName(), onChange};
                        result = new EvaluatedScript(ShapeJSErrors.ErrorType.ONCHANGE_FUNCTION_NOT_FOUND, args, System.currentTimeMillis() - t0);
                        return result;
                    }

                    if (!(o instanceof Function)) {
                        String[] args = new String[] {pd.getName(), onChange};
                        result = new EvaluatedScript(ShapeJSErrors.ErrorType.ONCHANGE_FUNCTION_NOT_FOUND, args, System.currentTimeMillis() - t0);
                        return result;
                    }

                    Function main = (Function) o;

                    Object[] args = new Object[]{argsMap};
                    Object result2 = null;

                    try {
                        result2 = main.call(cx, scope, scope, args);
                    } catch(Exception e) {
                        e.printStackTrace();
                        String msg = null;
                        if (e instanceof WrappedException) {
                            WrappedException we = (WrappedException) e;
                            String line = e.getMessage();
                            int idx = line.lastIndexOf("(<cmd>");
                            if (idx != -1) {
                                line = line.substring(idx);
                            } else {
                                line = null;
                            }
                            msg = ((WrappedException) e).getWrappedException().getMessage();
                            if (line != null) msg = msg + line;
                        } else {
                            msg = e.getMessage();
                        }

                        result = new EvaluatedScript(ShapeJSErrors.ErrorType.PARSING_ERROR, addErrorLine(msg, this.script, headerLines), getPrintLogs(cx),System.currentTimeMillis() - t0);
                        return result;
                    }
                    if (DEBUG) printf("result of JS evaluation: %s\n", result2);

                    if (onChange.equals("main")) {
                        // We updated the who thing
                        main_called = true;

                        if (result2 instanceof NativeJavaObject) {
                            Object no = ((NativeJavaObject) result2).unwrap();

                            scene = (Scene) no;
                        }
                    }

                }
            }

            result = new EvaluatedScript(true,script,scene,getPrintLogs(cx),null,null,defs,System.currentTimeMillis() - t0);

            // Get all errors in a string array
            List<JsError> errorList = errors.getErrors();

            if (errorList != null && errorList.size() > 0) {
                int len = errorList.size();
                for (int i=0; i<len; i++) {
                    String err_st = errorList.get(i).toString();
                    String remap = errorRemap.get(err_st);
                    if (remap != null) {
                        err_st = remap;
                    }
                    result.addErrorLog(ShapeJSErrors.ErrorType.PARSING_ERROR, err_st);
                }
            }

            if (DEBUG) printf("reeval done.  time: %d ms\n",(System.currentTimeMillis() - t0));
            return result;
        } finally {
            Context.exit();
        }
    }


    private String[] getPrintLogs(Context cx) {
        List<String> prints = DebugLogger.getLog(cx);
        DebugLogger.clear(cx);

        String[] print_logs = prints != null ? (String[])prints.toArray(new String[prints.size()]) : null;

        return print_logs;
    }

    /**
     * Convert JSON encoded params into real params based on types
     * @param namedParams
     */
    public void mungeParams(Map<String, Object> namedParams, boolean createDefault) {
        Context cx = Context.enter();

        try {
            Map<String, Parameter> params = defs;

            for (Map.Entry<String, Object> entry : namedParams.entrySet()) {
                String key = entry.getKey();
                Object no = entry.getValue();
                Parameter param = params.get(key);
                Scriptable wrapped = null;

                if (no == null || no instanceof ScriptableObject) {
                    // If null, we want to remove the parameter, so skip parsing/munging
                    // If it is a ScriptableObject, it's already in the correct form
                    continue;
                }
                Object value = entry.getValue();
                String json = null;

                if (param == null) {
                    if (DEBUG) printf("Unknown param: %s: value: %s\nparams: %s\n", key, json, params);
                    continue;
                }

                switch(param.getType()) {
                    case LOCATION:
                        // expect Vector3d[]
                        if (value instanceof Vector3d[]) {

                            param.setValue(value);
                            namedParams.put(key,(Scriptable) Context.javaToJS(param, scope));
/*
                            // TODO: This smells bad
                            Vector3d[] vecs = (Vector3d[]) value;
                            HashMap<String,Object> map = new HashMap<String, Object>();
                            map.put("point",vecs[0]);
                            map.put("normal",vecs[1]);

                            param.setValue(map);
                            namedParams.put(key, new ParameterJSWrapper(scope, param));
*/
                            return;
                        }
                        // fall through to default handling
                    default:
                        // TODO: Should we move this to type specific logic all the time?
                        if (value instanceof String) {
                            json = (String) value;
                        } else if (value instanceof SandboxNativeJavaObject) {
                            // already munged ignore
                            continue;
                        } else if (value instanceof Color) {
                            param.setValue(value);
                            namedParams.put(key, new ParameterJSWrapper(scope, param));
                            continue;
                        } else if (value instanceof AxisAngle4d) {
                            param.setValue(value);
                            namedParams.put(key, new ParameterJSWrapper(scope, param));
                            continue;
                        } else {
                            //printf("Default to toString for param: %s  class: %s\n", key, value.getClass());
                            json = value.toString();
                        }
                }
                wrapped = mungeParam(param,json);
                namedParams.put(key, wrapped);
            }

            if (createDefault) {
                for (Map.Entry<String, Parameter> entry : defs.entrySet()) {
                    String key = entry.getKey();
                    Parameter param = entry.getValue();

                    if (namedParams.containsKey(key)) continue;

                    // We have parameter without a named param
                    Scriptable wrapped = null;

                    Object value = param.getDefaultValue();

                    if (value == null) continue;

                    if (!defaultProvided.contains(key)) {
                        if (DEBUG) printf("  no default: %s\n", key);
                        continue;
                    }

                    wrapped = mungeParam(param,value);
                    namedParams.put(key, wrapped);
                }
            }
        } finally {
            Context.exit();
        }
    }

    private Scriptable mungeParam(Parameter param, String json) {
        Scriptable wrapped = null;

        if (DEBUG) printf("Munging: %s  type: %s\n", param.getName(), param.getType());
        try {

            switch (param.getType()) {
                case BOOLEAN:
                    Boolean bv = gson.fromJson(json, Boolean.class);
                    BooleanParameter bp = (BooleanParameter) param;
                    bp.setValue(bv);
                    wrapped = new ParameterJSWrapper(scope, bp);
                    break;
                case DOUBLE:
                    Double dv = gson.fromJson(json, Double.class);
                    DoubleParameter dp = (DoubleParameter) param;
                    dp.setValue(dv);
                    wrapped = new ParameterJSWrapper(scope, dp);
                    break;
                case INTEGER:
                    Integer iv = gson.fromJson(json, Integer.class);
                    IntParameter ip = (IntParameter) param;
                    ip.setValue(iv);
                    wrapped = new ParameterJSWrapper(scope, ip);
                    break;
                case DOUBLE_LIST:
                    List<Number> dlv = gson.fromJson(json, doubleListType);
                    NativeArray dlna = new NativeArray(dlv.size());
                    int dllen = dlv.size();
                    for (int i = 0; i < dllen; i++) {
                        Double num = null;
                        Object nob = dlv.get(i);
                        if (nob instanceof Double) num = (Double) nob;
                        else num = ((Number) nob).doubleValue();
                        dlna.put(i, dlna, new ParameterJSWrapper(scope, new DoubleParameter(param.getName(), param.getDesc(), num)));
                    }
                    wrapped = dlna;
                    break;
                case AXIS_ANGLE_4D:
                    AxisAngle4d aa = null;
                    String jsonVal = json.trim();

                    // Handle string value in both formats:
                    // - double list [x,y,z,angle]
                    // - axis angle {"x":1,"y":0,"z":0,"angle":0}
                    if (jsonVal.startsWith("[")) {
                        aa = new AxisAngle4d();
                        List<Number> aalv = gson.fromJson(jsonVal, doubleListType);
                        int aalen = aalv.size();

                        if (aalen != 4) {
                            throw new IllegalArgumentException("Axis angle must be 4 values: " + param.getName() + " val: " + json);
                        }

                        Object nob = aalv.get(0);
                        if (nob instanceof Double) aa.x = (Double) nob;
                        else aa.x = ((Number) nob).doubleValue();
                        nob = aalv.get(1);
                        if (nob instanceof Double) aa.y = (Double) nob;
                        else aa.y = ((Number) nob).doubleValue();
                        nob = aalv.get(2);
                        if (nob instanceof Double) aa.z = (Double) nob;
                        else aa.z = ((Number) nob).doubleValue();
                        nob = aalv.get(3);
                        if (nob instanceof Double) aa.angle = (Double) nob;
                        else aa.angle = ((Number) nob).doubleValue();
                    } else if (jsonVal.startsWith("{")) {
                        aa = (AxisAngle4d) gson.fromJson(jsonVal, axisAngle4DType);
                    }

                    wrapped = new ParameterJSWrapper(scope, new AxisAngle4dParameter(param.getName(), param.getDesc(), aa));
                    break;
                case STRING:
                    String sv = null;
                    try {
                        // A regular string is invalid json, but gson parses it and returns a "null" string
                        if (json.equals("")) {
                            throw new JsonSyntaxException("Invalid json: " + json);
                        }
                        // json of string consisting of spaces also returns a "null" string
                        String trimmed = json.trim();
                        if (trimmed.equals("")) {
                        	sv = json;
                        } else {
                        	sv = gson.fromJson(json, String.class);
                        }
                    } catch (JsonSyntaxException jse) {
                        sv = json;
                    }
                    StringParameter sp = (StringParameter) param;
                    sp.setValue(sv);
                    wrapped = new ParameterJSWrapper(scope, sp);
                    break;
                case COLOR:
                    String cv = null;
                    try {
                        cv = gson.fromJson(json, String.class);
                    } catch (JsonSyntaxException jse) {
                        cv = json;
                    }

                    if (cv == null) {
                        cv = json;
                    }

                    ColorParameter cp = (ColorParameter) param;
                    cp.setValue(Color.fromHEX(cv));
                    wrapped = new ParameterJSWrapper(scope, cp);
                    break;
                case ENUM:
                    String ev = null;
                    try {
                        ev = gson.fromJson(json, String.class);
                    } catch (JsonSyntaxException jse) {
                        ev = json;
                    }
                    EnumParameter ep = (EnumParameter) param;
                    ep.setValue(ev);
                    wrapped = new ParameterJSWrapper(scope, ep);
                    break;
                case URI:
                    // TODO: not JSON encoded decide if we want this
                    String uv = null;
                    if (json.charAt(0) == '"') {
                        uv = gson.fromJson(json, String.class);
                    } else {
                        uv = json;
                    }

                    //uv = Utils.checkForValidShapewaysUri(uv);
                    URIParameter up = (URIParameter) param;
                    up.setValue(uv);
                    wrapped = new ParameterJSWrapper(scope, up);
                    break;
                case URI_LIST:
                    String[] ulv = gson.fromJson(json, String[].class);
                    NativeArray ulna = new NativeArray(ulv.length);
                    int ullen = ulv.length;
                    for (int i = 0; i < ullen; i++) {
                        String st = ulv[i];
                        //st = Utils.checkForValidShapewaysUri(st);
                        ulna.put(i, ulna, new ParameterJSWrapper(scope, new URIParameter(param.getName(), param.getDesc(), st)));
                    }
                    wrapped = ulna;
                    break;
                case STRING_LIST:
                    List<String> slv = gson.fromJson(json, stringListType);
                    NativeArray slna = new NativeArray(slv.size());
                    int sllen = slv.size();
                    for (int i = 0; i < sllen; i++) {
                        String st = slv.get(i);
                        slna.put(i, slna, new ParameterJSWrapper(scope, new StringParameter(param.getName(), param.getDesc(), st)));
                        slna.put(i, slna, new ParameterJSWrapper(scope, new StringParameter(param.getName(), param.getDesc(), st)));
                    }
                    wrapped = slna;
                    break;
                case LOCATION:
                    if (json == null || json.length() == 0 || json.equals("null") || json.equals("\"null\"")) return null;

                    if (DEBUG) printf("Parsing location: json is: %s\n",json);

                    Map<String, Object> map = gson.fromJson(json, Map.class);
                    if (map == null) return null;

                    Vector3d point = null;
                    Vector3d normal = null;
                    Object o = map.get("point");
                    if (o != null) {
                        mungeToVector3d(o, v3d1);
                        point = new Vector3d(v3d1);
                    }
                    o = map.get("normal");
                    if (o != null) {
                        mungeToVector3d(o, v3d2);
                        normal = new Vector3d(v3d2);
                    }

                    LocationParameter lp = (LocationParameter) param;
                    if (point != null && normal != null) {
                        Vector3d[] lpVal = {point, normal};
                        lp.setValue(lpVal);
                        wrapped = (Scriptable) Context.javaToJS(lp, scope);
                    }

                    break;
                case USERDEFINED:
                    // A user-defined parameter's values are a map of parameters
                    UserDefinedParameter udp = (UserDefinedParameter) param;
                    Map<String, Object> propvals = gson.fromJson(json, Map.class);

                    // Iterate the new values and set it for the correct property value of the user-defined param
                    for(Map.Entry<String, Object> propval : propvals.entrySet()) {
                        Parameter sbp = udp.getProperty(propval.getKey());
                        if (sbp != null) {
                            Scriptable s = mungeParam(sbp, (propval.getValue()));
                            udp.setPropertyValue(propval.getKey(), (Parameter)s);
                        }
                    }
                    wrapped = (Scriptable) Context.javaToJS(udp, scope);
                    break;
            }
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException(iae.getMessage());
        } catch (Exception e) {
            printf("Error parsing: %s\n", json);
            e.printStackTrace();
        }

        return wrapped;
    }

    private Scriptable mungeParam(Parameter param, Object value) {
        Scriptable wrapped = null;

        if (DEBUG) printf("Munging: %s  type: %s\n", param.getName(), param.getType());
        try {

            switch (param.getType()) {
                case BOOLEAN:
                    Boolean bv = (Boolean) value;
                    BooleanParameter bp = (BooleanParameter) param;
                    bp.setValue(bv);
                    wrapped = new ParameterJSWrapper(scope, bp);
                    break;
                case DOUBLE:
                    Double dv = (Double) value;
                    DoubleParameter dp = (DoubleParameter) param;
                    dp.setValue(dv);
                    wrapped = new ParameterJSWrapper(scope, dp);
                    break;
                case INTEGER:
                    Integer iv = (Integer) value;
                    IntParameter ip = (IntParameter) param;
                    ip.setValue(iv);
                    wrapped = new ParameterJSWrapper(scope, ip);
                    break;
                case DOUBLE_LIST:
                    List<Number> dlv = (List<Number>) value;
                    NativeArray dlna = new NativeArray(dlv.size());
                    int dllen = dlv.size();
                    for (int i = 0; i < dllen; i++) {
                        Double num = null;
                        Object nob = dlv.get(i);
                        if (nob instanceof Double) num = (Double) nob;
                        else num = ((Number) nob).doubleValue();
                        dlna.put(i, dlna, new ParameterJSWrapper(scope, new DoubleParameter(param.getName(), param.getDesc(), num)));
                    }
                    wrapped = dlna;
                    break;
                case AXIS_ANGLE_4D:
                    // At this point, value should already be an AxisAngle4d
                    AxisAngle4d aa = (AxisAngle4d) value;
                    wrapped = new ParameterJSWrapper(scope, new AxisAngle4dParameter(param.getName(), param.getDesc(), aa));
                    break;
                case STRING:
                    String sv = (String) value;

                    StringParameter sp = (StringParameter) param;
                    sp.setValue(sv);
                    wrapped = new ParameterJSWrapper(scope, sp);
                    break;
                case COLOR:
                    Color cv = (Color) value;
                    ColorParameter cp = (ColorParameter) param;
                    cp.setValue(cv);
                    wrapped = new ParameterJSWrapper(scope, cp);
                    break;
                case ENUM:
                    String ev = (String) value;

                    EnumParameter ep = (EnumParameter) param;
                    ep.setValue(ev);
                    wrapped = new ParameterJSWrapper(scope, ep);
                    break;
                case URI:
                    // TODO: not JSON encoded decide if we want this
                    String uv = (String) value;

                    //uv = Utils.checkForValidShapewaysUri(uv);
                    URIParameter up = (URIParameter) param;
                    up.setValue(uv);
                    wrapped = new ParameterJSWrapper(scope, up);
                    break;
                case URI_LIST:
                    String[] ulv = (String[]) value;
                    NativeArray ulna = new NativeArray(ulv.length);
                    int ullen = ulv.length;
                    for (int i = 0; i < ullen; i++) {
                        String st = ulv[i];
                        //st = Utils.checkForValidShapewaysUri(st);
                        ulna.put(i, ulna, new ParameterJSWrapper(scope, new URIParameter(param.getName(), param.getDesc(), st)));
                    }
                    wrapped = ulna;
                    break;
                case STRING_LIST:
                    List<String> slv = (List<String>) value;
                    NativeArray slna = new NativeArray(slv.size());
                    int sllen = slv.size();
                    for (int i = 0; i < sllen; i++) {
                        String st = slv.get(i);
                        slna.put(i, slna, new ParameterJSWrapper(scope, new StringParameter(param.getName(), param.getDesc(), st)));
                        slna.put(i, slna, new ParameterJSWrapper(scope, new StringParameter(param.getName(), param.getDesc(), st)));
                    }
                    wrapped = slna;
                    break;
                case LOCATION:
                    LocationParameter lp = null;

                    if (value == null) return null;

                    if (!(value instanceof Vector3d[])) {
                        Map<String, Object> map =(Map<String, Object>) value;
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
                        lp = (LocationParameter) param;
                        if (point != null) lp.setPoint(point);
                        if (normal != null) lp.setNormal(normal);
                    } else {
                        lp = (LocationParameter) param;
                        lp.setValue(value);
                    }
                    wrapped = (Scriptable) Context.javaToJS(lp, scope);
                    break;
                case USERDEFINED:
                    // A user-defined parameter's values are a map of parameters
                    UserDefinedParameter udp = (UserDefinedParameter) param;
                    Map<String, Object> propvals = (Map<String, Object>) value;

                    // Iterate the new values and set it for the correct property value of the user-defined param
                    for(Map.Entry<String, Object> propval : propvals.entrySet()) {
                        Parameter sbp = udp.getProperty(propval.getKey());

                        Scriptable s = null;
                        if (propval.getValue() instanceof Parameter) {
                            s = new ParameterJSWrapper(scope, (Parameter)propval.getValue());
                        } else {
                            s = mungeParam(sbp, propval.getValue());
                        }

                        udp.setPropertyValue(propval.getKey(), (Parameter)s);
                    }

                    wrapped = (Scriptable) Context.javaToJS(udp, scope);
                    break;
            }
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException(iae.getMessage());
        } catch (Exception e) {
            printf("Error parsing: %s\n", value);
            e.printStackTrace();
        }

        return wrapped;
    }

    /**
     * Evaluate the script the first time.
     *
     * @param script
     * @param method Which method to execute after eval
     * @param namedParams   JSON encoded values
     * @return
     */
    public EvaluatedScript evalScript(String script, String method, Map<String, Object> namedParams) {
        long t0 = System.currentTimeMillis();

        if (sandboxed && !ContextFactory.hasExplicitGlobal()) {
            org.mozilla.javascript.ContextFactory.GlobalSetter gsetter = ContextFactory.getGlobalSetter();

            if (gsetter != null) {
                gsetter.setContextFactoryGlobal(new SandboxContextFactory());
            }
        }

        if (DEBUG) printf("evalScript(this: %s, script, sandbox: %b namedParams: %s)\n", this,sandboxed, namedParams);
        Context cx = Context.enter();

        try {
            Context.ClassShutterSetter setter = cx.getClassShutterSetter();
            if (sandboxed && setter != null) {
                setter.setClassShutter(getShutter());
            }

            DebugLogger.clearLog(cx);

            parseScript(script);

            if (result.isSuccess() == false) return result;

            if (namedParams != null) {
                try {
                    mungeParams(namedParams, true);
                } catch (IllegalArgumentException iae) {
                    // TODO: Use INVALID_PARAMETER_VALUE error string and include parameter name and value
                    return new EvaluatedScript(ShapeJSErrors.ErrorType.INVALID_PARAMETER_VALUE, iae.getMessage(), getPrintLogs(cx),System.currentTimeMillis() - t0);
                }

                for (Map.Entry<String, Object> entry : namedParams.entrySet()) {
                    if (defs.get(entry.getKey()) == null) {
                        printf("Undefined parameter %s, ignoring\n", entry.getKey());
                        continue;
                    }

                    if (entry.getValue() == null) {
                        printf("Removing arg: %s\n", entry.getKey());
                        argsMap.remove(entry.getKey());
                    }  else {
                        Object argVal = entry.getValue();
                        if (DEBUG) printf("Adding arg: %s -> %s\n", entry.getKey(), argVal.toString() + " class: " + argVal.getClass());

                        if (argVal instanceof SandboxNativeJavaObject) {
                            SandboxNativeJavaObject wrapper = (SandboxNativeJavaObject) argVal;
                            Object no = wrapper.unwrap();
                            if (no instanceof LocationParameter) {
                                LocationParameter lp = (LocationParameter) no;
                                if (DEBUG) printf("---> param: %s point: %s normal: %s\n",no,lp.getPoint(),lp.getNormal());
                            } else if (no instanceof UserDefinedParameter) {
                                UserDefinedParameter udp = (UserDefinedParameter) no;
                                Map<String, Parameter> udpvals = udp.getValue();
                                NativeObject udpArgs = new NativeObject();
                                printf("---> param: %s vals: %s \n",udp.getName(),udpvals);
                                for (Map.Entry<String, Parameter> vals : udpvals.entrySet()) {
                                    Parameter val = vals.getValue();
                                    udpArgs.defineProperty(val.getName(), val.getValue(), 0);
                                }
                                argVal = udpArgs;
                            } else {
                                if (DEBUG) printf("---> param: %s\n",entry.getKey());
                            }
                        } else {
                            if (argVal instanceof ParameterJSWrapper) {
                                ParameterJSWrapper wrapper = (ParameterJSWrapper) argVal;
                                Parameter p = wrapper.getParameter();
                                if (p instanceof DoubleParameter) {
                                    DoubleParameter dp = (DoubleParameter) p;
                                    argVal = dp.getUnit().getConversionVal(dp.getValue());

                                    if (DEBUG) printf("---> param: %s defValue: %s\n",wrapper.getParameter(),argVal);
                                } else {
                                    if (DEBUG) printf("---> param: %s defValue: %s\n",wrapper.getParameter(),wrapper.getDefaultValue(null));
                                }
                            } else {
                                printf("Unhandled type in executeScript.  key: %s  val: %s\n",entry.getKey(),entry.getValue());
                                continue;
                            }
                        }

                        argsMap.defineProperty(entry.getKey(), argVal, 0);
                    }
                }
            }

            if (method == null) {
                result = new EvaluatedScript(true,script,null,null,null,null,defs,(System.currentTimeMillis() - t0));

                // Get all errors in a string array
                List<JsError> errorList = errors.getErrors();

                if (errorList != null && errorList.size() > 0) {
                    int len = errorList.size();
                    for (int i=0; i<len; i++) {
                        String err_st = errorList.get(i).toString();
                        String remap = errorRemap.get(err_st);
                        if (remap != null) {
                            err_st = remap;
                        }
                        result.addErrorLog(ShapeJSErrors.ErrorType.PARSING_ERROR, err_st);
                    }
                }

                return result;
            }

            Object o = scope.get(method, scope);

            if (o == org.mozilla.javascript.Scriptable.NOT_FOUND) {
                System.out.println("Cannot find function main");
                result = new EvaluatedScript(ShapeJSErrors.ErrorType.MAIN_FUNCTION_NOT_FOUND, System.currentTimeMillis() - t0);
                return result;
            }
            Function main = (Function) o;

            Object[] args = new Object[]{argsMap};
            Object result2 = null;

            try {
                result2 = main.call(cx, scope, scope, args);
            } catch(Exception e) {
                if(DEBUG)printf("Script: %s\n",script);
                e.printStackTrace();
                if (e instanceof EcmaError) {
                    printf("line: %d  col: %d\n",((EcmaError)e).lineNumber(),((EcmaError) e).columnNumber());
                }
                result = new EvaluatedScript(ShapeJSErrors.ErrorType.PARSING_ERROR, addErrorLine(e.getMessage(), this.script, headerLines), getPrintLogs(cx),System.currentTimeMillis() - t0);
                return result;
            }
            if (DEBUG) printf("result of JS evaluation: %s\n", result2);

            if (result2 == null) {
                result = new EvaluatedScript(true,script,null,null,null,null,defs,(System.currentTimeMillis() - t0));
                return result;
            }

            if (result2 instanceof NativeJavaObject) {
                Object no = ((NativeJavaObject) result2).unwrap();

                scene = (Scene) no;

                if (DEBUG) printf("end of runScript() shape: %s\n", scene);

                // Get print logs
                List<String> prints = DebugLogger.getLog(cx);
                DebugLogger.clear(cx);

                String[] print_logs = prints != null ? (String[])prints.toArray(new String[prints.size()]) : null;

                result = new EvaluatedScript(true,script,scene,print_logs,null,null, defs,System.currentTimeMillis() - t0);

                // Get all errors in a string array
                List<JsError> errorList = errors.getErrors();

                if (errorList != null && errorList.size() > 0) {
                    int len = errorList.size();
                    for (int i=0; i<len; i++) {
                        String err_st = errorList.get(i).toString();
                        String remap = errorRemap.get(err_st);
                        if (remap != null) {
                            err_st = remap;
                        }
                        result.addErrorLog(ShapeJSErrors.ErrorType.PARSING_ERROR, err_st);
                    }
                }

                return result;
            }

        } finally {
            Context.exit();
        }

        return null;
    }

    /**
     * Evaluate the script the first time.
     *
     * @param method
     * @param namedParams   JSON encoded values
     * @return
     */
    public EvaluatedScript executeScript(String method, Map<String, Object> namedParams) {
        long t0 = System.currentTimeMillis();

        if (sandboxed && !ContextFactory.hasExplicitGlobal()) {
            org.mozilla.javascript.ContextFactory.GlobalSetter gsetter = ContextFactory.getGlobalSetter();

            if (gsetter != null) {
                gsetter.setContextFactoryGlobal(new SandboxContextFactory());
            }
        }

        if (DEBUG) printf("executeScript(script, sandbox: %b namedParams)\n", sandboxed, namedParams);
        Context cx = Context.enter();

        try {
            Context.ClassShutterSetter setter = cx.getClassShutterSetter();
            if (sandboxed && setter != null) {
                setter.setClassShutter(getShutter());
            }

            DebugLogger.clearLog(cx);

            for (Map.Entry<String, Object> entry : namedParams.entrySet()) {
                if (defs.get(entry.getKey()) == null) {
                    printf("Undefined parameter %s, ignoring\n", entry.getKey());
                    continue;
                }

                if (entry.getValue() == null) {
                    if (DEBUG) printf("Removing arg: %s\n", entry.getKey());
                    argsMap.remove(entry.getKey());
                }  else {
                    Object argVal = entry.getValue();

                    if (argVal instanceof SandboxNativeJavaObject) {
                        SandboxNativeJavaObject wrapper = (SandboxNativeJavaObject) argVal;
                        Object no = wrapper.unwrap();
                        if (no instanceof LocationParameter) {
                            LocationParameter lp = (LocationParameter) no;
                            if (DEBUG) printf("---> param: %s point: %s normal: %s\n",no,lp.getPoint(),lp.getNormal());
                        } else if (no instanceof UserDefinedParameter) {
                            UserDefinedParameter udp = (UserDefinedParameter) no;
                            Map<String, Parameter> udpvals = udp.getValue();
                            NativeObject udpArgs = new NativeObject();
                            if (DEBUG) printf("---> param: %s vals: %s \n",udp.getName(),udpvals);
                            for (Map.Entry<String, Parameter> vals : udpvals.entrySet()) {
                                Parameter val = vals.getValue();
                                udpArgs.defineProperty(val.getName(), val.getValue(), 0);
                            }
                            argVal = udpArgs;
                        } else {
                            if (DEBUG) printf("---> param: %s\n",entry.getKey());
                        }
                    } else {
                        if (argVal instanceof ParameterJSWrapper) {
                            ParameterJSWrapper wrapper = (ParameterJSWrapper) argVal;
                            Parameter p = wrapper.getParameter();
                            if (p instanceof DoubleParameter) {
                                DoubleParameter dp = (DoubleParameter) p;
                                argVal = dp.getUnit().getConversionVal(dp.getValue());
                                if (DEBUG) printf("---> param: %s defValue: %s\n",wrapper.getParameter(),argVal);
                            } else {
                                if (DEBUG) printf("---> param: %s defValue: %s\n",wrapper.getParameter(),wrapper.getDefaultValue(null));
                            }
                        } else {
                            if (DEBUG) printf("Unhandled type in executeScript.  key: %s  val: %s\n",entry.getKey(),entry.getValue());
                            continue;
                        }
                    }

                    argsMap.defineProperty(entry.getKey(), argVal, 0);
                }
            }

            if (method == null) {

                result = new EvaluatedScript(true,script,null,null,null,null,defs,(System.currentTimeMillis() - t0));

                // Get all errors in a string array
                List<JsError> errorList = errors.getErrors();

                if (errorList != null && errorList.size() > 0) {
                    int len = errorList.size();
                    for (int i=0; i<len; i++) {
                        String err_st = errorList.get(i).toString();
                        String remap = errorRemap.get(err_st);
                        if (remap != null) {
                            err_st = remap;
                        }
                        result.addErrorLog(ShapeJSErrors.ErrorType.PARSING_ERROR, err_st);
                    }
                }

                return result;
            }

            Object o = scope.get(method, scope);

            if (o == org.mozilla.javascript.Scriptable.NOT_FOUND) {
                System.out.println("Cannot find function main");
                result = new EvaluatedScript(ShapeJSErrors.ErrorType.MAIN_FUNCTION_NOT_FOUND, System.currentTimeMillis() - t0);
                return result;
            }
            Function main = (Function) o;

            Object[] args = new Object[]{argsMap};
            Object result2 = null;

            try {
                result2 = main.call(cx, scope, scope, args);
            } catch(Exception e) {
                if(DEBUG)printf("Script: %s\n", script);
                e.printStackTrace();
                if (e instanceof EcmaError) {
                    printf("line: %d  col: %d\n",((EcmaError)e).lineNumber(),((EcmaError) e).columnNumber());
                }
                result = new EvaluatedScript(ShapeJSErrors.ErrorType.PARSING_ERROR, addErrorLine(e.getMessage(), this.script, headerLines), getPrintLogs(cx),System.currentTimeMillis() - t0);
                return result;
            }
            if (DEBUG) printf("result of JS evaluation: %s\n", result2);

            if (result2 == null || result2 instanceof Undefined) {
                // This used to be a success, changed to a failure as we require a Scene to be returned
                result = new EvaluatedScript(ShapeJSErrors.ErrorType.EMPTY_SCENE, System.currentTimeMillis() - t0);
                return result;
            }

            if (result2 instanceof NativeJavaObject) {
                Object no = ((NativeJavaObject) result2).unwrap();
                scene = (Scene) no;

                if (DEBUG) printf("end of runScript() shape: %s\n", scene);

                // Get print logs
                List<String> prints = DebugLogger.getLog(cx);
                DebugLogger.clear(cx);

                String[] print_logs = prints != null ? (String[])prints.toArray(new String[prints.size()]) : null;

                result = new EvaluatedScript(true, script,scene,print_logs,null,null, defs,System.currentTimeMillis() - t0);

                // Get all errors in a string array
                List<JsError> errorList = errors.getErrors();

                if (errorList != null && errorList.size() > 0) {
                    int len = errorList.size();
                    for (int i=0; i<len; i++) {
                        String err_st = errorList.get(i).toString();
                        String remap = errorRemap.get(err_st);
                        if (remap != null) {
                            err_st = remap;
                        }
                        result.addErrorLog(ShapeJSErrors.ErrorType.PARSING_ERROR, err_st);
                    }
                }

                return result;
            }

        } finally {
            Context.exit();
        }

        return null;
    }

    private Parameter createParameter(Map no) {
        String name = (String) no.get("name");

        String desc = (String) no.get("desc");
        String type = (String) no.get("type");
        String onChange = (String) no.get("onChange");
        String group = (String) no.get("group");
        String label = (String) no.get("label");
        String hidden = (String) no.get("hidden");


        if (name == null) {
            throw new IllegalArgumentException("Parameter name cannot be null.  desc: " + desc + " type: " + type);
        }

        Parameter defParam = defaultParams.get(name);
        if (defParam != null) return mergeParams(no,defParam);

        Object defaultValue = no.get("defaultVal");
        if (defaultValue != null) defaultProvided.add(name);
        if (onChange == null) onChange = "main";

        String btype = type;

        if (type.endsWith("[]")) {
            btype = type.substring(0,type.length()-2);
        }

        Parameter pdef = types.get(btype);
        ParameterType ptype = null;

        if (pdef != null) {
            if (type.endsWith("[]")) ptype = ParameterType.USERDEFINED_LIST;
            else ptype = ParameterType.USERDEFINED;
        } else {

            if (type.endsWith("[]")) {
                type = type.substring(0, type.length() - 2) + "_LIST";
            }
            try {
                ptype = ParameterType.valueOf(type.toUpperCase());
            } catch(Exception e) {
                throw new IllegalArgumentException("Invalid parameter type: " + type + " for parameter: " + name);
            }
        }

        Parameter pd = null;
        Object val = null;

        for(String pn : errorFields) {
            if (no.get(pn) != null) {
                throw new IllegalArgumentException("Invalid field: " + pn + " in param: " + name);
            }
        }

        try {
            //printf("Creating definition:  %s  type: %s\n",name,type);
            switch(ptype) {
                case DOUBLE:
                    double rangeMin = Double.NEGATIVE_INFINITY;
                    double rangeMax = Double.POSITIVE_INFINITY;
                    double step = 1.0;
                    double def = 0;
                    Unit unit = Unit.NONE;

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
                        if (def < rangeMin) {
                            def = rangeMin;
                        } else if (def > rangeMax) {
                            def = rangeMax;
                        }
                    } else {
                        def = rangeMin;
                    }
                    val = no.get("unit");
                    if (val != null) {
                        try {
                            unit = Unit.valueOf(((String)val).toUpperCase());
                        } catch (Exception e) {
                            // ignore and leave as NONE
                        }
                    }

                    pd = new DoubleParameter(name,desc,def, rangeMin, rangeMax, step, unit);
                    break;
                case INTEGER:
                    int irangeMin = Integer.MIN_VALUE;
                    int irangeMax = Integer.MAX_VALUE;
                    int istep = 1;
                    int idef = 0;

                    val = no.get("rangeMin");
                    if (val != null) {
                        irangeMin = ((Number) val).intValue();
                    }
                    val = no.get("rangeMax");
                    if (val != null) {
                        irangeMax = ((Number) val).intValue();
                    }

                    val = no.get("defaultVal");
                    if (val != null) {
                        idef = ((Number) val).intValue();
                        if (idef < irangeMin) {
                            idef = irangeMin;
                        } else if (idef > irangeMax) {
                            idef = irangeMax;
                        }
                    } else {
                        idef = irangeMin;
                    }

                    pd = new IntParameter(name,desc,idef, irangeMin, irangeMax);
                    break;
                case STRING:
                    pd = new StringParameter(name,desc,(String) defaultValue);
                    break;
                case COLOR:
                    pd = new ColorParameter(name,desc,(String) defaultValue);
                    break;
                case BOOLEAN:
                    boolean bdef = false;
                    if (defaultValue != null) {
                        bdef = (Boolean) defaultValue;
                    }
                    pd = new BooleanParameter(name,desc,bdef);
                    break;
                case ENUM:
                    Object ovalues = no.get("values");
                    String[] values = null;
                    if (ovalues instanceof NativeArray) {
                        NativeArray sna = (NativeArray) ovalues;
                        int slen = sna.size();
                        values = new String[slen];
                        for(int j=0; j < sna.size(); j++) {
                            String st = ((String) sna.get(j));
                            values[j] = st;
                        }
                    } else if (ovalues instanceof String[]) {
                        values = (String[]) ovalues;
                    }
                    if (values == null) {
                        throw new IllegalArgumentException("Error parsing definition.  Enumeration has no valid values: " + name);
                    }

                    pd = new EnumParameter(name,desc,values,(String) defaultValue);

                    // TODO: need to add values for validation
                    break;
                case URI:
                    val = no.get("mimeType");
                    String[] mimes = null;
                    if (val instanceof NativeArray) {
                        NativeArray ula = (NativeArray) val;
                        mimes = new String[ula.size()];
                        for(int j=0; j < ula.size(); j++) {
                            String mime = (String) ula.get(j);
                            mimes[j] = mime;
                        }
                    } else if (val instanceof String) {
                        mimes = new String[] {(String)val};
                    }
                    pd = new URIParameter(name,desc,(String) defaultValue,mimes);

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

                    // TODO: Not sure about using DoubleParameter inside the list, why not Double.
                    if (val != null) {
                        if (val instanceof Number) {
                            dlDef = ((Number) val).doubleValue();
                            if (dlDef < dlRangeMin) {
                                dlDef = dlRangeMin;
                            } else if (dlDef > dlRangeMax) {
                                dlDef = dlRangeMax;
                            }
                            dll.add(new DoubleParameter(name,desc,dlDef,dlRangeMin,dlRangeMax,dlStep));
                        } else if (val instanceof NativeArray) {
                            NativeArray dla = (NativeArray) defaultValue;
                            for(int j=0; j < dla.size(); j++) {
                                double dlaDef = ((Number)dla.get(j)).doubleValue();
                                if (dlaDef < dlRangeMin) {
                                    dlaDef = dlRangeMin;
                                } else if (dlDef > dlRangeMax) {
                                    dlaDef = dlRangeMax;
                                }
                                dll.add(new DoubleParameter(name,desc,dlaDef,dlRangeMin,dlRangeMax,dlStep));
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
                    Vector3d minp = null;
                    Vector3d maxp = null;
                    double[] point = null;
                    double[] normal = null;
                    dArray1[0] = 0;
                    dArray1[1] = 0;
                    dArray1[2] = 0;
                    dArray3[0] = -10000;
                    dArray3[1] = -10000;
                    dArray3[2] = -10000;
                    dArray4[0] = 10000;
                    dArray4[1] = 10000;
                    dArray4[2] = 10000;

                    if (defmap != null) {
                        Object o = defmap.get("point");
                        if (o != null) {
                            mungeToDoubleArray(o, dArray1);
                            point = dArray1;
                            p = new Vector3d(point);
                        }
                        o = defmap.get("normal");
                        if (o != null) {
                            mungeToDoubleArray(o, dArray2);
                            normal = dArray2;
                            n = new Vector3d(normal);
                        }
                    }

                    val = no.get("pointMin");
                    if (val != null) {
                        mungeToDoubleArray(val, dArray3);
                    }

                    val = no.get("pointMax");
                    if (val != null) {
                        mungeToDoubleArray(val, dArray4);
                    }

                    minp = new Vector3d(dArray3);
                    maxp = new Vector3d(dArray4);

                    // Validate default point against min and max allowed
                    if (p != null) {
                        if (p.x < minp.x || p.y < minp.y || p.z < minp.z) {
                            p.x = minp.x;
                            p.y = minp.y;
                            p.z = minp.z;
                        } else if (p.x > maxp.x || p.y > maxp.y || p.z > maxp.z) {
                            p.x = maxp.x;
                            p.y = maxp.y;
                            p.z = maxp.z;
                        }
                    }

                    pd = new LocationParameter(name,desc,p,n,minp,maxp);
                    break;
                case AXIS_ANGLE_4D:
                    AxisAngle4d aa = new AxisAngle4d();
                    if (defaultValue != null) {
                        if (defaultValue instanceof NativeArray) {
                            NativeArray dla = (NativeArray) defaultValue;
                            int alen = dla.size();
                            if (alen != 4) throw new IllegalArgumentException("Invalid Axis Angle: " + defaultValue + " for: " + name);
                            aa.x = ((Number)dla.get(0)).doubleValue();
                            aa.y = ((Number)dla.get(1)).doubleValue();
                            aa.z = ((Number)dla.get(2)).doubleValue();
                            aa.angle = ((Number)dla.get(3)).doubleValue();
                        } else {
                            throw new IllegalArgumentException("Invalid Axis Angle: " + defaultValue);
                        }
                    }

                    pd = new AxisAngle4dParameter(name,desc,aa);
                    break;
                case USERDEFINED:
                    if (types.get(type) != null) {
                        pd = ((UserDefinedParameter) types.get(type)).clone();
                        pd.setName(name);
                        defaultProvided.add(name);
                    } else {
                        pd = new UserDefinedParameter(name,desc);
                        val = no.get("properties");

                        if (val != null) {
                            if (val instanceof NativeArray) {
                                NativeArray udna = (NativeArray) val;
                                int udnalen = udna.size();
                                for(int j=0; j < udnalen; j++) {
                                    Map prop = (Map) udna.get(j);
                                    Parameter udp = createParameter(prop);
                                    ((UserDefinedParameter)pd).addProperty(udp.getName(),udp);
                                }

                                if (pd.getDefaultValue() != null) defaultProvided.add(name);
                            }
                        }
                    }

                    break;
                default:
                    throw new IllegalArgumentException("Error parsing definition.  Unhandled parameter type: " + ptype + " for parameter: " + name);
            }
        } catch (ClassCastException cce) {
            cce.printStackTrace();
            throw new ClassCastException("Error parsing definition for parameter: " + name + ".\n" + cce.getMessage());
        }

        if (hidden != null) {
            pd.setHidden(Boolean.parseBoolean(hidden));
        }

        pd.setOnChange(onChange);
        if (label != null) pd.setLabel(label);
        if (group != null) pd.setGroup(group);
        if (DEBUG) printf("Creating pd: name: %s desc: %s type: %s defVal: %s onChange: %s\n",name,desc,type,defaultValue,onChange);

        return pd;
    }

    /**
     * Parse the definition section of the script.
     *
     * @param params
     */
    private LinkedHashMap<String, Parameter> parseDefinition(Object params, boolean clear) {
        LinkedHashMap<String, Parameter> ret_val = new LinkedHashMap<String, Parameter>();

        if (clear) {
            types.clear();
            defs.clear();
            defaultProvided.clear();
        }

        if (params == null || !(params instanceof NativeArray)) {
            addDefaultParams(null,ret_val);

            return ret_val;
        }

        NativeArray arr = (NativeArray) params;
        int len = (int) arr.getLength();
        if (DEBUG) printf("Params length: %d\n",len);

        for(int i=0; i < len; i++) {
            Object po = arr.get(i);
            NativeObject no = (NativeObject) po;

            Parameter pd = createParameter(no);

            ret_val.put(pd.getName(),pd);
        }


        addDefaultParams(arr,ret_val);

        return ret_val;
    }

    private void addDefaultParams(NativeArray params, LinkedHashMap<String, Parameter> ret_val) {
        int len;

        if (params == null) len = 0;
        else len = (int) params.getLength();

        for(Map.Entry<String,Parameter> entry : defaultParams.entrySet()) {
            boolean found = false;
            for(int i=0; i < len; i++) {
                Object po = params.get(i);
                NativeObject no = (NativeObject) po;

                String name = (String) no.get("name");
                if (name != null && entry.getKey().equals(name)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                HashMap<String,String> map = new HashMap<String, String>(1);
                map.put("name",entry.getKey());
                Parameter pd = createParameter(map);
                ret_val.put(pd.getName(),pd);
            }
        }
    }

    /**
     * Merge two parameter definitions.  Only support enums currently
     * @param fp The most important definition
     * @param sp The second most important
     */
    private Parameter mergeParams(Map fp, Parameter sp) {
        EnumParameter sep = (EnumParameter) sp;

        String name = (String) fp.get("name");
        String label = (String) fp.get("label");
        String desc = (String) fp.get("desc");
        String hidden = (String) fp.get("hidden");

        Object vals = fp.get("values");
        String[] values = null;
        if (vals instanceof NativeArray) {
            NativeArray nav = (NativeArray) vals;
            values = new String[nav.size()];
            for(int j=0; j < nav.size(); j++) {
                values[j] = (String)nav.get(j);
            }
        } else {
            values = (String[]) fp.get("values");
        }
        String defaultValue = (String) fp.get("defaultVal");
        String onChange = (String) fp.get("onChange");
        String group = (String) fp.get("group");

        if (name == null) name = sep.getName();
        if (label == null) label = sep.getLabel();
        if (desc == null) desc = sep.getDesc();
        if (values == null) values = sep.getValues();
        if (defaultValue == null) defaultValue = (String) sep.getDefaultValue();
        if (onChange == null) onChange = (String) sep.getOnChange();
        if (group == null) group = (String) sep.getGroup();
        if (hidden == null) hidden = (String) (sep.isHidden() ? "true" : "false");

        EnumParameter ret_val = new EnumParameter(name,desc,values,defaultValue);
        ret_val.setLabel(label);
        ret_val.setOnChange(onChange);
        ret_val.setGroup(group);
        ret_val.setHidden(Boolean.parseBoolean(hidden));

        return ret_val;
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
        if (DEBUG) printf("Add error line: %s header: %d\n",msg,header);
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