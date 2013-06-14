/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package insides;

// External Imports
import java.util.*;
import java.io.*;

import abfab3d.grid.op.InteriorFinderTriangleBased;
import abfab3d.io.input.IndexedTriangleSetLoader;
import abfab3d.util.BoundingBoxUtilsFloat;
import org.web3d.vrml.export.*;
import org.web3d.util.ErrorReporter;
import org.j3d.geom.*;

// Internal Imports
import abfab3d.geom.*;
import abfab3d.grid.*;
import abfab3d.io.output.BoxesX3DExporter;


/**
 * Example of boolean operations.  Voxels may not be the best way to
 * evaluate this but it's nice its so easy to code.
 *
 * @author Alan Hudson
 */
public class Demo {
//    public static final double HORIZ_RESOLUTION = 0.0002;
    public static final double HORIZ_RESOLUTION = 0.1;
//    public static final double HORIZ_RESOLUTION = 0.25;

    /** Verticle resolution of the printer in meters.  */
//    public static final double VERT_RESOLUTION = 0.0002;
    public static final double VERT_RESOLUTION = HORIZ_RESOLUTION;

    public void generate(String filename) {
        ErrorReporter console = new PlainTextErrorReporter();

        float bsize = 10f;
        float overlap = 0.02f;
        boolean useArrays = true;
        int innerMaterial = 1;
        int outerMaterial = 1;

        // TODO: arrays are using much less ram then hashmap?

        int xsize = 61;
        int ysize = 120;
        int zsize = 13;

        BoxGenerator bg = new BoxGenerator(xsize,ysize,zsize);
        GeometryData geom = new GeometryData();
        geom.geometryType = GeometryData.INDEXED_TRIANGLES;
        bg.generate(geom);

/*
        double ir = 50;
        double or = 40f;
        int facets = 64;

        TorusGenerator tg = new TorusGenerator((float)ir, (float)or, facets, facets);
        geom = new GeometryData();
        geom.geometryType = GeometryData.INDEXED_TRIANGLES;
        tg.generate(geom);

        System.out.println("geom: " + geom);
        System.out.println("indices: " + (geom.indexes == null ? null : geom.indexes.length));
        System.out.println("coords: " + (geom.coordinates == null ? null : geom.coordinates.length));
        System.out.println("indexes(cnt): " + geom.indexesCount);
        System.out.println("coords(cnt): " + geom.vertexCount);
  */

        double[] trans =  new double[3];
        double[] maxsize = new double[3];

        BoundingBoxUtilsFloat bc = new BoundingBoxUtilsFloat();

        float[] bounds = new float[6];
        bc.computeMinMax(geom.coordinates,geom.vertexCount,bounds);

        findGridParams(geom, HORIZ_RESOLUTION, VERT_RESOLUTION, trans, maxsize);
//            maxsize[1] += 2 * overlap;  // account for overlap of cylinder

        double x = trans[0];
        double y = trans[1];
        double z = trans[2];

        Grid grid = new ArrayAttributeGridByte((double)xsize + 2, (double)ysize + 2, (double)zsize + 2, HORIZ_RESOLUTION, VERT_RESOLUTION);

        grid = new RangeCheckWrapper(grid, false);
System.out.println("Grid size: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());
        double[] min = new double[3];
        double[] max = new double[3];
        grid.getGridBounds(min,max);
System.out.println("Grid bounds: " + java.util.Arrays.toString(min) + " " + java.util.Arrays.toString(max));
        TriangleModelCreator tmc = null;

        double rx = 0,ry = 1,rz = 0,rangle = 0;

        tmc = new TriangleModelCreator(geom,x,y,z,
                rx,ry,rz,rangle,outerMaterial,innerMaterial,true,
                new InteriorFinderTriangleBased(geom,bounds,x,y,z,rx,ry,rz,rangle,outerMaterial,innerMaterial));

        //tmc.generate(grid);

        loadFile(grid,"/cygwin/home/giles/projs/shapeways/code/trunk/service/creator/soundwave_cover/src/models/soundwave_cover/x3d/IPHONE4.x3db");
//        loadFile(grid,"/cygwin/home/giles/projs/shapeways/code/trunk/service/creator/soundwave_cover/src/models/soundwave_cover/x3d/foo_tri.x3dv");
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".")+1);
            BoxesX3DExporter exporter = new BoxesX3DExporter(encoding, fos, console);

            HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
            colors.put(new Integer(Grid.INSIDE), new float[] {0,1,0});
            //colors.put(new Integer(Grid.OUTSIDE), new float[] {0,0,1});

            HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
            transparency.put(new Integer(Grid.INSIDE), new Float(0));
            //transparency.put(new Integer(Grid.OUTSIDE), new Float(0.95));

//            exporter.write(grid, null);
            exporter.writeDebug(grid, colors, transparency);
            exporter.close();

            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Find the params needed to place a model in the grid.
     *
     * @param geom The geometry
     * @param horiz The horiz voxel size
     * @param vert The vertical voxel size
     * @param trans The translation to use.  Preallocate to 3.
     * @param size The minimum size the grid needs to be
     */
    private void findGridParams(GeometryData geom, double horiz, double vert, double[] trans, double[] size) {
        double[] min = new double[3];
        double[] max = new double[3];

        min[0] = Double.POSITIVE_INFINITY;
        min[1] = Double.POSITIVE_INFINITY;
        min[2] = Double.POSITIVE_INFINITY;
        max[0] = Double.NEGATIVE_INFINITY;
        max[1] = Double.NEGATIVE_INFINITY;
        max[2] = Double.NEGATIVE_INFINITY;

        int len = geom.coordinates.length / 3;
        int idx = 0;

        for(int i=0; i < len; i++) {
            if (geom.coordinates[idx] > max[0]) {
                max[0] = geom.coordinates[idx];
            }
            if (geom.coordinates[idx] < min[0]) {
                min[0] = geom.coordinates[idx];
            }

            idx++;

            if (geom.coordinates[idx] > max[1]) {
                max[1] = geom.coordinates[idx];
            }

            if (geom.coordinates[idx] < min[1]) {
                min[1] = geom.coordinates[idx];
            }

            idx++;

            if (geom.coordinates[idx] > max[2]) {
                max[2] = geom.coordinates[idx];
            }

            if (geom.coordinates[idx] < min[2]) {
                min[2] = geom.coordinates[idx];
            }

            idx++;
        }

        // Leave one ring of voxels around the item

        int numVoxels = 1;

        size[0] = (max[0] - min[0]) + (numVoxels * 2 * horiz);
        size[1] = (max[1] - min[1]) + (numVoxels * 2 * vert);
        size[2] = (max[2] - min[2]) + (numVoxels * 2 * horiz);

        trans[0] = -min[0] + numVoxels * horiz;
        trans[1] = -min[1] + numVoxels * vert;
        trans[2] = -min[2] + numVoxels * horiz;
    }

    /**
     * Find the absolute maximum bounds of a geometry.
     *
     * @return The max
     */
    private double findMaxBounds(GeometryData geom) {
        double max = Double.NEGATIVE_INFINITY;

        int len = geom.coordinates.length;

        for(int i=0; i < len; i++) {
            if (geom.coordinates[i] > max) {
                max = geom.coordinates[i];
            }
        }

        return Math.abs(max);
    }

    /**
     *  Load a 3D file into the grid
     *
     * @param file
     */
    private void loadFile(Grid grid, String file) {
        long start = System.currentTimeMillis();

        IndexedTriangleSetLoader loader = new IndexedTriangleSetLoader(false);
        loader.processFile(new File(file));

        GeometryData geom = new GeometryData();
        geom.geometryType = GeometryData.INDEXED_TRIANGLES;
        geom.coordinates = loader.getCoords();
        geom.indexes = loader.getVerts();
        geom.indexesCount = geom.indexes.length;

        loader.computeModelBounds();
        float[] bounds = loader.getBounds();

        System.out.println("bounds: " + java.util.Arrays.toString(bounds));
        double x = -bounds[0];
        double y = -bounds[2];
        double z = -bounds[4];

        TriangleModelCreator tmc = null;

        double rx = 0,ry = 1,rz = 0,rangle = 0;

        // TODO: using a transform with InteriorFinder duplicates geometry transform

/*
        tmc = new TriangleModelCreator(geom,x,y,z,
                rx,ry,rz,rangle,EXT_MAT,INT_MAT,true);
*/

        tmc = new TriangleModelCreator(geom,x,y,z,
                rx,ry,rz,rangle,1,1,true,
                new InteriorFinderTriangleBased(geom,bounds, x,y,z,rx,ry,rz,rangle,1, 1));

        tmc.generate(grid);

        System.out.println("load time: " + (System.currentTimeMillis() - start));

    }

    public static void main(String[] args) {
        Demo c = new Demo();
        c.generate("out.x3db");
    }
}