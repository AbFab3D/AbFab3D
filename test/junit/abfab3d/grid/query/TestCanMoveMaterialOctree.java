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
 * Tests the functionality of CanMoveMaterial Query against Octree
 *
 * @author Alan Hudson
 * @version
 */
public class TestCanMoveMaterialOctree extends BaseTestGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestCanMoveMaterialOctree.class);
    }

    /**
     * Test basic operation
     */
    public void testBasic() {
        int size = 16;

        Grid grid = new OctreeGridByte(size,size,size,0.001, 0.001);

        // Add Object 1
        int mat1_count = 5;

        grid.setData(0,0,0, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,0,1, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,0,2, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,0,3, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,0,4, Grid.EXTERIOR, (byte) 1);

        // Add Object 2
        int mat2_count = 6;

        grid.setData(0,2,0, Grid.EXTERIOR, (byte) 2);
        grid.setData(0,2,1, Grid.EXTERIOR, (byte) 2);
        grid.setData(0,2,2, Grid.EXTERIOR, (byte) 2);
        grid.setData(0,2,3, Grid.EXTERIOR, (byte) 2);
        grid.setData(0,2,4, Grid.EXTERIOR, (byte) 2);
        grid.setData(0,2,5, Grid.EXTERIOR, (byte) 2);

        StraightPath path = new StraightPath(new int[] {1,0,0});
        CanMoveMaterial query = new CanMoveMaterial((byte) 1, path);
        boolean escaped = query.execute(grid);

        assertTrue("X Axis move", escaped == true);
        System.out.println("Escaped: " + escaped);

        path = new StraightPath(new int[] {0,1,0});
        query = new CanMoveMaterial((byte) 1, path);
        escaped = query.execute(grid);

        assertTrue("Y Axis move", escaped == false);

    }

    /**
     * Test a complex move where movement is only allowed in one direction
     */
    public void testComplexTrue() {
        int matToMove = 2;
        Grid grid = new OctreeGridByte(128,128,128,0.001, 0.001);

        // set the voxels of a square
        setX(grid, 50, 40, Grid.EXTERIOR, 1, 40, 60);
        setX(grid, 50, 60, Grid.EXTERIOR, 1, 40, 60);
        setZ(grid, 40, 50, Grid.EXTERIOR, 1, 40, 60);
        setZ(grid, 60, 50, Grid.EXTERIOR, 1, 40, 60);

        // set the voxels of a T shape with the bottom of the T intersecting the opening of the square
        setX(grid, 60, 50, Grid.EXTERIOR, matToMove, 30, 70);
        setY(grid, 50, 50, Grid.EXTERIOR, matToMove, 40, 60);

        //------------------------------------------------------
        // test movement in all directions
        // should only be able to move in {0,1,0} direction
        //------------------------------------------------------

        // negative X axis direction
        boolean escaped = canMove(grid, new int[] {-1,0,0}, matToMove);
        assertEquals("Negative X axis move is not false", false, escaped);

        // positive X axis direction
        escaped = canMove(grid, new int[] {1,0,0}, matToMove);
        assertEquals("Positive X axis move is not false", false, escaped);

        // negative Y axis direction
        escaped = canMove(grid, new int[] {0,-1,0}, matToMove);
        assertEquals("Negative Y axis move is not false", false, escaped);

        // negative Z axis direction
        escaped = canMove(grid, new int[] {0,0,-1}, matToMove);
        assertEquals("Negative Z axis move is not false", false, escaped);

        // positive Z axis direction
        escaped = canMove(grid, new int[] {0,0,1}, matToMove);
        assertEquals("Positive Z axis move is not false", false, escaped);

        // positive Y axis direction
        escaped = canMove(grid, new int[] {0,1,0}, matToMove);
        assertEquals("Positive Y axis move is not true", true, escaped);
    }

    /**
     * Test a complex move where movement is not allowed
     */
    public void testComplexFalse() {
        int matToMove = 2;
        Grid grid = new OctreeGridByte(128,128,128,0.001, 0.001);

        // set the voxels of a square
        setX(grid, 50, 40, Grid.EXTERIOR, 1, 40, 60);
        setX(grid, 50, 60, Grid.EXTERIOR, 1, 40, 60);
        setZ(grid, 40, 50, Grid.EXTERIOR, 1, 40, 60);
        setZ(grid, 60, 50, Grid.EXTERIOR, 1, 40, 60);

        // set the voxels of an I shape with the vertical part intersecting the opening of the square
        setX(grid, 60, 50, Grid.EXTERIOR, matToMove, 30, 70);
        setX(grid, 40, 50, Grid.EXTERIOR, matToMove, 30, 70);
        setY(grid, 50, 50, Grid.EXTERIOR, matToMove, 40, 60);

        //------------------------------------------------------
        // test movement in all directions
        // movement should fail in all drections
        //------------------------------------------------------

        // negative X axis direction
        boolean escaped = canMove(grid, new int[] {-1,0,0}, matToMove);
        assertEquals("Negative X axis move is not false", false, escaped);

        // positive X axis direction
        escaped = canMove(grid, new int[] {1,0,0}, matToMove);
        assertEquals("Positive X axis move is not false", false, escaped);

        // negative Y axis direction
        escaped = canMove(grid, new int[] {0,-1,0}, matToMove);
        assertEquals("Negative Y axis move is not false", false, escaped);

        // positive Y axis direction
        escaped = canMove(grid, new int[] {0,1,0}, matToMove);
        assertEquals("Positive Y axis move is not false", false, escaped);

        // negative Z axis direction
        escaped = canMove(grid, new int[] {0,0,-1}, matToMove);
        assertEquals("Negative Z axis move is not false", false, escaped);

        // positive Z axis direction
        escaped = canMove(grid, new int[] {0,0,1}, matToMove);
        assertEquals("Positive Z axis move is not false", false, escaped);
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
    // TODO: Need to review this test, it fails not certain if its the
    // the test or code.
/*
    public void testCanMoveAllPaths() {
        int material1 = 1;
        int material2 = 2;
        int size = 16;
        int center = size / 2;
        Grid grid = new OctreeGridByte(size,size,size,0.001, 0.001);

        // set the material to move at the center of the grid
        grid.setData(center, center, center, Grid.EXTERIOR, material2);

        // set the edge voxels of the grid to a different material
        setPlaneX(grid, 0, Grid.EXTERIOR, material1);
        setPlaneX(grid, size-1, Grid.EXTERIOR, material1);
        setPlaneY(grid, 0, Grid.EXTERIOR, material1);
        setPlaneY(grid, size-1, Grid.EXTERIOR, material1);
        setPlaneZ(grid, 0, Grid.EXTERIOR, material1);
        setPlaneZ(grid, size-1, Grid.EXTERIOR, material1);

        //------------------------------------------------------
        // test movement in all directions
        //------------------------------------------------------
        int endIndex = size - 1;

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
                    voxelsRemovedToAllowEscape[i][j] = 0;
                } else {
                    voxelsRemovedToAllowEscape[i][j] = center;
                }
            }
//System.out.println("voxel: " + java.util.Arrays.toString(voxelsRemovedToAllowEscape[i]));
        }

        boolean escaped;

        // assert that movement is initially false in all paths
        for (int j=0; j<paths.length; j++) {
            escaped = canMove(grid, paths[j], material2);
            assertEquals(
                    java.util.Arrays.toString(paths[j]) + " move is not false",
                    false,
                    escaped);
        }


        // set the voxels to outside and material 0 in order to
        // allow escape in the corresponding path
        for (int j=0; j<paths.length; j++) {
            grid.setData(voxelsRemovedToAllowEscape[j][0], voxelsRemovedToAllowEscape[j][1], voxelsRemovedToAllowEscape[j][2], Grid.OUTSIDE, 0);

            escaped = canMove(grid, paths[j], material2);

            assertEquals(
                    java.util.Arrays.toString(paths[j]) + " move is not true",
                    true,
                    escaped);
        }

    }
*/
    public void testIgnoredVoxels() {
        int size = 16;

        Grid grid = new OctreeGridByte(size,size,size,0.001, 0.001);

        // Add Object 1
        int mat1_count = 5;

        grid.setData(0,0,0, Grid.EXTERIOR, 1);
        grid.setData(0,0,1, Grid.INTERIOR, 1);
        grid.setData(0,0,2, Grid.INTERIOR, 1);
        grid.setData(0,0,3, Grid.INTERIOR, 1);
        grid.setData(0,0,4, Grid.EXTERIOR, 1);

        StraightPath path = new StraightPath(new int[] {0,0,-1});
        CanMoveMaterial query = new CanMoveMaterial(1, path);
        boolean escaped = query.execute(grid);

//        assertTrue("X Axis move", escaped == true);
//        System.out.println("Escaped: " + escaped);

    }

    public void testIgnoredVoxelsPerformance() {
        int size = 512;
        int startIndex = 50;
        int endIndex = 295;

        Grid grid = new OctreeGridByte(size,size,size,0.001, 0.001);

//        for (int i=startIndex; i<=endIndex; i++){
//          grid.setData(i,0,0, Grid.INTERIOR, (byte) 1);
//        }

        setX(grid, 0, 0, Grid.INTERIOR, 1, startIndex+1, endIndex-1);
        setX(grid, 0, 0, Grid.OUTSIDE, 1, 101, 109);

        grid.setData(startIndex,0,0, Grid.EXTERIOR, 1);
        grid.setData(endIndex,0,0, Grid.EXTERIOR, 1);
        grid.setData(100,0,0, Grid.EXTERIOR, 1);
        grid.setData(110,0,0, Grid.EXTERIOR, 1);

        StraightPath path = new StraightPath(new int[] {-1,0,0});
        CanMoveMaterial query = new CanMoveMaterial(1, path);

        long stime = System.nanoTime();
        query.execute(grid);
        long totalTime1 = System.nanoTime() - stime;

        System.out.println("With ignored voxels on        : " + totalTime1);

    }

    public void testPerformanceOneDirVsAllDir() {

//        Grid grid = setSmallGrid();
        Grid grid = setLargeGrid();

        // Set the paths
        int[][] directions = new int[][] {{-1,0,0}, {1,0,0}, {0,-1,0}, {0,1,0}};
        StraightPath[] paths = new StraightPath[directions.length];

        for (int i=0; i<directions.length; i++) {
            paths[i] = new StraightPath(directions[i]);
        }

        //------------------------------------------------------------
        // Can move material one path at a time for all voxels
        //------------------------------------------------------------
        CanMoveMaterial queryOnePath;

        long stime = System.nanoTime();

        for (int j=0; j<paths.length; j++) {
            queryOnePath = new CanMoveMaterial(1, paths[j]);
            queryOnePath.execute(grid);
        }

        long totalTime1 = System.nanoTime() - stime;
        System.out.println("CanMoveMaterial        : " + totalTime1);

        //------------------------------------------------------------
        // Can move material all paths at a time for each voxels
        //------------------------------------------------------------
        stime = System.nanoTime();

        CanMoveMaterialAllPaths queryAllPaths = new CanMoveMaterialAllPaths(1, paths);
        queryAllPaths.execute(grid);

        totalTime1 = System.nanoTime() - stime;
        System.out.println("CanMoveMaterialAllPaths: " + totalTime1);
    }

    private boolean canMove(Grid grid, int[] dir, int mat) {
        StraightPath path = new StraightPath(dir);
        CanMoveMaterial query = new CanMoveMaterial(mat, path);

        return query.execute(grid);
    }

    private Grid setSmallGrid() {
        int size = 32;
        int startIndex = 7;
        int endIndex = 15;
        int yIndex = 10;

        Grid grid = new OctreeGridByte(size,size,size,0.001, 0.001);

        setX(grid, yIndex, 0, Grid.INTERIOR, 1, startIndex+1, endIndex-1);
        setX(grid, yIndex, 0, Grid.OUTSIDE, 0, 11, 12);

        grid.setData(startIndex,yIndex,0, Grid.EXTERIOR, 1);
        grid.setData(endIndex,yIndex,0, Grid.EXTERIOR, 1);
        grid.setData(10,yIndex,0, Grid.EXTERIOR, 1);
        grid.setData(13,yIndex,0, Grid.EXTERIOR, 1);

        // Set different material
//        grid.setData(5,yIndex,0, Grid.EXTERIOR, (byte) 2);
        grid.setData(18,yIndex,0, Grid.EXTERIOR, 2);
        grid.setData(7,5,0, Grid.EXTERIOR, 2);
        grid.setData(10,15,0, Grid.EXTERIOR, 2);

        return grid;
    }

    private Grid setLargeGrid() {
        int size = 512;
        int startIndex = 100;
        int endIndex = 300;
        int yIndex = 10;

        Grid grid = new OctreeGridByte(size,size,size,0.001, 0.001);

        setX(grid, yIndex, 0, Grid.INTERIOR, 1, startIndex+1, endIndex-1);
        setX(grid, yIndex, 0, Grid.OUTSIDE, 0, 201, 249);

        grid.setData(startIndex,yIndex,0, Grid.EXTERIOR, 1);
        grid.setData(endIndex,yIndex,0, Grid.EXTERIOR, 1);
        grid.setData(200,yIndex,0, Grid.EXTERIOR, 1);
        grid.setData(250,yIndex,0, Grid.EXTERIOR, 1);

        // Set different material
//        grid.setData(50,yIndex,0, Grid.EXTERIOR, (byte) 2);
        grid.setData(400,yIndex,0, Grid.EXTERIOR, 2);
        grid.setData(100,5,0, Grid.EXTERIOR, 2);
        grid.setData(200,15,0, Grid.EXTERIOR, 2);

        return grid;
    }

}
