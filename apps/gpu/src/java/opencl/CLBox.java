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

import abfab3d.param.BaseParameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Vector3dParameter;
import javax.vecmath.Vector3d;

import static prototype.CLUtils.floatToInt;

import static abfab3d.util.Units.MM;

public class CLBox  implements CLCodeGenerator {

    static int OPCODE = Opcodes.oBOX;
    static int STRUCTSIZE = 12;
    
    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(BaseParameterizable node, CLCodeBuffer codeBuffer) {
        
        Vector3d center = ((Vector3dParameter)node.getParam("center")).getValue();
        Vector3d size = ((Vector3dParameter)node.getParam("size")).getValue();
        double rounding = ((DoubleParameter)node.getParam("rounding")).getValue();

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
        return STRUCTSIZE;
               
    }
}