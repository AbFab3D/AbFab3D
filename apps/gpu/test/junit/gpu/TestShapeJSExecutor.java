/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package gpu;

// External Imports

import abfab3d.datasources.Sphere;
import abfab3d.datasources.Subtraction;
import abfab3d.datasources.Union;
import abfab3d.datasources.VolumePatterns;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Bounds;
import abfab3d.grid.NIOAttributeGridByte;
import abfab3d.grid.op.GridMaker;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.STLWriter;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import abfab3d.transforms.Translation;
import abfab3d.util.DataSource;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Units.*;
import static java.lang.System.nanoTime;
import static java.lang.System.out;

/**
 * Tests the functionality of ShapeJSExecutor
 *
 * @author Alan Hudson
 */
public class TestShapeJSExecutor extends TestCase {
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestShapeJSExecutor.class);
    }

    public void _testBasic() {
        ShapeJSExecutor exec = new ShapeJSExecutor();

        DataSource source = null;
        //double size = 10;
        double size = 30;
        double w = size * CM;
        double h = size * CM;
        double d = size * CM;
        double vs = 0.1 * MM;

        double[] bounds = new double[]{
                -w / 2, w / 2, -h / 2, h / 2, -d / 2, d / 2
        };

        Bounds bnds = new Bounds(bounds);

        int TIMES = 1;
        for(int i=0; i < TIMES; i++) {
            exec.executeGrid(source, bnds, vs, null);
        }
    }

    public void testSpeed() {
        ShapeJSExecutor exec = new ShapeJSExecutor();

        DataSource source = null;
        //double size = 10;
        double size = 12;
        double w = size * CM;
        double h = size * CM;
        double d = size * CM;
        double vs = 0.1 * MM;

        double[] bounds = new double[]{
                -w / 2, w / 2, -h / 2, h / 2, -d / 2, d / 2
        };

        Bounds bnds = new Bounds(bounds);

        int CPU_ST_TIMES = 0;
        int CPU_MT_TIMES = 0;
        int GPU_TIMES = 1;
        int max_cpu_threads = 8;
        if (Runtime.getRuntime().availableProcessors() > max_cpu_threads) max_cpu_threads = Runtime.getRuntime().availableProcessors();

        boolean writeSTL = false;

        int MAX_TIMES = Math.max(CPU_MT_TIMES, CPU_ST_TIMES);
        MAX_TIMES = Math.max(GPU_TIMES, MAX_TIMES);
        long[] cpuSTTimes = new long[MAX_TIMES];
        long[] cpuMTTimes = new long[MAX_TIMES];
        long[] gpuTimes = new long[MAX_TIMES];

        long t0;
        for(int i=0; i < GPU_TIMES; i++) {
            if (writeSTL) {
                SliceWriter sw = new SliceWriter();
                sw.start();
                exec.executeGrid(source, bnds, vs, sw);
                gpuTimes[i] = exec.getLastRuntime();
                sw.terminate();
            } else {
                exec.executeGrid(source, bnds, vs, null);
                gpuTimes[i] = exec.getLastRuntime();
            }
        }

        for(int i=0; i < CPU_MT_TIMES; i++) {
            cpuMTTimes[i] = gyroidBallMT(bnds, vs, max_cpu_threads,writeSTL);
        }

        for(int i=0; i < CPU_ST_TIMES; i++) {
            cpuSTTimes[i] = gyroidBallMT(bnds, vs, 1,writeSTL);
        }

        double tot_cpu_st = 0;
        double tot_cpu_mt = 0;
        double tot_gpu = 0;
        for (int i = 0; i < Math.max(CPU_ST_TIMES, GPU_TIMES); i++) {
            if (GPU_TIMES >= i + 1) {
                printf("run:%2d  cpu_st: %6d cpu_mt: %6d gpu: %6d s1: %4.2f  s2: %4.2f\n", i, (long) (cpuSTTimes[i] / 1e6), (long) (cpuMTTimes[i] / 1e6), (long) (gpuTimes[i] / 1e6), ((float) cpuSTTimes[i] / gpuTimes[i]), ((float) cpuMTTimes[i] / gpuTimes[i]));
                tot_gpu += gpuTimes[i] / 1e6;
                tot_cpu_st += cpuSTTimes[i] / 1e6;
                tot_cpu_mt += cpuMTTimes[i] / 1e6;
            } else {
                printf("run:%2d  cpu_st: %6d  cpu_mt: %6d  speedup: %4.2f\n", i, (long) (cpuSTTimes[i] / 1e6), (long) (cpuMTTimes[i] / 1e6), ((float) cpuSTTimes[i] / cpuMTTimes[i]));
                tot_cpu_st += cpuSTTimes[i] / 1e6;
                tot_cpu_mt += cpuMTTimes[i] / 1e6;
            }
        }

        double avg_cpu_st = (float)tot_cpu_st / CPU_ST_TIMES;
        double avg_cpu_mt = (float)tot_cpu_mt / CPU_MT_TIMES;
        double avg_gpu =  ((float)tot_gpu / GPU_TIMES);

        if (GPU_TIMES > 0) {
            printf("avg: cpu_st: %4.2f cpu_mt: %4.2f gpu: %4.2f s1: %4.2f  s2: %4.2f\n", (avg_cpu_st), avg_cpu_mt,avg_gpu, ((float) avg_cpu_st / avg_gpu), ((float) avg_cpu_mt / avg_gpu));
        } else {
            printf("avg: cpu_st: %4.2f cpu_mt: %4.2f speedup: %4.2f\n", avg_cpu_st, avg_cpu_mt, avg_cpu_st / avg_cpu_mt);
        }
    }

    private long gyroidBallMT(Bounds bounds, double vs, int threads, boolean save) {
        long time = nanoTime();
        NIOAttributeGridByte dest = new NIOAttributeGridByte(bounds.getWidth(vs), bounds.getHeight(vs), bounds.getDepth(vs), vs, vs);
        dest.setGridBounds(bounds);

        GridMaker gm = new GridMaker();
        gm.setBounds(bounds);
        gm.setThreadCount(threads);

        double radius = 0.02;
        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(0.01,0.001);
        Sphere sphere1 = new Sphere(radius);
        Sphere sphere2 = new Sphere(radius);
        sphere2.setTransform(new Translation(radius*2,0,0));

        Subtraction subtract1 = new Subtraction(sphere1,gyroid);
        Subtraction subtract2 = new Subtraction(sphere2,gyroid);
        Union union = new Union(subtract1,subtract2);

        gm.setSource(union);

        gm.makeGrid(dest);
        time = nanoTime() - time;

        if (save) save(dest, "cpu_mt_out.stl");

        System.gc();

        return time;
    }

    private static void save(AttributeGrid grid, String filename) {
        MeshMakerMT meshmaker = new MeshMakerMT();
        int max_threads = 8;
        if (max_threads == 0) {
            max_threads = Runtime.getRuntime().availableProcessors();
        }
        meshmaker.setThreadCount(max_threads);
        meshmaker.setSmoothingWidth(0.5);
        meshmaker.setMaxDecimationError(grid.getVoxelSize() * grid.getVoxelSize() * 0.1);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
        meshmaker.makeMesh(grid, its);

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
        try {
            STLWriter stl = new STLWriter(filename);
            mesh.getTriangles(stl);
            stl.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        printf("Saved STL: %s\n", filename);
    }

    static class SliceWriter extends Thread implements TransferBackListener {
        private ConcurrentLinkedQueue<SaveDetails> slices = new ConcurrentLinkedQueue<SaveDetails>();
        private boolean done = false;
        private ExecutorService exec;
        private boolean threaded = true;

        @Override
        public void sliceArrived(NIOAttributeGridByte slice, double ymin, double ymax, int idx) {
            if (threaded) {
                // Must make a local copy now
                slices.add(new SaveDetails((NIOAttributeGridByte)slice.clone(), idx));
            } else {
                save(slice, "gpu_out_" + idx + ".stl");
            }
        }

        /**
         * Blocking call
         */
        public void terminate() {
            System.out.printf("Terminating SliceWriter\n");
            done = true;

            try {
                exec.shutdown();
                exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        public void run() {
            int threads = 8;
            exec = Executors.newFixedThreadPool(threads);

            while(!done) {
                SaveDetails details = slices.poll();

                if (details != null) {
                    exec.submit(new SaveRunner(details));
                }
            }
        }
    }

    static class SaveRunner implements Runnable {
        SaveDetails details;

        public SaveRunner(SaveDetails details) {
            this.details = details;
        }

        public void run() {
            System.out.printf("Saving slice: " + details.idx);
            save(details.slice, "gpu_out_" + details.idx + ".stl");
        }
    }
    static class SaveDetails {
        NIOAttributeGridByte slice;
        int idx;

        public SaveDetails(NIOAttributeGridByte slice, int idx) {
            this.slice = slice;
            this.idx = idx;
        }
    }
}

