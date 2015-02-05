package render;

import abfab3d.util.Units;
import com.jogamp.opencl.*;
import com.jogamp.opencl.gl.CLGLBuffer;
import datasources.Instruction;
import gpu.GPUUtil;
import org.apache.commons.io.FileUtils;
import program.ProgramLoader;

import javax.vecmath.Matrix4f;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static abfab3d.util.Output.printf;
import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;

/**
 * Volume renderer using GPU.  Renders to any CLBuffer.
 * This class only handles rendering, it should not deal with window events or require OpenGL.
 *
 * @author Alan Hudson
 */
public class VolumeRenderer {
    private static final boolean DEBUG = true;
    private static final boolean STATS = true;
    private static final boolean CACHE_PROGRAM = true;
    private static final String CACHE_LOCATION = "/tmp/openCL_cache";

    public static final String VERSION_DIST = "dist";
    public static final String VERSION_OPCODE = "opcode";
    public static final String VERSION_OPCODE_V2 = "opcode_v2";
    public static final String VERSION_OPCODE_V2_DIST = "opcode_v2_dist";
    public static final String VERSION_DENS = "dens";

    private float[] viewData = new float[16];
    private CLBuffer<FloatBuffer> viewBuffer;
    private int maxSteps = 512;
    private int maxShadowSteps = 0;
    private int maxAntialiasingSteps = 0;
    private CLContext context;
    private CLCommandQueue queue;
    private CLProgram program;
    private CLKernel kernel;
    private long compileTime;
    private long renderTime;
    private long kernelTIme;
    private String renderVersion = VERSION_DIST;
    private String kernelName;


    // Prototype vars for opcode
    private int opLen;
    private CLBuffer<FloatBuffer> floatBuffer;
    private CLBuffer<IntBuffer> intBuffer;
    private CLBuffer<FloatBuffer> floatVectorBuffer;
    private CLBuffer<FloatBuffer> matrixBuffer;
    private CLBuffer<ByteBuffer> booleanBuffer;
    private CLBuffer<IntBuffer> opBuffer;


    public VolumeRenderer(CLContext context, CLCommandQueue queue) {
        this.context = context;
        this.queue = queue;
    }

