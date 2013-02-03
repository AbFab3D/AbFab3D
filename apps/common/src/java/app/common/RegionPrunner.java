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


import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Grid;
import abfab3d.grid.Region;
import abfab3d.grid.RegionTraverser;
import abfab3d.grid.query.RegionFinder;
import abfab3d.grid.util.ExecutionStoppedException;
import abfab3d.io.output.MeshExporter;
import abfab3d.io.output.SAVExporter;
import abfab3d.mesh.*;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.sav.BinaryContentHandler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static abfab3d.util.Output.printf;
import static java.lang.System.currentTimeMillis;

/**
 * Tools for changing the number of regions in a design.
 *
 * @author Alan Hudson
 */
public class RegionPrunner {
    static final String COLOR_REGION = "1 0 0 0";

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

        if (Thread.currentThread().isInterrupted()) {
            throw new ExecutionStoppedException();
        }

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

            if (Thread.currentThread().isInterrupted()) {
                throw new ExecutionStoppedException();
            }
        }
    }

    /**
     * Reduce a grid down to one largest region.
     *
     * @param grid
     */
    public static void reduceToOneRegion(Grid grid, BinaryContentHandler handler, double[] bounds) {
        System.out.println("Finding Regions: ");
        // Remove all but the largest region
        RegionFinder finder = new RegionFinder();
        List<Region> regions = finder.execute(grid);
        Region largest = regions.get(0);

        if (Thread.currentThread().isInterrupted()) {
            throw new ExecutionStoppedException();
        }

        System.out.println("Regions: " + regions.size());
        for (Region r : regions) {
            if (r.getVolume() > largest.getVolume()) {
                largest = r;
            }
            //System.out.println("Region: " + r.getVolume());
        }

        if (regions.size() > 1) {
            System.out.println("Largest Region: " + largest);
            Grid vis_grid = grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(), grid.getVoxelSize(), grid.getSliceHeight());

            RegionClearerAndCopier clearer = new RegionClearerAndCopier(grid, vis_grid);
            System.out.println("Clearing regions: ");
            for (Region r : regions) {
                if (r != largest) {
                    //System.out.println("   Region: " + r.getVolume());
                    r.traverse(clearer);
                }

                if (Thread.currentThread().isInterrupted()) {
                    throw new ExecutionStoppedException();
                }
            }


            // should isosurface value be 0.9?
            // This point style is faster to generate(4X) but doesn't look as nice.
            writePointVisFile(vis_grid, 0.9, bounds, handler, "debug", COLOR_REGION, "REMOVED_REGIONS");
//            writeVisFile(vis_grid, 0.9, 3, 1e-8, bounds, handler, "debug", COLOR_REGION, "REMOVED_REGIONS");
        }
    }

    /**
     * writes grid voxels to the visualization file
     */
    static void writePointVisFile(Grid grid, double isoValue, double[] bounds, BinaryContentHandler handler,
                             String material, String finish, String defName) {

        double vs = grid.getVoxelSize();

        abfab3d.io.output.IsosurfaceMaker im = new abfab3d.io.output.IsosurfaceMaker();
        im.setIsovalue(isoValue);

        im.setBounds(extendBounds(bounds, -vs / 2));
        im.setGridSize(grid.getWidth() - 1, grid.getHeight() - 1, grid.getDepth() - 1);

        PointSetBuilder its = new PointSetBuilder(100);

        im.makeIsosurface(new abfab3d.io.output.IsosurfaceMaker.SliceGrid(grid, bounds, 0), its);

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(SAVExporter.GEOMETRY_TYPE, SAVExporter.GeometryType.POINTSET);   // Required now for ITS?
        if (material != null) {
            params.put(SAVExporter.MATERIAL, material);
        }
        if (finish != null) {
            params.put(SAVExporter.FINISH, finish);
        }

        try {
            MeshExporter.writePointSet(its.getVertices(), handler, params, new float[3], true, defName);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * writes grid voxels to the visualization file
     */
    static void writeVisFile(Grid grid, double isoValue, int smooth, double maxDecimationError, double[] bounds, BinaryContentHandler handler,
                      String material, String finish, String defName) {

        try {
            // Voxelize and Decimate with fixed edge length = voxels size
            IndexedTriangleSetBuilder its = generateIsosurface(grid, isoValue, bounds);

            int[] faces = its.getFaces();
            WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), faces);

            double centerWeight = 1.0; // any non negative value is OK

            LaplasianSmooth ls = new LaplasianSmooth();

            ls.setCenterWeight(centerWeight);

            printf("***Smoothing mesh\n");
            long t0 = currentTimeMillis();
            printf("smoothMesh()\n");
            t0 = currentTimeMillis();
            ls.processMesh(mesh, smooth);
            printf("mesh processed: %d ms\n", (currentTimeMillis() - t0));

            int fcount = mesh.getTriangleCount();

            if (maxDecimationError > 0 && fcount > 0) {
                MeshDecimator md = new MeshDecimator();
                md.setMaxCollapseError(maxDecimationError);

                long start_time = System.currentTimeMillis();

                int target = mesh.getTriangleCount() / 4;
                int current = fcount;
                System.out.println("Original face count: " + fcount);

                while (target > 0) {
                    target = mesh.getTriangleCount() / 2;
                    System.out.println("Target face count : " + target);
                    md.processMesh(mesh, target);

                    current = mesh.getFaceCount();
                    System.out.println("Current face count: " + current);
                    if (current > target * 1.25 || target < 1) {
                        // not worth continuing
                        break;
                    }
                }

                fcount = mesh.getFaceCount();
                System.out.println("Final face count: " + fcount);
                System.out.println("Decimate time: " + (System.currentTimeMillis() - start_time));
            }

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put(SAVExporter.GEOMETRY_TYPE, SAVExporter.GeometryType.POINTSET);   // Required now for ITS?
            if (material != null) {
                params.put(SAVExporter.MATERIAL, material);
            }
            if (finish != null) {
                params.put(SAVExporter.FINISH, finish);
            }

            MeshExporter.writeMesh(mesh, handler, params, new float[3], true, defName);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static IndexedTriangleSetBuilder generateIsosurface(Grid grid, double isoValue, double[] bounds) {

        // isosurface grid is placed between centers of voxels of orginal grid
        // therefore isosurface grid size is decremented by one voxel
        double vs = grid.getVoxelSize();

        //removeBoundaryVoxels(grid);

        abfab3d.io.output.IsosurfaceMaker im = new abfab3d.io.output.IsosurfaceMaker();
        im.setIsovalue(isoValue);

        im.setBounds(extendBounds(bounds, -vs / 2));
        im.setGridSize(grid.getWidth() - 1, grid.getHeight() - 1, grid.getDepth() - 1);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();

        im.makeIsosurface(new abfab3d.io.output.IsosurfaceMaker.SliceGrid(grid, bounds, 0), its);

        return its;

    }

    /**
     * return bounds extended by given margin
     */
    static double[] extendBounds(double bounds[], double margin) {
        return new double[]{
                bounds[0] - margin,
                bounds[1] + margin,
                bounds[2] - margin,
                bounds[3] + margin,
                bounds[4] - margin,
                bounds[5] + margin,
        };
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

class RegionClearerAndCopier implements RegionTraverser {
    private Grid grid;
    private Grid copy;

    public RegionClearerAndCopier(Grid grid, Grid copy) {
        this.grid = grid;
        this.copy = copy;
    }

    @Override
    public void found(int x, int y, int z) {
        grid.setState(x, y, z, Grid.OUTSIDE);
        copy.setState(x, y, z, Grid.EXTERIOR);
    }

    @Override
    public boolean foundInterruptible(int x, int y, int z) {
        grid.setState(x, y, z, Grid.OUTSIDE);
        copy.setState(x, y, z, Grid.EXTERIOR);

        return true;
    }
}
