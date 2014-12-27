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
 * Tests the functionality of DilationSphere Operation
 *
 * @author Tony Wong
 * @version
 */
public class TestDilationSphere extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDilationSphere.class);
    }

    /**
     * Test basic operation
     */
    public void testBasic() {
        int size = 10;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                setX(grid, y, z, Grid.INSIDE, 1, 2, 7);
            }
        }

        int distance = 1;

        DilationSphere ec = new DilationSphere(distance);
        Grid dilatedGrid = ec.execute(grid);

        int width = dilatedGrid.getWidth();
        int height = dilatedGrid.getHeight();
        int depth = dilatedGrid.getDepth();
        for (int y=0; y<height; y++) {
            for (int z=0; z<depth; z++) {
                for (int x=0; x<width; x++) {
                    System.out.println(x + ", " + y + ", " + z + ": " + dilatedGrid.getState(x, y, z));
                }
            }
        }

        distance = 2;

        ec = new DilationSphere(distance);
        dilatedGrid = ec.execute(grid);

        width = dilatedGrid.getWidth();
        height = dilatedGrid.getHeight();
        depth = dilatedGrid.getDepth();
        for (int y=0; y<height; y++) {
            for (int z=0; z<depth; z++) {
                for (int x=0; x<width; x++) {
                    System.out.println(x + ", " + y + ", " + z + ": " + dilatedGrid.getState(x, y, z));
                }
            }
        }

    }

    //---------------------------------------------------
    // Functions for writing out an dilated object
    //---------------------------------------------------

    private Grid generateCube() {
        int size = 10;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        for (int y=5; y<6; y++) {
            for (int z=5; z<6; z++) {
                setX(grid, y, z, Grid.INSIDE, 1, 0, size-1);
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

        Grid grid = new ArrayAttributeGridByte(new Bounds(size,size,size),0.0005, 0.0005);

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
                setX(grid, y, z, Grid.INSIDE, 1, 0, 4);
            }
        }

        // right cube
        for (int y=0; y<size; y++) {
            for (int z=0; z<size; z++) {
                setX(grid, y, z, Grid.INSIDE, 1, 15, size-1);
            }
        }

        // the bridge
        int bridgeThickness = 3;
        int bridgeHeight = size / 2;
        int bridgeDepth = size / 2;

        for (int y=bridgeHeight; y<bridgeHeight+bridgeThickness; y++) {
            for (int z=bridgeDepth; z<bridgeDepth+bridgeThickness; z++) {
                setX(grid, y, z, Grid.INSIDE, 1, 4, 15);
            }
        }

        return grid;
    }

    private Grid dilate(Grid grid, int distance) {
        DilationSphere ec = new DilationSphere(distance);
        Grid dilatedGrid = ec.execute(grid);

        return dilatedGrid;
    }

    private void dilateCube() {
        Grid originalGrid = generateCube();

        generate(originalGrid, "preSphereDilationOfCube.x3db");

        int distance = 1;
        Grid dilatedGrid = dilate(originalGrid, distance);
        generate(dilatedGrid, "postSphereDilationOfCube_distance" + distance + ".x3db");

        distance = 2;
        dilatedGrid = dilate(originalGrid, distance);;
        generate(dilatedGrid, "postSphereDilationOfCube_distance" + distance + ".x3db");

    }

    private void dilateTorus() {
        Grid originalGrid = generateTorus();

        generate(originalGrid, "preSphereDilationOfTorus.x3db");

        int distance = 1;
        Grid dilatedGrid = dilate(originalGrid, distance);
        generate(dilatedGrid, "postSphereDilationOfTorus_distance" + distance + ".x3db");

        distance = 2;
        dilatedGrid = dilate(originalGrid, distance);;
        generate(dilatedGrid, "postSphereDilationOfTorus_distance" + distance + ".x3db");
    }

    private void dilateDumbBell() {
        Grid originalGrid = generateDumbBell();

        generate(originalGrid, "preSphereDilationOfDumbBell.x3db");

        int distance = 1;
        Grid dilatedGrid = dilate(originalGrid, distance);
        generate(dilatedGrid, "postSphereDilationOfDumbBell_distance" + distance + ".x3db");

        distance = 2;
        dilatedGrid = dilate(originalGrid, distance);;
        generate(dilatedGrid, "postSphereDilationOfDumbBell_distance" + distance + ".x3db");

    }

    private void erodeDilateDumbBell() {
        Grid originalGrid = generateDumbBell();

        generate(originalGrid, "preSphereErosionDilationOfDumbBell.x3db");

        int distance = 2;

        // erode the grid
        ErosionSphere es = new ErosionSphere(distance);
        Grid erodedGrid = es.execute(originalGrid);
        generate(erodedGrid, "postSphereErosionOfDumbBell_radius" + distance + ".x3db");

        // dilate the eroded grid
        Grid dilatedGrid = dilate(erodedGrid, distance);
        generate(dilatedGrid, "postSphereDilationOfDumbBell_distance" + distance + ".x3db");

    }

    private void erodeDilateTorus() {
        Grid originalGrid = generateTorus();

        generate(originalGrid, "preSphereErosionDilationOfTorus.x3db");

        int distance = 2;

        // erode the grid
        ErosionSphere es = new ErosionSphere(distance);
        Grid erodedGrid = es.execute(originalGrid);
        generate(erodedGrid, "postSphereErosionOfTorus_radius" + distance + ".x3db");

        // dilate the eroded grid
        Grid dilatedGrid = dilate(erodedGrid, distance);
        generate(dilatedGrid, "postSphereDilationOfTorus_distance" + distance + ".x3db");

    }

    private void generate(Grid grid, String filename) {
        try {
            ErrorReporter console = new PlainTextErrorReporter();

            FileOutputStream fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".")+1);
            BoxesX3DExporter exporter = new BoxesX3DExporter(encoding, fos, console);

            HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
            colors.put(new Integer(Grid.INSIDE), new float[] {0,1,0});
            colors.put(new Integer(Grid.INSIDE), new float[] {1,0,0});
            colors.put(new Integer(Grid.OUTSIDE), new float[] {0,1,1});

            HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
            transparency.put(new Integer(Grid.INSIDE), new Float(0));
            transparency.put(new Integer(Grid.INSIDE), new Float(0.5));
            transparency.put(new Integer(Grid.OUTSIDE), new Float(0.8));

            exporter.writeDebug(grid, colors, transparency);

            exporter.close();

            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }


    public static void main(String[] args) {
        TestDilationSphere ec = new TestDilationSphere();
//        ec.dilateCube();
        ec.dilateTorus();
        ec.dilateDumbBell();
//      ec.erodeDilateDumbBell();
//      ec.erodeDilateTorus();
    }
}
