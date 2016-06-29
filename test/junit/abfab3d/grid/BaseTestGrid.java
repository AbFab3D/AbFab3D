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

import abfab3d.core.ClassTraverser;
import abfab3d.core.Grid;
import abfab3d.core.VoxelClasses;
import abfab3d.core.VoxelData;
import abfab3d.io.output.BoxesX3DExporter;
import junit.framework.TestCase;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

// Internal Imports

/**
 * Base functionality for testing grids.  Only uses the Grid interface.
 *
 * @author Alan Hudson
 */
public class BaseTestGrid extends TestCase implements ClassTraverser {
    /**
     * The material count
     */
    protected int allCount;
    protected int mrkCount;
    protected int extCount;
    protected int intCount;
    protected int outCount;

    public void insideGrid(Grid grid) {
        double voxelSize = grid.getVoxelSize();

        assertTrue("Really Inside grid", grid.insideGrid(1,1,1));
        assertFalse("Outside grid", grid.insideGrid(-1,1,1));
        assertFalse("Outside grid", grid.insideGrid(1,26,1));

        assertTrue("Really Inside grid", grid.insideGridWorld(voxelSize * 1, voxelSize * 1, voxelSize * 1));
        assertFalse("Outside grid", grid.insideGridWorld(voxelSize * 1, voxelSize * 26, voxelSize * 1));
    }

    public void runToString(Grid grid) {
        String st = grid.toString();
        String st2 = grid.toStringAll();
        String st3 = grid.toStringSlice(0);
    }

    /**
     * Test clone.
     */
    public void runClone(Grid grid) {
        double voxelSize = grid.getVoxelSize();
        double sliceHeight = grid.getSliceHeight();

        grid.setState(0, 0, 0, Grid.INSIDE);
        grid.setState(9, 9, 9, Grid.INSIDE);
        grid.setState(5, 0, 7, Grid.INSIDE);

        Grid grid2 = (Grid) grid.clone();

        assertEquals("Voxel size should be ", voxelSize, grid2.getVoxelSize());
        assertEquals("Slight height should be ", sliceHeight, grid2.getSliceHeight());

        // check that the state and material are set
        assertEquals("State should be ", Grid.INSIDE, grid2.getState(0, 0, 0));

        assertEquals("State should be ", Grid.INSIDE, grid2.getState(9, 9, 9));

        assertEquals("State should be ", Grid.INSIDE, grid2.getState(5, 0, 7));
    }

    /**
     * Test getData by voxels.
     */
    public void getDataByVoxel(Grid grid) {
        grid.setState(0, 0, 0, Grid.OUTSIDE);
        grid.setState(9, 8, 7, Grid.INSIDE);
        grid.setState(5, 0, 7, Grid.INSIDE);

        VoxelData vd = grid.getVoxelData();

        grid.getData(0, 0, 0, vd);
        assertEquals("State should be ", Grid.OUTSIDE, vd.getState());
        grid.getData(9, 8, 7, vd);
        assertEquals("State should be ", Grid.INSIDE, vd.getState());
        grid.getData(5, 0, 7, vd);
        assertEquals("State should be ", Grid.INSIDE, vd.getState());

        // Index that are not set should default to 0
        assertEquals("State should be ", 0, grid.getState(2, 2, 2));
    }

    /**
     * Test getData by voxels.
     */
    public void getDataByCoord(Grid grid) {
        grid.setStateWorld(0.0, 0.0, 0.0, Grid.OUTSIDE);
        grid.setStateWorld(0.95, 0.39, 0.45, Grid.INSIDE);
        grid.setStateWorld(0.6, 0.1, 0.4, Grid.INSIDE);
        VoxelData vd = grid.getVoxelData();
        grid.getDataWorld(0.0, 0.0, 0.0, vd);
        assertEquals("State should be ", Grid.OUTSIDE, vd.getState());
        grid.getDataWorld(0.95, 0.39, 0.45, vd);
        assertEquals("State should be ", Grid.INSIDE, vd.getState());
        grid.getDataWorld(0.6, 0.1, 0.4, vd);
        assertEquals("State should be ", Grid.INSIDE, vd.getState());
    }

