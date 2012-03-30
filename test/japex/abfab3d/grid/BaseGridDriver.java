package abfab3d.grid;

import abfab3d.geom.CubeCreator;
import abfab3d.geom.TriangleModelCreator;
import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;
import org.j3d.geom.GeometryData;
import org.j3d.geom.TorusGenerator;

import java.util.Random;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public abstract class BaseGridDriver extends JapexDriverBase {
    protected int CLEAR_MEMORY = 2;
    protected static final long MEMORY_UNITS = 1;

    protected float randomPercent = 0.16f;
    protected Grid grid;
    protected long startMemory;

    public abstract void allocate(TestCase testCase);

    public void prepare(TestCase testCase) {
        if (testCase.getParam("type").equals("memory")) {
            // Work harder to clear memory for memory tests
            CLEAR_MEMORY = 25;
        }
    }

    public void run(TestCase testCase) {
        clearMemory();

        startMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MEMORY_UNITS;
        System.out.println("\nFreeMemory: " + startMemory + " max: " +  Runtime.getRuntime().maxMemory() + " total: " +  Runtime.getRuntime().totalMemory());

        allocate(testCase);

        String input = testCase.getParam("input");

        if (input.equals("WriteRandom")) {
            writeRandom(grid, randomPercent);
        } else if (input.equals("WriteTorusShell")) {
            setTorus(grid, false);
        } else if (input.equals("WriteTorusSolid")) {
            setTorus(grid, true);
        } else if (input.equals("WriteLinkedCubes")) {
            setLinkedCubes(grid);
        } else if (input.equals("ReadRandom")) {
            readRandom(grid,randomPercent);
        }

        // Make a test cleanup its own memory allocation
        clearMemory();

    }

    public void finish(TestCase testCase) {
        clearMemory();  // Only count permanent storage costs

        if (testCase.getParam("type").equals("memory")) {
            long alloc = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MEMORY_UNITS;
            System.out.println("\n   free: " + Runtime.getRuntime().freeMemory() + " used: " + (alloc));
//            testCase.setLongParam("japex.resultValue",Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory());
            testCase.setLongParam("japex.resultValue",alloc );
        } else {
            super.finish(testCase);
        }

        grid = null;
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
            grid.setState(x,y,z,Grid.EXTERIOR);
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
                rx,ry,rz,rangle,outerMaterial,innerMaterial,solid);

        tmc.generate(grid);
        //System.out.println("interior cnt: " + grid.findCount(Grid.VoxelClasses.INTERIOR) + " solid: " + solid);
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
     * Write a random pattern to the grid
     *
     * @param grid
     * @param percent How much of the grid to fill
     */
    protected void readRandom(Grid grid, float percent) {
        // Use a static seed for reproducibility
        // Differnt from read seed to mix it up
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
            
            if (state == Grid.EXTERIOR) {
                ext_cnt++;     // force compiler to execute paths
            }
        }
    }
    
    protected void clearMemory() {
        for(int i=0; i < CLEAR_MEMORY; i++) {
            System.gc();
            try {
                Thread.sleep(25);
            } catch(Exception e)  {}
        }            
        
    }
}
