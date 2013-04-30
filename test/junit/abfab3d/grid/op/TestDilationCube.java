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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.j3d.geom.GeometryData;
import org.j3d.geom.TorusGenerator;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;

import junit.framework.Test;
import junit.framework.TestSuite;

// Internal Imports
import abfab3d.geom.TorusCreator;
import abfab3d.geom.TriangleModelCreator;
import abfab3d.grid.*;
import abfab3d.io.output.BoxesX3DExporter;

/**
 * Tests the functionality of DilationCube Operation
 *
 * @author Tony Wong
 * @version
 */
public class TestDilationCube extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDilationCube.class);
    }

    /**
     * Test basic operation
     */
    public void testBasic() {
        int size = 10;
        long material = 1;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                setX(grid, y, z, Grid.INTERIOR, material, 2, 7);
            }
        }

        int distance = 1;

        DilationCube ec = new DilationCube(distance);
        Grid dilatedGrid = ec.execute(grid);

        int width = dilatedGrid.getWidth();
        int height = dilatedGrid.getHeight();
        int depth = dilatedGrid.getDepth();
        for (int y=0; y<height; y++) {
            for (int z=0; z<depth; z++) {
                for (int x=0; x<width; x++) {
                    byte state = dilatedGrid.getState(x, y, z);
//                    System.out.println(x + ", " + y + ", " + z + ": " + state);

                    if (y >= 2 && y < 10) {
                        if (z >=2 && z < 10) {
                            if (x >= 2 && x < 10) {
                                assertEquals("State of (" + x + " " + y + " " + z + " is not interior",
                                        Grid.INTERIOR, state);
                            } else {
                                assertEquals("State of (" + x + " " + y + " " + z + " is not outside",
                                        Grid.OUTSIDE, state);
                            }
                        } else {
                            assertEquals("State of (" + x + " " + y + " " + z + " is not outside",
                                    Grid.OUTSIDE, state);
                        }
                      } else {
                        assertEquals("State of (" + x + " " + y + " " + z + " is not outside",
                                Grid.OUTSIDE, state);
                    }

                }
            }
        }
