package gpu;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Bounds;
import abfab3d.grid.Grid;
import abfab3d.grid.NIOAttributeGridByte;
import abfab3d.util.DataSource;
import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLPlatform;
import org.apache.commons.io.IOUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static abfab3d.util.Output.printf;
import static com.jogamp.opencl.CLDevice.Type.CPU;
import static com.jogamp.opencl.CLDevice.Type.GPU;
import static com.jogamp.opencl.util.CLPlatformFilters.type;
import static java.lang.System.out;
import static java.lang.System.nanoTime;

/**
 * Execute ShapeJS scripts with a GPU
 *
 * @author Alan Hudson
 */
public class ShapeJSExecutor {
    /** Last runtime without compile time */
    private long lastRuntime;

    /**
     * Execute and return a list of y sliced grids
     *
     * @param bounds
     * @param vs
     */
    public void executeGrid(DataSource source, Bounds bounds, double vs, TransferBackListener listener) {

        CLDevice[] devices = chooseDevices();
        int num_devices = devices.length;

        Slicer slicer = new Slicer(devices,bounds,vs);
        List<Slice> slices = slicer.slice();

        ConcurrentLinkedQueue<Slice> compute_queue = new ConcurrentLinkedQueue<Slice>(slices);

        ComputeRunner[] compute = new ComputeRunner[num_devices*2];
        int read_threads = 4;  // TODO: How to choose this param?
        TransferBackRunner[] transfer = new TransferBackRunner[num_devices*read_threads];

        DeviceResources[] ocd = new DeviceResources[compute.length];
        String src = generateOpenCL();

        long t0 = nanoTime();
        int nx = bounds.getWidth(vs);
        int ny = Bounds.roundSize((slices.get(0).ymax - slices.get(0).ymin) / vs);
        int nz = bounds.getDepth(vs);

        for (int i = 0; i < compute.length; i++) {
            int device = i % num_devices;

            ocd[i] = new DeviceResources(devices[device]);
            ocd[i].init(src,"makeGrid");
            ocd[i].setBufferPool(new GenericObjectPool<SliceBuffer>(new SliceBufferFactory(nx, ny, nz, vs, ocd[i].getContext())));
        }
        long time = nanoTime() - t0;
        printf("Compile time: %d ms\n", (time / 1000000));

        printf("Slices to compute: %d\n",slices.size());

        t0 = nanoTime();
        try {
            ExecutorService compute_executor = Executors.newFixedThreadPool(compute.length);
            for (int i = 0; i < compute.length; i++) {
                compute[i] = new ComputeRunner(bounds, vs, ocd[i], compute_queue);
                compute_executor.submit(compute[i]);
            }
            compute_executor.shutdown();

            ExecutorService transfer_executor = Executors.newFixedThreadPool(compute.length);
            for (int i = 0; i < transfer.length; i++) {
                int device = i % compute.length;
                transfer[i] = new TransferBackRunner(ocd[device]);
                if (listener != null) transfer[i].setListener(listener);
                transfer_executor.submit(transfer[i]);
            }
            transfer_executor.shutdown();

            try {
                compute_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

                for (int i = 0; i < transfer.length; i++) {
                    transfer[i].terminate();
                }

                transfer_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            time = nanoTime() - t0;
            lastRuntime = time;
            printf("Run time: %d ms\n",(time / 1000000));
        } finally {
            for (int i = 0; i < compute.length; i++) {
                ocd[i].cleanup();
            }
        }
    }

    public void executeSVX(DataSource source, double[] bounds, double vs, OutputStream os) {
    }

    public void executeMesh(DataSource source, double[] bounds, double vs, OutputStream os) {

    }

    public void executeColorMesh(DataSource source, double[] bounds, double vs, OutputStream os) {

    }

    public void executeShellPerMaterial(DataSource source,double[] bounds, double vs, OutputStream os) {

    }

    public long getLastRuntime() {
        return lastRuntime;
    }

    /**
     * Choose which devices to use.  Hard code for now
     * @return
     */
    private CLDevice[] chooseDevices() {
        CLDevice[] devices = CLPlatform.getDefault(type(GPU)).listCLDevices();
//        CLDevice[] devices = new CLDevice[] {CLPlatform.getDefault(type(CPU)).getMaxFlopsDevice()};
//        CLDevice[] devices = new CLDevice[] {CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice()};
/*
        CLDevice[] gpu_devices = CLPlatform.getDefault(type(GPU)).listCLDevices();
        CLDevice[] devices = new CLDevice[gpu_devices.length + 1];
        for(int i=0; i < gpu_devices.length; i++) {
            devices[i] = gpu_devices[i];
        }
        devices[devices.length-1] = CLPlatform.getDefault(type(CPU)).getMaxFlopsDevice();
*/
        for (int i = 0; i < devices.length; i++) {
            out.println("Using device: " + devices[i]);
            out.println("Caps: cores: " + devices[i].getMaxComputeUnits() + " " + devices[i].getMaxClockFrequency() + " MHZ");
            out.println("glCap: " + devices[i].isGLMemorySharingSupported());
/*
            if (devices[i].getName().contains("GTX 780")) {
                devices = new CLDevice[] {devices[i]};
                break;
            }
*/
        }

        return devices;
    }

    /**
     * Generate the openCL script from the ShapeJS Tree
     */
    private String generateOpenCL() {
        try {
            InputStream is1 = getStreamFor("DataSources.cl");
            InputStream is2 = getStreamFor("GyroidBalls.cl");

            String st1 = IOUtils.toString(is1);
            String st2 = IOUtils.toString(is2);

            return st1 + st2;
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        return null;
    }

    public static InputStream getStreamFor(String filename) {
        InputStream is = ShapeJSExecutor.class.getResourceAsStream(filename);

        if (is == null) {
            String path = "classes" + File.separator + filename;
            //printf("Loading openCL Script: %s\n", path);
            try {
                FileInputStream fis = new FileInputStream(path);

                return fis;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            printf("Loading openCL Script: %s\n", filename);
        }

        return is;
    }

}
