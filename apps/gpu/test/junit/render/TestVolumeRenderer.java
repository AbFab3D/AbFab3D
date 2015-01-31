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
import abfab3d.util.SysErrorReporter;
import com.jogamp.opencl.*;
import com.objectplanet.image.PngEncoder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import shapejs.ShapeJSEvaluator;
import viewer.OpenCLOpWriterV2;
import viewer.OpenCLWriter;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.*;
import java.util.ArrayList;
import java.util.List;

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
    private CLDevice device;
    private CLContext context;
    private CLCommandQueue queue;
    private VolumeRenderer renderer;
    private CLBuffer<FloatBuffer> viewBuffer;
    private IntBuffer dest;
    private CLBuffer<IntBuffer> pixelBuffer;
    private float worldScale;
    private long lastLoadScriptTime;
    private long lastImageTime;
    private long lastPngTime;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestVolumeRenderer.class);
    }

    public void testSpeed() {
        int width = 512;
        int height = 512;

        initCL(DEBUG, width, height);

        int TIMES = 3;
        for(int i=0; i < TIMES; i++) {
            long t0 = System.nanoTime();
            BufferedImage base = render("scripts/gyrosphere.js", width, height);
            makePng(base);

            printf("total time: %d ms\n\tjs eval: %d\n\tocl compile: %d ms\n\tkernel: %d ms\n\timage: %d ms\n\tpng: %d ms\n", (int) ((System.nanoTime() - t0) / 1e6), (int) (lastLoadScriptTime / 1e6), (int) (renderer.getLastCompileTime() / 1e6), (int) (renderer.getLastKernelTime() / 1e6), (int) (lastImageTime / 1e6), (int) (lastPngTime / 1e6));

            if (DEBUG) {
                try {
                    ImageIO.write(base, "png", new File("/tmp/render_base.png"));
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
        BufferedImage base = render("test/scripts/transform_base.js",width,height);
        printf("total time: %d ms\n\tjs eval: %d\n\tocl compile: %d ms\n\tkernel: %d ms\n\timage: %d\n",(int)((System.nanoTime() - t0) / 1e6),(int)(lastLoadScriptTime / 1e6),(int)(renderer.getLastCompileTime() / 1e6),(int) (renderer.getLastKernelTime() / 1e6),(int) (lastImageTime / 1e6));
        BufferedImage test = render("test/scripts/translation.js",width,height);

        assertFalse("Constant image", ImageUtilTest.isConstantImage(test));
        assertFalse("Same image", ImageUtilTest.isImageEqual(base,test));

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


        BufferedImage base = render("test/scripts/transform_base.js",width,height);
        BufferedImage test = render("test/scripts/rotation.js",width,height);

        assertFalse("Constant image", ImageUtilTest.isConstantImage(test));
        assertFalse("Same image", ImageUtilTest.isImageEqual(base,test));

        if (DEBUG) {
            try {
                ImageIO.write(base, "png", new File("/tmp/render_base.png"));
                ImageIO.write(test, "png", new File("/tmp/render_test.png"));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private void makePng(BufferedImage img) {
        long t0 = System.nanoTime();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(baos);
            PngEncoder encoder = new PngEncoder(PngEncoder.COLOR_TRUECOLOR_ALPHA, PngEncoder.BEST_SPEED);
            encoder.encode(img, bos);
            bos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        lastPngTime = (System.nanoTime() - t0);
    }

    private BufferedImage render(String script, int width, int height) {
        String ver = VolumeRenderer.VERSION_OPCODE_V2;
        List<Instruction> inst = loadScript(script);
        ArrayList progs = new ArrayList();

        boolean result = renderer.init(progs, inst,"",VolumeRenderer.VERSION_OPCODE_V2);

        if (!result) {
            CLProgram program = renderer.getProgram();
            printf("Status: %s\n", program.getBuildStatus());
            printf("Build Log: %s\n", program.getBuildLog());
        }
        assertTrue("Compiled", result);
        renderer.renderOps(getView(), 0, 0, width,height,width, height, viewBuffer, worldScale, queue, pixelBuffer);

        long t0 = System.nanoTime();
        queue.putReadBuffer(pixelBuffer, true); // read results back (blocking read)
        queue.finish();

        BufferedImage img = createImage(width,height,pixelBuffer);
        lastImageTime = System.nanoTime() - t0;

        return img;
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
        if (debug) {
            device = CLPlatform.getDefault(type(CPU)).getMaxFlopsDevice();
        } else {
            device = CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice();
        }

        context = CLContext.create(device);
        queue = device.createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);

        renderer = new VolumeRenderer(context, queue);
        viewBuffer = context.createFloatBuffer(16, READ_ONLY);

        IntBuffer dest = ByteBuffer.allocateDirect(height * width * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        pixelBuffer = context.createBuffer(dest, CLBuffer.Mem.WRITE_ONLY, CLBuffer.Mem.ALLOCATE_BUFFER);
    }

    private List<Instruction> loadScript(String filename) {
        long t0 = System.nanoTime();
        ShapeJSEvaluator eval = new ShapeJSEvaluator();
        Bounds bounds = new Bounds();
        DataSource source = eval.runScript(filename, bounds);

        OpenCLOpWriterV2 writer = new OpenCLOpWriterV2();
        Vector3d scale;
        scale = new Vector3d((bounds.xmax - bounds.xmin) / 2.0, (bounds.ymax - bounds.ymin) / 2.0, (bounds.zmax - bounds.zmin) / 2.0);
        worldScale = (float) Math.min(Math.min(scale.x,scale.y),scale.z);

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
    private BufferedImage createImage(int width, int height, CLBuffer<IntBuffer> buffer) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[buffer.getBuffer().capacity()];
        buffer.getBuffer().get(pixels).rewind();
        WritableRaster raster = image.getRaster();
        int[] pixel = new int[4];
        int r,g,b;
        long t0 = System.nanoTime();
        for(int w=0; w < width; w++) {
            for(int h=0; h < height;h++) {
                int packed = pixels[h*width + w];

                b = (packed & 0x00FF0000) >> 16;
                g = (packed & 0x0000FF00) >> 8;
                r = (packed & 0x000000FF);
                pixel[0] = r;
                pixel[1] = g;
                pixel[2] = b;
                pixel[3] = 0xFFFFFFFF;
                raster.setPixel(w,h,pixel);
            }
        }

        printf("alloc time: %d\n", (int) ((System.nanoTime() - t0) / 1e6));
        return image;
    }
}

