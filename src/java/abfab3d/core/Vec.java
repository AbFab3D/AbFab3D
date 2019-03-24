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

package abfab3d.core;

import javax.vecmath.Tuple3d;
import javax.vecmath.Matrix3d;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;


/**
 * Wrapper for array of floating point data of arbitrary length 
 * it is used as values holder for both calculation of transformations using VecTransform and calculation of data values using DataSource 
 * @author Vladimir Bulatov
 */
public class Vec {

    static final boolean DEBUG = true;
    
    // holder for data
    public double v[];    
    public double voxelSize = 1; // size of voxel in the grid in physical units 
    public double scaleFactor = 1;   // accumulated scale factor of all applied transform 
    public int materialIndex = 0;

    public Vec(int size){
        this.v = new double[size];
    }

    public Vec(double x, double y){
        this.v = new double[]{x,y};        
    }

    public Vec(double x, double y, double z){
        this.v = new double[]{x,y,z};        
    }

    public Vec(double x, double y, double z, double u){
        this.v = new double[]{x,y,z, u};        
    }

    public Vec(double x, double y, double z, double u, double v){
        this.v = new double[]{x,y,z, u, v};        
    }

    public Vec(double x, double y, double z, double u, double v, double w){
        this.v = new double[]{x,y,z, u, v, w};        
    }

    public Vec(Tuple3d p){
        this.v = new double[]{p.x,p.y,p.z};        
    }

    public Vec(Vec in){
        this.v = new double[in.v.length];
        set(in);
    }
    
    public final double getVoxelSize(){
        return voxelSize;
    }

    public final void setVoxelSize(double value){
        voxelSize = value;
    }

    public final double getScaleFactor(){
        return scaleFactor;
    }
    public final void setScaleFactor(double value){
        scaleFactor = value;
    }

    public final double getScaledVoxelSize(){

        return voxelSize*scaleFactor;

    }

    
    public final void mulScale(double value){ 

        scaleFactor *= value;

    }

    public void set(double x, double y, double z){
        v[0] = x;
        v[1] = y;
        v[2] = z;       
    }

    public void set(double x, double y, double z, double w){
        v[0] = x;
        v[1] = y;
        v[2] = z;       
        v[3] = w;       
    }

    public void set(Tuple3d t){
        v[0] = t.x;
        v[1] = t.y;
        v[2] = t.z;       
    }

    public void get(Tuple3d t){
        t.x = v[0];
        t.y = v[1];
        t.z = v[2];       
    }

    public final void get(double data[]){
        System.arraycopy(v, 0, data, 0,v.length); 
    }

    public final void get(double data[], int offset){
        System.arraycopy(v, 0, data, offset,v.length); 
    }

    public final void get(double data[], int offset, int count){
        System.arraycopy(v, 0, data, offset,count); 
    }

    public final void set(double data[], int offset){
        System.arraycopy(data, offset, v, 0, v.length); 
    }

    public void set(Vec in){

        if(in == this)
            return;
        int len = Math.min(v.length, in.v.length);
        
        for(int i=0; i < len; i++){
            v[i] = in.v[i];
        }
        voxelSize = in.voxelSize;
        scaleFactor = in.scaleFactor;
    }

    public void subSet(Tuple3d a){
        v[0] -= a.x;
        v[1] -= a.y;
        v[2] -= a.z;

    }

    public void addSet(Tuple3d a){
        v[0] += a.x;
        v[1] += a.y;
        v[2] += a.z;        
    }

    public void addSet(double a[]){
        int n  = Math.min(v.length, a.length);
        for(int i = 0; i < n; i++){
            v[i] += a[i];
        }
    }

    public void clamp(double x0, double x1){

        double n = v.length;

        for(int i = 0; i < n; i++){
            v[i] = MathUtil.clamp(v[i], x0, x1);
        }
    }

    public double dot(Tuple3d a){
        return v[0]*a.x +  v[1]*a.y+  v[2]*a.z;
    }

    /**
       makes linear interpolation between u, and v using param t and places result in res
     */
    public static void lerp(Vec u, Vec w, double t, Vec res){

        MathUtil.lerp(u.v, w.v, t, res.v);

    }

    // multiply vector to given matrix from the left 
    public void mulSetLeft(Matrix3d m){

        double x = m.m00 * v[0] + m.m01 * v[1] + m.m02 * v[2];
        double y = m.m10 * v[0] + m.m11 * v[1] + m.m12 * v[2];
        double z = m.m20 * v[0] + m.m21 * v[1] + m.m22 * v[2];
        v[0] = x;
        v[1] = y;
        v[2] = z;
    }


    public static String toString(Vec v) {
        StringBuilder sb = new StringBuilder();

        sb.append("Vec: ");
        for(int i=0; i < v.v.length; i++) {
            sb.append(fmt("%8.4f",v.v[i]));
            sb.append(" ");
        }
        return sb.toString();
    }


}
