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
 * Tests the performance of grid operations.
 *
 * @author Tony Wong
 * @version
 */
public class TestOpPerformance extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestOpPerformance.class);
    }

    /**
     * Test that multithreaded performance improves as you add threads.
     */
    public void testSubtractPerformanceMT() {
        int size1 = 500;
        int size2 = 550;
        byte state1 = Grid.INSIDE;
        byte state2 = Grid.INSIDE;

        int warmup = 4;
        int cores = Runtime.getRuntime().availableProcessors();;
        long[] times = new long[cores];
        double[] factors = new double[cores];


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
            if (i >= 1) {
                factors[i] = (float) times[0] / times[i];
            }
            System.out.println("Time: " + (System.currentTimeMillis() - t0));
        }
        System.out.println("Times: " + java.util.Arrays.toString(times));
        System.out.println("Factors: " + java.util.Arrays.toString(factors));
        double factor = cores / 2.0 * 0.8;

        boolean found = false;
        for(int i=0; i < times.length; i++) {
            if (factors[i] >= factor) {
                found = true;
                break;
            }
        }
        assertTrue("Speed check - goal factor: " + factor,found);
    }
    public void testIntersectPerformanceMT() {
        int size1 = 750;
        int size2 = 750;
        byte state1 = Grid.INSIDE;
        byte state2 = Grid.INSIDE;

        int warmup = 4;
        int cores = Runtime.getRuntime().availableProcessors();;
        long[] times = new long[cores];
        float[] factors = new float[cores];


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
            IntersectMT op = new IntersectMT(grid1, (n+1) % cores);
            Grid dest = (Grid) op.execute(grid2);
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
            IntersectMT op = new IntersectMT(grid1, i+1);
            Grid dest = (Grid) op.execute(grid2);
            times[i] = (System.currentTimeMillis() - t0);
            if (i >= 1) {
                factors[i] = (float) times[0] / times[i];
            }
        }
        System.out.println("Times: " + java.util.Arrays.toString(times));
        System.out.println("Factors: " + java.util.Arrays.toString(factors));
        // Spot check, make sure 4 threads is better then 1 thread by at least 2X
        // Hyperthreads lie so only assume we should get 90% of threads / 2 improvements
        double factor = cores / 2.0 * 0.8;

        boolean found = false;
        for(int i=0; i < times.length; i++) {
            if (factors[i] >= factor) {
                found = true;
                break;
            }
        }
        assertTrue("Speed check - goal factor: " + factor,found);
    }

}
