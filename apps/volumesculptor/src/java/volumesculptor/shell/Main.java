/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package volumesculptor.shell;

import abfab3d.grid.Grid;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.TriangleMesh;
import abfab3d.mesh.WingedEdgeTriangleMesh;
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

    static {
        global.initQuitAction(new IProxy(IProxy.SYSTEM_EXIT));

        packageWhitelist = new ArrayList();
        packageWhitelist.add("abfab3d.");
        packageWhitelist.add("javax.vecmath");

        scriptImports = new ArrayList<String>();

        //scriptImports.add("abfab3d.grid.op");
        scriptImports.add("abfab3d.grid");
        scriptImports.add("abfab3d.datasources");

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
        String[] files;
        String[] params;
        private boolean show;
        String scriptText;
        private TriangleMesh mesh;

        IProxy(int type) {
            this.type = type;
        }

        public Object run(Context cx) {
            if (useRequire) {
                require = global.installRequire(cx, modulePath, sandboxed);
            }
            if (type == PROCESS_FILES) {
                mesh = processFile(cx, args, files, params,show);
            } else if (type == EVAL_INLINE_SCRIPT) {
                mesh = evalInlineScript(cx, scriptText, files, params, show);
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
            System.out.println("Initializing Java security model");
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

        System.out.println("Orig: " + java.util.Arrays.toString(origArgs));
        System.out.println("Args: " + java.util.Arrays.toString(args));

        //  TODO: somewhat dodgy idea but check for none file types and call those files
        HashSet<String> file_types = new HashSet<String>();
        file_types.add("stl");
        file_types.add("x3db");
        file_types.add("x3dv");
        file_types.add("x3d");
        file_types.add("png");
        file_types.add("jpg");
        file_types.add("gz");

        ArrayList<String> file_list = new ArrayList<String>();
        ArrayList<String> param_list = new ArrayList<String>();

        for(int i=0; i < args.length; i++) {
            if (file_types.contains(FilenameUtils.getExtension(args[i]))) {
                file_list.add(args[i]);
            } else {
                param_list.add(args[i]);
            }
        }

        String[] files = new String[file_list.size()];
        files = file_list.toArray(files);
        String[] params = new String[param_list.size()];
        params = param_list.toArray(params);
        IProxy iproxy = new IProxy(IProxy.PROCESS_FILES);
        iproxy.args = new String[0];
        iproxy.files = files;
        iproxy.params = params;
        iproxy.show = true;

        System.out.println("Show:" + iproxy.show);
        shellContextFactory.call(iproxy);

        return exitCode;
    }

    /**
     * Execute the given arguments, but don't System.exit at the end.
     */
    public static TriangleMesh execMesh(String origArgs[], String[] files, String[] params) {
    	fileList = new ArrayList<String>();
    	
        System.out.println("Execute mesh.  args: ");
        for (int i = 0; i < origArgs.length; i++) {
            System.out.println(origArgs[i]);
        }

        errorReporter = new ToolErrorReporter(false, global.getErr());
        shellContextFactory.setErrorReporter(errorReporter);
        String[] args = processOptions(origArgs);
        if (processStdin) {
            fileList.add(null);
        }
        if (!global.initialized) {
            global.init(shellContextFactory);
        }
        IProxy iproxy = new IProxy(IProxy.PROCESS_FILES);
        iproxy.args = args;
        iproxy.files = files;
        iproxy.params = params;
        iproxy.show = false;

        shellContextFactory.call(iproxy);

        return iproxy.getMesh();
    }

    static TriangleMesh processFile(Context cx, String[] args, String[] files, String[] params, boolean show) {
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
                return processSource(cx, file, files, params, show);
            } catch (IOException ioex) {
                Context.reportError(ToolErrorReporter.getMessage(
                        "msg.couldnt.read.source", file, ioex.getMessage()));
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
        }

        return null;
    }

    static TriangleMesh evalInlineScript(Context cx, String scriptText, String[] files, String[] params, boolean show) {
        try {
            Script script = cx.compileString(scriptText, "<command>", 1, null);
            if (script != null) {
                script.exec(cx, getShellScope());
                return executeMain(cx, getShellScope(), show, files, params);
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
                IProxy iproxy = new IProxy(IProxy.EVAL_INLINE_SCRIPT);
                iproxy.scriptText = args[i];
                shellContextFactory.call(iproxy);
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
    public static TriangleMesh processSource(Context cx, String filename, String[] files, String[] params, boolean show)
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
                        return executeMain(cx, scope, show, files, params);
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
            return processFile(cx, getScope(filename), filename, files, params, show);
        }

        return null;
    }

    public static TriangleMesh processFileNoThrow(Context cx, Scriptable scope, String filename, String[] files, String[] params, boolean show) {
        try {
            return processFile(cx, scope, filename, files, params, show);
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

    public static TriangleMesh processFile(Context cx, Scriptable scope, String filename, String[] files, String[] params, boolean show)
            throws IOException {
        if (securityImpl == null) {
            return processFileSecure(cx, scope, filename, null, files, params, show);
        } else {
            return securityImpl.callProcessFileSecure(cx, scope, filename, files, params, show);
        }
    }

    static TriangleMesh processFileSecure(Context cx, Scriptable scope,
                                          String path, Object securityDomain, String[] files, String[] params, boolean show)
            throws IOException {

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

                System.out.println("Not importing classes");
                // TODO: removing default imports to test
                //strSrc = addImports(strSrc);
                System.out.println("Compiling: " + strSrc);
                script = cx.compileString(strSrc, path, 1, securityDomain);
            }
            //scriptCache.put(key, digest, script);
        }

        System.out.println("Script: " + script);

        if (script != null) {
            script.exec(cx, scope);

            return executeMain(cx, scope, show, files, params);
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
     * Execute the main function.  We expect a Grid back.
     *
     * @param cx
     * @param scope
     */
    private static TriangleMesh executeMain(Context cx, Scriptable scope, boolean show, String[] files, String[] params) {

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

        Object[] func_args = new Object[files.length + params.length];
        int idx = 0;
        for(int i=0; i < files.length; i++) {
            func_args[idx++] = files[i];
        }
        for(int i=0; i < params.length; i++) {
            func_args[idx++] = params[i];
        }


        System.out.println("Func Args: " + java.util.Arrays.toString(func_args));

        Object result = main.call(cx, scope, scope, func_args);

        Grid grid = null;
        if (result instanceof Grid) {
            grid = (Grid) result;
        } else {
            NativeJavaObject njo = (NativeJavaObject) result;
            grid = (Grid) njo.unwrap();
        }

        if (show) {
            AbFab3DGlobal.show(cx, scope, new Object[]{grid}, null);
        }

        return save(grid);
    }

    private static TriangleMesh save(Grid grid) {
        System.out.println("Save file to handler");

        if (grid == null) {
            System.out.println("No grid specified");
        }
        double vs = grid.getVoxelSize();


        System.out.println("Saving world: " + grid + " to triangles");
        double errorFactor = 0.5;
        double maxDecimationError = errorFactor * vs * vs;

        // Write out the grid to an STL file
        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(50);
        meshmaker.setThreadCount(Runtime.getRuntime().availableProcessors());
        meshmaker.setSmoothingWidth(0.5);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(10);
        meshmaker.setMaxAttributeValue(255);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
        meshmaker.makeMesh(grid, its);

        System.out.println("Vertices: " + its.getVertexCount() + " faces: " + its.getFaceCount());
        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

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
