package abfab3d.grid;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.Collections;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public class TestVoxelCoordinate extends BaseTestAttributeGrid {
        /**
         * Creates a test suite consisting of all the methods that start with "test".
         */
        public static Test suite() {
            return new TestSuite(TestVoxelCoordinate.class);
        }

        /**
         * Test setGrid.
         */
        public void testHashCode() {
            int x = 1;
            int y = 10;
            int z = 20;

            VoxelCoordinate wc1 = new VoxelCoordinate(x, y, z);

            int hc1 = wc1.hashCode();

            x = x * 2;

            VoxelCoordinate wc2 = new VoxelCoordinate(x, y, z);
            int hc2 = wc2.hashCode();

            assertFalse("HashCode should not be equal", hc1 == hc2);
        }

        public void testEquals() {
            int x = 2;
            int y = 4;
            int z = 55;

            VoxelCoordinate wc1 = new VoxelCoordinate(x, y, z);
            VoxelCoordinate wc2 = new VoxelCoordinate(x+1, y, z);

            assertFalse("WC1 should not be equal to WC2", wc1.equals(wc2));

            wc2 = new VoxelCoordinate(2, 4, 55);

            assertTrue("WC1 is not equal to WC2", wc1.equals(wc2));
        }

        public void testSort() {
            ArrayList<VoxelCoordinate> list = new ArrayList<VoxelCoordinate>();
/*
            VoxelCoordinate vc1 = new VoxelCoordinate(0,0,0);
            VoxelCoordinate vc2 = new VoxelCoordinate(10,10,10);
            VoxelCoordinate vc3 = new VoxelCoordinate(5,5,5);
            VoxelCoordinate vc4 = new VoxelCoordinate(7,7,7);
            VoxelCoordinate vc5 = new VoxelCoordinate(0,0,2);
            VoxelCoordinate vc6 = new VoxelCoordinate(5,6,12);
            VoxelCoordinate vc7 = new VoxelCoordinate(1,1,11);
  */

            VoxelCoordinate vc1 = new VoxelCoordinate(0,38,24);
            VoxelCoordinate vc2 = new VoxelCoordinate(1,8,23);
            VoxelCoordinate vc3 = new VoxelCoordinate(0,39,24);

            list.add(vc1);
            list.add(vc2);
            list.add(vc3);
/*
            list.add(vc4);
            list.add(vc5);
            list.add(vc6);
            list.add(vc7);
  */
            Collections.sort(list);

            System.out.println("Sorted list: ");
            for(VoxelCoordinate vc : list) {
                System.out.println(vc);
            }
/*
            int idx = 0;
            assertTrue("Position " + idx + " wrong",vc1.equals(list.get(idx++)));
            assertTrue("Position " + idx + " wrong",vc5.equals(list.get(idx++)));
            assertTrue("Position " + idx + " wrong", vc3.equals(list.get(idx++)));
            assertTrue("Position " + idx + " wrong", vc6.equals(list.get(idx++)));
            assertTrue("Position " + idx + " wrong", vc4.equals(list.get(idx++)));
            assertTrue("Position " + idx + " wrong", vc2.equals(list.get(idx++)));
*/
        }
}
