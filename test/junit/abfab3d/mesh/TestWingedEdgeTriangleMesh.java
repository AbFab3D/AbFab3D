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
import abfab3d.mesh.*;
import abfab3d.util.StructMixedData;
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

import static abfab3d.util.Output.printf;

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

        double[] pyr_vert = new double[]{-1, -1, -1,
                1, -1, -1,
                1, 1, -1,
                -1, 1, -1,
                0, 0, 1};
        int pyr_faces[] = new int[]{
                3, 2, 0,
                2, 1, 0,
                0, 1, 4,
                1, 2, 4,
                2, 3, 4,
                3, 0, 4};

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(pyr_vert, pyr_faces);

        we.writeOBJ(System.out);

        writeMesh(we, "c:/tmp/pyramid.x3dv");
    }

    /**
     * Test that we can create a simple object with color.
     *
     * @throws Exception
     */
    public void testColor() throws Exception {

        double[] pyr_vert = new double[]{-1, -1, -1,
                1, -1, -1,
                1, 1, -1,
                -1, 1, -1,
                0, 0, 1};
        float[][] pyr_attribs = new float[][]{{1, 1, 1},
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1},
                {1, 0, 1}};
        int pyr_faces[] = new int[]{
                3, 2, 0,
                2, 1, 0,
                0, 1, 4,
                1, 2, 4,
                2, 3, 4,
                3, 0, 4};

        int[] semantics = new int[] {WingedEdgeTriangleMesh.VA_COLOR};
        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(pyr_vert, pyr_attribs, semantics, pyr_faces);

        we.writeOBJ(System.out);

        writeMesh(we, "c:/tmp/pyramid_color.x3dv");
    }

    public void testCollapse() throws Exception {
        double[] verts = new double[] {
                -0.5, 0, -0.5,
                0.5, 0, -0.5,
                -1, 0, 0,
                0, 0, -0.25,
                1, 0, 0,
                0, 0, 0.5,
                -0.5, 0, 1,
                0.5, 0, 1
        };
        int faces[] = new int[]{
                0, 2, 3,
                0, 3, 1,
                1, 3, 4,
                3, 2, 5,
                4, 3, 5,
                2, 6, 5,
                5, 6, 7,
                4, 5, 7};

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        //we.writeOBJ(System.out);

        writeMesh(we, "c:/tmp/collapse1.x3dv");

        int expected_verts = 8;
        int expected_faces = 8;
        int expected_edges = 15;
        assertEquals("Initial Vertex Count", expected_verts, we.getVertexCount());
        assertEquals("Initial Face Count", expected_faces, we.getFaceCount());
        assertEquals("Initial Edge Count", expected_edges, we.getEdgeCount());


        double EPS = 1e-8;
        boolean result = collapseEdge(3,5,we,verts,EPS);
        assertEquals("Did collapse", result, true);

        writeMesh(we, "c:/tmp/collapse2.x3dv");

        // verify number of vertices remaining
        assertEquals("Vertex Count", expected_verts - 1, we.getVertexCount());
        assertEquals("Face Count", expected_faces - 2, we.getFaceCount());
        assertEquals("Edge Count", expected_edges - 3, we.getEdgeCount());
    }

    private boolean collapseEdge(int vpos1, int vpos2, WingedEdgeTriangleMesh we, double[] verts, double EPS) {
        // Find edge from vertex 3 to 5
        int v1 = we.findVertex(new double[] {verts[vpos1*3],verts[vpos1*3+1],verts[vpos1*3+2]},EPS);
        int v2 = we.findVertex(new double[] {verts[vpos2*3],verts[vpos2*3+1], verts[vpos2*3+2]},EPS);

        StructMixedData edges = we.getEdges();
        StructMixedData hedges = we.getHalfEdges();
        int e = we.getStartEdge();
        int he = -1;
        int start, end;

        boolean found = false;

        while (e != -1) {
            he = Edge.getHe(edges, e);
            start = HalfEdge.getStart(hedges, he);
            end = HalfEdge.getEnd(hedges,he);

            if ((start == v1 && end == v2) ||
                    (start == v2 && end == v1)) {
                found = true;
                break;
            }

            e = Edge.getNext(edges, e);
        }

        if (!found) {
            fail("Edge not found");
        }

        //System.out.println("edge: " + edges);

        // use center point of vertices as new pos
        Point3d pos = new Point3d();
        pos.x = (verts[vpos1*3] + verts[vpos2*3]) / 2.0;
        pos.y = (verts[vpos1*3+1] + verts[vpos2*3+1]) / 2.0;
        pos.z = (verts[vpos1*3+2] + verts[vpos2+3+2]) / 2.0;

        EdgeCollapseResult ecr = new EdgeCollapseResult();
        EdgeCollapseParams ecp = new EdgeCollapseParams();

        boolean result = we.collapseEdge(e, pos, ecp, ecr);


        return result;
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

        double[] verts = new double[data.coordinates.length];
        int len = verts.length;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            verts[i] = data.coordinates[i];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, data.indexes);
        int edge_cnt = we.getEdgeCount();

        writeMesh(we, "c:/tmp/box.x3dv");

        //we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifold(we));
        assertTrue("Initial Triangle Check", verifyTriangles(we));

        // Find edge from vertex 0 to 2
        double EPS = 1e-8;
        boolean result = collapseEdge(0,2,we,verts,EPS);
        assertFalse("Edge Collapse", result);

        //we.writeOBJ(System.out);
        writeMesh(we, "c:/tmp/box2.x3dv");

        assertTrue("Manifold2", isManifold(we));

        assertTrue("Triangle Check2", verifyTriangles(we));

        // No change expected
        assertEquals("Edge count", edge_cnt, we.getEdgeCount());
        //assertEquals("Removed Edges", 0, ecr.removedEdges.size());

    }

    public void testDegenerateFace() throws Exception {
        double[] verts = new double[] {
                -1, 0, -1,       // 0
                0, 0, -1,        // 1
                1, 0, -1,        // 2
                0, 0, -0.5,      // 3
                -1, 0, 0,        // 4
                1, 0, 0,         // 5
                0, 0, 0.5,       // 6
                -1, 0, 1,        // 7
                0, 0, 1,         // 8
                1, 0, 1,         // 9
                0.5, 0, 0,       // 10
                0,-1,0           // 11
        };
        int faces[] = new int[]{
                1, 0, 3,
                2, 1, 3,
                0, 4, 3,
                2, 3, 5,
                3, 4, 6,
                3, 6, 10,
                3, 10, 5,
                10, 6, 5,
                4, 7, 6,
                6, 7, 8,
                6, 8, 9,
                5, 6, 9,
                // base
                7,4,11,
                4,0,11,
                2,5,11,
                5,9,11,
                0,1,11,
                1,2,11,
                9,8,11,
                8,7,11,
        };

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        //we.writeOBJ(System.out);

        writeMesh(we, "c:/tmp/degenface1.x3dv");

        int expected_verts = 12;
        int expected_faces = 20;
        int expected_edges = 30;
        assertEquals("Initial Vertex Count", expected_verts, we.getVertexCount());
        assertEquals("Initial Face Count", expected_faces, we.getFaceCount());
        assertEquals("Initial Edge Count", expected_edges, we.getEdgeCount());
        assertTrue("Initial Manifold", isManifold(we));
        assertTrue("Initial Structural Check", verifyStructure(we, true));


        double EPS = 1e-8;
        // Find edge from vertex 3 to 6
        boolean result = collapseEdge(3,6,we,verts,EPS);

        assertEquals("Did not collapse", result, false);

        writeMesh(we, "c:/tmp/degenface2.x3dv");
        //we.writeOBJ(System.out);
        assertTrue("Structural Check", verifyStructure(we, false));

        //we.removeDegenerateFaces();
        writeMesh(we, "c:/tmp/degenface3.x3dv");
        //we.writeOBJ(System.out);
        assertTrue("Structural Check", verifyStructure(we, false));

        // verify number of vertices remaining
        assertEquals("Vertex Count", expected_verts, we.getVertexCount());
        assertEquals("Face Count", expected_faces, we.getFaceCount());
        assertEquals("Edge Count", expected_edges, we.getEdgeCount());
    }

    /**
     * Test a box is manifold on construction and edge collapse
     */
    public void testManifoldE() throws Exception {
        IndexedTriangleSetLoader loader = new IndexedTriangleSetLoader(false);
        loader.processFile(new File("test/models/wTest1_ITS.x3d"));

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.coordinates = loader.getCoords();
        data.vertexCount = data.coordinates.length / 3;
        data.indexes = loader.getVerts();
        data.indexesCount = data.indexes.length;

        double[] verts = new double[data.coordinates.length];
        int len = data.coordinates.length;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            verts[i] = data.coordinates[i];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, data.indexes);

        writeMesh(we, "c:/tmp/etest1.x3dv");

        int[] tris = we.getFaceIndexes();

        assertTrue("Number of triangles", tris.length == data.indexes.length);
        //we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifold(we));
        assertTrue("Initial Triangle Check", verifyTriangles(we));

        Random rand = new Random(42);
        int collapses = 10;

        int faceCount = we.getFaceCount();
        StructMixedData edges = we.getEdges();
        StructMixedData hedges = we.getHalfEdges();
        StructMixedData vertices = we.getVertices();


        for (int i = 0; i < collapses; i++) {
            idx = rand.nextInt(we.getEdgeCount());

            int e = we.getStartEdge();

            for (int j = 0; j < idx; j++) {
                e = Edge.getNext(edges,e);
            }


            //System.out.println("Collapse: " + idx + " e: " + e);
            Point3d pos = new Point3d();
            double[] p1 = new double[3];
            double[] p2 = new double[3];

            int he = Edge.getHe(edges,e);
            int start = HalfEdge.getStart(hedges,he);
            int end = HalfEdge.getEnd(hedges,he);
            Vertex.getPoint(vertices, start, p1);
            Vertex.getPoint(vertices, end, p2);

            pos.x = (p1[0] + p2[0]) / 2.0;
            pos.y = (p1[1] + p2[1]) / 2.0;
            pos.z = (p1[2] + p2[2]) / 2.0;

            EdgeCollapseResult ecr = new EdgeCollapseResult();
            EdgeCollapseParams ecp = new EdgeCollapseParams();
            int pre_tri_cnt = we.getTriangleCount();
            boolean worked = we.collapseEdge(e, pos, ecp, ecr);

            //System.out.println("Collapsed: " + idx + " worked: " + worked);
            writeMesh(we, "c:/tmp/etest_loop" + i + ".x3dv");
            //we.writeOBJ(System.out);

            if (worked) {
                assertTrue("Tri count", pre_tri_cnt > we.getTriangleCount());
            } else {
                assertTrue("Tri count", pre_tri_cnt == we.getTriangleCount());
            }
            assertTrue("Manifold", isManifold(we));
            assertTrue("Triangle Check", verifyTriangles(we));
        }

        assertTrue("Manifold2", isManifold(we));
        assertTrue("Triangle Check2", verifyTriangles(we));

        writeMesh(we, "c:/tmp/etest2.x3dv");
    }

    /**
     * Test a sphere is manifold on construction and edge collapse
     */
    public void testManifoldSphere() throws Exception {
        long start_time = System.currentTimeMillis();

        IndexedTriangleSetLoader loader = new IndexedTriangleSetLoader(false);
        loader.processFile(new File("test/models/sphere_10cm_rough_manifold.x3dv"));

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.coordinates = loader.getCoords();
        data.vertexCount = data.coordinates.length / 3;
        data.indexes = loader.getVerts();
        data.indexesCount = data.indexes.length;

        double[] verts = new double[data.coordinates.length];
        int len = data.coordinates.length;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            verts[i] = data.coordinates[i];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, data.indexes);

        writeMesh(we, "c:/tmp/etest1.x3dv");

        int[] tris = we.getFaceIndexes();

        assertTrue("Number of triangles", tris.length == data.indexes.length);
        //we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifold(we));
        assertTrue("Initial Triangle Check", verifyTriangles(we));

        Random rand = new Random(42);
        int collapses = 158;

        StructMixedData edges = we.getEdges();
        StructMixedData hedges = we.getHalfEdges();
        StructMixedData vertices = we.getVertices();

        Point3d pos = new Point3d();
        double[] p1 = new double[3];
        double[] p2 = new double[3];

        for (int i = 0; i < collapses; i++) {
            idx = rand.nextInt(we.getEdgeCount());

            int e = we.getStartEdge();

            for (int j = 0; j < idx; j++) {
                e = Edge.getNext(edges,e);
            }


            //System.out.println("Collapse: " + idx + " e: " + e);

            int he = Edge.getHe(edges,e);
            int start = HalfEdge.getStart(hedges,he);
            int end = HalfEdge.getEnd(hedges,he);
            Vertex.getPoint(vertices, start, p1);
            Vertex.getPoint(vertices, end, p2);

            pos.x = (p1[0] + p2[0]) / 2.0;
            pos.y = (p1[1] + p2[1]) / 2.0;
            pos.z = (p1[2] + p2[2]) / 2.0;

            EdgeCollapseResult ecr = new EdgeCollapseResult();
            EdgeCollapseParams ecp = new EdgeCollapseParams();
            int pre_tri_cnt = we.getTriangleCount();
            boolean worked = we.collapseEdge(e, pos, ecp, ecr);

            //System.out.println("Collapsed: " + idx + " worked: " + worked);
            writeMesh(we, "c:/tmp/etest_loop" + i + ".x3dv");
            //we.writeOBJ(System.out);

            if (worked) {
                assertTrue("Tri count", pre_tri_cnt > we.getTriangleCount());
            } else {
                assertTrue("Tri count", pre_tri_cnt == we.getTriangleCount());
            }
            assertTrue("Manifold", isManifold(we));
            assertTrue("Triangle Check", verifyTriangles(we));
        }

        assertTrue("Manifold2", isManifold(we));
        assertTrue("Triangle Check2", verifyTriangles(we));

        writeMesh(we, "c:/tmp/etest2.x3dv");

        System.out.println("Total time: " + (System.currentTimeMillis() - start_time));
    }

    /**
     * Test a box is manifold on construction and edge collapse
     */
    public void testManifoldSpeedKnot() throws Exception {
        long start_time = System.currentTimeMillis();

        IndexedTriangleSetLoader loader = new IndexedTriangleSetLoader(false);
        loader.processFile(new File("test/models/speed-knot.x3db"));

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.coordinates = loader.getCoords();
        data.vertexCount = data.coordinates.length / 3;
        data.indexes = loader.getVerts();
        data.indexesCount = data.indexes.length;

        double[] verts = new double[data.coordinates.length];
        int len = data.coordinates.length;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            verts[i] = data.coordinates[i];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, data.indexes);

        writeMesh(we, "c:/tmp/speed-knot1.x3dv");

        int[] tris = we.getFaceIndexes();

        assertTrue("Number of triangles", tris.length == data.indexes.length);
        //we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifold(we));
        assertTrue("Initial Triangle Check", verifyTriangles(we));

        Random rand = new Random(42);
        int collapses = 1000;

        StructMixedData edges = we.getEdges();
        StructMixedData hedges = we.getHalfEdges();
        StructMixedData vertices = we.getVertices();


        Point3d pos = new Point3d();
        double[] p1 = new double[3];
        double[] p2 = new double[3];

        for (int i = 0; i < collapses; i++) {
            idx = rand.nextInt(we.getEdgeCount());

            int e = we.getStartEdge();

            for (int j = 0; j < idx; j++) {
                e = Edge.getNext(edges,e);
            }


            //System.out.println("Collapse: " + idx + " e: " + e);

            int he = Edge.getHe(edges,e);
            int start = HalfEdge.getStart(hedges,he);
            int end = HalfEdge.getEnd(hedges,he);
            Vertex.getPoint(vertices, start, p1);
            Vertex.getPoint(vertices, end, p2);

            pos.x = (p1[0] + p2[0]) / 2.0;
            pos.y = (p1[1] + p2[1]) / 2.0;
            pos.z = (p1[2] + p2[2]) / 2.0;

            EdgeCollapseResult ecr = new EdgeCollapseResult();
            EdgeCollapseParams ecp = new EdgeCollapseParams();
            int pre_tri_cnt = we.getTriangleCount();
            boolean worked = we.collapseEdge(e, pos, ecp, ecr);

            //System.out.println("Collapsed: " + idx + " worked: " + worked);
            //writeMesh(we, "c:/tmp/etest_loop" + i + ".x3dv");
            //we.writeOBJ(System.out);

            if (worked) {
                assertTrue("Tri count", pre_tri_cnt > we.getTriangleCount());
            } else {
                assertTrue("Tri count", pre_tri_cnt == we.getTriangleCount());
            }
            assertTrue("Manifold", isManifold(we));
            assertTrue("Triangle Check", verifyTriangles(we));
        }

        assertTrue("Manifold2", isManifold(we));
        assertTrue("Triangle Check2", verifyTriangles(we));

        writeMesh(we, "c:/tmp/speed-knot2.x3dv");

        System.out.println("Total time: " + (System.currentTimeMillis() - start_time));
    }

    /**
     * Test whether a mesh is manifold
     *
     * @param mesh
     * @return
     */
    public static boolean isManifold(WingedEdgeTriangleMesh mesh) {
        // Check via twins structure
        boolean manifold = true;
        StructMixedData edges = mesh.getEdges();
        int e = mesh.getStartEdge();
        StructMixedData hedges = mesh.getHalfEdges();
        StructMixedData vertices = mesh.getVertices();

        while (e != -1) {
            int he = Edge.getHe(edges,e);
            int twin = HalfEdge.getTwin(hedges,he);

            if (twin == -1) {
                System.out.println("NonManifold edge: " + edges + " he: " + he);
                return false;
            }

            e = Edge.getNext(edges,e);
        }


        // check via counts

        int[] faces = mesh.getFaceIndexes();

        int len = faces.length;

        LongHashMap edgeCount = new LongHashMap();

        for (int i = 0; i < len / 3; i++) {
            //System.out.println("Count face: " + faces[i][0].getID() + " " + faces[i][1].getID() + " " + faces[i][2].getID());
            processEdge(Vertex.getID(vertices,faces[i*3]), Vertex.getID(vertices,faces[i*3+1]), edgeCount);
            processEdge(Vertex.getID(vertices,faces[i*3+1]), Vertex.getID(vertices,faces[i*3+2]), edgeCount);
            processEdge(Vertex.getID(vertices,faces[i*3+2]), Vertex.getID(vertices,faces[i*3]), edgeCount);
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

    private static void processEdge(int index1, int index2, LongHashMap edgeCount) {

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

            se.outputX3D(we, params, writer, null);
            writer.endDocument();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * Verify that the mesh structure is correct.  Chase as many pointers and references as we can to confirm that
     * nothing is messed up.
     *
     * @param mesh
     * @return
     */
    public static boolean verifyStructure(WingedEdgeTriangleMesh mesh, boolean manifold) {
        // Walk edges and make sure no referenced head or twin values are null
        // Make sure twin references same vertices

        StructMixedData edges = mesh.getEdges();
        int startEdge = mesh.getStartEdge();

        StructMixedData faces = mesh.getFaces();
        int startFace = mesh.getStartFace();

        StructMixedData vertices = mesh.getVertices();
        int startVertex = mesh.getStartVertex();

        StructMixedData hedges = mesh.getHalfEdges();

        int e = startEdge;

        int edgeCount = 0;

        while(e != -1) {
            edgeCount++;
            int he = Edge.getHe(edges,e);
            if (he == -1) {
                System.out.println("Edge found with null Head: " + e);
                return false;
            }

            int twin = HalfEdge.getTwin(hedges, he);

            if (manifold && twin == -1) {
                System.out.println("Edge found with null Twin: " + e);
                return false;
            }

            if (twin != -1) {
                int estart = HalfEdge.getStart(hedges, he);
                int eend = HalfEdge.getEnd(hedges, he);
                int tstart = HalfEdge.getStart(hedges, twin);
                int tend = HalfEdge.getEnd(hedges, twin);
                if (estart != tend || eend != tstart) {
                    System.out.println("Invalid twins: " + he + " twin: " + twin);
                    return false;
                }
            }

            e = Edge.getNext(edges,e);
        }

        assertEquals("Edge count", mesh.getEdgeCount(), edgeCount);

        // Make sure all faces have three half edges
        // Make sure all edge and face references in halfedge are valid
        // Make sure forward traversal(next) around face is same as backwards(prev)

        int f = startFace;
        int faceCount = 0;
        while(f != -1) {
            faceCount++;

            int he = Face.getHe(faces,f);

            int start = he;

            if (he == -1) {
                System.out.println("Half edge null: " + f);
                return false;
            }
            int cnt = 0;
            while(he != -1) {
                int edge = HalfEdge.getEdge(hedges,he);
                if (!findEdge(mesh, edge)) {
                    System.out.println("Cannot find edge: " + edge);
                    return false;
                }
                int left = HalfEdge.getLeft(hedges,he);
                if (!findFace(mesh, left)) {
                    System.out.println("Cannot find face: " + left);
                    return false;
                }

                cnt++;
                he = HalfEdge.getNext(hedges,he);
                if (he == start) {
                    break;
                }
            }

            if (cnt != 3) {
                System.out.println("Face without 3 half edges(next): " + f);
                return false;
            }

            he = Face.getHe(faces,f);
            start = he;
            cnt = 0;
            while(he != -1) {
                cnt++;
                he = HalfEdge.getPrev(hedges,he);
                if (he == start) {
                    break;
                }
            }

            if (cnt != 3) {
                System.out.println("Face without 3 half edges(prev): " + f);
                return false;
            }


            f = Face.getNext(faces,f);
        }

        assertEquals("Face count", mesh.getFaceCount(), faceCount);

        // verify vertex link is bidirectional, ie edge thinks its connected to vertex

        int v = startVertex;
        int vertexCount = 0;
        while(v != -1) {
            vertexCount++;
            int he = Vertex.getLink(vertices,v);

            if (he == -1) {
                System.out.println("Vertex not linked: " + v);
                return false;
            }

            int start = HalfEdge.getStart(hedges,he);
            int end = HalfEdge.getEnd(hedges,he);

            if (start != v && end != v) {
                System.out.println("Vertex linkage not bidirectional: " + v + " he: " + he);
                return false;
            }

            v = Vertex.getNext(vertices,v);
        }
        assertEquals("Vertex count", mesh.getVertexCount(), vertexCount);


        // Check for any edges that are duplicated
        int e1 = startEdge;
        int e2 = startEdge;

        while(e1 != -1) {
            while(e2 != -1) {
                if (e1 == e2) {
                    e2 = Edge.getNext(edges, e2);
                }

                int he1 = Edge.getHe(edges,e1);
                int he2 = Edge.getHe(edges,e2);

                if (he1 != -1) {
                    int start = HalfEdge.getStart(hedges,he1);
                    int end = HalfEdge.getEnd(hedges,he1);
                    if (start == end) {
                        System.out.println("Collapsed edge detected: " + he1);
                        return false;
                    }
                }
                if (he2 != -1) {
                    int start = HalfEdge.getStart(hedges,he2);
                    int end = HalfEdge.getEnd(hedges,he2);
                    if (start == end) {
                        System.out.println("Collapsed edge detected: " + he2);
                        return false;
                    }
                }
                if (he1 != -1 && he2 != -1) {
                    int start1 = HalfEdge.getStart(hedges,he1);
                    int end1 = HalfEdge.getEnd(hedges,he1);
                    int start2 = HalfEdge.getStart(hedges,he2);
                    int end2 = HalfEdge.getEnd(hedges,he2);

                    if ((start1 == start2 && end1 == end2) ||
                            (start1 == end2 && end1 == start2)) {
                        System.out.println("Duplicate detected: " + e1 + " is: " + e2);
                        return false;
                    }
                }

                e2 = Edge.getNext(edges, e2);
            }
            e1 = Edge.getNext(edges, e1);
        }

        v = startVertex;
        while(v != -1){
            int start = Vertex.getLink(vertices, v);
            int he = start;
            int tricount = 0;

            do {
                //printf("he: %s\n", he + " hc: " + he.hashCode());

                int twin = HalfEdge.getTwin(hedges,he);
                he = HalfEdge.getNext(hedges, twin);

            } while(he != start && tricount++ < 20);

            if (tricount >= 20) {
                System.out.println("***Strange linking error?");
                return false;
            }

            v = Vertex.getNext(vertices,v);
        }

        return true;
    }

    /**
     * Traverse edges list and make sure an edge is traversible from there.
     *
     * @param mesh
     * @param dest
     * @return
     */
    private static boolean findEdge(WingedEdgeTriangleMesh mesh, int dest) {
        StructMixedData edges = mesh.getEdges();
        int e = mesh.getStartEdge();

        while(e != -1) {
            if (e == dest) {
                return true;
            }

            e = Edge.getNext(edges, e);
        }

        return false;
    }

    /**
     * Traverse faces list and make sure a face is traversible from there.
     *
     * @param mesh
     * @param dest
     * @return
     */
    private static boolean findFace(WingedEdgeTriangleMesh mesh, int dest) {
        StructMixedData faces = mesh.getFaces();
        int f = mesh.getStartFace();

        while(f != -1) {
            if (f == dest) {
                return true;
            }

            f = Face.getNext(faces, f);
        }

        return false;
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

        StructMixedData faces = mesh.getFaces();
        int startFace = mesh.getStartFace();
        StructMixedData hedges = mesh.getHalfEdges();
        StructMixedData vertices = mesh.getVertices();

        int f = startFace;

        double[] tmp = new double[3];

        Point3d pnt1 = new Point3d();
        Point3d pnt2 = new Point3d();
        Point3d pnt3 = new Point3d();
        
        while(f != -1) {
            int he1 = Face.getHe(faces,f);
            int p1 = HalfEdge.getStart(hedges, he1);
            int p2 = HalfEdge.getEnd(hedges, he1);


            int he2 = HalfEdge.getNext(hedges,he1);
            int p3 = HalfEdge.getStart(hedges, he2);

            if (p3 != p1 && p3 != p2) {
                // start is good for third point
            } else if (p3 != p1 && HalfEdge.getEnd(hedges,he2) != p2) {
                p3 = HalfEdge.getEnd(hedges,he2);
            } else {
                System.out.println("Cannot find third unique point?");
                return false;
            }

            double EPS = 1e-10;

            Vertex.getPoint(vertices,p1,pnt1);
            Vertex.getPoint(vertices,p2,pnt2);
            Vertex.getPoint(vertices,p3,pnt3);
            
            if (pnt1.epsilonEquals(pnt2, EPS)) {
                System.out.println("Points equal(1,2): " + p1 + " p2: " + p2 + " face: " + faces);
                return false;
            }
            if (pnt1.epsilonEquals(pnt3, EPS)) {
                System.out.println("Points equal(1,3): " + p1 + " p2: " + p3 + " face: " + faces);
                return false;
            }
            if (pnt2.epsilonEquals(pnt3, EPS)) {
                System.out.println("Points equal(2,3): " + p2 + " p2: " + p3 + " face: " + faces);
                return false;
            }

            svec1.x = pnt2.x - pnt1.x;
            svec1.y = pnt2.y - pnt1.y;
            svec1.z = pnt2.z - pnt1.z;

            svec2.x = pnt3.x - pnt1.x;
            svec2.y = pnt3.y - pnt1.y;
            svec2.z = pnt3.z - pnt1.z;

            svec1.cross(svec1, svec2);
            double area = svec1.length();

            if (area < EPS) {
                System.out.println("Triangle area 0: " + faces);
                return false;
            }

            f = Face.getNext(faces,f);
        }

        return true;
    }

}
