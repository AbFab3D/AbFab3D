package prototype;

import com.jogamp.opencl.*;
import com.jogamp.opencl.util.CLDeviceFilters;
import program.ProgramLoader;

import javax.media.opengl.GLContext;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import javax.vecmath.Vector3d;

import static abfab3d.util.Output.printf;
import static com.jogamp.opencl.CLDevice.Type.GPU;
import static com.jogamp.opencl.CLDevice.Type.CPU;
import static com.jogamp.opencl.util.CLPlatformFilters.type;
import static java.lang.System.*;
import static com.jogamp.opencl.CLMemory.Mem.*;
import static java.lang.Math.*;

/**
 * Hello Java OpenCL example. Adds all elements of buffer A to buffer B
 * and stores the result in buffer C.<br/>
 * Sample was inspired by the Nvidia VectorAdd example written in C/C++
 * which is bundled in the Nvidia OpenCL SDK.
 * @author Michael Bien
 */
public class Prototype {

    static void testAddCPU(int count){
        float bufferA[] = new float[count];
        float bufferB[] = new float[count];
        float bufferC[] = new float[count];
        fillBuffer(bufferA, 10);
        fillBuffer(bufferB, 11);
        long time = nanoTime();
        for(int i = 0; i < count; i++){
            bufferC[i] = bufferA[i] + bufferB[i];
        }
        time = nanoTime() - time;
        System.out.printf("CPU calculations took %d ms\n", time/1000000);
        
    }

    

    static void testAddOpenCL(int elementCount) throws Exception{

        // set up (uses default CLPlatform and creates context for all devices)
        //CLContext context = CLContext.create();

        // always make sure to release the context under all circumstances
        // not needed for this particular sample but recommented
        //CLDevice device = CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice(CLDeviceFilters.queueMode(CLCommandQueue.Mode.OUT_OF_ORDER_MODE));
        CLPlatform platforms[] = CLPlatform.listCLPlatforms();
        for(int i =0; i < platforms.length; i++){
            out.printf("platform[%d]: %s\n", i, platforms[i]);
            CLDevice devices[] = platforms[i].listCLDevices();
            for(int k = 0; k < devices.length;k++){
                out.printf("  device[%d]: %s\n", k, devices[k]);                
            }
        }

        CLDevice device = CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice();
        //CLDevice device = platforms[0].listCLDevices()[0];
        
        
        CLContext context = CLContext.create(device);

        try{

            CLDevice[] devices = context.getDevices();
            for(int i=0; i < devices.length; i++) {
                System.out.println("Name: " + devices[i].getName() + " compute_units: " + devices[i].getMaxComputeUnits() + " freq: " + devices[i].getMaxClockFrequency());
            }

            out.println("using "+device);

            // create command queue on device.
            CLCommandQueue queue = device.createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);

            int localWorkSize = min(device.getMaxWorkGroupSize(), 256);  // Local work size dimensions
            int globalWorkSize = roundUp(localWorkSize, elementCount);   // rounded up to the nearest multiple of the localWorkSize

            // load sources, create and build program
            CLProgram program = ProgramLoader.load(context,"Prototype.cl");
            String buildOpts = "";
            program.build(buildOpts);

            printf("Status: %s\n",program.getBuildStatus());

            if (!program.isExecutable()) {
                printf("log: %s\n",program.getBuildLog());
                throw new IllegalArgumentException("Program didn't compile");
            }

            // A, B are input buffers, C is for the result
            CLBuffer<FloatBuffer> clBufferA = context.createFloatBuffer(globalWorkSize, READ_ONLY);
            CLBuffer<FloatBuffer> clBufferB = context.createFloatBuffer(globalWorkSize, READ_ONLY);
            CLBuffer<FloatBuffer> clBufferC = context.createFloatBuffer(globalWorkSize, WRITE_ONLY);

            out.println("used device memory: "
                + (clBufferA.getCLSize()+clBufferB.getCLSize()+clBufferC.getCLSize())/1000000 +"MB");

            // fill input buffers with random numbers
            // (just to have test data; seed is fixed -> results will not change between runs).
            fillBuffer(clBufferA.getBuffer(), 10);
            fillBuffer(clBufferB.getBuffer(), 11);

            // get a reference to the kernel function with the name 'VectorAdd'
            // and map the buffers to its input parameters.
            CLKernel kernel = program.createCLKernel("VectorAdd");
            out.printf("VectorAdd kernel: %s\n", kernel);
            out.printf("ByteReader kernel: %s\n", program.createCLKernel("ByteReader"));
            kernel.putArgs(clBufferA, clBufferB, clBufferC);
            kernel.putArg(elementCount);

            CLEventList list = new CLEventList(4);

            // asynchronous write of data to GPU device,
            // followed by blocking read to get the computed results back.

            long time = nanoTime();
            final CLEventList events = new CLEventList(2);
            long tk = nanoTime();
            
            queue.putWriteBuffer(clBufferA, true,list);//false, events, list);
            printf("  putWriteBuffer() time %d ms\n", (nanoTime() - tk)/1000000);
            tk = nanoTime();
            queue.putWriteBuffer(clBufferB, true,list);//false, events, list);
            printf("  putWriteBuffer() time %d ms\n", (nanoTime() - tk)/1000000);
            tk = nanoTime();
            queue.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize, list);
            printf("OpenCL kernel time %d ms\n", (nanoTime() - tk)/1000000);
            tk = nanoTime();
            queue.putReadBuffer(clBufferC, true, list);
            printf("  putReadBuffer() time %d ms\n", (nanoTime() - tk)/1000000);

