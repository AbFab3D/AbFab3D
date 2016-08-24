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

import static java.lang.Math.abs;

import static abfab3d.core.Output.printf;


import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.step10;
import static abfab3d.core.MathUtil.blendMin;


/**

   Union of multiple data sources. It can create union 
   
   <embed src="doc-files/Union.svg" type="image/svg+xml"/> 

   @author Vladimir Bulatov

 */

public class Union  extends TransformableDataSource implements SNode {
    
    Vector<DataSource> dataSources = new Vector<DataSource>();

    DoubleParameter mp_blendWidth = new DoubleParameter("blend", "blend width", 0.);
    SNodeListParameter mp_dataSources = new SNodeListParameter("sources", ShapesFactory.getInstance());
    
    Parameter m_aparam[] = new Parameter[]{
        mp_blendWidth,
        mp_dataSources
    };    

    // internal variables 
    private DataSource vDataSources[];
    private double m_blendWidth = 0;

    /**
       Create empty union. Use add() method to add arbitrary number of shapes to the union. 
     */
    public Union(){
        initParams();
    }

    /**
     * Union Constructor
     */
    public Union(DataSource shape1){
        initParams();
        add(shape1);
    }

    /**
       union of two shapes 
     */
    public Union(DataSource shape1, DataSource shape2 ){
        initParams();
        
        add(shape1);
        add(shape2);        
    }

    /**
       union of three shapes 
     */
    public Union(DataSource shape1, DataSource shape2, DataSource shape3 ){
        initParams();
        
        add(shape1);
        add(shape2);        
        add(shape3);        
    }

    /**
       union of four shapes 
     */
    public Union(DataSource shape1, DataSource shape2, DataSource shape3, DataSource shape4){
        initParams();
        
        add(shape1);
        add(shape2);        
        add(shape3);        
        add(shape4); 
    }

    /**
     * @noRefGuide
     */
    protected void initParams() {
        super.addParams(m_aparam);
    }

    /**
       Add item to union.
       @param shape item to add to union of multiple shapes 
    */
    public void add(DataSource shape){
        dataSources.add(shape);
        mp_dataSources.add((Parameterizable) shape);
    }

    /**
     * Set an item into the list
     *
     * @param idx The index, it must already exist
     * @param src
     */
    public void set(int idx, DataSource src) {
        mp_dataSources.set(idx, (Parameterizable) src);
        dataSources.set(idx,src);
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
     * @param value The value in meters
     */
    public void setBlend(double value){
        mp_blendWidth.setValue(value);
    }

    /**
     * Get the blending width
     * @return
     */
    public double getBlend() {
        return mp_blendWidth.getValue();
    }



    /**
       @noRefGuide
    */
    public int initialize(){
        super.initialize();
        vDataSources = (DataSource[])dataSources.toArray(new DataSource[dataSources.size()]);
        m_blendWidth = mp_blendWidth.getValue();
        for(int i = 0; i < vDataSources.length; i++){            
            initializeChild(vDataSources[i]);
        }


        return ResultCodes.RESULT_OK;
    }
    
    
    /**
     * calculates values of all data sources and return maximal value
     * can be used to make union of few shapes
       @noRefGuide
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

    public int getDensityData(Vec pnt, Vec data) {

        int len = vDataSources.length;
        DataSource dss[] = vDataSources;
        
        double value = 0.;
        for(int i = 0; i < len; i++){
            
            DataSource ds = dss[i];

            Vec pnt1 = new Vec(pnt);
            int res = ds.getDataValue(pnt1, data);

            if(res != ResultCodes.RESULT_OK){
                // outside of domain
                continue;
            }
            double v = data.v[0];
            if(v >= 1.){
                data.v[0] = 1;
                return ResultCodes.RESULT_OK;
            }
            
            if( v > value) value = v;
        }
        
        data.v[0] = value;
        
        return ResultCodes.RESULT_OK;
    }

    public int getDistanceData(Vec pnt, Vec data) {

        int len = vDataSources.length;
        DataSource dss[] = vDataSources;
        
        double value = Double.MAX_VALUE;

        //TODO garbage collecton 
        Vec pnt1 = new Vec(pnt);

        for(int i = 0; i < len; i++){
            
            DataSource ds = dss[i];
            pnt1.set(pnt);
            ds.getDataValue(pnt1, data);
            double v = data.v[0];
            value = blendMin(value, data.v[0], m_blendWidth);            
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

} // class Union
