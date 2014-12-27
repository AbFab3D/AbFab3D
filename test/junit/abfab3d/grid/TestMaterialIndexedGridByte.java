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
import abfab3d.grid.Grid.VoxelClasses;
import junit.framework.Test;
import junit.framework.TestSuite;

// Internal Imports

/**
 * Tests the functionality of a MaterialIndexedAttributeGridByte.
 *
 * @author Alan Hudson
 * @version
 */
public class TestMaterialIndexedGridByte extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestMaterialIndexedGridByte.class);
    }

    /**
     * Test the constructors and the grid size.
     */
    public void testMaterialIndexedGrid() {
        AttributeGrid grid =new MaterialIndexedAttributeGridByte(1, 1, 1, 0.001, 0.001);
        assertEquals("Array size is not 1", 1, grid.getWidth()*grid.getHeight()*grid.getDepth());

        grid = new MaterialIndexedAttributeGridByte(100, 101, 102, 0.001, 0.001);
        assertEquals("Array size is not 1030200", 1030200, grid.getWidth()*grid.getHeight()*grid.getDepth());

        grid = new MaterialIndexedAttributeGridByte(1.0, 1.0, 1.0, 0.2, 0.1);
        assertEquals("Array size is not 250", 250, grid.getWidth()*grid.getHeight()*grid.getDepth());

        grid = new MaterialIndexedAttributeGridByte(1.1, 1.1, 1.1, 0.2, 0.1);
        assertEquals("Array size is not 396", 396, grid.getWidth()*grid.getHeight()*grid.getDepth());
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGetByVoxelCoords() {
        AttributeGrid grid =new MaterialIndexedAttributeGridByte(1, 1, 1, 0.001, 0.001);
        setGetAllVoxelCoords(grid);

        grid = new MaterialIndexedAttributeGridByte(3,2,2,0.001, 0.001);
        setGetAllVoxelCoords(grid);

        grid = new MaterialIndexedAttributeGridByte(11, 11, 11, 0.001, 0.001);
        setGetAllVoxelCoords(grid);

/*
        // Too slow for smoke tests
        grid = new MaterialIndexedAttributeGridByte(100, 91, 85, 0.001, 0.001);
        setGetAllVoxelCoords(grid);
*/
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGetByWorldCoords() {
System.out.println("test2");
        AttributeGrid grid =new MaterialIndexedAttributeGridByte(1, 1, 1, 0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);

        grid = new MaterialIndexedAttributeGridByte(3,2,2,0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);

        grid = new MaterialIndexedAttributeGridByte(11, 11, 11, 0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);

/*
        // Too slow for smoke tests
        grid = new MaterialIndexedAttributeGridByte(100, 91, 85, 0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);
*/
    }

    /**
     * Test getState by voxels.
     */
    public void testGetStateByVoxel() {
System.out.println("test3");
        AttributeGrid grid =new MaterialIndexedAttributeGridByte(10, 9, 8, 0.001, 0.001);
        grid.setData(0, 0, 0, Grid.OUTSIDE, (byte)2);
        grid.setData(9, 8, 7, Grid.INSIDE, (byte)1);
        grid.setData(5, 0, 7, Grid.INSIDE, (byte)0);

        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0, 0, 0));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(9, 8, 7));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(5, 0, 7));

        // Index that are not set should default to 0
        assertEquals("State should be ", 0, grid.getState(2, 2, 2));
    }

    /**
     * Test getState by world coordinates.
     */
    public void testGetStateByCoord() {
        AttributeGrid grid =new MaterialIndexedAttributeGridByte(1.0, 0.4, 0.5, 0.05, 0.01);

        // set and test get on some random world coordinates
        grid.setData(0.0, 0.0, 0.0, Grid.OUTSIDE, (byte)2);
        grid.setData(0.95, 0.39, 0.45, Grid.INSIDE, (byte)1);
        grid.setData(0.6, 0.1, 0.4, Grid.INSIDE, (byte)0);
        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0.0, 0.0, 0.0));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.95, 0.39, 0.45));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.6, 0.1, 0.4));

        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid = new MaterialIndexedAttributeGridByte(0.15, 0.12, 0.20, 0.05, 0.02);
        grid.setData(0.06, 0.07, 0.08, Grid.INSIDE, (byte)2);
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.05, 0.06, 0.05));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.0999, 0.06, 0.05));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.05, 0.0799, 0.05));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.05, 0.06, 0.0999));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.0999, 0.0799, 0.0999));
        assertEquals("State should be ", 0, grid.getState(0.0499, 0.06, 0.05));
        assertEquals("State should be ", 0, grid.getState(0.05, 0.0599, 0.05));
        assertEquals("State should be ", 0, grid.getState(0.05, 0.06, 0.0499));
        assertEquals("State should be ", 0, grid.getState(0.1, 0.06, 0.05));
        assertEquals("State should be ", 0, grid.getState(0.05, 0.08, 0.05));
        assertEquals("State should be ", 0, grid.getState(0.05, 0.06, 0.1));

        // set data for beginning voxel 0,0,0 and test the bounds
        grid.setData(0.0, 0.0, 0.0, Grid.INSIDE, (byte)2);
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.0, 0.0, 0.0));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.0499, 0.0, 0.0));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.0, 0.0199, 0.0));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.0, 0.0, 0.0499));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.0499, 0.0199, 0.0499));
        assertEquals("State should be ", 0, grid.getState(0.05, 0.0, 0.0));
        assertEquals("State should be ", 0, grid.getState(0.0, 0.02, 0.0));
        assertEquals("State should be ", 0, grid.getState(0.0, 0.0, 0.05));

        // set data for last voxel 2,5,3 and test the bounds
        grid.setData(0.149, 0.119, 0.199, Grid.INSIDE, (byte)2);
