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
import java.text.NumberFormat;

// Internal Imports

/**
 * Tests the performance of grids.  Confirms the relative design
 * points of tradeoffs between access speed and memory.
 *
 * @author Alan Hudson
 * @version
 */
public class TestPerformanceGrid extends BaseTestGrid {
    public static final int SIZE = 16;

    public static final int SMALL_SIZE = 32;
    public static final int LARGE_SIZE = 128;

    public static final int TIMES = 30;
    public static final int SMALL_TIMES = 3 * TIMES;
    public static final int LARGE_TIMES = TIMES;

    public static final int WARMUP = 0;
    public static final int WARMUP2 = 15;

    public static final NumberFormat formater;

    static {
        formater = NumberFormat.getNumberInstance();
        formater.setMaximumFractionDigits(4);
        formater.setGroupingUsed(false);

        // TODO: I think these prints may be necessary to avoid some weird
        // issues with the initial run timing

        System.out.println("Starting Warmup");

/*
        for(int i=0; i < WARMUP2; i++) {
            writeAccessY(false);
        }
        for(int i=0; i < WARMUP2; i++) {
            writeAccessZ(false);
        }
*/
        // Let JIT finish compiling
        try {
            Thread.sleep(1500);
        } catch(Exception e) {
        }

        System.out.println("Finished Warmup");
    }

    /**
     * Test the write speed of slice aligned traversal.
     */
    public void testStyleSmall() {
        System.out.println("ReadStyle Large");
        for(int i=0; i < WARMUP2; i++) {
            readStyles(SMALL_SIZE,SMALL_TIMES,false);
        }
        readStyles(SMALL_SIZE,SMALL_TIMES,true);
        //readAccessState(SMALL_SIZE,SMALL_TIMES,true);
        //readAccessState(SMALL_SIZE,SMALL_TIMES,true);
    }

    /**
     * Test the write speed of slice aligned traversal.
     */
    public void testStyleLarge() {
        System.out.println("ReadStyle Large");
        for(int i=0; i < WARMUP2; i++) {
            readStyles(LARGE_SIZE,LARGE_TIMES,false);
        }
        readStyles(LARGE_SIZE,LARGE_TIMES,true);
        //readAccessState(LARGE_SIZE,LARGE_TIMES,true);
        //readAccessState(LARGE_SIZE,LARGE_TIMES,true);

        fail("stop");
    }

    /**
     * Test the read access of a particular state
     */
    public static void testFindCount() {
        // Test Method 1
        int size = 32;
        Grid grid = new OctreeGridByte(size,size,size,0.001, 0.001);

        //setup
        setX(grid, Grid.EXTERIOR, 0);
        setY(grid, Grid.EXTERIOR, 0);
        setZ(grid, Grid.EXTERIOR, 0);

System.out.println("Finding exterior voxels");
        grid.findCount(Grid.VoxelClasses.EXTERIOR);

//        assertTrue("stop",1 == 0);
    }

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {

        return new TestSuite(TestPerformanceGrid.class);
    }



    /**
     * Test the write speed of slice aligned traversal.
     */
    public void testWriteAccessXSmall() {
        System.out.println("WriteAccess Small");
        for(int i=0; i < WARMUP2; i++) {
System.out.println("Warmup: " + i);
            writeAccessX(SMALL_SIZE,SMALL_TIMES,false);
        }
        writeAccessX(SMALL_SIZE,SMALL_TIMES,true);
        writeAccessX(SMALL_SIZE,SMALL_TIMES,true);
        writeAccessX(SMALL_SIZE,SMALL_TIMES,true);
    }

    /**
     * Test the write speed of slice aligned traversal.
     */
/*
    public void testWriteAccessXLarge() {
        System.out.println("WriteAccess Large");
        for(int i=0; i < WARMUP2; i++) {
            writeAccessX(LARGE_SIZE,LARGE_TIMES,false);
        }
        writeAccessX(LARGE_SIZE,LARGE_TIMES,true);
        writeAccessX(LARGE_SIZE,LARGE_TIMES,true);
        writeAccessX(LARGE_SIZE,LARGE_TIMES,true);
    }
*/

