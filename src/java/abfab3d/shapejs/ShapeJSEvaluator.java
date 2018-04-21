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

import abfab3d.core.Location;
import abfab3d.core.Material;
import abfab3d.param.*;
import abfab3d.util.Unit;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.mozilla.javascript.*;
import org.mozilla.javascript.commonjs.module.ModuleScope;
import org.mozilla.javascript.tools.ToolErrorReporter;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Vector3d;
import java.io.File;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;

/**
 * Evaluate a ShapeJS script to get its graph.
 * <p>
 * General security mechanism is a classes package must be listed in the packageWhitelist.
 * If not, then it must be in classWhitelist otherwise its not allowed.
 *
 * @author Alan Hudson
 */
public class ShapeJSEvaluator implements MaterialMapper {

    final static boolean DEBUG = false;
    final static boolean DEBUG_SECURITY = false;

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

    //private GlobalScope scope;
    private TopLevel scope;
    private ErrorReporterWrapper errors;
    private LinkedHashMap<String, Parameter> types;
    private LinkedHashMap<String, Parameter> defs;

    /** Parameters as Javascript Objects */
    private LinkedHashMap<String, Scriptable> jsObjects = new LinkedHashMap<>();

    private Scene scene;
    private String basedir; // The base directory of the loaded script

    /** Should we run this in a sandbox, default it true */
    private boolean sandboxed;

    private static Type stringListType = new TypeToken<List<String>>() {
    }.getType();
    private static Type doubleListType = new TypeToken<List<Double>>() {
    }.getType();
    private static Type axisAngle4DType = new TypeToken<AxisAngle4d>() {
    }.getType();

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
    private static LinkedHashMap<String, Material> materials = new LinkedHashMap<String, Material>();


    static {

        restrictedPackages = new HashSet();
        restrictedPackages.add("java.lang");

        errorFields = new HashSet<String>();
        errorFields.add("defaultValue");
        errorFields.add("units");

        errorRemap = new HashMap<String, String>();
        errorRemap.put("Wrapped abfab3d.grid.util.ExecutionStoppedException", "Execution time exceeded.");

        materials = new LinkedHashMap<String, Material>();
        materials.put(SingleColorMaterial.getInstance().getName(), SingleColorMaterial.getInstance());
        materials.put(FullColorMaterial.getInstance().getName(), FullColorMaterial.getInstance());
        Materials.add(SingleColorMaterial.getInstance().getName(), SingleColorMaterial.getInstance());
        Materials.add(FullColorMaterial.getInstance().getName(), FullColorMaterial.getInstance());

        setupSecurity();
    }

    public ShapeJSEvaluator() {
        this.sandboxed = true;
        types = new LinkedHashMap<String, Parameter>();
        defs = new LinkedHashMap<String, Parameter>();

        initHeader();
    }

