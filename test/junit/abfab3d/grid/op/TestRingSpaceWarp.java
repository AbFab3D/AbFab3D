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

package abfab3d.grid.op;

// External Imports

import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.BaseTestAttributeGrid;
import abfab3d.grid.Grid;
import abfab3d.io.output.BoxesX3DExporter;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.export.X3DBinaryRetainedDirectExporter;
import org.web3d.vrml.export.X3DBinarySerializer;
import org.web3d.vrml.sav.BinaryContentHandler;

// Internal Imports

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

import static abfab3d.util.Output.printf;

/**
 * Tests the functionality of the RingSpaceWarp
 *
 * @author Tony Wong
 * @version
 */
public class TestRingSpaceWarp extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestRingSpaceWarp.class);
    }

    /**
     * Test equality of forward and backward conversion
     */
    public void testConvert1() {
        Grid grid = new ArrayAttributeGridByte(10,10,10,0.001,0.001);
        double r = 0.05;
        RingSpaceWarp warp = new RingSpaceWarp(grid,r);

        double EPS = 1e-10;

        printf("testConvert1()\n");
        int N  = 20;
        for(int i =0; i <= N; i++){
            double x = (i * 2*Math.PI/N - Math.PI) * r;
            //  keep x inside (-r*pi, r*pi)
            // keep z inside -r,infinity
            double[] coord = new double[] {x,0,5*r};
            double[] dest = new double[3];
            double[] dest2 = new double[3];

            warp.transform(coord, dest);
            printf("coord: [%7.4f, %7.4f, %7.4f] -> dest1: [%7.4f, %7.4f, %7.4f]\n", coord[0],coord[1],coord[2],dest[0],dest[1],dest[2]);
            warp.invert(dest, dest2);
            printf("dest2: [%7.4f, %7.4f, %7.4f]\n", dest2[0],dest2[1],dest2[2]);
            assertEquals("Same X", coord[0],dest2[0],EPS);
            assertEquals("Same Y", coord[1],dest2[1],EPS);
            assertEquals("Same Z", coord[2],dest2[2],EPS);

        }


        //assertEquals("Same X", coord[0],dest2[0],EPS);
        //assertEquals("Same Y", coord[1],dest2[1],EPS);
        //assertEquals("Same Z", coord[2],dest2[2],EPS);
    }

