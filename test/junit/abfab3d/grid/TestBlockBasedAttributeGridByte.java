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

package abfab3d.grid;

// External Imports
import java.util.HashSet;
import java.util.Iterator;

import abfab3d.grid.Grid.VoxelClasses;
import junit.framework.Test;
import junit.framework.TestSuite;

// Internal Imports

/**
 * Tests the functionality of a BlockBasedAttributeGridByte.
 *
 * @author Alan Hudson
 * @version
 */
public class TestBlockBasedAttributeGridByte extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestBlockBasedAttributeGridByte.class);
    }

    /**
     * Test the reverse mapping functions
     */
    public void testReverseMappingFunctionsEqual() {
        int w = 8;
        int h = 8;
        int d = 8;
        int blockOrder = 2;

        BlockBasedAttributeGridByte grid = new BlockBasedAttributeGridByte(w, h, d, 0.001, 0.001,blockOrder);

        int[] bcoord = new int[3];
        int[] vcoord = new int[3];

        for(int y=0; y < h; y++) {
            for(int x=0; x < w; x++) {
                for(int z=0; z < d; z++) {

                    // Find block coord
                    grid.getBlockCoord(x, y, z, bcoord);

                    // Inline getBlockID call, confirm faster?
                    int bid = grid.getBlockID(bcoord);

                    grid.getVoxelInBlock(x, y, z, vcoord);

                    int vid = grid.getID(vcoord);

                    //System.out.println("voxel: " + x + " " + y + " " + z + " bid: " + bid + " vid: " + vid + " calced: " + java.util.Arrays.toString(vcoord));

                    grid.getVoxelCoord(bid, vid, vcoord);

                    //System.out.println("calced : " + java.util.Arrays.toString(vcoord));
                    //System.out.println("correct: " + x + " " + y + " " + z);
                    //System.out.println();
                    assertEquals("x coord incorrect", x, vcoord[0]);
                    assertEquals("y coord incorrect", y, vcoord[1]);
                    assertEquals("z coord incorrect", z, vcoord[2]);
                }
            }
        }
    }

    /**
     * Test the reverse mapping functions
     */
    public void testReverseMappingFunctionsNonEqual() {
        int w = 16;
        int h = 8;
        int d = 8;
        int blockOrder = 2;

        BlockBasedAttributeGridByte grid = new BlockBasedAttributeGridByte(w, h, d, 0.001, 0.001,blockOrder);

        int[] bcoord = new int[3];
        int[] vcoord = new int[3];

        for(int y=0; y < h; y++) {
            for(int x=0; x < w; x++) {
                for(int z=0; z < d; z++) {

                    // Find block coord
                    grid.getBlockCoord(x, y, z, bcoord);

                    // Inline getBlockID call, confirm faster?
                    int bid = grid.getBlockID(bcoord);

                    grid.getVoxelInBlock(x, y, z, vcoord);

                    int vid = grid.getID(vcoord);

                    //System.out.println("voxel: " + x + " " + y + " " + z + " bid: " + bid + " vid: " + vid + " calced: " + java.util.Arrays.toString(vcoord));

                    grid.getVoxelCoord(bid, vid, vcoord);

                    //System.out.println("calced : " + java.util.Arrays.toString(vcoord));
                    //System.out.println("correct: " + x + " " + y + " " + z);
                    //System.out.println();
                    assertEquals("x coord incorrect", x, vcoord[0]);
                    assertEquals("y coord incorrect", y, vcoord[1]);
                    assertEquals("z coord incorrect", z, vcoord[2]);
                }
            }
        }
    }

    /**
     * Test copy constructor.
     */
    public void testBlockBasedGridByteCopyConstructor() {
        BlockBasedAttributeGridByte grid = new BlockBasedAttributeGridByte(10, 9, 8, 0.001, 0.001);

        grid.setData(0, 0, 0, Grid.INTERIOR, 2);
        grid.setData(9, 8, 7, Grid.EXTERIOR, 1);
        grid.setData(5, 0, 7, Grid.INTERIOR, 0);

        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0, 0, 0));
        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(9, 8, 7));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(5, 0, 7));

        BlockBasedAttributeGridByte grid2 = new BlockBasedAttributeGridByte(grid);

        assertEquals("State should be ", Grid.INTERIOR, grid2.getState(0, 0, 0));
        assertEquals("State should be ", Grid.EXTERIOR, grid2.getState(9, 8, 7));
        assertEquals("State should be ", Grid.INTERIOR, grid2.getState(5, 0, 7));
        assertEquals("State should be ", Grid.OUTSIDE, grid2.getState(0, 0, 1));

        assertEquals("Material should be ", 2, grid2.getAttribute(0, 0, 0));
        assertEquals("Material should be ", 1, grid2.getAttribute(9, 8, 7));
        assertEquals("Material should be ", 0, grid2.getAttribute(5, 0, 7));
    }

    /**
     * Test creating an empty grid.
     */
    public void testCreateEmpty() {
        AttributeGrid grid =new BlockBasedAttributeGridByte(100, 101, 102, 0.001, 0.001);

        grid.setData(5, 5, 5, Grid.EXTERIOR, 10);

        AttributeGrid grid2 =(AttributeGrid) grid.createEmpty(10, 11, 12, 0.002, 0.003);

        assertTrue("Grid type is not BlockBasedAttributeGridByte", grid2 instanceof BlockBasedAttributeGridByte);
        assertEquals("Grid voxel size is not 0.002", 0.002, grid2.getVoxelSize());
        assertEquals("Grid slice height is not 0.003", 0.003, grid2.getSliceHeight());

        // all voxels in empty grid should be OUTSIDE state and 0 material
        assertEquals("State is not OUTSIDE for (5, 5, 5)", Grid.OUTSIDE, grid2.getState(5, 5, 5));
        assertEquals("Material is not 0 for (5, 5, 5)", 0, grid2.getAttribute(5, 5, 5));
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGetByVoxelCoords() {
        AttributeGrid grid =new BlockBasedAttributeGridByte(8, 8, 8, 0.001, 0.001, 1);
        setGetAllVoxelCoords(grid);

        grid = new BlockBasedAttributeGridByte(8, 8, 8, 0.001, 0.001, 2);
        setGetAllVoxelCoords(grid);

        grid = new BlockBasedAttributeGridByte(8, 8, 8, 0.001, 0.001, 3);
        setGetAllVoxelCoords(grid);

        grid = new BlockBasedAttributeGridByte(16,8,8,0.001, 0.001, 2);
        setGetAllVoxelCoords(grid);

        grid = new BlockBasedAttributeGridByte(16, 16, 16, 0.001, 0.001, 2);
        setGetAllVoxelCoords(grid);

        double vs = 0.001;

        grid = new BlockBasedAttributeGridByte(100 * vs, 91 * vs, 85 * vs, 0.001, 0.001, 2);
        setGetAllVoxelCoords(grid);
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGetByWorldCoords() {
        AttributeGrid grid =new BlockBasedAttributeGridByte(8, 8, 8, 0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);

        grid = new BlockBasedAttributeGridByte(3,2,2,0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);

        grid = new BlockBasedAttributeGridByte(11, 11, 11, 0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);

        grid = new BlockBasedAttributeGridByte(100, 91, 85, 0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);
    }

    /**
     * Test getState by voxels.
     */
    public void testGetStateByVoxel() {
        AttributeGrid grid = new BlockBasedAttributeGridByte(10, 9, 9, 0.001, 0.001);
        grid.setData(0, 0, 0, Grid.OUTSIDE, (byte)2);
        grid.setData(9, 8, 7, Grid.EXTERIOR, (byte)1);
        grid.setData(5, 0, 7, Grid.INTERIOR, (byte)0);

        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0, 0, 0));
        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(9, 8, 7));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(5, 0, 7));

        // Index that are not set should default to 0
        assertEquals("State should be ", 0, grid.getState(8, 8, 8));
    }

    /**
     * Test getState by world coordinates.
     */
    public void testGetStateByCoord() {
        AttributeGrid grid =new BlockBasedAttributeGridByte(1.0, 0.4, 0.5, 0.05, 0.01);

        // set and test get on some random world coordinates
        grid.setData(0.0, 0.0, 0.0, Grid.OUTSIDE, (byte)2);
        grid.setData(0.95, 0.39, 0.45, Grid.EXTERIOR, (byte)1);
        grid.setData(0.6, 0.1, 0.4, Grid.INTERIOR, (byte)0);
        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0.0, 0.0, 0.0));
        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(0.95, 0.39, 0.45));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.6, 0.1, 0.4));

        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid = new BlockBasedAttributeGridByte(0.12, 0.11, 0.16, 0.05, 0.02);
        grid.setData(0.06, 0.07, 0.08, Grid.INTERIOR, (byte)2);
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.05, 0.06, 0.05));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.0999, 0.06, 0.05));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.05, 0.0799, 0.05));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.05, 0.06, 0.0999));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.0999, 0.0799, 0.0999));
        assertEquals("State should be ", 0, grid.getState(0.0499, 0.06, 0.05));
        assertEquals("State should be ", 0, grid.getState(0.05, 0.0599, 0.05));
        assertEquals("State should be ", 0, grid.getState(0.05, 0.06, 0.0499));
        assertEquals("State should be ", 0, grid.getState(0.1, 0.06, 0.05));
        assertEquals("State should be ", 0, grid.getState(0.05, 0.08, 0.05));
        assertEquals("State should be ", 0, grid.getState(0.05, 0.06, 0.1));

        // set data for beginning voxel 0,0,0 and test the bounds
        grid.setData(0.0, 0.0, 0.0, Grid.INTERIOR, (byte)2);
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.0, 0.0, 0.0));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.0499, 0.0, 0.0));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.0, 0.0199, 0.0));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.0, 0.0, 0.0499));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.0499, 0.0199, 0.0499));
        assertEquals("State should be ", 0, grid.getState(0.05, 0.0, 0.0));
        assertEquals("State should be ", 0, grid.getState(0.0, 0.02, 0.0));
        assertEquals("State should be ", 0, grid.getState(0.0, 0.0, 0.05));

        // set data for last voxel 2,5,3 and test the bounds
        grid.setData(0.149, 0.119, 0.199, Grid.INTERIOR, (byte)2);
