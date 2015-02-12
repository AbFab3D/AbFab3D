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

import abfab3d.param.Parameterizable;


import static abfab3d.util.Output.printf;


public class OpcodeMaker implements CLCodeGenerator {
    

    
    /**
       makes CL code for given node 
       @return count of opcodes 
     */
    CLNodeFactory m_factory = new CLNodeFactory();

    public CLCodeBuffer makeOpcode(Parameterizable node){

        CLCodeBuffer codeBuffer = new CLCodeBuffer(10000);

        CLCodeGenerator clnode = m_factory.getCLNode(node);

        clnode.getCLCode(node, codeBuffer);

        return codeBuffer;
        
    }

    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer){
        
        CLCodeGenerator clnode = m_factory.getCLNode(node);
        int count = clnode.getCLCode(node, codeBuffer);
        return count;
        
    }

    public static void printOpcodeBuffer(IntBuffer buffer){
        
        printf("printOpcodeBuffer(%s)\n", buffer);
        int work[] = new int[1000];

        while(true){
            
            int size = buffer.get();
            int opcode = buffer.get();
            if(size == 0) 
                break;
            printf("size: %4d opcode: %4d ", size, opcode);
            switch(opcode){
            default: 
                printf("Unknown opcode\n", opcode); break;
            case Opcodes.oSPHERE:
                printf("Sphere\n"); break;                
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