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
import abfab3d.core.VoxelClasses;
import abfab3d.io.output.BoxesX3DExporter;

/**
 * Tests the functionality of DilationCube Operation
 *
 * @author Tony Wong
 * @version
 */
public class TestInteriorFinderVoxelBased extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestInteriorFinderVoxelBased.class);
    }

    /**
     * Test basic operation
     */
    public void testInteriorFinderCube() {
        long mat = 1;
        int cubeVoxelsX = 16;
        int cubeVoxelsY = 10;
        int cubeVoxelsZ = 6;

        AttributeGrid grid = new ArrayAttributeGridByte(cubeVoxelsX+3,cubeVoxelsY+3,cubeVoxelsZ+3,0.001, 0.001, StoredInsideOutsideFuncFactory.create(1,6));

        // Just set every voxel to attribute 1
        for (int x=0; x<grid.getWidth(); x++) {
            for (int y=0; y<grid.getHeight(); y++) {
                for (int z=0; z<grid.getDepth(); z++) {
                    grid.setAttribute(x, y, z, mat);
                }
            }
        }

        generateCubeExteriors(grid, cubeVoxelsX, cubeVoxelsY, cubeVoxelsZ, 1, 1, 1);
//        generate(grid, "cube_IFVB.x3db");

        int expectedExteriors = getCubeExteriorVoxelCount(cubeVoxelsX, cubeVoxelsY, cubeVoxelsZ);
        assertEquals(expectedExteriors, grid.findCount(VoxelClasses.INSIDE));

    }

    //---------------------------------------------------
    // Functions for writing out an dilated object
    //---------------------------------------------------

    private void generateCubeExteriors(AttributeGrid grid, int xVoxels, int yVoxels, int zVoxels, int startX, int startY, int startZ) {
        int endX = startX + xVoxels - 1;
        int endY = startY + yVoxels - 1;
        int endZ = startZ + zVoxels - 1;

        assertTrue(endX < grid.getWidth());
        assertTrue(endY < grid.getHeight());
        assertTrue(endZ < grid.getDepth());

        for (int x=startX; x<endX; x++) {
            for (int y=startY; y<endY; y++) {
                grid.setState(x, y, startZ, Grid.INSIDE);
                grid.setState(x, y, endZ, Grid.INSIDE);
            }
        }

        for (int x=startX; x<=endX; x++) {
            for (int z=startZ; z<=endZ; z++) {
                grid.setState(x, startY, z, Grid.INSIDE);
                grid.setState(x, endY, z, Grid.INSIDE);
            }
        }

        for (int y=startY; y<=endY; y++) {
            for (int z=startZ; z<=endZ; z++) {
                grid.setState(startX, y, z, Grid.INSIDE);
                grid.setState(endX, y, z, Grid.INSIDE);
            }
        }

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

    /**
     * Get the total number of exterior voxels of a cube.
     *
     * @param xVoxels Number of voxels in the x direction
     * @param yVoxels Number of voxels in the y direction
     * @param zVoxels Number of voxels in the z direction
     * @return The number of exterior voxels of the cube
     */
    private int getCubeExteriorVoxelCount(int xVoxels, int yVoxels, int zVoxels) {
        // expected number of exterior voxels should be the following formula:
        //   (exteriorVoxels per face in XY plane * 2) +
        //   (exteriorVoxels per face in XZ plane * 2) +
        //   (exteriorVoxels per face in YZ plane * 2) -
        //   (numEdges in X dir * gridWidth) -
        //   (numEdges in Y dir * gridHeight) -
        //   (numEdges in Z dir * gridDepth) +
        //   (number of corners)
        return (xVoxels * yVoxels * 2) + (xVoxels * zVoxels * 2) + (yVoxels * zVoxels * 2) -
               (4 * xVoxels) - (4 * yVoxels) - (4 * zVoxels) + 8;
    }

}
