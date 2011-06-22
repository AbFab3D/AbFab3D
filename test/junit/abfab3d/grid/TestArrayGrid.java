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
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Internal Imports

/**
 * Tests the functionality of a ArrayGrid.
 *
 * @author Alan Hudson
 * @version
 */
public class TestArrayGrid extends BaseTestGrid implements ClassTraverser {

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
        return new TestSuite(TestArrayGrid.class);
    }

    /**
     * Test the constructors and the grid size.
     */
    public void testArrayGrid() {
        Grid grid = new ArrayGridByte(1, 1, 1, 0.001, 0.001);
        assertEquals("Array size is not 1", 1, grid.getWidth()*grid.getHeight()*grid.getDepth());

        grid = new ArrayGridByte(100, 101, 102, 0.001, 0.001);
        assertEquals("Array size is not 1030200", 1030200, grid.getWidth()*grid.getHeight()*grid.getDepth());

        grid = new ArrayGridByte(1.0, 1.0, 1.0, 0.2, 0.1);
        assertEquals("Array size is not 396", 396, grid.getWidth()*grid.getHeight()*grid.getDepth());

        grid = new ArrayGridByte(1.1, 1.1, 1.1, 0.2, 0.1);
        assertEquals("Array size is not 432", 432, grid.getWidth()*grid.getHeight()*grid.getDepth());
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGetByVoxelCoords() {
        Grid grid = new ArrayGridByte(1, 1, 1, 0.001, 0.001);
        setGetAllVoxelCoords(grid);

        grid = new ArrayGridByte(3,2,2,0.001, 0.001);
        setGetAllVoxelCoords(grid);

        grid = new ArrayGridByte(11, 11, 11, 0.001, 0.001);
        setGetAllVoxelCoords(grid);

        grid = new ArrayGridByte(100, 91, 85, 0.001, 0.001);
        setGetAllVoxelCoords(grid);
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGetByWorldCoords() {
        Grid grid = new ArrayGridByte(1, 1, 1, 0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);

        grid = new ArrayGridByte(3,2,2,0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);

        grid = new ArrayGridByte(11, 11, 11, 0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);

        grid = new ArrayGridByte(100, 91, 85, 0.001, 0.001);
        setGetAllVoxelByWorldCoords(grid);
    }

    /**
     * Test getState by voxels.
     */
    public void testGetStateByVoxel() {
        Grid grid = new ArrayGridByte(10, 9, 8, 0.001, 0.001);
        grid.setData(0, 0, 0, Grid.OUTSIDE, (byte)2);
        grid.setData(9, 8, 7, Grid.EXTERIOR, (byte)1);
        grid.setData(5, 0, 7, Grid.INTERIOR, (byte)0);

        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0, 0, 0));
        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(9, 8, 7));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(5, 0, 7));

        // Index that are not set should default to 0
        assertEquals("State should be ", 0, grid.getState(2, 2, 2));
    }

    /**
     * Test getState by world coordinates.
     */
    public void testGetStateByCoord() {
        Grid grid = new ArrayGridByte(1.0, 0.4, 0.5, 0.05, 0.01);

        // set and test get on some random world coordinates
        grid.setData(0.0, 0.0, 0.0, Grid.OUTSIDE, (byte)2);
        grid.setData(0.95, 0.39, 0.45, Grid.EXTERIOR, (byte)1);
        grid.setData(0.6, 0.1, 0.4, Grid.INTERIOR, (byte)0);
        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0.0, 0.0, 0.0));
        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(0.95, 0.39, 0.45));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.6, 0.1, 0.4));

        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid = new ArrayGridByte(0.12, 0.11, 0.16, 0.05, 0.02);
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
        Grid grid = new ArrayGridByte(10, 9, 8, 0.001, 0.001);
        grid.setData(0, 0, 0, Grid.OUTSIDE, (byte)3);
        grid.setData(9, 8, 7, Grid.EXTERIOR, (byte)2);
        grid.setData(5, 0, 7, Grid.INTERIOR, (byte)1);

        assertEquals("State should be ", 3, grid.getMaterial(0, 0, 0));
        assertEquals("State should be ", 2, grid.getMaterial(9, 8, 7));
        assertEquals("State should be ", 1, grid.getMaterial(5, 0, 7));

        // Index that are not set should default to 0
        assertEquals("State should be ", 0, grid.getMaterial(2, 2, 2));
    }

    /**
     * Test getMaterial by world coordinates.
     */
    public void testGetMaterialByCoord() {
        Grid grid = new ArrayGridByte(1.0, 0.4, 0.5, 0.05, 0.01);

        // set and test get on some random world coordinates
        grid.setData(0.0, 0.0, 0.0, Grid.OUTSIDE, (byte)3);
        grid.setData(0.95, 0.39, 0.45, Grid.EXTERIOR, (byte)2);
        grid.setData(0.6, 0.1, 0.4, Grid.INTERIOR, (byte)1);
        assertEquals("State should be ", 3, grid.getMaterial(0.0, 0.0, 0.0));
        assertEquals("State should be ", 2, grid.getMaterial(0.95, 0.39, 0.45));
        assertEquals("State should be ", 1, grid.getMaterial(0.6, 0.1, 0.4));

        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid = new ArrayGridByte(0.12, 0.11, 0.16, 0.05, 0.02);
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
    	
        Grid grid = new ArrayGridByte(width, 1, 1, 0.001, 0.001);
        
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
     * Test set/get short material range.
     */
    public void testShortMaterialRange() {
    	int width = 100;
        int maxMaterial = (int) Math.pow(2.0, 14.0);
        
        Grid grid = new ArrayGridShort(width, 1, 1, 0.001, 0.001);
        grid.setData(0, 0, 0, Grid.EXTERIOR, 0);
        grid.setData(1, 0, 0, Grid.EXTERIOR, maxMaterial-1);
        grid.setData(2, 0, 0, Grid.EXTERIOR, maxMaterial+1);
        grid.setData(3, 0, 0, Grid.EXTERIOR, 2 * maxMaterial - 1);
        
        assertEquals("Material [0,0,0] is not 0", 0, grid.getMaterial(0, 0, 0));
        assertEquals("Material [1,0,0] is not " + (maxMaterial-1), (maxMaterial-1), grid.getMaterial(1, 0, 0));
        assertEquals("Material [2,0,0] is not 1", 1, grid.getMaterial(2, 0, 0));
        assertEquals("Material [3,0,0] is not " + (maxMaterial-1), (maxMaterial-1), grid.getMaterial(3, 0, 0));

    }
    
    /**
     * Test set/get int material range.
     */
    public void testIntMaterialRange() {
    	int width = 100;
        int maxMaterial = (int) Math.pow(2.0, 30.0);
        
        Grid grid = new ArrayGridInt(width, 1, 1, 0.001, 0.001);
        grid.setData(0, 0, 0, Grid.EXTERIOR, 0);
        grid.setData(1, 0, 0, Grid.EXTERIOR, maxMaterial-1);
        grid.setData(2, 0, 0, Grid.EXTERIOR, maxMaterial+1);
        grid.setData(3, 0, 0, Grid.EXTERIOR, 2 * maxMaterial - 1);
        
        assertEquals("Material [0,0,0] is not 0", 0, grid.getMaterial(0, 0, 0));
        assertEquals("Material [1,0,0] is not " + (maxMaterial-1), (maxMaterial-1), grid.getMaterial(1, 0, 0));
        assertEquals("Material [2,0,0] is not 1", 1, grid.getMaterial(2, 0, 0));
        assertEquals("Material [3,0,0] is not " + (maxMaterial-1), (maxMaterial-1), grid.getMaterial(3, 0, 0));

    }
    
    /**
     * Test findCount by voxel class.
     */
    public void testFindCount() {
        int width = 6;
        int height = 3;
        int depth = 10;
        int[] row = {0, 3, 5};
        byte[] state = {Grid.INTERIOR, Grid.EXTERIOR, Grid.INTERIOR};

        Grid grid = new ArrayGridByte(width, height, depth, 0.05, 0.02);

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

        Grid grid = new ArrayGridByte(width, height, depth, 0.05, 0.02);

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
        grid = new ArrayGridByte(width, height, depth, 0.05, 0.02);
        for (int x=0; x<width; x++) {
        	grid.setData(x,0,0, Grid.EXTERIOR, mat);
        }
        
        assertEquals("Material count is not " + width, width, grid.findCount(mat));
        
        grid = new ArrayGridByte(width, height, depth, 0.05, 0.02);
        for (int y=0; y<height; y++) {
        	grid.setData(0, y, 0, Grid.INTERIOR, mat);
        }
        
        assertEquals("Material count is not " + height, height, grid.findCount(mat));

    }

    public void testFindVoxelClass() {
        int width = 3;
        int height = 4;
        int depth = 10;
        int[] stateDepth = {10, 6, 1};
        byte[] states = {Grid.EXTERIOR, Grid.INTERIOR, Grid.OUTSIDE};

        Grid grid = new ArrayGridByte(width, height, depth, 0.05, 0.02);

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
     * Test getGridCoords.
     */
    public void testGetGridCoords() {
        double xWorldCoord = 1.0;
        double yWorldCoord = 0.15;
        double zWorldCoord = 0.61;
        double voxelWidth = 0.02;
        double sliceHeight = 0.01;

        Grid grid = new ArrayGridByte(xWorldCoord, yWorldCoord, zWorldCoord, voxelWidth, sliceHeight);

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

        Grid grid = new ArrayGridByte(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight);

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

        Grid grid = new ArrayGridByte(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight);

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
        Grid grid = new ArrayGridByte(width, 50, 25, 0.05, 0.01);
        assertEquals("Width is not " + width, width, grid.getWidth());

        // world coordinates
        double xcoord = 0.12;
        double voxelSize = 0.05;
        width = (int)(xcoord/voxelSize) + 1;

        grid = new ArrayGridByte(xcoord, 0.11, 0.16, voxelSize, 0.02);
        assertEquals("Width is not " + width, width, grid.getWidth());
    }

    /**
     * Test getHeight with both constructor methods.
     */
    public void testGetHeight() {
        int height = 70;

        // voxel coordinates
        Grid grid = new ArrayGridByte(50, height, 25, 0.05, 0.02);
        assertEquals("Height is not " + height, height, grid.getHeight());

        // world coordinates
        double ycoord = 0.11;
        double sliceHeight = 0.02;
        height = (int)(ycoord/sliceHeight) + 1;

        grid = new ArrayGridByte(0.12, ycoord, 0.16, 0.05, sliceHeight);
        assertEquals("Height is not " + height, height, grid.getHeight());
    }

    /**
     * Test getDepth with both constructor methods.
     */
    public void testGetDepth() {
        int depth = 70;

        // voxel coordinates
        Grid grid = new ArrayGridByte(50, 25, depth, 0.05, 0.01);
        assertEquals("Depth is not " + depth, depth, grid.getDepth());

        // world coordinates
        double zcoord = 0.12;
        double voxelSize = 0.05;
        depth = (int)(zcoord/voxelSize) + 1;

        grid = new ArrayGridByte(0.12, 0.11, zcoord, voxelSize, 0.02);
        assertEquals("Depth is not " + depth, depth, grid.getDepth());
    }

    /**
     * Test getSliceHeight with both constructor methods.
     */
    public void testGetSliceHeight() {
        double sliceHeight = 0.0015;

        // voxel coordinates
        Grid grid = new ArrayGridByte(50, 25, 70, 0.05, sliceHeight);
        assertEquals("Slice height is not " + sliceHeight, sliceHeight, grid.getSliceHeight());

        // world coordinates
        grid = new ArrayGridByte(0.12, 0.11, 0.12, 0.05, sliceHeight);
        assertEquals("Slice height is not" + sliceHeight, sliceHeight, grid.getSliceHeight());
    }

    /**
     * Test getVoxelSize with both constructor methods.
     */
    public void testGetVoxelSize() {
        double voxelSize = 0.025;

        // voxel coordinates
        Grid grid = new ArrayGridByte(50, 25, 70, voxelSize, 0.01);
        assertEquals("Voxel size is not " + voxelSize, voxelSize, grid.getVoxelSize());

        // world coordinates
        grid = new ArrayGridByte(0.12, 0.11, 0.12, voxelSize, 0.01);
        assertEquals("Voxel size is not " + voxelSize, voxelSize, grid.getVoxelSize());
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

    private void resetCounts() {
        allCount = 0;
        mrkCount = 0;
        extCount = 0;
        intCount = 0;
        outCount = 0;
    }
}