            time = (nanoTime() - time);

            for(int i=0; i < list.size(); i++) {
                CLEvent event = list.getEvent(i);
                System.out.println("cmd: " + i + " time: " + (event.getProfilingInfo(CLEvent.ProfilingCommand.END)
                          - event.getProfilingInfo(CLEvent.ProfilingCommand.START))/1000000.0);
            }
            // print first few elements of the resulting buffer to the console.
            out.println("a+b=c results snapshot: ");
            for(int i = 0; i < 20; i++)
                out.print(clBufferC.getBuffer().get() + ", ");
            out.printf("...; %d more \n", clBufferC.getBuffer().remaining()); 
            out.printf("OpenCL calculations took %d ms\n", time/1000000);

            
        }finally{
            // cleanup all resources associated with this context.
            context.release();
        }
    }

    private static void fillBuffer(FloatBuffer buffer, int m) {
        float c = 0;
        while(buffer.remaining() != 0){
            buffer.put(c);
            c += 1;
            if(c > m) c = 0;
        }
        buffer.rewind();
    }
    private static void fillBuffer(float buffer[], int m) {
        float c = 0;
        int cnt = 0;
        while(cnt < buffer.length){
            buffer[cnt++] = c;
            c += 1;
            if(c > m) c = 0;
        }
    }

    private static int roundUp(int groupSize, int globalSize) {
        int r = globalSize % groupSize;
        if (r == 0) {
            return globalSize;
        } else {
            return globalSize + groupSize - r;
        }
    }

    static void testByteReader(int elementCount) throws Exception{
        
        CLDevice device = CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice();
        CLContext context = CLContext.create(device);

        out.printf("context: %s\n", context);
        out.printf("device: %s\n", device);

        int localWorkSize = min(device.getMaxWorkGroupSize(), 256);  // Local work size dimensions
        int globalWorkSize = roundUp(localWorkSize, elementCount);   // rounded up to the nearest multiple of the localWorkSize
        
        CLCommandQueue queue = device.createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);
        CLProgram program = ProgramLoader.load(context,"Prototype.cl");
        String buildOpts = "";
        program.build(buildOpts);
        
        printf("ProgramBuildStatus: %s\n",program.getBuildStatus());
        
        if (!program.isExecutable()) {
            printf("log: %s\n",program.getBuildLog());
            throw new IllegalArgumentException("Program didn't compile");
        }
        
        CLKernel kernel = program.createCLKernel("ByteReader");
        out.printf("ByteReader kernel: %s\n", kernel);
        int opcodeCount = 100;
        CLBuffer<ByteBuffer> clBufferData = context.createByteBuffer(opcodeCount, READ_ONLY);
        CLBuffer<FloatBuffer> clBufferResult = context.createFloatBuffer(elementCount, WRITE_ONLY);
        
        initOpcodeBuffer(clBufferData.getBuffer(), opcodeCount);

        kernel.putArg(clBufferData);
        kernel.putArg(opcodeCount);
        kernel.putArg(clBufferResult);
        kernel.putArg(elementCount);


        CLEventList list = new CLEventList(4);
        queue.putWriteBuffer(clBufferData, true,list);
        queue.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize, list);
        queue.putReadBuffer(clBufferResult, true, list);

        for(int i = 0; i < 20; i++)
            out.printf("%f ", clBufferResult.getBuffer().get());        
        out.printf("\n  DONE\n");

        context.release();

    }

    static void testWordWriter(int elementCount) throws Exception{
        
        CLDevice device = CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice();
        CLContext context = CLContext.create(device);

        out.printf("context: %s\n", context);
        out.printf("device: %s\n", device);

        int localWorkSize = min(device.getMaxWorkGroupSize(), 256);  // Local work size dimensions
        int globalWorkSize = roundUp(localWorkSize, elementCount);   // rounded up to the nearest multiple of the localWorkSize
        
        CLCommandQueue queue = device.createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);
        CLProgram program = ProgramLoader.load(context,"Prototype.cl");
        String buildOpts = "";
        program.build(buildOpts);
        
        printf("ProgramBuildStatus: %s\n",program.getBuildStatus());
        
        if (!program.isExecutable()) {
            printf("log: %s\n",program.getBuildLog());
            throw new IllegalArgumentException("Program didn't compile");
        }
        
        CLKernel kernel = program.createCLKernel("WordWriter");
        out.printf("WordWriter kernel: %s\n", kernel);
        int opcodeCount = 200;
        int outCount = 200;
        CLBuffer<IntBuffer> clBufferData = context.createIntBuffer(opcodeCount, READ_ONLY);
        CLBuffer<IntBuffer> clBufferResult = context.createIntBuffer(outCount, WRITE_ONLY);
        
        initIntOpcodeBuffer(clBufferData.getBuffer(), opcodeCount);

        kernel.putArg(clBufferData);
        kernel.putArg(opcodeCount);
        kernel.putArg(clBufferResult);
        kernel.putArg(elementCount);

        CLEventList list = new CLEventList(4);
        queue.putWriteBuffer(clBufferData, true,list);
        queue.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize, list);
        queue.putReadBuffer(clBufferResult, true, list);

        for(int k = 0; k < 1; k++){
            int size = clBufferResult.getBuffer().get();
            out.printf("%4d ", size);        
            for(int i = 0; i < size; i++){
                int ri = (clBufferResult.getBuffer().get());
                float rf = Float.intBitsToFloat(ri);            
                out.printf("%8x(%6.2f) ", ri,rf);        
            }
            out.printf("\n");
        }

        context.release();

    }

    static void testStructWriting(int elementCount) throws Exception{
        
        CLDevice device = CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice();
        CLContext context = CLContext.create(device);
        int localWorkSize = min(device.getMaxWorkGroupSize(), 256);  // Local work size dimensions
        int globalWorkSize = roundUp(localWorkSize, elementCount);   // rounded up to the nearest multiple of the localWorkSize        
        CLCommandQueue queue = device.createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);
        CLProgram program = ProgramLoader.load(context,"OpcodeReader.cl");
        String buildOpts = "";
        program.build(buildOpts);        
        printf("ProgramBuildStatus: %s\n",program.getBuildStatus());
        
        if (!program.isExecutable()) {
            printf("log: %s\n",program.getBuildLog());
            throw new IllegalArgumentException("Program didn't compile");
        }
        
        CLKernel kernel = program.createCLKernel("StructReader");
        out.printf("StructReader kernel: %s\n", kernel);
        int bufferSize = 2000;
        CLBuffer<IntBuffer> clBufferData = context.createIntBuffer(bufferSize, READ_ONLY);
        CLBuffer<IntBuffer> clBufferResult = context.createIntBuffer(bufferSize, WRITE_ONLY);
        
        initStructBuffer(clBufferData.getBuffer());

        kernel.putArg(clBufferData);
        kernel.putArg(bufferSize);
        kernel.putArg(clBufferResult);
        kernel.putArg(bufferSize);

        CLEventList list = new CLEventList(4);
        queue.putWriteBuffer(clBufferData, true,list);
        queue.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize, list);
        queue.putReadBuffer(clBufferResult, true, list);

        while(true){

            int size = clBufferResult.getBuffer().get();
            if(size == 0) 
                break;
            out.printf("%4d ", size);        
            for(int i = 0; i < size; i++){
                int ri = (clBufferResult.getBuffer().get());
                float rf = Float.intBitsToFloat(ri);            
                out.printf("%8x(%6.2f) ", ri,rf);        
            }
            out.printf("\n");
        }    
        out.printf("\nDONE");
        context.release();
    }

    static void writeToByteBuffer(ByteBuffer buffer, int x){
    
        buffer.put((byte)(x & 0xFF));
        buffer.put((byte)((x >> 8) & 0xFF));
        buffer.put((byte)((x >> 16) & 0xFF));
        buffer.put((byte)((x >> 24) & 0xFF));
    } 

    static void writeToIntBuffer(IntBuffer buffer, int x){    
        buffer.put(x);
    } 

    static void writeToIntBuffer(IntBuffer buffer, int array[], int count){    
        for(int i = 0; i < count; i++){
            buffer.put(array[i]);
        }
    } 

    static void initStructBuffer(IntBuffer buffer){

        int workBuffer[] = new int[1000];

        CSphere s1 = new CSphere(1.10, new Vector3d(1.11, 1.12, 1.13));
        CSphere s2 = new CSphere(2.20, new Vector3d(2.11, 2.12, 2.13));
        CSphere s3 = new CSphere(3.20, new Vector3d(3.11, 3.12, 3.13));
        CGyroid g1 = new CGyroid(1.1, 2.2, 3.3, new Vector3d(4.11, 4.12, 4.13));

        s1.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]);
        s2.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]);
        s3.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]); 
        g1.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]); 

        buffer.rewind();
        
    }


    static void initOpcodeBuffer(ByteBuffer buffer, int length){

        writeToByteBuffer(buffer, 24);
        writeToByteBuffer(buffer, Float.floatToRawIntBits(1.1f));
        writeToByteBuffer(buffer, Float.floatToRawIntBits(2.2f));  //
        writeToByteBuffer(buffer, Float.floatToRawIntBits(3.3f));
        writeToByteBuffer(buffer, Float.floatToRawIntBits(4.4f));  //
        writeToByteBuffer(buffer, Float.floatToRawIntBits(5.5f));  //
        writeToByteBuffer(buffer, Float.floatToRawIntBits(6.6f));  //
        writeToByteBuffer(buffer, Float.floatToRawIntBits(7.7f));

        buffer.rewind();

    }

    static void initIntOpcodeBuffer(IntBuffer buffer, int length){

        writeToIntBuffer(buffer, 24);
        writeToIntBuffer(buffer, Float.floatToRawIntBits(1.1f));
        writeToIntBuffer(buffer, Float.floatToRawIntBits(2.2f));  //
        writeToIntBuffer(buffer, Float.floatToRawIntBits(3.3f));
        writeToIntBuffer(buffer, Float.floatToRawIntBits(4.4f));  //
        writeToIntBuffer(buffer, Float.floatToRawIntBits(5.5f));  //
        writeToIntBuffer(buffer, Float.floatToRawIntBits(6.6f));  //
        writeToIntBuffer(buffer, Float.floatToRawIntBits(7.7f));

        buffer.rewind();

    }
    
    public static void main(String[] args) throws Exception {
        int cnt = 100000000;
        //testAddCPU(cnt);
        //testAddOpenCL(cnt);
        //testByteReader(cnt);
        testWordWriter(cnt);        
        //testStructWriting(cnt);        
    }
}

