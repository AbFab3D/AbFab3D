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

import abfab3d.datasources.DataSourceGrid;
import abfab3d.datasources.ImageWrapper;

import abfab3d.core.AttributeGrid;
import abfab3d.core.Grid;
import abfab3d.core.Grid2D;
import abfab3d.grid.Grid2DShort;
import abfab3d.grid.GridShortIntervals;
import abfab3d.grid.ArrayAttributeGridByte;

import abfab3d.io.input.AttributedMeshReader;
import abfab3d.io.input.GridLoader;
import abfab3d.io.input.URIMapper;
import abfab3d.util.URIUtils;
import abfab3d.io.input.X3DReader;
import abfab3d.io.input.SVXReader;
import abfab3d.io.input.STLReader;
import abfab3d.io.input.WaveletRasterizer;
import abfab3d.io.output.SingleMaterialModelWriter;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.ShellResults;
import abfab3d.io.output.GridSaver;
import abfab3d.io.output.STLWriter;
import abfab3d.io.output.SVXWriter;

import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.ParamCache;

import abfab3d.core.Units;
import abfab3d.util.BoundingBoxCalculator;
import abfab3d.core.TriangleProducer;
import abfab3d.core.MathUtil;
import abfab3d.core.Bounds;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import com.google.gson.Gson;
import org.apache.commons.io.FilenameUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.NativeJavaObject;

import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.export.X3DBinaryRetainedDirectExporter;
import org.web3d.vrml.export.X3DBinarySerializer;
import org.web3d.vrml.export.X3DClassicRetainedExporter;
import org.web3d.vrml.export.X3DXMLRetainedExporter;

import org.web3d.vrml.lang.UnsupportedSpecVersionException;
import org.web3d.vrml.sav.BinaryContentHandler;

import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.image.BufferedImage;

import java.io.OutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;

/**
 * This class provides for sharing functions across multiple threads.
 * This is of particular interest to server applications.
 *
 * @author Alan Hudson
 */
public class ShapeJSGlobal {
    private static final boolean DEBUG_CACHE = false;

    public static final int MAX_GRID_SIZE = 2000;
    public static final int MAX_TRIANGLE_SIZE = 3000000;
    public static final boolean CACHE_URLS = true;
    public static final int MAX_TIME = 120 * 1000;

    public static final String SMOOTHING_WIDTH_VAR = "meshSmoothingWidth";
    public static final String ERROR_FACTOR_VAR = "meshErrorFactor";
    public static final String MESH_MIN_PART_VOLUME_VAR = "meshMinPartVolume";
    public static final String MESH_MAX_PART_COUNT_VAR = "meshMaxPartsCount";
    public static final String MESH_MAX_TRI_COUNT_VAR = "meshMaxTriCount";
//    public static final int MAX_IMAGE_PIXEL_COUNT = 2000 * 2000;
    public static final int MAX_IMAGE_PIXEL_COUNT = 2600 * 2600;

    public static final int m_svr = 255;

    public static double errorFactorDefault = SingleMaterialModelWriter.errorFactorDefault;
    public static int maxDecimationCountDefault = 10;
    public static double smoothingWidthDefault = SingleMaterialModelWriter.smoothingWidthDefault;
    public static int blockSizeDefault = 30;
    public static double minimumVolumeDefault = SingleMaterialModelWriter.minimumVolumeDefault;
    public static int maxPartsDefault = SingleMaterialModelWriter.maxPartsDefault;
    public static int maxTriCountDefault = Integer.MAX_VALUE;

    private static String outputFolder = "/tmp";
    private static String inputFilePath = "shape.js";
    private static String inputFileName = "shape.js";
    private static String outputType = "x3d";
    private static String outputFileName = "save.x3d";

    private static boolean isLocalRun = false;
    private static boolean isDebugViz = false;
    private static int maxThreadCount;
    private static LoadingCache<String, ModelDistanceCacheEntry> modelDistanceCache;
    private static ConcurrentHashMap<String, URLToFileCacheEntry> urlToFileCache;
    private static ConcurrentHashMap<String, String> fileToUrlCache;

    private static HashMap<String, Object> sm_dataCache = new HashMap<String, Object>();

    // Keep loadURL from being used to attack servers.  Every period number of requests to the same host we back off by delay * requests
    // based on window time
    private static final long LUR_DELAY = 100;
    private static final int LUR_PERIOD = 20;
    private static final int LUR_WINDOW = 5 * 60 * 1000;

    private static URIMapper uriMapper = null;

    private static ConcurrentHashMap<String, URLStats> loading = new ConcurrentHashMap<String, URLStats>();
    private static HashMap<String,URLHandler> contentHandlers = new HashMap<String, URLHandler>();
    private static HTTPRequester httpRequester;

    static {
        if (!CACHE_URLS) {
            printf("*** ShapeJSGlobal caching turned off\n");
            new Exception().printStackTrace();
        }
    }

    public static void addContentHandler(String ext, URLHandler handler) {
        contentHandlers.put(ext,handler);
    }

    public static void setURIMapper(URIMapper mapper) {
        uriMapper = mapper;
    }

    public static void setHTTPRequester(HTTPRequester req) {
        httpRequester = req;
    }

    public static String getOutputFolder() {
        return outputFolder;
    }

    public static void setOutputFolder(String folder) {
        outputFolder = folder;
    }

    /**
     * switch on full set of functionality for application running locally
     */
    public static void setLocalRun(boolean value) {
        isLocalRun = value;
    }

    public static void setDebugViz(boolean value) {
        isDebugViz = value;
    }

    /**
     * Set the maximum threads to use
     */
    public static void setMaximumThreadCount(int value) {
        maxThreadCount = value;
    }

    public static int getMaxThreadCount() {
        return maxThreadCount;
    }

    public static String getInputFileName() {
        return inputFileName;
    }

