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

import static abfab3d.core.Output.printf;


import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.step10;


/**

   class to return neighborhood of a limit set. 
   These are area where 1/scaleFactor is close to 0. 

   @author Vladimir Bulatov
   
*/

public class LimitSet  extends TransformableDataSource {
    
    final boolean DEBUG = false;
    int debugCount = 100;
    private double distance = 1;
    private double stretchFactor = 1;
    
    public LimitSet(double distance, double stretchFactor){
        
        this.distance = distance;
        this.stretchFactor = stretchFactor;
        
    }
    
    /**
     * returns 1 if pnt is closer to the limit set then distance
     * returns 0 if pnt is further from he limit set 
     limit set distance is calculated as  stretchFactor/pnt.scaleFactor 
    */
    public int getDataValue(Vec pnt, Vec data) {

        super.transform(pnt);
        double dist = stretchFactor/pnt.getScaleFactor();
        
        if(DEBUG ) {
            double s = pnt.getScaleFactor();
            if(s != 1.0 && debugCount-- > 0)
                printf("limitSet scaleFactor: %10.5f\n", s);
        }
        
        data.v[0] = step10(dist, distance, pnt.getVoxelSize());
        
        return ResultCodes.RESULT_OK;
        
    }
    
}  // class LimitSet

