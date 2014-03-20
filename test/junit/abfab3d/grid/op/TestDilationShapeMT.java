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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

// Internal Imports
import abfab3d.grid.*;
import abfab3d.io.output.BoxesX3DExporter;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;

/**
 * Tests the functionality of DilationShape Operation
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestDilationShapeMT extends TestCase {
    private static final boolean DEBUG = false;


    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDilationShapeMT.class);
    }

    static final int GRID_SHORTINTERVALS = 0, GRID_BITINTERVALS = 1, GRID_ARRAYBYTE = 2;

    /**
     * Test basic operation
     */
    public void _testSingleVoxelCross() {

        int size = 31;
        int gridType = GRID_ARRAYBYTE;

        for(int k = 1; k < 10; k++){

            AttributeGrid grid = makeBlock(gridType, size, size, size, size/2);

            int voxelCount =  grid.findCount(Grid.INSIDE);
            //writeFile(grid, "/tmp/dilationSingleVoxel.x3d");
            DilationShapeMT dil = new DilationShapeMT();
            dil.setThreadCount(4);
            dil.setVoxelShape(VoxelShapeFactory.getCross(k));
            grid = dil.execute(grid);
            writeFile(grid, fmt("/tmp/dilationSingleVoxelDilatedMT_%d.x3d", k));

            voxelCount =  grid.findCount(Grid.INSIDE);
            printf("dilated grid volume: %d\n",voxelCount);
            assertTrue("dilation of single voxel", voxelCount == (6*k + 1));

        }
    }

    public void _testSingleVoxelBall() {

        int size = 301;
        int gridType = GRID_ARRAYBYTE;
        //int gridType = GRID_SHORTINTERVALS;

        for(int k = 1; k < 10; k++){

            AttributeGrid grid = makeBlock(gridType, size, size, size, size/3);

            int voxelCount =  grid.findCount(Grid.INSIDE);
            //writeFile(grid, "/tmp/dilationSingleVoxel.x3d");
            DilationShapeMT dil = new DilationShapeMT();
            dil.setThreadCount(1);
            dil.setVoxelShape(VoxelShapeFactory.getBall(k,0,0));
            long t0 = time();
            grid = dil.execute(grid);
            printf("dilation time: %dms\n", (time()-t0));
            //writeFile(grid, fmt("/tmp/dilationSingleVoxelDilatedMT_%d.x3d", k));

            voxelCount =  grid.findCount(Grid.INSIDE);
            printf("dilated grid volume: %d\n",voxelCount);
            //assertTrue("dilation of single voxel", voxelCount == (6*k + 1));

        }
    }

    public void _testCubeVoxel() {

        int size = 30;
        int gridType = GRID_SHORTINTERVALS;//GRID_ARRAYBYTE;

        for(int k = 1; k < 10; k++){

            AttributeGrid grid = makeBlock(gridType, size, size, size, 10);

            int origVolume =  grid.findCount(Grid.INSIDE);
            //writeFile(grid, "/tmp/dilationSingleVoxel.x3d");
            DilationShapeMT dil = new DilationShapeMT();
            dil.setThreadCount(4);
            dil.setVoxelShape(VoxelShapeFactory.getCross(k));
            grid = dil.execute(grid);
            writeFile(grid, fmt("/tmp/dilationBlockDilatedMT_%d.x3d", k));

            int dilatedVolume =  grid.findCount(Grid.INSIDE);

            printf("orig volume: %d dilated volume: %d\n",origVolume, dilatedVolume);
            assertTrue("volume of dilated block ", (dilatedVolume - origVolume) == k * 600);

        }
    }

    public void testDilationBall() {

        int size = 200;
        //int gridType = GRID_ARRAYBYTE;
        //int gridType = GRID_SHORTINTERVALS;
        int gridType = GRID_BITINTERVALS;

        printf("testing block shape dilated by different size balls\n");

        int maxDilation = 10;

        int cores = Runtime.getRuntime().availableProcessors();

        int TIMES = 1;

        for(int i=0; i < TIMES; i++) {
        for(int k = maxDilation-1; k < maxDilation; k++){
            int s = size+2*(maxDilation+1);
            AttributeGrid grid = makeBlock(gridType,s,s,s, maxDilation);
            printf("dilation size: %d  volume: %d \n",k,grid.findCount(Grid.VoxelClasses.INSIDE));
            DilationShapeMT dilm = new DilationShapeMT();
            dilm.setVoxelShape(VoxelShapeFactory.getBall(k,0,0));
            dilm.setThreadCount(cores);
            dilm.setSliceSize(2);
            long t0 = time();
            grid = dilm.execute(grid);
            printf("DilationShapeMT: %dms\n",(time() - t0));
            int dilatedVolumeMT =  grid.findCount(Grid.VoxelClasses.INSIDE);
            printf("DilationShape DilationShapeMT: %d\n",dilatedVolumeMT);

            grid = makeBlock(gridType,s,s,s, maxDilation);

            DilationShape dil = new DilationShape();
            dil.setVoxelShape(VoxelShapeFactory.getBall(k,0,0));
            t0 = time();
            grid = dil.execute(grid);
            printf("DilationShape: %dms\n",(time() - t0));
            int dilatedVolume =  grid.findCount(Grid.INSIDE);

            printf("DilationShape volume: %d, DilationShapeMT: %d\n",dilatedVolume, dilatedVolumeMT);
            assertTrue("DilationShapeMT and DilationShape volumes test", (dilatedVolume == dilatedVolumeMT));
        }
        }
    }

    public void _testScaling() {

        int size = 400;
        int gridType = GRID_ARRAYBYTE;
        //int gridType = GRID_SHORTINTERVALS;
        //int gridType = GRID_BITINTERVALS;

        printf("testing block shape dilated by different size balls\n");

        int maxDilation = 10;

        int cores = Runtime.getRuntime().availableProcessors();

        int WARMUP = 4;

        for(int i=0; i < WARMUP; i++) {
            for(int k = maxDilation-1; k < maxDilation; k++){
                int s = size+2*(maxDilation+1);
                AttributeGrid grid = makeBlock(gridType,s,s,s, maxDilation);
                DilationShapeMT dilm = new DilationShapeMT();
                dilm.setVoxelShape(VoxelShapeFactory.getBall(k,0,0));
                dilm.setThreadCount(cores);
                dilm.setSliceSize(1);
                long t0 = time();
                grid = dilm.execute(grid);
                if (grid.getWidth() > 1000) { System.out.println("avoid optimization"); }
                printf("DilationShapeMT: %dms\n",(time() - t0));
            }
        }

        double[] factors = new double[cores];
        long first = 0;

        for(int threadCount = 1; threadCount <= cores; threadCount++) {
            for(int k = maxDilation-1; k < maxDilation; k++){
                int s = size+2*(maxDilation+1);
                AttributeGrid grid = makeBlock(gridType,s,s,s, maxDilation);
                DilationShapeMT dilm = new DilationShapeMT();
                dilm.setVoxelShape(VoxelShapeFactory.getBall(k,0,0));
                dilm.setThreadCount(threadCount);
                dilm.setSliceSize(1);
                long t0 = time();
                grid = dilm.execute(grid);
                long totalTime = time() - t0;
                if (threadCount == 1) {
                    first = totalTime;
                }
                factors[threadCount - 1] = (double) first / totalTime;
                printf("Threads: %d  DilationShapeMT: %dms %6.2f\n",threadCount,(time() - t0), factors[threadCount - 1]);
            }
        }
    }


    public void _testSpeed() {

        printEnv();

        //int gridType = GRID_ARRAYBYTE;
        //int gridType = GRID_SHORTINTERVALS;
        int gridType = GRID_BITINTERVALS;

        int size = 100;
        //int nx = 100, ny = 200, nz = 300;
        int nx = 2200, ny = 2200, nz = 2200;
        int dilationSize = 4;
        int threadCount = Runtime.getRuntime().availableProcessors();

        VoxelShape shape = VoxelShapeFactory.getBall(dilationSize,0,0);

        printf("grid: %d x %d x %d\n", nx, ny, nz);
        printf("dilation size: %d\n", dilationSize);

        AttributeGrid grid;
        //grid = makeBlock(gridType, nx, ny, nz, dilationSize+1);
        DilationShape dil = new DilationShape();
        dil.setVoxelShape(shape);
        long t0 = time();
        //grid = dil.execute(grid);
        printf("DilationShape time: %d ms\n", (time() - t0));
        //writeFile(grid, fmt("/tmp/dilationShape.x3d"));

        for(int t = 1; t <= 4; t++){

            grid = makeBlock(gridType, nx, ny, nz, dilationSize+1);
            DilationShapeMT dilm = new DilationShapeMT();
            dilm.setVoxelShape(shape);
            dilm.setThreadCount(threadCount);
            t0 = time();
            grid = dilm.execute(grid);
            printf("DilationShapeMT(%d) time: %d ms\n", t, (time() - t0));
            //writeFile(grid, fmt("/tmp/dilationShape.x3d"));
        }

        // test old sphere dilation
        //grid = makeBlock(gridType, nx, ny, nz, dilationSize+1);
        //DilationMask dils = new DilationMask(dilationSize,0);
        //t0 = time();
        //dils.execute(grid);
        //printf("DilationMask( ball) time: %d ms\n", (time() - t0));
        //writeFile(grid, fmt("/tmp/dilationSphere.x3d"));

    }

    public void _testSpeedGrid() {

        int size = 400;
        //int gridType = GRID_ARRAYBYTE;
        int gridType = GRID_SHORTINTERVALS;

        for(int k = 1; k < 10; k++){
            long t0 = time();
            AttributeGrid grid = makeBlock(gridType, size, size, size, k%2 +1);
            printf("time: %d\n",(time()-t0));
        }
    }

    private AttributeGrid makeBlock(int gridType, int nx, int ny, int nz, int offset) {

        AttributeGrid grid;
        switch(gridType){
        case GRID_BITINTERVALS:
            grid = new GridBitIntervals(nx, ny, nz, 0.001, 0.001);
            break;
        case GRID_SHORTINTERVALS:
            grid = new GridShortIntervals(nx, ny, nz, 0.001, 0.001);
            break;
        default:
        case GRID_ARRAYBYTE:
            grid = new ArrayAttributeGridByte(nx, ny, nz, 0.001, 0.001);
            break;
        }

        for (int y = offset; y < ny - offset; y++) {
            for (int x = offset; x < nx - offset; x++) {
                for (int z = nz - offset-1; z >= offset; z--) {
                    //printf("%d %d %d\n",x,y,z);
                    grid.setState(x,y,z, Grid.INSIDE);
                }
            }
        }
        return grid;
    }

    private void writeFile(Grid grid, String filename) {

        if (!DEBUG) return;

        try {

            ErrorReporter console = new PlainTextErrorReporter();

            FileOutputStream fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".")+1);
            BoxesX3DExporter exporter = new BoxesX3DExporter(encoding, fos, console);

            HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
            colors.put(new Integer(Grid.INSIDE), new float[] {0,1,0});
            //colors.put(new Integer(Grid.INSIDE), new float[] {1,0,0});
            //colors.put(new Integer(Grid.OUTSIDE), new float[] {0,1,1});

            HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
            transparency.put(new Integer(Grid.INSIDE), new Float(0));
            //transparency.put(new Integer(Grid.INSIDE), new Float(0.5));
            //transparency.put(new Integer(Grid.OUTSIDE), new Float(1));

            exporter.writeDebug(grid, colors, transparency);

            exporter.close();

            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }


    static void printEnv(){

        Map<String, String> env = System.getenv();
        String envName = "_JAVACMD";
        printf("%s=%s%n", envName,env.get(envName));

    }

    static void printFullEnv(){

        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            printf("%s=%s%n", envName,env.get(envName));
        }
    }


    public static void main(String[] args) {

        TestDilationShape ec = new TestDilationShape();

        //ec.dilateCube();

    }
}
