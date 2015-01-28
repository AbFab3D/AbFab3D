package prototype;

import com.jogamp.opencl.*;
import com.jogamp.opencl.util.CLDeviceFilters;
import program.ProgramLoader;

import javax.media.opengl.GLContext;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Random;

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

    public static void main(String[] args) throws IOException {

        // set up (uses default CLPlatform and creates context for all devices)
        //CLContext context = CLContext.create();

        // always make sure to release the context under all circumstances
        // not needed for this particular sample but recommented
//        CLDevice device = CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice(CLDeviceFilters.queueMode(CLCommandQueue.Mode.OUT_OF_ORDER_MODE));
        CLDevice device = CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice();
        CLContext context = CLContext.create(device);

        try{
/*
            CLDevice[] devices = context.getDevices();
            for(int i=0; i < devices.length; i++) {
                System.out.println("Name: " + devices[i].getName() + " compute_units: " + devices[i].getMaxComputeUnits() + " freq: " + devices[i].getMaxClockFrequency());
            }
*/
            // select fastest device
//            CLDevice device = devices[0];
            out.println("using "+device);

            // create command queue on device.
            CLCommandQueue queue = device.createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);

//            int elementCount = 1444477;                                  // Length of arrays to process
            int elementCount = (int) 2e8;                                  // Length of arrays to process
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
            fillBuffer(clBufferA.getBuffer(), 12345);
            fillBuffer(clBufferB.getBuffer(), 67890);

            // get a reference to the kernel function with the name 'VectorAdd'
            // and map the buffers to its input parameters.
            CLKernel kernel = program.createCLKernel("VectorAdd");
            kernel.putArgs(clBufferA, clBufferB, clBufferC).putArg(elementCount);

            CLEventList list = new CLEventList(4);

            // asynchronous write of data to GPU device,
            // followed by blocking read to get the computed results back.
            long time = nanoTime();
            /*
            queue.putWriteBuffer(clBufferA, false, list)
                 .putWriteBuffer(clBufferB, false, list)
                 .put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize, list)
                 .putReadBuffer(clBufferC, true, list);
            */
            final CLEventList events = new CLEventList(2);

            queue.putWriteBuffer(clBufferA, false, events, list)
                    .putWriteBuffer(clBufferB, false, events, list);
            queue.putWaitForEvents(events, true);

            queue.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize, list)
                    .putReadBuffer(clBufferC, true, list);
            time = nanoTime() - time;

            for(int i=0; i < list.size(); i++) {
                CLEvent event = list.getEvent(i);
                System.out.println("cmd: " + i + " time: " + (event.getProfilingInfo(CLEvent.ProfilingCommand.END)
                          - event.getProfilingInfo(CLEvent.ProfilingCommand.START))/1000000.0);
            }
            // print first few elements of the resulting buffer to the console.
            out.println("a+b=c results snapshot: ");
            for(int i = 0; i < 10; i++)
                out.print(clBufferC.getBuffer().get() + ", ");
            out.println("...; " + clBufferC.getBuffer().remaining() + " more");

            out.println("computation took: "+(time/1000000)+"ms");
            
        }finally{
            // cleanup all resources associated with this context.
            context.release();
        }

    }

    private static void fillBuffer(FloatBuffer buffer, int seed) {
        Random rnd = new Random(seed);
        while(buffer.remaining() != 0)
            buffer.put(rnd.nextFloat()*100);
        buffer.rewind();
    }

    private static int roundUp(int groupSize, int globalSize) {
        int r = globalSize % groupSize;
        if (r == 0) {
            return globalSize;
        } else {
            return globalSize + groupSize - r;
        }
    }

}
