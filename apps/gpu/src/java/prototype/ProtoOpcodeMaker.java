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
import abfab3d.util.DataSource;
import abfab3d.grid.Bounds;

import opencl.CLCodeMaker;
import opencl.CLCodeBuffer;

import shapejs.Shape;


import abfab3d.util.Output;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
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
    
    static final int DEVICE_INDEX = 0;


    int testMakeOpcode(int devIndex) throws Exception{
        
        printf("testMakeOpcode()\n");
        //CLDevice device = CLPlatform.getDefault(type(GPU)).getMaxFlopsDevice();
        CLDevice devs[] = CLPlatform.getDefault(type(GPU)).listCLDevices();
        
        CLDevice dev = devs[devIndex];
        printf("dev:%s\n", dev.getName());
        printf("  MaxMemAllocSize:%5d MB\n", dev.getMaxMemAllocSize()/(1<<20));
        printf("  GlobalMemSize:%5d MB\n", dev.getGlobalMemSize()/(1<<20));
        printf("  LocalMemSize:%5d KB\n", dev.getLocalMemSize()/(1<<10));
        printf("  MaxConstantBufferSize:%5d KB\n", dev.getMaxConstantBufferSize()/(1<<10));
        printf("  GlobalMemCachelineSize:%5d B\n", dev.getGlobalMemCachelineSize());
        printf("  GlobalMemCacheSize:%5d KB\n", dev.getGlobalMemCacheSize()>>10);
        printf("  MaxParamSize: %d\n",dev.getMaxParameterSize());
        printf("  MaxConstantArgs: %d\n",dev.getMaxConstantArgs());
        printf("  AddressBits: %d\n",dev.getAddressBits());
        printf("  PreferredCharVectorWidth: %d\n",dev.getPreferredCharVectorWidth());
        printf("  PreferredShortVectorWidth: %d\n",dev.getPreferredShortVectorWidth());
        printf("  PreferredIntVectorWidth: %d\n",dev.getPreferredIntVectorWidth());
        printf("  MaxComputeUnits: %d\n",dev.getMaxComputeUnits());
        printf("  MaxWorkGroupSize: %d\n",dev.getMaxWorkGroupSize());
        int dd[] = dev.getMaxWorkItemSizes();
        printf("  MaxWorkItemSize: [%d x %d x %d]\n",dd[0],dd[1],dd[2]);
            

        CLContext context = CLContext.create(dev);

        CLCommandQueue queue = dev.createCommandQueue(CLCommandQueue.Mode.PROFILING_MODE);
        long t0 = time();
        CLProgram program = null;
        try {
            //program = ProgramLoader.load(context,"OpcodeReader.cl");
            program = context.createProgram("#include \"GridCalculator.cl\"\n");
            printf("program loaded in %d ms\n", (time()-t0));
        } catch(Exception e){
            printf("failed to load program\n");
            //e.printStackTrace(Output.out);
        } 
        
        String buildOpts = " -Werror -I src/opencl";
        t0 = time();
        try {
            program.build(buildOpts, dev);        
            printf("ProgramBuildStatus: %s \n",program.getBuildStatus());
            printf("program build time: %d ms\n",(time() - t0));
        } catch(Exception e){
            printf("failde to compile program");
            //e.printStackTrace(Output.out);
        } 
        if (!program.isExecutable()) {
            printf("program did'n not compile\n");
            printf("---\n%s---\n",program.getSource());
            printf("%s\n",program.getBuildLog());
            return -1;
        }
        String kernelName = "GridCalculator";
        CLKernel kernel = null;
        try {
            kernel = program.createCLKernel(kernelName);
            out.printf("kernel: %s\n", kernel);
        } catch(Exception e){
            printf("failed to create kernel \"%s\"\n",kernelName);
            return -1;
        }

        Shape shape = makeTransformedSphere();

        Bounds bounds = shape.getBounds();
        double voxelSize = shape.getVoxelSize();
       
        int wgSizeX = 16; // workgroup size
        int wgSizeY = 16;

        int gridSizeX = (int)Math.ceil((bounds.xmax - bounds.xmin)/voxelSize);
        int gridSizeY = (int)Math.ceil((bounds.ymax - bounds.ymin)/voxelSize);
        int gridSizeZ = (int)Math.ceil((bounds.zmax - bounds.zmin)/voxelSize);


        int blockSizeX = min(gridSizeX,4*wgSizeX);
        int blockSizeY = min(gridSizeY, 4*wgSizeY);
        int blockSizeZ = min(gridSizeZ,max(blockSizeY, blockSizeX));
        int blockSize = blockSizeX*blockSizeY*blockSizeZ;
        
        int offsetX = 0; // block offset  in the whole grid 
        int offsetY = 0;
        int offsetZ = 0;

        
        int workSizeX = roundUp(blockSizeX, wgSizeX);
        int workSizeY = roundUp(blockSizeY, wgSizeY);
        
        
        printf("grid: [%d x %d x %d]\n", gridSizeX, gridSizeY,gridSizeZ);
        printf("block: [%d x %d x %d]\n", blockSizeX, blockSizeY, blockSizeZ);
        printf("workgroup: [%d x %d]\n", wgSizeX,wgSizeY);
        printf("xmin: [%5.2f, %5.2f, %5.2f ]\n", bounds.xmin, bounds.ymin, bounds.zmin );
        
        CLCodeBuffer code = makeOpcode(shape);
        
        int opCount = code.opcodesCount();
        int opSize = code.opcodesSize();        
        int dataSize = code.dataSize();
        printf("opcode count:%d\n", opCount);
        printf("opcode size:%d\n", opSize);
        printf("dataSize:%d\n", dataSize);
        printf(":code:\n%s\n:code end:",CLCodeMaker.createText(code));
        

        CLBuffer<IntBuffer> clOpcodeBuffer = context.createIntBuffer(opSize, READ_ONLY);
        clOpcodeBuffer.getBuffer().put(code.getOpcodesData());
        clOpcodeBuffer.getBuffer().rewind();
        
        CLBuffer<IntBuffer> clDataBuffer = context.createIntBuffer(dataSize, READ_ONLY);
        CLBuffer<FloatBuffer> clResultBuffer = context.createFloatBuffer(blockSize, WRITE_ONLY);

        /*
kernel void GridCalculator(
                           float voxelSize, 
                           float gridXmin, // grid origin
                           float gridYmin, 
                           float gridZmin, 
                           int offsetX,  // grid block origin 
                           int offsetY,
                           int offsetZ,
                           int sizeX,    // size of grid block  
                           int sizeY,
                           int sizeZ,
                           
                           global const int * pgOps, // operations 
                           int opCount, // operations count 
                           int opBufferSize, // operations buffer size 
                           local int *plOps, // ops in local memory 
                           global const char *pgData, // large global data
                           global float *outGrid // output grid data 
                           ) {
        */
        kernel.putArg((float)voxelSize);
        kernel.putArg((float)bounds.xmin);
        kernel.putArg((float)bounds.ymin);
        kernel.putArg((float)bounds.zmin);
        kernel.putArg(offsetX);
        kernel.putArg(offsetY);
        kernel.putArg(offsetZ);
        kernel.putArg(blockSizeX);
        kernel.putArg(blockSizeY);
        kernel.putArg(blockSizeZ);
        kernel.putArg(clOpcodeBuffer);
        kernel.putArg(opCount);
        kernel.putArg(opSize);
        kernel.putNullArg(opSize*4); // allocate buffer for data in workgroup local memory 
        kernel.putArg(clDataBuffer);
        kernel.putArg(clResultBuffer);

        printf("kernels arg done\n");
        t0 = time();
        CLEventList list = new CLEventList(4);
        queue.putWriteBuffer(clOpcodeBuffer, true,list);
        queue.put2DRangeKernel(kernel, 0, 0, workSizeX, workSizeY,wgSizeX, wgSizeY, list);
        queue.putReadBuffer(clResultBuffer, true, list);

        printf("queue done %d ms\n",(time() - t0));
        //if(true) return 0;
        
        //printOpcodeBuffer(clBufferResult.getBuffer());
        // last item in results is register data1 
        printResultBuffer(clResultBuffer.getBuffer(), blockSizeX, blockSizeY, blockSizeZ);


        context.release();
        return 0;
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



    static void printResultBuffer(FloatBuffer buffer, int nx, int ny, int nz){
        
    
        out.printf("printResultsBuffer(%s, (%d x %d x %d)\n", buffer, nx, ny, nz);

        //if(true)return;
        for(int z = 0; z < nz; z++) {
            printf("========== z = %d\n", z);
            for(int y = 0; y < ny; y++) {
                for(int x = 0; x < nx; x++) {
                    int index = z + nz*(x + ny*y);
                    float d = buffer.get(index);
                    printf("%5.2f ", d);
                }
                printf("\n");
            }
        }
        printf("==========\n");
    }

    static CLCodeBuffer makeOpcode(Shape shape){
                
        CLCodeBuffer code = new CLCodeBuffer(1000);
        CLCodeMaker codeMaker = new CLCodeMaker();        
        
        Parameterizable node = (Parameterizable)shape.getDataSource();
        if(node instanceof Initializable) 
            ((Initializable)node).initialize();

        codeMaker.getCLCode(node, code);
        
        return code;

    }

    static Shape makeTransformedSphere(){
 
        Sphere s = new Sphere(0,0,0,1.);
        s.setTransform(new Translation(0,0,0));            
        double t = 1;
        return new Shape(s, new Bounds(-t,t,-t,t,-t,t), 0.1);

    }

    private static int roundUp(int value, int denom) {        
        return denom*((value + denom - 1)/ denom);
    }

    
    public static void main(String[] args) throws Exception {
        
        int result = new ProtoOpcodeMaker().testMakeOpcode(1); 
        printf("result: %d\n", result);
    }
}
