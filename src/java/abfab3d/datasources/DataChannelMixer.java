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

import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;
import javax.vecmath.AxisAngle4d;


import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;

import abfab3d.util.PointToTriangleDistance;

import static java.lang.Math.sqrt;
import static java.lang.Math.atan2;
import static java.lang.Math.abs;

import static abfab3d.util.Output.printf;


import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.intervalCap;
import static abfab3d.util.MathUtil.step10;
import static abfab3d.util.MathUtil.step01;

import static abfab3d.util.Units.MM;


/**

   Data multiplexer combines several channels of data channels into single multidemensional data source
   
   @author Vladimir Bulatov

 */

public class DataChannelMixer extends TransformableDataSource {
        
    
    protected Vector<DataSource> m_vchannels = new Vector<DataSource>();
    
    protected DataSource m_channels[];
    protected int m_count;
    protected int m_channelsCounts[]; // dimension of each data source 
    
    /**
     *
     */
    public DataChannelMixer(){

    }

    public DataChannelMixer(DataSource channel1, DataSource channel2){
        m_vchannels.add(channel1);
        m_vchannels.add(channel2);
    }

    public DataChannelMixer(DataSource channel1, DataSource channel2,DataSource channel3){
        m_vchannels.add(channel1);
        m_vchannels.add(channel2);
        m_vchannels.add(channel3);
    }

    public DataChannelMixer(DataSource channel1, DataSource channel2,DataSource channel3,DataSource channel4){
        m_vchannels.add(channel1);
        m_vchannels.add(channel2);
        m_vchannels.add(channel3);
        m_vchannels.add(channel4);
    }
    
    public void addDataChannel(DataSource channel){
        m_vchannels.add(channel);
    }

    /**
       @override 
     */
    public int initialize(){

        super.initialize();
        

        m_channelsCounts = new int[m_vchannels.size()];        
        m_count = m_vchannels.size();
        
        int ccount = 0;
        for(int i = 0; i < m_count; i++){
            DataSource ds = m_vchannels.get(i);
            // initialize children 
            if(ds instanceof Initializable){
                ((Initializable)ds).initialize();
            }
            // calculate total channels count which is sum of cahnnel counts of each source 
            m_channelsCounts[i] = ds.getChannelsCount();
            ccount += m_channelsCounts[i];
        }
        
        // total dimension of date of this object 
        m_channelsCount = ccount;

        // store channels into plain array 
        m_channels = new DataSource[m_count];

        for(int i = 0; i < m_count; i++){
            m_channels[i] = m_vchannels.get(i);
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
        Vec channelData = new Vec(m_count);

        for(int i = 0, channel = 0; i < m_count; i++){
            m_channels[i].getDataValue(pnt, channelData);
            int cc = m_channelsCounts[i];
            switch(cc) {
            case 1: // one dimensional channel 
                data.v[channel++] = channelData.v[0]; break;
            default:
                for(int k =0; k < cc; k++){
                    data.v[channel++] = channelData.v[k];
                }
                break;
            }        
        }
        return RESULT_OK;
    }    
}  

