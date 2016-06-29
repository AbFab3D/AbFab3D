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

import static abfab3d.core.Output.fmt;

/**
   class to test attribute value agains specified testValue
   @author Vladimir Bulatov
 */
public class AttributeTesterValue implements AttributeTester {
    
    // value to test against
    protected long testValue;

    public AttributeTesterValue(long testValue){
        this.testValue = testValue;
    }
    /**
       @param value to test 
       @return true if value is good and false otherwise  
    */
    public final boolean test(int x, int y, int z, long value){
        return (value == testValue);
    }

    public String toString(){
        return fmt("AttributeTesterValue(%d)", testValue);
    }

}