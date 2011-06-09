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
import abfab3d.grid.op.RemoveMaterial;

/**
 * Tests the functionality of a OccupiedWrapper
 *
 * @author Alan Hudson
 * @version
 */
public class TestMaterialIndexedWrapper extends BaseTestGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestMaterialIndexedWrapper.class);
    }

    /**
     * Test removal operation
     */
    public void testRemoval() {
        int size = 250;
        int numMaterials = 10;
        int matWidth = 15;

        Grid grid = new ArrayGrid(size,size,size,0.001, 0.001);
        MaterialIndexedWrapper wrapper = new MaterialIndexedWrapper(grid);

        int idx;

        for(int i=0; i < numMaterials; i++) {
            for(int j=0; j < matWidth; j++) {
                setX(wrapper, matWidth * i + j,1,Grid.EXTERIOR, (byte) i);
            }
        }

        int matSize = grid.getWidth() * matWidth;

        int[] counts = new int[numMaterials];
        for(int i=0; i < numMaterials; i++) {
            counts[i] = wrapper.findCount((byte) i);

            assertEquals("Insert count wrong", matSize,counts[i]);
        }

        for(int i=0; i < numMaterials; i++) {
            RemoveMaterial rm = new RemoveMaterial((byte) i);
            rm.execute(wrapper);

            assertEquals("Material not removed", wrapper.findCount((byte) i), 0);

            for(int j=i+1; j < numMaterials; j++) {
                assertEquals("Other material removed", wrapper.findCount((byte) j), counts[j]);
            }
        }
    }

    /**
     * Test removal operation
     */
    public void testRemovalSpeed() {
        int size = 250;
        int numMaterials = 10;
        int matWidth = 15;
        int warmup = 20;
        int times = 10;
        long startTime;
        long time1;
        long time2;


        Grid grid = new ArrayGrid(size,size,size,0.001, 0.001);
        MaterialIndexedWrapper wrapper = new MaterialIndexedWrapper(grid);
        int matSize = grid.getWidth() * matWidth;

        // Warmup method1
        for(int n=0; n < warmup; n++) {

            for(int i=0; i < numMaterials; i++) {
                for(int j=0; j < matWidth; j++) {
                    setX(wrapper, matWidth * i + j,1,Grid.EXTERIOR, (byte) i);
                }
            }

            for(int i=0; i < numMaterials; i++) {
                RemoveMaterial rm = new RemoveMaterial((byte) i);
                rm.execute(wrapper);
            }
        }

        Grid grid2 = new ArrayGrid(size,size,size,0.001, 0.001);

        // Warmup method2
        for(int n=0; n < warmup; n++) {

            for(int i=0; i < numMaterials; i++) {
                for(int j=0; j < matWidth; j++) {
                    setX(grid2, matWidth * i + j,1,Grid.EXTERIOR, (byte) i);
                }
            }

            for(int i=0; i < numMaterials; i++) {
                RemoveMaterial rm = new RemoveMaterial((byte) i);
                rm.execute(grid2);
            }
        }

        // Run wrapper code

        startTime = System.nanoTime();

        for(int n=0; n < times; n++) {
            // TODO: Can we remove this timing, does it matter?
            for(int i=0; i < numMaterials; i++) {
                for(int j=0; j < matWidth; j++) {
                    setX(wrapper, matWidth * i + j,1,Grid.EXTERIOR, (byte) i);
                }
            }

            for(int i=0; i < numMaterials; i++) {
                RemoveMaterial rm = new RemoveMaterial((byte) i);
                rm.execute(wrapper);
            }
        }

        time1 = System.nanoTime() - startTime;

        // Run direct code

        startTime = System.nanoTime();

        for(int n=0; n < times; n++) {
            // TODO: Can we remove this timing, does it matter?
            for(int i=0; i < numMaterials; i++) {
                for(int j=0; j < matWidth; j++) {
                    setX(grid2, matWidth * i + j,1,Grid.EXTERIOR, (byte) i);
                }
            }

            for(int i=0; i < numMaterials; i++) {
                RemoveMaterial rm = new RemoveMaterial((byte) i);
                rm.execute(grid2);
            }
        }

        time2 = System.nanoTime() - startTime;

        // Time trials show this method is 30X faster.  Error out
        // if its ever < 10X faster

        assertTrue("Wrapper method too slow", time1 * 10 < time2);
        System.out.println("Wrapper: " + time1);
        System.out.println(" Direct: " + time2 + " " + (time2 / time1) + "X");
    }

    /**
     * Test that promised operations are faster.
     */
    public void testSpeed() {
    }

    /**
     * Set all the X values of a grid.
     *
     * @param state The new state
     * @param mat The new material
     */
    protected static void setX(Grid grid, int y, int z, byte state, byte mat) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        for(int x=0; x < width; x++) {
            grid.setData(x,y,z, state, mat);
        }
    }
}
