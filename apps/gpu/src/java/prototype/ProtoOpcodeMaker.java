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

import abfab3d.param.Parameterizable;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Box;
import abfab3d.datasources.Union;
import abfab3d.datasources.Intersection;
import abfab3d.transforms.Translation;

import abfab3d.param.DoubleParameter;
import abfab3d.util.Initializable;

import opencl.CLCodeMaker;
import opencl.CLSphere;
import opencl.CLBox;
import opencl.CLCodeBuffer;



import static abfab3d.util.Output.printf;
import static com.jogamp.opencl.CLDevice.Type.GPU;
import static com.jogamp.opencl.CLDevice.Type.CPU;
import static com.jogamp.opencl.util.CLPlatformFilters.type;
import static java.lang.System.*;
import static com.jogamp.opencl.CLMemory.Mem.*;
import static java.lang.Math.*;

import opencl.CLUtils;

/**
   makes Opcode buffer and executes on GPU 
 */
public class ProtoOpcodeMaker {
    
    static void testMakeOpcode(int elementCount) throws Exception{
        
        printf("testMakeOpcode()\n");
        CLDevice device = CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice();
        CLContext context = CLContext.create(device);
        int localWorkSize = min(device.getMaxWorkGroupSize(), 256);  // Local work size dimensions
        int globalWorkSize = roundUp(localWorkSize, elementCount);   // rounded up to the nearest multiple of the localWorkSize        
        CLCommandQueue queue = device.createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);
        CLProgram program = ProgramLoader.load(context,"OpcodeReader.cl");
        String buildOpts = " -Werror -I classes";
        program.build(buildOpts);        
        printf("ProgramBuildStatus: %s\n",program.getBuildStatus());
        
        if (!program.isExecutable()) {
            printf("log: %s\n",program.getBuildLog());
            throw new IllegalArgumentException("Program didn't compile");
        }
        
        CLKernel kernel = program.createCLKernel("opcodeReader");
        out.printf("StructReader kernel: %s\n", kernel);
        int bufferSize = 2000;
        CLBuffer<IntBuffer> clOpcodeBuffer = context.createIntBuffer(bufferSize, READ_ONLY);
        CLBuffer<IntBuffer> clBufferResult = context.createIntBuffer(bufferSize, WRITE_ONLY);
        CLBuffer<IntBuffer> clBufferResult2 = context.createIntBuffer(bufferSize, WRITE_ONLY);
        
        int opCount = makeOpcodeBuffer(clOpcodeBuffer.getBuffer());

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
        
        //printOpcodeBuffer(clBufferResult.getBuffer());
        // last item in results is register data1 
        printResultBuffer(clBufferResult2.getBuffer(), (opCount+1)*4);

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

    static void writeToIntBuffer(IntBuffer buffer, CLCodeBuffer code){    
        for(int i = 0; i < code.opcodesSize(); i++)
            buffer.put(code.get(i));
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

        int opcount = 0;
        
        Parameterizable shape = makeTransformedSphere();
        
        CLCodeBuffer code = new CLCodeBuffer(1000);
        CLCodeMaker codeMaker = new CLCodeMaker();
        
        
        if(shape instanceof Initializable) 
            ((Initializable)shape).initialize();

        codeMaker.getCLCode(shape, code);
        
        printf("opcount: %d\n:code:\n%s:end:\n", code.opcodesCount(), codeMaker.createText(code));

        writeToIntBuffer(buffer, code);

        buffer.rewind();

        return code.opcodesCount();

    }

    static Parameterizable makeTransformedSphere(){
 
        Sphere s = new Sphere(0,0,0,1.);
        s.setTransform(new Translation(1.,1,0));
        return s;

    }

    private static int roundUp(int denom, int value) {        
        return denom*((value + denom - 1)/ denom);
    }

    
    public static void main(String[] args) throws Exception {
        testMakeOpcode(10000);        
    }
}
