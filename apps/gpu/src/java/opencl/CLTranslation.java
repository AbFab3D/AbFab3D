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
public class CLTranslation implements CLCodeGenerator {

    static final boolean DEBUG = false;

    static int OPCODE = Opcodes.oTRANSLATION;
    static int STRUCTSIZE = 8;
    

    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer) {
        
        if(DEBUG)printf("CLTranslation(%s)\n", node);
        Vector3d trans = ((Vector3dParameter)node.getParam("translation")).getValue();

        if(DEBUG)printf("trans(%7.5f,%7.5f,%7.5f)\n", trans.x,trans.y,trans.z);
        
        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        c += 2;  // align to 4 words boundary 
        buffer[c++] = floatToInt(trans.x);
        buffer[c++] = floatToInt(trans.y);
        buffer[c++] = floatToInt(trans.z);
        buffer[c++] = 0;

        codeBuffer.add(buffer, STRUCTSIZE);
        return STRUCTSIZE;
    }
}