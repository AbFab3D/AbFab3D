package gpu;

import com.jogamp.opencl.*;
import org.apache.commons.pool2.ObjectPool;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Wrapper for OpenCL device attributes
 *
 * @author Alan Hudson
 */
public class DeviceResources {
    private CLDevice device = null;
    private CLContext context = null;
    private CLProgram program = null;
    private CLCommandQueue queue;
    private CLKernel kernel;
    private String kernelName;
    private ConcurrentLinkedQueue<Slice> transferQueue = new ConcurrentLinkedQueue<Slice>();
    private ObjectPool<SliceBuffer> bufferPool;

    /** Start time of the first command on the device */
    private long startTime;

    public DeviceResources(CLDevice device) {
        this.device = device;
    }

    public void init(String src, String kernel) {
        this.kernelName = kernel;
        context = CLContext.create(device);

        try {
            program = context.createProgram(src);

            program.build();
            assert program.isExecutable();

            queue = context.getDevices()[0].createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);
        } catch (Exception e) {
            e.printStackTrace();
            //printf("Log: %s\n",program.getBuildLog());
            //printf("Source: %s\n",program.getSource());
        }

    }


    public ObjectPool<SliceBuffer> getBufferPool() {
        return bufferPool;
    }

    public void setBufferPool(ObjectPool<SliceBuffer> bufferPool) {
        this.bufferPool = bufferPool;
    }

    public ConcurrentLinkedQueue<Slice> getTransferQueue() {
        return transferQueue;
    }

    public CLContext getContext() {
        return context;
    }

    public CLProgram getProgram() {
        return program;
    }

    public CLDevice getDevice() {
        return device;
    }

    public CLKernel getKernel() {
        // TODO: not thread safe, need to thread local
        return program.createCLKernel(kernelName);
    }

    public CLCommandQueue getQueue() {
        // TODO: not thread safe
        return queue = context.getDevices()[0].createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void cleanup() {
        context.release();
        context = null;
        program = null;
        device = null;
        queue = null;
        kernel = null;
    }
}
