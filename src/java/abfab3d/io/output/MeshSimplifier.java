package abfab3d.io.output;

import toxi.geom.Line3D;
import toxi.geom.Vec3D;
import toxi.geom.mesh.WEFace;
import toxi.geom.mesh.WETriangleMesh;
import toxi.geom.mesh.WingedEdge;
import toxi.geom.mesh.WEVertex;

import javax.vecmath.Vector3d;
import java.util.*;

/**
 * Mesh Simplifier.
 *
 *
 * @author Alan Hudson
 */
public class MeshSimplifier {
    private static final boolean DEBUG = false;

    private final Line3D edgeCheck = new Line3D(new Vec3D(), new Vec3D());

    /**
     * Execute the simplification algorithm
     *
     * @param mesh
     */
    public void execute(WETriangleMesh mesh) {
        ArrayList<WingedEdge> collapse_list = new ArrayList<WingedEdge>() ;
        
        int size = 1;
        int pass = 0;
//        int max_pass = 1000;
        int max_pass = 1000;

        while(size > 0) {
            System.out.println("Starting pass: " + pass + " edges: " + mesh.getNumEdges());
            collapse_list.clear();
            if (pass > max_pass) {
                break;
            }
            for(WingedEdge edge : mesh.edges.values()) {
                if (canCollapse(edge)) {
                    
                    collapse_list.add(edge);
                }
            }
            
            // TODO: Sort edge list by id for determinism.  Can remove later
            Collections.sort(collapse_list, new EdgeSorter());
            size = collapse_list.size();
            ArrayList<WEFace> face_list = new ArrayList<WEFace>();

            System.out.println("Collapsable Edges: " + collapse_list.size() + " total: " + mesh.getNumEdges());
            int edges_removed = 0;
            
            for(WingedEdge edge : collapse_list) {
if (DEBUG) System.out.println("Collapse edge: " + edge);
                face_list.clear();
                
                for(WEFace face : edge.faces) {
if (DEBUG) System.out.println("   edge face: " + face);
                    face_list.add(face);
                }

                if (face_list.size() != 2) {
                    // Edge likely has already been removed during another collapse
                    continue;
                }
                //System.out.println("edges0: " + mesh.getNumEdges());
                //System.out.println("Edge faces: " + face_list.size());

                for(WEFace face : face_list) {
                    mesh.removeFace(face);
                    if (DEBUG) printFace(face.a,face.b,face.c, new float[] {1,0,0});
                }

                //System.out.println("edges1: " + mesh.getNumEdges());

                // edge vertices get moved to some v'
    
                WEVertex va = (WEVertex) edge.a;
                WEVertex vb = (WEVertex) edge.b;
    
                // Choose center point of edge for new vertex location
                Vec3D mid = edge.getMidPoint();

/*
                if (edge.id % 2 == 0) {
                    mid = va;
                } else {
                    mid = vb;
                }
  */
                List<WEFace> lista = va.getRelatedFaces();
                List<WEFace> listb = vb.getRelatedFaces();

                //System.out.println("Related faces: " + lista.size() + " " + listb.size());
                // Update faces with shared vertices to new v' location
                // TODO: Would rather just update vertex
                
                face_list.clear();
    
                for(WEFace face : lista) {
//System.out.println("   related: " + face);
                    face_list.add(face);
                }
                for(WEFace face : listb) {
//                    System.out.println("   related: " + face);
                    face_list.add(face);
                }

                Vec3D[] normals = new Vec3D[face_list.size()];
                int idx = 0;

                for(WEFace face : face_list) {
                    if (DEBUG) printFace(face.a,face.b,face.c, new float[] {0,0,1});
                    normals[idx++] = face.normal;
                    mesh.removeFace(face);
                }

                //System.out.println("edges2: " + mesh.getNumEdges());

                idx = 0;
                for(WEFace face : lista) {
                    if (face.a.equals(va)) {
                        if (DEBUG) printFace(mid, face.b, face.c, new float[] {0,1,0});
                        mesh.addFace(mid, face.b, face.c, normals[idx++]);
                    } else if (face.b.equals(va)) {
                        if (DEBUG) printFace(face.a, mid, face.c, new float[] {0,1,0});
                        mesh.addFace(face.a, mid, face.c,normals[idx++]);
                    } else if (face.c.equals(va)){
                        if (DEBUG) printFace(face.a, face.b, mid, new float[] {0,1,0});
                        mesh.addFace(face.a, face.b, mid,normals[idx++]);
                    } else {
                        System.out.println("ERROR: why here?");
                    }
                }
    
                for(WEFace face : listb) {
                    if (face.a.equals(vb)) {
                        if (DEBUG) printFace(mid, face.b, face.c, new float[] {0,1,0});
                        mesh.addFace(mid, face.b, face.c,normals[idx++]);
                    } else if (face.b.equals(vb)) {
                        if (DEBUG) printFace(face.a, mid, face.c, new float[] {0,1,0});
                        mesh.addFace(face.a, mid, face.c,normals[idx++]);
                    } else if (face.c.equals(vb)){
                        if (DEBUG) printFace(face.a, face.b, mid, new float[] {0,1,0});
                        mesh.addFace(face.a, face.b, mid,normals[idx++]);
                    } else {
                        System.out.println("ERROR: why here?");
                    }
                }

                //System.out.println("edges3: " + mesh.getNumEdges());
                edges_removed++;

                // TODO: not sure this is needed, face removal does it.
                //removeEdge(mesh, edge);
                if (edges_removed % 10000 == 0) {
                    System.out.println("   edge count: " + edges_removed);
                }
            }
            
            System.out.println("Pass done: edges removed: " + edges_removed + " edges left: " + mesh.getNumEdges());
            
            if (edges_removed == 0) {
                System.out.println("Terminate.");
                break;
            }
            //mesh.rebuildIndex();
            pass++;
        }
        
        // TODO: Relies on custom change to WETriangleMesh on removeEdge, fix
    }

