package abfab3d.grid;

import abfab3d.geom.CubeCreator;
import abfab3d.geom.TriangleModelCreator;
import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;
import org.j3d.geom.GeometryData;
import org.j3d.geom.TorusGenerator;

import java.util.Random;

/**
 * Base Japex setup for grid testing.
 *
 * @author Alan Hudson
 */
public abstract class BaseGridDriver extends JapexDriverBase {
    double voxel_size = 0.001;
    double slice_height = 0.001;

    protected int CLEAR_MEMORY = 2;
    protected static final long MEMORY_UNITS = (long) 1e3;    // Kilobytes

    protected float randomPercent = 0.16f;
    protected Grid grid;
    protected Grid torusGridShell;
    protected Grid torusGridSolid;

    protected long startMemory;
    protected long alloc;

    public abstract void allocate(TestCase testCase);

    public void prepare(TestCase testCase) {
        //System.out.println("Prepare FreeMemory: " + Runtime.getRuntime().freeMemory() + " max: " +  Runtime.getRuntime().maxMemory() + " total: " +  Runtime.getRuntime().totalMemory());

//        System.out.println("prepare");

        if (testCase.getParam("type").equals("memory")) {
            // Work harder to clear memory for memory tests
            CLEAR_MEMORY = 25;
        }

        String input = testCase.getParam("input");

        // prepare torus grid.  Avoid TriangleModelCreator during runtime as its really expensive
        int w = Integer.parseInt(testCase.getParam("width"));
        int h = Integer.parseInt(testCase.getParam("height"));
        int d = Integer.parseInt(testCase.getParam("depth"));

        //System.out.println("\nPre FreeMem: " + Runtime.getRuntime().freeMemory() + " max: " +  Runtime.getRuntime().maxMemory() + " tot: " +  Runtime.getRuntime().totalMemory() + " alc: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

        if (!testCase.getParam("type").equals("memory")) {
            torusGridShell =  new ArrayGridByte(w,h,d,voxel_size, slice_height);
            writeTorus(torusGridShell, false);
            torusGridSolid =  new ArrayGridByte(w,h,d,voxel_size, slice_height);
            writeTorus(torusGridSolid, true);
        }

        if (input.equals("ReadRandom")) {
            allocate(testCase);
            writeRandom(grid, randomPercent);
        } else if (input.equals("ReadExterior")) {
            allocate(testCase);
            writeTorus(grid, true);
        } else if (input.equals("ReadInterior")) {
            allocate(testCase);
            writeTorus(grid, true);
        } else if (input.equals("ReadXZY")) {
            allocate(testCase);
            writeTorus(grid, true);
        } else if (input.equals("ReadYXZ")) {
            allocate(testCase);
            writeTorus(grid, true);
        }
    }

    public void run(TestCase testCase) {
//System.out.println("run");
        int times = 1;

        clearMemory();

        startMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MEMORY_UNITS;
        //System.out.println("\nFreeMemory: " + startMemory + " max: " +  Runtime.getRuntime().maxMemory() + " total: " +  Runtime.getRuntime().totalMemory());


        String input = testCase.getParam("input");


        if (input.equals("WriteRandom")) {
            allocate(testCase);

            for (int i=0; i < times; i++) {
                writeRandom(grid, randomPercent);
            }
        } else if (input.equals("WriteTorusShell")) {
            allocate(testCase);
            for (int i=0; i < times; i++) {
                if (torusGridShell != null) {
                    copyGrid(torusGridShell, grid);
                } else {
                    writeTorus(grid, false);
                }
            }
        } else if (input.equals("WriteTorusSolid")) {
            allocate(testCase);

            for (int i=0; i < times; i++) {
                if (torusGridSolid != null) {
                    copyGrid(torusGridSolid, grid);
                } else {
                    writeTorus(grid, true);
                }
            }
        } else if (input.equals("WriteLinkedCubes")) {
            allocate(testCase);

            for (int i=0; i < times; i++) {
                writeLinkedCubes(grid);
            }
        } else if (input.equals("ReadRandom")) {
            for (int i=0; i < times; i++) {
                readRandom(grid,randomPercent);
            }
        } else if (input.equals("ReadExterior")) {
            for (int i=0; i < times; i++) {
                readExterior(grid);
            }
        } else if (input.equals("ReadInterior")) {
            for (int i=0; i < times; i++) {
                readInterior(grid);
            }
        } else if (input.equals("ReadXZY")) {
            for (int i=0; i < times; i++) {
                readStyleXZY(grid);
            }
        } else if (input.equals("ReadYXZ")) {
            for (int i=0; i < times; i++) {
                readStyleYXZ(grid);
            }
        }  else {
            System.out.println("Unhandled input: " + input);
        }

        // Make a test cleanup its own memory allocation
        clearMemory();

        if (testCase.getParam("type").equals("memory")) {
            alloc = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MEMORY_UNITS;
            grid = null;
        }
    }

