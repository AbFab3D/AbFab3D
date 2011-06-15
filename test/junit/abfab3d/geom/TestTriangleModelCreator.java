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

package abfab3d.geom;

// External Imports
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.export.X3DBinaryRetainedDirectExporter;
import org.web3d.vrml.export.X3DBinarySerializer;

// Internal Imports
import abfab3d.grid.*;
import abfab3d.io.output.BoxesX3DExporter;

/**
 * Tests the functionality of TriangleModelCreator.
 *
 * @author Tony Wong
 * @version
 */
public class TestTriangleModelCreator extends TestCase {

    /** Horizontal resolution of the printer in meters.  */
    public static final double HORIZ_RESOLUTION = 0.002;

    /** Vertical resolution of the printer in meters.  */
    public static final double VERT_RESOLUTION = 0.001;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTriangleModelCreator.class);
    }

    /**
     * Test the voxelization of a simple cube.
     */
    public void testCube() {

        // Use 0.0999 instead of 0.01 voxelization is unpredictable when model
        // lines up exactly with a grid
        float width = 0.0999f;
        float height = 0.0999f;
        float depth = 0.0999f;

        BoxGenerator bg = new BoxGenerator(width, height, depth);
        GeometryData geom = new GeometryData();
        geom.geometryType = GeometryData.TRIANGLES;
        bg.generate(geom);

System.out.println("coord count: " + geom.coordinates.length);
System.out.println("coords: " + java.util.Arrays.toString(geom.coordinates));

        double bounds = findMaxBounds(geom);
System.out.println("geometry bounds: " + bounds);

        // twice the bounds (since centered at origin) plus a slight over allocate
        int gWidth = (int) (width / HORIZ_RESOLUTION) + 10;
        int gHeight = (int) (height / VERT_RESOLUTION) + 10;
        int gDepth = (int) (depth / HORIZ_RESOLUTION) + 10;

        Grid grid = new ArrayGrid(gWidth, gHeight, gDepth, HORIZ_RESOLUTION, VERT_RESOLUTION);

        double x = bounds;
        double y = x;
        double z = x;

        double rx = 0,ry = 1,rz = 0,rangle = 0;
        byte outerMaterial = 1;
        byte innerMaterial = 1;

        TriangleModelCreator tmc = null;
        tmc = new TriangleModelCreator(geom,x,y,z,
            rx,ry,rz,rangle,outerMaterial,innerMaterial,true);

        tmc.generate(grid);

System.out.println("grid dimensions: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());


        int xVoxels = (int) Math.round(width / HORIZ_RESOLUTION);
        int yVoxels = (int) Math.round(height / VERT_RESOLUTION);
        int zVoxels = (int) Math.round(depth / HORIZ_RESOLUTION);
        int expectedMatCount = xVoxels * yVoxels * zVoxels;

        assertEquals("Material count is not " + expectedMatCount, expectedMatCount, grid.findCount(outerMaterial));


    }

    /**
     * Test the voxelization of a simple cube.
     */
    public void testIndexedCube() {

        // Use 0.0999 instead of 0.01 voxelization is unpredictable when model
        // lines up exactly with a grid
        float width = 0.0999f;
        float height = 0.0999f;
        float depth = 0.0999f;

        BoxGenerator bg = new BoxGenerator(width, height, depth);
        GeometryData geom = new GeometryData();
        geom.geometryType = GeometryData.INDEXED_TRIANGLES;
        bg.generate(geom);

System.out.println("indexes: " + java.util.Arrays.toString(geom.indexes));
System.out.println("coord count: " + geom.coordinates.length);
System.out.println("coords: " + java.util.Arrays.toString(geom.coordinates));

        double bounds = findMaxBounds(geom);

        // twice the bounds (since centered at origin) plus a slight over allocate
        int gWidth = (int) (width / HORIZ_RESOLUTION) + 10;
        int gHeight = (int) (height / VERT_RESOLUTION) + 10;
        int gDepth = (int) (depth / HORIZ_RESOLUTION) + 10;

        Grid grid = new ArrayGrid(gWidth, gHeight, gDepth, HORIZ_RESOLUTION, VERT_RESOLUTION);

        double x = bounds;
        double y = x;
        double z = x;

        double rx = 0,ry = 1,rz = 0,rangle = 0;
        byte outerMaterial = 1;
        byte innerMaterial = 1;

        TriangleModelCreator tmc = null;
        tmc = new TriangleModelCreator(geom,x,y,z,
            rx,ry,rz,rangle,outerMaterial,innerMaterial,true);

        tmc.generate(grid);

System.out.println("grid dimensions: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());


        int xVoxels = (int) Math.round(width / HORIZ_RESOLUTION);
        int yVoxels = (int) Math.round(height / VERT_RESOLUTION);
        int zVoxels = (int) Math.round(depth / HORIZ_RESOLUTION);
        int expectedMatCount = xVoxels * yVoxels * zVoxels;

        assertEquals("Material count is not " + expectedMatCount, expectedMatCount, grid.findCount(outerMaterial));


    }

    /**
     * Find the absolute maximum bounds of a geometry.
     *
     * @return The max
     */
    private static double findMaxBounds(GeometryData geom) {
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
     * Generate an X3D file
     * @param filename
     */
    public static void generate(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ErrorReporter console = new PlainTextErrorReporter();

//            int method = X3DBinarySerializer.METHOD_SMALLEST_NONLOSSY;
            int method = X3DBinarySerializer.METHOD_FASTEST_PARSING;

            X3DBinaryRetainedDirectExporter writer = new X3DBinaryRetainedDirectExporter(fos,
                                                         3, 0, console,method, 0.001f);

            long stime = System.currentTimeMillis();
            // Use 0.0999 instead of 0.01 voxelization is unpredictable when model
            // lines up exactly with a grid
            float width = 0.0999f;
            float height = 0.0999f;
            float depth = 0.0999f;

            BoxGenerator bg = new BoxGenerator(width, height, depth);
            GeometryData geom = new GeometryData();
            geom.geometryType = GeometryData.TRIANGLES;
            bg.generate(geom);

            double bounds = findMaxBounds(geom);
    System.out.println("geometry bounds: " + bounds);

            // twice the bounds (since centered at origin) plus a slight over allocate
            int gWidth = (int) (width / HORIZ_RESOLUTION) + 10;
            int gHeight = (int) (height / VERT_RESOLUTION) + 10;
            int gDepth = (int) (depth / HORIZ_RESOLUTION) + 10;

            Grid grid = new ArrayGrid(gWidth, gHeight, gDepth, HORIZ_RESOLUTION, VERT_RESOLUTION);

            double x = bounds;
            double y = x;
            double z = x;

            double rx = 0,ry = 1,rz = 0,rangle = 0;
            byte outerMaterial = 1;
            byte innerMaterial = 1;

            TriangleModelCreator tmc = null;
            tmc = new TriangleModelCreator(geom,x,y,z,
                rx,ry,rz,rangle,outerMaterial,innerMaterial,true);

            tmc.generate(grid);

    System.out.println("grid dimensions: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());
    System.out.println("mat count: " + grid.findCount((byte) 1));

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

            System.out.println("Writing x3d");
            stime = System.currentTimeMillis();

            BoxesX3DExporter exporter = new BoxesX3DExporter();
            exporter.toX3D(grid, writer, null);

            System.out.println("GenX3D time: " + (System.currentTimeMillis() - stime));
            System.out.println("End doc");
            stime = System.currentTimeMillis();
            writer.endDocument();
            System.out.println("EndDoc time: " + (System.currentTimeMillis() - stime));

            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        generate("out.x3db");
    }
}
