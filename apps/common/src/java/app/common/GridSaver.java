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

package app.common;

import abfab3d.grid.Grid;
import abfab3d.grid.util.ExecutionStoppedException;
import abfab3d.io.output.IsosurfaceMaker;
import abfab3d.io.output.MeshExporter;
import abfab3d.mesh.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import static java.lang.System.currentTimeMillis;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;

import org.web3d.vrml.sav.BinaryContentHandler;

import javax.vecmath.Point3d;


/**
 * Common code for saving grids.
 *
 * @author Alan Hudson
 */
public class GridSaver {
    /**
     * Write a grid using the IsoSurfaceMaker to the specified file
     *
     * @param grid
     * @param smoothSteps
     * @param maxCollapseError
     * @throws IOException
     */
    public static void writeIsosurfaceMaker(String filename, Grid grid, int smoothSteps, double maxCollapseError) throws IOException {
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        double vs = grid.getVoxelSize();


        double gbounds[] = new double[]{-nx*vs/2,nx*vs/2,-ny*vs/2,ny*vs/2,-nz*vs/2,nz*vs/2};
        double ibounds[] = extendBounds(gbounds, -vs/2);

        String encoding = filename.substring(filename.lastIndexOf(".")+1);

        IsosurfaceMaker im = new IsosurfaceMaker();
        im.setIsovalue(0.);
        im.setBounds(ibounds);
        im.setGridSize(nx, ny, nz);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();

        im.makeIsosurface(new IsosurfaceMaker.SliceGrid(grid, gbounds, 0), its);
        int[][] faces = its.getFaces();
        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), faces);

        double centerWeight = 1.0; // any non negative value is OK

        LaplasianSmooth ls = new LaplasianSmooth();
        ls.setCenterWeight(centerWeight);
        long t0 = currentTimeMillis();
        printf("smoothMesh(%d)\n", smoothSteps);
        t0 = currentTimeMillis();
        ls.processMesh(mesh, smoothSteps);
        printf("mesh smoothed in %d ms\n",(currentTimeMillis() - t0));

        int fcount = faces.length;

        if (maxCollapseError > 0) {
            MeshDecimator md = new MeshDecimator();
            md.setMaxCollapseError(maxCollapseError);
            long start_time = System.currentTimeMillis();

            int target = mesh.getTriangleCount() / 4;
            int current = fcount;
            System.out.println("Original face count: " + fcount);

            while(true) {
                target = mesh.getTriangleCount() / 2;
                System.out.println("Target face count : " + target);
                md.processMesh(mesh, target);

                current = mesh.getFaceCount();
                System.out.println("Current face count: " + current);
                if (current >= target * 1.25) {
                    System.out.println("Leaving loop");
                    // not worth continuing
                    break;
                }
            }

            fcount = mesh.getFaceCount();
            System.out.println("Final face count: " + fcount);
            System.out.println("Decimate time: " + (System.currentTimeMillis() - start_time));
        }

        if (encoding.equals("stl")) {
            MeshExporter.writeMeshSTL(mesh, fmt(filename, fcount));
        } else if (encoding.startsWith("x3d")) {
            MeshExporter.writeMesh(mesh, fmt(filename, fcount));
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + encoding);
        }
    }

    /**
     * Write a grid using the IsoSurfaceMaker to the specified file
     *
     * @param grid
     * @param smoothSteps
     * @param maxCollapseError
     * @throws IOException
     */
    public static void writeIsosurfaceMaker(Grid grid, OutputStream os, String encoding, int smoothSteps, double maxCollapseError) throws IOException {
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        double vs = grid.getVoxelSize();


        double gbounds[] = new double[]{-nx*vs/2,nx*vs/2,-ny*vs/2,ny*vs/2,-nz*vs/2,nz*vs/2};
        double ibounds[] = extendBounds(gbounds, -vs/2);

        IsosurfaceMaker im = new IsosurfaceMaker();
        im.setIsovalue(0.);
        im.setBounds(ibounds);
        im.setGridSize(nx, ny, nz);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();

        im.makeIsosurface(new IsosurfaceMaker.SliceGrid(grid, gbounds, 0), its);
        int[][] faces = its.getFaces();
        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), faces);

        double centerWeight = 1.0; // any non negative value is OK

        LaplasianSmooth ls = new LaplasianSmooth();

        ls.setCenterWeight(centerWeight);

        long t0 = currentTimeMillis();
        printf("smoothMesh(%d)\n",smoothSteps);
        t0 = currentTimeMillis();
        ls.processMesh(mesh, smoothSteps);
        printf("mesh smoohed in %d ms\n",(currentTimeMillis() - t0));

        int fcount = faces.length;

        if (maxCollapseError > 0) {
            MeshDecimator md = new MeshDecimator();
            md.setMaxCollapseError(maxCollapseError);
            long start_time = System.currentTimeMillis();

            int target = mesh.getTriangleCount() / 4;
            int current = fcount;
            System.out.println("Original face count: " + fcount);

            while(true) {
                target = mesh.getTriangleCount() / 2;
                System.out.println("Target face count : " + target);
                md.processMesh(mesh, target);

                current = mesh.getFaceCount();
                System.out.println("Current face count: " + current);
                if (current >= target * 1.25) {
                    // not worth continuing
                    break;
                }
            }

            fcount = mesh.getFaceCount();
            System.out.println("Final face count: " + fcount);
            System.out.println("Decimate time: " + (System.currentTimeMillis() - start_time));
        }

        double max_axis = Math.max(grid.getHeight() * grid.getSliceHeight(), grid.getWidth() * grid.getVoxelSize());
        max_axis = Math.max(max_axis, grid.getDepth() * grid.getVoxelSize());

        double z = 2 * max_axis / Math.tan(Math.PI / 4);
        float[] pos = new float[] {0,0,(float) z};

        if (encoding.equals("stl")) {
            // TODO: Need to implement streaming version
            throw new IllegalArgumentException("Unsupported file format: " + encoding);
            //MeshExporter.writeMeshSTL(mesh, os, encoding);
        } else if (encoding.startsWith("x3d")) {
            MeshExporter.writeMesh(mesh, os, encoding, pos);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + encoding);
        }
    }

    /**
     * Write a grid using the IsoSurfaceMaker to the specified file
     *
     * @param grid
     * @param smoothSteps
     * @param maxCollapseError
     * @throws IOException
     */
    public static void writeIsosurfaceMaker(Grid grid, BinaryContentHandler writer, Map<String,Object> params, int smoothSteps, double maxCollapseError) throws IOException {
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        double vs = grid.getVoxelSize();


        double gbounds[] = new double[]{-nx*vs/2,nx*vs/2,-ny*vs/2,ny*vs/2,-nz*vs/2,nz*vs/2};
        double ibounds[] = extendBounds(gbounds, -vs/2);

        IsosurfaceMaker im = new IsosurfaceMaker();
        im.setIsovalue(0.);
        im.setBounds(ibounds);
        im.setGridSize(nx, ny, nz);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();

        im.makeIsosurface(new IsosurfaceMaker.SliceGrid(grid, gbounds, 0), its);
        int[][] faces = its.getFaces();
        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), faces);

        double centerWeight = 1.0; // any non negative value is OK

        LaplasianSmooth ls = new LaplasianSmooth();

        ls.setCenterWeight(centerWeight);

        long t0 = currentTimeMillis();
        printf("smoothMesh(%d)\n", smoothSteps);
        t0 = currentTimeMillis();
        ls.processMesh(mesh, smoothSteps);
        printf("mesh smoothed in %d ms\n",(currentTimeMillis() - t0));

        int fcount = faces.length;

        if (maxCollapseError > 0) {
            MeshDecimator md = new MeshDecimator();
            md.setMaxCollapseError(maxCollapseError);
            long start_time = System.currentTimeMillis();

            int target = mesh.getTriangleCount() / 4;
            int current = fcount;
            System.out.println("Original face count: " + fcount);

            while(true) {
                target = mesh.getTriangleCount() / 2;
                System.out.println("Target face count : " + target);
                md.processMesh(mesh, target);

                current = mesh.getFaceCount();
                System.out.println("Current face count: " + current);
                if (current >= target * 1.25) {
                    // not worth continuing
                    break;
                }
            }

            fcount = mesh.getFaceCount();
            System.out.println("Final face count: " + fcount);
            System.out.println("Decimate time: " + (System.currentTimeMillis() - start_time));
        }

        double max_axis = Math.max(grid.getHeight() * grid.getSliceHeight(), grid.getWidth() * grid.getVoxelSize());
        max_axis = Math.max(max_axis, grid.getDepth() * grid.getVoxelSize());

        double z = 2 * max_axis / Math.tan(Math.PI / 4);
        float[] pos = new float[] {0,0,(float) z};

        MeshExporter.writeMesh(mesh, writer, params, pos);
    }

    /**
     * Write a grid using the IsoSurfaceMaker to the specified file
     *
     * @param grid
     * @param smoothSteps
     * @throws IOException
     */
    public static WingedEdgeTriangleMesh createIsosurface(Grid grid, int smoothSteps) throws IOException {
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        double vs = grid.getVoxelSize();


        double gbounds[] = new double[]{-nx*vs/2,nx*vs/2,-ny*vs/2,ny*vs/2,-nz*vs/2,nz*vs/2};
        double ibounds[] = extendBounds(gbounds, -vs/2);

        IsosurfaceMaker im = new IsosurfaceMaker();
        im.setIsovalue(0.);
        im.setBounds(ibounds);
        im.setGridSize(nx, ny, nz);

        printf("makeIsosurface()\n");
        long t0 = currentTimeMillis();

        //IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();
        //printf("using OLD IndexedTriangleSetBuilder\n");
        //IndexedTriangleSetBuilderNew its = new IndexedTriangleSetBuilderNew();
        int estimatedFaceCount = (nx*ny + ny*nz + nx*nz)*2*2;
        IndexedTriangleSetBuilderNew its = new IndexedTriangleSetBuilderNew(estimatedFaceCount); 
        printf("using NEW IndexedTriangleSetBuilder\n");
        im.makeIsosurface(new IsosurfaceMaker.SliceGrid(grid, gbounds, 0), its);
        printf("makeIsosurface() done in %d ms\n", (currentTimeMillis() - t0));
        //return null;
        
        if (Thread.currentThread().isInterrupted()) {
            throw new ExecutionStoppedException();
        }
        
        int faces[] = its.getFaces();
        printf("estimated face count: %d, actual face count: %d\n", estimatedFaceCount, faces.length/3);
        t0 = currentTimeMillis();
        printf("making WingedEdgeTriangleMesh\n");
        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), faces);
        printf("making WingedEdgeTriangleMesh done: %d\n", (currentTimeMillis()-t0));

        if (Thread.currentThread().isInterrupted()) {
            throw new ExecutionStoppedException();
        }

        double centerWeight = 1.0; // any non negative value is OK

        LaplasianSmooth ls = new LaplasianSmooth();

        ls.setCenterWeight(centerWeight);
        t0 = currentTimeMillis();
        printf("smoothMesh(%d)\n",smoothSteps);
        t0 = currentTimeMillis();
        ls.processMesh(mesh, smoothSteps);
        printf("mesh smoothed in %d ms\n",(currentTimeMillis() - t0));

        return mesh;
        
    }

        /**
        * Write a grid using the IsoSurfaceMaker to the specified file
        *
        * @param grid
        * @param smoothSteps
        * @param maxCollapseError
        * @throws IOException
        */
    public static void writeIsosurfaceMaker(Grid grid, BinaryContentHandler writer, Map<String,Object> params,
    		int smoothSteps, double maxCollapseError, boolean meshOnly) throws IOException {
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        double vs = grid.getVoxelSize();


        double gbounds[] = new double[]{-nx*vs/2,nx*vs/2,-ny*vs/2,ny*vs/2,-nz*vs/2,nz*vs/2};
        double ibounds[] = extendBounds(gbounds, -vs/2);

        IsosurfaceMaker im = new IsosurfaceMaker();
        im.setIsovalue(0.);
        im.setBounds(ibounds);
        im.setGridSize(nx, ny, nz);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();

        im.makeIsosurface(new IsosurfaceMaker.SliceGrid(grid, gbounds, 0), its);
        int[][] faces = its.getFaces();
        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), faces);

        double centerWeight = 1.0; // any non negative value is OK

        LaplasianSmooth ls = new LaplasianSmooth();

        ls.setCenterWeight(centerWeight);

        long t0 = currentTimeMillis();
        printf("smoothMesh(%d)\n", smoothSteps);
        t0 = currentTimeMillis();
        ls.processMesh(mesh, smoothSteps);
        printf("mesh processed: %d ms\n",(currentTimeMillis() - t0));

        // We could release the grid at this point
        int fcount = faces.length;

        if (maxCollapseError > 0) {
            MeshDecimator md = new MeshDecimator();
            md.setMaxCollapseError(maxCollapseError);
            long start_time = System.currentTimeMillis();

            int target = mesh.getTriangleCount() / 4;
            int current = fcount;
            System.out.println("Original face count: " + fcount);

            while(true) {
                target = mesh.getTriangleCount() / 2;
                System.out.println("Target face count : " + target);
                md.processMesh(mesh, target);

                current = mesh.getFaceCount();
                System.out.println("Current face count: " + current);
                if (current >= target * 1.25) {
                    // not worth continuing
                    break;
                }
            }

            fcount = mesh.getFaceCount();
            System.out.println("Final face count: " + fcount);
            System.out.println("Decimate time: " + (System.currentTimeMillis() - start_time));
        }

        double max_axis = Math.max(grid.getHeight() * grid.getSliceHeight(), grid.getWidth() * grid.getVoxelSize());
        max_axis = Math.max(max_axis, grid.getDepth() * grid.getVoxelSize());

        double z = 2 * max_axis / Math.tan(Math.PI / 4);
        float[] pos = new float[] {0,0,(float) z};

        MeshExporter.writeMesh(mesh, writer, params, pos, meshOnly, null);
    }

    /**
     * Write a grid using the IsoSurfaceMaker to the specified file
     *
     * @param maxCollapseError
     * @throws IOException
     */
    public static void writeIsosurfaceMaker(TriangleMesh mesh, int gw, int gh, int gd, double vs, double sh, BinaryContentHandler writer, Map<String,Object> params,
                                            double maxCollapseError, boolean meshOnly) throws IOException {
        // We could release the grid at this point
        int fcount = mesh.getFaceCount();

        if (maxCollapseError > 0) {

            MeshDecimatorNew md = new MeshDecimatorNew();
            System.out.println("*****Using new MeshDecimator*****");
/*
            MeshDecimator md = new MeshDecimator();
            System.out.println("*****Using old MeshDecimator*****");
*/
            md.setMaxCollapseError(maxCollapseError);
            long start_time = System.currentTimeMillis();

            int target = mesh.getTriangleCount() / 4;
            int current = fcount;
            System.out.println("Original face count: " + fcount);

            while(true) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new ExecutionStoppedException();
                }

                target = mesh.getTriangleCount() / 2;
                System.out.println("Target face count : " + target);
                md.processMesh(mesh, target);

                current = mesh.getFaceCount();
                System.out.println("Current face count: " + current);
                if (current >= target * 1.25) {
                    // not worth continuing
                    break;
                }
            }

            fcount = mesh.getFaceCount();
            System.out.println("Final face count: " + fcount);
            System.out.println("Decimate time: " + (System.currentTimeMillis() - start_time));
        }

        double max_axis = Math.max(gh * sh, gw * vs);
        max_axis = Math.max(max_axis, gd * vs);

        double z = 2 * max_axis / Math.tan(Math.PI / 4);
        float[] pos = new float[] {0,0,(float) z};

        MeshExporter.writeMesh(mesh, writer, params, pos, meshOnly, null);

        return;
    }

    /**
     return bounds extended by given margin
     */
    static double[] extendBounds(double bounds[], double margin){
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
