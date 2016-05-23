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

package abfab3d.opencl;

// External Imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.UUID;

// Internal Imports

/**
 * Tests the functionality of CLResourceManager
 *
 * @author Alan Hudson
 * @version
 */
public class TestCLResourceManager extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestCLResourceManager.class);
    }

    public void testAdd() {
        TestResource tr1 = new TestResource("tr1",1);
        TestResource tr2 = new TestResource("tr2",1);
        TestResource tr3 = new TestResource("tr3",1);

        CLResourceManager rm = CLResourceManager.getInstance(UUID.randomUUID().timestamp(),1);

        rm.add(tr1,tr1.getSize());
        rm.add(tr2,tr2.getSize());
        rm.add(tr3,tr3.getSize());

        assertTrue("tr1 not gone", !rm.isResident(tr1));
        assertTrue("tr2 gone", !rm.isResident(tr2));
        assertTrue("tr3", rm.isResident(tr3));
    }

    public void testAging() {
        CLResourceManager rm = CLResourceManager.getInstance(UUID.randomUUID().timestamp(),1, 10);

        TestResource tr1 = new TestResource("tr1",1);
        rm.add(tr1,tr1.getSize());

        try { Thread.sleep(500); } catch(Exception e) {}

        assertTrue("tr1 not gone", !rm.isResident(tr1));
    }

    public void testInsureCapacity() {
        TestResource tr1 = new TestResource("tr1",1);
        TestResource tr2 = new TestResource("tr2",1);
        TestResource tr3 = new TestResource("tr3",1);

        CLResourceManager rm = CLResourceManager.getInstance(UUID.randomUUID().timestamp(),3);

        rm.add(tr1,tr1.getSize());
        rm.add(tr2,tr2.getSize());

        rm.insureCapacity(3);
        assertTrue("tr1 gone", !rm.isResident(tr1));
        assertTrue("tr2 gone", !rm.isResident(tr2));

        rm.add(tr3,tr3.getSize());

        assertTrue("tr3", rm.isResident(tr3));
    }

    static class TestResource implements Resource {
        private String name;
        private long size;

        private boolean released;

        public TestResource(String name, long size) {
            this.name = name;
            this.size = size;
        }

        public void release() {
            if (released) throw new IllegalArgumentException("Already released");
        }
        public boolean isReleased() {
            return released;
        }
        public long getSize() {
            return size;
        }

        public String toString() {
            return "Resource.  name: " + name + " size: " + size + " released: " + released;
        }
    }
}
