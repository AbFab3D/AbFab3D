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
import junit.framework.Test;
import junit.framework.TestSuite;

// Internal Imports

/**
 * Tests the functionality of a ArrayGridByte.
 *
 * @author Alan Hudson
 */
public class TestArrayGridByte extends BaseTestGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestArrayGridByte.class);
    }

    public void testToString() {
        Grid grid = new ArrayGridByte(1, 1, 1, 0.001, 0.001);

        runToString(grid);
    }

    /**
     * Test the constructors and the grid size.
     */
    public void testConstructors() {
        Grid grid = new ArrayGridByte(1, 1, 1, 0.001, 0.001);
        assertEquals("Array size is not 1", 1, grid.getWidth() * grid.getHeight() * grid.getDepth());

        grid = new ArrayGridByte(100, 101, 102, 0.001, 0.001);
        assertEquals("Array size is not 1030200", 1030200, grid.getWidth() * grid.getHeight() * grid.getDepth());

        grid = new ArrayGridByte(1.0, 1.0, 1.0, 0.2, 0.1);
        assertEquals("Array size is not 250", 250, grid.getWidth() * grid.getHeight() * grid.getDepth());

        // grid size should be 6x6x11
        grid = new ArrayGridByte(1.1, 1.1, 1.1, 0.2, 0.1);
        assertEquals("Array size is not 396", 396, grid.getWidth() * grid.getHeight() * grid.getDepth());

        try {
            // test > int index size
            grid = new ArrayGridByte(10000,10000,10000, 0.2,0.1);
            fail("Index size check failed");
        } catch(IllegalArgumentException iae) {
            // passed
        }
    }

    /**
     * Test creating an empty grid.
     */
    public void testCreateEmpty() {
        Grid grid = new ArrayGridByte(100, 101, 102, 0.001, 0.001);

        createEmpty(grid);
    }

    /**
     * Test clone.
     */
    public void testClone() {
        int size = 10;
        double voxelSize = 0.002;
        double sliceHeight = 0.001;

        Grid grid = new ArrayGridByte(size,size,size,voxelSize,sliceHeight);
        runClone(grid);
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGetByVoxelCoords() {
        Grid grid = new ArrayGridByte(1, 1, 1, 0.001, 0.001);
        setGetAllVoxelCoords(grid);

        grid = new ArrayGridByte(3, 2, 2, 0.001, 0.001);
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

        grid = new ArrayGridByte(3, 2, 2, 0.001, 0.001);
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
        getStateByVoxel(grid);
    }

    /**
     * Test getData by voxels.
     */
    public void testGetDataByVoxel() {
        Grid grid = new ArrayGridByte(10, 9, 8, 0.001, 0.001);
        getDataByVoxel(grid);
    }

    /**
     * Test getData by voxels.
     */
    public void testGetDataByCoord() {
        Grid grid = new ArrayGridByte(1.0, 0.4, 0.5, 0.05, 0.01);
        getDataByCoord(grid);
    }

    /**
     * Test getState by world coordinates.
     */
    public void testGetStateByCoord() {
        Grid grid = new ArrayGridByte(1.0, 0.4, 0.5, 0.05, 0.01);
        getStateByCoord1(grid);

        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid = new ArrayGridByte(0.15, 0.12, 0.20, 0.05, 0.02);
        getStateByCoord2(grid);
    }

    /**
     * Test findCount by voxel class.
     */
    public void testFindCountByVoxelClass() {
        int width = 6;
        int height = 3;
        int depth = 10;

        Grid grid = new ArrayGridByte(width, height, depth, 0.05, 0.02);
        findCountByVoxelClass(grid);

    }

    /**
     * Test find voxels by voxel class
     */
    public void testFindVoxelClass() {
        int width = 3;
        int height = 4;
        int depth = 10;

        Grid grid = new ArrayGridByte(width, height, depth, 0.05, 0.02);
        findVoxelClass(grid);
    }

    /**
     * Test that find voxels by VoxelClass actually found the voxels in the correct coordinates
     */
    public void testFindVoxelClassIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;

        Grid grid = new ArrayGridByte(width, height, depth, 0.001, 0.001);
        findVoxelClassIterator1(grid);

        grid = new ArrayGridByte(width, height, depth, 0.001, 0.001);
        findVoxelClassIterator2(grid);
    }

    /**
     * Test that find voxels by VoxelClass actually found the voxels in the correct coordinates
     */
    public void testFindInterruptableVoxelClassIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;

        Grid grid = new ArrayGridByte(width, height, depth, 0.001, 0.001);
        findInterruptableVoxelClassIterator1(grid);
        grid = new ArrayGridByte(width, height, depth, 0.001, 0.001);
        findInterruptableVoxelClassIterator2(grid);
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
        getGridCoords(grid);
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
        getWorldCoords(grid);
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
        getGridBounds(grid);
        getGridBounds2(grid);
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
        width = BaseGrid.roundSize(xcoord / voxelSize);

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
        height = BaseGrid.roundSize(ycoord / sliceHeight);

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
        depth = BaseGrid.roundSize(zcoord / voxelSize);

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

    public void testInsideGrid() {
        double voxelSize = 0.01;
        Grid grid = new ArrayGridByte(50, 25, 70, voxelSize, 0.01);
        insideGrid(grid);
    }
}


