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

import abfab3d.datasources.Union;

import static opencl.CLUtils.floatToInt;

public class CLUnion implements CLCodeGenerator {

    static int OPCODE = Opcodes.oSPHERE;
    static int STRUCTSIZE = 8;
    
    int buffer[] = new int[STRUCTSIZE];
    
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer) {

        Union union = (Union)node;

        SNode[] children = union.getChildren();

        int wcount = 0;
        for(int i = 0; i < children.length; i++){

            Parameterizable child = (Parameterizable)children[i];
            CLCodeGenerator clnode = CLNodeFactory.getCLNode((Parameterizable)child);
            wcount += clnode.getCLCode((Parameterizable)child, codeBuffer);
        }
        return wcount;

    }

}