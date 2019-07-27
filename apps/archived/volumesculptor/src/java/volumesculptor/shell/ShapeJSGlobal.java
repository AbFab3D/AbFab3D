/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package volumesculptor.shell;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.core.AttributeGrid;
import abfab3d.core.Grid;
import abfab3d.core.Bounds;
import abfab3d.grid.GridShortIntervals;

import abfab3d.grid.op.GridMaker;
import abfab3d.io.input.STLReader;
import abfab3d.io.input.SVXReader;
import abfab3d.io.input.WaveletRasterizer;
import abfab3d.io.input.X3DReader;
import abfab3d.io.output.GridSaver;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.STLWriter;
import abfab3d.io.output.SVXWriter;
import abfab3d.io.output.ShellResults;

import abfab3d.io.output.SingleMaterialModelWriter;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.WingedEdgeTriangleMesh;

import abfab3d.util.BoundingBoxCalculator;
import abfab3d.core.MathUtil;
import abfab3d.core.TriangleProducer;
import abfab3d.core.Units;

import abfab3d.datasources.ImageWrapper;

import org.apache.commons.compress.utils.IOUtils;
import org.mozilla.javascript.*;
import org.web3d.vrml.export.*;
import org.web3d.vrml.sav.BinaryContentHandler;

import static abfab3d.core.Units.MM;

import java.io.*;
import java.lang.IllegalArgumentException;
import java.util.HashMap;
import java.util.Map;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;

/**
 * This class provides for sharing functions across multiple threads.
 * This is of particular interest to server applications.
 *
 * @author Alan Hudson
 */
public class ShapeJSGlobal {
    public static final int MAX_GRID_SIZE = 2000;
    public static final int MAX_TRIANGLE_SIZE = 3000000;
    public static final int MAX_TIME = 120 * 1000;

    public static final String SMOOTHING_WIDTH_VAR = "meshSmoothingWidth";
    public static final String ERROR_FACTOR_VAR = "meshErrorFactor";
    public static final String MESH_MIN_PART_VOLUME_VAR = "meshMinPartVolume";
    public static final String MESH_MAX_PART_COUNT_VAR = "meshMaxPartsCount";
    public static final String MESH_MAX_TRI_COUNT_VAR = "meshMaxTriCount";

    public static final int maxAttribute = 255;

    public static double errorFactorDefault = SingleMaterialModelWriter.errorFactorDefault;
    public static int maxDecimationCountDefault = 10;
    public static double smoothingWidthDefault = SingleMaterialModelWriter.smoothingWidthDefault;
    public static int blockSizeDefault = 30;
    public static double minimumVolumeDefault = SingleMaterialModelWriter.minimumVolumeDefault;
    public static int maxPartsDefault = SingleMaterialModelWriter.maxPartsDefault;
    public static int maxTriCountDefault = Integer.MAX_VALUE;

    private static String outputFolder = "/tmp";
    private static String inputFilePath= "shape.js";
    private static String inputFileName = "shape.js";
    private static String outputType = "x3d";
    private static String outputFileName = "save.x3d";
    
    private static boolean isLocalRun = false;
    private static boolean isDebugViz = false;
    private static int maxThreadCount;

    public static String getOutputFolder(){
        return outputFolder;
    }
    public static void setOutputFolder(String folder){
        outputFolder = folder;
    }

    /**
       switch on full set of functionality for application running locally 
     */
    public static void setLocalRun(boolean value){
        isLocalRun = value;
    }

    public static void setDebugViz(boolean value) {
        isDebugViz = value;
    }

    /**
     * Set the maximum threads to use
     */
    public static void setMaximumThreadCount(int value){
        maxThreadCount = value;
    }

    public static int getMaxThreadCount() {
        return maxThreadCount;
    }

    public static String getInputFileName(){
        return inputFileName;
    }

    public static void setInputFilePath(String path){

        inputFilePath = path;
        int index = path.lastIndexOf('/');
        if(index >= 0){
            inputFileName = path.substring(index,path.length());
        } else {
            inputFileName = path;
        }
    }

    public static void setOutputType(String type){
        outputType = type;
    }