    /**
     * Test creating an empty grid.
     */
    public void createEmpty(Grid grid) {
        grid.setState(5, 5, 5, Grid.INSIDE);

        Grid grid2 = (Grid) grid.createEmpty(10, 11, 12, 0.002, 0.003);
        int gridSize = 10 * 11 * 12;

        assertEquals("Grid size is not " + gridSize, gridSize, grid2.getWidth() * grid2.getHeight() * grid2.getDepth());
        assertEquals("Grid voxel size is not 0.002", 0.002, grid2.getVoxelSize());
        assertEquals("Grid slice height is not 0.003", 0.003, grid2.getSliceHeight());

        // all voxels in empty grid should be OUTSIDE state and 0 material
        assertEquals("State is not OUTSIDE for (5, 5, 5)", Grid.OUTSIDE, grid2.getState(5, 5, 5));
    }


    /**
     * Test find voxels by voxel class
     */
    public void findVoxelClass(Grid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        int[] stateDepth = {10, 6, 1};
        byte[] states = {Grid.INSIDE, Grid.INSIDE, Grid.OUTSIDE};

        // set some data
        for (int x = 0; x < states.length; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < stateDepth[x]; z++) {
                    grid.setState(x, y, z, states[x]);
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
        grid.find(VoxelClasses.INSIDE, this);
        assertEquals("Marked voxel count is not " + expectedMrkCount, expectedMrkCount, mrkCount);

        resetCounts();
        grid.find(VoxelClasses.OUTSIDE, this);
        assertEquals("Outside voxel count is not " + expectedOutCount, expectedOutCount, outCount);
    }

    /**
     * Test that find voxels by VoxelClass actually found the voxels in the correct coordinates
     */
    public void findVoxelClassIterator1(Grid grid) {
        int width = grid.getWidth();

        HashSet<VoxelCoordinate> vcSetInt = new HashSet<VoxelCoordinate>();

        for (int x = 0; x < width; x++) {
            grid.setState(x, 2, 2, Grid.INSIDE);
            vcSetInt.add(new VoxelCoordinate(x, 2, 2));

            grid.setState(x, 5, 6, Grid.INSIDE);
            vcSetInt.add(new VoxelCoordinate(x, 5, 6));
        }

        FindIterateTester ft = new FindIterateTester(vcSetInt);
        grid.find(VoxelClasses.INSIDE, ft);

        assertTrue("Found iterator did not find all voxels with INSIDE state",
                ft.foundAllVoxels());

        // make sure that finding a voxel not in the list returns false
        grid.setState(10, 6, 2, Grid.INSIDE);
        ft = new FindIterateTester(vcSetInt);
        grid.find(VoxelClasses.INSIDE, ft);

        assertFalse("Found state iterator should return false",
                ft.foundAllVoxels());

        // make sure that not finding a voxel in the list returns false
        grid.setState(1, 5, 6, Grid.INSIDE);
        ft = new FindIterateTester(vcSetInt);
        grid.find(VoxelClasses.INSIDE, ft);

        assertFalse("Found state iterator should return false",
                ft.foundAllVoxels());

    }

    /**
     * Test that find voxels by VoxelClass actually found the voxels in the correct coordinates
     */
    public void findVoxelClassIterator2(Grid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        HashSet<VoxelCoordinate> vcSetInt = new HashSet<VoxelCoordinate>();


        int[][] coords = {
                {0, 0, 0},
                {width / 2, height / 2, depth / 2},
                {0, height - 1, depth - 1},
                {width - 1, 0, 0},
                {width - 1, height - 1, depth - 1}
        };

        grid = new ArrayAttributeGridByte(width, height, depth, 0.001, 0.001);
        vcSetInt = new HashSet<VoxelCoordinate>();

        for (int i = 0; i < coords.length; i++) {
            grid.setState(coords[i][0], coords[i][1], coords[i][2], Grid.INSIDE);
            vcSetInt.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        FindIterateTester ft = new FindIterateTester(vcSetInt);
        grid.find(VoxelClasses.INSIDE, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state",
                ft.foundAllVoxels());

    }

    /**
     * Test that find voxels by VoxelClass actually found the voxels in the correct coordinates
     */
    public void findInterruptableVoxelClassIterator1(Grid grid) {
        int width = grid.getWidth();

        HashSet<VoxelCoordinate> vcSetInt = new HashSet<VoxelCoordinate>();

        for (int x = 0; x < width; x++) {
            grid.setState(x, 2, 2, Grid.INSIDE);
            grid.setState(x, 4, 4, Grid.INSIDE);
            vcSetInt.add(new VoxelCoordinate(x, 2, 2));
            vcSetInt.add(new VoxelCoordinate(x, 4, 4));

            grid.setState(x, 5, 6, Grid.INSIDE);
            vcSetInt.add(new VoxelCoordinate(x, 5, 6));
        }

        FindIterateTester ft = new FindIterateTester(vcSetInt);
        grid.findInterruptible(VoxelClasses.INSIDE, ft);

        assertTrue("Found iterator did not find all voxels with INSIDE state",
                ft.foundAllVoxels());

        // make sure that findInterruptible stops interating when voxel is not found
        // do this by adding a new exterior voxel
        grid.setState(5, 2, 2, Grid.OUTSIDE);
        grid.setState(1, 3, 3, Grid.INSIDE);
        ft = new FindIterateTester(vcSetInt);
        grid.findInterruptible(VoxelClasses.INSIDE, ft);

        assertFalse("Found state interruptible iterator should return false",
                ft.foundAllVoxels());
        assertTrue("Found state interruptible did not get interrupted ",
                ft.getIterateCount() < vcSetInt.size());

        // make sure that not finding a voxel in the list returns false
        // do this by changing one of the interior voxels to outside state
        grid.setState(1, 5, 6, Grid.OUTSIDE);
        ft = new FindIterateTester(vcSetInt);
        grid.findInterruptible(VoxelClasses.INSIDE, ft);

        assertFalse("Found state interruptible iterator should return false", ft.foundAllVoxels());
    }

    /**
     * Test that find voxels by VoxelClass actually found the voxels in the correct coordinates
     */
    public void findInterruptableVoxelClassIterator2(Grid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        //-------------------------------------------------------
        // test on some random coordinates
        int[][] coords = {
                {0, 0, 0},
                {width / 2, height / 2, depth / 2},
                {0, height - 1, depth - 1},
                {width - 1, 0, 0},
                {width - 1, height - 1, depth - 1}
        };

        grid = new ArrayGridByte(width, height, depth, 0.001, 0.001);
        HashSet<VoxelCoordinate> vcSetInt = new HashSet<VoxelCoordinate>();

        for (int i = 0; i < coords.length; i++) {
            grid.setState(coords[i][0], coords[i][1], coords[i][2], Grid.INSIDE);
            vcSetInt.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        FindIterateTester ft = new FindIterateTester(vcSetInt);
        grid.findInterruptible(VoxelClasses.INSIDE, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state",
                ft.foundAllVoxels());

    }


    /**
     * Test findCount by voxel class.
     */
    public void findCountByVoxelClass(Grid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        int[] row = {0, 3, 5};
        byte[] state = {Grid.INSIDE, Grid.INSIDE, Grid.INSIDE};

        // set some rows to interior and exterior
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                grid.setState(row[0], y, z, state[0]);
                grid.setState(row[1], y, z, state[1]);
                grid.setState(row[2], y, z, state[2]);
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
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                grid.setState(row[0], y, z, Grid.OUTSIDE);
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
     * Test getState by world coordinates.
     */
    public void getStateByCoord2(Grid grid) {

        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid.setStateWorld(0.06, 0.07, 0.08, Grid.INSIDE);
        assertEquals("State should be ", Grid.INSIDE, grid.getStateWorld(0.05, 0.06, 0.05));
        assertEquals("State should be ", Grid.INSIDE, grid.getStateWorld(0.0999, 0.06, 0.05));
        assertEquals("State should be ", Grid.INSIDE, grid.getStateWorld(0.05, 0.0799, 0.05));
        assertEquals("State should be ", Grid.INSIDE, grid.getStateWorld(0.05, 0.06, 0.0999));
        assertEquals("State should be ", Grid.INSIDE, grid.getStateWorld(0.0999, 0.0799, 0.0999));
        assertEquals("State should be ", 0, grid.getStateWorld(0.0499, 0.06, 0.05));
        assertEquals("State should be ", 0, grid.getStateWorld(0.05, 0.0599, 0.05));
        assertEquals("State should be ", 0, grid.getStateWorld(0.05, 0.06, 0.0499));
        assertEquals("State should be ", 0, grid.getStateWorld(0.1, 0.06, 0.05));
        assertEquals("State should be ", 0, grid.getStateWorld(0.05, 0.08, 0.05));
        assertEquals("State should be ", 0, grid.getStateWorld(0.05, 0.06, 0.1));

        // set data for beginning voxel 0,0,0 and test the bounds
        grid.setStateWorld(0.0, 0.0, 0.0, Grid.INSIDE);
        assertEquals("State should be ", Grid.INSIDE, grid.getStateWorld(0.0, 0.0, 0.0));
        assertEquals("State should be ", Grid.INSIDE, grid.getStateWorld(0.0499, 0.0, 0.0));
        assertEquals("State should be ", Grid.INSIDE, grid.getStateWorld(0.0, 0.0199, 0.0));
        assertEquals("State should be ", Grid.INSIDE, grid.getStateWorld(0.0, 0.0, 0.0499));
        assertEquals("State should be ", Grid.INSIDE, grid.getStateWorld(0.0499, 0.0199, 0.0499));
        assertEquals("State should be ", 0, grid.getStateWorld(0.05, 0.0, 0.0));
        assertEquals("State should be ", 0, grid.getStateWorld(0.0, 0.02, 0.0));
        assertEquals("State should be ", 0, grid.getStateWorld(0.0, 0.0, 0.05));

        // set data for last voxel 2,5,3 and test the bounds
        grid.setStateWorld(0.149, 0.119, 0.199, Grid.INSIDE);
//        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.1, 0.1, 0.15));
        assertEquals("State should be ", Grid.INSIDE, grid.getStateWorld(0.149, 0.1, 0.151));
        assertEquals("State should be ", Grid.INSIDE, grid.getStateWorld(0.1, 0.119, 0.151));
        assertEquals("State should be ", Grid.INSIDE, grid.getStateWorld(0.1, 0.1, 0.199));
        assertEquals("State should be ", Grid.INSIDE, grid.getStateWorld(0.149, 0.119, 0.199));
        assertEquals("State should be ", 0, grid.getStateWorld(0.0999, 0.1, 0.1501));
        assertEquals("State should be ", 0, grid.getStateWorld(0.1, 0.0999, 0.1501));
        assertEquals("State should be ", 0, grid.getStateWorld(0.1, 0.1, 0.1499));
        assertEquals("State should be ", 0, grid.getStateWorld(0.0999, 0.0999, 0.1499));
    }


    /**
     * Test getState by world coordinates.
     */
    public void getStateByCoord1(Grid grid) {
        // set and test get on some random world coordinates
        grid.setStateWorld(0.0, 0.0, 0.0, Grid.OUTSIDE);
        grid.setStateWorld(0.95, 0.39, 0.45, Grid.INSIDE);
        grid.setStateWorld(0.6, 0.1, 0.4, Grid.INSIDE);
        assertEquals("State should be ", Grid.OUTSIDE, grid.getStateWorld(0.0, 0.0, 0.0));
        assertEquals("State should be ", Grid.INSIDE, grid.getStateWorld(0.95, 0.39, 0.45));
        assertEquals("State should be ", Grid.INSIDE, grid.getStateWorld(0.6, 0.1, 0.4));

    }

    /**
     * Test getState by voxels.
     */
    public void getStateByVoxel(Grid grid) {
        grid.setState(0, 0, 0, Grid.OUTSIDE);
        grid.setState(9, 8, 7, Grid.INSIDE);
        grid.setState(5, 0, 7, Grid.INSIDE);

        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0, 0, 0));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(9, 8, 7));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(5, 0, 7));

        // Index that are not set should default to 0
        assertEquals("State should be ", 0, grid.getState(2, 2, 2));
    }

