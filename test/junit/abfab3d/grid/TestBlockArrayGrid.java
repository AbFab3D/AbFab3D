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
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Internal Imports

/**
 * Tests the functionality of a BlockArrayGrid.
 *
 * @author Alan Hudson, James Gray
 * @version
 */
public class TestBlockArrayGrid extends BaseTestGrid implements ClassTraverser {

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
        return new TestSuite(TestBlockArrayGrid.class);
    }
    
    /**
     * Test f.nextpow2 function
     */
    public void testHelpFun_nextpow2() {
    assertEquals("Result should be ", 0, f.nextpow2(1));
    assertEquals("Result should be ", 2, f.nextpow2(3));
    assertEquals("Result should be ", 2, f.nextpow2(3.1));
    assertEquals("Result should be ", 5, f.nextpow2(32));
    assertEquals("Result should be ", 6, f.nextpow2(33));
    }
    
    /**
     * Test f.c2i conversion function
     */
    public void testHelpFun_coordToIndex() {
    int[] order = {1,1,1}; // 2x2x2 cube
    
    assertEquals("Result should be ", 0, f.coordToIndex(new int[] {0,0,0},order));
    assertEquals("Result should be ", 1, f.coordToIndex(new int[] {1,0,0},order));
    assertEquals("Result should be ", 2, f.coordToIndex(new int[] {0,1,0},order));
    assertEquals("Result should be ", 3, f.coordToIndex(new int[] {1,1,0},order));
    assertEquals("Result should be ", 4, f.coordToIndex(new int[] {0,0,1},order));
    assertEquals("Result should be ", 5, f.coordToIndex(new int[] {1,0,1},order));
    assertEquals("Result should be ", 6, f.coordToIndex(new int[] {0,1,1},order));
    assertEquals("Result should be ", 7, f.coordToIndex(new int[] {1,1,1},order));
    }
    
    /**
     * Test blockIndex
     */
    public void testBlockIndex() {
    BlockArrayGrid grid = new BlockArrayGrid(16, 16, 16, 1.0, 1.0, new int[] {3,3,3});
    
    assertEquals("block order ", 3, grid.BLOCK_ORDER[0]);
    assertEquals("grid order ", 1, grid.GRID_ORDER[0]);
    
    int[] gridSize = grid.gridSizeInBlocks();
    int[] blockSize = grid.blockSizeInVoxels();
    
    assertEquals("grid width in blocks ", 2, gridSize[0]);
    assertEquals("block width in voxels ", 8, blockSize[0]);
    
    assertEquals("number of blocks ", 8, grid.blocks.length);
    
    assertEquals("Result should be ", 0, grid.blockIndex(new int[] {0,0,0}));
    assertEquals("Result should be ", 1, grid.blockIndex(new int[] {15,0,0}));
    assertEquals("Result should be ", 7, grid.blockIndex(new int[] {15,15,15}));
    }
    
    /**
     * Test grid set and get
     */
    public void testSetGet() {
    BlockArrayGrid grid = new BlockArrayGrid(4096,4096,4096,1.0,1.0,new int[] {4,4,4});
    
    byte[][][] data = {{ {0,1,2},
     {3,4,5},
     {6,7,2} },
       { {5,7,3},
       {4,1,0},
       {5,4,3} },
       { {1,3,0},
       {2,5,4},
       {7,4,7} }};
    
    for (int x = 0; x < 3; x++) {
    for (int y = 0; y < 3; y++) {
    for (int z = 0; z < 3; z++) {
    grid.set(x,y,z, data[x][y][z]);
        assertEquals("Result should be ",data[x][y][z],grid.get(x,y,z));
    }
    }
    }
    }

    /**
     * Test copy constructor.
     */
    public void testBlockArrayGridCopyConstructor() {
    BlockArrayGrid grid = new BlockArrayGrid(10, 9, 8, 0.001, 0.001, new int[] {2,2,2});
    
    assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0, 0, 0));
    assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0, 0, 1));
    
        grid.setData(0, 0, 0, Grid.INTERIOR, 2);
        grid.setData(9, 8, 7, Grid.EXTERIOR, 1);
        grid.setData(5, 0, 7, Grid.INTERIOR, 0);
        
        assertEquals("Material should be ",0,grid.getMaterial(0, 0, 0));

        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0, 0, 1));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0, 0, 0));
        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(9, 8, 7));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(5, 0, 7));
        
        Grid grid2 = new BlockArrayGrid(grid);
        
        assertEquals("State should be ", Grid.OUTSIDE, grid2.getState(0, 0, 1));
        assertEquals("State should be ", Grid.INTERIOR, grid2.getState(0, 0, 0));
        assertEquals("State should be ", Grid.EXTERIOR, grid2.getState(9, 8, 7));
        assertEquals("State should be ", Grid.INTERIOR, grid2.getState(5, 0, 7));
    }
    
    /**
     * Test creating an empty grid.
     */
    public void testCreateEmpty() {
    BlockArrayGrid grid = new BlockArrayGrid(100, 101, 102, 0.001, 0.001, new int[] {2,2,2});
        
    assertEquals("block width in voxels ", 4, ((BlockArrayGrid)grid).blockSizeInVoxels()[0]);
    assertEquals("grid width in blocks ", 32, ((BlockArrayGrid)grid).gridSizeInBlocks()[0]);
    
        grid.setData(5, 5, 5, Grid.EXTERIOR, 10);
        
        Grid grid2 = grid.createEmpty(10, 11, 12, 0.002, 0.003);
        
        assertTrue("Grid type is BlockArrayGrid", grid2 instanceof BlockArrayGrid);
        assertEquals("Grid voxel size is 0.002", 0.002, grid2.getVoxelSize());
        assertEquals("Grid slice height is 0.003", 0.003, grid2.getSliceHeight());
        
        // all voxels in empty grid should be OUTSIDE state and 0 material
        assertEquals("State is not OUTSIDE for (5, 5, 5)", Grid.OUTSIDE, grid2.getState(5, 5, 5));
    }
    
    /**
     * Test set/get all data points.
     */
    public void testSetGetByVoxelCoords() {
        Grid grid = new BlockArrayGrid(8, 8, 8, 0.001, 0.001, new int[] {2,2,2});
        setGetAllVoxelCoords(grid);

        grid = new BlockArrayGrid(8, 8, 8, 0.001, 0.001, new int[] {2,2,2});
        setGetAllVoxelCoords(grid);

        grid = new BlockArrayGrid(8, 8, 8, 0.001, 0.001, new int[] {2,2,2});
        setGetAllVoxelCoords(grid);

        grid = new BlockArrayGrid(16,8,8,0.001, 0.001, new int[] {2,2,2});
        setGetAllVoxelCoords(grid);

        grid = new BlockArrayGrid(16, 16, 16, 0.001, 0.001, new int[] {2,2,2});
        setGetAllVoxelCoords(grid);

        grid = new BlockArrayGrid(100, 91, 85, 0.001, 0.001, new int[] {2,2,2});
        setGetAllVoxelCoords(grid);
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGetByWorldCoords() {
        Grid grid = new BlockArrayGrid(8, 8, 8, 0.001, 0.001, new int[] {2,2,2});
        setGetAllVoxelByWorldCoords(grid);

        grid = new BlockArrayGrid(3,2,2,0.001, 0.001, new int[] {2,2,2});
        setGetAllVoxelByWorldCoords(grid);

        grid = new BlockArrayGrid(11, 11, 11, 0.001, 0.001, new int[] {2,2,2});
        setGetAllVoxelByWorldCoords(grid);

        grid = new BlockArrayGrid(100, 91, 85, 0.001, 0.001, new int[] {2,2,2});
        setGetAllVoxelByWorldCoords(grid);
    }

    /**
     * Test getState by voxels.
     */
    public void testGetStateByVoxel() {
    BlockArrayGrid grid = new BlockArrayGrid(10, 9, 8, 0.001, 0.001, new int[] {2,2,2});
        
    grid.setData(0, 0, 0, Grid.OUTSIDE, (byte)2);
        grid.setData(9, 8, 7, Grid.EXTERIOR, (byte)1);
        grid.setData(5, 0, 7, Grid.INTERIOR, (byte)0);

        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0, 0, 0));
        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(9, 8, 7));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(5, 0, 7));

        // Index that are not set should default to 0
        assertEquals("State should be ", 0, grid.getState(7, 7, 7));
    }

    /**
     * Test getState by world coordinates.
     */
    public void testGetStateByCoord() {
    BlockArrayGrid grid = new BlockArrayGrid(1.0, 0.4, 0.5, 0.05, 0.01, new int[] {0,0,0});

    // make sure the grid is the expected size
    int xVoxels = grid.getWidth();
    int yVoxels = grid.getHeight();
    int zVoxels = grid.getDepth();
        assertTrue(xVoxels >= 20);
        assertTrue(yVoxels >= 40);
        assertTrue(zVoxels >= 10);
    
        // set and test get on some random world coordinates
        grid.setData(0.0, 0.0, 0.0, Grid.OUTSIDE, (byte)2);
        grid.setData(0.95, 0.39, 0.45, Grid.EXTERIOR, (byte)1);
        grid.setData(0.6, 0.1, 0.4, Grid.INTERIOR, (byte)0);
        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0.0, 0.0, 0.0));
        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(0.95, 0.39, 0.45));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.6, 0.1, 0.4));

        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid = new BlockArrayGrid(0.12, 0.11, 0.16, 0.05, 0.02, new int[] {0,0,0});
        grid.setData(0.06, 0.07, 0.08, Grid.INTERIOR, (byte)2);
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.05, 0.07, 0.075));
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
     * Test findCount by voxel class.
     */
    public void testFindCountByVoxelClass() {
        int width = 6;
        int height = 3;
        int depth = 10;
        int[] row = {0, 3, 5};
        byte[] state = {Grid.INTERIOR, Grid.EXTERIOR, Grid.INTERIOR};

        BlockArrayGrid grid = new BlockArrayGrid(width, height, depth, 0.05, 0.02, new int[] {2,2,2});

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
     * Test find voxels by voxel class
     */
    public void testFindVoxelClass() {
        int width = 3;
        int height = 4;
        int depth = 10;
        int[] stateDepth = {10, 6, 1};
        byte[] states = {Grid.EXTERIOR, Grid.INTERIOR, Grid.OUTSIDE};

        BlockArrayGrid grid = new BlockArrayGrid(width, height, depth, 0.05, 0.02, new int[] {2,2,2});

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

        BlockArrayGrid grid = new BlockArrayGrid(width, height, depth, 0.001, 0.001, new int[] {2,2,2});
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

        grid = new BlockArrayGrid(width, height, depth, 0.001, 0.001, new int[] {2,2,2});
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

        BlockArrayGrid grid = new BlockArrayGrid(width, height, depth, 0.001, 0.001, new int[] {2,2,2});
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

        grid = new BlockArrayGrid(width, height, depth, 0.001, 0.001, new int[] {2,2,2});
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
     * Test getGridCoords.
     */
    public void testGetGridCoords() {
        double xWorldCoord = 1.0;
        double yWorldCoord = 0.15;
        double zWorldCoord = 0.61;
        double voxelWidth = 0.02;
        double sliceHeight = 0.01;

        Grid grid = new BlockArrayGrid(xWorldCoord, yWorldCoord, zWorldCoord, voxelWidth, sliceHeight, new int[] {2,2,2});

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

        Grid grid = new BlockArrayGrid(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight, new int[] {2,2,2});

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

        Grid grid = new BlockArrayGrid(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight, new int[] {2,2,2});

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
        Grid grid = new BlockArrayGrid(50, 25, 70, 0.05, sliceHeight, new int[] {2,2,2});
        assertEquals("Slice height is not " + sliceHeight, sliceHeight, grid.getSliceHeight());

        // world coordinates
        grid = new BlockArrayGrid(0.12, 0.11, 0.12, 0.05, sliceHeight, new int[] {2,2,2});
        assertEquals("Slice height is not" + sliceHeight, sliceHeight, grid.getSliceHeight());
    }

    /**
     * Test getVoxelSize with both constructor methods.
     */
    public void testGetVoxelSize() {
        double voxelSize = 0.025;

        // voxel coordinates
        Grid grid = new BlockArrayGrid(50, 25, 70, voxelSize, 0.01, new int[] {2,2,2});
        assertEquals("Voxel size is not " + voxelSize, voxelSize, grid.getVoxelSize());

        // world coordinates
        grid = new BlockArrayGrid(0.12, 0.11, 0.12, voxelSize, 0.01, new int[] {2,2,2});
        assertEquals("Voxel size is not " + voxelSize, voxelSize, grid.getVoxelSize());
    }
    
    /**
     * Test setState.
     */
    public void testSetState() {
    int size = 10;
    
    BlockArrayGrid grid = new BlockArrayGrid(size, size, size, 0.001, 0.001, new int[] {0,0,0});
        
        grid.setData(0, 0, 0, Grid.INTERIOR, 1);
        grid.setData(9, 9, 9, Grid.EXTERIOR, 2);
        grid.setData(5, 0, 7, Grid.INTERIOR, 3);
        
        grid.setState(0, 0, 0, Grid.EXTERIOR);
        grid.setState(9, 9, 9, Grid.INTERIOR);
        grid.setState(5, 0, 7, Grid.EXTERIOR);

        // check that the state changed, but the material did not
        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(0, 0, 0));
        //assertEquals("Material should be ", 1, grid.getMaterial(0, 0, 0));

        assertEquals("State should be ", Grid.INTERIOR, grid.getState(9, 9, 9));
        //assertEquals("Material should be ", 2, grid.getMaterial(9, 9, 9));

        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(5, 0, 7));
        //assertEquals("Material should be ", 3, grid.getMaterial(5, 0, 7));
    }
    
    /**
     * Test clone.
     */
    public void testClone() {
    int size = 10;
    double voxelSize = 0.002;
    double sliceHeight = 0.001;
    
    BlockArrayGrid grid = new BlockArrayGrid(size, size, size, voxelSize, sliceHeight, new int[] {2,2,2});
        
    grid.setData(0, 0, 0, Grid.INTERIOR, 1);
        grid.setData(9, 9, 9, Grid.EXTERIOR, 2);
        grid.setData(5, 0, 7, Grid.INTERIOR, 3);
        
        Grid grid2 = (BlockArrayGrid) grid.clone();

        assertEquals("Voxel size should be ", voxelSize, grid2.getVoxelSize());
        assertEquals("Slight height should be ", sliceHeight, grid2.getSliceHeight());
        
        // check that the state and material are set
        assertEquals("State should be ", Grid.INTERIOR, grid2.getState(0, 0, 0));

        assertEquals("State should be ", Grid.EXTERIOR, grid2.getState(9, 9, 9));

        assertEquals("State should be ", Grid.INTERIOR, grid2.getState(5, 0, 7));
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
     * A voxel of the class requested has been found.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public void found(int x, int y, int z, byte state) {
        allCount++;

        if (state == Grid.EXTERIOR) {
            mrkCount++;
            extCount++;
        } else if (state == Grid.INTERIOR) {
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
    public boolean foundInterruptible(int x, int y, int z, byte state) {
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
            ((BlockArrayGrid) grid).setData(x,y,z, state, mat);
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