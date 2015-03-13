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

import abfab3d.datasources.ImageBox;

import javax.vecmath.Vector3d;

import static opencl.CLUtils.floatToInt;

import static abfab3d.util.Units.MM;
import static abfab3d.util.Output.printf;

/**
   CL code generatror for box
   @author Vladimir Bulatov 
 */
public class CLImageBox  extends CLNodeBase {

    static final boolean DEBUG = true;
    static int OPCODE = Opcodes.oIMAGEBOX;
    /*
typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode 
    // custom parameters
    // coefficients to calculate data value
    float vOffset; // value = byteValue*vFactor + vOffset;
    float vFactor; 

    float rounding; // edges rounding      
    int tiling; // (tilesx | tilesy << 16)
    int2 dim; // grid count in x and y directions 

    float3 center;  // center in world units

    float3 halfsize; // size in world units

    float3 origin; // location of bottom left corner
    float vscale; // 1/voxelSize 
    float outsideValue; 
    int data; // location of data in the data buffer 
    PTRDATA char *pData; // actual grid data 
} sGrid2dByte;
    */

    static int STRUCTSIZE = 24;
    
    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer) {

        int wcount =  super.getTransformCLCode(node,codeBuffer);

        ImageBox image = (ImageBox)node;
        
        Vector3d center = ((Vector3dParameter)node.getParam("center")).getValue();
        Vector3d size = ((Vector3dParameter)node.getParam("size")).getValue();
        double rounding = ((DoubleParameter)node.getParam("rounding")).getValue();
        int imagePlace = ((IntParameter)node.getParam("imagePlace")).getValue();
        int tilesX = ((IntParameter)node.getParam("tilesX")).getValue();
        int tilesY = ((IntParameter)node.getParam("tilesY")).getValue();

        double baseThickness = ((DoubleParameter)node.getParam("baseThickness")).getValue();

        int tiling = ((tilesX & 0xFFFF) | (tilesY & 0xFFFF) << 8) | ((imagePlace & 3) << 16);

        if(DEBUG)printf("imageBox(center:[%7.5f,%7.5f,%7.5f] size:[%7.5f,%7.5f,%7.5f], rounding: %7.5f, baseThickness: %7.3f, tiling:%dx%d)\n", 
                        center.x, center.y, center.z, size.x, size.y, size.z, rounding, baseThickness, tilesX, tilesY);

        int nx = image.getBitmapWidth();
        int ny = image.getBitmapHeight();
        //byte[] data = new byte[nx*ny];
        byte[] sdata = new byte[nx*ny*2];// data are uhort

        double outsideValue = 0.; // what it should be? 
        image.getBitmapDataUShort(sdata);
        double valueFactor = size.z*(1-baseThickness)/0xFFFF;
        double valueOffset = size.z*baseThickness;
        if(imagePlace == 2){// both sides
            valueOffset *= 0.5;
            valueFactor *= 0.5;            
        }

        int dataOffset = codeBuffer.addData(sdata);
        
        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        buffer[c++] = floatToInt(valueOffset); // valueOffset 
        buffer[c++] = floatToInt(valueFactor); // valueFactor
        buffer[c++] = floatToInt(rounding); // rounding
        buffer[c++] = tiling;
        buffer[c++] = nx; // dim[0]
        buffer[c++] = ny; // dim[1]
        buffer[c++] = floatToInt(center.x);
        buffer[c++] = floatToInt(center.y);
        buffer[c++] = floatToInt(center.z);
        buffer[c++] = 0; // alignment
        buffer[c++] = floatToInt(size.x/2.);
        buffer[c++] = floatToInt(size.y/2.);
        buffer[c++] = floatToInt(size.z/2.);
        buffer[c++] = 0;// alignment
        buffer[c++] = floatToInt(center.x-size.x/2.);
        buffer[c++] = floatToInt(center.y-size.y/2.);
        buffer[c++] = floatToInt(center.z-size.z/2.);
        buffer[c++] = 0;// alignment
        buffer[c++] = floatToInt(nx/size.x); //1./pixelSize 
        buffer[c++] = floatToInt(ny/size.y); //1./pixelSize 
        buffer[c++] = floatToInt(outsideValue); 
        buffer[c++] = dataOffset; // data offset 

        codeBuffer.add(buffer, STRUCTSIZE);
        wcount += STRUCTSIZE;

        wcount +=  super.getMaterialCLCode(node,codeBuffer);
       
        return wcount;
               
    }
}