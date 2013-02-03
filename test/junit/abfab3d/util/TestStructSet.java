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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;


import abfab3d.mesh.PointSet;
import org.web3d.vrml.sav.BinaryContentHandler;

import abfab3d.io.input.IndexedTriangleSetLoader;
import abfab3d.io.input.STLReader;
import abfab3d.io.input.STLRasterizer;
import abfab3d.io.input.MeshRasterizer;
import abfab3d.io.output.SAVExporter;
import abfab3d.io.output.MeshExporter;
import abfab3d.io.output.IsosurfaceMaker;
import abfab3d.io.output.STLWriter;


import abfab3d.grid.Grid;
import abfab3d.grid.GridShortIntervals;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.ClassTraverser;
import abfab3d.grid.op.ErosionMask;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.j3d.geom.*;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.*;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;

import static java.lang.System.currentTimeMillis;

/**
 * Tests the functionality of a StructSet
 *
 * @author Alan Hudson
 * @version
 */
public class TestStructSet extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestStructSet.class);
    }

    public void testRegularHash() {
        DefaultHashFunction hasher = new DefaultHashFunction();
        StructSet ps = new StructSet(null,hasher);

        int maxID = 15;
        int[] ids = new int[] {
            13,14,15,2,0
        };

        for(int i=0; i < ids.length; i++) {
            ps.add(ids[i]);
        }

        for(int i=0; i < ids.length; i++) {
            assertTrue("Found: " + i, ps.contains(ids[i]));
        }

        for(int i=0; i < maxID * 2; i++) {
            boolean found = false;
            for(int j=0; j < ids.length; j++) {
                if (ids[j] == i) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                assertFalse("Not Find: " + i, ps.contains(i));
            }
        }
    }

    public void testDifferent() {
        DefaultHashFunction hasher = new DefaultHashFunction();
        StructSet ps = new StructSet(null,hasher);

        boolean id0 = ps.add(0);
        boolean id1 = ps.add(1);

        assertTrue("First Unique", id0);
        assertTrue("Second Unique", id1);

        int[] points = ps.keys();
        assertEquals("Array lengths", points.length, 2);
    }

    public void testDifferentCustomHash() {
        double TOLERANCE = 1.e-8;

        PointStruct hasher = new PointStruct(TOLERANCE);
        StructMixedData data = new StructMixedData(PointStruct.DEFINITION, 2);
        StructSet ps = new StructSet(data,hasher);

        double[] pos = new double[] {
                1,2,3,
                3,2,1
        };

        int pnt0 = PointStruct.create(pos[0],pos[1],pos[2], data);
        int pnt1 = PointStruct.create(pos[3],pos[4],pos[5], data);

        boolean id0 = ps.add(pnt0);
        boolean id1 = ps.add(pnt1);

        assertTrue("First Unique", id0);
        assertTrue("Second Unique", id1);

        int[] points = ps.keys();
        assertEquals("Array lengths", points.length, 2);
    }


    public void testSame() {
        double TOLERANCE = 1.e-8;

        PointStruct hasher = new PointStruct(TOLERANCE);
        StructMixedData data = new StructMixedData(PointStruct.DEFINITION, 2);
        StructSet ps = new StructSet(data,hasher);

        double[] pos = new double[] {
                1,2,3,
        };

        int pnt0 = PointStruct.create(pos[0],pos[1],pos[2], data);

        boolean id0 = ps.add(pnt0);
        boolean id1 = ps.add(pnt0);

        assertTrue("First Unique", id0);
        assertFalse("Second Not Unique", id1);

        double[] points = getPoints(ps, data);

        assertEquals("Array lengths", points.length, 3);

        System.out.println("Values: " + java.util.Arrays.toString(points));
        for(int i=0; i < points.length; i++) {
            assertEquals("val: " + i, pos[i], points[i]);
        }
    }

    public void testResize() {
        double TOLERANCE = 1.e-8;

        PointStruct hasher = new PointStruct(TOLERANCE);
        StructMixedData data = new StructMixedData(PointStruct.DEFINITION, 2);
        StructSet ps = new StructSet(data,hasher);

        int cnt = 13;
        for(int i=0; i < cnt; i++) {
            int pnt0 = PointStruct.create(1,2,i, data);
            assertTrue("Unique",ps.add(pnt0));
        }

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
    public double[] getPoints(StructSet set, StructMixedData data) {
        double[] ret_val = new double[set.size() * 3];

        int[] keys = set.keys();
        double[] pnt = new double[3];
        int idx = 0;

        for (int j = 0; j < keys.length; j++) {
            PointStruct.getPosition(data, keys[j],pnt);

            ret_val[idx++] = pnt[0];
            ret_val[idx++] = pnt[1];
            ret_val[idx++] = pnt[2];
        }

        return ret_val;
    }
}

