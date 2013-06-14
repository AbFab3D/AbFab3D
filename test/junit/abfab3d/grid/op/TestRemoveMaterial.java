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

package abfab3d.grid.op;

// External Imports
import junit.framework.Test;
import junit.framework.TestSuite;

// Internal Imports
import abfab3d.grid.*;

/**
 * Tests the functionality of RemoveMaterial Operation
 *
 * @author Alan Hudson
 * @version
 */
public class TestRemoveMaterial extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestRemoveMaterial.class);
    }

    /**
     * Test basic operation
     */
    public void testBasic() {
        int size = 11;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        // Add Object 1
        long mat1_count = 5;

        grid.setData(0,0,0, Grid.INSIDE, 1);
        grid.setData(0,0,1, Grid.INSIDE, 1);
        grid.setData(0,0,2, Grid.INSIDE, 1);
        grid.setData(0,1,0, Grid.INSIDE, 1);
        grid.setData(0,1,1, Grid.INSIDE, 1);

        assertEquals("Material1 count wrong on insert",
            grid.findCount(1),mat1_count);

        // Add Object 2
        long mat2_count = 6;

        grid.setData(6,5,0, Grid.INSIDE, 2);
        grid.setData(6,5,1, Grid.INSIDE, 2);
        grid.setData(6,5,2, Grid.INSIDE, 2);
        grid.setData(6,6,0, Grid.INSIDE, 2);
        grid.setData(6,6,1, Grid.INSIDE, 2);
        grid.setData(6,6,2, Grid.INSIDE, 2);

        assertEquals("Material2 count wrong after insert2",
            grid.findCount(2),mat2_count);

        assertEquals("Material1 count wrong after insert2",
            grid.findCount(1),mat1_count);

        AttributeOperation op = new RemoveMaterial(1);
        op.execute(grid);

        assertEquals("Material1 count wrong after removal",
            grid.findCount(1),0);

        assertEquals("Material2 count wrong after removal",
            grid.findCount(2),mat2_count);

        op = new RemoveMaterial(2);
        op.execute(grid);

        assertEquals("Material2 count wrong after removal",
            grid.findCount(2),0);
    }
}
