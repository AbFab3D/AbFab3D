package opencl;

import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameterizable;
import abfab3d.param.Vector3dParameter;

import javax.vecmath.Vector3d;

import static java.lang.Math.PI;
import static opencl.CLUtils.floatToInt;


public class CLGyroid implements CLCodeGenerator {

    static int OPCODE = Opcodes.oGYROID;

    static int STRUCTSIZE = 12;
    
    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer) {

        DoubleParameter plevel = (DoubleParameter)node.getParam("level");
        double level = plevel.getValue();
        
        DoubleParameter pthickness = (DoubleParameter)node.getParam("thickness");
        double thickness = pthickness.getValue();

        DoubleParameter pperiod = (DoubleParameter)node.getParam("period");
        double period = pperiod.getValue();

        Vector3d offset = ((Vector3dParameter)node.getParam("offset")).getValue();

        double factor = 2 * PI / period;

        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        buffer[c++] = floatToInt(level);
        buffer[c++] = floatToInt(thickness);
        buffer[c++] = floatToInt(factor);
        c += 3; // align to 4 words boundary
        buffer[c++] = floatToInt(offset.x);
        buffer[c++] = floatToInt(offset.y);
        buffer[c++] = floatToInt(offset.z);
        buffer[c++] = 0;

        codeBuffer.add(buffer, STRUCTSIZE);
        return STRUCTSIZE;
    }
    
}