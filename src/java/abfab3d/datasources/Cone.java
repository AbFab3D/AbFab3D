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


import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;

import abfab3d.util.Vec;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import static abfab3d.util.MathUtil.step10;
import static java.lang.Math.*;


/**
 * Cone with point at apex, axis along given direction and given half angle.  This
 * Cone goes infinitely in the axis direction.
   <embed src="doc-files/Cone.svg" type="image/svg+xml"/> 
 *
 * @author Vladimir Bulatov
 */
public class Cone extends TransformableDataSource {


    private Vector3d m_apex;
    private double m_normalY, m_normalR;
    private Matrix3d m_rotation;
    private double m_rounding;

    static final double EPSILON = 1.e-8;

    static final Vector3d Yaxis = new Vector3d(0, 1, 0);

    Vector3dParameter  mp_apex = new Vector3dParameter("apex","Cone apex",new Vector3d(0,0,0));
    Vector3dParameter  mp_axis = new Vector3dParameter("axis","Cone axis",new Vector3d(0,1,0));
    DoubleParameter  mp_angle = new DoubleParameter("angle","Cone angle",Math.PI/4);
    // rounding of the edges
    DoubleParameter  mp_rounding = new DoubleParameter("rounding","rounding of the cone apex", 0.);

    Parameter m_aparam[] = new Parameter[]{
        mp_apex,
        mp_axis,
        mp_angle,
        mp_rounding,
    };

    public Cone() {
        initParams();
    }

    /**
     * Cone with apex at the given center, axis along given direction and given half angle.   This
     * Cone goes infinitely in the axis direction.
     * @param apex Cone apex 
     * @param axis Direction of cone axis 
     * @param angle Cone's half angle
     */
    public Cone(Vector3d apex, Vector3d axis, double angle) {
        initParams();
        setApex(apex);
        setAxis(axis);
        setAngle(angle);
    }

    /**
       set cone apex
     */
    public void setApex(Vector3d apex){
        mp_apex.setValue(apex);
    } 

    /**
       set cone axis
     */
    public void setAxis(Vector3d axis){
        mp_axis.setValue(axis);
    } 

    /**
       set cone angle
     */
    public void setAngle(double angle){
        mp_angle.setValue(angle);
    } 

    /**
       set cone rounding
     */
    public void setRounding(double rounding){
        mp_rounding.setValue(rounding);
    } 

    public Matrix3d getRotation(){
        return m_rotation;
    }

    public double getNormalY(){
        return m_normalY;
    }
    public double getNormalR(){
        return m_normalR;
    }
    public double getRounding(){
        return m_rounding;
    }

    public Vector3d getApex(){
        return m_apex;
    }
    
    /**
     * @noRefGuide
     */
    protected void initParams() {
        super.addParams(m_aparam);
    }

    /**
     * @noRefGuide
     */
    public int initialize() {

        m_rounding = mp_rounding.getValue();
        Vector3d naxis = mp_axis.getValue();
        naxis.normalize();

        // rotation axis 
        Vector3d raxis = new Vector3d();
        raxis.cross(naxis, Yaxis);
        double sina = raxis.length();
        double cosa = Yaxis.dot(naxis);
        double aa = 0;
        if (abs(sina) < EPSILON) {  // we are parallel to Y
            raxis = new Vector3d(1, 0, 0); // axis of rotation orthogonal to the Y
            if (cosa < 0)
                aa = Math.PI;
            else
                aa = 0;
        } else {

            raxis.normalize();
            aa = atan2(sina, cosa);
        }

        m_rotation = new Matrix3d();
        m_rotation.set(new AxisAngle4d(raxis, aa));

        m_apex = mp_apex.getValue();

        double angle = mp_angle.getValue();

        this.m_normalY = -sin(angle);
        this.m_normalR = cos(angle);

        return RESULT_OK;
    }

    public double getDistance(Vec pnt){

        Vec pntc = new Vec(pnt);
        canonicalTransform(pntc);

        double x = pntc.v[0];
        double y = pntc.v[1];
        double z = pntc.v[2];

        // cone is in canonical orientation with apex at origin and axis along positive Y axis
        double r = sqrt(x * x + z * z); // coordinate in orthogonal to Y axis 
        double dist = y * m_normalY + r * m_normalR;

        return dist;
        
    }
    /**
     *
     * @noRefGuide
     *
     * returns 1 if pnt is inside of cone
     * returns interpolated value on the boundary
     * returns 0 if pnt is outside of cone
     */
    public int getDataValue(Vec pnt, Vec data) {

        super.transform(pnt);


        double dist = getDistance(pnt);

        double vs = pnt.getScaledVoxelSize();
        data.v[0] = step10(dist, 0., vs);

        super.getMaterialDataValue(pnt, data);        
        return RESULT_OK;        

    }

    /**
     *  move cone into canonical position with apex at origin and axis aligned with Y-axis

     @noRefGuide     
     */
    protected void canonicalTransform(Vec pnt) {
        pnt.subSet(m_apex);
        pnt.mulSetLeft(m_rotation);
    }

}  // class Cone


