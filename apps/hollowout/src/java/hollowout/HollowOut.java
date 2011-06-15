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

package hollowout;

// External Imports
import java.util.*;
import java.io.*;
import org.web3d.vrml.sav.ContentHandler;
import org.web3d.vrml.sav.BinaryContentHandler;
import org.web3d.vrml.export.*;
import org.web3d.util.ErrorReporter;
import org.j3d.geom.*;

// Internal Imports
import abfab3d.geom.*;
import abfab3d.geom.CubeCreator.Style;
import abfab3d.grid.*;
import abfab3d.grid.op.*;
import abfab3d.io.output.BoxesX3DExporter;


/**
 * Example of how to hollow out a model.
 *
 * @author Alan Hudson
 */
public class HollowOut {
    public static final double HORIZ_RESOLUTION = 0.0003;

    /** Verticle resolution of the printer in meters.  */
    public static final double VERT_RESOLUTION = 0.0003;

    public void generate(String filename) {
            ErrorReporter console = new PlainTextErrorReporter();

        float bsize = 0.01f;
        boolean useArrays = true;

        // TODO: arrays are using much less ram then hashmap?

        SphereGenerator tg = new SphereGenerator(bsize, 64);
        GeometryData geom = new GeometryData();
        geom.geometryType = GeometryData.TRIANGLES;
        tg.generate(geom);

        double[] trans =  new double[3];
        double[] maxsize = new double[3];


        int thickenPasses = 1;

        findGridParams(geom, HORIZ_RESOLUTION, VERT_RESOLUTION, trans, maxsize);

        // Reserve space for thickening
        double x = trans[0] + (thickenPasses / 2) * HORIZ_RESOLUTION;
        double y = trans[1] + (thickenPasses / 2) * VERT_RESOLUTION;
        double z = trans[2] + (thickenPasses / 2) * HORIZ_RESOLUTION;

        maxsize[0] += thickenPasses * HORIZ_RESOLUTION;
        maxsize[1] += thickenPasses * VERT_RESOLUTION;
        maxsize[2] += thickenPasses * HORIZ_RESOLUTION;

        Grid grid = new SliceGrid(maxsize[0],maxsize[1],maxsize[2],
            HORIZ_RESOLUTION, VERT_RESOLUTION, useArrays);

        TriangleModelCreator tmc = null;

        double rx = 0,ry = 1,rz = 0,rangle = 0;
        byte outerMaterial = 1;
        byte innerMaterial = 1;


        tmc = new TriangleModelCreator(geom,x,y,z,
            rx,ry,rz,rangle,outerMaterial,innerMaterial,false);

        tmc.generate(grid);

        ThickenUniform op = new ThickenUniform(outerMaterial);

        for(int i=0; i < thickenPasses; i++) {
            op.execute(grid);
        }

        try {
            FileOutputStream fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".")+1);
            BoxesX3DExporter exporter = new BoxesX3DExporter(encoding, fos, console);

            HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
            colors.put(new Integer(Grid.INTERIOR), new float[] {0,1,0});
            colors.put(new Integer(Grid.EXTERIOR), new float[] {1,0,0});
            colors.put(new Integer(Grid.OUTSIDE), new float[] {0,0,1});

            HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
            transparency.put(new Integer(Grid.INTERIOR), new Float(0));
            transparency.put(new Integer(Grid.EXTERIOR), new Float(0.5));
            transparency.put(new Integer(Grid.OUTSIDE), new Float(0.98));

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

    public static void main(String[] args) {
        HollowOut c = new HollowOut();
        c.generate("out.x3db");
    }
}