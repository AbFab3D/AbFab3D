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

import java.util.Vector;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.AxisAngle4d;

import abfab3d.param.Parameter;
import abfab3d.param.Parameterizable;
import abfab3d.param.SNode;
import abfab3d.param.SNodeListParameter;
import abfab3d.util.*;

import net.jafama.FastMath;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Symmetry.getReflection;
import static abfab3d.util.Symmetry.toFundamentalDomain;


/**

   Arbitrary long chain of transformation to be applied to the shape. 
   
 */
public class CompositeTransform extends BaseTransform implements VecTransform, Initializable {
    
    private Vector<VecTransform> vTransforms = new Vector<VecTransform>();
    
    private VecTransform aTransforms[]; // array used in calculations 

    public CompositeTransform() {
        initParams();
    }

    /**
       add transform to the chain of transforms
     */
    public void add(VecTransform transform){
        
        vTransforms.add(transform);
        ((SNodeListParameter) params.get("transforms")).getValue().add((SNode) transform);

    }

    public void initParams() {
        super.initParams();

        Parameter p = new SNodeListParameter("transforms");
        params.put(p.getName(), p);
    }

    /**
       @noRefGuide
     */
    public int initialize(){
        
        int len = vTransforms.size();
        
        aTransforms = (VecTransform[])vTransforms.toArray(new VecTransform[len]);
        
        for(int i = 0; i < len; i++){
            VecTransform tr = aTransforms[i];
            if(tr instanceof Initializable){
                int res = ((Initializable)tr).initialize();
                if(res != RESULT_OK)
                    return res;
            }
        }
        
        return RESULT_OK;
    }
    
    /**
       @noRefGuide
     */
    public int transform(Vec in, Vec out) {
        
        int len = aTransforms.length;
        if(len < 1){
            // copy input to output                 
            out.set(in);
            return RESULT_OK;                
        }
        
        //TODO garbage generation 
        Vec vin = new Vec(in);
        
        for(int i = 0; i < len; i++){
            
            VecTransform tr = aTransforms[i];
            int res = tr.transform(in, out);
                if(res != RESULT_OK)
                    return res;
                
                in.set(out);
        }
        
        return RESULT_OK;
    }                
    
    /**
       @noRefGuide
     */
    public int inverse_transform(Vec in, Vec out) {
        
        int len = aTransforms.length;
        if(len < 1){
            // copy input to output                 
            out.set(in);
            return RESULT_OK;                
        }
        
        //TODO garbage generation 
        Vec vin = new Vec(in);
        
        for(int i = aTransforms.length-1; i >= 0; i--){
            
            VecTransform tr = aTransforms[i];
            int res = tr.inverse_transform(vin, out);
            
            if(res != RESULT_OK)
                return res;
            vin.set(out);
        }
        
        return RESULT_OK;
        
    }

    @Override
    public SNode[] getChildren() {
        aTransforms = (VecTransform[])vTransforms.toArray(new VecTransform[vTransforms.size()]);

        SNode[] ret = new SNode[aTransforms.length];
        for(int i=0; i < aTransforms.length; i++) {
            ret[i] = (SNode)aTransforms[i];
        }
        return ret;
    }

}  // class CompositeTransform
