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
public class TestSetUnion extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestSetUnion.class);
    }

    /**
     * Test basic operation
     */
    public void testBasic() {
        int size = 10;

        AttributeGrid grid1 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);
        AttributeGrid grid2 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);
        AttributeGrid grid3 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        // set left rump of dumbbell for grid1
        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                grid1.setData(2, y, z, Grid.INTERIOR, 1);
            }
        }

        // set right rump of dumbbell for grid2
        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                grid2.setData(7, y, z, Grid.INTERIOR, 1);
            }
        }

        // set connecting bar for grid3
        for (int x=3; x<7; x++) {
            grid3.setData(x, 5, 5, Grid.INTERIOR, 1);
        }

        AttributeGrid[] grids = {grid1, grid2, grid3};

        // get the set difference of grid1 and grid2
        SetUnion union = new SetUnion(grids);
        AttributeGrid finalGrid = union.execute(null);

        // final grid should have
        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                assertTrue("(2, " + y + ", " + z + ") state is not interior or exterior" ,
                        finalGrid.getState(2, y, z) == Grid.EXTERIOR || finalGrid.getState(2, y, z) == Grid.INTERIOR);
            }
        }

        // set difference grid should be outside at right rump coordinates
        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                assertTrue("(7, " + y + ", " + z + ") state is not interior or exterior",
                        finalGrid.getState(7, y, z) == Grid.EXTERIOR || finalGrid.getState(7, y, z) == Grid.INTERIOR);
            }
        }

        // set difference grid should have the connecting bar
        for (int x=3; x<7; x++) {
            assertTrue("(" + x + ", 5, 5) state is not interior",
                    finalGrid.getState(x, 5, 5) == Grid.EXTERIOR || finalGrid.getState(x, 5, 5) == Grid.INTERIOR);
        }

    }

    /**
     * Test basic operation with a new material
     */
    public void testBasicNewMaterial() {
        int size = 10;
        long material = 50;

        AttributeGrid grid1 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);
        AttributeGrid grid2 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);
        AttributeGrid grid3 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        // set left rump of dumbbell for grid1
        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                grid1.setData(2, y, z, Grid.INTERIOR, 1);
            }
        }

        // set right rump of dumbbell for grid2
        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                grid2.setData(7, y, z, Grid.INTERIOR, 1);
            }
        }

        // set connecting bar for grid3
        for (int x=3; x<7; x++) {
            grid3.setData(x, 5, 5, Grid.INTERIOR, 1);
        }

        AttributeGrid[] grids = {grid1, grid2, grid3};

        // get the set difference of grid1 and grid2
        SetUnion union = new SetUnion(grids, material);
        AttributeGrid finalGrid = union.execute(null);

        // final grid should have
        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                assertTrue("(2, " + y + ", " + z + ") state is not interior or exterior" ,
                        finalGrid.getState(2, y, z) == Grid.EXTERIOR || finalGrid.getState(2, y, z) == Grid.INTERIOR);

                assertTrue("(2, " + y + ", " + z + ") material is not " + material + " but was " + finalGrid.getAttribute(2,y,z),
                        (finalGrid.getAttribute(2, y, z)) == material);
            }
        }

        // set difference grid should be outside at right rump coordinates
        for (int y=2; y<8; y++) {
            for (int z=2; z<8; z++) {
                assertTrue("(7, " + y + ", " + z + ") state is not interior or exterior",
                        finalGrid.getState(7, y, z) == Grid.EXTERIOR || finalGrid.getState(7, y, z) == Grid.INTERIOR);

                assertTrue("(7, " + y + ", " + z + ") material is not " + material ,
                        (finalGrid.getAttribute(7, y, z)) == material);
            }
        }

        // set difference grid should have the connecting bar
        for (int x=3; x<7; x++) {
            assertTrue("(" + x + ", 5, 5) state is not interior",
                    finalGrid.getState(x, 5, 5) == Grid.EXTERIOR || finalGrid.getState(x, 5, 5) == Grid.INTERIOR);

            assertTrue("(" + x + ", 5, 5) material is not " + material ,
                    (finalGrid.getAttribute(x, 5, 5)) == material);
        }
    }

    //---------------------------------------------------
    // Functions for writing out an dilated object
    //---------------------------------------------------

    /**
     * Generate a dumbbell using two cubes and a connecting length of filled voxels
     * in different grids, and then unioning them.
     *
     * @return Grid containing the dumbbell-filled voxels
     */
    private AttributeGrid generateDumbBell() {
        int size = 20;

        AttributeGrid grid1 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);
        AttributeGrid grid2 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);
        AttributeGrid grid3 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        // left cube
        for (int y=0; y<size; y++) {
            for (int z=0; z<size; z++) {
                setX(grid1, y, z, Grid.INTERIOR, 1, 0, 4);
            }
        }

        // right cube
        for (int y=0; y<size; y++) {
            for (int z=0; z<size; z++) {
                setX(grid2, y, z, Grid.INTERIOR, 1, 15, size-1);
            }
        }

        // the bridge
        int bridgeThickness = 3;
        int bridgeHeight = size / 2;
        int bridgeDepth = size / 2;

        for (int y=bridgeHeight; y<bridgeHeight+bridgeThickness; y++) {
            for (int z=bridgeDepth; z<bridgeDepth+bridgeThickness; z++) {
                setX(grid3, y, z, Grid.INTERIOR, 1, 4, 15);
            }
        }

        AttributeGrid[] grids = {grid1, grid2, grid3};

        SetUnion union = new SetUnion(grids);
        AttributeGrid finalGrid = union.execute(null);

        return finalGrid;
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
        TestSetUnion ec = new TestSetUnion();

        AttributeGrid grid = ec.generateDumbBell();
        ec.generate(grid, "unionGrid.x3db");
    }

}
