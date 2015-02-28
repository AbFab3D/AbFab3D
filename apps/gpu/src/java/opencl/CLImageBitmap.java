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

import abfab3d.datasources.ImageBitmap;

import javax.vecmath.Vector3d;

import static opencl.CLUtils.floatToInt;

import static abfab3d.util.Units.MM;
import static abfab3d.util.Output.printf;

/**
   CL code generatror for box
   @author Vladimir Bulatov 
 */
public class CLImageBitmap  extends CLNodeBase {

    static final boolean DEBUG = true;
    static int OPCODE = Opcodes.oGRID2DBYTE;
    /*
    typedef struct {
        int size;  // size of struct in words 
        int opcode; // opcode 
        // custom parameters
        // coefficients to calculate data value        
        float vOffset; // value = byteValue*vFactor + vOffset;
        float vFactor;                                 // 4
        float rounding; // edges rounding              // 1   
        int tiling;                                    // 1
        int2 dim; // grid count in x and y directions  // 2
        float3 center;  // center in world units       // 4
        float3 halfsize; // size in world units        // 4
        int data; // location of data in the data buffer //  1
        PTRDATA char *pData; // actual grid data         //  1
   } sGrid2dByte;
    */

    static int STRUCTSIZE = 20;
    
    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer) {

        int wcount =  super.getTransformCLCode(node,codeBuffer);

        ImageBitmap image = (ImageBitmap)node;
        
        Vector3d center = ((Vector3dParameter)node.getParam("center")).getValue();
        Vector3d size = ((Vector3dParameter)node.getParam("size")).getValue();
        double rounding = ((DoubleParameter)node.getParam("rounding")).getValue();
        int tilesX = ((IntParameter)node.getParam("tilesX")).getValue();
        int tilesY = ((IntParameter)node.getParam("tilesY")).getValue();
        int tiling = ((tilesX & 0xFFFF)| (tilesY & 0xFFFF) << 16);

        if(DEBUG)printf("box([%7.5f,%7.5f,%7.5f][%7.5f,%7.5f,%7.5f],%7.5f)\n", center.x, center.y, center.z, size.x, size.y, size.z, rounding);
        int nx = image.getBitmapWidth();
        int ny = image.getBitmapHeight();
        byte[] data = new byte[nx*ny];
        
        image.getBitmapData(data);

        int offset = codeBuffer.addData(data);

        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        buffer[c++] = floatToInt(0.); // vOffset 
        buffer[c++] = floatToInt(1.); // vFactor
        buffer[c++] = floatToInt(rounding); // roundng
        buffer[c++] = tiling;
        buffer[c++] = 0; // dim[0]
        buffer[c++] = 0; // dim[1]
        buffer[c++] = floatToInt(center.x);
        buffer[c++] = floatToInt(center.y);
        buffer[c++] = floatToInt(center.z);
        buffer[c++] = 0; // alignment
        buffer[c++] = floatToInt(size.x/2.);
        buffer[c++] = floatToInt(size.y/2.);
        buffer[c++] = floatToInt(size.z/2.);
        buffer[c++] = 0;// alignment
        buffer[c++] = offset; // data offset 
        buffer[c++] = 0; // place for data pointer

        codeBuffer.add(buffer, STRUCTSIZE);
        wcount += STRUCTSIZE;

        wcount +=  super.getMaterialCLCode(node,codeBuffer);
       
        return wcount;
               
    }
}