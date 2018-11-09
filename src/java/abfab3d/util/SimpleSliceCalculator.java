/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014-2018
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.util;

import javax.vecmath.Vector3d;

import abfab3d.core.DataSource;
import abfab3d.core.Vec;

public class SimpleSliceCalculator implements SliceCalculator {
    
    
    // maximal number of data channels to expect 
    int maxDataDimension = 8;
    
    public SimpleSliceCalculator(int maxDataDimension){
        this.maxDataDimension = maxDataDimension;
    }
    
    public void getSliceData(DataSource model, Vector3d origin, Vector3d eu, Vector3d ev, int nu, int nv, int dataDimension, double sliceData[]){
        //protected void calculateSlice(int iz, double sliceData[]){            
        //if(DEBUG)printf("getSlice(%7.5f %7.5f %7.5f)mm\n", origin.x/MM, origin.y/MM, origin.z/MM);
        
        Vec pnt = new Vec(3);
        Vec data = new Vec(maxDataDimension);
        
        for(int j = 0; j < nv; j++){
            for(int i = 0; i < nu; i++){
                
                double x = origin.x + eu.x*i + ev.x*j;
                double y = origin.y + eu.y*i + ev.y*j;
                double z = origin.z + eu.z*i + ev.z*j;
                
                pnt.set(x,y,z);
                model.getDataValue(pnt, data);
                //normalizeVoxel(data);
                data.get(sliceData, offset(i, j, nu, dataDimension), dataDimension);
            }
        }
        
    }
    
    final int offset(int ix, int iy, int nx, int dataDimension){
        return (ix + iy*nx)*dataDimension;
    }
    
} // class SimpleSliceCalculator
