
/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
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

// External Imports

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;

/**
 * Test ImageUtil methods
 *
 * @author Alan Hudson
 */
public class TestImageUtil extends TestCase {
    public static final boolean DEBUG = true;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestImageUtil.class);
    }

    /**
     * Test that the spacing of input to output is the same
     */
    public void testUb2us() {
        //
        // distance between values shoule be 0x101 = 257
        //
        for(int i = 0; i < 255; i++){
            int s = ImageUtil.ub2us(i);
            int s1 = ImageUtil.ub2us(i+1);
            assertTrue(fmt("ub2us(0x%02x) = 0x%04x diff: 0x%04x", i, s, (s1-s)), (s1 - s) == 0x101);
            //printf("ub2us(0x%02x) = 0x%04x  ub2us(0x%02x) = 0x%04x  diff: 0x%04x\n", i, s, (i+1), s1, s1 - s);
            
        }
    }
}
