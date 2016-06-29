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

// External Imports

import abfab3d.io.input.IndexedTriangleSetLoader;
import abfab3d.io.input.STLReader;
import abfab3d.io.output.MeshExporter;
import abfab3d.util.StructMixedData;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.j3d.geom.GeometryData;

import javax.vecmath.Vector3d;
import java.io.File;
import java.io.IOException;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;
import static java.lang.System.currentTimeMillis;

/**
 * Tests the functionality of the LaplasianSmooth
 *
 * @author Alan Hudson
 */
public class TestLaplasianSmooth extends TestCase {

    static final double MM = 1000; // m -> mm conversion 
    static final double MM3 = 1.e9; // m^3 -> mm^3 conversion 

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestLaplasianSmooth.class);
    }

    /*
    public void testCubeOrig() {
        String fpath = "test/models/cube_1mm.x3dv";
        abfab3d.mesh.WingedEdgeTriangleMesh mesh = loadX3D(fpath);

        AreaCalculator ac = new AreaCalculator();
        mesh.getTriangles(ac);
        double volume = ac.getVolume();
        double surfaceArea = ac.getArea();

        double centerWeight = 1.0; // any non negative value is OK

//        abfab3d.mesh2.LaplasianSmooth ls = new abfab3d.mesh2.LaplasianSmooth();
        abfab3d.mesh.LaplasianSmooth ls = new abfab3d.mesh.LaplasianSmooth();

        ls.setCenterWeight(centerWeight);
        ls.processMesh(mesh, 1);

        ac = new AreaCalculator();
        mesh.getTriangles(ac);
        double volume2 = ac.getVolume();
        double surfaceArea2 = ac.getArea();


        System.out.println("Orig Volume: " + volume + " area: " + surfaceArea);
        System.out.println("New Volume : " + volume2 + " area: " + surfaceArea2);

        assertTrue("Volume Decreased", volume2 < volume);

        abfab3d.mesh.Vertex verts = mesh.getVertices();

        while(verts != null) {
            System.out.println(verts.getPoint());

            verts = verts.getNext();
        }

        try {
            MeshExporter.writeMeshSTL(mesh, fmt("/tmp/cube.stl", mesh.getFaceCount()));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("IO Exception");
        }
    }
   */
    public void testCubeNew() {
        String fpath = "test/models/cube_1mm.x3dv";
        abfab3d.mesh.WingedEdgeTriangleMesh mesh = loadX3D(fpath);

        assertTrue("Initial Manifold", TestWingedEdgeTriangleMesh.isManifold(mesh));

        AreaCalculator ac = new AreaCalculator();
        mesh.getTriangles(ac);
        double volume = ac.getVolume();
        double surfaceArea = ac.getArea();

        double centerWeight = 1.0; // any non negative value is OK

        abfab3d.mesh.LaplasianSmooth ls = new abfab3d.mesh.LaplasianSmooth();

        ls.setCenterWeight(centerWeight);
        ls.processMesh(mesh, 1);

        assertTrue("After Manifold", TestWingedEdgeTriangleMesh.isManifold(mesh));

        ac = new AreaCalculator();
        mesh.getTriangles(ac);
        double volume2 = ac.getVolume();
        double surfaceArea2 = ac.getArea();


        System.out.println("Orig Volume: " + volume + " area: " + surfaceArea);
        System.out.println("New Volume : " + volume2 + " area: " + surfaceArea2);

        assertTrue("Volume Decreased", volume2 < volume);

        StructMixedData verts = mesh.getVertices();
        int v = mesh.getStartVertex();
        double[] pnt = new double[3];

        PointSet ps = new PointSet(1e-8);

        while (v != -1) {
            Vertex.getPoint(verts, v, pnt);
            ps.add(pnt[0], pnt[1], pnt[2]);

            v = Vertex.getNext(verts, v);
            System.out.println(java.util.Arrays.toString(pnt));
        }

        assertEquals("Number of Unique points", 8, ps.getPoints().length / 3);

        try {
            MeshExporter.writeMeshSTL(mesh, fmt("/tmp/cube.stl", mesh.getFaceCount()));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("IO Exception");
        }
    }

    public void processFile(String fpath, double maxDecimationError, double reduceFactor) throws Exception {

        long t0 = currentTimeMillis();
        WingedEdgeTriangleMesh mesh = loadMesh(fpath);
        printf("mesh loading: %d ms\n", (currentTimeMillis() - t0));
        t0 = currentTimeMillis();

        int orig_fcount = mesh.getFaceCount();
        int fcount = orig_fcount;
        System.out.println("Initial Face Count: " + fcount);
        MeshExporter.writeMeshSTL(mesh, fmt("/tmp/mesh_orig_%07d.stl", fcount));

        printf("mesh faces: %d, vertices: %d, edges: %d\n", fcount, mesh.getVertexCount(), mesh.getEdgeCount());
        printf("initial counts: faces: %d, vertices: %d, edges: %d \n", mesh.getFaceCount(), mesh.getVertexCount(), mesh.getEdgeCount());

        assertTrue("Initial Manifold", TestWingedEdgeTriangleMesh.isManifold(mesh));


        MeshDecimator md = new MeshDecimator();
        md.setMaxCollapseError(maxDecimationError);

        int target;
        int current;

        while (true) {

            target = mesh.getTriangleCount() / 2;
            System.out.println("Target face count : " + target);
            t0 = currentTimeMillis();
            printf("processMesh() start\n");
            md.processMesh(mesh, target);
            //md.DEBUG = true;
            printf("processMesh() done %d ms\n", (currentTimeMillis() - t0));
            fcount = mesh.getFaceCount();

            // these things hang on large file - TODO - check this
            //assertTrue("verifyVertices", verifyVertices(mesh));
            //assertTrue("Structural Check", TestWingedEdgeTriangleMesh2.verifyStructure(mesh, true));
            //assertTrue("Final Manifold", TestWingedEdgeTriangleMesh2.isManifold(mesh));
            //printf("processMesh() done %d ms\n",(currentTimeMillis()-t0));

            current = mesh.getFaceCount();
            System.out.println("Current face count: " + current);
            if (current >= target * 1.25) {
                System.out.println("Leaving loop");
                // not worth continuing
                break;
            }

        }
        MeshExporter.writeMeshSTL(mesh, fmt("/tmp/mesh_dec_%07d.stl", fcount));


        assertTrue("Not Reduced enough", mesh.getFaceCount() < reduceFactor * orig_fcount);
    }


    /**

     */
    public static WingedEdgeTriangleMesh loadMesh(String fpath) {
        if (fpath.toLowerCase().lastIndexOf(".stl") > 0) {
            return loadSTL(fpath);
        } else {
            return loadX3D(fpath);
        }
    }

    /**
     * load STL file
     */
    public static WingedEdgeTriangleMesh loadSTL(String fpath) {

        STLReader reader = new STLReader();
        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();
        try {
            reader.read(fpath, its);
            return new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * load X3D file
     */
    public static abfab3d.mesh.WingedEdgeTriangleMesh loadX3D(String fpath) {

        IndexedTriangleSetLoader loader = new IndexedTriangleSetLoader(false);

        loader.processFile(new File(fpath));

        GeometryData data = new GeometryData();
        data.coordinates = loader.getCoords();
        data.vertexCount = data.coordinates.length / 3;
        data.indexes = loader.getVerts();
        data.indexesCount = data.indexes.length;

        Vector3d[] verts = new Vector3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Vector3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        idx = 0;
        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();
        for (int i = 0; i < len; i++) {
            its.addTri(verts[data.indexes[idx++]], verts[data.indexes[idx++]], verts[data.indexes[idx++]]);
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
        return we;

    }


    /**
     * inits all vertices user data to Integer
     */
    static void setVerticesUserData(WingedEdgeTriangleMesh mesh) {

        int vcount = 0;
        StructMixedData vertices = mesh.getVertices();
        int v = mesh.getStartVertex();
        while (v != -1) {
            Vertex.setUserData(vcount, vertices, v);
            vcount++;
            v = Vertex.getNext(vertices, v);
        }
    }

    /**
     * check, that all the vertices have consistent ring of faces
     */
    static boolean verifyVertices(WingedEdgeTriangleMesh mesh) {

        //printf("verifyVertices()\n");
        StructMixedData vertices = mesh.getVertices();
        StructMixedData hedges = mesh.getHalfEdges();
        int v = mesh.getStartVertex();
        int vcount = 0;
        while (v != -1) {
            //printf("v:%s: ", v);
            vcount++;
            int start = Vertex.getLink(vertices, v);
            int he = start;
            int tricount = 0;

            do {
                //printf("[%3s %3s %3s] ", he.getEnd().getUserData(), he.getNext().getEnd().getUserData(),  he.getNext().getNext().getEnd().getUserData()); 

                if (tricount++ > 100) {

                    printf("verifyVertices() !!! tricount exceeded\n");

                    return false;
                }

                int twin = HalfEdge.getTwin(hedges, he);
                he = HalfEdge.getNext(hedges, twin);

            } while (he != start);
            //printf("\n");

            v = Vertex.getNext(vertices, v);
        }
        printf("vcount: %3d\n:", vcount);

        return true;

    }

}

