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

import static abfab3d.util.Output.fmt;

/**
   attribute channel to represent float as int bits 
 */
public class GridDataChannelFloat extends GridDataChannel {

    public GridDataChannelFloat(String type, String name, int shift){
        super(type, name, 32, shift);
    }

    public String toString(){
        return  fmt("AttribiuteChannelFloat(%s:%s)", getType(),  getName());
    }

    public double getValue(long attribute){

        return Float.intBitsToFloat((int)getBits(attribute));

    }

    public final long makeAtt(double value){
        return Float.floatToIntBits((float)value) << m_shift;
    }
}