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

import javax.vecmath.Vector3d;


import abfab3d.core.ResultCodes;
import abfab3d.core.Vec;

import abfab3d.util.PointToTriangleDistance;

import static java.lang.Math.abs;

import static abfab3d.core.Output.printf;


import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.step10;


/**

   3D shape within given distance threshold to given 3D triangle

   @author Vladimir Bulatov

 */

public class Triangle  extends TransformableDataSource{
    
    static final boolean DEBUG=false;
    
    double threshold = 1.;
    Vector3d v0, v1, v2;
    
    public Triangle(Vector3d v0, Vector3d v1, Vector3d v2, double threshold){
        this.v0 = new Vector3d(v0);
        this.v1 = new Vector3d(v1);
        this.v2 = new Vector3d(v2);
        this.threshold = threshold;
    }
    
    public int getBaseValue(Vec pnt, Vec data) {
        
        double x = pnt.v[0];
        double y = pnt.v[1];
        double z = pnt.v[2];
        
        if(DEBUG)
            printf("pnt: (%8.5f %8.5f %8.5f)  ", x,y,z);
        
        Vector3d p = new Vector3d(x,y,z);
        double dist = PointToTriangleDistance.get(p, v0, v1, v2);
        
        double vs = pnt.getScaledVoxelSize();
        
        data.v[0] = step10(dist, threshold, vs);
        
        if(DEBUG)
            printf("dist: %9.5f threshold:%9.5f diff: %9.5f data: %9.5f\n ", dist, threshold,  dist - threshold, data.v[0]);
        
        return ResultCodes.RESULT_OK;
    }                
} // class Triangle 
