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

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.AttributeOperation;
import abfab3d.grid.Grid;
import abfab3d.grid.Operation;
import abfab3d.util.AbFab3DGlobals;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;

// Internal Imports

/**
 * Intersection operation multithreaded version.
 * <p/>
 * Intersects two grids.  A voxel which is INSIDE in both
 * grids will be in the destination.
 *
 * @author Alan Hudson
 */
public class IntersectOpMT implements Operation, AttributeOperation {
    /**
     * The source grid, A
     */
    private Grid src;

    /**
     * The dest grid, B
     */
    private Grid dest;

    /** The number of threads to use */
    private int threadCount = 1;

    /** The size of slizes in y direction */
    private int sliceSize = 1;

    /** The number of voxels of the grid in x an y */
    private int nx, ny;

    /** Slices of work */
    private ConcurrentLinkedQueue<Slice> slices;

    public IntersectOpMT(Grid src) {
        this.src = src;
    }

    public IntersectOpMT(Grid src, int threads) {
        this.src = src;

        setThreadCount(threads);
    }

    public void setThreadCount(int count) {
        if (count < 1) {
            count = Runtime.getRuntime().availableProcessors();
        }

        int max_threads = ((Number) AbFab3DGlobals.get(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY)).intValue();
        if (count > max_threads)
            count = max_threads;

        threadCount = count;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The destination grid
     * @return The new grid
     */
    public Grid execute(Grid dest) {
        long t0 = time();

        nx = dest.getWidth();
        ny = dest.getHeight();

        slices = new ConcurrentLinkedQueue<Slice>();

        int sliceHeight = sliceSize;

        for (int y = 0; y < ny; y += sliceHeight) {
            int ymax = y + sliceHeight;
            if (ymax > ny)
                ymax = ny;

            if (ymax > y) {
                // non zero slice
                slices.add(new Slice(y, ymax - 1));
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {

            Runnable runner = new IntersectRunner(src,dest);
            executor.submit(runner);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        printf("intersectMT: %d ms\n", (time() - t0));

        return dest;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use or null to create one
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid dest) {
        return (AttributeGrid) execute((Grid)dest);
    }

    private Slice getNextSlice() {
        return slices.poll();
    }

    /**
     * class processes one slice of grid from the array of slices
     */
    class IntersectRunner implements Runnable {

        Grid src;
        Grid dest;

        IntersectRunner(Grid src, Grid dest) {
            this.src = src;
            this.dest = dest;
        }

        public void run() {

            while (true) {
                Slice slice = getNextSlice();
                if (slice == null) {
                    // end of processing
                    break;
                }

                int width = dest.getWidth();
                int depth = dest.getDepth();

                //System.out.println("Process slice: " + slice.ymin + " max: " + slice.ymax);
                for (int y = slice.ymin; y <= slice.ymax; y++) {
                    for (int x = 0; x < width; x++) {
                        for (int z = 0; z < depth; z++) {
                            byte src_state = src.getState(x, y, z);
                            if (src_state == Grid.OUTSIDE) {
                                dest.setState(x, y, z, Grid.OUTSIDE);
                            }
                        }
                    }
                }
            }
        }
    }

    //
    //  class to represent one slice of grid
    //
    static class Slice {

        int ymin;
        int ymax;

        Slice(int ymin, int ymax) {
            this.ymin = ymin;
            this.ymax = ymax;
        }
    }

}