    public static void setInputFilePath(String path) {

        inputFilePath = path;
        int index = path.lastIndexOf('/');
        if (index >= 0) {
            inputFileName = path.substring(index, path.length());
        } else {
            inputFileName = path;
        }
    }

    public static void setOutputType(String type) {
        outputType = type;
    }

    public static String getOutputType() {

        return outputType;

    }

    public static boolean isDebugViz() {
        return isDebugViz;
    }

    public static String getOutputName() {
        return outputFileName;
    }

    /*
    public static void setErrorFactor(double value){
        errorFactor = value;
    }

    public static void setMaxDecimationCount(int value){
        maxDecimationCount = value;
    }

    public static void setSmoothingWidth(double value){
        smoothingWidth = value;
    }
    */
    private static String[] globalFunctionsSecure = {
            "load", "loadImage", "getModelBounds", "loadModelDistance", "loadModelDensity", "loadFont", "createGrid", "writeTree", "print", "loadURL"
    };       // "writeImage" as debug


    private static String[] globalFunctionsAll = {
            "load", "loadImage", "getModelBounds", "loadModelDistance", "loadModelDensity", "loadFont", "createGrid", "writeAsMesh", "writeAsSVX", "writeTree", "print", "loadURL"
    };  // "writeImage"

    private HashMap<String, Object> globals = new HashMap<String, Object>();

    static {
        modelDistanceCache = CacheBuilder.newBuilder()
                .softValues()
                .expireAfterAccess(24 * 60, TimeUnit.MINUTES)
                .removalListener(new ModelDistanceCacheEntry())
                .build(
                        new CacheLoader<String, ModelDistanceCacheEntry>() {
                            public ModelDistanceCacheEntry load(String key) throws ExecutionException {
                                throw new ExecutionException(new IllegalArgumentException("Can't load key: " + key));
                            }

                        });

        urlToFileCache = new ConcurrentHashMap<String, URLToFileCacheEntry>();
        fileToUrlCache = new ConcurrentHashMap<String, String>();
    }

    public ShapeJSGlobal() {
        globals.put("MM", Units.MM);
        globals.put("MM3", Units.MM3);
        globals.put("CM", Units.CM);
        globals.put("IN", Units.IN);
        globals.put("FT", Units.FT);
        globals.put("PI", Math.PI);
        globals.put("TORADIANS", Units.TORADIANS);
        globals.put("TODEGREE", Units.TODEGREE);

        globals.put(ERROR_FACTOR_VAR, errorFactorDefault);
        globals.put(SMOOTHING_WIDTH_VAR, smoothingWidthDefault);
        globals.put(MESH_MIN_PART_VOLUME_VAR, minimumVolumeDefault);
        globals.put(MESH_MAX_PART_COUNT_VAR, maxPartsDefault);
        globals.put(MESH_MAX_TRI_COUNT_VAR, maxTriCountDefault);

        globals.put("console", new Console());

    }

    public String[] getFunctions() {
        if (isLocalRun)
            return globalFunctionsAll;
        else
            return globalFunctionsSecure;

    }

    public Map<String, Object> getProperties() {
        return globals;
    }

    /**
     * Print the string values of its arguments.
     *
     * This method is defined as a JavaScript function.
     * Note that its arguments are of the "varargs" form, which
     * allows it to handle an arbitrary number of arguments
     * supplied to the JavaScript function.
     *
     */
    public static Object print(Context cx, Scriptable thisObj,
                               Object[] args, Function funObj) {

        //PrintStream out = getInstance(funObj).getOut();
        StringBuilder bldr = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                //out.print(" ");
                bldr.append(" ");
            }
            // Convert the arbitrary JavaScript value into a string form.
            String s = Context.toString(args[i]);

            //out.print(s);
            bldr.append(s);
        }
        //out.println();
