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

package abfab3d.util;

import static abfab3d.core.Output.fmt;

/**
   class to test long value agains specified range
   @author Vladimir Bulatov
 */
public class LongTesterRange implements LongTester {
    
    // value to test against
    protected long minValue;
    protected long maxValue;

    /**
       @param minValue minimal possible value (inlusive)
       @param maxValue maximal possible value (inlusive)
     */
    public LongTesterRange(long minValue, long maxValue){
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public boolean test(long value){
        return (value >= minValue && value <= maxValue);
    }

    public String toString(){
        return fmt("LongTesterRange(%d,%d)", minValue,maxValue);
    }
}