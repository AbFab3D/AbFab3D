/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.datasources;


import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;
import javax.vecmath.AxisAngle4d;


import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;

import abfab3d.util.PointToTriangleDistance;

import static java.lang.Math.sqrt;
import static java.lang.Math.atan2;
import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.PI;

import static abfab3d.util.Output.printf;


import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.intervalCap;
import static abfab3d.util.MathUtil.step10;
import static abfab3d.util.MathUtil.step01;

import static abfab3d.util.Units.MM;


/**

   Cone with poiunt at given center, axis along given direction and given half angle 

   @author Vladimir Bulatov

 */

public class Cone extends TransformableDataSource {
    

    double cx, cy, cz;
    double nx, nw; // components of normal to surface 
    Matrix3d rotation;
    
    static final double EPSILON = 1.e-8;

    static final Vector3d Xaxis = new Vector3d(1,0,0);
    
    public Cone(){
        this(new Vector3d(0,0,0), new Vector3d(1,0,0), PI/4);
    }

    public Cone(Vector3d center, Vector3d axis, double angle){

        init(center, axis, angle);

    }
    
    void init(Vector3d center, Vector3d axis, double angle){
        
        Vector3d naxis = new Vector3d(axis);
        naxis.normalize();
        
        // rotation axis 
        Vector3d raxis = new Vector3d();
        raxis.cross(naxis, Xaxis); 
        double sina = raxis.length();
        double cosa = Xaxis.dot(naxis);
        double aa = 0;
        if(abs(sina) < EPSILON) {  // we are parallel to X 
            raxis = new Vector3d(0,1,0); // axis of rotation orthogonal to the X
            if(cosa < 0) 
                aa = Math.PI;
            else 
                aa = 0;
        } else {

            raxis.normalize();
            aa = atan2(sina, cosa);
        }
        
        rotation = new Matrix3d();
        rotation.set(new AxisAngle4d(raxis, aa));        
        
        this.cx = center.x;
        this.cy = center.y;
        this.cz = center.z;
        
        this.nx = -sin(angle);
        this.nw = cos(angle);
        

    }

    /**
     * returns 1 if pnt is inside of cone
     * returns intepolated value on the boundary
     * returns 0 if pnt is outside of cone
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);
        
        double v[] = pnt.v;
        double x = v[0];
        double y = v[1];
        double z = v[2];
        double vs = pnt.getScaledVoxelSize();
        
        Vector3d p = new Vector3d(x-cx,y-cy,z-cz);
        rotation.transform(p);

        x = p.x;
        y = p.y;
        z = p.z;

        // cone is in canonical orientation along positive X axis
        double w = sqrt(y*y + z*z); // coordinate in direction 
        double dist = x*nx + w*nw;
        // TODO better handling of 
        data.v[0] = step10(dist, 0., vs);
        
        return RESULT_OK;
    }
    
}  // class Cone


