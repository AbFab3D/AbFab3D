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

package abfab3d.distance;

import abfab3d.util.DataSource;
import abfab3d.util.Vec;

import static abfab3d.util.MathUtil.step10;



public class DensityFromDistance implements DataSource {

    
    protected DistanceData distData;
    protected double surface;
    protected double thickness;

    /**
       @param distData object which calculates distance data 
       @param surface - distance value for the shape surface
       @param thickness - thickness of transition surface layer
     */
    public DensityFromDistance(DistanceData distData, double surface, double thickness){
        this.distData = distData;
        this.surface = surface;
        this.thickness = thickness;
        
    }

    public int getDataValue(Vec pnt, Vec dataValue){

        double d = distData.getDistance(pnt.v[0],pnt.v[1],pnt.v[2]);
        dataValue.v[0] = step10(d, surface, thickness);
        return RESULT_OK;
    }   

    public int getChannelsCount(){
        return 1;
    }
} 