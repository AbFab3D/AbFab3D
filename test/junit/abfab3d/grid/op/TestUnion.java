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
 * Tests the functionality of the SetDifference Operation
 *
 * @author Tony Wong
 * @version
 */
public class TestUnion extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestUnion.class);
    }

    /**
     * Test basic operation
     */
    public void testBasic() {
        int size1 = 10;
        int size2 = 15;
        byte state1 = Grid.INTERIOR;
        byte state2 = Grid.EXTERIOR;

        Grid grid1 = new ArrayGridByte(size1, size1, size1, 0.001, 0.001);
        Grid grid2 = new ArrayGridByte(size2, size2, size2, 0.002, 0.002);

        // set grid1
        for (int x=3; x<7; x++) {
            grid1.setState(x, 5, 5, state1);
        }

        // set grid2
        for (int y=5; y<size2; y++) {
            grid2.setState(1, y, 10, state2);
        }

        // get the union of grid1 and grid2
        Union u = new Union(grid1, 0, 0, 0, 0);
        Grid unionGrid = u.execute(grid2);

        assertEquals(size2, unionGrid.getWidth());

        // check filled voxel state and material
        for (int x=0; x<size2; x++) {
            for (int y=0; y<size2; y++) {
                for (int z=0; z<size2; z++) {

                    if (x >= 3 && x < 7 && y == 5 && z == 5) {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not " + state1,
                                state1, unionGrid.getState(x, y, z));
                    } else if (x == 1 && y >= 5 && y < size2 && z == 10) {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not " + state2,
                                state2, unionGrid.getState(x, y, z));
                    } else {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not outside",
                                Grid.OUTSIDE, unionGrid.getState(x, y, z));
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
        byte state1 = Grid.INTERIOR;
        byte state2 = Grid.EXTERIOR;
        long mat1 = 1;
        long mat2 = 2;

        AttributeGrid grid1 = new ArrayAttributeGridByte(size1, size1, size1, 0.001, 0.001);
        AttributeGrid grid2 = new ArrayAttributeGridByte(size2, size2, size2, 0.002, 0.002);

        // set grid1
        for (int x=3; x<7; x++) {
            grid1.setData(x, 5, 5, state1, mat1);
        }

        // set grid2
        for (int y=5; y<size2; y++) {
            grid2.setData(1, y, 10, state2, mat2);
        }

        // get the union of grid1 and grid2
        Union u = new Union(grid1, 0, 0, 0, 0);
        AttributeGrid unionGrid = u.execute(grid2);

        assertEquals(size2, unionGrid.getWidth());

        int expectedMat1 = 7 - 3;
        int expectedMat2 = size2 - 5;

        assertEquals(expectedMat1, unionGrid.findCount(mat1));
        assertEquals(expectedMat2, unionGrid.findCount(mat2));

        // check filled voxel state and material
        for (int x=0; x<size2; x++) {
            for (int y=0; y<size2; y++) {
                for (int z=0; z<size2; z++) {

                    if (x >= 3 && x < 7 && y == 5 && z == 5) {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not " + state1,
                                state1, unionGrid.getState(x, y, z));
                        assertEquals("(" + x + ", " + y + ", " + z + ")) material is not " + mat1,
                                mat1, unionGrid.getAttribute(x, y, z));
                    } else if (x == 1 && y >= 5 && y < size2 && z == 10) {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not " + state2,
                                state2, unionGrid.getState(x, y, z));
                        assertEquals("(" + x + ", " + y + ", " + z + ")) material is not " + mat2,
                                mat2, unionGrid.getAttribute(x, y, z));
                    } else {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not outside",
                                Grid.OUTSIDE, unionGrid.getState(x, y, z));
                    }
                }
            }
        }

    }

    /**
     * Grids sharing a voxel coordinate should union to the state and material
     * of gridB
     */
    public void testSharedVoxelAttribute() {
        int size1 = 10;
        int size2 = 15;
        byte state1 = Grid.INTERIOR;
        byte state2 = Grid.EXTERIOR;
        long mat1 = 1;
        long mat2 = 2;

        AttributeGrid grid1 = new ArrayAttributeGridByte(size1, size1, size1, 0.001, 0.001);
        AttributeGrid grid2 = new ArrayAttributeGridByte(size2, size2, size2, 0.002, 0.002);

        // set grid1
        for (int x=3; x<7; x++) {
            grid1.setData(x, 5, 5, state1, mat1);
        }

        // set grid2
        for (int y=5; y<size2; y++) {
            grid2.setData(3, y, 5, state2, mat2);
        }

        // get the union of grid1 and grid2
        Union u = new Union(grid1, 0, 0, 0, 0);
        AttributeGrid unionGrid = u.execute(grid2);

        assertEquals(size2, unionGrid.getWidth());

        int expectedMat1 = 7 - 3;
        int expectedMat2 = size2 - 5 - 1;

        assertEquals(expectedMat1, unionGrid.findCount(mat1));
        assertEquals(expectedMat2, unionGrid.findCount(mat2));

        // check filled voxel state and material
        for (int x=0; x<size2; x++) {
            for (int y=0; y<size2; y++) {
                for (int z=0; z<size2; z++) {

                    if (x >= 3 && x < 7 && y == 5 && z == 5) {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not " + state1,
                                state1, unionGrid.getState(x, y, z));
                        assertEquals("(" + x + ", " + y + ", " + z + ")) material is not " + mat1,
                                mat1, unionGrid.getAttribute(x, y, z));
                    } else if (x == 3 && y >= 5 && y < size2 && z == 5) {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not " + state2,
                                state2, unionGrid.getState(x, y, z));
                        assertEquals("(" + x + ", " + y + ", " + z + ")) material is not " + mat2,
                                mat2, unionGrid.getAttribute(x, y, z));
                    } else {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not outside",
                                Grid.OUTSIDE, unionGrid.getState(x, y, z));
                    }
                }
            }
        }

    }

    /**
     * Grids sharing a voxel coordinate should union to the state and material
     * of gridB
     */
    public void testSharedVoxel() {
        int size1 = 10;
        int size2 = 15;
        byte state1 = Grid.INTERIOR;
        byte state2 = Grid.EXTERIOR;

        Grid grid1 = new ArrayGridByte(size1, size1, size1, 0.001, 0.001);
        Grid grid2 = new ArrayGridByte(size2, size2, size2, 0.002, 0.002);

        // set grid1
        for (int x=3; x<7; x++) {
            grid1.setState(x, 5, 5, state1);
        }

        // set grid2
        for (int y=5; y<size2; y++) {
            grid2.setState(3, y, 5, state2);
        }

        // get the union of grid1 and grid2
        Union u = new Union(grid1, 0, 0, 0, 0);
        Grid unionGrid = u.execute(grid2);

        assertEquals(size2, unionGrid.getWidth());

        // check filled voxel state and material
        for (int x=0; x<size2; x++) {
            for (int y=0; y<size2; y++) {
                for (int z=0; z<size2; z++) {

                    if (x >= 3 && x < 7 && y == 5 && z == 5) {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not " + state1,
                                state1, unionGrid.getState(x, y, z));
                    } else if (x == 3 && y >= 5 && y < size2 && z == 5) {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not " + state2,
                                state2, unionGrid.getState(x, y, z));
                    } else {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not outside",
                                Grid.OUTSIDE, unionGrid.getState(x, y, z));
                    }
                }
            }
        }

    }

}
