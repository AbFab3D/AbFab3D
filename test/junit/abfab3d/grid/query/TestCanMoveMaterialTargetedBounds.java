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

package abfab3d.grid.query;

// External Imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Internal Imports
import abfab3d.grid.*;
import abfab3d.path.StraightPath;

/**
 * Tests the functionality of CanMoveMaterial Query.
 *
 * @author Alan Hudson
 * @version
 */
public class TestCanMoveMaterialTargetedBounds extends BaseTestGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestCanMoveMaterialTargetedBounds.class);
    }

    /**
     * Test basic operation
     */
    public void testBasic() {
        int size = 20;

        Grid grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        // Fill voxels such that it looks like:
        //      22222222222
        //      2         2
        //      2  11111  2
        //      2         2
        //      2         2
        setX(grid, 10, 10, Grid.EXTERIOR, 1, 8, 12);
        
//        setX(grid, 8, 10, Grid.EXTERIOR, 2, 5, 15);
        setX(grid, 12, 10, Grid.EXTERIOR, 2, 5, 15);
        setY(grid, 5, 10, Grid.EXTERIOR, 2, 8, 11);
        setY(grid, 15, 10, Grid.EXTERIOR, 2, 8, 11);
        
        int[] minBounds = {5, 8, 10};
        int[] maxBounds = {15, 12, 10};

        // Move mat 1 in positive x direction
        boolean escaped = canMoveBounds(grid, new int[] {1,0,0}, 1, 2, minBounds, maxBounds);
        assertTrue("Positive X Axis move is not false", escaped == false);

        // Move mat 1 in positive y direction
        escaped = canMoveBounds(grid, new int[] {0,1,0}, 1, 2, minBounds, maxBounds);
        assertTrue("Positive Y Axis move is not false", escaped == false);
        
        // Move mat 1 in negative y direction 
        escaped = canMoveBounds(grid, new int[] {0,-1,0}, 1, 2, minBounds, maxBounds);
        assertTrue("Negative Y Axis move is not true", escaped == true);
        
        // Move mat 1 in positive z  direction 
        escaped = canMoveBounds(grid, new int[] {0,0,1}, 1, 2, minBounds, maxBounds);

        assertTrue("Positive Z Axis move is not true", escaped == true);

    }

    /**
     * Test ignore of intersections with non targeted material
     */
    public void testIgnoreOther() {
        int size = 20;

        Grid grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        // Add Object 1
        setZ(grid, 10, 8, Grid.EXTERIOR, 1, 5, 15);

        // Add Object 2
        setZ(grid, 10, 12, Grid.EXTERIOR, 2, 5, 15);

        // Add Object 3
        setZ(grid, 12, 8, Grid.EXTERIOR, 3, 5, 15);

        // Move mat 1 in positive x direction to collide with mat 3, but mat 2 is targeted
        int[] minBounds = {10, 12, 5};
        int[] maxBounds = {10, 12, 15};
        boolean escaped = canMoveBounds(grid, new int[] {1,0,0}, 1, 2, minBounds, maxBounds);

        assertTrue("X Axis move", escaped == true);
        System.out.println("Escaped: " + escaped);

        // Move mat 1 to collide with mat 2, with mat 2 is targeted
        minBounds = new int[] {10, 12, 5};
        maxBounds = new int[] {10, 12, 15};
        escaped = canMoveBounds(grid, new int[] {0,1,0}, 1, 2, minBounds, maxBounds);

        assertTrue("Y Axis move", escaped == false);

    }

    /**
     * Test a complex move where movement is only allowed in one direction
     */
    public void testComplexTrue() {
        int matToMove = 2;
        int target = 1;

        Grid grid = new ArrayGridByte(100,100,100,0.001, 0.001);
        MaterialIndexedWrapper wrapper = new MaterialIndexedWrapper(grid);

        // set the voxels of a square
        setX(wrapper, 50, 40, Grid.EXTERIOR, 1, 40, 60);
        setX(wrapper, 50, 60, Grid.EXTERIOR, 1, 40, 60);
        setZ(wrapper, 40, 50, Grid.EXTERIOR, 1, 40, 60);
        setZ(wrapper, 60, 50, Grid.EXTERIOR, 1, 40, 60);

        // set the voxels of a T shape with the bottom of the T intersecting the opening of the square
        setX(wrapper, 60, 50, Grid.EXTERIOR, matToMove, 30, 70);
        setY(wrapper, 50, 50, Grid.EXTERIOR, matToMove, 40, 60);

        int[] minBounds = {40, 50, 40};
        int[] maxBounds = {60, 50, 60};
        
        //------------------------------------------------------
        // test movement in all directions
        // should only be able to move in {0,1,0} direction
        //------------------------------------------------------

        // set the paths
        int[][] paths = {
                {-1,0,0}, {1,0,0},
                {0,0,-1}, {0,0,1},
                {0,-1,0}, {0,1,0}
        };
        
        boolean[] result = {
                false, false,
                false, false,
                false, true
        };
        
        boolean escaped;
        
        // assert that movement is initially false in all paths
        for (int j=0; j<paths.length; j++) {
//System.out.println("\n=====> Path: " + java.util.Arrays.toString(paths[j]));
            escaped = canMoveBounds(wrapper, paths[j], matToMove, target, minBounds, maxBounds);
            assertEquals(
                    java.util.Arrays.toString(paths[j]) + " move is not " + result[j],
                    result[j],
                    escaped);
        }

    }

    /**
     * Test a complex move where movement is not allowed
     */
    public void testComplexFalse() {
        int matToMove = 2;
        int target = 1;
        Grid grid = new ArrayGridByte(100,100,100,0.001, 0.001);

        // set the voxels of a square
        setX(grid, 50, 40, Grid.EXTERIOR, 1, 40, 60);
        setX(grid, 50, 60, Grid.EXTERIOR, 1, 40, 60);
        setZ(grid, 40, 50, Grid.EXTERIOR, 1, 40, 60);
        setZ(grid, 60, 50, Grid.EXTERIOR, 1, 40, 60);

        // set the voxels of an I shape with the vertical part intersecting the opening of the square
        setX(grid, 60, 50, Grid.EXTERIOR, matToMove, 30, 70);
        setX(grid, 40, 50, Grid.EXTERIOR, matToMove, 30, 70);
        setY(grid, 50, 50, Grid.EXTERIOR, matToMove, 40, 60);

        int[] minBounds = {40, 50, 40};
        int[] maxBounds = {60, 50, 60};
        
        //------------------------------------------------------
        // test movement in all directions
        // movement should fail in all drections
        //------------------------------------------------------

        // set the paths
        int[][] paths = {
                {-1,0,0}, {1,0,0},
                {0,0,-1}, {0,0,1},
                {0,-1,0}, {0,1,0}
        };
        
        boolean escaped;
        
        // assert that movement is initially false in all paths
        for (int j=0; j<paths.length; j++) {
//System.out.println("\n=====> Path: " + java.util.Arrays.toString(paths[j]));
            escaped = canMoveBounds(grid, paths[j], matToMove, target, minBounds, maxBounds);
            assertEquals(
                    java.util.Arrays.toString(paths[j]) + " move is not false",
                    false,
                    escaped);
        }
    }

    /**
     * Test movement in all 26 paths
     *
     * 1) Set the center voxel to a material. This is the material to move.
     * 2) Set the all the grid edge voxels to a different material. This
     *    should block the center material from escaping.
     * 3) For each path, reset the corresponding grid edge voxel back to
     *    outside and material 0, and then test movement in that path.
     */
    public void testCanMoveAllPaths() {
        int material1 = 1;
        int material2 = 2;
        int size = 20;
        int center = size / 2;
        Grid grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        // set the material to move at the center of the grid
        grid.setData(center, center, center, Grid.EXTERIOR, material2);

        int startIndex = 5;
        int endIndex = 15;
        
        // set the edge voxels of the grid to a different material
        setPlaneX(grid, startIndex, Grid.EXTERIOR, material1);
        setPlaneX(grid, endIndex, Grid.EXTERIOR, material1);
        setPlaneY(grid, startIndex, Grid.EXTERIOR, material1);
        setPlaneY(grid, endIndex, Grid.EXTERIOR, material1);
        setPlaneZ(grid, startIndex, Grid.EXTERIOR, material1);
        setPlaneZ(grid, endIndex, Grid.EXTERIOR, material1);
        
        int[] minBounds = {5, 5, 5};
        int[] maxBounds = {15, 15, 15};

        //------------------------------------------------------
        // test movement in all directions
        //------------------------------------------------------

        // set the 26 paths
        int[][] paths = {
                {1,0,0},    {-1,0,0},
                {0,1,0},    {0,-1,0},
                {0,0,1},    {0,0,-1},
                {1,1,1},    {1,1,0},
                {1,1,-1},   {0,1,-1},
                {-1,1,-1},  {-1,1,0},
                {-1,1,1},   {0,1,1},
                {1,0,1},    {1,0,-1},
                {-1,0,-1},  {-1,0,1},
                {1,-1,1},   {1,-1,0},
                {1,-1,-1},  {0,-1,-1},
                {-1,-1,-1}, {-1,-1,0},
                {-1,-1,1},  {0,-1,1}
        };

        // the corresponding voxels that need to be reset to outside and
        // material 0 in order to allow the middle material to escape
        int[][] voxelsRemovedToAllowEscape = new int[paths.length][3];

        for (int i=0; i<paths.length; i++) {
            for (int j=0; j<3; j++) {
                if (paths[i][j] > 0) {
                    voxelsRemovedToAllowEscape[i][j] = endIndex;
                } else if (paths[i][j] < 0) {
                    voxelsRemovedToAllowEscape[i][j] = startIndex;
                } else {
                    voxelsRemovedToAllowEscape[i][j] = center;
                }
            }
//System.out.println("voxel: " + java.util.Arrays.toString(voxelsRemovedToAllowEscape[i]));
        }

        boolean escaped;

        // assert that movement is initially false in all paths
        for (int j=0; j<paths.length; j++) {
//System.out.println("\n=====> Path: " + java.util.Arrays.toString(paths[j]));
            escaped = canMoveBounds(grid, paths[j], material2, material1, minBounds, maxBounds);
            assertEquals(
                    java.util.Arrays.toString(paths[j]) + " move is not false",
                    false,
                    escaped);
        }


        // set the voxels to outside and material 0 in order to
        // allow escape in the corresponding path
        for (int j=0; j<paths.length; j++) {
//System.out.println("\n=====> Path: " + java.util.Arrays.toString(paths[j]));
            grid.setData(voxelsRemovedToAllowEscape[j][0], voxelsRemovedToAllowEscape[j][1], voxelsRemovedToAllowEscape[j][2], Grid.OUTSIDE, 0);

            escaped = canMoveBounds(grid, paths[j], material2, material1, minBounds, maxBounds);

            assertEquals(
                    java.util.Arrays.toString(paths[j]) + " move is not true",
                    true,
                    escaped);
        }

    }

    /**
     * Test ignored voxels.
     */
    public void testIgnoredVoxels() {
        int size = 12;

        Grid grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        // Add Object 1
        int mat1_count = 5;

        grid.setData(0,0,0, Grid.EXTERIOR, 1);
        grid.setData(0,0,1, Grid.INTERIOR, 1);
        grid.setData(0,0,2, Grid.INTERIOR, 1);
        grid.setData(0,0,3, Grid.INTERIOR, 1);
        grid.setData(0,0,4, Grid.EXTERIOR, 1);

        StraightPath path = new StraightPath(new int[] {0,0,-1});
        CanMoveMaterialTargeted query = new CanMoveMaterialTargeted(1, 2, path);
        boolean escaped = query.execute(grid);

//        assertTrue("X Axis move", escaped == true);
//        System.out.println("Escaped: " + escaped);

    }
    
    /**
     * Test basic operation
     */
    public void testGetIgnoredCount() {
        int size = 20;

        Grid grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        // Fill voxels such that it looks like:
        //
        //      22222222222
        //                2
        //         11111  2
        //         333 3  2
        //         33333  2
        //
        setX(grid, 10, 10, Grid.EXTERIOR, 1, 8, 12);
        setX(grid, 9, 10, Grid.EXTERIOR, 3, 8, 10);
        setX(grid, 8, 10, Grid.EXTERIOR, 3, 8, 12);
        grid.setData(12, 9, 10, Grid.EXTERIOR, 3);
        
        setX(grid, 12, 10, Grid.EXTERIOR, 2, 5, 15);
//        setY(grid, 5, 10, Grid.EXTERIOR, 2, 8, 11);
        setY(grid, 15, 10, Grid.EXTERIOR, 2, 8, 11);
        
        int[] minBounds = {5, 8, 10};
        int[] maxBounds = {15, 12, 10};

        // Move mat 1 in negative x direction
        StraightPath path = new StraightPath(new int[] {-1,0,0});
        CanMoveMaterialTargetedBounds query = 
        	new CanMoveMaterialTargetedBounds(1, 2, minBounds, maxBounds, path);
        boolean escaped = query.execute(grid);
        
        assertTrue("Negative X Axis move of material 1 is not true", escaped == true);
        assertEquals("Ignored voxel count is not 4", 4, query.getIgnoredCount());

        // Move mat 3 in negative x direction
        query =  new CanMoveMaterialTargetedBounds(3, 2, minBounds, maxBounds, path);
        escaped = query.execute(grid);

        assertTrue("Negative X Axis move of material 3 is not true", escaped == true);
        assertEquals("Ignored voxel count is not 6", 6, query.getIgnoredCount());
    }
    
    /**
     * Test performance between can move with and without using target bounds.
     */
    public void testPerformance() {
    	
    	int size = 500;
        int matToMove = 2;
        int target = 1;

        Grid grid = new ArrayGridByte(size,size,size,0.001, 0.001);
        MaterialIndexedWrapper wrapper = new MaterialIndexedWrapper(grid);

        // set the voxels of a square
        setX(wrapper, 50, 40, Grid.EXTERIOR, 1, 40, 60);
        setX(wrapper, 50, 60, Grid.EXTERIOR, 1, 40, 60);
        setZ(wrapper, 40, 50, Grid.EXTERIOR, 1, 40, 60);
        setZ(wrapper, 60, 50, Grid.EXTERIOR, 1, 40, 60);

        // set the voxels of a T shape with the bottom of the T intersecting the opening of the square
        setX(wrapper, 60, 50, Grid.EXTERIOR, matToMove, 30, 70);
        setY(wrapper, 50, 50, Grid.EXTERIOR, matToMove, 40, 60);

        int[] minBounds = {40, 50, 40};
        int[] maxBounds = {60, 50, 60};
        
        //------------------------------------------------------
        // test movement in all directions
        // should only be able to move in {0,1,0} direction
        //------------------------------------------------------

        // set the paths
        int[][] paths = {
                {-1,0,0}, {1,0,0},
                {0,0,-1}, {0,0,1},
                {0,-1,0}, {0,1,0}
        };
        
        boolean[] result = {
                false, false,
                false, false,
                false, true
        };
        
        boolean escaped;
        
        // execute a dummy run to load the classes
        dummyRun();
        
        long stime = System.nanoTime();
        
        // assert that movement is initially false in all paths
        for (int j=0; j<paths.length; j++) {
            escaped = canMoveBounds(grid, paths[j], matToMove, target, minBounds, maxBounds);
            assertEquals(
                    java.util.Arrays.toString(paths[j]) + " move is not " + result[j],
                    result[j],
                    escaped);
        }
        
        long totalTime = System.nanoTime() - stime;
        System.out.println("CanMoveMaterialTargetedBounds  : " + totalTime);
        
        stime = System.nanoTime();
        
        // assert that movement is initially false in all paths
        for (int j=0; j<paths.length; j++) {
            escaped = canMoveNoBounds(grid, paths[j], matToMove, target);
            assertEquals(
                    java.util.Arrays.toString(paths[j]) + " move is not " + result[j],
                    result[j],
                    escaped);
        }
        
        totalTime = System.nanoTime() - stime;
        System.out.println("CanMoveMaterialTargeted        : " + totalTime);
    	
    }

    /**
     * Execute can move of a material against a target material.  Can move is 
     * tested within the minimum and maximum bounds fo the target material.
     * 
     * @param grid The grid
     * @param dir The direction to check movement
     * @param mat The material to move
     * @param target The target material to check against
     * @param minBounds The minimum bounds of the target
     * @param maxBounds The maximum bounds of the target
     * @return True if the material can move away from the target material
     */
    private boolean canMoveBounds(Grid grid, int[] dir, int mat, int target,
    		int[] minBounds, int[] maxBounds) {
    	
        StraightPath path = new StraightPath(dir);
        CanMoveMaterialTargetedBounds query = 
        	new CanMoveMaterialTargetedBounds(mat, target, minBounds, maxBounds, path);

        return query.execute(grid);
    }

    /**
     * Execute can move of a material against a target material.
     * 
     * @param grid The grid
     * @param dir The direction to check movement
     * @param mat The material to move
     * @param target The target material to check against
     * @return True if the material can move away from the target material
     */
    private boolean canMoveNoBounds(Grid grid, int[] dir, int mat, int target) {
    	
        StraightPath path = new StraightPath(dir);
        CanMoveMaterialTargeted query = 
        	new CanMoveMaterialTargeted(mat, target, path);

        return query.execute(grid);
    }
    
    private Grid setSmallGrid() {
        int size = 20;
        int startIndex = 7;
        int endIndex = 15;
        int yIndex = 10;

        Grid grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        setX(grid, yIndex, 0, Grid.INTERIOR, 1, startIndex+1, endIndex-1);
        setX(grid, yIndex, 0, Grid.OUTSIDE, 0, 11, 12);

        grid.setData(startIndex,yIndex,0, Grid.EXTERIOR, 1);
        grid.setData(endIndex,yIndex,0, Grid.EXTERIOR, 1);
        grid.setData(10,yIndex,0, Grid.EXTERIOR, 1);
        grid.setData(13,yIndex,0, Grid.EXTERIOR, 1);

        // Set different material
//        grid.setData(5,yIndex,0, Grid.EXTERIOR, 2);
        grid.setData(18,yIndex,0, Grid.EXTERIOR, 2);
        grid.setData(7,5,0, Grid.EXTERIOR, 2);
        grid.setData(10,15,0, Grid.EXTERIOR, 2);

        return grid;
    }

    private Grid setLargeGrid() {
        int size = 500;
        int startIndex = 100;
        int endIndex = 300;
        int yIndex = 10;

        Grid grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        setX(grid, yIndex, 0, Grid.INTERIOR, 1, startIndex+1, endIndex-1);
        setX(grid, yIndex, 0, Grid.OUTSIDE, 0, 201, 249);

        grid.setData(startIndex,yIndex,0, Grid.EXTERIOR, 1);
        grid.setData(endIndex,yIndex,0, Grid.EXTERIOR, 1);
        grid.setData(200,yIndex,0, Grid.EXTERIOR, 1);
        grid.setData(250,yIndex,0, Grid.EXTERIOR, 1);

        // Set different material
//        grid.setData(50,yIndex,0, Grid.EXTERIOR, 2);
        grid.setData(400,yIndex,0, Grid.EXTERIOR, 2);
        grid.setData(100,5,0, Grid.EXTERIOR, 2);
        grid.setData(200,15,0, Grid.EXTERIOR, 2);

        return grid;
    }
    
    /**
     * Execute a dummy run to initialize the can move classes.
     */
    private void dummyRun() {
        int size = 10;

        Grid grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        grid.setData(2, 2, 2, Grid.EXTERIOR, 1);
        grid.setData(5, 5, 5, Grid.EXTERIOR, 2);
        
        int[] minBounds = {5, 5, 5};
        int[] maxBounds = {5, 5, 5};

        // Move mat 1 in positive x direction
        boolean escaped = canMoveBounds(grid, new int[] {1,0,0}, 1, 2, minBounds, maxBounds);
        assertTrue("Positive X Axis move is not false", escaped == true);
        
        escaped = canMoveNoBounds(grid, new int[] {1,0,0}, 1, 2);
        assertTrue("Positive X Axis move is not false", escaped == true);
    }

}
