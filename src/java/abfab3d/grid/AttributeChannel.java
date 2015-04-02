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
 *
 * @author Vladimir Bulatov
 */
public class AttributeChannel  implements LongConverter,DensityMaker {
    
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
     // bitmask to extract channel bits from long
     long m_mask;
     // maximal value to be stored in the chanel  
     long m_maxValue;
     double m_conversionFactor;

     public AttributeChannel(String type, String name, int bits, int shift){

         m_type = type;
         m_name = name;
         m_shift = shift;
         m_bits = bits;
         m_mask = MathUtil.getBitMask(bits);
         m_maxValue = (1 << bits)-1;
         m_conversionFactor = 1./m_maxValue;
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

     /**
        extracts value of this channel from the attribute  
        @Override         
     */
     public long get(long att){

         return (att >> m_shift) & m_mask;

     }

     public double makeDensity(long att){
         return m_conversionFactor*get(att);
    }
}

