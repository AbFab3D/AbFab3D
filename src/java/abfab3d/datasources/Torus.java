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
import abfab3d.util.Bounds;
import abfab3d.util.Vec;
import static java.lang.Math.sqrt;
import static abfab3d.util.MathUtil.step10;

import static abfab3d.util.Units.MM;


/**

   Torus centered at the given point with axis parallel to z-axis

   <embed src="doc-files/Torus.svg" type="image/svg+xml"/> 

   @author Vladimir Bulatov

 */

public class Torus extends TransformableDataSource {
    
    private double R, r;
    private double x0, y0, z0;

    DoubleParameter mp_centerX = new DoubleParameter("centerX","Center X",0);
    DoubleParameter mp_centerY = new DoubleParameter("centerY","Center Y",0);
    DoubleParameter mp_centerZ = new DoubleParameter("centerZ","Center Z",0);

    private DoubleParameter  mp_rin = new DoubleParameter("rin","Radius of the torus tube", 1.*MM);
    private DoubleParameter  mp_rout = new DoubleParameter("rout","Radius of the torus spine", 5.*MM);

    Parameter m_aparam[] = new Parameter[]{
        mp_centerX,mp_centerY,mp_centerZ,
        mp_rout,
        mp_rin,
    };

    /**
       @param center - location of torus center
       @param Rout - outer radius of torus
       @param Rin - inner radius of torus

     */
    public Torus(Vector3d center, double Rout, double Rin) {
        this(center.x,center.y,center.z,Rout,Rin);
    }

    /**
       torus centered at origin
       @param Rout - outer radius of torus
       @param Rin - innter radius of torus

     */
    public Torus(double Rout, double Rin){
        this(0, 0, 0, Rout, Rin);
    }

    /**
       @param cx - x component of center
       @param cy - y component of center
       @param cz - z component of center
       @param Rout - outer radius of torus
       @param Rin - inner radius of torus

     */
    public Torus(double cx, double cy, double cz, double Rout, double Rin){
        initParams();
        setCenter(cx,cy,cz);
        setRout(Rout);
        setRin(Rin);
    }

    /**
     * @noRefGuide
     */
    protected void initParams() {
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
     * Set the spine radius
     */
    public void setRout(double value){
        mp_rout.setValue(value);
        boundsDirty = true;
    }

    /**
     * Get the spine radius
     * @return The value in meters
     */
    public double getRout() {
        return mp_rout.getValue();
    }

    /**
     * Set the radius of the torus tube
     */
    public void setRin(double value){
        mp_rin.setValue(value);
    }

    /**
     * Get the radius of the torus tube
     * @return The value in meters
     */
    public double getRin() {
        return mp_rin.getValue();
    }

    /**
     * Call to update bounds after each param change that affects bounds
     * @noRefGuide;
     */
    protected void updateBounds() {
        double rout = mp_rout.getValue();
        double rin = mp_rout.getValue();

        double r = rout + rin;
        double centerX = mp_centerX.getValue();
        double centerY = mp_centerY.getValue();
        double centerZ = mp_centerZ.getValue();

        m_bounds = new Bounds(centerX - r,centerX + r,centerY - r,centerY + r,centerZ - r,centerZ + r);
        boundsDirty = false;
    }

    /**
     * @noRefGuide
     */
    public int initialize() {

        super.initialize();

        R = mp_rout.getValue();
        r = mp_rin.getValue();

        x0 = mp_centerX.getValue();
        y0 = mp_centerY.getValue();
        z0 = mp_centerZ.getValue();

        return RESULT_OK;
    }

    /**
     * returns 1 if pnt is inside of Torus
     * returns interpolated value if point is within voxel size distance from the boundary
     * returns 0 if pnt is outside the Torus
       @noRefGuide
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);

        double
                x = pnt.v[0] - x0,
                y = pnt.v[1] - y0,
                z = pnt.v[2] - z0;
        
        double rxy = sqrt(x*x + y*y) - R;
        
        data.v[0] = step10(((rxy*rxy + z*z) - r*r)/(2*r), 0, pnt.getScaledVoxelSize());
        
        super.getMaterialDataValue(pnt, data);        
        return RESULT_OK;
    }
}  // class Torus

