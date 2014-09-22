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

package abfab3d.util;

import javax.vecmath.Tuple3d;
import javax.vecmath.Matrix3d;


import static abfab3d.util.Output.printf;


/**
 * Wrapper for array of floating point data of arbitrary length 
 * it is used as values holder for both calculation of transformations using VecTransform and calculation of data values using DataSource 
 * @author Vladimir Bulatov
 */
public class Vec {

    static final boolean DEBUG = true;
    
    // holder for data
    public double v[];
    
    private double voxelSize = 1; // size of voxel in the grid in physical units 
    private double scaleFactor = 1;   // accumulated scale factor of all applied transform 

    public Vec(int size){
        v = new double[size];
    }

    public Vec(Vec in){
        v = new double[in.v.length];
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

    public void set(Vec in){

        if(in == this)
            return;

        for(int i=0; i < v.length; i++){
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

    // multiply vector to given matrix from the left 
    public void mulSetLeft(Matrix3d m){

        double x = m.m00 * v[0] + m.m01 * v[1] + m.m02 * v[2];
        double y = m.m10 * v[0] + m.m11 * v[1] + m.m12 * v[2];
        double z = m.m20 * v[0] + m.m21 * v[1] + m.m22 * v[2];
        v[0] = x;
        v[1] = y;
        v[2] = z;
    }

}
