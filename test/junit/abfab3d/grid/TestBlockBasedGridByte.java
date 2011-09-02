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
import abfab3d.grid.op.RemoveMaterial;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Internal Imports

/**
 * Tests the functionality of a BlockBasedGridByte.
 *
 * @author Alan Hudson
 * @version
 */
public class TestBlockBasedGridByte extends BaseTestGrid implements ClassTraverser {

    /** The material count */
    private int allCount;
    private int mrkCount;
    private int extCount;
    private int intCount;
    private int outCount;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestBlockBasedGridByte.class);
    }

    /**
     * Test the reverse mapping functions
     */
    public void testReverseMappingFunctionsEqual() {
        int w = 8;
        int h = 8;
        int d = 8;
        int blockOrder = 2;

        BlockBasedGridByte grid = new BlockBasedGridByte(w, h, d, 0.001, 0.001,blockOrder);

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

        BlockBasedGridByte grid = new BlockBasedGridByte(w, h, d, 0.001, 0.001,blockOrder);

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
    	BlockBasedGridByte grid = new BlockBasedGridByte(10, 9, 8, 0.001, 0.001);
    	
        grid.setData(0, 0, 0, Grid.INTERIOR, 2);
        grid.setData(9, 8, 7, Grid.EXTERIOR, 1);
        grid.setData(5, 0, 7, Grid.INTERIOR, 0);

        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0, 0, 0));
        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(9, 8, 7));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(5, 0, 7));
        
        BlockBasedGridByte grid2 = new BlockBasedGridByte(grid);
        
        assertEquals("State should be ", Grid.INTERIOR, grid2.getState(0, 0, 0));
        assertEquals("State should be ", Grid.EXTERIOR, grid2.getState(9, 8, 7));
        assertEquals("State should be ", Grid.INTERIOR, grid2.getState(5, 0, 7));
        assertEquals("State should be ", Grid.OUTSIDE, grid2.getState(0, 0, 1));
        
        assertEquals("Material should be ", 2, grid2.getMaterial(0, 0, 0));
        assertEquals("Material should be ", 1, grid2.getMaterial(9, 8, 7));
        assertEquals("Material should be ", 0, grid2.getMaterial(5, 0, 7));
    }
    
    /**
     * Test creating an empty grid.
     */
    public void testCreateEmpty() {
        Grid grid = new BlockBasedGridByte(100, 101, 102, 0.001, 0.001);
        
        grid.setData(5, 5, 5, Grid.EXTERIOR, 10);
        
        Grid grid2 = grid.createEmpty(10, 11, 12, 0.002, 0.003);
        
        assertTrue("Grid type is not BlockBasedGridByte", grid2 instanceof BlockBasedGridByte);
        assertEquals("Grid voxel size is not 0.002", 0.002, grid2.getVoxelSize());
        assertEquals("Grid slice height is not 0.003", 0.003, grid2.getSliceHeight());
        
        // all voxels in empty grid should be OUTSIDE state and 0 material
        assertEquals("State is not OUTSIDE for (5, 5, 5)", Grid.OUTSIDE, grid2.getState(5, 5, 5));
        assertEquals("Material is not 0 for (5, 5, 5)", 0, grid2.getMaterial(5, 5, 5));
    }
    
    /**
     * Test set/get all data points.
     */
    public void testSetGetByVoxelCoords() {
        Grid grid = new BlockBasedGridByte(8, 8, 8, 0.001, 0.001, 1);
        setGetAllVoxelCoords(grid);

        grid = new BlockBasedGridByte(8, 8, 8, 0.001, 0.001, 2);
        setGetAllVoxelCoords(grid);

        grid = new BlockBasedGridByte(8, 8, 8, 0.001, 0.001, 3);
        setGetAllVoxelCoords(grid);

        grid = new BlockBasedGridByte(16,8,8,0.001, 0.001, 2);
        setGetAllVoxelCoords(grid);

        grid = new BlockBasedGridByte(16, 16, 16, 0.001, 0.001, 2);
        setGetAllVoxelCoords(grid);

        grid = new BlockBasedGridByte(100, 91, 85, 0.001, 0.001, 2);
        setGetAllVoxelCoords(grid);
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGetByWorldCoords() {
        Grid grid = new BlockBasedGridByte(8, 8, 8, 0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);

        grid = new BlockBasedGridByte(3,2,2,0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);

        grid = new BlockBasedGridByte(11, 11, 11, 0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);

        grid = new BlockBasedGridByte(100, 91, 85, 0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);
    }

    /**
     * Test getState by voxels.
     */
    public void testGetStateByVoxel() {
        Grid grid = new BlockBasedGridByte(10, 9, 8, 0.001, 0.001);
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
        Grid grid = new BlockBasedGridByte(1.0, 0.4, 0.5, 0.05, 0.01);

        // set and test get on some random world coordinates
        grid.setData(0.0, 0.0, 0.0, Grid.OUTSIDE, (byte)2);
        grid.setData(0.95, 0.39, 0.45, Grid.EXTERIOR, (byte)1);
        grid.setData(0.6, 0.1, 0.4, Grid.INTERIOR, (byte)0);
        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0.0, 0.0, 0.0));
        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(0.95, 0.39, 0.45));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.6, 0.1, 0.4));

        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid = new BlockBasedGridByte(0.12, 0.11, 0.16, 0.05, 0.02);
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
     * Test getMaterial by voxels.
     */
    public void testGetMaterialByVoxel() {
        Grid grid = new BlockBasedGridByte(10, 9, 8, 0.001, 0.001);

        // Removed outside as it doesn't have to carry state
        //grid.setData(0, 0, 0, Grid.OUTSIDE, (byte)3);
        grid.setData(9, 8, 7, Grid.EXTERIOR, (byte)2);
        grid.setData(5, 0, 7, Grid.INTERIOR, (byte)1);

        assertEquals("Material should be ", 2, grid.getMaterial(9, 8, 7));
        assertEquals("Material should be ", 1, grid.getMaterial(5, 0, 7));

        // Index that are not set should default to 0
        assertEquals("Material should be ", 0, grid.getMaterial(8, 8, 8));
    }

    /**
     * Test getMaterial by world coordinates.
     */
    public void testGetMaterialByCoord() {
        Grid grid = new BlockBasedGridByte(1.0, 0.4, 0.5, 0.05, 0.01);

        // set and test get on some random world coordinates
        grid.setData(0.0, 0.0, 0.0, Grid.OUTSIDE, (byte)3);
        grid.setData(0.95, 0.39, 0.45, Grid.EXTERIOR, (byte)2);
        grid.setData(0.6, 0.1, 0.4, Grid.INTERIOR, (byte)1);
        assertEquals("State should be ", 3, grid.getMaterial(0.0, 0.0, 0.0));
        assertEquals("State should be ", 2, grid.getMaterial(0.95, 0.39, 0.45));
        assertEquals("State should be ", 1, grid.getMaterial(0.6, 0.1, 0.4));

        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid = new BlockBasedGridByte(0.12, 0.11, 0.16, 0.05, 0.02);
        grid.setData(0.06, 0.07, 0.08, Grid.INTERIOR, (byte)2);
        assertEquals("State should be ", 2, grid.getMaterial(0.05, 0.06, 0.05));
        assertEquals("State should be ", 2, grid.getMaterial(0.0999, 0.06, 0.05));
        assertEquals("State should be ", 2, grid.getMaterial(0.05, 0.0799, 0.05));
        assertEquals("State should be ", 2, grid.getMaterial(0.05, 0.06, 0.0999));
        assertEquals("State should be ", 2, grid.getMaterial(0.0999, 0.0799, 0.0999));
        assertEquals("State should be ", 0, grid.getMaterial(0.0499, 0.06, 0.05));
        assertEquals("State should be ", 0, grid.getMaterial(0.05, 0.0599, 0.05));
        assertEquals("State should be ", 0, grid.getMaterial(0.05, 0.06, 0.0499));
        assertEquals("State should be ", 0, grid.getMaterial(0.1, 0.06, 0.05));
        assertEquals("State should be ", 0, grid.getMaterial(0.05, 0.08, 0.05));
        assertEquals("State should be ", 0, grid.getMaterial(0.05, 0.06, 0.1));

        // set data for beginning voxel 0,0,0 and test the bounds
        grid.setData(0.0, 0.0, 0.0, Grid.INTERIOR, (byte)5);
        assertEquals("State should be ", 5, grid.getMaterial(0.0, 0.0, 0.0));
        assertEquals("State should be ", 5, grid.getMaterial(0.0499, 0.0, 0.0));
        assertEquals("State should be ", 5, grid.getMaterial(0.0, 0.0199, 0.0));
        assertEquals("State should be ", 5, grid.getMaterial(0.0, 0.0, 0.0499));
        assertEquals("State should be ", 5, grid.getMaterial(0.0499, 0.0199, 0.0499));
        assertEquals("State should be ", 0, grid.getMaterial(0.05, 0.0, 0.0));
        assertEquals("State should be ", 0, grid.getMaterial(0.0, 0.02, 0.0));
        assertEquals("State should be ", 0, grid.getMaterial(0.0, 0.0, 0.05));

        // set data for last voxel 2,5,3 and test the bounds
        grid.setData(0.149, 0.119, 0.199, Grid.INTERIOR, (byte)12);
//        assertEquals("State should be ", 12, grid.getMaterial(0.1, 0.1, 0.15)); //failing because 0.15/0.05=2.999997
        assertEquals("State should be ", 12, grid.getMaterial(0.1499, 0.1, 0.1501));
        assertEquals("State should be ", 12, grid.getMaterial(0.1, 0.119, 0.1501));
        assertEquals("State should be ", 12, grid.getMaterial(0.1, 0.1, 0.199));
        assertEquals("State should be ", 12, grid.getMaterial(0.1499, 0.1199, 0.1999));
        assertEquals("State should be ", 0, grid.getMaterial(0.0999, 0.1, 0.1501));
        assertEquals("State should be ", 0, grid.getMaterial(0.1, 0.0999, 0.1501));
        assertEquals("State should be ", 0, grid.getMaterial(0.1, 0.1, 0.1499));
        assertEquals("State should be ", 0, grid.getMaterial(0.0999, 0.0999, 0.1499));
    }


    /**
     * Test set/get byte material range.
     */
    public void testByteMaterialRange() {
        int width = 100;
        int maxMaterial = 64;
        int mat, expectedMat;

        Grid grid = new BlockBasedGridByte(width, 1, 1, 0.001, 0.001);

        for (int x=0; x<width; x++) {
            grid.setData(x, 0, 0, Grid.EXTERIOR, x);
        }

        for (int x=0; x<width; x++) {
            mat = grid.getMaterial(x, 0, 0);
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

        Grid grid = new BlockBasedGridByte(width, height, depth, 0.05, 0.02);

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
        int material0 = 2;
        int material1 = 5;
        int material2 = 12;
        int[] materialDepth = {10, 6, 1};
        int[] material = {material0, material1, material2};

        Grid grid = new BlockBasedGridByte(width, height, depth, 0.05, 0.02);

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
        int mat = 0;
        grid = new BlockBasedGridByte(width, height, depth, 0.05, 0.02);
        for (int x=0; x<width; x++) {
            grid.setData(x,0,0, Grid.EXTERIOR, mat);
        }

        assertEquals("Material count is not " + width, width, grid.findCount(mat));

        grid = new BlockBasedGridByte(width, height, depth, 0.05, 0.02);
        for (int y=0; y<height; y++) {
            grid.setData(0, y, 0, Grid.INTERIOR, mat);
        }

        assertEquals("Material count is not " + height, height, grid.findCount(mat));

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

        Grid grid = new BlockBasedGridByte(width, height, depth, 0.05, 0.02);

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
        int mat = 1;

        Grid grid = new BlockBasedGridByte(width, height, depth, 0.001, 0.001);
        HashSet<VoxelCoordinate> vcSetExt = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetInt = new HashSet<VoxelCoordinate>();

        for (int x=0; x<width; x++) {
            grid.setData(x, 2, 2, Grid.EXTERIOR, mat);
            vcSetExt.add(new VoxelCoordinate(x, 2, 2));

            grid.setData(x, 5, 6, Grid.INTERIOR, mat);
            vcSetInt.add(new VoxelCoordinate(x, 5, 6));
        }

        FindIterateTester ft = new FindIterateTester(vcSetExt);
        grid.find(VoxelClasses.EXTERIOR, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state",
                ft.foundAllVoxels());

        ft = new FindIterateTester(vcSetInt);
        grid.find(VoxelClasses.INTERIOR, ft);

        assertTrue("Found state iterator did not find all voxels with INTERIOR state",
                ft.foundAllVoxels());

        // make sure that finding a voxel not in the list returns false
        grid.setData(10, 6, 2, Grid.EXTERIOR, mat);
        ft = new FindIterateTester(vcSetExt);
        grid.find(VoxelClasses.EXTERIOR, ft);

        assertFalse("Found state iterator should return false",
                ft.foundAllVoxels());

        // make sure that not finding a voxel in the list returns false
        grid.setData(1, 5, 6, Grid.EXTERIOR, mat);
        ft = new FindIterateTester(vcSetInt);
        grid.find(VoxelClasses.INTERIOR, ft);

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

        grid = new BlockBasedGridByte(width, height, depth, 0.001, 0.001);
        vcSetExt = new HashSet<VoxelCoordinate>();

        for (int i=0; i<coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.EXTERIOR, mat);
            vcSetExt.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        ft = new FindIterateTester(vcSetExt);
        grid.find(VoxelClasses.EXTERIOR, ft);

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
        int mat = 1;

        Grid grid = new BlockBasedGridByte(width, height, depth, 0.001, 0.001);
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

        grid = new BlockBasedGridByte(width, height, depth, 0.001, 0.001);
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
        int mat1 = 1;
        int mat2 = 2;

        Grid grid = new BlockBasedGridByte(width, height, depth, 0.001, 0.001);
        HashSet<VoxelCoordinate> vcSetMat1 = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetMat2 = new HashSet<VoxelCoordinate>();

        for (int x=0; x<width; x++) {
            grid.setData(x, 2, 2, Grid.EXTERIOR, mat1);
            vcSetMat1.add(new VoxelCoordinate(x, 2, 2));

            grid.setData(x, 5, 6, Grid.INTERIOR, mat2);
            vcSetMat2.add(new VoxelCoordinate(x, 5, 6));
        }

        FindIterateTester ft = new FindIterateTester(vcSetMat1);
        grid.find(mat1, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat1,
                ft.foundAllVoxels());

        ft = new FindIterateTester(vcSetMat2);
        grid.find(mat2, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat2,
                ft.foundAllVoxels());

        // make sure that finding a voxel not in the list returns false
        grid.setData(10, 6, 2, Grid.EXTERIOR, mat1);
        ft = new FindIterateTester(vcSetMat1);
        grid.find(mat1, ft);

        assertFalse("Found material iterator should return false",
                ft.foundAllVoxels());

        // make sure that not finding a voxel in the list returns false
        grid.setData(1, 5, 6, Grid.EXTERIOR, mat1);
        ft = new FindIterateTester(vcSetMat2);
        grid.find(mat1, ft);

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

        grid = new BlockBasedGridByte(width, height, depth, 0.001, 0.001);
        vcSetMat1 = new HashSet<VoxelCoordinate>();

        for (int i=0; i<coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.EXTERIOR, mat1);
            vcSetMat1.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        ft = new FindIterateTester(vcSetMat1);
        grid.find(mat1, ft);

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
        int mat1 = 1;
        int mat2 = 2;

        Grid grid = new BlockBasedGridByte(width, height, depth, 0.001, 0.001);
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

        FindIterateTester ft = new FindIterateTester(vcSetMat1);
        grid.findInterruptible(mat1, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat1,
                ft.foundAllVoxels());

        ft = new FindIterateTester(vcSetMat2);
        grid.findInterruptible(mat2, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat2,
                ft.foundAllVoxels());

        // make sure that findInterruptible stops interating when voxel is not found
        // do this by adding a new material voxel
        grid.setData(5, 2, 2, Grid.OUTSIDE, 0);
        grid.setData(1, 3, 3, Grid.EXTERIOR, mat1);
        ft = new FindIterateTester(vcSetMat1);
        grid.findInterruptible(mat1, ft);

        assertFalse("Found material interruptible iterator should return false",
                ft.foundAllVoxels());
        assertTrue("Found material interruptible did not get interrupted ",
                ft.getIterateCount() < vcSetMat1.size());

        // make sure that not finding a voxel in the list returns false
        // do this by changing one of the interior voxels to EXTERIOR state
        grid.setData(1, 5, 6, Grid.EXTERIOR, mat1);
        ft = new FindIterateTester(vcSetMat2);
        grid.findInterruptible(mat2, ft);

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

        grid = new BlockBasedGridByte(width, height, depth, 0.001, 0.001);
        vcSetMat1 = new HashSet<VoxelCoordinate>();

        for (int i=0; i<coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.EXTERIOR, mat1);
            vcSetMat1.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        ft = new FindIterateTester(vcSetMat1);
        grid.findInterruptible(mat1, ft);

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
        int mat1 = 1;
        int mat2 = 2;

        Grid grid = new BlockBasedGridByte(width, height, depth, 0.001, 0.001);
        HashSet<VoxelCoordinate> vcSetExtMat1 = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetIntMat2 = new HashSet<VoxelCoordinate>();

        for (int x=0; x<width; x++) {
            grid.setData(x, 2, 2, Grid.EXTERIOR, mat1);
            vcSetExtMat1.add(new VoxelCoordinate(x, 2, 2));

            grid.setData(x, 5, 6, Grid.INTERIOR, mat2);
            vcSetIntMat2.add(new VoxelCoordinate(x, 5, 6));
        }

        FindIterateTester ft = new FindIterateTester(vcSetExtMat1);
        grid.find(VoxelClasses.EXTERIOR, mat1, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state and material " + mat1,
                ft.foundAllVoxels());

        ft = new FindIterateTester(vcSetIntMat2);
        grid.find(VoxelClasses.INTERIOR, mat2, ft);

        assertTrue("Found state iterator did not find all voxels with INTERIOR state and material " + mat2,
                ft.foundAllVoxels());

        // make sure that finding a voxel not in the list returns false
        grid.setData(10, 6, 2, Grid.EXTERIOR, mat1);
        ft = new FindIterateTester(vcSetExtMat1);
        grid.find(VoxelClasses.EXTERIOR, mat1, ft);

        assertFalse("Found state and material iterator should return false",
                ft.foundAllVoxels());

        // make sure that not finding a voxel in the list returns false
        grid.setData(1, 5, 6, Grid.EXTERIOR, mat1);
        ft = new FindIterateTester(vcSetIntMat2);
        grid.find(VoxelClasses.INTERIOR, ft);

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

        grid = new BlockBasedGridByte(width, height, depth, 0.001, 0.001);
        vcSetExtMat1 = new HashSet<VoxelCoordinate>();

        for (int i=0; i<coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.EXTERIOR, mat1);
            vcSetExtMat1.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        ft = new FindIterateTester(vcSetExtMat1);
        grid.find(VoxelClasses.EXTERIOR, mat1, ft);

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
        int mat1 = 1;
        int mat2 = 2;

        Grid grid = new BlockBasedGridByte(width, height, depth, 0.001, 0.001);
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

        FindIterateTester ft = new FindIterateTester(vcSetExtMat1);
        grid.findInterruptible(VoxelClasses.EXTERIOR, mat1, ft);
        assertTrue("Found iterator did not find all voxels with EXTERIOR state and material " + mat1,
                ft.foundAllVoxels());

        ft = new FindIterateTester(vcSetIntMat1);
        grid.findInterruptible(VoxelClasses.INTERIOR, mat1, ft);
        assertTrue("Found iterator did not find all voxels with INTERIOR state and material " + mat1,
                ft.foundAllVoxels());

        ft = new FindIterateTester(vcSetExtMat2);
        grid.findInterruptible(VoxelClasses.EXTERIOR, mat2, ft);
        assertTrue("Found iterator did not find all voxels with EXTERIOR state and material " + mat2,
                ft.foundAllVoxels());

        // make sure that findInterruptible stops interating when voxel is not found
        // do this by adding a new material voxel
        grid.setData(5, 2, 2, Grid.OUTSIDE, 0);
        grid.setData(1, 3, 3, Grid.EXTERIOR, mat1);
        ft = new FindIterateTester(vcSetExtMat1);
        grid.findInterruptible(VoxelClasses.EXTERIOR, mat1, ft);

        assertFalse("Found state and material interruptible iterator should return false",
                ft.foundAllVoxels());
        assertTrue("Found state and material interruptible did not get interrupted ",
                ft.getIterateCount() < vcSetExtMat1.size());

        // make sure that not finding a voxel in the list returns false
        // do this by changing one of the interior and mat2 voxels
        grid.setData(1, 5, 6, Grid.EXTERIOR, mat1);
        ft = new FindIterateTester(vcSetExtMat2);
        grid.findInterruptible(VoxelClasses.EXTERIOR, mat2, ft);

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

        grid = new BlockBasedGridByte(width, height, depth, 0.001, 0.001);
        vcSetExtMat1 = new HashSet<VoxelCoordinate>();

        for (int i=0; i<coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.EXTERIOR, mat1);
            vcSetExtMat1.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        ft = new FindIterateTester(vcSetExtMat1);
        grid.findInterruptible(VoxelClasses.EXTERIOR, mat1, ft);

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

        Grid grid = new BlockBasedGridByte(xWorldCoord, yWorldCoord, zWorldCoord, voxelWidth, sliceHeight);

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

        Grid grid = new BlockBasedGridByte(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight);

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

        Grid grid = new BlockBasedGridByte(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight);

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
        Grid grid = new BlockBasedGridByte(50, 25, 70, 0.05, sliceHeight);
        assertEquals("Slice height is not " + sliceHeight, sliceHeight, grid.getSliceHeight());

        // world coordinates
        grid = new BlockBasedGridByte(0.12, 0.11, 0.12, 0.05, sliceHeight);
        assertEquals("Slice height is not" + sliceHeight, sliceHeight, grid.getSliceHeight());
    }

    /**
     * Test getVoxelSize with both constructor methods.
     */
    public void testGetVoxelSize() {
        double voxelSize = 0.025;

        // voxel coordinates
        Grid grid = new BlockBasedGridByte(50, 25, 70, voxelSize, 0.01);
        assertEquals("Voxel size is not " + voxelSize, voxelSize, grid.getVoxelSize());

        // world coordinates
        grid = new BlockBasedGridByte(0.12, 0.11, 0.12, voxelSize, 0.01);
        assertEquals("Voxel size is not " + voxelSize, voxelSize, grid.getVoxelSize());
    }
    
    /**
     * Test setMaterial.
     */
    public void testSetMaterial() {
    	int size = 10;
    	
        Grid grid = new BlockBasedGridByte(size, size, size, 0.001, 0.001);
        grid.setData(0, 0, 0, Grid.INTERIOR, 1);
        grid.setData(9, 9, 9, Grid.EXTERIOR, 2);
        grid.setData(5, 0, 7, Grid.INTERIOR, 3);
        
        grid.setMaterial(0, 0, 0, 10);
        grid.setMaterial(9, 9, 9, 11);
        grid.setMaterial(5, 0, 7, 12);

        // check that the material changed, but the state did not
        assertEquals("Material should be ", 10, grid.getMaterial(0, 0, 0));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0, 0, 0));
        
        assertEquals("Material should be ", 11, grid.getMaterial(9, 9, 9));
        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(9, 9, 9));
        
        assertEquals("Material should be ", 12, grid.getMaterial(5, 0, 7));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(5, 0, 7));
    }
    
    /**
     * Test setState.
     */
    public void testSetState() {
    	int size = 10;
    	
        Grid grid = new BlockBasedGridByte(size, size, size, 0.001, 0.001);
        grid.setData(0, 0, 0, Grid.INTERIOR, 1);
        grid.setData(9, 9, 9, Grid.EXTERIOR, 2);
        grid.setData(5, 0, 7, Grid.INTERIOR, 3);
        
        grid.setState(0, 0, 0, Grid.EXTERIOR);
        grid.setState(9, 9, 9, Grid.INTERIOR);
        grid.setState(5, 0, 7, Grid.EXTERIOR);

        // check that the state changed, but the material did not
        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(0, 0, 0));
        assertEquals("Material should be ", 1, grid.getMaterial(0, 0, 0));

        assertEquals("State should be ", Grid.INTERIOR, grid.getState(9, 9, 9));
        assertEquals("Material should be ", 2, grid.getMaterial(9, 9, 9));

        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(5, 0, 7));
        assertEquals("Material should be ", 3, grid.getMaterial(5, 0, 7));
    }
    
    /**
     * Test reassignMaterial.
     */
    public void testReassignMaterial() {
        int size = 20;

        Grid grid = new BlockBasedGridByte(size,size,size,0.001, 0.001);

        // Fill voxels such that it looks like:
        //
        //      2  11111 
        //      2 
        //      2  33 33
        //
        setX(grid, 10, 10, Grid.EXTERIOR, 1, 8, 12);
        setX(grid, 8, 10, Grid.INTERIOR, 3, 8, 12);
        grid.setState(10, 8, 10, Grid.OUTSIDE);
        setY(grid, 5, 10, Grid.EXTERIOR, 2, 8, 10);

        int newMaterial = 10;
        
        // reassign a non-existing material
        grid.reassignMaterial(new int[] {50}, newMaterial);
        assertEquals(0, grid.findCount(50));
        assertEquals(5, grid.findCount(1));
        assertEquals(3, grid.findCount(2));
        assertEquals(4, grid.findCount(3));

        // reassign a single existing material
        // check that the original material count is 0
        // check that the material has changed for the set positions
        grid.reassignMaterial(new int[] {1}, newMaterial);
        
        assertEquals(0, grid.findCount(1));
        
        for (int i=8; i<=12; i++) {
            assertEquals("State should be ", Grid.EXTERIOR, grid.getState(i, 10, 10));
            assertEquals("Material should be ", newMaterial, grid.getMaterial(i, 10, 10));
        }
        
        // reassign several material
        // check that the original material count is 0
        // check that the material has changed for the set positions
        newMaterial = 20;
        grid.reassignMaterial(new int[] {2, 3, 10}, newMaterial);
        
        assertEquals(0, grid.findCount(2));
        assertEquals(0, grid.findCount(3));
        assertEquals(0, grid.findCount(10));
        
        for (int i=8; i<=12; i++) {
            assertEquals("State should be ", Grid.EXTERIOR, grid.getState(i, 10, 10));
            assertEquals("Material should be ", newMaterial, grid.getMaterial(i, 10, 10));
        }

        for (int i=8; i<=9; i++) {
            assertEquals("State should be ", Grid.INTERIOR, grid.getState(i, 8, 10));
            assertEquals("Material should be ", newMaterial, grid.getMaterial(i, 8, 10));
        }
        
        for (int i=11; i<=12; i++) {
            assertEquals("State should be ", Grid.INTERIOR, grid.getState(i, 8, 10));
            assertEquals("Material should be ", newMaterial, grid.getMaterial(i, 8, 10));
        }
        
        for (int i=8; i<=10; i++) {
            assertEquals("State should be ", Grid.EXTERIOR, grid.getState(5, i, 10));
            assertEquals("Material should be ", newMaterial, grid.getMaterial(5, i, 10));
        }
    }
    
    /**
     * Test clone.
     */
    public void testClone() {
    	int size = 10;
    	double voxelSize = 0.002;
    	double sliceHeight = 0.001;
    	
        Grid grid = new BlockBasedGridByte(size, size, size, voxelSize, sliceHeight);
        grid.setData(0, 0, 0, Grid.INTERIOR, 1);
        grid.setData(9, 9, 9, Grid.EXTERIOR, 2);
        grid.setData(5, 0, 7, Grid.INTERIOR, 3);
        
        Grid grid2 = (BlockBasedGridByte) grid.clone();

        assertEquals("Voxel size should be ", voxelSize, grid2.getVoxelSize());
        assertEquals("Slight height should be ", sliceHeight, grid2.getSliceHeight());
        
        // check that the state and material are set
        assertEquals("State should be ", Grid.INTERIOR, grid2.getState(0, 0, 0));
        assertEquals("Material should be ", 1, grid2.getMaterial(0, 0, 0));

        assertEquals("State should be ", Grid.EXTERIOR, grid2.getState(9, 9, 9));
        assertEquals("Material should be ", 2, grid2.getMaterial(9, 9, 9));

        assertEquals("State should be ", Grid.INTERIOR, grid2.getState(5, 0, 7));
        assertEquals("Material should be ", 3, grid2.getMaterial(5, 0, 7));
    }

    /**
     * Test that remove material removes all specified material
     */
    public void testRemoveMaterialIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;
        int mat1 = 1;
        int mat2 = 2;

        Grid grid = new BlockBasedGridByte(width, height, depth, 0.001, 0.001);
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
        FindIterateTester ft = new FindIterateTester(vcSetMat1);
        grid.find(mat1, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat1,
                ft.foundAllVoxels());

        // remove all mat1
        grid.removeMaterial(mat1);

        // check that find mat1 returns false and iterate count returns zero
        ft = new FindIterateTester(vcSetMat1);
        grid.find(mat1, ft);

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
                    grid.getMaterial(vc.getX(), vc.getY(), vc.getZ()));
        }

        // make sure other material has not been removed
        ft = new FindIterateTester(vcSetMat2);
        grid.find(mat2, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat2,
                ft.foundAllVoxels());

    }

    /**
     * A voxel of the class requested has been found.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public void found(int x, int y, int z, VoxelData vd) {
        allCount++;

        if (vd.getState() == Grid.EXTERIOR) {
            mrkCount++;
            extCount++;
        } else if (vd.getState() == Grid.INTERIOR) {
            mrkCount++;
            intCount++;
        } else {
            outCount++;
        }

    }

    /**
     * A voxel of the class requested has been found.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public boolean foundInterruptible(int x, int y, int z, VoxelData vd) {
        // ignore
        return true;
    }

    /**
     * Set the X values of a grid.
     *
     * @param state The new state
     * @param mat The new material
     */
    protected static void setX(Grid grid, int y, int z, byte state, byte mat, int startIndex, int endIndex) {
        for(int x=startIndex; x <= endIndex; x++) {
            grid.setData(x,y,z, state, mat);
        }
    }

    /**
     * Resets the voxel counts.
     */
    private void resetCounts() {
        allCount = 0;
        mrkCount = 0;
        extCount = 0;
        intCount = 0;
        outCount = 0;
    }
}