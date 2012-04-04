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

package booleanops;

// External Imports
import java.util.*;
import java.io.*;

import abfab3d.grid.op.InteriorFinderTriangleBased;
import abfab3d.util.BoundingBoxUtilsFloat;
import org.web3d.vrml.export.*;
import org.web3d.util.ErrorReporter;
import org.j3d.geom.*;

// Internal Imports
import abfab3d.geom.*;
import abfab3d.grid.*;
import abfab3d.grid.op.Subtract;
import abfab3d.io.output.BoxesX3DExporter;


/**
 * Example of boolean operations.  Voxels may not be the best way to
 * evaluate this but it's nice its so easy to code.
 *
 * This example creates a cube and then subtracts 3 rotated cylinders from it.
 *
 * @author Alan Hudson
 */
public class BooleanOps {
    private static final boolean DEBUG = false;

    /** Resolution of the printer in meters.  */
    public static final double RESOLUTION = 0.00015;

    public void generate(String filename) {
        ErrorReporter console = new PlainTextErrorReporter();

        float bsize = 0.0254f;

        BoxGenerator tg = new BoxGenerator(bsize,bsize,bsize);
        GeometryData geom = new GeometryData();
        geom.geometryType = GeometryData.TRIANGLES;
        tg.generate(geom);

        double[] trans =  new double[3];
        double[] maxsize = new double[3];

        BoundingBoxUtilsFloat bc = new BoundingBoxUtilsFloat();

        findGridParams(geom, RESOLUTION, RESOLUTION, trans, maxsize);

        // account for larger cylinder size
        maxsize[0] *= 1.2;
        maxsize[1] *= 1.2;
        maxsize[2] *= 1.2;

        double x = maxsize[0] / 2;
        double y = maxsize[1] / 2;
        double z = maxsize[2] / 2;

        Grid grid = new ArrayAttributeGridByte(maxsize[0],maxsize[1],maxsize[2],
                RESOLUTION, RESOLUTION);

System.out.println("Grid size: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());
        TriangleModelCreator tmc = null;

        double rx = 0,ry = 1,rz = 0,rangle = 0;
        int outerMaterial = 1;
        int innerMaterial = 1;


        tmc = new TriangleModelCreator(geom,x,y,z,
            rx,ry,rz,rangle,outerMaterial,innerMaterial,true,
                new InteriorFinderTriangleBased(geom,x,y,z,rx,ry,rz,rangle,innerMaterial));

        tmc.generate(grid);

        double height = bsize * 1.1;
        double radius = bsize / 2.5f;
        int facets = 64;

        CylinderGenerator cg = new CylinderGenerator((float)height, (float)radius, facets);
        geom = new GeometryData();
        geom.geometryType = GeometryData.TRIANGLES;
        cg.generate(geom);

        Grid grid2 = new ArrayAttributeGridByte(maxsize[0],maxsize[1],maxsize[2],
                RESOLUTION, RESOLUTION);

        tmc = new TriangleModelCreator(geom,x,y,z,
            rx,ry,rz,rangle,outerMaterial,innerMaterial,true,
                new InteriorFinderTriangleBased(geom,x,y,z,rx,ry,rz,rangle,innerMaterial));

        tmc.generate(grid2);


        Subtract op = new Subtract(grid2, 0, 0, 0, 1);
//        Union op = new Union(grid2, 0, 0, 0, 1);
        grid = op.execute(grid);


        rx = 1;
        ry = 0;
        rz = 0;
        rangle = 1.57075;

        grid2 = new ArrayAttributeGridByte(maxsize[0],maxsize[1],maxsize[2],
                RESOLUTION, RESOLUTION);

        tmc = new TriangleModelCreator(geom,x,y,z,
            rx,ry,rz,rangle,outerMaterial,innerMaterial,true,
                new InteriorFinderTriangleBased(geom,x,y,z,rx,ry,rz,rangle,innerMaterial));

        tmc.generate(grid2);

        op = new Subtract(grid2, 0, 0, 0, 1);
//        op = new Union(grid2, 0, 0, 0, 1);
        grid = op.execute(grid);

        rx = 0;
        ry = 0;
        rz = 1;
        rangle = 1.57075;

        grid2 = new ArrayAttributeGridByte(maxsize[0],maxsize[1],maxsize[2],
                RESOLUTION, RESOLUTION);

        tmc = new TriangleModelCreator(geom,x,y,z,
            rx,ry,rz,rangle,outerMaterial,innerMaterial,true);

        tmc.generate(grid2);

        op = new Subtract(grid2, 0, 0, 0, 1);
//        op = new Union(grid2, 0, 0, 0, 1);
        grid = op.execute(grid);


        grid2 = null;  // Free this to save memory before export

        if (DEBUG) {
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


                exporter.writeDebug(grid, colors, transparency);
                exporter.close();

                fos.close();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            try {
                FileOutputStream fos = new FileOutputStream(filename);
                String encoding = filename.substring(filename.lastIndexOf(".")+1);
                BoxesX3DExporter exporter = new BoxesX3DExporter(encoding, fos, console);

                exporter.write(grid, null);
                exporter.close();

                fos.close();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }

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

    public static void main(String[] args) {
        BooleanOps c = new BooleanOps();
        c.generate("out.x3db");
    }
}