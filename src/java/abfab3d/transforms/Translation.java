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
   
   Performs translation in space 
   
*/
public class Translation  implements VecTransform {
    
    protected double tx = 1, ty = 1, tz = 1; 
   
    /**
       identity transform 
     */
    public Translation(){
        setTranslation(0,0,0);
    }
    
    /**
       translation to given point
       @param tx  x component of translation 
       @param ty  y component of translation 
       @param tz  z component of translation 
     */
    public Translation(double tx, double ty, double tz){
        
        setTranslation(tx, ty, tz);
        
    }

    /**
       translation to given point
       @param p  vector of translation 
     */
    public Translation(Vector3d p){
        
        setTranslation(p.x, p.y,p.z);
        
    }
    
    /**
       @noRefGuide
     */
    public void setTranslation(double tx, double ty, double tz){
        
        this.tx = tx;
        this.ty = ty;
        this.tz = tz;
        
    }
    
    /**
       @noRefGuide
     */
    public int transform(Vec in, Vec out) {
        
        out.set(in);
        out.v[0] = in.v[0] + tx;
        out.v[1] = in.v[1] + ty;
        out.v[2] = in.v[2] + tz;
        
        
        return RESULT_OK;
    }                
    
    /**
       @noRefGuide
     */
    public int inverse_transform(Vec in, Vec out) {
        out.set(in);
        out.v[0] = in.v[0] - tx;
        out.v[1] = in.v[1] - ty;
        out.v[2] = in.v[2] - tz;
        
        return RESULT_OK;
        
    }
} // class Translation