    /**
     * Test getGridCoords.
     */
    public void getGridCoords(Grid grid) {
        double voxelWidth = grid.getVoxelSize();
        double sliceHeight = grid.getSliceHeight();

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
    public void getWorldCoords(Grid grid) {
        double voxelWidth = grid.getVoxelSize();
        double sliceHeight = grid.getSliceHeight();

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
    public void getGridBounds(Grid grid) {
        int xVoxels = grid.getWidth();
        int yVoxels = grid.getHeight();
        int zVoxels = grid.getDepth();
        double voxelWidth = grid.getVoxelSize();
        double sliceHeight = grid.getSliceHeight();

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
     * Test getWorldCoords.
     */
    public void getGridBounds2(Grid grid) {
        int xVoxels = grid.getWidth();
        int yVoxels = grid.getHeight();
        int zVoxels = grid.getDepth();
        double voxelWidth = grid.getVoxelSize();
        double sliceHeight = grid.getSliceHeight();

        double[] bounds = new double[6];
        double expectedMaxX = xVoxels * voxelWidth;
        double expectedMaxY = yVoxels * sliceHeight;
        double expectedMaxZ = zVoxels * voxelWidth;

        grid.getGridBounds(bounds);
//System.out.println(maxBounds[0] + ", " + maxBounds[1] + ", " + maxBounds[2]);

        assertTrue("Minimum bounds is not (0, 0, 0)",
                bounds[0] == 0.0 &&
                        bounds[2] == 0.0 &&
                        bounds[4] == 0.0);

        assertTrue("Minimum bounds is not (" + expectedMaxX + ", " + expectedMaxY + ", " + expectedMaxZ + ")",
                bounds[1] == expectedMaxX &&
                        bounds[3] == expectedMaxY &&
                        bounds[5] == expectedMaxZ);

    }

    /**
     * Set and get all values of a grid using voxel coords
     *
     * @param grid The grid to test
     */
    public void setGetAllVoxelCoords(Grid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        VoxelData vd = grid.getVoxelData();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    grid.setState(x, y, z, Grid.INSIDE);
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    grid.getData(x, y, z, vd);
                    assertTrue("State wrong", vd.getState() == Grid.INSIDE);
                }
            }
        }
    }

