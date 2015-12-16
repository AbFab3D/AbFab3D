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

import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.util.Bounds;
import abfab3d.util.Vec;

import static abfab3d.util.Output.printf;

import static abfab3d.util.MathUtil.step10;
import static abfab3d.util.MathUtil.step01;

import static abfab3d.util.Units.CM;
import static abfab3d.util.Units.MM;


/**

   Sphere with given location and radius. 
   <br/>
   If radius is positive the shape is interior of the sphere, if radius is negative - the shape is exterior of the sphere.  

   <embed src="doc-files/Sphere.svg" type="image/svg+xml"/> 
   

   @author Vladimir Bulatov

 */

public class Sphere extends TransformableDataSource {
    
    DoubleParameter mp_centerX = new DoubleParameter("centerX","Center X",0);
    DoubleParameter mp_centerY = new DoubleParameter("centerY","Center Y",0);
    DoubleParameter mp_centerZ = new DoubleParameter("centerZ","Center Z",0);
    private DoubleParameter  mp_radius = new DoubleParameter("radius","radius of the sphere", 1.*CM);

    private double R, R2, RR;
    private double x0,y0,z0;
    private boolean sign = true; // inside (true) or outside (false) of the sphere

    Parameter m_aparam[] = new Parameter[]{
        mp_centerX,mp_centerY,mp_centerZ,
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
        mp_centerX.setValue(val.x);
        mp_centerY.setValue(val.y);
        mp_centerZ.setValue(val.z);
        boundsDirty = true;
    }

    /**
     * Set the center of the coordinate system
     */
    public void setCenter(double x,double y, double z) {
        mp_centerX.setValue(x);
        mp_centerY.setValue(y);
        mp_centerZ.setValue(z);
        boundsDirty = true;
    }

    /**
     * Set the center x position
     * @param val The value in meters
     */
    public void setCenterX(double val) {
        mp_centerX.setValue(val);
        boundsDirty = true;
    }

    /**
     * Get the center x position
     * @return The value in meters
     */
    public double getCenterX() {
        return mp_centerX.getValue();
    }

    /**
     * Set the center y position
     * @param val The value in meters
     */
    public void setCenterY(double val) {
        mp_centerY.setValue(val);
        boundsDirty = true;
    }

    /**
     * Get the center y position
     * @return The value in meters
     */
    public double getCenterY() {
        return mp_centerY.getValue();
    }

    /**
     * Set the center z position
     * @param val The value in meters
     */
    public void setCenterZ(double val) {
        mp_centerZ.setValue(val);
        boundsDirty = true;
    }

    /**
     * Get the center z position
     * @return The value in meters
     */
    public double getCenterZ() {
        return mp_centerY.getValue();
    }

    /**
     * Get the center of the coordinate system.
     * @return The value in meters
     */
    public Vector3d getCenter() {
        return new Vector3d(mp_centerX.getValue(),mp_centerY.getValue(),mp_centerZ.getValue());
    }
    
    /**
     * Set the radius
     * @param r The value in meters.  Default is 1mm.
     */
    public void setRadius(double r){
        mp_radius.setValue(r);
        boundsDirty = true;
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
            sign = false;
        } else {
            R = r;
            sign = true;
        }

        R2 = 2*r;
        RR = r*r;

        x0 = mp_centerX.getValue();
        y0 = mp_centerY.getValue();
        z0 = mp_centerZ.getValue();

        return RESULT_OK;
    }

    /**
     * Call to update bounds after each param change that affects bounds
     * @noRefGuide;
     */
    protected void updateBounds() {
        double r = mp_radius.getValue();
        double centerX = mp_centerX.getValue();
        double centerY = mp_centerY.getValue();
        double centerZ = mp_centerZ.getValue();

        m_bounds = new Bounds(centerX - r,centerX + r,centerY - r,centerY + r,centerZ - r,centerZ + r);
        boundsDirty = false;
    }

    /**
     * @noRefGuide

     * returns 1 if pnt is inside of ball
     * returns intepolated value if poiunt is within voxel size to the boundary
     * returns 0 if pnt is outside the ball
     *
     * @noRefGuide
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);
        
        double res = 1.;
        double
            x = pnt.v[0]-x0,
            y = pnt.v[1]-y0,
            z = pnt.v[2]-z0;
        
        double vs = pnt.getScaledVoxelSize();
        
        //double rv = (R); // add slight growing with voxel size ? 
            
        // good approximation to the distance to the surface of the ball).x                             
        //double dist = ((x*x + y*y + z*z) - rv*rv)/(2*rv);
        double r = Math.sqrt(x*x + y*y + z*z);//)/(R2);
        if(sign){
            data.v[0] = step10(r, this.R, vs);
        } else {
            data.v[0] = step01(r, this.R, vs);
        }
        super.getMaterialDataValue(pnt, data);
        return RESULT_OK;
    }
    
}  // class Sphere

