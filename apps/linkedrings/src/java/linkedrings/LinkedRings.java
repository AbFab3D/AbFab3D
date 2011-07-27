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

// Internal Imports
import abfab3d.geom.*;
import abfab3d.grid.*;
import abfab3d.io.output.BoxesX3DExporter;


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
    public static final double HORIZ_RESOLUTION = 0.00003;
//    public static final double HORIZ_RESOLUTION = 142e-6;   // 42 microns

    /** Verticle resolution of the printer in meters.  */
    public static final double VERT_RESOLUTION = 0.00003;
//    public static final double VERT_RESOLUTION = 116e-6;   // 16 microns

    public void generate(String filename) {
        try {
            ErrorReporter console = new PlainTextErrorReporter();

            long stime = System.currentTimeMillis();
            float ir = 0.001f;
            float or = 0.004f;
            int facets = 64;
            TorusGenerator tg = new TorusGenerator(ir, or, facets, facets);
            GeometryData geom = new GeometryData();
            geom.geometryType = GeometryData.TRIANGLES;
            tg.generate(geom);

            int rings = 3;
            double bounds = TriangleModelCreator.findMaxBounds(geom);
            double size = rings * 2.1 * bounds;  // Slightly over allocate

System.out.println("voxels: " + (size / VERT_RESOLUTION));

//            Grid grid = new ArrayGridByteIndexLong(size,size,size,
            Grid grid = new OctreeGridByte(size,size,size,
                HORIZ_RESOLUTION, VERT_RESOLUTION);

            TriangleModelCreator tmc = null;
            double x = bounds;
            double y = x;
            double z = x;

            double rx = 0,ry = 1,rz = 0,rangle = 0;
            int outerMaterial = 1;
            int innerMaterial = 1;


            tmc = new TriangleModelCreator(geom,x,y,z,
                rx,ry,rz,rangle,outerMaterial,innerMaterial,false);

            tmc.generate(grid);

             x = 1 * bounds * 2.15;
             rx = 1;
             ry = 0;
             rz = 0;
             rangle = 1.57075;

             tmc = new TriangleModelCreator(geom,x,y,z,
                 rx,ry,rz,rangle,outerMaterial,innerMaterial,false);

             tmc.generate(grid);
/*
// Start test code

            Grid grid2 = new ArrayGridByte(grid.getWidth(), grid.getHeight(), grid.getDepth(),
                HORIZ_RESOLUTION, VERT_RESOLUTION);



            tmc.generate(grid2);

            abfab3d.grid.query.Equals op = new abfab3d.grid.query.Equals(grid2);
            if (!op.execute(grid)) {
                System.out.println("Grids not equal!");
            } else {
                System.out.println("Grids equal.");
            }

// End Test Code
*/
            x = bounds * 3.2;
            rx = 1;
            ry = 0;
            rz = 0;
            rangle = 0;

            tmc = new TriangleModelCreator(geom,x,y,z,
                rx,ry,rz,rangle,outerMaterial,innerMaterial,false);

            tmc.generate(grid);

            FileOutputStream fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".")+1);
            BoxesX3DExporter exporter = new BoxesX3DExporter(encoding, fos, console);

            System.out.println("Gen time: " + (System.currentTimeMillis() - stime));

            System.out.println("Writing x3d");
            stime = System.currentTimeMillis();

            exporter.write(grid, null);
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