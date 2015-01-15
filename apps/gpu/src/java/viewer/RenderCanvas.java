package viewer;

import abfab3d.param.SNode;
import abfab3d.util.Units;
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
import javax.swing.*;
import javax.vecmath.Matrix4f;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Random;

import static abfab3d.util.Output.printf;
import static com.jogamp.opencl.CLDevice.Type.CPU;
import static com.jogamp.opencl.CLDevice.Type.GPU;
import static com.jogamp.opencl.util.CLPlatformFilters.type;
import static com.jogamp.opencl.CLMemory.Mem.*;

/**
 * Rendering thread.  Regenerate the image when navigation changes.
 *
 * @author Alan Hudson
 */
public class RenderCanvas implements GLEventListener {
    private boolean debug = false;

    private int width;
    private int height;
    private int maxSteps = StepsAction.getDefaultNumberOfSteps();
    private int maxShadowSteps = ShadowStepsAction.getDefaultNumberOfSteps();
    private int maxAntialiasingSteps = AntialiasingAction.getDefaultNumberOfSteps();

    private Navigator nav;
    private StatusBar statusBar;
    private transient boolean windowChanged = false;
    private transient boolean compiling = false;
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
    private SNode scene;
    private String sceneProg;
    private boolean sceneLoaded = false;
    private boolean graphicsInitialized = false;
    private float worldScale=1;

    // Scratch vars
    private Matrix4f view = new Matrix4f();
    private float[] viewData = new float[16];
    private CLBuffer<FloatBuffer> viewBuffer;

    private final GLCanvas canvas;

    public RenderCanvas(Navigator nav) {
        this.nav = nav;
        GLCapabilities config = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        config.setSampleBuffers(true);
        config.setNumSamples(2);

        canvas = new GLCanvas(config);
        canvas.addGLEventListener(this);
        nav.init(canvas);
    }

    public void setStatusBar(StatusBar status) {
        this.statusBar = status;
    }

    public Component getComponent() {
        return canvas;
    }

    public FPSCounter getCounter() {
        return animator;
    }

    public void setAntialiasingSteps(int numSteps) {
        this.maxAntialiasingSteps = numSteps;

        statusBar.setStatusText("Loading program...");
        canvas.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                buildProgram(debug);
            }
        });
    }

    public void setSteps(int numSteps) {
        this.maxSteps = numSteps;

        statusBar.setStatusText("Loading program...");
        canvas.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                buildProgram(debug);
            }
        });
    }

    public void setShadowSteps(int numSteps) {
        this.maxShadowSteps = numSteps;

        statusBar.setStatusText("Loading program...");
        canvas.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                buildProgram(debug);
            }
        });
    }

    public void setScene(String scene, float worldScale) {
        this.worldScale = worldScale;

        // Wait for the graphics to initialize
        while(!graphicsInitialized) {
            try { Thread.sleep(50); } catch(InterruptedException ie) {}
        }

        sceneProg = scene;

        buildProgram(debug);

        sceneLoaded = true;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        printf("Init canvas\n");
        if (clContext != null) return;

        this.drawable = drawable;

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

        graphicsInitialized = true;

        // start rendering thread
        animator = new Animator(drawable);
        animator.setUpdateFPSFrames(3, null);
        animator.start();
    }

    private void initCL(GL2 gl, int bufferSize, boolean debug) {
        printf("initCL called\n");
        commandQueue = device.createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);

        viewBuffer = clContext.createFloatBuffer(16, READ_ONLY);

        System.out.println("cl initialised");
    }

    private void buildProgram(boolean debug) {

        if (sceneProg == null) return;

        String kernel_name = "render";

        compiling = true;
        long t0 = System.currentTimeMillis();
        try {
            printf("Building program with maxSteps: %s debug: %b\n",maxSteps,debug);
            String buildOpts = "";
            if (debug) buildOpts += " -DDEBUG";
            buildOpts += " -DmaxSteps=" + maxSteps;
//            double vs = (2 * 2.0/maxSteps * worldScale);  // TODO: not sure this is right
            double vs = 0.1* Units.MM;
            printf("voxelSize: %f\n",vs);
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

            //program = ProgramLoader.load(clContext, "VolumeRenderer.cl");
            ArrayList list = new ArrayList();
            //list.add(new File("Noise.cl"));
            list.add(new File("ShapeJS.cl"));
            list.add(sceneProg);
            list.add(new File("VolumeRenderer.cl"));
            program = ProgramLoader.load(clContext,list);
            program.build(buildOpts);

            System.out.println(program.getBuildStatus());
            System.out.println(program.isExecutable());
            System.out.println(program.getBuildLog());
        } catch (IOException ex) {
            statusBar.setStatusText("Exception building program");
            throw new RuntimeException("can not handle exception", ex);
        }

        kernel = program.createCLKernel(kernel_name);
        statusBar.setStatusText("Done loading program.  compile time: " + (System.currentTimeMillis() - t0) / 1000 + " secs");
        compiling = false;

        canvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        windowChanged = true;
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
        if (!sceneLoaded || compiling || rendering || (!nav.hasChanged() && !windowChanged)) return;

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
        if (this.width == width && this.height == height && clPixelBuffer != null) {
            // ignore, not sure why we get these
            return;
        }
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