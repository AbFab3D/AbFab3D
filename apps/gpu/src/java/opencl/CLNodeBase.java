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
import abfab3d.datasources.TransformableDataSource;

import static opencl.CLUtils.floatToInt;

import static abfab3d.util.Units.MM;
import static abfab3d.util.Output.printf;

/**
   base node for CLCodeGenerators 
   handles build-in  transforms and materials

   @author Vladimir Bulatov
 */
public abstract class CLNodeBase implements CLCodeGenerator {
    
    static final boolean DEBUG = false;

    /**
       adds code for transforations (if transform is not null)
       @return word count of the generated code 
     */
    public int getTransformCLCode(Parameterizable node, CLCodeBuffer codeBuffer){
        if(DEBUG)printf("getTransformCLCode: %s\n", node);

        if(!(node instanceof TransformableDataSource)) {
            // node without interface 
            return 0;
        }
        
        TransformableDataSource tds = (TransformableDataSource)node;
        if(DEBUG)printf("  tds: %s\n", tds);
        Parameterizable trans = (Parameterizable)tds.getTransform();
        if(DEBUG)printf("  trans: %s\n", trans);
        if(trans == null)
            return 0;

        CLCodeGenerator clnode = CLNodeFactory.getCLNode(trans);
        if(DEBUG)printf("  clnode: %s\n", clnode);

        return clnode.getCLCode(trans, codeBuffer);

    }

    /**
       adds code for material calculations (if material in not null)
       @return word count of the generated code 
     */
    public int getMaterialCLCode(Parameterizable node, CLCodeBuffer codeBuffer){
        return 0;
    }

    public abstract int getCLCode(Parameterizable node, CLCodeBuffer codeBuffer);
   
}