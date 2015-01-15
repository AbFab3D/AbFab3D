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

import java.util.List;
import java.util.Vector;


import abfab3d.param.Parameter;
import abfab3d.param.SNodeListParameter;
import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;

import static java.lang.Math.abs;

import static abfab3d.util.Output.printf;


import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.step10;


/**

   Intersection of multiple data sources
   
   <embed src="doc-files/Intersection.svg" type="image/svg+xml"/> 

   @author Vladimir Bulatov
*/ 
public class Intersection extends TransformableDataSource implements SNode {
    
    Vector<DataSource> dataSources = new Vector<DataSource>();
    // fixed vector for calculations
    DataSource vDataSources[];
    
    public Intersection(){
        initParams();
    }
    
    public Intersection(DataSource ds1, DataSource ds2){
        initParams();

        add(ds1);
        add(ds2);        
    }

    /**
     * @noRefGuide
     */
    protected void initParams() {
        super.initParams();
        Parameter p = new SNodeListParameter("datasources");
        params.put(p.getName(), p);
    }

    /**
       add items to set of data sources
    */
    public void add(DataSource ds){

        dataSources.add(ds);
        ((List)params.get("datasources").getValue()).add(ds);
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
            int res = ds.getDataValue(new Vec(pnt), data);
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

    @Override
    public SNode[] getChildren() {
        vDataSources = (DataSource[])dataSources.toArray(new DataSource[dataSources.size()]);

        // TODO: this is messy cleanup
        SNode[] ret = new SNode[vDataSources.length];
        for(int i=0; i < vDataSources.length; i++) {
            ret[i] = (SNode)vDataSources[i];
        }
        return ret;
    }

} // class Intersection