//        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.1, 0.1, 0.15));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.149, 0.1, 0.151));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.1, 0.119, 0.151));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.1, 0.1, 0.199));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.149, 0.119, 0.199));
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
        AttributeGrid grid =new MaterialIndexedAttributeGridByte(10, 9, 8, 0.001, 0.001);
        grid.setData(9, 8, 7, Grid.INSIDE, (byte)2);
        grid.setData(5, 0, 7, Grid.INSIDE, (byte)1);

        assertEquals("State should be ", 2, grid.getAttribute(9, 8, 7));
        assertEquals("State should be ", 1, grid.getAttribute(5, 0, 7));

        // Index that are not set should default to 0
        assertEquals("State should be ", 0, grid.getAttribute(2, 2, 2));
    }

    /**
     * Test getAttribute by world coordinates.
     */
    public void testGetMaterialByCoord() {
        AttributeGrid grid =new MaterialIndexedAttributeGridByte(1.0, 0.4, 0.5, 0.05, 0.01);

        // set and test get on some random world coordinates
        grid.setData(0.0, 0.0, 0.0, Grid.OUTSIDE, (byte)3);
        grid.setData(0.95, 0.39, 0.45, Grid.INSIDE, (byte)2);
        grid.setData(0.6, 0.1, 0.4, Grid.INSIDE, (byte)1);
        assertEquals("State should be ", 3, grid.getAttribute(0.0, 0.0, 0.0));
        assertEquals("State should be ", 2, grid.getAttribute(0.95, 0.39, 0.45));
        assertEquals("State should be ", 1, grid.getAttribute(0.6, 0.1, 0.4));

        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid = new MaterialIndexedAttributeGridByte(0.15, 0.12, 0.20, 0.05, 0.02);
        grid.setData(0.06, 0.07, 0.08, Grid.INSIDE, (byte)2);
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
        grid.setData(0.0, 0.0, 0.0, Grid.INSIDE, (byte)5);
        assertEquals("State should be ", 5, grid.getAttribute(0.0, 0.0, 0.0));
        assertEquals("State should be ", 5, grid.getAttribute(0.0499, 0.0, 0.0));
        assertEquals("State should be ", 5, grid.getAttribute(0.0, 0.0199, 0.0));
        assertEquals("State should be ", 5, grid.getAttribute(0.0, 0.0, 0.0499));
        assertEquals("State should be ", 5, grid.getAttribute(0.0499, 0.0199, 0.0499));
        assertEquals("State should be ", 0, grid.getAttribute(0.05, 0.0, 0.0));
        assertEquals("State should be ", 0, grid.getAttribute(0.0, 0.02, 0.0));
        assertEquals("State should be ", 0, grid.getAttribute(0.0, 0.0, 0.05));

        // set data for last voxel 2,5,3 and test the bounds
        grid.setData(0.149, 0.119, 0.199, Grid.INSIDE, (byte)12);
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
        int width = 63;
        int maxMaterial = 63;
        long mat, expectedMat;

        AttributeGrid grid =new MaterialIndexedAttributeGridByte(width, 1, 1, 0.001, 0.001);

        for (int x=0; x<width; x++) {
            grid.setData(x, 0, 0, Grid.INSIDE, x);
        }

        for (int x=0; x<width; x++) {
            mat = grid.getAttribute(x, 0, 0);
            expectedMat = x % maxMaterial;
//System.out.println("Material [" + x + ",0,0]: " + mat);
            assertEquals("Material [" + x + ",0,0] is not " + expectedMat, expectedMat, mat);
        }
    }

    /**
     * Test set/get short material range.
     */
