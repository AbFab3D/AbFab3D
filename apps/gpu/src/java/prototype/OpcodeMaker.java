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
        CLBuffer<IntBuffer> clOpcodeBuffer = context.createIntBuffer(bufferSize, READ_ONLY);
        CLBuffer<IntBuffer> clBufferResult = context.createIntBuffer(bufferSize, WRITE_ONLY);
        CLBuffer<IntBuffer> clBufferResult2 = context.createIntBuffer(bufferSize, WRITE_ONLY);
        
        int opCount = makeOpcodeBuffer(clOpcodeBuffer.getBuffer());
        printOpcodeBuffer(clOpcodeBuffer.getBuffer());

        //if(true) return;

        kernel.putArg(clOpcodeBuffer);
        kernel.putArg(opCount);
        kernel.putArg(clBufferResult);
        kernel.putArg(bufferSize);
        kernel.putArg(clBufferResult2);

        CLEventList list = new CLEventList(4);
        queue.putWriteBuffer(clOpcodeBuffer, true,list);
        queue.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize, list);
        queue.putReadBuffer(clBufferResult, true, list);
        queue.putReadBuffer(clBufferResult2, true, list);
        
        printOpcodeBuffer(clBufferResult.getBuffer());
        printResultBuffer(clBufferResult2.getBuffer(), opCount);

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
            case Opcodes.oBOX:
                out.printf("CBox\n"); break;                
            case Opcodes.oTORUS:
                out.printf("CTorus\n"); break;                
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

    static int makeOpcodeBuffer(IntBuffer buffer){

        int workBuffer[] = new int[1000];
        int opcount = 0;
        CSphere s1 = new CSphere(1.0, new Vector3d(0., 0., 0.));
        CSphere s2 = new CSphere(1.0, new Vector3d(0.99, 0., 0.));
        CSphere s3 = new CSphere(1.0, new Vector3d(2., 0., 0.));
        CGyroid g1 = new CGyroid(1.1, 2.2, 3.3, new Vector3d(4.11, 4.12, 4.13));
        CBox b1 = new CBox(new Vector3d(0,0,0), new Vector3d(2.,4.,1.), 0.1);
        CBox b2 = new CBox(new Vector3d(1,0,0), new Vector3d(2.,4.,1.), 0.1);
        CBox b3 = new CBox(new Vector3d(1,2,0.5), new Vector3d(2.,4.,1.), 0.05);
        CTorus t1 = new CTorus(new Vector3d(0,0,0), 1., 2);
        CTorus t2 = new CTorus(new Vector3d(0,0,1), 1., 2);
        CTorus t3 = new CTorus(new Vector3d(0,0,2), 1., 2);
        
        //s1.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]); opcount++;
        //s2.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]); opcount++;
        //s3.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]); opcount++;
        //b1.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]); opcount++;
        //b2.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]); opcount++;
        //b3.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]); opcount++;
        t1.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]); opcount++;
        t2.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]); opcount++;
        t3.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]); opcount++;
        //g1.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]); opcount++;
        // b1.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]); opcount++;
        //s2.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]); opcount++;
        //s3.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]); opcount++;
        //g1.getStruct(workBuffer); writeToIntBuffer(buffer,workBuffer, workBuffer[0]); opcount++;
        
        buffer.rewind();

        return opcount;
    }

    private static int roundUp(int denom, int value) {        
        return denom*((value + denom - 1)/ denom);
    }

    
    public static void main(String[] args) throws Exception {
        testMakeOpcode(10000);        
    }
}
