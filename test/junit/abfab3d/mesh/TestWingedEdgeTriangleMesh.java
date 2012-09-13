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

package abfab3d.mesh;

// External Imports

import abfab3d.io.input.IndexedTriangleSetLoader;
import abfab3d.io.output.SAVExporter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.j3d.geom.GeometryData;
import org.web3d.util.ErrorReporter;
import org.web3d.util.LongHashMap;
import org.web3d.vrml.export.*;
import org.web3d.vrml.sav.BinaryContentHandler;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

// Internal Imports

/**
 * Tests the functionality of WingedEdgeMesh
 *
 * @author Alan Hudson
 */
public class TestWingedEdgeTriangleMesh extends TestCase {
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestWingedEdgeTriangleMesh.class);
    }

    /**
     * Test that we can create a simple object without crashing.
     *
     * @throws Exception
     */
    public void testBasic() throws Exception {

        Point3d[] pyr_vert = new Point3d[]{new Point3d(-1, -1, -1), new Point3d(1, -1, -1),
                new Point3d(1, 1, -1), new Point3d(-1, 1, -1),
                new Point3d(0, 0, 1)};
        int pyr_faces[][] = new int[][]{{3, 2, 0}, {2, 1, 0}, {0, 1, 4}, {1, 2, 4}, {2, 3, 4}, {3, 0, 4}};

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(pyr_vert, pyr_faces);

        we.writeOBJ(System.out);

        Vertex[][] findex = we.getFaceIndexes();
        Vertex v = we.getVertices();

        while (v != null) {
            System.out.println(v);
            v = v.getNext();
        }

        for (int i = 0; i < findex.length; i++) {

            Vertex face[] = findex[i];

            System.out.print("[");
            for (int j = 0; j < face.length; j++) {
                System.out.print(" " + face[j]);
            }
            System.out.println(" ]");

        }

        writeMesh(we, "c:/tmp/pyramid.x3dv");
    }

    public void testCollapse() throws Exception {
        Point3d[] verts = new Point3d[]{
                new Point3d(-0.5, 0, -0.5),
                new Point3d(0.5, 0, -0.5),
                new Point3d(-1, 0, 0),
                new Point3d(0, 0, -0.25),
                new Point3d(1, 0, 0),
                new Point3d(0, 0, 0.5),
                new Point3d(-0.5, 0, 1),
                new Point3d(0.5, 0, 1)
        };
        int faces[][] = new int[][]{{0, 2, 3}, {0, 3, 1}, {1, 3, 4}, {3, 2, 5}, {4, 3, 5}, {2, 6, 5}, {5, 6, 7}, {4, 5, 7}};

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        we.writeOBJ(System.out);

        writeMesh(we, "c:/tmp/collapse1.x3dv");

        int expected_verts = 8;
        int expected_faces = 8;
        int expected_edges = 15;
        assertEquals("Initial Vertex Count", expected_verts, we.getVertexCount());
        assertEquals("Initial Face Count", expected_faces, we.getFaceCount());
        assertEquals("Initial Edge Count", expected_edges, we.getEdgeCount());


        // Find edge from vertex 3 to 5
        Vertex v1 = we.findVertex(verts[3]);
        Vertex v2 = we.findVertex(verts[5]);

        Edge edges = we.getEdges();
        HalfEdge he = null;

        boolean found = false;

        while (edges != null) {
            he = edges.getHe();

            if ((he.getStart() == v1 && he.getEnd() == v2) ||
                    (he.getStart() == v2 && he.getEnd() == v1)) {
                found = true;
                break;
            }

            edges = edges.getNext();
        }

        if (!found) {
            fail("Edge not found");
        }

        System.out.println("edge: " + edges);

        // use center point of vertices as new pos
        Point3d pos = new Point3d();
        pos.x = (verts[3].x + verts[5].x) / 2.0;
        pos.y = (verts[3].y + verts[5].y) / 2.0;
        pos.z = (verts[3].z + verts[5].z) / 2.0;

        HashSet<Edge> removedEdges = new HashSet<Edge>();
        we.collapseEdge(edges, pos, removedEdges);

        writeMesh(we, "c:/tmp/collapse2.x3dv");

        // verify number of vertices remaining
        assertEquals("Vertex Count", expected_verts - 1, we.getVertexCount());
        assertEquals("Face Count", expected_faces - 2, we.getFaceCount());
        assertEquals("Edge Count", expected_edges - 1, we.getEdgeCount());
    }

    public void testDegenerateFace() throws Exception {
        Point3d[] verts = new Point3d[]{
                new Point3d(-1, 0, -1),
                new Point3d(0, 0, -1),
                new Point3d(1, 0, -1),
                new Point3d(0, 0, -0.5),
                new Point3d(-1, 0, 0),
                new Point3d(1, 0, 0),
                new Point3d(0, 0, 0.5),
                new Point3d(-1, 0, 1),
                new Point3d(0, 0, 1),
                new Point3d(1, 0, 1),
                new Point3d(0.5, 0, 0)
        };
        int faces[][] = new int[][]{{1, 0, 3}, {2, 1, 3}, {0, 4, 3}, {2, 3, 5}, {3, 4, 6}, {3, 6, 10}, {3, 10, 5}, {10, 6, 5}, {4, 7, 6}, {6, 7, 8}, {6, 8, 9}, {5, 6, 9}};

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        we.writeOBJ(System.out);

        writeMesh(we, "c:/tmp/degenface1.x3dv");

        int expected_verts = 11;
        int expected_faces = 12;
        int expected_edges = 22;
        assertEquals("Initial Vertex Count", expected_verts, we.getVertexCount());
        assertEquals("Initial Face Count", expected_faces, we.getFaceCount());
        assertEquals("Initial Edge Count", expected_edges, we.getEdgeCount());


        // Find edge from vertex 3 to 5
        Vertex v1 = we.findVertex(verts[3]);
        Vertex v2 = we.findVertex(verts[6]);

        Edge edges = we.getEdges();
        HalfEdge he = null;

        boolean found = false;

        while (edges != null) {
            he = edges.getHe();

            if ((he.getStart() == v1 && he.getEnd() == v2) ||
                    (he.getStart() == v2 && he.getEnd() == v1)) {
                found = true;
                break;
            }

            edges = edges.getNext();
        }

        if (!found) {
            fail("Edge not found");
        }

        System.out.println("edge: " + edges);

        // use center point of vertices as new pos
        Point3d pos = new Point3d();
        pos.x = (verts[3].x + verts[6].x) / 2.0;
        pos.y = (verts[3].y + verts[6].y) / 2.0;
        pos.z = (verts[3].z + verts[6].z) / 2.0;

        HashSet<Edge> removedEdges = new HashSet<Edge>();
        we.collapseEdge(edges, pos, removedEdges);

        writeMesh(we, "c:/tmp/degenface2.x3dv");
        we.writeOBJ(System.out);

        // verify number of vertices remaining
        assertEquals("Vertex Count", expected_verts - 1, we.getVertexCount());
        assertEquals("Face Count", expected_faces - 4, we.getFaceCount());
        assertEquals("Edge Count", expected_edges - 4, we.getEdgeCount());
    }

    /**
     * Test a box is manifold on construction and edge collapse
     */
    public void testBox() throws Exception {
        GeometryData data = new GeometryData();

        data.vertexCount = 8;
        data.coordinates = new float[]{
                1, -1, 1,
                1, 1, 1,
                -1, 1, 1,
                -1, -1, 1,
                1, -1, -1,
                1, 1, -1,
                -1, 1, -1,
                -1, -1, -1
        };

        data.indexesCount = 36;
        data.indexes = new int[]{
                1, 2, 3,
                0, 1, 3,

                5, 1, 0,
                4, 5, 0,

                5, 6, 2,
                5, 2, 1,

                2, 6, 7,
                2, 7, 3,

                6, 5, 4,
                7, 6, 4,

                3, 7, 4,
                3, 4, 0
        };

        Point3d[] verts = new Point3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Point3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        int faces[][] = new int[len][3];
        idx = 0;

        for (int i = 0; i < len; i++) {
            faces[i][0] = data.indexes[idx++];
            faces[i][1] = data.indexes[idx++];
            faces[i][2] = data.indexes[idx++];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        writeMesh(we, "c:/tmp/box.x3dv");

        we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifold(we));
        assertTrue("Initial Triangle Check", verifyTriangles(we));

        // Find edge from vertex 1 to 2
        Vertex v1 = we.findVertex(verts[1]);
        Vertex v2 = we.findVertex(verts[2]);

        Edge edges = we.getEdges();
        HalfEdge he = null;

        boolean found = false;

        while (edges != null) {
            he = edges.getHe();

            if ((he.getStart() == v1 && he.getEnd() == v2) ||
                    (he.getStart() == v2 && he.getEnd() == v1)) {
                found = true;
                break;
            }

            edges = edges.getNext();
        }

        if (!found) {
            fail("Edge not found");
        }

        // use center point of vertices as new pos
        Point3d pos = new Point3d();
        pos.x = (verts[1].x + verts[2].x) / 2.0;
        pos.y = (verts[1].y + verts[2].y) / 2.0;
        pos.z = (verts[1].z + verts[2].z) / 2.0;

        HashSet<Edge> removedEdges = new HashSet<Edge>();
        we.collapseEdge(edges, pos, removedEdges);

        writeMesh(we, "c:/tmp/box2.x3dv");

        we.writeOBJ(System.out);

        assertTrue("Manifold2", isManifold(we));

        assertTrue("Triangle Check2", verifyTriangles(we));

    }

    /**
     * Test a box is manifold on construction and edge collapse where its mixed oriented to cause manifold failure
     */
    public void testBoxMixedOrientation() throws Exception {
        GeometryData data = new GeometryData();

        data.vertexCount = 8;
        data.coordinates = new float[]{
                1, -1, 1,
                1, 1, 1,
                -1, 1, 1,
                -1, -1, 1,
                1, -1, -1,
                1, 1, -1,
                -1, 1, -1,
                -1, -1, -1
        };

        data.indexesCount = 36;
        data.indexes = new int[]{
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

                3, 7, 4,
                3, 4, 0
        };

        Point3d[] verts = new Point3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Point3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        int faces[][] = new int[len][3];
        idx = 0;

        for (int i = 0; i < len; i++) {
            faces[i][0] = data.indexes[idx++];
            faces[i][1] = data.indexes[idx++];
            faces[i][2] = data.indexes[idx++];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        writeMesh(we, "c:/tmp/box.x3dv");

        we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifold(we));
        assertTrue("Initial Triangle Check", verifyTriangles(we));

        // Find edge from vertex 1 to 2
        Vertex v1 = we.findVertex(verts[0]);
        Vertex v2 = we.findVertex(verts[2]);

        Edge edges = we.getEdges();
        HalfEdge he = null;

        boolean found = false;

        while (edges != null) {
            he = edges.getHe();

            if ((he.getStart() == v1 && he.getEnd() == v2) ||
                    (he.getStart() == v2 && he.getEnd() == v1)) {
                found = true;
                break;
            }

            edges = edges.getNext();
        }

        if (!found) {
            fail("Edge not found");
        }

        // use center point of vertices as new pos
        Point3d pos = new Point3d();
        pos.x = (verts[1].x + verts[2].x) / 2.0;
        pos.y = (verts[1].y + verts[2].y) / 2.0;
        pos.z = (verts[1].z + verts[2].z) / 2.0;

        HashSet<Edge> removedEdges = new HashSet<Edge>();
        we.collapseEdge(edges, pos, removedEdges);

        we.writeOBJ(System.out);
        writeMesh(we, "c:/tmp/box2.x3dv");

        assertTrue("Manifold2", isManifold(we));

        assertTrue("Triangle Check2", verifyTriangles(we));

    }

    /**
     * Test a box is manifold on construction and edge collapse
     */
    public void testManifoldSpeedKnot() throws Exception {
        IndexedTriangleSetLoader loader = new IndexedTriangleSetLoader(false);
        loader.processFile(new File("test/junit/models/speed-knot.x3db"));

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.coordinates = loader.getCoords();
        data.vertexCount = data.coordinates.length / 3;
        data.indexes = loader.getVerts();
        data.indexesCount = data.indexes.length;

        Point3d[] verts = new Point3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Point3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        int faces[][] = new int[len][3];
        idx = 0;

        for (int i = 0; i < len; i++) {
            faces[i][0] = data.indexes[idx++];
            faces[i][1] = data.indexes[idx++];
            faces[i][2] = data.indexes[idx++];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        writeMesh(we, "c:/tmp/speed-knot1.x3dv");

        //we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifold(we));
        assertTrue("Initial Triangle Check", verifyTriangles(we));

        Random rand = new Random(42);
        int collapses = 100;

        for (int i = 0; i < collapses; i++) {
            idx = rand.nextInt(faces.length / 3);

            Edge e = we.getEdges();

            for (int j = 0; j < idx; j++) {
                e = e.getNext();
            }


            System.out.println("Collapse: " + idx + " e: " + e);
            Point3d pos = new Point3d();
            Point3d p1 = e.getHe().getStart().getPoint();
            Point3d p2 = e.getHe().getEnd().getPoint();
            pos.x = (p1.x + p2.x) / 2.0;
            pos.y = (p1.y + p2.y) / 2.0;
            pos.z = (p1.z + p2.z) / 2.0;

            HashSet<Edge> removedEdges = new HashSet<Edge>();
            we.collapseEdge(e, pos, removedEdges);

            writeMesh(we, "c:/tmp/speed-knot_loop" + i + ".x3dv");

            assertTrue("Manifold", isManifold(we));
            assertTrue("Triangle Check", verifyTriangles(we));
        }

        assertTrue("Manifold2", isManifold(we));
        assertTrue("Triangle Check2", verifyTriangles(we));

        writeMesh(we, "c:/tmp/speed-knot2.x3dv");
    }

    /**
     * Test a box is manifold on construction and edge collapse
     */
    public void testManifoldE() throws Exception {
        IndexedTriangleSetLoader loader = new IndexedTriangleSetLoader(false);
        loader.processFile(new File("test/junit/models/wTest1_ITS.x3d"));

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.coordinates = loader.getCoords();
        data.vertexCount = data.coordinates.length / 3;
        data.indexes = loader.getVerts();
        data.indexesCount = data.indexes.length;

        Point3d[] verts = new Point3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Point3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        int faces[][] = new int[len][3];
        idx = 0;

        for (int i = 0; i < len; i++) {
            faces[i][0] = data.indexes[idx++];
            faces[i][1] = data.indexes[idx++];
            faces[i][2] = data.indexes[idx++];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        writeMesh(we, "c:/tmp/etest1.x3dv");

        we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifold(we));
        assertTrue("Initial Triangle Check", verifyTriangles(we));

        Random rand = new Random(42);
        int collapses = 10;

        for (int i = 0; i < collapses; i++) {
            idx = rand.nextInt(faces.length / 3);

            Edge e = we.getEdges();

            for (int j = 0; j < idx; j++) {
                e = e.getNext();
            }


            System.out.println("Collapse: " + idx + " e: " + e);
            Point3d pos = new Point3d();
            Point3d p1 = e.getHe().getStart().getPoint();
            Point3d p2 = e.getHe().getEnd().getPoint();
            pos.x = (p1.x + p2.x) / 2.0;
            pos.y = (p1.y + p2.y) / 2.0;
            pos.z = (p1.z + p2.z) / 2.0;

            HashSet<Edge> removedEdges = new HashSet<Edge>();
            we.collapseEdge(e, pos, removedEdges);

            writeMesh(we, "c:/tmp/etest_loop" + i + ".x3dv");
            we.writeOBJ(System.out);

            assertTrue("Manifold", isManifold(we));
            assertTrue("Triangle Check", verifyTriangles(we));
        }

        assertTrue("Manifold2", isManifold(we));
        assertTrue("Triangle Check2", verifyTriangles(we));

        writeMesh(we, "c:/tmp/etest2.x3dv");
    }

    /**
     * Test a box is manifold on construction and edge collapse
     */
    public void testManifoldError() throws Exception {
        IndexedTriangleSetLoader loader = new IndexedTriangleSetLoader(false);
        loader.processFile(new File("c:/tmp/debug.x3dv"));

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.coordinates = loader.getCoords();
        data.vertexCount = data.coordinates.length / 3;
        data.indexes = loader.getVerts();
        data.indexesCount = data.indexes.length;

        Point3d[] verts = new Point3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Point3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        int faces[][] = new int[len][3];
        idx = 0;

        for (int i = 0; i < len; i++) {
            faces[i][0] = data.indexes[idx++];
            faces[i][1] = data.indexes[idx++];
            faces[i][2] = data.indexes[idx++];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        writeMesh(we, "c:/tmp/me1.x3dv");

        we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifoldOver(we));
        assertTrue("Initial Triangle Check", verifyTriangles(we));

        idx = 1;

        Edge e = we.getEdges();

        for (int j = 0; j < idx; j++) {
            e = e.getNext();
        }


        System.out.println("Collapse: " + idx + " e: " + e);
        Point3d pos = new Point3d();
        Point3d p1 = e.getHe().getStart().getPoint();
        Point3d p2 = e.getHe().getEnd().getPoint();
        pos.x = (p1.x + p2.x) / 2.0;
        pos.y = (p1.y + p2.y) / 2.0;
        pos.z = (p1.z + p2.z) / 2.0;

        HashSet<Edge> removedEdges = new HashSet<Edge>();
        we.collapseEdge(e, pos, removedEdges);

        we.writeOBJ(System.out);
        writeMesh(we, "c:/tmp/me2.x3dv");

        assertTrue("Manifold2", isManifoldOver(we));
        assertTrue("Triangle Check2", verifyTriangles(we));
    }

    private void writeMesh(WingedEdgeTriangleMesh we, String filename) throws IOException {
        SAVExporter se = new SAVExporter();
        HashMap<String, Object> params = new HashMap<String, Object>();

        FileOutputStream fos = null;

        try {
            BinaryContentHandler writer = null;
            fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".") + 1);

            ErrorReporter console = new PlainTextErrorReporter();

            if (encoding.equals("x3db")) {
                writer = new X3DBinaryRetainedDirectExporter(fos,
                        3, 0, console,
                        X3DBinarySerializer.METHOD_FASTEST_PARSING,
                        0.001f, true);
            } else if (encoding.equals("x3dv")) {
                writer = new X3DClassicRetainedExporter(fos, 3, 0, console);
            } else if (encoding.equals("x3d")) {
                writer = new X3DXMLRetainedExporter(fos, 3, 0, console);
            } else {
                throw new IllegalArgumentException("Unhandled X3D encoding: " + encoding);
            }

            writer.startDocument("", "", "utf8", "#X3D", "V3.0", "");
            writer.profileDecl("Immersive");
            writer.startNode("NavigationInfo", null);
            writer.startField("avatarSize");
            writer.fieldValue(new float[]{0.01f, 1.6f, 0.75f}, 3);
            writer.endNode(); // NavigationInfo

            se.outputX3D(we, params, writer);
            writer.endDocument();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * Test whether a mesh is manifold
     *
     * @param mesh
     * @return
     */
    private boolean isManifold(WingedEdgeTriangleMesh mesh) {
        // Check via twins structure
        boolean manifold = true;
        Edge edges = mesh.getEdges();

        while (edges != null) {
            HalfEdge he = edges.getHe();
            HalfEdge twin = he.getTwin();

            if (twin == null) {
                manifold = false;

                System.out.println("NonManifold edge: " + edges + " he: " + he);
                return false;
            }

            edges = edges.getNext();
        }


        // check via counts

        Vertex[][] faces = mesh.getFaceIndexes();

        int len = faces.length;

        LongHashMap edgeCount = new LongHashMap();

        for (int i = 0; i < len; i++) {
            //System.out.println("Count face: " + faces[i][0].getID() + " " + faces[i][1].getID() + " " + faces[i][2].getID());
            processEdge(faces[i][0].getID(), faces[i][1].getID(), edgeCount);
            processEdge(faces[i][1].getID(), faces[i][2].getID(), edgeCount);
            processEdge(faces[i][2].getID(), faces[i][0].getID(), edgeCount);
        }

        long[] keys = edgeCount.keySet();

        for (int i = 0; i < keys.length; i++) {

            Integer count = (Integer) edgeCount.get(keys[i]);

            if (count != 2) {
                manifold = false;
                int index1 = (int) (keys[i] >> 32);
                int index2 = (int) (keys[i]);

                System.out.println("Invalid edge: " + index1 + "->" + index2 + " cnt: " + count);

            }
        }

        return manifold;
    }

    /**
     * Test whether a mesh is manifold
     *
     * @param mesh
     * @return
     */
    private boolean isManifoldOver(WingedEdgeTriangleMesh mesh) {
        boolean manifold = true;
        // check via counts

        Vertex[][] faces = mesh.getFaceIndexes();

        int len = faces.length;

        LongHashMap edgeCount = new LongHashMap();

        for (int i = 0; i < len; i++) {
            //System.out.println("Count face: " + faces[i][0].getID() + " " + faces[i][1].getID() + " " + faces[i][2].getID());
            processEdge(faces[i][0].getID(), faces[i][1].getID(), edgeCount);
            processEdge(faces[i][1].getID(), faces[i][2].getID(), edgeCount);
            processEdge(faces[i][2].getID(), faces[i][0].getID(), edgeCount);
        }

        long[] keys = edgeCount.keySet();

        for (int i = 0; i < keys.length; i++) {

            Integer count = (Integer) edgeCount.get(keys[i]);

            if (count > 2) {
                manifold = false;
                int index1 = (int) (keys[i] >> 32);
                int index2 = (int) (keys[i]);

                System.out.println("Invalid edge: " + index1 + "->" + index2 + " cnt: " + count);

            }
        }

        return manifold;
    }

    private void processEdge(int index1, int index2, LongHashMap edgeCount) {

//System.out.println("Edges being processed: " + index1 + "," + index2);

        long edge;
        int count = 1;

        // place the smallest index first for
        // consistent lookup
        if (index1 > index2) {
            int temp = index1;
            index1 = index2;
            index2 = temp;
        }

        // put the larger of the 2 points into the long
        edge = index2;

        // shift the point to the left to make room for
        // the smaller point
        edge <<= 32;

        // bit OR the smaller point into the long
        edge |= index1;

        // add the edge to the count
        if (edgeCount.containsKey(edge)) {
            Integer val = (Integer) edgeCount.get(edge);
            count = val.intValue();
            count++;
        }

        edgeCount.put(edge, new Integer(count));
    }

    /**
     * Verify all triangles contain 3 distinct points and area > 0
     *
     * @param mesh
     * @return
     */
    private boolean verifyTriangles(WingedEdgeTriangleMesh mesh) {
        Vector3d svec1 = new Vector3d();
        Vector3d svec2 = new Vector3d();

        for (Face faces = mesh.getFaces(); faces != null; faces = faces.getNext()) {
            Vertex p1;
            Vertex p2;
            Vertex p3;

            p1 = faces.getHe().getStart();
            p2 = faces.getHe().getEnd();

            HalfEdge he = faces.getHe().getNext();

            if (he.getStart() != p1 && he.getStart() != p2) {
                p3 = he.getStart();
            } else if (he.getEnd() != p1 && he.getEnd() != p2) {
                p3 = he.getStart();
            } else {
                System.out.println("Cannot find third unique point?");
                he = faces.getHe();
                HalfEdge start = he;
                while (he != null) {
                    System.out.println(he);
                    he = he.getNext();

                    if (he == start) {
                        break;
                    }
                }
                return false;
            }

            double EPS = 1e-10;

            if (p1.getPoint().epsilonEquals(p2.getPoint(), EPS)) {
                System.out.println("Points equal: " + p1 + " p2: " + p2);
                return false;
            }
            if (p1.getPoint().epsilonEquals(p3.getPoint(), EPS)) {
                System.out.println("Points equal: " + p1 + " p2: " + p3);
                return false;
            }
            if (p2.getPoint().epsilonEquals(p3.getPoint(), EPS)) {
                System.out.println("Points equal: " + p2 + " p2: " + p3);
                return false;
            }

            svec1.x = p2.getPoint().x - p1.getPoint().x;
            svec1.y = p2.getPoint().y - p1.getPoint().y;
            svec1.z = p2.getPoint().z - p1.getPoint().z;

            svec2.x = p3.getPoint().x - p1.getPoint().x;
            svec2.y = p3.getPoint().y - p1.getPoint().y;
            svec2.z = p3.getPoint().z - p1.getPoint().z;

            svec1.cross(svec1, svec2);
            double area = svec1.length();

            if (area < EPS) {
                return false;
            }
        }

        return true;
    }

    /**
     * Test vertex iterator
     *
     * @throws Exception
     */
    public void testVertexIteratorBasic() throws Exception {

        Point3d[] pyr_vert = new Point3d[]{new Point3d(-1, -1, -1), new Point3d(1, -1, -1),
                new Point3d(1, 1, -1), new Point3d(-1, 1, -1),
                new Point3d(0, 0, 1)};
        int pyr_faces[][] = new int[][]{{3, 2, 0}, {2, 1, 0}, {0, 1, 4}, {1, 2, 4}, {2, 3, 4}, {3, 0, 4}};

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(pyr_vert, pyr_faces);


        int cnt = 0;

        for (Iterator<Vertex> itr = we.vertexIterator(); itr.hasNext(); ) {
            Vertex v = itr.next();
            cnt++;
        }

        int expected_verts = pyr_vert.length;
        assertEquals("Vertex Count", expected_verts, cnt);
    }

    /**
     * Test face iterator
     *
     * @throws Exception
     */
    public void testFaceIteratorBasic() throws Exception {

        Point3d[] pyr_vert = new Point3d[]{new Point3d(-1, -1, -1), new Point3d(1, -1, -1),
                new Point3d(1, 1, -1), new Point3d(-1, 1, -1),
                new Point3d(0, 0, 1)};
        int pyr_faces[][] = new int[][]{{3, 2, 0}, {2, 1, 0}, {0, 1, 4}, {1, 2, 4}, {2, 3, 4}, {3, 0, 4}};

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(pyr_vert, pyr_faces);


        int cnt = 0;

        for (Iterator<Face> itr = we.faceIterator(); itr.hasNext(); ) {
            Face f = itr.next();
            cnt++;
        }

        int expected_faces = pyr_faces.length;

        assertEquals("Face Count", expected_faces, cnt);
    }

    /**
     * Test edge iterator
     *
     * @throws Exception
     */
    public void testEdgeIteratorBasic() throws Exception {

        Point3d[] pyr_vert = new Point3d[]{new Point3d(-1, -1, -1), new Point3d(1, -1, -1),
                new Point3d(1, 1, -1), new Point3d(-1, 1, -1),
                new Point3d(0, 0, 1)};
        int pyr_faces[][] = new int[][]{{3, 2, 0}, {2, 1, 0}, {0, 1, 4}, {1, 2, 4}, {2, 3, 4}, {3, 0, 4}};

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(pyr_vert, pyr_faces);


        int cnt = 0;

        for (Iterator<Edge> itr = we.edgeIterator(); itr.hasNext(); ) {
            Edge e = itr.next();
            cnt++;
        }

        int expected_edges = 9;

        assertEquals("Edge Count", expected_edges, cnt);
    }

    public void testVertexVertexIterator() throws Exception {
        GeometryData data = new GeometryData();

        data.vertexCount = 8;
        data.coordinates = new float[]{
                1, -1, 1,
                1, 1, 1,
                -1, 1, 1,
                -1, -1, 1,
                1, -1, -1,
                1, 1, -1,
                -1, 1, -1,
                -1, -1, -1
        };

        data.indexesCount = 36;
        data.indexes = new int[]{
                1, 2, 3,
                0, 1, 3,

                5, 1, 0,
                4, 5, 0,

                5, 6, 2,
                5, 2, 1,

                2, 6, 7,
                2, 7, 3,

                6, 5, 4,
                7, 6, 4,

                3, 7, 4,
                3, 4, 0
        };

        Point3d[] verts = new Point3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Point3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        int faces[][] = new int[len][3];
        idx = 0;

        for (int i = 0; i < len; i++) {
            faces[i][0] = data.indexes[idx++];
            faces[i][1] = data.indexes[idx++];
            faces[i][2] = data.indexes[idx++];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        Vertex vert = we.findVertex(verts[2]);
        VertexVertexIterator vii = new VertexVertexIterator(we, vert);

        int cnt = 0;
        while(vii.hasNext()) {
            Vertex v = vii.next();
            System.out.println("Vertex: " + v.getID());
            cnt++;
        }

        HashSet<Integer> expected_verts = new HashSet<Integer>();
        expected_verts.add(3);
        expected_verts.add(7);
        expected_verts.add(5);
        expected_verts.add(6);
        expected_verts.add(1);

        assertEquals("Vertex Count", expected_verts.size(), cnt);


    }
}
