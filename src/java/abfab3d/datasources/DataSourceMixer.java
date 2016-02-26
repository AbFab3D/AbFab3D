/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2016
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package abfab3d.datasources;


import abfab3d.param.Parameter;
import abfab3d.param.Parameterizable;
import abfab3d.param.SNodeListParameter;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.Vec;

import java.util.List;


/**

 combines data from multiple sources into single multidimensional DataSource

 @author Vladimir Bulatov

 */

public class DataSourceMixer extends TransformableDataSource {

    // plain array of sources 
    protected DataSource m_sources[];
    // count of sources 
    protected int m_count;
    // dimension of each data source 
    protected int m_channelsCounts[];

    SNodeListParameter mp_sources = new SNodeListParameter("sources");

    Parameter m_aparam[] = new Parameter[]{
            mp_sources
    };


    /**
     *  mixer with no data sources 
     */
    public DataSourceMixer() {
        addParams(m_aparam);

    }

    /**
     mixes single data sources
     */
    public DataSourceMixer(DataSource source1) {

        addParams(m_aparam);

        add(source1);
    }

    /**
     mixes two data sources
     */
    public DataSourceMixer(DataSource source1, DataSource source2) {

        addParams(m_aparam);

        add(source1);
        add(source2);
    }

    /**
     mixes three data sources
     */
    public DataSourceMixer(DataSource source1, DataSource source2, DataSource source3) {

        addParams(m_aparam);

        add(source1);
        add(source2);
        add(source3);
    }

    /**
     mixes four data sources
     */
    public DataSourceMixer(DataSource source1, DataSource source2, DataSource source3, DataSource source4) {

        addParams(m_aparam);

        add(source1);
        add(source2);
        add(source3);
        add(source4);
    }

    public void add(DataSource source) {

        mp_sources.add((Parameterizable) source);

    }

    /**
     @override
     */
    public int initialize() {

        super.initialize();

        List sources = mp_sources.getValue();
        m_count = sources.size();

        m_channelsCounts = new int[m_count];

        int ccount = 0;
        for (int i = 0; i < m_count; i++) {
            DataSource ds = (DataSource) sources.get(i);
            // initialize children 
            if (ds instanceof Initializable) {
                ((Initializable) ds).initialize();
            }
            // calculate total sources count which is sum of cahnnel counts of each source 
            m_channelsCounts[i] = ds.getChannelsCount();
            ccount += m_channelsCounts[i];
        }

        // total data dimension of this object 
        m_channelsCount = ccount;

        // store sources into plain array 
        m_sources = new DataSource[m_count];

        for (int i = 0; i < m_count; i++) {
            m_sources[i] = (DataSource) sources.get(i);
        }
        return RESULT_OK;
    }

    /**
     * @noRefGuide
     * @override
     */
    public int getDataValue(Vec pnt, Vec data) {

        super.transform(pnt);

        // TODO - reduce garbage collection 
        Vec sourceData = new Vec(m_count);

        for (int i = 0, channel = 0; i < m_count; i++) {
            m_sources[i].getDataValue(pnt, sourceData);
            int cc = m_channelsCounts[i];
            switch (cc) {
                case 1: // one dimensional source
                    data.v[channel] = sourceData.v[0];
                    channel++;
                    break;
                case 2: // two dimensional source
                    data.v[channel] = sourceData.v[0];
                    data.v[channel + 1] = sourceData.v[1];
                    channel += 2;
                    break;
                case 3: // three dimensional source
                    data.v[channel] = sourceData.v[0];
                    data.v[channel + 1] = sourceData.v[1];
                    data.v[channel + 2] = sourceData.v[2];
                    channel += 3;
                    break;
                default:
                    for (int k = 0; k < cc; k++) {
                        data.v[channel++] = sourceData.v[k];
                    }
                    break;
            }
        }
        return RESULT_OK;
    }
}  

