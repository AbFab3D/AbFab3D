package prototype;

import javax.vecmath.Vector3d;


public class CTorus {

    static int OPCODE = Opcodes.oTORUS;
    static int STRUCTSIZE = 8;

    double r;
    double R;
    Vector3d center;
    
    public CTorus(Vector3d center, double R, double r){
        this.R = R;
        this.r = r;
        this.center = center;
    }
    
    public void getStruct(int buffer[]){

        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        buffer[c++] = Float.floatToIntBits((float)r);        
        buffer[c++] = Float.floatToIntBits((float)R);        
        buffer[c++] = Float.floatToIntBits((float)(center.x));
        buffer[c++] = Float.floatToIntBits((float)(center.y));
        buffer[c++] = Float.floatToIntBits((float)(center.z));
        buffer[c++] = Float.floatToIntBits(0.f);
               
    }
}