    /**
     * Set and get all values of a grid using voxel coords using stripped
     * exterior/interior pattern.
     *
     * @param grid The grid to test
     */
    public void setGetAllVoxelCoordsStripped(Grid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    if ((x % 2) == 0 && (y % 2) == 0 && (z % 2) == 0) {
                        grid.setState(x, y, z, Grid.INSIDE);
                    } else {
                        grid.setState(x, y, z, Grid.INSIDE);
                    }

                }
            }
        }

        VoxelData vd = grid.getVoxelData();
        long cnt = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    grid.getData(x, y, z, vd);
//System.out.println(x + ", " + y + ", " + z + ": " + vd.getState());
                    if ((x % 2) == 0 && (y % 2) == 0 && (z % 2) == 0) {
                        assertTrue("State wrong", vd.getState() == Grid.INSIDE);
                    } else {
                        assertTrue("State wrong", vd.getState() == Grid.INSIDE);
                    }
                }
            }
        }
    }

    /**
     * Set and get all values of a grid using voxel coords using stripped
     * exterior/interior pattern.
     *
     * @param grid The grid to test
     */
    public void setGetAllVoxelCoordsDiagonal(Grid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    if (x == y && y == z) {
                        grid.setState(x, y, z, Grid.INSIDE);
                    }
                }
            }
        }

        VoxelData vd = grid.getVoxelData();
        long cnt = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    grid.getData(x, y, z, vd);