    /**
     * Initialize the renderer with the provided user scripts.
     *
     * @param progs Can either be File or String objects.
     * @param opts  The build options
     * @param version The version to load, empty for regular, "_dist" or "_opcode"
     */
    public boolean init(List progs, List<Instruction> instructions, String opts, String version) {
        String kernel_name;

        renderVersion = version;

        printf("VolumeRenderer Init: %s\n",version);
        long t0 = System.nanoTime();
        try {
            String buildOpts = "";
            if (opts != null) buildOpts = opts;
            double vs = 0.1 * Units.MM;
            printf("Voxel Size: %f\n",vs);
            printf("MaxSteps: %d\n",maxSteps);
            buildOpts += " -cl-fast-relaxed-math";
            buildOpts += " -cl-no-signed-zeros";
            buildOpts += " -DmaxSteps=" + maxSteps;
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
            printf("Building program with opts: %s\n", buildOpts);

            boolean from_cache = false;
            try {
                if (CACHE_PROGRAM) {
                    CLDevice[] devices = context.getDevices();
                    int len = devices.length;
                    HashMap<CLDevice, byte[]> bins = new HashMap<CLDevice, byte[]>();

                    for (int i = 0; i < len; i++) {
                        CLDevice device = devices[i];
                        String dir = CACHE_LOCATION + File.separator + device.getName() + "_" + device.getDriverVersion();
                        File f = new File(dir + File.separator + renderVersion + "_compiled.ocl");
                        if (f.exists()) {
                            if (DEBUG) printf("Loading OpenCL program from binary\n");
                            byte[] bytes = FileUtils.readFileToByteArray(f);
                            bins.put(device, bytes);
                        }

                        if (bins.size() == len) {
                            program = context.createProgram(bins);
                            program.build(buildOpts);

                            from_cache = true;
                            if (DEBUG) printf("Successfully loaded cached OpenCL binaries\n");
                        }
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

            if (program == null) {
                printf("Compiling text program:\n");
                //program = ProgramLoader.load(clContext, "VolumeRenderer.cl");
                ArrayList list = new ArrayList();
                list.add(new File("ShapeJS_" + renderVersion + ".cl"));
                list.addAll(progs);

                if (renderVersion.equals(VolumeRenderer.VERSION_OPCODE_V2) || renderVersion.equals(VolumeRenderer.VERSION_OPCODE_V2_DIST)) {
                    // TODO: this would need to be updated to support jar file deployment
                    File dir = new File("classes");
                    boolean in_jar = false;

                    String[] files = dir.list();

                    if (files == null) {
                        in_jar = true;
                        files = new String[] {"box_opcode_v2_dist.cl",
                             "gyroid_opcode_v2_dist.cl",
                             "intersection_opcode_v2_dist.cl",
                             "rotation_opcode_v2_dist.cl",
                             "scale_opcode_v2_dist.cl",
                             "sphere_opcode_v2_dist.cl",
                             "subtraction_opcode_v2_dist.cl",
                             "torus_opcode_v2_dist.cl",
                             "translation_opcode_v2_dist.cl",
                             "union_opcode_v2_dist.cl"};
                    }

                    String rv = renderVersion + ".cl";
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].contains(rv)) {
                            if (files[i].contains("VolumeRenderer") || files[i].contains("ShapeJS")) {
                                continue;
                            }
                            list.add(new File(files[i]));
                        }
                    }
                }
                list.add(new File("VolumeRenderer_" + renderVersion + ".cl"));


                if (DEBUG) {
                    printf("Loading files:\n");
                    for (int i = 0; i < list.size(); i++) {
                        printf("%s\n", list.get(i));
                    }
                    printf("Prog: \n%s\n", progs);
                }
                program = ProgramLoader.load(context, list);
                program.build(buildOpts);
            }


            if (DEBUG) printf("Build status: %s\n",program.getBuildStatus());
            if (!program.isExecutable()) return false;

            if (CACHE_PROGRAM && !from_cache) {
                printf("Caching program\n");
                Map<CLDevice,byte[]> bins = program.getBinaries();

                for(Map.Entry<CLDevice,byte[]> entry : bins.entrySet()) {
                    CLDevice device = entry.getKey();
                    byte[] compiled = entry.getValue();
                    String dir = CACHE_LOCATION + File.separator + device.getName() + "_" + device.getDriverVersion();
                    File f = new File(dir);
                    f.mkdirs();
                    f = new File(dir + File.separator + renderVersion + "_compiled.ocl");
                    FileUtils.writeByteArrayToFile(f,compiled);

                }
            }

            if (renderVersion.equals(VERSION_OPCODE) || renderVersion.equals(VERSION_OPCODE_V2) || renderVersion.equals(VERSION_OPCODE_V2_DIST)) {
                float[] fparams;
                int[] iparams;
                float[] fvparams;
                boolean[] bparams;
                float[] mparams;
                int[] ops;

                int f_count = 0;
                int i_count = 0;
                int fv_count = 0;
                int b_count = 0;
                int op_count = 0;
                int m_count = 0;

                for(Instruction inst : instructions) {
                    inst.compact();

                    f_count += inst.getFloatCount();
                    i_count += inst.getIntCount();
                    fv_count += inst.getFloatVectorCount();
                    b_count += inst.getBooleanCount();
                    m_count += inst.getMatrixCount();
                    op_count++;
                }

                int f_idx = 0;
                int i_idx = 0;
                int fv_idx = 0;
                int m_idx = 0;
                int b_idx = 0;
                int op_idx = 0;

                fparams = new float[f_count];
                iparams = new int[i_count];
                fvparams = new float[fv_count * 3];
                mparams = new float[fv_count * 16];
                bparams = new boolean[b_count];
                ops = new int[op_count];
                opLen = op_count;

                for(Instruction inst : instructions) {
                    inst.getFloatParams(fparams,f_idx);
                    f_idx += inst.getFloatCount();

                    inst.getIntParams(iparams, i_idx);
                    i_idx += inst.getIntCount();

                    inst.getFloatVectorParams(fvparams, fv_idx);
                    fv_idx += inst.getFloatVectorCount();

                    inst.getMatrixParams(mparams, m_idx);
                    m_idx += inst.getMatrixCount();

                    inst.getBooleanParams(bparams, b_idx);
                    b_idx += inst.getBooleanCount();

                    ops[op_idx++] = inst.getOpCode();
                }

                if (f_count != 0) {
                    floatBuffer = context.createFloatBuffer(f_count, READ_ONLY);
                    floatBuffer.getBuffer().put(fparams);
                    floatBuffer.getBuffer().rewind();
                } else {
                    floatBuffer = context.createFloatBuffer(1, READ_ONLY);
                    floatBuffer.getBuffer().rewind();
                }
                if (i_count != 0) {
                    intBuffer = context.createIntBuffer(i_count, READ_ONLY);
                    intBuffer.getBuffer().put(iparams);
                    intBuffer.getBuffer().rewind();
                } else {
                    intBuffer = context.createIntBuffer(1, READ_ONLY);
                    intBuffer.getBuffer().rewind();
                }
                if (fv_count != 0) {
                    /*
                    floatVectorBuffer = context.createFloatBuffer(fv_count * 3, READ_ONLY);
                    floatVectorBuffer.getBuffer().put(fvparams);
                    floatVectorBuffer.getBuffer().rewind();
                    */
                    // TODO: these have to be memory aligned to 64 bit chunks
                    floatVectorBuffer = context.createFloatBuffer(fv_count * 4, READ_ONLY);
                    FloatBuffer fb = floatVectorBuffer.getBuffer();

                    fb.rewind();
                    int len = fvparams.length / 3;
                    for(int i=0; i < len; i++) {
                        fb.put(fvparams[i*3]);
                        fb.put(fvparams[i*3+1]);
                        fb.put(fvparams[i*3+2]);
                        fb.put(0);
                    }
                    floatVectorBuffer.getBuffer().rewind();

                } else {
                    floatVectorBuffer = context.createFloatBuffer(1 * 4, READ_ONLY);
                    floatVectorBuffer.getBuffer().rewind();
                }

                if (m_count != 0) {
                    matrixBuffer = context.createFloatBuffer(fv_count * 16, READ_ONLY);
                    matrixBuffer.getBuffer().put(mparams);
                    matrixBuffer.getBuffer().rewind();
                } else {
                    matrixBuffer = context.createFloatBuffer(1 * 16, READ_ONLY);
                    matrixBuffer.getBuffer().rewind();
                }

                if (b_count != 0) {
                    booleanBuffer = context.createByteBuffer(b_count, READ_ONLY);
                    byte[] temp = new byte[bparams.length];
                    for(int i=0; i < bparams.length; i++) {
                        if (bparams[i]) temp[i] = 1;
                    }
                    booleanBuffer.getBuffer().put(temp);
                    booleanBuffer.getBuffer().rewind();
                } else {
                    booleanBuffer = context.createByteBuffer(1, READ_ONLY);
                    booleanBuffer.getBuffer().rewind();
                }

                opBuffer = context.createIntBuffer(op_count, READ_ONLY);
                opBuffer.getBuffer().put(ops);
                opBuffer.getBuffer().rewind();


                // Call OpenCL kernel
                CLEventList events = new CLEventList(6);

                // no need to wait for any of this
                queue.putWriteBuffer(opBuffer, false, null, events);
                if (floatBuffer != null) queue.putWriteBuffer(floatBuffer, false, null, events);
                if (intBuffer != null) queue.putWriteBuffer(intBuffer, false, null, events);
                if (floatVectorBuffer != null) queue.putWriteBuffer(floatVectorBuffer, false, null, events);
                if (matrixBuffer != null) queue.putWriteBuffer(matrixBuffer, false, null, events);
                if (booleanBuffer != null) queue.putWriteBuffer(booleanBuffer, false, null, events);

            }
        } catch (Exception e) {
            if (program == null) {
                e.printStackTrace();
            } else {
                String src = program.getSource();
                printf("Src: \n%s", src);
                printf("End Source\n");
                e.printStackTrace();
            }
            return false;
        }

        kernelName = kernel_name;
        kernel = program.createCLKernel(kernel_name);
        compileTime = (System.nanoTime() - t0);
        
        return true;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public void setMaxShadowSteps(int maxShadowSteps) {
        this.maxShadowSteps = maxShadowSteps;
    }

    public void setMaxAntialiasingSteps(int maxAntialiasingSteps) {
        this.maxAntialiasingSteps = maxAntialiasingSteps;
    }

    /**
     * Get the compiled program.  Useful for getting at the logs.
     * 
     * @return
     */
    public CLProgram getProgram() {
        return program;
    }

    public void sendView(Matrix4f invView, CLBuffer<FloatBuffer> viewBuffer) {
        viewData[0] = invView.m00;
        viewData[1] = invView.m01;
        viewData[2] = invView.m02;
        viewData[3] = invView.m03;
        viewData[4] = invView.m10;
        viewData[5] = invView.m11;
        viewData[6] = invView.m12;
        viewData[7] = invView.m13;
        viewData[8] = invView.m20;
        viewData[9] = invView.m21;
        viewData[10] = invView.m22;
        viewData[11] = invView.m23;
        viewData[12] = invView.m30;
        viewData[13] = invView.m31;
        viewData[14] = invView.m32;
        viewData[15] = invView.m33;

        viewBuffer.getBuffer().put(viewData);
        viewBuffer.getBuffer().rewind();
        this.viewBuffer = viewBuffer;

        queue.putWriteBuffer(viewBuffer, true, null);
    }
    /**
     * Render the program from the desired view into a buffer.
     *
     * @param dest
     */
    public void renderOps(int w0, int h0, int wsize, int hsize, int width, int height,
                          float worldScale, CLBuffer dest) {

        //printf("RenderOps: w0: %d h0: %d wsize: %d hsize: %d width:%d height: %d dest: %s this: %s\n",w0,h0,wsize,hsize,width,height,dest,this);
        long t0 = System.nanoTime();

        // TODO: needs 0 for Apple, 8 is fastest on Desktop GPU
        int localWorkSizeX = 8; // this seems the fastest not sure why
        int localWorkSizeY = 8;

        long globalWorkSizeX = GPUUtil.roundUp(localWorkSizeX,wsize);
        long globalWorkSizeY = GPUUtil.roundUp(localWorkSizeY,hsize);

        //printf("inv view: \n%s\n",view);

        // Call OpenCL kernel
        CLEventList list = new CLEventList(3);

        boolean usingGL = (dest instanceof CLGLBuffer);

        if (usingGL) {
            queue.putAcquireGLObject((CLGLBuffer) dest, list);
        }

        kernel.setArg(0, dest).rewind();
        kernel.setArg(1, w0 * wsize);
        kernel.setArg(2, h0 * hsize);
        kernel.setArg(3, wsize);
        kernel.setArg(4, hsize);
        kernel.setArg(5, width);
        kernel.setArg(6, height);
        kernel.setArg(7, viewBuffer).rewind();
        kernel.setArg(8, worldScale);
        kernel.setArg(9,opBuffer).rewind();
        kernel.setArg(10,opLen);
        kernel.setArg(11,floatBuffer).rewind();
        kernel.setArg(12,intBuffer).rewind();
        kernel.setArg(13,floatVectorBuffer).rewind();
        kernel.setArg(14,booleanBuffer).rewind();
        kernel.setArg(15,matrixBuffer).rewind();

//        queue.put2DRangeKernel(kernel, 0, 0, globalWorkSizeX, globalWorkSizeY, localWorkSizeX, localWorkSizeY, list);
        // Changed to 0 needed to work on MAC
        // TODO: Test
        //queue.put2DRangeKernel(kernel, 0, 0, wsize, hsize, 0, 0, list);

        queue.put2DRangeKernel(kernel, 0, 0, globalWorkSizeX, globalWorkSizeY, localWorkSizeX, localWorkSizeY, list);
        if (usingGL) {
            queue.putReleaseGLObject((CLGLBuffer) dest, list);
        }
        queue.finish();

        if (STATS) {
            int idx = 0;
            if (usingGL) idx++;
            kernelTIme = list.getEvent(idx).getProfilingInfo(CLEvent.ProfilingCommand.END) - list.getEvent(idx).getProfilingInfo(CLEvent.ProfilingCommand.START);
            renderTime = System.nanoTime() - t0;
        }
/*
        for(int i=0; i < list.size(); i++) {
            CLEvent event = list.getEvent(i);
            System.out.println("cmd: " + i + " " + event.getType() + " time: " + (event.getProfilingInfo(CLEvent.ProfilingCommand.END)
                    - event.getProfilingInfo(CLEvent.ProfilingCommand.START))/1000000.0);
        }
*/

    }

    /**
     * Render the program from the desired view into a buffer.
     *
     * @param view
     * @param viewBuffer
     * @param queue
     * @param dest
     */
    public void render(Matrix4f view, int width, int height, CLBuffer<FloatBuffer> viewBuffer, CLCommandQueue queue,
                       CLBuffer dest) {
        long t0 = System.nanoTime();

        int localWorkSizeX = 0; // this seems the fastest not sure why
        int localWorkSizeY = 0;
/*
        int localWorkSizeX = 8; // this seems the fastest not sure why
        int localWorkSizeY = 8;
*/
        long globalWorkSizeX = GPUUtil.roundUp(localWorkSizeX, width);
        long globalWorkSizeY = GPUUtil.roundUp(localWorkSizeY,height);

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

        boolean usingGL = (dest instanceof CLGLBuffer);

        if (usingGL) {
            queue.putAcquireGLObject((CLGLBuffer) dest, list);
        }

        queue.putWriteBuffer(viewBuffer, true, null, list);

        kernel.setArg(0, dest).rewind();
        kernel.setArg(1, width);
        kernel.setArg(2, height);
        kernel.setArg(3, viewBuffer).rewind();

//        queue.put2DRangeKernel(kernel, 0, 0, globalWorkSizeX, globalWorkSizeY, localWorkSizeX, localWorkSizeY, list);
        // Changed to 0 needed to work on MAC
        queue.put2DRangeKernel(kernel, 0, 0, globalWorkSizeX, globalWorkSizeY, 0, 0, list);
        if (usingGL) {
            queue.putReleaseGLObject((CLGLBuffer) dest, list);
        }
        queue.finish();

        int idx = 1;
        if (usingGL) idx++;
        kernelTIme = list.getEvent(idx).getProfilingInfo(CLEvent.ProfilingCommand.END) - list.getEvent(idx).getProfilingInfo(CLEvent.ProfilingCommand.START);
        /*
        for(int i=0; i < list.size(); i++) {
            CLEvent event = list.getEvent(i);
            System.out.println("cmd: " + i + " time: " + (event.getProfilingInfo(CLEvent.ProfilingCommand.END)
                    - event.getProfilingInfo(CLEvent.ProfilingCommand.START))/1000000.0);
        }
        */

        renderTime = System.nanoTime() - t0;
    }

    /**
     * Get the total rendering time.
     * @return The time in nanoseconds
     */
    public long getLastTotalRenderTime() {
        return renderTime;
    }

    /**
     * Get the render time reported by OpenCL.  No transfers, just the kernel call
     * @return The time in nanoseconds
     */
    public long getLastKernelTime() {
        return kernelTIme;
    }

    public long getLastCompileTime() {
        return compileTime;
    }

    public CLCommandQueue getCommandQueue() {
        return queue;
    }

    /**
     * Cleanup resources used
     */
    public void cleanup() {
        if (kernel != null) kernel.release();
        if (program != null) program.release();
    }
}
