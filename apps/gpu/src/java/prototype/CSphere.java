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
package prototype;

import javax.vecmath.Vector3d;

import static opencl.CLUtils.floatToInt;
import opencl.Opcodes;

public class CSphere  {

    static int OPCODE = Opcodes.oSPHERE;
    static int STRUCTSIZE = 8;
    Vector3d center;
    double radius;

    public CSphere(double radyus, Vector3d center){

        this.center = center;
        this.radius = radius;
        
    }
    
    public void getStruct(int buffer[]){
        
        int c = 0;
        buffer[c++] = STRUCTSIZE;
        buffer[c++] = OPCODE;
        buffer[c++] = floatToInt(radius);        
        c++; // align to 4 words boundary 
        buffer[c++] = floatToInt(center.x);
        buffer[c++] = floatToInt(center.y);
        buffer[c++] = floatToInt(center.z);
        buffer[c++] = 0;

    }

}