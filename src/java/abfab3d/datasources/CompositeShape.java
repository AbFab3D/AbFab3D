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
import abfab3d.core.DataSource;
import abfab3d.core.Initializable;
import abfab3d.core.Vec;

import abfab3d.param.Parameter;
import abfab3d.param.SNodeParameter;
import abfab3d.param.Parameterizable;

import abfab3d.util.ShapeProducer;


/**
 * Makes complex composte node act as single opaque data source with only exposed parameters 
 *    
 *  
 * @author Vladimir Bulatov
 */
public class CompositeShape extends TransformableDataSource {

    DataSource m_source;

    //SNodeParameter mp_source = new SNodeParameter("source");

    /**
     * empty CompositeShape()
     * 
     */
    public CompositeShape() {
    }

    
    public CompositeShape(ShapeProducer shape) {        

        m_source = shape.getShape();
        if(shape instanceof Parameterizable){
            super.addParams(((Parameterizable)shape).getParams());
        }
        //mp_source.setValue(m_source);
    }

    /**
     *
     * @noRefGuide
     */
    public int initialize() {

        super.initialize();

        if (m_source != null && m_source instanceof Initializable) {

            ((Initializable) m_source).initialize();
            super.m_channelsCount = m_source.getChannelsCount();

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
            m_source.getDataValue(pnt, data);
        } else {
            data.v[0] = 1.;
        }
        super.getMaterialDataValue(pnt, data);        
        return ResultCodes.RESULT_OK;
    }

} // class CompositeShape
