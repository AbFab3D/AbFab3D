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
   performs rotation about given axis 
*/
public class Rotation implements VecTransform, Initializable {
    
    private Vector3d m_axis = new Vector3d(1,0,0); 
    private double m_angle = 0;
    
    private Matrix3d 
        mat = new Matrix3d(),
        mat_inv = new Matrix3d();
    
    public Rotation(){
    }
    
    public Rotation(Vector3d axis, double angle){
        
        setRotation(axis, angle);
        
    }
    
    public void setRotation(Vector3d axis, double angle){
        
        m_axis = new Vector3d(axis); 
        m_angle = angle;
        
   } 
    
    public int initialize(){
        
        mat.set(new AxisAngle4d(m_axis.x,m_axis.y,m_axis.z,m_angle));
        mat_inv.set(new AxisAngle4d(m_axis.x,m_axis.y,m_axis.z,-m_angle));
        
        return RESULT_OK;
    }
    
    /**
     *
     */
    public int transform(Vec in, Vec out) {
        
        out.set(in);
        double x,y,z;
        
        x = in.v[0];
        y = in.v[1];
        z = in.v[2];
        
        out.v[0] = mat.m00* x + mat.m01*y + mat.m02*z;
        out.v[1] = mat.m10* x + mat.m11*y + mat.m12*z;
        out.v[2] = mat.m20* x + mat.m21*y + mat.m22*z;
        
        return RESULT_OK;
    }
    
    /**
     *
     */
    public int inverse_transform(Vec in, Vec out) {
        
        out.set(in);
        
        double x,y,z;
        
        x = in.v[0];
        y = in.v[1];
        z = in.v[2];
        
        out.v[0] = mat_inv.m00* x + mat_inv.m01*y + mat_inv.m02*z;
        out.v[1] = mat_inv.m10* x + mat_inv.m11*y + mat_inv.m12*z;
        out.v[2] = mat_inv.m20* x + mat_inv.m21*y + mat_inv.m22*z;
        
        return RESULT_OK;
        
    }
    
} // class Rotation