    int count = 0;

    private boolean canCollapse2(WingedEdge edge) {

        if (count > 0) {
            return false;
        }

        count++;
        WEVertex va = (WEVertex) edge.a;
        WEVertex vb = (WEVertex) edge.b;

        Vec3D face1 = edge.faces.get(0).normal;
        Vec3D face2 = edge.faces.get(1).normal;

        System.out.println("norm1: " + face1);
        System.out.println("norm2: " + face2);
        Vec3D cross = face1.cross(face2);

        return cross.isZeroVector();
    }

    private boolean canCollapse(WingedEdge edge) {
        double max_edge_length = 3;

        count++;

/*
        // > 490 --> 650      --> shows first cross of center
        if (edge.id < 490 || edge.id > 655) {
            return false;
        }
  */
        WEVertex va = (WEVertex) edge.a;
        WEVertex vb = (WEVertex) edge.b;

        double dx = va.x - vb.x;
        double dy = va.y - vb.y;
        double dz = va.z - vb.z;
        double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
        
        if (distance > max_edge_length) {
            return false;
        }

        List<WEFace> lista = va.getRelatedFaces();
        List<WEFace> listb = vb.getRelatedFaces();


        Vec3D face_normal = edge.faces.get(0).normal;

        for(WEFace face : lista)    {
            Vec3D cross = face_normal.cross(face.normal);
            if (!cross.isZeroVector())
                return false;
        }

        for(WEFace face : listb)    {
            Vec3D cross = face_normal.cross(face.normal);
            if (!cross.isZeroVector())
                return false;
        }

        return true;
    }
    
    
    /**
     * Copied from toxiclibs as its protected.
     *
     * @param mesh
     * @param e
     */
    protected void removeEdge(WETriangleMesh mesh, WingedEdge e) {
        e.remove();
// TODO: vertex removal doesn't work right as faces used vertexID as index into coordinate array
/*
        WEVertex v = (WEVertex) e.a;
        if (v.edges.size() == 0) {
            mesh.vertices.remove(v);
        }
        v = (WEVertex) e.b;
        if (v.edges.size() == 0) {
            mesh.vertices.remove(v);
        }
*/
        for (WEFace f : e.faces) {
            mesh.removeFace(f);
        }


System.out.println("remove: " + e);
        WingedEdge removed = mesh.edges.remove(edgeCheck.set(e.a, e.b));
        
        if (removed == null) {
            removed = mesh.edges.remove(edgeCheck.set(e.b, e.a));
        }
        if (removed != e) {
            throw new IllegalStateException("can't remove edge: " + e);
        }
    }

    private void printFace(Vec3D a, Vec3D b, Vec3D c, float[] color) {
        System.out.println("Shape { " + "appearance Appearance { material Material { diffuseColor " + java.util.Arrays.toString(color) +
                " } } \n geometry IndexedTriangleSet { index [0 1 2] coord Coordinate { point [");
        System.out.println("   " + a.x() + " " + a.y() + " " + a.z() + ",");
        System.out.println("   " + b.x() + " " + b.y() + " " + b.z() + ",");
        System.out.println("   " + c.x() + " " + c.y() + " " + c.z());
        System.out.println("] } } }");
        /*
        System.out.print("va: " + a);
        System.out.print(" vb: " + b);
        System.out.println(" vc: " + c);
        */
    }
}

class EdgeSorter implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        WingedEdge we1 = (WingedEdge) o1;
        WingedEdge we2 = (WingedEdge) o2;

        return we1.id - we2.id;
    }
}