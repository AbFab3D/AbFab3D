package prototype;

import javax.vecmath.Vector3d;


public class CBox {

    static int OPCODE = Opcodes.oBOX;
    static int STRUCTSIZE = 12;

    Vector3d center;
    Vector3d size;
    double rounding;
    
    public CBox(Vector3d center, Vector3d size, double rounding){

        this.center = center;
        this.size = size;
        this.rounding = rounding;
    }
    
    public void getStruct(int buffer[]){

        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        buffer[c++] = Float.floatToIntBits((float)(rounding));
        c += 1; // align to 4 words boundary 
        buffer[c++] = Float.floatToIntBits((float)(center.x));
        buffer[c++] = Float.floatToIntBits((float)(center.y));
        buffer[c++] = Float.floatToIntBits((float)(center.z));
        buffer[c++] = Float.floatToIntBits(0.f);
        buffer[c++] = Float.floatToIntBits((float)(size.x/2.));
        buffer[c++] = Float.floatToIntBits((float)(size.y/2.));
        buffer[c++] = Float.floatToIntBits((float)(size.z/2.));
        buffer[c++] = Float.floatToIntBits(0.f);
               
    }
}