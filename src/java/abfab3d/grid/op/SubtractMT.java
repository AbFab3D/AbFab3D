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
package abfab3d.grid.op;

import abfab3d.grid.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static abfab3d.util.Output.*;

/**
 * Subtraction operation.   Multithreaded version.
 *
 * Subtracts one grid from another.  Grid A is the base grid.  B is
 * the subtracting grid.  INSIDE voxels of grid B will become
 * OUTSIDE voxels of A.
 *
 * Would like a mode that preserves EXTERIOR/INTERRIOR difference.
 *
 * @author Alan Hudson
 */
public class SubtractMT implements Operation {
    /** The grid used for subtraction */
    private Grid src;

    /** The dest grid */
    private Grid dest;

    /** The number of threads to use */
    private int threadCount = 1;

    /** The size of slizes in y direction */
    private int sliceSize = 1;

    /** The number of voxels of the grid in x an y */
    private int nx, ny;

    /** Slices of work */
    private ConcurrentLinkedQueue<Slice> slices;

    public SubtractMT(Grid src) {
        this.src = src;
    }

    public SubtractMT(Grid src, int threads) {
        this.src = src;

        setThreadCount(threads);
    }

    public void setThreadCount(int count) {
        threadCount = count;

        if (threadCount < 1) {
            threadCount = 1;
        }
    }

    /**
     * Set the size of y slices used for multithreading.  Changes the chunk size of jobs used in threading.  Has a fair
     * bit of interaction with cache sizes.  Not sure of a good guidance right now for setting this.
     * @param size
     */
    public void setSliceSize(int size) {
        sliceSize = size;
    }


    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid A.
     * @return original grid modified
     */
    public Grid execute(Grid dest) {
        long t0 = time();

        this.dest = dest;

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

            Runnable runner = new SubtractRunner(src,dest);
            executor.submit(runner);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        printf("subtractMT: %d ms\n", (time() - t0));

        return dest;
    }


    public AttributeGrid execute(AttributeGrid dest) {
        long t0 = time();

        this.dest = dest;

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

            Runnable runner = new SubtractRunner(src,dest);
            executor.submit(runner);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        printf("subtract: %d ms\n", (time() - t0));

        return dest;
    }

    private Slice getNextSlice() {
        return slices.poll();
    }

    /**
     * class processes one slice of grid from the array of slices
     */
    class SubtractRunner implements Runnable, ClassTraverser {

        Grid src;
        Grid dest;

        SubtractRunner(Grid src, Grid dest) {
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
                src.find(Grid.VoxelClasses.INSIDE, this, 0, nx - 1, slice.ymin, slice.ymax);
            }
        }

        public void found(int x, int y, int z, byte state) {
            dest.setState(x,y,z,Grid.OUTSIDE);
        }

        /**
         * A voxel of the class requested has been found.
         * VoxelData classes may be reused so clone the object
         * if you keep a copy.
         *
         * @param x The x grid coordinate
         * @param y The y grid coordinate
         * @param z The z grid coordinate
         * @param state The voxel state
         *
         * @return True to continue, false stops the traversal.
         */
        public boolean foundInterruptible(int x, int y, int z, byte state) {
            dest.setState(x,y,z,Grid.OUTSIDE);
            return true;
        }

    } // SubtractRunner


    //
    //  class to represent one slice of grid
    //
    static class Slice {

        int ymin;
        int ymax;

        Slice() {
            ymin = 0;
            ymax = -1;

        }

        Slice(int ymin, int ymax) {

            this.ymin = ymin;
            this.ymax = ymax;

        }

    }
}
