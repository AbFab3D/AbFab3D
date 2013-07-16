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

   DataSource representation of torus centered at origin with axis along z-axis

   @author Vladimir Bulatov

 */

public class Torus extends TransformableDataSource{
    
    private double R, r;

    public Torus(double R, double r){
        
        this.R = R;
        this.r = r;
    }
    
    /**
     * returns 1 if pnt is inside of Torus
     * returns intepolated value if poiunt is within voxel size to the boundary
     * returns 0 if pnt is outside the Torus
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);
        double res = 1.;
        double
            x = pnt.v[0],
            y = pnt.v[1],
            z = pnt.v[2];
        
        double rxy = sqrt(x*x + y*y) - R;
        
        data.v[0] = step10(((rxy*rxy + z*z) - r*r)/(2*r), 0, pnt.getScaledVoxelSize());
        
        return RESULT_OK;
    }
}  // class Torus

