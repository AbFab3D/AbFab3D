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
import java.util.*;

import junit.framework.Test;
import junit.framework.TestSuite;

// Internal Imports
import abfab3d.grid.Grid.VoxelClasses;
import abfab3d.grid.query.Equals;

/**
 * Tests the functionality of an OctreeAttributeGridByte.
 *
 * @author Alan Hudson
 * @version
 */
public class TestOctreeGridByte extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestOctreeGridByte.class);
    }

    // TODO: Need to review this test, it fails not certain if its the
    // the test or code.

/*    public void testBatchedCounting() {
        int width = 4;
        int height = width;
        int depth = width;
//        int[] stateDepth = {10, 6, 1};
        int[] stateDepth = {1, 1, 1};
        byte[] states = {Grid.EXTERIOR, Grid.INTERIOR, Grid.OUTSIDE};

        OctreeAttributeGridByte grid = new OctreeAttributeGridByte(width, height, depth, 0.05, 0.05);

        grid.setData(0,0,0,Grid.EXTERIOR,0);
        grid.setData(1,0,0,Grid.EXTERIOR,0);
        grid.setData(0,1,0,Grid.EXTERIOR,0);
        grid.setData(1,1,0,Grid.EXTERIOR,0);

        // Should form one area together
        grid.setData(2,2,2,Grid.EXTERIOR,0);
        grid.setData(2,3,2,Grid.EXTERIOR,0);
        grid.setData(3,2,2,Grid.EXTERIOR,0);
        grid.setData(3,3,2,Grid.EXTERIOR,0);
        grid.setData(2,2,3,Grid.EXTERIOR,0);
        grid.setData(2,3,3,Grid.EXTERIOR,0);
        grid.setData(3,2,3,Grid.EXTERIOR,0);
        grid.setData(3,3,3,Grid.EXTERIOR,0);

//System.out.println("Node count: " + grid.getCellCount());
//grid.printTree();

        int expectedAllCount = width*height*depth;
        int expectedExtCount = 12;
        int expectedIntCount = 0;
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

//assertEquals("stop", 1, 0);
    }
*/

    /**
     * Test set/get all data points.
     */
    public void testGridEquality() {
        int size = 4;

        OctreeAttributeGridByte grid = new OctreeAttributeGridByte(size, size, size, 0.001, 0.001);
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        int x = width / 2;
        int y = height / 2;
        int z = depth / 2;

        x = 2;
        y = 2;
        z = 2;

        grid.setData(x,y,z, Grid.EXTERIOR,1);

/*
        VoxelData vd = grid.getData(hwidth+1,hheight+1,hdepth+1);

        assertEquals("state", vd.getState() == Grid.EXTERIOR);
        assertEquals("material", vd.getAttribute() == 1);
*/

        AttributeGrid grid2 =new ArrayAttributeGridByte(size, size, size, 0.001, 0.001);

        grid2.setData(x,y,z, Grid.EXTERIOR,1);

System.out.println("***Get unset data");
        VoxelData vd = grid.getVoxelData();
        grid.getData(2,2,z+1,vd);

        if (vd.getState() != Grid.OUTSIDE)
            grid.printTree();
        assertEquals("unset", vd.getState(), Grid.OUTSIDE);

System.out.println("Checking equals");
        Equals op = new Equals(grid);
        boolean result = op.execute(grid2);

        if (result == false) {
            System.out.println("OctreeGrid: \n" + grid.toStringAll());
            System.out.println("ArrayGrid: \n" + grid2.toStringAll());

            grid.printTree();
        }

        if (!result)
            System.out.println("Reason: " + op.getReason());
        assertTrue("equals", result);

//assertEquals("stop",1,0);
    }


    /**
     * Test that find voxels by VoxelClass actually found the voxels in the correct coordinates
     */
    public void testFindVoxelClassIterator() {
        int width = 64;
        int height = width;
        int depth = width;
        long mat = 1;

        AttributeGrid grid =new OctreeAttributeGridByte(width, height, depth, 0.001, 0.001);
        HashSet<VoxelCoordinate> vcSetExt = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetInt = new HashSet<VoxelCoordinate>();

        for (int x=0; x<width; x++) {
            grid.setData(x, 2, 2, Grid.EXTERIOR, mat);
            vcSetExt.add(new VoxelCoordinate(x, 2, 2));

            grid.setData(x, 5, 6, Grid.INTERIOR, mat);
            vcSetInt.add(new VoxelCoordinate(x, 5, 6));
        }

        for (int y=0; y<height; y++) {
            grid.setData(2, y, 2, Grid.EXTERIOR, mat);
            vcSetExt.add(new VoxelCoordinate(2, y, 2));

            grid.setData(17, y, 6, Grid.INTERIOR, mat);
            vcSetInt.add(new VoxelCoordinate(17, y, 6));
        }


        for (int z=depth/2; z<depth; z++) {
            grid.setData(2, 2, z, Grid.EXTERIOR, mat);
            vcSetExt.add(new VoxelCoordinate(2, 2, z));

            grid.setData(17, 6, z, Grid.INTERIOR, mat);
            vcSetInt.add(new VoxelCoordinate(17, 6, z));
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
        grid.setData(7, 6, 2, Grid.EXTERIOR, mat);
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

        grid = new OctreeAttributeGridByte(width, height, depth, 0.001, 0.001);
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

    public void testFindVoxelClass() {
        int width = 16;
        int height = width;
        int depth = width;
        int[] stateDepth = {10, 6, 1};
        byte[] states = {Grid.EXTERIOR, Grid.INTERIOR, Grid.OUTSIDE};

        OctreeAttributeGridByte grid = new OctreeAttributeGridByte(width, height, depth, 0.05, 0.05);

        // set some data
        for (int x=0; x<states.length; x++){
            for (int y=0; y<height; y++) {
                for (int z=0; z<stateDepth[x]; z++) {
                    grid.setData(x, y, z, states[x], 2);
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
     * Test set/get all data points.
     */
    public void testSimpleSetGet() {
        OctreeAttributeGridByte grid = new OctreeAttributeGridByte(4, 4, 4, 0.001, 0.001);

        grid.setData(1,0,0,Grid.EXTERIOR,0);
        grid.setData(2,0,0,Grid.EXTERIOR,0);

        VoxelData vd = grid.getVoxelData();
        grid.getData(3,0,0,vd);
        assertEquals(Grid.OUTSIDE,vd.getState());

        grid.getData(2,0,0,vd);
        assertEquals(Grid.EXTERIOR,vd.getState());

        grid.getData(1,0,0,vd);
        assertEquals(Grid.EXTERIOR,vd.getState());

        // Test 3rd quadrant
        grid.setData(3,0,0,Grid.INTERIOR,0);

        grid.getData(3,0,0,vd);
        assertEquals(Grid.INTERIOR,vd.getState());
    }

    /**
     * Test set/get all data points.
     */
    public void testCollapse() {
        OctreeAttributeGridByte grid = new OctreeAttributeGridByte(4, 4, 4, 0.001, 0.001);

        grid.setData(0,0,0,Grid.EXTERIOR,0);
        grid.setData(0,1,0,Grid.EXTERIOR,0);
        grid.setData(1,0,0,Grid.EXTERIOR,0);
        grid.setData(1,1,0,Grid.EXTERIOR,0);
        grid.setData(0,0,1,Grid.EXTERIOR,0);
        grid.setData(0,1,1,Grid.EXTERIOR,0);
        grid.setData(1,0,1,Grid.EXTERIOR,0);

        int count1 = grid.getCellCount();
        grid.setData(1,1,1,Grid.EXTERIOR,0);

        int count2 = grid.getCellCount();

        assertTrue("Collapse worked", count2 < count1);

        VoxelData vd = grid.getVoxelData();
        grid.getData(3,0,0,vd);
        assertEquals(Grid.OUTSIDE,vd.getState());

        grid.getData(1,0,0,vd);
        assertEquals(Grid.EXTERIOR,vd.getState());
    }

    /**
     * Test set/get all data points.
     */
/*
    public void testMultiLevelCollapse() {
        OctreeAttributeGridByte grid = new OctreeAttributeGridByte(4, 4, 4, 0.001, 0.001);

        grid.setData(0,0,0,Grid.EXTERIOR,0);
        grid.setData(0,1,0,Grid.EXTERIOR,0);
        grid.setData(1,0,0,Grid.EXTERIOR,0);
        grid.setData(1,1,0,Grid.EXTERIOR,0);
        grid.setData(0,0,1,Grid.EXTERIOR,0);
        grid.setData(0,1,1,Grid.EXTERIOR,0);
        grid.setData(1,0,1,Grid.EXTERIOR,0);

        int count1 = grid.getCellCount();
        grid.setData(1,1,1,Grid.EXTERIOR,0);

        int count2 = grid.getCellCount();

System.out.println("grid2");
grid.printTree();
        assertTrue("Collapse worked", count2 < count1);

        // Fill in octant grid to cause further collapse
        grid.setData(0,0,2,Grid.EXTERIOR,0);
        grid.setData(0,1,2,Grid.EXTERIOR,0);
        grid.setData(1,0,2,Grid.EXTERIOR,0);
        grid.setData(1,1,2,Grid.EXTERIOR,0);
        grid.setData(0,0,3,Grid.EXTERIOR,0);
        grid.setData(0,1,3,Grid.EXTERIOR,0);
        grid.setData(1,0,3,Grid.EXTERIOR,0);
        grid.setData(1,1,3,Grid.EXTERIOR,0);

        int count3 = grid.getCellCount();
System.out.println("count1: " + count1 + " count2: " + count2 + " count3: " + count3);
System.out.println("grid3");
grid.printTree();

        assertTrue("Collapse2 worked", count3 < count2);

        VoxelData vd = grid.getData(3,0,0);
        assertEquals(Grid.OUTSIDE,vd.getState());

        vd = grid.getData(1,0,0);
        assertEquals(Grid.EXTERIOR,vd.getState());
    }
*/

    /**
     * Test the constructors and the grid size.
     */
    public void testOctreeGridByte() {
        AttributeGrid grid =new OctreeAttributeGridByte(1, 1, 1, 0.001, 0.001);
        assertEquals("Array size is not 1", 1, grid.getWidth()*grid.getHeight()*grid.getDepth());

        grid = new OctreeAttributeGridByte(100, 101, 102, 0.001, 0.001);
        assertEquals("Array size is not 1030200", 1030200, grid.getWidth()*grid.getHeight()*grid.getDepth());
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGetByVoxelCoords() {
        AttributeGrid grid =new OctreeAttributeGridByte(1, 1, 1, 0.001, 0.001);
        setGetAllVoxelCoords(grid);

        grid = new OctreeAttributeGridByte(3,3,3,0.001, 0.001);
        setGetAllVoxelCoords(grid);

        grid = new OctreeAttributeGridByte(11, 11, 11, 0.001, 0.001);
        setGetAllVoxelCoords(grid);

        // Too slow for smoke tests
        grid = new OctreeAttributeGridByte(100, 100, 100, 0.001, 0.001);
        setGetAllVoxelCoords(grid);
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGetByVoxelCoordsStripped() {
        AttributeGrid grid =new OctreeAttributeGridByte(1, 1, 1, 0.001, 0.001);
        setGetAllVoxelCoordsStripped(grid);

        grid = new OctreeAttributeGridByte(3,3,3,0.001, 0.001);
        setGetAllVoxelCoordsStripped(grid);

        grid = new OctreeAttributeGridByte(11, 11, 11, 0.001, 0.001);
        setGetAllVoxelCoordsStripped(grid);

        // Too slow for smoke tests
        grid = new OctreeAttributeGridByte(100, 100, 100, 0.001, 0.001);
        setGetAllVoxelCoordsStripped(grid);
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGetByVoxelCoordsDiagonal() {
        AttributeGrid grid =new OctreeAttributeGridByte(1, 1, 1, 0.001, 0.001);
        setGetAllVoxelCoordsDiagonal(grid);

        grid = new OctreeAttributeGridByte(3,3,3,0.001, 0.001);
        setGetAllVoxelCoordsDiagonal(grid);

        grid = new OctreeAttributeGridByte(11, 11, 11, 0.001, 0.001);
        setGetAllVoxelCoordsDiagonal(grid);

        // Too slow for smoke tests
        grid = new OctreeAttributeGridByte(100, 100, 100, 0.001, 0.001);
        setGetAllVoxelCoordsDiagonal(grid);
    }

    /**
     * Test getState by voxels.
     */
    public void testGetStateByVoxel() {
        AttributeGrid grid =new OctreeAttributeGridByte(10, 9, 8, 0.001, 0.001);
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
/*
    public void testGetStateByCoord() {
        AttributeGrid grid =new OctreeAttributeGridByte(1.0, 1.0, 1.0, 0.01, 0.01);

        // set and test get on some random world coordinates
        grid.setData(0.0, 0.0, 0.0, Grid.OUTSIDE, (byte)2);
        grid.setData(0.95, 0.39, 0.45, Grid.EXTERIOR, (byte)1);
        grid.setData(0.6, 0.1, 0.4, Grid.INTERIOR, (byte)0);
        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0.0, 0.0, 0.0));
        assertEquals("State should be ", Grid.EXTERIOR, grid.getState(0.95, 0.39, 0.45));
        assertEquals("State should be ", Grid.INTERIOR, grid.getState(0.6, 0.1, 0.4));

        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid = new OctreeAttributeGridByte(0.12, 0.12, 0.12, 0.05, 0.05);
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
    }
*/

    /**
     * Test getAttribute by voxels.
     */
    public void testGetMaterialByVoxel() {
        AttributeGrid grid =new OctreeAttributeGridByte(10, 10, 10, 0.001, 0.001);
        grid.setData(9, 8, 7, Grid.EXTERIOR, 2);
        grid.setData(5, 0, 7, Grid.INTERIOR, 1);

        assertEquals("State should be ", 2, grid.getAttribute(9, 8, 7));
        assertEquals("State should be ", 1, grid.getAttribute(5, 0, 7));

        // Index that are not set should default to 0
        assertEquals("State should be ", 0, grid.getAttribute(2, 2, 2));
    }

    /**
     * Test getAttribute by world coordinates.
     */
/*
    public void testGetMaterialByCoord() {
        AttributeGrid grid =new OctreeAttributeGridByte(1.0, 0.4, 0.5, 0.05, 0.01);

        // set and test get on some random world coordinates
        grid.setData(0.0, 0.0, 0.0, Grid.OUTSIDE, (byte)3);
        grid.setData(0.95, 0.39, 0.45, Grid.EXTERIOR, (byte)2);
        grid.setData(0.6, 0.1, 0.4, Grid.INTERIOR, (byte)1);
        assertEquals("State should be ", 3, grid.getAttribute(0.0, 0.0, 0.0));
        assertEquals("State should be ", 2, grid.getAttribute(0.95, 0.39, 0.45));
        assertEquals("State should be ", 1, grid.getAttribute(0.6, 0.1, 0.4));

        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid = new OctreeAttributeGridByte(0.12, 0.11, 0.16, 0.05, 0.05);
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
*/

    /**
     * Test set/get byte material range.
     */
    public void testByteMaterialRange() {
        int width = 63;
        int maxMaterial = 63;
        long mat, expectedMat;

        OctreeAttributeGridByte grid = new OctreeAttributeGridByte(width, 1, 1, 0.001, 0.001);

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
     * Test set/get short material range.
     */
/*
    public void testShortMaterialRange() {
        int width = 100;
        int maxMaterial = (int) Math.pow(2.0, 14.0);

        AttributeGrid grid =new MaterialIndexedAttributeGridShort(width, 1, 1, 0.001, 0.001);
        grid.setData(0, 0, 0, Grid.EXTERIOR, 0);
        grid.setData(1, 0, 0, Grid.EXTERIOR, maxMaterial-1);
        grid.setData(2, 0, 0, Grid.EXTERIOR, maxMaterial+1);
        grid.setData(3, 0, 0, Grid.EXTERIOR, 2 * maxMaterial - 1);

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
        grid.setData(0, 0, 0, Grid.EXTERIOR, 0);
        grid.setData(1, 0, 0, Grid.EXTERIOR, maxMaterial-1);
        grid.setData(2, 0, 0, Grid.EXTERIOR, maxMaterial+1);
        grid.setData(3, 0, 0, Grid.EXTERIOR, 2 * maxMaterial - 1);

        assertEquals("Material [0,0,0] is not 0", 0, grid.getAttribute(0, 0, 0));
        assertEquals("Material [1,0,0] is not " + (maxMaterial-1), (maxMaterial-1), grid.getAttribute(1, 0, 0));
        assertEquals("Material [2,0,0] is not 1", 1, grid.getAttribute(2, 0, 0));
        assertEquals("Material [3,0,0] is not " + (maxMaterial-1), (maxMaterial-1), grid.getAttribute(3, 0, 0));

    }
*/
    /**
     * Test getGridCoords.
     */
/*
    public void testGetGridCoords() {
        double xWorldCoord = 1.0;
        double yWorldCoord = 0.15;
        double zWorldCoord = 0.61;
        double voxelWidth = 0.02;
        double sliceHeight = 0.01;

        AttributeGrid grid =new OctreeAttributeGridByte(xWorldCoord, yWorldCoord, zWorldCoord, voxelWidth, sliceHeight);

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
*/

    /**
     * Test getWorldCoords.
     */
    public void testGetWorldCoords() {
        int xVoxels = 64;
        int yVoxels = 64;
        int zVoxels = 64;
        double voxelWidth = 0.02;
        double sliceHeight = 0.02;

        AttributeGrid grid =new OctreeAttributeGridByte(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight);

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
        int xVoxels = 64;
        int yVoxels = 64;
        int zVoxels = 64;
        double voxelWidth = 0.02;
        double sliceHeight = 0.02;

        AttributeGrid grid =new OctreeAttributeGridByte(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight);

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
        int width = 64;

        // voxel coordinates
        AttributeGrid grid =new OctreeAttributeGridByte(width, width, width, 0.05, 0.05);
        assertEquals("Width is not " + width, width, grid.getWidth());

        // world coordinates
        double xcoord = 0.12;
        double voxelSize = 0.05;
        width = (int)(xcoord/voxelSize) + 1;

        grid = new OctreeAttributeGridByte(xcoord, xcoord, xcoord, voxelSize, 0.05);
        assertEquals("Width is not " + width, width, grid.getWidth());
    }

    /**
     * Test getHeight with both constructor methods.
     */
    public void testGetHeight() {
        int height = 64;

        // voxel coordinates
        AttributeGrid grid =new OctreeAttributeGridByte(height, height, height, 0.05, 0.05);
        assertEquals("Height is not " + height, height, grid.getHeight());

        // world coordinates
        double ycoord = 0.11;
        double sliceHeight = 0.05;
        height = (int)(ycoord/sliceHeight) + 1;

        grid = new OctreeAttributeGridByte(ycoord, ycoord, ycoord, 0.05, sliceHeight);
        assertEquals("Height is not " + height, height, grid.getHeight());
    }

    /**
     * Test getDepth with both constructor methods.
     */
    public void testGetDepth() {
        int depth = 32;

        // voxel coordinates
        AttributeGrid grid =new OctreeAttributeGridByte(depth, depth, depth, 0.05, 0.05);
        assertEquals("Depth is not " + depth, depth, grid.getDepth());

        // world coordinates
        double zcoord = 0.12;
        double voxelSize = 0.05;
        depth = (int)(zcoord/voxelSize) + 1;

        grid = new OctreeAttributeGridByte(zcoord, zcoord, zcoord, voxelSize, 0.05);
        assertEquals("Depth is not " + depth, depth, grid.getDepth());
    }

    /**
     * Test findCount by voxel class.
     */
    public void testFindCount() {
        int width = 16;
        int height = 16;
        int depth = 16;
        int[] row = {0, 3, 5};
        byte[] state = {Grid.INTERIOR, Grid.EXTERIOR, Grid.INTERIOR};

        AttributeGrid grid =new OctreeAttributeGridByte(width, height, depth, 0.05, 0.05);

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
     * Test that find voxels by VoxelClass actually found the voxels in the correct coordinates
     */
    public void testFindInterruptableVoxelClassIterator() {
        int width = 16;
        int height = 16;
        int depth = 16;
        long mat = 1;

        AttributeGrid grid =new OctreeAttributeGridByte(width, height, depth, 0.001, 0.001);
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

        grid = new OctreeAttributeGridByte(width, height, depth, 0.001, 0.001);
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
     * Test that find voxels by material actually found the voxels in the correct coordinates
     */
    public void testFindMaterialIterator() {
        int width = 16;
        int height = 16;
        int depth = 16;
        long mat1 = 1;
        long mat2 = 2;

        AttributeGrid grid =new OctreeAttributeGridByte(width, height, depth, 0.001, 0.001);
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

        grid = new OctreeAttributeGridByte(width, height, depth, 0.001, 0.001);
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
        int width = 16;
        int height = 16;
        int depth = 16;
        long mat1 = 1;
        long mat2 = 2;

        AttributeGrid grid =new OctreeAttributeGridByte(width, height, depth, 0.001, 0.001);
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

        grid = new OctreeAttributeGridByte(width, height, depth, 0.001, 0.001);
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
        int width = 16;
        int height = 16;
        int depth = 16;
        long mat1 = 1;
        long mat2 = 2;

        AttributeGrid grid =new OctreeAttributeGridByte(width, height, depth, 0.001, 0.001);
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

        grid = new OctreeAttributeGridByte(width, height, depth, 0.001, 0.001);
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
        int width = 16;
        int height = 16;
        int depth = 16;
        long mat1 = 1;
        long mat2 = 2;

        AttributeGrid grid =new OctreeAttributeGridByte(width, height, depth, 0.001, 0.001);
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

        grid = new OctreeAttributeGridByte(width, height, depth, 0.001, 0.001);
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
     * Test findCount by voxel class.
     */
    public void testFindCountByVoxelClass() {
        int width = 16;
        int height = 16;
        int depth = 16;
        int[] row = {0, 3, 5};
        byte[] state = {Grid.INTERIOR, Grid.EXTERIOR, Grid.INTERIOR};

        AttributeGrid grid =new OctreeAttributeGridByte(width, height, depth, 0.05, 0.05);

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
        int width = 16;
        int height = 16;
        int depth = 16;
        long material0 = 2;
        long material1 = 5;
        long material2 = 12;
        int[] materialDepth = {10, 6, 1};
        long[] material = {material0, material1, material2};

        AttributeGrid grid =new OctreeAttributeGridByte(width, height, depth, 0.05, 0.05);

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
        grid = new OctreeAttributeGridByte(width, height, depth, 0.05, 0.05);
        for (int x=0; x<width; x++) {
            grid.setData(x,0,0, Grid.EXTERIOR, mat);
        }

        assertEquals("Material count is not " + width, width, grid.findCount(mat));

        grid = new OctreeAttributeGridByte(width, height, depth, 0.05, 0.05);
        for (int y=0; y<height; y++) {
            grid.setData(0, y, 0, Grid.INTERIOR, mat);
        }

        assertEquals("Material count is not " + height, height, grid.findCount(mat));

    }

    /**
     * Test that remove material removes all specified material
     */
    public void testRemoveMaterialIterator() {
        int width = 16;
        int height = 16;
        int depth = 16;
        long mat1 = 1;
        long mat2 = 2;

        AttributeGrid grid =new OctreeAttributeGridByte(width, height, depth, 0.001, 0.001);
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
