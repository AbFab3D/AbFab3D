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

package abfab3d;

// External Imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Internal Imports


/**
 * Base Test Case
 *
 * @author Alan Hudson
 * @version
 */
public class BaseTestCase extends TestCase {
    public void assertArrayEquals(String msg, float[] expected, float[] actual, float eps) {
        if (expected.length != actual.length)
            fail(msg + " -> AssertEquals array lengths not equal");

        for(int i=0; i < expected.length; i++) {
            assertEquals(msg + " idx: " + i, expected[i], actual[i], eps);
        }
    }

    public void assertArrayEquals(String msg, int[] expected, int[] actual) {
        if (expected.length != actual.length)
            fail(msg + " -> AssertEquals array lengths not equal");

        for(int i=0; i < expected.length; i++) {
            assertEquals(msg + " idx: " + i, expected[i], actual[i]);
        }
    }
    
    public void assertArrayEquals(String msg, float[] expected, float[] actual) {
        if (expected.length != actual.length)
            fail(msg + " -> AssertEquals array lengths not equal");

        for(int i=0; i < expected.length; i++) {
            assertEquals(msg + " idx: " + i, expected[i], actual[i]);
        }
    }
    
    public boolean inArray(int[] list, int val) {
    	if (list == null)
    		return false;
    	
    	for (int i=0; i<list.length; i++) {
    		if (list[i] == val)
    			return true;
    	}
    	
    	return false;
    }
    
    public boolean arrayInArray(int[][] list, int[] val) {
    	if (list == null)
    		return false;
    	
		for (int i=0; i<list.length; i++) {
			if (list[i][0] == val[0] && 
				list[i][1] == val[1] &&
				list[i][2] == val[2]) {
				
				return true;
			}
		}
		
    	return false;
    }
}