//System.out.println(x + ", " + y + ", " + z + ": " + vd.getState());
                    if (x == y && y == z) {
                        assertTrue("State wrong", vd.getState() == Grid.INSIDE);
                    }
                }
            }
        }
    }

    /**
     * Set and get all values of a grid using world coords
     *
     * @param grid The grid to test
     */
    public void setGetAllVoxelByWorldCoords(Grid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        double voxelSize = grid.getVoxelSize();
        double sliceHeight = grid.getSliceHeight();

        double xcoord, ycoord, zcoord;

        for (int x = 0; x < width; x++) {
            xcoord = (double) (x) * voxelSize + voxelSize / 2.0;
            for (int y = 0; y < height; y++) {
                ycoord = (double) (y) * sliceHeight + sliceHeight / 2.0;
                for (int z = 0; z < depth; z++) {
                    zcoord = (double) (z) * voxelSize + voxelSize / 2.0;
                    grid.setStateWorld(xcoord, ycoord, zcoord, Grid.INSIDE);
                }
            }
        }

        VoxelData vd = grid.getVoxelData();

        for (int x = 0; x < width; x++) {
            xcoord = (double) (x) * voxelSize + voxelSize / 2.0;
            for (int y = 0; y < height; y++) {
                ycoord = (double) (y) * sliceHeight + sliceHeight / 2.0;
                for (int z = 0; z < depth; z++) {
                    zcoord = (double) (z) * voxelSize + voxelSize / 2.0;
                    grid.getDataWorld(xcoord, ycoord, zcoord, vd);
//System.out.println(x + ", " + y + ", " + z + ": " + vd.getState());
                    assertTrue("State wrong", vd.getState() == Grid.INSIDE);
                }
            }
        }
    }

    /**
     * Resets the voxel counts.
     */
    protected void resetCounts() {
        allCount = 0;
        mrkCount = 0;
        extCount = 0;
        intCount = 0;
        outCount = 0;
    }

    /**
     * Set the X values of a grid with a given Y and Z to the given state and material.
     *
     * @param state      The new state
     * @param mat        The new material
     * @param startIndex The starting X index
     * @param endIndex   The ending X Index
     */
    protected static void setX(Grid grid, int y, int z, byte state, long mat, int startIndex, int endIndex) {
        for (int x = startIndex; x <= endIndex; x++) {
            grid.setState(x, y, z, state);
        }
    }

    /**
     * Set the Y values of a grid with a given X and Z to the given state and material.
     *
     * @param state      The new state
     * @param mat        The new material
     * @param startIndex The starting Y index
     * @param endIndex   The ending Y Index
     */
    protected static void setY(Grid grid, int x, int z, byte state, long mat, int startIndex, int endIndex) {
        for (int y = startIndex; y <= endIndex; y++) {
            grid.setState(x, y, z, state);
        }
    }

    /**
     * Set the Z values of a grid with a given X and Y to the given state and material.
     *
     * @param state      The new state
     * @param mat        The new material
     * @param startIndex The starting Z index
     * @param endIndex   The ending Z Index
     */
    protected static void setZ(Grid grid, int x, int y, byte state, long mat, int startIndex, int endIndex) {
        for (int z = startIndex; z <= endIndex; z++) {
            grid.setState(x, y, z, state);
        }
    }


    /**
     * Set the data for an X plane.
     *
     * @param grid     The grid to set
     * @param x        The X plane to set
     * @param state    The new state
     * @param material The new material
     */
    protected static void setPlaneX(Grid grid, int x, byte state, long material) {
        int height = grid.getHeight();
        int depth = grid.getDepth();

        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                grid.setState(x, y, z, state);
            }
        }
    }

    /**
     * Set the data for a Y plane.
     *
     * @param grid     The grid to set
     * @param y        The Y plane to set
     * @param state    The new state
     * @param material The new material
     */
    protected static void setPlaneY(Grid grid, int y, byte state, long material) {
        int width = grid.getWidth();
        int depth = grid.getDepth();

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                grid.setState(x, y, z, state);
            }
        }
    }

    /**
     * Set the data for a Z plane.
     *
     * @param grid     The grid to set
     * @param z        The Z plane to set
     * @param state    The new state
     * @param material The new material
     */
    protected static void setPlaneZ(Grid grid, int z, byte state, long material) {
        int width = grid.getWidth();
        int height = grid.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid.setState(x, y, z, state);
            }
        }
    }

    protected static void saveDebug(Grid grid, String filename, boolean showOutside) {
        ErrorReporter console = new PlainTextErrorReporter();

        try {
            FileOutputStream fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".") + 1);
            BoxesX3DExporter exporter = new BoxesX3DExporter(encoding, fos, console);

            HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
            colors.put(new Integer(Grid.INSIDE), new float[]{0, 1, 0});
            colors.put(new Integer(Grid.INSIDE), new float[]{1, 0, 0});

            HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
            transparency.put(new Integer(Grid.INSIDE), new Float(0));
            transparency.put(new Integer(Grid.INSIDE), new Float(0.5));
            if (showOutside) {
                colors.put(new Integer(Grid.OUTSIDE), new float[]{0, 0, 1});
                transparency.put(new Integer(Grid.OUTSIDE), new Float(0.96));
            }


            exporter.writeDebug(grid, colors, transparency);
            exporter.close();

            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }


    @Override
    public void found(int x, int y, int z, byte state) {
        allCount++;

        if (state == Grid.INSIDE) {
            mrkCount++;
            extCount++;
        } else if (state == Grid.INSIDE) {
            mrkCount++;
            intCount++;
        } else {
            outCount++;
        }
    }

    @Override
    public boolean foundInterruptible(int x, int y, int z, byte state) {
        // ignore
        return true;
    }

}

