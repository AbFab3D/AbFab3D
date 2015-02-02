/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package render;


import abfab3d.grid.Bounds;
import abfab3d.param.Parameterizable;
import abfab3d.util.DataSource;
import com.jogamp.opencl.*;
import com.objectplanet.image.PngEncoder;
import datasources.Instruction;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import shapejs.ShapeJSEvaluator;
import viewer.OpenCLOpWriterV2;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static abfab3d.util.Output.printf;
import static com.jogamp.opencl.CLDevice.Type.CPU;
import static com.jogamp.opencl.CLDevice.Type.GPU;
import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.util.CLPlatformFilters.type;

/**
 * Tests the functionality of ShapeJSExecutor
 *
 * @author Alan Hudson
 */
public class TestVolumeRenderer extends TestCase {
    private static final boolean DEBUG = false;
    public static final String VERSION = VolumeRenderer.VERSION_OPCODE_V2_DIST;

    private int numJobs;
    private RenderJob[] jobs;
    private int numRenderers;
    private VolumeRenderer[] render;
    private int numDevices;
    private float worldScale;
    private long lastLoadScriptTime;
    private long lastImageTime;
    private long lastPngTime;
    private long lastKernelTime;
    private HashMap<String, List<Instruction>> cache = new HashMap<String, List<Instruction>>();

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestVolumeRenderer.class);
    }

    /**
     * Look at using GPU to make a jpg directly, could save 14ms or about 50%
     */
    public void testSpeed() {
        int width = 512;
        int height = 512;

        long t0 = System.nanoTime();

        // Do these items once for the servlet.  Should be per-thread resources
        initCL(false, width, height);
        PngEncoder encoder = new PngEncoder(PngEncoder.COLOR_TRUECOLOR, PngEncoder.BEST_SPEED);
//        int MAX_IMG_SIZE = 150000;
        int MAX_IMG_SIZE = width*height;
        byte[] buff = new byte[MAX_IMG_SIZE];
        int[] pixels = new int[width*height];
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        printf("initCL time: %d ms\n", (int) ((System.nanoTime() - t0) / 1e6));

        // End of per-thread resources

        String jobID = UUID.randomUUID().toString();

        int TIMES = 3;

        if (DEBUG) TIMES = 1;

//        String script = "scripts/dodecahedron.js";
        String script = "scripts/gyrosphere.js";
        for (int i = 0; i < TIMES; i++) {
            t0 = System.nanoTime();
            BufferedImage base = render(jobID, script, width, height,pixels,image);
            int size = makePng(encoder,base,buff);  // return buff[0] to size bytes to client

            printf("total time: %d ms\n\tjs eval: %d\n\tocl compile: %d ms\n\tkernel: %d ms\n\timage: %d ms\n\tpng: %d ms\n", (int) ((System.nanoTime() - t0) / 1e6), (int) (lastLoadScriptTime / 1e6), (int) (jobs[0].getRenderer().getLastCompileTime() / 1e6), (int) (jobs[0].getRenderer().getLastKernelTime() / 1e6), (int) (lastImageTime / 1e6), (int) (lastPngTime / 1e6));

            if (DEBUG) {
                try {
                    ImageIO.write(base, "png", new File("/tmp/render_speed.png"));
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

        }
    }

    /**
     * Look at using GPU to make a jpg directly, could save 14ms or about 50%
     */
    public void testMultiDevice() {
        int width = 1024;
        int height = 1024;

        long t0 = System.nanoTime();

        // Do these items once for the servlet.  Should be per-thread resources
        initMultiCL(width, height);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        PngEncoder encoder = new PngEncoder(PngEncoder.COLOR_TRUECOLOR, PngEncoder.BEST_SPEED);
//        int MAX_IMG_SIZE = 150000;
        int MAX_IMG_SIZE = width*height;
        byte[] buff = new byte[MAX_IMG_SIZE];
        int[] pixels = new int[width*height];
        printf("initCL time: %d ms\n", (int) ((System.nanoTime() - t0) / 1e6));

        // End of per-thread resources

        String jobID = UUID.randomUUID().toString();

        int TIMES = 3;

        if (DEBUG) TIMES = 1;

        long compileTime = -1;
        String script = "scripts/dodecahedron.js";
//        String script = "scripts/gyrosphere.js";

        for (int i = 0; i < TIMES; i++) {
            t0 = System.nanoTime();
            BufferedImage base = renderMT(jobID, script, width, height, pixels,image);
            int size = makePng(encoder,base,buff);  // return buff[0] to size bytes to client

            printf("total time: %d ms\n\tjs eval: %d\n\tocl compile: %d ms\n\tkernel: %d ms\n\timage: %d ms\n\tpng: %d ms\n", (int) ((System.nanoTime() - t0) / 1e6), (int) (lastLoadScriptTime / 1e6), (int) (compileTime / 1e6), (int) (lastKernelTime / 1e6), (int) (lastImageTime / 1e6), (int) (lastPngTime / 1e6));

            if (DEBUG) {
                try {
                    ImageIO.write(base, "png", new File("/tmp/render_speed.png"));
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

        }
    }

    public void testTranslation() {
        int width = 512;
        int height = 512;

        initCL(true, width, height);


        long t0 = System.nanoTime();
        BufferedImage base = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage test = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        render("test/scripts/transform_base.js", width, height, base);
        printf("total time: %d ms\n\tjs eval: %d\n\tocl compile: %d ms\n\tkernel: %d ms\n\timage: %d\n", (int) ((System.nanoTime() - t0) / 1e6), (int) (lastLoadScriptTime / 1e6), (int) (jobs[0].getRenderer().getLastCompileTime() / 1e6), (int) (jobs[0].getRenderer().getLastKernelTime() / 1e6), (int) (lastImageTime / 1e6));
        render("test/scripts/translation.js", width, height, test);

        assertFalse("Constant image", ImageUtilTest.isConstantImage(test));
        assertFalse("Same image", ImageUtilTest.isImageEqual(base, test));

        if (DEBUG) {
            try {
                ImageIO.write(base, "png", new File("/tmp/render_base.png"));
                ImageIO.write(test, "png", new File("/tmp/render_test.png"));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public void testRotation() {
        int width = 256;
        int height = 256;

        initCL(true, width, height);

        BufferedImage base = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage test = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        render("test/scripts/transform_base.js", width, height, base);
        test = render("test/scripts/rotation.js", width, height,test);

        assertFalse("Constant image", ImageUtilTest.isConstantImage(test));
        assertFalse("Same image", ImageUtilTest.isImageEqual(base, test));

        if (DEBUG) {
            try {
                ImageIO.write(base, "png", new File("/tmp/render_base.png"));
                ImageIO.write(test, "png", new File("/tmp/render_test.png"));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private byte[] makePng(BufferedImage img) {
        long t0 = System.nanoTime();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(baos);
            PngEncoder encoder = new PngEncoder(PngEncoder.COLOR_TRUECOLOR, PngEncoder.BEST_SPEED);
            encoder.encode(img, bos);
            bos.close();

            byte[] ret_val = baos.toByteArray();
            lastPngTime = (System.nanoTime() - t0);
            return ret_val;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        lastPngTime = (System.nanoTime() - t0);

        return null;
    }

    private int makePng(PngEncoder encoder, BufferedImage img, byte[] buff) {
        long t0 = System.nanoTime();

        try {
            ReusableByteArrayOutputStream baos = new ReusableByteArrayOutputStream(buff);
            BufferedOutputStream bos = new BufferedOutputStream(baos);
            encoder.encode(img, bos);
            bos.close();

            lastPngTime = (System.nanoTime() - t0);

            return baos.size();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        lastPngTime = (System.nanoTime() - t0);
        return 0;
    }

    private BufferedImage render(String script, int width, int height, BufferedImage image) {
        List<Instruction> inst = loadScript(script);
        ArrayList progs = new ArrayList();

        for(int i=0; i < numRenderers; i++) {
            boolean result = render[i].init(progs, inst, "", VERSION);

            if (!result) {
                CLProgram program = render[i].getProgram();
                printf("Status: %s\n", program.getBuildStatus());
                printf("Build Log: %s\n", program.getBuildLog());
            }
            assertTrue("Compiled", result);
        }

        BufferedImage img = null;
        for(int i=0; i < numJobs; i++) {
            RenderJob job = jobs[i];
            jobs[i].getRenderer().renderOps(getView(), job.getX0(), job.getY0(), job.getWidth(), job.getHeight(), width, height, job.getView(), worldScale, job.getCommandQueue(), job.getDest());
            job.getCommandQueue().putReadBuffer(job.getDest(), true); // read results back (blocking read)
            job.getCommandQueue().finish();
            int[] pixels = new int[width*height];
            long t0 = System.nanoTime();
            createImage(width, height, pixels, job.getDest(), image);
            lastImageTime = System.nanoTime() - t0;
        }


        return image;
    }

    /**
     * Render a script with caching
     *
     * @param jobID  UniqueID for caching results
     * @param script
     * @param width
     * @param height
     * @return
     */
    private BufferedImage render(String jobID, String script, int width, int height,int[] pixels, BufferedImage image) {
        List<Instruction> inst = null;

        inst = cache.get(jobID);
        if (inst == null) {
            inst = loadScript(script);
            cache.put(jobID, inst);
        } else {
            lastLoadScriptTime = 0;
        }
        ArrayList progs = new ArrayList();

        for(int i=0; i < numRenderers; i++) {
            boolean result = render[i].init(progs, inst, "", VERSION);

            if (!result) {
                CLProgram program = render[i].getProgram();
                printf("Status: %s\n", program.getBuildStatus());
                printf("Build Log: %s\n", program.getBuildLog());
            }
            assertTrue("Compiled", result);
        }

        for(int i=0; i < numJobs; i++) {
            RenderJob job = jobs[i];

            job.getRenderer().renderOps(getView(), job.getX0(), job.getY0(), job.getWidth(), job.getHeight(), width, height,
                    job.getView(), worldScale, job.getCommandQueue(), job.getDest());
            job.getCommandQueue().putReadBuffer(job.getDest(), false); // read results back (blocking read)
        }

        for(int i=0; i < numRenderers; i++) {
            render[i].getCommandQueue().finish();
        }

        long t0 = System.nanoTime();
        createImage(width, height, pixels, image, jobs);
        lastImageTime = System.nanoTime() - t0;

        return image;
    }

    /**
     * Render a script with caching
     *
     * @param jobID  UniqueID for caching results
     * @param script
     * @param width
     * @param height
     * @return
     */
    private BufferedImage renderMT(String jobID, String script, int width, int height,int[] pixels,BufferedImage image) {
        List<Instruction> inst = null;

        inst = cache.get(jobID);
        if (inst == null) {
            inst = loadScript(script);
            cache.put(jobID, inst);
        } else {
            lastLoadScriptTime = 0;
        }
        ArrayList progs = new ArrayList();

        for(int i=0; i < numRenderers; i++) {
            boolean result = render[i].init(progs, inst, "", VERSION);

            if (!result) {
                CLProgram program = render[i].getProgram();
                printf("Status: %s\n", program.getBuildStatus());
                printf("Build Log: %s\n", program.getBuildLog());
            }
            assertTrue("Compiled", result);
        }

        long t0 = System.nanoTime();

        JobRunner[] runners = new JobRunner[numJobs];

        ExecutorService compute_executor = Executors.newFixedThreadPool(numJobs);
        Matrix4f view = getView();
        for (int i = 0; i < numJobs; i++) {
            RenderJob job = jobs[i];
            runners[i] = new JobRunner(job,view,width,height,worldScale);
            compute_executor.submit(runners[i]);
        }
        compute_executor.shutdown();

        try {
            compute_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        lastKernelTime = (System.nanoTime() - t0);
        t0 = System.nanoTime();
        createImage(width, height, pixels, image, jobs);
        lastImageTime = System.nanoTime() - t0;

        return image;
    }


    private Matrix4f getView() {
        float[] DEFAULT_TRANS = new float[]{0, 0, -4};
        float z = DEFAULT_TRANS[2];
        float rotx = 0;
        float roty = 0;

        Vector3f trans = new Vector3f();
        Matrix4f tmat = new Matrix4f();
        Matrix4f rxmat = new Matrix4f();
        Matrix4f rymat = new Matrix4f();

        trans.z = z;
        tmat.set(trans, 1.0f);

        rxmat.rotX(rotx);
        rymat.rotY(roty);

        Matrix4f mat = new Matrix4f();
        mat.mul(tmat, rxmat);
        mat.mul(rymat);

        return mat;
    }

    public void initCL(boolean debug, int width, int height) {
        numDevices = 1;
        numJobs = 1;
        numRenderers = 1;
        jobs = new RenderJob[1];
        render = new VolumeRenderer[1];
        jobs[0] = new RenderJob(0,0,width,height);
        RenderJob job = jobs[0];

        if (debug) {
            job.setDevice(CLPlatform.getDefault(type(CPU)).getMaxFlopsDevice());
        } else {
            job.setDevice(CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice());
        }

        job.setContext(CLContext.create(job.getDevice()));

        job.setQueue(job.getDevice().createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE));

        render[0] = new VolumeRenderer(job.getContext(), job.getCommandQueue());
        job.setRenderer(render[0]);
        job.setView(job.getContext().createFloatBuffer(16, READ_ONLY));

        IntBuffer dest = ByteBuffer.allocateDirect(height * width * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        job.setDest(job.getContext().createBuffer(dest, CLBuffer.Mem.WRITE_ONLY, CLBuffer.Mem.ALLOCATE_BUFFER));
    }

    public void initMultiCL(int width, int height) {
        CLDevice[] devices = CLPlatform.getDefault(type(GPU)).listCLDevices();

        numDevices = devices.length;
        jobs = new RenderJob[numDevices];
        numRenderers = numDevices;
        render = new VolumeRenderer[numRenderers];

        int jobWidth = width;
        int jobHeight = height / numDevices;

        numJobs = numDevices;
        for(int i=0; i < numDevices; i++) {
            RenderJob job = new RenderJob(0,i*jobHeight,jobWidth,jobHeight);
            jobs[i] = job;
            job.setDevice(devices[i]);
            job.setContext(CLContext.create(job.getDevice()));
            job.setQueue(job.getDevice().createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE));
            render[i] = new VolumeRenderer(job.getContext(), job.getCommandQueue());
            job.setRenderer(render[i]);
            job.setView(job.getContext().createFloatBuffer(16, READ_ONLY));
            IntBuffer dest = ByteBuffer.allocateDirect(height * width * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
            job.setDest(job.getContext().createBuffer(dest, CLBuffer.Mem.WRITE_ONLY, CLBuffer.Mem.ALLOCATE_BUFFER));
        }
    }

    private List<Instruction> loadScript(String filename) {
        long t0 = System.nanoTime();
        ShapeJSEvaluator eval = new ShapeJSEvaluator();
        Bounds bounds = new Bounds();
        DataSource source = eval.runScript(filename, bounds);

        OpenCLOpWriterV2 writer = new OpenCLOpWriterV2();
        Vector3d scale;
        scale = new Vector3d((bounds.xmax - bounds.xmin) / 2.0, (bounds.ymax - bounds.ymin) / 2.0, (bounds.zmax - bounds.zmin) / 2.0);
        worldScale = (float) Math.min(Math.min(scale.x, scale.y), scale.z);

        //printf("Scale is: %s\n", scale);
        List<Instruction> inst = writer.generate((Parameterizable) source, scale);

        lastLoadScriptTime = System.nanoTime() - t0;
        return inst;
    }

    /*
        private static BufferedImage createImage(int width,int height,CLBuffer<FloatBuffer> buffer){
            BufferedImage image=new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);
            float[] pixels=new float[buffer.getBuffer().capacity()];
            buffer.getBuffer().get(pixels).rewind();
            image.getRaster().setPixels(0,0,width,height,pixels);
            return image;
        }
    */
    private BufferedImage createImage(int width, int height, int[] pixels,CLBuffer<IntBuffer> buffer, BufferedImage image) {
        //int[] pixels = new int[buffer.getBuffer().capacity()];
        buffer.getBuffer().get(pixels).rewind();
        WritableRaster raster = image.getRaster();
        int[] pixel = new int[4];
        int r, g, b;
        long t0 = System.nanoTime();
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                int packed = pixels[h * width + w];

                b = (packed & 0x00FF0000) >> 16;
                g = (packed & 0x0000FF00) >> 8;
                r = (packed & 0x000000FF);
                pixel[0] = r;
                pixel[1] = g;
                pixel[2] = b;
                pixel[3] = 0xFFFFFFFF;
                raster.setPixel(w, h, pixel);
            }
        }

        printf("alloc time: %d\n", (int) ((System.nanoTime() - t0) / 1e6));
        return image;
    }

    /**
     * Create a combined image from multiple buffers
     *
     * @param width Total image width
     * @param height Total image height
     * @param pixels Preallocated array to place results
     * @param jobs The completed rendering jobs
     * @return
     */
    private BufferedImage createImage(int width, int height, int[] pixels,BufferedImage image, RenderJob[] jobs) {

        //int[] pixels = new int[buffer.getBuffer().capacity()];
        WritableRaster raster = image.getRaster();

        int len = jobs.length;
        int[] pixel = new int[4];

        long t0 = System.nanoTime();
        for(int i=0; i < len; i++) {
            RenderJob job = jobs[i];
            job.getDest().getBuffer().get(pixels).rewind();
            int r, g, b;
            for (int w = 0; w < job.getWidth(); w++) {
                for (int h = 0; h < job.getHeight(); h++) {
                    int packed = pixels[(job.getY0() + h) * width + w + job.getX0()];

                    b = (packed & 0x00FF0000) >> 16;
                    g = (packed & 0x0000FF00) >> 8;
                    r = (packed & 0x000000FF);
                    pixel[0] = r;
                    pixel[1] = g;
                    pixel[2] = b;
                    pixel[3] = 0xFFFFFFFF;
                    raster.setPixel(job.getX0() + w, job.getY0() + h, pixel);
                }
            }
        }

        printf("create image: %d\n", (int) ((System.nanoTime() - t0) / 1e6));
        return image;
    }
}


class RenderJob {
    private int x0;
    private int y0;
    private int width;
    private int height;

    private CLDevice  device;
    private CLContext context;
    private CLCommandQueue queue;
    private VolumeRenderer renderer;
    private CLBuffer<IntBuffer> dest;
    private CLBuffer<FloatBuffer> view;

    public RenderJob(int x0, int y0, int width, int height) {
        this.x0 = x0;
        this.y0 = y0;
        this.width = width;
        this.height = height;
    }

    public RenderJob(int x0, int y0, int width, int height, CLBuffer<IntBuffer> dest, CLBuffer<FloatBuffer> view) {
        this.x0 = x0;
        this.y0 = y0;
        this.width = width;
        this.height = height;
        this.dest = dest;
        this.view = view;
    }

    public RenderJob(int x0, int y0, int width, int height,
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

class JobRunner extends Thread {
    RenderJob job;
    Matrix4f view;
    float worldScale;
    int width;
    int height;

    public JobRunner(RenderJob job, Matrix4f view, int width, int height, float worldScale) {
        this.job = job;
        this.view = view;
        this.width = width;
        this.height = height;
        this.worldScale = worldScale;
    }

    public void run() {
        long t0 = System.nanoTime();

        printf("Job: %d,%d %d %d\n",job.getX0(),job.getY0(),job.getWidth(),job.getHeight());
        job.getRenderer().renderOps(view, job.getX0(), job.getY0(), job.getWidth(), job.getHeight(), width, height,
                job.getView(), worldScale, job.getCommandQueue(), job.getDest());
        job.getCommandQueue().putReadBuffer(job.getDest(), true); // read results back (blocking read)
        printf("Job time: %d\n",(int)((System.nanoTime() - t0) / 1e6));
    }
}