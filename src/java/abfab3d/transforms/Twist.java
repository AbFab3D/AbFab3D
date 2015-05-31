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
public class Twist extends BaseTransform implements VecTransform, Initializable {
    
    private double m_twistSpeed = 2*Math.PI;

    // length at which the transform performs complete 360 degree twist
    protected DoubleParameter mp_period = new DoubleParameter("period","Rotation angle",1);

    protected Parameter m_aparam[] = new Parameter[]{
            mp_period
    };

    /**
       twist around z-axis 
       @param period  distance at which the rotation angle is measured in radians
     */
    public Twist(double period){
        super.addParams(m_aparam);

        setPeriod(period);
    }

    public void setPeriod(double val) {
        mp_period.setValue(val);
        m_twistSpeed = 2*Math.PI/mp_period.getValue();
    }

    /**
       @noRefGuide
     */
    public int initialize(){
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
