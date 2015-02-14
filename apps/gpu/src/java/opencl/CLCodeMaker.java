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

import abfab3d.param.Parameterizable;


import static abfab3d.util.Output.printf;


/**
   makes opcode for fot given node 
   
   @author Vladimir Bulatov
 */
public class CLCodeMaker implements CLCodeGenerator {

    // factory to get CL nodes from Parametrizable 
    CLNodeFactory m_factory = new CLNodeFactory();

    /**
       makes CL code for given node 
       creates opcodes buffer 
       @return buffer of opcodes 
     */
    public CLCodeBuffer makeCLCode(Parameterizable node){

        CLCodeBuffer codeBuffer = new CLCodeBuffer(10000);

        CLCodeGenerator clnode = m_factory.getCLNode(node);

        clnode.getCLCode(node, codeBuffer);

        return codeBuffer;
        
    }

    
    /**
       makes CL code for given node 
       add opcodes to supplied opcodes buffer
       @return buffer of opcodes 
     */
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer){
        
        CLCodeGenerator clnode = m_factory.getCLNode(node);
        int count = clnode.getCLCode(node, codeBuffer);
        return count;
        
    }

}