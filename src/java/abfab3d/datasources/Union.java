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

   a DataSource union of several shapes

   return 1 if any of input data sources is 1, return 0 if all data sources are 0
   can be used to make union of few shapes

   @author Vladimir Bulatov

 */

public class Union  extends TransformableDataSource {
    
    Vector<DataSource> dataSources = new Vector<DataSource>();
    // fixed vector for calculations
    DataSource vDataSources[];
    
    
    public Union(){
        
    }
    
    /**
       add items to set of data sources
    */
    public void add(DataSource ds){
        dataSources.add(ds);            
    }
    public void addDataSource(DataSource ds){
        
        dataSources.add(ds);
        
    }
    
    public int initialize(){

        super.initialize();
        vDataSources = (DataSource[])dataSources.toArray(new DataSource[dataSources.size()]);
        
        for(int i = 0; i < vDataSources.length; i++){
            
            DataSource ds = vDataSources[i];
            if(ds instanceof Initializable){
                ((Initializable)ds).initialize();
            }
        }
        return RESULT_OK;
        
    }
    
    
    /**
     * calculates values of all data sources and return maximal value
     * can be used to make union of few shapes
     */
    public int getDataValue(Vec pnt, Vec data) {
        super.transform(pnt);
        int len = vDataSources.length;
        DataSource dss[] = vDataSources;
        
        double value = 0.;
        
        for(int i = 0; i < len; i++){
            
            DataSource ds = dss[i];
            int res = ds.getDataValue(pnt, data);
            
            if(res != RESULT_OK){
                // outside of domain
                continue;
            }
            double v = data.v[0];
            if(v >= 1.){
                data.v[0] = 1;
                return RESULT_OK;
            }
            
            if( v > value) value = v;
        }
        
        data.v[0] = value;
        
        return RESULT_OK;
    }
    
} // class Union
