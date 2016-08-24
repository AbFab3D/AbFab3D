/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.core;


/**
   interface to convert data stored in long attribute into double value and back 
   can be used to get some component of data stored as bits in the grid attribute
 */
public interface AttributeConverter  {

    /**
       return data custom data component stored in long attribute
     */
    public double getValue(long attribute);
    /**
       converts from double value into attribute value 
     */
    public long makeAtt(double value);

    
}

