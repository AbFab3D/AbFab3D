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

import abfab3d.param.BaseParameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Vector3dParameter;

import static prototype.CLUtils.floatToInt;

public class CLSphere implements CLCodeGenerator {

    static int OPCODE = Opcodes.oSPHERE;
    static int STRUCTSIZE = 8;
    
    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(BaseParameterizable node, CLCodeBuffer codeBuffer) {

        DoubleParameter pradius = (DoubleParameter)node.getParam("radius");
        double radius = pradius.getValue();
        Vector3d center = ((Vector3dParameter)node.getParam("center")).getValue();
        
        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        buffer[c++] = floatToInt(radius);        
        c++; // align to 4 words boundary 
        buffer[c++] = floatToInt(center.x);
        buffer[c++] = floatToInt(center.y);
        buffer[c++] = floatToInt(center.z);
        buffer[c++] = 0;

        codeBuffer.add(buffer, STRUCTSIZE);
        return STRUCTSIZE;
    }

}