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

   <embed src="doc-files/Plane.svg" type="image/svg+xml"/> 

   @author Vladimir Bulatov

 */

public class Plane extends TransformableDataSource {
    
    private double nx, ny, nz, dist;
    
    public Plane(){
        this(1.,0.,0.,0);
    }

    /**
     * Plane is defined via external normal and distance along normal from origin.
     *
     * @param normal The normal to the plane
     * @param dist The distance to the plane
     */
    public Plane(Vector3d normal, double dist){
        init(normal.x,normal.y,normal.z, dist);
    }
    
    /**
     * Plane is defined via external normal and a point, which lies in the plane
     *
     * @param normal The normal to the plane
     * @param pointOnPlane the point on the plane
     */
    public Plane(Vector3d normal, Vector3d pointOnPlane){

        Vector3d nn = new Vector3d(normal);
        nn.normalize();

        init(nn.x, nn.y, nn.z, nn.dot(pointOnPlane));
    }

    /**
     * Plane is defined via 3 points, which lie in the plane. 
     External normal points into direction from which points pnt0, pnt1, pnt2 look oriented counter clock wise
     *
     * @param pnt0 point in the plane
     * @param pnt1 point in the plane
     * @param pnt2 point in the plane
     */
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

    /**
     * Plane is defined via components of normal and distance from origin
     *
     * @param nx x component of normal 
     * @param ny y component of normal 
     * @param nz z component of normal 

     * @param dist distance from plane to origin
     */
    public Plane(double nx, double ny, double nz, double dist){

        init(nx, ny, nz, dist);

        
    }

    /**

       @noRefGuide

     */
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
     @noRefGuide
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


