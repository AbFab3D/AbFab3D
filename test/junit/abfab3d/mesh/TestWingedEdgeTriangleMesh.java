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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import abfab3d.io.output.SAVExporter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.j3d.geom.*;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.*;

// Internal Imports
import abfab3d.grid.*;
import abfab3d.grid.Grid.VoxelClasses;
import abfab3d.io.output.BoxesX3DExporter;
import org.web3d.vrml.sav.BinaryContentHandler;

import javax.vecmath.Point3d;

/**
 * Tests the functionality of WingedEdgeMesh
 *
 * @author Alan Hudson
 * @version
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

        Point3d[] pyr_vert = new Point3d[] {new Point3d(-1, -1, -1), new Point3d(1, -1, -1),
                new Point3d(1, 1, -1), new Point3d(-1, 1, -1),
                new Point3d(0, 0, 1)};
        int pyr_faces[][] = new int[][]{{3, 2, 0}, {2,1,0}, {0, 1, 4}, {1, 2, 4}, {2, 3, 4}, {3, 0, 4}};

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(pyr_vert, pyr_faces);

        we.writeOBJ(System.out);

        Vertex[][] findex = we.getFaceIndexes();
        Vertex v = we.getVertices();

        while (v != null) {
            System.out.println(v);
            v = v.next;
        }

        for (int i = 0; i < findex.length; i++) {

            Vertex face[] = findex[i];

            System.out.print("[");
            for (int j = 0; j < face.length; j++) {
                System.out.print(" " + face[j]);
            }
            System.out.println(" ]");

        }

        writeMesh(we,"c:/tmp/pyramid.x3dv");
    }

    public void testCollapse() throws Exception {
        Point3d[] verts = new Point3d[] {
                new Point3d(-0.5, 0, -0.5),
                new Point3d(0.5, 0, -0.5),
                new Point3d(-1, 0, 0),
                new Point3d(0,0,-0.25),
                new Point3d(1,0,0),
                new Point3d(0,0,0.5),
                new Point3d(-0.5,0,1),
                new Point3d(0.5,0,1)
        };
        int faces[][] = new int[][]{{0,2,3}, {0,3,1}, {1,3,4}, {3,2,5}, {4,3,5}, {2,6,5}, {5,6,7}, {4,5,7}};

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
            he = edges.he;

            if ((he.getHead() == v1 && he.getTail() == v2) ||
                (he.getHead() == v2 && he.getTail() == v1)) {
                found = true;
                break;
            }

            edges = edges.next;
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

        we.collapseEdge(edges, pos);
        writeMesh(we, "c:/tmp/collapse2.x3dv");

        // verify number of vertices remaining
        assertEquals("Vertex Count", expected_verts - 1, we.getVertexCount());
        assertEquals("Face Count", expected_faces - 2, we.getFaceCount());
        assertEquals("Edge Count", expected_edges - 1, we.getEdgeCount());
    }

    /**
     * Collapse many edges from a manifold structure.  Confirm it stays manifold
     */
    public void testSphere() throws Exception {
        SphereGenerator generator = new SphereGenerator(10, 4);
        GeometryData data = new GeometryData();

        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.geometryComponents = GeometryData.NORMAL_DATA;

        generator.generate(data);


        Point3d[] verts = new Point3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for(int i=0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Point3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        int faces[][] = new int[len][3];
        idx = 0;

        for(int i=0; i < len; i++) {
            faces[i][0] = data.indexes[idx++];
            faces[i][1] = data.indexes[idx++];
            faces[i][2] = data.indexes[idx++];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        writeMesh(we, "c:/tmp/sphere.x3dv");

        we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifold(we));

    }

    /**
     * Test a box is manifold on construction and edge collapse
     */
    public void testBox() throws Exception {
        GeometryData data = new GeometryData();

        data.vertexCount = 8;
        data.coordinates = new float[] {
                1,-1,1,
                1,1,1,
                -1,1,1,
                -1,-1,1,
                1,-1,-1,
                1,1,-1,
                -1,1,-1,
                -1,-1,-1
        };

        data.indexesCount = 36;
        data.indexes = new int[] {
                1,2,3,
                0,1,3,

                5,1,0,
                4,5,0,

                5,6,2,
                5,2,1,

                2,6,7,
                2,7,3,

                6,5,4,
                7,6,4,

                3,7,4,
                3,4,0
        };

        Point3d[] verts = new Point3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for(int i=0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Point3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        int faces[][] = new int[len][3];
        idx = 0;

        for(int i=0; i < len; i++) {
            faces[i][0] = data.indexes[idx++];
            faces[i][1] = data.indexes[idx++];
            faces[i][2] = data.indexes[idx++];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        writeMesh(we, "c:/tmp/box.x3dv");

        we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifold(we));
        assertTrue("Initial Triangle Check", verifyTriangles(we));

        // Find edge from vertex 3 to 5
        Vertex v1 = we.findVertex(verts[1]);
        Vertex v2 = we.findVertex(verts[2]);

        Edge edges = we.getEdges();
        HalfEdge he = null;

        boolean found = false;

        while (edges != null) {
            he = edges.he;

            if ((he.getHead() == v1 && he.getTail() == v2) ||
                    (he.getHead() == v2 && he.getTail() == v1)) {
                found = true;
                break;
            }

            edges = edges.next;
        }

        if (!found) {
            fail("Edge not found");
        }

        // use center point of vertices as new pos
        Point3d pos = new Point3d();
        pos.x = (verts[1].x + verts[2].x) / 2.0;
        pos.y = (verts[1].y + verts[2].y) / 2.0;
        pos.z = (verts[1].z + verts[2].z) / 2.0;

        we.collapseEdge(edges, pos);
        writeMesh(we, "c:/tmp/box2.x3dv");

        we.writeOBJ(System.out);

        assertTrue("Manifold2", isManifold(we));

        assertTrue("Triangle Check2", verifyTriangles(we));

    }

    private void writeMesh(WingedEdgeTriangleMesh we, String filename) throws IOException {
        SAVExporter se = new SAVExporter();
        HashMap<String,Object> params = new HashMap<String, Object>();

        FileOutputStream fos = null;

        try {
            BinaryContentHandler writer = null;
            fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".")+1);

            ErrorReporter console = new PlainTextErrorReporter();

            if (encoding.equals("x3db")) {
                writer = new X3DBinaryRetainedDirectExporter(fos,
                        3, 0, console,
                        X3DBinarySerializer.METHOD_FASTEST_PARSING,
                        0.001f, true);
            } else if (encoding.equals("x3dv")) {
                writer = new X3DClassicRetainedExporter(fos,3,0,console);
            } else if (encoding.equals("x3d")) {
                writer = new X3DXMLRetainedExporter(fos,3,0,console);
            } else {
                throw new IllegalArgumentException("Unhandled X3D encoding: " + encoding);
            }

            writer.startDocument("","", "utf8", "#X3D", "V3.0", "");
            writer.profileDecl("Immersive");
            writer.startNode("NavigationInfo", null);
            writer.startField("avatarSize");
            writer.fieldValue(new float[] {0.01f, 1.6f, 0.75f}, 3);
            writer.endNode(); // NavigationInfo

            se.outputX3D(we, params,writer);
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
        boolean manifold = true;
        Edge edges = mesh.getEdges();

        while(edges != null) {
            HalfEdge he = edges.he;
            HalfEdge twin = he.getTwin();

            if (twin == null) {
                manifold = false;

                System.out.println("NonManifold edge: " + edges + " he: " + he);
                return false;
            }

            edges = edges.next;
        }

        return manifold;
    }

    /**
     * Verify all triangles contain 3 distinct points
     *
     * @param mesh
     * @return
     */
    private boolean verifyTriangles(WingedEdgeTriangleMesh mesh) {
        for(Face faces = mesh.getFaces(); faces != null; faces = faces.next) {
            Vertex p1;
            Vertex p2;
            Vertex p3;

            p1 = faces.he.getHead();
            p2 = faces.he.getTail();

            HalfEdge he = faces.he.next;

            if (he.getHead() != p1 && he.getHead() != p2) {
                p3 = he.getHead();
            } else if (he.getTail() != p1 && he.getTail() != p2) {
                p3 = he.getHead();
            } else {
                System.out.println("Cannot find third unique point?");
                return false;
            }

            double EPS = 1e-10;

            if (p1.p.epsilonEquals(p2.p, EPS)) {
                System.out.println("Points equal: " + p1 + " p2: " + p2);
                return false;
            }
            if (p1.p.epsilonEquals(p3.p, EPS)) {
                System.out.println("Points equal: " + p1 + " p2: " + p3);
                return false;
            }
            if (p2.p.epsilonEquals(p3.p, EPS)) {
                System.out.println("Points equal: " + p2 + " p2: " + p3);
                return false;
            }
        }

        return true;
    }
}
