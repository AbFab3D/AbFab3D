package abfab3d.io.output;

import abfab3d.core.Grid;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.export.X3DBinaryRetainedDirectExporter;
import org.web3d.vrml.export.X3DBinarySerializer;
import org.web3d.vrml.sav.BinaryContentHandler;
import toxi.geom.Line3D;
import toxi.geom.Vec3D;
import toxi.geom.mesh.*;
import toxi.math.MathUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Mesh Simplifier.
 *
 * TODO: Marching Cubes can generate really short edges, add a first pass that removes those?
 *
 * @author Alan Hudson
 */
public class EdgeCollapseSimplifier implements MeshSimplifier {
    private static final boolean DEBUG = false;

    private double maxDistance = 0.25;
    private double maxAngle = 0.75;

    public EdgeCollapseSimplifier() {
        this(0.25, 0.75);
    }

    public EdgeCollapseSimplifier(double maxDistance, double maxAngle) {
        this.maxDistance = maxDistance;
        this.maxAngle = maxAngle;
    }


    public void executeRandom(WETriangleMesh mesh, Grid grid) {
        ArrayList<WingedEdge> collapse_list = new ArrayList<WingedEdge>() ;

        int size = 1;
        int pass = 0;
//        int max_pass = 1000;
        int max_pass = 1500;
        int max_edges = Integer.MAX_VALUE;

        long edges_removed = 0;

        loop:
        for(int i=0; i < max_pass; i++) {
            while(true) {
                WingedEdge[] edges = new WingedEdge[mesh.edges.size()];
                edges = (WingedEdge[]) mesh.edges.values().toArray(edges);

                WingedEdge edge = getBestEdge(edges, mesh);

                if (edge == null) {
                    break;
                }

//                collapseEdge3(edge, (WEVertex) edge.a, mesh);
                collapseEdge2(edge, (WEVertex) edge.a, mesh);

                edges_removed++;

                if (edges_removed >= max_edges)  {
                    break loop;
                }
                if (edges_removed % 1000 == 0) {
                    System.out.println("   edge count: " + edges_removed);
                }
            }
            System.out.println("Pass number: " + i);
        }
    }
    
    private WingedEdge getBestEdge(WingedEdge[] edges, WETriangleMesh mesh) {
        int sample = 100;
        
        for(int i=0; i < sample; i++) {

            // TODO: this has a synchronized block in it, likely bad for multithreading
            int num = (int) (Math.random() * edges.length);

            WingedEdge edge = edges[num];
            
            Vec3D mid_p = getMidPoint(edge.a, edge.b);
            WEVertex mid = new WEVertex(mid_p,999999999);

            if (canCollapseRelatedMinDot(edge,0.8f) && canCollapseDistance(edge, maxDistance) &&
                    canCollapseNoNonManifoldEdges(edge, mid, mesh)) {
                return edge;               
            }                        
        }
        
        return null;
    }
    
