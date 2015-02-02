package viewer;

import abfab3d.param.SNode;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.*;
import com.jogamp.opencl.gl.CLGLBuffer;
import com.jogamp.opencl.gl.CLGLContext;
import com.jogamp.opengl.util.Animator;
import datasources.Instruction;
import render.VolumeRenderer;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.*;
import javax.vecmath.Matrix4f;
import java.awt.*;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static abfab3d.util.Output.printf;
import static com.jogamp.opencl.CLDevice.Type.CPU;
import static com.jogamp.opencl.CLDevice.Type.GPU;
import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.WRITE_ONLY;
import static com.jogamp.opencl.util.CLPlatformFilters.type;
import static java.lang.System.out;

/**
 * Rendering thread.  Regenerate the image when navigation changes.
 *
 * @author Alan Hudson
 */
public class MultiDeviceRenderCanvas implements RenderCanvas {
    private boolean debug = false;  // forces CPU for printf

    private int width;
    private int height;
    private int maxSteps = StepsAction.getDefaultNumberOfSteps();
    private int maxShadowSteps = ShadowStepsAction.getDefaultNumberOfSteps();
    private int maxAntialiasingSteps = AntialiasingAction.getDefaultNumberOfSteps();

    private Navigator nav;
    private StatusBar statusBar;
    private transient boolean windowChanged = false;
    private transient boolean compiling = false;
    private VolumeRenderer[] renderer;
    private CLDevice glDevice;
    private CLDevice[] devices;
    private int numDevices;
    private CLGLContext glContext;
    private CLContext[] contexts;
    private CLCommandQueue glCommandQueue;
    private CLCommandQueue[] commandQueues;
    private GLAutoDrawable drawable;
    private Animator animator;
    private CLGLBuffer[] clPixelBuffer;
    private int[] glPixelBuffer;
    private CLBuffer[] clBuffer;
    private transient boolean rendering;
    private SNode scene;
    private String sceneProg;
    private List<Instruction> instructions;
    private boolean sceneLoaded = false;
    private boolean graphicsInitialized = false;
    private float worldScale=1;
    private boolean firstRender = true;

    // Scratch vars
    private Matrix4f view = new Matrix4f();
    private CLBuffer<FloatBuffer> glViewBuffer;
    private CLBuffer<FloatBuffer>[] viewBuffers;
    private final GLCanvas canvas;
    private String renderVersion = VolumeRenderer.VERSION_DIST;

    private NumberFormat format = new DecimalFormat("####.#");

    public MultiDeviceRenderCanvas(Navigator nav) {
        this.nav = nav;
        GLCapabilities config = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        config.setSampleBuffers(true);
        config.setNumSamples(2);

        canvas = new GLCanvas(config);
        canvas.addGLEventListener(this);
        nav.init(canvas);
    }

    @Override
    public void setRenderVersion(String st) {
        renderVersion = st;
    }

    @Override
    public void setStatusBar(StatusBar status) {
        this.statusBar = status;
    }

    @Override
    public Component getComponent() {
        return canvas;
    }

    @Override
    public FPSCounter getCounter() {
        return animator;
    }