    /**
     * Test the write speed of slice aligned traversal.
     */
    public void testReadAccessSmall() {
        System.out.println("ReadAccess Small");
        for(int i=0; i < WARMUP2; i++) {
            readAccessState(SMALL_SIZE,SMALL_TIMES,false);
        }
        readAccessState(SMALL_SIZE,SMALL_TIMES,true);
        //readAccessState(SMALL_SIZE,SMALL_TIMES,true);
        //readAccessState(SMALL_SIZE,SMALL_TIMES,true);
    }

    /**
     * Test the write speed of slice aligned traversal.
     */
    public void testReadAccessLarge() {
        System.out.println("ReadAccess Large");
        for(int i=0; i < WARMUP2; i++) {
            readAccessState(LARGE_SIZE,LARGE_TIMES,false);
        }
        readAccessState(LARGE_SIZE,LARGE_TIMES,true);
        //readAccessState(LARGE_SIZE,LARGE_TIMES,true);
        //readAccessState(LARGE_SIZE,LARGE_TIMES,true);
    }

    /**
     * Test read access styles.
     */
    public static void readStyles(int size, int times, boolean display) {
        if (display) System.out.println("ReadAccessStyle Times:");

        // Test Method 2
        Grid grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            readStyle3(grid);
        }

        long stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            readStyle3(grid);
        }

        long totalTime1 = System.nanoTime() - stime;

        if (display) System.out.println("Style3        : " + totalTime1);

        // Test Method 1
        grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            readStyle1(grid);
        }

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            readStyle1(grid);
        }

        long totalTime3 = System.nanoTime() - stime;

        if (display) System.out.println("Style1 : " + totalTime3 + " " +
            formater.format((float)totalTime3 / totalTime1) + "X");

        // Test Method 2
        grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            readStyle2(grid);
        }

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            readStyle2(grid);
        }

        long totalTime2 = System.nanoTime() - stime;

        if (display) System.out.println("Style2 : " + totalTime2 + " " +
            formater.format((float)totalTime2 / totalTime1) + "X");

        grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            readStyle4(grid);
        }

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            readStyle4(grid);
        }

        long totalTime4 = System.nanoTime() - stime;

        if (display) System.out.println("Style4 : " + totalTime4 + " " +
            formater.format((float)totalTime4 / totalTime1) + "X");

        grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            readStyle5(grid);
        }

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            readStyle5(grid);
        }

        long totalTime5 = System.nanoTime() - stime;

        if (display) System.out.println("Style5 : " + totalTime5 + " " +
            formater.format((float)totalTime5 / totalTime1) + "X");

        grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            readStyle6(grid);
        }

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            readStyle6(grid);
        }

        long totalTime6 = System.nanoTime() - stime;

        if (display) System.out.println("Style6 : " + totalTime6 + " " +
            formater.format((float)totalTime6 / totalTime1) + "X");

        if (display) System.out.println();
    }

    /**
     * Test the read access of a particular state
     */
    public static void readAccessState(int size, int times, boolean display) {
        if (display) System.out.println("ReadState Times:");

        // Test Method 1
        Grid grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        //setup
        setX(grid, Grid.EXTERIOR, 0);
        setY(grid, Grid.EXTERIOR, 0);
        setZ(grid, Grid.EXTERIOR, 0);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            grid.findCount(Grid.VoxelClasses.EXTERIOR);
        }

        long stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            grid.findCount(Grid.VoxelClasses.EXTERIOR);
        }

        long totalTime1 = System.nanoTime() - stime;

        if (display) System.out.println("ArrayGrid        : " + totalTime1);

        // Test Method 2
        grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        //setup
        setX(grid, Grid.EXTERIOR, 0);
        setY(grid, Grid.EXTERIOR, 0);
        setZ(grid, Grid.EXTERIOR, 0);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            grid.findCount(Grid.VoxelClasses.EXTERIOR);
        }

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            grid.findCount(Grid.VoxelClasses.EXTERIOR);
        }

        long totalTime2 = System.nanoTime() - stime;

        if (display) System.out.println("SliceGrid(Array) : " + totalTime2 + " " +
            formater.format((float)totalTime2 / totalTime1) + "X");

        // Test Method 2
        grid = new OctreeGridByte(size,size,size,0.001, 0.001);


        //setup
        setX(grid, Grid.EXTERIOR, 0);
        setY(grid, Grid.EXTERIOR, 0);
        setZ(grid, Grid.EXTERIOR, 0);

        System.out.println("Cell Count: " + ((OctreeGridByte)grid).getCellCount() + " verses: " + (size*size*size));

        // warmup
        for(int i=0; i < WARMUP; i++) {
            grid.findCount(Grid.VoxelClasses.EXTERIOR);
        }

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            grid.findCount(Grid.VoxelClasses.EXTERIOR);
        }

        long totalTime3 = System.nanoTime() - stime;

        if (display) System.out.println("OctreeGrid       : " + totalTime3 + " " +
            formater.format((float)totalTime3 / totalTime1) + "X");

        if (display) System.out.println();
    }

    /**
     * Test the write speed of slice aligned traversal.
     */
    public static void writeAccessX(int size, int times, boolean display) {
        if (display) System.out.println("WriteX Times:");

        // Test Method 1
        Grid grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            setX(grid, Grid.EXTERIOR, 8);
        }

        long stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            setX(grid, Grid.EXTERIOR, 8);
        }

        long totalTime1 = System.nanoTime() - stime;

        if (display) System.out.println("ArrayGrid        : " + totalTime1);

        // Test Method 2
        grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            setX(grid, Grid.EXTERIOR, 8);
        }

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            setX(grid, Grid.EXTERIOR, 8);
        }

        long totalTime2 = System.nanoTime() - stime;

        if (display) System.out.println("SliceGrid(Array) : " + totalTime2 + " " +
            formater.format((float)totalTime2 / totalTime1) + "X");

        // Test Method 2
        grid = new ArrayGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            setX(grid, Grid.EXTERIOR, 8);
        }

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            setX(grid, Grid.EXTERIOR, 8);
        }

        long totalTime3 = System.nanoTime() - stime;

        if (display) System.out.println("SliceGrid(Map)   : " + totalTime3 +
            " " + formater.format((float)totalTime3 / totalTime1) + "X");

        // Test Method 2
        grid = new OctreeGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            setX(grid, Grid.EXTERIOR, 8);
        }

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            setX(grid, Grid.EXTERIOR, 8);
        }

        long totalTime4 = System.nanoTime() - stime;

        if (display) System.out.println("OctreeGrid()     : " + totalTime4 +
            " " + formater.format((float)totalTime4 / totalTime1) + "X");

        if (display) System.out.println();
    }

    /**
     * Test the write speed of slice unaligned traversal.
     */
