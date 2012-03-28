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

// Internal Imports

/**
 * Tests the performance of grids.  Confirms the relative design
 * points of tradeoffs between access speed and memory.
 *
 * @author Alan Hudson
 * @version
 */
public class TestPerformanceGrid extends BaseTestAttributeGrid {
    public static final int SIZE = 16;

    /** How many times to run a test to compare with itself */
    public static final int RUNS = 1;

    public static final int SMALL_SIZE = 32;
    public static final int LARGE_SIZE = 128;

    public static final int TIMES = 10;
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
                new ArrayAttributeGridByte(size,size,size,voxel_size, slice_height),
                new ArrayAttributeGridByteIndexLong(size,size,size,voxel_size, slice_height),
//                new ArrayAttributeGridShort(size,size,size,voxel_size, slice_height),
//                new ArrayAttributeGridInt(size,size,size,voxel_size, slice_height),
                new BlockBasedAttributeGridByte(size,size,size,voxel_size, slice_height),
                new OctreeAttributeGridByte(size,size,size,voxel_size, slice_height)
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
        AttributeGrid grid = new OctreeAttributeGridByte(size,size,size,0.001, 0.001);

        //setup
        setX(grid, Grid.EXTERIOR, 0);
        setY(grid, Grid.EXTERIOR, 0);
        setZ(grid, Grid.EXTERIOR, 0);

System.out.println("Finding exterior voxels");
        grid.findCount(Grid.VoxelClasses.EXTERIOR);

//        assertTrue("stop",1 == 0);
    }



    /**
     * Test the write speed of slice aligned traversal.
     */
    public void testWriteAccessXSmall() {

        createGrids(SMALL_SIZE);

        System.out.println("WriteAccess XSmall");
        for(int i=0; i < WARMUP2; i++) {
            writeAccessX(grids,SMALL_TIMES,false);
        }
        for(int i=0; i < RUNS; i++) {
            writeAccessX(grids,SMALL_TIMES,true);
        }
    }

    /**
     * Test the write speed of slice aligned traversal.
     */
    public void testWriteAccessXLarge() {
        createGrids(LARGE_SIZE);

        System.out.println("WriteAccess Large");
        for(int i=0; i < WARMUP2; i++) {
            writeAccessX(grids,LARGE_TIMES,false);
        }
        for(int i=0; i < RUNS; i++) {
            writeAccessX(grids,LARGE_TIMES,true);
        }
    }

    /**
     * Test the write speed of a solid object with just exteriors
     */
    public void testWriteAccessTorusShellLarge() {
        createGrids(LARGE_SIZE);

        //System.out.println("WriteAccessTorusShell Large");
        for(int i=0; i < WARMUP2; i++) {
            writeAccessTorusShell(grids, LARGE_TIMES, false, false);
        }
        for(int i=0; i < RUNS; i++) {
            writeAccessTorusShell(grids, LARGE_TIMES, false, true);
        }
    }

    /**
     * Test the write speed of a solid object with interiors
     */
    public void testWriteAccessTorusSolidLarge() {
        createGrids(LARGE_SIZE);

        int div = 8;    // Solid is much slower then other things
        //System.out.println("WriteAccessTorusSolid Large");
        for(int i=0; i < WARMUP2; i++) {
            writeAccessTorusShell(grids, LARGE_TIMES / div, true, false);
        }
        for(int i=0; i < RUNS; i++) {
            writeAccessTorusShell(grids, LARGE_TIMES / div, true, true);
        }
    }

    /**
     * Test the write speed of sparse object.  In theory will give block based structures a workout.
     */
    public void testWriteAccessLinkedCubesLarge() {
        createGrids(LARGE_SIZE);

        //System.out.println("WriteAccessTorusSolid Large");
        for(int i=0; i < WARMUP2; i++) {
            writeAccessLinkedCubes(grids, LARGE_TIMES, false);
        }

        for(int i=0; i < RUNS; i++) {
            writeAccessLinkedCubes(grids, LARGE_TIMES, true);
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

        for(int i=0; i < RUNS; i++) {
            readAccessState(SMALL_SIZE,SMALL_TIMES,true);
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
        for(int i=0; i < RUNS; i++) {
            readAccessState(LARGE_SIZE,LARGE_TIMES,true);
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

        long stime = System.nanoTime();

        for(int i=0; i < times; i++) {
            readStyleYXZ(grid);
        }

        long totalTime1 = System.nanoTime() - stime;

        if (display) System.out.println("Style YXZ        : " + totalTime1);

        // Test Method 1
        grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        // warmup
        for(int i=0; i < WARMUP; i++) {
            readStyleXYZ(grid);
        }

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
        grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

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
        grid = new OctreeAttributeGridByte(size,size,size,0.001, 0.001);


        //setup
        setX(grid, Grid.EXTERIOR, 0);
        setY(grid, Grid.EXTERIOR, 0);
        setZ(grid, Grid.EXTERIOR, 0);

        //System.out.println("Cell Count: " + ((OctreeAttributeGridByte)grid).getCellCount() + " verses: " + (size*size*size));

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
                System.out.println(String.format("%1$-25s",name) + "        : " + String.format("%1$-13s",totalTime[n]) + " NS " + String.format("%1$-7s",formater.format(tps)) + " TPS " + " " + String.format("%1$-7s",formater.format((float)totalTime[n] / totalTime[0])) + "X");
            }
        }


        if (display) System.out.println();

    }

    public static void writeAccessLinkedCubes(Grid[] grids, int times, boolean display) {
        if (display) System.out.println("WriteAccessLinkedCubed Times:");

        long[] totalTime = new long[grids.length];

        for(int n=0; n < grids.length; n++) {
            // warmup
            for(int i=0; i < WARMUP; i++) {
                setLinkedCubes(grids[n]);
            }

            long stime = System.nanoTime();

            for(int i=0; i < times; i++) {
                setLinkedCubes(grids[n]);
            }

            totalTime[n] = System.nanoTime() - stime;

            String name = grids[n].getClass().getSimpleName();

            if (display) {
                float tps = 1000000000f / totalTime[n];
                System.out.println(String.format("%1$-25s",name) + "        : " + String.format("%1$-13s",totalTime[n]) + " NS " + String.format("%1$-7s",formater.format(tps)) + " TPS " + " " + String.format("%1$-7s",formater.format((float)totalTime[n] / totalTime[0])) + "X");
            }
        }


        if (display) System.out.println();

    }

    public static void setLinkedCubes(Grid grid) {
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

        float max_dim = (float) Math.max(Math.max(grid.getWidth() * grid.getVoxelSize(), grid.getHeight()) * grid.getSliceHeight(), grid.getDepth() * grid.getVoxelSize());

        double boxSize = 0.008;
        int size = (int) Math.floor(max_dim / boxSize / 2.0);

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

                    cg = new CubeCreator(styles, boxSize, boxSize, boxSize,
                            x,y,z,1);

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
                setX(grids[n], Grid.EXTERIOR, 8);
            }

            long stime = System.nanoTime();

            for(int i=0; i < times; i++) {
                setX(grids[n], Grid.EXTERIOR, 8);
            }

            totalTime[n] = System.nanoTime() - stime;

            String name = grids[n].getClass().getSimpleName();

            if (display) {
                System.out.println(String.format("%1$-25s",name) + "        : " + totalTime[n] + " " + formater.format((float)totalTime[n] / totalTime[0]) + "X");
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
            setY(grid, Grid.EXTERIOR, 8);
        }

        long stime = System.nanoTime();

        for(int i=0; i < TIMES; i++) {
            setY(grid, Grid.EXTERIOR, 8);
        }

        long totalTime1 = System.nanoTime() - stime;

        System.out.println("ArrayGrid        : " + totalTime1);

        // Test Method 2
        grid = new ArrayAttributeGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

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
        grid = new ArrayAttributeGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

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
        Grid grid = new ArrayAttributeGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

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
        grid = new ArrayAttributeGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

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
        grid = new ArrayAttributeGridByte(SIZE,SIZE,SIZE,0.001, 0.001);

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
                rx,ry,rz,rangle,outerMaterial,innerMaterial,solid);

        tmc.generate(grid);
        //System.out.println("interior cnt: " + grid.findCount(Grid.VoxelClasses.INTERIOR) + " solid: " + solid);
    }

    /**
     * Set all the X values of a grid.
     *
     * @param state The new state
     * @param mat The new material
     */
    protected static void setX(AttributeGrid grid, byte state, int mat) {

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
    protected static void setY(AttributeGrid grid, byte state, int mat) {

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
    protected static void setZ(AttributeGrid grid, byte state, int mat) {

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

        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                for(int z=0; z < depth; z++) {
                    VoxelData vd = grid.getData(x,y,z);
                }
            }
        }
    }

    protected static void readStyleXZY(Grid grid) {
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

    protected static void readStyleYXZ(Grid grid) {
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

    protected static void readStyleYZX(Grid grid) {
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

    protected static void readStyleZXY(Grid grid) {
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

    protected static void readStyleZYX(Grid grid) {
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

    public static final void main(String[] args) {
        TestPerformanceGrid tester = new TestPerformanceGrid();
        tester.testWriteAccessTorusShellLarge();
        tester.testWriteAccessTorusSolidLarge();
        tester.testWriteAccessLinkedCubesLarge();
    }
}
