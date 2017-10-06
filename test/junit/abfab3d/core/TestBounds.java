/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2017
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.core;

//External Imports

import abfab3d.BaseTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import static abfab3d.core.Output.printf;

// Internal Imports

// Internal Imports

/**
 * Tests the functionality of Color
 *
 * @author Alan Hudson
 * @version
 */
public class TestBounds extends BaseTestCase  {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestBounds.class);
    }

    public void testCheckIntersectionRayXPlusAxis() {
        Bounds bounds = new Bounds(-10,10,-10,10,-10,10);

        double[] intersect = new double[3];
        boolean found = bounds.checkIntersectionRay(new double[] {15,0,0},new double[] {-1,0,0},intersect);

        assertTrue("Intersect",found);
        double EPS = 1-6;

        printf("Intersection: %f %f %f\n",intersect[0],intersect[1],intersect[2]);
        assertTrue("x coord", Math.abs(intersect[0] - bounds.xmax) > EPS);

    }
}
