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
 * Tests the functionality of the Intersect operation
 *
 * @author Alan Hudson
 * @version
 */
public class TestIntersect extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestIntersect.class);
    }

    /**
     * Test basic operation
     */
    public void testBasicAttribute() {
        int size1 = 10;
        int size2 = 10;
        byte state1 = Grid.INSIDE;
        byte state2 = Grid.INSIDE;
        long mat1 = 1;

        AttributeGrid grid1 = new ArrayAttributeGridByte(size1, size1, size1, 0.001, 0.001);
        AttributeGrid grid2 = new ArrayAttributeGridByte(size2, size2, size2, 0.002, 0.002);

        // set grid1
        for (int x=3; x<7; x++) {
            grid1.setData(x, 5, 5, state1, mat1);
        }

        // set grid2
        for (int x=2; x<8; x++) {
            grid2.setData(x, 5, 5, state2, mat1);
        }

        // get the intersection of grid1 and grid2
        Intersect op = new Intersect(grid1);
        AttributeGrid dest = (AttributeGrid) op.execute(grid2);

        assertEquals(size2, dest.getWidth());

        int expected_marked = grid1.findCount(Grid.VoxelClasses.INSIDE);

        assertEquals(expected_marked, dest.findCount(Grid.VoxelClasses.INSIDE));

        // check filled voxel state and material
        for (int x=0; x<size2; x++) {
            for (int y=0; y<size2; y++) {
                for (int z=0; z<size2; z++) {

                    if (x >=3 && x < 7 && y == 5 && z == 5) {
                        assertTrue("(" + x + ", " + y + ", " + z + ") state is not " + state2,
                                dest.getState(x, y, z) == Grid.INSIDE || dest.getState(x,y,z) == Grid.INSIDE);
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
    public void testBasicMT() {
        int size1 = 200;
        int size2 = 200;
        byte state1 = Grid.INSIDE;
        byte state2 = Grid.INSIDE;
        long mat1 = 1;

        AttributeGrid grid1 = new ArrayAttributeGridByte(size1, size1, size1, 0.001, 0.001);
        AttributeGrid grid2 = new ArrayAttributeGridByte(size2, size2, size2, 0.002, 0.002);

        // set grid1
        for (int x=3; x<7; x++) {
            grid1.setData(x, 5, 5, state1, mat1);
        }

        // set grid2
        for (int x=2; x<8; x++) {
            grid2.setData(x, 5, 5, state2, mat1);
        }

        // get the intersection of grid1 and grid2
        IntersectMT op = new IntersectMT(grid1, Runtime.getRuntime().availableProcessors());
        AttributeGrid dest = (AttributeGrid) op.execute(grid2);

        assertEquals(size2, dest.getWidth());

        int expected_marked = grid1.findCount(Grid.VoxelClasses.INSIDE);

        assertEquals(expected_marked, dest.findCount(Grid.VoxelClasses.INSIDE));

        // check filled voxel state and material
        for (int x=0; x<size2; x++) {
            for (int y=0; y<size2; y++) {
                for (int z=0; z<size2; z++) {

                    if (x >=3 && x < 7 && y == 5 && z == 5) {
                        assertTrue("(" + x + ", " + y + ", " + z + ") state is not " + state2,
                                dest.getState(x, y, z) == Grid.INSIDE || dest.getState(x,y,z) == Grid.INSIDE);
                    } else {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not outside",
                                Grid.OUTSIDE, dest.getState(x, y, z));
                    }
                }
            }
        }

    }

    /**
     * Test scaling of MT operations
     */
    public void testScalingMT() {
        int size1 = 200;
        int size2 = 200;
        byte state1 = Grid.INSIDE;
        byte state2 = Grid.INSIDE;
        long mat1 = 1;

        AttributeGrid grid1 = new ArrayAttributeGridByte(size1, size1, size1, 0.001, 0.001);
        AttributeGrid grid2 = new ArrayAttributeGridByte(size2, size2, size2, 0.002, 0.002);

        // set grid1
        for (int x=3; x<7; x++) {
            grid1.setData(x, 5, 5, state1, mat1);
        }

        // set grid2
        for (int x=2; x<8; x++) {
            grid2.setData(x, 5, 5, state2, mat1);
        }

        // get the intersection of grid1 and grid2
        IntersectMT op = new IntersectMT(grid1, Runtime.getRuntime().availableProcessors());
        AttributeGrid dest = (AttributeGrid) op.execute(grid2);

        assertEquals(size2, dest.getWidth());

        int expected_marked = grid1.findCount(Grid.VoxelClasses.INSIDE);

        assertEquals(expected_marked, dest.findCount(Grid.VoxelClasses.INSIDE));

        // check filled voxel state and material
        for (int x=0; x<size2; x++) {
            for (int y=0; y<size2; y++) {
                for (int z=0; z<size2; z++) {

                    if (x >=3 && x < 7 && y == 5 && z == 5) {
                        assertTrue("(" + x + ", " + y + ", " + z + ") state is not " + state2,
                                dest.getState(x, y, z) == Grid.INSIDE || dest.getState(x,y,z) == Grid.INSIDE);
                    } else {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not outside",
                                Grid.OUTSIDE, dest.getState(x, y, z));
                    }
                }
            }
        }

    }

    /**
     * Test that ST and MT yield the exact same result
     */
    public void testMTEquals() {
        int size1 = 10;
        int size2 = 15;
        byte state1 = Grid.INSIDE;
        byte state2 = Grid.INSIDE;

        Grid grid1 = new ArrayGridByte(size1, size1, size1, 0.001, 0.001);
        Grid grid2 = new ArrayGridByte(size2, size2, size2, 0.002, 0.002);
        Grid grid3 = new ArrayGridByte(size2, size2, size2, 0.002, 0.002);

        // set grid1
        for (int x=3; x<7; x++) {
            grid1.setState(x, 5, 5, state1);
        }

        // set grid2
        for (int y=5; y<size2; y++) {
            grid2.setState(1, y, 10, state2);
        }
        // set grid3
        for (int y=5; y<size2; y++) {
            grid3.setState(1, y, 10, state2);
        }

        // get the subtraction of grid1 from grid2
        SubtractMT op = new SubtractMT(grid1, Runtime.getRuntime().availableProcessors());
        Grid subtrGridMT = op.execute(grid2);

        Subtract op2 = new Subtract(grid1,0,0,0,1);
        Grid subtrGridST = op2.execute(grid2);

        assertEquals(size2, subtrGridST.getWidth());
        assertEquals(size2, subtrGridMT.getWidth());

        // check filled voxel state and material
        for (int x=0; x<size2; x++) {
            for (int y=0; y<size2; y++) {
                for (int z=0; z<size2; z++) {
                    assertEquals("(" + x + ", " + y + ", " + z + ") state is not equal",subtrGridST.getState(x,y,z),subtrGridMT.getState(x,y,z));
                }
            }
        }
    }

}
