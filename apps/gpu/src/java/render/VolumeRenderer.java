package render;

import abfab3d.util.Units;
import com.jogamp.opencl.*;
import com.jogamp.opencl.gl.CLGLBuffer;
import gpu.GPUUtil;
import program.ProgramLoader;

import javax.vecmath.Matrix4f;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static abfab3d.util.Output.printf;

/**
 * Volume renderer using GPU.  Renders to any CLBuffer.
 * This class only handles rendering, it should not deal with window events or require OpenGL.
 *
 * @author Alan Hudson
 */
public class VolumeRenderer {
    private float[] viewData = new float[16];
    private CLBuffer<FloatBuffer> viewBuffer;
    private int maxSteps = 512;
    private int maxShadowSteps = 0;
    private int maxAntialiasingSteps = 0;
    private CLContext context;
    private CLCommandQueue queue;
    private CLProgram program;
    private CLKernel kernel;
    private long compileTime;
    private long renderTime;
    private long kernelTIme;

    public VolumeRenderer(CLContext context, CLCommandQueue queue) {
        this.context = context;
        this.queue = queue;
    }

    /**
     * Initialize the renderer with the provided user scripts.
     *
     * @param progs Can either be File or String objects.
     * @param opts  The build options
     */
    public boolean init(List progs, String opts) {
        String kernel_name;

        long t0 = System.nanoTime();
        try {
            String buildOpts = "";
            if (opts != null) buildOpts = opts;
            double vs = 0.1 * Units.MM;
            buildOpts += " -DmaxSteps=" + maxSteps;
            buildOpts += " -DvoxelSize=" + vs;
            buildOpts += " -DmaxShadowSteps=" + maxShadowSteps;
            buildOpts += " -Dsamples=" + maxAntialiasingSteps;
            if (maxShadowSteps > 0) {
                buildOpts += " -DSHADOWS";
            }
            if (maxAntialiasingSteps > 0) {
                buildOpts += " -DSUPERSAMPLE";
                kernel_name = "renderSuper";
            } else {
                kernel_name = "render";
            }
            printf("Building program with opts: %s\n", buildOpts);

            //program = ProgramLoader.load(clContext, "VolumeRenderer.cl");
            ArrayList list = new ArrayList();
            //list.add(new File("Noise.cl"));
            list.add(new File("ShapeJS.cl"));
            list.addAll(progs);
            list.add(new File("VolumeRenderer.cl"));
            program = ProgramLoader.load(context, list);
            program.build(buildOpts);

            if (!program.isExecutable()) return false;
/*
            System.out.println(program.getBuildStatus());
            System.out.println(program.isExecutable());
            System.out.println(program.getBuildLog());
*/            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        kernel = program.createCLKernel(kernel_name);
        compileTime = (System.nanoTime() - t0);
        
        return true;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public void setMaxShadowSteps(int maxShadowSteps) {
        this.maxShadowSteps = maxShadowSteps;
    }

    public void setMaxAntialiasingSteps(int maxAntialiasingSteps) {
        this.maxAntialiasingSteps = maxAntialiasingSteps;
    }

    /**
     * Get the compiled program.  Useful for getting at the logs.
     * 
     * @return
     */
    public CLProgram getProgram() {
        return program;
    }

    /**
     * Render the program from the desired view into a buffer.
     *
     * @param view
     * @param viewBuffer
     * @param queue
     * @param dest
     */
    public void render(Matrix4f view, int width, int height, CLBuffer<FloatBuffer> viewBuffer, CLCommandQueue queue,
                       CLBuffer dest) {
        long t0 = System.nanoTime();

        int localWorkSizeX = 8;
        int localWorkSizeY = 8;
        long globalWorkSizeX = GPUUtil.roundUp(localWorkSizeX, width);
        long globalWorkSizeY = GPUUtil.roundUp(localWorkSizeY,height);

        view.invert();
        //printf("inv view: \n%s\n",view);

        viewData[0] = view.m00;
        viewData[1] = view.m01;
        viewData[2] = view.m02;
        viewData[3] = view.m03;
        viewData[4] = view.m10;
        viewData[5] = view.m11;
        viewData[6] = view.m12;
        viewData[7] = view.m13;
        viewData[8] = view.m20;
        viewData[9] = view.m21;
        viewData[10] = view.m22;
        viewData[11] = view.m23;
        viewData[12] = view.m30;
        viewData[13] = view.m31;
        viewData[14] = view.m32;
        viewData[15] = view.m33;

        viewBuffer.getBuffer().put(viewData);
        viewBuffer.getBuffer().rewind();

        // Call OpenCL kernel
        CLEventList list = new CLEventList(4);

        boolean usingGL = (dest instanceof CLGLBuffer);

        if (usingGL) {
            queue.putAcquireGLObject((CLGLBuffer) dest, list);
        }
        queue.putWriteBuffer(viewBuffer, true, null, list);

        kernel.setArg(0, dest).rewind();
        kernel.setArg(1, width);
        kernel.setArg(2, height);
        kernel.setArg(3, viewBuffer).rewind();

        queue.put2DRangeKernel(kernel, 0, 0, globalWorkSizeX, globalWorkSizeY, localWorkSizeX, localWorkSizeY, list);
        if (usingGL) {
            queue.putReleaseGLObject((CLGLBuffer) dest, list);
        }
        queue.finish();

        int idx = 1;
        if (usingGL) idx++;
        kernelTIme = list.getEvent(idx).getProfilingInfo(CLEvent.ProfilingCommand.END) - list.getEvent(idx).getProfilingInfo(CLEvent.ProfilingCommand.START);
        /*
        for(int i=0; i < list.size(); i++) {
            CLEvent event = list.getEvent(i);
            System.out.println("cmd: " + i + " time: " + (event.getProfilingInfo(CLEvent.ProfilingCommand.END)
                    - event.getProfilingInfo(CLEvent.ProfilingCommand.START))/1000000.0);
        }
        */

        renderTime = System.nanoTime() - t0;
    }

    /**
     * Get the total rendering time.
     * @return The time in nanoseconds
     */
    public long getLastTotalRenderTime() {
        return renderTime;
    }

    /**
     * Get the render time reported by OpenCL.  No transfers, just the kernel call
     * @return The time in nanoseconds
     */
    public long getLastKernelTime() {
        return kernelTIme;
    }

    public long getLastCompileTime() {
        return compileTime;
    }
}
