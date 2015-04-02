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
package opencl;

import abfab3d.param.Parameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.param.IntParameter;

import abfab3d.datasources.Noise;

import javax.vecmath.Vector3d;

import static opencl.CLUtils.floatToInt;

import static abfab3d.util.Units.MM;
import static abfab3d.util.Output.printf;

/**
   CL code generatror for ImageMap
   @author Vladimir Bulatov 
 */
public class CLNoise3D  extends CLNodeBase {

    static final boolean DEBUG = true;
    static int OPCODE = Opcodes.oNOISE3D;
    /*

typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode to perform 
    // custom parameters of DataSource 
    float offset;
    float factor;
    float3 scale; // scale to transform to grid coords 
    int3 dimension; // grid dimensions
    int gradOffset; // offset to gradient data in bytes
} sNoise3D;
    */

    static int STRUCTSIZE = 16;
    
    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer) {

        int wcount =  super.getTransformCLCode(node,codeBuffer);

        Noise noise = (Noise)node;
        
        Vector3d size = (Vector3d)node.get("size");
        
        int nx = (Integer)node.get("nx");
        int ny = (Integer)node.get("ny");
        int nz = (Integer)node.get("nz");
        double vOffset = (Double)node.get("offset");
        double vFactor = (Double)node.get("factor");

        if(DEBUG)printf("Noise3D([%7.5f,%7.5f,%7.5f][%d,%d,%d])\n", size.x, size.y, size.z, nx, ny, nz);

        double grads[] = noise.getGradients();

        byte data[] = new byte[grads.length];

        for(int i = 0; i < grads.length; i++){

            int b = (byte)(grads[i]*127);
            data[i] =   (byte)(b & 0xFF);
            if(DEBUG){
                printf("%3d ", data[i]);
                if((i+1)%3 == 0)printf("\n");
            }
            //data[i] =   0;
            //int g = Float.floatToIntBits((float)grads[i]);
            //data[4*i] =   (byte)(g & 0xFF);
            //data[4*i+1] = (byte)((g >> 8) & 0xFF);
            //data[4*i+2] = (byte)((g >> 16) & 0xFF);
            //data[4*i+3] = (byte)((g >> 24) & 0xFF);
        }
       
        int dataOffset = codeBuffer.addData(data);


        double scaleX = nx/size.x;
        double scaleY = ny/size.y;
        double scaleZ = nz/size.z;

        if(DEBUG)printf("scale: %7.3f %7.3f %7.3f\n", scaleX,scaleY,scaleZ);
        if(DEBUG)printf("offset: %7.3f factor: %7.3f\n", vOffset,vFactor);
        if(DEBUG)printf("dataOffset: %d\n", dataOffset);

        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        buffer[c++] = floatToInt(vOffset); // vOffset 
        buffer[c++] = floatToInt(vFactor); // vFactor

        buffer[c++] = floatToInt(scaleX);
        buffer[c++] = floatToInt(scaleY);
        buffer[c++] = floatToInt(scaleZ);
        buffer[c++] = 0;// alignment

        buffer[c++] = nx;
        buffer[c++] = ny;
        buffer[c++] = nz;
        buffer[c++] = 0;// alignment        
        buffer[c++] = dataOffset; // data offset 

        codeBuffer.add(buffer, STRUCTSIZE);
        wcount += STRUCTSIZE;

        wcount +=  super.getMaterialCLCode(node,codeBuffer);
       
        return wcount;
               
    }
}