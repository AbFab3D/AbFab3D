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
import abfab3d.geom.CubeCreator;
import abfab3d.geom.TriangleModelCreator;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.j3d.geom.GeometryData;
import org.j3d.geom.TorusGenerator;

import java.text.NumberFormat;
import java.util.Random;

// Internal Imports

/**
 * Tests the performance of grids.  Confirms the relative design
 * points of tradeoffs between access speed and memory.
 *
 * @author Alan Hudson
 * @version
 */
public class TestPerformanceGrid extends BaseTestAttributeGrid {
    /** How many times to run a test to compare with itself */
    public static final int RUNS = 4;

    public static final int SMALL_SIZE = 32;
    public static final int LARGE_SIZE = 256;

//    public static final int TIMES = 20;
    public static final int TIMES = 30;
    public static final int SMALL_TIMES = 3 * TIMES;
    public static final int LARGE_TIMES = TIMES;

    public static final int WARMUP = 0;
    public static final int WARMUP2 = TIMES;

    public static final NumberFormat formater;

    double voxel_size = 0.001;
    double slice_height = 0.001;

    private AttributeGrid[] grids;

    static {
        formater = NumberFormat.getNumberInstance();
        formater.setMaximumFractionDigits(4);
        formater.setGroupingUsed(false);


        // TODO: I think these prints may be necessary to avoid some weird
        // issues with the initial run timing

//        System.out.println("Starting Warmup");

/*
        for(int i=0; i < WARMUP2; i++) {
            writeAccessY(false);
        }
        for(int i=0; i < WARMUP2; i++) {
            writeAccessZ(false);
        }
*/

  /*
        // Let JIT finish compiling
        try {
            Thread.sleep(1500);
        } catch(Exception e) {
        }

        System.out.println("Finished Warmup");
*/
    }

