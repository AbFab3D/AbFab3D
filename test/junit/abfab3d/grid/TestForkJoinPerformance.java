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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// Internal Imports

/**
 * Tests the functionality of CanMoveMaterial Query.
 *
 * @author Alan Hudson
 */
public class TestForkJoinPerformance extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestForkJoinPerformance.class);
    }

    public void testPureCPU() {
        long total_ops = (long) 1e7;
        int maxThreads = Runtime.getRuntime().availableProcessors();
        int warmup = 4;
        long start_time;

        for (int i = 0; i < warmup; i++) {
            start_time = System.nanoTime();
            ForkMath fb = new ForkMath(total_ops);
            fb.computeDirectly();
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

            long stime = System.nanoTime();

            ForkMath fb = new ForkMath(total_ops);
            fb.compute(total_ops, threadCount);


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

}

class ForkMath extends RecursiveAction {
    long ops;

    public ForkMath(long ops) {
        this.ops = ops;
    }

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

    // Average pixels from source, write results into destination.
    protected void computeDirectly() {
        long cnt = getCPUTime(ops);
    }

    protected static int sThreshold = 10000;

    @Override
    protected void compute() {
        if (ops < sThreshold) {
            computeDirectly();
            return;
        }

        long split = ops / 2;

        invokeAll(new ForkMath(split),
                new ForkMath(split));
    }

    public static void compute(long ops, int processors) {
        ForkMath fb = new ForkMath(ops);

        ForkJoinPool pool = new ForkJoinPool(processors);

        //long startTime = System.currentTimeMillis();
        pool.invoke(fb);
        //long endTime = System.currentTimeMillis();
/*
        System.out.println("Image blur took " + (endTime - startTime) +
                " milliseconds.");
*/
    }
}
/*
class ForkTraverse extends RecursiveAction {

    private int[] mSource;
    private int mStart;
    private int mLength;
    private int mBlurWidth = 15; // Processing window size, should be odd.

    public ForkTraverse(int[] src, int start, int length) {
        mSource = src;
        mStart = start;
        mLength = length;
    }

    // Average pixels from source, write results into destination.
    protected void computeDirectly() {
        long cnt = 0;
        for (int index = mStart; index < mStart + mLength; index++) {
            // Calculate average.
            float rt = 0, gt = 0, bt = 0;
            cnt += mSource[index];
        }

        total = cnt;
    }
    protected static int sThreshold = 10000;

    @Override
    protected void compute() {
        if (mLength < sThreshold) {
            computeDirectly();
            return;
        }

        int split = mLength / 2;

        invokeAll(new ForkBlur(mSource, mStart, split, mDestination),
                new ForkBlur(mSource, mStart + split, mLength - split,
                        mDestination));
    }

    public static int[] blur(int[] srcImage) {
        int w = srcImage.getWidth();
        int h = srcImage.getHeight();

        int[] src = srcImage.getRGB(0, 0, w, h, null, 0, w);
        int[] dst = new int[src.length];

        System.out.println("Array size is " + src.length);
        System.out.println("Threshold is " + sThreshold);

        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println(Integer.toString(processors) + " processor"
                + (processors != 1 ? "s are " : " is ")
                + "available");

        ForkBlur fb = new ForkBlur(src, 0, src.length, dst);

        ForkJoinPool pool = new ForkJoinPool();

        long startTime = System.currentTimeMillis();
        pool.invoke(fb);
        long endTime = System.currentTimeMillis();

        System.out.println("Image blur took " + (endTime - startTime) +
                " milliseconds.");

        BufferedImage dstImage =
                new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        dstImage.setRGB(0, 0, w, h, dst, 0, w);

        return dstImage;
    }
}
*/