    public ShapeJSEvaluator(boolean sandboxed) {
        new Exception().printStackTrace();
        this.sandboxed = sandboxed;
        types = new LinkedHashMap<String, Parameter>();
        defs = new LinkedHashMap<String, Parameter>();

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
        classImports.add("abfab3d.io.input.AttributedMeshSourceWrapper");
        classImports.add("abfab3d.io.input.ModelLoader");
        classImports.add("abfab3d.io.input.MeshBuilder");

        classImports.add("abfab3d.util.MeshRasterizer");
        classImports.add("abfab3d.util.Polygon");

        classImports.add("abfab3d.shapejs.Scene");
        classImports.add("abfab3d.shapejs.Light");
        classImports.add("abfab3d.shapejs.Viewpoint");
        classImports.add("abfab3d.shapejs.Background");
        classImports.add("abfab3d.shapejs.Materials");
        classImports.add("abfab3d.shapejs.SingleColorMaterial");
        classImports.add("abfab3d.shapejs.FullColorMaterial");
        classImports.add("abfab3d.shapejs.TracingParams");

        classImports.add("abfab3d.grid.op.ImageLoader");

        classImports.add("abfab3d.core.MathUtil");
        classImports.add("abfab3d.core.Color");
        classImports.add("abfab3d.core.Bounds");
        classImports.add("abfab3d.core.Vec");
        classImports.add("abfab3d.core.Location");
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
        classWhiteList.add("java.awt.geom.Point2D$Double");
        classWhiteList.add("sun.java2d.SunGraphics2D");  // Needed for image creation

        printf("Added Point2D\n");

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
        for (String entry : pwl) {
            if (restrictedPackages.contains(entry)) {
                printf("SECURITY: Attempt to add restricted package: %s\n", entry);
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
        if (DEBUG) printf("Clearing defs\n");
        result = null;
        types.clear();
        defs.clear();
        scope = null;
        scene = null;
    }

    /**
     * Parses the script.  Stores its parameter definitions and converts param values into Javascript objects
     *
     * @param script The script value
     * @param params The Parameter level object values
     * @return
     */
    public void prepareScript(String script, Map<String, Object> params) {
        long t0 = time();

        if (sandboxed && !ContextFactory.hasExplicitGlobal()) {
            org.mozilla.javascript.ContextFactory.GlobalSetter gsetter = ContextFactory.getGlobalSetter();

            if (gsetter != null) {

                if (DEBUG_SECURITY) printf("Adding SandboxContextFactory\n");
                gsetter.setContextFactoryGlobal(new SandboxContextFactory());
            }
        } else if (DEBUG_SECURITY) {
            printf("Explicit global: %b\n", ContextFactory.hasExplicitGlobal());
        }

        if (DEBUG) printf("prepareScript(this: %s script, sandbox: %b)\n", this, sandboxed);
        Context cx = Context.enter();
        Object scene = null;

        /*
        if (script != null && this.script != null) {
            // reset scope to clear state for new script
            resetParams();
            clear();
        }
        */
        try {
            Context.ClassShutterSetter setter = cx.getClassShutterSetter();
            DebugLogger.clearLog(cx);

            if (sandboxed && setter != null) {
                setter.setClassShutter(getShutter());
            }

            // Use a new scope on each script change
            if (script != null) {
                long t1 = time();
                ContextFactory contextFactory = null;
                contextFactory = new ContextFactory();

                ToolErrorReporter errorReporter = new ToolErrorReporter(false, System.err);
                errors = new ErrorReporterWrapper(errorReporter);
                contextFactory.setErrorReporter(errors);

                GlobalScope gs = new GlobalScope();
                gs.initShapeJS(contextFactory,basedir);

                URI uri = null;
                if (basedir != null) {
                    uri = new File(basedir).toURI();
                }

                scope = new ModuleScope(gs, uri, null);
            }

            if (script == null && this.script == null) {
                throw new IllegalArgumentException("Script must be set before using null script");
            }

            // Only parse the script if a new version was passed in
            if (script != null) {
                if (DEBUG) printf("Parsing new script\n");
                this.script = addImports(script);


                //printf("Final script:\n%s\n",script);
                try {
                    scene = cx.evaluateString(scope, this.script, "<cmd>", 1, null);
                } catch (Exception e) {
                    printf("evaluateString() failed: %s\n", e.getMessage());
                    if (false) e.printStackTrace(System.out);
                    if (DEBUG) printf("Script failed: %s\nScript:\n%s", e.getMessage(), this.script);
                    String msg = e.getMessage();
                    if (msg == null) {
                        printf("Null error message.  Orig execption: \n");
                        e.printStackTrace();
                    }
                    result = new EvaluatedScript(ShapeJSErrors.ErrorType.PARSING_ERROR, addErrorLine(e.getMessage(), this.script, headerLines), getPrintLogs(cx), time() - t0);
                    return;
                }

                // Set parameter types
                try {
                    Object ptypes = scope.get("types", scope);
                    types = parseDefinition(ptypes, true);
                } catch (ClassCastException cce) {
                    result = new EvaluatedScript(ShapeJSErrors.ErrorType.INVALID_PARAMETER_VALUE, cce.getMessage(), getPrintLogs(cx), time() - t0);
                    return;
                }

                // Set parameter definitions
                try {
                    Object uiParams = scope.get("params", scope);
                    if (uiParams == null || uiParams == UniqueTag.NOT_FOUND) uiParams = scope.get("uiParams", scope);

                    LinkedHashMap<String, Parameter> newDefs = parseDefinition(uiParams, false);

                    if (DEBUG) printf("Handling new param defs.  this: %s current defs: %s\n", this, defs);
                    // Retain values
                    HashMap<String, Parameter> toRemove = new HashMap<>(defs);
                    for (Parameter p : newDefs.values()) {
                        toRemove.remove(p.getName());

                        if (defs.containsKey(p.getName())) {
                            // already exists, retain value if available
                            Parameter old = defs.get(p.getName());
                            if (DEBUG)
                                printf("Defs already exists: %s  keeping value: %s\n", p.getName(), old.getValue());
                            p.setValue(old.getValue());
                        }

                        newDefs.put(p.getName(), p);
                    }

                    for (Parameter p : toRemove.values()) {
                        if (DEBUG) printf("Clearing def: %s\n", p.getName());
                        defs.remove(p.getName());
                    }

                    defs = newDefs;
                    if (DEBUG) {
                        printf("New Defs: \n");
                        printf("%s\n", defs);
                    }
                } catch (ClassCastException cce) {
                    result = new EvaluatedScript(ShapeJSErrors.ErrorType.INVALID_PARAMETER_VALUE, cce.getMessage(), getPrintLogs(cx), time() - t0);
                    return;
                }
            }

            updateParamMap(params);
        } finally {
            Context.exit();
        }

        if (DEBUG) printf("Eval worked.  defs: %s\n", defs);

        result = new EvaluatedScript(true, script, null, null, null, null, defs, (time() - t0));
    }

    /**
     * Updates a scripts parameter values.
     *
     * @param params The Parameter level object values
     * @return
     */
    public void updateParams(Map<String, Object> params) {
        long t0 = time();

        if (DEBUG) printf("updateScript(this: %s script, sandbox: %b)\n", this, sandboxed);
        Context cx = Context.enter();

        try {
            Context.ClassShutterSetter setter = cx.getClassShutterSetter();
            DebugLogger.clearLog(cx);

            if (sandboxed && setter != null) {
                setter.setClassShutter(getShutter());
            }
            if (scope == null) {
                throw new IllegalArgumentException("Cannot update params, call prepareScript first");
            }

            updateParamMap(params);
        } finally {
            Context.exit();
        }

        result = new EvaluatedScript(true, script, null, null, null, null, defs, (time() - t0));
    }

    /**
     * Reset parameter values back to their default state
     */
    public void resetParams() {
        Context cx = Context.enter();

        if (DEBUG) printf("*** ShapeJSEval. Resetting params ***\n");
        try {
            Context.ClassShutterSetter setter = cx.getClassShutterSetter();
            DebugLogger.clearLog(cx);

            if (sandboxed && setter != null) {
                setter.setClassShutter(getShutter());
            }
            if (scope == null) {
                throw new IllegalArgumentException("Cannot update params, call prepareScript first");
            }

            // Reset params to their defaultValue
            for (Map.Entry<String, Parameter> entry : defs.entrySet()) {
                String key = entry.getKey();
                Parameter param = entry.getValue();

                param.setValue(param.getDefaultValue());
            }
        } finally {
            Context.exit();
        }
    }


    /**
     * Execute the script
     *
     * @param method The method to execute or null to not execute any specific function
     * @return
     */
    public EvaluatedScript executeScript(String method) {
        if (DEBUG) printf("ShapeJSEvaluator.executeScript()\n");
        long t0 = time();

        if (sandboxed && !ContextFactory.hasExplicitGlobal()) {
            org.mozilla.javascript.ContextFactory.GlobalSetter gsetter = ContextFactory.getGlobalSetter();

            if (gsetter != null) {
                gsetter.setContextFactoryGlobal(new SandboxContextFactory());
            }
        }

        if (DEBUG) printf("executeScript(script, sandbox: %b)\n", sandboxed);
        Context cx = Context.enter();

        try {
            Context.ClassShutterSetter setter = cx.getClassShutterSetter();
            if (sandboxed && setter != null) {
                setter.setClassShutter(getShutter());
            }

            DebugLogger.clearLog(cx);

            if (method == null) {

                result = new EvaluatedScript(true, script, null, null, null, null, defs, (time() - t0));

                // Get all errors in a string array
                addErrors(errors, result);

                return result;
            }

            Object o = scope.get(method, scope);

            if (o == org.mozilla.javascript.Scriptable.NOT_FOUND) {
                printf("Cannot find function main()\n");
                result = new EvaluatedScript(ShapeJSErrors.ErrorType.MAIN_FUNCTION_NOT_FOUND, time() - t0);
                return result;
            }
            Function main = (Function) o;

            // Create args map from current parameters.  Ideally we'd be able to cache these but changed to easier logic to debug for now
            NativeObject argsMap = createArgsMap();
            if (DEBUG) printArgsMap(argsMap);

            Object[] args = new Object[]{argsMap};
            Object result2 = null;

            try {
                result2 = main.call(cx, scope, scope, args);
            } catch (Exception e) {
                if (DEBUG) printf("Script: %s\n", script);
                e.printStackTrace();
                if (e instanceof EcmaError) {
                    printf("line: %d  col: %d\n", ((EcmaError) e).lineNumber(), ((EcmaError) e).columnNumber());
                }
                String msg;

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

                result = new EvaluatedScript(ShapeJSErrors.ErrorType.PARSING_ERROR, addErrorLine(msg, this.script, headerLines), getPrintLogs(cx), time() - t0);
                return result;
            }
            if (DEBUG) printf("result of JS evaluation: %s\n", result2);

            if (result2 == null || result2 instanceof Undefined) {
                // This used to be a success, changed to a failure as we require a Scene to be returned
                result = new EvaluatedScript(ShapeJSErrors.ErrorType.EMPTY_SCENE, time() - t0);
                return result;
            }

            if (result2 instanceof NativeJavaObject) {
                Object no = ((NativeJavaObject) result2).unwrap();
                scene = (Scene) no;

                if (DEBUG) printf("end of runScript() shape: %s\n", scene);

                // Get print logs
                List<String> prints = DebugLogger.getLog(cx);
                DebugLogger.clear(cx);

                String[] print_logs = prints != null ? (String[]) prints.toArray(new String[prints.size()]) : null;

                result = new EvaluatedScript(true, script, scene, print_logs, null, null, defs, time() - t0);

                // Get all errors in a string array
                addErrors(errors, result);

                return result;
            }

        } finally {
            Context.exit();
        }

        return null;
    }

    /**
     * Updates the parameter map with new values
     *
     * @param newParams The new parameter values.  use NULL to indicate parameter value removal
     */
    private void updateParamMap(Map<String, Object> newParams) {

        if (newParams == null) return;

        for (Map.Entry<String, Object> entry : newParams.entrySet()) {
            String key = entry.getKey();
            Parameter param = defs.get(key);

            if (param == null) {
                printf("Ignoring unknown param: %s\n", key);
                continue;
            }

            if (entry.getValue() == null) {
                if (DEBUG) printf("Removing arg: %s\n", entry.getKey());
                param.setValue(param.getDefaultValue());
            } else {
                param.setValue(entry.getValue());
            }
        }
    }

    /**
     * Create Javascript objects for all defined params
     */
    private NativeObject createArgsMap() {
        NativeObject argsMap = new NativeObject();

        for (Parameter p : defs.values()) {
            Object jo = convParameterToJSObject(p);

            if (jo != null) {
                if (DEBUG) printf("Adding arg: %s -> %s\n", p.getName(), jo);
                argsMap.defineProperty(p.getName(), jo, 0);
            }
        }

        return argsMap;
    }

    /**
     * Convert a Parameter into a Javascript Native Object
     *
     * @param param
     * @return The object to use at the Javascript level
     */
    private Object convParameterToJSObject(Parameter param) {
        Object jsVal = null;

        switch (param.getType()) {
            case DOUBLE:
                DoubleParameter dp = (DoubleParameter) param;
                jsVal = dp.getUnit().getConversionVal(dp.getValue());
                break;
            case DOUBLE_LIST:
                // TODO: This is rather ineffecient for large values
                // Why is this different then the other list handling?   Conversion factory basically
                DoubleListParameter dlp = (DoubleListParameter) param;
                Unit unit = ((DoubleParameter) dlp.getDefinition()).getUnit();
                List<Parameter> raw = dlp.getValue();
                ArrayList<Parameter> dlist = new ArrayList<Parameter>(raw.size());
                for (Parameter val : raw) {
                    DoubleParameter dp2 = new DoubleParameter(dlp.getName(), "");
                    dp2.setValue(unit.getConversionVal(((DoubleParameter) val).getValue()));
                    dlist.add(dp2);
                }
                jsVal = dlist;
                break;
            // Simple types, return primitive
            case DATE_TIME:
            case URI:
            case FLOAT:
            case BYTE:
            case SHORT:
            case ENUM:
            case LONG:
            case INTEGER:
            case STRING:
            case BOOLEAN:
                jsVal = param.getValue();
                break;
            case BOOLEAN_LIST:
            case BYTE_LIST:
            case SHORT_LIST:
            case INTEGER_LIST:
            case LONG_LIST:
            case URI_LIST:
            case STRING_LIST:
            case DATE_TIME_LIST:
                jsVal = param.getValue();
                break;
            case USERDEFINED:
                if (param.getValue() == null) return null;
                UserDefinedParameter udp = (UserDefinedParameter) param;

                NativeObject result = new NativeObject();
                Map<String,Parameter> props = udp.getProperties();
                for(Map.Entry<String,Parameter> p : props.entrySet()) {
                    result.defineProperty(p.getKey(),convParameterToJSObject(p.getValue()),0);
                }
                jsVal = result;
                break;
            case USERDEFINED_LIST:
                throw new IllegalArgumentException("Unhandled case");
            default:
                // For complex items wrap them in Scriptables and return
                if (param.getValue() == null) return null;

                jsVal = Context.javaToJS(param.getValue(), scope);
                break;
        }

        return jsVal;
    }

    private ClassShutter getShutter() {
        if (shutter == null) {
            shutter = new ClassShutter() {
                public boolean visibleToScripts(String className) {
                    if (DEBUG_SECURITY) printf("Checking class: %s\n", className);
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

                    if (DEBUG_SECURITY) printf("Rejecting class: %s\n", className);
                    return false;
                }
            };
        }

        return shutter;
    }

    /**
     * Get the parameter type for a param.  The current script must be parsed first.
     *
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
     * Get the parameter type and value for a param.  The current script must be parsed first.
     *
     * @param param
     * @return The type or null if not found
     */
    public Parameter getParameter(String param) {

        return defs.get(param);
    }

    public Map<String, Parameter> getParams() {
        return defs;
    }

    public Map<String, Parameter> getTypes() {
        return types;
    }

    /**
     * Returns the result of executing this script.  A null result means no errors yet.
     *
     * @return
     */
    public EvaluatedScript getResult() {
        return result;
    }


    public void setBaseDir(String dir) {
        this.basedir = dir;
    }

    public String getBaseDir() {
        return basedir;
    }

    private String[] getPrintLogs(Context cx) {
        List<String> prints = DebugLogger.getLog(cx);
        DebugLogger.clear(cx);

        String[] print_logs = prints != null ? (String[]) prints.toArray(new String[prints.size()]) : null;

        return print_logs;
    }


    private void addErrors(ErrorReporterWrapper errors, EvaluatedScript result) {
        // Get all errors in a string array
        List<JsError> errorList = errors.getErrors();

        if (errorList != null && errorList.size() > 0) {
            int len = errorList.size();
            for (int i = 0; i < len; i++) {
                String err_st = errorList.get(i).toString();
                String remap = errorRemap.get(err_st);
                if (remap != null) {
                    err_st = remap;
                }
                result.addErrorLog(ShapeJSErrors.ErrorType.PARSING_ERROR, err_st);
            }
        }

    }

    /**
     * Create a parameter from its Javascript definition.  The defaultVal is a JSON encoded value that may allow more human typeable definitions
     *
     * @param no
     * @return
     */
    private Parameter createParameter(Map no) {
        String name = (String) no.get("name");

        String desc = (String) no.get("desc");
        String type = (String) no.get("type");
        String onChange = (String) no.get("onChange");
        String group = (String) no.get("group");
        String label = (String) no.get("label");
        Boolean visible = (Boolean) no.get("visible");
        Boolean enabled = (Boolean) no.get("enabled");
        Map editor = (Map) no.get("editor");


        if (name == null) {
            throw new IllegalArgumentException("Parameter name cannot be null.  desc: " + desc + " type: " + type);
        }

        Object defaultValue = no.get("defaultVal");
        if (onChange == null) onChange = "main";

        String btype = type;

        if (type.endsWith("[]")) {
            btype = type.substring(0, type.length() - 2);
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
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid parameter type: " + type + " for parameter: " + name);
            }
        }

        Parameter pd = null;
        Object val = null;

        for (String pn : errorFields) {
            if (no.get(pn) != null) {
                throw new IllegalArgumentException("Invalid field: " + pn + " in param: " + name);
            }
        }

        try {
            //printf("Creating definition:  %s  type: %s\n",name,type);
            switch (ptype) {
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
                            unit = Unit.valueOf(((String) val).toUpperCase());
                        } catch (Exception e) {
                            // ignore and leave as NONE
                        }
                    }

                    pd = new DoubleParameter(name, desc, def, rangeMin, rangeMax, step, unit);
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

                    pd = new IntParameter(name, desc, idef, irangeMin, irangeMax);
                    break;
                case STRING:
                    pd = new StringParameter(name, desc, (String) defaultValue);
                    break;
                case COLOR:
                    pd = new ColorParameter(name, desc, (String) defaultValue);
                    break;
                case BOOLEAN:
                    boolean bdef = false;
                    if (defaultValue != null) {
                        bdef = (Boolean) defaultValue;
                    }
                    pd = new BooleanParameter(name, desc, bdef);
                    break;
                case ENUM:
                    Object ovalues = no.get("values");
                    String[] values = null;
                    if (ovalues instanceof NativeArray) {
                        NativeArray sna = (NativeArray) ovalues;
                        int slen = sna.size();
                        values = new String[slen];
                        for (int j = 0; j < sna.size(); j++) {
                            String st = ((String) sna.get(j));
                            values[j] = st;
                        }
                    } else if (ovalues instanceof String[]) {
                        values = (String[]) ovalues;
                    }
                    if (values == null) {
                        throw new IllegalArgumentException("Error parsing definition.  Enumeration has no valid values: " + name);
                    }

                    Object lvalues = no.get("labels");
                    String[] labels = null;
                    if (lvalues instanceof NativeArray) {
                        NativeArray sna = (NativeArray) lvalues;
                        int slen = sna.size();
                        labels = new String[slen];
                        for (int j = 0; j < sna.size(); j++) {
                            String st = ((String) sna.get(j));
                            labels[j] = st;
                        }
                    } else if (lvalues instanceof String[]) {
                        labels = (String[]) lvalues;
                    }

                    pd = new EnumParameter(name, desc, values, labels,(String) defaultValue);

                    // TODO: need to add values for validation
                    break;
                case URI:
                    val = no.get("mimeType");
                    String[] mimes = null;
                    if (val instanceof NativeArray) {
                        NativeArray ula = (NativeArray) val;
                        mimes = new String[ula.size()];
                        for (int j = 0; j < ula.size(); j++) {
                            String mime = (String) ula.get(j);
                            mimes[j] = mime;
                        }
                    } else if (val instanceof String) {
                        mimes = new String[]{(String) val};
                    }
                    pd = new URIParameter(name, desc, (String) defaultValue, mimes);

                    break;
                case URI_LIST:
                    NativeArray ula = (NativeArray) defaultValue;
                    ArrayList<URIParameter> ul = new ArrayList<URIParameter>();
                    for (int j = 0; j < ula.size(); j++) {
                        ul.add(new URIParameter(name, desc, (String) ula.get(j)));
                    }
                    pd = new URIListParameter(name, desc, ul);
                    break;
                case STRING_LIST:
                    NativeArray sla = (NativeArray) defaultValue;
                    ArrayList<StringParameter> sl = new ArrayList<StringParameter>();
                    for (int j = 0; j < sla.size(); j++) {
                        sl.add(new StringParameter(name, desc, (String) sla.get(j)));
                    }
                    pd = new StringListParameter(name, desc, sl);
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
                            dll.add(new DoubleParameter(name, desc, dlDef, dlRangeMin, dlRangeMax, dlStep));
                        } else if (val instanceof NativeArray) {
                            NativeArray dla = (NativeArray) defaultValue;
                            for (int j = 0; j < dla.size(); j++) {
                                double dlaDef = ((Number) dla.get(j)).doubleValue();
                                if (dlaDef < dlRangeMin) {
                                    dlaDef = dlRangeMin;
                                } else if (dlDef > dlRangeMax) {
                                    dlaDef = dlRangeMax;
                                }
                                dll.add(new DoubleParameter(name, desc, dlaDef, dlRangeMin, dlRangeMax, dlStep));
                            }

                        }
                    }

