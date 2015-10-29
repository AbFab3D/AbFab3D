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
 *
 * @author Vladimir Bulatov
 */
public class AttributeChannel  implements LongConverter { // , ValueMaker { 
    
    // standard chnnel types
    public static final String DENSITY = "DENSITY";
    public static final String COLOR = "COLOR";
    public static final String MATERIAL = "MATERIAL";
    public static final String DISTANCE = "DISTANCE";

    
    
    // name of the channel 
    String m_name;
    // type of the channel 
    String m_type;
     // shift to move bits toward origin 
     int m_shift;
     // bit count of the channel 
     int m_bits;
    // mask to get sign bit 
    long m_signMask;
    long m_complementMask;
     // bitmask to extract channel bits from unsigned long
    long m_mask;
    long m_maxLongValue;
    double m_maxValue;
    double m_minValue;
    double m_value0;
    double m_value1;
    double m_D2B; // Double -> Bits conversion factor 
    double m_B2D; // Bits -> Double conversion factor 

    /**
       attribute channel stores data in given number of bits 
       the physical value is interpolated between value1 and value 2
       value with bits 0000 is mapped to value0
       value bits with valus 1111 is maped to value1 
       
     */
    public AttributeChannel(String type, String name, int bits, int shift, double value0, double value1){
        m_type = type;
        m_name = name;
        m_shift = shift;
        m_bits = bits;
        m_mask = MathUtil.getBitMask(bits);
        m_maxLongValue = (1 << bits)-1;
        if(value1 > value0){
            m_maxValue = value1;
            m_minValue = value0;
        } else if(value1 < value0){
            m_maxValue = value0;
            m_minValue = value1;
        } else {
            throw new RuntimeException("(value1 == value0) is not allowed");
        }
        m_value0 = value0;
        m_value1 = value1;
        m_D2B = m_maxLongValue/(value1 - value0);        
        m_B2D = 1./m_D2B;

    }

    public AttributeChannel(String type, String name, int bits, int shift){
        this(type, name, bits, shift, 0., 1.);
    }
    
     /**
        bit count stored in the channel 
      */
     public int getBitCount(){

         return m_bits;

     }

     /**
        sets shift used to move bits toward origin 
      */
     public void setShift(int shift){
         m_shift = shift;
     }

     /**
        type of the channel. Return one of the standard types 
      */
     public String getType(){
         return m_type;
     }

    public String getName(){
        return m_name;
    }
    
    public String toString(){
        return  getType() + ":" + getName() + ":" + getBitCount();
    }

    /**
       method of interface LongConverter 
    */
    public long get(long att){
        return getBits(att);
    }

    /**
       convert attribute bits into double value  
    */
    public double getValue(long attribute){
         return m_B2D*getBits(attribute)+m_value0;
    }

    /**
       extract value bits out of attribute 
    */
     public long getBits(long att){

         return (att >> m_shift) & m_mask;
         
     }
    // code to get bits in signed version 
    public long getBits_signed(long att){
            
        long value = (att >> m_shift) & m_mask;
        //if((value & m_signMask) != 0){
        //    value = value | m_complementMask;
        //}
        return value;
    }
    
    /**
       convert double value into attribute bits
    */
    public long makeAtt(double value){
        value = (clamp(value, m_minValue, m_maxValue)-m_value0)*m_D2B;
        return (((long)(value + 0.5))& m_mask) << m_shift;
    }
    
}

