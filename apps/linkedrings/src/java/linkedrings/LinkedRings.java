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
    public static final double RESOLUTION = 0.00025;

    public void generate(String filename) {
        try {
            ErrorReporter console = new PlainTextErrorReporter();

            double ir = 0.0005f;
            double or = 0.006f;
            int facets = 64;
            TorusGenerator tg = new TorusGenerator((float)ir, (float)or, facets, facets);
            GeometryData geom = new GeometryData();
            geom.geometryType = GeometryData.TRIANGLES;
            tg.generate(geom);

            int rings = 3;
            double bounds = TriangleModelCreator.findMaxBounds(geom);
            double size = rings * 2.1 * bounds;  // Slightly over allocate

            Grid grid = new ArrayAttributeGridByte(size,size,size,
                    RESOLUTION, RESOLUTION);


            System.out.println("Grid: dim: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());
            TriangleModelCreator tmc = null;
            double x = bounds;
            double y = x;
            double z = x;

            double rx = 0,ry = 1,rz = 0,rangle = 0;
            int outerMaterial = 1;
            int innerMaterial = 1;

            tmc = new TriangleModelCreator(geom,x,y,z,
                rx,ry,rz,rangle,outerMaterial,innerMaterial,true);

            tmc.generate(grid);

             x = bounds * 2.4;
             rx = 1;
             ry = 0;
             rz = 0;
             rangle = 1.57075;

             tmc = new TriangleModelCreator(geom,x,y,z,
                 rx,ry,rz,rangle,outerMaterial,innerMaterial,true);

             tmc.generate(grid);

            x = bounds * 3.8;
            rx = 1;
            ry = 0;
            rz = 0;
            rangle = 0;

            tmc = new TriangleModelCreator(geom,x,y,z,
                rx,ry,rz,rangle,outerMaterial,innerMaterial,true);

            tmc.generate(grid);

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

    public static void main(String[] args) {
        LinkedRings c = new LinkedRings();
        c.generate("out.x3db");
    }
}