                    pd = new DoubleListParameter(name, desc, dll, dlRangeMin, dlRangeMax, dlStep);
                    break;

                case LOCATION:
                    // TODO: garbage

                    // default should be a map of point and normal
                    Map<String, Object> defmap = (Map<String, Object>) defaultValue;
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

                    Location loc = null;
                    if (p != null || n != null) {
                        loc = new Location(p, n);
                    }
                    pd = new LocationParameter(name, desc, loc, minp, maxp);
                    break;
                case AXIS_ANGLE_4D:
                    AxisAngle4d aa = null;
                    if (defaultValue != null) {
                        if (defaultValue instanceof NativeArray) {
                            NativeArray dla = (NativeArray) defaultValue;
                            int alen = dla.size();
                            if (alen != 4)
                                throw new IllegalArgumentException("Invalid Axis Angle: " + defaultValue + " for: " + name);

                            aa = new AxisAngle4d();
                            aa.x = ((Number) dla.get(0)).doubleValue();
                            aa.y = ((Number) dla.get(1)).doubleValue();
                            aa.z = ((Number) dla.get(2)).doubleValue();
                            aa.angle = ((Number) dla.get(3)).doubleValue();
                        } else {
                            throw new IllegalArgumentException("Invalid Axis Angle: " + defaultValue);
                        }
                    }

