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

import abfab3d.util.MathUtil;
import abfab3d.util.TriangleCounter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static java.lang.System.currentTimeMillis;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.MathUtil.extendBounds;

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
        int[] faces = its.getFaces();
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
            mesh = decimateMesh(mesh, maxCollapseError);
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
        int[] faces = its.getFaces();
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
            mesh  = decimateMesh(mesh,maxCollapseError);
        }

        float[] pos = new float[] {0,0,(float) getViewDistance(grid)};

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
        int[] faces = its.getFaces();
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
            mesh = decimateMesh(mesh, maxCollapseError);
        }

        float[] pos = new float[] {0,0,(float) getViewDistance(grid)};

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
        int estimatedFaceCount = (nx*ny + ny*nz + nx*nz)*2*2;
        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(estimatedFaceCount);


        im.makeIsosurface(new IsosurfaceMaker.SliceGrid(grid, gbounds, 0), its);
        printf("using NEW IsosurfaceMaker");

        printf("makeIsosurface() done in %d ms\n", (currentTimeMillis() - t0));
        //return null;
        
        if (Thread.currentThread().isInterrupted()) {
            throw new ExecutionStoppedException();
        }
        
        t0 = currentTimeMillis();
        printf("making WingedEdgeTriangleMesh\n");
        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
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
     * @throws IOException
     */
    public static abfab3d.mesh.WingedEdgeTriangleMesh createIsosurface2(Grid grid, int smoothSteps, int resamplingFactor) throws IOException {

        double bounds[] = new double[]{-grid.getWidth()/2 * grid.getVoxelSize()
                                       ,grid.getWidth()/2*grid.getVoxelSize(),
                                       -grid.getHeight()/2*grid.getSliceHeight(),
                                       grid.getHeight()/2*grid.getSliceHeight(),
                                       -grid.getDepth()/2*grid.getVoxelSize(),
                                       grid.getDepth()/2*grid.getVoxelSize()};

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        double dx2 = resamplingFactor*(bounds[1] - bounds[0])/nx;
        double dy2 = resamplingFactor*(bounds[3] - bounds[2])/ny;
        double dz2 = resamplingFactor*(bounds[5] - bounds[4])/nz;

        int nx2 = (nx+resamplingFactor-1)/resamplingFactor;
        int ny2 = (ny+resamplingFactor-1)/resamplingFactor;
        int nz2 = (nz+resamplingFactor-1)/resamplingFactor;
        printf("nx2:[%d,%d,%d]\n",nx2, ny2, nz2);

        double bounds2[] = new double[]{
                bounds[0], bounds[0] + dx2*nx2,
                bounds[2], bounds[2] + dy2*ny2,
                bounds[4], bounds[4] + dz2*nz2
        };

        IsosurfaceMaker im = new IsosurfaceMaker();
        im.setIsovalue(0.);
        im.setBounds(bounds2);
        im.setGridSize(nx2, ny2, nz2);

        printf("makeIsosurface()\n");
        long t0 = currentTimeMillis();

        //IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();
        //printf("using OLD IndexedTriangleSetBuilder\n");
        int estimatedFaceCount = (nx*ny + ny*nz + nx*nz)*2*2;
        abfab3d.mesh.IndexedTriangleSetBuilder its = new abfab3d.mesh.IndexedTriangleSetBuilder(estimatedFaceCount);

        // cut in half
        im.makeIsosurface(new IsosurfaceMaker.SliceGrid2(grid, bounds, resamplingFactor, 2.9), its);

        printf("makeIsosurface() done in %d ms\n", (currentTimeMillis() - t0));
        //return null;

        if (Thread.currentThread().isInterrupted()) {
            throw new ExecutionStoppedException();
        }

        t0 = currentTimeMillis();
        printf("making WingedEdgeTriangleMesh\n");
        abfab3d.mesh.WingedEdgeTriangleMesh mesh = new abfab3d.mesh.WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
        printf("making WingedEdgeTriangleMesh done: %d\n", (currentTimeMillis()-t0));

        if (Thread.currentThread().isInterrupted()) {
            throw new ExecutionStoppedException();
        }

        double centerWeight = 1.0; // any non negative value is OK

        abfab3d.mesh.LaplasianSmooth ls = new abfab3d.mesh.LaplasianSmooth();

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
        int[] faces = its.getFaces();
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
            mesh = decimateMesh(mesh, maxCollapseError);
        }
        
        writeMesh(mesh, getViewDistance(grid), writer, params, meshOnly);
        
    }




    /**
     * Write a grid mesh into output
     *
     * @throws IOException
     */
    public static void writeMesh(WingedEdgeTriangleMesh mesh,
                                 String filename
                                 ) throws IOException {

        MeshExporter.writeMesh(mesh, filename);

        return;
    }

    /**
     * Write a grid mesh into output
     *
     * @throws IOException
     */
    public static void writeMesh(WingedEdgeTriangleMesh mesh, 
                                 double viewDistance, 
                                 BinaryContentHandler writer, 
                                 Map<String,Object> params,
                                 boolean meshOnly ) throws IOException {
        
        float[] pos = new float[] {0,0,(float) viewDistance};

        MeshExporter.writeMesh(mesh, writer, params, pos, meshOnly, null);

        return;
    }

    /**
       retuns good viewpoint for given box
     */
    public static double getViewDistance(Grid grid){
        
        double bounds[] = new double[6];
        grid.getGridBounds(bounds);

        double sizex = bounds[1] - bounds[0];
        double sizey = bounds[3] - bounds[2]; 
        double sizez = bounds[5] - bounds[4]; 
        
        double max = sizex;
        if(sizey > max) max = sizey;
        if(sizez > max) max = sizez;

        double z = 2 * max / Math.tan(Math.PI / 4);
        return z;
        
    }

    /**
       returns mesh with largest shell 
     */
    public static ShellResults getLargestShell(WingedEdgeTriangleMesh mesh, int minVolume){
    
        ShellFinder shellFinder = new ShellFinder();
        ShellFinder.ShellInfo shells[] = shellFinder.findShells(mesh);        
        printf("shellsCount: %d\n",shells.length);

        int regions_removed = 0;

        if(shells.length > 1){
            
            ShellFinder.ShellInfo maxShell = shells[0];
            
            for(int i = 0; i < shells.length; i++){
                
                printf("shell: %d faces\n",shells[i].faceCount);
                if(shells[i].faceCount > maxShell.faceCount){
                    maxShell = shells[i];
                }
            }

            for(int i = 0; i < shells.length; i++){

                if (shells[i] != maxShell) {
                    if (shells[i].faceCount >= minVolume) {
                        regions_removed++;
                    }
                }
            }
            printf("extracting largest shell: %d\n",maxShell.faceCount);
            IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(maxShell.faceCount);
            shellFinder.getShell(mesh, maxShell.startFace, its);
            mesh = new WingedEdgeTriangleMesh(its.getVertices(),its.getFaces());
            return new ShellResults(mesh, regions_removed);

        } else {

            return new ShellResults(mesh,regions_removed);
        }
    }

    /**
     * Returns up to numShells shells that are above the minimum volume.
     *
     * @param mesh The mesh
     * @param numShells The maximum number of shells
     * @param minVolume The minimum volume
     */
    public static ShellResults getLargestShells(WingedEdgeTriangleMesh mesh, int numShells, double minVolume){

        ShellFinder shellFinder = new ShellFinder();
        ShellFinder.ShellInfo shells[] = shellFinder.findShells(mesh);
        printf("shellsCount: %d\n",shells.length);

        int regions_removed = 0;

        System.out.println("Minimum volume: " + minVolume);
        ArrayList<ShellData> saved_shells = new ArrayList<ShellData>();
        int face_count = 0;
        int cnt = 0;
        for(int i=0; i < shells.length; i++) {
            AreaCalculator ac = new AreaCalculator();
            shellFinder.getShell(mesh, shells[i].startFace, ac);
            mesh.getTriangles(ac);
            double volume = ac.getVolume();

            System.out.println("   vol: " + volume);
            if (volume >= minVolume) {
                saved_shells.add(new ShellData(shells[i],volume));
                if (cnt < numShells) {
                    face_count += shells[i].faceCount;
                }
                cnt++;
            } else {
                regions_removed++;
            }
        }

        Collections.sort(saved_shells, Collections.reverseOrder());

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(face_count);
        int shell_cnt = 0;
        for(ShellData sd : saved_shells) {
            shellFinder.getShell(mesh, sd.info.startFace, its);
            shell_cnt++;
            if (shell_cnt >= numShells) break;
        }

        printf("extracting largest shells: %d\n",face_count);
        mesh = new WingedEdgeTriangleMesh(its.getVertices(),its.getFaces());
        return new ShellResults(mesh, regions_removed);
    }

    public static WingedEdgeTriangleMesh decimateMesh(WingedEdgeTriangleMesh mesh, double maxCollapseError ){
        
            printf("GridSaver.decimateMesh()\n");

            MeshDecimator md = new MeshDecimator();            
            md.setMaxCollapseError(maxCollapseError);
            long start_time = System.currentTimeMillis();
            
            int fcount = mesh.getTriangleCount();
            int target = fcount / 2;
            int current = fcount;
            printf("   Original face count: " + fcount);

            while(true) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new ExecutionStoppedException();
                }

                target = mesh.getTriangleCount() / 2;
                printf("   Target face count : %d\n", target);
                md.processMesh(mesh, target);

                current = mesh.getFaceCount();
                printf("   Current face count: %d \n", current);
                if (current >= target * 1.25) {
                    // not worth continuing
                    break;
                }
            }            
            fcount = current;            
            printf("   Final face count: %d \n",fcount);
            return mesh;
    }


    /**
     * Write a grid mesh into output
     *
     * @param maxCollapseError
     * @throws IOException
     */
    /*
    public static void writeMesh(WingedEdgeTriangleMesh mesh, 
                                 double sizex, double sizey, double sizez, 
                                 BinaryContentHandler writer, Map<String,Object> params,
                                 double maxCollapseError, boolean meshOnly,
                                 boolean writeLargestShellOnly) throws IOException {

        // We could release the grid at this point
        int fcount = mesh.getFaceCount();

        if (maxCollapseError > 0) {

            MeshDecimator md = new MeshDecimator();

            md.setMaxCollapseError(maxCollapseError);
            long start_time = System.currentTimeMillis();

            int target = mesh.getTriangleCount() / 2;
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
            
            
            System.out.println("Decimate time: " + (System.currentTimeMillis() - start_time)  + " ms");
            
        }
        
        if(writeLargestShellOnly) {
            ShellFinder shellFinder = new ShellFinder();
            long t0 = time();
            ShellFinder.ShellInfo shells[] = shellFinder.findShells(mesh);

            printf("shellsCount: %d\n",shells.length);

            if(shells.length > 1){
                
                ShellFinder.ShellInfo maxShell = shells[0];

                for(int i = 0; i < shells.length; i++){

                    printf("shell: %d faces\n",shells[i].faceCount);
                    if(shells[i].faceCount > maxShell.faceCount){
                        maxShell = shells[i];
                    }                        
                }
                
                printf("extracting largest shell: %d\n",maxShell.faceCount);               
                IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(maxShell.faceCount);
                shellFinder.getShell(mesh, maxShell.startFace, its);
                mesh = new WingedEdgeTriangleMesh(its.getVertices(),its.getFaces());
            }
            printf("shell extraction: %d ms\n", (time() - t0));
        }

        double max_axis = Math.max(gh * sh, gw * vs);
        max_axis = Math.max(max_axis, gd * vs);

        double z = 2 * max_axis / Math.tan(Math.PI / 4);
        float[] pos = new float[] {0,0,(float) z};


        MeshExporter.writeMesh(mesh, writer, params, pos, meshOnly, null);

        return;
    }
    */
}

class ShellData implements Comparable<ShellData> {
    public ShellFinder.ShellInfo info;
    public double volume;


    ShellData(ShellFinder.ShellInfo info, double volume) {
        this.info = info;
        this.volume = volume;
    }

    @Override
    public int compareTo(ShellData o) {
        return Double.compare(volume, o.volume);
    }
}