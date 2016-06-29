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

import abfab3d.core.AttributeGrid;
import abfab3d.core.Grid;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;

import junit.framework.Test;
import junit.framework.TestSuite;

// Internal Imports
import abfab3d.grid.*;
import abfab3d.io.output.BoxesX3DExporter;

/**
 * Tests the functionality of the SetDifference Operation
 *
 * @author Tony Wong
 * @version
 */
public class TestSetDifference extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestSetDifference.class);
    }

    /**
     * Test basic operation
     */
    public void testBasic() {
        int size = 10;

        AttributeGrid grid1 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);
        AttributeGrid grid2 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        // set left rump of dumbbell for both grid
        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                grid1.setData(2, y, z, Grid.INSIDE, 1);
                grid2.setData(2, y, z, Grid.INSIDE, 1);
            }
        }

        // set right rump of dumbbell for both grid
        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                grid1.setData(7, y, z, Grid.INSIDE, 1);
                grid2.setData(7, y, z, Grid.INSIDE, 1);
            }
        }

        // set connecting bar for grid1 only
        for (int x=3; x<7; x++) {
            grid1.setData(x, 5, 5, Grid.INSIDE, 1);
        }

        // get the set difference of grid1 and grid2
        SetDifference sd = new SetDifference(grid1, grid2);
        Grid diffGrid = sd.execute(null);

        // set difference grid should be outside at left rump coordinates
        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                assertEquals("(2, " + y + ", " + z + ") state is not " + Grid.OUTSIDE,
                        Grid.OUTSIDE, diffGrid.getState(2, y, z));
            }
        }

        // set difference grid should be outside at right rump coordinates
        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                assertEquals("(2, " + y + ", " + z + ") state is not " + Grid.OUTSIDE,
                        Grid.OUTSIDE, diffGrid.getState(7, y, z));
            }
        }

        // set difference grid should have the connecting bar
        for (int x=3; x<7; x++) {
            assertEquals("(" + x + ", 5, 5) state is not " + Grid.INSIDE,
                    Grid.INSIDE, diffGrid.getState(x, 5, 5));
        }

    }

    /**
     * Test basic operation with a new material
     */
    public void testBasicNewMaterial() {
        int size = 10;
        int newMaterial = 5;

        AttributeGrid grid1 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);
        AttributeGrid grid2 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        // set left rump of dumbbell for both grid
        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                grid1.setData(2, y, z, Grid.INSIDE, 1);
                grid2.setData(2, y, z, Grid.INSIDE, 1);
            }
        }

        // set right rump of dumbbell for both grid
        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                grid1.setData(7, y, z, Grid.INSIDE, 1);
                grid2.setData(7, y, z, Grid.INSIDE, 1);
            }
        }

        // set connecting bar for grid1 only
        for (int x=3; x<7; x++) {
            grid1.setData(x, 5, 5, Grid.INSIDE, 1);
        }

        // get the set difference of grid1 and grid2
        SetDifference sd = new SetDifference(grid1, grid2, newMaterial);
        AttributeGrid diffGrid = sd.execute(null);

        // set difference grid should be outside at left rump coordinates
        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                assertEquals("(2, " + y + ", " + z + ") state is not " + Grid.OUTSIDE,
                        Grid.OUTSIDE, diffGrid.getState(2, y, z));
            }
        }

        // set difference grid should be outside at right rump coordinates
        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                assertEquals("(2, " + y + ", " + z + ") state is not " + Grid.OUTSIDE,
                        Grid.OUTSIDE, diffGrid.getState(7, y, z));
            }
        }

        // set difference grid should have the connecting bar
        for (int x=3; x<7; x++) {
            assertEquals("(" + x + ", 5, 5) state is not " + Grid.INSIDE,
                    Grid.INSIDE, diffGrid.getState(x, 5, 5));
            assertEquals("(" + x + ", 5, 5) material is not " + newMaterial,
                    newMaterial, diffGrid.getAttribute(x, 5, 5));
        }

    }

    //---------------------------------------------------
    // Functions for writing out an dilated object
    //---------------------------------------------------

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

    private void cubeErodeDilateDumbBell() {
        Grid originalGrid = generateDumbBell();

        generate(originalGrid, "preCubeErosionDilationOfDumbBell.x3db");

        int distance = 2;

        // erode the grid
        ErosionCube es = new ErosionCube(distance);
        Grid erodedGrid = es.execute(originalGrid);
        generate(erodedGrid, "postCubeErosionOfDumbBell_radius" + distance + ".x3db");

        // dilate the eroded grid
        DilationCube ec = new DilationCube(distance);
        Grid dilatedGrid = ec.execute(erodedGrid);
        generate(dilatedGrid, "postCubeDilationOfDumbBell_distance" + distance + ".x3db");

        SetDifference sd = new SetDifference(originalGrid, dilatedGrid);
        AttributeGrid diffGridTheta = sd.execute(null);
        generate(diffGridTheta, "diffThetaOfCubeMorphDumbBell_distance" + distance + ".x3db");

    }

    private void sphereErodeDilateDumbBell() {
        Grid originalGrid = generateDumbBell();

        generate(originalGrid, "preSphereErosionDilationOfDumbBell.x3db");

        int distance = 2;

        // erode the grid
        ErosionSphere es = new ErosionSphere(distance);
        Grid erodedGrid = es.execute(originalGrid);
        generate(erodedGrid, "postSphereErosionOfDumbBell_radius" + distance + ".x3db");

        // dilate the eroded grid
        DilationSphere ec = new DilationSphere(distance);
        Grid dilatedGrid = ec.execute(erodedGrid);
        generate(dilatedGrid, "postSphereDilationOfDumbBell_distance" + distance + ".x3db");

        SetDifference sd = new SetDifference(originalGrid, dilatedGrid);
        AttributeGrid diffGridTheta = sd.execute(null);
        generate(diffGridTheta, "diffThetaOfSphereMorphDumbBell_distance" + distance + ".x3db");

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
        TestSetDifference ec = new TestSetDifference();

//        ec.cubeErodeDilateDumbBell();
        ec.sphereErodeDilateDumbBell();
    }

}
