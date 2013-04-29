/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.mesh;

import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.Grid;
import abfab3d.grid.GridShortIntervals;
import abfab3d.grid.op.GridMaker;
import abfab3d.io.output.IsosurfaceMaker;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.STLWriter;
import abfab3d.util.DataSource;
import abfab3d.util.MathUtil;
import abfab3d.util.Vec;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static abfab3d.mesh.TestMeshDecimator.loadMesh;
import static abfab3d.util.Output.*;
import static java.lang.System.nanoTime;

// External Imports

/**
 * Tests the functionality of MeshDecimatorMT
 *
 * @author Vladimir Bulatov
 */
public class TestMeshDecimatorMT extends TestCase {

    static final double MM = 0.001; // mm -> m conversion 

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestMeshDecimatorMT.class);
    }

    public void _testFile() throws Exception {
        //String fpath = "test/models/speed-knot.x3db";
        //String fpath = "test/models/sphere_10cm_rough_manifold.x3dv";
        //String fpath = "test/models/sphere_10cm_smooth_manifold.x3dv";

        //String fpath = "c:/tmp/text_iso_0.stl";  // 250K faces
        String fpath = "c:/tmp/leaf_01.stl";     // 1.6M faces 
        //String fpath = "c:/tmp/ring_image_45.stl"; // 1.0M faces
        //String fpath = "c:/tmp/cup_image.stl"; // 1.0M faces 
        //String fpath = "c:/tmp/ring_with_band.stl"; // 1.7M faces
        //String fpath = "/tmp/ring_plain_3.stl"; // 500K faces 
        //String fpath = "/tmp/dilationShape1.x3d"; // 100K faces 

        //String fpath = "/tmp/dilationBlockDilated_17.x3d"; // 15K faces 


        //String fpath = "c:/tmp/text_iso_2.stl";
        //String fpath = "c:/tmp/sf31.stl";
        //String fpath = "c:/tmp/leaf_01_0832206.stl";
        //String fpath = "c:/tmp/sf21.stl";
        //String fpath = "c:/tmp/rtc_v3_04.stl";

        for (int i = 0; i < 20; i++) {
            runFile(fpath);
        }

    }

    public void runFile(String fpath) throws Exception {


        long t0 = time();
        WingedEdgeTriangleMesh mesh = loadMesh(fpath);
        printf("mesh loading: %d ms\n", (time() - t0));
        t0 = time();

        int fcount = mesh.getFaceCount();
        //MeshExporter.writeMeshSTL(mesh,fmt("/tmp/00_mesh_orig_%07d.stl", fcount));

        printf("mesh faces: %d, vertices: %d, edges: %d\n", fcount, mesh.getVertexCount(), mesh.getEdgeCount());
        printf("initial counts: faces: %d, vertices: %d, edges: %d \n", mesh.getFaceCount(), mesh.getVertexCount(), mesh.getEdgeCount());

        //assertTrue("Initial Manifold", TestWingedEdgeTriangleMesh.isManifold(mesh));

        //MeshDecimator md = new MeshDecimator();
        MeshDecimatorMT md = new MeshDecimatorMT();
        md.setThreadCount(4);

        md.setMaxCollapseError(1.0);

        int target;
        int current;

        int count = 1;

        while (count-- > 0) {

            target = mesh.getTriangleCount() / 2;
            printf("Target face count : %d \n", target);
            t0 = time();
            md.processMesh(mesh, target);
            //md.DEBUG = true;
            printf("processMesh() done %d ms\n", (time() - t0));
            fcount = mesh.getFaceCount();
            printf("Actual face count: %d\n", fcount);

            //MeshExporter.writeMeshSTL(mesh,fmt("/tmp/00_mesh_%07d.stl", fcount));

            current = fcount;

            System.out.println("Current face count: " + current);

            if (current >= target * 1.25) {
                System.out.println("Leaving loop");
                // not worth continuing
                break;
            }

        }

    }

    public void _testOpenBox() throws Exception {

        double vert[] = new double[]{
                1, -1, 1,
                1, 1, 1,
                -1, 1, 1,
                -1, -1, 1,
                1, -1, -1,
                1, 1, -1,
                -1, 1, -1,
                -1, -1, -1
        };
        int indexes[] = new int[]{
                2, 3, 0,
                1, 2, 0,

                5, 1, 0,
                4, 5, 0,

                5, 6, 2,
                5, 2, 1,

                2, 6, 7,
                2, 7, 3,

                6, 5, 4,
                7, 6, 4,

                //3, 7, 4,
                //3, 4, 0
        };

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(vert, indexes);

        STLWriter stl = new STLWriter("/tmp/00_box.stl");
        mesh.getTriangles(stl);
        stl.close();


        MeshDecimator md = new MeshDecimator();

        //md.setMaxCollapseError(1.e-8);
        //md.setMaxEdgeLength(0.3*MM);

        int count = 1;

        int fcount = mesh.getTriangleCount();

        while (count-- > 0) {

            int target = fcount / 2;
            md.processMesh(mesh, target);
            fcount = mesh.getTriangleCount();

            stl = new STLWriter(fmt("/tmp/00_boundary_%d.stl", count));
            mesh.getTriangles(stl);
            stl.close();

        }


    }

    public void _testMeshWithBoundary() throws Exception {

        long t00 = time();
        int cellGrid = 200; // grid dimension of one cell 
        int cellsCount = 1; // count of part along each dimension 
        double sphereSize = 10 * MM;
        double voxelSize = sphereSize / cellGrid;

        double cellSize = cellGrid * voxelSize;
        double bodySize = cellSize * cellsCount;

        int nx = cellGrid * cellsCount;

        printf("grid: [%d x %d x %d]\n", nx, nx, nx);
        printf("cell: [%d x %d x %d]\n", cellGrid, cellGrid, cellGrid);
        printf("cells: [%d x %d x %d]\n", cellsCount, cellsCount, cellsCount);
        //printf("threads: %d\n", threadCount);

        ArrayOfSpheres spheres = new ArrayOfSpheres(cellSize, 0.97);

        long t0 = time();

        //Grid grid = new ArrayAttributeGridByte(nx,nx,nx,voxelSize, voxelSize);
        Grid grid = new GridShortIntervals(nx, nx, nx, voxelSize, voxelSize);

        GridMaker gridMaker = new GridMaker();

        double gridBounds[] = new double[]{0, bodySize, 0, bodySize, 0, bodySize};
        int goffset = 10;
        double offset = goffset * voxelSize;
        double padding = 0.5 * voxelSize;

        double ibounds[] = new double[]{padding + offset, bodySize - padding - offset,
                padding + offset, bodySize - padding - offset,
                padding + offset, bodySize - padding - offset};

        gridMaker.setBounds(gridBounds);
        gridMaker.setDataSource(new ArrayOfSpheres(sphereSize, 0.97));
        gridMaker.makeGrid(grid);
        printf("grid made: %d ms\n", (time() - t0));

        IsosurfaceMaker im = new IsosurfaceMaker();
        im.setIsovalue(0.);

        im.setBounds(ibounds);
        im.setGridSize(nx - 2 * goffset, nx - 2 * goffset, nx - 2 * goffset);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();

        im.makeIsosurface(new IsosurfaceMaker.SliceGrid(grid, gridBounds, 0), its);

        printf("isosurface made: %d ms\n", (time() - t0));

        STLWriter stl = new STLWriter("/tmp/00_boundary.stl");
        its.getTriangles(stl);
        stl.close();

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getVertexCount(), its.getFaces(), its.getFaceCount());

        MeshDecimator md = new MeshDecimator();

        md.setMaxCollapseError(1.e-8);
        md.setMaxEdgeLength(0.3 * MM);

        int count = 8;

        int fcount = mesh.getTriangleCount();

        while (count-- > 0) {

            int target = fcount / 2;
            md.processMesh(mesh, target);
            fcount = mesh.getTriangleCount();

            stl = new STLWriter(fmt("/tmp/00_boundary_%d.stl", count));
            mesh.getTriangles(stl);
            stl.close();

        }


    }


    public void testMeshMakerMT() throws Exception {

        for (int i = 0; i < 3; i++) {
            runMeshMakerMT();
        }
    }

    public void runMeshMakerMT() throws Exception {

        double voxelSize = 0.1 * MM;

        int nx = 230, ny = 230, nz = 70;
        int threadCount = 4;
        int blockSize = 30;
        double radius = 1.05;

        printf("gridSize: %d x %d x %d\n", nx, ny, nz);
        printf("threadCount: %d\n", threadCount);
        printf("blockSize: %d\n", blockSize);

        double gridBounds[] = new double[]{0, nx * voxelSize, 0, ny * voxelSize, 0, nz * voxelSize};
        double sphereSize = (gridBounds[1] - gridBounds[0]) / 1.5;

        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        //Grid grid = new GridShortIntervals(nx, ny, nz, voxelSize,voxelSize);
        grid.setGridBounds(gridBounds);

        long t0 = time();
        GridMaker gridMaker = new GridMaker();
        gridMaker.setBounds(gridBounds);
        gridMaker.setDataSource(new ArrayOfSpheres(sphereSize, radius));
        gridMaker.setThreadCount(threadCount);
        gridMaker.makeGrid(grid);
        printf("GRID_RENDERING_TIME: %d ms\n", (time() - t0));

        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(blockSize);
        meshmaker.setThreadCount(threadCount);
        //IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();

        STLWriter stl = new STLWriter("/tmp/00_meshMaker.stl");
        //TriangleCounter tc = new TriangleCounter();
        meshmaker.makeMesh(grid, stl);
        //printf("total triangles: %d\n", tc.getCount());
        stl.close();

    }

    /**
     * checking idea of partioning a mesh into many fragmrents and decimate each
     * fragment seperatelly
     */

    public void _testMeshPartitioning() throws Exception {
        for (int i = 0; i < 20; i++) {
            runMeshPartitioning();
        }
    }

    public void runMeshPartitioning() throws Exception {

        long t00 = time();
        int cellGrid = 30; // grid dimension of one cell 
        int cellsCount = 1; // count of part along each dimension 
        double voxelSize = 0.1 * MM;

        double cellSize = cellGrid * voxelSize;
        double bodySize = cellSize * cellsCount;

        int nx = cellGrid * cellsCount;

        printf("grid: [%d x %d x %d]\n", nx, nx, nx);
        printf("cell: [%d x %d x %d]\n", cellGrid, cellGrid, cellGrid);
        printf("cells: [%d x %d x %d]\n", cellsCount, cellsCount, cellsCount);

        ArrayOfSpheres spheres = new ArrayOfSpheres(cellSize, 0.97);

        long t0 = time();

        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx, nx, nx, voxelSize, voxelSize);

        GridMaker gridMaker = new GridMaker();

        double gridBounds[] = new double[]{0, bodySize, 0, bodySize, 0, bodySize};

        gridMaker.setBounds(gridBounds);
        gridMaker.setDataSource(new ArrayOfSpheres(cellSize, 0.97));

        gridMaker.makeGrid(grid);
        printf("grid made: %d ms\n", (time() - t0));


        t0 = time();
        double ibounds[] = MathUtil.extendBounds(gridBounds, -voxelSize / 2);

        IsosurfaceMaker im = new IsosurfaceMaker();
        im.setIsovalue(0.);
        im.setBounds(ibounds);
        im.setGridSize(nx, nx, nx);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();

        im.makeIsosurface(new IsosurfaceMaker.SliceGrid(grid, gridBounds, 0), its);
        printf("isosurface made: %d ms\n", (nanoTime() - t0));
        t0 = time();
        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
        printf("mesh created: %d ms\n", (time() - t0));

        MeshDecimatorMT md = new MeshDecimatorMT();

        md.setThreadCount(1);

        md.setMaxCollapseError(1.0);
        int fcount = mesh.getTriangleCount();
        int target = fcount / 2;

        printf("mesh faces: %d\n", fcount);

        t0 = time();
        md.processMesh(mesh, target);

        printf("mesh decimation: %d ms\n", (time() - t0));
        printf("decimated mesh count: %d\n", mesh.getTriangleCount());

        //MeshExporter.writeMeshSTL(mesh, "/tmp/spheres.stl");
        printf("TOTAL_TIME: %d ms\n", (time() - t00));

    }


    static class ArrayOfSpheres implements DataSource {

        double r = 0.97;
        double r2 = r * r;
        double period;

        ArrayOfSpheres(double period, double radius) {
            this.period = period;
            this.r = radius;
            r2 = r * r;
        }

        public int getDataValue(Vec pnt, Vec data) {

            double x = pnt.v[0] / period;
            double y = pnt.v[1] / period;
            double z = pnt.v[2] / period;
            x = 2 * (x - Math.floor(x) - 0.5);
            y = 2 * (y - Math.floor(y) - 0.5);
            z = 2 * (z - Math.floor(z) - 0.5);

            // x, y, z are in [-1,1]
            data.v[0] = 10 * (r2 - (x * x + y * y + z * z));

            return RESULT_OK;

        }

    } // class ArrayOfSpheres


    public void _testGaussianKernel() {
        double kernel[] = MathUtil.getGaussianKernel(1.);
        for (int i = 0; i < kernel.length; i++) {
            printf("%7.3f\n", kernel[i]);
        }
    }


    public void _testAtomicArray() throws Exception {

        printf("testAtomicArray()\n");

        int arraySize = 40000000;
        int lockSize = 100;

        AtomicIntegerArray lockedEdges = new AtomicIntegerArray(arraySize);
        AtomicInteger counter = new AtomicInteger(arraySize / 2);
        int threadCount = 4;

        int edgeData[] = new int[arraySize];
        for (int i = 0; i < arraySize; i++) {
            edgeData[i] = 1;
        }

        printf("initial count: %d, lock size: %d, threads: %d \n", arraySize, lockSize, threadCount);

        long t0 = time();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        EdgeReducer threads[] = new EdgeReducer[threadCount];

        for (int i = 0; i < threadCount; i++) {

            threads[i] = new EdgeReducer(edgeData, lockedEdges, counter, lockSize);
            executor.submit(threads[i]);

        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        printf("reduction done: %d ms\n", (time() - t0));
        int cnt = 0;
        for (int i = 0; i < arraySize; i++) {
            if (edgeData[i] == 1)
                cnt++;
        }
        printf("final count: %d\n", cnt);
        for (int i = 0; i < threadCount; i++) {
            printf("badEdgeCount: %d, badLockCount: %d\n", threads[i].badEdgeCount, threads[i].badLockCount);
        }


    }

    static class EdgeReducer implements Runnable {
        AtomicIntegerArray lockedEdges;
        int edges[];
        AtomicInteger counter;
        int lockSize;
        Random rnd;
        int badEdgeCount = 0;
        int badLockCount = 0;

        EdgeReducer(int edges[], AtomicIntegerArray lockedEdges, AtomicInteger counter, int lockSize) {

            this.lockedEdges = lockedEdges;
            this.edges = edges;
            this.counter = counter;
            this.lockSize = lockSize;
            this.rnd = new Random();
            //printf("new EdgeReducer(%s)\n", counter);

        }

        public void run() {

            //printf("%s.run()\n", this);

            while (true) {
                // 
                int dataSize = edges.length;
                int edge = rnd.nextInt(dataSize);

                if (edges[edge] == 0) {
                    badEdgeCount++;
                    continue;
                }

                //printf("edge: %d\n", edge);
                if (lockNeighbors(edge)) {
                    if (counter.decrementAndGet() >= 0) {
                        // do edge collapse 
                        edges[edge] = 0;
                        // unlock back 
                        unlockNeighbors(edge);
                    } else {
                        // unlock back 
                        unlockNeighbors(edge);
                        break;
                    }
                } else {
                    badLockCount++;
                }
            }
        }

        boolean lockNeighbors(int edge) {

            int dataSize = edges.length;
            for (int k = -lockSize; k <= lockSize; k++) {

                int lock = (edge + k + dataSize) % dataSize;

                if (!lockedEdges.compareAndSet(lock, 0, 1)) {
                    // faled to lock - unlock locked edges 
                    for (int u = -lockSize; u < k; u++) {
                        lock = (edge + u + dataSize) % dataSize;
                        lockedEdges.compareAndSet(lock, 0, 0);
                    }
                    return false;
                }
            }

            // we locked all the edges 
            return true;
        }

        boolean unlockNeighbors(int edge) {

            int dataSize = edges.length;
            for (int k = -lockSize; k <= lockSize; k++) {

                int lock = (edge + k + dataSize) % dataSize;

                if (!lockedEdges.compareAndSet(lock, 1, 0)) {
                    // failed to unlock - what do do? 
                    printf("failed to unlock edge %d\n", lock);
                }
            }

            // we unlocked locked all the edges 
            return true;
        }

    }

}

