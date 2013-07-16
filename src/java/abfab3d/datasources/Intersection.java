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

   Intersection of multiple data sourrces
   return 1 if all data sources return 1
   return 0 otherwise
   
   @author Vladimir Bulatov
*/ 
public class Intersection extends TransformableDataSource{
    
    Vector<DataSource> dataSources = new Vector<DataSource>();
    // fixed vector for calculations
    DataSource vDataSources[];
    
    public Intersection(){
        
    }
    
    
    /**
       simpler name for addDataSource()
    */
    public void add(DataSource ds){
        dataSources.add(ds);
    }
    
    /**
       add items to set of data sources
    */
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
     * calculates intersection of all values
     *
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);

        DataSource dss[] = vDataSources;
        int len = dss.length;
        
        double value = 1;
        
        for(int i = 0; i < len; i++){
            
            DataSource ds = dss[i];
            //int res = ds.getDataValue(pnt, workPnt);
            int res = ds.getDataValue(pnt, data);
            if(res != RESULT_OK){
                data.v[0] = 0.;
                return res;
            }
            
            double v = data.v[0];
            
            if(v <= 0.){
                data.v[0] = 0;
                return RESULT_OK;
            }
            //value *= v;
            
            if(v < value)
                value = v;
            
        }
        
        data.v[0] = value;
        return RESULT_OK;
    }
    
} // class Intersection
