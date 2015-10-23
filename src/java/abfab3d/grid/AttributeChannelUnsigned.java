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

import abfab3d.util.LongConverter;
import abfab3d.util.MathUtil;


/**
 * A description of a single channel of a grid attribute
 *  data are treated as unsigned values 
 * @author Vladimir Bulatov
 */
public class AttributeChannelUnsigned  extends AttributeChannel {
    
     public AttributeChannelUnsigned(String type, String name, int bits, int shift){
         super(type, name, bits, shift);
     }

     /**
        extracts bits value of this channel from the attribute  
        @Override         
     */
     public long getBits(long att){

         return (att >> m_shift) & m_mask;

     }

     public double getValue(long att){
         return m_conversionFactor*getBits(att);
    }

    /**
       convert double value ino attribute bits
    */
    public long makeBits(double value){
        return (((long)(value/m_conversionFactor + 0.5))& m_mask) << m_shift;
    }

}

