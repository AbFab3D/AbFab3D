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

import static abfab3d.util.Output.printf;


import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.intervalCap;
import static abfab3d.util.MathUtil.step10;
import static abfab3d.util.MathUtil.step01;

import static abfab3d.util.Units.MM;


/**

   Plane with with given external normal and distance from the origin
   the shape is half space bounded by that plane. 
   plane normal is pointing outside of that half space 

   @author Vladimir Bulatov

 */

public class Plane extends TransformableDataSource {
    
    private double nx, ny, nz, dist;
    
    public Plane(){
        this(1.,0.,0.,0);
    }

    public Plane(Vector3d normal, double dist){
        init(normal.x,normal.y,normal.z, dist);
    }
    
    public Plane(Vector3d normal, Vector3d pointOnPlane){

        Vector3d nn = new Vector3d(normal);
        nn.normalize();

        init(nn.x, nn.y, nn.z, nn.dot(pointOnPlane));
    }

    public Plane(Vector3d pnt0, Vector3d pnt1, Vector3d pnt2 ){

        Vector3d v1 = new Vector3d(pnt1);
        Vector3d v2 = new Vector3d(pnt2);
        v1.sub(pnt0);
        v2.sub(pnt0);
        Vector3d nn = new Vector3d();
        nn.cross(v1, v2);
        nn.normalize();

        init(nn.x, nn.y, nn.z, nn.dot(pnt0));
    }

    public Plane(double nx, double ny, double nz, double dist){

        init(nx, ny, nz, dist);

        
    }

    void init(double nx, double ny, double nz, double dist){

        // normalize normal 
        double n = sqrt(nx * nx + ny*ny + nz*nz);
        if(n == 0.0){
            this.nx = 1;
            this.ny = 0;
            this.nz = 0;           
        } else {
            this.nx = nx/n;
            this.ny = ny/n;
            this.nz = nz/n;             
        }
        this.dist = dist;
    }

    /**
     * returns 1 if pnt is inside of half space
     * returns intepolated value on the boundary
     * returns 0 if pnt is outside the half space
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);
        
        double v[] = pnt.v;
        double x = v[0];
        double y = v[1];
        double z = v[2];
        double vs = pnt.getScaledVoxelSize();
        
        double dot = x * nx + y*ny + z*nz;
        
        data.v[0] = step10(dot, this.dist, vs);
        
        return RESULT_OK;
    }
    
}  // class Plane


