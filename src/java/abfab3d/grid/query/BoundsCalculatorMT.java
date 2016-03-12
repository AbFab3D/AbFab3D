/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid.query;

// External Imports

// Internal Imports

import abfab3d.grid.AttributeChannel;
import abfab3d.grid.AttributeGrid;
import abfab3d.io.input.*;
import abfab3d.util.AbFab3DGlobals;
import abfab3d.util.Bounds;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Calculate the bounds of the object based on the surface density
 *
 * @author Alan Hudson
 */
public class BoundsCalculatorMT {
    private double m_threshold;
    private BoundsCalcJob[] m_jobs;
    private AtomicInteger m_jobIdx;
    int m_threadCount = 1;

    public BoundsCalculatorMT(double threshold) {
        m_threshold = threshold;
    }

    public Bounds execute(AttributeGrid grid,  AttributeChannel channel) {
        double[] min = new double[3];
        double[] max = new double[3];

        int height = grid.getHeight();

        double[] coord = new double[3];

        int sliceHeight = 1;
        m_jobs = new BoundsCalcJob[(int) Math.ceil(height / sliceHeight) + 1];
        int idx = 0;

        for(int y = 0; y < height; y+= sliceHeight){
            int ymax = y + sliceHeight;
            if(ymax > height)
                ymax = height;

            if(ymax > y){
                // non zero slice
                m_jobs[idx++] = new BoundsCalcJob(y, ymax-1);
            }
        }

        m_jobIdx = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(m_threadCount);
        BoundsCalcRunner[] runners = new BoundsCalcRunner[m_threadCount];
        for(int i = 0; i < m_threadCount; i++){
            runners[i] = new BoundsCalcRunner(grid, channel);
            executor.submit(runners[i]);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // combine all ranges to get final range
        min = runners[0].getMin();
        max = runners[0].getMax();

        for(int i=1; i < m_threadCount; i++) {
            double[] smin = runners[i].getMin();
            double[] smax = runners[i].getMax();

            if (smin[0] < min[0]) min[0] = smin[0];
            if (smin[1] < min[1]) min[1] = smin[1];
            if (smin[2] < min[2]) min[2] = smin[2];

            if (smax[0] > max[0]) max[0] = smax[0];
            if (smax[1] > max[1]) max[1] = smax[1];
            if (smax[2] > max[2]) max[2] = smax[2];
        }
        Bounds bounds = new Bounds(min[0],max[0],min[1],max[1],min[2],max[2]);

        return bounds;
    }

    public Bounds execute(AttributeGrid grid,  AttributeChannel channel, int sign) {
        return null;
    }

    public void setThreadCount(int count){
        if (count < 1) {
            count = Runtime.getRuntime().availableProcessors();
        }

        int max_threads = ((Number) AbFab3DGlobals.get(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY)).intValue();
        if (count > max_threads)
            count = max_threads;

        m_threadCount = count;
    }

    BoundsCalcJob getNextJob(){
        if(m_jobIdx.intValue() >= m_jobs.length)
            return null;

        return m_jobs[m_jobIdx.getAndIncrement()];

    }

    class BoundsCalcRunner implements Runnable {
        private AttributeGrid m_grid;
        private AttributeChannel m_channel;
        private double[] min;
        private double[] max;

        public BoundsCalcRunner(AttributeGrid grid, AttributeChannel channel) {
            m_grid = grid;
            m_channel = channel;
            min = new double[3];
            max = new double[3];
        }

        public double[] getMin() {
            return min;
        }

        public double[] getMax() {
            return max;
        }
        public void run() {
            int width = m_grid.getWidth();
            int depth = m_grid.getDepth();

            min[0] = Double.MAX_VALUE;
            min[1] = Double.MAX_VALUE;
            min[2] = Double.MAX_VALUE;

            max[0] = -Double.MAX_VALUE;
            max[1] = -Double.MAX_VALUE;
            max[2] = -Double.MAX_VALUE;
            double[] coord = new double[3];

            while(true){
                BoundsCalcJob job = getNextJob();
                if(job == null){
                    // end of processing
                    break;
                }

                for(int y=job.ymin; y <= job.ymax; y++) {
                    for (int x = 0; x < width; x++) {
                        int zmin = Integer.MAX_VALUE;
                        int zmax = Integer.MIN_VALUE;
                        for (int z = 0; z < depth; z++) {
                            if (m_channel.getValue(m_grid.getAttribute(x, y, z)) > m_threshold) {
                                if (z > zmax) zmax = z;
                                if (z < zmin) zmin = z;
                            }
                        }
                        if (zmin < Integer.MAX_VALUE) {
                            m_grid.getWorldCoords(x, y, zmin, coord);
                            if (coord[0] < min[0]) {
                                min[0] = coord[0];
                            }
                            if (coord[1] < min[1]) {
                                min[1] = coord[1];
                            }
                            if (coord[2] < min[2]) {
                                min[2] = coord[2];
                            }
                        }
                        if (zmax > Integer.MIN_VALUE) {
                            m_grid.getWorldCoords(x, y, zmax, coord);
                            if (coord[0] > max[0]) {
                                max[0] = coord[0];
                            }
                            if (coord[1] > max[1]) {
                                max[1] = coord[1];
                            }
                            if (coord[2] > max[2]) {
                                max[2] = coord[2];
                            }
                        }

                    }
                }
            }

            //System.out.printf("job min: %s  max: %s\n", Arrays.toString(min), Arrays.toString(max));
        }
    }
}


class BoundsCalcJob {
    int ymin;
    int ymax;

    BoundsCalcJob() {
        ymin = 0;
        ymax = -1;

    }

    BoundsCalcJob(int ymin, int ymax){

        this.ymin = ymin;
        this.ymax = ymax;

    }
}