/*
    public void testWriteAccessY() {
        System.out.println("WriteY Times:");

        // Test Method 1
        Grid grid = new ArrayGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            setY(grid, Grid.EXTERIOR, 8);
        }

        long stime = System.nanoTime();

        for(int i=0; i < TIMES; i++) {
            setY(grid, Grid.EXTERIOR, 8);
        }

        long totalTime1 = System.nanoTime() - stime;

        System.out.println("ArrayGrid        : " + totalTime1);

        // Test Method 2
        grid = new ArrayGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            setY(grid, Grid.EXTERIOR, 8);
        }

        stime = System.nanoTime();

        for(int i=0; i < TIMES; i++) {
            setY(grid, Grid.EXTERIOR, 8);
        }

        long totalTime2 = System.nanoTime() - stime;

        System.out.println("SliceGrid(Array) : " + totalTime2 + " " + formater.format((float)totalTime2 / totalTime1) + "X");

        // Test Method 2
        grid = new ArrayGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            setY(grid, Grid.EXTERIOR, 8);
        }

        stime = System.nanoTime();

        for(int i=0; i < TIMES; i++) {
            setY(grid, Grid.EXTERIOR, 8);
        }

        long totalTime3 = System.nanoTime() - stime;

        System.out.println("SliceGrid(Map)   : " + totalTime3 + " " + formater.format((float)totalTime3 / totalTime1) + "X");

        System.out.println();
    }
*/
    /**
     * Test the write speed of slice unaligned traversal.
     */
