/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011-2015
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

import java.util.List;

import abfab3d.core.Initializable;
import abfab3d.core.ResultCodes;
import abfab3d.core.Vec;
import abfab3d.core.VecTransform;
import abfab3d.param.Parameter;
import abfab3d.param.SNode;
import abfab3d.param.SNodeListParameter;
import abfab3d.param.ValueHash;

import static abfab3d.core.Output.printf;
import static abfab3d.util.Symmetry.toFundamentalDomain;


/**

   Arbitrary chain of transformations to be applied to the point 
   
   @author Vladimir Bulatov   
 */
public class CompositeTransform extends BaseTransform implements VecTransform, Initializable, ValueHash {
        
    private VecTransform aTransforms[]; // array of transforms used in calculations 

    SNodeListParameter mp_transforms = new SNodeListParameter("transforms");

    protected Parameter m_aparams[] = new Parameter[]{
        mp_transforms
    };

    /**
       creates empty composite transform
     */
    public CompositeTransform() {
        addParams(m_aparams);
    }

    /**
       creates composite transform with single transform 
     */
    public CompositeTransform(VecTransform transform) {

        addParams(m_aparams);
        add(transform);

    }

    /**
       creates composite transform with two transforms 
     */
    public CompositeTransform(VecTransform transform1,VecTransform transform2) {
        addParams(m_aparams);
        add(transform1);
        add(transform2);

    }

    /**
       creates composite transform with three transforms 
     */
    public CompositeTransform(VecTransform transform1,VecTransform transform2, VecTransform transform3) {
        addParams(m_aparams);
        add(transform1);
        add(transform2);
        add(transform3);

    }

    /**
       creates composite transform with four transforms 
     */
    public CompositeTransform(VecTransform transform1,VecTransform transform2, VecTransform transform3, VecTransform transform4) {
        addParams(m_aparams);
        add(transform1);
        add(transform2);
        add(transform3);
        add(transform4);

    }

    /**
       add transform to the chain of transforms
     */
    public void add(VecTransform transform){
        
        ((List)mp_transforms.getValue()).add(transform);
        
    }

    /**
       @noRefGuide
     */
    public int initialize(){
        
        aTransforms = getTransformsArray();
        int size = aTransforms.length;
        for(int i = 0; i < size; i++){
            VecTransform tr = aTransforms[i];
            if(tr instanceof Initializable){
                int res = ((Initializable)tr).initialize();
                if(res != RESULT_OK)
                    return res;
            }
        }
        
        return ResultCodes.RESULT_OK;
    }

    public VecTransform [] getTransformsArray(){

        List<SNode> trans = (List<SNode>)mp_transforms.getValue();
        int size = trans.size();
        VecTransform ta[] = new VecTransform[size];
        int k = 0;
        for(SNode t: trans){
            ta[k++] = (VecTransform)t;
        }
        return ta;
    }

    /**
       @noRefGuide
     */
    public int transform(Vec in, Vec out) {
        
        int len = aTransforms.length;
        if(len < 1){
            // copy input to output                 
            out.set(in);
            return ResultCodes.RESULT_OK;
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
        
        return ResultCodes.RESULT_OK;
    }                
    
    /**
       @noRefGuide
     */
    public int inverse_transform(Vec in, Vec out) {
        
        int len = aTransforms.length;
        if(len < 1){
            // copy input to output                 
            out.set(in);
            return ResultCodes.RESULT_OK;
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
        
        return ResultCodes.RESULT_OK;
        
    }

    @Override
    public SNode[] getChildren() {
        
        VecTransform[] ta = getTransformsArray();

        SNode[] ret = new SNode[ta.length];
        for(int i=0; i < ta.length; i++) {
            ret[i] = (SNode)ta[i];
        }
        return ret;
    }

    /**
     * Implement this as a value
     * @return
     */
    public String getParamString() {
        StringBuilder sb = new StringBuilder();
        List list = mp_transforms.getValue();
        int len = list.size();
        for(int i=0; i < len; i++) {
            VecTransform vt = (VecTransform) list.get(i);
            if (vt instanceof ValueHash) {
                sb.append(((ValueHash) vt).getParamString());
            } else {
                sb.append(vt.toString());
            }
        }

        return sb.toString();
    }
}  // class CompositeTransform
