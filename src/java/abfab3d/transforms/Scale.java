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
   performs scaling by given factor
*/
public class Scale  implements VecTransform {
    
    protected double sx = 1., sy = 1., sz = 1.; 
    protected double averageScale = 1.;

    /**
       identity transform 
     */
    public Scale(){
    }
    
    /**
       uniform scaling
       @param s uniform scaling factor 
     */
    public Scale(double s){
        setScale(s);
    }
    
    /**
       non uniform scaling
       @param sx x axis scaling factor 
       @param sy y axis scaling factor 
       @param sz z axis scaling factor 
     */
    public Scale(double sx, double sy, double sz){
        setScale(sx,sy,sz);
    }
    
    /**
       @noRefGuide
     */
    public void setScale(double s){
        
        sx = s;
        sy = s;
        sz = s;
        this.averageScale = s;
        
    }
    
    /**
       @noRefGuide
     */
    public void setScale(double sx, double sy, double sz){
        
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;
        
        this.averageScale = Math.pow(sz*sy*sz, 1./3);
    }
    
    /**
       @noRefGuide
     */
    public int transform(Vec in, Vec out) {
        
        out.set(in);
        
        out.v[0] = in.v[0]*sx;
        out.v[1] = in.v[1]*sy;
        out.v[2] = in.v[2]*sz;
        
        out.mulScale(averageScale);
        
        return RESULT_OK;
    }                
    
    /**
       @noRefGuide
     */
    public int inverse_transform(Vec in, Vec out) {
        
        out.set(in);
        out.v[0] = in.v[0]/sx;
        out.v[1] = in.v[1]/sy;
        out.v[2] = in.v[2]/sz;
        
        out.mulScale(1/averageScale);
        
        return RESULT_OK;
        
    }
} // class Scale
