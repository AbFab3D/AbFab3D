/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.transforms;

import abfab3d.core.ResultCodes;
import abfab3d.param.BaseParameterizable;
import abfab3d.core.Vec;
import abfab3d.core.VecTransform;

import static abfab3d.core.Output.printf;
import static abfab3d.util.Symmetry.toFundamentalDomain;


/**
   
   identity transform, does nothing 
   only transfers data 
   
*/
public class Identity extends BaseParameterizable implements VecTransform {
    /**
       @noRefGuide
     */
    public int transform(Vec in, Vec out) {
        out.set(in);
        return ResultCodes.RESULT_OK;
    }
    /**
       @noRefGuide
     */
    public int inverse_transform(Vec in, Vec out) {
        out.set(in);
        return ResultCodes.RESULT_OK;
    }
    
}
