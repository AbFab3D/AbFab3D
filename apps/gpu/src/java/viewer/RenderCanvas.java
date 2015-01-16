package viewer;

import abfab3d.param.SNode;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.*;
import com.jogamp.opencl.gl.CLGLBuffer;
import com.jogamp.opencl.gl.CLGLContext;
import com.jogamp.opengl.util.Animator;
import render.VolumeRenderer;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.*;
import javax.vecmath.Matrix4f;

import java.awt.*;
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
    private CLCommandQueue commandQueue;
    private GLAutoDrawable drawable;
    private Animator animator;
    private CLGLBuffer clPixelBuffer;
    private int[] glPixelBuffer = new int[1];
    private transient boolean rendering;
    private SNode scene;
    private String sceneProg;
    private boolean sceneLoaded = false;
    private boolean graphicsInitialized = false;
    private float worldScale=1;

    // Scratch vars
    private Matrix4f view = new Matrix4f();
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

        renderer.setMaxAntialiasingSteps(numSteps);
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

        renderer.setMaxSteps(numSteps);
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

        renderer.setMaxShadowSteps(numSteps);
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

        renderer = new VolumeRenderer(clContext,commandQueue);
        renderer.setMaxSteps(maxSteps);
        renderer.setMaxShadowSteps(maxShadowSteps);
        renderer.setMaxAntialiasingSteps(maxAntialiasingSteps);
        viewBuffer = clContext.createFloatBuffer(16, READ_ONLY);

        System.out.println("cl initialised");
    }

    private void buildProgram(boolean debug) {
        if (sceneProg == null) return;

        String opts = "";
        if (debug) opts = " -DDEBUG";
        ArrayList progs = new ArrayList();
        progs.add(sceneProg);
        compiling = true;
        if (!renderer.init(progs, opts)) {
            CLProgram program = renderer.getProgram();
            statusBar.setStatusText("Program failed to load: " + program.getBuildStatus());
            System.out.println(program.getBuildStatus());
            System.out.println(program.getBuildLog());
            return;
        }

        statusBar.setStatusText("Done loading program.  compile time: " + (renderer.getLastCompileTime()) / 1e6 + " secs");
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
        renderer.render(view, width, height, viewBuffer,commandQueue,clPixelBuffer);

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

    public long getLastRenderTime() {
        return renderer.getLastTotalRenderTime();
    }

    public long getLastKernelTime() {
        return renderer.getLastKernelTime();
    }

    public void setNavigator(Navigator nav) {
        printf("Ignoring setNavigator for now\n");
       // this.nav = nav;
    }

    public void terminate() {
        animator.stop();
    }
}