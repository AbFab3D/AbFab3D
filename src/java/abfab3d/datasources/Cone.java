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


import abfab3d.core.ResultCodes;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;

import abfab3d.core.Vec;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import static abfab3d.core.Output.printf;
import static abfab3d.core.MathUtil.step10;
import static abfab3d.core.MathUtil.blendMax;
import static java.lang.Math.*;


/**
 * Cone with point at apex, axis along given direction and given half angle.  This
 * Cone goes infinitely in the axis direction.
   <embed src="doc-files/Cone.svg" type="image/svg+xml"/> 
 *
 * @author Vladimir Bulatov
 */
public class Cone extends TransformableDataSource {



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
     * Get the cone apex
     * @return
     */
    public Vector3d getApex() {
        return mp_apex.getValue();
    }

    /**
       set cone axis
     */
    public void setAxis(Vector3d axis){
        mp_axis.setValue(axis);
    }

    /**
     * Get the cone axis
     * @return
     */
    public Vector3d getAxis() {
        return mp_axis.getValue();
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

    /**
     * @noRefGuide
     * @return
     */
    public double getNormalA(){
        return m_normalA;
    }

    /**
     * @noRefGuide
     * @return
     */
    public double getNormalO(){
        return m_normalO;
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
        m_ax = naxis.x;
        m_ay = naxis.y;
        m_az = naxis.z;

        Vector3d apex = mp_apex.getValue();
        m_apx = apex.x;
        m_apy = apex.y;
        m_apz = apex.z;

        double angle = mp_angle.getValue();

        this.m_normalA = -sin(angle);
        this.m_normalO = cos(angle);
        printf("m_normalA: %7.5f m_normalO: %7.5f\n", m_normalA, m_normalO);

        return ResultCodes.RESULT_OK;
    }

    private double m_apx, m_apy, m_apz; // apex location 
    private double m_ax, m_ay, m_az; // cone axis 

    // 
    private double 
        m_normalA, // side normal projection on axis
        m_normalO; // side normal projection on orthogonal plane

    private double m_rounding;

    public double getDistanceValue(Vec pnt, Vec data){

        if(false)printf("pnt: %7.5f %7.5f %7.5f\n", pnt.v[0],pnt.v[1],pnt.v[2]);

        double x = pnt.v[0] - m_apx;
        double y = pnt.v[1] - m_apy;
        double z = pnt.v[2] - m_apz;
        if(false)printf("xyz: %7.5f %7.5f %7.5f\n", x, y, z);
        // projection to axis
        double pa = x*m_ax + y*m_ay + z*m_az; 
        
        // projection to orthogonal plane 
        double po = sqrt(max(0,x*x + y*y + z*z - pa*pa));
        if(false)printf("pa: %7.5f po: %7.5f\n", pa, po);

        double dist1 = pa * m_normalA + po * m_normalO;
        double dist2 = pa * m_normalA - po * m_normalO;
        if(false)printf("dist1: %7.5f dist2: %7.5f\n", dist1, dist2);
        
        data.v[0] = blendMax(dist1, dist2, m_rounding);
        return ResultCodes.RESULT_OK;
        
    }
    /**
     *
     * @noRefGuide
     *
     * returns 1 if pnt is inside of cone
     * returns interpolated value on the boundary
     * returns 0 if pnt is outside of cone
     */
    public int getBaseValue(Vec pnt, Vec data) {

        getDistanceValue(pnt, data);
        data.v[0] = getShapeValue(data.v[0], pnt);
        return ResultCodes.RESULT_OK;
    }

}  // class Cone


