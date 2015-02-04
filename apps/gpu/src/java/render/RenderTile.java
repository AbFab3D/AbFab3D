package render;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * A tile to render.
 *
 * @author Alan Hudson
 */
public class RenderTile {
    private int x0;
    private int y0;
    private int width;
    private int height;

    private DeviceResources resources;
    private CLDevice device;
    private CLContext context;
    private CLCommandQueue queue;
    private VolumeRenderer renderer;
    private CLBuffer<IntBuffer> dest;
    private CLBuffer<FloatBuffer> view;

    public RenderTile(int x0, int y0, int width, int height) {
        this.x0 = x0;
        this.y0 = y0;
        this.width = width;
        this.height = height;
    }

    public RenderTile(int x0, int y0, int width, int height, CLBuffer<IntBuffer> dest, CLBuffer<FloatBuffer> view) {
        this.x0 = x0;
        this.y0 = y0;
        this.width = width;
        this.height = height;
        this.dest = dest;
        this.view = view;
    }

    public RenderTile(int x0, int y0, int width, int height,
                     CLDevice device, CLContext context, CLCommandQueue queue, VolumeRenderer renderer,
                     CLBuffer<IntBuffer> dest, CLBuffer<FloatBuffer> view) {
        this.x0 = x0;
        this.y0 = y0;
        this.width = width;
        this.height = height;
        this.device = device;
        this.context = context;
        this.queue = queue;
        this.renderer = renderer;
        this.dest = dest;
        this.view = view;
    }

    public CLDevice getDevice() {
        return device;
    }

    public void setDevice(CLDevice device) {
        this.device = device;
    }

    public DeviceResources getResources() {
        return resources;
    }

    public void setResources(DeviceResources resources) {
        this.resources = resources;
    }

    public CLContext getContext() {
        return context;
    }

    public void setContext(CLContext context) {
        this.context = context;
    }

    public CLCommandQueue getCommandQueue() {
        return queue;
    }

    public void setQueue(CLCommandQueue queue) {
        this.queue = queue;
    }

    public VolumeRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(VolumeRenderer renderer) {
        this.renderer = renderer;
    }

    public CLBuffer<FloatBuffer> getView() {
        return view;
    }

    public void setView(CLBuffer<FloatBuffer> view) {
        this.view = view;
    }

    public void setDest(CLBuffer<IntBuffer> dest) {
        this.dest = dest;
    }

    public int getX0() {
        return x0;
    }

    public void setX0(int x0) {
        this.x0 = x0;
    }

    public int getY0() {
        return y0;
    }

    public void setY0(int y0) {
        this.y0 = y0;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public CLBuffer<IntBuffer> getDest() {
        return dest;
    }
}
