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

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;
import org.j3d.geom.CylinderGenerator;
import org.j3d.geom.TorusGenerator;
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

        Vertex[][] findex = we.getFaces();
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

        SAVExporter se = new SAVExporter();
        HashMap<String,Object> params = new HashMap<String, Object>();

        FileOutputStream fos = null;

        try {
            BinaryContentHandler writer = null;
            String filename = "c:/tmp/out.x3dv";
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
