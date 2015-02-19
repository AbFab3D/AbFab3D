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
import org.apache.commons.io.FileUtils;
import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.TJCompressor;
import shapejs.ShapeJSEvaluator;

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
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private static final boolean DEBUG = true;
    public static final String VERSION = VolumeRenderer.VERSION_OPCODE_V2_DIST;

    private int numTiles;
    private RenderTile[] tiles;
    private int numRenderers;
    private VolumeRenderer[] render;
    private DeviceResources[] devices;
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

    public void testJPEGSpeed() throws Exception {
        int width = 512;
        int height = 512;

        long t0 = System.nanoTime();

        // Do these items once for the servlet.  Should be per-thread resources
        ImageRenderer render = new ImageRenderer();
        render.initCL(1, width, height);
        int MAX_IMG_SIZE = TJ.bufSize(width,height,TJ.SAMP_420);

        printf("Max size: %d\n",MAX_IMG_SIZE);
        byte[] buff = new byte[MAX_IMG_SIZE];
        int[] pixels = new int[width * height];
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        printf("initCL time: %d ms\n", (int) ((System.nanoTime() - t0) / 1e6));

        // End of per-thread resources

        String jobID = UUID.randomUUID().toString();

        int TIMES = 3;

        if (DEBUG) TIMES = 1;

//        String script = "scripts/dodecahedron.js";
        String script = "scripts/gyrosphere.js";

        for (int i = 0; i < TIMES; i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(150000);
            BufferedOutputStream bos = new BufferedOutputStream(baos);
                    t0 = System.nanoTime();
            render.render(jobID, new File(script), new HashMap(), getView(), true, ImageRenderer.IMAGE_JPEG, 0.5f, bos);

            if (DEBUG) {
                FileUtils.writeByteArrayToFile(new File("/tmp/render_speed.jpg"),baos.toByteArray());
            }

        }

    }

    public void testV3Speed() throws Exception {
        int width = 512;
        int height = 512;

        long t0 = System.nanoTime();

        // Do these items once for the servlet.  Should be per-thread resources
        ImageRenderer render = new ImageRenderer();
        render.setVersion(VolumeRenderer.VERSION_OPCODE_V3_DIST);
        render.initCL(1, width, height);
        int MAX_IMG_SIZE = TJ.bufSize(width,height,TJ.SAMP_420);

        printf("Max size: %d\n",MAX_IMG_SIZE);
        byte[] buff = new byte[MAX_IMG_SIZE];
        int[] pixels = new int[width * height];
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        printf("initCL time: %d ms\n", (int) ((System.nanoTime() - t0) / 1e6));

        // End of per-thread resources

        String jobID = UUID.randomUUID().toString();

        int TIMES = 3;

        if (DEBUG) TIMES = 1;

//        String script = "scripts/dodecahedron.js";
        String script = "scripts/gyrosphere.js";

        for (int i = 0; i < TIMES; i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(150000);
            BufferedOutputStream bos = new BufferedOutputStream(baos);
            t0 = System.nanoTime();
            render.render(jobID, new File(script), new HashMap(), getView(), true, ImageRenderer.IMAGE_JPEG, 0.5f, bos);

            if (DEBUG) {
                FileUtils.writeByteArrayToFile(new File("/tmp/render_speed.jpg"),baos.toByteArray());
            }

        }

    }

    public void testVersionSpeed() throws Exception {

        int width = 512;
        int height = 512;

        long t0 = System.nanoTime();

        // Do these items once for the servlet.  Should be per-thread resources
        //render.setVersion(VolumeRenderer.VERSION_OPCODE_V2_DIST);
        //render.setVersion(VolumeRenderer.VERSION_OPCODE_V3_DIST);
        render.setVersion(VolumeRenderer.VERSION_DIST);
        render.initCL(1, width, height);
        int MAX_IMG_SIZE = TJ.bufSize(width,height,TJ.SAMP_420);

        printf("Max size: %d\n",MAX_IMG_SIZE);
        byte[] buff = new byte[MAX_IMG_SIZE];
        int[] pixels = new int[width * height];
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double initCLTime = ((System.nanoTime() - t0) / 1e6);
        printf("initCL time: %6.0f ms\n", initCLTime);

        // End of per-thread resources

        String jobID = UUID.randomUUID().toString();

        int TIMES = 6;

//        String script = "scripts/dodecahedron.js";
        String script = 
            "function main(args) {                                         \n"+
            "  var radius = 15 * MM;                                       \n"+
            "  var num = args['num'];                                      \n"+
            "  var gs = 2*radius;                                          \n"+
            "  var grid = createGrid(-gs, gs, -gs, gs, -gs, gs, 0.1 * MM); \n"+
            "  var result;                                                 \n"+
            "  if (num == 1) {                                             \n"+
            "    result = new Sphere(0,0,0,radius);                        \n"+
            "  } else {                                                    \n"+
            "    var union = new Union();                                  \n"+
            "    var x0 = -radius;                                         \n"+
            "    var dx = 2*radius/(num-1);                                \n"+
            "    for (i = 0; i < num; i++) {                               \n"+
            "      var x = x0 + dx * i;                                    \n"+
            "      var y = radius;                                         \n"+
            "      union.add(new Sphere(x, -y, 0, radius));                \n"+
            "      union.add(new Box(x,y, 0,radius/2 ,radius/2,radius/2)); \n"+
            "    }                                                         \n"+
            "    result = union;                                           \n"+
            "  }                                                           \n"+
            "  var maker = new GridMaker();                                \n"+
            "  maker.setSource(result);                                    \n"+
            "  maker.makeGrid(grid);                                       \n"+
            "  return grid;                                                \n"+
            "}                                                             \n";

        HashMap<String,Object> params = new HashMap<String, Object>();
        ImageRenderer.TimeStat[] times = new ImageRenderer.TimeStat[TIMES];
        int base = 0;

        for (int i = 0; i < 3; i++) {
            // to warm up 
            params.put("num",base + (i+1));
            ByteArrayOutputStream baos = new ByteArrayOutputStream(150000);
            BufferedOutputStream bos = new BufferedOutputStream(baos);
            t0 = System.nanoTime();
            render.render(jobID, script, params, getView(), true, ImageRenderer.IMAGE_JPEG, 0.5f, bos);            
            if (DEBUG) {
                bos.close();
                FileUtils.writeByteArrayToFile(new File("/tmp/render_speed_" + (base+i) + ".jpg"),baos.toByteArray());
            }
        }

        for (int i = 0; i < TIMES; i++) {
            params.put("num",base + (i+1));
            ByteArrayOutputStream baos = new ByteArrayOutputStream(150000);
            BufferedOutputStream bos = new BufferedOutputStream(baos);
            t0 = System.nanoTime();
            render.render(jobID, script, params, getView(), true, ImageRenderer.IMAGE_JPEG, 0.5f, bos);
            times[i] = render.getTimeStat();
            
            if (DEBUG) {
                bos.close();
                FileUtils.writeByteArrayToFile(new File("/tmp/render_speed_" + (base+i) + ".jpg"),baos.toByteArray());
            }
        }

        printf("Version: %s\n",render.getVersion());
        printf("initCL time: %6.0f ms\n", initCLTime);
        printf("count %s\n",times[0].getHeader());
        for(int i=0; i < TIMES; i++) {
            printf("%5d %s\n",(i+1),times[i].toString());
        }
    }

    public void testQuality() throws Exception {
        int width = 512;
        int height = 512;

        long t0 = System.nanoTime();

        // Do these items once for the servlet.  Should be per-thread resources
        ImageRenderer render = new ImageRenderer();
        render.initCL(1, width, height);
        int MAX_IMG_SIZE = TJ.bufSize(width,height,TJ.SAMP_420);

        printf("Max size: %d\n",MAX_IMG_SIZE);
        byte[] buff = new byte[MAX_IMG_SIZE];
        int[] pixels = new int[width * height];
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        printf("initCL time: %d ms\n", (int) ((System.nanoTime() - t0) / 1e6));

        // End of per-thread resources

        String jobID = UUID.randomUUID().toString();

//        String script = "scripts/dodecahedron.js";
        String script = "scripts/gyrosphere.js";
        float quality = 0.25f;

        ByteArrayOutputStream baos = new ByteArrayOutputStream(150000);
        BufferedOutputStream bos = new BufferedOutputStream(baos);
        render.render(jobID, new File(script), new HashMap(), getView(), true, ImageRenderer.IMAGE_PNG, quality, bos);
        bos.close();

        if (DEBUG) {
            FileUtils.writeByteArrayToFile(new File("/tmp/render_quality_" + quality + ".png"),baos.toByteArray());
        }

        quality = 0.5f;
        baos = new ByteArrayOutputStream(150000);
        bos = new BufferedOutputStream(baos);
        render.render(jobID, new File(script), new HashMap(), getView(), true, ImageRenderer.IMAGE_PNG, quality, bos);
        bos.close();

        if (DEBUG) {
            FileUtils.writeByteArrayToFile(new File("/tmp/render_quality" + quality + ".png"),baos.toByteArray());
        }

        quality = 1f;
        baos = new ByteArrayOutputStream(150000);
        bos = new BufferedOutputStream(baos);
        render.render(jobID, new File(script), new HashMap(), getView(), true, ImageRenderer.IMAGE_PNG, quality, bos);
        bos.close();

        if (DEBUG) {
            FileUtils.writeByteArrayToFile(new File("/tmp/render_quality" + quality + ".png"),baos.toByteArray());
        }
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
        int MAX_IMG_SIZE = width * height;
        byte[] buff = new byte[MAX_IMG_SIZE];
        int[] pixels = new int[width * height];
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
            BufferedImage base = render(jobID, script, width, height, pixels, image);
            int size = makePng(encoder, base, buff);  // return buff[0] to size bytes to client

            printf("total size: %d time: %d ms\n\tjs eval: %d\n\tocl compile: %d ms\n\tkernel: %d ms\n\timage: %d ms\n\tpng: %d ms\n", size,(int) ((System.nanoTime() - t0) / 1e6), (int) (lastLoadScriptTime / 1e6), (int) (tiles[0].getRenderer().getLastCompileTime() / 1e6), (int) (tiles[0].getRenderer().getLastKernelTime() / 1e6), (int) (lastImageTime / 1e6), (int) (lastPngTime / 1e6));

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
//        int width = 1024;
//        int height = 1024;
        int width = 4096;
        int height = 4096;

        long t0 = System.nanoTime();

        // Do these items once for the servlet.  Should be per-thread resources
        initMultiCL(width, height);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        PngEncoder encoder = new PngEncoder(PngEncoder.COLOR_TRUECOLOR, PngEncoder.BEST_SPEED);
//        int MAX_IMG_SIZE = 150000;
        int MAX_IMG_SIZE = width * height;
        byte[] buff = new byte[MAX_IMG_SIZE];
        int[] pixels = new int[tiles[0].getWidth() * tiles[0].getHeight()];
//        int[] pixels = new int[width * height];
        printf("initCL time: %d ms\n", (int) ((System.nanoTime() - t0) / 1e6));

        // End of per-thread resources

        String jobID = UUID.randomUUID().toString();

        int TIMES = 3;

//        if (DEBUG) TIMES = 1;

        long compileTime = -1;
        String script = "scripts/dodecahedron.js";
//        String script = "scripts/gyrosphere.js";

        for (int i = 0; i < TIMES; i++) {
            t0 = System.nanoTime();
            BufferedImage base = renderMT(jobID, script, width, height, pixels, image);
            int size = makePng(encoder, base, buff);  // return buff[0] to size bytes to client

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
        printf("total time: %d ms\n\tjs eval: %d\n\tocl compile: %d ms\n\tkernel: %d ms\n\timage: %d\n", (int) ((System.nanoTime() - t0) / 1e6), (int) (lastLoadScriptTime / 1e6), (int) (tiles[0].getRenderer().getLastCompileTime() / 1e6), (int) (tiles[0].getRenderer().getLastKernelTime() / 1e6), (int) (lastImageTime / 1e6));
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
        test = render("test/scripts/rotation.js", width, height, test);

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

    private int makeJpg(BufferedImage img, byte[] buff) {
        long t0 = System.nanoTime();

        try {
            TJCompressor tj = new TJCompressor();
            tj.setJPEGQuality(75);
            tj.setSubsamp(TJ.SAMP_420);

            tj.setSourceImage(img, 0, 0, 0, 0);
            tj.compress(buff,0);
            int size = tj.getCompressedSize();
            printf("JPEG size is: %d\n", size);

            lastPngTime = (System.nanoTime() - t0);

            return size;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }

        lastPngTime = (System.nanoTime() - t0);
        return 0;
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

        VolumeScene vscene = new VolumeScene(progs, inst, "", VERSION);

        for (int i = 0; i < numRenderers; i++) {
            boolean result = render[i].init(vscene);

            if (!result) {
                CLProgram program = render[i].getProgram();
                printf("Status: %s\n", program.getBuildStatus());
                printf("Build Log: %s\n", program.getBuildLog());
            }
            assertTrue("Compiled", result);
        }

        BufferedImage img = null;
        Matrix4f view = getView();
        view.invert();

        for (int i = 0; i < numTiles; i++) {
            RenderTile tile = tiles[i];
            tile.getRenderer().sendView(view,tile.getView());
            tile.getRenderer().renderOps(tile.getX0(), tile.getY0(), tile.getWidth(), tile.getHeight(), width, height, worldScale, tile.getDest());
            tile.getCommandQueue().putReadBuffer(tile.getDest(), true); // read results back (blocking read)
            tile.getCommandQueue().finish();
            int[] pixels = new int[width * height];
            long t0 = System.nanoTime();
            createImage(width, height, pixels, tile.getDest(), image);
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
    private BufferedImage render(String jobID, String script, int width, int height, int[] pixels, BufferedImage image) {
        List<Instruction> inst = null;

        inst = cache.get(jobID);
        if (inst == null) {
            inst = loadScript(script);
            if (inst != null && inst.size() > 0) {
                cache.put(jobID, inst);
            }
        } else {
            lastLoadScriptTime = 0;
        }
        ArrayList progs = new ArrayList();
        VolumeScene vscene = new VolumeScene(progs, inst, "", VERSION);

        for (int i = 0; i < numRenderers; i++) {
            boolean result = render[i].init(vscene);

            if (!result) {
                CLProgram program = render[i].getProgram();
                printf("Status: %s\n", program.getBuildStatus());
                printf("Build Log: %s\n", program.getBuildLog());
            }
            assertTrue("Compiled", result);
        }

        Matrix4f view = getView();
        view.invert();

            tiles[0].getRenderer().sendView(view,tiles[0].getView());
        for (int i = 0; i < numTiles; i++) {
            RenderTile tile = tiles[i];

            tile.getRenderer().renderOps(tile.getX0(), tile.getY0(), tile.getWidth(), tile.getHeight(), width, height,
                    worldScale, tile.getDest());
            tile.getCommandQueue().putReadBuffer(tile.getDest(), false); // read results back (blocking read)
        }

        for (int i = 0; i < numRenderers; i++) {
            render[i].getCommandQueue().finish();
        }

        long t0 = System.nanoTime();
        createImage(width, height, pixels, tiles[0].getDest(), image);
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
    private BufferedImage renderMT(String jobID, String script, int width, int height, int[] pixels, BufferedImage image) {
        List<Instruction> inst = null;

        inst = cache.get(jobID);
        if (inst == null) {
            inst = loadScript(script);
            cache.put(jobID, inst);
        } else {
            lastLoadScriptTime = 0;
        }
        ArrayList progs = new ArrayList();

        long t0 = System.nanoTime();


        RenderThread[] rthreads = new RenderThread[numDevices];
        ConcurrentLinkedQueue<RenderTile> rqueue = new ConcurrentLinkedQueue<RenderTile>();
        for (RenderTile rt : tiles) {
            rqueue.add(rt);
        }

        ConcurrentLinkedQueue<RenderTile> tqueue = new ConcurrentLinkedQueue<RenderTile>();

        int read_factor = 4;  // TODO: How to choose this param?
        int read_threads = numDevices * read_factor;

        //read_threads = 1;
        TransferResultsThread[] transfer = new TransferResultsThread[read_threads];
        ResultsListener[] rl = new ResultsListener[transfer.length];

        for (int i = 0; i < transfer.length; i++) {
            rl[i] = new ResultsListener(width, height, pixels, image);

            transfer[i] = new TransferResultsThread(tqueue);
            transfer[i].setListener(rl[i]);
            transfer[i].start();
        }

        VolumeScene vscene = new VolumeScene(new ArrayList(), inst, "", VERSION);

        for (int i = 0; i < numDevices; i++) {
            rthreads[i] = new RenderThread(devices[i], rqueue, tqueue, vscene, getView(), width, height, worldScale);

            // TODO: short circuit listener thread to force on same system thread
            //rthreads[i].setListener(rl[0]);
            rthreads[i].start();
        }
/*
        // wait for completion
        boolean waiting_initialize = true;
        while (waiting_initialize) {
            int initialized_count = 0;
            for (int i = 0; i < numDevices; i++) {
                if (rthreads[i].isInitialized()) initialized_count++;
            }

            if (initialized_count == numDevices) {
                waiting_initialize = false;
            }

            try {
                Thread.yield();
            } catch (Exception e) {
            }
        }
*/
        // wait for completion
        boolean waiting_render = true;
        while (waiting_render) {
            int done_count = 0;
            for (int i = 0; i < numDevices; i++) {
                if (rthreads[i].isDone()) done_count++;
            }

            if (done_count == numDevices) {
                waiting_render = false;
            }

            try {
                Thread.yield();
            } catch (Exception e) {
            }
        }

        for (int i = 0; i < transfer.length; i++) {
            transfer[i].terminate();
        }

        boolean waiting_transfer = true;
        while (waiting_transfer) {
            int done_count = 0;
            for (int i = 0; i < transfer.length; i++) {
                if (transfer[i].isDone()) done_count++;
            }

            if (done_count == transfer.length) {
                waiting_transfer = false;
            }

            try {
                Thread.yield();
            } catch (Exception e) {
            }
        }

        for (int i = 0; i < numDevices; i++) {
            rthreads[i].cleanup();
        }

        lastKernelTime = 0;
        lastImageTime = 0;

        // STATS
        for(int i=0; i < numDevices; i++) {
            printf("Device: %s Tiles: %d Kernel: %d\n",devices[i].getDevice().getName(),rthreads[i].getTilesProcessed(),((int)(rthreads[i].getKernelTime() / 1e6)));
            lastKernelTime = Math.max(lastKernelTime, rthreads[i].getKernelTime());
        }

        for(int i=0; i < transfer.length; i++) {
            printf("Transfer: %d Tiles: %d Image: %d\n",i,transfer[i].getTilesProcessed(),((int)(transfer[i].getImageTime() / 1e6)));
            lastImageTime = Math.max(lastImageTime, transfer[i].getImageTime());

        }
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
        numTiles = 1;
        numRenderers = 1;
        tiles = new RenderTile[1];
        render = new VolumeRenderer[1];
        tiles[0] = new RenderTile(0, 0, width, height);
        RenderTile tile = tiles[0];

        CLPlatform platform = CLPlatform.getDefault();

        if (debug) {
            tile.setDevice(CLPlatform.getDefault(type(CPU)).getMaxFlopsDevice());
        } else {
            if (platform.getName().contains("Apple")) {
                // Apple does not get the GPU maxFlops right, just find the nvidia card
                CLDevice[] devices = platform.listCLDevices();
                boolean found = false;
                for(int i=0; i < devices.length; i++) {
                    printf("Checking device: %s %s\n",devices[i],devices[i].getVendor());
                    if (devices[i].getVendor().contains("NVIDIA")) {
                        tile.setDevice(devices[i]);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    tile.setDevice(CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice());
                }
            } else {
                tile.setDevice(CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice());
            }
        }

        tile.setContext(CLContext.create(tile.getDevice()));

        tile.setQueue(tile.getDevice().createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE));

        render[0] = new VolumeRenderer(tile.getContext(), tile.getCommandQueue());
        tile.setRenderer(render[0]);
        tile.setView(tile.getContext().createFloatBuffer(16, READ_ONLY));

        IntBuffer dest = ByteBuffer.allocateDirect(height * width * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        tile.setDest(tile.getContext().createBuffer(dest, CLBuffer.Mem.WRITE_ONLY, CLBuffer.Mem.ALLOCATE_BUFFER));
    }

    public void initMultiCL(int width, int height) {
//        CLDevice[] ocl_devices = CLPlatform.getDefault(type(CPU)).listCLDevices();
        CLDevice[] ocl_devices = CLPlatform.getDefault(type(GPU)).listCLDevices();
//        CLDevice[] ocl_devices = new CLDevice[] {CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice()};
        numDevices = ocl_devices.length;
        devices = new DeviceResources[numDevices];

        long t0 = System.nanoTime();
        int factor = numDevices * 1;
        //int factor = 1;
        int block_size = width / factor;

        int jobWidth = width / block_size;
        int jobHeight = height / block_size;

        while (width % block_size != 0 || height % block_size != 0) {
            block_size = block_size / 2;
            printf("Changing block size to: %d\n", block_size);
        }

        numTiles = jobWidth * jobHeight;


        for (int i = 0; i < numDevices; i++) {
            devices[i] = new DeviceResources(ocl_devices[i], block_size, block_size, width, height);
        }

        tiles = new RenderTile[numTiles];
        for (int i = 0; i < jobWidth; i++) {
            for (int j = 0; j < jobHeight; j++) {
                RenderTile tile = new RenderTile(i, j, block_size, block_size);
                tiles[i * jobWidth + j] = tile;
            }
        }

        if (DEBUG) {
            for (int i = 0; i < numTiles; i++) {
                printf("Job: %d x0: %d y0: %d w: %d h: %d\n", i, tiles[i].getX0(), tiles[i].getY0(), tiles[i].getWidth(), tiles[i].getHeight());
            }
            printf("OpenCL Init: %d\n", (int) ((System.nanoTime() - t0) / 1e6));
        }
    }

    private List<Instruction> loadScript(String filename) {
        long t0 = System.nanoTime();
        ShapeJSEvaluator eval = new ShapeJSEvaluator();
        Bounds bounds = new Bounds();
        DataSource source = eval.runScript(new File(filename), bounds);

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
    private BufferedImage createImage(int width, int height, int[] pixels, CLBuffer<IntBuffer> buffer, BufferedImage image) {
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

        lastImageTime = (System.nanoTime() - t0);
        //printf("alloc time: %d\n", (int) ((System.nanoTime() - t0) / 1e6));
        return image;
    }


    public static void main(String arg[]) throws Exception {
        
        new TestVolumeRenderer().testVersionSpeed();
    }

}

