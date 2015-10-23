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

import static abfab3d.util.Output.printf;
import static abfab3d.util.MathUtil.clamp;

/**
 * A description of a single channel of a grid attribute
 *  data are treated as unsigned values 
 * @author Vladimir Bulatov
 */
public class AttributeChannelSigned  extends AttributeChannel {
    
    public AttributeChannelSigned(String type, String name, int bits, int shift){
        super(type, name, bits, shift);
    }
    public AttributeChannelSigned(String type, String name, int bits, int shift, double maxValue){
         super(type, name, bits, shift);
         m_maxDoubleValue = maxValue;
         m_conversionFactor = maxValue/((1<<(bits-1))-1);
         printf("m_conversionFactor: %7.5f", m_conversionFactor);
     }

     /**
        extracts value of this channel from the attribute  
        @Override         
     */
     public long getBits(long att){

         
         long value = (att >> m_shift) & m_mask;
         if((value & m_signMask) != 0){
             value = value | m_complementMask;
         }
         return value;
     }

     public double getValue(long att){
         return m_conversionFactor*getBits(att);
    }
    
    /**
       convert double value into attribute bits
    */
    public long makeBits(double value){
        value = clamp(value, -m_maxDoubleValue, m_maxDoubleValue);
        //printf("value: %7.5f \n", value);
        return (((long)Math.floor(value/m_conversionFactor + 0.5)) & m_mask) << m_shift;
    }

}

