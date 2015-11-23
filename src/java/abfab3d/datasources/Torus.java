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

    private Vector3dParameter  mp_center = new Vector3dParameter("center","center of the sphere",new Vector3d(0,0,0));
    private DoubleParameter  mp_r = new DoubleParameter("rin","radius of the torus tube", 1.*MM);
    private DoubleParameter  mp_R = new DoubleParameter("rout","radius of the torus spine", 5.*MM);

    Parameter m_aparam[] = new Parameter[]{
        mp_center,
        mp_R,
        mp_r,
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
        setCenter(new Vector3d(cx,cy,cz));
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
     * @param val The center
     */
    public void setCenter(Vector3d val) {
        mp_center.setValue(val);
    }

    /**
     * Get the center of the coordinate system
     * @return
     */
    public Vector3d getCenter() {
        return mp_center.getValue();
    }

    /**
     * @noRefGuide
     */
    public void setRout(double value){

        mp_R.setValue(value);
        this.R = value;

    }

    /**
     * @noRefGuide
     */
    public void setRin(double value){
        this.r = value;
        mp_r.setValue(value);
    }

    /**
     * returns 1 if pnt is inside of Torus
     * returns intepolated value if point is within voxel size distance from the boundary
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

