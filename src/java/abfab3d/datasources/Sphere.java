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

package abfab3d.datasources;


import javax.vecmath.Vector3d;

import abfab3d.core.ResultCodes;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.core.Bounds;
import abfab3d.core.Vec;

import static abfab3d.core.Output.printf;

import static abfab3d.core.MathUtil.step10;
import static abfab3d.core.MathUtil.step01;

import static abfab3d.core.Units.MM;


/**

   Sphere with given location and radius. 
   <br/>
   If radius is positive the shape is interior of the sphere, if radius is negative - the shape is exterior of the sphere.  

   <embed src="doc-files/Sphere.svg" type="image/svg+xml"/> 
   

   @author Vladimir Bulatov

 */

public class Sphere extends TransformableDataSource {
    

    Vector3dParameter mp_center = new Vector3dParameter("center","Center",new Vector3d(0,0,0));
    private DoubleParameter  mp_radius = new DoubleParameter("radius","radius of the sphere", 1.*MM);

    private double R; // radius 
    private double x0,y0,z0; // center 
    private double sign = 1; // 1- inside of sphere, -1 - outside of sphere

    Parameter m_aparam[] = new Parameter[]{
        mp_center,
        mp_radius
    };


    /**
     * @noRefGuide
     */
    public Sphere(){
        
        this(0.,0.,0.,1*MM);
    }

    /**
     * sphere with given radius centered at origin
     */
    public Sphere(double radius){

        this(0., 0., 0., radius);
    }
    

    /**
     * sphere with given center and radius
     */
    public Sphere(Vector3d center, double radius){
        this(center.x, center.y, center.z, radius);
    }
    
    /**
     * sphere with given center and radius
     */
    public Sphere(double cx, double cy, double cz, double radius){

        initParams();

        setCenter(cx, cy, cz);
        setRadius(radius);
    }

    /**
     * @noRefGuide;
     */
    protected void initParams(){
        super.addParams(m_aparam);
    }

    /**
     * Set the center of the coordinate system
     * @param val The value in meters
     */
    public void setCenter(Vector3d val) {
        setCenter(val.x,val.y,val.z);

    }

    /**
     * Set the center of the sphere
     */
    public void setCenter(double x,double y, double z) {

        mp_center.setValue(new Vector3d(x,y,z));

    }
    /**
     * Get the center of the sphere
     * @return The value in meters
     */
    public Vector3d getCenter() {
        return new Vector3d(mp_center.getValue());
    }
    
    /**
     * Set the radius of the sphere
     * @param r The value in meters.  Default is 1mm.
     */
    public void setRadius(double r){
        mp_radius.setValue(r);
    }

    /**
     * Get the radius
     */
    public double getRadius() {
        return mp_radius.getValue();
    }

    /**
     * @noRefGuide
     */
    public int initialize() {

        super.initialize();
        
        double r = mp_radius.getValue();

        if( r < 0){
            R = -r;
            sign = -1;
        } else {
            R = r;
            sign = 1;
        }

        Vector3d c = mp_center.getValue();
        x0 = c.x;
        y0 = c.y;
        z0 = c.z;

        //updateBounds();

        return ResultCodes.RESULT_OK;
    }

    /**
     * Call to update bounds after each param change that affects bounds
     * @noRefGuide;
     */
    protected void updateBounds() {
        double r = mp_radius.getValue();

        Vector3d c = mp_center.getValue();
        double centerX = c.x;
        double centerY = c.y;
        double centerZ = c.z;

        m_bounds = new Bounds(centerX - r,centerX + r,centerY - r,centerY + r,centerZ - r,centerZ + r);
    }

    /**
     * @noRefGuide

     * returns 1 if pnt is inside of ball
     * returns intepolated value if poiunt is within voxel size to the boundary
     * returns 0 if pnt is outside the ball
     *
     * @noRefGuide
     */
    public final int getBaseValue(Vec pnt, Vec data) {
        
        double v[] = pnt.v;
        double 
            x = v[0] - x0,
            y = v[1] - y0,
            z = v[2] - z0;
        double dist = sign*(Math.sqrt(x*x + y*y + z*z)-R);
        
        data.v[0] = getShapeValue(dist, pnt);

        return ResultCodes.RESULT_OK;        
        
    }
    
}  // class Sphere

