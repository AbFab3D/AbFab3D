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



public class OpcodeMaker {

    static void printOpcodeBuffer(IntBuffer buffer){

        out.printf("printStructBuffer(%s)\n", buffer);
        int work[] = new int[1000];

        while(true){
            
            int size = buffer.get();
            int opcode = buffer.get();
            if(size == 0) 
                break;
            out.printf("size: %4d opcode: %4d ", size, opcode);
            switch(opcode){
            default: 
                out.printf("Unknown opcode\n", opcode); break;
            case Opcodes.oSPHERE:
                out.printf("Sphere\n"); break;                
            case Opcodes.oBOX:
                out.printf("Box\n"); break;                
            case Opcodes.oTORUS:
                out.printf("Torus\n"); break;                
            case Opcodes.oGYROID:
                out.printf("Gyroid\n"); break;                
            }
            for(int i = 2; i < size; i++){
                int ri = (buffer.get());
                //float rf = Float.intBitsToFloat(ri); 
                //out.printf("%8x(%6.2f) ", ri,rf);        
            }
            //out.printf("\n");
        }    
        out.printf("printStructBuffer() DONE\n");
        buffer.rewind();
    }
}