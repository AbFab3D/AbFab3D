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
}