/**
 * Class to test that the find methods actually found the voxel states in the correct coordinate.
 *
 * @author Tony
 */
class FindIterateTester implements ClassTraverser {
    private boolean foundCorrect;
    private Set vcSet;
    private int iterateCount;
    private int vcSetCount;

    /**
     * Constructor that takes in a HashSet of VoxelCoordinates known to be
     * in the VoxelClass to find
     *
     * @param vc
     */
    public FindIterateTester(HashSet<VoxelCoordinate> vc) {
        this(vc,false);
    }

    /**
     * Constructor that takes in a HashSet of VoxelCoordinates known to be
     * in the VoxelClass to find
     *
     * @param vc
     */
    public FindIterateTester(HashSet<VoxelCoordinate> vc, boolean threadSafe) {
        if (threadSafe) {
            this.vcSet = Collections.synchronizedSet((Set)vc.clone());
        } else {
            this.vcSet = (HashSet<VoxelCoordinate>) vc.clone();
        }
        foundCorrect = true;
        iterateCount = 0;
        vcSetCount = vcSet.size();
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x  The x grid coordinate
     * @param y  The y grid coordinate
     * @param z  The z grid coordinate
     * @param vd The voxel data
     */
    public void found(int x, int y, int z, byte vd) {
        VoxelCoordinate c = new VoxelCoordinate(x, y, z);
//System.out.println(x + ", " + y + ", " + z);
        if (!inCoordList(c)) {
//System.out.println("not in cood list: " + x + ", " + y + ", " + z);
            foundCorrect = false;
        }

        iterateCount++;
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x  The x grid coordinate
     * @param y  The y grid coordinate
     * @param z  The z grid coordinate
     * @param vd The voxel data
     * @return True to continue, false stops the traversal.
     */
    public boolean foundInterruptible(int x, int y, int z, byte vd) {
        VoxelCoordinate c = new VoxelCoordinate(x, y, z);
//System.out.println(x + ", " + y + ", " + z);
        if (!inCoordList(c)) {
//System.out.println("not in cood list: " + x + ", " + y + ", " + z);
            foundCorrect = false;
            return false;
        }

        iterateCount++;
        return true;
    }

    /**
     * Returns whether all voxels have been found, and that the number of
     * times iterated through the grid is equal to the expected value.
     *
     * @return True if voxels were found correctly
     */
    public boolean foundAllVoxels() {
        return (foundCorrect && (iterateCount == vcSetCount));
    }

    /**
     * Returns the number of times voxels of the correct state was found.
     *
     * @return count of the times voxels of the correct state was found\
     */
    public int getIterateCount() {
        return iterateCount;
    }

    /**
     * Check if the VoxelCoordinate is in the known list, and removes
     * it from the list if found.
     *
     * @param c The voxel coordinate
     * @return True if the voxel coordinate is in the know list
     */
    private boolean inCoordList(VoxelCoordinate c) {
        if (vcSet.contains(c)) {
            vcSet.remove(c);
            return true;
        }

        return false;
    }
}
