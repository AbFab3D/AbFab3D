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
public class SphereInversion  implements VecTransform, Initializable  {
    
    public Vector3d m_center = new Vector3d(0,0,1); 
    public double m_radius = FastMath.sqrt(2.);
    
    private double radius2; 
    static double EPS = 1.e-20;
    
    /**
       Creates default sphere which interchanges upper half space and unit ball
    */
    public SphereInversion(){
    }
    
    /**
       Inversion in a sphere of given center and radius 
       @param center 
       @param radus 
     */
    public SphereInversion(Vector3d center, double radius){

        setSphere(center, radius); 

    }


    /**
       @noRefGuide
     */
    public void setSphere(Vector3d center, double radius){
        
        m_center = new Vector3d(center);
        m_radius = radius;
        
    }
    
    /**
       @noRefGuide
     */
    public int initialize(){
        
        radius2 = m_radius*m_radius; 
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
        x -= m_center.x;
        y -= m_center.y;
        z -= m_center.z;
        double r2 = (x*x + y*y + z*z);
        if(r2 < EPS) r2 = EPS;
        
        double scale = radius2/r2;
        
        x *= scale;
        y *= scale;
        z *= scale;
        
        // move origin back to center
        x += m_center.x;
        y += m_center.y;
        z += m_center.z;
            
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
