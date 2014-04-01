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


import abfab3d.util.Vec;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import static abfab3d.util.MathUtil.step10;
import static java.lang.Math.*;

import static abfab3d.util.Output.fmt;


/**
 * Cylinder with given ends and radius
 *
   <embed src="doc-files/Cylinder.svg" type="image/svg+xml"/> 
 * @author Vladimir Bulatov
 */
public class Cylinder extends TransformableDataSource {

    static final double EPSILON = 1.e-8;

    private double R0,R1; // cylinder radiuses 
    private boolean uniform = true;
    private double h2; // cylnder's half height of
    private double scaleFactor = 0;
    private Vector3d center;
    private Matrix3d rotation;
    private Vector3d v0, v1;
    // params for non uniform cylinder 
    private double R01, normalR, normalY;

    static final Vector3d Yaxis = new Vector3d(0, 1, 0);

    /**
     * Circular cylinder of uniform radius
     * @param v0 center of first base
     * @param v1 center of second base
     * @param radius Radius of cylinder 
     */
    public Cylinder(Vector3d v0, Vector3d v1, double radius) {

        if (radius < 0) {
            throw new IllegalArgumentException("Cylinder radius < 0.  Value: " + radius);
        }
        this.R0 = radius;
        this.R1 = radius;
        this.v0 = new Vector3d(v0);
        this.v1 = new Vector3d(v1);
        this.uniform = true;

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
        this.R0 = radius0;
        this.R1 = radius1;
        this.v0 = new Vector3d(v0);
        this.v1 = new Vector3d(v1);
        this.uniform = false;

    }

    /**

      @noRefGuide     
     */
    public int initialize() {

        super.initialize();

        center = new Vector3d(v0);
        center.add(v1);
        center.scale(0.5);
        Vector3d caxis = new Vector3d(v1); // cylinder axis 
        caxis.sub(center);

        this.h2 = caxis.length();

        caxis.normalize();

        // rotation axis 
        Vector3d raxis = new Vector3d();
        raxis.cross(caxis, Yaxis);
        double sina = raxis.length();
        double cosa = Yaxis.dot(caxis);
        if (abs(sina) < EPSILON) {
            //TODO do something smart 
            raxis = new Vector3d(1, 0, 0);
        }
        raxis.normalize();
        double angle = atan2(sina, cosa);
        rotation = new Matrix3d();
        rotation.set(new AxisAngle4d(raxis, angle));

        if(!uniform){
            // cylinder has different bases 
            this.R01 = (R0 + R1)/2;
            double nY = (R0 - R1);
            double nR = h2*2;
            double ss = sqrt(nY*nY + nR*nR);
            this.normalY = nY/ss;
            this.normalR = nR/ss;

        }

        return RESULT_OK;
    }

    /**
      the scale factor increases thickness of cylinder proportional to scale of transformation 

      it can be used to thicken the thin parts 
      @noRefGuide
     */
    public void setScaleFactor(double value) {
        scaleFactor = value;
    }

    /**
     * returns 1 if pnt is inside of cylinder
     * returns intepolated value if point is within voxel size to the boundary
     * returns 0 if pnt is outside the ball
     @noRefGuide
     */
    public int getDataValue(Vec pntIn, Vec data) {

        super.transform(pntIn);

        Vec pnt = new Vec(pntIn);
        canonicalTransform(pnt);
        // cylinder is along Y axis with midpoint at origin 

        double x = pnt.v[0];
        double y = pnt.v[1];
        double z = pnt.v[2];
        double vs = pnt.getScaledVoxelSize();
        if (scaleFactor != 0.) {
            double s = 1 / (scaleFactor * pnt.getScaleFactor());
            x *= s;
            y *= s;
            z *= s;
        }


        double baseCap = step10(abs(y), this.h2, vs);
        if (baseCap == 0.0) {
            data.v[0] = 0;
            return RESULT_OK;
        }

        double r = sqrt(x * x + z * z);
        double distanceToSide;

        if(uniform)
            distanceToSide = r-R0;
        else 
            distanceToSide = (r-R01)*normalR + y * normalY;

        double sideCap = step10(distanceToSide, vs);
        if (sideCap < baseCap) baseCap = sideCap;
        data.v[0] = baseCap;
        return RESULT_OK;
    }

    /**
     *  move cylinder into canononical position with center at origin and cylinder axis aligned with Y-axis

     @noRefGuide     
     */
    protected void canonicalTransform(Vec pnt) {
        pnt.subSet(center);
        pnt.mulSetLeft(rotation);
    }

}  // class Cylinder
