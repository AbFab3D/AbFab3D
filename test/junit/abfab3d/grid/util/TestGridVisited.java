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

package abfab3d.grid.util;

// External Imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Internal Imports
import abfab3d.grid.util.GridVisited;
import abfab3d.grid.VoxelCoordinate;

/**
 * Tests the functionality of the GridVisited class
 *
 * @author Alan Hudson
 * @version
 */
public class TestGridVisited extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestGridVisited.class);
    }

    /**
     * Test basic operation
     */
    public void testBasic() {
        GridVisited gv = new GridVisited(10,10,10,1);

        gv.setVisited(0,0,0, true);

        assertEquals("Get Visited", gv.getVisited(0,0,0),true);
        assertEquals("Get Not Visited", gv.getVisited(1,0,0),false);
    }

    /**
     * Test get/set all
     */
    public void testGetSetAll() {
        int w = 10;
        int h = 10;
        int d = 10;

        GridVisited gv = new GridVisited(w,h,d,1);

        for(int i=0; i < w; i++) {
            for(int j=0; j < h; j ++) {
                for(int k=0; k < d; k++) {

                    if (i % 2 == 0 &&
                        j % 2 == 0 &&
                        k % 2 == 0) {

                        gv.setVisited(i,j,k,true);
                    }
                }
            }
        }

        for(int i=0; i < w; i++) {
            for(int j=0; j < h; j ++) {
                for(int k=0; k < d; k++) {

                    if (i % 2 == 0 &&
                        j % 2 == 0 &&
                        k % 2 == 0) {

                        assertTrue("AllGet True", gv.getVisited(i,j,k));
                    } else {
                        assertFalse("AllGet False", gv.getVisited(i,j,k));
                    }
                }
            }
        }
    }

    /**
     * Test basic operation
     */
    public void testChangeRep() {
        // Force change on first add with a 0X multiplier
        GridVisited gv = new GridVisited(10,10,10,0);

        gv.setVisited(0,0,0, true);
        gv.setVisited(0,1,0, true);
        gv.setVisited(0,1,1, true);

        assertEquals("Get Visited", gv.getVisited(0,0,0),true);
        assertEquals("Get Visited", gv.getVisited(0,1,0),true);
        assertEquals("Get Visited", gv.getVisited(0,1,1),true);

        assertEquals("Get Not Visited", gv.getVisited(1,0,0),false);
    }

    /**
     * Test basic operation
     */
    public void testVoxelCoordinate() {
        GridVisited gv = new GridVisited(10,10,10,1);

        gv.setVisited(new VoxelCoordinate(0,0,0), true);

        assertEquals("Get Visited", gv.getVisited(new VoxelCoordinate(0,0,0)),true);
        assertEquals("Get Not Visited", gv.getVisited(new VoxelCoordinate(1,0,0)),false);
    }

}