    private void createGrids(int size) {
        grids = new AttributeGrid[] {
                new GridShortIntervals(size,size,size,voxel_size, slice_height),
//                new ArrayAttributeGridByteDefault(size,size,size,voxel_size, slice_height),
//                new ArrayAttributeGridByte(size,size,size,voxel_size, slice_height),
//                new ArrayAttributeGridByteProto(size,size,size,voxel_size, slice_height,null),
//                new ArrayAttributeGridByteProto(size,size,size,voxel_size, slice_height,StoredInsideOutsideFuncFactory.create(2,6))

//                new ArrayAttributeGridByteIndexLong(size,size,size,voxel_size, slice_height),
//                new ArrayAttributeGridShort(size,size,size,voxel_size, slice_height),
//                new ArrayAttributeGridInt(size,size,size,voxel_size, slice_height),
//                new BlockBasedAttributeGridByte(size,size,size,voxel_size, slice_height),
                //new OctreeAttributeGridByte(size,size,size,voxel_size, slice_height)
//                new ArrayAttributeGridByteProto(size,size,size,voxel_size, slice_height,StoredInsideOutsideFuncFactory.create(2,6))
        };

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
    public void testReadStyleSmall() {
        System.out.println("ReadStyle Small");
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
    public void testReadStyleLarge() {
        System.out.println("ReadStyle Large");
        for(int i=0; i < WARMUP2; i++) {
            readStyles(LARGE_SIZE,LARGE_TIMES,false);
        }
        readStyles(LARGE_SIZE,LARGE_TIMES,true);
        //readAccessState(LARGE_SIZE,LARGE_TIMES,true);
        //readAccessState(LARGE_SIZE,LARGE_TIMES,true);

//        fail("stop");
    }

    /**
     * Test the read access of a particular state
     */
    public static void testFindCount() {
        // Test Method 1
        int size = 32;
        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        //setup
        setX(grid, Grid.INSIDE, 0);
        setY(grid, Grid.INSIDE, 0);
        setZ(grid, Grid.INSIDE, 0);

        grid.findCount(VoxelClasses.INSIDE);

//        assertTrue("stop",1 == 0);
    }



    /**
     * Test the write speed of slice aligned traversal.
     */
    public void testWriteAccessXSmall() {


        System.out.println("WriteAccess XSmall");
        for(int i=0; i < WARMUP2; i++) {
            createGrids(SMALL_SIZE);
            writeAccessX(grids,SMALL_TIMES,false);
        }
        for(int i=0; i < RUNS; i++) {
            createGrids(SMALL_SIZE);
            writeAccessX(grids,SMALL_TIMES,true);
        }
    }

    /**
     * Test the write speed of slice aligned traversal.
     */
    public void testWriteAccessXLarge() {

        System.out.println("WriteAccess Large");
        for(int i=0; i < WARMUP2; i++) {
            createGrids(LARGE_SIZE);
            writeAccessX(grids,LARGE_TIMES,false);
        }
        System.gc();

        for(int i=0; i < RUNS; i++) {
            createGrids(LARGE_SIZE);
            writeAccessX(grids,LARGE_TIMES,true);
            System.gc();

        }
    }

    /**
     * Test the write speed of a solid object with just exteriors
     */
    public void testWriteAccessTorusShellLarge() {

        //System.out.println("WriteAccessTorusShell Large");
        for(int i=0; i < WARMUP2; i++) {
            createGrids(LARGE_SIZE);
            writeAccessTorusShell(grids, LARGE_TIMES, false, false);
        }
        System.gc();

        for(int i=0; i < RUNS; i++) {
            createGrids(LARGE_SIZE);
            writeAccessTorusShell(grids, LARGE_TIMES, false, true);
            System.gc();
        }
    }

    /**
     * Test the write speed of a solid object with interiors
     */
    public void testWriteAccessTorusSolidLarge() {

        int div = 8;    // Solid is much slower then other things
        //System.out.println("WriteAccessTorusSolid Large");
        for(int i=0; i < WARMUP2; i++) {
            createGrids(LARGE_SIZE);
            writeAccessTorusShell(grids, LARGE_TIMES / div, true, false);
        }
        System.gc();

        for(int i=0; i < RUNS; i++) {
            createGrids(LARGE_SIZE);
            writeAccessTorusShell(grids, LARGE_TIMES / div, true, true);
            System.gc();
        }
    }

    /**
     * Test the write speed of sparse object.  In theory will give block based structures a workout.
     */
    public void testWriteAccessLinkedCubesLarge() {

        //System.out.println("WriteAccessTorusSolid Large");
        for(int i=0; i < WARMUP2; i++) {
            createGrids(LARGE_SIZE);
            writeAccessLinkedCubes(grids, LARGE_TIMES, false);
        }

        System.gc();

        for(int i=0; i < RUNS; i++) {
            createGrids(LARGE_SIZE*2);
            writeAccessLinkedCubes(grids, LARGE_TIMES, true);
            System.gc();
        }
    }

    /**
     * Test the write speed of sparse object.  In theory will give block based structures a workout.
     */
    public void testWriteAccessRandom() {
        int div = 8;    // Solid is much slower then other things


        //System.out.println("WriteAccessTorusSolid Large");
        for(int i=0; i < WARMUP2; i++) {
            createGrids(LARGE_SIZE);
            writeAccessRandom(grids, LARGE_TIMES / div, false);
        }

        System.gc();

        for(int i=0; i < RUNS; i++) {
            createGrids(LARGE_SIZE);
            writeAccessRandom(grids, LARGE_TIMES / div, true);
            System.gc();
        }
    }

    /**
     * Test the write speed of slice aligned traversal.
     */
    public void testReadAccessSmall() {
        System.out.println("ReadAccess Small");
        for(int i=0; i < WARMUP2; i++) {
            readAccessState(SMALL_SIZE,SMALL_TIMES,false);
        }
        System.gc();

        for(int i=0; i < RUNS; i++) {
            readAccessState(SMALL_SIZE,SMALL_TIMES,true);
            System.gc();
        }
    }

    /**
     * Test the write speed of slice aligned traversal.
     */
    public void testReadAccessLarge() {
        System.out.println("ReadAccess Large");
        for(int i=0; i < WARMUP2; i++) {
            readAccessState(LARGE_SIZE,LARGE_TIMES,false);
        }
        System.gc();

        for(int i=0; i < RUNS; i++) {
            readAccessState(LARGE_SIZE,LARGE_TIMES,true);
            System.gc();
        }
    }

    /**
     * Test read access styles.
     */
    public static void readStyles(int size, int times, boolean display) {
        if (display) System.out.println("ReadAccessStyle Times:");

        // Test Method 2
        Grid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            readStyleYXZ(grid);
        }
        System.gc();

        long stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            readStyleYXZ(grid);
            System.gc();

        }

        long totalTime1 = System.nanoTime() - stime;

        if (display) System.out.println("Style YXZ        : " + totalTime1);

        // Test Method 1
        grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            readStyleXYZ(grid);
        }

