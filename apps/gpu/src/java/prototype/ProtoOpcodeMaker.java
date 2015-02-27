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
        
        CLDevice device = CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice();
        CLContext context = CLContext.create(device);
        int localWorkSize = min(device.getMaxWorkGroupSize(), 256);  // Local work size dimensions
        int globalWorkSize = roundUp(localWorkSize, elementCount);   // rounded up to the nearest multiple of the localWorkSize        
        CLCommandQueue queue = device.createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);
        CLProgram program = ProgramLoader.load(context,"OpcodeReader.cl");
        String buildOpts = " -Werror";
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
        printf("opCount: %d\n",opCount);
        CLUtils.printOpcodeBuffer(clOpcodeBuffer.getBuffer());

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
        printResultBuffer(clBufferResult2.getBuffer(), opCount+1);

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

        int workBuffer[] = new int[1000];
        int opcount = 0;
        Union union1 = new Union();
        ((DoubleParameter)union1.getParam("blend")).setValue(0.);
        union1.add(new Sphere(new Vector3d(1.03, 0., 0.), 1));
        union1.add(new Sphere(new Vector3d(-1.02, 0., 0.),1));

        Intersection inter1 = new Intersection();
        ((DoubleParameter)inter1.getParam("blend")).setValue(0.);
        inter1.add(new Sphere(new Vector3d(0.95, 0., 0.), 1));
        inter1.add(new Sphere(new Vector3d(-0.96, 0., 0.),1));

        Union union2 = new Union(inter1, union1);

        
        CLCodeBuffer code = new CLCodeBuffer(1000);
        CLCodeMaker codeMaker = new CLCodeMaker();
        
        Parameterizable shape = union2;

        if(shape instanceof Initializable) 
            ((Initializable)shape).initialize();

        codeMaker.getCLCode(shape, code);


        //Sphere s1 = new Sphere(new Vector3d(0., 0., 0.), 1.);
        //Sphere s2 = new Sphere(new Vector3d(0.99, 0., 0.),1.);
        //Sphere s3 = new Sphere(new Vector3d(2., 0., 0.),1.);               
        //CGyroid g1 = new CGyroid(1.1, 2.2, 3.3, new Vector3d(4.11, 4.12, 4.13));
        //Box b1 = new Box(new Vector3d(0,0,0), new Vector3d(2.,4.,1.), 0.1);
        //Box b2 = new Box(new Vector3d(1,0,0), new Vector3d(2.,4.,1.), 0.1);
        //Box b3 = new Box(new Vector3d(1,2,0.5), new Vector3d(2.,4.,1.), 0.05);
        //CTorus t1 = new CTorus(new Vector3d(0,0,0), 1., 2);
        //CTorus t2 = new CTorus(new Vector3d(0,0,1), 1., 2);
        //CTorus t3 = new CTorus(new Vector3d(0,0,2), 1., 2);
        
        writeToIntBuffer(buffer, code);

        buffer.rewind();

        return code.opcodesCount();
    }

    private static int roundUp(int denom, int value) {        
        return denom*((value + denom - 1)/ denom);
    }

    
    public static void main(String[] args) throws Exception {
        testMakeOpcode(10000);        
    }
}
