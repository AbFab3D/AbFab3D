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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Internal Imports

/**
 * Tests the functionality of a ArrayGrid.
 *
 * @author Alan Hudson
 * @version
 */
public class TestArrayGrid extends BaseTestGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestArrayGrid.class);
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGet() {
        int size = 11;

        Grid grid = new ArrayGrid(size,size,size,0.001, 0.001);

        setGetAllVoxelCoords(grid);
    }
}
