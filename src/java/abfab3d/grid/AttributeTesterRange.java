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

package abfab3d.grid;

import static abfab3d.util.Output.fmt;

/**
   class to test attribute value agains specified range
   @author Vladimir Bulatov
 */
public class AttributeTesterRange implements AttributeTester {
    
    // value to test against
    protected long minValue;
    protected long maxValue;

    /**
       @param minValue minimal possible value (inclusive)
       @param maxValue maximal possible value (inclusive)
     */
    public AttributeTesterRange(long minValue, long maxValue){
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public final boolean test(int x, int y, int z, long value){
        return (value >= minValue && value <= maxValue);
    }

    public String toString(){
        return fmt("AttributeTesterRange(%d,%d)", minValue,maxValue);
    }
}