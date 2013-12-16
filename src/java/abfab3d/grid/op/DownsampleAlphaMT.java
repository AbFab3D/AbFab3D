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
import abfab3d.util.DefaultLongConverter;
import abfab3d.util.LongConverter;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;

/**
 * Downsample alpha operation.   Multithreaded version.
 *
 * @author Alan Hudson
 */
public class DownsampleAlphaMT implements Operation {
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

    public DownsampleAlphaMT(double coeff, int factor, long maxAttributeValue) {
        this(coeff, factor, maxAttributeValue, 0);
    }

    public DownsampleAlphaMT(double coeff, int factor, long maxAttributeValue, int threads) {
        this.coeff = coeff;
        this.factor = factor;
        this.maxAttributeValue = maxAttributeValue;

        kernelSize = factor * factor * factor;
        dataConverter = new DefaultLongConverter();
        sliceSize = factor;

        setThreadCount(threads);
    }

    public void setThreadCount(int count) {

        if (count == 0) {
            count = Runtime.getRuntime().availableProcessors();
        }

        threadCount = count;

        if (threadCount < 1) {
            threadCount = 1;
        }
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
        long t0 = time();

        this.dest = dest;

        int nx = dest.getWidth();
        int ny = dest.getHeight();

        int len_x = dest.getWidth() / factor;
        int len_y = dest.getHeight() / factor;
        int len_z = dest.getDepth() / factor;

        AttributeGrid ret_val = (AttributeGrid) dest.createEmpty(len_x,len_y,len_z,
                dest.getVoxelSize() * factor, dest.getSliceHeight() * factor);

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

            Runnable runner = new DownsampleRunner(dest,ret_val,coeff,factor,maxAttributeValue,dataConverter,kernelSize);
            executor.submit(runner);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return ret_val;
    }

    private Slice getNextSlice() {
        return slices.poll();
    }

    /**
     * class processes one slice of grid from the array of slices
     */
    class DownsampleRunner implements Runnable {
        /** The src grid */
        private AttributeGrid src;

        /** The dest grid */
        private AttributeGrid dest;

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


        DownsampleRunner(AttributeGrid src, AttributeGrid dest, double coeff, int factor, long maxAttributeValue, LongConverter dataConverter, int kernelSize) {
            this.src = src;
            this.dest = dest;
            this.coeff = coeff;
            this.factor = factor;
            this.maxAttributeValue = maxAttributeValue;
            this.dataConverter = dataConverter;
            this.kernelSize = kernelSize;
        }

        public void run() {

            VoxelData vd = dest.getVoxelData();

            while (true) {
                Slice slice = getNextSlice();
                if (slice == null) {
                    // end of processing
                    break;
                }

                executeBoxAverage(src,dest,vd,slice.ymin,slice.ymax,factor);
            }
        }

        /**
         * Execute an operation on a grid.  If the operation changes the grid
         * dimensions then a new one will be returned from the call.
         *
         * @param dest The grid to use for grid A.
         * @return The new grid
         */
        public void executeBoxAverage(AttributeGrid src, AttributeGrid dest, VoxelData vd, int ymin, int ymax, int factor) {
            int width = src.getWidth();
            int depth = src.getDepth();

            // TODO: we should structure this so it doesn't need a y / factor.  Suspect edge cases are bad too.
            int len_x = width / factor;
            int len_y = ymax;
            int len_z = depth / factor;

            for(int y=ymin; y < len_y; y = y + factor) {
                for(int x=0; x < len_x; x++) {
                    for(int z=0; z < len_z; z++) {
                        long att_avg = avgAttribute(src, x*2, y, z*2,vd);
                        byte state = Grid.OUTSIDE;
                        if (att_avg != 0) {
                            state = Grid.INSIDE;
                        }
                        dest.setData(x,y / factor,z, state, att_avg);
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
         * @param vd - scratch voxel data aligned with grid type
         * @return
         */
        private long avgAttribute(AttributeGrid grid, int x, int y, int z, VoxelData vd) {
            long sum = 0;

            for(int yy = 0; yy < factor; yy++) {
                for(int xx = 0; xx < factor; xx++) {
                    for(int zz = 0; zz < factor; zz++) {
                        grid.getData(x + xx,y + yy,z + zz,vd);

                        long mat = dataConverter.get(vd.getMaterial());
                        sum += mat;
                    }
                }
            }

            return sum / kernelSize;
        }

    } // DownsampleRunner


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
