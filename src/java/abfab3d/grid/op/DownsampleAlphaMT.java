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

import abfab3d.core.AttributeGrid;
import abfab3d.core.Grid;
import abfab3d.grid.*;
import abfab3d.util.AbFab3DGlobals;
import abfab3d.util.DefaultLongConverter;
import abfab3d.core.LongConverter;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;

/**
 * Downsample alpha operation.   Multithreaded version.
 *
 * The coeff = 0 case of simple averaging is special cased as its about twice as fast as the general method.
 *
 * @author Alan Hudson
 */
public class DownsampleAlphaMT implements Operation {
    private static final boolean STATS = false;

    /** The number of threads to use */
    private int threadCount = 1;

    /** The size of slices in y direction */
    private int sliceSize;

    /** The weight of voxel when averaging */
    private double coeff;

    /** How much to downsample.  */
    private int factor;

    /** The maximum alpha value */
    private long maxAttributeValue;

    /** Convert attribute to alpha */
    private LongConverter dataConverter;

    /** Number of voxels in a kernel */
    private int kernelSize;

    /** The dest */
    private AttributeGrid dest;

    /** Slices of work */
    private ConcurrentLinkedQueue<Slice> slices;

    /** Is the input a binary grid */
    private boolean binaryInput;

    public DownsampleAlphaMT(double coeff, int factor, long maxAttributeValue) {
        this(false,coeff, factor, maxAttributeValue, 0);
    }
    public DownsampleAlphaMT(boolean binary, double coeff, int factor, long maxAttributeValue) {
        this(binary,coeff, factor, maxAttributeValue, 0);
    }