/*
    public void testShortMaterialRange() {
        int width = 100;
        int maxMaterial = (int) Math.pow(2.0, 14.0);

        AttributeGrid grid =new MaterialIndexedAttributeGridShort(width, 1, 1, 0.001, 0.001);
        grid.setData(0, 0, 0, Grid.INSIDE, 0);
        grid.setData(1, 0, 0, Grid.INSIDE, maxMaterial-1);
        grid.setData(2, 0, 0, Grid.INSIDE, maxMaterial+1);
        grid.setData(3, 0, 0, Grid.INSIDE, 2 * maxMaterial - 1);

        assertEquals("Material [0,0,0] is not 0", 0, grid.getAttribute(0, 0, 0));
        assertEquals("Material [1,0,0] is not " + (maxMaterial-1), (maxMaterial-1), grid.getAttribute(1, 0, 0));
        assertEquals("Material [2,0,0] is not 1", 1, grid.getAttribute(2, 0, 0));
        assertEquals("Material [3,0,0] is not " + (maxMaterial-1), (maxMaterial-1), grid.getAttribute(3, 0, 0));

    }
*/
    /**
     * Test set/get long material range.
     */
/*
    public void testIntMaterialRange() {
        int width = 100;
        int maxMaterial = (int) Math.pow(2.0, 30.0);

        AttributeGrid grid =new MaterialIndexedGridInt(width, 1, 1, 0.001, 0.001);
        grid.setData(0, 0, 0, Grid.INSIDE, 0);
        grid.setData(1, 0, 0, Grid.INSIDE, maxMaterial-1);
        grid.setData(2, 0, 0, Grid.INSIDE, maxMaterial+1);
        grid.setData(3, 0, 0, Grid.INSIDE, 2 * maxMaterial - 1);

        assertEquals("Material [0,0,0] is not 0", 0, grid.getAttribute(0, 0, 0));
        assertEquals("Material [1,0,0] is not " + (maxMaterial-1), (maxMaterial-1), grid.getAttribute(1, 0, 0));
        assertEquals("Material [2,0,0] is not 1", 1, grid.getAttribute(2, 0, 0));
        assertEquals("Material [3,0,0] is not " + (maxMaterial-1), (maxMaterial-1), grid.getAttribute(3, 0, 0));

    }
*/
    /**
     * Test findCount by voxel class.
     */
    public void testFindCount() {
        int width = 6;
        int height = 3;
        int depth = 10;
        int[] row = {0, 3, 5};
        byte[] state = {Grid.INSIDE, Grid.INSIDE, Grid.INSIDE};

        AttributeGrid grid =new MaterialIndexedAttributeGridByte(width, height, depth, 0.05, 0.02);

        // set some rows to interior and exterior
        for (int y=0; y<height; y++) {
            for (int z=0; z<depth; z++) {
                grid.setData(row[0], y, z, state[0], 2);
                grid.setData(row[1], y, z, state[1], 2);
                grid.setData(row[2], y, z, state[2], 2);
            }
        }

        int expectedAllCount = width * depth * height;
        int expectedIntCount = depth * height * 2;
        int expectedExtCount = depth * height;
        int expectedMrkCount = expectedIntCount + expectedExtCount;
        int expectedOutCount = expectedAllCount - expectedIntCount - expectedExtCount;

        assertEquals("Expected total voxels is not " + expectedAllCount, expectedAllCount, grid.findCount(VoxelClasses.ALL));
        assertEquals("Expected marked voxels is not " + expectedMrkCount, expectedMrkCount, grid.findCount(VoxelClasses.INSIDE));
        assertEquals("Expected outside voxels is not " + expectedOutCount, expectedOutCount, grid.findCount(VoxelClasses.OUTSIDE));

        // change one of the interior voxel rows to outside
        for (int y=0; y<height; y++) {
            for (int z=0; z<depth; z++) {
                grid.setData(row[0], y, z, Grid.OUTSIDE, 2);
            }
        }

        expectedIntCount = depth * height;
        expectedExtCount = depth * height;
        expectedMrkCount = expectedIntCount + expectedExtCount;
        expectedOutCount = expectedAllCount - expectedIntCount - expectedExtCount;

        assertEquals("Expected total voxels is not " + expectedAllCount, expectedAllCount, grid.findCount(VoxelClasses.ALL));
        assertEquals("Expected marked voxels is not " + expectedMrkCount, expectedMrkCount, grid.findCount(VoxelClasses.INSIDE));
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

        AttributeGrid grid =new MaterialIndexedAttributeGridByte(width, height, depth, 0.05, 0.02);

        // set some material data
        for (int x=0; x<material.length; x++){
            for (int y=0; y<height; y++) {
                for (int z=0; z<materialDepth[x]; z++) {
//System.out.println(x + ", " + y + ", " + z + ": " + material[x]);
                    grid.setData(x, y, z, Grid.INSIDE, material[x]);
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
        grid = new MaterialIndexedAttributeGridByte(width, height, depth, 0.05, 0.02);
        for (int x=0; x<width; x++) {
            grid.setData(x,0,0, Grid.INSIDE, mat);
        }

        assertEquals("Material count is not " + width, width, grid.findCount(mat));

        grid = new MaterialIndexedAttributeGridByte(width, height, depth, 0.05, 0.02);
        for (int y=0; y<height; y++) {
            grid.setData(0, y, 0, Grid.INSIDE, mat);
        }

        assertEquals("Material count is not " + height, height, grid.findCount(mat));

    }

    public void testFindVoxelClass() {
        int width = 3;
        int height = 4;
        int depth = 10;
        int[] stateDepth = {10, 6, 1};
        byte[] states = {Grid.INSIDE, Grid.INSIDE, Grid.OUTSIDE};

        AttributeGrid grid =new MaterialIndexedAttributeGridByte(width, height, depth, 0.05, 0.02);

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
        grid.findAttribute(VoxelClasses.INSIDE, this);
        assertEquals("Marked voxel count is not " + expectedMrkCount, expectedMrkCount, mrkCount);

        resetCounts();
        grid.findAttribute(VoxelClasses.OUTSIDE, this);
        assertEquals("Outside voxel count is not " + expectedOutCount, expectedOutCount, outCount);
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

        AttributeGrid grid =new MaterialIndexedAttributeGridByte(xWorldCoord, yWorldCoord, zWorldCoord, voxelWidth, sliceHeight);

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

        AttributeGrid grid =new MaterialIndexedAttributeGridByte(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight);

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

        AttributeGrid grid =new MaterialIndexedAttributeGridByte(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight);

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

        assertTrue("Minimum bounds is not (" + expectedMaxX + ", " + expectedMaxY + ", " + expectedMaxZ + ")",
                maxBounds[0] == expectedMaxX &&
                maxBounds[1] == expectedMaxY &&
                maxBounds[2] == expectedMaxZ);

    }

    /**
     * Test getWidth with both constructor methods.
     */
    public void testGetWidth() {
        int width = 70;

        // voxel coordinates
        AttributeGrid grid =new MaterialIndexedAttributeGridByte(width, 50, 25, 0.05, 0.01);
        assertEquals("Width is not " + width, width, grid.getWidth());

        // world coordinates
        double xcoord = 0.12;
        double voxelSize = 0.05;
        width = BaseGrid.roundSize(xcoord/voxelSize);

        grid = new MaterialIndexedAttributeGridByte(xcoord, 0.11, 0.16, voxelSize, 0.02);
        assertEquals("Width is not " + width, width, grid.getWidth());
    }

    /**
     * Test getHeight with both constructor methods.
     */
    public void testGetHeight() {
        int height = 70;

        // voxel coordinates
        AttributeGrid grid =new MaterialIndexedAttributeGridByte(50, height, 25, 0.05, 0.02);
        assertEquals("Height is not " + height, height, grid.getHeight());

        // world coordinates
        double ycoord = 0.11;
        double sliceHeight = 0.02;
        height = BaseGrid.roundSize(ycoord/sliceHeight);

        grid = new MaterialIndexedAttributeGridByte(0.12, ycoord, 0.16, 0.05, sliceHeight);
        assertEquals("Height is not " + height, height, grid.getHeight());
    }

    /**
     * Test getDepth with both constructor methods.
     */
    public void testGetDepth() {
        int depth = 70;

        // voxel coordinates
        AttributeGrid grid =new MaterialIndexedAttributeGridByte(50, 25, depth, 0.05, 0.01);
        assertEquals("Depth is not " + depth, depth, grid.getDepth());

        // world coordinates
        double zcoord = 0.12;
        double voxelSize = 0.05;
        depth = BaseGrid.roundSize(zcoord/voxelSize);

        grid = new MaterialIndexedAttributeGridByte(0.12, 0.11, zcoord, voxelSize, 0.02);
        assertEquals("Depth is not " + depth, depth, grid.getDepth());
    }

    /**
     * Test getSliceHeight with both constructor methods.
     */
    public void testGetSliceHeight() {
        double sliceHeight = 0.0015;

        // voxel coordinates
        AttributeGrid grid =new MaterialIndexedAttributeGridByte(50, 25, 70, 0.05, sliceHeight);
        assertEquals("Slice height is not " + sliceHeight, sliceHeight, grid.getSliceHeight());

        // world coordinates
        grid = new MaterialIndexedAttributeGridByte(0.12, 0.11, 0.12, 0.05, sliceHeight);
        assertEquals("Slice height is not" + sliceHeight, sliceHeight, grid.getSliceHeight());
    }

}
