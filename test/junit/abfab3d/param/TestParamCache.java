/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2011
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package abfab3d.param;

// External Imports


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import static abfab3d.core.Output.time;
import static abfab3d.core.Output.printf;

/**
 * Tests the functionality of ParamCache
 *
 * @version
 */
public class TestParamCache extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestParamCache.class);
    }

    /**
     * Check relative hit/miss speeds.  Since its using Exceptions the misses will be much more expensive
     */
    public void _testMissSpeed() {
        ParamCache cache = ParamCache.getInstance();

        String key = "HelloWorld";
        String key2 = "GoodbyeWorld";

        cache.put(key2,key2);

        long TIMES = (long) 1e6;
        long t0 = time();
        for (int i = 0; i < TIMES; i++) {
            Object o = cache.get(key);
            if (o != null) {
                fail("WTF");
            }
        }
        long tot = time() - t0;
        printf("Miss Time: total: %d  avg: %f ms\n", tot,(float)tot/TIMES);

        t0 = time();
        for (int i = 0; i < TIMES; i++) {
            Object o = cache.get(key2);
            if (o == null) {
                fail("WTF");
            }
        }
        tot = time() - t0;
        printf("Hit Time: total: %d  avg: %f ms\n", tot,(float)tot/TIMES);
    }

    public void testBasic() {
        String key = "KEY";
        String val = "VAL";

        ParamCache cache = ParamCache.getInstance();
        cache.put(key,val);

        Object o = cache.get(key);
        assertTrue("not found",(o == val));
    }


}