/*
    public void testConvertComplex() {
        Grid grid = new ArrayAttributeGridByte(10,10,10,0.001,0.001);
        double r = 0.05;
        RingSpaceWarp warp = new RingSpaceWarp(grid,r);

        double EPS = 1e-10;

        printf("testConvert1()\n");
        int N  = 20;
        for(int i =0; i <= N; i++){
            double x = (i * 2*Math.PI/N - Math.PI) * r;
            //  keep x inside (-r*pi, r*pi)
            // keep z inside -r,infinity
            double[] coord = new double[] {x,0,5*r};
            double[] dest = new double[3];
            double[] dest2 = new double[3];

            warp.transformComplex(coord, dest);
            printf("coord: [%7.4f, %7.4f, %7.4f] -> dest1: [%7.4f, %7.4f, %7.4f]\n", coord[0],coord[1],coord[2],dest[0],dest[1],dest[2]);
            warp.invertComplex(dest, dest2);
            printf("dest2: [%7.4f, %7.4f, %7.4f]\n", dest2[0],dest2[1],dest2[2]);
            assertEquals("Same X", coord[0],dest2[0],EPS);
            assertEquals("Same Y", coord[1],dest2[1],EPS);
            assertEquals("Same Z", coord[2],dest2[2],EPS);

        }
    }
  */

    public void _testTmp() {
        int size = 10;
        double r = size / (2.0 * Math.PI);
        AttributeGrid grid = new ArrayAttributeGridByte(size,1,size,1,1);

        RingSpaceWarp warp = new RingSpaceWarp(grid,r);

        int[] pos = new int[] {-4,0,-4};
        double EPS = 1e-10;
        double[] coord = new double[] {-4.5,0.5,-4.5};
        grid.getGridCoords(coord[0],coord[1],coord[2], pos);

        double[] dest = new double[3];
        double[] dest2 = new double[3];

        warp.transform(coord, dest);
        grid.getGridCoords(dest[0],dest[1],dest[2],pos);
        printf("coord: [%7.4f, %7.4f, %7.4f] -> dest1: [%7.4f, %7.4f, %7.4f]\n", coord[0],coord[1],coord[2],dest[0],dest[1],dest[2]);
        printf("pos: [%d, %d, %d]\n", pos[0],pos[1],pos[2]);
        warp.invert(dest, dest2);
        grid.getGridCoords(dest2[0],dest2[1],dest2[2],pos);
        printf("dest2: [%7.4f, %7.4f, %7.4f]\n", dest2[0],dest2[1],dest2[2]);
        printf("pos2: [%d, %d, %d]\n", pos[0],pos[1],pos[2]);
        assertEquals("Same X", coord[0],dest2[0],EPS);
        assertEquals("Same Y", coord[1],dest2[1],EPS);
        assertEquals("Same Z", coord[2],dest2[2],EPS);

    }

    /**
     * Test forward version
     */
    public void testForward() {
        int size = 15;
        AttributeGrid grid = new ArrayAttributeGridByte(size,1,size,1,1);
        AttributeGrid dest = new ArrayAttributeGridByte(size,1,size,1,1);
        double radius = size / (2.0 * Math.PI);

        System.out.println("Radius: " + radius);
        RingSpaceWarp warp = new RingSpaceWarp(grid,radius, true);

        for(int x=0; x < grid.getWidth(); x++) {
            grid.setData(x,0,0,Grid.INSIDE, x+1);
        }

        warp.execute(dest);
        System.out.println("Grid: \n" + grid.toStringAll());
        System.out.println("Dest: \n" + dest.toStringAll());

        try {
            ErrorReporter console = new PlainTextErrorReporter();
            FileOutputStream fos = new FileOutputStream("grid.x3db");
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            BinaryContentHandler writer = (BinaryContentHandler) new X3DBinaryRetainedDirectExporter(bos, 3, 0, console, X3DBinarySerializer.METHOD_FASTEST_PARSING, 0.001f, true);

            writeDebug(grid, writer, console);

            fos = new FileOutputStream("dest.x3db");
            bos = new BufferedOutputStream(fos);
            writer = (BinaryContentHandler) new X3DBinaryRetainedDirectExporter(bos, 3, 0, console, X3DBinarySerializer.METHOD_FASTEST_PARSING, 0.001f, true);
            writeDebug(dest, writer, console);
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Test forward version
     */
    public void testReverse() {
        int size = 15;
        AttributeGrid grid = new ArrayAttributeGridByte(size,1,size,1,1);
        AttributeGrid dest = new ArrayAttributeGridByte(size,1,size,1,1);
        double radius = size / (2.0 * Math.PI);

        System.out.println("Radius: " + radius);
        RingSpaceWarp warp = new RingSpaceWarp(grid,radius, false);

        for(int x=0; x < grid.getWidth(); x++) {
            grid.setData(x,0,0,Grid.INSIDE, x+1);
        }

        warp.execute(dest);
        System.out.println("Grid: \n" + grid.toStringAll());
        System.out.println("Dest: \n" + dest.toStringAll());

        try {
            ErrorReporter console = new PlainTextErrorReporter();
            FileOutputStream fos = new FileOutputStream("grid.x3db");
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            BinaryContentHandler writer = (BinaryContentHandler) new X3DBinaryRetainedDirectExporter(bos, 3, 0, console, X3DBinarySerializer.METHOD_FASTEST_PARSING, 0.001f, true);

            writeDebug(grid, writer, console);

            fos = new FileOutputStream("dest.x3db");
            bos = new BufferedOutputStream(fos);
            writer = (BinaryContentHandler) new X3DBinaryRetainedDirectExporter(bos, 3, 0, console, X3DBinarySerializer.METHOD_FASTEST_PARSING, 0.001f, true);
            writeDebug(dest, writer, console);
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    private void writeDebug(Grid grid, BinaryContentHandler handler, ErrorReporter console) {
        // Output File

        System.out.println("Writing debug output");
        BoxesX3DExporter exporter = new BoxesX3DExporter(handler, console,true);

        HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
        colors.put(new Integer(Grid.INSIDE), new float[] {1,0,0});
        colors.put(new Integer(Grid.INSIDE), new float[]{0, 1, 0});
        colors.put(new Integer(Grid.OUTSIDE), new float[] {0,0,1});

        HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
        transparency.put(new Integer(Grid.INSIDE), new Float(0));
        transparency.put(new Integer(Grid.INSIDE), new Float(0.5));
        transparency.put(new Integer(Grid.OUTSIDE), new Float(0.9));

        exporter.writeDebug(grid, colors, transparency);
        exporter.close();
    }

}


