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

import java.util.Vector;

import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;
import javax.vecmath.AxisAngle4d;


import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;

import abfab3d.util.PointToTriangleDistance;

import static java.lang.Math.sqrt;
import static java.lang.Math.atan2;
import static java.lang.Math.abs;

import static abfab3d.util.Output.printf;


import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.intervalCap;
import static abfab3d.util.MathUtil.step10;

import static abfab3d.util.Units.MM;


/**

   Torus centered at the given point with axis parallel to z-axis

   <embed src="doc-files/Torus.svg" type="image/svg+xml"/> 

   @author Vladimir Bulatov

 */

public class Torus extends TransformableDataSource{
    
    private double R, r;
    private double x0, y0, z0;
    
    /**
       @param center - location of torus center
       @param Rout - outer radius of torus
       @param Rin - innter radius of torus

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
        this.R = Rout;
        this.r = Rin;
    }

    /**
       @param cx - x component of center
       @param cy - y component of center
       @param cz - z component of center
       @param Rout - outer radius of torus
       @param Rin - innter radius of torus

     */
    public Torus(double cx, double cy, double cz, double Rout, double Rin){
        setCenter(cx, cy, cz);
        this.R = Rout;
        this.r = Rin;
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
     * returns 1 if pnt is inside of Torus
     * returns intepolated value if point is within voxel size to the boundary
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
        
        return RESULT_OK;
    }
}  // class Torus

