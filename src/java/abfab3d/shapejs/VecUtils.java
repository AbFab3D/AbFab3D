/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2016
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/
package abfab3d.shapejs;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import abfab3d.core.MathUtil;
import abfab3d.core.Color;

import static java.lang.Math.*;
import static abfab3d.core.Output.fmt;

/**
   utils to simplify works with Vector3d
*/
public class VecUtils {

    static final double EPS = 1.e-8;

    public static Vector4d mul(Vector4d u, Vector4d v){
        return new Vector4d(u.x*v.x, u.y*v.y,u.z*v.z,u.w*v.w);  
    }

    public static Vector4d mul(Vector4d u, double s){
        return new Vector4d(u.x*s, u.y*s,u.z*s,u.w*s);  
    }

    public static Vector3d mul(Vector3d u, double s){
        return new Vector3d(u.x*s, u.y*s,u.z*s);  
    }

    //
    // return component wise product of 2 vectors 
    //
    public static Vector3d mul(Vector3d u, Vector3d v){
        return new Vector3d(u.x*v.x, u.y*v.y, u.z*v.z);  
    }

    //
    // return component wise product of 3 vectors 
    //
    public static Vector3d mul(Vector3d u, Vector3d v, Vector3d w){
        return new Vector3d(u.x * v.x * w.x, u.y * v.y * w.y, u.z * v.z * w.z);  
    }

    public static Vector3d mul(Matrix4f m, Vector3d v){
        return new Vector3d(
                            m.m00*v.x + m.m01*v.y + m.m02*v.z,
                            m.m10*v.x + m.m11*v.y + m.m12*v.z,
                            m.m20*v.x + m.m21*v.y + m.m22*v.z);
    }

    public static Vector3d minVV(Vector3d u, Vector3d v){
        return new Vector3d(min(u.x, v.x), min(u.y, v.y), min(u.z, v.z));
    }
    static Vector3d maxVV(Vector3d u, Vector3d v){
        return new Vector3d(max(u.x, v.x), max(u.y, v.y), max(u.z, v.z));
    }

    public static Vector3d sub(Vector3d u, Vector3d v){
        return new Vector3d(u.x-v.x, u.y-v.y, u.z-v.z);
    }

    public static void clamp(Vector4d v, double vmin, double vmax){

        v.x = MathUtil.clamp(v.x,vmin, vmax);
        v.y = MathUtil.clamp(v.y,vmin, vmax);
        v.z = MathUtil.clamp(v.z,vmin, vmax);
        v.w = MathUtil.clamp(v.w,vmin, vmax);
    }

    public static Vector4d addSet(Vector4d u, Vector4d v){
        u.x += v.x;
        u.y += v.y;
        u.z += v.z;
        u.w += v.w;
        return u;
    }

    public static Vector3d addSet(Vector3d u, Vector3d v){
        u.x += v.x;
        u.y += v.y;
        u.z += v.z;
        return u;
    }

    public static double dot(Vector3d u,Vector3d v){
        return u.x*v.x+ u.y*v.y + u.z*v.z;
    }

    //
    // Calculate the reflection of vector v in plane with normal n
    //
    public static Vector3d reflect(Vector3d v, Vector3d n) {
        
        return sub(v, mul(n,2*dot(v,n)));        

    }
    
    public static Vector3d mulVV(Vector3d u, Vector3d v){
        return new Vector3d(u.x*v.x,u.y*v.y,u.z*v.z);
    }

    /**
       calculates vector result = u + t*v;
     */
    static void interpolate(Vector3d u, Vector3d v, double t, Vector3d result){
        result.x = u.x + v.x*t;
        result.y = u.y + v.y*t;
        result.z = u.z + v.z*t;
    }

    public static Vector3d normalize(Vector3d v){
        //double s = 1./sqrt(v.lengthSquared());
        //v.scale(s);
        v.normalize();
        return v;
    }

    public static Vector3d exp(Vector3d v){
        return new Vector3d(Math.exp(v.x),Math.exp(v.y),Math.exp(v.z));
    }

    public static String str(String format, Vector3d v){
        return fmt("["+format+","+format+","+format+"]", v.x,v.y,v.z);
    }

    public static String str(String format, Vector4d v){
        return fmt("["+format+","+format+","+format+","+format+"]", v.x,v.y,v.z,v.w);
    }

    /**
       return transmittance of layer of material 
       transmittanceFactor - vector of transmittance factors for R,G,B light 
       transmittanceFactor.x - thickness of layer of material which refuces transmitted light by factor e
     */
    public static Vector3d getLayerTransmittance(Vector3d transmittanceFactor,double layerThickness){

        Vector3d tr = new Vector3d();
        tr.x = getLayerTransmittanceComponent(transmittanceFactor.x, layerThickness);
        tr.y = getLayerTransmittanceComponent(transmittanceFactor.y, layerThickness);
        tr.z = getLayerTransmittanceComponent(transmittanceFactor.z, layerThickness);

        return tr;
    }
    
    /**
       return transmittance for single component
     */
    public static double getLayerTransmittanceComponent(double transmittanceFactor,double layerThickness){

        if(transmittanceFactor <= EPS){ 
            // material is opaque 
            return 0;
        }
        return Math.exp(-layerThickness/transmittanceFactor);        
    }

    public static boolean isZero(Vector3d v){

        return (abs(v.x) < EPS) && (abs(v.y) < EPS) && (abs(v.z) < EPS);

    }

    static Vector3d vec3(Vector4d v){
        return new Vector3d(v.x, v.y, v.z);
    }

    /**
       conversion of Color into vec4
     */
    static Vector4d vec4(Color c){
        return new Vector4d(c.getr(),c.getg(), c.getb(), c.geta());
    }

    /**
       adding component to Vector3d 
     */
    static Vector4d vec4(Vector3d v, double w){
        return new Vector4d(v.x,v.y,v.z, w);
    }

    /**
       conversiomn of scalar into Vector4d 
     */
    static Vector4d vec4(double v){
        return new Vector4d(v,v,v, v);
    }

    static Vector4d vec4(double x,double y,double z,double w){
        return new Vector4d(x,y,z,w);
    }


} // class VecUtil 
