package viewer;

import abfab3d.param.SNode;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.*;
import com.jogamp.opencl.gl.CLGLBuffer;
import com.jogamp.opencl.gl.CLGLContext;
import com.jogamp.opengl.util.Animator;
import datasources.Instruction;
import render.VolumeRenderer;
import render.VolumeScene;

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
import static com.jogamp.opencl.util.CLPlatformFilters.type;
import static com.jogamp.opencl.CLMemory.Mem.*;

/**
 * Rendering thread.  Regenerate the image when navigation changes.
 *
 * @author Alan Hudson
 */
public class SingleDeviceRenderCanvas implements RenderCanvas {
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

    private VolumeScene vscene; 
    //private String sceneProg;
    //private List<Instruction> instructions;

    private boolean sceneLoaded = false;
    private boolean graphicsInitialized = false;
    //private float worldScale=1;
    private boolean firstRender = true;

    // Scratch vars
    private Matrix4f view = new Matrix4f();
    private CLBuffer<FloatBuffer> viewBuffer;
    private final GLCanvas canvas;
    private String renderVersion = VolumeRenderer.VERSION_DIST;

    private NumberFormat format = new DecimalFormat("####.#");

    public SingleDeviceRenderCanvas(Navigator nav) {
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

        renderer.setMaxAntialiasingSteps(numSteps);
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

        renderer.setMaxSteps(numSteps);
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

        renderer.setMaxShadowSteps(numSteps);
        statusBar.setStatusText("Loading program...");
        canvas.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                buildProgram(debug);
            }
        });
    }

    /**
       @Override
    */
    public void setScene(VolumeScene vscene){ 
        // public void setScene(String scene, List<Instruction> instructions, float worldScale) {
        this.vscene = vscene;
        //this.worldScale = worldScale;
        //this.instructions = instructions;
        // Wait for the graphics to initialize
        while(!graphicsInitialized) {
            try { Thread.sleep(50); } catch(InterruptedException ie) {}
        }
        //sceneProg = scene;

        buildProgram(debug);

        sceneLoaded = true;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        printf("Init canvas: debug: %b\n",debug);
        if (clContext != null) return;

        this.drawable = drawable;

        CLPlatform platform = CLPlatform.getDefault();

        if (platform.getName().contains("Apple")) {
            // Intel drivers seem to be borked, force to GeForce
            CLDevice[] devices = CLPlatform.getDefault().listCLDevices();
            System.out.printf("CL Devices: %d\n",devices.length);
            for (CLDevice d : devices) {
                System.out.printf("Device: %s\n",d);

                if (d.getName().contains("GeForce")) {
                    device = d;
                    break;
                }
                /*
                if (d.isGLMemorySharingSupported()) {
                    device = d;
                    break;
                }
                */
            }

        } else {
            if (debug) {
                device = CLPlatform.getDefault(type(CPU)).getMaxFlopsDevice();
            } else {
                device = CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice();
/*
                CLDevice[] devices = CLPlatform.getDefault(type(GPU)).listCLDevices();
                System.out.printf("CL Devices: %d\n",devices.length);
                for (CLDevice d : devices) {
                    System.out.printf("Device: %s\n",d);

                    if (d.getName().contains("GTX 780")) {
                        device = d;
                        break;
                    }
                }
  */
            }
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

        printf("Using device: %s\n",device);
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
        
        //if (sceneProg == null && instructions == null) return;

        if (debug) vscene.opts += " -DDEBUG";

        //ArrayList progs = new ArrayList();
        //if (sceneProg != null) progs.add(sceneProg);

        compiling = true;

        printf("Building using programs: renderVersion: %s\n",renderVersion);
        //for(int i=0; i < vscene.progs.size(); i++) {
        //    printf("\t%s\n",vscene.progs.get(i));
        //}

        if (!renderer.init(vscene)) {
            CLProgram program = renderer.getProgram();
            statusBar.setStatusText("Program failed to load: " + program.getBuildStatus());
            System.out.println(program.getBuildStatus());
            System.out.println(program.getBuildLog());
            return;
        }

        if (debug) {
            CLProgram program = renderer.getProgram();
            System.out.println(program.getBuildStatus());
            System.out.println(program.getBuildLog());
        }


        statusBar.setStatusText("Done loading program.  compile time: " + format.format((renderer.getLastCompileTime()) / 1e6) + " ms");
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
        view.invert();  // TODO: let opencl do this?

        int w = width;
        //w = 16;
        int h = height;
        
        if (renderVersion.equals(VolumeRenderer.VERSION_OPCODE_V3_DIST )) {

            // opcode v3
            renderer.sendView(view,viewBuffer);
            renderer.renderStruct(0,0, w,h, w,h, vscene.getWorldScale(), clPixelBuffer);
            
        } else if (renderVersion.equals(VolumeRenderer.VERSION_OPCODE) ||
                   renderVersion.equals(VolumeRenderer.VERSION_OPCODE_V2) ||
                   renderVersion.equals(VolumeRenderer.VERSION_OPCODE_V2_DIST)) {
            // opcode 
            renderer.sendView(view,viewBuffer);
            renderer.renderOps(0,0,w,h,w, h, vscene.getWorldScale(), clPixelBuffer);
        } else { 
            // text program 
            renderer.render(view, w, h, viewBuffer, commandQueue, clPixelBuffer);
        }
        // Render image using OpenGL

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        gl.glDisable(GL2.GL_DEPTH_TEST);
        gl.glRasterPos2i(0,0);
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

        graphicsInitialized = false;
        printf("Window reshaped: %d x %d\n", width, height);
        this.width = width;
        this.height = height;


        windowChanged = true;

        GL2 gl = drawable.getGL().getGL2();
        initPixelBuffer(gl,width,height);

        graphicsInitialized = true;
    }

    /**
       @Override
    */
    public long getLastRenderTime() {
        return renderer.getLastTotalRenderTime();
    }

    /**
       @Override
    */
    public long getLastKernelTime() {
        return renderer.getLastKernelTime();
    }

    /**
       @Override
    */
    public void setNavigator(Navigator nav) {
        printf("Ignoring setNavigator for now\n");
       // this.nav = nav;
    }

    @Override
    public void terminate() {
        animator.stop();
    }
}