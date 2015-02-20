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

import abfab3d.transforms.CompositeTransform;
import abfab3d.util.VecTransform;

import static opencl.CLUtils.floatToInt;

import static abfab3d.util.Output.printf;

/**
   OpenCL code for chain of transformations

   @author Vladimir Bulatov
 */
public class CLCompositeTransform extends CLNodeBase {

    static final boolean DEBUG = false;
        
    public int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer) {

        CompositeTransform transform = (CompositeTransform)node;

        VecTransform vt[] = transform.getTransformsArray();

        if(vt.length == 0) {
            // no trnasoforms - do nothing 
            return 0;
        }
        int wcount = 0;

        // transforms in voxel space are applied in reverse order 
        for(int i = vt.length-1; i >= 0; i--){
            CLCodeGenerator clnode = CLNodeFactory.getCLNode((Parameterizable)vt[i]);
            wcount += clnode.getCLCode((Parameterizable)vt[i], codeBuffer);
        }
        return wcount;
    }
}