class PointStruct extends StructDataDefinition implements HashFunction {
    public static final StructDataDefinition DEFINITION = new PointStruct();

    public static final int DOUBLE_DATA_SIZE = 3;

    // double positions
    public static final int POS_X = 0;
    public static final int POS_Y = 1;
    public static final int POS_Z = 2;

    static final double  // arbitrary constants for hashcode calculations
            CX = 10556796.789,
            CY = 26556797.891,
            CZ = 37556792.981,
            CW = 45556795.955;


    private double epsilon;

    public PointStruct() {
        this(1e-8);
    }

    public PointStruct(double epsilon) {
        this.epsilon = epsilon;
    }

    public static int create(StructMixedData dest) {
        int destIdx = dest.addItem();

        return destIdx;
    }

    public static int create(double x, double y, double z, StructMixedData dest) {
        int destIdx = dest.addItem();
        set(x,y,z,dest, destIdx);

        return destIdx;
    }

    public static void set(double x, double y, double z, StructMixedData dest, int destIdx) {
        int double_pos = destIdx * DOUBLE_DATA_SIZE;
        double[] double_data = dest.getDoubleData();

        double_data[double_pos + POS_X] = x;
        double_data[double_pos + POS_Y] = y;
        double_data[double_pos + POS_Z] = z;
    }

    public static void getPosition(StructMixedData src, int srcIdx, double[] pos) {
        int double_pos = srcIdx * DOUBLE_DATA_SIZE;
        double[] double_data = src.getDoubleData();

        pos[0] = double_data[double_pos +  + POS_X];
        pos[1] = double_data[double_pos +  + POS_Y];
        pos[2] = double_data[double_pos +  + POS_Z];
    }

    @Override
    public int calcHashCode(StructMixedData src, int srcIdx) {
        int double_pos = srcIdx * DOUBLE_DATA_SIZE;
        double[] double_data = src.getDoubleData();

        double x = double_data[double_pos + POS_X];
        double y = double_data[double_pos + POS_Y];
        double z = double_data[double_pos + POS_Z];

        return (int)(CX*x + CY * y + CZ * z + CW);
    }

    @Override
    public boolean calcEquals(StructMixedData src, int a, int b) {
        double[] double_data = src.getDoubleData();
        int double_pos = a * DOUBLE_DATA_SIZE;

        double ax = double_data[double_pos + POS_X];
        double ay = double_data[double_pos + POS_Y];
        double az = double_data[double_pos + POS_Z];

        double_pos = b * DOUBLE_DATA_SIZE;

        double bx = double_data[double_pos + POS_X];
        double by = double_data[double_pos + POS_Y];
        double bz = double_data[double_pos + POS_Z];

        double tmp = Math.max( Math.abs(ax-bx), Math.abs(ay-by));
        double d = Math.max(tmp,Math.abs(az-bz));

        if(d <= epsilon)
            return true;
        else
            return false;

    }

    public int getDoubleDataSize() {
        return DOUBLE_DATA_SIZE;
    }
}
