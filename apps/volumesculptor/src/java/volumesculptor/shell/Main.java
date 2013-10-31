/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package volumesculptor.shell;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Grid;
import abfab3d.io.output.GridSaver;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.SlicesWriter;
import abfab3d.io.output.STLWriter;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.TriangleMesh;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import app.common.ShellResults;
import app.common.X3DViewer;
import org.apache.commons.io.FilenameUtils;
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
import static abfab3d.util.Output.time;

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

        //scriptImports.add("abfab3d.grid.op");
        //scriptImports.add("abfab3d.grid");
        scriptImports.add("abfab3d.datasources");
        scriptImports.add("abfab3d.transforms");
        scriptImports.add("abfab3d.grid.op");
        scriptImports.add("javax.vecmath");

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
        private boolean show;
        String scriptText;
        private TriangleMesh mesh;
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
                mesh = processFile(cx, args, script_args,show);
            } else if (type == EVAL_INLINE_SCRIPT) {
                mesh = evalInlineScript(cx, scriptText, script_args, show);
            } else {
                throw Kit.codeBug();
            }
            return null;
        }

        public void quit(Context cx, int exitCode) {
            if (type == SYSTEM_EXIT) {
                System.out.println("quit. Not calling exit");

                //System.exit(exitCode);
                return;
            }
            throw Kit.codeBug();
        }

        public TriangleMesh getMesh() {
            return mesh;
        }

        public void clear() {
            cx = null;
            mesh = null;
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
        iproxy.show = true;

        System.out.println("Show:" + iproxy.show);
        shellContextFactory.call(iproxy);

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
        iproxy.show = false;

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

        List<String> prints = DebugLogger.getLog(iproxy.cx);

        String print_msg = "";
        if (prints != null) {
            for(String print : prints) {
                bldr.append(print);
            }
            print_msg = bldr.toString();
        }

        System.out.println("Print msgs: " + print_msg);
        TriangleMesh mesh = iproxy.getMesh();
        iproxy.clear();

        return new ExecResult(mesh,err_msg,print_msg);
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


    static TriangleMesh processFile(Context cx, String[] args, Object[] scriptArgs, boolean show) {
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
                return processSource(cx, file, scriptArgs, show);
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
                    we.printStackTrace(System.out);
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

    static TriangleMesh evalInlineScript(Context cx, String scriptText, Object[] args, boolean show) {
        try {
            Script script = cx.compileString(scriptText, "<command>", 1, null);
            if (script != null) {
                script.exec(cx, getShellScope());
                return executeMain(cx, getShellScope(), show, args);
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
            }
            if (arg.equals("-opt") || arg.equals("-O")) {
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
            }
            if (arg.equals("-encoding")) {
                if (++i == args.length) {
                    usageError = arg;
                    break goodUsage;
                }
                String enc = args[i];
                shellContextFactory.setCharacterEncoding(enc);
                continue;
            }
            if (arg.equals("-outputType")) {
                if (++i == args.length) {
                    usageError = arg;
                    break goodUsage;
                }
                AbFab3DGlobal.setOutputType(args[i]);
                continue;
            }
            if (arg.equals("-outputFolder")) {
                if (++i == args.length) {
                    usageError = arg;
                    break goodUsage;
                }
                AbFab3DGlobal.setOutputFolder(args[i]);
                continue;
            }
            if (arg.equals("-allowWrite")) {
                if (++i == args.length) {
                    usageError = arg;
                    break goodUsage;
                }
                AbFab3DGlobal.setLocalRun(Boolean.parseBoolean(args[i]));
                continue;
            }
            if (arg.equals("-strict")) {
                shellContextFactory.setStrictMode(true);
                shellContextFactory.setAllowReservedKeywords(false);
                errorReporter.setIsReportingWarnings(true);
                continue;
            }
            if (arg.equals("-fatal-warnings")) {
                shellContextFactory.setWarningAsError(true);
                continue;
            }
            if (arg.equals("-e")) {
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
            }
            if (arg.equals("-require")) {
                useRequire = true;
                continue;
            }
            if (arg.equals("-sandbox")) {
                sandboxed = true;
                useRequire = true;
                continue;
            }
            if (arg.equals("-modules")) {
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
            }
            if (arg.equals("-w")) {
                errorReporter.setIsReportingWarnings(true);
                continue;
            }
            if (arg.equals("-f")) {
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
            }
            if (arg.equals("-sealedlib")) {
                global.setSealedStdLib(true);
                continue;
            }
            if (arg.equals("-debug")) {
                shellContextFactory.setGeneratingDebug(true);
                continue;
            }
            if (arg.equals("-?") ||
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
    public static TriangleMesh processSource(Context cx, String filename, Object[] args, boolean show)
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
                        return executeMain(cx, scope, show, args);
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
            return processFile(cx, getScope(filename), filename, args, show);
        }

        return null;
    }

    public static TriangleMesh processFileNoThrow(Context cx, Scriptable scope, String filename, String[] args, boolean show) {
        try {
            return processFile(cx, scope, filename, args, show);
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

    public static TriangleMesh processFile(Context cx, Scriptable scope, String filename, Object[] args, boolean show)
            throws IOException {
        if (securityImpl == null) {
            return processFileSecure(cx, scope, filename, null, args, show);
        } else {
            return securityImpl.callProcessFileSecure(cx, scope, filename, args, show);
        }
    }

    static TriangleMesh processFileSecure(Context cx, Scriptable scope,
                                          String path, Object securityDomain, Object[] args, boolean show)
            throws IOException {
        printf("processing file: %s\n", path);
        AbFab3DGlobal.setInputFilePath(path);
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
                System.out.println("Compiling: \n" + strSrc);
                script = cx.compileString(strSrc, path, 1, securityDomain);
            }
            //scriptCache.put(key, digest, script);
        }

        System.out.println("Script: " + script);

        if (script != null) {
            script.exec(cx, scope);

            return executeMain(cx, scope, show, args);
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
        System.out.println("final: " + bldr.toString());
        return bldr.toString();
    }

    /**
     * Execute the main function.  We expect a Grid back.
     *
     * @param cx
     * @param scope
     */
    private static TriangleMesh executeMain(Context cx, Scriptable scope, boolean show, Object[] args) {

        System.out.println("ExecMain.  show: " + show);

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

        for(int i=0; i < args.length; i++) {
            System.out.println("class: " + args[i].getClass());
        }
        System.out.println("Main is: " + main.getClass());
        Object result = main.call(cx, scope, scope, new Object[] {args});

        Grid grid = null;
        if(result == null)
            return null;

        if (result instanceof Grid) {
            grid = (Grid) result;
        } else {
            NativeJavaObject njo = (NativeJavaObject) result;
            grid = (Grid) njo.unwrap();
        }

        if (show) {
            show(cx, scope, new Object[]{grid}, null);
        }

        return save(grid,scope);
    }

    /**
     * Stops execution and shows a grid.  TODO:  How to make it stop?
     * <p/>
     * This method is defined as a JavaScript function.
     */
    public static void show(Context cx, Scriptable thisObj,
                            Object[] args, Function funObj) {


        printf("show()\n");
        AttributeGrid grid = null;

        boolean show_slices = false;

        if (args.length > 0) {
            if (args[0] instanceof Boolean) {
                show_slices = (Boolean) args[0];
            } else if (args[0] instanceof AttributeGrid) {
                grid = (AttributeGrid) args[0];
            } else if (args[0] instanceof NativeJavaObject) {
                grid = (AttributeGrid) ((NativeJavaObject)args[0]).unwrap();
            }
        }

        if (grid == null) {
            System.out.println("No grid specified");
        }
        if (args.length > 1) {
            if (args[1] instanceof Boolean) {
                show_slices = (Boolean) args[0];
            }
        }

        double vs = grid.getVoxelSize();


        if (show_slices) {
            SlicesWriter slicer = new SlicesWriter();
            slicer.setFilePattern("/tmp/slices2/slice_%03d.png");
            slicer.setCellSize(5);
            slicer.setVoxelSize(4);

            slicer.setMaxAttributeValue(AbFab3DGlobal.maxAttribute);
            try {
                slicer.writeSlices(grid);
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }

        System.out.println("Saving world: " + grid + " to triangles");

        Object smoothing_width = thisObj.get(AbFab3DGlobal.SMOOTHING_WIDTH_VAR, thisObj);
        Object error_factor = thisObj.get(AbFab3DGlobal.ERROR_FACTOR_VAR, thisObj);
        Object min_volume = thisObj.get(AbFab3DGlobal.MESH_MIN_PART_VOLUME_VAR, thisObj);
        Object max_parts = thisObj.get(AbFab3DGlobal.MESH_MAX_PART_COUNT_VAR, thisObj);

        printf("max_parts: %s\n", max_parts);
        
        double sw;
        double ef;
        double mv;
        int mp;

        if (smoothing_width instanceof Number) {
            sw = ((Number)smoothing_width).doubleValue();
        } else {
            sw = AbFab3DGlobal.smoothingWidthDefault;
        }

        if (smoothing_width instanceof Number) {
            ef = ((Number)error_factor).doubleValue();
        } else {
            ef = AbFab3DGlobal.errorFactorDefault;
        }

        if (min_volume instanceof Number) {
            mv = ((Number)min_volume).doubleValue();
        } else {
            mv = AbFab3DGlobal.minimumVolumeDefault;
        }

        if (max_parts instanceof Number) {
            mp = ((Number)max_parts).intValue();
        } else {
            mp = AbFab3DGlobal.maxPartsDefault;
        }

        double maxDecimationError = ef * vs * vs;
        // Write out the grid to an STL file
        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(AbFab3DGlobal.blockSizeDefault);
        meshmaker.setThreadCount(Runtime.getRuntime().availableProcessors());
        meshmaker.setSmoothingWidth(sw);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(AbFab3DGlobal.maxDecimationCountDefault);
        meshmaker.setMaxAttributeValue(AbFab3DGlobal.maxAttribute);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
        meshmaker.makeMesh(grid, its);

        System.out.println("Vertices: " + its.getVertexCount() + " faces: " + its.getFaceCount());

        if (its.getFaceCount() > AbFab3DGlobal.MAX_TRIANGLE_SIZE) {
            System.out.println("Maximum triangle count exceeded: " + its.getFaceCount());
            throw Context.reportRuntimeError(
                    "Maximum triangle count exceeded.  Max is: " + AbFab3DGlobal.MAX_TRIANGLE_SIZE + " count is: " + its.getFaceCount());
        }

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        System.out.println("Mesh Min Volume: " + mv + " max Parts: " + mp);

        if (mv > 0 || mp < Integer.MAX_VALUE) {
            ShellResults sr = app.common.GridSaver.getLargestShells(mesh, mp, mv);
            mesh = sr.getLargestShell();
            int regions_removed = sr.getShellsRemoved();
            System.out.println("Regions removed: " + regions_removed);
        }

        try {
            String outputType = AbFab3DGlobal.getOutputType();
            
            if(outputType.equals("x3d")){

                String path = AbFab3DGlobal.getOutputFolder();
                String name = "save.x3d";//AbFab3DGlobal.getOutputName();
                String out = path + "/" + name;
                double[] bounds_min = new double[3];
                double[] bounds_max = new double[3];
                
                grid.getGridBounds(bounds_min,bounds_max);
                double max_axis = Math.max(bounds_max[0] - bounds_min[0], bounds_max[1] - bounds_min[1]);
                max_axis = Math.max(max_axis, bounds_max[2] - bounds_min[2]);
                
                double z = 2 * max_axis / Math.tan(Math.PI / 4);
                float[] pos = new float[] {0,0,(float) z};
                
                GridSaver.writeMesh(mesh, out);
                X3DViewer.viewX3DOM(name, pos);
            } else if(outputType.equals("stl")){
                STLWriter stl = new STLWriter(AbFab3DGlobal.getOutputFolder() + "/" + AbFab3DGlobal.getInputFileName() + ".stl");
                mesh.getTriangles(stl);
                stl.close();
            }
            
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
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


        Object smoothing_width = thisObj.get(AbFab3DGlobal.SMOOTHING_WIDTH_VAR, thisObj);
        Object error_factor = thisObj.get(AbFab3DGlobal.ERROR_FACTOR_VAR, thisObj);
        Object min_volume = thisObj.get(AbFab3DGlobal.MESH_MIN_PART_VOLUME_VAR, thisObj);
        Object max_parts = thisObj.get(AbFab3DGlobal.MESH_MAX_PART_COUNT_VAR, thisObj);

        double sw;
        double ef;
        double mv;
        int mp;

        if (smoothing_width instanceof Number) {
            sw = ((Number)smoothing_width).doubleValue();
        } else {
            sw = AbFab3DGlobal.smoothingWidthDefault;
        }

        if (smoothing_width instanceof Number) {
            ef = ((Number)error_factor).doubleValue();
        } else {
            ef = AbFab3DGlobal.errorFactorDefault;
        }

        if (min_volume instanceof Number) {
            mv = ((Number)min_volume).doubleValue();
        } else {
            mv = AbFab3DGlobal.minimumVolumeDefault;
        }

        if (max_parts instanceof Number) {
            mp = ((Number)max_parts).intValue();
        } else {
            mp = AbFab3DGlobal.maxPartsDefault;
        }

        double maxDecimationError = ef * vs * vs;
        // Write out the grid to an STL file
        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(AbFab3DGlobal.blockSizeDefault);
        meshmaker.setThreadCount(Runtime.getRuntime().availableProcessors());
        meshmaker.setSmoothingWidth(sw);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(AbFab3DGlobal.maxDecimationCountDefault);
        meshmaker.setMaxAttributeValue(AbFab3DGlobal.maxAttribute);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
        meshmaker.makeMesh(grid, its);

        System.out.println("Vertices: " + its.getVertexCount() + " faces: " + its.getFaceCount());

        if (its.getFaceCount() > AbFab3DGlobal.MAX_TRIANGLE_SIZE) {
            System.out.println("Maximum triangle count exceeded: " + its.getFaceCount());
            throw Context.reportRuntimeError(
                    "Maximum triangle count exceeded.  Max is: " + AbFab3DGlobal.MAX_TRIANGLE_SIZE + " count is: " + its.getFaceCount());
        }

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        System.out.println("Mesh Min Volume: " + mv + " max Parts: " + mp);

        if (mv > 0 || mp < Integer.MAX_VALUE) {
            ShellResults sr = app.common.GridSaver.getLargestShells(mesh, mp, mv);
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
        return SourceReader.readFileOrUrl(path, convertToString,
                shellContextFactory.getCharacterEncoding());
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