/*
        distance = 2;

        ec = new DilationCube(distance);
        dilatedGrid = ec.execute(grid);

        width = dilatedGrid.getWidth();
        height = dilatedGrid.getHeight();
        depth = dilatedGrid.getDepth();
        for (int y=0; y<height; y++) {
            for (int z=0; z<depth; z++) {
                for (int x=0; x<width; x++) {
                    byte state = dilatedGrid.getState(x, y, z);
//                    System.out.println(x + ", " + y + ", " + z + ": " + state);

                    if (y >= 2 && y < 8) {
                        if (z >=2 && y < 8) {
                            if (x >= 2 && x < 8) {
                                assertEquals(Grid.INTERIOR, state);
                            } else {
                                assertEquals(Grid.OUTSIDE, state);
                            }
                        } else {
                            assertEquals(Grid.OUTSIDE, state);
                        }
                    } else {
                        assertEquals(Grid.INTERIOR, state);
                    }
                }
            }
        }
*/
    }

    //---------------------------------------------------
    // Functions for writing out an dilated object
    //---------------------------------------------------

    private Grid generateCube() {
        int size = 10;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        for (int y=5; y<6; y++) {
            for (int z=5; z<6; z++) {
                setX(grid, y, z, Grid.INTERIOR, 1, 0, size-1);
            }
        }

        return grid;
    }

    private Grid generateTorus() {
        double ir = 0.002f;
        double or = 0.006f;
        int facets = 64;
        TorusGenerator tg = new TorusGenerator((float)ir, (float)or, facets, facets);
        GeometryData geom = new GeometryData();
        geom.geometryType = GeometryData.TRIANGLES;
        tg.generate(geom);

        double bounds = TriangleModelCreator.findMaxBounds(geom);
        double size = 2.1 * bounds;  // Slightly over allocate

        Grid grid = new ArrayAttributeGridByte(size,size,size,0.0005, 0.0005);

        double x = bounds;
        double y = x;
        double z = x;

        double rx = 0,ry = 1,rz = 0,rangle = 0;
        int outerMaterial = 1;
        int innerMaterial = 1;

        TorusCreator tc = new TorusCreator(ir,or,x,y,z,rx,ry,rz,rangle,innerMaterial,outerMaterial);

        grid = new RangeCheckWrapper(grid);

        tc.generate(grid);

        return grid;
    }

    /**
     * Generate a dumbbell using two cubes and a connecting length of filled voxels.
     *
     * @return Grid containing the dumbbell-filled voxels
     */
    private Grid generateDumbBell() {
        int size = 20;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        // left cube
        for (int y=0; y<size; y++) {
            for (int z=0; z<size; z++) {
                setX(grid, y, z, Grid.INTERIOR, 1, 0, 4);
            }
        }

        // right cube
        for (int y=0; y<size; y++) {
            for (int z=0; z<size; z++) {
                setX(grid, y, z, Grid.INTERIOR, 1, 15, size-1);
            }
        }

        // the bridge
        int bridgeThickness = 3;
        int bridgeHeight = size / 2;
        int bridgeDepth = size / 2;

        for (int y=bridgeHeight; y<bridgeHeight+bridgeThickness; y++) {
            for (int z=bridgeDepth; z<bridgeDepth+bridgeThickness; z++) {
                setX(grid, y, z, Grid.INTERIOR, 1, 4, 15);
            }
        }

        return grid;
    }

    private Grid dilate(Grid grid, int distance) {
        DilationCube ec = new DilationCube(distance);
        Grid dilatedGrid = ec.execute(grid);

        return dilatedGrid;
    }

    private void dilateCube() {
        Grid originalGrid = generateCube();

        generate(originalGrid, "preCubeDilationOfCube.x3db");

        int distance = 1;
        Grid dilatedGrid = dilate(originalGrid, distance);
        generate(dilatedGrid, "postCubeDilationOfCube_distance" + distance + ".x3db");

        distance = 2;
        dilatedGrid = dilate(originalGrid, distance);;
        generate(dilatedGrid, "postCubeDilationOfCube_distance" + distance + ".x3db");

    }

    private void dilateTorus() {
        Grid originalGrid = generateTorus();

        generate(originalGrid, "preCubeDilationOfTorus.x3db");

        int distance = 1;
        Grid dilatedGrid = dilate(originalGrid, distance);
        generate(dilatedGrid, "postCubeDilationOfTorus_distance" + distance + ".x3db");

        distance = 2;
        dilatedGrid = dilate(originalGrid, distance);;
        generate(dilatedGrid, "postCubeDilationOfTorus_distance" + distance + ".x3db");
    }

    private void dilateDumbBell() {
        TestDilationCube ec = new TestDilationCube();
        Grid originalGrid = ec.generateDumbBell();

        ec.generate(originalGrid, "preCubeDilationOfDumbBell.x3db");

        int distance = 1;
        Grid dilatedGrid = ec.dilate(originalGrid, distance);
        ec.generate(dilatedGrid, "postCubeDilationOfDumbBell_distance" + distance + ".x3db");

        distance = 2;
        dilatedGrid = ec.dilate(originalGrid, distance);;
        ec.generate(dilatedGrid, "postCubeDilationOfDumbBell_distance" + distance + ".x3db");

    }

    private void generate(Grid grid, String filename) {
        try {
            ErrorReporter console = new PlainTextErrorReporter();

            FileOutputStream fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".")+1);
            BoxesX3DExporter exporter = new BoxesX3DExporter(encoding, fos, console);

            HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
            colors.put(new Integer(Grid.INTERIOR), new float[] {0,1,0});
            colors.put(new Integer(Grid.EXTERIOR), new float[] {1,0,0});
            colors.put(new Integer(Grid.OUTSIDE), new float[] {0,1,1});

            HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
            transparency.put(new Integer(Grid.INTERIOR), new Float(0));
            transparency.put(new Integer(Grid.EXTERIOR), new Float(0.5));
            transparency.put(new Integer(Grid.OUTSIDE), new Float(0.8));

            exporter.writeDebug(grid, colors, transparency);

            exporter.close();

            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }


    public static void main(String[] args) {
        TestDilationCube ec = new TestDilationCube();
//        ec.dilateCube();
//        ec.dilateTorus();
        ec.dilateDumbBell();
    }
}