    /**
     * Execute the simplification algorithm
     *
     * @param mesh
     */
    public void execute(WETriangleMesh mesh, Grid grid) {
        ArrayList<WingedEdge> collapse_list = new ArrayList<WingedEdge>() ;
        
        int size = 1;
        int pass = 0;
//        int max_pass = 1000;
        int max_pass = 100;
        int max_edges = Integer.MAX_VALUE;
        boolean display_edges = false;
        BinaryContentHandler stream = null;

        if (display_edges) {
            try {
                PlainTextErrorReporter console = new PlainTextErrorReporter();
                
                FileOutputStream fos = new FileOutputStream("/tmp/viz.x3db");
                stream = new X3DBinaryRetainedDirectExporter(fos,
                        3, 0, console,
                        X3DBinarySerializer.METHOD_FASTEST_PARSING,
                        0.001f, true);

                stream.startDocument("","", "utf8", "#X3D", "V3.0", "");
                stream.profileDecl("Immersive");

            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        loop: while(size > 0) {
            if (DEBUG) System.out.println("Starting pass: " + pass + " edges: " + mesh.getNumEdges());
            collapse_list.clear();
            if (pass >= max_pass) {
                break;
            }


            for(WingedEdge edge : mesh.edges.values()) {
                    // TODO: This is the best so far, Coplanar might be ok as well
//                if (canCollapseRelatedCoplanar(edge) && canCollapseDistance(edge,maxDistance)) {
//                if (canCollapseRelatedMinDot(edge,0.8f) && canCollapseDistance(edge, maxDistance)) {

                
                
//                WEVertex mid = (WEVertex) edge.a;
                Vec3D mid_p = getMidPoint(edge.a, edge.b);
                WEVertex mid = new WEVertex(mid_p,999999999);

                if (canCollapse(edge, mid, mesh)) {

                    collapse_list.add(edge);
                    
                    if (display_edges) {
                        ejectSegment(stream, new float[] {edge.a.x, edge.a.y, edge.a.z,}, 
                                new float[] {edge.b.x, edge.b.y, edge.b.z},null);
                    }
                }                                
            }

            // TODO: Sort edge list by id for determinism.  Can remove later
            Collections.sort(collapse_list, new EdgeSorter());
            size = collapse_list.size();
            ArrayList<WEFace> face_list = new ArrayList<WEFace>();

            if (DEBUG) System.out.println("Collapsable Edges: " + collapse_list.size() + " total: " + mesh.getNumEdges());
            int edges_removed = 0;
            
            for(WingedEdge edge : collapse_list) {
                if (edge.faces.size() != 2) {
                    // Edge likely has already been removed during another collapse
                    continue;
                }
                if (DEBUG) System.out.println("Collapse edge: " + edge);

                Vec3D mid_p = getMidPoint(edge.a, edge.b);
                WEVertex mid = new WEVertex(mid_p,999999999);

                // Reverify edge
                if (!canCollapse(edge, mid, mesh)) {
                    continue;
                }


                collapseEdge2(edge, (WEVertex) mid, mesh);
//                collapseEdge3(edge, (WEVertex) mid, mesh);


/*
                boolean valid = validateMesh(mesh, null);

                if (!valid) {
                    System.out.println("****Mesh now invalid.  Collapsed edge: " + edge);
                    throw new IllegalArgumentException("stop");
                }

 */
                edges_removed++;

                if (edges_removed >= max_edges)  {
                    break loop;
                }
                if (edges_removed % 10000 == 0) {
                    if (DEBUG) System.out.println("   edge count: " + edges_removed);
                }
            }

            if (DEBUG) System.out.println("Pass done: edges removed: " + edges_removed + " edges left: " + mesh.getNumEdges());
            
            if (edges_removed == 0) {
                System.out.println("Terminate.");
                break;
            }
            //mesh.rebuildIndex();
            pass++;
        }

        if (display_edges) {
            stream.endDocument();
        }
    }

    private boolean canCollapse(WingedEdge edge, WEVertex mid, WETriangleMesh mesh) {
        return (canCollapseRelatedMinDot(edge,maxAngle) &&
                canCollapseDistance(edge, maxDistance) &&
                canCollapseNoNonManifoldEdges(edge, mid, mesh));

//                if (canCollapseRelatedCoplanar(edge) && canCollapseDistance(edge,maxDistance)) {
//                if (canCollapseRelatedCoplanar(edge) && canCollapseEdgesInsideGrid(edge, 1, grid)) {
//                if (canCollapseRelatedCoplanar(edge) && canCollapseMidPoint(edge, 1, grid)) {
//                if (canCollapseRelatedCoplanar(edge)) {
//                if (canCollapseRelatedAxis(edge) && canCollapseMidPoint(edge, 1, grid)) {
//                if (canCollapseRelatedCoplanar(edge)) {
//                if (canCollapseCoplanar(edge)) {

        
    }
    
    protected void collapseEdge(WingedEdge edge, WETriangleMesh mesh) {
        ArrayList<WEFace> face_list = new ArrayList<WEFace>(2);

        for(WEFace face : edge.faces) {
            if (DEBUG) System.out.println("   edge face: " + face);
            face_list.add(face);
        }

        
        WEVertex va = (WEVertex) edge.a;
        WEVertex vb = (WEVertex) edge.b;
        
        System.out.println("vertexa:" + va);
        System.out.println("neigbors a: size: " + va.getNeighbors().size() + " --> " + va.getNeighbors());
        System.out.println("related a: size: " + va.getRelatedFaces().size() + " --> " + va.getRelatedFaces());

        System.out.println("vertexb:" + vb);
        System.out.println("neigbors b: size: " + vb.getNeighbors().size() + " --> " + vb.getNeighbors());
        System.out.println("related b: size: " + vb.getRelatedFaces().size() + " --> " + vb.getRelatedFaces());

        List<WEFace> relatedb = vb.getRelatedFaces();  // This change during collapse so save

        WingedEdge[][] collapses = new WingedEdge[2][2];

        int f_idx = 0;
        // Find collapsing edges before deletion
        for(WEFace face : face_list) {        // faces b2,b4
            int e_idx = 0;
            
            for(WingedEdge e : face.edges) {
                if (!e.equals(edge)) {
                    collapses[f_idx][e_idx++] = e;
                }
            }
            f_idx++;
        }

        // Choose center point of edge for new vertex location
        WEVertex mid = mesh.checkVertex(getMidPoint(edge.a, edge.b));

        mesh.removeEdge(edge);
        WingedEdge e1 = null,e2;

        // Fix up edges that went to 1 (e1,e2,e3,e4)
        for(int i=0; i < collapses.length; i++) {

            e1 = collapses[i][0];
            e2 = collapses[i][1];
        
            System.out.println("Commonizing edges: e1: " + e1 + " e2: " + e2);           
            WEFace e2_face = null;

            e2_face =  e2.faces.get(0);          // b1

            System.out.println("e2_face: " + e2_face);

            // commonize to e1
            e1.addFace(e2_face);

            e2_face.edges.remove(e2);
            System.out.println("Adding edge to e2_face: " + e1);
            e2_face.addEdge(e1);

            // fix up vertices
            //System.out.println("removing edge from verts: " + e2);
            //System.out.println("edges before: size: " + ((WEVertex)e2_face.a).edges.size() + " " + ((WEVertex)e2_face.a).edges);
            //System.out.println("edges before: size: " + ((WEVertex)e2_face.b).edges.size() + " " + ((WEVertex)e2_face.b).edges);
            //System.out.println("edges before: size: " + ((WEVertex)e2_face.c).edges.size() + " " + ((WEVertex)e2_face.c).edges);
            ((WEVertex)e2_face.a).removeEdge(e2);
            ((WEVertex)e2_face.b).removeEdge(e2);
            ((WEVertex)e2_face.c).removeEdge(e2);
            //System.out.println("edges after: size: " + ((WEVertex)e2_face.a).edges.size() + " " + ((WEVertex)e2_face.a).edges);
            //System.out.println("edges after: size: " + ((WEVertex)e2_face.b).edges.size() + " " + ((WEVertex)e2_face.b).edges);
            //System.out.println("edges after: size: " + ((WEVertex)e2_face.c).edges.size() + " " + ((WEVertex)e2_face.c).edges);

            e2.faces.clear();

            mesh.removeEdge(e2);
        }

        // Folded edges are now delinked its safe to move vertices


        // Add edges to new vertex.  All related verts of edge.a and edge.b except collapses
        mid.edges.addAll(va.edges);
        mid.edges.addAll(vb.edges);

        for(WEFace face : va.getRelatedFaces()) {
            System.out.println("va relface: " + face);
            if (face.a.equals(va)) {
                for(WingedEdge e : face.edges) {
                    System.out.println("Move edge: " + e);
                    if (e.a.equals(face.a)) {
                        System.out.println("Update vert: " + e.a);
                        mesh.edges.remove(e);
                        WEVertex v = ((WEVertex)face.a);
                        v.edges.remove(e);
                        e.a = mid;
                        mesh.edges.put(e, e);
                        v.edges.add(e);
                    }
                    if (e.b.equals(face.a)) {
                        System.out.println("Update vert: " + e.b);
                        mesh.edges.remove(e);
                        WEVertex v = ((WEVertex)face.a);
                        v.edges.remove(e);
                        e.b = mid;
                        mesh.edges.put(e, e);
                        v.edges.add(e);
                    }
                }
                face.a = mid;
            } else if (face.b.equals(va)) {
                System.out.println("vb relface: " + face);

                for(WingedEdge e : face.edges) {
                    System.out.println("Move edge: " + e);
                    if (e.a.equals(face.b)) {
                        System.out.println("Update vert: " + e.a);
                        mesh.edges.remove(e);
                        WEVertex v = ((WEVertex)face.a);
                        v.edges.remove(e);
                        e.a = mid;
                        mesh.edges.put(e, e);
                        v.edges.add(e);
                    }
                    if (e.b.equals(face.b)) {
                        System.out.println("Update vert: " + e.b);
                        mesh.edges.remove(e);
                        WEVertex v = ((WEVertex)face.a);
                        v.edges.remove(e);
                        e.b = mid;
                        mesh.edges.put(e, e);
                        v.edges.add(e);
                    }
                }

                face.b = mid;
            } else if (face.c.equals(va)) {
                System.out.println("vc relface: " + face);

                for(WingedEdge e : face.edges) {
                    System.out.println("Move edge: " + e);
                    if (e.a.equals(face.c)) {
                        System.out.println("Update vert: " + e.a + " a: " + e.a + " c: " + face.c);
                        mesh.edges.remove(e);
                        WEVertex v = ((WEVertex)face.a);
                        v.edges.remove(e);
                        e.a = mid;
                        mesh.edges.put(e, e);
                        v.edges.add(e);
                    }
                    if (e.b.equals(face.c)) {
                        System.out.println("Update vert: " + e.b + " b: " + e.b + " c: " + face.c);
                        mesh.edges.remove(e);
                        WEVertex v = ((WEVertex)face.a);
                        v.edges.remove(e);
                        e.b = mid;
                        mesh.edges.put(e, e);
                        v.edges.add(e);
                    }
                }

                face.c = mid;
            } else {
                System.out.println("Can't find shared vertex? " + va);
            }
        }


        System.out.println("***Fixing up vertex b related");

        for(WEFace face : relatedb) {
            System.out.println("va relface: " + face);
            if (face.a.equals(vb)) {
                for(WingedEdge e : face.edges) {
                    System.out.println("Move edge: " + e);
                    if (e.a.equals(face.a)) {
                        System.out.println("Update vert: " + e.a);
                        mesh.edges.remove(e);
                        WEVertex v = ((WEVertex)face.a);
                        v.edges.remove(e);
                        e.a = mid;
                        mesh.edges.put(e, e);
                        v.edges.add(e);
                    }
                    if (e.b.equals(face.a)) {
                        System.out.println("Update vert: " + e.b);
                        mesh.edges.remove(e);
                        WEVertex v = ((WEVertex)face.a); 
                        v.edges.remove(e);
                        e.b = mid;
                        mesh.edges.put(e, e);
                        v.edges.add(e);
                    }
                }
                face.a = mid;
            } else if (face.b.equals(vb)) {
                System.out.println("va relface: " + face);

                for(WingedEdge e : face.edges) {
                    System.out.println("Move edge: " + e);
                    if (e.a.equals(face.b)) {
                        System.out.println("Update vert: " + e.a);
                        mesh.edges.remove(e);
                        WEVertex v = ((WEVertex)face.a);
                        v.edges.remove(e);
                        e.a = mid;
                        mesh.edges.put(e, e);
                        v.edges.add(e);
                    }
                    if (e.b.equals(face.b)) {
                        System.out.println("Update vert: " + e.b);
                        mesh.edges.remove(e);
                        WEVertex v = ((WEVertex)face.a);
                        v.edges.remove(e);
                        e.b = mid;
                        mesh.edges.put(e, e);
                        v.edges.add(e);
                    }
                }

                face.b = mid;
            } else if (face.c.equals(vb)) {
                System.out.println("vc relface: " + face);

                for(WingedEdge e : face.edges) {
                    System.out.println("Move edge: " + e);
                    if (e.a.equals(face.c)) {
                        System.out.println("Update vert: " + e.a + " a: " + e.a + " c: " + face.c);
                        mesh.edges.remove(e);
                        WEVertex v = ((WEVertex)face.a);
                        v.edges.remove(e);
                        e.a = mid;
                        mesh.edges.put(e, e);
                        v.edges.add(e);
                    }
                    if (e.b.equals(face.c)) {
                        System.out.println("Update vert: " + e.b + " b: " + e.b + " c: " + face.c);
                        mesh.edges.remove(e);
                        WEVertex v = ((WEVertex)face.a);
                        v.edges.remove(e);
                        e.b = mid;
                        mesh.edges.put(e, e);
                        v.edges.add(e);
                    }
                }

                face.c = mid;
            } else {
                System.out.println("Can't find shared vertex? " + va);
            }
        }


        mesh.edges.put(e1,e1);

    }

    /**
     * Edge collapsed based on remove and re-add.
     * @param edge
     * @param mesh
     */
    protected void collapseEdge2(WingedEdge edge, WEVertex mid, WETriangleMesh mesh) {
        int orig_verts = mesh.vertices.size();
        
        // Choose center point of edge for new vertex location
//        WEVertex mid = mesh.checkVertex(getMidPoint(edge.a, edge.b));

        // TODO: meshlab suggests using a point on the surface is better then the mid.

        List<WEFace> orig_faces_list = edge.getFaces();

/*
        System.out.println("Removing faces:");
        for(WEFace face : orig_faces_list) {
            System.out.println(face);
        }
 */
        if (DEBUG) System.out.println("Removing edge: " + edge);
        mesh.removeEdge(edge);

        WEVertex va = (WEVertex) edge.a;
        WEVertex vb = (WEVertex) edge.b;
        List<WEFace> lista = va.getRelatedFaces();
        List<WEFace> listb = vb.getRelatedFaces();


        int idx = 0;

        float[] anormals = new float[lista.size() * 3];
        float[] bnormals = new float[listb.size() * 3];
        boolean use_normals = false; //  TODO: this should be required but it breaks things


        int orig_faces = mesh.faces.size();
        int faces = lista.size() + listb.size();
        if (DEBUG) System.out.println("Removing faces  a: " + lista.size() + " b: " + listb.size());
        for(WEFace face : lista) {
            if (use_normals) {
                anormals[idx++] = face.normal.x;
                anormals[idx++] = face.normal.y;
                anormals[idx++] = face.normal.z;
            }
            
            if (DEBUG) System.out.println("   " + face);
            mesh.removeFace(face);
        }

        idx = 0;
        for(WEFace face : listb) {
            if (use_normals) {
                bnormals[idx++] = face.normal.x;
                bnormals[idx++] = face.normal.y;
                bnormals[idx++] = face.normal.z;
            }

            if (DEBUG) System.out.println("   " + face);
            mesh.removeFace(face);
        }

        if (orig_faces - mesh.faces.size() != faces) {
            System.out.println("ERROR: wrong face count.  orig: " + orig_faces + " to_remove: " + faces + " curr: " + mesh.faces.size());
        }

        idx = 0;
        for(WEFace face : lista) {
            if (face.a.equals(va)) {
                if (DEBUG) printFace(mid, face.b, face.c, new float[] {0,1,0});
                if (use_normals) {
//                    mesh.addFace(mid, face.b, face.c, new Vec3D(anormals[idx++],anormals[idx++],anormals[idx++]));
                    mesh.addFace(mid, face.b, face.c, face.normal);
                } else {
                    mesh.addFace(mid, face.b, face.c);
                }
            } else if (face.b.equals(va)) {
                if (DEBUG) printFace(face.a, mid, face.c, new float[] {0,1,0});
                if (use_normals) {
//                    mesh.addFace(face.a, mid, face.c,new Vec3D(anormals[idx++],anormals[idx++],anormals[idx++]));
                    mesh.addFace(face.a, mid, face.c,face.normal);
                } else {
                    mesh.addFace(face.a, mid, face.c);
                }
            } else if (face.c.equals(va)){
                if (DEBUG) printFace(face.a, face.b, mid, new float[] {0,1,0});
                if (use_normals) {
//                    mesh.addFace(face.a, face.b, mid,new Vec3D(anormals[idx++],anormals[idx++],anormals[idx++]));
                    mesh.addFace(face.a, face.b, mid,face.normal);
                } else {
                    mesh.addFace(face.a, face.b, mid);
                }
            } else {
                System.out.println("ERROR: why here?");
            }
        }

        idx = 0;
        for(WEFace face : listb) {
            if (face.a.equals(vb)) {
                if (DEBUG) printFace(mid, face.b, face.c, new float[] {0,1,0});
                if (use_normals) {
//                    mesh.addFace(mid, face.b, face.c, new Vec3D(bnormals[idx++],bnormals[idx++],bnormals[idx++]));
                    mesh.addFace(mid, face.b, face.c, face.normal);
                } else {
                    mesh.addFace(mid, face.b, face.c);
                }
            } else if (face.b.equals(vb)) {
                if (DEBUG) printFace(face.a, mid, face.c, new float[] {0,1,0});
                if (use_normals) {
//                    mesh.addFace(face.a, mid, face.c, new Vec3D(bnormals[idx++],bnormals[idx++],bnormals[idx++]));
                    mesh.addFace(face.a, mid, face.c, face.normal);
                } else {
                    mesh.addFace(face.a, mid, face.c);
                }
            } else if (face.c.equals(vb)){
                if (DEBUG) printFace(face.a, face.b, mid, new float[] {0,1,0});
                if (use_normals) {
//                    mesh.addFace(face.a, face.b, mid, new Vec3D(bnormals[idx++],bnormals[idx++],bnormals[idx++]));
                    mesh.addFace(face.a, face.b, mid, face.normal);
                } else {
                    mesh.addFace(face.a, face.b, mid);
                }
            } else {
                System.out.println("ERROR: why here?");
            }
        }
 
        if (orig_faces != mesh.faces.size()) {
            System.out.println("ERROR: not all faces restored.  orig: " + orig_faces + " now: " + mesh.faces.size());
        }
        int new_verts = mesh.vertices.size();


        if (orig_verts - new_verts != 1) {
            System.out.println("ERROR: vert count off: orig_verts: " + orig_verts + " new_verts: " + new_verts);
        }

        // TODO: We should remove va and vb.  But WETriangleMesh doesn't support this
    }

    /**
     * Edge collapsed based on remove and re-add.
     * @param edge
     * @param mesh
     */
    protected void collapseEdge3(WingedEdge edge, WEVertex mid, WETriangleMesh mesh) {
        int orig_verts = mesh.vertices.size();

        // Choose center point of edge for new vertex location
//        WEVertex mid = mesh.checkVertex(getMidPoint(edge.a, edge.b));

        // TODO: meshlab suggests using a point on the surface is better then the mid.

        List<WEFace> orig_faces_list = new ArrayList<WEFace>();
        
        orig_faces_list.addAll(edge.getFaces());

        if (edge.getFaces().size() != 2) {
            System.out.println("Collapsing non-manifold edge!");
            throw new IllegalArgumentException("faces wrong: " + edge.getFaces().size());
        }
/*
        System.out.println("Removing faces:");
        for(WEFace face : orig_faces_list) {
            System.out.println(face);
        }
*/
        if (DEBUG) System.out.println("Removing edge: " + edge);
        mesh.removeEdge(edge);

        WEVertex va = (WEVertex) edge.a;
        WEVertex vb = (WEVertex) edge.b;
        List<WEFace> lista = va.getRelatedFaces();
        List<WEFace> listb = vb.getRelatedFaces();


        int idx = 0;

        float[] anormals = new float[lista.size() * 3];
        float[] bnormals = new float[listb.size() * 3];
        boolean use_normals = true; //  TODO: sometimes this helps, other times it doesnt


        int orig_faces = mesh.faces.size();
        int faces = lista.size() + listb.size();
        if (DEBUG) System.out.println("Removing faces  a: " + lista.size() + " b: " + listb.size());
        for(WEFace face : lista) {
            anormals[idx++] = face.normal.x;
            anormals[idx++] = face.normal.y;
            anormals[idx++] = face.normal.z;

            orig_faces_list.add(face);
            if (DEBUG) System.out.println("   " + face);
            mesh.removeFace(face);
        }

        idx = 0;
        for(WEFace face : listb) {
            bnormals[idx++] = face.normal.x;
            bnormals[idx++] = face.normal.y;
            bnormals[idx++] = face.normal.z;
            if (DEBUG) System.out.println("   " + face);
            orig_faces_list.add(face);
            mesh.removeFace(face);
        }

        if (orig_faces - mesh.faces.size() != faces) {
            System.out.println("ERROR: wrong face count.  orig: " + orig_faces + " to_remove: " + faces + " curr: " + mesh.faces.size());
        }

        boolean failed = false;
        WEFace failure = null;
        
        ArrayList<WEFace> added_faces = new ArrayList<WEFace>(lista.size() + listb.size());
        
        add: {
            idx = 0;
            for(WEFace face : lista) {
                if (face.a.equals(va)) {
                    if (use_normals) {
                        WEFace f = mesh.addFaceManifold(mid, face.b, face.c, new Vec3D(anormals[idx++],anormals[idx++],anormals[idx++]));
                        
                        if (f != null) {
                            added_faces.add(f);
                        } else {
                            failed = true;  
                            failure = new WEFace((WEVertex) mid, (WEVertex) face.b, (WEVertex) face.c);
                            break add;
                        }
                    }
                } else if (face.b.equals(va)) {
                    if (use_normals) {
                        WEFace f = mesh.addFaceManifold(face.a, mid, face.c,new Vec3D(anormals[idx++],anormals[idx++],anormals[idx++]));

                        if (f != null) {
                            added_faces.add(f);
                        } else {
                            failed = true;
                            failure = new WEFace((WEVertex) face.a, (WEVertex) mid, (WEVertex) face.c);
                            break add;
                        }                        
                    }
                } else if (face.c.equals(va)){
                    if (use_normals) {
                        WEFace f = mesh.addFaceManifold(face.a, face.b, mid,new Vec3D(anormals[idx++],anormals[idx++],anormals[idx++]));

                        if (f != null) {
                            added_faces.add(f);
                        } else {
                            failed = true;
                            failure = new WEFace((WEVertex) face.a, (WEVertex) face.b, (WEVertex) mid);
                            break add;
                        }
                    }
                } else {
                    System.out.println("ERROR: why here?");
                }
            }
    
            idx = 0;
            for(WEFace faceb : listb) {
                if (faceb.a.equals(vb)) {
                    if (use_normals) {
                        WEFace f = mesh.addFaceManifold(mid, faceb.b, faceb.c, new Vec3D(bnormals[idx++],bnormals[idx++],bnormals[idx++]));
                        if (f != null) {
                            added_faces.add(f);
                        } else {
                            failed = true;
                            failure = new WEFace((WEVertex) mid, (WEVertex) faceb.b, (WEVertex) faceb.c);
                            break add;
                        }
                    }
                } else if (faceb.b.equals(vb)) {
                    if (use_normals) {
                        WEFace f = mesh.addFaceManifold(faceb.a, mid, faceb.c,new Vec3D(bnormals[idx++],bnormals[idx++],bnormals[idx++]));
                        if (f != null) {
                            added_faces.add(f);
                        } else {
                            failed = true;
                            failure = new WEFace((WEVertex) faceb.a, (WEVertex) mid, (WEVertex) faceb.c);
                            break add;
                        }
                    }
                } else if (faceb.c.equals(vb)){
                    if (use_normals) {
                        WEFace f = mesh.addFaceManifold(faceb.a, faceb.b, mid,new Vec3D(bnormals[idx++],bnormals[idx++],bnormals[idx++]));
                        if (f != null) {
                            added_faces.add(f);
                        } else {
                            failed = true;
                            failure = new WEFace((WEVertex) faceb.a, (WEVertex) faceb.b, (WEVertex) mid);
                            break add;
                        }
                    }
                } else {
                    System.out.println("ERROR: why here?");
                }
            }

        }        
        
        if (failed) {
            
/*            
            System.out.println("****Operation failed manifold.  Rollback");
            for(WEFace f : added_faces) {
                mesh.removeFace(f);
            }
            
            System.out.println("Readding faces");
            for(WEFace f : orig_faces_list) {
                mesh.addFace(f.a,f.b,f.c);
            }
            
            System.out.println("Rollback done");
*/

            BinaryContentHandler stream = null;
            FileOutputStream fos = null;

            try {
                PlainTextErrorReporter console = new PlainTextErrorReporter();

                fos = new FileOutputStream("/tmp/viz.x3db");
                stream = new X3DBinaryRetainedDirectExporter(fos,
                        3, 0, console,
                        X3DBinarySerializer.METHOD_FASTEST_PARSING,
                        0.001f, true);

                stream.startDocument("","", "utf8", "#X3D", "V3.0", "");
                stream.profileDecl("Immersive");

            } catch(Exception e) {
                e.printStackTrace();
            }

            float[] red = new float[] {1,0,0};
            float[] blue = new float[] {0,0,1};
            float[] origin = new float[3];
            float[] dest = new float[3];

            stream.startNode("Viewpoint",null);
            stream.startField("centerOfRotation");
            stream.fieldValue(new float[] {edge.a.x,edge.a.y,edge.a.z}, 3);
            stream.endNode();

            float scale = 0.9999f;
            origin[0] = failure.a.x() * scale;
            origin[1] = failure.a.y() * scale;
            origin[2] = failure.a.z() * scale;
            dest[0] = failure.b.x() * scale;
            dest[1] = failure.b.y() * scale;
            dest[2] = failure.b.z() * scale;

            ejectSegment(stream, origin, dest, red);

            origin[0] = failure.b.x() * scale;
            origin[1] = failure.b.y() * scale;
            origin[2] = failure.b.z() * scale;
            dest[0] = failure.c.x() * scale;
            dest[1] = failure.c.y() * scale;
            dest[2] = failure.c.z() * scale;

            ejectSegment(stream, origin, dest, red);

            origin[0] = failure.c.x() * scale;
            origin[1] = failure.c.y() * scale;
            origin[2] = failure.c.z() * scale;
            dest[0] = failure.a.x() * scale;
            dest[1] = failure.a.y() * scale;
            dest[2] = failure.a.z() * scale;

            ejectSegment(stream, origin, dest, red);

            System.out.println("degenerate? " + isFaceDegenerate(failure));
            for(WEFace face : added_faces) {
                origin[0] = face.a.x();
                origin[1] = face.a.y();
                origin[2] = face.a.z();
                dest[0] = face.b.x();
                dest[1] = face.b.y();
                dest[2] = face.b.z();
                
                ejectSegment(stream, origin, dest, blue);
                
                origin[0] = face.b.x();
                origin[1] = face.b.y();
                origin[2] = face.b.z();
                dest[0] = face.c.x();
                dest[1] = face.c.y();
                dest[2] = face.c.z();

                ejectSegment(stream, origin, dest, blue);

                origin[0] = face.c.x();
                origin[1] = face.c.y();
                origin[2] = face.c.z();
                dest[0] = face.a.x();
                dest[1] = face.a.y();
                dest[2] = face.a.z();

                ejectSegment(stream, origin, dest, blue);
                
            }


            float[] white = new float[] {1,1,1};
            float[] green = new float[] {0,1,0};
            
            float trans = 2;
            idx = 0;
            float[] color;
            
            for(WEFace face : orig_faces_list) {
                if (idx < 2) {
                    color = green;
                }  else {
                    color = white;
                }
                origin[0] = face.a.x() + trans;
                origin[1] = face.a.y();
                origin[2] = face.a.z();
                dest[0] = face.b.x() + trans;
                dest[1] = face.b.y();
                dest[2] = face.b.z();

                ejectSegment(stream, origin, dest, color);

                origin[0] = face.b.x() + trans;
                origin[1] = face.b.y();
                origin[2] = face.b.z();
                dest[0] = face.c.x() + trans;
                dest[1] = face.c.y();
                dest[2] = face.c.z();

                ejectSegment(stream, origin, dest, color);

                origin[0] = face.c.x() + trans;
                origin[1] = face.c.y();
                origin[2] = face.c.z();
                dest[0] = face.a.x() + trans;
                dest[1] = face.a.y();
                dest[2] = face.a.z();

                ejectSegment(stream, origin, dest, color);
                
                idx++;

            }

            stream.endDocument();
            try { fos.close(); } catch(IOException ioe) {}

            throw new IllegalArgumentException("failed");
            
        } else {

            if (orig_faces != mesh.faces.size()) {
                System.out.println("ERROR: not all faces restored.  orig: " + orig_faces + " now: " + mesh.faces.size());
            }
            int new_verts = mesh.vertices.size();


            if (orig_verts - new_verts != 1) {
                System.out.println("ERROR: vert count off: orig_verts: " + orig_verts + " new_verts: " + new_verts);
            }
        }
        // TODO: We should remove va and vb.  But WETriangleMesh doesn't support this
    }
    
    // coplanar collapse logic
    private boolean canCollapseDebug(WingedEdge edge) {
        if (edge.id == 317092) {
            return true;
        }

       return false;
    }

    private boolean isCoplanar(Vec3D vec, float eps) {
        return MathUtils.abs(vec.x) < eps
                && MathUtils.abs(vec.y) < eps
                && MathUtils.abs(vec.z) < eps;
    }

    // Insure that any new vertex positions are not on top each other
    private boolean canCollapseVertexMashing(WingedEdge edge, WEVertex mid) {
        return false;
    }

    /**
     *  Insure calculated mid point would not be on the outside of the object.
     *  
     *  TODO: This never seems to allow anything
     * @param edge
     * @param grid
     * @return
     */
    private boolean canCollapseMidPoint(WingedEdge edge, WEVertex mid, Grid grid) {
        if (!grid.insideGridWorld(mid.x, mid.y, mid.z)) {
            return false;
        }

        byte state = grid.getStateWorld(mid.x, mid.y, mid.z);

        if (state == Grid.OUTSIDE) {
System.out.println("Ignoring outside mid: " + mid + " a: " + edge.a + " b: " + edge.b);
            System.out.println("   a: " + grid.getStateWorld(edge.a.x, edge.a.y, edge.a.z));
            System.out.println("   b: " + grid.getStateWorld(edge.b.x, edge.b.y, edge.b.z));
            return false;
        }

        return true;
    }

    /**
     * Verify that the new location of every edge involved will lie inside the voxel grid.  Avoids closing holes
     * @param edge
     * @param grid
     * @return
     */
    private boolean canCollapseEdgesInsideGrid(WingedEdge edge, int dist, WEVertex mid, Grid grid) {
        WEVertex va = (WEVertex) edge.a;
        WEVertex vb = (WEVertex) edge.b;
        List<WEFace> lista = va.getRelatedFaces();
        List<WEFace> listb = vb.getRelatedFaces();

        int[] counts = new int[2];
        int[] gcoords = new int[3];
        WingedEdge tmp = new WingedEdge((WEVertex)edge.a,(WEVertex)edge.b, (WEFace)edge.faces.get(0),  edge.id);

        Vec3D emid = null;
        int ratio = 2;  // out_cnt > ratio * in_cnt fails

        if (!grid.insideGridWorld(mid.x, mid.y, mid.z)) {
            return false;
        }

        grid.getGridCoords(mid.x(), mid.y,mid.z,gcoords);

        getCounts(grid, gcoords[0], gcoords[1], gcoords[2],dist, counts);

        if (counts[1] > ratio * counts[0]) {
            return false;
        }

        for(WEFace face : lista) {
            if (face.a.equals(va)) {
                tmp.a = mid;
                tmp.b = face.b;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }

                tmp.a = face.b;
                tmp.b = face.c;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }

                tmp.a = face.c;
                tmp.b = face.a;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }                
            } else if (face.b.equals(va)) {
                tmp.a = face.a;
                tmp.b = mid;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }

                tmp.a = mid;
                tmp.b = face.c;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }

                tmp.a = face.c;
                tmp.b = face.a;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }

            } else if (face.c.equals(va)){
                tmp.a = face.a;
                tmp.b = face.b;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }

                tmp.a = face.b;
                tmp.b = mid;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }

                tmp.a = mid;
                tmp.b = face.a;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }
                
            } else {
                System.out.println("ERROR: why here?");
            }
        }

        for(WEFace face : listb) {
            if (face.a.equals(vb)) {
                tmp.a = mid;
                tmp.b = face.b;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }

                tmp.a = face.b;
                tmp.b = face.c;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }

                tmp.a = face.c;
                tmp.b = face.a;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }
            } else if (face.b.equals(vb)) {
                tmp.a = face.a;
                tmp.b = mid;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }

                tmp.a = mid;
                tmp.b = face.c;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }

                tmp.a = face.c;
                tmp.b = face.a;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }

            } else if (face.c.equals(vb)){
                tmp.a = face.a;
                tmp.b = face.b;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }

                tmp.a = face.b;
                tmp.b = mid;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }

                tmp.a = mid;
                tmp.b = face.a;
                emid = getMidPoint(tmp.a, tmp.b);
                grid.getGridCoords(emid.x,emid.y,emid.z,gcoords);
                getCounts(grid,gcoords[0],gcoords[1],gcoords[2],dist,counts);
                if (counts[1] > ratio * counts[0]) {
                    return false;
                }

            } else {
                System.out.println("ERROR: why here?");
            }
        }

        return true;
    }

    /**
     *  Insure calculated mid point would not be on the outside of the object.
     *
     *  TODO: This never seems to allow anything
     * @param edge
     * @param grid
     * @return
     */
    private boolean canCollapseMidPoint(WingedEdge edge, int dist, Vec3D mid, Grid grid) {
        int[] coords = new int[3];

        if (!grid.insideGridWorld(mid.x, mid.y, mid.z)) {
            return false;
        }

        grid.getGridCoords(mid.x(), mid.y,mid.z,coords);
        
        int[] counts = new int[2];

        getCounts(grid, coords[0], coords[1], coords[2],dist, counts);

        if (counts[1] > 2 * counts[0]) {
            System.out.println("in_cnt: " + counts[0] + " out: " + counts[1]);

            System.out.println("Ignoring outside mid: " + mid + " a: " + edge.a + " b: " + edge.b);
            System.out.println("   a: " + grid.getStateWorld(edge.a.x, edge.a.y, edge.a.z));
            System.out.println("   b: " + grid.getStateWorld(edge.b.x, edge.b.y, edge.b.z));

            return false;
        }

        return true;
    }
    
    private void getCounts(Grid grid, int x, int y, int z, int dist, int[] counts) {
        int out_cnt = 0;
        int in_cnt = 0;

        byte state;
        for(int i=-dist; i <= dist; i++) {
            for(int j=-dist; j <= dist; j++) {
                for(int k=-dist; k <= dist; k++) {
                    if (!grid.insideGrid(x + i,  y + j,  z + k)) {
                        out_cnt++;
                        continue;
                    }
                    state = grid.getState(x + i,  y + j,  z + k);
                    if (state == Grid.OUTSIDE) {
                        out_cnt++;
                    } else {
                        in_cnt++;
                    }
                }
            }
        }
        
        
        counts[0] = in_cnt;
        counts[1] = out_cnt;

    }
    
    private boolean canCollapseNoNonManifoldEdges(WingedEdge edge, WEVertex mid, WETriangleMesh mesh) {
        HashMap<WingedEdge, Integer> edges = new HashMap<WingedEdge, Integer>();
        
        WEVertex va = (WEVertex) edge.a;
        WEVertex vb = (WEVertex) edge.b;
        List<WEFace> lista = va.getRelatedFaces();
        List<WEFace> listb = vb.getRelatedFaces();


        if (mesh.vertices.containsKey(mid)) {
            System.out.println("Mid point already on something, ignore");
            return false;
        }
        
        int idx = 0;
        Integer cnt = null;

        List<WEFace>[] lists = new ArrayList[2];
        Vertex[] verts = new Vertex[2];
        lists[0] = lista;
        lists[1] = listb;
        verts[0] = va;
        verts[1] = vb;
        
        lista.removeAll(edge.getFaces());
        listb.removeAll(edge.getFaces());
        WingedEdge real_edge = null;

        for(int i=0; i < lists.length; i++) {
//System.out.println("faces: " + lists[i]);
            for(WEFace face : lists[i]) {
//System.out.println("vert: " + verts[i]);
                if (face.a.equals(verts[i])) {
                    if (DEBUG) printFace(mid, face.b, face.c, new float[] {0,1,0});
                    
                    WEFace f = new WEFace((WEVertex) mid, (WEVertex) face.b, (WEVertex) face.c);
                    if (isFaceDegenerate(f)) {
                        return false;
                    }
                    WingedEdge e = new WingedEdge(mid, (WEVertex) face.b, face, 0);
                                        
                    cnt = edges.get(e);
                    if (cnt == null) {
                        edges.put(e, new Integer(1));
                    } else if (cnt >= 2) {
                        return false;
                    } else {
                        edges.put(e, new Integer(2));
                    }
                    
                    // Check current edges and make sure its not already there

                    if (mesh.edges.containsKey(e)) {
                        System.out.println("Edge already exists");
                        return false;
                    }

                    e = new WingedEdge((WEVertex) face.b, (WEVertex) face.c, face, 0);
                    cnt = edges.get(e);
                    if (cnt == null) {
                        edges.put(e, new Integer(1));
                    } else if (cnt >= 2) {
                        return false;
                    } else {
                        edges.put(e, new Integer(2));
                    }

                    e = new WingedEdge((WEVertex) face.c, (WEVertex) mid, face, 0);
                    cnt = edges.get(e);
                    if (cnt == null) {
                        edges.put(e, new Integer(1));
                    } else if (cnt >= 2) {
                        return false;
                    } else {
                        edges.put(e, new Integer(2));
                    }


                    // Check current edges and make sure its not already there
                    if (mesh.edges.containsKey(e)) {
                        System.out.println("Edge already exists");
                        return false;
                    }
                } else if (face.b.equals(verts[i])) {
                    if (DEBUG) printFace(face.a, mid, face.c, new float[] {0,1,0});
                    WEFace f = new WEFace((WEVertex) face.a, (WEVertex) mid, (WEVertex) face.c);
                    if (isFaceDegenerate(f)) {
                        return false;
                    }

                    WingedEdge e = new WingedEdge((WEVertex) face.a, (WEVertex) mid, face, 0);
                    cnt = edges.get(e);
                    if (cnt == null) {
                        edges.put(e, new Integer(1));
                    } else if (cnt >= 2) {
                        return false;
                    } else {
                        edges.put(e, new Integer(2));
                    }

                    // Check current edges and make sure its not already there
                    if (mesh.edges.containsKey(e)) {
                        System.out.println("Edge already exists");
                        return false;
                    }

                    e = new WingedEdge((WEVertex) mid, (WEVertex) face.c, face, 0);
                    cnt = edges.get(e);
                    if (cnt == null) {
                        edges.put(e, new Integer(1));
                    } else if (cnt >= 2) {
                        return false;
                    } else {
                        edges.put(e, new Integer(2));
                    }


                    // Check current edges and make sure its not already there
                    if (mesh.edges.containsKey(e)) {
                        System.out.println("Edge already exists");
                        return false;
                    }

                    e = new WingedEdge((WEVertex) face.c, (WEVertex) face.a, face, 0);
                    cnt = edges.get(e);
                    if (cnt == null) {
                        edges.put(e, new Integer(1));
                    } else if (cnt >= 2) {
                        return false;
                    } else {
                        edges.put(e, new Integer(2));
                    }
                } else if (face.c.equals(verts[i])){
                    if (DEBUG) printFace(face.a, face.b, mid, new float[] {0,1,0});
                    WEFace f = new WEFace((WEVertex) face.a, (WEVertex) face.b, (WEVertex) mid);
                    if (isFaceDegenerate(f)) {
                        return false;
                    }

                    WingedEdge e = new WingedEdge((WEVertex) face.a, (WEVertex) face.b, face, 0);
                    cnt = edges.get(e);
                    if (cnt == null) {
                        edges.put(e, new Integer(1));
                    } else if (cnt >= 2) {
                        return false;
                    } else {
                        edges.put(e, new Integer(2));
                    }

                    e = new WingedEdge((WEVertex) face.b, (WEVertex) mid, face, 0);
                    cnt = edges.get(e);
                    if (cnt == null) {
                        edges.put(e, new Integer(1));
                    } else if (cnt >= 2) {
                        return false;
                    } else {
                        edges.put(e, new Integer(2));
                    }

                    // Check current edges and make sure its not already there
                    if (mesh.edges.containsKey(e)) {
                        System.out.println("Edge already exists");
                        return false;
                    }

                    e = new WingedEdge((WEVertex) mid, (WEVertex) face.a, face, 0);
                    cnt = edges.get(e);
                    if (cnt == null) {
                        edges.put(e, new Integer(1));
                    } else if (cnt >= 2) {
                        return false;
                    } else {
                        edges.put(e, new Integer(2));
                    }

                    // Check current edges and make sure its not already there
                    if (mesh.edges.containsKey(e)) {
                        System.out.println("Edge already exists");
                        return false;
                    }
                } else {
                    System.out.println("ERROR: why here?");
                }
            }
        }

        return true;
    }
    
    // Check that all vertices related to the edge have exactly them same normals
    private boolean canCollapseRelatedAxis(WingedEdge edge) {

        WEVertex va = (WEVertex) edge.a;
        WEVertex vb = (WEVertex) edge.b;

        if (edge.faces.size() < 2) {
            return false;
        }

        Vec3D face1 = edge.faces.get(0).normal;
        Vec3D face2 = edge.faces.get(1).normal;

        float EPS = 1e-7f;

        if (!face1.equalsWithTolerance(face2, EPS)) {
            return false;
        }


        List<WEFace> list = va.getRelatedFaces();
        for(WEFace face : list) {
            face2 = face.normal;

            if (!face1.equalsWithTolerance(face2, EPS)) {
                return false;
            }
        }

        list = vb.getRelatedFaces();
        for(WEFace face : list) {
            face2 = face.normal;

            if (!face1.equalsWithTolerance(face2, EPS)) {
                return false;
            }
        }

        return true;
    }

    // Check that all vertices related to the edge are coplanar
    private boolean canCollapseRelatedMinDot(WingedEdge edge, double angle) {

        WEVertex va = (WEVertex) edge.a;
        WEVertex vb = (WEVertex) edge.b;

        if (edge.faces.size() < 2) {
            return false;
        }

        Vec3D face1 = edge.faces.get(0).normal;
        Vec3D face2 = edge.faces.get(1).normal;

        float dot = face1.dot(face2);

        if (dot < angle)  {
            return false;
        }

        
        List<WEFace> list = va.getRelatedFaces();
        for(WEFace face : list) {
            face2 = face.normal;
            dot = face1.dot(face2);
            if(dot < angle) {
                return false;
            }
        }

        list = vb.getRelatedFaces();
        for(WEFace face : list) {
            face2 = face.normal;
            dot = face1.dot(face2);

            if(dot < angle) {
                return false;
            }
        }

        return true;
    }

    // Check that all vertices related to the edge are coplanar
    private boolean canCollapseRelatedCoplanar(WingedEdge edge) {

        WEVertex va = (WEVertex) edge.a;
        WEVertex vb = (WEVertex) edge.b;
        
        if (edge.faces.size() < 2) {
            return false;
        }
        
        Vec3D face1 = edge.faces.get(0).normal;
        Vec3D face2 = edge.faces.get(1).normal;

        Vec3D cross = face1.cross(face2);

        float EPS = 1e-6f;

        if (!isCoplanar(cross, EPS))
            return false;
        
        List<WEFace> list = va.getRelatedFaces();
        for(WEFace face : list) {
            face2 = face.normal;
            cross = face1.cross(face2);
            
            if (!isCoplanar(cross, EPS)) {
                return false;
            }
        }

        list = vb.getRelatedFaces();
        for(WEFace face : list) {
            face2 = face.normal;
            cross = face1.cross(face2);

            if (!isCoplanar(cross, EPS)) {
                return false;
            }
        }

        return true;
    }
    
    // coplanar collapse logic
    private boolean canCollapseCoplanar(WingedEdge edge) {

        WEVertex va = (WEVertex) edge.a;
        WEVertex vb = (WEVertex) edge.b;

        if (edge.faces.size() < 2) {
            return false;
        }

        Vec3D face1 = edge.faces.get(0).normal;
        Vec3D face2 = edge.faces.get(1).normal;

        Vec3D cross = face1.cross(face2);

        float EPS = 1e-6f;

        return isCoplanar(cross, EPS);
    }

    // distance collapse logic
    private boolean canCollapseDistance(WingedEdge edge, double dist) {

        WEVertex va = (WEVertex) edge.a;
        WEVertex vb = (WEVertex) edge.b;

        double dx = va.x - vb.x;
        double dy = va.y - vb.y;
        double dz = va.z - vb.z;
        double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);

        if (distance > dist) {
/*
            System.out.println("reject distance: " + distance + " max: " + dist);
            System.out.println("va: " + va + " vb: " + vb);
*/
            return false;
        }

        return true;
    }
    
    
    private void printFace(Vec3D a, Vec3D b, Vec3D c, float[] color) {
/*
        System.out.println("Shape { " + "appearance Appearance { material Material { diffuseColor " + java.util.Arrays.toString(color) +
                " } } \n geometry IndexedTriangleSet { index [0 1 2] coord Coordinate { point [");
        System.out.println("   " + a.x() + " " + a.y() + " " + a.z() + ",");
        System.out.println("   " + b.x() + " " + b.y() + " " + b.z() + ",");
        System.out.println("   " + c.x() + " " + c.y() + " " + c.z());
        System.out.println("] } } }");
*/
        /*
        System.out.print("va: " + a);
        System.out.print(" vb: " + b);
        System.out.println(" vc: " + c);
        */
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
                if (border != null && border.contains(edge)) {
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
                System.out.println("Invalid face: " + face +  " edges: " + f.edges.size());
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
    
    private Vec3D getMidPoint(Vec3D a, Vec3D b) {
        Vec3D ret_val = new Vec3D();

        ret_val.x = (float) (((double)a.x + b.x) * 0.5);
        ret_val.y = (float) (((double)a.y + b.y) * 0.5);
        ret_val.z = (float) (((double)a.z + b.z) * 0.5);

        return ret_val;
    }

    /**
     * Display a line segment
     *
     * @param stream The X3D stream to output too
     * @param origin The origin of the segment
     * @param dest The dest of the segment
     */
    public static void ejectSegment(BinaryContentHandler stream, float[] origin, float[] dest, float[] color) {

        if (stream == null) {
            return;
        }

        float[] all_point = new float[] {
                (float) origin[0], (float) origin[1], (float) origin[2],
                ((float) (dest[0])), ((float) (dest[1])),
                ((float) (dest[2]))
        };

        int[] all_index = new int[] {0,1};

        stream.startNode("Shape", null);
        if (color != null) {
            stream.startNode("Appearance", null);
            stream.startField("material");
            stream.startNode("Material", null);
            stream.startField("emissiveColor");
            stream.fieldValue(color,3);
            stream.endNode();  // Material
            stream.endNode();  // Appearance            
        }
        stream.startField("geometry");
        stream.startNode("IndexedLineSet", null);
        stream.startField("coord");
        stream.startNode("Coordinate", null);
        stream.startField("point");
        stream.fieldValue(all_point, all_point.length);
        stream.endNode();  // Coordinate
        stream.startField("coordIndex");
        stream.fieldValue(all_index, all_index.length);
        stream.endNode();  // IndexedLineSet
        stream.endNode();  // Shape
    }


    /**
     * Compute cross product of two edges, if its near zero its bad
     * @param f
     * @return
     */
    private boolean isFaceDegenerate(Face f) {
        double area = f.b.sub(f.a).cross(f.c.sub(f.a)).magSquared();

        double EPS = 1e-17f;

        if (area < EPS) {
            //System.out.println("Degenerate tri: " + f + " area: " + area);
            return true;
        }

        return false;
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