        System.gc();

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            readStyleXYZ(grid);
        }

        long totalTime3 = System.nanoTime() - stime;

        if (display) System.out.println("Style XYZ : " + totalTime3 + " " +
            formater.format((float)totalTime3 / totalTime1) + "X");

        // Test Method 2
        grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            readStyleXZY(grid);
        }

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            readStyleXZY(grid);
        }

        long totalTime2 = System.nanoTime() - stime;

        if (display) System.out.println("Style XZY : " + totalTime2 + " " +
            formater.format((float)totalTime2 / totalTime1) + "X");

        grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            readStyleYZX(grid);
        }

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            readStyleYZX(grid);
        }

        long totalTime4 = System.nanoTime() - stime;

        if (display) System.out.println("Style YZX : " + totalTime4 + " " +
            formater.format((float)totalTime4 / totalTime1) + "X");

        grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            readStyleZXY(grid);
        }

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            readStyleZXY(grid);
        }

        long totalTime5 = System.nanoTime() - stime;

        if (display) System.out.println("Style ZXY : " + totalTime5 + " " +
            formater.format((float)totalTime5 / totalTime1) + "X");

        grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            readStyleZYX(grid);
        }

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            readStyleZYX(grid);
        }

        long totalTime6 = System.nanoTime() - stime;

        if (display) System.out.println("Style ZYX : " + totalTime6 + " " +
            formater.format((float)totalTime6 / totalTime1) + "X");

        if (display) System.out.println();
    }

    /**
     * Test the read access of a particular state
     */
    public static void readAccessState(int size, int times, boolean display) {
        if (display) System.out.println("ReadState Times:");

        // Test Method 1
        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        //setup
        setX(grid, Grid.INSIDE, 0);
        setY(grid, Grid.INSIDE, 0);
        setZ(grid, Grid.INSIDE, 0);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            grid.findCount(VoxelClasses.INSIDE);
        }

        long stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            grid.findCount(VoxelClasses.INSIDE);
        }

        long totalTime1 = System.nanoTime() - stime;

        if (display) System.out.println("ArrayGrid        : " + totalTime1);

        // Test Method 2
        grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        //setup
        setX(grid, Grid.INSIDE, 0);
        setY(grid, Grid.INSIDE, 0);
        setZ(grid, Grid.INSIDE, 0);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            grid.findCount(VoxelClasses.INSIDE);
        }

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            grid.findCount(VoxelClasses.INSIDE);
        }

        long totalTime2 = System.nanoTime() - stime;

        if (display) System.out.println("SliceGrid(Array) : " + totalTime2 + " " +
            formater.format((float)totalTime2 / totalTime1) + "X");

        /*
        // Test Method 2
        grid = new OctreeAttributeGridByte(size,size,size,0.001, 0.001);


        //setup
        setX(grid, Grid.INSIDE, 0);
        setY(grid, Grid.INSIDE, 0);
        setZ(grid, Grid.INSIDE, 0);

        //System.out.println("Cell Count: " + ((OctreeAttributeGridByte)grid).getCellCount() + " verses: " + (size*size*size));

        // warmup
        for(int i=0; i < WARMUP; i++) {
            grid.findCount(VoxelClasses.INSIDE);
        }

        stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            grid.findCount(VoxelClasses.INSIDE);
        }

        long totalTime3 = System.nanoTime() - stime;

        if (display) System.out.println("OctreeGrid       : " + totalTime3 + " " +
            formater.format((float)totalTime3 / totalTime1) + "X");

        if (display) System.out.println();
        */
    }

    /**
     * Voxelize an object shell.
     *
     * @param times
     * @param display
     */
    public static void writeAccessTorusShell(Grid[] grids, int times, boolean fill, boolean display) {
        if (display) {
            if (fill) {
                System.out.println("WriteAccessTorusSolid Times:");
            } else {
                System.out.println("WriteAccessTorusShell Times:");

            }
        }

        long[] totalTime = new long[grids.length];

        for(int n=0; n < grids.length; n++) {
            // warmup
            for(int i=0; i < WARMUP; i++) {
                setTorus(grids[n], fill);
            }

            long stime = System.nanoTime();

            for(int i=0; i < times; i++) {
                setTorus(grids[n], fill);
            }

            totalTime[n] = System.nanoTime() - stime;

            String name = grids[n].getClass().getSimpleName();

            if (display) {
                float tps = 1000000000f / totalTime[n];
                System.out.println(String.format("%1$-31s",name) + "        : " + String.format("%1$-13s",totalTime[n]) + " NS " + String.format("%1$-7s",formater.format(tps)) + " TPS " + " " + String.format("%1$-7s",formater.format((float)totalTime[n] / totalTime[0])) + "X");
            }
        }


        if (display) System.out.println();

    }

    /**
     * Voxelize an object shell.
     *
     * @param times
     * @param display
     */
    public static void writeAccessRandom(Grid[] grids, int times, boolean display) {
        if (display) {
            System.out.println("WriteAccessRandom Times:");
        }

        long[] totalTime = new long[grids.length];
        float percent = 0.01f;

        for(int n=0; n < grids.length; n++) {
            // warmup
            for(int i=0; i < WARMUP; i++) {
                setRandom(grids[n], percent);
            }

            long stime = System.nanoTime();

            for(int i=0; i < times; i++) {
                setRandom(grids[n], percent);
            }

            totalTime[n] = System.nanoTime() - stime;

            String name = grids[n].getClass().getSimpleName();

            if (display) {
                float tps = 1000000000f / totalTime[n];
                System.out.println(String.format("%1$-31s",name) + "        : " + String.format("%1$-13s",totalTime[n]) + " NS " + String.format("%1$-7s",formater.format(tps)) + " TPS " + " " + String.format("%1$-7s",formater.format((float)totalTime[n] / totalTime[0])) + "X");
            }
        }


        if (display) System.out.println();

    }

    public static void writeAccessLinkedCubes(Grid[] grids, int times, boolean display) {
        if (display) System.out.println("WriteAccessLinkedCubed Times:");

        long[] totalTime = new long[grids.length];

        CubeCreator.Style[][] styles = new CubeCreator.Style[6][];

        styles[0] = new CubeCreator.Style[4];
        styles[0][0] = CubeCreator.Style.TOP_ROW;
        styles[0][1] = CubeCreator.Style.BOTTOM_ROW;
        styles[0][2] = CubeCreator.Style.LEFT_ROW;
        styles[0][3] = CubeCreator.Style.RIGHT_ROW;

        styles[0] = new CubeCreator.Style[4];
        styles[0][0] = CubeCreator.Style.TOP_ROW;
        styles[0][1] = CubeCreator.Style.BOTTOM_ROW;
        styles[0][2] = CubeCreator.Style.LEFT_ROW;
        styles[0][3] = CubeCreator.Style.RIGHT_ROW;


        styles[1] = new CubeCreator.Style[4];
        styles[1][0] = CubeCreator.Style.TOP_ROW;
        styles[1][1] = CubeCreator.Style.BOTTOM_ROW;
        styles[1][2] = CubeCreator.Style.LEFT_ROW;
        styles[1][3] = CubeCreator.Style.RIGHT_ROW;

        styles[2] = new CubeCreator.Style[4];
        styles[2][0] = CubeCreator.Style.TOP_ROW;
        styles[2][1] = CubeCreator.Style.BOTTOM_ROW;
        styles[2][2] = CubeCreator.Style.LEFT_ROW;
        styles[2][3] = CubeCreator.Style.RIGHT_ROW;

        styles[3] = new CubeCreator.Style[4];
        styles[3][0] = CubeCreator.Style.TOP_ROW;
        styles[3][1] = CubeCreator.Style.BOTTOM_ROW;
        styles[3][2] = CubeCreator.Style.LEFT_ROW;
        styles[3][3] = CubeCreator.Style.RIGHT_ROW;

        double x,y,z;
        CubeCreator cg = null;

        float max_dim = (float) Math.max(Math.max(grids[0].getWidth() * grids[0].getVoxelSize(), grids[0].getHeight()) * grids[0].getSliceHeight(), grids[0].getDepth() * grids[0].getVoxelSize());

        double boxSize = 0.008;
        int size = (int) Math.floor(max_dim / boxSize / 2.0);


        cg = new CubeCreator(styles, boxSize, boxSize, boxSize,
                0,0,0,1);


        for(int n=0; n < grids.length; n++) {
            // warmup
            for(int i=0; i < WARMUP; i++) {
                setLinkedCubes(grids[n], cg, boxSize, size);
            }

            long stime = System.nanoTime();

            for(int i=0; i < times; i++) {
                setLinkedCubes(grids[n], cg, boxSize, size);
            }

            totalTime[n] = System.nanoTime() - stime;

            String name = grids[n].getClass().getSimpleName();

            if (display) {
                float tps = 1000000000f / totalTime[n];
                System.out.println(String.format("%1$-31s",name) + "        : " + String.format("%1$-13s",totalTime[n]) + " NS " + String.format("%1$-7s",formater.format(tps)) + " TPS " + " " + String.format("%1$-7s",formater.format((float)totalTime[n] / totalTime[0])) + "X");
            }
        }


        if (display) System.out.println();

    }

    public static void setLinkedCubes(Grid grid, CubeCreator cg, double boxSize, int size) {
        double exoffset = 0;
        double eyoffset = 0;
        double ezoffset = 0;
        double exspacer = boxSize / 4;
        double eyspacer = boxSize / 4;
        double ezspacer = -(boxSize + boxSize / 2) / 4;

        double oxoffset = boxSize / 2;
        double oyoffset = boxSize / 2;
        double ozoffset = 0.000;
        double oxspacer = boxSize / 4;
        double oyspacer = boxSize / 4;
        double ozspacer = -(boxSize + boxSize / 2) / 4;

        double xoffset;
        double yoffset;
        double zoffset;
        double xspacer;
        double yspacer;
        double zspacer;

        double x;
        double y;
        double z;

        for(int k=0; k < 2 * size - 1; k++) {
            if (k % 2 == 0) {
                xspacer = exspacer;
                yspacer = eyspacer;
                zspacer = ezspacer;
                xoffset = exoffset;
                yoffset = eyoffset;
                zoffset = ezoffset;

                z = zoffset + boxSize * (k+1) + zspacer * (k+1);
            } else {
                xspacer = oxspacer;
                yspacer = oyspacer;
                zspacer = ozspacer;
                xoffset = oxoffset;
                yoffset = oyoffset;
                zoffset = ozoffset;

                z = zoffset + boxSize * (k+1) + zspacer * (k+1);
            }

            int len;

            if (k % 2 == 0) {
                len = size;
            } else {
                len = size - 1;
            }

            for(int i=0; i < len; i ++) {
                for(int j=0; j < len; j++) {
                    if (i % 2 == 0) {
                        x = xoffset + boxSize * (j+1) + xspacer * (j+1);
                        y = yoffset + boxSize * (i+1) + yspacer * (i+1);
                    } else {
                        x = xoffset + boxSize * (j+1) + xspacer * (j+1);
                        y = yoffset + boxSize * (i+1) + yspacer * (i+1);
                    }

                    cg.setCenter(x,y,z);
                    cg.generate(grid);
                }
            }
        }

    }

    /**
     * Test the write speed of slice aligned traversal.
     */
    public static void writeAccessX(AttributeGrid[] grids, int times, boolean display) {
        if (display) System.out.println("WriteX Times:");

        long[] totalTime = new long[grids.length];

        for(int n=0; n < grids.length; n++) {
            // warmup
            for(int i=0; i < WARMUP; i++) {
                setX(grids[n], Grid.INSIDE, 8);
            }

            long stime = System.nanoTime();

            for(int i=0; i < times; i++) {
                setX(grids[n], Grid.INSIDE, 8);
            }

            totalTime[n] = System.nanoTime() - stime;

            String name = grids[n].getClass().getSimpleName();

            if (display) {
                System.out.println(String.format("%1$-30s",name) + "        : " + totalTime[n] + " " + formater.format((float)totalTime[n] / totalTime[0]) + "X");
            }
        }


        if (display) System.out.println();
    }

    /**
     * Test the write speed of slice unaligned traversal.
     */
