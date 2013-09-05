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

   ring in XZ plane

   <embed src="doc-files/Ring.svg" type="image/svg+xml"/> 

   @author Vladimir Bulatov
*/

public class Ring  extends TransformableDataSource{
    
    protected double ymin, ymax;
    protected double innerRadius2;
    protected double innerRadius;
    protected double exteriorRadius;
    protected double exteriorRadius2;
    
    /**
       makes ring centered at orign with possible differetn offsets in y directions 
     */
    public Ring(double radius, double thickness, double ymin, double ymax){
        
        this.ymin = ymin;
        this.ymax = ymax;
        
        this.innerRadius = radius;
        this.exteriorRadius = innerRadius + thickness;
        
        this.innerRadius2 = innerRadius*innerRadius;
        
        this.exteriorRadius2 = exteriorRadius*exteriorRadius;
        
    }
    
    /**
       makes ring centered at orign
     */
    public Ring(double innerRadius, double thickeness, double width){
        
        this(innerRadius, thickeness, -width/2, width/2);
    }
    
    
    /**
     * @noRefGuide

     * calculates values of all data sources and return maximal value
     * can be used to make union of few shapes
     */
    public int getDataValue(Vec pnt, Vec data) {        
        super.transform(pnt);
        double y = pnt.v[1];
        double vs = pnt.getScaledVoxelSize();
        //double w2 = width2 + vs;
        
        double yvalue = 1.;
        
        if(y < ymin-vs || y > ymax+vs){
            
            data.v[0] = 0;
            return RESULT_OK;
            
        } else if(y < (ymin + vs)){
            // interpolate lower rim
            
            yvalue = (y - (ymin - vs))/(2*vs);
            
        } else if(y > (ymax - vs)){
            
            // interpolate upper rim
            yvalue = ((ymax + vs)-y)/(2*vs);
            
        }
        
        
        double x = pnt.v[0];
        double z = pnt.v[2];
        double r = Math.sqrt(x*x + z*z);
        
        double rvalue = 1;
        if(r < (innerRadius-vs) || r > (exteriorRadius+vs)){
            data.v[0] = 0;
            return RESULT_OK;
            
        } else if(r < (innerRadius+vs)){
            // interpolate interior surface
            rvalue = (r-(innerRadius-vs))/(2*vs);
            
        } else if(r > (exteriorRadius - vs)){
            
            rvalue = ((exteriorRadius + vs) - r)/(2*vs);
            // interpolate exterior surface
            
        }
        
        //data.v[0] = (rvalue < yvalue)? rvalue : yvalue;
        if(rvalue < yvalue)
            data.v[0] = rvalue;
        else
            data.v[0] = yvalue;
        
        return RESULT_OK;
    }
    
} // class Ring