    public static String getOutputType(){

        return outputType;

    }

    public static boolean isDebugViz() {
        return isDebugViz;
    }

    public static String getOutputName(){
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
        "load", "loadImage","createGrid", "createDebug"
    };
    private static String[] globalFunctionsAll = {
        "load", "loadImage","createGrid", "writeAsMesh", "writeAsSVX", 
    };

    private HashMap<String,Object> globals = new HashMap<String,Object>();

    public ShapeJSGlobal() {
        globals.put("MM", Units.MM);
        globals.put("MM3", Units.MM3);
        globals.put("CM", Units.CM);
        globals.put("IN", Units.IN);
        globals.put("FT", Units.FT);
        globals.put("PI", Math.PI);

        globals.put(ERROR_FACTOR_VAR,errorFactorDefault);
        globals.put(SMOOTHING_WIDTH_VAR,smoothingWidthDefault);
        globals.put(MESH_MIN_PART_VOLUME_VAR,minimumVolumeDefault);
        globals.put(MESH_MAX_PART_COUNT_VAR,maxPartsDefault);
        globals.put(MESH_MAX_TRI_COUNT_VAR,maxTriCountDefault);
    }

    public String[] getFunctions() {
        if(isLocalRun)
            return globalFunctionsAll;
        else 
            return globalFunctionsSecure;
        
    }

    public Map<String,Object> getProperties() {
        return globals;
    }

