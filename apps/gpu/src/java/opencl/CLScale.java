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

import javax.vecmath.Vector3d;

import abfab3d.param.Parameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Vector3dParameter;

import static opencl.CLUtils.floatToInt;

import static abfab3d.util.Output.printf;

/**
   CL code generator for translation
   @author Vladimir Bulatov 
 */
public class CLScale implements CLCodeGenerator {

    static final boolean DEBUG = false;

    static int OPCODE = Opcodes.oSCALE;
    static int STRUCTSIZE = 12;
        
    //      typedef struct {
    //         int size;  // size of struct in words 
    //         int opcode; // opcode to perform 
    //         // custom parameters of sScale
    //         float averageFactor; 
    //         float3 factor; 
    //         float3 center; 
    //      } sScale;
    

    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer) {
        
        if(DEBUG)printf("CLTranslation(%s)\n", node);
        Vector3d center = (Vector3d)node.getParam("center").getValue();
        Vector3d scale = (Vector3d)node.getParam("scale").getValue();

        if(DEBUG)printf("CLScale(scale[%7.5f,%7.5f,%7.5f],center[%7.5f,%7.5f,%7.5f])\n", scale.x,scale.y,scale.z,center.x,center.y,center.z);
        double averageScale = Math.pow(Math.abs(scale.x*scale.y*scale.z), 1./3);

        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        buffer[c++] = floatToInt(1./averageScale);
        c += 1;  // align to 4 words boundary 
        buffer[c++] = floatToInt(1./scale.x);
        buffer[c++] = floatToInt(1./scale.y);
        buffer[c++] = floatToInt(1./scale.z);
        buffer[c++] = 0;
        buffer[c++] = floatToInt(center.x);
        buffer[c++] = floatToInt(center.y);
        buffer[c++] = floatToInt(center.z);
        buffer[c++] = 0;

        codeBuffer.add(buffer, STRUCTSIZE);
        return STRUCTSIZE;
    }
}