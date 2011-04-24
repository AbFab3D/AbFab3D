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
import org.web3d.vrml.sav.ContentHandler;
import org.web3d.vrml.export.*;
import org.web3d.util.ErrorReporter;

import org.j3d.geom.GeometryData;
import org.j3d.geom.TorusGenerator;

// Internal Imports
import abfab3d.geom.*;
import abfab3d.geom.CubeCreator.Style;
import abfab3d.grid.*;


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
    public static final double HORIZ_RESOLUTION = 0.0001;
//    public static final double HORIZ_RESOLUTION = 142e-6;   // 42 microns

    /** Verticle resolution of the printer in meters.  */
    public static final double VERT_RESOLUTION = 0.0001;
//    public static final double VERT_RESOLUTION = 116e-6;   // 16 microns

    public void generate(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ErrorReporter console = new PlainTextErrorReporter();

            X3DBinaryRetainedDirectExporter writer = new X3DBinaryRetainedDirectExporter(fos,
                                                         3, 0, console,
                                                         X3DBinarySerializer.METHOD_SMALLEST_NONLOSSY,
                                                         0.001f);

            long stime = System.currentTimeMillis();
            float ir = 0.001f;
            float or = 0.004f;
            int facets = 64;
            TorusGenerator tg = new TorusGenerator(ir, or, facets, facets);
            GeometryData geom = new GeometryData();
            geom.geometryType = GeometryData.TRIANGLES;
            tg.generate(geom);

            int rings = 3;
            double bounds = findMaxBounds(geom);
            double size = rings * 2.1 * bounds;  // Slightly over allocate

            Grid grid = new SliceGrid(size,size,size,
                HORIZ_RESOLUTION, VERT_RESOLUTION, false);

            TriangleModelCreator tmc = null;
            double x = bounds;
            double y = x;
            double z = x;

            double rx = 0,ry = 1,rz = 0,rangle = 0;
            byte outerMaterial = 1;
            byte innerMaterial = 1;


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

            x = bounds * 3.2;
            rx = 1;
            ry = 0;
            rz = 0;
            rangle = 0;

            tmc = new TriangleModelCreator(geom,x,y,z,
                rx,ry,rz,rangle,outerMaterial,innerMaterial,false);

            tmc.generate(grid);

            System.out.println("Gen time: " + (System.currentTimeMillis() - stime));
            writer.startDocument("","", "utf8", "#X3D", "V3.0", "");
            writer.profileDecl("Immersive");
            writer.startNode("NavigationInfo", null);
            writer.startField("avatarSize");
            writer.fieldValue(new float[] {0.01f, 1.6f, 0.75f}, 3);
            writer.endNode(); // NavigationInfo
            writer.startNode("Viewpoint", null);
            writer.startField("position");
            writer.fieldValue(new float[] {0.028791402f,0.005181627f,0.11549001f},3);
            writer.startField("orientation");
            writer.fieldValue(new float[] {-0.06263941f,0.78336f,0.61840385f,0.31619227f},4);
            writer.endNode(); // Viewpoint

            grid.toX3D(writer, null);
            writer.endDocument();

            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
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
        LinkedRings c = new LinkedRings();
        c.generate("out.x3db");
    }
}