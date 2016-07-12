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


import abfab3d.core.ResultCodes;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.core.Vec;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import static abfab3d.core.MathUtil.step10;
import static java.lang.Math.*;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;
import static abfab3d.core.MathUtil.blendMax;


/**
 * Cylinder with given ends and radius
 *
   <embed src="doc-files/Cylinder.svg" type="image/svg+xml"/> 
 * @author Vladimir Bulatov
 */
public class Cylinder extends TransformableDataSource {

    static final double EPSILON = 1.e-8;

    //private double m_h2; // cylnder's half height of
    //private double m_scaleFactor = 0;
    //private Vector3d m_center;
    //private Matrix3d m_rotation;
    //private Vector3d m_v0, m_v1;
    // params for non uniform cylinder 
    private double m_R01, m_normalO, m_normalA;

    static final Vector3d Yaxis = new Vector3d(0, 1, 0);


    Vector3dParameter  mp_v0 = new Vector3dParameter("v0","vertex 0",new Vector3d(0,-10*MM,0));
    Vector3dParameter  mp_v1 = new Vector3dParameter("v1","vertex 1",new Vector3d(0,10*MM,0));
    DoubleParameter  mp_r0 = new DoubleParameter("r0","radius at vertex 0",5*MM);
    DoubleParameter  mp_r1 = new DoubleParameter("r1","radius at vertex 1",5*MM);
    // rounding of the edges
    DoubleParameter  mp_rounding = new DoubleParameter("rounding","rounding of the cylinder edges", 0.);

    Parameter m_aparam[] = new Parameter[]{
        mp_v0,
        mp_v1,
        mp_r0,
        mp_r1,
        mp_rounding,
    };

    private double m_centerX,m_centerY,m_centerZ,m_halfHeight, m_height, m_r0, m_r1, m_ax, m_ay, m_az, m_rounding; 

    /**
     * Circular cylinder of uniform radius
     * @param v0 center of first base
     * @param v1 center of second base
     * @param radius Radius of cylinder 
     */
    public Cylinder(Vector3d v0, Vector3d v1, double radius) {       
        this(v0,v1,radius,radius);
    }

    /**
     * Circular cylinder of variable radius from point v0 to point v1
     * @param v0 center of first base
     * @param v1 center of second base
     * @param radius0 radius of base centered at v0
     * @param radius1 radius of base centered at v1
     */
    public Cylinder(Vector3d v0, Vector3d v1, double radius0, double radius1  ) {

        if (radius0 < 0 || radius1 < 0) {
            throw new IllegalArgumentException(fmt("Cylinder radius < 0.  radius0: %15.8g radius1: %15.8g ",radius0,radius1));
        }

        initParams();

        setRadius0(radius0);
        setRadius1(radius1);
        setV0(v0);
        setV1(v1);
    }

    /**
     * @noRefGuide
     */
    protected void initParams() {
        super.addParams(m_aparam);
    }

    /**
     * The the radius of vertex0
     */
    public void setRadius0(double r){
        mp_r0.setValue(r);
    }

    /**
     * The the radius of vertex1
     */
    public void setRadius1(double r){
        mp_r1.setValue(r);
    }

    /**
     * Set the vertex0 location
     */
    public void setV0(Vector3d v){
        mp_v0.setValue(v);
    }

    /**
     * Get the vertex0 location
     * @return
     */
    public Vector3d getV0() {
        return mp_v0.getValue();
    }

    /**
     * Set the vertex1 location
     */
    public void setV1(Vector3d v){
        mp_v1.setValue(v);
    }

    /**
     * Set the amount of rounding of the edges
     * @param val
     */
    public void setRounding(double val) {
        mp_rounding.setValue(val);
    }

    /**
     * Get the amount of rounding of the edges
     */
    public double getRounding() {
        return mp_rounding.getValue();
    }

