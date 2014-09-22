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

import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;

/**
   Base class for DataSources which want to be Transformable

   @author Vladimir Bulatov

 */
public abstract class TransformableDataSource implements DataSource, Initializable {

    // transformation which is aplied to the data point before the calculation of data value 
    protected VecTransform m_transform; 
    // count of data channels 
    protected int m_channelsCount = 1;

    protected TransformableDataSource(){
    }

    /**
     * Transform the data source
     * @param transform General transformation to apply to the object before it is rendered
     */
    public void setTransform(VecTransform transform){
        m_transform = transform; 
    }

    /**
     * @noRefGuide
     */
    public int initialize(){
        if(m_transform != null && m_transform instanceof Initializable){
            return ((Initializable)m_transform).initialize();
        }

        return RESULT_OK;
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
       @override
     * @noRefGuide
     */
    public int getChannelsCount(){
        return m_channelsCount;
    }

}
