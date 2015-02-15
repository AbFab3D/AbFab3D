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

import javax.vecmath.Vector3d;

import abfab3d.param.Parameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.param.SNode;

import abfab3d.datasources.Intersection;

import static opencl.CLUtils.floatToInt;

import static abfab3d.util.Output.printf;

/**
  OpenCL code for union of multiple objects 

   @author Vladimir Bulatov
 */
public class CLIntersection implements CLCodeGenerator {

    static int STRUCTSIZE = 4;    
    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer) {

        Intersection inter = (Intersection)node;

        double blendWidth = ((DoubleParameter)inter.getParam("blend")).getValue();
        printf("intersection.blend: %f\n", blendWidth);

        SNode[] children = inter.getChildren();
        if(children.length == 0) 
            return 0;

        int wcount = 0;

        // save working register 
        wcount += CLUtils.addOPCode(Opcodes.oPUSH_D2, codeBuffer);           

        Parameterizable child = (Parameterizable)children[0];
        CLCodeGenerator clnode = CLNodeFactory.getCLNode((Parameterizable)child);
        wcount += clnode.getCLCode((Parameterizable)child, codeBuffer);
        wcount += CLUtils.addOPCode(Opcodes.oCOPY_D1D2, codeBuffer);

        for(int i = 1; i < children.length; i++){
            child = (Parameterizable)children[i];
            clnode = CLNodeFactory.getCLNode((Parameterizable)child);
            wcount += clnode.getCLCode((Parameterizable)child, codeBuffer);
            if(blendWidth != 0.) {
                int c = 0;
                buffer[c++] = STRUCTSIZE;
                buffer[c++] = Opcodes.oBLENDMAX;
                buffer[c++] = floatToInt(blendWidth);  
                codeBuffer.add(buffer, STRUCTSIZE);
            } else {
                wcount += CLUtils.addOPCode(Opcodes.oMAX, codeBuffer); 
            }
        }

        wcount += CLUtils.addOPCode(Opcodes.oCOPY_D2D1, codeBuffer);           
         // restore working register 
        wcount += CLUtils.addOPCode(Opcodes.oPOP_D2, codeBuffer);           
       return wcount;
    }
}