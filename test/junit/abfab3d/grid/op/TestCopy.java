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

import abfab3d.grid.*;
import junit.framework.Test;
import junit.framework.TestSuite;

// Internal Imports


/**
 * Tests the functionality of the Copy Operation
 *
 * @author Alan Hudson
 * @version
 */
public class TestCopy extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestCopy.class);
    }

    /**
     * Test basic operation
     */
    public void testBasic() {
        int size1 = 10;
        int size2 = 15;
        byte state1 = Grid.INSIDE;

        Grid src = new ArrayGridByte(size1, size1, size1, 0.001, 0.001);
        Grid dest = new ArrayGridByte(size2, size2, size2, 0.002, 0.002);

        // set grid1
        for (int x=3; x<7; x++) {
            src.setState(x, 5, 5, state1);
        }

        Copy op = new Copy(src, 0, 0, 0);
        Grid copyGrid = op.execute(dest);

        assertEquals(size2, copyGrid.getWidth());

        int expectedMat1 = 0;
        int expectedMat2 = size2 - 5;

        // check filled voxel state and material
        for (int x=0; x<size2; x++) {
            for (int y=0; y<size2; y++) {
                for (int z=0; z<size2; z++) {

                    if (x >= 3 && x < 7 && y == 5 && z == 5) {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not " + state1,
                                state1, dest.getState(x, y, z));
/*
                        assertEquals("(" + x + ", " + y + ", " + z + ")) material is not " + mat2,
                                mat2, dest.getAttribute(x, y, z));
*/
                    } else {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not outside",
                                Grid.OUTSIDE, dest.getState(x, y, z));
                    }
                }
            }
        }

    }

    /**
     * Test basic operation
     */
    public void testBasicAttribute() {
        int size1 = 10;
        int size2 = 15;
        byte state1 = Grid.INSIDE;
        long mat1 = 42;

        AttributeGrid src = new ArrayAttributeGridByte(size1, size1, size1, 0.001, 0.001);
        AttributeGrid dest = new ArrayAttributeGridByte(size2, size2, size2, 0.002, 0.002);

        // set grid1
        for (int x=3; x<7; x++) {
            src.setData(x, 5, 5, state1,mat1);
        }

        Copy op = new Copy(src, 0, 0, 0);
        AttributeGrid copyGrid = op.execute(dest);

        assertEquals(size2, copyGrid.getWidth());

        // check filled voxel state and material
        for (int x=0; x<size2; x++) {
            for (int y=0; y<size2; y++) {
                for (int z=0; z<size2; z++) {

                    if (x >= 3 && x < 7 && y == 5 && z == 5) {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not " + state1,
                                state1, dest.getState(x, y, z));

                        assertEquals("(" + x + ", " + y + ", " + z + ")) material is not " + mat1,
                                mat1, dest.getAttribute(x, y, z));
                    } else {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not outside",
                                Grid.OUTSIDE, dest.getState(x, y, z));
                    }
                }
            }
        }
    }
}
