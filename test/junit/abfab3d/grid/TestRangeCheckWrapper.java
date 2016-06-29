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
import abfab3d.core.Grid;
import abfab3d.core.VoxelClasses;
import abfab3d.core.VoxelData;
import abfab3d.core.Bounds;
import junit.framework.Test;
import junit.framework.TestSuite;

// Internal Imports

/**
 * Tests the functionality of a ArrayGrid.
 *
 * @author Alan Hudson
 * @version
 */
public class TestRangeCheckWrapper extends BaseTestGrid {
    public void testToString() {
        Grid grid = new ArrayGridByte(1, 1, 1, 0.001, 0.001);
        RangeCheckWrapper wrapper = new RangeCheckWrapper(grid);

        runToString(wrapper);
    }


    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestRangeCheckWrapper.class);
    }

    /**
     * Test that find voxels by VoxelClass actually found the voxels in the correct coordinates
     */
    public void testFindInterruptableVoxelClassIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;

        Grid grid = new ArrayGridByte(width, height, depth, 0.001, 0.001);
        RangeCheckWrapper wrapper = new RangeCheckWrapper(grid);

        findInterruptableVoxelClassIterator1(wrapper);
        grid = new ArrayGridByte(width, height, depth, 0.001, 0.001);
        wrapper = new RangeCheckWrapper(grid);
        findInterruptableVoxelClassIterator2(wrapper);
    }

    public void testInsideGrid() {
        double voxelSize = 0.01;
        Grid grid = new ArrayGridByte(50, 25, 70, voxelSize, 0.01);
        RangeCheckWrapper wrapper = new RangeCheckWrapper(grid);

        insideGrid(wrapper);
    }

    /**
     * Test creating an empty grid.
     */
    public void testCreateEmpty() {
        Grid grid = new ArrayGridByte(100, 101, 102, 0.001, 0.001);
        RangeCheckWrapper wrapper = new RangeCheckWrapper(grid);

        createEmpty(wrapper);
    }

    /**
     * Test clone.
     */
    public void testClone() {
        int size = 10;
        double voxelSize = 0.002;
        double sliceHeight = 0.001;

        Grid grid = new ArrayGridByte(size,size,size,voxelSize,sliceHeight);
        RangeCheckWrapper wrapper = new RangeCheckWrapper(grid);
        runClone(wrapper);
    }

    /**
     * Test the constructors and the grid size.
     */
    public void testSetGrid() {
        Grid grid = null;

        grid = new ArrayAttributeGridByte(2, 3, 4, 0.001, 0.001);
        RangeCheckWrapper wrapper = new RangeCheckWrapper(grid);

        assertEquals("Wrapper grid width is not " + 2, 2, wrapper.getWidth());
        assertEquals("Wrapper grid height is not " + 3, 3, wrapper.getHeight());
        assertEquals("Wrapper grid depth is not " + 4, 4, wrapper.getDepth());
    }

    /**
     * Test set/get.
     */
    public void testGetSetDataByVoxelCoords() {
        int width = 20;
        int height = 30;
        int depth = 40;
        long mat = 1;

        Grid grid = new ArrayAttributeGridByte(width, height, depth, 0.001, 0.001);
        RangeCheckWrapper wrapper = new RangeCheckWrapper(grid);

        //-------------------------------------------------------
        // Invalid setData
        //-------------------------------------------------------
        try {
            wrapper.setState(-1,0,0, Grid.INSIDE);
            fail("Negative width voxel coord did not throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("No voxels should have been set", 0, wrapper.findCount(VoxelClasses.INSIDE));
        }

        try {
            wrapper.setState(width,0,0, Grid.INSIDE);
            fail("Voxel coord greater than width-1 should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("No voxels should have been set", 0, wrapper.findCount(VoxelClasses.INSIDE));
        }

        try {
            wrapper.setState(0,-1,0, Grid.INSIDE);
            fail("Negative height voxel coord did not throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("No voxels should have been set", 0, wrapper.findCount(VoxelClasses.INSIDE));
        }

        try {
            wrapper.setState(0,height,0, Grid.INSIDE);
            fail("Voxel coord greater than height-1 should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("No voxels should have been set", 0, wrapper.findCount(VoxelClasses.INSIDE));
        }

        try {
            wrapper.setState(0,0,-1, Grid.INSIDE);
            fail("Negative depth voxel coord did not throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("No voxels should have been set", 0, wrapper.findCount(VoxelClasses.INSIDE));
        }

        try {
            wrapper.setState(0,0,depth, Grid.INSIDE);
            fail("Voxel coord greater than depth-1 should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("No voxels should have been set", 0, wrapper.findCount(VoxelClasses.INSIDE));
        }

        //-------------------------------------------------------
        // Invalid getData
        //-------------------------------------------------------
        VoxelData vd = wrapper.getVoxelData();

        try {
            wrapper.getData(-1,0,0,vd);
            fail("Negative width voxel coord did not throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            wrapper.getData(width,0,0,vd);
            fail("Voxel coord greater than width-1 should throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            wrapper.getData(0,-1,0,vd);
            fail("Negative height voxel coord did not throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            wrapper.getData(0,height,0,vd);
            fail("Voxel coord greater than height-1 should throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            wrapper.getData(0,0,-1,vd);
            fail("Negative depth voxel coord did not throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            wrapper.getData(0,0,depth,vd);
            fail("Voxel coord greater than depth-1 should throw exception");
        } catch (IllegalArgumentException e) {
        }

        //-------------------------------------------------------
        // Valid setData and getData
        //-------------------------------------------------------
        wrapper.setState(0,0,0, Grid.INSIDE);
        wrapper.setState(width-1,0,0, Grid.INSIDE);
        wrapper.setState(0,height-1,0, Grid.INSIDE);
        wrapper.setState(0,0,depth-1, Grid.INSIDE);
        wrapper.setState(width-1,height-1,depth-1, Grid.INSIDE);

        wrapper.getData(0,0,0,vd);
        assertTrue("Voxel [0,0,0] data is incorrect", vd.getState() == Grid.INSIDE);

        wrapper.getData(width-1,0,0,vd);
        assertTrue("Voxel [width-1,0,0] data is incorrect", vd.getState() == Grid.INSIDE);

        wrapper.getData(0,height-1,0,vd);
        assertTrue("Voxel [0,height-1,0] data is incorrect", vd.getState() == Grid.INSIDE);

        wrapper.getData(0,0,depth-1,vd);
        assertTrue("Voxel [0,0,depth-1] data is incorrect", vd.getState() == Grid.INSIDE);

        wrapper.getData(width-1,height-1,depth-1,vd);
        assertTrue("Voxel [width-1,height-1,width-1] data is incorrect", vd.getState() == Grid.INSIDE);
    }

    /**
     * Test set/get all data points.
     */
    public void testGetSetDataByWorldCoords() {
        double width = 0.021;
        double height = 0.031;
        double depth = 0.041;
        double hres = 0.002;
        double vres = 0.002;
        long mat = 1;

        Grid grid = new ArrayAttributeGridByte(new Bounds(width, height, depth), hres, vres);
        RangeCheckWrapper wrapper = new RangeCheckWrapper(grid);

        //-------------------------------------------------------
        // Invalid setData
        //-------------------------------------------------------
        try {
            wrapper.setStateWorld(-hres, 0.0, 0.0, Grid.INSIDE);
            fail("Negative width world coord did not throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("No voxels should have been set", 0, wrapper.findCount(VoxelClasses.INSIDE));
        }

        try {
            wrapper.setStateWorld(width + 2 * hres, 0, 0, Grid.INSIDE);
            fail("World coord greater than width+2*hres should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("No voxels should have been set", 0, wrapper.findCount(VoxelClasses.INSIDE));
        }

        try {
            wrapper.setStateWorld(0, -vres, 0, Grid.INSIDE);
            fail("Negative height world coord did not throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("No voxels should have been set", 0, wrapper.findCount(VoxelClasses.INSIDE));
        }

        try {
            wrapper.setStateWorld(0, height + 2 * vres, 0, Grid.INSIDE);
            fail("Voxel world greater than height+2*vres should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("No voxels should have been set", 0, wrapper.findCount(VoxelClasses.INSIDE));
        }

        try {
            wrapper.setStateWorld(0, 0, -hres, Grid.INSIDE);
            fail("Negative depth world coord did not throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("No voxels should have been set", 0, wrapper.findCount(VoxelClasses.INSIDE));
        }

        try {
            wrapper.setStateWorld(0, 0, depth + 2 * hres, Grid.INSIDE);
            fail("World coord greater than depth+2*hres should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("No voxels should have been set", 0, wrapper.findCount(VoxelClasses.INSIDE));
        }

        //-------------------------------------------------------
        // Invalid getData
        //-------------------------------------------------------
        VoxelData vd = wrapper.getVoxelData();

        try {
            wrapper.getDataWorld(-hres, 0, 0, vd);
            fail("Negative width world coord did not throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            wrapper.getDataWorld(width + 2 * hres, 0, 0, vd);
            fail("Voxel coord greater than width+2*hres should throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            wrapper.getDataWorld(0, -vres, 0, vd);
            fail("Negative height world coord did not throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            wrapper.getDataWorld(0, height + 2 * vres, 0, vd);
            fail("World coord greater than height+2*vres should throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            wrapper.getDataWorld(0, 0, -hres, vd);
            fail("Negative depth voxel coord did not throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            wrapper.getDataWorld(0, 0, depth + 2 * hres, vd);
            fail("World coord greater than depth+2*hres should throw exception");
        } catch (IllegalArgumentException e) {
        }

        //-------------------------------------------------------
        // Valid setData and getData
        //-------------------------------------------------------
        wrapper.setStateWorld(0.0, 0.0, 0.0, Grid.INSIDE);
        wrapper.setStateWorld(width, 0, 0, Grid.INSIDE);
        wrapper.setStateWorld(0, height, 0, Grid.INSIDE);
        wrapper.setStateWorld(0, 0, depth, Grid.INSIDE);
        wrapper.setStateWorld(width, height, depth, Grid.INSIDE);

        wrapper.getDataWorld(0.0, 0.0, 0.0, vd);
        assertTrue("World coord [0,0,0] data is incorrect", vd.getState() == Grid.INSIDE);

        wrapper.getDataWorld(width, 0.0, 0.0, vd);
        assertTrue("World coord [width,0,0] data is incorrect", vd.getState() == Grid.INSIDE);

        wrapper.getDataWorld(0.0, height, 0.0, vd);
        assertTrue("World coord [0,height,0] data is incorrect", vd.getState() == Grid.INSIDE);

        wrapper.getDataWorld(0.0, 0.0, depth, vd);
        assertTrue("World coord [0,0,depth-1] data is incorrect", vd.getState() == Grid.INSIDE);

        wrapper.getDataWorld(width, height, depth, vd);
        assertTrue("World coord [width,height,width] data is incorrect", vd.getState() == Grid.INSIDE);
    }

    /**
     * Test getState by voxels.
     */
    public void testGetStateByVoxelCoords() {
        int width = 20;
        int height = 30;
        int depth = 40;
        long mat = 1;

        Grid grid = new ArrayAttributeGridByte(width, height, depth, 0.001, 0.001);
        RangeCheckWrapper wrapper = new RangeCheckWrapper(grid);
        wrapper.setState(0, 0, 0, Grid.INSIDE);
        wrapper.setState(width-1, 0, 0, Grid.INSIDE);
        setX(grid, 0, 0, Grid.INSIDE, mat, 1, width-2);

        Byte state = null;

        //-------------------------------------------------------
        // Invalid getState
        //-------------------------------------------------------
        try {
            state = wrapper.getState(-1,0,0);
            fail("Negative voxel coord should throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            state = wrapper.getState(width,0,0);
            fail("Voxel coord greater than width should throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            state = wrapper.getState(0,-1,0);
            fail("Negative voxel coord should throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            state = wrapper.getState(0,height,0);
            fail("Voxel coord greater than height should throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            state = wrapper.getState(0,0,-1);
            fail("Negative voxel coord should throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            state = wrapper.getState(0,0,depth);
            fail("Voxel coord greater than depth should throw exception");
        } catch (IllegalArgumentException e) {
        }

        //-------------------------------------------------------
        // Valid getState
        //-------------------------------------------------------
        try {
            state = wrapper.getState(0,0,0);
            assertEquals("Voxel state should be exterior", Grid.INSIDE, (byte)state);
        } catch (IllegalArgumentException e) {
            fail("Should not throw exception");
        }

        try {
            state = wrapper.getState(width-1,0,0);
            assertEquals("Voxel state should be exterior", Grid.INSIDE, (byte)state);
        } catch (IllegalArgumentException e) {
            fail("Should not throw exception");
        }

        try {
            state = wrapper.getState(width/2,0,0);
            assertEquals("Voxel state should be interior", Grid.INSIDE, (byte)state);
        } catch (IllegalArgumentException e) {
            fail("Should not throw exception");
        }
    }

    /**
     * Test getState by world coordinates.
     */
    public void testGetStateByWorldCoord() {
        double width = 0.021;
        double height = 0.031;
        double depth = 0.041;
        double hres = 0.002;
        double vres = 0.002;
        long mat = 1;

        Grid grid = new ArrayAttributeGridByte(new Bounds(width, height, depth), hres, vres);
        RangeCheckWrapper wrapper = new RangeCheckWrapper(grid);
        wrapper.setStateWorld(0.0, 0.0, 0.0, Grid.INSIDE);
        wrapper.setStateWorld(width, 0.0, 0.0, Grid.INSIDE);
        setX(grid, 0, 0, Grid.INSIDE, mat, 1, (int)((width-hres)/hres));

        Byte state = null;

        //-------------------------------------------------------
        // Invalid getState
        //-------------------------------------------------------
        try {
            state = wrapper.getStateWorld(-hres, 0.0, 0.0);
            fail("Negative world coord should throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            state = wrapper.getStateWorld(width + 2 * hres, 0, 0);
            fail("World coord greater than width should throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            state = wrapper.getStateWorld(0, -vres, 0);
            fail("Negative world coord should throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            state = wrapper.getStateWorld(0, height + 2 * vres, 0);
            fail("World coord greater than height should throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            state = wrapper.getStateWorld(0, 0, -hres);
            fail("Negative world coord should throw exception");
        } catch (IllegalArgumentException e) {
        }

        try {
            state = wrapper.getStateWorld(0, 0, depth + 2 * hres);
            fail("World coord greater than depth should throw exception");
        } catch (IllegalArgumentException e) {
        }

        //-------------------------------------------------------
        // Valid getState
        //-------------------------------------------------------
        try {
            state = wrapper.getStateWorld(0.0, 0.0, 0.0);
            assertEquals("Voxel state should be exterior", Grid.INSIDE, (byte)state);
        } catch (IllegalArgumentException e) {
            fail("Should not throw exception");
        }

        try {
            state = wrapper.getStateWorld(width, 0.0, 0.0);
            assertEquals("Voxel state should be exterior", Grid.INSIDE, (byte)state);
        } catch (IllegalArgumentException e) {
            fail("Should not throw exception");
        }

        try {
            state = wrapper.getStateWorld(width / 2.0f, 0.0, 0.0);
            assertEquals("Voxel state should be interior", Grid.INSIDE, (byte)state);
        } catch (IllegalArgumentException e) {
            fail("Should not throw exception");
        }
    }



    /**************************************************************************************/


    /**
     * Test findCount by voxel class.
     */
    public void testFindCount() {
        int width = 6;
        int height = 3;
        int depth = 10;
        int[] row = {0, 3, 5};
        byte[] state = {Grid.INSIDE, Grid.INSIDE, Grid.INSIDE};

        int temp = -1;
        RangeCheckWrapper wrapper = new RangeCheckWrapper(null);

        // test null grid
        try {
            temp = wrapper.findCount(VoxelClasses.ALL);
            fail("Null grid did not throw exception");
        } catch (NullPointerException e) {
            assertEquals("findCount should fail on null grid", -1, temp);
        }

        Grid grid = new ArrayAttributeGridByte(width, height, depth, 0.05, 0.02);
        wrapper = new RangeCheckWrapper(grid);

        // set some rows to interior and exterior
        for (int y=0; y<height; y++) {
            for (int z=0; z<depth; z++) {
                wrapper.setState(row[0], y, z, state[0]);
                wrapper.setState(row[1], y, z, state[1]);
                wrapper.setState(row[2], y, z, state[2]);
            }
        }

        int expectedAllCount = width * depth * height;
        int expectedIntCount = depth * height * 2;
        int expectedExtCount = depth * height;
        int expectedMrkCount = expectedIntCount + expectedExtCount;
        int expectedOutCount = expectedAllCount - expectedIntCount - expectedExtCount;

        assertEquals("Expected total voxels is not " + expectedAllCount, expectedAllCount, wrapper.findCount(VoxelClasses.ALL));
        assertEquals("Expected marked voxels is not " + expectedMrkCount, expectedMrkCount, wrapper.findCount(VoxelClasses.INSIDE));
        assertEquals("Expected outside voxels is not " + expectedOutCount, expectedOutCount, wrapper.findCount(VoxelClasses.OUTSIDE));

        // change one of the interior voxel rows to outside
        for (int y=0; y<height; y++) {
            for (int z=0; z<depth; z++) {
                wrapper.setState(row[0], y, z, Grid.OUTSIDE);
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

    public void testFindVoxelClass() {
        int width = 10;
        int height = 4;
        int depth = 3;
        long mat = 5;

        boolean threw = false;
        RangeCheckWrapper wrapper = new RangeCheckWrapper(null);

        // test null grid
        try {
            wrapper.find(VoxelClasses.ALL, this);
            fail("Null grid did not throw exception");
        } catch (NullPointerException e) {
            threw = true;
        }
        assertEquals("find should fail on null grid", true, threw);

        Grid grid = new ArrayAttributeGridByte(width, height, depth, 0.05, 0.02);
        wrapper = new RangeCheckWrapper(grid);

        setX(wrapper, 0, 0, Grid.INSIDE, mat, 1, width-2);
        setX(wrapper, 1, 1, Grid.INSIDE, mat, 0, width-1);

        int expectedAllCount = width * height * depth;
        int expectedExtCount = width;
        int expectedIntCount = width - 2;
        int expectedMrkCount = expectedExtCount + expectedIntCount;
        int expectedOutCount = expectedAllCount - expectedMrkCount;

        resetCounts();
        wrapper.find(VoxelClasses.ALL, this);
        assertEquals("All voxel count is not " + expectedAllCount, expectedAllCount, allCount);

        resetCounts();
        wrapper.find(VoxelClasses.INSIDE, this);
        assertEquals("Marked voxel count is not " + expectedMrkCount, expectedMrkCount, mrkCount);

        resetCounts();
        wrapper.find(VoxelClasses.OUTSIDE, this);
        assertEquals("Outside voxel count is not " + expectedOutCount, expectedOutCount, outCount);
    }

    /**
     * Test getGridCoords.
     */
    public void testGetGridCoords() {
        double width = 1.0;
        double height = 0.15;
        double depth = 0.61;
        double hres = 0.02;
        double vres = 0.01;
        int[] coords = {-999, -999, -999};

        Grid grid = new ArrayAttributeGridByte(new Bounds(width, height, depth), hres, vres);
        RangeCheckWrapper wrapper = new RangeCheckWrapper(grid);

        //-------------------------------------------------------
        // Invalid getGridCoords
        //-------------------------------------------------------
        try {
            wrapper.getGridCoords(-hres, 0.0, 0.0, coords);
            fail("Negative world coord should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("getGridCoords should fail on negative width world coords", -999, coords[0]);
        }

        try {
            wrapper.getGridCoords(width+hres, 0.0, 0.0, coords);
            fail("Negative grid coord should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("getGridCoords should fail on world coords greater than width", -999, coords[0]);
        }

        try {
            wrapper.getGridCoords(0.0, -vres, 0.0, coords);
            fail("Negative world coord should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("getGridCoords should fail on negative height world coords", -999, coords[0]);
        }

        try {
            wrapper.getGridCoords(0.0, height+vres, 0.0, coords);
            fail("Negative grid coord should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("getGridCoords should fail on world coords greater than height", -999, coords[0]);
        }

        try {
            wrapper.getGridCoords(0.0, 0.0, -hres, coords);
            fail("Negative world coord should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("getGridCoords should fail on negative depth world coords", -999, coords[0]);
        }

        try {
            wrapper.getGridCoords(0.0, 0.0, depth+2*hres, coords);
            fail("Negative grid coord should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("getGridCoords should fail on world coords greater than depth", -999, coords[0]);
        }

        //-------------------------------------------------------
        // Invalid getGridCoords
        //-------------------------------------------------------
        double xcoord = 0.55;
        double ycoord = 0.0202;
        double zcoord = 0.401;

        int expectedXVoxelCoord = (int) (xcoord / hres);
        int expectedYVoxelCoord = (int) (ycoord / vres);
        int expectedZVoxelCoord = (int) (zcoord / hres);

        grid.getGridCoords(xcoord, ycoord, zcoord, coords);
//System.out.println(coords[0] + ", " + coords[1] + ", " + coords[2]);
        assertTrue("Grid coordinate is not (" + expectedXVoxelCoord + ", " + expectedYVoxelCoord + ", " + expectedZVoxelCoord + ")",
                coords[0] == expectedXVoxelCoord &&
                coords[1] == expectedYVoxelCoord &&
                coords[2] == expectedZVoxelCoord);

        // test on a voxel line
        xcoord = 0.6;
        ycoord = 0.05;
        zcoord = 0.08;

        expectedXVoxelCoord = (int) (xcoord / hres);
        expectedYVoxelCoord = (int) (ycoord / vres);
        expectedZVoxelCoord = (int) (zcoord / hres);

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
        int width = 50;
        int height = 15;
        int depth = 31;
        double hres = 0.02;
        double vres = 0.01;
        double[] coords = {-999.0, -999.0, -999.0};

        Grid grid = new ArrayAttributeGridByte(width, height, depth, hres, vres);
        RangeCheckWrapper wrapper = new RangeCheckWrapper(grid);

        //-------------------------------------------------------
        // Invalid getWorldCoords
        //-------------------------------------------------------
        try {
            wrapper.getWorldCoords(-1, 0, 0, coords);
            fail("Negative grid coord should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("getWorldCoords should fail on negative width grid coords", -999.0, coords[0]);
        }

        try {
            wrapper.getWorldCoords(width, 0, 0, coords);
            fail("Negative grid coord should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("getGridCoords should fail on grid coords greater than width", -999.0, coords[0]);
        }

        try {
            wrapper.getWorldCoords(0, -1, 0, coords);
            fail("Negative grid coord should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("getGridCoords should fail on negative height grid coords", -999.0, coords[0]);
        }

        try {
            wrapper.getWorldCoords(0, height, 0, coords);
            fail("Negative grid coord should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("getGridCoords should fail on grid coords greater than height", -999.0, coords[0]);
        }

        try {
            wrapper.getWorldCoords(0, 0, -1, coords);
            fail("Negative grid coord should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("getGridCoords should fail on negative depth grid coords", -999.0, coords[0]);
        }

        try {
            wrapper.getWorldCoords(0, 0, depth, coords);
            fail("Negative grid coord should throw exception");
        } catch (IllegalArgumentException e) {
            assertEquals("getGridCoords should fail on world grid greater than depth", -999.0, coords[0]);
        }

        //-------------------------------------------------------
        // Invalid getGridCoords
        //-------------------------------------------------------
        int xcoord = 27;
        int ycoord = 2;
        int zcoord = 20;

        double expectedXWorldCoord = (double) (xcoord * hres + hres / 2);
        double expectedYWorldCoord = (double) (ycoord * vres + vres / 2);
        double expectedZWorldCoord = (double) (zcoord * hres + hres / 2);

        wrapper.getWorldCoords(xcoord, ycoord, zcoord, coords);
//System.out.println(coords[0] + ", " + coords[1] + ", " + coords[2]);
        assertTrue("World coordinate is not (" + expectedXWorldCoord + ", " + expectedYWorldCoord + ", " + expectedZWorldCoord + ")",
                coords[0] == expectedXWorldCoord &&
                coords[1] == expectedYWorldCoord &&
                coords[2] == expectedZWorldCoord);

    }

    /**
     * Test getGridBounds.
     */
    public void testGetGridBounds() {
        int xVoxels = 50;
        int yVoxels = 15;
        int zVoxels = 31;
        double voxelWidth = 0.02;
        double sliceHeight = 0.01;

        Grid grid = new ArrayAttributeGridByte(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight);
        Grid wrapper = new RangeCheckWrapper(grid);
        getGridBounds(wrapper);
        getGridBounds2(wrapper);

    }

    /**
     * Test getWidth with both constructor methods.
     */
    public void testGetWidth() {
        int width = 70;
        int temp = -1;

        RangeCheckWrapper wrapper = new RangeCheckWrapper(null);

        // test null grid
        try {
            temp = wrapper.getWidth();
            fail("Null grid did not throw exception");
        } catch (NullPointerException e) {
            assertEquals("getWidth should fail on null grid", -1, temp);
        }

        // voxel coordinates
        Grid grid = new ArrayAttributeGridByte(width, 50, 25, 0.05, 0.01);
        wrapper = new RangeCheckWrapper(grid);
        assertEquals("Width is not " + width, width, wrapper.getWidth());

        // world coordinates
        double xcoord = 0.12;
        double voxelSize = 0.05;
        width = BaseGrid.roundSize(xcoord/voxelSize);

        grid = new ArrayAttributeGridByte(new Bounds(xcoord, 0.11, 0.16), voxelSize, 0.02);
        wrapper = new RangeCheckWrapper(grid);
        assertEquals("Width is not " + width, width, wrapper.getWidth());
    }

    /**
     * Test getHeight with both constructor methods.
     */
    public void testGetHeight() {
        int height = 70;
        int temp = -1;

        RangeCheckWrapper wrapper = new RangeCheckWrapper(null);

        // test null grid
        try {
            temp = wrapper.getHeight();
            fail("Null grid did not throw exception");
        } catch (NullPointerException e) {
            assertEquals("testGetHeight should fail on null grid", -1, temp);
        }

        // voxel coordinates
        Grid grid = new ArrayAttributeGridByte(50, height, 25, 0.05, 0.02);
        wrapper = new RangeCheckWrapper(grid);
        assertEquals("Height is not " + height, height, wrapper.getHeight());

        // world coordinates
        double ycoord = 0.11;
        double sliceHeight = 0.02;
        height = BaseGrid.roundSize(ycoord/sliceHeight);

        grid = new ArrayAttributeGridByte(new Bounds(0.12, ycoord, 0.16), 0.05, sliceHeight);
        wrapper = new RangeCheckWrapper(grid);
        assertEquals("Height is not " + height, height, wrapper.getHeight());
    }

    /**
     * Test getDepth with both constructor methods.
     */
    public void testGetDepth() {
        int depth = 70;
        int temp = -1;

        RangeCheckWrapper wrapper = new RangeCheckWrapper(null);

        // test null grid
        try {
            temp = wrapper.getDepth();
            fail("Null grid did not throw exception");
        } catch (NullPointerException e) {
            assertEquals("testGetDepth should fail on null grid", -1, temp);
        }

        // voxel coordinates
        Grid grid = new ArrayAttributeGridByte(50, 25, depth, 0.05, 0.01);
        wrapper = new RangeCheckWrapper(grid);
        assertEquals("Depth is not " + depth, depth, wrapper.getDepth());

        // world coordinates
        double zcoord = 0.12;
        double voxelSize = 0.05;
        depth = BaseGrid.roundSize(zcoord/voxelSize);

        grid = new ArrayAttributeGridByte(new Bounds(0.12, 0.11, zcoord), voxelSize, 0.02);
        wrapper = new RangeCheckWrapper(grid);
        assertEquals("Depth is not " + depth, depth, wrapper.getDepth());
    }

    /**
     * Test getSliceHeight with both constructor methods.
     */
    public void testGetSliceHeight() {
        double sliceHeight = 0.0015;
        double temp = -1.0;

        RangeCheckWrapper wrapper = new RangeCheckWrapper(null);

        // test null grid
        try {
            temp = wrapper.getSliceHeight();
            fail("Null grid did not throw exception");
        } catch (NullPointerException e) {
            assertEquals("getSliceHeight should fail on null grid", -1.0, temp);
        }

        // voxel coordinates
        Grid grid = new ArrayAttributeGridByte(50, 25, 70, 0.05, sliceHeight);
        wrapper = new RangeCheckWrapper(grid);
        assertEquals("Slice height is not " + sliceHeight, sliceHeight, wrapper.getSliceHeight());

        // world coordinates
        grid = new ArrayAttributeGridByte(new Bounds(0.12, 0.11, 0.12), 0.05, sliceHeight);
        wrapper = new RangeCheckWrapper(grid);
        assertEquals("Slice height is not" + sliceHeight, sliceHeight, wrapper.getSliceHeight());
    }

    /**
     * Test getVoxelSize with both constructor methods.
     */
    public void testGetVoxelSize() {
        double voxelSize = 0.025;
        double temp = -1.0;

        RangeCheckWrapper wrapper = new RangeCheckWrapper(null);

        // test null grid
        try {
            temp = wrapper.getVoxelSize();
            fail("Null grid did not throw exception");
        } catch (NullPointerException e) {
            assertEquals("getVoxelSize should fail on null grid", -1.0, temp);
        }

        // voxel coordinates
        Grid grid = new ArrayAttributeGridByte(50, 25, 70, voxelSize, 0.01);
        wrapper = new RangeCheckWrapper(grid);
        assertEquals("Voxel size is not " + voxelSize, voxelSize, wrapper.getVoxelSize());

        // world coordinates
        grid = new ArrayAttributeGridByte(new Bounds(0.12, 0.11, 0.12), voxelSize, 0.01);
        wrapper = new RangeCheckWrapper(grid);
        assertEquals("Voxel size is not " + voxelSize, voxelSize, wrapper.getVoxelSize());
    }
}
