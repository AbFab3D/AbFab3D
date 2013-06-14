/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
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
import junit.framework.TestSuite;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// Internal Imports

/**
 * Tests the functionality of CanMoveMaterial Query.
 *
 * @author Alan Hudson
 */
public class TestTraversalPerformance extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTraversalPerformance.class);
    }

    /**
     * Test traversal across the same grid with multiple threads.  Should in theory be perfectly cached aligned.
     * This shows how much overhead there is to having multiple cores running
     */
    public void _testSameGridTraversal() {

        int size = 3 * 5 * 7 * 8;  // make dividable by 1,2,3,4,5,6,7,8 cores

        Grid grid = new ArrayGridByte(size, size, size, 0.001, 0.001);

        int width = 8;
        // set the voxels of a square
        for (int i = 0; i < width; i++) {
            // set the voxels of a square
            setX(grid, 50 + i, 40, Grid.INSIDE, 1, 40, 450);
            setX(grid, 50 + i, 60, Grid.INSIDE, 1, 40, 450);
            setZ(grid, 40, 50 + i, Grid.INSIDE, 1, 40, 60);
            setZ(grid, 450, 50 + i, Grid.INSIDE, 1, 40, 60);
        }

        //saveDebug(grid,"/tmp/out.x3db",false);

        int warmup = 4;
        int expected_count = 6880;
        long start_time;

        for (int i = 0; i < warmup; i++) {
            start_time = System.currentTimeMillis();
            long cnt = getNumMarked(grid, 0, grid.getWidth() - 1, 0, grid.getHeight() - 1);
            System.out.println("Time: " + (System.currentTimeMillis() - start_time));

            assertEquals("Count", expected_count, cnt);
        }


        System.out.println("Warmup done");
        int maxThreads = Runtime.getRuntime().availableProcessors();
        int loops = 1;
        long first = 0;
        double[] factors = new double[maxThreads];

        for (int threadCount = 1; threadCount <= maxThreads; threadCount++) {
            long stime = System.currentTimeMillis();

            Slice[] slices = new Slice[loops];

            for (int i = 0; i < loops; i++) {
                Slice slice = new Slice(0, grid.getWidth() - 1, 0, grid.getHeight() - 1);
                slices[i] = slice;
            }

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            SliceRunner[] runners = new SliceRunner[threadCount];

            AtomicInteger idx = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {

                runners[i] = new SliceRunner(grid, slices, idx);
                executor.submit(runners[i]);
            }
            executor.shutdown();

            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long total = 0;
            for (int i = 0; i < threadCount; i++) {
                total += runners[i].getTotal();
            }

            long totalTime = System.currentTimeMillis() - stime;

            if (threadCount == 1) {
                first = totalTime;
            }
            factors[threadCount - 1] = (double) first / totalTime;
            System.out.println("Threads: " + threadCount + " time: " + totalTime + " overhead: " + (factors[threadCount - 1]));

        }
        /*
        double min_factor = 1.5;

        for (int i = 1; i < factors.length; i++) {
            assertTrue("Non Linear Scaling: threads: " + i + " scale: " + factors[i], factors[i] >= ((i - 1) * min_factor));
        }
        */

    }

    /**
     * Test traversal across a grid using multiple threads
     */
    public void _testMTTraversal() {

        long voxels = 1000 * 1000 * 1000;
        int size = 250;
        int sizez = (int) (voxels / (size * size));

        Grid grid = new ArrayGridByte(size, size, sizez, 0.001, 0.001);

        int expected_count = 6880;

        Random rnd = new Random(42);
        for (int i = 0; i < expected_count; i++) {
            int x = rnd.nextInt(size);
            int y = rnd.nextInt(size);
            int z = rnd.nextInt(sizez);

            if (grid.getState(x,y,z) != Grid.INSIDE) {
                grid.setState(x,y,z,Grid.INSIDE);
            }
        }

        //saveDebug(grid,"/tmp/out.x3db",false);

        int maxThreads = Runtime.getRuntime().availableProcessors();
        int warmup = 4;
        long start_time;

        for (int i = 0; i < warmup; i++) {
            start_time = System.nanoTime();
            long cnt = getNumMarked(grid, 0, grid.getWidth() - 1, 0, grid.getHeight() - 1);
            System.out.println("Time: " + ((int) ((System.nanoTime() - start_time) / 1000000)));

            assertEquals("Count", expected_count, cnt);
        }


        System.out.println("Warmup done");
        long first = 0;
        double[] factors = new double[maxThreads];
        boolean yslice = true;
        int fixed = -1;

        for (int threadCount = 1; threadCount <= maxThreads; threadCount++) {
            if (fixed > 0 && threadCount != 1 && threadCount != fixed) {
                continue;
            }

            int sliceHeight = 1;
            int ny = grid.getHeight();
            int nx = grid.getWidth();
            Slice[] slices = new Slice[ny / sliceHeight];

            int s_idx = 0;
            if (yslice) {
                for (int y = 0; y < ny; y += sliceHeight) {
                    int ymax = y + sliceHeight;
                    if (ymax > ny)
                        ymax = ny;

                    if (ymax > y) {
                        // non zero slice
                        slices[s_idx++] = new Slice(0, nx - 1, y, ymax - 1);
                    }
                }
            } else {
                for (int x = 0; x < nx; x += sliceHeight) {
                    int xmax = x + sliceHeight;
                    if (xmax > nx)
                        xmax = nx;

                    if (xmax > x) {
                        // non zero slice
                        slices[s_idx++] = new Slice(x, xmax - 1, 0, ny - 1);
                    }
                }

            }

            long stime = System.nanoTime();

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            SliceRunner[] runners = new SliceRunner[threadCount];

            AtomicInteger idx = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {

                runners[i] = new SliceRunner(grid, slices, idx);
                executor.submit(runners[i]);
            }
            executor.shutdown();

            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long total = 0;
            for (int i = 0; i < threadCount; i++) {
                total += runners[i].getTotal();
            }

            long totalTime = System.nanoTime() - stime;

            if (threadCount == 1) {
                first = totalTime;
            }
            factors[threadCount - 1] = (double) first / totalTime;
            System.out.println("Threads: " + threadCount + " time: " + ((int) (totalTime / 1000000)) + " scaling: " + (factors[threadCount - 1]));

        }
        /*
        double min_factor = 1.5;

        for (int i = 1; i < factors.length; i++) {
            assertTrue("Non Linear Scaling: threads: " + i + " scale: " + factors[i], factors[i] >= ((i - 1) * min_factor));
        }
        */

    }

    public void testPureCPU() {
        long total_ops = (long) 1e7;
        int maxThreads = Runtime.getRuntime().availableProcessors();
        int warmup = 4;
        long start_time;

        for (int i = 0; i < warmup; i++) {
            start_time = System.nanoTime();
            long cnt = getCPUTime(total_ops);
            //System.out.println("Total Ops: " + cnt);
            System.out.println("Time: " + ((int) ((System.nanoTime() - start_time) / 1000000)));
        }


        System.out.println("Warmup done");
        long first = 0;
        double[] factors = new double[maxThreads];
        int fixed = -1;

        for (int threadCount = 1; threadCount <= maxThreads; threadCount++) {
            if (fixed > 0 && threadCount != 1 && threadCount != fixed) {
                continue;
            }

            int num_slices = 100;
            SliceCPU[] slices = new SliceCPU[num_slices];

            for(int i=0; i < num_slices; i++) {
                slices[i] = new SliceCPU((int) (total_ops / num_slices));
            }

            long stime = System.nanoTime();

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            SliceRunnerCPU[] runners = new SliceRunnerCPU[threadCount];

            AtomicInteger idx = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {

                runners[i] = new SliceRunnerCPU(slices, idx);
                executor.submit(runners[i]);
            }
            executor.shutdown();

            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long total = 0;
            for (int i = 0; i < threadCount; i++) {
                total += runners[i].getTotal();
                //System.out.println("   tot: " + total);
            }

            //System.out.println("Total ops: " + total);
            long totalTime = System.nanoTime() - stime;

            if (threadCount == 1) {
                first = totalTime;
            }
            factors[threadCount - 1] = (double) first / totalTime;
            System.out.println("Threads: " + threadCount + " time: " + ((int) (totalTime / 1000000)) + " scaling: " + (factors[threadCount - 1]));

        }
        /*
        double min_factor = 1.5;

        for (int i = 1; i < factors.length; i++) {
            assertTrue("Non Linear Scaling: threads: " + i + " scale: " + factors[i], factors[i] >= ((i - 1) * min_factor));
        }
        */

    }

    /**
     * Sum the number of marked voxels
     *
     * @param grid The grid
     * @return True if the material can move away from the target material
     */
    private long getNumMarked(Grid grid, int xmin, int xmax, int ymin, int ymax) {
        Counter counter = new Counter();

        grid.find(Grid.VoxelClasses.INSIDE, counter, xmin, xmax, ymin, ymax);

        return counter.getCount();
    }

    /**
     * Sum the number of marked voxels
     *
     * @return True if the material can move away from the target material
     */
    private long getCPUTime(long ops) {
        long cnt = 0;
        long cnt2 = 0;
        double sin;

        //System.out.println("Get CPUTime: " + ops);
        for(int i=0; i < ops; i++) {
            sin = Math.sin(i);

            if (sin < 0.1) {
                cnt2++;
            }
            cnt++;
        }

        return cnt;
    }

    /**
     * class processes one slice of grid from the array of slices
     */
    class SliceRunner implements Runnable {

        Grid src;
        Slice[] que;
        AtomicInteger idx;
        long total;

        SliceRunner(Grid src, Slice[] slices, AtomicInteger idx) {
            this.src = src;
            this.que = slices;
            this.idx = idx;
        }

        public void run() {

            while (true) {
                int val = idx.getAndIncrement();

                if (val == que.length) {
                    break;
                }
                Slice slice = que[val];

                long cnt = 0;

                cnt = getNumMarked(src, slice.xmin, slice.xmax, slice.ymin, slice.ymax);
                total += cnt;
            }
        }

        long getTotal() {
            return total;
        }
    }

    /**
     * class processes one slice of grid from the array of slices
     */
    class SliceRunnerCPU implements Runnable {

        SliceCPU[] que;
        AtomicInteger idx;
        long total;

        SliceRunnerCPU(SliceCPU[] slices, AtomicInteger idx) {
            this.que = slices;
            this.idx = idx;
        }

        public void run() {

            while (true) {
                int val = idx.getAndIncrement();

                if (val == que.length) {
                    break;
                }
                SliceCPU slice = que[val];

                long cnt = 0;

                cnt = getCPUTime(slice.ops);

                //System.out.println("SliceRunner finished: " + cnt + " id: " + Thread.currentThread().getId());
                total += cnt;
            }
        }

        long getTotal() {
            return total;
        }
    }

    //
    //  class to represent one slice of grid
    //
    static class Slice {
        int xmin;
        int xmax;
        int ymin;
        int ymax;

        Slice(int xmin, int xmax, int ymin, int ymax) {
            this.xmin = xmin;
            this.xmax = xmax;
            this.ymin = ymin;
            this.ymax = ymax;
        }
    }

    //
    //  class to represent one slice of grid
    //
    static class SliceCPU {
        int ops;

        SliceCPU(int ops) {
            this.ops = ops;
        }
    }

    static class Counter implements ClassTraverser {
        private long count;

        @Override
        public void found(int x, int y, int z, byte state) {
            count++;
        }

        @Override
        public boolean foundInterruptible(int x, int y, int z, byte state) {
            count++;

            return true;
        }

        long getCount() {
            return count;
        }
    }
}
