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

import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Symmetry.getReflection;
import static abfab3d.util.Symmetry.toFundamentalDomain;

/**
   performs twist of space via rotation about z-axis 
   @author Vladimir Bulatov 
*/
public class Twist implements VecTransform, Initializable {
    
    // length at which the transform performs complete 360 degree twist
    private double m_period = 1.; 

    private double m_twistSpeed = 2*Math.PI;
    
    /**
       twist around z-axis 
       @param period  distance at which the rotation angle is measured in radians
     */
    public Twist(double period){
        
        m_period = period;
        
    }
    
    /**
       @noRefGuide
     */
    public int initialize(){

        m_twistSpeed = 2*Math.PI/m_period;
        return RESULT_OK;
    }
    
    /**
       @noRefGuide
     */
    public int transform(Vec in, Vec out) {
        
        out.set(in);
        twistXY(out.v, (m_twistSpeed*out.v[2]));

        return RESULT_OK;
    }
    
    /**
       @noRefGuide
     */
    public int inverse_transform(Vec in, Vec out) {
        
        out.set(in);
        
        twistXY(out.v, -(m_twistSpeed*out.v[2]));

        return RESULT_OK;
        
    }

    
    private static final void twistXY(double v[], double angle){

        double x,y,z;
        
        x = v[0];
        y = v[1];
        
        double cosa = cos(angle);
        double sina = sin(angle);

        v[0] = cosa * x + sina * y;
        v[1] = -sina * x + cosa * y;        
        
    }
    
} // class Rotation