                    pd = new AxisAngle4dParameter(name, desc, aa);
                    break;
                case USERDEFINED:
                    if (types.get(type) != null) {
                        pd = ((UserDefinedParameter) types.get(type)).clone();
                        pd.setName(name);

                        if (defaultValue != null) {
                            // TODO: Handle only single level of params right now
                            NativeObject uddv = (NativeObject) defaultValue;
                            HashMap<String,Object> result = new HashMap<>();
                            for(Map.Entry entry : uddv.entrySet()) {
                                result.put((String)entry.getKey(),entry.getValue());
                            }
                            pd.setValue(result);
                        }
                    } else {
                        pd = new UserDefinedParameter(name, desc);
                        val = no.get("properties");

                        if (val != null) {
                            if (val instanceof NativeArray) {
                                NativeArray udna = (NativeArray) val;
                                int udnalen = udna.size();
                                for (int j = 0; j < udnalen; j++) {
                                    Map prop = (Map) udna.get(j);
                                    Parameter udp = createParameter(prop);
                                    ((UserDefinedParameter) pd).addProperty(udp.getName(), udp);
                                }
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

        if (enabled != null) pd.setEnabled(enabled);
        if (visible != null) pd.setVisible(visible);
        if (editor != null) pd.setEditor(editor);
        pd.setOnChange(onChange);
        if (label != null) pd.setLabel(label);
        if (group != null) pd.setGroup(group);
        if (DEBUG)
            printf("Creating pd: name: %s desc: %s type: %s defVal: %s onChange: %s\n", name, desc, type, defaultValue, onChange);

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
        }

        if (DEBUG) printf("Parsing defs.  Params: %s\n", params);
        if (params == null || !(params instanceof NativeArray)) {
            return ret_val;
        }

        NativeArray arr = (NativeArray) params;
        int len = (int) arr.getLength();
        if (DEBUG) printf("Params length: %d\n", len);

        for (int i = 0; i < len; i++) {
            Object po = arr.get(i);
            NativeObject no = (NativeObject) po;

            Parameter pd = createParameter(no);

            ret_val.put(pd.getName(), pd);
        }

        return ret_val;
    }

    private void mungeToDoubleArray(Object in, double[] out) {
        if (in == null) return;

        if (in instanceof NativeArray) {
            out[0] = ((Number) ((NativeArray) in).get(0)).doubleValue();
            out[1] = ((Number) ((NativeArray) in).get(1)).doubleValue();
            out[2] = ((Number) ((NativeArray) in).get(2)).doubleValue();
        } else if (in instanceof double[]) {
            out[0] = ((double[]) in)[0];
            out[1] = ((double[]) in)[1];
            out[2] = ((double[]) in)[2];
        } else if (in instanceof int[]) {
            out[0] = ((int[]) in)[0];
            out[1] = ((int[]) in)[1];
            out[2] = ((int[]) in)[2];
        } else if (in instanceof List) {
            List list = (List) in;
            out[0] = ((Number) list.get(0)).doubleValue();
            out[1] = ((Number) list.get(1)).doubleValue();
            out[2] = ((Number) list.get(2)).doubleValue();
        } else {
            throw new IllegalArgumentException("Unhandled type: " + in + " class: " + in.getClass());
        }
    }

    private String addErrorLine(String msg, String script, int header) {
        // line number is <cmd>#23 form
        if (DEBUG) printf("Add error line: %s header: %d\n", msg, header);
        int idx = msg.indexOf("<cmd>#");
        if (idx == -1) {
            return msg;
        }

        String line_st = msg.substring(idx + 6);
        int idx2 = line_st.indexOf(")");
        line_st = line_st.substring(0, idx2);

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

    private void printArgsMap(NativeObject argsMap) {
        printf("Args Map:\n");
        for (Map.Entry<Object, Object> entry : argsMap.entrySet()) {
            printf("%s -> %s\n", entry.getKey(), entry.getValue());
        }
    }
}