    public void finish(TestCase testCase) {
//System.out.println("finish");
        clearMemory();  // Only count permanent storage costs

        if (testCase.getParam("type").equals("memory")) {
/*
            long alloc = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MEMORY_UNITS;
            System.out.println("\nFreeMemory: " + Runtime.getRuntime().freeMemory() + " max: " +  Runtime.getRuntime().maxMemory() + " total: " +  Runtime.getRuntime().totalMemory());
            System.out.println("\n   free: " + Runtime.getRuntime().freeMemory() + " used: " + (alloc));
//            testCase.setLongParam("japex.resultValue",Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory());
*/
            testCase.setLongParam("japex.resultValue",alloc);
        }
        clearMemory();
    }

    /**
     * Write a random pattern to the grid
     *
     * @param grid
     * @param percent How much of the grid to fill
     */
    protected void writeRandom(Grid grid, float percent) {
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

            cnt++;
            grid.setState(x,y,z,Grid.INSIDE);
        }
    }

    /**
     * Set a torus into the grid
     *
     * @param grid
     */
    protected void writeTorus(Grid grid, boolean solid) {

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

    public static void writeLinkedCubes(Grid grid) {
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

        double boxSize = 0.004;
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
     * Read a random pattern from the grid
     *
     * @param grid
     * @param percent How much of the grid to read
     */
    protected void readRandom(Grid grid, float percent) {
        // Use a static seed for reproducibility
        // Different from read seed to mix it up
        Random r = new Random(17);

        int w = grid.getWidth();
        int h = grid.getHeight();
        int d = grid.getDepth();

        int num = Math.round(grid.getWidth() * grid.getHeight() * grid.getDepth() * percent);
        int cnt = 0;
        int ext_cnt = 0;

        while(cnt < num) {
            int x = r.nextInt(w);
            int y = r.nextInt(h);
            int z = r.nextInt(d);

            cnt++;
            byte state = grid.getState(x,y,z);

            if (state == Grid.INSIDE) {
                ext_cnt++;     // force compiler to execute paths
            }
        }
    }

    /**
     * Read all exterior voxels
     *
     * @param grid
     */
    protected void readExterior(Grid grid) {
        grid.find(VoxelClasses.INSIDE, new CountTraverser());
    }

    /**
     * Read all exterior voxels
     *
     * @param grid
     */
    protected void readInterior(Grid grid) {
        grid.find(VoxelClasses.INSIDE, new CountTraverser());
    }

    protected void clearMemory() {
        for(int i=0; i < CLEAR_MEMORY; i++) {
            System.gc();
            try {
                Thread.sleep(25);
            } catch(Exception e)  {}
        }

    }

    /**
     * Read from a grid using y,x,z axis order.  Likely fast path.
     * @param grid
     */
    protected void readStyleYXZ(Grid grid) {
        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        int cnt = 0;

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    byte state = grid.getState(x,y,z);

                    if (state == Grid.INSIDE) {
                        cnt++;
                    }
                }
            }
        }
    }

    /*
     * Read from a grid using x,z,y axis order.  Likely slower path.
     */
    protected void readStyleXZY(Grid grid) {
        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        int cnt = 0;

        for(int x=0; x < width; x++) {
            for(int z=0; z < depth; z++) {
                for(int y=0; y < height; y++) {
                    byte state = grid.getState(x,y,z);

                    if (state == Grid.INSIDE) {
                        cnt++;
                    }
                }
            }
        }
    }

    private void copyGrid(Grid src, Grid dest) {
        int w = src.getWidth();
        int h = src.getHeight();
        int d = src.getDepth();

        for(int y=0; y < h; y++) {
            for(int x=0; x < w; x++) {
                for(int z=0; z < d; z++) {
                    dest.setState(x,y,z,src.getState(x,y,z));
                }
            }
        }
    }
}

class CountTraverser implements ClassTraverser {
    private long cnt;

    @Override
    public void found(int x, int y, int z, byte state) {
        cnt++;
    }

    @Override
    public boolean foundInterruptible(int x, int y, int z, byte state) {
        cnt++;

        return true;
    }

    public long getCnt() {
        return cnt;
    }
}