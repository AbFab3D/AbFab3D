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


/**
   interface to calculate data values of data source on 2d slice 

   slice in defined via origin O and two basis vectors  u, v 

   
   O+3v O+u+3v O+2u+3v O+3u+2v  
   O+2v O+u+2v O+2u+2v O+3u+2v 
   O+v  O+u+v  O+2u+v  O+3u+v 
   O    O+u    O+2u    O+3u
   
   slice data returned in pre-allocated 2d array of with dataDimension double date per point \
   
   data source has to be initialized 

 */
public interface SliceCalculator {
    
    
    public void getSliceData(DataSource source, Vector3d origin, Vector3d u, Vector3d v, int nu, int nv, int dataDimension, double sliceData[]);
            
}