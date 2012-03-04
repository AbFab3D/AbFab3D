package abfab3d.io.output;

import abfab3d.grid.Grid;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.export.X3DBinaryRetainedDirectExporter;
import org.web3d.vrml.export.X3DBinarySerializer;
import org.web3d.vrml.sav.BinaryContentHandler;
import toxi.geom.Line3D;
import toxi.geom.Ray3D;
import toxi.geom.Vec3D;
import toxi.geom.mesh.*;
import toxi.math.MathUtils;

import javax.vecmath.Vector3d;
import java.io.FileOutputStream;
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

    public EdgeCollapseSimplifier() {
        this(0.25);
    }

    public EdgeCollapseSimplifier(double maxDistance) {
        this.maxDistance = maxDistance;
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
                
                FileOutputStream fos = new FileOutputStream("c:/tmp/viz.x3db");
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
            System.out.println("Starting pass: " + pass + " edges: " + mesh.getNumEdges());
            collapse_list.clear();
            if (pass >= max_pass) {
                break;
            }


            for(WingedEdge edge : mesh.edges.values()) {
                    // TODO: This is the best so far, Coplanar might be ok as well
//                if (canCollapseRelatedCoplanar(edge) && canCollapseDistance(edge,maxDistance)) {
                
                
                
//.6 was last
                if (canCollapseRelatedMinDot(edge,0.8f) && canCollapseDistance(edge, maxDistance)) {

//                if (canCollapseRelatedCoplanar(edge) && canCollapseDistance(edge,maxDistance)) {
//                if (canCollapseRelatedCoplanar(edge) && canCollapseEdgesInsideGrid(edge, 1, grid)) {
//                if (canCollapseRelatedCoplanar(edge) && canCollapseMidPoint(edge, 1, grid)) {
//                if (canCollapseRelatedCoplanar(edge)) {
//                if (canCollapseRelatedAxis(edge) && canCollapseMidPoint(edge, 1, grid)) {
//                if (canCollapseRelatedCoplanar(edge)) {
//                if (canCollapseCoplanar(edge)) {
                    collapse_list.add(edge);
                    
                    if (display_edges) {
                        ejectSegment(stream, new float[] {edge.a.x, edge.a.y, edge.a.z,}, 
                                new float[] {edge.b.x, edge.b.y, edge.b.z});
                    }
                }                                
            }
            System.out.println("CoPlanar: " + collapse_list.size());

/*
            collapse_list.clear();

            for(WingedEdge edge : mesh.edges.values()) {
                if (canCollapseRelatedCoplanar(edge) && canCollapseEdgesInsideGrid(edge, 1, grid)) {
//                if (canCollapseRelatedCoplanar(edge) && canCollapseMidPoint(edge, 1, grid)) {
//                if (canCollapseRelatedCoplanar(edge)) {
//                if (canCollapseRelatedAxis(edge) && canCollapseMidPoint(edge, 1, grid)) {
//                if (canCollapseRelatedCoplanar(edge)) {
//                if (canCollapseCoplanar(edge)) {
                    collapse_list.add(edge);
                }
            }
            System.out.println("CoPlanarRelated: " + collapse_list.size());
  */
            // TODO: Sort edge list by id for determinism.  Can remove later
            Collections.sort(collapse_list, new EdgeSorter());
            size = collapse_list.size();
            ArrayList<WEFace> face_list = new ArrayList<WEFace>();

            System.out.println("Collapsable Edges: " + collapse_list.size() + " total: " + mesh.getNumEdges());
            int edges_removed = 0;
            
            for(WingedEdge edge : collapse_list) {
                if (edge.faces.size() != 2) {
                    // Edge likely has already been removed during another collapse
                    continue;
                }
                if (DEBUG) System.out.println("Collapse edge: " + edge);

                collapseEdge2(edge, mesh);
                
/*                
                boolean valid = validateMesh(mesh, null);

                if (!valid) {
                    System.out.println("Mesh now invalid");
                }
 */
                
                edges_removed++;

                if (edges_removed >= max_edges)  {
                    break loop;
                }
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

        if (display_edges) {
            stream.endDocument();
        }
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
     * Edge collapsed based on remove and readd.
     * @param edge
     * @param mesh
     */
    protected void collapseEdge2(WingedEdge edge, WETriangleMesh mesh) {
        // Choose center point of edge for new vertex location
        WEVertex mid = mesh.checkVertex(getMidPoint(edge.a, edge.b));

        if (DEBUG) System.out.println("Removing edge: " + edge);
        mesh.removeEdge(edge);

        WEVertex va = (WEVertex) edge.a;
        WEVertex vb = (WEVertex) edge.b;
        List<WEFace> lista = va.getRelatedFaces();
        List<WEFace> listb = vb.getRelatedFaces();


        List<WEFace> face_list = new ArrayList<WEFace>();

        int idx = 0;

        float[] anormals = new float[lista.size() * 3];
        float[] bnormals = new float[listb.size() * 3];
        
        if (DEBUG) System.out.println("Removing faces  a: " + lista.size() + " b: " + listb.size());
        for(WEFace face : lista) {
            anormals[idx++] = face.normal.x;
            anormals[idx++] = face.normal.y;
            anormals[idx++] = face.normal.z;
            
            if (DEBUG) System.out.println("   " + face);
            mesh.removeFace(face);
        }

        idx = 0;
        for(WEFace face : listb) {
            bnormals[idx++] = face.normal.x;
            bnormals[idx++] = face.normal.y;
            bnormals[idx++] = face.normal.z;
            if (DEBUG) System.out.println("   " + face);
            mesh.removeFace(face);
        }


        boolean use_normals = true; //  TODO: sometimes this helps, other times it doesnt

        idx = 0;
        for(WEFace face : lista) {
            if (face.a.equals(va)) {
                if (DEBUG) printFace(mid, face.b, face.c, new float[] {0,1,0});
                if (use_normals) {
                    mesh.addFace(mid, face.b, face.c, new Vec3D(anormals[idx++],anormals[idx++],anormals[idx++]));
                } else {
                    mesh.addFace(mid, face.b, face.c);
                }
            } else if (face.b.equals(va)) {
                if (DEBUG) printFace(face.a, mid, face.c, new float[] {0,1,0});
                if (use_normals) {
                    mesh.addFace(face.a, mid, face.c,new Vec3D(anormals[idx++],anormals[idx++],anormals[idx++]));
                } else {
                    mesh.addFace(face.a, mid, face.c);
                }
            } else if (face.c.equals(va)){
                if (DEBUG) printFace(face.a, face.b, mid, new float[] {0,1,0});
                if (use_normals) {
                    mesh.addFace(face.a, face.b, mid,new Vec3D(anormals[idx++],anormals[idx++],anormals[idx++]));
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
                    mesh.addFace(mid, face.b, face.c, new Vec3D(bnormals[idx++],bnormals[idx++],bnormals[idx++]));
                } else {
                    mesh.addFace(mid, face.b, face.c);
                }
            } else if (face.b.equals(vb)) {
                if (DEBUG) printFace(face.a, mid, face.c, new float[] {0,1,0});
                if (use_normals) {
                    mesh.addFace(face.a, mid, face.c, new Vec3D(bnormals[idx++],bnormals[idx++],bnormals[idx++]));
                } else {
                    mesh.addFace(face.a, mid, face.c);
                }
            } else if (face.c.equals(vb)){
                if (DEBUG) printFace(face.a, face.b, mid, new float[] {0,1,0});
                if (use_normals) {
                    mesh.addFace(face.a, face.b, mid, new Vec3D(bnormals[idx++],bnormals[idx++],bnormals[idx++]));
                } else {
                    mesh.addFace(face.a, face.b, mid);
                }
            } else {
                System.out.println("ERROR: why here?");
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

    /**
     *  Insure calculated mid point would not be on the outside of the object.
     *  
     *  TODO: This never seems to allow anything
     * @param edge
     * @param grid
     * @return
     */
    private boolean canCollapseMidPoint(WingedEdge edge, Grid grid) {
        Vec3D mid = getMidPoint(edge.a, edge.b);

        if (!grid.insideGrid(mid.x,mid.y,mid.z)) {
            return false;
        }

        byte state = grid.getState(mid.x,  mid.y,  mid.z);

        if (state == Grid.OUTSIDE) {
System.out.println("Ignoring outside mid: " + mid + " a: " + edge.a + " b: " + edge.b);
            System.out.println("   a: " + grid.getState(edge.a.x,  edge.a.y,  edge.a.z));
            System.out.println("   b: " + grid.getState(edge.b.x,  edge.b.y,  edge.b.z));
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
    private boolean canCollapseEdgesInsideGrid(WingedEdge edge, int dist, Grid grid) {
        WEVertex va = (WEVertex) edge.a;
        WEVertex vb = (WEVertex) edge.b;
        List<WEFace> lista = va.getRelatedFaces();
        List<WEFace> listb = vb.getRelatedFaces();

        int[] counts = new int[2];
        int[] gcoords = new int[3];
        WingedEdge tmp = new WingedEdge((WEVertex)edge.a,(WEVertex)edge.b, (WEFace)edge.faces.get(0),  edge.id);
        Vec3D mid = getMidPoint(edge.a, edge.b);
        Vec3D emid = null;
        int ratio = 2;  // out_cnt > ratio * in_cnt fails

        if (!grid.insideGrid(mid.x,mid.y,mid.z)) {
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
    private boolean canCollapseMidPoint(WingedEdge edge, int dist, Grid grid) {
        Vec3D mid = getMidPoint(edge.a, edge.b);
        int[] coords = new int[3];

        if (!grid.insideGrid(mid.x,mid.y,mid.z)) {
            return false;
        }

        grid.getGridCoords(mid.x(), mid.y,mid.z,coords);
        
        int[] counts = new int[2];

        getCounts(grid, coords[0], coords[1], coords[2],dist, counts);

        if (counts[1] > 2 * counts[0]) {
            System.out.println("in_cnt: " + counts[0] + " out: " + counts[1]);

            System.out.println("Ignoring outside mid: " + mid + " a: " + edge.a + " b: " + edge.b);
            System.out.println("   a: " + grid.getState(edge.a.x,  edge.a.y,  edge.a.z));
            System.out.println("   b: " + grid.getState(edge.b.x,  edge.b.y,  edge.b.z));

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
    private boolean canCollapseRelatedMinDot(WingedEdge edge, float angle) {

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

        // insure all faces have three edges
        for(Face face : mesh.faces.values()) {
            WEFace f = (WEFace) face;

            if (f.edges.size() != 3) {
                System.out.println("Invalid face: " + face);
                valid = false;
            }

            WEVertex v = (WEVertex) f.a;
            List<WingedEdge> edges = v.edges;

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
    public static void ejectSegment(BinaryContentHandler stream, float[] origin, float[] dest) {

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


}

class EdgeSorter implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        WingedEdge we1 = (WingedEdge) o1;
        WingedEdge we2 = (WingedEdge) o2;

        return we1.id - we2.id;
    }
}