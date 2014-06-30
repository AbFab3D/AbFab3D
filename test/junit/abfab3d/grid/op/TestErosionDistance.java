/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Units.MM;

// Internal Imports

/**
 * Tests the functionality of DilationDistance
 *
 * @author Alan Hudson
 * @version
 */
public class TestErosionDistance extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestErosionDistance.class);
    }

    /**
     * Test basic operation
     */
    public void testRows() {
        int size = 10;
        int subvoxelResolution = 100;
        double vs = 0.1 * MM;

        int steps = 2;
        int alpha = 80;
        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,vs,vs);

        for(int y=5-steps; y <= 5+steps; y++) {
            for(int z=5-steps; z <= 5+steps; z++) {
                grid.setAttribute(5-steps-1,y,z,alpha);
                for(int x=5-steps; x <= 5+steps; x++) {
                    grid.setAttribute(x, y, z, subvoxelResolution);
                }
                grid.setAttribute(5+steps+1,y,z,alpha);
            }
        }

        for(int i=5-steps; i <= 5 + steps; i++) {
            printGrid(grid, i, "Original");
        }

        double distance = 1 * vs;

        ErosionDistance ec = new ErosionDistance(distance,subvoxelResolution);
        AttributeGrid erodedGrid = ec.execute(grid);

        for(int i=5-steps; i <= 5 + steps; i++) {
            printGrid(erodedGrid, i, "ErodedGrid");
        }
    }

    /**
     * Test basic operation
     */
    public void testDilateErode() {
        int size = 10;
        int subvoxelResolution = 100;
        double vs = 0.1 * MM;

        int steps = 1;
        int steps1 = steps+1;
        int alpha = 80;
        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,vs,vs);

        for(int y=5-steps; y <= 5+steps; y++) {
            for(int z=5-steps; z <= 5+steps; z++) {
                grid.setAttribute(5-steps-1,y,z,alpha);
                for(int x=5-steps; x <= 5+steps; x++) {
                    grid.setAttribute(x, y, z, subvoxelResolution);
                }
                grid.setAttribute(5+steps+1,y,z,alpha);
            }
        }

        for(int i=5-steps; i <= 5 + steps; i++) {
            printGrid(grid, i, "Original");
        }

        double distance = steps * vs;

        DilationDistance dc = new DilationDistance(distance,subvoxelResolution);
        AttributeGrid dilatedGrid = dc.execute(grid);

        for(int i=5-steps1; i <= 5 + steps1; i++) {
            printGrid(dilatedGrid, i, "Dilated");
        }

        ErosionDistance ec = new ErosionDistance(distance,subvoxelResolution);
        AttributeGrid erodedGrid = ec.execute(dilatedGrid);

        for(int i=5-steps; i <= 5 + steps; i++) {
            printGrid(erodedGrid, i, "Final");
        }
    }

    public void printGrid(AttributeGrid grid, int y, String label) {
        printf("%s\n", label);
        for (int z = 0; z < grid.getDepth(); z++) {
            printf("%3d ", z);
        }
        printf("\n");
        for (int z = 0; z < grid.getDepth(); z++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                printf("%3d ", grid.getAttribute(x, y, z));
            }
            printf("\n");
        }
    }
    /*
    public void testSphere2D() {
        int size = 10;
        int subvoxelResolution = 100;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        grid.setAttribute(5,3,4,20);
        grid.setAttribute(5,4,4,20);
        grid.setAttribute(5,2,5,20);
        grid.setAttribute(5,3,5,subvoxelResolution);
        grid.setAttribute(5,4,5,subvoxelResolution);
        grid.setAttribute(5,5,5,20);
        grid.setAttribute(5,3,6,20);
        grid.setAttribute(5,4,6,20);

        int distance = 1;

        DilationDistance ec = new DilationDistance(distance,subvoxelResolution);
        AttributeGrid dilatedGrid = ec.execute(grid);

        assertEquals(20,dilatedGrid.getAttribute(5,2,3));
        assertEquals(20,dilatedGrid.getAttribute(5,3,3));
        assertEquals(20,dilatedGrid.getAttribute(5,4,3));
        assertEquals(20,dilatedGrid.getAttribute(5,5,3));

        assertEquals(20,dilatedGrid.getAttribute(5,1,4));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,2,4));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,3,4));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,4,4));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,5,4));
        assertEquals(20,dilatedGrid.getAttribute(5,6,4));

        assertEquals(20,dilatedGrid.getAttribute(5,1,5));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,2,5));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,3,5));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,4,5));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,5,5));
        assertEquals(20,dilatedGrid.getAttribute(5,6,5));

        assertEquals(20,dilatedGrid.getAttribute(5,1,6));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,2,6));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,3,6));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,4,6));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,5,6));
        assertEquals(20,dilatedGrid.getAttribute(5,6,6));

        assertEquals(20,dilatedGrid.getAttribute(5,2,7));
        assertEquals(20,dilatedGrid.getAttribute(5,3,7));
        assertEquals(20,dilatedGrid.getAttribute(5,4,7));
        assertEquals(20,dilatedGrid.getAttribute(5,5,7));

    }

    public void testBoxOverlap() {
        int size = 10;
        int subvoxelResolution = 100;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        grid.setAttribute(5,2,3,100);
        grid.setAttribute(5,3,3,20);
        grid.setAttribute(5,2,4,100);
        grid.setAttribute(5,3,4,20);
        grid.setAttribute(5,2,5,100);
        grid.setAttribute(5,3,5,20);

        grid.setAttribute(5,5,3,20);
        grid.setAttribute(5,6,3,100);
        grid.setAttribute(5,5,4,20);
        grid.setAttribute(5,6,4,100);
        grid.setAttribute(5,5,5,20);
        grid.setAttribute(5,6,5,100);

        int distance = 1;

        DilationDistance ec = new DilationDistance(distance,subvoxelResolution);
        AttributeGrid dilatedGrid = ec.execute(grid);

        assertEquals(40,dilatedGrid.getAttribute(5,4,2));
        assertEquals(40,dilatedGrid.getAttribute(5,4,3));
        assertEquals(40,dilatedGrid.getAttribute(5,4,4));
        assertEquals(40,dilatedGrid.getAttribute(5,4,5));
        assertEquals(40,dilatedGrid.getAttribute(5,4,6));

        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,1,2));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,1,3));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,1,4));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,1,5));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,1,6));

        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,2,2));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,2,3));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,2,4));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,2,5));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,2,6));

        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,3,2));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,3,3));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,3,4));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,3,5));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,3,6));

        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,5,2));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,5,3));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,5,4));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,5,5));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,5,6));

        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,6,2));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,6,3));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,6,4));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,6,5));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,6,6));

        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,7,2));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,7,3));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,7,4));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,7,5));
        assertEquals(subvoxelResolution,dilatedGrid.getAttribute(5,7,6));

    }

    public void testDistance() {
        int size = 15;
        int subvoxelResolution = 255;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        grid.setAttribute(5,5,5,20);
        grid.setAttribute(5,5,6,subvoxelResolution);
        grid.setAttribute(5,5,7,20);

        grid.setAttribute(5,5,17,20);
        grid.setAttribute(5,5,18,subvoxelResolution);
        grid.setAttribute(5,5,19,20);

        int distance = 4;

        DilationDistance ec = new DilationDistance(distance,subvoxelResolution);
        AttributeGrid dilatedGrid = ec.execute(grid);

        assertEquals("5,5,12 value",0,dilatedGrid.getAttribute(5,5,12));
        assertEquals("5,5,11 value",20,dilatedGrid.getAttribute(5,5,11));
        assertEquals("5,5,13 value",20,dilatedGrid.getAttribute(5,5,13));
    }

*/
    //---------------------------------------------------
    // Functions for writing out an dilated object
    //---------------------------------------------------

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
        TestErosionDistance ec = new TestErosionDistance();
//        ec.dilateCube();
//        ec.dilateTorus();
    }
}
