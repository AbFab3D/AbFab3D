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

package abfab3d.mesh;

// External Imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import abfab3d.io.input.IndexedTriangleSetLoader;
import abfab3d.io.output.SAVExporter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.j3d.geom.*;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.*;

// Internal Imports
import org.web3d.vrml.sav.BinaryContentHandler;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import static abfab3d.util.Output.printf; 

/**
 * Tests the functionality of MeshDecimator
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestMeshDecimator extends TestCase {
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestMeshDecimator.class);
    }

    /**
     * Test that we can create a simple object without crashing.
     *
     * @throws Exception
     */
    public void _testPyramid() throws Exception {

        Point3d[] pyr_vert = new Point3d[] {
            new Point3d(-1., -1., -1.), 
            new Point3d( 1., -1., -1.),
            new Point3d( 1.,  1., -1.), 
            new Point3d(-1.,  1., -1.),
            new Point3d( 0.,  0.,  1.), 
        };
        int pyr_faces[][] = new int[][]{{3, 2, 0}, {2,1,0}, {0, 1, 4}, {1, 2, 4}, {2, 3, 4}, {3, 0, 4}};

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

        writeMesh(we,"c:/tmp/pyramid.x3dv");

        MeshDecimator md = new MeshDecimator();

        printf("startng decimations\n");

        int count = md.processMesh(we, 100);

        printf("final faces count: %d\n", count);        

    }

    public void testFile() throws Exception {

        //String fpath = "test/models/speed-knot.x3db";
        String fpath = "test/models/sphere_10cm_rough.x3dv";
        
        WingedEdgeTriangleMesh mesh = loadMesh(fpath);

        printf("mesh faces: %d, vertices: %d, edges: %d\n", mesh.getFaceCount(),mesh.getVertexCount(), mesh.getEdgeCount());
        writeMesh(mesh,"c:/tmp/test_01.x3dv");
        
        int fcount = mesh.getFaceCount();

        MeshDecimator md = new MeshDecimator();
        
        int count = md.processMesh(mesh, 100);
                               
    }

    /**
       
     */
    public static WingedEdgeTriangleMesh loadMesh(String fpath){
        
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
        
        for(int i=0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Vector3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        idx = 0;
        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();
        for(int i=0; i < len; i++) {
            its.addTri(verts[data.indexes[idx++]],verts[data.indexes[idx++]],verts[data.indexes[idx++]]);
        }
        
        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
        return we;

    }

    public static void writeMesh(WingedEdgeTriangleMesh we, String filename) throws IOException {
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
}

