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

import abfab3d.param.BaseParameterizable;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;

/**
   Base class for DataSources which want to be Transformable

   the TransformableDataSource may have it's own Transform and Material
   
   subclasses are responsible to implement getDataValue() 
   according to template 
   int getDataValue(Vec pnt, Vec data) {
      super.transform(pnt);
      //
      ...  do own calculations ...
      //
      super.getMaterialDataValue(pnt, data);      
   }
   

   @author Vladimir Bulatov

 */
public abstract class TransformableDataSource extends BaseParameterizable implements DataSource, Initializable {

    // transformation which is aplied to the data point before the calculation of data value 
    protected VecTransform m_transform; 
    // count of data channels 
    protected int m_channelsCount = 1;
    // count of channels of material 
    protected int m_materialChannelsCount = 0;
    // material used for this shape 

    // the material is potential multichannel data source and it adds channels to the total channels count
    protected DataSource m_material = null; 

    protected TransformableDataSource(){
    }

    /**
     * Initialize parameters.
     */
    protected void initParams() {
        Parameter p = new Vector3dParameter("center");
        params.put(p.getName(),p);
    }

    /**
     * Transform the data source
     * @param transform General transformation to apply to the object before it is rendered
     */
    public void setTransform(VecTransform transform){
        m_transform = transform; 
    }

    public void setMaterial(DataSource material){
        m_material = material; 
    }

    public VecTransform getTransform() {
        return m_transform;
    }

    /**
     * @noRefGuide
     */
    public int initialize(){

        int res = RESULT_OK;

        if(m_transform != null && m_transform instanceof Initializable){
            res = ((Initializable)m_transform).initialize();
        }

        if(m_material != null){
            if( m_material instanceof Initializable){
                res = res | ((Initializable)m_material).initialize();
            }
            m_materialChannelsCount = m_material.getChannelsCount();
        }        
        return res;
    }

    /**
     * @noRefGuide
     */
    public abstract int getDataValue(Vec pnt, Vec data);


    /**
     * @noRefGuide
     */
    protected final int transform(Vec pnt){
        if(m_transform != null){
            return m_transform.inverse_transform(pnt, pnt);
        }
        return RESULT_OK;
    }
    
    /**
     *  @return number of channes this data source generates 
     *  
     * @noRefGuide
     */
    public int getChannelsCount(){
        return m_channelsCount + m_materialChannelsCount;
    }

    /**
       fills data with values from he material channel
     */
    protected int getMaterialDataValue(Vec pnt, Vec data){

        if(m_material == null)
            return RESULT_OK;

        // TODO - garbage generation !
        Vec mdata = new Vec(m_materialChannelsCount);
        
        m_material.getDataValue(pnt, mdata);

        // copy material into data 
        switch(m_materialChannelsCount){
        default: 
            for(int k = 0; k < m_materialChannelsCount; k++)
                data.v[m_channelsCount + k] = mdata.v[k];
            break;
            
        case 3: data.v[m_channelsCount + 2] = mdata.v[2]; // no break here 
        case 2: data.v[m_channelsCount + 1] = mdata.v[1]; // no break here 
        case 1: data.v[m_channelsCount ] = mdata.v[0];    // no break here 
        case 0: break;
        }

        //TODO 
        return RESULT_OK;
    }

}
