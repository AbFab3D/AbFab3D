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


//import java.awt.image.Raster;

import abfab3d.util.Vec;

import javax.vecmath.Vector3d;

import static abfab3d.util.MathUtil.step10;
import static abfab3d.util.Units.MM;


/**
 * Sphere with given location and radius
 *
 * @author Vladimir Bulatov
 */

public class Sphere extends TransformableDataSource {

    private double R, R2, RR;

    private double x0, y0, z0;

    public Sphere() {
        this(0., 0., 0., 1 * MM);
    }

    public Sphere(Vector3d c, double r) {
        this(c.x, c.y, c.z, r);
    }

    public Sphere(double r) {

        setRadius(r);
    }

    public Sphere(double x0, double y0, double z0, double r) {

        setCenter(x0, y0, z0);
        setRadius(r);

    }

    public void setCenter(double x, double y, double z) {

        this.x0 = x;
        this.y0 = y;
        this.z0 = z;

    }

    public void setRadius(double r) {

        R = r;
        R2 = 2 * r;
        RR = r * r;

    }

    /**
     * returns 1 if pnt is inside of ball
     * returns intepolated value if poiunt is within voxel size to the boundary
     * returns 0 if pnt is outside the ball
     */
    public int getDataValue(Vec pnt, Vec data) {

        super.transform(pnt);

        double res = 1.;
        double
                x = pnt.v[0] - x0,
                y = pnt.v[1] - y0,
                z = pnt.v[2] - z0;

        double vs = pnt.getScaledVoxelSize();

        //double rv = (R); // add slight growing with voxel size ? 

        // good approximation to the distance to the surface of the ball).x                             
        //double dist = ((x*x + y*y + z*z) - rv*rv)/(2*rv);
        double r = Math.sqrt(x * x + y * y + z * z);//)/(R2);
        data.v[0] = step10(r, this.R, vs);

        return RESULT_OK;
    }

}  // class Sphere

