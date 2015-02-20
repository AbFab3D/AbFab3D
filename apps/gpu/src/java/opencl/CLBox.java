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
import javax.vecmath.Vector3d;

import static opencl.CLUtils.floatToInt;

import static abfab3d.util.Units.MM;
import static abfab3d.util.Output.printf;

/**
   CL code generatror for box
   @author Vladimir Bulatov 
 */
public class CLBox  extends CLNodeBase {

    static final boolean DEBUG = true;
    static int OPCODE = Opcodes.oBOX;
    static int STRUCTSIZE = 12;
    
    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer) {

        int wcount =  super.getTransformCLCode(node,codeBuffer);

        Vector3d center = ((Vector3dParameter)node.getParam("center")).getValue();
        Vector3d size = ((Vector3dParameter)node.getParam("size")).getValue();
        double rounding = ((DoubleParameter)node.getParam("rounding")).getValue();

        if(DEBUG)printf("box([%7.5f,%7.5f,%7.5f][%7.5f,%7.5f,%7.5f],%7.5f)\n", center.x, center.y, center.z, size.x, size.y, size.z, rounding);

        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        buffer[c++] = floatToInt(rounding);
        c += 1; // align to 4 words boundary 
        buffer[c++] = floatToInt(center.x);
        buffer[c++] = floatToInt(center.y);
        buffer[c++] = floatToInt(center.z);
        buffer[c++] = 0; // alignment
        buffer[c++] = floatToInt(size.x/2.);
        buffer[c++] = floatToInt(size.y/2.);
        buffer[c++] = floatToInt(size.z/2.);
        buffer[c++] = 0;// alignment

        codeBuffer.add(buffer, STRUCTSIZE);
        wcount += STRUCTSIZE;

        wcount +=  super.getMaterialCLCode(node,codeBuffer);
       
        return wcount;
               
    }
}