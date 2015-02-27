package render;

import abfab3d.grid.Bounds;
import abfab3d.param.Parameterizable;
import abfab3d.util.DataSource;
import com.jogamp.opencl.*;
import com.objectplanet.image.PngEncoder;
import datasources.Instruction;
import opencl.CLCodeBuffer;
import opencl.CLCodeMaker;
import org.apache.commons.io.FileUtils;
import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.TJCompressor;
import shapejs.ShapeJSEvaluator;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static com.jogamp.opencl.CLDevice.Type.CPU;
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

    static final boolean DEBUG = true;
    public static final int IMAGE_JPEG = 0;
    public static final int IMAGE_PNG = 1;
    
    public String version = VolumeRenderer.VERSION_OPCODE_V2_DIST;

    private int numTiles;
    private RenderTile[] tiles;
    private int numRenderers;
    private VolumeRenderer[] render;
    private DeviceResources[] devices;
    private int numDevices;
    private float worldScale;
    // timig params for STAT 
    private long lastLoadScriptTime;
    private long lastImageTime;
    private long lastInitializationTime;
    private long lastRenderTime;
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

        CLPlatform platform = CLPlatform.getDefault();

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

        tile.setContext(CLContext.create(tile.getDevice()));

        tile.setQueue(tile.getDevice().createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE));

        render[0] = new VolumeRenderer(tile.getContext(), tile.getCommandQueue());
        tile.setRenderer(render[0]);
        tile.setView(tile.getContext().createFloatBuffer(16, READ_ONLY));

        IntBuffer dest = ByteBuffer.allocateDirect(height * width * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        tile.setDest(tile.getContext().createBuffer(dest, CLBuffer.Mem.WRITE_ONLY, CLBuffer.Mem.ALLOCATE_BUFFER));

        float expandFactor = 1.5f;  // Account for png's which get larger, is this really necessary?
        int imgSize = (int)(width * height * expandFactor);

        int jpgSize = 0;
        try {
            jpgSize = TJ.bufSize(width, height, TJ.SAMP_420);
        } catch(Exception e) {e.printStackTrace();}

        imgSize = Math.max(imgSize,jpgSize);

        // TODO: these need to be thread local resources
        buff = new byte[imgSize];
        pixels = new int[width * height];
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);


        int frames = 36;
        int frameX = 6;
        int frameY = frames / frameX;

        allocBuffers(width,height,frames,frameX);

        initialized = true;
    }

    public void setVersion(String version_value) {
        this.version = version_value;
    }

    public String getVersion() {
        return version;
    }

    public TimeStat getTimeStat() {
        TimeStat ts = new TimeStat(lastLoadScriptTime,getLastCompileTime(), lastImageTime, getLastKernelTime(),lastPngTime, lastInitializationTime, lastRenderTime);
        return ts;
    }

    public long getLastKernelTime() {
        long ret_val = 0;

        for(int i=0; i < render.length; i++) {
            ret_val = Math.max(render[i].getLastKernelTime(),ret_val);
        }

        return ret_val;
    }

    public long getLastCompileTime() {
        long ret_val = 0;

        for(int i=0; i < render.length; i++) {
            ret_val = Math.max(render[i].getLastCompileTime(),ret_val);
        }

        return ret_val;
    }

    private void allocBuffers(int width, int height, int frames, int frameX) {
        int frameY = frames / frameX;
        printf("AllocBuffers: frames: %d frameX: %d frameY: %d\n",frames,frameX,frameY);

        float expandFactor = 1.5f;
        int imgSize = (int)(width * height * expandFactor * frames);

        /*
        int jpgSize = 0;
        try {
            jpgSize = TJ.bufSize(width * frameX, height * frameY, TJ.SAMP_420);
        } catch(Exception e) {e.printStackTrace();}

        imgSize = Math.max(imgSize,jpgSize);
*/
        if (bigBuff == null || bigBuff.length < imgSize) {
            bigBuff = new byte[(int) (imgSize)];
            bigPixels = new int[width * height];
            bigImage = new BufferedImage(width * frameX, height * frameY, BufferedImage.TYPE_INT_ARGB);
            printf("Realloc buffers: imgSize: %d   w: %d  h: %d\n",imgSize,bigImage.getWidth(),bigImage.getHeight());
        }
    }

    public int render(String jobID, String script, Map<String,Object> params, Matrix4f view,boolean cache, int imgType, float quality, OutputStream os) throws IOException {
        if (!initialized) {
            throw new IllegalArgumentException("Renderer not initialized");
        }

        long t0 = System.nanoTime();

        makeRender(jobID, script, params,cache, quality, view,0,0,width, height, pixels, image);
        int size = 0;
        switch(imgType) {
            case IMAGE_PNG:
                PngEncoder encoder = new PngEncoder(PngEncoder.COLOR_TRUECOLOR, PngEncoder.BEST_SPEED);
                size = makePng(encoder, image, buff);  // return buff[0] to size bytes to client
                break;
            case IMAGE_JPEG:
                size = makeJpg(image, buff);
                break;
        }
        if(DEBUG)printf("total size: %d time: %d ms\n  js eval: %d\n  ocl compile: %d ms\n  kernel: %d ms\n  image: %d ms\n  png: %d ms\n", 
                        size,(int) ((System.nanoTime() - t0) / 1e6), (int) (lastLoadScriptTime / 1e6), (int) (tiles[0].getRenderer().getLastCompileTime() / 1e6), 
                        (int) (tiles[0].getRenderer().getLastKernelTime() / 1e6), (int) (lastImageTime / 1e6), (int) (lastPngTime / 1e6));

        os.write(buff,0,size);

        return size;
    }

    public int render(String jobID, File file, Map<String,Object> params, Matrix4f view,boolean cache, int imgType, float quality, OutputStream os) throws IOException {
        String script = FileUtils.readFileToString(file);

        return render(jobID, script, params, view, cache, imgType, quality, os);
    }

    public int renderImages(String jobID, String script, Map<String,Object> params, Matrix4f view,int frames, int frameX, boolean useCache, int imgType, OutputStream os) throws IOException {
        if (!initialized) {
            throw new IllegalArgumentException("Renderer not initialized");
        }

        long t0 = System.nanoTime();

        // Do these items once for the servlet.  Should be per-thread resources

        printf("initCL time: %d ms\n", (int) ((System.nanoTime() - t0) / 1e6));

        // End of per-thread resources


        t0 = System.nanoTime();
        int pixX = 0;
        int pixY = 0;

        List<Instruction> inst = null;

        if (useCache && jobID != null) {
            inst = cache.get(jobID);
        }
        if (inst == null) {
            inst = loadScript(script,params);
            if (inst != null && inst.size() > 0 && jobID != null && useCache) {
                cache.put(jobID, inst);
            }
        } else {
            lastLoadScriptTime = 0;
        }
        VolumeScene vscene = new VolumeScene(new ArrayList(), inst, "", version);
        for (int i = 0; i < numRenderers; i++) {
            boolean result = render[i].init(vscene);

            if (!result) {
                CLProgram program = render[i].getProgram();
                printf("Status: %s\n", program.getBuildStatus());
                printf("Build Log: %s\n", program.getBuildLog());

                throw new IllegalArgumentException("Compile failed");
            }
        }

        allocBuffers(width,height,frames,frameX);

        float drot = 360f / frames;
        float rotx = 0;

        printf("Rot degrees: %f\n",drot);
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
        int size = 0;
        switch(imgType) {
            case IMAGE_PNG:
                PngEncoder encoder = new PngEncoder(PngEncoder.COLOR_TRUECOLOR, PngEncoder.BEST_SPEED);
                size = makePng(encoder, bigImage, bigBuff);  // return buff[0] to size bytes to client
                break;
            case IMAGE_JPEG:
                size = makeJpg(bigImage, bigBuff);
                break;
        }

        printf("total size: %d time: %d ms\n\tjs eval: %d\n\tocl compile: %d ms\n\tkernel: %d ms\n\timage: %d ms\n\tpng: %d ms\n", 
               size,(int) ((System.nanoTime() - t0) / 1e6), (int) (lastLoadScriptTime / 1e6), (int) (tiles[0].getRenderer().getLastCompileTime() / 1e6), 
               (int) (tiles[0].getRenderer().getLastKernelTime() / 1e6), (int) (lastImageTime / 1e6), (int) (lastPngTime / 1e6));

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

    /**
     * Render a script with caching
     *
     * @param jobID  UniqueID for caching results
     * @param script
     * @param width
     * @param height
     * @return
     */
    private void makeRender(String jobID, String script, Map<String,Object> params, boolean useCache, float quality, Matrix4f view,
                            int pixX, int pixY, int width, int height,
                            int[] pixels, BufferedImage image) {

        VolumeScene vscene = new VolumeScene(new ArrayList(), null, "", version);
        if(DEBUG) printf("makeRender(%s, version:%s )\n", jobID, version);
        long t0 = System.nanoTime();

        if (version.equals(VolumeRenderer.VERSION_OPCODE_V3_DIST)) {
            
            if(DEBUG)printf("js evaluation\n");
            ShapeJSEvaluator eval = new ShapeJSEvaluator();
            Bounds bounds = new Bounds();
            DataSource source = eval.runScript(script, bounds,params);

            Vector3d scale;
            scale = new Vector3d((bounds.xmax - bounds.xmin) / 2.0, (bounds.ymax - bounds.ymin) / 2.0, (bounds.zmax - bounds.zmin) / 2.0);
            worldScale = (float) Math.min(Math.min(scale.x, scale.y), scale.z);

            CLCodeMaker maker = new CLCodeMaker();
            CLCodeBuffer ops = maker.makeCLCode((Parameterizable) source);

            vscene.setCLCode(ops);

            if(DEBUG)printf("code generation %5.2f ms\n", (System.nanoTime()-t0)*1.e-6);

        } else if (version.equals(VolumeRenderer.VERSION_DIST)) {
            ShapeJSEvaluator eval = new ShapeJSEvaluator();
            Bounds bounds = new Bounds();
            DataSource source = eval.runScript(script, bounds,params);
            
            Vector3d scale;
            scale = new Vector3d((bounds.xmax - bounds.xmin) / 2.0, (bounds.ymax - bounds.ymin) / 2.0, (bounds.zmax - bounds.zmin) / 2.0);
            worldScale = (float) Math.min(Math.min(scale.x, scale.y), scale.z);
            OpenCLWriter writer = new OpenCLWriter();
            vscene.setCode(writer.generate((Parameterizable) source, scale));
            if(DEBUG) printf("OpenCL Code: \n%s",vscene.getCode());
        } else { // 
            List<Instruction> inst = null;

            if (useCache && jobID != null && params.size() == 0) {
                inst = cache.get(jobID);
            }
            if (inst == null) {
                inst = loadScript(script, params);
                if (inst == null) {
                    throw new IllegalArgumentException("Script failed to load: " + script);
                }
                if (inst.size() > 0 && jobID != null && useCache) {
                    cache.put(jobID, inst);
                }

            } else {
                lastLoadScriptTime = 0;
            }

            vscene.setInstructions(inst);
        }

        for (int i = 0; i < numRenderers; i++) {
            if (quality >= 0.75) {
                // high quality
                render[i].setMaxSteps(1024);
                render[i].setMaxAntialiasingSteps(2);
                // TODO: Eventually add shadows
            } else if (quality <= 0.25) {
                // low quality
                render[i].setMaxSteps(256);
                render[i].setMaxAntialiasingSteps(0);
            } else {
                // normal
                render[i].setMaxSteps(512);
                render[i].setMaxAntialiasingSteps(0);
            }
            boolean result = render[i].init(vscene);

            if (!result) {
                CLProgram program = render[i].getProgram();
                printf("Status: %s\n", program.getBuildStatus());
                printf("Build Log: %s\n", program.getBuildLog());

                throw new IllegalArgumentException("Compile failed");
            }
        }
        lastInitializationTime = (System.nanoTime() - t0);
        //if(DEBUG) printf("frame initialization time: %5.2f ms\n",(System.nanoTime() - t0)*1.e-6);    

        t0 = System.nanoTime();

        Matrix4f inv_view = new Matrix4f(view);
        inv_view.invert();
        t0 = System.nanoTime();
        tiles[0].getRenderer().sendView(inv_view,tiles[0].getView());
        for (int i = 0; i < numTiles; i++) {
            RenderTile tile = tiles[i];

            if (version.equals(VolumeRenderer.VERSION_OPCODE_V3_DIST)) {
                tile.getRenderer().renderStruct(tile.getX0(), tile.getY0(), tile.getWidth(), tile.getHeight(), width, height,
                        worldScale, tile.getDest());
            }  else if (version.equals(VolumeRenderer.VERSION_DIST)) {
                tile.getRenderer().render(inv_view, tile.getWidth(), tile.getHeight(), tiles[0].getView(), render[i].getCommandQueue(), tile.getDest());
            } else {
                tile.getRenderer().renderOps(tile.getX0(), tile.getY0(), tile.getWidth(), tile.getHeight(), width, height,
                        worldScale, tile.getDest());
            }
            tile.getCommandQueue().putReadBuffer(tile.getDest(), false); // read results back (blocking read)
        }

        
        for (int i = 0; i < numRenderers; i++) {
            render[i].getCommandQueue().finish();
        }

        lastRenderTime = (System.nanoTime() - t0);
        //if(DEBUG) printf("frame render time: %5.2f ms\n",(System.nanoTime() - t0)*1.e-6);    

        t0 = System.nanoTime();
        createImage(pixX,pixY,width, height, pixels, tiles[0].getDest(), image);
        lastImageTime = System.nanoTime() - t0;    
    }

    private Matrix4f getView(float roty) {
        float[] DEFAULT_TRANS = new float[]{0, 0, -4};
        float z = DEFAULT_TRANS[2];
        float rotx = 0;

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

    private List<Instruction> loadScript(String script, Map<String,Object> params) {
        long t0 = System.nanoTime();
        ShapeJSEvaluator eval = new ShapeJSEvaluator();
        Bounds bounds = new Bounds();
        DataSource source = eval.runScript(script, bounds,params);

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

    public static class TimeStat {
        
        public long script;
        public long compile;
        public long kernel;
        public long png;
        public long image;
        public long total; 
        public long initialization;
        public long render;

        public TimeStat(long script, long compile, long image, long kernel, long png, long initTime, long renderTime){
            this.script = script;
            this.compile = compile;
            this.image = image;
            this.kernel = kernel;
            this.png = png;
            this.initialization = initTime;
            this.render = renderTime;
            this.total = script + compile + image + kernel + png;
            
        }

        public String getHeader(){
            return "script  compile kernel image  png   total(ms)  init render ";
        }
        public String toString(){
            double f = 1.e-6;
            return fmt("%6.2f %6.1f %6.1f %6.1f %6.1f %6.1f %6.1f %6.1f", script*f,compile*f,kernel*f,image*f, png*f, total*f, initialization*f, render*f);
        }
    }

}