    @Override
    public void setAntialiasingSteps(int numSteps) {
        this.maxAntialiasingSteps = numSteps;

        for(int i=0; i < numDevices; i++) {
            renderer[i].setMaxAntialiasingSteps(numSteps);
        }
        statusBar.setStatusText("Loading program...");
        canvas.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                buildProgram(debug);
            }
        });
    }

    @Override
    public void setSteps(int numSteps) {
        this.maxSteps = numSteps;

        for(int i=0; i < numDevices; i++) {
            renderer[i].setMaxSteps(numSteps);
        }
        statusBar.setStatusText("Loading program...");
        canvas.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                buildProgram(debug);
            }
        });
    }

    @Override
    public void setShadowSteps(int numSteps) {
        this.maxShadowSteps = numSteps;

        for(int i=0; i < numDevices; i++) {
            renderer[i].setMaxShadowSteps(numSteps);
        }
        statusBar.setStatusText("Loading program...");
        canvas.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                buildProgram(debug);
            }
        });
    }

    @Override
    public void setScene(String scene, List<Instruction> instructions, float worldScale) {
        this.worldScale = worldScale;
        this.instructions = instructions;
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
        if (glContext != null) return;

        this.drawable = drawable;

        CLPlatform platforms[] = CLPlatform.listCLPlatforms();
        CLPlatform platform = null;
        for(int i =0; i < platforms.length; i++){
            out.printf("platform[%d]: %s\n", i, platforms[i]);
            if (platforms[i].getICDSuffix().equals("NV")) {
                platform = platforms[i];
            }
            CLDevice devices[] = platforms[i].listCLDevices();
            for(int k = 0; k < devices.length;k++){
                out.printf("  device[%d]: %s\n", k, devices[k]);
            }
        }

        devices = platform.listCLDevices();
        CLContext context = CLContext.create(devices);

        if (platform.getName().contains("Apple")) {
            // Intel drivers seem to be borked, force to GeForce
            CLDevice[] devices = CLPlatform.getDefault().listCLDevices();
            System.out.printf("CL Devices: %d\n",devices.length);
            for (CLDevice d : devices) {
                System.out.printf("Device: %s\n",d);

                if (d.getName().contains("GeForce")) {
                    glDevice = d;
                    devices = new CLDevice[] {d};
                    break;
                }
            }

        } else {
            if (debug) {
                glDevice = CLPlatform.getDefault(type(CPU)).getMaxFlopsDevice();
                devices = new CLDevice[] {CLPlatform.getDefault(type(CPU)).getMaxFlopsDevice() };
            } else {
                // Do not try and meld all devices into one context, gives an CL_INVALID_OPERATION
                devices = CLPlatform.getDefault(type(GPU)).listCLDevices();

                CLDevice max = null;
                int flops = 0;

                for(int i=0; i < devices.length; i++) {
                    int maxComputeUnits     = devices[i].getMaxComputeUnits();
                    int maxClockFrequency   = devices[i].getMaxClockFrequency();

                    if (maxClockFrequency * maxComputeUnits > flops) {
                        max = devices[i];
                        flops = maxClockFrequency * maxComputeUnits;
                    }
                }

                glDevice = max;

                CLDevice[] tdevices = new CLDevice[devices.length - 1];
                int idx = 0;
                for(int i=0; i < devices.length; i++) {
                    if (devices[i] == glDevice) {
                        continue;
                    }

                    tdevices[idx++] = devices[i];
                }

                devices = tdevices;
            }
        }

        numDevices = devices.length;

        /*
        if (!device.isGLMemorySharingSupported()) {
            CLDevice[] devices = CLPlatform.getDefault().listCLDevices();
            for (CLDevice d : devices) {
                if (d.isGLMemorySharingSupported()) {
                    device = d;
                    break;
                }
            }
        }
        */
        if(devices == null) {
            throw new RuntimeException("couldn't find any CL/GL memory sharing devices ..");
        }

        // TODO: Why can't we create one context, grrr...

        //        glContext = CLGLContext.create(drawable.getContext(), devices);

        glContext = CLGLContext.create(drawable.getContext(), glDevice);
        contexts = new CLContext[numDevices];

        for(int i=0; i < numDevices; i++) {
            contexts[i] = CLContext.create(devices[i]);
        }
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
        glCommandQueue = glDevice.createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);
        commandQueues = new CLCommandQueue[numDevices];
        renderer = new VolumeRenderer[numDevices+1];

        renderer[0] = new VolumeRenderer(glContext,glCommandQueue);
        renderer[0].setMaxSteps(maxSteps);
        renderer[0].setMaxShadowSteps(maxShadowSteps);
        renderer[0].setMaxAntialiasingSteps(maxAntialiasingSteps);

        for(int i=0; i < devices.length; i++) {
            commandQueues[i] = devices[i].createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);
            renderer[i+1] = new VolumeRenderer(contexts[i],commandQueues[i]);
            renderer[i+1].setMaxSteps(maxSteps);
            renderer[i+1].setMaxShadowSteps(maxShadowSteps);
            renderer[i+1].setMaxAntialiasingSteps(maxAntialiasingSteps);
        }

        viewBuffers = new CLBuffer[numDevices + 1];
        glViewBuffer = glContext.createFloatBuffer(16, READ_ONLY);

        for(int i=0; i < numDevices; i++) {
            viewBuffers[i] = contexts[i].createFloatBuffer(16, READ_ONLY);
        }
        System.out.println("cl initialised");
    }

    private void buildProgram(boolean debug) {
        if (sceneProg == null && instructions == null) return;

        String opts = "";
        if (debug) opts = " -DDEBUG";
        ArrayList progs = new ArrayList();
        if (sceneProg != null) progs.add(sceneProg);
        compiling = true;

        printf("Building using programs: renderVersion: %s\n",renderVersion);
        for(int i=0; i < progs.size(); i++) {
            printf("\t%s\n",progs.get(i));
        }

        for(int i=0; i < renderer.length; i++) {
            if (!renderer[i].init(progs, instructions, opts, renderVersion)) {
                CLProgram program = renderer[i].getProgram();
                statusBar.setStatusText("Program failed to load: " + program.getBuildStatus());
                System.out.println(program.getBuildStatus());
                System.out.println(program.getBuildLog());
                return;
            }

            if (debug) {
                CLProgram program = renderer[i].getProgram();
                System.out.println(program.getBuildStatus());
                System.out.println(program.getBuildLog());
            }
        }

        statusBar.setStatusText("Done loading program.  compile time: " + format.format((renderer[0].getLastCompileTime()) / 1e6) + " ms");
        compiling = false;

        canvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        windowChanged = true;
    }
    private void initPixelBuffer(GL2 gl, int width, int height) {

        if (glPixelBuffer == null) {
            glPixelBuffer = new int[numDevices+1];
        }

        if (clPixelBuffer != null) {
            for(int i=0; i < numDevices; i++) {
                // release old buffer
                clPixelBuffer[i].release();
            }
            gl.glDeleteBuffers(numDevices+1,glPixelBuffer,0);
            clPixelBuffer = null;
        }

        if (clBuffer != null) {
            for(int i=0; i < numDevices; i++) {
                // release old buffer
                clBuffer[i].release();
            }
            clBuffer = null;
        }

        int bufferSize = width * height * 4 * Buffers.SIZEOF_BYTE;

        gl.glGenBuffers(numDevices+1,glPixelBuffer,0);

        for(int i=0; i < numDevices + 1; i++) {
            gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER, glPixelBuffer[i]);
            gl.glBufferData(GL2.GL_PIXEL_UNPACK_BUFFER, bufferSize, null, GL2.GL_STREAM_DRAW);
            gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER, 0);
        }

        clPixelBuffer = new CLGLBuffer[numDevices+1];
        clPixelBuffer[0] = glContext.createFromGLBuffer(glPixelBuffer[0], bufferSize, WRITE_ONLY);
        clBuffer = new CLBuffer[numDevices];

        for(int i=0; i < numDevices; i++) {
            clBuffer[i] = contexts[i].createByteBuffer(bufferSize);
            clPixelBuffer[i+1] = glContext.createFromGLBuffer(glPixelBuffer[i+1], bufferSize, WRITE_ONLY);
        }
    }

    @Override
    public void display(GLAutoDrawable drawable) {

        if (!sceneLoaded || compiling || rendering) {
            return;
        }
        boolean navChanged = nav.hasChanged();

        if (!navChanged && !windowChanged) {
            if (firstRender)
                printf("scene: %b  compile: %b rendering: %b nav: %b wc: %b\n", sceneLoaded, compiling, rendering, navChanged,windowChanged);
            return;
        } else {
            //printf("Rendering: wc: %b\n",windowChanged);
        }

        rendering = true;
        windowChanged = false;
        firstRender = false;

        GL2 gl = drawable.getGL().getGL2();

        // ensure pipeline is clean before doing cl work
        gl.glFinish();

        nav.getViewMatrix(view);

        int w = width;
        int h = height;

        int wsize = w;
        int hsize = h / 2;

        renderer[0].renderOps(view, (0*hsize), (0*hsize), wsize, hsize, w, h, glViewBuffer, worldScale, glCommandQueue, clPixelBuffer[0]);
        for(int i=0; i < numDevices; i++) {
            renderer[i+1].renderOps(view, 0, ((i+1)*hsize), wsize, hsize, w, h, viewBuffers[i], worldScale, commandQueues[i], clBuffer[i]);
        }

        // Render image using OpenGL

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        gl.glDisable(GL2.GL_DEPTH_TEST);

        gl.glRasterPos2i(-1,-1);
        gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER, glPixelBuffer[0]);
        gl.glDrawPixels(width, height, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, 0);
        gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER, 0);

        // TODO: Need to generalize
        if (numDevices > 0) {
            // Need to copy the buffer from the device to host and back to openGL
            commandQueues[0].putReadBuffer(clBuffer[0],true);
            glCommandQueue.putWriteBuffer(clPixelBuffer[1],true);
            gl.glRasterPos2i(-1, 0);
            gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER, glPixelBuffer[1]);
            gl.glDrawPixels(width, height, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, 0);
            gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER, 0);
        }

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

        graphicsInitialized = false;
        printf("Window reshaped: %d x %d\n", width, height);
        this.width = width;
        this.height = height;


        windowChanged = true;

        GL2 gl = drawable.getGL().getGL2();
        initPixelBuffer(gl,width,height);

        graphicsInitialized = true;
    }

    @Override
    public long getLastRenderTime() {
        return renderer[0].getLastTotalRenderTime();
    }

    @Override
    public long getLastKernelTime() {
        return renderer[0].getLastKernelTime();
    }

    @Override
    public void setNavigator(Navigator nav) {
        printf("Ignoring setNavigator for now\n");
       // this.nav = nav;
    }

    @Override
    public void terminate() {
        animator.stop();
    }
}