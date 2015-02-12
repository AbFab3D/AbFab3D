package opencl;

import javax.vecmath.Vector3d;


public class CGyroid {

    static int OPCODE = Opcodes.oGYROID;

    static int STRUCTSIZE = 12;
    
    double level;
    double thickness;
    double period;

    Vector3d offset;
    
    public CGyroid(double level, double thickness, double period, Vector3d offset){
        this.level = level;
        this.thickness = thickness;
        this.period = period;
        this.offset = offset;
    }
    
    public void getStruct(int buffer[]){

        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        buffer[c++] = Float.floatToIntBits((float)(level));
        buffer[c++] = Float.floatToIntBits((float)(period));
        buffer[c++] = Float.floatToIntBits((float)(thickness));
        c +=3; // align to 4 words boundary 
        buffer[c++] = Float.floatToIntBits((float)(offset.x));
        buffer[c++] = Float.floatToIntBits((float)(offset.y));
        buffer[c++] = Float.floatToIntBits((float)(offset.z));
        buffer[c++] = Float.floatToIntBits(0.f);               
    }
}