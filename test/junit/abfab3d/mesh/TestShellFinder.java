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

import abfab3d.core.ResultCodes;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.op.GridMaker;
import abfab3d.io.input.STLReader;
import abfab3d.io.input.X3DReader;
import abfab3d.io.output.IsosurfaceMaker;
import abfab3d.io.output.STLWriter;
import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.core.MathUtil;
import abfab3d.core.Vec;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import static abfab3d.core.Output.*;

// External Imports

/**
 * Tests the functionality of ShellFinder
 *
 * @author Vladimir Bulatov
 */
public class TestShellFinder extends TestCase {

    static final double MM = 0.001; // mm -> m conversion 

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestShellFinder.class);
    }

    public void testOpenBox() throws Exception {

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

                //5, 1, 0,
                //4, 5, 0,

                //5, 6, 2,
                //5, 2, 1,

                //2, 6, 7,
                //2, 7, 3,

                6, 5, 4,
                7, 6, 4,

                //3, 7, 4,
                //3, 4, 0
        };

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(vert, indexes);

        ShellFinder sf = new ShellFinder();

        ShellFinder.ShellInfo si[] = sf.findShells(mesh);

        assertEquals("Detect 2 shells", 2, si.length);
    }

    public void testFile() throws Exception {

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();

        STLReader sr = new STLReader();
        sr.read("test/models/Deer.stl", its);

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        ShellFinder sf = new ShellFinder();

        ShellFinder.ShellInfo si[] = sf.findShells(mesh);

        printf("shells count: %d\n", si.length);

        assertEquals("Detect 4 shells", 4,si.length);

        for (int i = 0; i < si.length; i++) {

            AreaCalculator ac = new AreaCalculator();
            sf.getShell(mesh, si[i].startFace, ac);
            printf("face: %7d count: %7d  vol: %f cm^3\n", si[i].startFace, si[i].faceCount, ac.getVolume() * 1e6);
        }

    }

    public void testLargeTriCount() throws Exception {

        long t0 = time();
        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();

//        STLReader sr = new STLReader();
//        sr.read("test/models/sphere_10cm_400K_tri.stl", its);

        X3DReader sr = new X3DReader("test/models/gyroid_2M_10mm_am.x3db");
        sr.getTriangles(its);

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        ShellFinder sf = new ShellFinder();

        ShellFinder.ShellInfo si[] = sf.findShells(mesh);

        printf("shells count: %d\n", si.length);

        assertEquals("Detect 1 shells", 1,si.length);

        for (int i = 0; i < si.length; i++) {

            AreaCalculator ac = new AreaCalculator();
            sf.getShell(mesh, si[i].startFace, ac);
            printf("face: %7d count: %7d  vol: %f cm^3\n", si[i].startFace, si[i].faceCount, ac.getVolume() * 1e6);
        }

        printf("Time: %d\n",(time()-t0)/1000);
    }

    public void testSorting() throws Exception {

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();

        STLReader sr = new STLReader();
        sr.read("test/models/Deer.stl", its);

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        ShellFinder sf = new ShellFinder();

        ShellFinder.ShellInfo si[] = sf.findShellsSorted(mesh, true);

        printf("shells count: %d\n", si.length);

        assertEquals("Detect 4 shells", 4,si.length);

        for (int i = 0; i < si.length; i++) {
            printf("face: %7d count: %7d  vol: %f cm^3\n", si[i].startFace, si[i].faceCount, si[i].volume * 1e6);
        }

        assertTrue("Ordered1", si[0].volume <= si[1].volume);
        assertTrue("Ordered2", si[1].volume <= si[2].volume);
        assertTrue("Ordered2", si[2].volume <= si[3].volume);

        for (int i = 0; i < si.length; i++) {
            printf("face: %7d count: %7d  vol: %f cm^3\n", si[i].startFace, si[i].faceCount, si[i].volume * 1e6);
        }

    }

    public void testSortingReversed() throws Exception {

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();

        STLReader sr = new STLReader();
        sr.read("test/models/Deer.stl", its);

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        ShellFinder sf = new ShellFinder();

        ShellFinder.ShellInfo si[] = sf.findShellsSorted(mesh, false);

        printf("shells count: %d\n", si.length);

        assertEquals("Detect 4 shells", 4,si.length);

        for (int i = 0; i < si.length; i++) {
            printf("face: %7d count: %7d  vol: %f cm^3\n", si[i].startFace, si[i].faceCount, si[i].volume * 1e6);
        }

        assertTrue("Ordered1", si[0].volume >= si[1].volume);
        assertTrue("Ordered2", si[1].volume >= si[2].volume);
        assertTrue("Ordered2", si[2].volume >= si[3].volume);

        for (int i = 0; i < si.length; i++) {
            printf("face: %7d count: %7d  vol: %f cm^3\n", si[i].startFace, si[i].faceCount, si[i].volume * 1e6);
        }

    }

    public void _testSpheres() throws Exception {
        for (int i = 0; i < 5; i++)
            runSpheres();
    }

    public void runSpheres() throws Exception {

        long t00 = time();
        int nx = 100; // grid dimension 
        double voxelSize = 0.1 * MM;
        double radius = 0.97;

        double bodySize = nx * voxelSize;
        double gridBounds[] = new double[]{0, bodySize, 0, bodySize, 0, bodySize};

        double cellSize = bodySize / 2;

        printf("grid: [%d x %d x %d]\n", nx, nx, nx);

        ArrayOfSpheres spheres = new ArrayOfSpheres(cellSize, radius);

        long t0 = time();

        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx, nx, nx, voxelSize, voxelSize);

        GridMaker gridMaker = new GridMaker();

        gridMaker.setBounds(gridBounds);
        gridMaker.setSource(new ArrayOfSpheres(cellSize, radius));

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
        printf("isosurface made: %d ms\n", (time() - t0));
        t0 = time();

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        printf("mesh created: %d ms\n", (time() - t0));

        //MeshExporter.writeMeshSTL(mesh, "/tmp/spheres.stl");

        ShellFinder sf = new ShellFinder();

        t0 = time();

        ShellFinder.ShellInfo si[] = sf.findShells(mesh);
        printf("ShellFinder time: %d ms\n", (time() - t0));

        printf("shells count: %d\n", si.length);

        for (int i = 0; i < si.length; i++) {

            printf("face: %7d count: %7d\n", si[i].startFace, si[i].faceCount);
            STLWriter stl = new STLWriter(fmt("/tmp/00_shell_%05d.stl", i));
            sf.getShell(mesh, si[i].startFace, stl);
            stl.close();
        }

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

            return ResultCodes.RESULT_OK;

        }
        public int getChannelsCount(){
            return 1;
        }

        /**
         * Get the bounds of this data source.  The data source can be infinite.
         * @return
         */
        public Bounds getBounds() {
            return null;
        }

        /**
         * Set the bounds of this data source.  For infinite bounds use Bounds.INFINITE
         * @param bounds
         */
        public void setBounds(Bounds bounds) {
        }

    } // class ArrayOfSpheres   
}