/*
    public void testWriteAccessZ() {
        System.out.println("WriteZ Times:");

        // Test Method 1
        Grid grid = new ArrayGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            setZ(grid, Grid.EXTERIOR, 8);
        }

        long stime = System.nanoTime();

        for(int i=0; i < TIMES; i++) {
            setZ(grid, Grid.EXTERIOR, 8);
        }

        long totalTime1 = System.nanoTime() - stime;

        System.out.println("ArrayGrid        : " + totalTime1);

        // Test Method 2
        grid = new ArrayGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            setZ(grid, Grid.EXTERIOR, 8);
        }

        stime = System.nanoTime();

        for(int i=0; i < TIMES; i++) {
            setZ(grid, Grid.EXTERIOR, 8);
        }

        long totalTime2 = System.nanoTime() - stime;

        System.out.println("SliceGrid(Array) : " + totalTime2 +
            " " + formater.format((float)totalTime2 / totalTime1) + "X");

        // Test Method 2
        grid = new ArrayGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            setZ(grid, Grid.EXTERIOR, 8);
        }

        stime = System.nanoTime();

        for(int i=0; i < TIMES; i++) {
            setZ(grid, Grid.EXTERIOR, 8);
        }

        long totalTime3 = System.nanoTime() - stime;

        System.out.println("SliceGrid(Map)   : " + totalTime3 +
            " " + formater.format((float)totalTime3 / totalTime1) + "X");

        System.out.println();
    }
*/

    /**
     * Set all the X values of a grid.
     *
     * @param state The new state
     * @param mat The new material
     */
    protected static void setX(Grid grid, byte state, int mat) {

        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        for(int x=0; x < width; x++) {
            grid.setData(x,0,0, state, mat);
        }
    }

    /**
     * Set all the Y values of a grid.
     *
     * @param state The new state
     * @param mat The new material
     */
    protected static void setY(Grid grid, byte state, int mat) {

        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        for(int y=0; y < height; y++) {
            grid.setData(0,y,0, state, mat);
        }
    }

    /**
     * Set all the Z values of a grid.
     *
     * @param state The new state
     * @param mat The new material
     */
    protected static void setZ(Grid grid, byte state, int mat) {

        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        for(int z=0; z < depth; z++) {
            grid.setData(0,0,z, state, mat);
        }
    }

    protected static void readStyle1(Grid grid) {
        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                for(int z=0; z < depth; z++) {
                    VoxelData vd = grid.getData(x,y,z);
                }
            }
        }
    }

    protected static void readStyle2(Grid grid) {
        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        for(int x=0; x < width; x++) {
            for(int z=0; z < depth; z++) {
                for(int y=0; y < height; y++) {
                    VoxelData vd = grid.getData(x,y,z);
                }
            }
        }
    }

    protected static void readStyle3(Grid grid) {
        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    VoxelData vd = grid.getData(x,y,z);
                }
            }
        }
    }

    protected static void readStyle4(Grid grid) {
        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        for(int y=0; y < height; y++) {
            for(int z=0; z < depth; z++) {
                for(int x=0; x < width; x++) {
                    VoxelData vd = grid.getData(x,y,z);
                }
            }
        }
    }

    protected static void readStyle5(Grid grid) {
        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        for(int z=0; z < depth; z++) {
            for(int x=0; x < width; x++) {
                for(int y=0; y < height; y++) {
                    VoxelData vd = grid.getData(x,y,z);
                }
            }
        }
    }

    protected static void readStyle6(Grid grid) {
        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        for(int z=0; z < depth; z++) {
            for(int y=0; y < height; y++) {
                for(int x=0; x < width; x++) {
                    VoxelData vd = grid.getData(x,y,z);
                }
            }
        }
    }

}
