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


package abfab3d.grid;

import java.util.Vector;

import abfab3d.util.Vec;

/**
 * A description of a grid attribute
 *
 * @author Vladimir Bulatov
 */
public class GridDataDesc {

    AttributeMaker m_attributeMaker; 

    public GridDataDesc(){
    }

    public GridDataDesc(GridDataChannel channel){
        addChannel(channel);
    }

    public GridDataDesc(GridDataChannel channel1, GridDataChannel channel2){
        addChannel(channel1);
        addChannel(channel2);
    }

    public GridDataDesc(GridDataChannel channel1, GridDataChannel channel2, GridDataChannel channel3){
        addChannel(channel1);
        addChannel(channel2);
        addChannel(channel3);
    }

    public GridDataDesc(GridDataChannel channel1, GridDataChannel channel2, GridDataChannel channel3, GridDataChannel channel4){
        addChannel(channel1);
        addChannel(channel2);
        addChannel(channel3);
        addChannel(channel4);
    }

    Vector<GridDataChannel> m_channels = new Vector<GridDataChannel>();

    public int size(){
        return m_channels.size();
    }
    
    public int getBitCount(){
        int cnt = 0;
        for(int i = 0; i < m_channels.size(); i++){
            GridDataChannel ac = m_channels.get(i);
            cnt += ac.getBitCount();
        }
        return cnt;
    }


    public GridDataChannel getChannel(int index){
        return m_channels.get(index);
    }

    public GridDataChannel getChannelWithType(String channelType){
        
        for(int i = 0; i < m_channels.size(); i++){
            GridDataChannel channel = m_channels.get(i);
            if(channel.getType().equals(channelType))
                return channel;
        }
        return null;
    }
    
    public void addChannel(GridDataChannel channel){

        m_channels.add(channel);

    }

    public AttributeMaker getAttributeMaker(){

        if(m_attributeMaker == null) {
            // TODO make real attribute maker 
            // this is temp hack !!!
            m_attributeMaker = new AttributeMakerDensity(255);
        }
        //m_attributeMaker = new DefaultAttributeMaker(this);

        return m_attributeMaker;

    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("AttributeDesc[");
        for(int i = 0; i < m_channels.size(); i++){
            sb.append(m_channels.get(i).toString());
            if(i < m_channels.size()-1)sb.append(",");
        }
        sb.append("]");        
        return sb.toString();
    }

    public static class DefaultAttributeMaker implements AttributeMaker {
        int resolution[];
        public DefaultAttributeMaker(GridDataDesc attDesc){
            resolution = new int[attDesc.size()];
            for(int i = 0; i < resolution.length;i++){
                //TODO 
            }
        }
        
        public long makeAttribute(Vec v){
            //TODO 
            return 0;
        }        
    }

    public GridDataChannel getDensityChannel() {
        for(int i = 0; i < m_channels.size(); i++){
            GridDataChannel ac = m_channels.get(i);
            if(GridDataChannel.DENSITY.equals(ac.m_type))
                return ac;
        }
        return null;
    }

    public GridDataChannel getDefaultChannel() {
        return getChannel(0);
    }

    /**
       creates default attrinute descrition with single 8 bits density channel) 
     */
    public static GridDataDesc getDefaultAttributeDesc(int bitCount){
        GridDataDesc at = new GridDataDesc();
        at.addChannel(new GridDataChannel(GridDataChannel.DENSITY, "density", bitCount, 0, 0., 1.));
        return at;
    }

    /**
       creates AttributeDesc with color+density channel 
     */
    public static GridDataDesc getDensityColor(){

        return new GridDataDesc(new GridDataChannel(GridDataChannel.DENSITY_COLOR, "density_color", 32, 0, 0., 1.));

    }
}



