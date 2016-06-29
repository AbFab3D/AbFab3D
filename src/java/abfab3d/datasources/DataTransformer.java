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


import abfab3d.core.ResultCodes;
import abfab3d.param.Parameter;
import abfab3d.param.SNodeParameter;

import abfab3d.core.DataSource;
import abfab3d.core.Initializable;
import abfab3d.core.Vec;


/**
 * Transform the data source.  This provides a transformable wrapper for any data source.
 * It is used when one need to appply transformation to a data source which already has its own transformation. 
 *
 * @author Vladimir Bulatov
 */
public class DataTransformer extends TransformableDataSource {

    DataSource m_source;

    SNodeParameter mp_source = new SNodeParameter("source");

    Parameter m_aparam[] = new Parameter[]{
        mp_source,
    };    

    /**
     * empty DataTransformer 
     * 
     */
    public DataTransformer() {
        super.addParams(m_aparam);        
    }

    public DataTransformer(DataSource ds) {
        super.addParams(m_aparam);
        mp_source.setValue(ds);
    }

    /**
     *
     * @param ds  data source to be transformed by this transformer
     */
    public void setSource(DataSource ds) {
        mp_source.setValue(ds);
    }

    /**
     *
     * @noRefGuide
     */
    public int initialize() {

        super.initialize();
        m_source = (DataSource)mp_source.getValue();

        if (m_source != null && m_source instanceof Initializable) {
            ((Initializable) m_source).initialize();
        }
        return ResultCodes.RESULT_OK;
    }


    /**
     *
     * @noRefGuide
     */
    public int getDataValue(Vec pnt, Vec data) {

        super.transform(pnt);

        if (m_source != null) {
            return m_source.getDataValue(pnt, data);
        } else {
            data.v[0] = 1.;
            return ResultCodes.RESULT_OK;
        }
    }

} // class DataTransformer
