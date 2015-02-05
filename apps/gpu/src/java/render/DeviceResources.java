package render;

import com.jogamp.opencl.*;
import datasources.Instruction;
import gpu.Slice;
import gpu.SliceBuffer;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.nio.IntBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static abfab3d.util.Output.printf;
import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;

/**
 * Device specific resources.
 *
 * @author Alan Hudson
 */
public class DeviceResources {
    private CLDevice device = null;
    private CLContext context = null;
    private CLProgram program = null;
    private CLCommandQueue queue;
    private VolumeRenderer renderer;
//    private ConcurrentLinkedQueue<RenderTile> transferQueue = new ConcurrentLinkedQueue<RenderTile>();
    private ObjectPool<CLBuffer<IntBuffer>> bufferPool;
    private CLBuffer view;
    private int width;
    private int height;
    private int twidth;
    private int theight;

    /** Start time of the first command on the device */
    private long startTime;

    public DeviceResources(CLDevice device, int twidth, int theight, int width, int height) {
        this.device = device;
        this.twidth = twidth;
        this.theight = theight;
        this.width = width;
        this.height = height;
    }

    /**
     * Initialize the device resources, must be called on the CPU thread that handles the device
     * @param prog
     * @param inst
     * @param opts
     * @param ver
     */
    public void init(List<String> prog, List<Instruction> inst,String opts, String ver) {
        printf("Init device: %s on thread: %s\n",this,Thread.currentThread());

        if (context == null) {
            context = CLContext.create(device);
            queue = device.createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);
            renderer = new VolumeRenderer(context, queue);
        }
        renderer.init(prog,inst,opts,ver);

        view = context.createFloatBuffer(16, READ_ONLY);

        bufferPool = new GenericObjectPool<CLBuffer<IntBuffer>>(new TileBufferFactory(twidth,theight, context));
    }


    public ObjectPool<CLBuffer<IntBuffer>> getBufferPool() {
        return bufferPool;
    }

    public void setBufferPool(ObjectPool<CLBuffer<IntBuffer>> bufferPool) {
        this.bufferPool = bufferPool;
    }

    /*
    public ConcurrentLinkedQueue<RenderTile> getTransferQueue() {
        return transferQueue;
    }
    */
    public CLContext getContext() {
        return context;
    }

    public CLProgram getProgram() {
        return program;
    }

    public CLDevice getDevice() {
        return device;
    }

    public CLCommandQueue getQueue() {
        return queue;
    }

    public VolumeRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(VolumeRenderer renderer) {
        this.renderer = renderer;
    }

    public CLBuffer getView() {
        return view;
    }

    public void setView(CLBuffer view) {
        this.view = view;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void cleanup() {

        if (renderer != null) renderer.cleanup();

        /*
        context.release();
        context = null;
        program = null;
        device = null;
        queue = null;
        */
    }
}
