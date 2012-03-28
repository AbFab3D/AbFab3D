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
import junit.framework.TestSuite;

// Internal Imports

/**
 * Tests the functionality of a OccupiedWrapper
 *
 * @author Alan Hudson
 * @version
 */
public class TestOccupiedWrapper extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestOccupiedWrapper.class);
    }

    /**
     * Test setGrid.
     */
    public void testSetGrid() {
        int size1 = 11;
        int size2 = 18;

        AttributeGrid grid1 = new ArrayAttributeGridByte(size1, size1, size1, 0.001, 0.001);
        AttributeGrid grid2 = new ArrayAttributeGridByte(size2, size2, size2, 0.001, 0.001);

        OccupiedWrapper wrapper = new OccupiedWrapper(grid1);
        assertEquals("Grid width is not " + size1, size1, wrapper.getWidth());

        wrapper.setGrid(grid2);
        assertEquals("Grid width is not " + size2, size2, wrapper.getWidth());
    }

    /**
     * Test setting of an already-occupied voxel.
     */
    public void testDoubleSetByVoxelCoords() {
        int size = 11;

        AttributeGrid grid = new ArrayAttributeGridByte(size, size, size, 0.001, 0.001);
        OccupiedWrapper wrapper = new OccupiedWrapper(grid);

        wrapper.setData(0,0,0, Grid.EXTERIOR, (byte) 1);

        try {
            wrapper.setData(0,0,0, Grid.INTERIOR, (byte) 2);
            fail("Double setting a voxel did not throw exception");
        } catch(IllegalArgumentException iae) {
            assertEquals("Double setting should not change data", Grid.EXTERIOR, wrapper.getState(0, 0, 0));
        }
    }

    /**
     * Test setting of an already-occupied voxel.
     */
    public void testDoubleSetByWorldCoords() {
        byte mat1 = (byte) 1;
        byte mat2 = (byte) 2;

        AttributeGrid grid = new ArrayAttributeGridByte(0.52, 0.41, 0.52, 0.005, 0.002);
        OccupiedWrapper wrapper = new OccupiedWrapper(grid);

        wrapper.setData(0.022, 0.031, 0.01, Grid.EXTERIOR, mat1);

        try {
            wrapper.setData(0.02, 0.030, 0.01, Grid.EXTERIOR, mat2);
            fail("Double setting a voxel did not throw exception");
        } catch(IllegalArgumentException iae) {
            assertEquals("Double setting should not change data", mat1, wrapper.getAttribute(0.02, 0.031, 0.01));
        }

        try {
            wrapper.setData(0.0249, 0.0319, 0.0149, Grid.EXTERIOR, mat2);
            fail("Double setting a voxel did not throw exception");
        } catch(IllegalArgumentException iae) {
            assertEquals("Double setting should not change data", mat1, wrapper.getAttribute(0.0249, 0.0319, 0.0149));
        }
    }
}
