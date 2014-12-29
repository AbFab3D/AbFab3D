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

   Boolean difference between two data sources 
   <br/>

   <embed src="doc-files/Subtraction.svg" type="image/svg+xml"/> 

   
   @author Vladimir Bulatov

 */
public class Subtraction extends TransformableDataSource implements GroupingNode {
    
    DataSource dataSource1;
    DataSource dataSource2;

    /**
       shape which is result of subtracting shape2 from shape1
     */
    public Subtraction(DataSource shape1, DataSource shape2){

        dataSource1 = shape1;
        dataSource2 = shape2;
        
    }
        
    /**
       @noRefGuide
     */
    public int initialize(){

        super.initialize();

        if(dataSource1 != null && dataSource1 instanceof Initializable){
            ((Initializable)dataSource1).initialize();
        }
        if(dataSource2 != null && dataSource2 instanceof Initializable){
            ((Initializable)dataSource2).initialize();
        }
        return RESULT_OK;
        
    }
    
    /**
     * @noRefGuide
       
     * calculates values of all data sources and return maximal value
     * can be used to make union of few shapes
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);
        double v1 = 0, v2 = 0;
        
        int res = dataSource1.getDataValue(new Vec(pnt), data);
        if(res != RESULT_OK){
            data.v[0] = 0.0;
            return res;
        }
        
        v1 = data.v[0];
        
        if(v1 <= 0.){
            data.v[0] = 0.0;
            return RESULT_OK;
        }
        
        // we are here if v1 > 0
        
        res = dataSource2.getDataValue(new Vec(pnt), data);
        
        if(res != RESULT_OK){
            data.v[0] = v1;
            return RESULT_OK;
        }
        
        v2 = data.v[0];
        if(v2 >= 1.){
            data.v[0] = 0.;
            return RESULT_OK;
        }
        data.v[0] = v1*(1-v2);
        
        return RESULT_OK;
    }

    @Override
    public DataSource[] getChildren() {
        return new DataSource[] {dataSource1,dataSource2};
    }
} // class Subtraction
