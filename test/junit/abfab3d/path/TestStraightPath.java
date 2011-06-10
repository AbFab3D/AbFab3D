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
import abfab3d.path.StraightPath;

// Internal Imports

/**
 * Tests the functionality of a ArrayGrid.
 *
 * @author Alan Hudson
 * @version
 */
public class TestStraightPath extends TestCase  {

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

        	countToLastVoxel = getNextPathCount(path, posData[i], width, height, depth);
            
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
    
    private int getNextPathCount(Path path, int[] pos, int width, int height, int depth) {
    	int count = 0;
    	path.init(pos, width, height, depth);
    	
        while(path.next(pos)) {
        	count++;
        }
        
    	return count;
    }
}
