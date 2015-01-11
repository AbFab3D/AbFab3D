package gpu;

import abfab3d.grid.Bounds;
import abfab3d.grid.NIOAttributeGridByte;
import com.jogamp.opencl.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static abfab3d.util.Output.printf;

/**
 * Runs the OpenCL Computation
 *
 * @author Alan Hudson
 */
public class ComputeRunner implements Runnable {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_TIMING = false;

    /** Incoming slices to process */
    private ConcurrentLinkedQueue<Slice> slices;

    /** Outgoing transfers to process */
    private ConcurrentLinkedQueue<Slice> transfers;

    private DeviceResources device;

    private CLEventList events;
    private double vs;
    private double[] bounds;

    // Scratch vars
    private float[] voxel = new float[3];
    private float[] offset = new float[3];

    int localWorkSizeX;
    int localWorkSizeY;
    int localWorkSizeZ;
    int globalWorkSizeX;
    int globalWorkSizeY;
    int globalWorkSizeZ;

    public ComputeRunner(Bounds bounds, double vs, DeviceResources device, ConcurrentLinkedQueue<Slice> slices) {
        this.device = device;
        this.slices = slices;
        this.transfers = device.getTransferQueue();
        this.bounds = new double[6];
        this.bounds[0] = bounds.xmin;
        this.bounds[1] = bounds.xmax;
        this.bounds[2] = bounds.ymin;
        this.bounds[3] = bounds.ymax;
        this.bounds[4] = bounds.zmin;
        this.bounds[5] = bounds.zmax;
        this.vs = vs;
    }

    private void cleanup() {
        events = null;
        device = null;
    }

    public void run() {

        if (DEBUG) printf("Starting compute: %s\n",device.getDevice().getName());
        CLKernel kernel = device.getKernel();

        double m_sizeX, m_sizeY, m_sizeZ;
        double m_centerX, m_centerY, m_centerZ;
        NIOAttributeGridByte dest = null;

        try {
            CLCommandQueue queue = device.getQueue();

            while (true) {
                Slice slice = getNextSlice();
                if (slice == null) {
                    // end of processing
                    break;
                }

                int nx = (int) ((bounds[1] - bounds[0]) / vs);
                int ny = (int) ((slice.ymax - slice.ymin) / vs);
                int nz = (int) ((bounds[5] - bounds[4]) / vs);
                int sliceLen = nx * nz;

                // get a free buffer.  alloc for now.  TODO: try reusing

                long t0 = System.nanoTime();
//                NIOAttributeGridByte dest = new NIOAttributeGridByte(nx,ny,nz,vs,vs);
                //printf("alloc time: %6.2f\n",(System.nanoTime() - t0) / 1000000.0);
                slice.buffer = device.getBufferPool().borrowObject();

                double[] slice_bounds = new double[6];
                slice.buffer.getGrid().getGridBounds(slice_bounds);
                bounds[2] = slice.ymin;
                bounds[3] = slice.ymax;
                slice.buffer.getGrid().setGridBounds(bounds);


                dest = slice.buffer.getGrid();
                bounds[2] = slice.ymin;
                bounds[3] = slice.ymax;

                m_centerX = (bounds[0] + bounds[1])/2;
                m_centerY = (bounds[2] + bounds[3])/2;
                m_centerZ = (bounds[4] + bounds[5])/2;

                m_sizeX = bounds[1] - bounds[0];
                m_sizeY = bounds[3] - bounds[2];
                m_sizeZ = bounds[5] - bounds[4];
                double voxelX = (m_sizeX / dest.getWidth());
                double voxelY = (m_sizeY / dest.getHeight());
                double voxelZ = (m_sizeZ / dest.getDepth());
                voxel[0] = (float) voxelX;
                voxel[1] = (float) voxelY;
                voxel[2] = (float) voxelZ;

                // half voxel shift get coordinate of the center of voxel
                offset[0] = (float) (m_centerX - m_sizeX/2 + voxelX/2);
                offset[1] = (float) (m_centerY - m_sizeY/2 + voxelY/2);
                offset[2] = (float) (m_centerZ - m_sizeZ/2 + voxelZ/2);

                int max_wg_size = device.getDevice().getMaxWorkGroupSize();

                //printf("ny: %d\n",ny);
                int maxYWorkSize = Math.min(8, ny);
                int sw = (int) Math.floor(Math.sqrt(max_wg_size / maxYWorkSize));
                int maxXWorkSize = sw;
                int maxZWorkSize = sw;

                localWorkSizeX = maxXWorkSize; // Local work size dimensions
                localWorkSizeY = maxYWorkSize; // Local work size dimensions
                localWorkSizeZ = maxZWorkSize; // Local work size dimensions
                globalWorkSizeX = GPUUtil.roundUp(localWorkSizeX, nx);  // rounded up to the nearest multiple of the localWorkSize
                globalWorkSizeY = GPUUtil.roundUp(localWorkSizeY, ny);  // rounded up to the nearest multiple of the localWorkSize
                globalWorkSizeZ = GPUUtil.roundUp(localWorkSizeZ, nz);  // rounded up to the nearest multiple of the localWorkSize


                //printf("\nlWS: %d %d %d  gWS: %d %d %d\n", localWorkSizeX, localWorkSizeY, localWorkSizeZ, globalWorkSizeX, globalWorkSizeY, globalWorkSizeZ);

                // change to setArg(idx) when reusing kernels
                int idx = 0;
                kernel.setArg(idx++,voxel[0]);
                kernel.setArg(idx++,voxel[1]);
                kernel.setArg(idx++,voxel[2]);
                kernel.setArg(idx++,offset[0]);
                kernel.setArg(idx++,offset[1]);
                kernel.setArg(idx++,offset[2]);
                kernel.setArg(idx++,sliceLen);
                kernel.setArg(idx++,dest.getWidth());
                kernel.setArg(idx++,dest.getHeight());
                kernel.setArg(idx++,dest.getDepth());
                kernel.setArg(idx++,slice.buffer.getBufferDest());

                events = new CLEventList(1);

                if (DEBUG) printf("Computing: %f --> %f  size: %d,%d,%d\n",slice.ymin,slice.ymax,nx,ny,nz);

                queue.put3DRangeKernel(kernel, 0, 0, 0, globalWorkSizeX, globalWorkSizeY, globalWorkSizeZ, localWorkSizeX, localWorkSizeY, localWorkSizeZ, events);
                queue.finish();

                transfers.add(slice);

                if (DEBUG_TIMING) {
                    long start = 0;
                    for (int j = 0; j < events.size(); j++) {
                        CLEvent event = events.getEvent(j);
                        if (j ==0) {
                            if (device.getStartTime() == 0) {
                                start = event.getProfilingInfo(CLEvent.ProfilingCommand.START);
                                device.setStartTime(start);
                            } else {
                                start = device.getStartTime();
                            }
                        }
                        printf("compute cmd(%s):  total: %6.2f  start:%6.2f end: %6.2f\n", device.getDevice().getName(),(event.getProfilingInfo(CLEvent.ProfilingCommand.END)
                                        - event.getProfilingInfo(CLEvent.ProfilingCommand.START)) / 1000000.0,
                                (event.getProfilingInfo(CLEvent.ProfilingCommand.START) - start) / 1000000.0,
                                (event.getProfilingInfo(CLEvent.ProfilingCommand.END) - start) / 1000000.0);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            kernel.release();
            cleanup();
        }

        //printf("Finished Compute\n");
    }

    private Slice getNextSlice() {
        return slices.poll();
    }

}