//        bldr.append("\n");

        printf("%s\n", bldr.toString());
        DebugLogger.log(cx, bldr.toString());
        return Context.getUndefinedValue();
    }

    /**
     * Load a model into a Grid, defaults to loadDensity for backwards compatibility
     * <p/>
     * This method is defined as a JavaScript function.
     */
    public static Object load(Context cx, Scriptable thisObj,
                              Object[] args, Function funObj) {
        return loadModelDensity(cx, thisObj, args, funObj);
    }

    /**
     * Get a models bounds
     * <p/>
     * This method is defined as a JavaScript function.
     */
    public static Object getModelBounds(Context cx, Scriptable thisObj,
                                        Object[] args, Function funObj) {
        if (args.length < 1) {
            throw Context.reportRuntimeError(
                    "No file provided for load() command");
        }
        String filename = Context.toString(args[0]);
        printf("filename: %s\n", filename);
        AttributeGrid grid = null;

        if (filename == null || filename.length() == 0) {
            throw Context.reportRuntimeError(
                    "No file provided for load() command");
        }

        double vs = 0.1 * MM;
        if (args.length > 1) {
            Object arg1 = unwrap(args[1]);
            printf("arg[1]: %s\n", arg1);
            if (arg1 instanceof Grid) {
                grid = (AttributeGrid) arg1;
            } else {
                vs = getDouble(arg1);
            }
        }
        double margin = vs;
        if (args.length > 2) {
            Object arg2 = unwrap(args[2]);
            margin = getDouble(arg2);
        }

        printf("getModelBounds(%s, vs: %7.3f mm, margin: %7.3f mm)\n", filename, vs / MM, margin / MM);

        // TODO: This need caching like loadModelDistance!

        try {
            BoundingBoxCalculator bb = new BoundingBoxCalculator();
            TriangleProducer tp = null;

            File f = new File(filename);
            if (f.isDirectory()) {
                AttributedMeshReader reader = new AttributedMeshReader(filename);
                reader.getAttTriangles(bb);
            } else {

                if (filename.endsWith(".x3d") || filename.endsWith(".x3db") || filename.endsWith(".x3dv")) {
                    tp = new X3DReader(filename);
                    tp.getTriangles(bb);
                } else if (filename.endsWith(".svx")) {
                    SVXReader reader = new SVXReader();
                    return reader.load(filename);
                } else {
                    tp = new STLReader(filename);
                    tp.getTriangles(bb);
                }
            }

            double bounds[] = new double[6];
            bb.getBounds(bounds);

            printf("   orig bounds: [ %7.3f, %7.3f], [%7.3f, %7.3f], [%7.3f, %7.3f] mm; vs: %7.3f mm\n",
                    bounds[0] / MM, bounds[1] / MM, bounds[2] / MM, bounds[3] / MM, bounds[4] / MM, bounds[5] / MM, vs / MM);

            // Add a margin around the model to get some space
            bounds = MathUtil.extendBounds(bounds, margin);
            //
            // round up to the nearest voxel
            //
            MathUtil.roundBounds(bounds, vs);
            int nx = (int) Math.round((bounds[1] - bounds[0]) / vs);
            int ny = (int) Math.round((bounds[3] - bounds[2]) / vs);
            int nz = (int) Math.round((bounds[5] - bounds[4]) / vs);
            printf("   grid bounds: [ %7.3f, %7.3f], [%7.3f, %7.3f], [%7.3f, %7.3f] mm; vs: %7.3f mm\n",
                    bounds[0] / MM, bounds[1] / MM, bounds[2] / MM, bounds[3] / MM, bounds[4] / MM, bounds[5] / MM, vs / MM);
            printf("   grid size: [%d x %d x %d]\n", nx, ny, nz);

            // Confirm valid bounds, as loading a non-model file could lead to negative bounds
            if (nx <= 0 || ny <= 0 || nz <= 0) {
                throw Context.reportRuntimeError("Failed to calculate bounds for: " + filename);
            }

            return new Bounds(bounds);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        throw Context.reportRuntimeError("Failed to calculate bounds for: " + filename);
    }

    /**
     * Load a model into a Grid
     * <p/>
     * This method is defined as a JavaScript function.
     */
    public static Object loadModelDensity(Context cx, Scriptable thisObj,
                                          Object[] args, Function funObj) {
        if (args.length < 1) {
            throw Context.reportRuntimeError(
                    "No file provided for load() command");
        }
        String filename = Context.toString(args[0]);
        printf("filename: %s\n", filename);
        AttributeGrid grid = null;

        if (filename == null || filename.length() == 0) {
            throw Context.reportRuntimeError(
                    "No file provided for load() command");
        }

        // TODO: This needs URL caching
        double vs = 0.1 * MM;
        if (args.length > 1) {
            Object arg1 = unwrap(args[1]);
            printf("arg[1]: %s\n", arg1);
            if (arg1 instanceof Grid) {
                grid = (AttributeGrid) arg1;
            } else {
                vs = getDouble(arg1);
            }
        }
        double margin = vs;
        if (args.length > 2) {
            margin = getDouble(args[2]);
        }

        printf("load(%s, vs: %7.3f mm, margin: %7.3f mm)\n", filename, vs / MM, margin / MM);

        try {
            BoundingBoxCalculator bb = new BoundingBoxCalculator();
            TriangleProducer tp = null;

            if (filename.endsWith(".x3d") || filename.endsWith(".x3db") || filename.endsWith(".x3dv")) {
                tp = new X3DReader(filename);
                tp.getTriangles(bb);
            } else if (filename.endsWith(".svx")) {
                SVXReader reader = new SVXReader();
                return reader.load(filename);
            } else {
                tp = new STLReader(filename);
                tp.getTriangles(bb);
            }

            double bounds[] = new double[6];
            bb.getBounds(bounds);

            printf("   orig bounds: [ %7.3f, %7.3f], [%7.3f, %7.3f], [%7.3f, %7.3f] mm; vs: %7.3f mm\n",
                    bounds[0] / MM, bounds[1] / MM, bounds[2] / MM, bounds[3] / MM, bounds[4] / MM, bounds[5] / MM, vs / MM);

            // Add a margin around the model to get some space 
            bounds = MathUtil.extendBounds(bounds, margin);
            //
            // round up to the nearest voxel 
            //
            MathUtil.roundBounds(bounds, vs);
            int nx = (int) Math.round((bounds[1] - bounds[0]) / vs);
            int ny = (int) Math.round((bounds[3] - bounds[2]) / vs);
            int nz = (int) Math.round((bounds[5] - bounds[4]) / vs);
            printf("   grid bounds: [ %7.3f, %7.3f], [%7.3f, %7.3f], [%7.3f, %7.3f] mm; vs: %7.3f mm\n",
                    bounds[0] / MM, bounds[1] / MM, bounds[2] / MM, bounds[3] / MM, bounds[4] / MM, bounds[5] / MM, vs / MM);
            printf("  grid size: [%d x %d x %d]\n", nx, ny, nz);

            // range check bounds and voxelSized
            for (int i = 0; i < bounds.length; i++) {
                Float f = new Float(bounds[i]);
                if (f.isNaN()) {
                    throw new IllegalArgumentException("Grid size[" + i + "] is Not a Number.");
                }

            }

            if (nx <= 0) {
                throw new IllegalArgumentException("Grid x size <= 0: " + nx);
            }
            if (ny <= 0) {
                throw new IllegalArgumentException("Grid y size <= 0" + ny);
            }
            if (nz <= 0) {
                throw new IllegalArgumentException("Grid z size <= 0" + nz);
            }

            AttributeGrid dest = null;

            if (grid == null) {
                dest = makeEmptyGrid(new int[]{nx, ny, nz}, vs);
            } else {
                dest = grid;
            }

            dest.setGridBounds(bounds);

            WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, nx, ny, nz);
            rasterizer.setMaxAttributeValue(m_svr);

            tp.getTriangles(rasterizer);

            rasterizer.getRaster(dest);

            System.out.println("Loaded: " + filename);

            return dest;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        throw Context.reportRuntimeError("Failed to load file: " + filename);
    }

    /**
     * Load a model into a Grid   loadModelDistance(filename,vs,maxDist)
     * <p/>
     * This method is defined as a JavaScript function.
     */
    public static Object loadModelDistance(Context cx, Scriptable thisObj,
                                           Object[] args, Function funObj) {
        if (args.length < 1) {
            throw Context.reportRuntimeError(
                    "No file provided for load() command");
        }
        String filename = Context.toString(args[0]);
        printf("filename: %s\n", filename);
        AttributeGrid grid = null;

        if (filename == null || filename.length() == 0) {
            throw Context.reportRuntimeError(
                    "No file provided for load() command");
        }

        double vs = 0.1 * MM;
        if (args.length > 1) {
            Object arg1 = unwrap(args[1]);
            printf("arg[1]: %s\n", arg1);
            if (arg1 instanceof Grid) {
                grid = (AttributeGrid) arg1;
            } else {
                vs = getDouble(arg1);
            }
        }
        double maxDist = 1 * MM;
        if (args.length > 2) {
            maxDist = getDouble(args[2]);
        }

        int rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE2;

        if (args.length > 3) {
            int alg = getInteger(unwrap(args[3]));
            switch (alg) {
                default:
                case 0:
                    rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE2;
                    break;
                case 1:
                    rasterAlgorithm = GridLoader.RASTERIZER_DISTANCE;
                    break;
                case 2:
                    rasterAlgorithm = GridLoader.RASTERIZER_WAVELET;
                    break;
            }
        }
        double margin = maxDist + 2 * vs;

        printf("loadModelDistance(%s, vs: %7.3f mm, maxDist: %7.3f mm, margin: %7.3f mm, algorithm:%d)\n", filename, vs / MM, maxDist / MM, margin / MM, rasterAlgorithm);

        String msg = null;
        String key = null;
        String url = null;


        if (CACHE_URLS) {
            url = fileToUrlCache.get(filename);

            if (url == null) {
                if (DEBUG_CACHE) printf("No file to url cache for: %s\n", filename);
            }
            if (url != null) {
                URLToFileCacheEntry ce = urlToFileCache.get(url);
                if (DEBUG_CACHE) printf("Checking url cache: %s  entry: %s\n", url,ce);
                if (ce != null) {
                    if (ce.filename.equals(filename)) {
                        key = ce.url + "_" + vs + "_" + maxDist + "_" + rasterAlgorithm;
                        url = ce.url;

                        try {
                            if (DEBUG_CACHE) printf("Cache worked, return data\n");
                            ModelDistanceCacheEntry mce = modelDistanceCache.get(key);
                            return mce.data;
                        } catch (ExecutionException ee) {
                            // no cache ignore
                            if (DEBUG_CACHE) printf("modelDistanceCache entry not available.  key: %s\n",key);
                        }
                    } else {
                        if (DEBUG_CACHE) printf("Cache filenames not equal: %s != %s\n",ce.filename,filename);
                    }
                } else {
                    if (DEBUG_CACHE) printf("No fileToUrlCache entry when expected: %s\n", url);
                }
            }
        }
/*
        HashMap<String,Object> cparams = new HashMap<String, Object>(2);
        cparams.put("filename",filename);
        cparams.put("maxDist",maxDist);
        cparams.put("algorithm",rasterAlgorithm);

        String vhash = BaseParameterizable.getParamObjString("loadDistance2",cparams);
        Object co = ParamCache.getInstance().get(vhash);
        if (co != null) {
            Scriptable ret_val = (Scriptable) cx.javaToJS(co,thisObj);
            return ret_val;
        }
*/

        String orig_filename = filename;
        // Download
        if (filename.startsWith("http")) {
            try {
                printf("Downloading url: %s\n", filename);
                if (uriMapper != null) filename = uriMapper.mapURI(filename);

                filename = URIUtils.downloadURI("loadModelDistance", filename);
                key = orig_filename + "_" + vs + "_" + maxDist + "_" + rasterAlgorithm;
                url = orig_filename;
            } catch (Exception ioe) {
                ioe.printStackTrace();
            }
        } else {
            key = orig_filename + "_" + vs + "_" + maxDist + "_" + rasterAlgorithm;
            url = orig_filename;
        }

        try {
            long t0 = System.currentTimeMillis();
            int densityBitCount = 8;
            int distanceBitCount = 16;
            double bandWidth = 1 * MM;
            double maxInDistance = maxDist;
            double maxOutDistance = maxDist;
            int threadCount = 4;

            GridLoader loader = new GridLoader();
            loader.setMaxGridSize(1000 * 1000 * 1000L);
            loader.setDensityBitCount(densityBitCount);
            loader.setDistanceBitCount(distanceBitCount);
            loader.setPreferredVoxelSize(vs);
            loader.setDensityAlgorithm(rasterAlgorithm);
            loader.setMaxInDistance(maxInDistance);
            loader.setMaxOutDistance(maxOutDistance);
            loader.setMargins(margin);
            loader.setShellHalfThickness(2);
            loader.setThreadCount(threadCount);

            AttributedMeshReader reader = new AttributedMeshReader(filename);
            AttributeGrid dest = loader.rasterizeAttributedTriangles(reader);

            loader = null;

            DataSourceGrid distData = new DataSourceGrid(dest);
            distData.setBounds(new Bounds(dest.getGridBounds()));
/*
            AttributeGrid dest = loader.loadDistanceGrid(filename);

            loader = null;

            DataSourceGrid distData = new DataSourceGrid(dest);
            distData.setBounds(new Bounds(dest.getGridBounds()));
*/
            if (CACHE_URLS && url != null && key != null) {
                if (DEBUG_CACHE) printf("Caching url: %s key: %s\n", url, key);
                modelDistanceCache.put(key, new ModelDistanceCacheEntry(url, orig_filename, vs, distData));
                URLToFileCacheEntry ce = new URLToFileCacheEntry(url, orig_filename);
                urlToFileCache.put(url, ce);
                fileToUrlCache.put(orig_filename, url);
            }

            printf("Distance calc: %d ms\n", (System.currentTimeMillis() - t0));
/*
            printf("TODO: DEBUG remove slice print\n");
            GridDataChannel dataChannel = dest.getDataDesc().getChannel(0);
            ColorMapper colorMapper = new ColorMapperDistance(0xFF00FF00,0xFFDDFFDD, 0xFF0000FF,0xFFDDDDFF, bandWidth);
            int iz = dest.getDepth() / 4;
            GridUtil.writeSlice(dest, 2,iz , dataChannel, colorMapper, fmt("/tmp/slice_" + iz + ".png", iz));
*/
            return distData;
        } catch (Throwable t) {
            if (t instanceof UnsupportedSpecVersionException) {
                // Those weak of heart look away for here be dragons
                // This seems to happen sometimes and after that its fatal, so shut down.
                printf("Detected fatal problem: " + t.getMessage() + " shutting down\n");
                System.out.flush();
                System.exit(-1);
            }
            msg = t.getMessage();
            t.printStackTrace();
        }

        String errMsg = "Failed to load file: " + filename;
        if (msg != null) errMsg = errMsg + ".  Error is: " + msg;

        if (msg != null && msg.contains("Java heap space")) {
            // Not sure why I can catch this elsewhere as a throwawble...
            printf("Out of memory, shutting down\n");
            System.exit(-1);
        }
        throw Context.reportRuntimeError(errMsg);
    }

    static Object unwrap(Object arg) {
        if (arg instanceof NativeJavaObject)
            arg = ((NativeJavaObject) arg).unwrap();
        return arg;
    }

    /*
    // TODO: these functions are not thread safe and were a temporary hack.
    public static void saveCachedData(Context cx, Scriptable thisObj,
                                      Object[] args, Function funObj) {
        if (args.length != 2) {
            throw Context.reportRuntimeError("saveCachedData(key, data) wrong arguments  count");
        }
        
        String key = Context.toString(args[0]);
        Object data = ((NativeJavaObject) args[1]).unwrap();
        printf("saveCachedData(%s,%s)\n", key, data);

        sm_dataCache.put(key, data);

    }

    public static Object getCachedData(Context cx, Scriptable thisObj,
                                      Object[] args, Function funObj) {
        if (args.length != 1) {
            throw Context.reportRuntimeError("getCachedData(key) wrong arguments count");
        }

        String key = Context.toString(args[0]);
        Object data = sm_dataCache.get(key);
        printf("getCachedData(%s) returs: %s\n", key, data);
        return data;

    }
    */

    /**
     * js function to load image
     * returns BufferedImage
     */
    public static Object loadImage(Context cx, Scriptable thisObj,
                                   Object[] args, Function funObj) {
        long t0 = time();
        if (args.length < 1) {
            throw Context.reportRuntimeError(
                    "No file provided for loadImage() command");
        }
        String filename = Context.toString(args[0]);

        if (filename == null || filename.length() == 0) {
            throw Context.reportRuntimeError("No file provided for load() command");
        }

        double psize = 0.1 * MM;

        if (args.length > 1) {
            psize = getDouble(args[1]);
        }

        HashMap<String, Object> cparams = new HashMap<String, Object>(2);
        cparams.put("filename", filename);
        cparams.put("psize", psize);
        String vhash = BaseParameterizable.getParamObjString("loadImage", cparams);
        Object co = ParamCache.getInstance().get(vhash);
        if (co != null) {
            Scriptable ret_val = (Scriptable) cx.javaToJS(co, thisObj);
            return ret_val;
        }

        BufferedImage image = null;
        String reason = null;

        // TODO: add caching for local downloads?

        String orig_filename = filename;
        // Download
        if (filename.startsWith("http") || filename.startsWith("https")) {
            try {
                printf("Downloading url: %s\n", filename);
                filename = URIUtils.downloadURI("loadImage", filename);
            } catch (Exception ioe) {
                ioe.printStackTrace();
            }
        }

        File f = new File(filename);
        if (!f.exists()) {
            throw Context.reportRuntimeError(fmt("Cannot find image file: %s\n", f.getAbsoluteFile()));
        }

        try {
            image = ImageIO.read(new File(filename));

        } catch (Exception e) {
            reason = e.getMessage();
            e.printStackTrace();
        }
        if (image == null) {
            throw Context.reportRuntimeError(fmt("failed to load image file: %s.  Reason: %s\n", filename, reason));
        }
        int w = image.getWidth();
        int h = image.getHeight();
        if (w * h > MAX_IMAGE_PIXEL_COUNT) {
            throw new IllegalArgumentException(fmt("image dimensions [%d x %d] exceed max allowed: %d", w, h, MAX_IMAGE_PIXEL_COUNT));
        }

        printf("Loading image file: %s  vs: %f  time: %d ms\n", filename, psize, time() - t0);
        ImageWrapper wrapper = new ImageWrapper(image, filename, psize);
        ParamCache.getInstance().put(vhash, wrapper);
        Scriptable ret_val = (Scriptable) cx.javaToJS(wrapper, thisObj);
        return ret_val;
    }

    /**
     * js function to load urls
     * returns map of return value
     */
    public static Object loadURL(Context cx, Scriptable thisObj,
                                 Object[] args, Function funObj) {
        long t0 = time();
        if (args.length < 1) {
            throw Context.reportRuntimeError(
                    "No file provided for loadURL() command");
        }
        String url = Context.toString(args[0]);

        if (url == null || url.length() == 0) {
            throw Context.reportRuntimeError("No url provided for loadURL() command");
        }
        Gson gson = new Gson();
        int TIMEOUT = 180000;
        String USER = null;
        String PASSWORD = null;
        Map<String, String> headers = new HashMap<String, String>();

        if (args.length > 1) {
            if (args[1] instanceof Map) {
                headers.putAll((Map) args[1]);
            }
        }

        if (!url.startsWith("http://")) {
            if (url.contains("..")) {
                printf("Attempt to hack loadURL: %s\n",url);
                return null;
            }
            File f = new File(url);

            if (!f.exists()) return null;
            String ext = FilenameUtils.getExtension(url);

            URLHandler handler = contentHandlers.get(ext);
            if (handler != null) {
                try {
                    FileReader r = new FileReader(f);
                    Object[] recs = handler.parse(r);

                    return recs;
                } catch(Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                return "File of type: " + ext + " not supported.";
            }
            // TODO:  we want to restrict to only files we uploaded via params
            // check that its in /tmp and doesn't contain .. to avoid changing dirs

            // Need to decide how to return the common types of JSON, XML, CSV data formats.
        } else {
            try {
                URL purl = new URL(url);
                String host = purl.getHost();

                URLStats stats = loading.get(host);
                if (stats == null) {
                    stats = new URLStats();
                }

                if (System.currentTimeMillis() - stats.lastLoad > LUR_WINDOW) {
                    stats.attempts = 0;
                }
                int factor = (int) Math.floor(stats.attempts / LUR_PERIOD);
                long delay = LUR_DELAY * factor;
                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                    }
                }

                long stime = System.currentTimeMillis();
                while (stats.inUse == true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ie) {
                    }
                    if (System.currentTimeMillis() - stime > 20000) {
                        return "{error:\"Timed out\"}";
                    }
                }
                Map<String, Object> respMap = null;
                try {
                    stats.inUse = true;
                    stats.lastLoad = System.currentTimeMillis();
                    stats.attempts++;

                    if (httpRequester == null) throw new IllegalArgumentException("Ho HTTP Requester set, cannot download URL");

                    String response = httpRequester.sendRequest(url, HTTPRequester.Method.GET, null, null, headers,
                            null, USER, PASSWORD, TIMEOUT, TIMEOUT, false);

                    respMap = gson.fromJson(response, Map.class);

                    printf("Copying over properties");
                    NativeObject no = new NativeObject();
                    for (Map.Entry<String, Object> vals : respMap.entrySet()) {
                        Object val = vals.getValue();
                        printf("key: %s  val: %s\n", vals.getKey(), val);
                        no.defineProperty(vals.getKey(), val, 0);
                    }
                    return no;
                } finally {
                    stats.inUse = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * js function to load a font
     * returns BufferedImage
     */
    public static Object loadFont(Context cx, Scriptable thisObj,
                                  Object[] args, Function funObj) {
        if (args.length < 1) {
            throw Context.reportRuntimeError(
                    "loadFont() requires one filename.");
        }

        String filename = Context.toString(args[0]);

        if (filename == null || filename.length() == 0) {
            throw Context.reportRuntimeError("No file provided for loadFont() command");
        }

        printf("loading font file: %s\n", filename);

        String reason = null;

        File f = new File(filename);
        Font font = null;
        if (!f.exists()) {
            throw Context.reportRuntimeError(fmt("Cannot find font file: %s\n", f.getAbsoluteFile()));
        }

        if (f.length() > 800000) {
            // Bit of security thought here, font's should not be too large unless they contain some malware payload
            throw Context.reportRuntimeError(fmt("Font file too large"));
        }
        try {
            int type = Font.TRUETYPE_FONT;
            if (filename.indexOf(".ttf") == -1) type = Font.TYPE1_FONT;
            font = Font.createFont(type, f);
        } catch (Exception e) {
            reason = e.getMessage();
            e.printStackTrace();
        }
        if (font == null) {
            throw Context.reportRuntimeError(fmt("failed to load font file: %s.  Reason: %s\n", filename, reason));
        }

        return font;
    }

    /**
     * write grid to a file as a decimated mesh
     */
    public static void writeAsMesh(Context cx, Scriptable thisObj,
                                   Object[] args, Function funObj) {

        AttributeGrid grid = (AttributeGrid) ((NativeJavaObject) args[0]).unwrap();
        String fname = (String) args[1];

        printf("writeAsMesh(%s, %s)\n", grid, fname);

        double vs = grid.getVoxelSize();

        double smoothingWidth = getDouble(thisObj.get(SMOOTHING_WIDTH_VAR, thisObj));
        double errorFactor = getDouble(thisObj.get(ERROR_FACTOR_VAR, thisObj));
        double minimumVolume = getDouble(thisObj.get(MESH_MIN_PART_VOLUME_VAR, thisObj));
        int maxParts = getInteger(thisObj.get(MESH_MAX_PART_COUNT_VAR, thisObj));
        int maxTriCount = getInteger(thisObj.get(MESH_MAX_TRI_COUNT_VAR, thisObj));


        double maxDecimationError = errorFactor * vs * vs;

        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(blockSizeDefault);
        meshmaker.setThreadCount(Runtime.getRuntime().availableProcessors());
        meshmaker.setSmoothingWidth(smoothingWidth);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(maxDecimationCountDefault);
        meshmaker.setMaxAttributeValue(m_svr);
        meshmaker.setMaxTriangles(maxTriCount);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
        meshmaker.makeMesh(grid, its);

        printf("output mesh vertices %d faces: %d\n", its.getVertexCount(), its.getFaceCount());

        if (its.getFaceCount() > ShapeJSGlobal.MAX_TRIANGLE_SIZE) {
            System.out.println("Maximum triangle count exceeded: " + its.getFaceCount());
            throw Context.reportRuntimeError(
                    "Maximum triangle count exceeded.  Max is: " + ShapeJSGlobal.MAX_TRIANGLE_SIZE + " count is: " + its.getFaceCount());
        }

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        printf("Mesh Min Volume: %g meshMaxParts: %d \n", minimumVolume, maxParts);

        if (minimumVolume > 0 || maxParts < Integer.MAX_VALUE) {
            ShellResults sr = GridSaver.getLargestShells(mesh, maxParts, minimumVolume);
            mesh = sr.getLargestShell();
            int regions_removed = sr.getShellsRemoved();
            System.out.println("Regions removed: " + regions_removed);
        }

        try {
            if (fname.endsWith(".stl")) {
                STLWriter stl = new STLWriter(fname);
                mesh.getTriangles(stl);
                stl.close();
            } else if (fname.endsWith(".x3d")) {
                GridSaver.writeMesh(mesh, fname);
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    /**
     * write grid to a file as a decimated mesh
     */
    public static void writeImage(Context cx, Scriptable thisObj,
                                  Object[] args, Function funObj) {

        Grid2D grid = (Grid2D) ((NativeJavaObject) args[0]).unwrap();
        String fname = (String) args[1];

        printf("writeImage(%s, %s)\n", grid, fname);

        try {
            Grid2DShort.write(grid, fname);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * write grid to a SVX file
     */
    public static void writeAsSVX(Context cx, Scriptable thisObj,
                                  Object[] args, Function funObj) {

        AttributeGrid grid = (AttributeGrid) ((NativeJavaObject) args[0]).unwrap();
        String fname = (String) args[1];

        printf("writeAsSVX(%s, %s)\n", grid, fname);


        SVXWriter writer = new SVXWriter();
        writer.write(grid, fname);

    }

    private static BinaryContentHandler createX3DWriter(String format, OutputStream os) {
        org.web3d.util.ErrorReporter console = new PlainTextErrorReporter();

        int sigDigits = 6; // TODO: was -1 but likely needed

        BinaryContentHandler x3dWriter = null;

        if (format.equals("x3db")) {
            x3dWriter = new X3DBinaryRetainedDirectExporter(os,
                    3, 0, console,
                    X3DBinarySerializer.METHOD_FASTEST_PARSING,
                    0.001f, true);
        } else if (format.equals("x3dv")) {
            if (sigDigits > -1) {
                x3dWriter = new X3DClassicRetainedExporter(os, 3, 0, console, sigDigits);
            } else {
                x3dWriter = new X3DClassicRetainedExporter(os, 3, 0, console);
            }
        } else if (format.equals("x3d")) {
            if (sigDigits > -1) {
                x3dWriter = new X3DXMLRetainedExporter(os, 3, 0, console, sigDigits);
            } else {
                x3dWriter = new X3DXMLRetainedExporter(os, 3, 0, console);
            }
        } else {
            throw new IllegalArgumentException("Unhandled file format: " + format);
        }

        x3dWriter.startDocument("", "", "utf8", "#X3D", "V3.0", "");
        x3dWriter.profileDecl("Immersive");
        x3dWriter.startNode("NavigationInfo", null);
        x3dWriter.startField("avatarSize");
        x3dWriter.fieldValue(new float[]{0.01f, 1.6f, 0.75f}, 3);
        x3dWriter.endNode(); // NavigationInfo

        return x3dWriter;
    }

    /**
     * Create a new grid
     * <p/>
     * This method is defined as a JavaScript function.
     */
    public static Object createGrid(Context cx, Scriptable thisObj,
                                    Object[] args, Function funObj) {

        double[] grid_bounds = new double[6];
        double vs = 0.1 * MM;

        for (int i = 0; i < args.length; i++) {
            String st = null;

            if (args[i] instanceof NativeJavaObject) {
                Object o = ((NativeJavaObject) args[i]).unwrap();
                st = "JNO class=" + o.getClass() + " val: " + (o.toString());
            } else {
                st = args[i].toString();
            }
            //System.out.println("arg: " + i + " val: " + st);
        }
        if (args.length == 1) {
            if (args[0] instanceof AttributeGrid) {
                AttributeGrid grid = (AttributeGrid) args[0];
                grid.getGridBounds(grid_bounds);
                vs = grid.getVoxelSize();
            } else if (args[0] instanceof NativeJavaObject) {
                AttributeGrid grid = (AttributeGrid) ((NativeJavaObject) args[0]).unwrap();
                grid.getGridBounds(grid_bounds);
                vs = grid.getVoxelSize();
            }
        } else if (args.length == 4) {
            if (args[0] instanceof AttributeGrid) {
                AttributeGrid grid = (AttributeGrid) args[0];
                grid.getGridBounds(grid_bounds);
                vs = grid.getVoxelSize();
            } else if (args[0] instanceof NativeJavaObject) {
                AttributeGrid grid = (AttributeGrid) ((NativeJavaObject) args[0]).unwrap();
                grid.getGridBounds(grid_bounds);
                vs = grid.getVoxelSize();
            }
            double x = getDouble(args[1]);
            double y = getDouble(args[2]);
            double z = getDouble(args[3]);

            grid_bounds[0] -= x;
            grid_bounds[1] += x;
            grid_bounds[2] -= y;
            grid_bounds[3] += y;
            grid_bounds[4] -= z;
            grid_bounds[5] += z;
        } else if (args.length == 7) {
            grid_bounds[0] = getDouble(args[0]);
            grid_bounds[1] = getDouble(args[1]);
            grid_bounds[2] = getDouble(args[2]);
            grid_bounds[3] = getDouble(args[3]);
            grid_bounds[4] = getDouble(args[4]);
            grid_bounds[5] = getDouble(args[5]);

            vs = getDouble(args[6]);
        } else {
            throw new IllegalArgumentException("Invalid number of arguments to CreateGrid(xmin,xmax,ymin,ymax,zmin,zmax,voxelSize)");
        }

        // range check bounds and voxelSized
        for (int i = 0; i < grid_bounds.length; i++) {
            Float f = new Float(grid_bounds[i]);
            if (f.isNaN()) {
                throw new IllegalArgumentException("Grid size[" + i + "] is Not a Number.");
            }
        }

        if (args.length != 1) {
            // When passed a grid make sure its exactly the same size
            grid_bounds = MathUtil.roundBounds(grid_bounds, vs);
        }
        int[] gs = MathUtil.getGridSize(grid_bounds, vs);

        // range check bounds and voxelSized
        for (int i = 0; i < gs.length; i++) {
            if (gs[i] <= 0) {
                throw new IllegalArgumentException("Grid size[" + i + "] <= 0");
            }
        }

        AttributeGrid dest = makeEmptyGrid(gs, vs);

        //System.out.println("Creating grid: " + java.util.Arrays.toString(gs) + java.util.Arrays.toString(grid_bounds) + " vs: " + vs);
        dest.setGridBounds(grid_bounds);

        return cx.getWrapFactory().wrapAsJavaObject(cx, funObj.getParentScope(), dest, null);
    }

    /**
     * Write out the datasources as a tree
     */
    public static void writeTree(Context cx, Scriptable thisObj,
                                 Object[] args, Function funObj) {

        printf("***writeTree Context is: %s", cx.getClass().getName());
    }

    /**
     * A number in Javascript might be of several forms.  Handle all of them here
     *
     * @param o
     * @return
     */
    private static Double getDouble(Object o) {
        if (o instanceof Double) {
            return (Double) o;
        }

        if (o instanceof NativeJavaObject) {
            return getDouble(((NativeJavaObject) o).unwrap());
        }

        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }

        if (o instanceof String) {
            return Double.parseDouble((String) o);
        }

        return null;
    }

    private static Integer getInteger(Object o) {
        if (o instanceof Integer) {
            return (Integer) o;
        }

        if (o instanceof NativeJavaObject) {
            return getInteger(((NativeJavaObject) o).unwrap());
        }

        if (o instanceof Number) {
            return ((Number) o).intValue();
        }

        if (o instanceof String) {
            return Integer.parseInt((String) o);
        }

        return null;
    }

    private static AttributeGrid makeEmptyGrid(int[] gs, double vs) {
        AttributeGrid dest = null;

        long voxels = ((long) (gs[0])) * gs[1] * gs[2];

        printf("Creating grid: %d %d %d\n", gs[0], gs[1], gs[2], voxels);
        long max_voxels = (long) MAX_GRID_SIZE * MAX_GRID_SIZE * MAX_GRID_SIZE;

        if (voxels > max_voxels) {
            System.out.println("Maximum grid size exceeded.  Max is: " + MAX_GRID_SIZE + "^3 grid is: " + gs[0] + " " + gs[1] + " " + gs[2]);
            throw Context.reportRuntimeError(
                    "Maximum grid size exceeded.  Max is: " + MAX_GRID_SIZE + "^3 grid is: " + gs[0] + " " + gs[1] + " " + gs[2]);
        }

        long MAX_MEMORY = Integer.MAX_VALUE;
        if (voxels > MAX_MEMORY) {
            dest = new GridShortIntervals(gs[0], gs[1], gs[2], vs, vs);
        } else {
            dest = new ArrayAttributeGridByte(gs[0], gs[1], gs[2], vs, vs);
        }

        return dest;
    }


    /**
     * Associate a url with a local filename.
     * @param url
     * @param filename
     */
    public static void mapURLToFile(String url, String filename) {
        if (!CACHE_URLS) return;

        printf("Mapping url: %s to file: %s\n", url, filename);
        URLToFileCacheEntry ce = new URLToFileCacheEntry(url, filename);

        urlToFileCache.put(url, ce);
        fileToUrlCache.put(filename, url);
    }

    /**
     * Is this URL cached already.  If so we won't download it again.
     * @param url
     * @return The local filename or null if not cached
     */
    public static String isURLCached(String url) {
        if (url == null) return null;

        URLToFileCacheEntry ce = urlToFileCache.get(url);

        if (ce == null) return null;
        return ce.filename;
    }

    // URL Caching Section


    static class ModelDistanceCacheEntry implements RemovalListener<String, ModelDistanceCacheEntry> {
        public String url;
        public String filename;
        public double vs;
        public DataSourceGrid data;

        public ModelDistanceCacheEntry() {
        }

        public ModelDistanceCacheEntry(String url, String filename, double vs, DataSourceGrid data) {
            this.url = url;
            this.filename = filename;
            this.vs = vs;
            this.data = data;
        }

        public void onRemoval(RemovalNotification<String, ModelDistanceCacheEntry> removalNotification) {
            ModelDistanceCacheEntry ce = removalNotification.getValue();

            if (ce == null || removalNotification.getCause() == RemovalCause.REPLACED) return;

            printf("Removing ModelDistanceCacheEntry: %s %s\n", ce.url, ce, filename);
            urlToFileCache.remove(ce.url);
            fileToUrlCache.remove(ce.filename);
        }

    }

    static class URLToFileCacheEntry {
        public String url;
        public String filename;

        public URLToFileCacheEntry(String url, String filename) {
            this.url = url;
            this.filename = filename;
        }
    }
}


class URLStats {
    public volatile long attempts;
    public volatile long lastLoad;
    public volatile boolean inUse;
}