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

import javax.vecmath.Vector3d;

import abfab3d.core.VecTransform;
import abfab3d.core.Initializable;
import abfab3d.core.Vec;
import abfab3d.core.ResultCodes;

import abfab3d.param.SNodeParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Parameterizable;

import abfab3d.util.ReflectionGroup;

import static abfab3d.core.Output.fmt;


public class SPlaneTransform extends BaseTransform implements VecTransform, Initializable {

    SNodeParameter mp_splane = new SNodeParameter("splane", "splane to use",new PlaneReflection(new Vector3d(1,0,0),0));
    
    VecTransform m_splane;  // may be SphereInversion or PlaneReflection
    
    Parameter m_aparam[] = new Parameter[]{
        mp_splane
    };
    
    public SPlaneTransform(){
        
        super.addParams(m_aparam);

    }

    public SPlaneTransform(ReflectionGroup.SPlane splane){
        super.addParams(m_aparam);

        if(splane instanceof ReflectionGroup.Sphere) {
            ReflectionGroup.Sphere s = (ReflectionGroup.Sphere)splane;
            mp_splane.setValue(new SphereInversion(new Vector3d(s.cx,s.cy,s.cz), s.r));
        } else if(splane instanceof ReflectionGroup.Plane){
            ReflectionGroup.Plane p = (ReflectionGroup.Plane)splane;
            mp_splane.setValue(new PlaneReflection(new Vector3d(p.nx, p.ny, p.nz), p.dist));
        } else throw new RuntimeException(fmt("illegal argument %s", splane));
    }
    
    public SPlaneTransform(Parameterizable splane){
        
        super.addParams(m_aparam);
        
        checkType(splane);

        mp_splane.setValue(splane);
    }

    void checkType(Object value){                
        if(!(value instanceof SphereInversion || value instanceof PlaneReflection))
            throw new RuntimeException(fmt("illegal value: %s in SPlaneTransform", value));
    }

    /**
       @noRefGuide
    */
    public int initialize(){
                
        Object value = mp_splane.getValue();
        checkType(value);
        m_splane = (VecTransform)value;        
        ((Initializable)m_splane).initialize();

        return ResultCodes.RESULT_OK;

    }
  
    /**
       @return SPlane for this transform 
     */
    public ReflectionGroup.SPlane  getSPlane(){
        Object value = mp_splane.getValue();
        if(value instanceof SphereInversion){

            SphereInversion sphere = (SphereInversion)value;
            Vector3d center = (Vector3d)sphere.get("center");
            double radius = (Double)sphere.get("radius");
            
            return new ReflectionGroup.Sphere(center, radius);

        } else if(value instanceof PlaneReflection){

            PlaneReflection plane = (PlaneReflection)value;
            Vector3d normal = (Vector3d)plane.get("normal");
            double distance = (Double)plane.get("dist");
            return new ReflectionGroup.Plane(normal, distance);
        }
        return null;
    }
    
    /**
       @noRefGuide
    */
    public int transform(Vec in, Vec out) {
        return m_splane.transform(in, out);
    }

    /**
       @noRefGuide
     *  inverse transform is identical to direct transform
     */
    public int inverse_transform(Vec in, Vec out) {
        
        return m_splane.inverse_transform(in, out);
        
    }
    
}