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


import abfab3d.core.ResultCodes;
import abfab3d.core.Vec;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

import static abfab3d.core.Output.printf;


import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.step10;


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
       makes ring centered at orign with possible different offsets along axis
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
    public Ring(double innerRadius, double thickness, double width){
        
        this(innerRadius, thickness, -width/2, width/2);
    }
    
    
    /**
     * @noRefGuide

     */
    public int getBaseValue(Vec pnt, Vec data) {        

        double 
            x = pnt.v[0],
            y = pnt.v[1],
            z = pnt.v[2];

        double dymax = y - ymax;
        double dymin = -(y - ymin);
        double r = sqrt(x*x + z*z);
        double dInner = (innerRadius-r);
        double dExt = (r - exteriorRadius);
        double dist = max(dymax, dymin);
        dist = max(dist, dInner);
        dist = max(dist, dExt);
        
        data.v[0] = getShapeValue(dist, pnt);
        
        return ResultCodes.RESULT_OK;
    }
    
} // class Ring