    public DownsampleAlphaMT(boolean binary, double coeff, int factor, long maxAttributeValue, int threads) {
        this.binaryInput = binary;
        this.coeff = coeff;
        this.factor = factor;
        this.maxAttributeValue = maxAttributeValue;

        kernelSize = factor * factor * factor;
        dataConverter = new DefaultLongConverter();
        sliceSize = factor;

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
     * @param dest The grid to use for grid A.
     * @return original grid modified
     */
    public Grid execute(Grid dest) {
        throw new IllegalArgumentException(fmt("DownsampleAlphaMT.execute(%s) not implemented!\n", dest));
    }


    public AttributeGrid execute(AttributeGrid dest) {
        this.dest = dest;

        int len_x = dest.getWidth() / factor;
        int len_y = dest.getHeight() / factor;
        int len_z = dest.getDepth() / factor;

        AttributeGrid ret_val = (AttributeGrid) dest.createEmpty(len_x,len_y,len_z,
                dest.getVoxelSize() * factor, dest.getSliceHeight() * factor);

        int
                nx = dest.getWidth(),
                ny = dest.getHeight(),
                nz = dest.getDepth(),
                nx1 = nx/factor,
                ny1 = ny/factor,
                nz1 = nz/factor;

        double bounds[] = new double[6];
        dest.getGridBounds(bounds);

        double dx = (bounds[1] - bounds[0] )/nx;
        double dy = (bounds[3] - bounds[2] )/ny;
        double dz = (bounds[5] - bounds[4] )/nz;
        double
                dx1 = dx * factor,
                dy1 = dy * factor,
                dz1 = dz * factor;

        bounds[1] = bounds[0] + dx1 * nx1;
        bounds[3] = bounds[2] + dy1 * ny1;
        bounds[5] = bounds[4] + dy1 * nz1;

        ret_val.setGridBounds(bounds);

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

        if (STATS) {
            System.out.println("DownsampleAlpaMT Stats");
            System.out.println("Slices: " + slices.size());
        }
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Runnable runner = null;

            if (binaryInput) {
                runner = new DownsampleRunnerBinaryCoeffBoxAverage(dest,ret_val,coeff,factor,maxAttributeValue,dataConverter,kernelSize);
            } else if (coeff == 0.0) {
                runner = new DownsampleRunnerCoeffZero(dest,ret_val,factor,maxAttributeValue,dataConverter,kernelSize);
            } else {
                runner = new DownsampleRunnerCoeffNonZero(dest,ret_val,coeff,factor,maxAttributeValue,dataConverter,kernelSize);
            }
            executor.submit(runner);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        dest = null;
        slices.clear();

        return ret_val;
    }

    private Slice getNextSlice() {
        return slices.poll();
    }

    /**
     * class processes one slice of grid from the array of slices
     */
    class DownsampleRunnerCoeffZero implements Runnable {
        /** The src grid */
        private AttributeGrid src;

        /** The dest grid */
        private AttributeGrid dest;

        /** How much to downsample.  */
        private int factor;

        /** The maximum alpha value */
        private long maxAttributeValue;

        /** Convert attribute to alpha */
        private LongConverter dataConverter;

        /** Number of voxels in a kernel */
        private int kernelSize;

        /** Operations, only calculated during stats */
        private long ops;

        DownsampleRunnerCoeffZero(AttributeGrid src, AttributeGrid dest, int factor, long maxAttributeValue, LongConverter dataConverter, int kernelSize) {
            this.src = src;
            this.dest = dest;
            this.factor = factor;
            this.maxAttributeValue = maxAttributeValue;
            this.dataConverter = dataConverter;
            this.kernelSize = kernelSize;
        }

        public void run() {

            long t0;
            int cnt = 0;

            if (STATS) {
                t0 = System.nanoTime();
            }

            while (true) {
                Slice slice = getNextSlice();
                if (slice == null) {
                    // end of processing
                    break;
                }

                executeBoxAverage(src,dest,slice.ymin,slice.ymax,factor);
                if (STATS) {
                    cnt++;
                }
            }

            if (STATS) {
                printf("DownsampleAlphaMT.  Thread: %s time: %9d items: %6d ops: %10d\n",
                Thread.currentThread().getName(),((System.nanoTime() - t0)/1000),cnt, ops);
            }

            src = null;
            dest = null;
        }

        /**
         * Execute an operation on a grid.  If the operation changes the grid
         * dimensions then a new one will be returned from the call.
         *
         * @param dest The grid to use for grid A.
         * @return The new grid
         */
        public void executeBoxAverage(AttributeGrid src, AttributeGrid dest, int ymin, int ymax, int factor) {
            int width = src.getWidth();
            int depth = src.getDepth();

            // TODO: we should structure this so it doesn't need a y / factor.  Suspect edge cases are bad too.
            int len_x = width / factor;
            int len_y = ymax;
            int len_z = depth / factor;

            for(int y=ymin; y < len_y; y = y + factor) {
                for(int x=0; x < len_x; x++) {
                    for(int z=0; z < len_z; z++) {
                        long att_avg = avgAttribute(src, x*factor, y, z*factor);

                        // This should be ok, ie state test comes from IOFunc anyway
                        dest.setAttribute(x,y / factor, z, att_avg);
                    }
                }
            }
        }

        /**
         * Average attribute values.
         *
         * @param grid
         * @param x
         * @param y
         * @param z
         * @return
         */
        private long avgAttribute(AttributeGrid grid, int x, int y, int z) {
            long sum = 0;

            for(int yy = 0; yy < factor; yy++) {
                for(int xx = 0; xx < factor; xx++) {
                    for(int zz = 0; zz < factor; zz++) {

                        /*
                        TODO: This method was 5X slower, why!?
                        grid.getData(x + xx,y + yy,z + zz);
                        long mat = dataConverter.get(vd.getMaterial());
                                                */


                        long mat = dataConverter.get(grid.getAttribute(x + xx,y + yy,z + zz));
                        sum += mat;

                        if (STATS) {
                            ops++;
                        }
                    }
                }
            }

            return sum / kernelSize;
        }

    }

    /**
     * class processes one slice of grid from the array of slices
     */
    class DownsampleRunnerCoeffNonZero implements Runnable {
        /** The src grid */
        private AttributeGrid src;

        /** The dest grid */
        private AttributeGrid dest;

        /** How much to downsample.  */
        private int factor;

        /** The maximum alpha value */
        private long maxAttributeValue;

        /** Convert attribute to alpha */
        private LongConverter dataConverter;

        /** Number of voxels in a kernel */
        private int kernelSize;

        /** Operations, only calculated during stats */
        private long ops;

        private double coeff;

        DownsampleRunnerCoeffNonZero(AttributeGrid src, AttributeGrid dest, double coeff, int factor, long maxAttributeValue, LongConverter dataConverter, int kernelSize) {
            this.src = src;
            this.dest = dest;
            this.coeff = coeff;
            this.factor = factor;
            this.maxAttributeValue = maxAttributeValue;
            this.dataConverter = dataConverter;
            this.kernelSize = kernelSize;
        }

        public void run() {

            long t0;
            int cnt = 0;

            if (STATS) {
                t0 = System.nanoTime();
            }

            while (true) {
                Slice slice = getNextSlice();
                if (slice == null) {
                    // end of processing
                    break;
                }

                executeBoxAverage(src,dest,slice.ymin,slice.ymax,factor);
                if (STATS) {
                    cnt++;
                }
            }

            if (STATS) {
                printf("DownsampleAlphaMT.  Thread: %s time: %9d items: %6d ops: %10d\n",
                        Thread.currentThread().getName(),((System.nanoTime() - t0)/1000),cnt, ops);
            }

            src = null;
            dest = null;
        }

        /**
         * Execute an operation on a grid.  If the operation changes the grid
         * dimensions then a new one will be returned from the call.
         *
         * @param dest The grid to use for grid A.
         * @return The new grid
         */
        public void executeBoxAverage(AttributeGrid src, AttributeGrid dest, int ymin, int ymax, int factor) {
            int width = src.getWidth();
            int depth = src.getDepth();

            // TODO: we should structure this so it doesn't need a y / factor.  Suspect edge cases are bad too.
            int len_x = width / factor;
            int len_y = ymax;
            int len_z = depth / factor;

            for(int y=ymin; y < len_y; y = y + factor) {
                for(int x=0; x < len_x; x++) {
                    for(int z=0; z < len_z; z++) {
                        long att_avg = avgAttribute(src, x*factor, y, z*factor);

                        // This should be ok, ie state test comes from IOFunc anyway
                        dest.setAttribute(x,y / factor, z, att_avg);
                    }
                }
            }
        }

        /**
         * Average attribute values.
         *
         * @param grid
         * @param x
         * @param y
         * @param z
         * @return
         */
        private long avgAttribute(AttributeGrid grid, int x, int y, int z) {
            double sum = 0;
            double total = 0;

            for(int yy = 0; yy < factor; yy++) {
                for(int xx = 0; xx < factor; xx++) {
                    for(int zz = 0; zz < factor; zz++) {
                        long mat = dataConverter.get(grid.getAttribute(x + xx,y + yy,z + zz));

                        if (mat == 0) {
                            total += 1.0 - coeff;
                        } else {
                            double fill = (double) mat / maxAttributeValue;
                            double vw = 1.0 + coeff * fill;
                            sum += vw * fill;
                            total += vw;
                        }
                    }
                }
            }

            // rounding is more accurate but expensive, favor speed.
//                    long avg = Math.round((float)sum / total * maxAttributeValue);
            long avg = (long) (sum / total * maxAttributeValue);

            return avg;
        }

    }

    /**
     * class processes one slice of grid from the array of slices
     */
    class DownsampleRunnerBinaryCoeffBoxAverage implements Runnable {
        /** The src grid */
        private AttributeGrid src;

        /** The dest grid */
        private AttributeGrid dest;

        /** How much to downsample.  */
        private int factor;

        /** The maximum alpha value */
        private long maxAttributeValue;

        /** Convert attribute to alpha */
        private LongConverter dataConverter;

        /** Number of voxels in a kernel */
        private int kernelSize;

        /** Operations, only calculated during stats */
        private long ops;

        private double coeff;

        DownsampleRunnerBinaryCoeffBoxAverage(AttributeGrid src, AttributeGrid dest, double coeff, int factor, long maxAttributeValue, LongConverter dataConverter, int kernelSize) {
            this.src = src;
            this.dest = dest;
            this.coeff = coeff;
            this.factor = factor;
            this.maxAttributeValue = maxAttributeValue;
            this.dataConverter = dataConverter;
            this.kernelSize = kernelSize;
        }

        public void run() {

            long t0;
            int cnt = 0;

            if (STATS) {
                t0 = System.nanoTime();
            }

            while (true) {
                Slice slice = getNextSlice();
                if (slice == null) {
                    // end of processing
                    break;
                }

                executeBoxAverage(src,dest,slice.ymin,slice.ymax,factor);
                if (STATS) {
                    cnt++;
                }
            }

            if (STATS) {
                printf("DownsampleAlphaMT.  Thread: %s time: %9d items: %6d ops: %10d\n",
                        Thread.currentThread().getName(),((System.nanoTime() - t0)/1000),cnt, ops);
            }

            src = null;
            dest = null;
        }

        /**
         * Execute an operation on a grid.  If the operation changes the grid
         * dimensions then a new one will be returned from the call.
         *
         * @param dest The grid to use for grid A.
         * @return The new grid
         */
        public void executeBoxAverage(AttributeGrid src, AttributeGrid dest, int ymin, int ymax, int factor) {
            int width = src.getWidth();
            int depth = src.getDepth();

            // TODO: we should structure this so it doesn't need a y / factor.  Suspect edge cases are bad too.
            int len_x = width / factor;
            int len_y = ymax;
            int len_z = depth / factor;

            for(int y=ymin; y < len_y; y = y + factor) {
                for(int x=0; x < len_x; x++) {
                    for(int z=0; z < len_z; z++) {
                        long att_avg = avgAttribute(src, x*factor, y, z*factor);

                        // This should be ok, ie state test comes from IOFunc anyway
                        dest.setAttribute(x,y / factor, z, att_avg);
                    }
                }
            }
        }

        /**
         * Average attribute values.
         *
         * @param grid
         * @param x
         * @param y
         * @param z
         * @return
         */
        private long avgAttribute(AttributeGrid grid, int x, int y, int z) {
            double sum = 0;
            double total = 0;

            for(int yy = 0; yy < factor; yy++) {
                for(int xx = 0; xx < factor; xx++) {
                    for(int zz = 0; zz < factor; zz++) {
                        if(grid.getState(x + xx,y + yy,z + zz) != Grid.OUTSIDE){
                            sum += coeff;
                            total += coeff;
                        } else {
                            total += 1.;
                        }
                    }
                }
            }

            // rounding is more accurate but expensive, favor speed.
//                    long avg = Math.round((float)sum / total * maxAttributeValue);
            long avg = (long) (sum / total * maxAttributeValue);

            return avg;
        }
    }

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
