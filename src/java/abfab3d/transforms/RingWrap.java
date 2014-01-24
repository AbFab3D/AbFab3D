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
   Wraps band in XZ plane about a cylinder of given radius.
   Cylinder axis is parallel to Y axis. 
   </p>
   <p>
   The diagram below is oriented with Y axis toward the user. 
   </p>
   <embed src="doc-files/ring_wrap.svg" type="image/svg+xml"/> 
   
*/
public class RingWrap implements VecTransform, Initializable {
    
    public double m_radius = 0.035; // units are meters       
    
    
    /**
       @noRefGuide
     */
    public RingWrap(){
    }
    
    
    /**
       Ring wrap with given radius
     */
    public RingWrap(double r){
        m_radius = r;            
    }
    
    /**
       set radius of the wrap
     */
    public void setRadius(double r){
        m_radius = r;
    }

    /**
       @noRefGuide       
     */
    public int initialize(){
        // do sometithing? 
        return RESULT_OK;
    }
    /**
     * Calculate cartesian to polar coordinates
     *
     * @param in
     * @param out
     @noRefGuide
     */
    public int transform(Vec in, Vec out) {
        
        out.set(in);
        
        double angle = in.v[0] / m_radius;
        double r = m_radius + in.v[2];
        double sina = FastMath.sin(angle);
        double cosa = FastMath.cos(angle);
        
        out.v[0] = r * sina;
        out.v[1] = in.v[1];
        out.v[2] = r * cosa;
        return RESULT_OK;
    }                
    
    /**
     * Calculate polar to cartesian coordinates
     * @param in
     * @param out
     @noRefGuide
     */
    public int inverse_transform(Vec in, Vec out) {
        
        out.set(in);
        double wx = in.v[0] / m_radius;
        double wy = in.v[1];
        double wz = in.v[2] / m_radius;
        
        double dist = FastMath.sqrt(wx * wx + wz * wz);
        double angle = FastMath.atan2(wx, wz);
        
        
        wx = angle * m_radius;
        wz = (dist-1) * m_radius;
        
        out.v[0] = wx;
        out.v[1] = wy;
        out.v[2] = wz;
        
        return RESULT_OK;
        
    }
}        
