package opencl;

import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameterizable;
import abfab3d.param.Vector3dParameter;

import javax.vecmath.Vector3d;

import static java.lang.Math.PI;
import static opencl.CLUtils.floatToInt;


/**
   CL code generatror for SchwatzD minimal surface 
   @author Vladimir Bulatov 
 */
public class CLLidinoid extends CLNodeBase {

    static int OPCODE = Opcodes.oLIDINOID;

    static int STRUCTSIZE = 8;
    
    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer) {

        int wcount =  super.getTransformCLCode(node,codeBuffer);

        double level = (Double)node.get("level");
        double thickness = (Double)node.get("thickness");
        double period = (Double)node.get("period");

        double factor = 2 * PI / period;

        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        buffer[c++] = floatToInt(level);
        buffer[c++] = floatToInt(thickness);
        buffer[c++] = floatToInt(factor);

        codeBuffer.add(buffer, STRUCTSIZE);

        wcount += STRUCTSIZE;
       
        wcount +=  super.getMaterialCLCode(node,codeBuffer);
       
        return wcount;
    }
    
}