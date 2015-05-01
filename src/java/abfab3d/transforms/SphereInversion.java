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

import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.AxisAngle4d;

import abfab3d.util.Vec;
import abfab3d.util.Initializable;
import abfab3d.util.Symmetry;
import abfab3d.util.ReflectionGroup;
import abfab3d.util.VecTransform;

import net.jafama.FastMath;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Symmetry.getReflection;
import static abfab3d.util.Symmetry.toFundamentalDomain;

/**
   <p>
   performs inversion in a sphere of given center and radius 
   </p>
   <p>
   See <a href="http://en.wikipedia.org/wiki/Inversion_in_a_sphere">inversion in a sphere</a>. 
   </p>
*/
public class SphereInversion  extends BaseTransform implements VecTransform, Initializable  {
    
    Vector3dParameter  mp_center = new Vector3dParameter("center","center of sphere",new Vector3d(0,0,1));
    DoubleParameter  mp_radius = new DoubleParameter("radius","radius of sphere", Math.sqrt(2.));

    Parameter m_aparam[] = new Parameter[]{
        mp_center,
        mp_radius
    };

    
    private double m_radius2, m_cx, m_cy, m_cz; 
    static double EPS = 1.e-20;
    
    /**
       Creates default sphere which interchanges upper half space and unit ball
    */
    public SphereInversion(){
        initParams();
    }
    
    /**
       Inversion in a sphere of given center and radius 
       @param center 
       @param radus 
     */
    public SphereInversion(Vector3d center, double radius){
        initParams();
        setSphere(center, radius); 

    }
    
    /**
       
       Inversion in a sphere with given components of center and radius 
       @param cx    
       @param cy
       @param cz    
       @param radus 
     */
    public SphereInversion(double cx, double cy, double cz, double radius){
        initParams();
        setSphere(new Vector3d(cx, cy, cz), radius);

    }

    public void setCenter(Vector3d val) {
        mp_center.setValue(val.clone());
    }

    public void setRadius(Double val) {
        mp_radius.setValue(val);
    }

    protected void initParams(){
        super.addParams(m_aparam);
    }

    /**
       @noRefGuide
     */
    public void setSphere(Vector3d center, double radius){
        
        mp_center.setValue(new Vector3d(center));
        mp_radius.setValue(radius);
        
    }
    
    /**
       @noRefGuide
     */
    public int initialize(){

        Vector3d center = mp_center.getValue();
        m_cx = center.x;
        m_cy = center.y;
        m_cz = center.z;

        double radius = mp_radius.getValue();
        m_radius2 = radius*radius; 

        return RESULT_OK;
        
    }
    
    
    /**
       @noRefGuide
     */
    public int transform(Vec in, Vec out) {
        
        out.set(in);
        
        double x = in.v[0];
        double y = in.v[1];
        double z = in.v[2];
        
        // move center to origin 
        x -= m_cx;
        y -= m_cy;
        z -= m_cz;
        double r2 = (x*x + y*y + z*z);
        if(r2 < EPS) r2 = EPS;
        
        double scale = m_radius2/r2;
        
        x *= scale;
        y *= scale;
        z *= scale;
        
        // move center back 
        x += m_cx;
        y += m_cy;
        z += m_cz;
            
        out.v[0] = x;
        out.v[1] = y;
        out.v[2] = z;
        
        out.mulScale(scale);
        
        return RESULT_OK;
    }                
    
    /**
       @noRefGuide
     */
    public int inverse_transform(Vec in, Vec out) {
        
        transform(in, out);
        
        return RESULT_OK;
        
    }
} // class SphereInversion
