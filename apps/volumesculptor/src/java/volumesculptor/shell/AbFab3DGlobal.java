/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package volumesculptor.shell;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Grid;
import abfab3d.grid.GridShortIntervals;

import abfab3d.io.input.STLReader;
import abfab3d.io.input.WaveletRasterizer;
import abfab3d.io.output.GridSaver;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.SlicesWriter;
import abfab3d.io.output.STLWriter;

import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.WingedEdgeTriangleMesh;

import abfab3d.util.BoundingBoxCalculator;
import abfab3d.util.MathUtil;
import abfab3d.util.Units;

import abfab3d.datasources.ImageWrapper;

import app.common.X3DViewer;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.ToolErrorReporter;
import static abfab3d.util.Units.MM;

import java.io.File;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.util.HashMap;
import java.util.Map;

import java.util.Map;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;

/**
 * This class provides for sharing functions across multiple threads.
 * This is of particular interest to server applications.
 *
 * @author Alan Hudson
 */
public class AbFab3DGlobal  {
    public static final int MAX_GRID_SIZE = 2000;
    public static final int MAX_TRIANGLE_SIZE = 3000000;
    public static final int MAX_TIME = 120 * 1000;

    public static final String SMOOTHING_WIDTH_VAR = "meshSmoothingWidth";
    public static final String ERROR_FACTOR_VAR = "meshErrorFactor";
    public static final String MESH_MIN_PART_VOLUME_VAR = "meshMinPartVolume";
    public static final String MESH_MAX_PART_COUNT_VAR = "meshMaxPartsCount";
    public static final String MESH_MAX_TRI_COUNT_VAR = "meshMaxTriCount";

    public static final int maxAttribute = 255;

    public static double errorFactorDefault = 0.1;
    public static int maxDecimationCountDefault = 10;
    public static double smoothingWidthDefault = 0.5;
    public static int blockSizeDefault = 30;
    public static double minimumVolumeDefault = 0;
    public static int maxPartsDefault = Integer.MAX_VALUE;
    public static int maxTriCountDefault = Integer.MAX_VALUE;

    private static String outputFolder = "/tmp";
    private static String inputFilePath= "shape.js";
    private static String inputFileName = "shape.js";
    private static String outputType = "x3d";
    private static String outputFileName = "save.x3d";
    
    private static boolean isLocalRun = false;

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
        "load", "loadImage","createGrid"
    };
    private static String[] globalFunctionsAll = {
        "load", "loadImage","createGrid", "writeAsMesh"
    };

    private HashMap<String,Object> globals = new HashMap<String,Object>();

    public AbFab3DGlobal() {
        WorldWrapper ww = new WorldWrapper();
        World world = ww.getWorld();
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
        AttributeGrid grid = null;

        if (filename == null || filename.length() == 0) {
            throw Context.reportRuntimeError(
                    "No file provided for load() command");
        }

        double vs = 0.1*MM;
        if (args.length > 1) {
            if (args[1] instanceof Grid) {
                grid = (AttributeGrid) args[1];
            } else if (args[1] instanceof Number) {
                vs = getDouble(args[1]);
            }
        }

        printf("load(%s, %7.3f mm)\n",filename, vs/MM);
        
        try {
            STLReader stl = new STLReader();
            BoundingBoxCalculator bb = new BoundingBoxCalculator();
            stl.read(filename, bb);

            double bounds[] = new double[6];
            bb.getBounds(bounds);

            // if any measurement is over 1M then the file "must" be in m instead of mm.  God I hate unspecified units
            if(false){
                // guessing units is bad and may be wrong 
                double sx = bounds[1] - bounds[0];
                double sy = bounds[3] - bounds[2];
                double sz = bounds[5] - bounds[4];
                if (sx > 1 || sy > 1 | sz > 1) {
                    stl.setScale(1);
                    
                    double factor = 1.0 / 1000;
                    for(int i=0; i < 6; i++) {
                        bounds[i] *= factor;
                    }
                }
            }
            //
            // round up to the nearest voxel 
            //
            MathUtil.roundBounds(bounds, vs);
            // Add a 1 voxel margin around the model to get some space 
            bounds = MathUtil.extendBounds(bounds, 1 * vs);
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

            stl.read(filename, rasterizer);

            rasterizer.getRaster(dest);

            System.out.println("Loaded: " + filename);

            return dest;
        } catch (IOException ioe) {
            ioe.printStackTrace();
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
        double maxParts = getInteger(thisObj.get(MESH_MAX_PART_COUNT_VAR, thisObj));
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
        
        if (its.getFaceCount() > AbFab3DGlobal.MAX_TRIANGLE_SIZE) {
            System.out.println("Maximum triangle count exceeded: " + its.getFaceCount());
            throw Context.reportRuntimeError(
                    "Maximum triangle count exceeded.  Max is: " + AbFab3DGlobal.MAX_TRIANGLE_SIZE + " count is: " + its.getFaceCount());
        }

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

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
        if (args.length == 1) {
            if (args[0] instanceof AttributeGrid) {
                AttributeGrid grid = (AttributeGrid) args[0];
                grid.getGridBounds(grid_bounds);
                vs = grid.getVoxelSize();
            } else if (args[0] instanceof NativeJavaObject) {
                AttributeGrid grid = (AttributeGrid) ((NativeJavaObject)args[0]).unwrap();
                grid.getGridBounds(grid_bounds);
                vs = grid.getVoxelSize();
            }
        } else if (args.length == 4) {
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
        } else if (args.length == 7) {
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

        grid_bounds = MathUtil.roundBounds(grid_bounds, vs);
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

        long voxels = (long) gs[0] * gs[1] * gs[2];
        long max_voxels = (long) MAX_GRID_SIZE * MAX_GRID_SIZE * MAX_GRID_SIZE;

        if (voxels > max_voxels) {
            System.out.println("Maximum voxel size exceeded.  Max is: " + MAX_GRID_SIZE + " grid is: " + gs[0] + " " + gs[1] + " " + gs[2]);
            throw Context.reportRuntimeError(
                    "Maximum voxel size exceeded.  Max is: " + MAX_GRID_SIZE + " grid is: " + gs[0] + " " + gs[1] + " " + gs[2]);
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



