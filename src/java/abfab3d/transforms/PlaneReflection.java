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
   reflect in a given plane 
*/
public class PlaneReflection  implements VecTransform, Initializable  {
    
    public Vector3d m_pointOnPlane = new Vector3d(0,0,0); 
    public Vector3d m_planeNormal = new Vector3d(1,0,0); 
    
    public PlaneReflection(Vector3d normal){

        m_planeNormal = new Vector3d(normal);

    }

    public PlaneReflection(Vector3d normal, Vector3d pointOnPlane){

        m_pointOnPlane = new Vector3d(pointOnPlane);
        m_planeNormal = new Vector3d(normal);

    }
    public int initialize(){
        
        m_planeNormal.normalize();
        
        return RESULT_OK;
        
    }
    
    
    /**
     *
     */
    public int transform(Vec in, Vec out) {
        out.set(in);
        double x = in.v[0];
        double y = in.v[1];
        double z = in.v[2];
        
        // move center to origin 
        x -= m_pointOnPlane.x;
        y -= m_pointOnPlane.y;
        z -= m_pointOnPlane.z;
        
        double dot = 2*(x*m_planeNormal.x + y*m_planeNormal.y + z*m_planeNormal.z);
        
        x -= dot*m_planeNormal.x;
        y -= dot*m_planeNormal.y;
        z -= dot*m_planeNormal.z;
        
        // move origin back to center
        x += m_pointOnPlane.x;
        y += m_pointOnPlane.y;
        z += m_pointOnPlane.z;
        
        out.v[0] = x;
        out.v[1] = y;
        out.v[2] = z;
        
        return RESULT_OK;
    }                
    
    /**
     *  inverse transform is identical to direct transform
     */
    public int inverse_transform(Vec in, Vec out) {
        
        return transform(in, out);
        
    }
} // class PlaneReflection