    /**
     * Load a model into a Grid
     * <p/>
     * This method is defined as a JavaScript function.
     */
    public static Object load(Context cx, Scriptable thisObj,
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

        double vs = 0.1*MM;
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
        
        printf("load(%s, vs: %7.3f mm, margin: %7.3f mm)\n",filename, vs/MM, margin/MM);
        
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
                    bounds[0]/MM, bounds[1]/MM, bounds[2]/MM, bounds[3]/MM, bounds[4]/MM, bounds[5]/MM, vs/MM);

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
                   bounds[0]/MM, bounds[1]/MM, bounds[2]/MM, bounds[3]/MM, bounds[4]/MM, bounds[5]/MM, vs/MM);
            printf("  grid size: [%d x %d x %d]\n", nx, ny, nz);

            // range check bounds and voxelSized
            for(int i = 0; i < bounds.length; i++) {
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
                dest = makeEmptyGrid(new int[] {nx,ny,nz},vs);
            } else {
                dest = grid;
            }

            dest.setGridBounds(bounds);

            WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, nx, ny, nz);
            rasterizer.setMaxAttributeValue(maxAttribute);

            tp.getTriangles(rasterizer);

            rasterizer.getRaster(dest);

            System.out.println("Loaded: " + filename);

            return dest;
        } catch(Throwable t) {
            t.printStackTrace();
        }
        throw Context.reportRuntimeError("Failed to load file: " + filename);
    }

    /**
       js function to load image
       returns BufferedImage 
     */
    public static Object loadImage(Context cx, Scriptable thisObj,
                                 Object[] args, Function funObj) {
        if (args.length < 1) {
            throw Context.reportRuntimeError(
                    "No file provided for loadImage() command");
        }
        String filename = Context.toString(args[0]);

        if (filename == null || filename.length() == 0) {
            throw Context.reportRuntimeError("No file provided for load() command");
        }
        
        printf("loading image file: %s\n", filename);

        BufferedImage image = null;

        try {
            image = ImageIO.read(new File(filename));
            
        } catch (Exception e) {
                        
        }
        if(image == null){
            throw Context.reportRuntimeError(fmt("failed to load image file: %s\n",filename)); 
        }

        return new ImageWrapper(image);

    }

    static Object unwrap(Object arg){
        if(arg instanceof NativeJavaObject) 
            arg = ((NativeJavaObject)arg).unwrap();
        return arg;
    }


    /**
       write grid to a file as a decimated mesh 
     */
    public static void writeAsMesh(Context cx, Scriptable thisObj,
                            Object[] args, Function funObj) {

        AttributeGrid grid = (AttributeGrid) ((NativeJavaObject)args[0]).unwrap();
        String fname = (String)args[1];

        printf("writeAsMesh(%s, %s)\n", grid,fname);        
       
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
        meshmaker.setMaxAttributeValue(maxAttribute);
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
            ShellResults sr = GridSaver.getLargestShells(mesh,maxParts, minimumVolume);
            mesh = sr.getLargestShell();
            int regions_removed = sr.getShellsRemoved();
            System.out.println("Regions removed: " + regions_removed);
        }

        try {
            if(fname.endsWith(".stl")){
                STLWriter stl = new STLWriter(fname);
                mesh.getTriangles(stl);
                stl.close();                
            } else if(fname.endsWith(".x3d")){
                GridSaver.writeMesh(mesh, fname);                
            }
            
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
               
    }


    /**
       write grid to a SVX file
     */
    public static void writeAsSVX(Context cx, Scriptable thisObj,
                            Object[] args, Function funObj) {

        AttributeGrid grid = (AttributeGrid) ((NativeJavaObject)args[0]).unwrap();
        String fname = (String)args[1];

        printf("writeAsSVX(%s, %s)\n", grid,fname);        
       
        
        SVXWriter writer = new SVXWriter();
        writer.write(grid, fname);  
              
    }

    /**
     * Create a debug version of the ShapeJS tree
     * <p/>
     * This method is defined as a JavaScript function.
     */
    public static Object createDebug(Context cx, Scriptable thisObj,
                                    Object[] args, Function funObj) {

        String filename;
        AttributeGrid grid;
        GridMaker maker;

        if (args.length == 3) {
            if (args[0] instanceof String) {
                filename = (String) args[0];
            } else if (args[0] instanceof NativeJavaObject) {
                filename = (String) ((NativeJavaObject)args[0]).unwrap();
            } else {
                throw new IllegalArgumentException("Unhandled type for filename: " + args[0]);
            }
            if (args[1] instanceof AttributeGrid) {
                grid = (AttributeGrid) args[1];
            } else if (args[1] instanceof NativeJavaObject) {
                grid = (AttributeGrid) ((NativeJavaObject)args[1]).unwrap();
            } else {
                throw new IllegalArgumentException("Unhandled type for grid: " + args[1]);
            }

            if (args[2] instanceof AttributeGrid) {
                maker = (GridMaker) args[2];
            } else if (args[1] instanceof NativeJavaObject) {
                maker = (GridMaker) ((NativeJavaObject)args[2]).unwrap();
            } else {
                throw new IllegalArgumentException("Unhandled type for gridMaker: " + args[2]);
            }
        }   else {
            throw new IllegalArgumentException("Invalid number of arguments to createDebug(filename,Grid,GridMaker)");
        }

        DataSourceX3DViewer viewer = new DataSourceX3DViewer(maker.getDataSource(),maker.getTransform());
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            fos = new FileOutputStream(filename);
            bos = new BufferedOutputStream(fos);
            BinaryContentHandler stream = createX3DWriter("x3dv",bos);

            viewer.generate(grid,stream);
            stream.endDocument();
        } catch(IOException ioe) {
            ioe.printStackTrace();
            return null;
        } finally {
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(fos);
        }
        return null;
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
        double vs = 0.1*MM;

        for(int i=0; i < args.length; i++) {
            String st = null;

            if (args[i] instanceof NativeJavaObject) {
                Object o = ((NativeJavaObject)args[i]).unwrap();
                st = "JNO class=" + o.getClass() + " val: " + (o.toString());
            } else {
                st = args[i].toString();
            }
            System.out.println("arg: " + i + " val: " + st);
        }
        if (args.length == 1) {  // grid 
            if (args[0] instanceof AttributeGrid) {
                AttributeGrid grid = (AttributeGrid) args[0];
                grid.getGridBounds(grid_bounds);
                vs = grid.getVoxelSize();
            } else if (args[0] instanceof NativeJavaObject) {
                AttributeGrid grid = (AttributeGrid) ((NativeJavaObject)args[0]).unwrap();
                grid.getGridBounds(grid_bounds);
                vs = grid.getVoxelSize();
            }
        } else if (args.length == 2) {  // Bounds, voxelSize
            Bounds bounds;
            if (args[0] instanceof Bounds) {
                bounds = (Bounds) args[0];
            } else {
                bounds = (Bounds) ((NativeJavaObject)args[0]).unwrap();
            }
            grid_bounds[0] = bounds.xmin;
            grid_bounds[1] = bounds.xmax;
            grid_bounds[2] = bounds.ymin;
            grid_bounds[3] = bounds.ymax;
            grid_bounds[4] = bounds.zmin;
            grid_bounds[5] = bounds.zmax;                
            vs = getDouble(args[1]); 
        } else if (args.length == 4) {  // grid, sizex, sizey, sizez 
            if (args[0] instanceof AttributeGrid) {
                AttributeGrid grid = (AttributeGrid) args[0];
                grid.getGridBounds(grid_bounds);
                vs = grid.getVoxelSize();
            } else if (args[0] instanceof NativeJavaObject) {
                AttributeGrid grid = (AttributeGrid) ((NativeJavaObject)args[0]).unwrap();
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
        } else if (args.length == 7) {  // bounds[], voxleSize
            grid_bounds[0] = getDouble(args[0]);
            grid_bounds[1] = getDouble(args[1]);
            grid_bounds[2] = getDouble(args[2]);
            grid_bounds[3] = getDouble(args[3]);
            grid_bounds[4] = getDouble(args[4]);
            grid_bounds[5] = getDouble(args[5]);
            
            vs = getDouble(args[6]);
        }  else {
            throw new IllegalArgumentException("Invalid number of arguments to CreateGrid(xmin,xmax,ymin,ymax,zmin,zmax,voxelSize)");
        }

        // range check bounds and voxelSized
        for(int i = 0; i < grid_bounds.length; i++) {
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
        for(int i = 0; i < gs.length; i++) {
            if (gs[i] <= 0) {
                throw new IllegalArgumentException("Grid size[" + i + "] <= 0");
            }
        }

        AttributeGrid dest = makeEmptyGrid(gs,vs);

        System.out.println("Creating grid: " + java.util.Arrays.toString(gs) + java.util.Arrays.toString(grid_bounds) + " vs: " + vs);
        dest.setGridBounds(grid_bounds);

        return cx.getWrapFactory().wrapAsJavaObject(cx, funObj.getParentScope(), dest, null);
    }

    /**
     * A number in Javascript might be of several forms.  Handle all of them here
     * @param o
     * @return
     */
    private static Double getDouble(Object o) {
        if (o instanceof Double) {
            return (Double)o;
        }

        if (o instanceof NativeJavaObject) {
            return getDouble(((NativeJavaObject) o).unwrap());
        }

        if (o instanceof Number) {
            return ((Number)o).doubleValue();
        }

        if (o instanceof String) {
            return Double.parseDouble((String)o);
        }

        return null;
    }

    private static Integer getInteger(Object o) {
        if (o instanceof Integer) {
            return (Integer)o;
        }

        if (o instanceof NativeJavaObject) {
            return getInteger(((NativeJavaObject) o).unwrap());
        }

        if (o instanceof Number) {
            return ((Number)o).intValue();
        }

        if (o instanceof String) {
            return Integer.parseInt((String)o);
        }

        return null;
    }

    private static AttributeGrid makeEmptyGrid(int[] gs, double vs) {
        AttributeGrid dest = null;

        long voxels = ((long) (gs[0])) * gs[1] * gs[2];

        printf("Creating grid: %d %d %d\n",gs[0],gs[1],gs[2],voxels);
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

}

// unused stuff 

    /**
     * <p/>
     * This method is defined as a JavaScript function.
     not used 
     */
    /*
    public static void _save(Context cx, Scriptable thisObj,
                            Object[] args, Function funObj) {



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

            slicer.setMaxAttributeValue(maxAttribute);
            try {
                slicer.writeSlices(grid);
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
        
        System.out.println("Saving world2: " + grid + " to triangles");


        double maxDecimationError = errorFactor * vs * vs;

        Object smoothing_width = thisObj.get("SMOOTHING_WIDTH", thisObj);

        if (smoothing_width instanceof Number) {
            smoothingWidth = ((Number)smoothing_width).doubleValue();
        }
        System.out.println("Smoothing width: " + smoothingWidth);
        // Write out the grid to an STL file
        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(blockSize);
        meshmaker.setThreadCount(Runtime.getRuntime().availableProcessors());
        meshmaker.setSmoothingWidth(smoothingWidth);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(maxDecimationCount);
        meshmaker.setMaxAttributeValue(maxAttribute);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
        meshmaker.makeMesh(grid, its);

        System.out.println("Vertices: " + its.getVertexCount() + " faces: " + its.getFaceCount());
        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        String path = "/tmp";
        String name = "save.x3d";
        String out = path + "/" + name  ;
        double[] bounds_min = new double[3];
        double[] bounds_max = new double[3];

        grid.getGridBounds(bounds_min,bounds_max);
        double max_axis = Math.max(bounds_max[0] - bounds_min[0], bounds_max[1] - bounds_min[1]);
        max_axis = Math.max(max_axis, bounds_max[2] - bounds_min[2]);

        double z = 2 * max_axis / Math.tan(Math.PI / 4);
        float[] pos = new float[] {0,0,(float) z};

        try {
            GridSaver.writeMesh(mesh, out);
            X3DViewer.viewX3DOM(name, pos);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

    }
    */


    /**
     * Stops execution and shows a grid.  TODO:  How to make it stop?
     * <p/>
     * This method is defined as a JavaScript function.
     not used 
     */
    /*
    public static void _show(Context cx, Scriptable thisObj,
                            Object[] args, Function funObj) {



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

            slicer.setMaxAttributeValue(maxAttribute);
            try {
                slicer.writeSlices(grid);
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }

        System.out.println("Saving world: " + grid + " to triangles");

        Object smoothing_width = thisObj.get(SMOOTHING_WIDTH_VAR, thisObj);
        double sw;
        double ef;

        if (smoothing_width instanceof Number) {
            sw = ((Number)smoothing_width).doubleValue();
        } else {
            sw = smoothingWidth;
        }

        Object error_factor = thisObj.get(ERROR_FACTOR_VAR, thisObj);

        if (smoothing_width instanceof Number) {
            ef = ((Number)error_factor).doubleValue();
        } else {
            ef = errorFactor;
        }

        double maxDecimationError = ef * vs * vs;
        // Write out the grid to an STL file
        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(blockSize);
        meshmaker.setThreadCount(Runtime.getRuntime().availableProcessors());
        meshmaker.setSmoothingWidth(sw);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(maxDecimationCount);
        meshmaker.setMaxAttributeValue(maxAttribute);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
        meshmaker.makeMesh(grid, its);

        System.out.println("Vertices: " + its.getVertexCount() + " faces: " + its.getFaceCount());

        System.out.println("Bigger then max?" + (its.getFaceCount() > MAX_TRIANGLE_SIZE));
        if (its.getFaceCount() > MAX_TRIANGLE_SIZE) {
            System.out.println("Maximum triangle count exceeded: " + its.getFaceCount());
            throw Context.reportRuntimeError(
                    "Maximum triangle count exceeded.  Max is: " + MAX_TRIANGLE_SIZE + " count is: " + its.getFaceCount());
        }

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());


        try {
            String path = "/tmp";
            String name = "save.x3d";
            String out = path + "/" + name  ;
            double[] bounds_min = new double[3];
            double[] bounds_max = new double[3];

            grid.getGridBounds(bounds_min,bounds_max);
            double max_axis = Math.max(bounds_max[0] - bounds_min[0], bounds_max[1] - bounds_min[1]);
            max_axis = Math.max(max_axis, bounds_max[2] - bounds_min[2]);

            double z = 2 * max_axis / Math.tan(Math.PI / 4);
            float[] pos = new float[] {0,0,(float) z};

            GridSaver.writeMesh(mesh, out);
            X3DViewer.viewX3DOM(name, pos);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        throw new ShowException();
    }
    */



