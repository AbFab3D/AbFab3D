/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014
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

import javax.vecmath.Vector3d;

import static abfab3d.util.MathUtil.step10;
import static java.lang.Math.floor;
import static java.lang.Math.sqrt;


/**
 * Spring centered at given point with axis parallel to z-axis
 *
 * <embed src="doc-files/Spring.svg" type="image/svg+xml"/>
 *
 * Working from this definition: http://en.wikipedia.org/wiki/Spring_(math)
 *
 * @author Alan Hudson
 */
public class Spring extends TransformableDataSource{

    /** Distance from the center of the tube */
    private double R;

    /** Radius of the tube */
    private double r;

    /** Speed of the movement along z axis.  Value is the distance traveled for a half spring.
     * Positive for right handed, negative for left.  */
    private double P;

    /** The number of rounds in the circle */
    private int n;

    private double x0,y0,z0;
    private double zmin,zmax;

    /**
     * Spring with specified center, outer radius, tube radius, speed and number of turns
     *
     * @param center - location of helix center
     * @param R -  Distance from the center of the tube
     * @param r - Radius of the tube
     * @param P - Speed of the movement along z axis.  Positive for right handed, negative for left.
     * @param n - The number of rounds in the circle
     */
    public Spring(Vector3d center, double R, double r, double P, int n) {
        this(center.x,center.y,center.z,R,r,P,n);
    }

    /**
     * Spring with specified outer radius, tube radius, speed and number of turns
     *
     * @param R -  Distance from the center of the tube
     * @param r - Radius of the tube
     * @param P - Speed of the movement along z axis.  Positive for right handed, negative for left.
     * @param n - The number of rounds in the circle
     * Spring centered at origin
     */
    public Spring(double R, double r, double P, int n){
        this(0,0,0,R,r,P,n);
    }

    /**
     * Spring with specified center, outer radius, tube radius, speed and number of turns
     *
     * @param cx - x component of center
     * @param cy - y component of center
     * @param cz - z component of center
     * @param R -  Distance from the center of the tube
     * @param r - Radius of the tube
     * @param P - Speed of the movement along z axis.  Positive for right handed, negative for left.
     * @param n - The number of rounds in the circle
     */
    public Spring(double cx, double cy, double cz, double R,double r, double P, int n){
        setCenter(cx, cy, cz);
        this.R = R;
        this.r = r;
        this.P = P;
        this.n = n;

        zmin = n * -P - r;
        zmax = n * P + r;
    }

    /**
       @noRefGuide
     */
    public void setCenter(double cx, double cy, double cz) {
        this.x0 = cx;
        this.y0 = cy;
        this.z0 = cz;
    }

    /**
     * returns 1 if pnt is inside of Spring
     * returns interpolated value if point is within voxel size to the boundary
     * returns 0 if pnt is outside the Spring
       @noRefGuide
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);

        double
                x = pnt.v[0] - x0,
                y = pnt.v[1] - y0,
                z = pnt.v[2] - z0;

        if (z < zmin || z > zmax) {
            data.v[0] = 0;
            return RESULT_OK;
        }

        double period = 2 * (P + r);
        z -= (period)*floor(z/(period));
        z -= period/2;

        /*
        // Closest to working for n=2
        if (z > (P + r)) {
            z = (z % (P + r)) - (P + r);
        }
        */

        //z = z % (P + r);
        //z = (z + (P + r) / 2.0) % (P + r);

        double rxy = sqrt(x*x + y*y) - R;
        double zloc = z + P * Math.atan2(x,y) / Math.PI;
        
        data.v[0] = step10((rxy*rxy + (zloc*zloc) - r*r) / (2*r), 0, pnt.getScaledVoxelSize());

        return RESULT_OK;
    }
}  // class Spring

