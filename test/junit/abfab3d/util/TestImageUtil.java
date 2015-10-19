
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
        printf("ub2us 0: %5d 1: %5d  2: %5d 127: %5d  128: %5d 254: %5d 255: %5d\n", ImageUtil.ub2us(0), ImageUtil.ub2us(1),ImageUtil.ub2us(2),ImageUtil.ub2us(127),ImageUtil.ub2us(0x80),ImageUtil.ub2us(0xFE),ImageUtil.ub2us(0xFF));

        int last = -1;
        for(int i=0; i < 127; i++) {
            short val = ImageUtil.ub2us((byte)(0xFF & i));
            if (last == -1) {
                last = val;
            } else {
                assertTrue("same dist.  idx: " + i + " last: " + last + " this: " + val, last <= val);
            }
        }
    }

}
