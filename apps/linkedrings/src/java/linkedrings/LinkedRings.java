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

package linkedrings;

// External Imports
import java.util.*;
import java.io.*;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;

import org.j3d.geom.GeometryData;
import org.j3d.geom.TorusGenerator;

import javax.vecmath.*;

// Internal Imports
import abfab3d.geom.*;
import abfab3d.grid.*;
import abfab3d.grid.op.*;
import abfab3d.io.output.BoxesX3DExporter;
import abfab3d.util.MatrixUtil;

/**
 * Linked Rings.
 *
 *
 * This example show how to start with a triangle mesh source and get
 * into a voxel form.
 *
 * @author Alan Hudson
 */
public class LinkedRings {
    /** Horiztonal resolution of the printer in meters.  */
    public static final double HORIZ_RESOLUTION = 0.001;
//    public static final double HORIZ_RESOLUTION = 0.00009;
//    public static final double HORIZ_RESOLUTION = 142e-6;   // 42 microns

    /** Verticle resolution of the printer in meters.  */
    public static final double VERT_RESOLUTION = 0.001;
//    public static final double VERT_RESOLUTION = 0.00009;
//    public static final double VERT_RESOLUTION = 116e-6;   // 16 microns

    public void generate(String filename) {
        try {
            ErrorReporter console = new PlainTextErrorReporter();

            long stime = System.currentTimeMillis();
            double ir = 0.002f;
            double or = 0.006f;
            int facets = 64;
            TorusGenerator tg = new TorusGenerator((float)ir, (float)or, facets, facets);
            GeometryData geom = new GeometryData();
            geom.geometryType = GeometryData.TRIANGLES;
            tg.generate(geom);

            int rings = 3;
            double bounds = TriangleModelCreator.findMaxBounds(geom);
            double size = rings * 2.1 * bounds;  // Slightly over allocate

System.out.println("voxels: " + (size / VERT_RESOLUTION));

            Grid grid = new ArrayAttributeGridByte(size,size,size,
//            Grid grid = new OctreeAttributeGridByte(size,size,size,
                HORIZ_RESOLUTION, VERT_RESOLUTION);


            TriangleModelCreator tmc = null;
            double x = bounds;
            double y = x;
            double z = x;

            double rx = 0,ry = 1,rz = 0,rangle = 0;
            int outerMaterial = 1;
            int innerMaterial = 1;

            TorusCreator tc = new TorusCreator(ir,or,x,y,z,rx,ry,rz,rangle,innerMaterial,outerMaterial);
//            SphereCreator tc = new SphereCreator(0.004,x,y,z,rx,ry,rz,rangle,innerMaterial,outerMaterial);

            grid = new RangeCheckWrapper(grid);

            tc.generate(grid);

            Matrix4d tmatrix = MatrixUtil.createMatrix(new double[] {x,y,z},
                new double[] {1,1,1}, new double[] {1,0,0,1.57075}, new double[] {0,0,0},
                new double[] {0,0,1,0});

            Operation op = new TransformPosition(tmatrix);
            grid = op.execute(grid);

            /*

            tmc = new TriangleModelCreator(geom,x,y,z,
                rx,ry,rz,rangle,outerMaterial,innerMaterial,true);

            tmc.generate(grid);

             x = 1 * bounds * 2.15;
             rx = 1;
             ry = 0;
             rz = 0;
             rangle = 1.57075;

             tmc = new TriangleModelCreator(geom,x,y,z,
                 rx,ry,rz,rangle,outerMaterial,innerMaterial,true);

             tmc.generate(grid);

            x = bounds * 3.2;
            rx = 1;
            ry = 0;
            rz = 0;
            rangle = 0;

            tmc = new TriangleModelCreator(geom,x,y,z,
                rx,ry,rz,rangle,outerMaterial,innerMaterial,true);

            tmc.generate(grid);
*/

            FileOutputStream fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".")+1);
            BoxesX3DExporter exporter = new BoxesX3DExporter(encoding, fos, console);

            System.out.println("Gen time: " + (System.currentTimeMillis() - stime));

            System.out.println("Writing x3d");
            stime = System.currentTimeMillis();

//            exporter.write(grid, null);

            HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
            colors.put(new Integer(Grid.INTERIOR), new float[] {0,1,0});
            colors.put(new Integer(Grid.EXTERIOR), new float[] {1,0,0});
            //colors.put(new Integer(Grid.OUTSIDE), new float[] {0,0,1});

            HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
            transparency.put(new Integer(Grid.INTERIOR), new Float(0));
            transparency.put(new Integer(Grid.EXTERIOR), new Float(0.5));
            //transparency.put(new Integer(Grid.OUTSIDE), new Float(1));

            exporter.writeDebug(grid, colors, transparency);

            exporter.close();

            System.out.println("GenX3D time: " + (System.currentTimeMillis() - stime));

            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        LinkedRings c = new LinkedRings();
        c.generate("out.x3db");
    }
}