//        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.1, 0.1, 0.15));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.149, 0.1, 0.151));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.1, 0.119, 0.151));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.1, 0.1, 0.199));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.149, 0.119, 0.199));
        assertEquals("State should be ", 0, grid.getState(0.0999, 0.1, 0.1501));
        assertEquals("State should be ", 0, grid.getState(0.1, 0.0999, 0.1501));
        assertEquals("State should be ", 0, grid.getState(0.1, 0.1, 0.1499));
        assertEquals("State should be ", 0, grid.getState(0.0999, 0.0999, 0.1499));
/*
//        System.out.println("0.1, 0.1, 0.15: " + grid.getState(0.1, 0.1, 0.151));
//        System.out.println("0.15, 0.119, 0.199: " + grid.getState(0.15, 0.119, 0.199));

        int[] coords = new int[3];
        grid.getGridCoords(0.0, 0.0, 0.0, coords);
        System.out.println(coords[0] + ", " + coords[1] + ", " + coords[2]);
        grid.getGridCoords(0.05, 0.0, 0.0, coords);
        System.out.println(coords[0] + ", " + coords[1] + ", " + coords[2]);
        grid.getGridCoords(0.1, 0.0, 0.0, coords);
        System.out.println(coords[0] + ", " + coords[1] + ", " + coords[2]);
        grid.getGridCoords(0.15, 0.0, 0.0, coords);
        System.out.println(coords[0] + ", " + coords[1] + ", " + coords[2]);
*/
    }

    /**
     * Test getAttribute by voxels.
     */
    public void testGetMaterialByVoxel() {
        AttributeGrid grid =new BlockBasedAttributeGridByte(10, 9, 8, 0.001, 0.001);

        // Removed outside as it doesn't have to carry state
        //grid.setData(0, 0, 0, Grid.OUTSIDE, (byte)3);
        grid.setData(9, 8, 7, Grid.EXTERIOR, (byte)2);
        grid.setData(5, 0, 7, Grid.INTERIOR, (byte)1);

        assertEquals("Material should be ", 2, grid.getAttribute(9, 8, 7));
        assertEquals("Material should be ", 1, grid.getAttribute(5, 0, 7));

        // Index that are not set should default to 0
        assertEquals("Material should be ", 0, grid.getAttribute(8, 8, 8));
    }

    /**
     * Test getAttribute by world coordinates.
     */
    public void testGetMaterialByCoord() {
        AttributeGrid grid =new BlockBasedAttributeGridByte(1.0, 0.4, 0.5, 0.05, 0.01);

        // set and test get on some random world coordinates
        grid.setData(0.0, 0.0, 0.0, Grid.OUTSIDE, (byte)3);
        grid.setData(0.95, 0.39, 0.45, Grid.EXTERIOR, (byte)2);
        grid.setData(0.6, 0.1, 0.4, Grid.INTERIOR, (byte)1);
        assertEquals("State should be ", 3, grid.getAttribute(0.0, 0.0, 0.0));
        assertEquals("State should be ", 2, grid.getAttribute(0.95, 0.39, 0.45));
        assertEquals("State should be ", 1, grid.getAttribute(0.6, 0.1, 0.4));

        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid = new BlockBasedAttributeGridByte(0.12, 0.11, 0.16, 0.05, 0.02);
        grid.setData(0.06, 0.07, 0.08, Grid.INTERIOR, (byte)2);
        assertEquals("State should be ", 2, grid.getAttribute(0.05, 0.06, 0.05));
        assertEquals("State should be ", 2, grid.getAttribute(0.0999, 0.06, 0.05));
        assertEquals("State should be ", 2, grid.getAttribute(0.05, 0.0799, 0.05));
        assertEquals("State should be ", 2, grid.getAttribute(0.05, 0.06, 0.0999));
        assertEquals("State should be ", 2, grid.getAttribute(0.0999, 0.0799, 0.0999));
        assertEquals("State should be ", 0, grid.getAttribute(0.0499, 0.06, 0.05));
        assertEquals("State should be ", 0, grid.getAttribute(0.05, 0.0599, 0.05));
        assertEquals("State should be ", 0, grid.getAttribute(0.05, 0.06, 0.0499));
        assertEquals("State should be ", 0, grid.getAttribute(0.1, 0.06, 0.05));
        assertEquals("State should be ", 0, grid.getAttribute(0.05, 0.08, 0.05));
        assertEquals("State should be ", 0, grid.getAttribute(0.05, 0.06, 0.1));

        // set data for beginning voxel 0,0,0 and test the bounds
        grid.setData(0.0, 0.0, 0.0, Grid.INTERIOR, (byte)5);
        assertEquals("State should be ", 5, grid.getAttribute(0.0, 0.0, 0.0));
        assertEquals("State should be ", 5, grid.getAttribute(0.0499, 0.0, 0.0));
        assertEquals("State should be ", 5, grid.getAttribute(0.0, 0.0199, 0.0));
        assertEquals("State should be ", 5, grid.getAttribute(0.0, 0.0, 0.0499));
        assertEquals("State should be ", 5, grid.getAttribute(0.0499, 0.0199, 0.0499));
        assertEquals("State should be ", 0, grid.getAttribute(0.05, 0.0, 0.0));
        assertEquals("State should be ", 0, grid.getAttribute(0.0, 0.02, 0.0));
        assertEquals("State should be ", 0, grid.getAttribute(0.0, 0.0, 0.05));

        // set data for last voxel 2,5,3 and test the bounds
        grid.setData(0.149, 0.119, 0.199, Grid.INTERIOR, (byte)12);
//        assertEquals("State should be ", 12, grid.getAttribute(0.1, 0.1, 0.15)); //failing because 0.15/0.05=2.999997
        assertEquals("State should be ", 12, grid.getAttribute(0.1499, 0.1, 0.1501));
        assertEquals("State should be ", 12, grid.getAttribute(0.1, 0.119, 0.1501));
        assertEquals("State should be ", 12, grid.getAttribute(0.1, 0.1, 0.199));
        assertEquals("State should be ", 12, grid.getAttribute(0.1499, 0.1199, 0.1999));
        assertEquals("State should be ", 0, grid.getAttribute(0.0999, 0.1, 0.1501));
        assertEquals("State should be ", 0, grid.getAttribute(0.1, 0.0999, 0.1501));
        assertEquals("State should be ", 0, grid.getAttribute(0.1, 0.1, 0.1499));
        assertEquals("State should be ", 0, grid.getAttribute(0.0999, 0.0999, 0.1499));
    }


    /**
     * Test set/get byte material range.
     */
    public void testByteMaterialRange() {
        int width = 100;
        int maxMaterial = 64;
        long mat, expectedMat;

        AttributeGrid grid =new BlockBasedAttributeGridByte(width, 1, 1, 0.001, 0.001);

        for (int x=0; x<width; x++) {
            grid.setData(x, 0, 0, Grid.EXTERIOR, x);
        }

        for (int x=0; x<width; x++) {
            mat = grid.getAttribute(x, 0, 0);
            expectedMat = x % maxMaterial;
//System.out.println("Material [" + x + ",0,0]: " + mat);
            assertEquals("Material [" + x + ",0,0] is not " + expectedMat, expectedMat, mat);
        }
    }

    /**
     * Test findCount by voxel class.
     */
    public void testFindCountByVoxelClass() {
        int width = 6;
        int height = 3;
        int depth = 10;
        int[] row = {0, 3, 5};
        byte[] state = {Grid.INTERIOR, Grid.EXTERIOR, Grid.INTERIOR};

        AttributeGrid grid =new BlockBasedAttributeGridByte(width, height, depth, 0.05, 0.02);

        width = grid.getWidth();
        height = grid.getHeight();
        depth = grid.getDepth();

        // set some rows to interior and exterior
        for (int y=0; y<height; y++) {
            for (int z=0; z<depth; z++) {
                grid.setData(row[0], y, z, state[0], (byte)2);
                grid.setData(row[1], y, z, state[1], (byte)2);
                grid.setData(row[2], y, z, state[2], (byte)2);
            }
        }

        int expectedAllCount = width * depth * height;
        int expectedIntCount = depth * height * 2;
        int expectedExtCount = depth * height;
        int expectedMrkCount = expectedIntCount + expectedExtCount;
        int expectedOutCount = expectedAllCount - expectedIntCount - expectedExtCount;

        assertEquals("Expected total voxels is not " + expectedAllCount, expectedAllCount, grid.findCount(VoxelClasses.ALL));
        assertEquals("Expected interior voxels is not " + expectedIntCount, expectedIntCount, grid.findCount(VoxelClasses.INTERIOR));
        assertEquals("Expected exterior voxels is not " + expectedExtCount, expectedExtCount, grid.findCount(VoxelClasses.EXTERIOR));
        assertEquals("Expected marked voxels is not " + expectedMrkCount, expectedMrkCount, grid.findCount(VoxelClasses.MARKED));
        assertEquals("Expected outside voxels is not " + expectedOutCount, expectedOutCount, grid.findCount(VoxelClasses.OUTSIDE));

        // change one of the interior voxel rows to outside
        for (int y=0; y<height; y++) {
            for (int z=0; z<depth; z++) {
                grid.setData(row[0], y, z, Grid.OUTSIDE, (byte)2);
            }
        }

        expectedIntCount = depth * height;
        expectedExtCount = depth * height;
        expectedMrkCount = expectedIntCount + expectedExtCount;
        expectedOutCount = expectedAllCount - expectedIntCount - expectedExtCount;

        assertEquals("Expected total voxels is not " + expectedAllCount, expectedAllCount, grid.findCount(VoxelClasses.ALL));
        assertEquals("Expected interior voxels is not " + expectedIntCount, expectedIntCount, grid.findCount(VoxelClasses.INTERIOR));
        assertEquals("Expected exterior voxels is not " + expectedExtCount, expectedExtCount, grid.findCount(VoxelClasses.EXTERIOR));
        assertEquals("Expected marked voxels is not " + expectedMrkCount, expectedMrkCount, grid.findCount(VoxelClasses.MARKED));
        assertEquals("Expected outside voxels is not " + expectedOutCount, expectedOutCount, grid.findCount(VoxelClasses.OUTSIDE));
    }

    /**
     * Test findCount by material.
     */
    public void testFindCountByMat() {
        int width = 3;
        int height = 4;
        int depth = 10;
        long material0 = 2;
        long material1 = 5;
        long material2 = 12;
        int[] materialDepth = {10, 6, 1};
        long[] material = {material0, material1, material2};

        AttributeGrid grid =new BlockBasedAttributeGridByte(width, height, depth, 0.05, 0.02);

        width = grid.getWidth();
        height = grid.getHeight();
        depth = grid.getDepth();

        // set some material data
        for (int x=0; x<material.length; x++){
            for (int y=0; y<height; y++) {
                for (int z=0; z<materialDepth[x]; z++) {
//System.out.println(x + ", " + y + ", " + z + ": " + material[x]);
                    grid.setData(x, y, z, Grid.INTERIOR, material[x]);
                }
            }
        }

        int[] expectedCount = new int[material.length];

        for (int j=0; j<material.length; j++) {
            expectedCount[j] = materialDepth[j] * height;
//System.out.println("count: " + expectedCount[j]);
            assertEquals("Material count for " + material[j] + " is not " + expectedCount[j], expectedCount[j], grid.findCount(material[j]));
        }

        // test material 0
        long mat = 0;
        grid = new BlockBasedAttributeGridByte(width, height, depth, 0.05, 0.02);
        for (int x=0; x<width; x++) {
            grid.setData(x,0,0, Grid.EXTERIOR, mat);
        }

        assertEquals("Material count is not " + width, width, grid.findCount(mat));

        grid = new BlockBasedAttributeGridByte(width, height, depth, 0.05, 0.02);
        for (int y=0; y<height; y++) {
            grid.setData(0, y, 0, Grid.INTERIOR, mat);
        }

        assertEquals("Material count is not " + height, height, grid.findCount(mat));

    }

    /**
     * Test find voxels by voxel class
     */
    public void testFindVoxelClassAttribute() {
        int width = 3;
        int height = 4;
        int depth = 10;
        int[] stateDepth = {10, 6, 1};
        byte[] states = {Grid.EXTERIOR, Grid.INTERIOR, Grid.OUTSIDE};

        AttributeGrid grid = new BlockBasedAttributeGridByte(width, height, depth, 0.05, 0.02);

        width = grid.getWidth();
        height = grid.getHeight();
        depth = grid.getDepth();

        // set some data
        for (int x=0; x<states.length; x++){
            for (int y=0; y<height; y++) {
                for (int z=0; z<stateDepth[x]; z++) {
                    grid.setData(x, y, z, states[x], (byte) 2);
                }
            }
        }

        int expectedAllCount = width * height * depth;
        int expectedExtCount = stateDepth[0] * height;
        int expectedIntCount = stateDepth[1] * height;
        int expectedMrkCount = expectedExtCount + expectedIntCount;
        int expectedOutCount = expectedAllCount - expectedMrkCount;

        resetCounts();
        grid.findAttribute(VoxelClasses.ALL, this);
        assertEquals("All voxel count is not " + expectedAllCount, expectedAllCount, allCount);

        resetCounts();
        grid.findAttribute(VoxelClasses.MARKED, this);
        assertEquals("Marked voxel count is not " + expectedMrkCount, expectedMrkCount, mrkCount);

        resetCounts();
        grid.findAttribute(VoxelClasses.EXTERIOR, this);
        assertEquals("Exterior voxel count is not " + expectedExtCount, expectedExtCount, extCount);

        resetCounts();
        grid.findAttribute(VoxelClasses.INTERIOR, this);
        assertEquals("Interior voxel count is not " + expectedIntCount, expectedIntCount, intCount);

        resetCounts();
        grid.findAttribute(VoxelClasses.OUTSIDE, this);
        assertEquals("Outside voxel count is not " + expectedOutCount, expectedOutCount, outCount);
    }

    /**
     * Test find voxels by voxel class
     */
    public void testFindVoxelClass() {
        int width = 3;
        int height = 4;
        int depth = 10;
        int[] stateDepth = {10, 6, 1};
        byte[] states = {Grid.EXTERIOR, Grid.INTERIOR, Grid.OUTSIDE};

        AttributeGrid grid = new BlockBasedAttributeGridByte(width, height, depth, 0.05, 0.02);

        width = grid.getWidth();
        height = grid.getHeight();
        depth = grid.getDepth();

        // set some data
        for (int x=0; x<states.length; x++){
            for (int y=0; y<height; y++) {
                for (int z=0; z<stateDepth[x]; z++) {
                    grid.setData(x, y, z, states[x], (byte) 2);
                }
            }
        }

        int expectedAllCount = width * height * depth;
        int expectedExtCount = stateDepth[0] * height;
        int expectedIntCount = stateDepth[1] * height;
        int expectedMrkCount = expectedExtCount + expectedIntCount;
        int expectedOutCount = expectedAllCount - expectedMrkCount;

        resetCounts();
        grid.find(VoxelClasses.ALL, this);
        assertEquals("All voxel count is not " + expectedAllCount, expectedAllCount, allCount);

        resetCounts();
        grid.find(VoxelClasses.MARKED, this);
        assertEquals("Marked voxel count is not " + expectedMrkCount, expectedMrkCount, mrkCount);

        resetCounts();
        grid.find(VoxelClasses.EXTERIOR, this);
        assertEquals("Exterior voxel count is not " + expectedExtCount, expectedExtCount, extCount);

        resetCounts();
        grid.find(VoxelClasses.INTERIOR, this);
        assertEquals("Interior voxel count is not " + expectedIntCount, expectedIntCount, intCount);

        resetCounts();
        grid.find(VoxelClasses.OUTSIDE, this);
        assertEquals("Outside voxel count is not " + expectedOutCount, expectedOutCount, outCount);
    }

    /**
     * Test that find voxels by VoxelClass actually found the voxels in the correct coordinates
     */
    public void testFindVoxelClassIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;
        long mat = 1;

        AttributeGrid grid =new BlockBasedAttributeGridByte(width, height, depth, 0.001, 0.001);
        HashSet<VoxelCoordinate> vcSetExt = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetInt = new HashSet<VoxelCoordinate>();

        for (int x=0; x<width; x++) {
            grid.setData(x, 2, 2, Grid.EXTERIOR, mat);
            vcSetExt.add(new VoxelCoordinate(x, 2, 2));

            grid.setData(x, 5, 6, Grid.INTERIOR, mat);
            vcSetInt.add(new VoxelCoordinate(x, 5, 6));
        }

        FindAttributeIterateTester ft = new FindAttributeIterateTester(vcSetExt);
        grid.findAttribute(VoxelClasses.EXTERIOR, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state",
                ft.foundAllVoxels());

        ft = new FindAttributeIterateTester(vcSetInt);
        grid.findAttribute(VoxelClasses.INTERIOR, ft);

        assertTrue("Found state iterator did not find all voxels with INTERIOR state",
                ft.foundAllVoxels());

        // make sure that finding a voxel not in the list returns false
        grid.setData(10, 6, 2, Grid.EXTERIOR, mat);
        ft = new FindAttributeIterateTester(vcSetExt);
        grid.findAttribute(VoxelClasses.EXTERIOR, ft);

        assertFalse("Found state iterator should return false",
                ft.foundAllVoxels());

        // make sure that not finding a voxel in the list returns false
        grid.setData(1, 5, 6, Grid.EXTERIOR, mat);
        ft = new FindAttributeIterateTester(vcSetInt);
        grid.findAttribute(VoxelClasses.INTERIOR, ft);

        assertFalse("Found state iterator should return false",
                ft.foundAllVoxels());

        //-------------------------------------------------------
        // test on some random coordinates
        int[][] coords = {
                {0,0,0},
                {width/2, height/2, depth/2},
                {0, height-1, depth-1},
                {width-1, 0, 0},
                {width-1, height-1, depth-1}
        };

        grid = new BlockBasedAttributeGridByte(width, height, depth, 0.001, 0.001);
        vcSetExt = new HashSet<VoxelCoordinate>();

        for (int i=0; i<coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.EXTERIOR, mat);
            vcSetExt.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        ft = new FindAttributeIterateTester(vcSetExt);
        grid.findAttribute(VoxelClasses.EXTERIOR, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state",
                ft.foundAllVoxels());

    }

    /**
     * Test that find voxels by VoxelClass actually found the voxels in the correct coordinates
     */
    public void testFindInterruptableVoxelClassIteratorAttribute() {
        int width = 20;
        int height = 10;
        int depth = 10;
        long mat = 1;

        AttributeGrid grid =new BlockBasedAttributeGridByte(width, height, depth, 0.001, 0.001);
        HashSet<VoxelCoordinate> vcSetExt = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetInt = new HashSet<VoxelCoordinate>();

        for (int x=0; x<width; x++) {
            grid.setData(x, 2, 2, Grid.EXTERIOR, mat);
            grid.setData(x, 4, 4, Grid.EXTERIOR, mat);
            vcSetExt.add(new VoxelCoordinate(x, 2, 2));
            vcSetExt.add(new VoxelCoordinate(x, 4, 4));

            grid.setData(x, 5, 6, Grid.INTERIOR, mat);
            vcSetInt.add(new VoxelCoordinate(x, 5, 6));
        }

        FindAttributeIterateTester ft = new FindAttributeIterateTester(vcSetExt);
        grid.findAttributeInterruptible(VoxelClasses.EXTERIOR, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state",
                ft.foundAllVoxels());

        ft = new FindAttributeIterateTester(vcSetInt);
        grid.findAttributeInterruptible(VoxelClasses.INTERIOR, ft);

        assertTrue("Found iterator did not find all voxels with INTERIOR state",
                ft.foundAllVoxels());

        // make sure that findInterruptible stops interating when voxel is not found
        // do this by adding a new exterior voxel
        grid.setData(5, 2, 2, Grid.OUTSIDE, mat);
        grid.setData(1, 3, 3, Grid.EXTERIOR, mat);
        ft = new FindAttributeIterateTester(vcSetExt);
        grid.findAttributeInterruptible(VoxelClasses.EXTERIOR, ft);

        assertFalse("Found state interruptible iterator should return false",
                ft.foundAllVoxels());
        assertTrue("Found state interruptible did not get interrupted ",
                ft.getIterateCount() < vcSetExt.size());

        // make sure that not finding a voxel in the list returns false
        // do this by changing one of the interior voxels to exterior state
        grid.setData(1, 5, 6, Grid.EXTERIOR, mat);
        ft = new FindAttributeIterateTester(vcSetInt);
        grid.findAttributeInterruptible(VoxelClasses.INTERIOR, ft);

        assertFalse("Found state interruptible iterator should return false", ft.foundAllVoxels());

        //-------------------------------------------------------
        // test on some random coordinates
        int[][] coords = {
                {0,0,0},
                {width/2, height/2, depth/2},
                {0, height-1, depth-1},
                {width-1, 0, 0},
                {width-1, height-1, depth-1}
        };

        grid = new BlockBasedAttributeGridByte(width, height, depth, 0.001, 0.001);
        vcSetExt = new HashSet<VoxelCoordinate>();

        for (int i=0; i<coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.EXTERIOR, mat);
            vcSetExt.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        ft = new FindAttributeIterateTester(vcSetExt);
        grid.findAttributeInterruptible(VoxelClasses.EXTERIOR, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state",
                ft.foundAllVoxels());

    }

    /**
     * Test that find voxels by VoxelClass actually found the voxels in the correct coordinates
     */
    public void testFindInterruptableVoxelClassIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;
        long mat = 1;

        AttributeGrid grid =new BlockBasedAttributeGridByte(width, height, depth, 0.001, 0.001);
        HashSet<VoxelCoordinate> vcSetExt = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetInt = new HashSet<VoxelCoordinate>();

        for (int x=0; x<width; x++) {
            grid.setData(x, 2, 2, Grid.EXTERIOR, mat);
            grid.setData(x, 4, 4, Grid.EXTERIOR, mat);
            vcSetExt.add(new VoxelCoordinate(x, 2, 2));
            vcSetExt.add(new VoxelCoordinate(x, 4, 4));

            grid.setData(x, 5, 6, Grid.INTERIOR, mat);
            vcSetInt.add(new VoxelCoordinate(x, 5, 6));
        }

        FindIterateTester ft = new FindIterateTester(vcSetExt);
        grid.findInterruptible(VoxelClasses.EXTERIOR, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state",
                ft.foundAllVoxels());

        ft = new FindIterateTester(vcSetInt);
        grid.findInterruptible(VoxelClasses.INTERIOR, ft);

        assertTrue("Found iterator did not find all voxels with INTERIOR state",
                ft.foundAllVoxels());

        // make sure that findInterruptible stops interating when voxel is not found
        // do this by adding a new exterior voxel
        grid.setData(5, 2, 2, Grid.OUTSIDE, mat);
        grid.setData(1, 3, 3, Grid.EXTERIOR, mat);
        ft = new FindIterateTester(vcSetExt);
        grid.findInterruptible(VoxelClasses.EXTERIOR, ft);

        assertFalse("Found state interruptible iterator should return false",
                ft.foundAllVoxels());
        assertTrue("Found state interruptible did not get interrupted ",
                ft.getIterateCount() < vcSetExt.size());

        // make sure that not finding a voxel in the list returns false
        // do this by changing one of the interior voxels to exterior state
        grid.setData(1, 5, 6, Grid.EXTERIOR, mat);
        ft = new FindIterateTester(vcSetInt);
        grid.findInterruptible(VoxelClasses.INTERIOR, ft);

        assertFalse("Found state interruptible iterator should return false", ft.foundAllVoxels());

        //-------------------------------------------------------
        // test on some random coordinates
        int[][] coords = {
                {0,0,0},
                {width/2, height/2, depth/2},
                {0, height-1, depth-1},
                {width-1, 0, 0},
                {width-1, height-1, depth-1}
        };

        grid = new BlockBasedAttributeGridByte(width, height, depth, 0.001, 0.001);
        vcSetExt = new HashSet<VoxelCoordinate>();

        for (int i=0; i<coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.EXTERIOR, mat);
            vcSetExt.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        ft = new FindIterateTester(vcSetExt);
        grid.findInterruptible(VoxelClasses.EXTERIOR, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state",
                ft.foundAllVoxels());

    }

    /**
     * Test that find voxels by material actually found the voxels in the correct coordinates
     */
    public void testFindMaterialIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;
        long mat1 = 1;
        long mat2 = 2;

        AttributeGrid grid =new BlockBasedAttributeGridByte(width, height, depth, 0.001, 0.001);
        HashSet<VoxelCoordinate> vcSetMat1 = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetMat2 = new HashSet<VoxelCoordinate>();

        for (int x=0; x<width; x++) {
            grid.setData(x, 2, 2, Grid.EXTERIOR, mat1);
            vcSetMat1.add(new VoxelCoordinate(x, 2, 2));

            grid.setData(x, 5, 6, Grid.INTERIOR, mat2);
            vcSetMat2.add(new VoxelCoordinate(x, 5, 6));
        }

        FindAttributeIterateTester ft = new FindAttributeIterateTester(vcSetMat1);
        grid.findAttribute(mat1, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat1,
                ft.foundAllVoxels());

        ft = new FindAttributeIterateTester(vcSetMat2);
        grid.findAttribute(mat2, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat2,
                ft.foundAllVoxels());

        // make sure that finding a voxel not in the list returns false
        grid.setData(10, 6, 2, Grid.EXTERIOR, mat1);
        ft = new FindAttributeIterateTester(vcSetMat1);
        grid.findAttribute(mat1, ft);

        assertFalse("Found material iterator should return false",
                ft.foundAllVoxels());

        // make sure that not finding a voxel in the list returns false
        grid.setData(1, 5, 6, Grid.EXTERIOR, mat1);
        ft = new FindAttributeIterateTester(vcSetMat2);
        grid.findAttribute(mat1, ft);

        assertFalse("Found material iterator should return false",
                ft.foundAllVoxels());

        //-------------------------------------------------------
        // test on some random boundary coordinates
        int[][] coords = {
                {0,0,0},
                {width/2, height/2, depth/2},
                {0, height-1, depth-1},
                {width-1, 0, 0},
                {width-1, height-1, depth-1}
        };

        grid = new BlockBasedAttributeGridByte(width, height, depth, 0.001, 0.001);
        vcSetMat1 = new HashSet<VoxelCoordinate>();

        for (int i=0; i<coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.EXTERIOR, mat1);
            vcSetMat1.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        ft = new FindAttributeIterateTester(vcSetMat1);
        grid.findAttribute(mat1, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat1,
                ft.foundAllVoxels());

    }

    /**
     * Test that find voxels by material actually found the voxels in the correct coordinates
     */
    public void testFindInterruptablMaterialIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;
        long mat1 = 1;
        long mat2 = 2;

        AttributeGrid grid =new BlockBasedAttributeGridByte(width, height, depth, 0.001, 0.001);
        HashSet<VoxelCoordinate> vcSetMat1 = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetMat2 = new HashSet<VoxelCoordinate>();

        for (int x=0; x<width; x++) {
            grid.setData(x, 2, 2, Grid.EXTERIOR, mat1);
            grid.setData(x, 4, 4, Grid.INTERIOR, mat1);
            vcSetMat1.add(new VoxelCoordinate(x, 2, 2));
            vcSetMat1.add(new VoxelCoordinate(x, 4, 4));

            grid.setData(x, 5, 6, Grid.EXTERIOR, mat2);
            vcSetMat2.add(new VoxelCoordinate(x, 5, 6));
        }

        FindAttributeIterateTester ft = new FindAttributeIterateTester(vcSetMat1);
        grid.findAttributeInterruptible(mat1, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat1,
                ft.foundAllVoxels());

        ft = new FindAttributeIterateTester(vcSetMat2);
        grid.findAttributeInterruptible(mat2, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat2,
                ft.foundAllVoxels());

        // make sure that findInterruptible stops interating when voxel is not found
        // do this by adding a new material voxel
        grid.setData(5, 2, 2, Grid.OUTSIDE, 0);
        grid.setData(1, 3, 3, Grid.EXTERIOR, mat1);
        ft = new FindAttributeIterateTester(vcSetMat1);
        grid.findAttributeInterruptible(mat1, ft);

        assertFalse("Found material interruptible iterator should return false",
                ft.foundAllVoxels());
        assertTrue("Found material interruptible did not get interrupted ",
                ft.getIterateCount() < vcSetMat1.size());

        // make sure that not finding a voxel in the list returns false
        // do this by changing one of the interior voxels to EXTERIOR state
        grid.setData(1, 5, 6, Grid.EXTERIOR, mat1);
        ft = new FindAttributeIterateTester(vcSetMat2);
        grid.findAttributeInterruptible(mat2, ft);

        assertFalse("Found material interruptible iterator should return false", ft.foundAllVoxels());

        //-------------------------------------------------------
        // test on some random coordinates
        int[][] coords = {
                {0,0,0},
                {width/2, height/2, depth/2},
                {0, height-1, depth-1},
                {width-1, 0, 0},
                {width-1, height-1, depth-1}
        };

        grid = new BlockBasedAttributeGridByte(width, height, depth, 0.001, 0.001);
        vcSetMat1 = new HashSet<VoxelCoordinate>();

        for (int i=0; i<coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.EXTERIOR, mat1);
            vcSetMat1.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        ft = new FindAttributeIterateTester(vcSetMat1);
        grid.findAttributeInterruptible(mat1, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat1,
                ft.foundAllVoxels());

    }

    /**
     * Test that find voxels by VoxelClass and material actually found the voxels in the correct coordinates
     */
    public void testFindMaterialAndVCIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;
        long mat1 = 1;
        long mat2 = 2;

        AttributeGrid grid =new BlockBasedAttributeGridByte(width, height, depth, 0.001, 0.001);
        HashSet<VoxelCoordinate> vcSetExtMat1 = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetIntMat2 = new HashSet<VoxelCoordinate>();

        for (int x=0; x<width; x++) {
            grid.setData(x, 2, 2, Grid.EXTERIOR, mat1);
            vcSetExtMat1.add(new VoxelCoordinate(x, 2, 2));

            grid.setData(x, 5, 6, Grid.INTERIOR, mat2);
            vcSetIntMat2.add(new VoxelCoordinate(x, 5, 6));
        }

        FindAttributeIterateTester ft = new FindAttributeIterateTester(vcSetExtMat1);
        grid.findAttribute(VoxelClasses.EXTERIOR, mat1, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state and material " + mat1,
                ft.foundAllVoxels());

        ft = new FindAttributeIterateTester(vcSetIntMat2);
        grid.findAttribute(VoxelClasses.INTERIOR, mat2, ft);

        assertTrue("Found state iterator did not find all voxels with INTERIOR state and material " + mat2,
                ft.foundAllVoxels());

        // make sure that finding a voxel not in the list returns false
        grid.setData(10, 6, 2, Grid.EXTERIOR, mat1);
        ft = new FindAttributeIterateTester(vcSetExtMat1);
        grid.findAttribute(VoxelClasses.EXTERIOR, mat1, ft);

        assertFalse("Found state and material iterator should return false",
                ft.foundAllVoxels());

        // make sure that not finding a voxel in the list returns false
        grid.setData(1, 5, 6, Grid.EXTERIOR, mat1);
        ft = new FindAttributeIterateTester(vcSetIntMat2);
        grid.findAttribute(VoxelClasses.INTERIOR, ft);

        assertFalse("Found state and material iterator should return false",
                ft.foundAllVoxels());

        //-------------------------------------------------------
        // test on some random coordinates
        int[][] coords = {
                {0,0,0},
                {width/2, height/2, depth/2},
                {0, height-1, depth-1},
                {width-1, 0, 0},
                {width-1, height-1, depth-1}
        };

        grid = new BlockBasedAttributeGridByte(width, height, depth, 0.001, 0.001);
        vcSetExtMat1 = new HashSet<VoxelCoordinate>();

        for (int i=0; i<coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.EXTERIOR, mat1);
            vcSetExtMat1.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        ft = new FindAttributeIterateTester(vcSetExtMat1);
        grid.findAttribute(VoxelClasses.EXTERIOR, mat1, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state and material " + mat1,
                ft.foundAllVoxels());

    }

    /**
     * Test that find voxels by voxel class and material actually found the voxels in the correct coordinates
     */
    public void testFindInterruptablMaterialAndVCIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;
        long mat1 = 1;
        long mat2 = 2;

        AttributeGrid grid =new BlockBasedAttributeGridByte(width, height, depth, 0.001, 0.001);
        HashSet<VoxelCoordinate> vcSetExtMat1 = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetIntMat1 = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetExtMat2 = new HashSet<VoxelCoordinate>();

        for (int x=0; x<width; x++) {
            grid.setData(x, 2, 2, Grid.EXTERIOR, mat1);
            vcSetExtMat1.add(new VoxelCoordinate(x, 2, 2));

            grid.setData(x, 4, 4, Grid.EXTERIOR, mat1);
            vcSetExtMat1.add(new VoxelCoordinate(x, 4, 4));

            grid.setData(x, 7, 7, Grid.INTERIOR, mat1);
            vcSetIntMat1.add(new VoxelCoordinate(x, 7, 7));

            grid.setData(x, 5, 6, Grid.EXTERIOR, mat2);
            vcSetExtMat2.add(new VoxelCoordinate(x, 5, 6));
        }

        FindAttributeIterateTester ft = new FindAttributeIterateTester(vcSetExtMat1);
        grid.findAttributeInterruptible(VoxelClasses.EXTERIOR, mat1, ft);
        assertTrue("Found iterator did not find all voxels with EXTERIOR state and material " + mat1,
                ft.foundAllVoxels());

        ft = new FindAttributeIterateTester(vcSetIntMat1);
        grid.findAttributeInterruptible(VoxelClasses.INTERIOR, mat1, ft);
        assertTrue("Found iterator did not find all voxels with INTERIOR state and material " + mat1,
                ft.foundAllVoxels());

        ft = new FindAttributeIterateTester(vcSetExtMat2);
        grid.findAttributeInterruptible(VoxelClasses.EXTERIOR, mat2, ft);
        assertTrue("Found iterator did not find all voxels with EXTERIOR state and material " + mat2,
                ft.foundAllVoxels());

        // make sure that findInterruptible stops interating when voxel is not found
        // do this by adding a new material voxel
        grid.setData(5, 2, 2, Grid.OUTSIDE, 0);
        grid.setData(1, 3, 3, Grid.EXTERIOR, mat1);
        ft = new FindAttributeIterateTester(vcSetExtMat1);
        grid.findAttributeInterruptible(VoxelClasses.EXTERIOR, mat1, ft);

        assertFalse("Found state and material interruptible iterator should return false",
                ft.foundAllVoxels());
        assertTrue("Found state and material interruptible did not get interrupted ",
                ft.getIterateCount() < vcSetExtMat1.size());

        // make sure that not finding a voxel in the list returns false
        // do this by changing one of the interior and mat2 voxels
        grid.setData(1, 5, 6, Grid.EXTERIOR, mat1);
        ft = new FindAttributeIterateTester(vcSetExtMat2);
        grid.findAttributeInterruptible(VoxelClasses.EXTERIOR, mat2, ft);

        assertFalse("Found state and material iterator should return false", ft.foundAllVoxels());

        //-------------------------------------------------------
        // test on some random coordinates
        int[][] coords = {
                {0,0,0},
                {width/2, height/2, depth/2},
                {0, height-1, depth-1},
                {width-1, 0, 0},
                {width-1, height-1, depth-1}
        };

        grid = new BlockBasedAttributeGridByte(width, height, depth, 0.001, 0.001);
        vcSetExtMat1 = new HashSet<VoxelCoordinate>();

        for (int i=0; i<coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.EXTERIOR, mat1);
            vcSetExtMat1.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        ft = new FindAttributeIterateTester(vcSetExtMat1);
        grid.findAttributeInterruptible(VoxelClasses.EXTERIOR, mat1, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state and material " + mat1,
                ft.foundAllVoxels());

    }

    /**
     * Test getGridCoords.
     */
    public void testGetGridCoords() {
        double xWorldCoord = 1.0;
        double yWorldCoord = 0.15;
        double zWorldCoord = 0.61;
        double voxelWidth = 0.02;
        double sliceHeight = 0.01;

        AttributeGrid grid =new BlockBasedAttributeGridByte(xWorldCoord, yWorldCoord, zWorldCoord, voxelWidth, sliceHeight);

        double xcoord = 0.55;
        double ycoord = 0.0202;
        double zcoord = 0.401;

        int expectedXVoxelCoord = (int) (xcoord / voxelWidth);
        int expectedYVoxelCoord = (int) (ycoord / sliceHeight);
        int expectedZVoxelCoord = (int) (zcoord / voxelWidth);
        int[] coords = new int[3];

        grid.getGridCoords(xcoord, ycoord, zcoord, coords);
//System.out.println(coords[0] + ", " + coords[1] + ", " + coords[2]);
        assertTrue("Voxel coordinate is not (" + expectedXVoxelCoord + ", " + expectedYVoxelCoord + ", " + expectedZVoxelCoord + ")",
                coords[0] == expectedXVoxelCoord &&
                coords[1] == expectedYVoxelCoord &&
                coords[2] == expectedZVoxelCoord);

        // test on a voxel line
        xcoord = 0.6;
        ycoord = 0.05;
        zcoord = 0.08;

        expectedXVoxelCoord = (int) (xcoord / voxelWidth);
        expectedYVoxelCoord = (int) (ycoord / sliceHeight);
        expectedZVoxelCoord = (int) (zcoord / voxelWidth);

        grid.getGridCoords(xcoord, ycoord, zcoord, coords);
//System.out.println(coords[0] + ", " + coords[1] + ", " + coords[2]);
        assertTrue("Voxel coordinate is not (" + expectedXVoxelCoord + ", " + expectedYVoxelCoord + ", " + expectedZVoxelCoord + ")",
                coords[0] == expectedXVoxelCoord &&
                coords[1] == expectedYVoxelCoord &&
                coords[2] == expectedZVoxelCoord);
    }

    /**
     * Test getWorldCoords.
     */
    public void testGetWorldCoords() {
        int xVoxels = 50;
        int yVoxels = 15;
        int zVoxels = 31;
        double voxelWidth = 0.02;
        double sliceHeight = 0.01;

        AttributeGrid grid =new BlockBasedAttributeGridByte(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight);

        int xcoord = 27;
        int ycoord = 2;
        int zcoord = 20;

        double expectedXWorldCoord = (double) (xcoord * voxelWidth + voxelWidth / 2);
        double expectedYWorldCoord = (double) (ycoord * sliceHeight + sliceHeight / 2);
        double expectedZWorldCoord = (double) (zcoord * voxelWidth + voxelWidth / 2);
        double[] coords = new double[3];

        grid.getWorldCoords(xcoord, ycoord, zcoord, coords);
//System.out.println(coords[0] + ", " + coords[1] + ", " + coords[2]);
        assertTrue("World coordinate is not (" + expectedXWorldCoord + ", " + expectedYWorldCoord + ", " + expectedZWorldCoord + ")",
                coords[0] == expectedXWorldCoord &&
                coords[1] == expectedYWorldCoord &&
                coords[2] == expectedZWorldCoord);

    }

    /**
     * Test getWorldCoords.
     */
    public void testGetGridBounds() {
        int xVoxels = 50;
        int yVoxels = 15;
        int zVoxels = 31;
        double voxelWidth = 0.02;
        double sliceHeight = 0.01;

        AttributeGrid grid =new BlockBasedAttributeGridByte(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight);

        xVoxels = grid.getWidth();
        yVoxels = grid.getHeight();
        zVoxels = grid.getDepth();

        double[] minBounds = new double[3];
        double[] maxBounds = new double[3];
        double expectedMaxX = xVoxels * voxelWidth;
        double expectedMaxY = yVoxels * sliceHeight;
        double expectedMaxZ = zVoxels * voxelWidth;

        grid.getGridBounds(minBounds, maxBounds);
//System.out.println(maxBounds[0] + ", " + maxBounds[1] + ", " + maxBounds[2]);

        assertTrue("Minimum bounds is not (0, 0, 0)",
                minBounds[0] == 0.0 &&
                minBounds[1] == 0.0 &&
                minBounds[2] == 0.0);

        assertTrue("Maximum bounds is not (" + expectedMaxX + ", " + expectedMaxY + ", " + expectedMaxZ + ")",
                maxBounds[0] == expectedMaxX &&
                maxBounds[1] == expectedMaxY &&
                maxBounds[2] == expectedMaxZ);

    }

    /**
     * Test getSliceHeight with both constructor methods.
     */
    public void testGetSliceHeight() {
        double sliceHeight = 0.0015;

        // voxel coordinates
        AttributeGrid grid =new BlockBasedAttributeGridByte(50, 25, 70, 0.05, sliceHeight);
        assertEquals("Slice height is not " + sliceHeight, sliceHeight, grid.getSliceHeight());

        // world coordinates
        grid = new BlockBasedAttributeGridByte(0.12, 0.11, 0.12, 0.05, sliceHeight);
        assertEquals("Slice height is not" + sliceHeight, sliceHeight, grid.getSliceHeight());
    }

    /**
     * Test getVoxelSize with both constructor methods.
     */
    public void testGetVoxelSize() {
        double voxelSize = 0.025;

        // voxel coordinates
        AttributeGrid grid =new BlockBasedAttributeGridByte(50, 25, 70, voxelSize, 0.01);
        assertEquals("Voxel size is not " + voxelSize, voxelSize, grid.getVoxelSize());

        // world coordinates
        grid = new BlockBasedAttributeGridByte(0.12, 0.11, 0.12, voxelSize, 0.01);
        assertEquals("Voxel size is not " + voxelSize, voxelSize, grid.getVoxelSize());
    }

    /**
     * Test setAttribute.
     */
    public void testsetAttribute() {
        int size = 10;

        AttributeGrid grid =new BlockBasedAttributeGridByte(size, size, size, 0.001, 0.001);
        grid.setData(0, 0, 0, Grid.INTERIOR, 1);
        grid.setData(9, 9, 9, Grid.EXTERIOR, 2);
        grid.setData(5, 0, 7, Grid.INTERIOR, 3);

        grid.setAttribute(0, 0, 0, 10);
        grid.setAttribute(9, 9, 9, 11);
        grid.setAttribute(5, 0, 7, 12);

        // check that the material changed, but the state did not
        assertEquals("Material should be ", 10, grid.getAttribute(0, 0, 0));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0, 0, 0));

        assertEquals("Material should be ", 11, grid.getAttribute(9, 9, 9));
        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(9, 9, 9));

        assertEquals("Material should be ", 12, grid.getAttribute(5, 0, 7));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(5, 0, 7));
    }

    /**
     * Test setState.
     */
    public void testSetState() {
        int size = 10;

        AttributeGrid grid =new BlockBasedAttributeGridByte(size, size, size, 0.001, 0.001);
        setState(grid);
    }

    /**
     * Test reassignAttribute.
     */
    public void testReassignMaterial() {
        int size = 20;

        AttributeGrid grid =new BlockBasedAttributeGridByte(size,size,size,0.001, 0.001);
        reassignMaterial(grid);
    }

    /**
     * Test clone.
     */
    public void testClone() {
        int size = 10;
        double voxelSize = 0.002;
        double sliceHeight = 0.001;

        AttributeGrid grid =new BlockBasedAttributeGridByte(size, size, size, voxelSize, sliceHeight);
        grid.setData(0, 0, 0, Grid.INTERIOR, 1);
        grid.setData(9, 9, 9, Grid.EXTERIOR, 2);
        grid.setData(5, 0, 7, Grid.INTERIOR, 3);

        AttributeGrid grid2 =(BlockBasedAttributeGridByte) grid.clone();

        assertEquals("Voxel size should be ", voxelSize, grid2.getVoxelSize());
        assertEquals("Slight height should be ", sliceHeight, grid2.getSliceHeight());

        // check that the state and material are set
        assertEquals("State should be ", Grid.INTERIOR, grid2.getState(0, 0, 0));
        assertEquals("Material should be ", 1, grid2.getAttribute(0, 0, 0));

        assertEquals("State should be ", Grid.EXTERIOR, grid2.getState(9, 9, 9));
        assertEquals("Material should be ", 2, grid2.getAttribute(9, 9, 9));

        assertEquals("State should be ", Grid.INTERIOR, grid2.getState(5, 0, 7));
        assertEquals("Material should be ", 3, grid2.getAttribute(5, 0, 7));
    }

    /**
     * Test that remove material removes all specified material
     */
    public void testRemoveMaterialIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;
        long mat1 = 1;
        long mat2 = 2;

        AttributeGrid grid =new BlockBasedAttributeGridByte(width, height, depth, 0.001, 0.001);
        HashSet<VoxelCoordinate> vcSetMat1 = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetMat2 = new HashSet<VoxelCoordinate>();

        for (int x=0; x<width; x++) {
            grid.setData(x, 2, 2, Grid.EXTERIOR, mat1);
            vcSetMat1.add(new VoxelCoordinate(x, 2, 2));

            grid.setData(x, 4, 4, Grid.INTERIOR, mat1);
            vcSetMat1.add(new VoxelCoordinate(x, 4, 4));

            grid.setData(x, 5, 6, Grid.INTERIOR, mat2);
            vcSetMat2.add(new VoxelCoordinate(x, 5, 6));
        }

        // make sure that all coordinates in list have been set to mat1 in grid
        FindAttributeIterateTester ft = new FindAttributeIterateTester(vcSetMat1);
        grid.findAttribute(mat1, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat1,
                ft.foundAllVoxels());

        // remove all mat1
        grid.removeAttribute(mat1);

        // check that find mat1 returns false and iterate count returns zero
        ft = new FindAttributeIterateTester(vcSetMat1);
        grid.findAttribute(mat1, ft);

        assertFalse("Found iterator did not return false after removing material " + mat1,
                ft.foundAllVoxels());

        assertEquals("Found iterate count is not 0 after removing material", 0, ft.getIterateCount());

        // make sure that all coordinates in list are no longer mat1 in grid
        // note: probably redundant since the above assertions have passed
        Iterator iter = vcSetMat1.iterator();

        while (iter.hasNext()) {
            VoxelCoordinate vc = (VoxelCoordinate) iter.next();
//          System.out.println(vc.getX() + ", " + vc.getY() + ", " + vc.getZ());
            assertEquals("Material is not 0 after removal for coordinate: " +
                        vc.getX() + ", " + vc.getY() + ", " + vc.getZ(),
                    0,
                    grid.getAttribute(vc.getX(), vc.getY(), vc.getZ()));
        }

        // make sure other material has not been removed
        ft = new FindAttributeIterateTester(vcSetMat2);
        grid.findAttribute(mat2, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat2,
                ft.foundAllVoxels());

    }
}