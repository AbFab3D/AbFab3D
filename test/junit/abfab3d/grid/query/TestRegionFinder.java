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
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.*;

// Internal Imports
import abfab3d.grid.query.RegionFinder;
import abfab3d.grid.*;

/**
 * Tests the functionality of the RegionFinder class
 *
 * @author Alan Hudson
 * @version
 */
public class TestRegionFinder extends TestCase {

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
        Grid grid = new ArrayGridByte(20,20,20,0.1,0.1);

        // Create a simple region
        grid.setData(5,5,5,Grid.EXTERIOR,1);
        grid.setData(5,5,6,Grid.EXTERIOR,1);
        grid.setData(5,5,7,Grid.EXTERIOR,1);
        grid.setData(5,6,5,Grid.EXTERIOR,1);
        grid.setData(5,6,6,Grid.EXTERIOR,1);
        grid.setData(5,6,7,Grid.EXTERIOR,1);

        RegionFinder rf = new RegionFinder(new VoxelCoordinate(5,5,5), 10);
        List<Region> regions = rf.execute(grid);

        assertNotNull("Regions", regions);
        assertEquals("Regions size", 1, regions.size());

        ListRegion region = (ListRegion) regions.get(0);
        int count = region.getNumCoords();

        assertEquals("ListRegion count", 6, count);
    }

    /**
     * Test basic operation
     */
    public void testMultiple() {
        int w = 20;
        int h = 20;
        int d = 20;

        Grid grid = new ArrayGridByte(20,20,20,0.1,0.1);

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
            grid.setData(vc.getX(), vc.getY(), vc.getZ(), Grid.EXTERIOR,1);
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
            grid.setData(vc.getX(), vc.getY(), vc.getZ(), Grid.INTERIOR,1);
        }


        RegionFinder rf = new RegionFinder(new VoxelCoordinate(5,5,5), 10);
        List<Region> regions = rf.execute(grid);

        assertNotNull("Regions", regions);
        assertEquals("Regions size", 2, regions.size());

        ListRegion region = (ListRegion) regions.get(0);
        int count = region.getNumCoords();

        assertEquals("ListRegion1 count", region1.size(), count);

        Iterator<VoxelCoordinate> itr2 = region.getList().iterator();
        while(itr2.hasNext()) {
            VoxelCoordinate vc = itr2.next();
            if (!region1.contains(vc)) {
                fail("Invalid coordinate in region: " + vc);
            }
        }

        region = (ListRegion) regions.get(1);
        count = region.getNumCoords();

        itr2 = region.getList().iterator();
        while(itr2.hasNext()) {
            VoxelCoordinate vc = itr2.next();
            if (!region2.contains(vc)) {
                fail("Invalid coordinate in region: " + vc);
            }
        }

        assertEquals("ListRegion2 count", region2.size(), count);

        // Test outside support
/*
        rf = new RegionFinder(new VoxelCoordinate(0,0,0), 10);
        regions = rf.execute(grid);

        assertNotNull("Regions", regions);
        assertEquals("Regions size", 2, regions.size());

        region = (ListRegion) regions.get(0);
        count = region.getNumCoords();

        assertEquals("Outside Region count", w*h*d - region1.size() - region2.size(), count);
*/
    }

}
