/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2011-2016
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package abfab3d.transforms;

import abfab3d.param.Parameter;
import abfab3d.param.SNodeParameter;
import abfab3d.util.*;


/**

 convert DataSource value into coordinate
 useful for example to convert distance data into color using ImageMap

 @author Vladimir Bulatov
 */
public class DataToCoordinate extends BaseTransform implements VecTransform, Initializable {

    DataSource m_source;
    SNodeParameter mp_source = new SNodeParameter("source");

    protected Parameter m_aparams[] = new Parameter[]{
            mp_source
    };

    /**
     creates empty
     */
    public DataToCoordinate() {
        addParams(m_aparams);
    }

    /**
     creates composite transform with single transform
     */
    public DataToCoordinate(DataSource source) {

        addParams(m_aparams);
        setSource(source);

    }

    /**
     set source used for coordinate
     */
    public void setSource(DataSource source) {

        mp_source.setValue(source);

    }

    /**
     @noRefGuide
     */
    public int initialize() {

        m_source = (DataSource) mp_source.getValue();
        if (m_source == null)
            m_source = new Zero();
        if (m_source instanceof Initializable) {
            int res = ((Initializable) m_source).initialize();
            return res;
        }
        return RESULT_OK;
    }

    /**
     @noRefGuide
     */
    public int transform(Vec in, Vec out) {

        m_source.getDataValue(in, out);
        return RESULT_OK;
    }

    /**
     @noRefGuide
     */
    public int inverse_transform(Vec in, Vec out) {

        m_source.getDataValue(in, out);
        return RESULT_OK;

    }

    static class Zero implements DataSource {
        public int getDataValue(Vec pnt, Vec dataValue) {
            dataValue.v[0] = 0;
            return RESULT_OK;
        }

        public int getChannelsCount() {
            return 1;
        }

        public Bounds getBounds() {
            return null;
        }

        public void setBounds(Bounds bounds) {
        }

    }
}  // class DataToCoordinate
