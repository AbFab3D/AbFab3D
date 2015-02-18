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

/**
   CL code generator for torus
   @author Vladimir Bulatov 
 */
public class CLTorus   extends CLNodeBase {

    static int OPCODE = Opcodes.oTORUS;
    static int STRUCTSIZE = 8;
    
    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer) {

        int wcount =  super.getTransformCLCode(node,codeBuffer);

        double rin = ((DoubleParameter)node.getParam("rin")).getValue();
        double rout = ((DoubleParameter)node.getParam("rout")).getValue();
        Vector3d center = ((Vector3dParameter)node.getParam("center")).getValue();
        
        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        buffer[c++] = floatToInt(rin);        
        buffer[c++] = floatToInt(rout);        
        buffer[c++] = floatToInt(center.x);
        buffer[c++] = floatToInt(center.y);
        buffer[c++] = floatToInt(center.z);
        buffer[c++] = 0;

        codeBuffer.add(buffer, STRUCTSIZE);

        wcount += STRUCTSIZE;
       
        wcount +=  super.getMaterialCLCode(node,codeBuffer);
       
        return wcount;
     }

}