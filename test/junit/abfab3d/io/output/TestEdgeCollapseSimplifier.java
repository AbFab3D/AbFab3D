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

package abfab3d.io.output;

// External Imports
import java.io.FileOutputStream;
import java.util.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.export.X3DClassicRetainedExporter;
import org.web3d.vrml.sav.BinaryContentHandler;

// Internal Imports
import toxi.geom.Line3D;
import toxi.geom.Vec3D;
import toxi.geom.mesh.*;

/**
 * Tests the functionality of the EdgeCollapseSimplifier
 *
 * @author Alan Hudson
 * @version
 */
public class TestEdgeCollapseSimplifier extends TestCase {

    /** Horizontal resolution of the printer in meters.  */
    public static final double HORIZ_RESOLUTION = 0.004;

    /** Vertical resolution of the printer in meters.  */
    public static final double VERT_RESOLUTION = 0.004;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestEdgeCollapseSimplifier.class);
    }

    /**
     * Test a single edge collapse of a known system
     */
    public void testEdgeCollapse() {
        WETriangleMesh mesh = new WETriangleMesh();

        int w = 3;
        int h = 3;
        float spacing = 1;
        
        for(int j=0; j < h; j++) {
            for(int i=0; i < w; i++) {
                Vec3D v0 = new Vec3D(i * spacing, j * spacing, 0);
                Vec3D v1 = new Vec3D((i+1) * spacing, (j+0) * spacing, 0);
                Vec3D v2 = new Vec3D((i+1) * spacing, (j+1) * spacing, 0);
                Vec3D v3 = new Vec3D((i+0) * spacing, (j+1) * spacing, 0);

                mesh.addFace(v0,v1,v2);
                mesh.addFace(v0,v2,v3);
            }
        }

        Set<WingedEdge> border = findBorder(mesh);
        System.out.println("Border edges: " + border);
        
        System.out.println("vertices: " + mesh.vertices);
        System.out.println("before valid: " + validateMesh(mesh, border));

        EdgeCollapseSimplifier reducer = new EdgeCollapseSimplifier();

        WingedEdge edge = getEdge(18,mesh);

        //System.out.println("removing edge: " + edge);
        reducer.collapseEdge2(edge, (WEVertex) edge.a, mesh);
        assertTrue("Valid mesh",validateMesh(mesh,border));

        write(mesh);
    }

    /**
     * Test a single edge collapse of a known system
     */
    public void testMultiEdgeCollapse() {
        WETriangleMesh mesh = new WETriangleMesh();

        int w = 40;
        int h = 40;
        float spacing = 1;

        for(int j=0; j < h; j++) {
            for(int i=0; i < w; i++) {
                Vec3D v0 = new Vec3D(i * spacing, j * spacing, 0);
                Vec3D v1 = new Vec3D((i+1) * spacing, (j+0) * spacing, 0);
                Vec3D v2 = new Vec3D((i+1) * spacing, (j+1) * spacing, 0);
                Vec3D v3 = new Vec3D((i+0) * spacing, (j+1) * spacing, 0);

                mesh.addFace(v0,v1,v2);
                mesh.addFace(v0,v2,v3);
            }
        }

        Set<WingedEdge> border = findBorder(mesh);
        System.out.println("Border edges: " + border);
        System.out.println("before valid: " + validateMesh(mesh, border));

        EdgeCollapseSimplifier reducer = new EdgeCollapseSimplifier();

        int cnt = 0;

        while(true) {
            WingedEdge edge = getNonBorderEdge(mesh, border);

            if (edge == null) {
                System.out.println("No edges left, quitting.");
                break;
            }
            //printEdgeDetails(edge);
            //System.out.println("removing edge: " + edge);
            reducer.collapseEdge2(edge, (WEVertex) edge.a, mesh);
            assertTrue("Valid mesh",validateMesh(mesh,border));
            cnt++;
        }

        System.out.println("Edges removed: " + cnt);
        write(mesh);
    }

    /**
     * Test a single edge collapse of a border.
     *
     * TODO: This test is no longer working not sure why
     */
    public void _testBorderCollapse() {
        WETriangleMesh mesh = new WETriangleMesh();

        int w = 3;
        int h = 3;
        float spacing = 1;

        for(int j=0; j < h; j++) {
            for(int i=0; i < w; i++) {
                Vec3D v0 = new Vec3D(i * spacing, j * spacing, 0);
                Vec3D v1 = new Vec3D((i+1) * spacing, (j+0) * spacing, 0);
                Vec3D v2 = new Vec3D((i+1) * spacing, (j+1) * spacing, 0);
                Vec3D v3 = new Vec3D((i+0) * spacing, (j+1) * spacing, 0);

                mesh.addFace(v0,v1,v2);
                mesh.addFace(v0,v2,v3);
            }
        }

        Set<WingedEdge> border = findBorder(mesh);
        System.out.println("Border edges: " + border);
        System.out.println("before valid: " + validateMesh(mesh, border));

        EdgeCollapseSimplifier reducer = new EdgeCollapseSimplifier();

        int cnt = 0;

        WingedEdge edge = getBorderEdge(mesh, border);

        //printEdgeDetails(edge);
        //System.out.println("removing edge: " + edge);
        reducer.collapseEdge2(edge, (WEVertex) edge.a, mesh);
        write(mesh);

        assertTrue("Valid mesh",validateMesh(mesh,border));
        cnt++;

        System.out.println("Edges removed: " + cnt);
    }

    private Set<WingedEdge> findBorder(WETriangleMesh mesh) {
        HashSet ret_val = new HashSet();
        
        for(WingedEdge edge : mesh.edges.values()) {
//System.out.println(edge);
            if (edge.faces.size() != 2) {
                ret_val.add(edge);
            }
        }
        
        return ret_val;
    }
    
    private void printEdges(WETriangleMesh mesh) {
        System.out.println("Printing edges:");
        Map.Entry<Line3D, WingedEdge> entry;
        Iterator<Map.Entry<Line3D, WingedEdge>> itr = mesh.edges.entrySet().iterator();
        while(itr.hasNext()) {
            entry = itr.next();
            System.out.println("Key: " + entry.getKey() + " --> " + entry.getValue());
        }
        
    }
    private boolean validateMesh(WETriangleMesh mesh, Set<WingedEdge> border) {
        // Insure all non border edges have 2 faces

        boolean valid = true;

        // Validate edge key to value
        Map.Entry<Line3D, WingedEdge> entry;
        Iterator<Map.Entry<Line3D, WingedEdge>> itr = mesh.edges.entrySet().iterator();
        while(itr.hasNext()) {
            entry = itr.next();
            WingedEdge edge = (WingedEdge) entry.getKey();

            if (edge.id != entry.getValue().id) {
                System.out.println("Invalid Key: " + entry.getKey() + " --> " + entry.getValue());
                valid = false;
            }
        }

        for(WingedEdge edge : mesh.edges.values()) {
            if (edge.faces.size() != 2) {
                if (border.contains(edge)) {
                    continue;
                }
                
                valid = false;
                System.out.println("Invalid edge: " + edge);
            }
        }
        
        // insure all faces have three edges and all vertices exist
        for(Face face : mesh.faces.values()) {
            WEFace f = (WEFace) face;

            if (f.edges.size() != 3) {
                System.out.println("Invalid face: " + face);
                valid = false;
            }

            WEVertex v = (WEVertex) f.a;
            List<WingedEdge> edges = v.edges;

            // check the vertex is in the mesh
            Vec3D vtx = mesh.vertices.get(v);
            
            if (vtx == null || !v.equals(vtx)) {
                System.out.println("Invalid vertex a: key: " + v + " value: " + vtx);

                valid = false;
            }
            // check that the edge is in mesh

            for(WingedEdge edge : edges) {
                if (mesh.edges.get(edge) == null) {
                    System.out.println("Face contains invalid edge: " + edge + " face: " + face);
                    valid = false;
                }
            }

            v = (WEVertex) f.b;
            vtx = mesh.vertices.get(v);
            edges = v.edges;

            if (vtx == null || !v.equals(vtx)) {
                System.out.println("Invalid vertex b: key: " + v + " value: " + vtx);
                valid = false;
            }

            // check that the edge is in mesh

            for(WingedEdge edge : edges) {
                if (mesh.edges.get(edge) == null) {
                    System.out.println("Face contains invalid edge: " + edge + " face: " + face);
                    valid = false;
                }
            }

            v = (WEVertex) f.c;
            vtx = mesh.vertices.get(v);
            edges = v.edges;

            if (vtx == null || !v.equals(vtx)) {
                System.out.println("Invalid vertex c: key: " + v + " value: " + vtx);
                valid = false;
            }

            // check that the edge is in mesh

            for(WingedEdge edge : edges) {
                if (mesh.edges.get(edge) == null) {
                    System.out.println("Face contains invalid edge: " + edge + " face: " + face);
                    valid = false;
                }
            }

        }
        return valid;
    }

    private WingedEdge getEdge(int id, WETriangleMesh mesh) {
        for(WingedEdge edge : mesh.edges.values()) {
            if (edge.id == id) {
                return edge;
            }
        }
        
        return null;
    }

    private WingedEdge getBorderEdge(WETriangleMesh mesh, Set<WingedEdge> border) {
        for(WingedEdge edge : mesh.edges.values()) {
            if (edge.faces.size() != 2) {
                return edge;
            }
        }

        return null;
    }

    private WingedEdge getNonBorderEdge(WETriangleMesh mesh, Set<WingedEdge> border) {
        for(WingedEdge edge : mesh.edges.values()) {
            if (edge.faces.size() == 2) {
                boolean border_edge = false;

                for(WEFace face : edge.faces) {

                    for(WingedEdge e : face.edges) {
                        if (border.contains(e)) {
                            border_edge = true;
                        }
                    }

                }

                if (!border_edge) {
                    double max_edge_length = 400;

                    WEVertex va = (WEVertex) edge.a;
                    WEVertex vb = (WEVertex) edge.b;

                    double dx = va.x - vb.x;
                    double dy = va.y - vb.y;
                    double dz = va.z - vb.z;
                    double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);

                    if (distance > max_edge_length) {
                        continue;
                    }

                    return edge;
                }
            }
        }

        return null;
    }

    private void printEdgeDetails(WingedEdge edge) {
        System.out.println("Edge: " + edge);
        System.out.println("Faces: ");
        for(WEFace face : edge.faces) {
            System.out.println("   " + face);
        }
    }

    private void write(WETriangleMesh mesh) {
        try {
            HashMap<String,Object> params = new HashMap<String, Object>();
            params.put(SAVExporter.EXPORT_NORMALS, false);
    
            FileOutputStream fos = new FileOutputStream("/tmp/out.x3dv");
            
            PlainTextErrorReporter console = new PlainTextErrorReporter();
            
            BinaryContentHandler writer = new X3DClassicRetainedExporter(fos,3,0,console);
            
            ejectHeader(writer);

            SAVExporter exporter = new SAVExporter();
            exporter.outputX3D(mesh, params, writer);

            ejectFooter(writer);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Eject a header appropriate for the file.  This is all stuff that's not
     * specific to a grid.  In X3D terms this is PROFILE/COMPONENT and ant
     * NavigationInfo/Viewpoints desired.
     *
     */
    private void ejectHeader(BinaryContentHandler writer) {
        writer.startDocument("","", "utf8", "#X3D", "V3.0", "");
        writer.profileDecl("Immersive");
        writer.startNode("NavigationInfo", null);
        writer.startField("avatarSize");
        writer.fieldValue(new float[] {0.01f, 1.6f, 0.75f}, 3);
        writer.endNode(); // NavigationInfo

        // TODO: This should really be a lookat to bounds calc of the grid
        // In theory this would need all grids to calculate.  Not all
        // formats allow viewpoints to be intermixed with geometry

        writer.startNode("Viewpoint", null);
        writer.startField("position");
        writer.fieldValue(new float[] {0,2,7},3);
        //writer.startField("orientation");
        //writer.fieldValue(new float[] {-0.9757987f,0.21643901f,0.031161053f,0.2929703f},4);
        writer.endNode(); // Viewpoint
    }

    /**
     * Eject a footer for the file.
     *
     */
    private void ejectFooter(BinaryContentHandler writer) {
        writer.endDocument();
    }
    
}
