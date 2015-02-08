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

import abfab3d.param.BaseParameterizable; 

/**
   interface to generate CL code for supplied node 

   @author Vladimir Bulatov
*/
public interface CLCodeGenerator {
    /**
       stores CL opcode for given node in the buffer 
       @param node for which code will be generated
       @param codeBuffer to store the code 
       @return word count of the code 
     */
    public int getCLCode(BaseParameterizable node, CLCodeBuffer codeBuffer);
        
}