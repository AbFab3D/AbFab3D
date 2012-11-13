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

package app.common;


import abfab3d.grid.Grid;
import abfab3d.grid.Region;
import abfab3d.grid.RegionTraverser;
import abfab3d.grid.query.RegionFinder;

import java.util.List;

/**
 * Tools for changing the number of regions in a design.
 *
 * @author Alan Hudson
 */
public class RegionPrunner {
    public enum Regions {
        ALL, ONE
    }

    /**
     * Reduce a grid down to one largest region.
     *
     * @param grid
     */
    public static void reduceToOneRegion(Grid grid) {
        System.out.println("Finding Regions: ");
        // Remove all but the largest region
        RegionFinder finder = new RegionFinder();
        List<Region> regions = finder.execute(grid);
        Region largest = regions.get(0);

        System.out.println("Regions: " + regions.size());
        for (Region r : regions) {
            if (r.getVolume() > largest.getVolume()) {
                largest = r;
            }
            //System.out.println("Region: " + r.getVolume());
        }

        System.out.println("Largest Region: " + largest);
        RegionClearer clearer = new RegionClearer(grid);
        System.out.println("Clearing regions: ");
        for (Region r : regions) {
            if (r != largest) {
                //System.out.println("   Region: " + r.getVolume());
                r.traverse(clearer);
            }
        }
    }

}

class RegionClearer implements RegionTraverser {
    private Grid grid;

    public RegionClearer(Grid grid) {
        this.grid = grid;
    }

    @Override
    public void found(int x, int y, int z) {
        grid.setState(x, y, z, Grid.OUTSIDE);
    }

    @Override
    public boolean foundInterruptible(int x, int y, int z) {
        grid.setState(x, y, z, Grid.OUTSIDE);

        return true;
    }
}
