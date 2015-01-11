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
public class AttributeDesc  {

    AttributeMaker m_attributeMaker; 

    Vector<AttributeChannel> m_channels = new Vector<AttributeChannel>();

    public int size(){
        return m_channels.size();
    }
    
    public int getBitCount(){
        int cnt = 0;
        for(int i = 0; i < m_channels.size(); i++){
            AttributeChannel ac = m_channels.get(i);
            cnt += ac.getBitCount();
        }
        return cnt;
    }


    public AttributeChannel getChannel(int index){
        return m_channels.get(index);
    }
    
    public void addChannel(AttributeChannel channel){

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

    public static class DefaultAttributeMaker implements AttributeMaker {
        int resolution[];
        public DefaultAttributeMaker(AttributeDesc attDesc){
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


    public static AttributeDesc getDefaultAttributeDesc(){
        AttributeDesc at = new AttributeDesc();
        at.addChannel(new AttributeChannel(AttributeChannel.DENSITY, "dens", 8, 0));
        return at;
    }
}