    /**
      @noRefGuide     
     */
    public int initialize() {

        
        super.initialize();
        Vector3d v0 = mp_v0.getValue();
        Vector3d v1 = mp_v1.getValue();
        Vector3d center = new Vector3d();
        center.add(v0, v1);
        center.scale(0.5);

        m_centerX = center.x;
        m_centerY = center.y;
        m_centerZ = center.z;

        Vector3d axis = new Vector3d();
        axis.sub(v1,v0);
        m_height = axis.length();
        m_halfHeight = m_height/2;
        axis.scale(1./m_height);

        m_ax= axis.x;
        m_ay = axis.y;
        m_az = axis.z;
        m_rounding = mp_rounding.getValue();        

        double r1 = mp_r1.getValue();
        double r0 = mp_r0.getValue();
        // cylinder may have different bases 
        m_R01 = (r0 + r1)/2; // radius at the center 
        double nA = (r0 - r1);
        double nO = m_height;
        double ss = sqrt(nA*nA + nO*nO);
        m_normalA = nA/ss;
        m_normalO = nO/ss;

        /*

        printf("axis: (%7.5f  %7.5f %7.5f) center:(%7.5f  %7.5f %7.5f) r: (%7.5f, %7.5f ) \n", m_ax, m_ay, m_az, m_centerX, m_centerY, m_centerZ, m_r0, m_r1);

        // old initiaization 
        m_v0 = mp_v0.getValue();
        m_v1= mp_v1.getValue();

        m_center = new Vector3d(m_v0);
        m_center.add(m_v1);
        m_center.scale(0.5);

        Vector3d caxis = new Vector3d(m_v1); // cylinder axis 
        caxis.sub(m_center);

        this.m_h2 =  caxis.length();

        caxis.normalize();

        // rotation axis 
        Vector3d raxis = new Vector3d();
        raxis.cross(caxis, Yaxis);
        double sina = raxis.length();
        double cosa = Yaxis.dot(caxis);
        if (abs(sina) < EPSILON) {
            // any axis orthogonal to Y will do the job 
            raxis = new Vector3d(1, 0, 0);
        }
        raxis.normalize();
        double angle = atan2(sina, cosa);
        m_rotation = new Matrix3d();
        m_rotation.set(new AxisAngle4d(raxis, angle));
        */
        return ResultCodes.RESULT_OK;
    }


    /**
     * @noRefGuide
     */
    //public Matrix3d getRotation(){
    //    return m_rotation;
    //}

    /**
     * @noRefGuide
     */
    public double getHalfHeight(){
        return m_halfHeight;
    }

    /**
     * @noRefGuide
     */
    public double getMidRadius(){
        return m_R01;
    }

    /**
     * @noRefGuide
     */
    public double getNormalA(){
        return m_normalA;
    }
    /**
     * @noRefGuide
     */
    public double getNormalO(){
        return m_normalO;
    }

    /**
     * @noRefGuide
     */
    public Vector3d getCenter(){
        return new Vector3d(m_centerX, m_centerY, m_centerZ);
    }

    /**
     * @noRefGuide
     */
    public Vector3d getAxis(){
        return new Vector3d(m_ax, m_ay, m_az);
    }

    /**
     @noRefGuide
     */
    public int getBaseValue(Vec pnt, Vec data) {
        
        getDistanceValue(pnt, data);
        data.v[0] = getShapeValue(data.v[0], pnt);
        return ResultCodes.RESULT_OK;
    }

    public int getDistanceValue(Vec pnt, Vec data) {

        double v[] = pnt.v;
        double 
            x = v[0] - m_centerX,
            y = v[1] - m_centerY,
            z = v[2] - m_centerZ;

        // projection to axis
        double pa = x*m_ax + y*m_ay + z*m_az; 
        // projection to orthogonal plane 
        double po = sqrt(max(0,x*x + y*y + z*z - pa*pa));
        double dside =  (po - m_R01)*m_normalO + pa * m_normalA; 
        double dbase = abs(pa) - m_halfHeight;
        //printf("sa: %7.5f sb:%7.5f\n", sa,sb);
        data.v[0] = blendMax(dside, dbase,m_rounding);
        return ResultCodes.RESULT_OK;
    }


    /**
     * @noRefGuide
     obsolete 
    */
    /*
    public double getDistance(Vec pnt){
        
        Vec pntc = new Vec(pnt);
        canonicalTransform(pntc);
        // cylinder is along Y axis and midpoint at origin 

        double x = pntc.v[0];
        double y = pntc.v[1];
        double z = pntc.v[2];
        
        double baseDist = abs(y) - m_h2;
        double r = sqrt(x * x + z * z);
        double sideDist = (r-m_R01)*m_normalR + y * m_normalY;

        double dist = max(baseDist, sideDist);
        return dist;

    }
    */
    /**
     *  move cylinder into canonical position with center at origin and cylinder axis aligned with Y-axis

     @noRefGuide     
     */
    //protected void canonicalTransform(Vec pnt) {
    //    pnt.subSet(m_center);
    //    pnt.mulSetLeft(m_rotation);
    //}

}  // class Cylinder
