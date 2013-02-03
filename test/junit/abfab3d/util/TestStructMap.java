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

import java.util.Arrays;

/**
 * Tests the functionality of a StructMap
 *
 * @author Alan Hudson
 * @version
 */
public class TestStructMap extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestStructMap.class);
    }

    public void testRegularHash() {
        DefaultHashFunction hasher = new DefaultHashFunction();
        StructMap ps = new StructMap(null,hasher);

        int[] ids = new int[] {
//                13,14,15,2,0
                13,14
        };

        for(int i=0; i < ids.length; i++) {
            ps.put(ids[i],ids[i]);
        }

        for(int i=0; i < ids.length; i++) {
            assertTrue("Find: " + ids[i], ids[i] == ps.get(ids[i]));
        }
    }

    public void testDifferent() {
        double TOLERANCE = 1.e-8;

        PointStruct hasher = new PointStruct(TOLERANCE);
        StructMixedData data = new StructMixedData(PointStruct.DEFINITION, 2);
        StructMap ps = new StructMap(data,hasher);

        double[] pos = new double[] {
                1,2,3,
                3,2,1
        };

        int pnt0 = PointStruct.create(pos[0],pos[1],pos[2], data);
        int pnt1 = PointStruct.create(pos[3],pos[4],pos[5], data);

        int id0 = ps.put(pnt0,pnt0);
        int id1 = ps.put(pnt1,pnt1);

        assertTrue("Unique id0", id0 == -1);
        assertTrue("Unique id1", id1 == -1);

        int[] points = ps.entrySet();
        assertEquals("Array lengths", points.length, 2*2);
    }


    public void testSame() {
        double TOLERANCE = 1.e-8;

        PointStruct hasher = new PointStruct(TOLERANCE);
        StructMixedData data = new StructMixedData(PointStruct.DEFINITION, 2);
        StructMap ps = new StructMap(data,hasher);

        double[] pos = new double[] {
                1,2,3,
        };

        int pnt0 = PointStruct.create(pos[0], pos[1], pos[2], data);

        int id0 = ps.put(pnt0,pnt0);
        int id1 = ps.put(pnt0,pnt0);

        assertTrue("First Unique", id0 == -1);
        assertFalse("Second Not Unique", id0 == id1);

        double[] points = getPoints(ps, data);

        assertEquals("Array lengths", points.length, 3);

        System.out.println("Values: " + Arrays.toString(points));
        for(int i=0; i < points.length; i++) {
            assertEquals("val: " + i, pos[i], points[i]);
        }
    }

    public void testResize() {
        double TOLERANCE = 1.e-8;

        PointStruct hasher = new PointStruct(TOLERANCE);
        StructMixedData data = new StructMixedData(PointStruct.DEFINITION, 2);
        StructMap ps = new StructMap(data,hasher);

        int cnt = 13;
        int[] ids = new int[cnt];

        int pnt0 = -1;
        for(int i=0; i < cnt; i++) {
            pnt0 = PointStruct.create(1,2,i, data);
            ids[i] = ps.put(pnt0,pnt0);
            assertTrue("New",ids[i] == -1);

        }

        assertTrue("Overwrite", ps.put(pnt0,pnt0) != -1);

        double[] points = getPoints(ps, data);
        int len = points.length / 3;
        assertEquals("PointCount", cnt, len);

        boolean[] found = new boolean[cnt];

        for(int i=0; i < len; i++) {
            if ((points[i*3] == 0) && (points[i*3+1] == 0) && (points[i*3+2] == 0)) {
                fail("Zero value in array");
            }

            found[(int) points[i*3+2]] = true;
        }

        for(int i=0; i < len; i++) {
            if (!found[i]) {
                fail("Could not find point: " + i);
            }
        }
    }


    /**
     * Get a list of all points.  No ordering is enforced.
     *
     * @return
     */
    public double[] getPoints(StructMap set, StructMixedData data) {
        double[] ret_val = new double[set.size() * 3];

        int[] keys = set.entrySet();
        double[] pnt = new double[3];
        int idx = 0;

        for (int j = 0; j < keys.length / 2; j++) {
            PointStruct.getPosition(data, keys[j*2],pnt);

            ret_val[idx++] = pnt[0];
            ret_val[idx++] = pnt[1];
            ret_val[idx++] = pnt[2];
        }

        return ret_val;
    }
}

