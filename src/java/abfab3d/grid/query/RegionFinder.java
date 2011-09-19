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
 * Find all the regions in a grid.
 *
 * TODO: no correct for finding OUTSIDE region
 *
 * TODO:  Add ability to find regions based on material
 *
 * @author Alan Hudson
 */
public class RegionFinder {
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

    /** The material to restrict regions to or -1 for no restriction */
    private int mat;

    /**
     * Constructor.
     *
     * @param max The max number of regions to create.
     */
    public RegionFinder(int max) {
        this(max,-1);
    }

    /**
     * Constructor.
     *
     * @param max The max number of regions to create.
     * @param mat The material to restrict finding to or -1 for no restriction
     */
    public RegionFinder(int max, int mat) {
        this.maxRegions = max;
        this.mat = mat;
    }

    /**
     * Find the regions.
     *
     * @param grid The grid to use for grid src
     * @return The region of voxels
     */
    public List<Region> execute(Grid grid) {
        if (mat == -1) {
            return executeState(grid);
        } else {
            return executeMaterial(grid);
        }
    }

    /**
     * Find the regions using state information.
     *
     * @param grid The grid to use for grid src
     * @return The region of voxels
     */
    private List<Region> executeState(Grid grid) {
        this.grid = grid;
        regions = new ArrayList<Region>();

        visited = new GridVisited(grid.getWidth(), grid.getHeight(), grid.getDepth());

        VoxelCoordinate vc;

        vc = visited.findUnvisited(grid);
System.out.println("Starting at vc: " + vc);

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
     * Find the regions using state and material information.
     *
     * @param grid The grid to use for grid src
     * @return The region of voxels
     */
    private List<Region> executeMaterial(Grid grid) {
        this.grid = grid;
        regions = new ArrayList<Region>();

        visited = new GridVisited(grid.getWidth(), grid.getHeight(), grid.getDepth());

        VoxelCoordinate vc;

        vc = visited.findUnvisited(grid,mat);

        while(vc != null) {
            region = new ListRegion(1000);
            growRegionMaterial(vc, region);
            regions.add(region);

            if (regions.size() > maxRegions) {
                return regions;
            }

            vc = visited.findUnvisited(grid,mat);
        }

        return regions;
    }

    /**
     * Grow a region from a starting seed.
     */
    private void growRegion(VoxelCoordinate start, ListRegion region) {
        int start_state = grid.getState(start.getX(), start.getY(), start.getZ());

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

                if (start_state == Grid.OUTSIDE && state != Grid.OUTSIDE)
                    continue;

                if (start_state != Grid.OUTSIDE && state == Grid.OUTSIDE)
                    continue;

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

                                    if (start_state == Grid.OUTSIDE && state != Grid.OUTSIDE)
                                        continue;

                                    if (start_state != Grid.OUTSIDE && state == Grid.OUTSIDE)
                                        continue;

                                    add_list.add(new VoxelCoordinate(ni,nj,nk));
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

    /**
     * Grow a region from a starting seed.
     */
    private void growRegionMaterial(VoxelCoordinate start, ListRegion region) {
        int start_state = grid.getState(start.getX(), start.getY(), start.getZ());

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

                VoxelData vd = grid.getData(i,j,k);

                int state = vd.getState();

                if (start_state == Grid.OUTSIDE && state != Grid.OUTSIDE)
                    continue;

                if (start_state != Grid.OUTSIDE && state == Grid.OUTSIDE)
                    continue;

                if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                    if (vd.getMaterial() != mat) {
                        continue;
                    }

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
                                    vd = grid.getData(ni,nj,nk);

                                    state = vd.getState();

                                    if (start_state == Grid.OUTSIDE && state != Grid.OUTSIDE)
                                        continue;

                                    if (start_state != Grid.OUTSIDE && state == Grid.OUTSIDE)
                                        continue;

                                    if (vd.getMaterial() != mat)
                                        continue;

                                    add_list.add(new VoxelCoordinate(ni,nj,nk));
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
