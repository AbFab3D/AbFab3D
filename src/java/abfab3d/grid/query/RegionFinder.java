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
import abfab3d.grid.util.GridVisitedIndexed;

import static abfab3d.util.Output.printf;

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

    private int maxRegionSize;

    /** The region we are using */
    private SetRegion region;

    /** All the regions found */
    private List<Region> regions;

    /** Visted voxels */
    private GridVisitedIndexed visited;

    /** The grid we are working on */
    private Grid grid;

    /** The material to restrict regions to or -1 for no restriction */
    private long mat;

    /**
     * Constructor.
     *
     */
    public RegionFinder() {
        this(Integer.MAX_VALUE,-1);
    }

    /**
     * Constructor.
     *
     * @param maxRegions The max number of regions to create.
     */
    public RegionFinder(int maxRegions) {
        this(maxRegions, Integer.MAX_VALUE,-1);
    }

    /**
     * Constructor.
     *
     * @param maxRegions The max number of regions to create.
     */
    public RegionFinder(int maxRegions, int maxRegionSize) {
        this(maxRegions, maxRegionSize,-1);
    }

    /**
     * Constructor.
     *
     * @param maxRegions The max number of regions to create.
     * @param mat The material to restrict finding to or -1 for no restriction
     */
    public RegionFinder(int maxRegions, int maxRegionSize, long mat) {
        this.maxRegions = maxRegions;
        this.maxRegionSize = maxRegionSize;
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
            return executeStateNew(grid);
        } else {
            return executeMaterial((AttributeGrid)grid);
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

        visited = new GridVisitedIndexed(grid, Grid.VoxelClasses.MARKED);

        VoxelCoordinate vc;

        vc = visited.findUnvisited();

        while(vc != null) {
            region = new SetRegion(1000);
            growRegion(vc, region);
            regions.add(region);

            if (regions.size() > maxRegions) {
                return regions;
            }

            vc = visited.findUnvisited();
        }

        return regions;
    }

    private List<Region> executeStateNew(Grid grid) {
        ArrayList<Region> ret_val = new ArrayList<Region>();

        // TODO: Should support MARKED as well
        byte state = Grid.INTERIOR;

        int nx1 = grid.getWidth()-1;
        int ny1 = grid.getHeight()-1;
        int nz1 = grid.getDepth()-1;

        GridBit mask = new GridBitIntervals(nx1+1,ny1+1, nz1+1);

        int compCount = 0;
        int volume = 0;

        zcycle:

        for(int z = 1; z < nz1; z++){

            for(int x = 1; x < nx1; x++){

                for(int y = 1; y < ny1; y++){

                    if(mask.get(x,y,z) != 0)// already visited
                        continue;

                    if(ConnectedComponentState.compareState(grid, x,y,z, state)){

                        ConnectedComponentState sc = new  ConnectedComponentState(grid, mask, x,y,z,state, true, ConnectedComponentState.DEFAULT_ALGORITHM);

                        ArrayInt coords = sc.getComponents();
                        ret_val.add(new ArrayRegion(coords, true));
                        volume+= sc.getVolume();
                        if(maxRegionSize > 0 && compCount > maxRegionSize)
                            break zcycle;
                    }
                }
            }
        }

        mask.release();

        return ret_val;
    }

    /**
     * Find the regions using state and material information.
     *
     * @param grid The grid to use for grid src
     * @return The region of voxels
     */
    private List<Region> executeMaterial(AttributeGrid grid) {
        this.grid = grid;
        regions = new ArrayList<Region>();

        visited = new GridVisitedIndexed(grid, Grid.VoxelClasses.MARKED, mat);

        VoxelCoordinate vc;

        vc = visited.findUnvisited();

        while(vc != null) {
            region = new SetRegion(1000);
            growRegionMaterial(vc, region);
            regions.add(region);

            if (regions.size() > maxRegions) {
                return regions;
            }

            vc = visited.findUnvisited();
        }

        return regions;
    }

    /**
     * Grow a region from a starting seed.
     */
    private void growRegion(VoxelCoordinate start, SetRegion region) {
        int start_state = grid.getState(start.getX(), start.getY(), start.getZ());

        LinkedList<VoxelCoordinate> que = new LinkedList<VoxelCoordinate>();

        que.add(start);
        visited.setVisited(start);

        while(!que.isEmpty()) {
            VoxelCoordinate vc = que.remove();

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

                                if (start_state == Grid.OUTSIDE && state != Grid.OUTSIDE)
                                    continue;

                                if (start_state != Grid.OUTSIDE && state == Grid.OUTSIDE)
                                    continue;


                                que.offer(new VoxelCoordinate(ni,nj,nk));
                                visited.setVisited(ni,nj,nk);
                                //System.out.println("que size: " + que.size());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Grow a region from a starting seed.
     */
    private void growRegionOld(VoxelCoordinate start, SetRegion region) {
        int start_state = grid.getState(start.getX(), start.getY(), start.getZ());

        HashSet<VoxelCoordinate> new_list = new HashSet<VoxelCoordinate>();
        new_list.add(start);

        ArrayList<VoxelCoordinate> add_list = new ArrayList<VoxelCoordinate>();

        int max_size1 = 0;
        int max_size2 = 0;

        while(new_list.size() > 0) {
            Iterator<VoxelCoordinate> itr2 = new_list.iterator();

            while(itr2.hasNext()) {
                VoxelCoordinate vc = itr2.next();

                visited.setVisited(vc);

                int i = vc.getX();
                int j = vc.getY();
                int k = vc.getZ();

                int state = grid.getState(i,j,k);

/*
                if (start_state == Grid.OUTSIDE && state != Grid.OUTSIDE)
                    continue;

                if (start_state != Grid.OUTSIDE && state == Grid.OUTSIDE)
                    continue;
*/

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

System.out.println("Add list: " + add_list.size());
            if (add_list.size() > max_size1) {
                max_size1 = add_list.size();
            }

            Iterator<VoxelCoordinate> itr = add_list.iterator();
            while(itr.hasNext()) {

                VoxelCoordinate vc = itr.next();
                if (!visited.getVisited(vc)) {
                    new_list.add(vc);
                }
            }

            if (new_list.size() > max_size2) {
                max_size2 = new_list.size();
            }

            add_list.clear();
        }

        System.out.println("add list max: " + max_size1 + " new_list max: " + max_size2);
    }

    /**
     * Grow a region from a starting seed.
     */
    private void growRegionMaterial(VoxelCoordinate start, SetRegion region) {
        int start_state = grid.getState(start.getX(), start.getY(), start.getZ());

        HashSet<VoxelCoordinate> new_list = new HashSet<VoxelCoordinate>();
        new_list.add(start);

        HashSet<VoxelCoordinate> add_list = new HashSet<VoxelCoordinate>();

        VoxelData vd = grid.getVoxelData();

        while(new_list.size() > 0) {
            Iterator<VoxelCoordinate> itr2 = new_list.iterator();

            while(itr2.hasNext()) {
                VoxelCoordinate vc = itr2.next();

                visited.setVisited(vc);

                int i = vc.getX();
                int j = vc.getY();
                int k = vc.getZ();

                grid.getData(i,j,k,vd);

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
                                    state = grid.getState(ni,nj,nk);

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
                if (!visited.getVisited(vc)) {
                    new_list.add(vc);
                }
            }
            add_list.clear();
        }
    }
}
