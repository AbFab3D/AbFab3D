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
   Boolean complement.  The datasource is the opposite of the input.
   <embed src="doc-files/Complement.svg" type="image/svg+xml"/> 
 
 * @author Vladimir Bulatov
 */
public class Complement extends TransformableDataSource {

    private DataSource dataSource = null;

    SNodeParameter mp_data = new SNodeParameter("source", ShapesFactory.getInstance());

    Parameter m_aparam[] = new Parameter[]{
        mp_data,
    };    

    public Complement() {
        super.addParams(m_aparam);
    }
    /**
     * Complement of the given datasource.
     * @param source  object to which the complement is generated
     */
    public Complement(DataSource source) {
        super.addParams(m_aparam);
        mp_data.setValue(source);
    }

    /**
     *  Set the source
     *
     * @param ds  data source
     */
    public void setSource(DataSource ds) {
        mp_data.setValue(ds);
    }

    /**
     * Get the source
     * @return
     */
    public Object getSource() {
        return mp_data.getValue();
    }

    /**
     * @noRefGuide
     */
    public int initialize() {

        super.initialize();
        dataSource = (DataSource)mp_data.getValue();
        if (dataSource instanceof Initializable) {
            ((Initializable) dataSource).initialize();
        }

        return ResultCodes.RESULT_OK;

    }

    /**
     * Get the data value for a pnt
     *
     * @noRefGuide
     */
    public int getBaseValue(Vec pnt, Vec data) {

        int res = dataSource.getDataValue(pnt, data);
        switch(m_dataType){
        default:
        case DATA_TYPE_DENSITY:
            data.v[0] = 1 - data.v[0];
            break;
        case  DATA_TYPE_DISTANCE:
            data.v[0] = - data.v[0];            
        }
        return ResultCodes.RESULT_OK;
        
    }
} // class Complement

