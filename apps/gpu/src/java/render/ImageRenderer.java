package render;

import abfab3d.grid.Bounds;
import abfab3d.param.Parameterizable;
import abfab3d.util.DataSource;
import com.jogamp.opencl.*;
import com.objectplanet.image.PngEncoder;
import datasources.Instruction;
import shapejs.ShapeJSEvaluator;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static abfab3d.util.Output.printf;
import static com.jogamp.opencl.CLDevice.Type.GPU;
import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.util.CLPlatformFilters.type;

/**
 * Image based rendering.  Uses the VolumeRenderer to generate images.  Includes optional caching and reuse
 * of temporary class variables to reduce garbage generation.  This class is not thread safe, allocate one per thread.
 *
 * @author Alan Hudson
 */
public class ImageRenderer {
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
    private int width;
    private int height;
    private boolean initialized = false;

    // TODO: make these thread local
    private byte[] buff;
    private int[] pixels;
    private BufferedImage image;

    private byte[] bigBuff;
    private int[] bigPixels;
    private BufferedImage bigImage;

    private HashMap<String, List<Instruction>> cache = new HashMap<String, List<Instruction>>();

    public void initCL(int maxDevices,int width, int height) {
        this.width = width;
        this.height = height;
        numDevices = 1;
        numTiles = 1;
        numRenderers = 1;
        tiles = new RenderTile[1];
        render = new VolumeRenderer[1];
        tiles[0] = new RenderTile(0, 0, width, height);
        RenderTile tile = tiles[0];

        // TODO: add CPU fallback logic
        tile.setDevice(CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice());

        tile.setContext(CLContext.create(tile.getDevice()));

        tile.setQueue(tile.getDevice().createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE));

        render[0] = new VolumeRenderer(tile.getContext(), tile.getCommandQueue());
        tile.setRenderer(render[0]);
        tile.setView(tile.getContext().createFloatBuffer(16, READ_ONLY));

        IntBuffer dest = ByteBuffer.allocateDirect(height * width * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        tile.setDest(tile.getContext().createBuffer(dest, CLBuffer.Mem.WRITE_ONLY, CLBuffer.Mem.ALLOCATE_BUFFER));

        float expandFactor = 1.5f;  // Account for png's which get larger, is this really necessary?

        // TODO: these need to be thread local resources
        buff = new byte[(int)(width * height * expandFactor)];
        pixels = new int[width * height];
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int frames = 36;
        int frameX = 6;
        int frameY = frames / frameX;
        bigBuff = new byte[(int)(width * height * expandFactor * frames)];
        bigPixels = new int[width * height];
        bigImage = new BufferedImage(width * frameX, height * frameY, BufferedImage.TYPE_INT_ARGB);

        initialized = true;
    }


    public int render(String jobID, String script, Matrix4f view,boolean cache, OutputStream os) throws IOException {
        if (!initialized) {
            throw new IllegalArgumentException("Renderer not initialized");
        }

        int width = 512;
        int height = 512;

        long t0 = System.nanoTime();

        // Do these items once for the servlet.  Should be per-thread resources

        PngEncoder encoder = new PngEncoder(PngEncoder.COLOR_TRUECOLOR, PngEncoder.BEST_SPEED);
//        PngEncoder encoder = new PngEncoder(PngEncoder.COLOR_TRUECOLOR, PngEncoder.BEST_COMPRESSION);
        printf("initCL time: %d ms\n", (int) ((System.nanoTime() - t0) / 1e6));

        // End of per-thread resources


        t0 = System.nanoTime();
        makeRender(jobID, script, cache, view,0,0,width, height, pixels, image);
        int size = makePng(encoder, image, buff);  // return buff[0] to size bytes to client

        printf("total size: %d time: %d ms\n\tjs eval: %d\n\tocl compile: %d ms\n\tkernel: %d ms\n\timage: %d ms\n\tpng: %d ms\n", size,(int) ((System.nanoTime() - t0) / 1e6), (int) (lastLoadScriptTime / 1e6), (int) (tiles[0].getRenderer().getLastCompileTime() / 1e6), (int) (tiles[0].getRenderer().getLastKernelTime() / 1e6), (int) (lastImageTime / 1e6), (int) (lastPngTime / 1e6));

        os.write(buff,0,size);

        return size;
    }

