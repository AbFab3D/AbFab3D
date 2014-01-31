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
import static abfab3d.util.Output.printf;


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
        byte state1 = Grid.INSIDE;
        byte state2 = Grid.INSIDE;
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

        // get the subtraction of grid1 from grid2
        SubtractOp op = new SubtractOp(grid1, 0, 0, 0, 0);
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
        byte state1 = Grid.INSIDE;
        byte state2 = Grid.INSIDE;
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

        // get the subtration of grid1 from grid2
        SubtractOp op = new SubtractOp(grid1, 0, 0, 0, 0);
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
        byte state1 = Grid.INSIDE;
        byte state2 = Grid.INSIDE;

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
        SubtractOp op = new SubtractOp(grid1, 0, 0, 0, 0);
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
        byte state1 = Grid.INSIDE;
        byte state2 = Grid.INSIDE;

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
        SubtractOp op = new SubtractOp(grid1, 0, 0, 0, 0);
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
     * Test that MT is faster then ST.  Assumes we always run on a MT box
     */
    public void _testMTFaster() {
        // TODO:  this test does not always works so removing for now
        int size1 = 800;
        int size2 = 800;
        byte state1 = Grid.INSIDE;
        byte state2 = Grid.INSIDE;

        Grid grid1 = new ArrayGridByte(size1, size1, size1, 0.001, 0.001);
        Grid grid2 = new ArrayGridByte(size2, size2, size2, 0.002, 0.002);
        Grid grid3 = new ArrayGridByte(size2, size2, size2, 0.002, 0.002);

        for(int y=0; y < grid1.getHeight(); y++) {
            for(int x=0; x < grid1.getWidth(); x++) {
                for(int z=0; z < grid1.getDepth(); z++) {
                    grid1.setState(x,y,z,state1);
                }
            }
        }
        for(int y=0; y < grid2.getHeight(); y++) {
            for(int x=0; x < grid2.getWidth(); x++) {
                for(int z=0; z < grid2.getDepth(); z++) {
                    grid2.setState(x,y,z,state1);
                }
            }
        }

        int WARMUP = 3;

        long st_time = 0;
        for(int i=0; i < WARMUP; i++) {
            long t0 = System.currentTimeMillis();
            // get the subtraction of grid1 from grid2
            SubtractOpMT op = new SubtractOpMT(grid1, Runtime.getRuntime().availableProcessors());
            Grid subtrGridMT = op.execute(grid2);

            long mt_time = System.currentTimeMillis() - t0;
            t0 = System.currentTimeMillis();
            SubtractOp op2 = new SubtractOp(grid1,0,0,0,1);
            Grid subtrGridST = op2.execute(grid2);
            st_time = System.currentTimeMillis() - t0;

            //printf("MT time: %6d  ST time: %6d  SpeedUp: %6.2f\n",mt_time,st_time,(float)st_time / mt_time);
        }

        int TIMES = 1;

        int cores = Runtime.getRuntime().availableProcessors();
        cores = Math.min(cores,16); // We expect linear scaling to stop by this time
        float expected_speedup = 0.5f * cores;

        for(int i=0; i < TIMES; i++) {
            long t0 = System.currentTimeMillis();
            // get the subtraction of grid1 from grid2
            SubtractOpMT op = new SubtractOpMT(grid1, Runtime.getRuntime().availableProcessors());
            op.setThreadCount(cores);
            op.setSliceSize(2);
            Grid subtrGridMT = op.execute(grid2);
            if (subtrGridMT.getWidth() > 10000) { System.out.println("no optimize away"); };

            long mt_time = System.currentTimeMillis() - t0;

            float speedup = (float)st_time / mt_time;
            printf("MT time: %6d  ST time: %6d  SpeedUp: %6.2f\n",mt_time,st_time,speedup);

            assertTrue("Speedup factor > " + expected_speedup, speedup >= expected_speedup);
        }
    }


    /**
     * Test basic operation
     */
    public void _testBasicMT() {
        int size1 = 10;
        int size2 = 15;
        byte state1 = Grid.INSIDE;
        byte state2 = Grid.INSIDE;

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
        SubtractOpMT op = new SubtractOpMT(grid1, Runtime.getRuntime().availableProcessors());
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
    public void _testSharedVoxelMT() {
        int size1 = 10;
        int size2 = 15;
        byte state1 = Grid.INSIDE;
        byte state2 = Grid.INSIDE;

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
        SubtractOpMT op = new SubtractOpMT(grid1, Runtime.getRuntime().availableProcessors());
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
        SubtractOpMT op = new SubtractOpMT(grid1, Runtime.getRuntime().availableProcessors());
        Grid subtrGridMT = op.execute(grid2);

        SubtractOp op2 = new SubtractOp(grid1,0,0,0,1);
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
