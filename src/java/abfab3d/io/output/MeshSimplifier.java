package abfab3d.io.output;

import toxi.geom.Line3D;
import toxi.geom.Vec3D;
import toxi.geom.mesh.WEFace;
import toxi.geom.mesh.WETriangleMesh;
import toxi.geom.mesh.WingedEdge;
import toxi.geom.mesh.WEVertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Mesh Simplifier.
 *
 *
 * @author Alan Hudson
 */
public class MeshSimplifier {
    private final Line3D edgeCheck = new Line3D(new Vec3D(), new Vec3D());

    /**
     * Execute the simplification algorithm
     *
     * @param mesh
     */
    public void execute(WETriangleMesh mesh) {
        ArrayList<WingedEdge> collapse_list = new ArrayList<WingedEdge>() ;
        
        for(WingedEdge edge : mesh.edges.values()) {
            if (canCollapse(edge)) {
                
                collapse_list.add(edge);
            }
        }
        
        ArrayList<WEFace> face_list = new ArrayList<WEFace>();
        
        for(WingedEdge edge : collapse_list) {
            face_list.clear();
            
            for(WEFace face : edge.faces) {
                face_list.add(face);
            }
            
            for(WEFace face : face_list) {
                mesh.removeFace(face);
            }

            // edge vertices get moved to some v'

            WEVertex va = (WEVertex) edge.a;
            WEVertex vb = (WEVertex) edge.b;

            // Choose center point of edge for new vertex location
            Vec3D mid = edge.getMidPoint();
            
            List<WEFace> lista = va.getRelatedFaces();
            List<WEFace> listb = va.getRelatedFaces();

            // Update faces with shared vertices to new v' location
            // TODO: Would rather just update vertex
            
            face_list.clear();

            for(WEFace face : lista) {
                face_list.add(face);
            }
            for(WEFace face : listb) {
                face_list.add(face);
            }

            for(WEFace face : face_list) {
                mesh.removeFace(face);
            }

            for(WEFace face : lista) {
                if (face.a.equals(va)) {
                    mesh.addFace(mid, face.b, face.c);
                } else if (face.b.equals(va)) {
                    mesh.addFace(face.a, mid, face.c);
                } else {
                    mesh.addFace(face.a, face.b, mid);
                }
            }

            for(WEFace face : listb) {
                if (face.a.equals(vb)) {
                    mesh.addFace(mid, face.b, face.c);
                } else if (face.b.equals(vb)) {
                    mesh.addFace(face.a, mid, face.c);
                } else {
                    mesh.addFace(face.a, face.b, mid);
                }
            }
        }
    }

    int count = 0;

    private boolean canCollapse(WingedEdge edge) {
        if (count < 1) {
            count++;
            return true;
        }

        return false;
    }

    /**
     * Copied from toxiclibs as its protected.
     *
     * @param mesh
     * @param e
     */
    protected void removeEdge(WETriangleMesh mesh, WingedEdge e) {
        e.remove();
        WEVertex v = (WEVertex) e.a;
        if (v.edges.size() == 0) {
            mesh.vertices.remove(v);
        }
        v = (WEVertex) e.b;
        if (v.edges.size() == 0) {
            mesh.vertices.remove(v);
        }
        for (WEFace f : e.faces) {
            mesh.removeFace(f);
        }


        WingedEdge removed = mesh.edges.remove(edgeCheck.set(e.a, e.b));
        if (removed != e) {
            throw new IllegalStateException("can't remove edge");
        }
    }

    /**
     * Calculate the quadratic error from collapsing an edge
     */
    private void calcError(WingedEdge edge, double[] error) {
    }
}
