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

/**
 * Wrapper for arbitrary vector of floating point data array
 *
 * @author Vladimir Bulatov
 */
public class Vec {
    
    // holder for data
    public double v[];
    public double voxelSize;

    public Vec(int size){
        v = new double[size];
    }

    public Vec(Vec in){
        v = new double[in.v.length];
        set(in);
    }

    public void set(double x, double y, double z){
        v[0] = x;
        v[1] = y;
        v[2] = z;       
    }

    public void set(Vec in){
        for(int i=0; i < v.length; i++){
            v[i] = in.v[i];
        }
    }
}
