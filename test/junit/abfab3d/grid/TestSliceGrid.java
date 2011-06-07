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
 * Tests the functionality of a SliceGrid.
 *
 * @author Alan Hudson
 * @version
 */
public class TestSliceGrid extends BaseTestGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestSliceGrid.class);
    }

    /**
     * Test set/get all data points for Array form.
     */
    public void testSetGetArray() {
        int size = 11;

        Grid grid = new SliceGrid(size,size,size,0.001, 0.001, true);

        setGetAllVoxelCoords(grid);
    }

    /**
     * Test set/get all data points for Map Form.
     */
    public void testSetGetMap() {
        int size = 11;

        Grid grid = new SliceGrid(size,size,size,0.001, 0.001, false);

        setGetAllVoxelCoords(grid);
    }
}
