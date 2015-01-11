package viewer;

import abfab3d.grid.Bounds;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.*;
import com.jogamp.opencl.gl.CLGLBuffer;
import com.jogamp.opencl.gl.CLGLContext;
import com.jogamp.opengl.util.Animator;
import gpu.GPUUtil;
import program.ProgramLoader;
import render.VolumeRenderer;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

import static abfab3d.util.Output.printf;
import static com.jogamp.common.nio.Buffers.SIZEOF_FLOAT;
import static com.jogamp.opencl.CLDevice.Type.CPU;
import static com.jogamp.opencl.CLDevice.Type.GPU;
import static com.jogamp.opencl.util.CLPlatformFilters.type;
import static com.jogamp.opencl.CLMemory.Mem.*;
import static java.lang.System.nanoTime;

/**
 * Rendering thread.  Regenerate the image when navigation changes.
 *
 * @author Alan Hudson
 */
public class RenderCanvas implements GLEventListener {
    private int width;
    private int height;

    private Navigator nav;
    private transient boolean windowChanged = false;
    private VolumeRenderer renderer;
    private CLDevice device;
    private CLGLContext clContext;
    private CLKernel kernel;
    private CLProgram program;
    private CLCommandQueue commandQueue;
    private GLAutoDrawable drawable;
    private Animator animator;
    private CLGLBuffer clPixelBuffer;
    private int[] glPixelBuffer = new int[1];
    private long globalWorkSizeX;
    private long globalWorkSizeY;
    private int localWorkSizeX;
    private int localWorkSizeY;
    private transient boolean rendering;

    // Scratch vars
    private Matrix4f view = new Matrix4f();
    private float[] viewData = new float[16];
    private CLBuffer<FloatBuffer> viewBuffer;

    private final GLCanvas canvas;

    public RenderCanvas(Navigator nav) {
        this.nav = nav;

        GLCapabilities config = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        config.setSampleBuffers(true);
        config.setNumSamples(4);

        canvas = new GLCanvas(config);
        canvas.addGLEventListener(this);
        nav.init(canvas);
    }

    public Component getComponent() {
        return canvas;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        printf("Init canvas\n");
        if (clContext != null) return;

        this.drawable = drawable;
        boolean debug = false;

        if (debug) {
            device = CLPlatform.getDefault(type(CPU)).getMaxFlopsDevice();
        } else {
            device = CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice();
        }

        if (!device.isGLMemorySharingSupported()) {
            CLDevice[] devices = CLPlatform.getDefault().listCLDevices();
            for (CLDevice d : devices) {
                if (d.isGLMemorySharingSupported()) {
                    device = d;
                    break;
                }
            }
        }

        if(device == null) {
            throw new RuntimeException("couldn't find any CL/GL memory sharing devices ..");
        }

        clContext = CLGLContext.create(drawable.getContext(), device);
        // enable GL error checking using the composable pipeline
        drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));

        // OpenGL initialization
        GL2 gl = drawable.getGL().getGL2();

        gl.setSwapInterval(1);

        gl.glFinish();

        int bsz = width * height;
        // init OpenCL
        initCL(gl, bsz,debug);

        // start rendering thread
        animator = new Animator(drawable);
        animator.start();
    }

    private void initCL(GL2 gl, int bufferSize, boolean debug) {
        printf("initCL called\n");
        try {

            String buildOpts = "";
            if (debug) buildOpts += " -DDEBUG";

            program = ProgramLoader.load(clContext, "VolumeRenderer.cl");
            program.build(buildOpts);

            System.out.println(program.getBuildStatus());
            System.out.println(program.isExecutable());
            System.out.println(program.getBuildLog());
        } catch (IOException ex) {
            throw new RuntimeException("can not handle exception", ex);
        }

        commandQueue = device.createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);

        kernel = program.createCLKernel("render");

        viewBuffer = clContext.createFloatBuffer(16, READ_ONLY);

        System.out.println("cl initialised");
    }

    private void initPixelBuffer(GL2 gl, int width, int height) {

        if (clPixelBuffer != null) {
            // release old buffer
            clPixelBuffer.release();
            gl.glDeleteBuffers(1,glPixelBuffer,0);
            clPixelBuffer = null;
        }

        int bufferSize = width * height * 4 * Buffers.SIZEOF_BYTE;
        gl.glGenBuffers(1,glPixelBuffer,0);
        gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER, glPixelBuffer[0]);
        gl.glBufferData(GL2.GL_PIXEL_UNPACK_BUFFER, bufferSize, null, GL2.GL_STREAM_DRAW);
        gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER, 0);
        clPixelBuffer = clContext.createFromGLBuffer(glPixelBuffer[0],
                bufferSize, WRITE_ONLY);

    }

    @Override
    public void display(GLAutoDrawable drawable) {
        if (!rendering && !nav.hasChanged() && !windowChanged) return;

        rendering = true;
        windowChanged = false;

        GL2 gl = drawable.getGL().getGL2();

        // ensure pipeline is clean before doing cl work
        gl.glFinish();

        nav.getViewMatrix(view);
        // Invert and send current view matrix to OpenCL
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

        commandQueue.putAcquireGLObject(clPixelBuffer, list);
        commandQueue.putWriteBuffer(viewBuffer, true, null, list);

        kernel.setArg(0,clPixelBuffer).rewind();
        kernel.setArg(1,width);
        kernel.setArg(2,height);
        kernel.setArg(3,viewBuffer).rewind();

        commandQueue.put2DRangeKernel(kernel, 0, 0, globalWorkSizeX, globalWorkSizeY, localWorkSizeX, localWorkSizeY, list);
        commandQueue.putReleaseGLObject(clPixelBuffer, list);
        commandQueue.finish();

        /*
        for(int i=0; i < list.size(); i++) {
            CLEvent event = list.getEvent(i);
            System.out.println("cmd: " + i + " time: " + (event.getProfilingInfo(CLEvent.ProfilingCommand.END)
                    - event.getProfilingInfo(CLEvent.ProfilingCommand.START))/1000000.0);
        }
        */
        // Render image using OpenGL

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        gl.glDisable(GL2.GL_DEPTH_TEST);
//        gl.glRasterPos2i(0,0);
        gl.glRasterPos2i(-1,-1);  // TODO: different then example not sure why necessary but it does center it
        gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER, glPixelBuffer[0]);
        gl.glDrawPixels(width, height, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, 0);
        gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER, 0);

        rendering = false;
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    public void reshape(GLAutoDrawable drawable, int arg1, int arg2, int width, int height) {
        printf("Window reshaped: %d x %d\n", width, height);
        this.width = width;
        this.height = height;

        int max_wg_size = device.getMaxWorkGroupSize();

        // TODO: optimize these
        localWorkSizeX = 16;
        localWorkSizeY = 16;
        globalWorkSizeX = GPUUtil.roundUp(localWorkSizeX,width);
        globalWorkSizeY = GPUUtil.roundUp(localWorkSizeY,height);

        windowChanged = true;

        GL2 gl = drawable.getGL().getGL2();
        initPixelBuffer(gl,width,height);
    }

    private static void fillBuffer(FloatBuffer buffer, int seed) {
        Random rnd = new Random(seed);
        while(buffer.remaining() != 0)
            buffer.put(rnd.nextFloat()*100);
        buffer.rewind();
    }

    public void setNavigator(Navigator nav) {
        printf("Ignoring setNavigator for now\n");
       // this.nav = nav;
    }

    public void terminate() {
        animator.stop();
    }
}