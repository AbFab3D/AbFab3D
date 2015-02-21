/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package volumesculptor.shell;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Grid;
import abfab3d.grid.Model;
import abfab3d.grid.ModelWriter;
import abfab3d.io.output.*;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.util.TriangleMesh;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import abfab3d.util.AbFab3DGlobals;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.mozilla.javascript.*;
import org.mozilla.javascript.commonjs.module.ModuleScope;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.tools.SourceReader;
import org.mozilla.javascript.tools.ToolErrorReporter;


import java.io.*;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static abfab3d.util.Output.printf;

/**
 * The shell program.
 * <p/>
 * Can execute scripts interactively or in batch mode at the command line.
 * An example of controlling the JavaScript engine.
 *
 * @author Norris Boyd
 */
public class Main {
    public static ShellContextFactory
            shellContextFactory = new ShellContextFactory();

    public static Global global = new Global();

    static protected ToolErrorReporter errorReporter;
    static protected int exitCode = 0;
    static private final int EXITCODE_RUNTIME_ERROR = 3;
    static private final int EXITCODE_FILE_NOT_FOUND = 4;
    static boolean processStdin = true;
    static List<String> fileList = new ArrayList<String>();
    static Map<String,Object> namedParams = new HashMap<String, Object>();
    static List<String> modulePath;
    static String mainModule;
    static boolean sandboxed = false;
    static boolean useRequire = false;
    static Require require;
    private static volumesculptor.shell.SecurityProxy securityImpl;
    private final static ScriptCache scriptCache = new ScriptCache(32);

    /** Packages allowed to be imported.  Security mechanism */
    private static final ArrayList<String> packageWhitelist;

    /** Default imports to add to scripts */
    private static final ArrayList<String> scriptImports;
    private static final ArrayList<String> classImports;

    /** Remap error messages to something readable */
    private static final HashMap<String,String> errorRemap;

    static {
        global.initQuitAction(new IProxy(IProxy.SYSTEM_EXIT));

        packageWhitelist = new ArrayList();
        packageWhitelist.add("abfab3d.");
        packageWhitelist.add("javax.vecmath");
        packageWhitelist.add("java.lang");
        packageWhitelist.add("app.common");

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

        errorRemap = new HashMap<String,String>();
        errorRemap.put("Wrapped abfab3d.grid.util.ExecutionStoppedException","Execution time exceeded.");
    }



    /**
     * Proxy class to avoid proliferation of anonymous classes.
     */
    private static class IProxy implements ContextAction, QuitAction {
        private static final int PROCESS_FILES = 1;
        private static final int EVAL_INLINE_SCRIPT = 2;
        private static final int SYSTEM_EXIT = 3;

        private int type;
        String[] args;
        Object[] script_args;
        String[] params;
        String scriptText;
        private Model model;
        private Context cx;

        IProxy(int type) {
            this.type = type;
        }

        public Object run(Context cx) {
            this.cx = cx;
            if (useRequire) {
                require = global.installRequire(cx, modulePath, sandboxed);
            }
            if (type == PROCESS_FILES) {
                model = processFile(cx, args, script_args);
            } else if (type == EVAL_INLINE_SCRIPT) {
                model = evalInlineScript(cx, scriptText, script_args);
            } else {
                throw Kit.codeBug();
            }

            if (model == null) {
                return null;
            }
            return model;
        }

        public void quit(Context cx, int exitCode) {
            if (type == SYSTEM_EXIT) {
                System.out.println("quit. Not calling exit");

                //System.exit(exitCode);
                return;
            }
            throw Kit.codeBug();
        }

        public Model getModel() {
            return model;
        }

        public void clear() {
            cx = null;
            model = null;
            script_args = null;
        }
    }

    /**
     * Main entry point.
     * <p/>
     * Process arguments as would a normal Java program. Also
     * create a new Context and associate it with the current thread.
     * Then set up the execution environment and begin to
     * execute scripts.
     */
    public static void main(String args[]) {
        try {
            printf("Initializing Java security model");
            initJavaPolicySecuritySupport();
        } catch (SecurityException ex) {
            ex.printStackTrace(System.err);
        }

        int result = exec(args);
        if (result != 0) {
            System.out.println("main. Not calling exit");
            //System.exit(result);
        }
    }

