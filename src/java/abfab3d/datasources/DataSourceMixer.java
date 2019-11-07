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


import abfab3d.core.ResultCodes;
import abfab3d.param.Parameter;
import abfab3d.param.Parameterizable;
import abfab3d.param.SNodeListParameter;
import abfab3d.core.DataSource;
import abfab3d.core.Initializable;
import abfab3d.core.Vec;

import java.util.List;

import static abfab3d.core.Output.printf;

/**

 combines data from multiple sources into single multidimensional DataSource

 @author Vladimir Bulatov

 */

public class DataSourceMixer extends TransformableDataSource {

    static final boolean DEBUG = false;

    // plain array of sources 
    protected DataSource m_sources[];
    // count of sources 
    protected int m_count;
    // dimension of each data source 
    protected int m_channelsCounts[];
    protected int m_maxChannelsCount = 1;

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

    /**
       mixes five sources
     */
    public DataSourceMixer(DataSource source1, DataSource source2, DataSource source3, DataSource source4, DataSource source5 ) {

        addParams(m_aparam);

        add(source1);
        add(source2);
        add(source3);
        add(source4);
        add(source5);

    }

    /**
       mixes six sources
     */
    public DataSourceMixer(DataSource source1, DataSource source2, DataSource source3, DataSource source4, 
                           DataSource source5,DataSource source6 ) {

        addParams(m_aparam);

        add(source1);
        add(source2);
        add(source3);
        add(source4);
        add(source5);
        add(source6);

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
            if(DEBUG){
                printf("m_channelsCounts[%d]: %d\n",i, m_channelsCounts[i]);
            }
            if(m_channelsCounts[i] > m_maxChannelsCount)
                m_maxChannelsCount = m_channelsCounts[i];
        }

        // total data dimension of this object 
        m_channelsCount = ccount;

        // store sources into plain array 
        m_sources = new DataSource[m_count];

        for (int i = 0; i < m_count; i++) {
            m_sources[i] = (DataSource) sources.get(i);
        }
        if(DEBUG){
            printf("DataSourceMixer m_count: %d, m_channelsCount: %d m_maxChannelsCount:%d\n", m_count, m_channelsCount, m_maxChannelsCount);
            for(int i = 0; i < m_count; i++){
                printf("source %d, dim: %d: %s\n", i, m_channelsCounts[i],m_sources[i]); 
            }
        }

        m_bounds = m_sources[0].getBounds();

        return ResultCodes.RESULT_OK;
    }

    /**
     * @noRefGuide
     * @override
     */
    public int getBaseValue(Vec pnt, Vec data) {

        // TODO - reduce garbage collection 
        //if(DEBUG) printf("m_maxChannelsCount:%d",m_maxChannelsCount);
        Vec sourceData = new Vec(m_maxChannelsCount);
        Vec sourcePnt = new Vec(pnt);
        //if(DEBUG && debugCount-- > 0) {
        //    printf("%s m_count\n", this, m_count)
        //}
            
        for (int i = 0, channel = 0; i < m_count; i++) {
            sourcePnt.set(pnt);
            m_sources[i].getDataValue(sourcePnt, sourceData);
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
        return ResultCodes.RESULT_OK;
    }
}  

