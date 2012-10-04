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

package abfab3d.grid.op;

// External Imports

import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.BaseTestAttributeGrid;
import abfab3d.grid.Grid;
import junit.framework.Test;
import junit.framework.TestSuite;

// Internal Imports


/**
 * Tests the functionality of the RingSpaceWarp
 *
 * @author Tony Wong
 * @version
 */
public class TestRingSpaceWarp extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestRingSpaceWarp.class);
    }

    /**
     * Test equality of forward and backward conversion
     */
    public void testConvert1() {
        Grid grid = new ArrayAttributeGridByte(10,10,10,0.001,0.001);
        double r = 0.05;
        RingSpaceWarp warp = new RingSpaceWarp(grid,r);

        double[] coord = new double[] {0,0,0};
        double[] dest = new double[3];
        double[] dest2 = new double[3];

        warp.transform(coord, dest);
        warp.transform(dest, dest2);

        double EPS = 1e-10;

        assertEquals("Same X", coord[0],dest2[0],EPS);
        assertEquals("Same Y", coord[1],dest2[1],EPS);
        assertEquals("Same Z", coord[2],dest2[2],EPS);
    }
    /**
     * Test equality of forward and backward conversion
     */
    public void testConvert2() {
        Grid grid = new ArrayAttributeGridByte(10,10,10,0.001,0.001);
        double r = 0.05;
        RingSpaceWarp warp = new RingSpaceWarp(grid,r);

        double[] coord = new double[] {1,2,3};
        double[] dest = new double[3];
        double[] dest2 = new double[3];

        warp.transform(coord, dest);
        warp.transform(dest, dest2);

        double EPS = 1e-10;

        assertEquals("Same X", coord[0],dest2[0],EPS);
        assertEquals("Same Y", coord[1],dest2[1],EPS);
        assertEquals("Same Z", coord[2],dest2[2],EPS);
    }
}