/*
    public void testWriteAccessY() {
        System.out.println("WriteY Times:");

        // Test Method 1
        Grid grid = new ArrayAttributeGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            setY(grid, Grid.INSIDE, 8);
        }

        long stime = System.nanoTime();

        for(int i=0; i < TIMES; i++) {
            setY(grid, Grid.INSIDE, 8);
        }

        long totalTime1 = System.nanoTime() - stime;

        System.out.println("ArrayGrid        : " + totalTime1);

        // Test Method 2
        grid = new ArrayAttributeGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            setY(grid, Grid.INSIDE, 8);
        }

        stime = System.nanoTime();

        for(int i=0; i < TIMES; i++) {
            setY(grid, Grid.INSIDE, 8);
        }

        long totalTime2 = System.nanoTime() - stime;

        System.out.println("SliceGrid(Array) : " + totalTime2 + " " + formater.format((float)totalTime2 / totalTime1) + "X");

        // Test Method 2
        grid = new ArrayAttributeGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            setY(grid, Grid.INSIDE, 8);
        }

        stime = System.nanoTime();

        for(int i=0; i < TIMES; i++) {
            setY(grid, Grid.INSIDE, 8);
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
        Grid grid = new ArrayAttributeGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            setZ(grid, Grid.INSIDE, 8);
        }

        long stime = System.nanoTime();

        for(int i=0; i < TIMES; i++) {
            setZ(grid, Grid.INSIDE, 8);
        }

        long totalTime1 = System.nanoTime() - stime;

        System.out.println("ArrayGrid        : " + totalTime1);

        // Test Method 2
        grid = new ArrayAttributeGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            setZ(grid, Grid.INSIDE, 8);
        }

        stime = System.nanoTime();

        for(int i=0; i < TIMES; i++) {
            setZ(grid, Grid.INSIDE, 8);
        }

        long totalTime2 = System.nanoTime() - stime;

        System.out.println("SliceGrid(Array) : " + totalTime2 +
            " " + formater.format((float)totalTime2 / totalTime1) + "X");

        // Test Method 2
        grid = new ArrayAttributeGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            setZ(grid, Grid.INSIDE, 8);
        }

        stime = System.nanoTime();

        for(int i=0; i < TIMES; i++) {
            setZ(grid, Grid.INSIDE, 8);
        }

        long totalTime3 = System.nanoTime() - stime;

        System.out.println("SliceGrid(Map)   : " + totalTime3 +
            " " + formater.format((float)totalTime3 / totalTime1) + "X");

        System.out.println();
    }
*/

    /**
     * Write a random pattern to the grid
     *
     * @param grid
     * @param percent How much of the grid to fill
     */
    protected static void setRandom(Grid grid, float percent) {
        // Use a static seed for reproducibility
        Random r = new Random(42);

        int w = grid.getWidth();
        int h = grid.getHeight();
        int d = grid.getDepth();

        int num = Math.round(grid.getWidth() * grid.getHeight() * grid.getDepth() * percent);
        int cnt = 0;

        while(cnt < num) {
            int x = r.nextInt(w);
            int y = r.nextInt(h);
            int z = r.nextInt(d);

            if (grid.getState(x,y,z) != Grid.OUTSIDE) {
                continue;
            }

            cnt++;
            grid.setState(x,y,z,Grid.INSIDE);
        }
    }

    /**
     * Set a torus into the grid
     *
     * @param grid
     */
    protected static void setTorus(Grid grid, boolean solid) {

        float max_dim = (float) Math.max(Math.max(grid.getWidth() * grid.getVoxelSize(), grid.getHeight()) * grid.getSliceHeight(), grid.getDepth() * grid.getVoxelSize());

        float ir = max_dim / 8f;
        float or = max_dim / 4f;
        int facets = 64;
        byte outerMaterial = 1;
        byte innerMaterial = 2;

        TorusGenerator tg = new TorusGenerator(ir, or, facets, facets);
        GeometryData geom = new GeometryData();
        geom.geometryType = GeometryData.INDEXED_TRIANGLES;
        tg.generate(geom);

        double bounds = TriangleModelCreator.findMaxBounds(geom);
//System.out.println("geometry bounds: " + bounds);

        int bufferVoxel = 4;
        int size = (int) (2.0 * bounds / grid.getVoxelSize()) + bufferVoxel;
//System.out.println("grid voxels per side: " + size);

        double x = bounds + bufferVoxel/2 * grid.getVoxelSize();
        double y = x;
        double z = x;

        double rx = 0,ry = 1,rz = 0,rangle = 0;
//        double rx = 1,ry = 0,rz = 0,rangle = 1.57079633;

        TriangleModelCreator tmc = null;
        tmc = new TriangleModelCreator(geom, x, y, z,
                rx,ry,rz,rangle,innerMaterial,solid);

        tmc.generate(grid);
        //System.out.println("interior cnt: " + grid.findCount(VoxelClasses.INSIDE) + " solid: " + solid);
    }

    /**
     * Set all the X values of a grid.
     *
     * @param state The new state
     * @param mat The new material
     */
    protected static void setX(AttributeGrid grid, byte state, long mat) {

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
    protected static void setY(AttributeGrid grid, byte state, long mat) {

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
    protected static void setZ(AttributeGrid grid, byte state, long mat) {

        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        for(int z=0; z < depth; z++) {
            grid.setData(0,0,z, state, mat);
        }
    }

    protected static void readStyleXYZ(Grid grid) {
        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        VoxelData vd = grid.getVoxelData();
        long cnt = 0;

        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                for(int z=0; z < depth; z++) {
                    grid.getData(x,y,z,vd);

                    if (vd.getState() != Grid.OUTSIDE) {
                        cnt++;
                    }
                }
            }
        }
    }

    protected static void readStyleXZY(Grid grid) {
        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        VoxelData vd = grid.getVoxelData();
        long cnt = 0;

        for(int x=0; x < width; x++) {
            for(int z=0; z < depth; z++) {
                for(int y=0; y < height; y++) {
                    grid.getData(x,y,z,vd);
                }
            }
        }
    }

    protected static void readStyleYXZ(Grid grid) {
        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        VoxelData vd = grid.getVoxelData();
        long cnt = 0;

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    grid.getData(x,y,z,vd);
                }
            }
        }
    }

    protected static void readStyleYZX(Grid grid) {
        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        VoxelData vd = grid.getVoxelData();
        long cnt = 0;

        for(int y=0; y < height; y++) {
            for(int z=0; z < depth; z++) {
                for(int x=0; x < width; x++) {
                    grid.getData(x,y,z,vd);
                }
            }
        }
    }

    protected static void readStyleZXY(Grid grid) {
        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        VoxelData vd = grid.getVoxelData();
        long cnt = 0;

        for(int z=0; z < depth; z++) {
            for(int x=0; x < width; x++) {
                for(int y=0; y < height; y++) {
                    grid.getData(x,y,z,vd);
                }
            }
        }
    }

    protected static void readStyleZYX(Grid grid) {
        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        VoxelData vd = grid.getVoxelData();
        long cnt = 0;

        for(int z=0; z < depth; z++) {
            for(int y=0; y < height; y++) {
                for(int x=0; x < width; x++) {
                    grid.getData(x,y,z,vd);
                }
            }
        }
    }

    public static final void main(String[] args) {
        TestPerformanceGrid tester = new TestPerformanceGrid();
        tester.testWriteAccessTorusShellLarge();
        tester.testWriteAccessTorusSolidLarge();
        tester.testWriteAccessLinkedCubesLarge();
        tester.testWriteAccessRandom();
    }
}
