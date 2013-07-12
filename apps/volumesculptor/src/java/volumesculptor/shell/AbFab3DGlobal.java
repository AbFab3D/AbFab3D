/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package volumesculptor.shell;

import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.GridShortIntervals;
import abfab3d.io.input.STLReader;
import abfab3d.io.input.WaveletRasterizer;
import abfab3d.io.output.GridSaver;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.SlicesWriter;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import abfab3d.util.BoundingBoxCalculator;
import abfab3d.util.MathUtil;
import abfab3d.util.Units;
import app.common.X3DViewer;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.ToolErrorReporter;
import static abfab3d.util.Units.MM;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides for sharing functions across multiple threads.
 * This is of particular interest to server applications.
 *
 * @author Norris Boyd
 */
public class AbFab3DGlobal extends ImporterTopLevel {
    private static final int maxAttribute = 63;

    private static String[] globalFunctions = {
            "load", "show", "createGrid"
    };

    private HashMap<String,Object> globals = new HashMap<String,Object>();

    public AbFab3DGlobal() {
        WorldWrapper ww = new WorldWrapper();
        World world = ww.getWorld();
        //globals.put("world", world);
        //globals.put("grid", world.getGrid());
        globals.put("maker", world.getMaker());
        //globals.put("bounds", world.getBounds());
        globals.put("MM", Units.MM);
    }

    public String[] getFunctions() {
        return globalFunctions;
    }

    public Map<String,Object> getProperties() {
        return globals;
    }

    private static Global getInstance(Function function) {
        Scriptable scope = function.getParentScope();
        if (!(scope instanceof Global))
            throw reportRuntimeError("msg.bad.shell.function.scope",
                    String.valueOf(scope));
        return (Global) scope;
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
                    "Expected a file to load");
        }
        String filename = Context.toString(args[0]);
        AttributeGrid grid = null;

        double vs = 0.1*MM;
        if (args.length > 1) {
            grid = (AttributeGrid) args[1];
        }

        if (args.length > 2) {
            vs = (Double) args[0];
        }

        try {
            STLReader stl = new STLReader();
            BoundingBoxCalculator bb = new BoundingBoxCalculator();
            stl.read(filename, bb);
            double bounds[] = new double[6];
            bb.getBounds(bounds);

            // Add a 1 voxel margin around the model
            MathUtil.roundBounds(bounds, vs);
            bounds = MathUtil.extendBounds(bounds, 1 * vs);
            int nx = (int) Math.round((bounds[1] - bounds[0]) / vs);
            int ny = (int) Math.round((bounds[3] - bounds[2]) / vs);
            int nz = (int) Math.round((bounds[5] - bounds[4]) / vs);
            System.out.println("Bounds: " + java.util.Arrays.toString(bounds) + " vs: " + vs);


            AttributeGrid dest = null;

            if (grid == null) {
                dest = new ArrayAttributeGridByte(nx,ny,nz, vs, vs);
            } else {
                dest = grid;
            }

            dest.setGridBounds(bounds);

            System.out.println("voxels: " + nx + " " + ny + " " + nz);
            WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, nx, ny, nz);
            rasterizer.setMaxAttributeValue(maxAttribute);

            stl.read(filename, rasterizer);

            rasterizer.getRaster(dest);

            System.out.println("Loaded: " + filename);

            return dest;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return null;
    }

    /**
     * Load a model into a Grid
     * <p/>
     * This method is defined as a JavaScript function.
     */
    public static void show(Context cx, Scriptable thisObj,
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
        double errorFactor = 0.5;
        double maxDecimationError = errorFactor * vs * vs;

        // Write out the grid to an STL file
        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(50);
        meshmaker.setThreadCount(Runtime.getRuntime().availableProcessors());
        meshmaker.setSmoothingWidth(0.5);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(10);
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

    /**
     * Create a new grid
     * <p/>
     * This method is defined as a JavaScript function.
     */
    public static Object createGrid(Context cx, Scriptable thisObj,
                            Object[] args, Function funObj) {

        double[] grid_bounds = new double[6];
        double vs = 0.1*MM;

        if (args.length == 1) {
            AttributeGrid grid = (AttributeGrid) args[0];
            grid.getGridBounds(grid_bounds);
            vs = grid.getVoxelSize();
        } else if (args.length == 7) {
            System.out.println("CreateGrid(xmin,xmax,ymin,ymax,zmin,zmax,voxelSize)");
            grid_bounds[0] = (Double) args[0];
            grid_bounds[1] = (Double) args[1];
            grid_bounds[2] = (Double) args[2];
            grid_bounds[3] = (Double) args[3];
            grid_bounds[4] = (Double) args[4];
            grid_bounds[5] = (Double) args[5];

            vs = (Double) args[6];
        }  else {
            throw new IllegalArgumentException("Invalid number of arguments to CreateGrid(xmin,xmax,ymin,ymax,zmin,zmax,voxelSize)");
        }


        grid_bounds = MathUtil.roundBounds(grid_bounds, vs);
        int[] gs = MathUtil.getGridSize(grid_bounds, vs);

        AttributeGrid dest = null;

        long voxels = gs[0] * gs[1] * gs[2];
        long MAX_MEMORY = 1000l * 1000 * 1000;
        if (voxels > MAX_MEMORY) {
            dest = new GridShortIntervals(gs[0], gs[1], gs[2], vs, vs);
        } else {
            dest = new ArrayAttributeGridByte(gs[0], gs[1], gs[2], vs, vs);
        }

        dest.setGridBounds(grid_bounds);
        System.out.println("Creating grid: " + java.util.Arrays.toString(gs) + java.util.Arrays.toString(grid_bounds) + " vs: " + vs);
        return cx.getWrapFactory().wrapAsJavaObject(cx, funObj.getParentScope(), dest, null);
    }

    static RuntimeException reportRuntimeError(String msgId, String msgArg) {
        String message = ToolErrorReporter.getMessage(msgId, msgArg);
        return Context.reportRuntimeError(message);
    }

}


