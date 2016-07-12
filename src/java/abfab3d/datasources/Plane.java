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

import abfab3d.core.ResultCodes;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;

import abfab3d.core.Vec;

import static java.lang.Math.abs;

import static abfab3d.core.Output.printf;


import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.step10;
import static abfab3d.core.MathUtil.step01;


/**

   Plane with with given external normal and distance from the origin
   the shape is half space bounded by that plane. 
   plane normal is pointing outside of that half space 

   <embed src="doc-files/Plane.svg" type="image/svg+xml"/> 

   @author Vladimir Bulatov

 */

public class Plane extends TransformableDataSource {
    
    private double m_nx, m_ny, m_nz, m_dist;    

    Vector3dParameter  mp_normal = new Vector3dParameter("normal","plane's external normal",new Vector3d(1,0,0));
    DoubleParameter  mp_dist = new DoubleParameter("dist","distance to plane from origin",0);

    Parameter m_aparam[] = new Parameter[]{
        mp_dist,
        mp_normal,
    };


    public Plane(){
        initParams();
    }

    /**
     * Plane is defined via external normal and distance along normal from origin.
     *
     * @param normal The normal to the plane
     * @param distance The distance to the plane
     */
    public Plane(Vector3d normal, double distance){
        initParams();
        normal.normalize();
        mp_normal.setValue(normal);
        mp_dist.setValue(distance);

    }
    
    /**
     * Plane is defined via external normal and a point, which lies in the plane
     *
     * @param normal The normal to the plane
     * @param pointOnPlane the point on the plane
     */
    public Plane(Vector3d normal, Vector3d pointOnPlane){
        initParams();

        normal.normalize();
        mp_normal.setValue(normal);
        mp_dist.setValue(normal.dot(pointOnPlane));

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
        initParams();

        Vector3d v1 = new Vector3d(pnt1);
        Vector3d v2 = new Vector3d(pnt2);
        v1.sub(pnt0);
        v2.sub(pnt0);
        Vector3d nn = new Vector3d();
        nn.cross(v1, v2);
        nn.normalize();

        mp_normal.setValue(nn);
        mp_dist.setValue(nn.dot(pnt0));

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

        this(new Vector3d(nx, ny, nz), dist);
        
    }


    /**
     * Set the distance to the plane freom the origin
     * @param val The value in meters
     */
    public void setDistance(double val) {
        mp_dist.getValue();
    }

    /**
     * Get the distance to the plane freom the origin
     */
    public double getDistance() {
        return mp_dist.getValue();
    }
    /**
       @noRefGuide
     */
    protected void initParams(){
        super.addParams(m_aparam);
    }

    /**
     * Set the plane normal
     */
    public void setNormal(Vector3d val) {
        mp_normal.setValue(val);
    }
    /**
     * Get the normal
     * @return
     */
    public Vector3d getNormal(){
        return mp_normal.getValue();
    }

    /**
     * @noRefGuide
     */
    public double getDistanceToOrigin(){
        return m_dist;
    }

    /**
     * @noRefGuide
     */
    public int initialize(){

        super.initialize();

        Vector3d normal = mp_normal.getValue();

        m_nx = normal.x;
        m_ny = normal.y;
        m_nz = normal.z;
        m_dist = mp_dist.getValue();

        return ResultCodes.RESULT_OK;

    }

    /**
     * @noRefGuide
     */
    public double getDistance(Vec pnt) {
        
        double v[] = pnt.v;
        double x = v[0];
        double y = v[1];
        double z = v[2];

        return (x * m_nx + y*m_ny + z*m_nz  - m_dist);
        
    }

    /**
     * returns 1 if pnt is inside of half space
     * returns intepolated value on the boundary
     * returns 0 if pnt is outside the half space
     * @noRefGuide
     */
    public int getBaseValue(Vec pnt, Vec data) {
                
        double dist = getDistance(pnt);
        
        data.v[0] = getShapeValue(dist, pnt);

        return ResultCodes.RESULT_OK;        

    }
    
}  // class Plane


