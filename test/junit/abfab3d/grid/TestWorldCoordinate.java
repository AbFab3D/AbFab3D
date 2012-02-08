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

// External Imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Internal Imports

/**
 * Tests the functionality of a OccupiedWrapper
 *
 * @author Alan Hudson
 * @version
 */
public class TestWorldCoordinate extends BaseTestGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestWorldCoordinate.class);
    }

    /**
     * Test setGrid.
     */
    public void testHashCode() {
    	float x = 2.5f;
    	float y = 10.1f;
    	float z = 55f;
    	
        WorldCoordinate wc1 = new WorldCoordinate(x, y, z);
        
        int hc1 = wc1.hashCode();

        x = x * 2;

        WorldCoordinate wc2 = new WorldCoordinate(x, y, z);
        int hc2 = wc2.hashCode();
        
        assertFalse("HashCode should not be equal", hc1 == hc2);
    }
    
    public void testEquals() {
    	float x = 2.5f;
    	float y = 10.1f;
    	float z = 55f;
    	
    	WorldCoordinate wc1 = new WorldCoordinate(x, y, z);
    	WorldCoordinate wc2 = new WorldCoordinate(10.1f, 55f, 2.5f);
    	
    	assertFalse("WC1 should not be equal to WC2", wc1.equals(wc2));
    	
    	wc2 = new WorldCoordinate(2.5f, 10.1f, 55f);
    	
    	assertTrue("WC1 is not equal to WC2", wc1.equals(wc2));
    }


}
