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


import java.nio.IntBuffer;

import static abfab3d.util.Output.printf;

/**
   utils to work with OpenCL code 

   @author Vladimir Bulatov
 */
public class CLUtils {

    public static int floatToInt(double v){
        return Float.floatToIntBits((float)v);
    }

    static final int[] buffer = new int[2];

    /**
       @return word count of the code generated 
     */
    public static int addOPCode(int code, CLCodeBuffer codeBuffer){

        buffer[0] = 2;
        buffer[1] = code;
        codeBuffer.add(buffer, 2);
        return 2;
    }



    public static void printOpcodeBuffer(IntBuffer buffer){
        
        printf("printOpcodeBuffer(%s)\n", buffer);
        int work[] = new int[1000];

        while(true){
            
            int size = buffer.get();
            int opcode = buffer.get();
            if(size == 0) 
                break;
            printf("size:%2d opcode: %4d ", size, opcode);
            switch(opcode){
            default: 
                printf("Unknown opcode\n", opcode); break;
            case Opcodes.oSPHERE:
                printf("Sphere\n"); break;                
            case Opcodes.oMIN:
                printf("Min\n"); break;                
            case Opcodes.oMAX:
                printf("Max\n"); break;                
            case Opcodes.oBLENDMIN:
                printf("BlendMin\n"); break;                
            case Opcodes.oBLENDMAX:
                printf("BlendMax\n"); break;                
            case Opcodes.oCOPY_D1D2:
                printf("copyD1D2\n"); break;                
            case Opcodes.oCOPY_D2D1:
                printf("copyD2D1\n"); break;                
            case Opcodes.oPUSH_D2:
                printf("pushD2\n"); break;                
            case Opcodes.oPOP_D2:
                printf("popD2\n"); break;                
            case Opcodes.oBOX:
                printf("Box\n"); break;                
            case Opcodes.oTORUS:
                printf("Torus\n"); break;                
            case Opcodes.oGYROID:
                printf("Gyroid\n"); break;                
            }
            for(int i = 2; i < size; i++){
                int ri = (buffer.get());
                //float rf = Float.intBitsToFloat(ri); 
                //out.printf("%8x(%6.2f) ", ri,rf);        
            }
            //out.printf("\n");
        }    
        printf("printOpcodeBuffer() DONE\n");
        buffer.rewind();
    }
}
