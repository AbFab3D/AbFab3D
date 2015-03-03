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

import abfab3d.grid.Bounds;
import abfab3d.param.Parameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.param.IntParameter;

import abfab3d.datasources.DataSourceGrid;

import javax.vecmath.Vector3d;

import static opencl.CLUtils.floatToInt;

import static abfab3d.util.Units.MM;
import static abfab3d.util.Output.printf;

/**
   CL code generatror for box
   @author Vladimir Bulatov 
 */
public class CLGrid  extends CLNodeBase {

    static final boolean DEBUG = true;
    static int OPCODE = Opcodes.oGRID2DBYTE;
    /*
typedef struct {
    int size;  // size of struct in words 
    int opcode; // opcode 
    // custom parameters
    // coefficients to calculate data value
    float vOffset; // value = byteValue*vFactor + vOffset;
    float vFactor; 

    int dimx; // grid count in x and y directions 
    int dimy; // grid count in x and y directions 
    int dimz; // grid count in x and y directions 

    float3 center;  // center in world units
    float3 halfsize; // size in world units

    float3 origin; // origoin of the grid 
    float vscale; // 1/voxelSize 

    int data; // location of data in the data buffer 
    PTRDATA char *pData; // actual grid data 
} sGrid2dByte;
    */

    static int STRUCTSIZE = 24;
    
    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer) {

        int wcount =  super.getTransformCLCode(node,codeBuffer);

        DataSourceGrid grid = (DataSourceGrid)node;
        
        Bounds bounds = grid.getGridBounds();
        int nx = grid.getGridWidth();
        int ny = grid.getGridHeight();
        int nz = grid.getGridDepth();
        
        //image.getBitmapData(data);

        //int offset = codeBuffer.addData(data);
        /*
        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        buffer[c++] = floatToInt(-1.); // vOffset 
        buffer[c++] = floatToInt(-1./255.); // vFactor
        buffer[c++] = nx; // dim[0]
        buffer[c++] = ny; // dim[1]
        buffer[c++] = nz; // dim[1]

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
        buffer[c++] = offset; // data offset 
        buffer[c++] = 0; // place for data pointer

        codeBuffer.add(buffer, STRUCTSIZE);
        wcount += STRUCTSIZE;

        wcount +=  super.getMaterialCLCode(node,codeBuffer);
        */
        return wcount;
               
    }
}