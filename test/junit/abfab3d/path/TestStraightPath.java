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

package abfab3d.path;

//External Imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Internal Imports
import abfab3d.BaseTestCase;
import abfab3d.path.StraightPath;

// Internal Imports

/**
 * Tests the functionality of a ArrayGrid.
 *
 * @author Alan Hudson
 * @version
 */
public class TestStraightPath extends BaseTestCase  {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestStraightPath.class);
    }
    
    public void testNextXDirection() {
        int width = 20;
        int height = 15;
        int depth = 12;
        int countToLastVoxel = 0;
        int expectedCount;

    	//----------------------------------------
    	// test in the positive X direction
    	//----------------------------------------
        int[] dir = {1, 0, 0};
        int[][] posData = {{0,0,0}, {8,10,12}, {19,14,11}};
        
        StraightPath path = new StraightPath(dir);
        
        for (int i=0; i<posData.length; i++) {
        	expectedCount = width - posData[i][0] - 1;
        	
        	countToLastVoxel = getNextPathCount(path, posData[i], width, height, depth);
            
        	assertEquals("Next should occur " + expectedCount + " times", expectedCount, countToLastVoxel);
        }
        
        // test when expected next count is 0
        width = 1;
    	int[] pos = {0,  0, 0};
    	expectedCount = 0;
    	
    	countToLastVoxel = getNextPathCount(path, pos, width, height, depth);
    	assertEquals("Next should occur " + expectedCount + " times", expectedCount, countToLastVoxel);

    	//----------------------------------------
    	// test in the negative X direction
    	//----------------------------------------
        width = 20;
    	dir = new int[] {-1, 0, 0};
        posData = new int[][] {{0,0,0}, {8,10,12}, {19,14,11}};
        
        path = new StraightPath(dir);
        
        for (int i=0; i<posData.length; i++) {
        	expectedCount = posData[i][0];

        	countToLastVoxel = getNextPathCount2(path, posData[i], width, height, depth);
            
        	assertEquals("Next should occur " + expectedCount + " times", expectedCount, countToLastVoxel);
        }
        
        // test when expected next count is 0
        width = 1;
    	pos = new int[] {0,  0, 0};
    	expectedCount = 0;
    	
    	countToLastVoxel = getNextPathCount(path, pos, width, height, depth);
    	assertEquals("Next should occur " + expectedCount + " times", expectedCount, countToLastVoxel);
    }
    
    /**
     * Test invertPath.
     */
    public void testInvertPath() {
        int width = 20;
        int height = 15;
        int depth = 12;
        int countToLastVoxel = 0;
        int expectedCount;

    	//----------------------------------------
    	// test in the positive X direction
    	//----------------------------------------
        int[] dir = {1, 0, 0};
        int[] pos = {8,10,12};
        
        StraightPath path = new StraightPath(dir);
        
    	expectedCount = width - pos[0] - 1;
    	countToLastVoxel = getNextPathCount(path, pos, width, height, depth);
    	assertEquals("Next should occur " + expectedCount + " times", expectedCount, countToLastVoxel);
    	
    	//----------------------------------------
    	// invert the path to the negative X direction
    	//----------------------------------------
    	pos = new int[] {8,10,12};
    	path = (StraightPath) path.invertPath();
    	
    	expectedCount = pos[0];
    	countToLastVoxel = getNextPathCount(path, pos, width, height, depth);
    	assertEquals("Next should occur " + expectedCount + " times", expectedCount, countToLastVoxel);
    }
    
    public void testInvalidPath() {
    	// test invalid path
        int[] dir = {0, 0, 0};
        StraightPath path = null;
        
        try {
        	path = new StraightPath(dir);
        	fail("Did not throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        	assertNull(path);
        }
        
    }
    
    public void testGetExtants() {
        int width = 20;
        int height = 15;
        int depth = 12;

    	//----------------------------------------
    	// test in the positive X direction
    	//----------------------------------------
        int[] dir = {1, 0, -1};
        int[] curPos = new int[] {8,10,5};
        
        StraightPath path = new StraightPath(dir);
        path.init(curPos, width, height, depth);
        
        int[] extents = new int[6];
        path.getExtents(extents);
        
        assertEquals(8, extents[0]);
        assertEquals(19, extents[1]);
        assertEquals(10, extents[2]);
        assertEquals(10, extents[3]);
        assertEquals(5, extents[4]);
        assertEquals(11, extents[5]);
    }
    
    public void testAxisAligned() {
        int[] dir = {0, 1, 0};
        StraightPath path = new StraightPath(dir);
        
        assertTrue("Path " + java.util.Arrays.toString(dir) + " is not axis aligned", path.isAxisAligned());
        
        dir = new int[] {1, -1, 0};
        path = new StraightPath(dir);
        
        assertFalse("Path " + java.util.Arrays.toString(dir) + " should not be axis aligned", path.isAxisAligned());
    }
    
    public void testGetDir() {
        int[] dir = {0, 1, -1};
        StraightPath path = new StraightPath(dir);
        
        int[] pathDir = path.getDir();
        
        assertArrayEquals("", dir, pathDir);
    }
    
    public void testMisc() {
        int[] dir = {0, 1, -1};
        StraightPath path = new StraightPath(dir);

        int idx = 0;
        idx = path.toString().indexOf(java.util.Arrays.toString(dir));
        
        assertTrue(idx > 0);
    }
    
    private int getNextPathCount(Path path, int[] pos, int width, int height, int depth) {
    	int count = 0;
    	path.init(pos, width, height, depth);
    	
        while(path.next(pos)) {
        	count++;
        }
        
    	return count;
    }
    
    private int getNextPathCount2(StraightPath path, int[] pos, int width, int height, int depth) {
    	int count = 0;
    	path.init(pos[0], pos[1], pos[2], width, height, depth);
    	
        while(path.next(pos)) {
        	count++;
        }
        
    	return count;
    }
}
