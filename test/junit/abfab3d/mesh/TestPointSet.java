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

package abfab3d.mesh;

// External Imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;


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
 * Tests the functionality of PointSet
 *
 * @author Alan Hudson
 * @version
 */
public class TestPointSet extends TestCase {
    private static final boolean DEBUG = false;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestPointSet.class);
    }

    public void testDifferent() {
        double TOLERANCE = 1.e-8;
        PointSet ps = new PointSet(TOLERANCE);

        double[] pos = new double[] {
                1,2,3,
                3,2,1
        };

        int id0 = ps.add(pos[0],pos[1],pos[2]);
        int id1 = ps.add(pos[3],pos[4],pos[5]);

        assertTrue("Different ID's", id0 != id1);

        double[] points = ps.getPoints();
        assertEquals("Array lengths", points.length, 2*3);
    }


    public void testSame() {
        double TOLERANCE = 1.e-8;
        PointSet ps = new PointSet(4000000, 0.75f,TOLERANCE);

        double[] pos = new double[] {
                -0.0425,-0.0429,-0.0060,
                -0.0425,-0.0429,-0.0060
        };

        int id0 = ps.add(pos[0],pos[1],pos[2]);
        int id1 = ps.add(pos[3],pos[4],pos[5]);
        int id3 = ps.add(pos[3],pos[4],pos[5]);

        assertEquals("Same ID's", id0,id1);

        double[] points = ps.getPoints();
        assertEquals("Array lengths", points.length, 3);

        if (DEBUG) {
            System.out.println("Values: " + java.util.Arrays.toString(points));
            for(int i=0; i < points.length; i++) {
                assertEquals("val: " + i, pos[i], points[i]);
            }
        }
    }

    public void testGetPoints() {
        double TOLERANCE = 1.e-8;
        PointSet ps = new PointSet(4000000, 0.75f,TOLERANCE);

        double[] pos = new double[] {
                -0.0425,-0.0429,-0.0060,
                -0.0425,-0.0429,-0.0060,
                -0.0425,-0.0429,0.0060,
                -0.0425,-0.0429,0.0060
                -0.042100000000000005,-0.0431,-0.0060
        };

        for(int i=0; i < pos.length / 3; i++) {
            int id0 = ps.add(pos[i*3+0],pos[i*3 + 1],pos[i*3 + 2]);
        }

        double[] points = ps.getPoints();
        assertEquals("Array lengths", 9, points.length);

        if (DEBUG) System.out.println("Values: " + java.util.Arrays.toString(points));
    }

    public void testGetPoints2() {
        double TOLERANCE = 1.e-8;
        PointSet ps = new PointSet(4000000, 0.75f,TOLERANCE);

        ps.add(-0.0189,-0.0508,-0.0059);
        ps.add(-0.0187,-0.0507,-0.0060);
        ps.add(-0.0187,-0.0507,-0.0060);

        double[] points = ps.getPoints();

        if (DEBUG) System.out.println("Values: " + java.util.Arrays.toString(points));

        int len = points.length / 3;

        for(int i=0; i < len; i++) {
            for(int j=0; j < len; j++) {
                if (i == j) continue;

                if (Math.abs(points[i*3+0] - points[j*3+0]) < TOLERANCE
                        && Math.abs(points[i*3+1] - points[j*3+1]) < TOLERANCE &&
                        Math.abs(points[i*3+2] - points[j*3+2]) < TOLERANCE) {

                    System.out.println("Points: " + i + " j:" + j);
                    System.out.println("Point 1: " + points[i*3] + " " + points[i*3+1] + " " + points[i*3+2]);
                    System.out.println("Point 2: " + points[j*3] + " " + points[j*3+1] + " " + points[j*3+2]);
                    fail("Duplicate Points");
                }
            }
        }
    }

    public void testGetPoints3() {
        double TOLERANCE = 1.e-8;

        PointSet ps = new PointSet(11, 1.0f,TOLERANCE);

        ps.add(-0.018500000000000003,-0.0507,-0.0060);
        ps.add(-0.018100000000000005,-0.0505,-0.0060);

        double[] points = ps.getPoints();

        if (DEBUG) System.out.println("Values: " + java.util.Arrays.toString(points));

        int len = points.length / 3;

        for(int i=0; i < len; i++) {

            if ((points[i*3] == 0) && (points[i*3+1] == 0) && (points[i*3+2] == 0)) {
                fail("Incorrect value in array");
            }

            for(int j=0; j < len; j++) {
                if (i == j) continue;

                if (Math.abs(points[i*3+0] - points[j*3+0]) < TOLERANCE
                        && Math.abs(points[i*3+1] - points[j*3+1]) < TOLERANCE &&
                        Math.abs(points[i*3+2] - points[j*3+2]) < TOLERANCE) {

                    System.out.println("Points: " + i + " j:" + j);
                    System.out.println("Point 1: " + points[i*3] + " " + points[i*3+1] + " " + points[i*3+2]);
                    System.out.println("Point 2: " + points[j*3] + " " + points[j*3+1] + " " + points[j*3+2]);
                    fail("Duplicate Points");
                }
            }
        }
    }

    public void testGetPoints4() {
        double TOLERANCE = 1.e-8;

        PointSet ps = new PointSet(10, 1.0f,TOLERANCE);

        ps.add(-0.018500000000000003,-0.0507,-0.0060);
        ps.add(-0.0187,-0.0508,-0.0059);
        ps.add(-0.0187,-0.0508,-0.0059);
        ps.add(-0.018500000000000003,-0.0507,-0.0060);
        ps.add(-0.018500000000000003,-0.0508,-0.0059);
        ps.add(-0.018500000000000003,-0.0507,-0.0060);
        ps.add(-0.018300000000000004,-0.0507,-0.0060);
        ps.add(-0.018500000000000003,-0.0508,-0.0059);
        ps.add(-0.018500000000000003,-0.0508,-0.0059);
        ps.add(-0.018300000000000004,-0.0507,-0.0060);
        ps.add(-0.018300000000000004,-0.0508,-0.0059);
        ps.add(-0.018300000000000004,-0.0507,-0.0060);
        ps.add(-0.018100000000000005,-0.0507,-0.0060);
        ps.add(-0.018300000000000004,-0.0508,-0.0059);
        ps.add(-0.018300000000000004,-0.0508,-0.0059);
        ps.add(-0.018100000000000005,-0.0507,-0.0060);
        ps.add(-0.018100000000000005,-0.0508,-0.0059);
        ps.add(-0.018099999999999998,-0.0507,-0.0060);
        ps.add(-0.0179,-0.0507,-0.0060);
        ps.add(-0.018099999999999998,-0.0508,-0.0059);
        ps.add(-0.018099999999999998,-0.0508,-0.0059);
        ps.add(-0.0179,-0.0507,-0.0060);
        ps.add(-0.0179,-0.0508,-0.0059);
        ps.add(-0.0179,-0.0507,-0.0060);
        ps.add(-0.0177,-0.0507,-0.0060);
        ps.add(-0.0179,-0.0508,-0.0059);
        ps.add(-0.0179,-0.0508,-0.0059);
        ps.add(-0.0177,-0.0507,-0.0060);
        ps.add(-0.0177,-0.0508,-0.0059);
        ps.add(-0.0177,-0.0507,-0.0060);
        ps.add(-0.0175,-0.0507,-0.0060);
        ps.add(-0.0177,-0.0508,-0.0059);
        ps.add(-0.0177,-0.0508,-0.0059);
        ps.add(-0.0175,-0.0507,-0.0060);
        ps.add(-0.0175,-0.0508,-0.0059);
        ps.add(-0.0175,-0.0507,-0.0060);
        ps.add(-0.017300000000000003,-0.0507,-0.0060);
        ps.add(-0.0175,-0.0508,-0.0059);
        ps.add(-0.0175,-0.0508,-0.0059);
        ps.add(-0.017300000000000003,-0.0507,-0.0060);
        ps.add(-0.017300000000000003,-0.0508,-0.0059);
        ps.add(-0.017300000000000003,-0.0507,-0.0060);
        ps.add(-0.017100000000000004,-0.0507,-0.0060);
        ps.add(-0.017300000000000003,-0.0508,-0.0059);
        ps.add(-0.017300000000000003,-0.0508,-0.0059);
        ps.add(-0.017100000000000004,-0.0507,-0.0060);
        ps.add(-0.017100000000000004,-0.0508,-0.0059);
        ps.add(-0.017100000000000004,-0.0507,-0.0060);
        ps.add(-0.016900000000000005,-0.0507,-0.0060);
        ps.add(-0.017100000000000004,-0.0508,-0.0059);
        ps.add(-0.017100000000000004,-0.0508,-0.0059);
        ps.add(-0.016900000000000005,-0.0507,-0.0060);
        ps.add(-0.016900000000000005,-0.0508,-0.0059);
        ps.add(-0.0169,-0.0507,-0.0060);
        ps.add(-0.0167,-0.0507,-0.0060);
        ps.add(-0.0169,-0.0508,-0.0059);
        ps.add(-0.0169,-0.0508,-0.0059);
        ps.add(-0.0167,-0.0507,-0.0060);
        ps.add(-0.0167,-0.0508,-0.0059);
        ps.add(-0.0167,-0.0507,-0.0060);
        ps.add(-0.0165,-0.0507,-0.0060);
        ps.add(-0.0167,-0.0508,-0.0059);
        ps.add(-0.0167,-0.0508,-0.0059);
        ps.add(-0.0165,-0.0507,-0.0060);
        ps.add(-0.0165,-0.0508,-0.0059);
        ps.add(-0.0165,-0.0507,-0.0060);
        ps.add(-0.016300000000000002,-0.0507,-0.0060);
        ps.add(-0.0165,-0.0508,-0.0059);
        ps.add(-0.0165,-0.0508,-0.0059);
        ps.add(-0.016300000000000002,-0.0507,-0.0060);
        ps.add(-0.016300000000000002,-0.0508,-0.0059);
        ps.add(-0.016300000000000002,-0.0507,-0.0060);
        ps.add(-0.016100000000000003,-0.0507,-0.0060);
        ps.add(-0.016300000000000002,-0.0508,-0.0059);
        ps.add(-0.016300000000000002,-0.0508,-0.0059);
        ps.add(-0.016100000000000003,-0.0507,-0.0060);
        ps.add(-0.016100000000000003,-0.0508,-0.0059);
        ps.add(-0.016100000000000003,-0.0507,-0.0060);
        ps.add(-0.015900000000000004,-0.0507,-0.0060);
        ps.add(-0.016100000000000003,-0.0508,-0.0059);
        ps.add(-0.016100000000000003,-0.0508,-0.0059);
        ps.add(-0.015900000000000004,-0.0507,-0.0060);
        ps.add(-0.015900000000000004,-0.0508,-0.0059);
        ps.add(-0.015900000000000004,-0.0507,-0.0060);
        ps.add(-0.015700000000000006,-0.0507,-0.0060);
        ps.add(-0.015900000000000004,-0.0508,-0.0059);
        ps.add(-0.015900000000000004,-0.0508,-0.0059);
        ps.add(-0.015700000000000006,-0.0507,-0.0060);
        ps.add(-0.015700000000000006,-0.0508,-0.0059);
        ps.add(-0.0157,-0.0507,-0.0060);
        ps.add(-0.015499999999999998,-0.0507,-0.0060);
        ps.add(-0.0157,-0.0508,-0.0059);
        ps.add(-0.0157,-0.0508,-0.0059);
        ps.add(-0.015499999999999998,-0.0507,-0.0060);
        ps.add(-0.015499999999999998,-0.0508,-0.0059);
        ps.add(-0.0155,-0.0507,-0.0060);
        ps.add(-0.0153,-0.0507,-0.0060);
        ps.add(-0.0155,-0.0508,-0.0059);
        ps.add(-0.0155,-0.0508,-0.0059);
        ps.add(-0.0153,-0.0507,-0.0060);
        ps.add(-0.0153,-0.0508,-0.0059);
        ps.add(-0.015300000000000001,-0.0507,-0.0060);
        ps.add(-0.0151,-0.0507,-0.0060);
        ps.add(-0.015300000000000001,-0.0508,-0.0059);
        ps.add(-0.015300000000000001,-0.0508,-0.0059);
        ps.add(-0.0151,-0.0507,-0.0060);
        ps.add(-0.0151,-0.0508,-0.0059);
        ps.add(-0.015100000000000002,-0.0507,-0.0060);
        ps.add(-0.014900000000000002,-0.0507,-0.0060);
        ps.add(-0.015100000000000002,-0.0508,-0.0059);
        ps.add(-0.015100000000000002,-0.0508,-0.0059);
        ps.add(-0.014900000000000002,-0.0507,-0.0060);
        ps.add(-0.014900000000000002,-0.0508,-0.0059);
        ps.add(-0.014900000000000004,-0.0507,-0.0060);
        ps.add(-0.014700000000000003,-0.0507,-0.0060);
        ps.add(-0.014900000000000004,-0.0508,-0.0059);
        ps.add(-0.014900000000000004,-0.0508,-0.0059);
        ps.add(-0.014700000000000003,-0.0507,-0.0060);
        ps.add(-0.014700000000000003,-0.0508,-0.0059);
        ps.add(-0.014699999999999998,-0.0507,-0.0060);
        ps.add(-0.014499999999999997,-0.0507,-0.0060);
        ps.add(-0.014699999999999998,-0.0508,-0.0059);
        ps.add(-0.014699999999999998,-0.0508,-0.0059);
        ps.add(-0.014499999999999997,-0.0507,-0.0060);
        ps.add(-0.014499999999999997,-0.0508,-0.0059);
        ps.add(-0.014499999999999999,-0.0507,-0.0060);
        ps.add(-0.014299999999999998,-0.0507,-0.0060);
        ps.add(-0.014499999999999999,-0.0508,-0.0059);
        ps.add(-0.014499999999999999,-0.0508,-0.0059);
        ps.add(-0.014299999999999998,-0.0507,-0.0060);
        ps.add(-0.014299999999999998,-0.0508,-0.0059);
        ps.add(-0.0143,-0.0507,-0.0060);
        ps.add(-0.0141,-0.0507,-0.0060);
        ps.add(-0.0143,-0.0508,-0.0059);
        ps.add(-0.0143,-0.0508,-0.0059);
        ps.add(-0.0141,-0.0507,-0.0060);
        ps.add(-0.0141,-0.0508,-0.0059);

        ps.add(-0.014100000000000001,-0.0507,-0.0060);
        ps.add(-0.013900000000000001,-0.0507,-0.0060);
        ps.add(-0.014100000000000001,-0.0508,-0.0059);
        ps.add(-0.014100000000000001,-0.0508,-0.0059);
        ps.add(-0.013900000000000001,-0.0507,-0.0060);
        ps.add(-0.013900000000000001,-0.0508,-0.0059);
        ps.add(-0.013900000000000003,-0.0507,-0.0060);
        ps.add(-0.013700000000000002,-0.0507,-0.0060);
        ps.add(-0.013900000000000003,-0.0508,-0.0059);
        ps.add(-0.013900000000000003,-0.0508,-0.0059);
        ps.add(-0.013700000000000002,-0.0507,-0.0060);
        ps.add(-0.013700000000000002,-0.0508,-0.0059);
        ps.add(-0.013700000000000004,-0.0507,-0.0060);
        ps.add(-0.013500000000000003,-0.0507,-0.0060);
        ps.add(-0.013700000000000004,-0.0508,-0.0059);
        ps.add(-0.013700000000000004,-0.0508,-0.0059);
        ps.add(-0.013500000000000003,-0.0507,-0.0060);
        ps.add(-0.013500000000000003,-0.0508,-0.0059);
        ps.add(-0.013499999999999998,-0.0507,-0.0060);
        ps.add(-0.013299999999999998,-0.0507,-0.0060);
        ps.add(-0.013499999999999998,-0.0508,-0.0059);
        ps.add(-0.013499999999999998,-0.0508,-0.0059);
        ps.add(-0.013299999999999998,-0.0507,-0.0060);
        ps.add(-0.013299999999999998,-0.0508,-0.0059);
        ps.add(-0.0133,-0.0507,-0.0060);
        ps.add(-0.013099999999999999,-0.0507,-0.0060);
        ps.add(-0.0133,-0.0508,-0.0059);
        ps.add(-0.0133,-0.0508,-0.0059);
        ps.add(-0.013099999999999999,-0.0507,-0.0060);
        ps.add(-0.013099999999999999,-0.0508,-0.0059);
        ps.add(-0.0131,-0.0507,-0.0060);
        ps.add(-0.0129,-0.0507,-0.0060);
        ps.add(-0.0131,-0.0508,-0.0059);
        ps.add(-0.0131,-0.0508,-0.0059);
        ps.add(-0.0129,-0.0507,-0.0060);
        ps.add(-0.0129,-0.0508,-0.0059);
        ps.add(-0.012900000000000002,-0.0507,-0.0060);
        ps.add(-0.012700000000000001,-0.0507,-0.0060);
        ps.add(-0.012900000000000002,-0.0508,-0.0059);
        ps.add(-0.012900000000000002,-0.0508,-0.0059);
        ps.add(-0.012700000000000001,-0.0507,-0.0060);
        ps.add(-0.012700000000000001,-0.0508,-0.0059);
        ps.add(-0.012700000000000003,-0.0507,-0.0060);
        ps.add(-0.012500000000000002,-0.0507,-0.0060);
        ps.add(-0.012700000000000003,-0.0508,-0.0059);
        ps.add(-0.012700000000000003,-0.0508,-0.0059);
        ps.add(-0.012500000000000002,-0.0507,-0.0060);
        ps.add(-0.012500000000000002,-0.0508,-0.0059);
        ps.add(-0.012500000000000004,-0.0507,-0.0060);
        ps.add(-0.012300000000000004,-0.0507,-0.0060);
        ps.add(-0.012500000000000004,-0.0508,-0.0059);
        ps.add(-0.012500000000000004,-0.0508,-0.0059);
        ps.add(-0.012300000000000004,-0.0507,-0.0060);
        ps.add(-0.012300000000000004,-0.0508,-0.0059);
        ps.add(-0.012299999999999998,-0.0507,-0.0060);
        ps.add(-0.012099999999999998,-0.0507,-0.0060);
        ps.add(-0.012299999999999998,-0.0508,-0.0059);
        ps.add(-0.012299999999999998,-0.0508,-0.0059);
        ps.add(-0.012099999999999998,-0.0507,-0.0060);
        ps.add(-0.012099999999999998,-0.0508,-0.0059);
        ps.add(-0.0121,-0.0507,-0.0060);
        ps.add(-0.011899999999999999,-0.0507,-0.0060);

        ps.add(-0.0121,-0.0508,-0.0059);
        ps.add(-0.0121,-0.0508,-0.0059);
        ps.add(-0.011899999999999999,-0.0507,-0.0060);
        ps.add(-0.011899999999999999,-0.0508,-0.0059);
        ps.add(-0.0119,-0.0507,-0.0060);
        ps.add(-0.0117,-0.0507,-0.0060);
        ps.add(-0.0119,-0.0508,-0.0059);
        ps.add(-0.0119,-0.0508,-0.0059);
        ps.add(-0.0117,-0.0507,-0.0060);
        ps.add(-0.0117,-0.0508,-0.0059);
        ps.add(-0.011700000000000002,-0.0507,-0.0060);
        ps.add(-0.011500000000000002,-0.0507,-0.0060);
        ps.add(-0.011700000000000002,-0.0508,-0.0059);
        ps.add(-0.011700000000000002,-0.0508,-0.0059);
        ps.add(-0.011500000000000002,-0.0507,-0.0060);
        ps.add(-0.011500000000000002,-0.0508,-0.0059);
        ps.add(-0.011500000000000003,-0.0507,-0.0060);
        ps.add(-0.011300000000000003,-0.0507,-0.0060);
        ps.add(-0.011500000000000003,-0.0508,-0.0059);
        ps.add(-0.011500000000000003,-0.0508,-0.0059);
        ps.add(-0.011300000000000003,-0.0507,-0.0060);
        ps.add(-0.011300000000000003,-0.0508,-0.0059);
        ps.add(-0.011299999999999998,-0.0507,-0.0060);
        ps.add(-0.011099999999999997,-0.0507,-0.0060);
        ps.add(-0.011299999999999998,-0.0508,-0.0059);
        ps.add(-0.011299999999999998,-0.0508,-0.0059);
        ps.add(-0.011099999999999997,-0.0507,-0.0060);
        ps.add(-0.011099999999999997,-0.0508,-0.0059);
        ps.add(-0.011099999999999999,-0.0507,-0.0060);
        ps.add(-0.010899999999999998,-0.0507,-0.0060);
        ps.add(-0.011099999999999999,-0.0508,-0.0059);
        ps.add(-0.011099999999999999,-0.0508,-0.0059);
        ps.add(-0.010899999999999998,-0.0507,-0.0060);
        ps.add(-0.010899999999999998,-0.0508,-0.0059);
        ps.add(-0.0109,-0.0507,-0.0060);
        ps.add(-0.0107,-0.0507,-0.0060);
        ps.add(-0.0109,-0.0508,-0.0059);
        ps.add(-0.0109,-0.0508,-0.0059);
        ps.add(-0.0107,-0.0507,-0.0060);
        ps.add(-0.0107,-0.0508,-0.0059);
        ps.add(-0.010700000000000001,-0.0507,-0.0060);
        ps.add(-0.0105,-0.0507,-0.0060);
        ps.add(-0.010700000000000001,-0.0508,-0.0059);
        ps.add(-0.010700000000000001,-0.0508,-0.0059);
        ps.add(-0.0105,-0.0507,-0.0060);
        ps.add(-0.0105,-0.0508,-0.0059);
        ps.add(-0.010500000000000002,-0.0507,-0.0060);
        ps.add(-0.010300000000000002,-0.0507,-0.0060);
        ps.add(-0.010500000000000002,-0.0508,-0.0059);
        ps.add(-0.010500000000000002,-0.0508,-0.0059);
        ps.add(-0.010300000000000002,-0.0507,-0.0060);
        ps.add(-0.010300000000000002,-0.0508,-0.0059);
        ps.add(-0.010300000000000004,-0.0507,-0.0060);
        ps.add(-0.010100000000000003,-0.0507,-0.0060);
        ps.add(-0.010300000000000004,-0.0508,-0.0059);
        ps.add(-0.010300000000000004,-0.0508,-0.0059);
        ps.add(-0.010100000000000003,-0.0507,-0.0060);
        ps.add(-0.010100000000000003,-0.0508,-0.0059);
        ps.add(-0.010099999999999998,-0.0507,-0.0060);
        ps.add(-0.009899999999999997,-0.0507,-0.0060);
        ps.add(-0.010099999999999998,-0.0508,-0.0059);
        ps.add(-0.010099999999999998,-0.0508,-0.0059);
        ps.add(-0.009899999999999997,-0.0507,-0.0060);
        ps.add(-0.009899999999999997,-0.0508,-0.0059);
        ps.add(-0.009899999999999999,-0.0507,-0.0060);
        ps.add(-0.009699999999999999,-0.0507,-0.0060);
        ps.add(-0.009899999999999999,-0.0508,-0.0059);
        ps.add(-0.009899999999999999,-0.0508,-0.0059);
        ps.add(-0.009699999999999999,-0.0507,-0.0060);
        ps.add(-0.009699999999999999,-0.0508,-0.0059);
        ps.add(-0.0097,-0.0507,-0.0060);
        ps.add(-0.0095,-0.0507,-0.0060);
        ps.add(-0.0097,-0.0508,-0.0059);
        ps.add(-0.0097,-0.0508,-0.0059);
        ps.add(-0.0095,-0.0507,-0.0060);
        ps.add(-0.0095,-0.0508,-0.0059);
        ps.add(-0.009500000000000001,-0.0507,-0.0060);
        ps.add(-0.009300000000000001,-0.0507,-0.0060);
        ps.add(-0.009500000000000001,-0.0508,-0.0059);
        ps.add(-0.009500000000000001,-0.0508,-0.0059);
        ps.add(-0.009300000000000001,-0.0507,-0.0060);
        ps.add(-0.009300000000000001,-0.0508,-0.0059);
        ps.add(-0.009300000000000003,-0.0507,-0.0060);
        ps.add(-0.009100000000000002,-0.0507,-0.0060);
        ps.add(-0.009300000000000003,-0.0508,-0.0059);
        ps.add(-0.009300000000000003,-0.0508,-0.0059);
        ps.add(-0.009100000000000002,-0.0507,-0.0060);
        ps.add(-0.009100000000000002,-0.0508,-0.0059);
        ps.add(-0.009100000000000004,-0.0507,-0.0060);
        ps.add(-0.008900000000000003,-0.0507,-0.0060);
        ps.add(-0.009100000000000004,-0.0508,-0.0059);
        ps.add(-0.009100000000000004,-0.0508,-0.0059);
        ps.add(-0.008900000000000003,-0.0507,-0.0060);
        ps.add(-0.008900000000000003,-0.0508,-0.0059);
        ps.add(-0.008899999999999998,-0.0507,-0.0060);
        ps.add(-0.008699999999999998,-0.0507,-0.0060);
        ps.add(-0.008899999999999998,-0.0508,-0.0059);
        ps.add(-0.008899999999999998,-0.0508,-0.0059);
        ps.add(-0.008699999999999998,-0.0507,-0.0060);
        ps.add(-0.008699999999999998,-0.0508,-0.0059);
        ps.add(-0.0087,-0.0507,-0.0060);
        ps.add(-0.008499999999999999,-0.0507,-0.0060);
        ps.add(-0.0087,-0.0508,-0.0059);
        ps.add(-0.0087,-0.0508,-0.0059);
        ps.add(-0.008499999999999999,-0.0507,-0.0060);
        ps.add(-0.008499999999999999,-0.0508,-0.0059);
        ps.add(-0.0085,-0.0507,-0.0060);
        ps.add(-0.0083,-0.0507,-0.0060);
        ps.add(-0.0085,-0.0508,-0.0059);
        ps.add(-0.0085,-0.0508,-0.0059);
        ps.add(-0.0083,-0.0507,-0.0060);
        ps.add(-0.0083,-0.0508,-0.0059);

        ps.add(-0.008300000000000002,-0.0507,-0.0060);
        ps.add(-0.008100000000000001,-0.0507,-0.0060);
        ps.add(-0.008300000000000002,-0.0508,-0.0059);
        ps.add(-0.008300000000000002,-0.0508,-0.0059);
        ps.add(-0.008100000000000001,-0.0507,-0.0060);
        ps.add(-0.008100000000000001,-0.0508,-0.0059);
        ps.add(-0.008100000000000003,-0.0507,-0.0060);
        ps.add(-0.007900000000000003,-0.0507,-0.0060);
        ps.add(-0.008100000000000003,-0.0508,-0.0059);
        ps.add(-0.008100000000000003,-0.0508,-0.0059);
        ps.add(-0.007900000000000003,-0.0507,-0.0060);
        ps.add(-0.007900000000000003,-0.0508,-0.0059);

        ps.add(-0.007900000000000004,-0.0507,-0.0060);
        ps.add(-0.007700000000000005,-0.0507,-0.0060);
        ps.add(-0.007900000000000004,-0.0508,-0.0059);
        ps.add(-0.007900000000000004,-0.0508,-0.0059);
        ps.add(-0.007700000000000005,-0.0507,-0.0060);
        ps.add(-0.007700000000000005,-0.0508,-0.0059);

        ps.add(-0.0076999999999999985,-0.0507,-0.0060);
        ps.add(-0.007499999999999999,-0.0507,-0.0060);
        ps.add(-0.0076999999999999985,-0.0508,-0.0059);
        ps.add(-0.0076999999999999985,-0.0508,-0.0059);

        ps.add(-0.007499999999999999,-0.0507,-0.0060);

        ps.add(-0.007499999999999999,-0.0508,-0.0059);
        double[] points = ps.getPoints();

        if (DEBUG) System.out.println("Values: " + java.util.Arrays.toString(points));

        int len = points.length / 3;

        for(int i=0; i < len; i++) {

            if ((points[i*3] == 0) && (points[i*3+1] == 0) && (points[i*3+2] == 0)) {
                fail("Incorrect value in array");
            }

            for(int j=0; j < len; j++) {
                if (i == j) continue;

                if (Math.abs(points[i*3+0] - points[j*3+0]) < TOLERANCE
                        && Math.abs(points[i*3+1] - points[j*3+1]) < TOLERANCE &&
                        Math.abs(points[i*3+2] - points[j*3+2]) < TOLERANCE) {

                    System.out.println("Points: " + i + " j:" + j);
                    System.out.println("Point 1: " + points[i*3] + " " + points[i*3+1] + " " + points[i*3+2]);
                    System.out.println("Point 2: " + points[j*3] + " " + points[j*3+1] + " " + points[j*3+2]);
                    fail("Duplicate Points");
                }
            }
        }
    }

    public void testWithinTolerance() {
        double TOLERANCE = 1.e-8;
        PointSet ps = new PointSet(TOLERANCE);

        int id0 = ps.add(1,2,3);
        int id1 = ps.add(1,2,3 + TOLERANCE / 2.0);

        assertEquals("Same ID's", id0,id1);
    }

    public void testResize() {
        double TOLERANCE = 1.e-8;
        PointSet ps = new PointSet(3,1f,TOLERANCE);

        int[] ids = new int[13];

        for(int i=0; i < ids.length; i++) {
            ids[i] = ps.add(1,2,i);
        }

        for(int i=0; i < ids.length - 1; i++) {
            assertTrue("UNIQUE" + i, ids[i] != ids[i+1]);
        }

        double[] points = ps.getPoints();
        int len = points.length / 3;
        assertEquals("PointCount", ids.length, len);

        for(int i=0; i < len; i++) {
            System.out.println(points[i*3] + "," + points[i*3+1] + "," + points[i*3+2]);
            if ((points[i*3] == 0) && (points[i*3+1] == 0) && (points[i*3+2] == 0)) {
                fail("Zero value in array");
            }

            if (points[i*3+2] != i) {
                fail("Incorrect value in array");
            }
        }

    }

    public void testResizeSameHash() {
        double TOLERANCE = 1.e-8;
        PointSet ps = new PointSet(1,1f,TOLERANCE);


        double CX = 10556796.789,
                CY = 26556797.891,
                CZ = 37556792.981,
                CW = 45556795.955;

        double factorXY = CY / CX;
        double factorZY = CY / CZ;

        int[] ids = new int[4];
        ids[0] = ps.add(0,1,0);
        ids[1] = ps.add(1*factorXY,0,0);
        ids[2] = ps.add(0,0,1*factorZY);
        ids[3] = ps.add(-1*factorXY,0,2*factorZY);

        System.out.println("IDS: " + java.util.Arrays.toString(ids));
        for(int i=0; i < ids.length - 1; i++) {
            assertTrue("UNIQUE" + i, ids[i] != ids[i+1]);
        }

        double[] points = ps.getPoints();
        int len = points.length / 3;
        assertEquals("PointCount", ids.length, len);

        System.out.println("Points: " + java.util.Arrays.toString(points));
        for(int i=0; i < len; i++) {
            System.out.println(points[i*3] + "," + points[i*3+1] + "," + points[i*3+2]);
            if ((points[i*3] == 0) && (points[i*3+1] == 0) && (points[i*3+2] == 0)) {
                fail("Zero value in array");
            }
        }

    }

    public void testSameHashDiffWorks() {
        double CX = 10556796.789,
                CY = 26556797.891,
                CZ = 37556792.981,
                CW = 45556795.955;

        double factor = CY / CX;

        double TOLERANCE = 1.e-8;
        PointSet ps = new PointSet(10, 0.75f, TOLERANCE);

        // hash function is (CX*x + CY * y + CZ * z + CW);

        int id0 = ps.add(0,1,0);
        int id1 = ps.add(1*factor,0,0);

        assertNotSame("Same ID's", id0, id1);

        id0 = ps.get(0,1,0);
        id1 = ps.get(1*factor,0,0);

        assertNotSame("ID0 not found",id0,-1);
        assertNotSame("ID1 not found",id1,-1);
        assertNotSame("Pos", id0,id1);

    }
}