    public int renderImages(String jobID, String script, Matrix4f view,int frames, int frameX, boolean useCache, OutputStream os) throws IOException {
        if (!initialized) {
            throw new IllegalArgumentException("Renderer not initialized");
        }

        int width = 512;
        int height = 512;

        long t0 = System.nanoTime();

        // Do these items once for the servlet.  Should be per-thread resources

        PngEncoder encoder = new PngEncoder(PngEncoder.COLOR_TRUECOLOR, PngEncoder.BEST_SPEED);
//        PngEncoder encoder = new PngEncoder(PngEncoder.COLOR_TRUECOLOR, PngEncoder.BEST_COMPRESSION);



        printf("initCL time: %d ms\n", (int) ((System.nanoTime() - t0) / 1e6));

        // End of per-thread resources


        t0 = System.nanoTime();
        int pixX = 0;
        int pixY = 0;

        List<Instruction> inst = null;

        if (useCache) {
            inst = cache.get(jobID);
        }
        if (inst == null) {
            inst = loadScript(script);
            if (inst != null && inst.size() > 0) {
                cache.put(jobID, inst);
            }
        } else {
            lastLoadScriptTime = 0;
        }
        ArrayList progs = new ArrayList();

        for (int i = 0; i < numRenderers; i++) {
            boolean result = render[i].init(progs, inst, "", VERSION);

            if (!result) {
                CLProgram program = render[i].getProgram();
                printf("Status: %s\n", program.getBuildStatus());
                printf("Build Log: %s\n", program.getBuildLog());

                throw new IllegalArgumentException("Compile failed");
            }
        }

        float drot = 360f / frames;
        float rotx = 0;
        for(int n=0; n < frames; n++) {
            // TODO: get view based on rotation

            Matrix4f inv_view = getView(rotx);
            inv_view.invert();

            rotx += drot;
            tiles[0].getRenderer().sendView(inv_view,tiles[0].getView());
            RenderTile tile = tiles[0];

            tile.getRenderer().renderOps(tile.getX0(), tile.getY0(), tile.getWidth(), tile.getHeight(), width, height,
                    worldScale, tile.getDest());
            tile.getCommandQueue().putReadBuffer(tile.getDest(), false); // read results back (blocking read)

            render[0].getCommandQueue().finish();

            createImage(pixX,pixY,width, height, bigPixels, tile.getDest(), bigImage);
            lastImageTime = System.nanoTime() - t0;
            pixX++;
            if (pixX == frameX) {
                pixY++;
                pixX = 0;
            }
        }
        int size = makePng(encoder, bigImage, bigBuff);  // return buff[0] to size bytes to client

        printf("total size: %d time: %d ms\n\tjs eval: %d\n\tocl compile: %d ms\n\tkernel: %d ms\n\timage: %d ms\n\tpng: %d ms\n", size,(int) ((System.nanoTime() - t0) / 1e6), (int) (lastLoadScriptTime / 1e6), (int) (tiles[0].getRenderer().getLastCompileTime() / 1e6), (int) (tiles[0].getRenderer().getLastKernelTime() / 1e6), (int) (lastImageTime / 1e6), (int) (lastPngTime / 1e6));

        os.write(bigBuff,0,size);

        return size;
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

    /**
     * Render a script with caching
     *
     * @param jobID  UniqueID for caching results
     * @param script
     * @param width
     * @param height
     * @return
     */
    private void makeRender(String jobID, String script, boolean useCache, Matrix4f view, int pixX, int pixY, int width, int height,
                            int[] pixels, BufferedImage image) {
        List<Instruction> inst = null;

        if (useCache) {
            inst = cache.get(jobID);
        }
        if (inst == null) {
            inst = loadScript(script);
            if (inst != null && inst.size() > 0) {
                cache.put(jobID, inst);
            }
        } else {
            lastLoadScriptTime = 0;
        }
        ArrayList progs = new ArrayList();

        for (int i = 0; i < numRenderers; i++) {
            boolean result = render[i].init(progs, inst, "", VERSION);

            if (!result) {
                CLProgram program = render[i].getProgram();
                printf("Status: %s\n", program.getBuildStatus());
                printf("Build Log: %s\n", program.getBuildLog());

                throw new IllegalArgumentException("Compile failed");
            }
        }

        Matrix4f inv_view = new Matrix4f(view);
        inv_view.invert();

        tiles[0].getRenderer().sendView(inv_view,tiles[0].getView());
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
        createImage(pixX,pixY,width, height, pixels, tiles[0].getDest(), image);
        lastImageTime = System.nanoTime() - t0;
    }

    private Matrix4f getView(float rotx) {
        float[] DEFAULT_TRANS = new float[]{0, 0, -4};
        float z = DEFAULT_TRANS[2];
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

    private List<Instruction> loadScript(String script) {
        long t0 = System.nanoTime();
        ShapeJSEvaluator eval = new ShapeJSEvaluator();
        Bounds bounds = new Bounds();
        DataSource source = eval.runScript(script, bounds);

        OpenCLOpWriterV2 writer = new OpenCLOpWriterV2();
        Vector3d scale;
        scale = new Vector3d((bounds.xmax - bounds.xmin) / 2.0, (bounds.ymax - bounds.ymin) / 2.0, (bounds.zmax - bounds.zmin) / 2.0);
        worldScale = (float) Math.min(Math.min(scale.x, scale.y), scale.z);

        //printf("Scale is: %s\n", scale);
        List<Instruction> inst = writer.generate((Parameterizable) source, scale);

        printf("Instructions: %d\n",inst.size());
        lastLoadScriptTime = System.nanoTime() - t0;
        return inst;
    }

    private BufferedImage createImage(int x0, int y0, int width, int height, int[] pixels, CLBuffer<IntBuffer> buffer, BufferedImage image) {
        //int[] pixels = new int[buffer.getBuffer().capacity()];
        buffer.getBuffer().get(pixels).rewind();
        WritableRaster raster = image.getRaster();
        int[] pixel = new int[4];
        int r, g, b;
        long t0 = System.nanoTime();
        int xorig = x0 * width;
        int yorig = y0 * height;

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
                raster.setPixel(xorig + w, yorig + h, pixel);
            }
        }

        lastImageTime = (System.nanoTime() - t0);
        //printf("alloc time: %d\n", (int) ((System.nanoTime() - t0) / 1e6));
        return image;
    }

}
