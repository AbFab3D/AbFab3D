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
public class TestSubtract extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestSubtract.class);
    }

    /**
     * Test basic operation
     */
    public void testBasicAttribute() {
        int size1 = 10;
        int size2 = 15;
        byte state1 = Grid.INTERIOR;
        byte state2 = Grid.EXTERIOR;
        int mat1 = 1;
        int mat2 = 2;

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

        // get the subtraction of grid1 from grid2
        Subtract op = new Subtract(grid1, 0, 0, 0, 0);
        AttributeGrid subtrGrid = (AttributeGrid) op.execute(grid2);

        assertEquals(size2, subtrGrid.getWidth());

        int expectedMat1 = 0;
        int expectedMat2 = size2 - 5;

        assertEquals(expectedMat1, subtrGrid.findCount(mat1));
        assertEquals(expectedMat2, subtrGrid.findCount(mat2));

        // check filled voxel state and material
        for (int x=0; x<size2; x++) {
            for (int y=0; y<size2; y++) {
                for (int z=0; z<size2; z++) {

                    if (x == 1 && y >= 5 && y < size2 && z == 10) {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not " + state2,
                                state2, subtrGrid.getState(x, y, z));
                        assertEquals("(" + x + ", " + y + ", " + z + ")) material is not " + mat2,
                                mat2, subtrGrid.getAttribute(x, y, z));
                    } else {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not outside",
                                Grid.OUTSIDE, subtrGrid.getState(x, y, z));
                    }
                }
            }
        }

    }

    /**
     * Grids sharing a voxel coordinate should have those voxels subtracted
     */
    public void testSharedVoxelAttribute() {
        int size1 = 10;
        int size2 = 15;
        byte state1 = Grid.INTERIOR;
        byte state2 = Grid.EXTERIOR;
        int mat1 = 1;
        int mat2 = 2;

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

        // get the subtration of grid1 from grid2
        Subtract op = new Subtract(grid1, 0, 0, 0, 0);
        AttributeGrid subtrGrid = (AttributeGrid) op.execute(grid2);

        assertEquals(size2, subtrGrid.getWidth());

        int expectedMat1 = 0;
        int expectedMat2 = size2 - 5 - 1;

        assertEquals(expectedMat1, subtrGrid.findCount(mat1));
        assertEquals(expectedMat2, subtrGrid.findCount(mat2));

        // check filled voxel state and material
        for (int x=0; x<size2; x++) {
            for (int y=0; y<size2; y++) {
                for (int z=0; z<size2; z++) {

                    if (x == 3 && y >= 6 && y < size2 && z == 5) {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not " + state2,
                                state2, subtrGrid.getState(x, y, z));
                        assertEquals("(" + x + ", " + y + ", " + z + ")) material is not " + mat2,
                                mat2, subtrGrid.getAttribute(x, y, z));
                    } else {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not outside",
                                Grid.OUTSIDE, subtrGrid.getState(x, y, z));
                    }
                }
            }
        }
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

        // get the subtraction of grid1 from grid2
        Subtract op = new Subtract(grid1, 0, 0, 0, 0);
        Grid subtrGrid = (Grid) op.execute(grid2);

        assertEquals(size2, subtrGrid.getWidth());

        // check filled voxel state and material
        for (int x=0; x<size2; x++) {
            for (int y=0; y<size2; y++) {
                for (int z=0; z<size2; z++) {

                    if (x == 1 && y >= 5 && y < size2 && z == 10) {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not " + state2,
                                state2, subtrGrid.getState(x, y, z));
                    } else {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not outside",
                                Grid.OUTSIDE, subtrGrid.getState(x, y, z));
                    }
                }
            }
        }

    }

    /**
     * Grids sharing a voxel coordinate should have those voxels subtracted
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

        // get the subtration of grid1 from grid2
        Subtract op = new Subtract(grid1, 0, 0, 0, 0);
        Grid subtrGrid = (Grid) op.execute(grid2);

        assertEquals(size2, subtrGrid.getWidth());

        // check filled voxel state and material
        for (int x=0; x<size2; x++) {
            for (int y=0; y<size2; y++) {
                for (int z=0; z<size2; z++) {

                    if (x == 3 && y >= 6 && y < size2 && z == 5) {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not " + state2,
                                state2, subtrGrid.getState(x, y, z));
                    } else {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not outside",
                                Grid.OUTSIDE, subtrGrid.getState(x, y, z));
                    }
                }
            }
        }
    }

    /**
     * Test basic operation
     */
    public void testBasicMT() {
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

        // get the subtraction of grid1 from grid2
        SubtractMT op = new SubtractMT(grid1, Runtime.getRuntime().availableProcessors());
        Grid subtrGrid = op.execute(grid2);

        assertEquals(size2, subtrGrid.getWidth());

        // check filled voxel state and material
        for (int x=0; x<size2; x++) {
            for (int y=0; y<size2; y++) {
                for (int z=0; z<size2; z++) {

                    if (x == 1 && y >= 5 && y < size2 && z == 10) {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not " + state2,
                                state2, subtrGrid.getState(x, y, z));
                    } else {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not outside",
                                Grid.OUTSIDE, subtrGrid.getState(x, y, z));
                    }
                }
            }
        }

    }

    /**
     * Grids sharing a voxel coordinate should have those voxels subtracted
     */
    public void testSharedVoxelMT() {
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

        // get the subtration of grid1 from grid2
        SubtractMT op = new SubtractMT(grid1, Runtime.getRuntime().availableProcessors());
        Grid subtrGrid = (Grid) op.execute(grid2);

        assertEquals(size2, subtrGrid.getWidth());

        // check filled voxel state and material
        for (int x=0; x<size2; x++) {
            for (int y=0; y<size2; y++) {
                for (int z=0; z<size2; z++) {

                    if (x == 3 && y >= 6 && y < size2 && z == 5) {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not " + state2,
                                state2, subtrGrid.getState(x, y, z));
                    } else {
                        assertEquals("(" + x + ", " + y + ", " + z + ") state is not outside",
                                Grid.OUTSIDE, subtrGrid.getState(x, y, z));
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
        byte state1 = Grid.INTERIOR;
        byte state2 = Grid.EXTERIOR;

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

    /**
     * Test that multithreaded performance improves as you add threads.
     */
    public void testPerformanceMT() {
        int size1 = 500;
        int size2 = 550;
        byte state1 = Grid.INTERIOR;
        byte state2 = Grid.EXTERIOR;

        int warmup = 4;
        int cores = 4;
        long[] times = new long[cores];


        for(int n=0; n < warmup; n++) {
            Grid grid1 = new ArrayGridByte(size1, size1, size1, 0.001, 0.001);
            Grid grid2 = new ArrayGridByte(size2, size2, size2, 0.002, 0.002);

            // set grid1
            for (int x=(int)(size1*0.3); x<(int)(size1*0.7); x++) {
                grid1.setState(x, 5, 5, state1);
            }

            // set grid2
            for (int y=(int)(size2*0.5); y<size2; y++) {
                grid2.setState(3, y, 5, state2);
            }

            long t0 = System.currentTimeMillis();
            // get the subtraction of grid1 from grid2
            SubtractMT op = new SubtractMT(grid1, (n+1) % cores);
            Grid subtrGrid = (Grid) op.execute(grid2);
            System.out.println("Time: " + (System.currentTimeMillis() - t0));
        }

        for(int i=0; i < cores; i++) {
            Grid grid1 = new ArrayGridByte(size1, size1, size1, 0.001, 0.001);
            Grid grid2 = new ArrayGridByte(size2, size2, size2, 0.002, 0.002);

            // set grid1
            for (int x=(int)(size1*0.3); x<(int)(size1*0.7); x++) {
                grid1.setState(x, 5, 5, state1);
            }

            // set grid2
            for (int y=(int)(size2*0.5); y<size2; y++) {
                grid2.setState(3, y, 5, state2);
            }

            long t0 = System.currentTimeMillis();
            // get the subtraction of grid1 from grid2
            SubtractMT op = new SubtractMT(grid1, i+1);
            Grid subtrGrid = (Grid) op.execute(grid2);
            times[i] = (System.currentTimeMillis() - t0);
            System.out.println("Time: " + (System.currentTimeMillis() - t0));
        }
        System.out.println("Times: " + java.util.Arrays.toString(times));
        // Spot check, make sure 4 threads is better then 1 thread by at least 2X
        assertTrue("Speed check",times[3] < (times[0] * 2));
    }

}
