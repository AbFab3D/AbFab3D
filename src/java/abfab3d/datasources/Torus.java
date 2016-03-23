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
import abfab3d.param.Vector3dParameter;
import abfab3d.param.Parameter;
import abfab3d.util.Bounds;
import abfab3d.util.Vec;
import static java.lang.Math.sqrt;
import static abfab3d.util.MathUtil.step10;

import static abfab3d.util.Units.MM;


/**

   Torus centered at the given point with given axis

   <embed src="doc-files/Torus.svg" type="image/svg+xml"/> 

   @author Vladimir Bulatov

 */

public class Torus extends TransformableDataSource {
    
    private double R, r;
    private double x0, y0, z0;
    private double ax, ay, az;

    Vector3dParameter mp_center = new Vector3dParameter("center","Center of torus",new Vector3d(0,0,0));
    Vector3dParameter mp_axis = new Vector3dParameter("axis","Axis of the torus",new Vector3d(0,0,1));

    private DoubleParameter  mp_rin = new DoubleParameter("rin","Radius of the torus tube", 1.*MM);
    private DoubleParameter  mp_rout = new DoubleParameter("rout","Radius of the torus spine", 5.*MM);

    Parameter m_aparam[] = new Parameter[]{
        mp_center,
        mp_axis,
        mp_rout,
        mp_rin,
    };

    /**
       creates default torus centered at origin 
       rout = 5mm 
       rin 1mm 
     */
    public Torus() {

    }

    /**
       torus centered ar center with axis parallel to z-axis
       @param center - location of torus center
       @param Rout - outer radius of torus
       @param Rin - inner radius of torus

     */
    public Torus(Vector3d center, Vector3d axis, double Rout, double Rin) {
        super.addParams(m_aparam);
        mp_center.setValue(center);
        mp_axis.setValue(axis);
        mp_rin.setValue(Rin);
        mp_rout.setValue(Rout);
    }

    /**
       torus centered ar center with axis parallel to z-axis
       @param center - location of torus center
       @param Rout - outer radius of torus
       @param Rin - inner radius of torus

     */
    public Torus(Vector3d center, double Rout, double Rin) {
        this(center,new Vector3d(0,0,1),Rout,Rin);
    }

    /**
       torus centered at origin with axis parallel to z-axis
       @param Rout - outer radius of torus
       @param Rin - innter radius of torus

     */
    public Torus(double Rout, double Rin){
        this(new Vector3d(0, 0, 0), Rout, Rin);
    }

    /**
       @param cx - x component of center
       @param cy - y component of center
       @param cz - z component of center
       @param Rout - outer radius of torus
       @param Rin - inner radius of torus

     */
    public Torus(double cx, double cy, double cz, double Rout, double Rin){
        this(new Vector3d(cx,cy,cz),Rout, Rin);
    }

    /**
     * Set the center of the torus
     * @param val The value in meters
     */
    public void setCenter(Vector3d val) {
        mp_center.setValue(val);
    }

    /**
     * Set the center of the coordinate system
     */
    public void setCenter(double x,double y, double z) {
        mp_center.setValue(new Vector3d(x,y,z));
    }

    /**
     * Get the center of the torus
     * @return The value in meters
     */
    public Vector3d getCenter() {
        return new Vector3d(mp_center.getValue());
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
        Vector3d c = mp_center.getValue();

        m_bounds = new Bounds(c.x - r,c.x + r,c.y - r,c.y + r,c.z - rin, c.z + rin);
        boundsDirty = false;
    }

    /**
     * @noRefGuide
     */
    public int initialize() {

        super.initialize();

        R = mp_rout.getValue();
        r = mp_rin.getValue();
        Vector3d c = mp_center.getValue();
        x0 = c.x;
        y0 = c.y;
        z0 = c.z;

        Vector3d a = mp_axis.getValue();
        a.normalize();

        ax = a.x;
        ay = a.y;
        az = a.z;

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

        double u =  x*ax + y*ay + z*az; // projection on axis 

        // coord of point in orthogonal plane 
        double 
            ppx = x - u*ax,
            ppy = y - u*ay,
            ppz = z - u*az;       

        double v = Math.sqrt(ppx*ppx + ppy*ppy +ppz*ppz); // dist of point to axis 
        v -= R;
        // distance to torus surface 
        double dist = Math.sqrt(v*v + u*u) - r;

        // convert to density 
        data.v[0] = step10(dist, 0, pnt.getScaledVoxelSize());

        //double rxy = sqrt(x*x + y*y) - R;        
        //data.v[0] = step10(((rxy*rxy + z*z) - r*r)/(2*r), 0, pnt.getScaledVoxelSize());
        
        super.getMaterialDataValue(pnt, data);        
        return RESULT_OK;
    }
}  // class Torus

