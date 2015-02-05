package prototype;

import javax.vecmath.Vector3d;


public class CSphere {

    static int OPCODE = Opcodes.oSPHERE;
    static int STRUCTSIZE = 8;

    double radius;
    Vector3d center;
    
    public CSphere(double radius, Vector3d center){
        this.radius = radius;
        this.center = center;
    }
    
    public void getStruct(int buffer[]){

        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        buffer[c++] = Float.floatToIntBits((float)radius);        
        c++; // align to 4 words boundary 
        buffer[c++] = Float.floatToIntBits((float)(center.x));
        buffer[c++] = Float.floatToIntBits((float)(center.y));
        buffer[c++] = Float.floatToIntBits((float)(center.z));
        buffer[c++] = Float.floatToIntBits(0.f);
               
    }
}