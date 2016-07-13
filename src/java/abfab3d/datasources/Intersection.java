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


import java.util.Vector;
import java.util.List;


import abfab3d.core.ResultCodes;
import abfab3d.param.Parameterizable;
import abfab3d.param.Parameter;
import abfab3d.param.SNode;
import abfab3d.param.SNodeListParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.core.Vec;
import abfab3d.core.DataSource;
import abfab3d.core.Initializable;

import static abfab3d.core.MathUtil.blendMax;

/**

   Intersection of multiple data sources.  The overlap of all data sources will be preserved.
   
   <embed src="doc-files/Intersection.svg" type="image/svg+xml"/> 

   @author Vladimir Bulatov
*/ 
public class Intersection extends TransformableDataSource implements SNode {
    
    Vector<DataSource> dataSources = new Vector<DataSource>();

    // internal variables 
    private DataSource vDataSources[];
    private double m_blendWidth = 0;

    DoubleParameter mp_blendWidth = new DoubleParameter("blend", "blend width", 0.);
    SNodeListParameter mp_dataSources = new SNodeListParameter("sources", ShapesFactory.getInstance());
    
    Parameter m_aparam[] = new Parameter[]{
        mp_blendWidth,
        mp_dataSources
    };    

    
    public Intersection(){
        initParams();
    }
    
    public Intersection(DataSource ds1, DataSource ds2, DataSource ds3){

        initParams();

        add(ds1);
        add(ds2);        
        add(ds3); 
        
    }

    public Intersection(DataSource ds1, DataSource ds2){
        initParams();

        add(ds1);
        add(ds2);        
    }

    public Intersection(DataSource ds1){
        initParams();
        add(ds1);
    }

    /**
     * @noRefGuide
     */
    protected void initParams() {
        super.addParams(m_aparam);
    }

    /**
       add items to set of data sources
    */
    public void add(DataSource ds){

        dataSources.add(ds);
        mp_dataSources.add((Parameterizable) ds);
    }

    /**
     * Set an item into the list
     *
     * @param idx The index, it must already exist
     * @param src
     */
    public void set(int idx, DataSource src) {
        mp_dataSources.set(idx, (Parameterizable) src);
        dataSources.set(idx, src);
    }

    /**
     * Clear the datasources
     */
    public void clear() {
        mp_dataSources.clear();
        dataSources.clear();
    }

    /**
     * Set the blending width
     *
     * @param val The value in meters
     */
    public void setBlend(double val){
        mp_blendWidth.setValue(val);
    }

    /**
     * Get the blending width
     * @return
     */
    public double getBlend() {
        return mp_blendWidth.getValue();
    }

    /**
     * @noRefGuide
     */
    public int initialize(){

        super.initialize();

        m_blendWidth = mp_blendWidth.getValue();

        vDataSources = (DataSource[])dataSources.toArray(new DataSource[dataSources.size()]);
        
        for(int i = 0; i < vDataSources.length; i++){
            
            DataSource ds = vDataSources[i];
            if(ds instanceof Initializable){
                ((Initializable)ds).initialize();
            }
        }
        return ResultCodes.RESULT_OK;
        
    }



    /**
     * calculates intersection of all values
     *
     * @noRefGuide
     *
     */
    public int getBaseValue(Vec pnt, Vec data) {
        switch(m_dataType){
        default:
        case DATA_TYPE_DENSITY:
            getDensityData(pnt, data);
            break;
        case DATA_TYPE_DISTANCE:
            getDistanceData(pnt, data);
            break;
        }
        return ResultCodes.RESULT_OK;        
    }

    /**
     * @noRefGuide
     */
    public int getDensityData(Vec pnt, Vec data) {
        
        DataSource dss[] = vDataSources;
        int len = dss.length;
        
        double value = 1;
        
        for(int i = 0; i < len; i++){
            
            DataSource ds = dss[i];
            int res = ds.getDataValue(new Vec(pnt), data);
            double v = data.v[0];
            
            if(v <= 0.){
                data.v[0] = 0;
                return ResultCodes.RESULT_OK;
            }            
            if(v < value)
                value = v;
            
        }
        
        data.v[0] = value;
        return ResultCodes.RESULT_OK;
    }
    
    /**
     * @noRefGuide
     */
    public int getDistanceData(Vec pnt, Vec data) {

        int len = vDataSources.length;
        DataSource dss[] = vDataSources;
        
        double value = -Double.MAX_VALUE;

        //TODO garbage collecton 
        Vec pnt1 = new Vec(pnt);

        for(int i = 0; i < len; i++){
            
            DataSource ds = dss[i];
            pnt1.set(pnt);
            ds.getDataValue(pnt1, data);
            double v = data.v[0];
            value = blendMax(value, data.v[0], m_blendWidth);            
        }
        
        data.v[0] = value;
        
        return ResultCodes.RESULT_OK;
    }   

    /**
     * @noRefGuide
     */
    
    public SNode[] getChildren() {

        List childrenList = mp_dataSources.getValue(); 
        SNode[] children = (SNode[])childrenList.toArray(new SNode[childrenList.size()]);
        return children;

    }
    
} // class Intersection
