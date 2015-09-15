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
import abfab3d.util.Bounds;
import junit.framework.Test;
import junit.framework.TestSuite;

// Internal Imports


/**
 * Tests the functionality of a OccupiedWrapper
 *
 * @author Alan Hudson
 * @version
 */
public class TestMaterialIndexedWrapper extends BaseTestAttributeGrid implements ClassAttributeTraverser {
    /** The current material */
    private byte currMaterial;

    /** The material count */
    private long matCount;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestMaterialIndexedWrapper.class);
    }


    /**
     * Test the constructors and the grid size.
     */
    public void testMaterialIndexWrapper() {
        AttributeGrid grid = new ArrayAttributeGridByte(1, 1, 1, 0.001, 0.001);
        MaterialIndexedWrapper wrapper = new MaterialIndexedWrapper(grid);

        assertEquals("Array size is not 1", 1, wrapper.getWidth()*wrapper.getHeight()*wrapper.getDepth());

        grid = new ArrayAttributeGridByte(100, 101, 102, 0.001, 0.001);
        wrapper = new MaterialIndexedWrapper(grid);
        assertEquals("Array size is not 1030200", 1030200, wrapper.getWidth()*wrapper.getHeight()*wrapper.getDepth());

        grid = new ArrayAttributeGridByte(new Bounds(1.0, 1.0, 1.0), 0.2, 0.1);
        wrapper = new MaterialIndexedWrapper(grid);
        assertEquals("Array size is not 250", 250, wrapper.getWidth()*wrapper.getHeight()*wrapper.getDepth());

        grid = new ArrayAttributeGridByte(new Bounds(1.1, 1.1, 1.1), 0.2, 0.1);
        wrapper = new MaterialIndexedWrapper(grid);
        assertEquals("Array size is not 396", 396, wrapper.getWidth()*wrapper.getHeight()*wrapper.getDepth());

        // pass a MaterialIndexedWrapper into a new MaterialIndexedWrapper
        MaterialIndexedWrapper wrapper2 = new MaterialIndexedWrapper(wrapper);
        assertEquals("Array size is not 396", 396, wrapper2.getWidth()*wrapper2.getHeight()*wrapper2.getDepth());
    }

    /**
     * Test removal operation
     */
    public void testRemoval() {
        int size = 250;
        int numMaterials = 10;
        int matWidth = 15;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);
        MaterialIndexedWrapper wrapper = new MaterialIndexedWrapper(grid);

        int idx;

        for(int i=0; i < numMaterials; i++) {
            for(int j=0; j < matWidth; j++) {
                setX(wrapper, matWidth * i + j,1,Grid.INSIDE, (byte) (i+1));
            }
        }

        long matSize = grid.getWidth() * matWidth;

        int[] counts = new int[numMaterials];
        for(int i=0; i < numMaterials; i++) {
            counts[i] = wrapper.findCount((byte) (i+1));

            assertEquals("Insert count wrong", matSize,counts[i]);
        }

        for(int i=0; i < numMaterials; i++) {
            wrapper.removeAttribute(i+1);

            assertEquals("Material not removed", wrapper.findCount((byte) (i+1)), 0);

            for(int j=i+1; j < numMaterials; j++) {
                assertEquals("Other material removed", wrapper.findCount((byte) j+1), counts[j]);
            }
        }
    }

    /**
     * Test removal operation
     */
    public void testRemovalSpeed() {
        int size = 160;
        int numMaterials = 10;
        int matWidth = 15;
        int warmup = 10;
        int times = 10;

        long startTime;
        long time1;
        long time2;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);
        MaterialIndexedWrapper wrapper = new MaterialIndexedWrapper(grid);
        int matSize = grid.getWidth() * matWidth;

        // Warmup method1
        for(int n=0; n < warmup; n++) {

            for(int i=0; i < numMaterials; i++) {
                for(int j=0; j < matWidth; j++) {
                    setX(wrapper, matWidth * i + j,1,Grid.INSIDE, (byte) i);
                }
            }

            for(int i=0; i < numMaterials; i++) {
                wrapper.removeAttribute(i);
            }
        }

        AttributeGrid grid2 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        // Warmup method2
        for(int n=0; n < warmup; n++) {

            for(int i=0; i < numMaterials; i++) {
                for(int j=0; j < matWidth; j++) {
                    setX(grid2, matWidth * i + j,1,Grid.INSIDE, (byte) i);
                }
            }

            for(int i=0; i < numMaterials; i++) {
                grid2.removeAttribute(i);
            }
        }

        // Run wrapper code

        long total_time = 0;
        long start_time;


        for(int n=0; n < times; n++) {
            for(int i=0; i < numMaterials; i++) {
                for(int j=0; j < matWidth; j++) {
                    setX(wrapper, matWidth * i + j,1,Grid.INSIDE, (byte) i);
                }
            }

            start_time = System.nanoTime();
            for(int i=0; i < numMaterials; i++) {
                wrapper.removeAttribute(i);
            }
            total_time += (System.nanoTime() - start_time);
        }

        time1 = total_time;

        // Run direct code

        System.out.println("Remove attribute speed");
        total_time = 0;

        for(int n=0; n < times; n++) {
            for(int i=0; i < numMaterials; i++) {
                for(int j=0; j < matWidth; j++) {
                    setX(grid2, matWidth * i + j,1,Grid.INSIDE, (byte) i);
                }
            }

            start_time = System.nanoTime();
            for(int i=0; i < numMaterials; i++) {
                grid2.removeAttribute(i);
            }
            total_time += (System.nanoTime() - start_time);
        }

        time2 = total_time;

        // Time trials show this method is 30X faster.  Error out
        // if its ever < 10X faster

        System.out.println("Wrapper: " + time1);
        System.out.println(" Direct: " + time2 + " " + (time2 / time1) + "X\n");
        assertTrue("Wrapper method too slow", time1 * 10 < time2);
    }

    /**
     * Test that traversal methods work
     */
    public void testMaterialTraversal() {
        int size = 250;
        int numMaterials = 10;
        int matWidth = 15;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);
        MaterialIndexedWrapper wrapper = new MaterialIndexedWrapper(grid);

        for(int i=0; i < numMaterials; i++) {
            for(int j=0; j < matWidth; j++) {
                setX(wrapper, matWidth * i + j,1,Grid.INSIDE, (byte) (i+1));
            }
        }

        long matSize = grid.getWidth() * matWidth;

        for(int i=0; i < numMaterials; i++) {
            currMaterial = (byte) (i+1);
            matCount = 0;
            wrapper.findAttribute((byte) (i+1), this);

            assertEquals("Insert count wrong", matSize,matCount);
        }


        for(int i=0; i < numMaterials; i++) {
            currMaterial = (byte) (i+1);
            matCount = 0;
            wrapper.findAttribute((byte) (i+1), this);

            assertEquals("Insert count wrong", matSize,matCount);
        }

    }

    /**
     * Test that traversal methods work
     */
    public void testMaterialTraversalSpeed() {
        int size = 160;
        int numMaterials = 10;
        int matWidth = 15;
        int warmup = 10;
        int times = 10;
        long startTime;
        long time1;
        long time2;

System.out.println("Material Traversal Speed");
        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);
        MaterialIndexedWrapper wrapper = new MaterialIndexedWrapper(grid);

        for(int i=0; i < numMaterials; i++) {
            for(int j=0; j < matWidth; j++) {
                setX(wrapper, matWidth * i + j,1,Grid.INSIDE, (byte) i);
            }
        }

        long matSize = grid.getWidth() * matWidth;

        // warmup method 1

        for(int n=0; n < warmup; n++) {
            for(int i=0; i < numMaterials; i++) {
                currMaterial = (byte) i;
                matCount = 0;
                wrapper.findAttribute((byte) i, this);
            }
        }


        startTime = System.nanoTime();

        for(int n=0; n < times; n++) {
            for(int i=0; i < numMaterials; i++) {
                currMaterial = (byte) i;
                matCount = 0;
                wrapper.findAttribute((byte) i, this);
            }
        }

        time1 = System.nanoTime() - startTime;

        AttributeGrid grid2 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        for(int i=0; i < numMaterials; i++) {
            for(int j=0; j < matWidth; j++) {
                setX(grid2, matWidth * i + j,1,Grid.INSIDE, (byte) i);
            }
        }

        // warmup method 2

        for(int n=0; n < warmup; n++) {
            for(int i=0; i < numMaterials; i++) {
                currMaterial = (byte) i;
                matCount = 0;
                grid2.findAttribute((byte) i, this);
            }
        }

        startTime = System.nanoTime();

        for(int n=0; n < times; n++) {
            for(int i=0; i < numMaterials; i++) {
                currMaterial = (byte) i;
                matCount = 0;
                grid2.findAttribute((byte) i, this);
            }
        }

        time2 = System.nanoTime() - startTime;

        System.out.println("Wrapper: " + time1);
        System.out.println(" Direct: " + time2 + " " + (time2 / time1) + "X\n");
        assertTrue("Wrapper method too slow", time1 * 10 < time2);
    }

    /**
     * Test that traversal methods work
     */
    public void testMaterialTraversalSpeedNotFound() {
        int size = 160;
        int numMaterials = 10;
        int matWidth = 15;
        int warmup = 10;
        int times = 10;
        long startTime;
        long time1;
        long time2;

//System.out.println("Material Traversal Speed Not Found");
        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);
        MaterialIndexedWrapper wrapper = new MaterialIndexedWrapper(grid);

        for(int i=0; i < numMaterials; i++) {
            for(int j=0; j < matWidth; j++) {
                setX(wrapper, matWidth * i + j,1,Grid.INSIDE, (byte) i);
            }
        }

        long matSize = grid.getWidth() * matWidth;

        // warmup method 1

        for(int n=0; n < warmup; n++) {
            for(int i=0; i < numMaterials; i++) {
                currMaterial = (byte) (numMaterials+1);
                matCount = 0;
                wrapper.findAttribute((byte) currMaterial, this);
            }
        }


        startTime = System.nanoTime();

        for(int n=0; n < times; n++) {
            for(int i=0; i < numMaterials; i++) {
                currMaterial = (byte) (numMaterials+1);
                matCount = 0;
                wrapper.findAttribute((byte) currMaterial, this);
            }
        }

        time1 = System.nanoTime() - startTime;

        assertEquals("Wrapper count wrong", matCount, 0);

        AttributeGrid grid2 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        for(int i=0; i < numMaterials; i++) {
            for(int j=0; j < matWidth; j++) {
                setX(grid2, matWidth * i + j,1,Grid.INSIDE, (byte) i);
            }
        }

        // warmup method 2

        for(int n=0; n < warmup; n++) {
            for(int i=0; i < numMaterials; i++) {
                currMaterial = (byte) (numMaterials+1);
                matCount = 0;
                grid2.findAttribute((byte) currMaterial, this);
            }
        }

        matCount = 0;
        startTime = System.nanoTime();

        for(int n=0; n < times; n++) {
            for(int i=0; i < numMaterials; i++) {
                currMaterial = (byte) (numMaterials+1);
                matCount = 0;
                grid2.findAttribute((byte) currMaterial, this);
            }
        }

        time2 = System.nanoTime() - startTime;

        assertEquals("Direct count wrong", matCount, 0);

        System.out.println("Wrapper: " + time1);
        System.out.println(" Direct: " + time2 + " " + (time2 / time1) + "X\n");
        assertTrue("Wrapper method too slow", time1 * 50 < time2);

    }

    /**
     * Test that material count speed is better then regular grids.
     */
    public void testMaterialCountSpeed() {
        int size = 160;
        int numMaterials = 5;
        int matWidth = 15;
        int warmup = 10;
        int times = 10;
        long startTime;
        long time1;
        long time2;

System.out.println("Material Count Speed");
        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);
        MaterialIndexedWrapper wrapper = new MaterialIndexedWrapper(grid);

        for(int i=0; i < numMaterials; i++) {
            for(int j=0; j < matWidth; j++) {
                setX(wrapper, matWidth * i + j,1,Grid.INSIDE, (byte) i);
            }
        }

        long matSize = grid.getWidth() * matWidth;

        // warmup method 1

        for(int n=0; n < warmup; n++) {
            for(int i=0; i < numMaterials; i++) {
                wrapper.findCount((byte) i);
            }
        }


        startTime = System.nanoTime();

        for(int n=0; n < times; n++) {
            for(int i=0; i < numMaterials; i++) {
                matCount = wrapper.findCount((byte) i);
            }
        }

        time1 = System.nanoTime() - startTime;

        AttributeGrid grid2 = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        for(int i=0; i < numMaterials; i++) {
            for(int j=0; j < matWidth; j++) {
                setX(grid2, matWidth * i + j,1,Grid.INSIDE, (byte) i);
            }
        }

        // warmup method 2

        for(int n=0; n < warmup; n++) {
            for(int i=0; i < numMaterials; i++) {
                matCount = grid2.findCount((byte) i);
            }
        }

        startTime = System.nanoTime();

        for(int n=0; n < times; n++) {
            for(int i=0; i < numMaterials; i++) {
                matCount = grid2.findCount((byte) i);
            }
        }

        time2 = System.nanoTime() - startTime;

        System.out.println("Wrapper: " + time1);
        System.out.println(" Direct: " + time2 + " " + (time2 / time1) + "X\n");

        assertTrue("Wrapper method too slow", time1 * 50 < time2);

    }

    /**
     * Test creating an empty grid.
     */
    public void testCreateEmpty() {
        AttributeGrid grid = new ArrayAttributeGridByte(100, 101, 102, 0.001, 0.001);
        MaterialIndexedWrapper wrapper = new MaterialIndexedWrapper(grid);

        AttributeGrid grid2 = (AttributeGrid) wrapper.createEmpty(10, 11, 12, 0.002, 0.003);
        int gridSize = 10 * 11 * 12;

        assertTrue("Grid type is not ArrayAttributeGridByte", grid2 instanceof ArrayAttributeGridByte);
        assertEquals("Grid size is not " + gridSize, gridSize, grid2.getWidth()*grid2.getHeight()*grid2.getDepth());
        assertEquals("Grid voxel size is not 0.002", 0.002, grid2.getVoxelSize());
        assertEquals("Grid slice height is not 0.003", 0.003, grid2.getSliceHeight());

        grid = new ArrayAttributeGridInt(100, 100, 100, 0.001, 0.001);
        wrapper = new MaterialIndexedWrapper(grid);

        wrapper.setData(5, 5, 5, Grid.INSIDE, 10);

        grid2 = (AttributeGrid) wrapper.createEmpty(20, 21, 22, 0.005, 0.006);
        gridSize = 20 * 21 * 22;

        assertTrue("Grid type is not ArrayAttributeGridInt", grid2 instanceof ArrayAttributeGridInt);
        assertEquals("Grid size is not " + gridSize, gridSize, grid2.getWidth()*grid2.getHeight()*grid2.getDepth());
        assertEquals("Grid voxel size is not 0.005", 0.005, grid2.getVoxelSize());
        assertEquals("Grid slice height is not 0.006", 0.006, grid2.getSliceHeight());

        // all voxels in empty grid should be OUTSIDE state and 0 material
        assertEquals("State is not OUTSIDE for (5, 5, 5)", Grid.OUTSIDE, grid2.getState(5, 5, 5));
        assertEquals("Material is not 0 for (5, 5, 5)", 0, grid2.getAttribute(5, 5, 5));
    }

    /**
     * Test reassignAttribute.
     */
    public void testReassignMaterial() {
        int size = 20;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);
        MaterialIndexedWrapper wrapper = new MaterialIndexedWrapper(grid);

        // Fill voxels such that it looks like:
        //
        //      2  11111
        //      2
        //      2  33 33
        //
        setX(wrapper, 10, 10, Grid.INSIDE, 1, 8, 12);
        setX(wrapper, 8, 10, Grid.INSIDE, 3, 8, 12);
        wrapper.setState(10, 8, 10, Grid.OUTSIDE);
        setY(wrapper, 5, 10, Grid.INSIDE, 2, 8, 10);

        long newMaterial = 10;

        // reassign a non-existing material
        wrapper.reassignAttribute(new long[]{50}, newMaterial);
        assertEquals(0, wrapper.findCount(50));
        assertEquals(5, wrapper.findCount(1));
        assertEquals(3, wrapper.findCount(2));
        assertEquals(4, wrapper.findCount(3));

        // reassign a single existing material
        // check that the original material count is 0
        // check that the material has changed for the set positions
        wrapper.reassignAttribute(new long[]{1}, newMaterial);

        assertEquals(0, wrapper.findCount(1));

        for (int i=8; i<=12; i++) {
            assertEquals("State should be ", Grid.INSIDE, wrapper.getState(i, 10, 10));
            assertEquals("Material should be ", newMaterial, wrapper.getAttribute(i, 10, 10));
        }

        // reassign several material
        // check that the original material count is 0
        // check that the material has changed for the set positions
        newMaterial = 20;
        wrapper.reassignAttribute(new long[]{2, 3, 10}, newMaterial);

        assertEquals(0, wrapper.findCount(2));
        assertEquals(0, wrapper.findCount(3));
        assertEquals(0, wrapper.findCount(10));

        for (int i=8; i<=12; i++) {
            assertEquals("State should be ", Grid.INSIDE, wrapper.getState(i, 10, 10));
            assertEquals("Material should be ", newMaterial, wrapper.getAttribute(i, 10, 10));
        }

        for (int i=8; i<=9; i++) {
            assertEquals("State should be ", Grid.INSIDE, wrapper.getState(i, 8, 10));
            assertEquals("Material should be ", newMaterial, wrapper.getAttribute(i, 8, 10));
        }

        for (int i=11; i<=12; i++) {
            assertEquals("State should be ", Grid.INSIDE, wrapper.getState(i, 8, 10));
            assertEquals("Material should be ", newMaterial, wrapper.getAttribute(i, 8, 10));
        }

        for (int i=8; i<=10; i++) {
            assertEquals("State should be ", Grid.INSIDE, wrapper.getState(5, i, 10));
            assertEquals("Material should be ", newMaterial, wrapper.getAttribute(5, i, 10));
        }

    }

    /**
     * Test the constructors and the grid size.
     */
    public void testSetGrid() {
        int size = 10;
        double voxelSize = 0.001;

        AttributeGrid grid = new ArrayAttributeGridByte(size, size, size, voxelSize, voxelSize);
        MaterialIndexedWrapper wrapper = new MaterialIndexedWrapper(grid);

        size = 20;
        voxelSize = 0.002;

        AttributeGrid grid2 = new ArrayAttributeGridByte(size,size,size,voxelSize, voxelSize);
        wrapper.setGrid(grid2);

        // Fill voxels such that it looks like:
        //
        //      2  11111
        //      2
        //      2  33 33
        //
        setX(wrapper, 10, 10, Grid.INSIDE, 1, 8, 12);
        setX(wrapper, 8, 10, Grid.INSIDE, 3, 8, 12);
        wrapper.setState(10, 8, 10, Grid.OUTSIDE);
        setY(wrapper, 5, 10, Grid.INSIDE, 2, 8, 10);

        int gridSize = size * size * size;

        assertEquals("Grid size is not " + gridSize, gridSize, wrapper.getWidth()*wrapper.getHeight()*wrapper.getDepth());
        assertEquals(5, wrapper.findCount(1));
        assertEquals(3, wrapper.findCount(2));
        assertEquals(4, wrapper.findCount(3));
    }

    /**
     * Test setAttribute.
     */
    public void testsetAttribute() {
        int size = 10;

        AttributeGrid grid = new ArrayAttributeGridByte(size, size, size, 0.001, 0.001);
        MaterialIndexedWrapper wrapper = new MaterialIndexedWrapper(grid);

        wrapper.setData(0, 0, 0, Grid.INSIDE, 1);
        wrapper.setData(9, 9, 9, Grid.INSIDE, 2);
        wrapper.setData(5, 0, 7, Grid.INSIDE, 3);

        wrapper.setAttribute(0, 0, 0, 10);
        wrapper.setAttribute(9, 9, 9, 11);
        wrapper.setAttribute(5, 0, 7, 12);

        // check that the material changed, but the state did not
        assertEquals("Material should be ", 10, wrapper.getAttribute(0, 0, 0));
        assertEquals("State should be ", Grid.INSIDE, wrapper.getState(0, 0, 0));

        assertEquals("Material should be ", 11, wrapper.getAttribute(9, 9, 9));
        assertEquals("State should be ", Grid.INSIDE, wrapper.getState(9, 9, 9));

        assertEquals("Material should be ", 12, wrapper.getAttribute(5, 0, 7));
        assertEquals("State should be ", Grid.INSIDE, wrapper.getState(5, 0, 7));
    }

    /**
     * Test setState.
     */
    public void testSetState() {
        int size = 10;

        AttributeGrid grid = new ArrayAttributeGridByte(size, size, size, 0.001, 0.001);
        MaterialIndexedWrapper wrapper = new MaterialIndexedWrapper(grid);

        wrapper.setData(0, 0, 0, Grid.INSIDE, 1);
        wrapper.setData(9, 9, 9, Grid.INSIDE, 2);
        wrapper.setData(5, 0, 7, Grid.INSIDE, 3);

        wrapper.setState(0, 0, 0, Grid.INSIDE);
        wrapper.setState(9, 9, 9, Grid.INSIDE);
        wrapper.setState(5, 0, 7, Grid.INSIDE);

        // check that the state changed, but the material did not
        assertEquals("State should be ", Grid.INSIDE, wrapper.getState(0, 0, 0));
        assertEquals("Material should be ", 1, wrapper.getAttribute(0, 0, 0));

        assertEquals("State should be ", Grid.INSIDE, wrapper.getState(9, 9, 9));
        assertEquals("Material should be ", 2, wrapper.getAttribute(9, 9, 9));

        assertEquals("State should be ", Grid.INSIDE, wrapper.getState(5, 0, 7));
        assertEquals("Material should be ", 3, wrapper.getAttribute(5, 0, 7));
    }

    /**
     * Set all the X values of a grid.
     *
     * @param state The new state
     * @param mat The new material
     */
    protected static void setX(AttributeGrid grid, int y, int z, byte state, byte mat) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        for(int x=0; x < width; x++) {
            grid.setData(x,y,z, state, mat);
        }
    }

    /**
     * A voxel of the class requested has been found.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public void found(int x, int y, int z, VoxelData vd) {
        if (vd.getMaterial() == currMaterial) {
            matCount++;
        }
    }

    /**
     * A voxel of the class requested has been found.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public boolean foundInterruptible(int x, int y, int z, VoxelData vd) {
        // ignore
        return true;
    }
}
