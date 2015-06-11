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


import java.util.List;
import java.util.Vector;


import abfab3d.param.Parameterizable;
import abfab3d.param.Parameter;
import abfab3d.param.SNode;
import abfab3d.param.SNodeListParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;

import static java.lang.Math.abs;

import static abfab3d.util.Output.printf;


import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.step10;


/**

   Union of multiple data sources. It can create union 
   
   <embed src="doc-files/Union.svg" type="image/svg+xml"/> 

   @author Vladimir Bulatov

 */

public class Union  extends TransformableDataSource implements SNode {
    
    Vector<DataSource> dataSources = new Vector<DataSource>();
    // fixed vector for calculations
    DataSource vDataSources[];
    DoubleParameter mp_blendWidth = new DoubleParameter("blend", "blend width", 0.);
    SNodeListParameter mp_dataSources = new SNodeListParameter("sources");
    
    Parameter m_aparam[] = new Parameter[]{
        mp_blendWidth,
        mp_dataSources
    };    

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
     * Set the blending width
     */
    public void setBlend(double r){
        mp_blendWidth.setValue(r);
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
     * @noRefGuide
     */
    protected void initParams() {
        for(int i = 0; i < m_aparam.length; i++){
            params.put(m_aparam[i].getName(),m_aparam[i]);
        }
    }

    /**
       add item to union. 
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
    }

    /**
       @noRefGuide
    */
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
       @noRefGuide
     */
    public int getDataValue(Vec pnt, Vec data) {

        super.transform(pnt);

        int len = vDataSources.length;
        DataSource dss[] = vDataSources;
        
        double value = 0.;
        for(int i = 0; i < len; i++){
            
            DataSource ds = dss[i];

            Vec pnt1 = new Vec(pnt);
            int res = ds.getDataValue(pnt1, data);

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
} // class Union
