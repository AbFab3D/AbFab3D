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

   a collection of various DataSource

   @author Vladimir Bulatov

   
   class to accept generic DataSource and VecTransform
   
   in getDataValue() it applies inverse_transform to the point and calculates data value in
   
   transformed point 
   
*/
public class DataTransformer   extends TransformableDataSource {
    
    protected DataSource dataSource;
    
    public DataTransformer(){
    }
    
    public void setSource(DataSource ds){
        dataSource = ds;
    }
        
    public int initialize(){
        
        super.initialize();
        
        if(dataSource != null && dataSource instanceof Initializable){
            ((Initializable)dataSource).initialize();
        }
        return RESULT_OK;
    }
    
    
    /**
     * 
     *
     */
    public int getDataValue(Vec pnt, Vec data) {

	super.transform(pnt);
        
        if(dataSource != null){
            return dataSource.getDataValue(pnt, data);
        } else {
            data.v[0] = 1.;
            return RESULT_OK;
        }
    }
    
} // class DataTransformer
