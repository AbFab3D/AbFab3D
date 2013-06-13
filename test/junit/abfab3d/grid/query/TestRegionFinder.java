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

package abfab3d.grid.query;

// External Imports
import junit.framework.Test;
import junit.framework.TestSuite;
import java.util.*;

// Internal Imports
import abfab3d.BaseTestCase;
import abfab3d.grid.*;

/**
 * Tests the functionality of the RegionFinder class
 *
 * @author Alan Hudson
 * @version
 */
public class TestRegionFinder extends BaseTestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestRegionFinder.class);
    }

    /**
     * Test basic operation
     */
    public void testBasic() {
        AttributeGrid grid = new ArrayAttributeGridByte(20,20,20,0.1,0.1);

        // Create a simple region
        grid.setData(5,5,5,Grid.INSIDE,1);
        grid.setData(5,5,6,Grid.INSIDE,1);
        grid.setData(5,5,7,Grid.INSIDE,1);
        grid.setData(5,6,5,Grid.INSIDE,1);
        grid.setData(5,6,6,Grid.INSIDE,1);
        grid.setData(5,6,7,Grid.INSIDE,1);

        RegionFinder rf = new RegionFinder(10);
        List<Region> regions = rf.execute(grid);

        assertNotNull("Regions", regions);
        assertEquals("Regions size", 1, regions.size());

        SetRegion region = (SetRegion) regions.get(0);
        int count = region.getNumCoords();

        assertEquals("SetRegion count", 6, count);
    }

    /**
     * Test multiple regions
     */
    public void testMultiple() {
        int w = 20;
        int h = 20;
        int d = 20;

        AttributeGrid grid = new ArrayAttributeGridByte(20,20,20,0.1,0.1);

        // Create a simple region
        HashSet<VoxelCoordinate> region1 = new HashSet<VoxelCoordinate>();
        region1.add(new VoxelCoordinate(5,5,5));
        region1.add(new VoxelCoordinate(5,5,6));
        region1.add(new VoxelCoordinate(5,5,7));
        region1.add(new VoxelCoordinate(5,6,5));
        region1.add(new VoxelCoordinate(5,6,6));
        region1.add(new VoxelCoordinate(5,6,7));

        Iterator<VoxelCoordinate> itr = region1.iterator();
        while(itr.hasNext()) {
            VoxelCoordinate vc = itr.next();
            grid.setData(vc.getX(), vc.getY(), vc.getZ(), Grid.INSIDE,1);
        }

        HashSet<VoxelCoordinate> region2 = new HashSet<VoxelCoordinate>();
        region2.add(new VoxelCoordinate(1,5,5));
        region2.add(new VoxelCoordinate(1,5,6));
        region2.add(new VoxelCoordinate(1,5,7));
        region2.add(new VoxelCoordinate(1,6,5));
        region2.add(new VoxelCoordinate(1,6,6));
        region2.add(new VoxelCoordinate(1,6,7));
        region2.add(new VoxelCoordinate(1,6,8));
        region2.add(new VoxelCoordinate(1,6,9));

        itr = region2.iterator();
        while(itr.hasNext()) {
            VoxelCoordinate vc = itr.next();
            grid.setData(vc.getX(), vc.getY(), vc.getZ(), Grid.INSIDE,1);
        }


        RegionFinder rf = new RegionFinder(10);
        List<Region> regions = rf.execute(grid);

        assertNotNull("Regions", regions);
        assertEquals("Regions size", 2, regions.size());

        // Determine which region it is.

        Iterator<Region> itr3 = regions.iterator();

        while(itr3.hasNext()) {
            SetRegion region = (SetRegion) itr3.next();
            int count = region.getNumCoords();

            if (count == region1.size()) {

                Iterator<VoxelCoordinate> itr2 = region.getValues().iterator();
                while(itr2.hasNext()) {
                    VoxelCoordinate vc = itr2.next();
                    if (!region1.contains(vc)) {
                        fail("Invalid coordinate in region: " + vc);
                    }
                }
            } else if (count == region2.size()) {
                Iterator<VoxelCoordinate> itr2 = region.getValues().iterator();
                while(itr2.hasNext()) {
                    VoxelCoordinate vc = itr2.next();
                    if (!region2.contains(vc)) {
                        fail("Invalid coordinate in region: " + vc);
                    }
                }
            } else {
                fail("Invalid region count");
            }
        }

        // Test outside support
/*
        rf = new RegionFinder(new VoxelCoordinate(0,0,0), 10);
        regions = rf.execute(grid);

        assertNotNull("Regions", regions);
        assertEquals("Regions size", 2, regions.size());

        region = (SetRegion) regions.get(0);
        count = region.getNumCoords();

        assertEquals("Outside Region count", w*h*d - region1.size() - region2.size(), count);
*/
    }

    /**
     * Test multiple regions
     */
    public void testMultipleMaterial() {
        int w = 20;
        int h = 20;
        int d = 20;

        AttributeGrid grid = new ArrayAttributeGridByte(20,20,20,0.1,0.1);

        // Create a simple region
        HashSet<VoxelCoordinate> region1 = new HashSet<VoxelCoordinate>();
        region1.add(new VoxelCoordinate(5,5,5));
        region1.add(new VoxelCoordinate(5,5,6));
        region1.add(new VoxelCoordinate(5,5,7));
        region1.add(new VoxelCoordinate(5,6,5));
        region1.add(new VoxelCoordinate(5,6,6));
        region1.add(new VoxelCoordinate(5,6,7));

        Iterator<VoxelCoordinate> itr = region1.iterator();
        while(itr.hasNext()) {
            VoxelCoordinate vc = itr.next();
            grid.setData(vc.getX(), vc.getY(), vc.getZ(), Grid.INSIDE,1);
        }

        HashSet<VoxelCoordinate> region2 = new HashSet<VoxelCoordinate>();
        region2.add(new VoxelCoordinate(1,5,5));
        region2.add(new VoxelCoordinate(1,5,6));
        region2.add(new VoxelCoordinate(1,5,7));
        region2.add(new VoxelCoordinate(1,6,5));
        region2.add(new VoxelCoordinate(1,6,6));
        region2.add(new VoxelCoordinate(1,6,7));
        region2.add(new VoxelCoordinate(1,6,8));
        region2.add(new VoxelCoordinate(1,6,9));

        itr = region2.iterator();
        while(itr.hasNext()) {
            VoxelCoordinate vc = itr.next();
            grid.setData(vc.getX(), vc.getY(), vc.getZ(), Grid.INSIDE,2);
        }


        RegionFinder rf = new RegionFinder(10);
        List<Region> regions = rf.execute(grid);

        assertNotNull("Regions", regions);
        assertEquals("Regions size", 2, regions.size());

        // Determine which region it is.

        Iterator<Region> itr3 = regions.iterator();

        while(itr3.hasNext()) {
            SetRegion region = (SetRegion) itr3.next();
            int count = region.getNumCoords();

            if (count == region1.size()) {

                Iterator<VoxelCoordinate> itr2 = region.getValues().iterator();
                while(itr2.hasNext()) {
                    VoxelCoordinate vc = itr2.next();
                    if (!region1.contains(vc)) {
                        fail("Invalid coordinate in region: " + vc);
                    }
                }
            } else if (count == region2.size()) {
                Iterator<VoxelCoordinate> itr2 = region.getValues().iterator();
                while(itr2.hasNext()) {
                    VoxelCoordinate vc = itr2.next();
                    if (!region2.contains(vc)) {
                        fail("Invalid coordinate in region: " + vc);
                    }
                }
            } else {
                fail("Invalid region count");
            }
        }

        rf = new RegionFinder(10,1);
        regions = rf.execute(grid);

        assertNotNull("Regions", regions);
        assertEquals("Regions size", 1, regions.size());

        SetRegion region = (SetRegion) regions.get(0);
        int count = region.getNumCoords();

        assertEquals("SetRegion1 count", region1.size(), count);

        Iterator<VoxelCoordinate> itr2 = region.getValues().iterator();
        while(itr2.hasNext()) {
            VoxelCoordinate vc = itr2.next();
            if (!region1.contains(vc)) {
                fail("Invalid coordinate in region: " + vc);
            }
        }


        rf = new RegionFinder(10,2);
        regions = rf.execute(grid);

        assertNotNull("Regions", regions);
        assertEquals("Regions size", 1, regions.size());

        region = (SetRegion) regions.get(0);
        count = region.getNumCoords();

        itr2 = region.getValues().iterator();
        while(itr2.hasNext()) {
            VoxelCoordinate vc = itr2.next();
            if (!region2.contains(vc)) {
                fail("Invalid coordinate in region: " + vc);
            }
        }

        assertEquals("SetRegion2 count", region2.size(), count);

    }

    /**
     * Test lots of regions.  Creates a regular grid of regions to find.
     */
    public void testLotsOfRegions() {
        int region_size = 35;
        int region_buffer = 1;
        int regions_axis = 5;
        int size = (region_size + region_buffer) * regions_axis;

        AttributeGrid grid = null;

        int num_regions = regions_axis * regions_axis * regions_axis;

        HashMap<Long, Integer> regions = new HashMap<Long, Integer>(num_regions);

        if (num_regions < 63) {
            grid = new ArrayAttributeGridByte(size,size,size,0.1,0.1);
        } else {
            grid = new ArrayAttributeGridShort(size,size,size,0.1,0.1);
        }

System.out.println("Num Regions: " + num_regions + " voxels: " + size);
        int[] ll_coord = new int[3];
        int[] vcoord = new int[3];
        long matID = 0;
        for(int i=0; i < regions_axis; i++) {
            for(int j=0; j < regions_axis; j++) {
                for(int k=0; k < regions_axis; k++) {
//System.out.println("Adding region: " + matID + " i: " + i + " j: " + j + " k: " + k);
                    // find lower left corner
                    ll_coord[0] = i * (region_size + region_buffer);
                    ll_coord[1] = j * (region_size + region_buffer);
                    ll_coord[2] = k * (region_size + region_buffer);

                    HashSet<VoxelCoordinate> region = new HashSet<VoxelCoordinate>();

                    for(int ii=0; ii < region_size; ii++) {
                        for(int jj=0; jj < region_size; jj++) {
                            for(int kk=0; kk < region_size; kk++) {
                                vcoord[0] = ll_coord[0] + ii;
                                vcoord[1] = ll_coord[1] + jj;
                                vcoord[2] = ll_coord[2] + kk;
//System.out.println("Setting Data: " + java.util.Arrays.toString(vcoord) + " matID: " + matID);
                                grid.setData(vcoord[0], vcoord[1], vcoord[2], Grid.INSIDE, matID);
                                region.add(new VoxelCoordinate(vcoord));
                            }
                        }
                    }

                    regions.put(new Long(matID), region_size * region_size * region_size);

                    matID++;
                }
            }
        }

        //exportGrid(grid, "out.x3db");

        RegionFinder rf = new RegionFinder();
        List<Region> found_regions = rf.execute(grid);

        assertNotNull("Regions", found_regions);
        assertEquals("Regions size", regions.size(), found_regions.size());

        // Determine which region it is.

        Iterator<Region> itr3 = found_regions.iterator();

        while(itr3.hasNext()) {
            SetRegion region = (SetRegion) itr3.next();
            int count = region.getNumCoords();

            if (count != region_size * region_size * region_size) {
                fail("Invalid size region: " + count);
            }
        }
    }


    public static void main(String[] args) {
        TestRegionFinder finder = new TestRegionFinder();
        finder.testLotsOfRegions();
    }
}
