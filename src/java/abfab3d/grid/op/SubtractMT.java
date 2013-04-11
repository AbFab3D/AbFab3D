package abfab3d.grid.op;

import abfab3d.grid.*;

import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static abfab3d.util.Output.*;

/**
 * Subtraction operation.   Multithreaded version.
 *
 * Subtracts one grid from another.  Grid A is the base grid.  B is
 * the subtracting grid.  MARKED voxels of grid B will become
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

    int m_threadCount = 1;
    int m_sliceSize = 1;
    int m_nx, m_ny, m_nz;
    //Stack<Slice> m_slices;
    ConcurrentLinkedQueue<Slice> m_slices;

    public SubtractMT(Grid src) {
        this.src = src;
    }

    public void setThreadCount(int count) {

        m_threadCount = count;

    }

    public void setSliceSize(int size) {

        m_sliceSize = size;

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

        m_nx = dest.getWidth();
        m_ny = dest.getHeight();
        m_nz = dest.getDepth();


//        m_slices = new Stack<Slice>();
        m_slices = new ConcurrentLinkedQueue<Slice>();

        int sliceHeight = m_sliceSize;

        for (int y = 0; y < m_ny; y += sliceHeight) {
            int ymax = y + sliceHeight;
            if (ymax > m_ny)
                ymax = m_ny;

            if (ymax > y) {
                // non zero slice
//                m_slices.push(new Slice(y, ymax - 1));
                m_slices.add(new Slice(y, ymax - 1));
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(m_threadCount);
        for (int i = 0; i < m_threadCount; i++) {

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

        m_nx = dest.getWidth();
        m_ny = dest.getHeight();
        m_nz = dest.getDepth();

//        m_slices = new Stack<Slice>();
        m_slices = new ConcurrentLinkedQueue<Slice>();

        int sliceHeight = m_sliceSize;

        for (int y = 0; y < m_ny; y += sliceHeight) {
            int ymax = y + sliceHeight;
            if (ymax > m_ny)
                ymax = m_ny;

            if (ymax > y) {
                // non zero slice
//                m_slices.push(new Slice(y, ymax - 1));
                m_slices.add(new Slice(y, ymax - 1));
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(m_threadCount);
        for (int i = 0; i < m_threadCount; i++) {

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

/*
    synchronized Slice getNextSlice() {

        if (m_slices.empty())
            return null;

        return m_slices.pop();

    }
*/
    private Slice getNextSlice() {
        return m_slices.poll();
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
                src.find(Grid.VoxelClasses.MARKED, this, 0, m_nx - 1, slice.ymin, slice.ymax);
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
