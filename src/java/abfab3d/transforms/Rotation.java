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

import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;
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
public class Rotation extends BaseTransform implements VecTransform, Initializable {
    
    private Vector3d m_axis = new Vector3d(1,0,0); 
    private double m_angle = 0;
    private Vector3d m_center;

    private Matrix3d 
        mat = new Matrix3d(),
        mat_inv = new Matrix3d();
    
    
    /**
       identity rotation
     */
    public Rotation(){
        this(1,0,0,0);
    }
    /**
       rotation with given axis and angle. Angle is measure in radians. 
       @param angle  rotation angle is measured in radians
     */
    public Rotation(Vector3d axis, double angle){
        
        this(axis.x, axis.y, axis.z, angle);
    }

    /**
       rotation with given axis components and angle. 
       @param ax  x component of rotation axis 
       @param ay  y component of rotation axis 
       @param az  z component of rotation axis 
       @param angle  rotation angle is measured in radians
       
     */
    public Rotation(double ax, double ay, double az, double angle){
        initParams();
        setRotation(new Vector3d(ax, ay, az), angle);
    }


    /**
       rotation with given axis, angle and center. Angle is measure in radians. 
       @param angle  rotation angle is measured in radians
     */
    public Rotation(Vector3d axis, double angle, Vector3d center){
        initParams();
        setRotation(axis, angle, center);
    }

    public void initParams() {
        super.initParams();

        Parameter p = new Vector3dParameter("axis");
        params.put(p.getName(), p);

        p = new DoubleParameter("angle");
        params.put(p.getName(), p);

        p = new Vector3dParameter("center");
        params.put(p.getName(), p);
    }

    /**
       @noRefGuide
       @param angle  rotation angle is measured in radians
     */
    public void setRotation(Vector3d axis, double angle){
        
        m_axis = new Vector3d(axis); 
        m_angle = angle;
        m_center = null;

        ((Vector3dParameter) params.get("axis")).setValue(new Vector3d(axis));
        ((DoubleParameter) params.get("angle")).setValue(angle);
    }

    /**
       @noRefGuide
       @param angle  rotation angle is measured in radians
     */
    public void setRotation(Vector3d axis, double angle, Vector3d center){
        
        m_axis = new Vector3d(axis); 
        m_axis.normalize();
        m_angle = angle;
        m_center = new Vector3d(center);

        ((Vector3dParameter) params.get("axis")).setValue(new Vector3d(axis));
        ((DoubleParameter) params.get("angle")).setValue(angle);
        ((Vector3dParameter) params.get("axis")).setValue(new Vector3d(center));
   }
    
    /**
       @noRefGuide
     */
    public int initialize(){

        mat.set(new AxisAngle4d(m_axis.x,m_axis.y,m_axis.z,m_angle));
        mat_inv.set(new AxisAngle4d(m_axis.x,m_axis.y,m_axis.z,-m_angle));
        
        return RESULT_OK;
    }
    
    /**
       @noRefGuide
     */
    public int transform(Vec in, Vec out) {
        
        out.set(in);
        double x,y,z;
        
        x = in.v[0];
        y = in.v[1];
        z = in.v[2];

        if(m_center != null){
            x -= m_center.x;
            y -= m_center.y;
            z -= m_center.z;
        }
        
        out.v[0] = mat.m00* x + mat.m01*y + mat.m02*z;
        out.v[1] = mat.m10* x + mat.m11*y + mat.m12*z;
        out.v[2] = mat.m20* x + mat.m21*y + mat.m22*z;

        if(m_center != null){
            out.v[0] += m_center.x;
            out.v[1] += m_center.y;
            out.v[2] += m_center.z;
        }
        
        return RESULT_OK;
    }
    
    /**
       @noRefGuide
     */
    public int inverse_transform(Vec in, Vec out) {
        
        out.set(in);
        
        double x,y,z;
        
        x = in.v[0];
        y = in.v[1];
        z = in.v[2];
        
        if(m_center != null){
            x -= m_center.x;
            y -= m_center.y;
            z -= m_center.z;
        }
        out.v[0] = mat_inv.m00* x + mat_inv.m01*y + mat_inv.m02*z;
        out.v[1] = mat_inv.m10* x + mat_inv.m11*y + mat_inv.m12*z;
        out.v[2] = mat_inv.m20* x + mat_inv.m21*y + mat_inv.m22*z;
        
        if(m_center != null){
            out.v[0] += m_center.x;
            out.v[1] += m_center.y;
            out.v[2] += m_center.z;
        }

        return RESULT_OK;
        
    }
    
} // class Rotation