    /**
     * Execute the given arguments, but don't System.exit at the end.
     */
    public static int exec(String origArgs[]) {
        errorReporter = new ToolErrorReporter(false, global.getErr());
        shellContextFactory.setErrorReporter(errorReporter);
        String[] args = processOptions(origArgs);
        if (processStdin) {
            fileList.add(null);
        }
        if (!global.initialized) {
            global.init(shellContextFactory);
        }

        global.initAbFab3D(shellContextFactory);
        System.out.println("Orig: " + java.util.Arrays.toString(origArgs));
        System.out.println("Args: " + java.util.Arrays.toString(args));

        IProxy iproxy = new IProxy(IProxy.PROCESS_FILES);
        iproxy.args = new String[0];
        iproxy.script_args = args;

        ErrorReporterWrapper errors = new ErrorReporterWrapper(errorReporter);
        shellContextFactory.setErrorReporter(errors);

        shellContextFactory.call(iproxy);

        Model model = iproxy.model;

        if (model != null) {
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;

            try {

                String outputType = ShapeJSGlobal.getOutputType();

                fos = new FileOutputStream(ShapeJSGlobal.getOutputFolder() + "/" + ShapeJSGlobal.getInputFileName() + "." + outputType);
                bos = new BufferedOutputStream(fos);

                ModelWriter writer = model.getWriter();

                if (writer == null) {
                    // TODO: this doesn't take into account the user specified versions in the script
                    SingleMaterialModelWriter smwriter = new SingleMaterialModelWriter();
                    model.setWriter(writer);
                    smwriter.setErrorFactor(ShapeJSGlobal.errorFactorDefault);
                    smwriter.setMaxPartsCount(ShapeJSGlobal.maxPartsDefault);
                    smwriter.setMinPartVolume(ShapeJSGlobal.minimumVolumeDefault);
                    smwriter.setSmoothingWidth(ShapeJSGlobal.smoothingWidthDefault);
                    writer = smwriter;
                }

                // We as the wrapper control these
                writer.setOutputFormat(outputType);
                writer.setOutputStream(bos);

                writer.execute(model.getGrid());
            } catch (IOException ioe) {
                ioe.printStackTrace();

                Context.reportError(ToolErrorReporter.getMessage(
                        "msg.couldnt.read.source", "unknown", ioe.getMessage()));
                exitCode = EXITCODE_RUNTIME_ERROR;

            } finally {
                IOUtils.closeQuietly(bos);
                IOUtils.closeQuietly(fos);
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
        System.out.println("Err msgs: " + err_msg);

        return exitCode;
    }

    /**
     * Execute the given arguments, but don't System.exit at the end.
     */
    public static ExecResult execMesh(String origArgs[], String[] scriptArgs) {
    	fileList = new ArrayList<String>();
    	
        System.out.println("Execute mesh.  args: ");
        for (int i = 0; i < origArgs.length; i++) {
            System.out.println(origArgs[i]);
        }

        errorReporter = new ToolErrorReporter(false, global.getErr());
        ErrorReporterWrapper errors = new ErrorReporterWrapper(errorReporter);
        shellContextFactory.setErrorReporter(errors);
        String[] args = processOptions(origArgs);
        if (processStdin) {
            fileList.add(null);
        }
        if (!global.initialized) {
            global.init(shellContextFactory);
        }
        global.initAbFab3D(shellContextFactory);

        IProxy iproxy = new IProxy(IProxy.PROCESS_FILES);
        iproxy.args = args;
        iproxy.script_args = typeArgs(scriptArgs);

        shellContextFactory.call(iproxy);


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
        System.out.println("Err msgs: " + err_msg);

        List<String> prints = DebugLogger.getLog(iproxy.cx);

        String print_msg = "";
        if (prints != null) {
            for(String print : prints) {
                bldr.append(print);
            }
            print_msg = bldr.toString();
        }

        System.out.println("Print msgs: " + print_msg);
        Model model = iproxy.getModel();

        // empty model means we had an error
        if (model != null) {
            ModelWriter writer = model.getWriter();
            if (model.getWriter() == null) {
                writer = createDefaultWriter("x3db", new NullOutputStream(), getShellScope());
                model.setWriter(writer);
            } if (model.getWriter() instanceof VoxelModelWriter) {
                writer.setOutputFormat("svx");
                writer.setOutputStream(new NullOutputStream());
            } else {
                writer.setOutputFormat("x3db");
                writer.setOutputStream(new NullOutputStream());
            }
            iproxy.clear();

            try {
                writer.execute(model.getGrid());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        return new ExecResult(model,err_msg,print_msg);
    }

    /**
     * Execute the given arguments, but don't System.exit at the end.
     */
    public static ExecResult execModel(String origArgs[], String[] scriptArgs) {
        fileList = new ArrayList<String>();

        System.out.println("Execute model.  args: ");
        for (int i = 0; i < origArgs.length; i++) {
            System.out.println(origArgs[i]);
        }

        errorReporter = new ToolErrorReporter(false, global.getErr());
        ErrorReporterWrapper errors = new ErrorReporterWrapper(errorReporter);
        shellContextFactory.setErrorReporter(errors);
        String[] args = processOptions(origArgs);
        if (processStdin) {
            fileList.add(null);
        }
        if (!global.initialized) {
            global.init(shellContextFactory);
        }
        global.initAbFab3D(shellContextFactory);

        IProxy iproxy = new IProxy(IProxy.PROCESS_FILES);
        iproxy.args = args;
        iproxy.script_args = typeArgs(scriptArgs);

        shellContextFactory.call(iproxy);


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
        System.out.println("Err msgs: " + err_msg);

        List<String> prints = DebugLogger.getLog(iproxy.cx);

        String print_msg = "";
        if (prints != null) {
            for(String print : prints) {
                bldr.append(print);
            }
            print_msg = bldr.toString();
        }

        System.out.println("Print msgs: " + print_msg);
        Model model = iproxy.getModel();

        // empty model means we had an error
        if (model != null) {
            ModelWriter writer = model.getWriter();
            if (model.getWriter() == null) {
                writer = createDefaultWriter("x3db", new NullOutputStream(), getShellScope());
                model.setWriter(writer);
            }
        }

        return new ExecResult(model,err_msg,print_msg);
    }

    /**
     * Assign a datatype to a param so normal operations will work right
     *
     * @param args
     * @return
     */
    static Object[] typeArgs(Object[] args) {
        Object[] ret_val = new Object[args.length];

        for(int i=0; i < args.length; i++) {
            try {
                if (args[i] instanceof String) {
                    Double d = new Double((String)args[i]);
                    ret_val[i] = d;
                    System.out.println("Munged arg: " + i + " into Double: " + d);

                    continue;
                } else {
                    ret_val[i] = args[i];
                }
            } catch(Exception e) {
                // ignore
            }

            ret_val[i] = args[i];
        }

        return ret_val;
    }


    static Model processFile(Context cx, String[] args, Object[] scriptArgs) {
        // define "arguments" array in the top-level object:
        // need to allocate new array since newArray requires instances
        // of exactly Object[], not ObjectSubclass[]
        Object[] array = new Object[args.length];
        System.arraycopy(args, 0, array, 0, args.length);
        Scriptable argsObj = cx.newArray(global, array);
        global.defineProperty("arguments", argsObj,
                ScriptableObject.DONTENUM);

        for (String file : fileList) {
            try {
                return processSource(cx, file, scriptArgs);
            } catch (IOException ioex) {
                Context.reportError(ToolErrorReporter.getMessage(
                        "msg.couldnt.read.source", file, ioex.getMessage()));
                exitCode = EXITCODE_FILE_NOT_FOUND;
            } catch (RhinoException rex) {
                if (rex instanceof WrappedException) {
                    System.out.println("Wrapped exception:");
                    int cnt = 0;
                    int max = 5;

                    Throwable we = ((WrappedException)rex).getWrappedException();
                    while(we instanceof WrappedException) {
                        we = ((WrappedException)rex).getWrappedException();
                        cnt++;
                        if (cnt > max) {
                            System.out.println("Exceeded maximum wrappings, exiting.");
                            break;
                        }
                    }
                    if (we != null) we.printStackTrace(System.out);
                }

                ToolErrorReporter.reportException(
                        cx.getErrorReporter(), rex);
                exitCode = EXITCODE_RUNTIME_ERROR;
            } catch (VirtualMachineError ex) {
                // Treat StackOverflow and OutOfMemory as runtime errors
                ex.printStackTrace();
                String msg = ToolErrorReporter.getMessage(
                        "msg.uncaughtJSException", ex.toString());
                Context.reportError(msg);
                exitCode = EXITCODE_RUNTIME_ERROR;
            }
        }

        return null;
    }

    static Model evalInlineScript(Context cx, String scriptText, Object[] args) {
        try {
            Script script = cx.compileString(scriptText, "<command>", 1, null);
            if (script != null) {
                script.exec(cx, getShellScope());
                return executeMain(cx, getShellScope(),args);
            }
        } catch (RhinoException rex) {
            ToolErrorReporter.reportException(
                    cx.getErrorReporter(), rex);
            exitCode = EXITCODE_RUNTIME_ERROR;
        } catch (VirtualMachineError ex) {
            // Treat StackOverflow and OutOfMemory as runtime errors
            ex.printStackTrace();
            String msg = ToolErrorReporter.getMessage(
                    "msg.uncaughtJSException", ex.toString());
            Context.reportError(msg);
            exitCode = EXITCODE_RUNTIME_ERROR;
        }

        return null;
    }

    public static Global getGlobal() {
        return global;
    }

    static Scriptable getShellScope() {
        return getScope(null);
    }

    static Scriptable getScope(String path) {
        if (useRequire) {
            // If CommonJS modules are enabled use a module scope that resolves
            // relative ids relative to the current URL, file or working directory.
            URI uri;
            if (path == null) {
                // use current directory for shell and -e switch
                uri = new File(System.getProperty("user.dir")).toURI();
            } else {
                // find out whether this is a file path or a URL
                if (SourceReader.toUrl(path) != null) {
                    try {
                        uri = new URI(path);
                    } catch (URISyntaxException x) {
                        // fall back to file uri
                        uri = new File(path).toURI();
                    }
                } else {
                    uri = new File(path).toURI();
                }
            }
            return new ModuleScope(global, uri, null);
        } else {
            return global;
        }
    }

    /**
     * Parse arguments.
     */
    public static String[] processOptions(String args[]) {
        String usageError;
        goodUsage:
        for (int i = 0; ; ++i) {
            if (i == args.length) {
                return new String[0];
            }
            String arg = args[i];
            if (!arg.startsWith("-")) {
                if (arg.equals("${script}")) {
                    // ignore
                    continue;
                }
                processStdin = false;
                fileList.add(arg);
                mainModule = arg;
                String[] result = new String[args.length - i - 1];
                System.arraycopy(args, i + 1, result, 0, args.length - i - 1);
                return result;
            }
            if (arg.equals("-version")) {
                if (++i == args.length) {
                    usageError = arg;
                    break goodUsage;
                }
                int version;
                try {
                    version = Integer.parseInt(args[i]);
                } catch (NumberFormatException ex) {
                    usageError = args[i];
                    break goodUsage;
                }
                if (!Context.isValidLanguageVersion(version)) {
                    usageError = args[i];
                    break goodUsage;
                }
                shellContextFactory.setLanguageVersion(version);
                continue;
            } else if (arg.equals("-opt") || arg.equals("-O")) {
                if (++i == args.length) {
                    usageError = arg;
                    break goodUsage;
                }
                int opt;
                try {
                    opt = Integer.parseInt(args[i]);
                } catch (NumberFormatException ex) {
                    usageError = args[i];
                    break goodUsage;
                }
                if (opt == -2) {
                    // Compatibility with Cocoon Rhino fork
                    opt = -1;
                } else if (!Context.isValidOptimizationLevel(opt)) {
                    usageError = args[i];
                    break goodUsage;
                }
                shellContextFactory.setOptimizationLevel(opt);
                continue;
            } else if (arg.equals("-encoding")) {
                if (++i == args.length) {
                    usageError = arg;
                    break goodUsage;
                }
                String enc = args[i];
                shellContextFactory.setCharacterEncoding(enc);
                continue;
            } else if (arg.equals("-outputType")) {
                if (++i == args.length) {
                    usageError = arg;
                    break goodUsage;
                }
                ShapeJSGlobal.setOutputType(args[i]);
                continue;
            } else if (arg.equals("-threads")) {
                if (++i == args.length) {
                    usageError = arg;
                    break goodUsage;
                }
                int max_threads = Integer.parseInt(args[i]);

                if (max_threads == 0) {
                    max_threads = Runtime.getRuntime().availableProcessors();
                }
                System.out.println("Setting max threads to: " + max_threads);
                AbFab3DGlobals.put(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY, max_threads);
                ShapeJSGlobal.setMaximumThreadCount(max_threads);
                continue;
            } else if (arg.equals("-outputFolder")) {
                if (++i == args.length) {
                    usageError = arg;
                    break goodUsage;
                }
                ShapeJSGlobal.setOutputFolder(args[i]);
                continue;
            } else if (arg.equals("-allowWrite")) {
                if (++i == args.length) {
                    usageError = arg;
                    break goodUsage;
                }
                ShapeJSGlobal.setLocalRun(Boolean.parseBoolean(args[i]));
                continue;
            } else if (arg.equals("-debugViz")) {
                if (++i == args.length) {
                    usageError = arg;
                    break goodUsage;
                }
                ShapeJSGlobal.setDebugViz(Boolean.parseBoolean(args[i]));
                continue;
            } else if (arg.equals("-strict")) {
                shellContextFactory.setStrictMode(true);
                shellContextFactory.setAllowReservedKeywords(false);
                errorReporter.setIsReportingWarnings(true);
                continue;
            } else if (arg.equals("-fatal-warnings")) {
                shellContextFactory.setWarningAsError(true);
                continue;
            } else if (arg.equals("-e")) {
                processStdin = false;
                if (++i == args.length) {
                    usageError = arg;
                    break goodUsage;
                }
                if (!global.initialized) {
                    global.init(shellContextFactory);
                }
                global.initAbFab3D(shellContextFactory);

                IProxy iproxy = new IProxy(IProxy.EVAL_INLINE_SCRIPT);
                iproxy.scriptText = args[i];
                shellContextFactory.call(iproxy);

                iproxy.clear();
                continue;
            } else if (arg.equals("-require")) {
                useRequire = true;
                continue;
            } else if (arg.equals("-sandbox")) {
                sandboxed = true;
                useRequire = true;
                continue;
            } else if (arg.equals("-modules")) {
                if (++i == args.length) {
                    usageError = arg;
                    break goodUsage;
                }
                if (modulePath == null) {
                    modulePath = new ArrayList<String>();
                }
                modulePath.add(args[i]);
                useRequire = true;
                continue;
            } else if (arg.equals("-w")) {
                errorReporter.setIsReportingWarnings(true);
                continue;
            } else if (arg.equals("-f")) {
                processStdin = false;
                if (++i == args.length) {
                    usageError = arg;
                    break goodUsage;
                }
                if (args[i].equals("-")) {
                    fileList.add(null);
                } else {
                    fileList.add(args[i]);
                    mainModule = args[i];
                }
                continue;
            } else if (arg.equals("-sealedlib")) {
                global.setSealedStdLib(true);
                continue;
            } else if (arg.equals("-debug")) {
                shellContextFactory.setGeneratingDebug(true);
                continue;
            } else if (arg.equals("-namedParams")) {
                if (++i == args.length) {
                    usageError = arg;
                    break goodUsage;
                }
                if (args[i].equals("${namedParams}")) continue;
                Gson gson = new Gson();
                namedParams = gson.fromJson(args[i], HashMap.class);
                continue;
            } else if (arg.equals("-?") ||
                    arg.equals("-help")) {
                // print usage message
                global.getOut().println(
                        ToolErrorReporter.getMessage("msg.shell.usage", Main.class.getName()));
                System.out.println("args. Not calling exit");
                //System.exit(1);
            }
            usageError = arg;
            break goodUsage;
        }
        // print error and usage message
        global.getOut().println(
                ToolErrorReporter.getMessage("msg.shell.invalid", usageError));
        global.getOut().println(
                ToolErrorReporter.getMessage("msg.shell.usage", Main.class.getName()));
        System.out.println("main. Not calling exit");

        //System.exit(1);
        return null;
    }

    private static void initJavaPolicySecuritySupport() {
        Throwable exObj;
        try {
            Class<?> cl = Class.forName
                    ("volumesculptor.shell.JavaPolicySecurity");
            securityImpl = (SecurityProxy) cl.newInstance();
            SecurityController.initGlobal(securityImpl);
            return;
        } catch (ClassNotFoundException ex) {
            exObj = ex;
        } catch (IllegalAccessException ex) {
            exObj = ex;
        } catch (InstantiationException ex) {
            exObj = ex;
        } catch (LinkageError ex) {
            exObj = ex;
        }
        throw Kit.initCause(new IllegalStateException(
                "Can not load security support: " + exObj), exObj);
    }

    /**
     * Evaluate JavaScript source.
     *
     * @param cx       the current context
     * @param filename the name of the file to compile, or null
     *                 for interactive mode.
     * @throws IOException    if the source could not be read
     * @throws RhinoException thrown during evaluation of source
     */
    public static Model processSource(Context cx, String filename, Object[] args)
            throws IOException {
        if (filename == null || filename.equals("-")) {
            Scriptable scope = getShellScope();
            PrintStream ps = global.getErr();
            if (filename == null) {
                // print implementation version
                ps.println(cx.getImplementationVersion());
            }

            String charEnc = shellContextFactory.getCharacterEncoding();
            if (charEnc == null) {
                charEnc = System.getProperty("file.encoding");
            }
            BufferedReader in;
            try {
                in = new BufferedReader(new InputStreamReader(global.getIn(),
                        charEnc));
            } catch (UnsupportedEncodingException e) {
                throw new UndeclaredThrowableException(e);
            }
            int lineno = 1;
            boolean hitEOF = false;
            while (!hitEOF) {
                String[] prompts = global.getPrompts(cx);
                if (filename == null)
                    ps.print(prompts[0]);
                ps.flush();
                String source = "";

                // Collect lines of source to compile.
                while (true) {
                    String newline;
                    try {
                        newline = in.readLine();
                    } catch (IOException ioe) {
                        ps.println(ioe.toString());
                        break;
                    }
                    if (newline == null) {
                        hitEOF = true;
                        break;
                    }
                    source = source + newline + "\n";
                    lineno++;
                    if (cx.stringIsCompilableUnit(source))
                        break;
                    ps.print(prompts[1]);
                }
                try {
                    Script script = cx.compileString(source, "<stdin>", lineno, null);
                    if (script != null) {

                        Object result = script.exec(cx, scope);
                        // Avoid printing out undefined or function definitions.
                        if (result != Context.getUndefinedValue() &&
                                !(result instanceof Function &&
                                        source.trim().startsWith("function"))) {
                            try {
                                ps.println(Context.toString(result));
                            } catch (RhinoException rex) {
                                ToolErrorReporter.reportException(
                                        cx.getErrorReporter(), rex);
                            }
                        }
                        NativeArray h = global.history;
                        h.put((int) h.getLength(), h, source);
                        return executeMain(cx, scope, args);
                    }
                } catch (RhinoException rex) {
                    ToolErrorReporter.reportException(
                            cx.getErrorReporter(), rex);
                    exitCode = EXITCODE_RUNTIME_ERROR;
                } catch (VirtualMachineError ex) {
                    // Treat StackOverflow and OutOfMemory as runtime errors
                    ex.printStackTrace();
                    String msg = ToolErrorReporter.getMessage(
                            "msg.uncaughtJSException", ex.toString());
                    Context.reportError(msg);
                    exitCode = EXITCODE_RUNTIME_ERROR;
                }
            }
            ps.println();
        } else if (useRequire && filename.equals(mainModule)) {
            require.requireMain(cx, filename);
        } else {
            return processFile(cx, getScope(filename), filename, args);
        }

        return null;
    }

    public static Model processFileNoThrow(Context cx, Scriptable scope, String filename, String[] args) {
        try {
            return processFile(cx, scope, filename, args);
        } catch (IOException ioex) {
            Context.reportError(ToolErrorReporter.getMessage(
                    "msg.couldnt.read.source", filename, ioex.getMessage()));
            exitCode = EXITCODE_FILE_NOT_FOUND;
        } catch (RhinoException rex) {
            ToolErrorReporter.reportException(
                    cx.getErrorReporter(), rex);
            exitCode = EXITCODE_RUNTIME_ERROR;
        } catch (VirtualMachineError ex) {
            // Treat StackOverflow and OutOfMemory as runtime errors
            ex.printStackTrace();
            String msg = ToolErrorReporter.getMessage(
                    "msg.uncaughtJSException", ex.toString());
            Context.reportError(msg);
            exitCode = EXITCODE_RUNTIME_ERROR;
        }

        return null;
    }

    public static Model processFile(Context cx, Scriptable scope, String filename, Object[] args)
            throws IOException {
        if (securityImpl == null) {
            return processFileSecure(cx, scope, filename, null, args);
        } else {
            return securityImpl.callProcessFileSecure(cx, scope, filename, args);
        }
    }

    static Model processFileSecure(Context cx, Scriptable scope,
                                          String path, Object securityDomain, Object[] args)
            throws IOException {
        printf("processing file: %s\n", path);
        ShapeJSGlobal.setInputFilePath(path);
        boolean isClass = path.endsWith(".class");
        Object source = readFileOrUrl(path, !isClass);

        byte[] digest = getDigest(source);
        String key = path + "_" + cx.getOptimizationLevel();

        // Remove caching as it doesn't work for VS
        //ScriptReference ref = scriptCache.get(key, digest);
        ScriptReference ref = null;

        Script script = ref != null ? ref.get() : null;

        if (script == null) {
            if (isClass) {
                script = loadCompiledScript(cx, path, (byte[]) source, securityDomain);
            } else {
                String strSrc = (String) source;
                // Support the executable script #! syntax:  If
                // the first line begins with a '#', treat the whole
                // line as a comment.
                if (strSrc.length() > 0 && strSrc.charAt(0) == '#') {
                    for (int i = 1; i != strSrc.length(); ++i) {
                        int c = strSrc.charAt(i);
                        if (c == '\n' || c == '\r') {
                            strSrc = strSrc.substring(i);
                            break;
                        }
                    }
                }

                strSrc = addImports(strSrc);
                strSrc = addParseFloats(strSrc, args);
                if (ShapeJSGlobal.isDebugViz()) {
                    strSrc = addDebugViz("debugviz",0,strSrc);
                }
                System.out.println("Compiling: \n" + strSrc);
                script = cx.compileString(strSrc, path, 1, securityDomain);
            }
            //scriptCache.put(key, digest, script);
        }

        System.out.println("Script: " + script);

        if (script != null) {
            script.exec(cx, scope);

            return executeMain(cx, scope, args);
        }

        return null;
    }

    /**
     * Add default imports to a script
     * @return
     */
    private static String addImports(String script) {
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
     * Add parse float to float params
     * @return
     */
    private static String addParseFloats(String script, Object[] args) {
        StringBuilder bldr = new StringBuilder();
        int cnt = 0;

        int s_idx = script.indexOf("function main");
        if (s_idx == -1) {
            System.out.println("Cannot find main");
            return script;
        }

        int e_idx = script.indexOf("{", s_idx);

        if (e_idx == -1) {
            System.out.println("Cannot find main");
            return script;
        }

        bldr.append(script.substring(0,e_idx+1));

        for(int i=0; i < args.length; i++) {
            Object o = args[i];
            if (o instanceof Number) {
                bldr.append("args[");
                bldr.append(i);
                bldr.append("] = -(-args[");
                bldr.append(i);
                bldr.append("]);");
                cnt++;
            }
        }
        bldr.append(script.substring(e_idx+2));

        System.out.println("Added ParseFloats: " + cnt);
        //System.out.println("final: " + bldr.toString());
        return bldr.toString();
    }

    /**
     * Add debug viz.  Change maker.makeGrid into createDebug calls
     * @return
     */
    private static String addDebugViz(String prefix,int cnt, String script) {
        // find foo.makeGrid(dest);
        // identify GridMaker(foo) by finding previous ;
        // identify Grid(dest);

        StringBuilder bldr = new StringBuilder();

        int s_idx = script.indexOf(".makeGrid(");
        if (s_idx == -1) {
            System.out.println("Cannot find makeGrid call");
            return script;
        }

        int sc_idx = script.substring(0,s_idx).lastIndexOf(";");

        if (sc_idx == -1) {
            System.out.println("Cannot find previous sc");
            return script;
        }

        int gs_idx = script.indexOf("(",s_idx);
        int ge_idx = script.indexOf(")",gs_idx+1);

        if (ge_idx == -1) {
            System.out.println("Cannot find end of grid param");
            return script;
        }

        String filename = prefix + cnt + ".x3db";
        String grid_maker = script.substring(sc_idx+1,s_idx).trim();
        String grid = script.substring(gs_idx+1,ge_idx).trim();

        System.out.printf("grid_maker: %s\n",grid_maker);
        System.out.printf("grid: %s\n",grid);
        bldr.append(script.substring(0,sc_idx+1));
        bldr.append("\n\tcreateDebug(\"");
        bldr.append(filename);
        bldr.append("\",");
        bldr.append(grid);
        bldr.append(",");
        bldr.append(grid_maker);
        bldr.append(");");

        String ret = addDebugViz(prefix,cnt+1,script.substring(ge_idx+1));
        if (ret != null) {
            bldr.append(ret);
        }

        return bldr.toString();
    }

    /**
     * Execute the main function.
     *
     * @param cx
     * @param scope
     */
    private static Model executeMain(Context cx, Scriptable scope, Object[] args) {

        cx.setClassShutter(new ClassShutter() {

            // Only allow AbFab3D classes to be created from scripts.
            // A type of security policy, but we should learn security policy better
            public boolean visibleToScripts(String className) {
                for(String pack : packageWhitelist) {
                    if (className.startsWith(pack)) {
                        return true;
                    }

                }

                return false;
            }
        });

        Object o = scope.get("main", scope);

        if (o == Scriptable.NOT_FOUND) {
            System.out.println("Cannot find function main");
            return null;

        }
        Function main = (Function) o;

        System.out.println("Func Args: " + java.util.Arrays.toString(args));

        System.out.println("Main is: " + main.getClass());

        Object result = main.call(cx, scope, scope, new Object[] {args});
        /*
           // TODO: this breaks coin.vss example.  Example is wrong but need to coordinate change with portal release
        NativeObject argsMap = new NativeObject();
        
        for(int i = 0; i < args.length; i++) {
            argsMap.defineProperty(Integer.toString(i), args[i].toString(), NativeObject.READONLY);
        }
        
        for(Map.Entry<String, Object> entry : namedParams.entrySet()){
            argsMap.defineProperty(entry.getKey(), entry.getValue().toString(), NativeObject.READONLY);
        }
        
        Object argForMain = argsMap;
        try {
            argForMain = new JsonParser(cx,scope).parseValue(new Gson().toJson(argsMap));
        } catch (Exception e) {e.printStackTrace();}
        
        Object[] argsForMain = new Object[] {argForMain};
        Object result = main.call(cx, scope, scope, argsForMain);
        */

        AttributeGrid grid = null;
        Model model = null;

        if(result == null)
            return null;

        // We can either get a Grid or a ModelWriter back.  If we don't get a ModelWriter then use SingleMaterial version
        // for backwards compatibility
        if (result instanceof AttributeGrid) {
            grid = (AttributeGrid) result;
        } else if (result instanceof Model) {
            model = (Model) result;
        } else {
            NativeJavaObject njo = (NativeJavaObject) result;

            Object no = njo.unwrap();

            if (no instanceof AttributeGrid) {
                grid = (AttributeGrid) no;
            } else if (no instanceof Model) {
                model = (Model) no;
            }
        }


        if (model == null) {
            model = new Model();
            model.setGrid(grid);

            // Create a writer based on scope variable
            model.setWriter(createDefaultWriter("x3db", new NullOutputStream(),scope));
            return model;
        }

        ModelWriter writer = model.getWriter();

        if (writer == null) {
            model.setWriter(createDefaultWriter("x3db", new NullOutputStream(),scope));
        }

        return model;
    }

    private static ModelWriter createDefaultWriter(String format, OutputStream os, Scriptable thisObj) {
        Object smoothing_width = thisObj.get(ShapeJSGlobal.SMOOTHING_WIDTH_VAR, thisObj);
        Object error_factor = thisObj.get(ShapeJSGlobal.ERROR_FACTOR_VAR, thisObj);
        Object min_volume = thisObj.get(ShapeJSGlobal.MESH_MIN_PART_VOLUME_VAR, thisObj);
        Object max_parts = thisObj.get(ShapeJSGlobal.MESH_MAX_PART_COUNT_VAR, thisObj);
        double sw;
        double ef;
        double mv;
        int mp;
        if (smoothing_width instanceof Number) {
            sw = ((Number)smoothing_width).doubleValue();
        } else {
            sw = ShapeJSGlobal.smoothingWidthDefault;
        }
        if (smoothing_width instanceof Number) {
            ef = ((Number)error_factor).doubleValue();
        } else {
            ef = ShapeJSGlobal.errorFactorDefault;
        }
        if (min_volume instanceof Number) {
            mv = ((Number)min_volume).doubleValue();
        } else {
            mv = ShapeJSGlobal.minimumVolumeDefault;
        }
        if (max_parts instanceof Number) {
            mp = ((Number)max_parts).intValue();
        } else {
            mp = ShapeJSGlobal.maxPartsDefault;
        }
        SingleMaterialModelWriter smwriter = new SingleMaterialModelWriter();
        smwriter.setErrorFactor(ef);
        smwriter.setMaxPartsCount(mp);
        smwriter.setMinPartVolume(mv);
        smwriter.setSmoothingWidth(sw);
        smwriter.setOutputFormat(format);
        smwriter.setOutputStream(os);

        return smwriter;
    }
    /**
     * Save used to save during web usage
     *
     * @param grid
     * @return
     */
    private static TriangleMesh save(Grid grid, Scriptable thisObj) {

        printf("save()\n");

        if (grid == null) {
            System.out.println("No grid specified");
        }
        double vs = grid.getVoxelSize();


        Object smoothing_width = thisObj.get(ShapeJSGlobal.SMOOTHING_WIDTH_VAR, thisObj);
        Object error_factor = thisObj.get(ShapeJSGlobal.ERROR_FACTOR_VAR, thisObj);
        Object min_volume = thisObj.get(ShapeJSGlobal.MESH_MIN_PART_VOLUME_VAR, thisObj);
        Object max_parts = thisObj.get(ShapeJSGlobal.MESH_MAX_PART_COUNT_VAR, thisObj);

        double sw;
        double ef;
        double mv;
        int mp;

        if (smoothing_width instanceof Number) {
            sw = ((Number)smoothing_width).doubleValue();
        } else {
            sw = ShapeJSGlobal.smoothingWidthDefault;
        }

        if (smoothing_width instanceof Number) {
            ef = ((Number)error_factor).doubleValue();
        } else {
            ef = ShapeJSGlobal.errorFactorDefault;
        }

        if (min_volume instanceof Number) {
            mv = ((Number)min_volume).doubleValue();
        } else {
            mv = ShapeJSGlobal.minimumVolumeDefault;
        }

        if (max_parts instanceof Number) {
            mp = ((Number)max_parts).intValue();
        } else {
            mp = ShapeJSGlobal.maxPartsDefault;
        }

        double maxDecimationError = ef * vs * vs;
        // Write out the grid to an STL file
        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(ShapeJSGlobal.blockSizeDefault);
        int max_threads = ShapeJSGlobal.getMaxThreadCount();
        if (max_threads == 0) {
            max_threads = Runtime.getRuntime().availableProcessors();
        }
        meshmaker.setThreadCount(max_threads);
        meshmaker.setSmoothingWidth(sw);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(ShapeJSGlobal.maxDecimationCountDefault);
        meshmaker.setMaxAttributeValue(ShapeJSGlobal.maxAttribute);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
        meshmaker.makeMesh(grid, its);

        System.out.println("Vertices: " + its.getVertexCount() + " faces: " + its.getFaceCount());

        if (its.getFaceCount() > ShapeJSGlobal.MAX_TRIANGLE_SIZE) {
            System.out.println("Maximum triangle count exceeded: " + its.getFaceCount());
            throw Context.reportRuntimeError(
                    "Maximum triangle count exceeded.  Max is: " + ShapeJSGlobal.MAX_TRIANGLE_SIZE + " count is: " + its.getFaceCount());
        }

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        System.out.println("Mesh Min Volume: " + mv + " max Parts: " + mp);

        if (mv > 0 || mp < Integer.MAX_VALUE) {
            ShellResults sr = GridSaver.getLargestShells(mesh, mp, mv);
            mesh = sr.getLargestShell();
            int regions_removed = sr.getShellsRemoved();
            System.out.println("Regions removed: " + regions_removed);
        }

        return mesh;
    }

    private static byte[] getDigest(Object source) {
        byte[] bytes, digest = null;

        if (source != null) {
            if (source instanceof String) {
                try {
                    bytes = ((String) source).getBytes("UTF-8");
                } catch (UnsupportedEncodingException ue) {
                    bytes = ((String) source).getBytes();
                }
            } else {
                bytes = (byte[]) source;
            }
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                digest = md.digest(bytes);
            } catch (NoSuchAlgorithmException nsa) {
                // Should not happen
                throw new RuntimeException(nsa);
            }
        }

        return digest;
    }

    private static Script loadCompiledScript(Context cx, String path,
                                             byte[] data, Object securityDomain)
            throws FileNotFoundException {
        if (data == null) {
            throw new FileNotFoundException(path);
        }
        // XXX: For now extract class name of compiled Script from path
        // instead of parsing class bytes
        int nameStart = path.lastIndexOf('/');
        if (nameStart < 0) {
            nameStart = 0;
        } else {
            ++nameStart;
        }
        int nameEnd = path.lastIndexOf('.');
        if (nameEnd < nameStart) {
            // '.' does not exist in path (nameEnd < 0)
            // or it comes before nameStart
            nameEnd = path.length();
        }
        String name = path.substring(nameStart, nameEnd);
        try {
            GeneratedClassLoader loader = SecurityController.createLoader(cx.getApplicationClassLoader(), securityDomain);
            Class<?> clazz = loader.defineClass(name, data);
            loader.linkClass(clazz);
            if (!Script.class.isAssignableFrom(clazz)) {
                throw Context.reportRuntimeError("msg.must.implement.Script");
            }
            return (Script) clazz.newInstance();
        } catch (IllegalAccessException iaex) {
            Context.reportError(iaex.toString());
            throw new RuntimeException(iaex);
        } catch (InstantiationException inex) {
            Context.reportError(inex.toString());
            throw new RuntimeException(inex);
        }
    }

    public static InputStream getIn() {
        return getGlobal().getIn();
    }

    public static void setIn(InputStream in) {
        getGlobal().setIn(in);
    }

    public static PrintStream getOut() {
        return getGlobal().getOut();
    }

    public static void setOut(PrintStream out) {
        getGlobal().setOut(out);
    }

    public static PrintStream getErr() {
        return getGlobal().getErr();
    }

    public static void setErr(PrintStream err) {
        getGlobal().setErr(err);
    }

    /**
     * Read file or url specified by <tt>path</tt>.
     *
     * @return file or url content as <tt>byte[]</tt> or as <tt>String</tt> if
     *         <tt>convertToString</tt> is true.
     */
    private static Object readFileOrUrl(String path, boolean convertToString)
            throws IOException {

        Object ret_val = null;

        try {
            ret_val = SourceReader.readFileOrUrl(path, convertToString,
                    shellContextFactory.getCharacterEncoding());
        } catch(IOException ioe) {
            printf("Fallback to resource: " + path);
            InputStream is = Main.class.getClassLoader().getResourceAsStream(path);

            if (is == null) {
                String cpath = "classes" + File.separator + path;
                printf("Fallback to resource/classes: " + cpath);
                is = Main.class.getClassLoader().getResourceAsStream(cpath);
            }

            if (is == null) {
                String cpath = "classes" + File.separator + path;
                is = new FileInputStream(cpath);

                if (is == null)
                    throw ioe;
            }

            ret_val = IOUtils.toByteArray(is);

            if (convertToString) {
                ret_val = new String((byte[]) ret_val, "UTF-8");
            }
        }

        return ret_val;
    }

    static class ScriptReference extends SoftReference<Script> {
        String path;
        byte[] digest;

        ScriptReference(String path, byte[] digest,
                        Script script, ReferenceQueue<Script> queue) {
            super(script, queue);
            this.path = path;
            this.digest = digest;
        }
    }

    static class ScriptCache extends LinkedHashMap<String, ScriptReference> {
        ReferenceQueue<Script> queue;
        int capacity;

        ScriptCache(int capacity) {
            super(capacity + 1, 2f, true);
            this.capacity = capacity;
            queue = new ReferenceQueue<Script>();
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ScriptReference> eldest) {
            return size() > capacity;
        }

        ScriptReference get(String path, byte[] digest) {
            ScriptReference ref;
            while ((ref = (ScriptReference) queue.poll()) != null) {
                remove(ref.path);
            }
            ref = get(path);
            if (ref != null && !Arrays.equals(digest, ref.digest)) {
                remove(ref.path);
                ref = null;
            }
            return ref;
        }

        void put(String path, byte[] digest, Script script) {
            put(path, new ScriptReference(path, digest, script, queue));
        }

    }
}
