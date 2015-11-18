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

package abfab3d.grid.op;

// External Imports
import java.util.List;

import abfab3d.grid.query.RegionFinder;

import junit.framework.Test;
import junit.framework.TestSuite;

// Internal Imports
import abfab3d.grid.*;

/**
 * Tests the functionality of the Downsample operation
 *
 * @author Alan Hudson
 * @version
 */
public class TestDownsample extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDownsample.class);
    }

    /**
     * Test prefer marked
     *
     * xz plane
     *
     * x000
     * xx00 ==>x0
     * x000    x0
     * xx00
     */
    public void testPreferMarked1() {
        int size = 4;
        long material = 1;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        grid.setData(0,0,0,Grid.INSIDE, material);
        grid.setData(0,1,0,Grid.INSIDE, material);
        grid.setData(0,0,1,Grid.INSIDE, material);
        grid.setData(0,0,2,Grid.INSIDE, material);
        grid.setData(1,0,2,Grid.INSIDE, material);
        grid.setData(0,0,3,Grid.INSIDE, material);


        DownsampleOp ds = new DownsampleOp(true);
        Grid dest = ds.execute(grid);

        assertEquals("Dest value000", Grid.INSIDE, dest.getState(0,0,0));
        assertEquals("Dest value100", Grid.OUTSIDE, dest.getState(1,0,0));
        assertEquals("Dest value001", Grid.INSIDE, dest.getState(0,0,1));
        assertEquals("Dest value101", Grid.OUTSIDE, dest.getState(1,0,1));
    }

    /**
     * Test prefer marked
     *
     * xz plane
     *
     * 0x0x
     * xx0x ==>xx
     * x000    x0
     * xx00
     */
    public void testPreferMarked2() {
        int size = 4;
        long material = 1;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        grid.setData(0,0,0,Grid.INSIDE, material);
        grid.setData(1,0,0,Grid.INSIDE, material);
        grid.setData(0,0,1,Grid.INSIDE, material);
        grid.setData(0,0,2,Grid.INSIDE, material);
        grid.setData(1,0,2,Grid.INSIDE, material);
        grid.setData(3,0,2,Grid.INSIDE, material);
        grid.setData(1,0,3,Grid.INSIDE, material);
        grid.setData(3,0,3,Grid.INSIDE, material);

        System.out.println("Initial grid: \n" + grid.toStringSlice(0));
        DownsampleOp ds = new DownsampleOp(true);
        Grid dest = ds.execute(grid);
        System.out.println("Dest grid: \n" + dest.toStringSlice(0));

        assertEquals("Dest value000", Grid.INSIDE, dest.getState(0,0,0));
        assertEquals("Dest value100", Grid.OUTSIDE, dest.getState(1,0,0));
        assertEquals("Dest value001", Grid.INSIDE, dest.getState(0,0,1));
        assertEquals("Dest value101", Grid.INSIDE, dest.getState(1,0,1));
    }

    /**
     * Test prefer marked
     *
     * xz plane
     *
     * 000x
     * 000x ==>0x
     * 000x    0x
     * 000x
     */
    public void testPreferMarked3() {
        int size = 4;
        long material = 1;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        grid.setData(3,0,0,Grid.INSIDE, material);
        grid.setData(3,0,1,Grid.INSIDE, material);
        grid.setData(3,0,2,Grid.INSIDE, material);
        grid.setData(3,0,3,Grid.INSIDE, material);

        System.out.println("Initial grid: \n" + grid.toStringSlice(0));
        DownsampleOp ds = new DownsampleOp(true);
        Grid dest = ds.execute(grid);
        System.out.println("Dest grid: \n" + dest.toStringSlice(0));

        assertEquals("Dest value000", Grid.OUTSIDE, dest.getState(0,0,0));
        assertEquals("Dest value100", Grid.INSIDE, dest.getState(1,0,0));
        assertEquals("Dest value001", Grid.OUTSIDE, dest.getState(0,0,1));
        assertEquals("Dest value101", Grid.INSIDE, dest.getState(1,0,1));
    }

    /**
     * Test prefer marked
     *
     * xz plane
     *
     * 000x
     * 00x0 ==>0x
     * 0x00    x0
     * x000
     */
    public void testPreferMarked4() {
        int size = 4;
        long material = 1;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        grid.setData(0,0,0,Grid.INSIDE, material);
        grid.setData(1,0,1,Grid.INSIDE, material);
        grid.setData(2,0,2,Grid.INSIDE, material);
        grid.setData(3,0,3,Grid.INSIDE, material);

        System.out.println("Initial grid: \n" + grid.toStringSlice(0));
        DownsampleOp ds = new DownsampleOp(true);
        Grid dest = ds.execute(grid);
        System.out.println("Dest grid: \n" + dest.toStringSlice(0));

        assertEquals("Dest value000", Grid.INSIDE, dest.getState(0,0,0));
        assertEquals("Dest value100", Grid.OUTSIDE, dest.getState(1,0,0));
        assertEquals("Dest value001", Grid.OUTSIDE, dest.getState(0,0,1));
        assertEquals("Dest value101", Grid.INSIDE, dest.getState(1,0,1));
    }

    /**
     * Test prefer marked
     *
     * xyz plane, diagonal
     *
     * 000x
     * 00x0 ==>0x
     * 0x00    x0
     * x000
     */
    public void testPreferMarked5() {
        int size = 4;
        long material = 1;

        AttributeGrid grid = new ArrayAttributeGridByte(size,size,size,0.001, 0.001);

        for(int y=0; y < size; y++) {
            grid.setData(0,y,0,Grid.INSIDE, material);
            grid.setData(1,y,1,Grid.INSIDE, material);
            grid.setData(2,y,2,Grid.INSIDE, material);
            grid.setData(3,y,3,Grid.INSIDE, material);
        }

        System.out.println("Initial grid: \n" + grid.toStringSlice(0));
        DownsampleOp ds = new DownsampleOp(true);
        Grid dest = ds.execute(grid);
        System.out.println("Dest grid: \n" + dest.toStringSlice(0));

        for(int y=0; y < size / 2; y++) {
            assertEquals("Dest value000", Grid.INSIDE, dest.getState(0,y,0));
            assertEquals("Dest value100", Grid.OUTSIDE, dest.getState(1,y,0));
            assertEquals("Dest value001", Grid.OUTSIDE, dest.getState(0,y,1));
            assertEquals("Dest value101", Grid.INSIDE, dest.getState(1,y,1));
        }
    }

    private void verifyRegions(Grid grid, Grid dest) {
        RegionFinder rf = new RegionFinder();
        List<Region> orig_regions = rf.execute(grid);
        int orig_region_count = orig_regions.size();

        List<Region> dest_regions = rf.execute(grid);
        int dest_region_count = orig_regions.size();

        System.out.println("Initial Regions: " + orig_region_count + " dest: " + dest_region_count);
        assertEquals("Region count", orig_region_count, dest_region_count);

        // volume of dest regions should be >= orig volume / 4.
        Region r1 = orig_regions.get(0);
        Region r2 = dest_regions.get(0);
        if (r2.getVolume() < r1.getVolume() / 4) {
            fail("Dest volume too small: " + r2.getVolume() + " compared to: " + r1.getVolume());
        }

    }

    public static void main(String[] args) {
        TestDownsample tds = new TestDownsample();
        tds.testPreferMarked1();
    }
}
