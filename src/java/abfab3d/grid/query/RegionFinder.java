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
import java.util.*;

// Internal Imports
import abfab3d.grid.*;
import abfab3d.grid.util.GridVisited;

/**
 * Find all the regions in a grid.  It starts from the main region and
 * continues to all other regions.
 *
 * @author Alan Hudson
 */
public class RegionFinder {
    /** The main region */
    private VoxelCoordinate main;

    /** The max number of regions to find */
    private int maxRegions;

    /** The region we are using */
    private ListRegion region;

    /** All the regions found */
    private List<Region> regions;

    /** Visted voxels */
    private GridVisited visited;

    /** The grid we are working on */
    private Grid grid;

    /**
     * Constructor.
     *
     * @param main The main region to start with
     * @param max The max number of regions to create.
     */
    public RegionFinder(VoxelCoordinate main, int max) {
        this.main = main;
        this.maxRegions = max;
    }

    /**
     * Find the regions.
     *
     * @param grid The grid to use for grid src
     * @return The region of voxels
     */
    public List<Region> execute(Grid grid) {
        this.grid = grid;
        regions = new ArrayList<Region>();

        visited = new GridVisited(grid.getWidth(), grid.getHeight(), grid.getDepth());

        // TODO:  No idea how to guess region size
        region = new ListRegion(1000);

        growRegion(main, region);

        regions.add(region);

        VoxelCoordinate vc;

        vc = visited.findUnvisited(grid);

        while(vc != null) {
            region = new ListRegion(1000);
            growRegion(vc, region);
            regions.add(region);

            if (regions.size() > maxRegions) {
                return regions;
            }

            vc = visited.findUnvisited(grid);
        }

        return regions;
    }

    /**
     * Grow a region from a starting seed.
     */
    private void growRegion(VoxelCoordinate start, ListRegion region) {

        ArrayList<VoxelCoordinate> new_list = new ArrayList<VoxelCoordinate>();
        new_list.add(start);

        ArrayList<VoxelCoordinate> add_list = new ArrayList<VoxelCoordinate>();

        while(new_list.size() > 0) {
            Iterator<VoxelCoordinate> itr2 = new_list.iterator();

            while(itr2.hasNext()) {
                VoxelCoordinate vc = itr2.next();

                if (visited.getVisited(vc)) {
                    // Avoid circular adds when add_list happens before new_list processing
                    // of a coordinate.  There might be a better way to handle this.
                    continue;
                }

                visited.setVisited(vc,true);

                int i = vc.getX();
                int j = vc.getY();
                int k = vc.getZ();

                int state = grid.getState(i,j,k);

                if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                    region.add(vc);

                    // test adjacent voxels

                    for(int n1=-1; n1 < 2; n1++) {
                        for(int n2=-1; n2 < 2; n2++) {
                            for(int n3=-1; n3 < 2; n3++) {
                                if (n1 == 0 && n2 == 0 && n3 == 0)
                                    continue;

                                int ni = i+n1;
                                int nj = j+n2;
                                int nk = k+n3;

                                if (grid.insideGrid(ni,nj,nk) && !visited.getVisited(ni,nj,nk)) {
                                    state = grid.getState(ni,nj,nk);
                                    if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                        add_list.add(new VoxelCoordinate(ni,nj,nk));
                                    }
                                }
                            }
                        }
                    }
                }

            }

            new_list.clear();

            Iterator<VoxelCoordinate> itr = add_list.iterator();
            while(itr.hasNext()) {
                VoxelCoordinate vc = itr.next();
                new_list.add(vc);
            }
            add_list.clear();
        }
    }
}
