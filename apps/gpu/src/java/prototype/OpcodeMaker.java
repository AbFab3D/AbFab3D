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
   makes Opcode buffer and executes on GPU 
 */
public class OpcodeMaker {
    
    static void testMakeOpcode(int elementCount) throws Exception{
        
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
        
        CLKernel kernel = program.createCLKernel("OpcodeReader");
        out.printf("StructReader kernel: %s\n", kernel);
        int bufferSize = 2000;
        CLBuffer<IntBuffer> clBufferData = context.createIntBuffer(bufferSize, READ_ONLY);
        CLBuffer<IntBuffer> clBufferResult = context.createIntBuffer(bufferSize, WRITE_ONLY);
        CLBuffer<IntBuffer> clBufferResult2 = context.createIntBuffer(bufferSize, WRITE_ONLY);
        
        makeOpcodeBuffer(clBufferData.getBuffer());
        printOpcodeBuffer(clBufferData.getBuffer());

        //if(true) return;

        kernel.putArg(clBufferData);
        kernel.putArg(bufferSize);
        kernel.putArg(clBufferResult);
        kernel.putArg(bufferSize);
        kernel.putArg(clBufferResult2);

        CLEventList list = new CLEventList(4);
        queue.putWriteBuffer(clBufferData, true,list);
        queue.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize, list);
        queue.putReadBuffer(clBufferResult, true, list);
        queue.putReadBuffer(clBufferResult2, true, list);
        
        printOpcodeBuffer(clBufferResult.getBuffer());
        printResultBuffer(clBufferResult2.getBuffer(), 10);

        context.release();
    }

    static void writeToIntBuffer(IntBuffer buffer, int x){    
        buffer.put(x);
    } 

    static void writeToIntBuffer(IntBuffer buffer, int array[], int count){    
        for(int i = 0; i < count; i++){
            buffer.put(array[i]);
        }
    } 

    static void printOpcodeBuffer(IntBuffer buffer){

        out.printf("printStructBuffer(%s)\n", buffer);
        int work[] = new int[1000];

        while(true){
            
            int size = buffer.get();
            int opcode = buffer.get();
            if(size == 0) 
                break;
            out.printf("size: %4d opcode: %4d ", size, opcode);
            switch(opcode){
            default: 
                out.printf("Unknown opcode\n", opcode); break;
            case Opcodes.oSPHERE:
                out.printf("CSphere\n"); break;                
            case Opcodes.oGYROID:
                out.printf("CGyroid\n"); break;                
            }
            for(int i = 2; i < size; i++){
                int ri = (buffer.get());
                //float rf = Float.intBitsToFloat(ri); 
                //out.printf("%8x(%6.2f) ", ri,rf);        
            }
            //out.printf("\n");
        }    
        out.printf("printStructBuffer() DONE\n");
        buffer.rewind();
    }

    static void printResultBuffer(IntBuffer buffer, int count){

        out.printf("printResultsBuffer(%s)\n", buffer);

        int cnt = 0;

        while(cnt++ < count){            
            int ri = buffer.get();
            float rf = Float.intBitsToFloat(ri); 
            out.printf("0x%8X (%6.2f)\n", ri,rf);                    
        }    
        out.printf("printResultsBuffer() DONE\n");
        buffer.rewind();
    }

    static void makeOpcodeBuffer(IntBuffer buffer){

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

    private static int roundUp(int denom, int value) {        
        return denom*((value + denom - 1)/ denom);
    }

    
    public static void main(String[] args) throws Exception {
        testMakeOpcode(10000);        
    }
}
