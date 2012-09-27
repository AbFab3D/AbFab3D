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

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.io.PrintStream;
import org.web3d.util.HashSet;
import java.util.*;

import static abfab3d.util.Output.printf;

/**
 * 3D Triangle Mesh structure implemented using a WingedEdge datastructure.  Made for easy traversal of edges and dynamic in
 * nature for easy edge collapses.
 *
 * @author Vladimir Bulatov
 * @author Alan Hudson
 */
public class WingedEdgeTriangleMesh {
    static boolean DEBUG = false;
    private static final boolean CHECK_DEGENERATE_PER_COLLAPSE = false;

    private Face faces;
    private Face lastFace;

    private Vertex vertices;
    private Vertex lastVertex;

    //
    private HashMap<Point3d, Vertex> tvertices = new HashMap<Point3d, Vertex>();
    
    // work set for topology check 
    private HashSet m_vset = new HashSet();
    // checker of face flip 
    protected FaceFlipChecker m_faceFlipChecker = new FaceFlipChecker(); 
    static boolean TEST_TOPOLOGY = false;

    private Edge edges;
    private Edge lastEdge;
//    private HashMap<HalfEdgeKey, HalfEdge> edgeMap = new HashMap<HalfEdgeKey, HalfEdge>();

    // TODO: Used a linked hashmap to get a consistent order for debugging
    private HashMap<HalfEdgeKey, HalfEdge> edgeMap = new LinkedHashMap<HalfEdgeKey, HalfEdge>();
    private HalfEdgeKey skey = new HalfEdgeKey();

    private int vertexCount = 0;

    /**
     * Scratch vector for computation
     */
    private Vector3d svec1 = new Vector3d();

    /**
     * Scratch vector for computation
     */
    private Vector3d svec2 = new Vector3d();

    public WingedEdgeTriangleMesh(Point3d vertCoord[], int[][] findex) {

        Vertex V[] = new Vertex[vertCoord.length];

        for (int nv = 0; nv < V.length; nv++) {

            V[nv] = new Vertex();
            V[nv].setPoint(new Point3d(vertCoord[nv]));
            V[nv].setID(nv);
        }

        ArrayList eface = new ArrayList(findex.length * 3);

        for (int i = 0; i < findex.length; i++) {

            // Create the half edges
            int[] face = findex[i];
            if (face == null) {
                throw new IllegalArgumentException("Face's cannot be null");
            }

            eface.clear();

            if (face.length != 3) {
                throw new IllegalArgumentException("Faces must be triangles.  Index: " + i);
            }
            for (int j = 0; j < face.length; j++) {

                Vertex v1 = V[face[j]];
                Vertex v2 = V[face[(j + 1) % face.length]];
                HalfEdge he = buildHalfEdge(v1, v2);
                edgeMap.put(new HalfEdgeKey(he.getStart(), he.getEnd()), he);
                eface.add(he);
            }

            // Create the face
            buildFace(eface);

        }

        boolean notifyNonManifold = true;

        HalfEdgeKey key = new HalfEdgeKey();

        // Find the twins
        for (HalfEdge he1 : edgeMap.values()) {
            if (he1.getTwin() == null) {
                // get halfedge of _opposite_ direction
                //key.setStart(he1.getStart());
                //key.setEnd(he1.getEnd());

                key.setStart(he1.getEnd());
                key.setEnd(he1.getStart());
                HalfEdge he2 = edgeMap.get(key);
                if (he2 != null) {
                    betwin(he1, he2);
                    buildEdge(he1); // create the edge!
                } else {
                    if (DEBUG && notifyNonManifold) {
                        System.out.println("NonManifold hedge: " + he1 + " ? " + he1.getStart().getID() + "->" + he1.getEnd().getID());
                    }
                    // Null twin means its an outer edge on a non-manifold surface
                    buildEdge(he1);
                }
            }
        }

        /* Add the vertices to the list */
        for (int i = 0; i < V.length; i++) {
            addVertex(V[i]);
        }
    }

    /**
     * Get the edges
     *
     * @return A linked list of edges
     */
    public Edge getEdges() {
        return edges;
    }

    public Iterator<Edge> edgeIterator() {
        return new EdgeIterator(edges);
    }

    public Vertex getVertices() {
        return vertices;
    }

    public Iterator<Vertex> vertexIterator() {
        return new VertexIterator(vertices);
    }

    public Face getFaces() {
        return faces;
    }

    public Iterator<Face> faceIterator() {
        return new FaceIterator(faces);
    }

    public Vertex[][] getFaceIndexes() {

        // init vert indices
        initVertexIndices();

        int faceCount = getFaceCount();

        Vertex[][] findex = new Vertex[faceCount][];
        int fcount = 0;

        for (Face f = faces; f != null; f = f.getNext()) {

            Vertex[] face = new Vertex[3];
            findex[fcount++] = face;
            int v = 0;
            HalfEdge he = f.getHe();
            do {
                face[v++] = he.getStart();
                he = he.getNext();
            } while (he != f.getHe());
        }

        return findex;

    }

    /**
     * Get the count of vertices in this mesh.  Local variable kept during upkeep so it's a fast operation.
     *
     * @return The count
     */
    public int getVertexCount() {
        return vertexCount;
    }

    /**
     * Get the count of triangles in this mesh.  Traverses the face list to count so its a relatively
     * slow operation.
     *
     * @return The count
     */
    public int getTriangleCount() {
        Face f = faces;
        Face start = f;
        int cnt = 0;

        while (f != null) {
            cnt++;

            f = f.getNext();

            if (f == start) {
                break;
            }
        }

        return cnt;
    }

    /**
     * Get the count of edges in this mesh.  Traverses the edge list to count so its a relatively
     * slow operation.
     *
     * @return The count
     */
    public int getEdgeCount() {
        Edge e = edges;
        Edge start = e;
        int cnt = 0;

        while (e != null) {
            cnt++;

            e = e.getNext();

            if (e == start) {
                break;
            }
        }

        return cnt;
    }

    /**
     * Collapse an edge.
     *
     * @param e   The edge to collapse
     * @param pos The position of the new common vertex
     */
    public boolean collapseEdge(Edge e, Point3d pos, EdgeCollapseResult ecr) {
        // third iteration of this 

        // how we do it? 
        //
        // /doc/html/svg/EdgeCollapse_Basic.svg  for all notations 
        //
        // 1) we identify 2 faces needed to be removed 
        // on edge v0->v1 
        // 
        //                   
        //                    V1.
        //                .   |    . 
        //             .      |       .
        //         .          |          .
        //      .       FL    |    FR       .
        //   .                |               .
        //    .               |           .
        //        .           |        .
        //            .       |     .
        //                .   |  .
        //                    V0....................
        //
        // fR- right face
        // fL- left face       
        //
        // 2) link halfedges connected to v0 to v1                
        
        HalfEdge hR = e.getHe();
        Vertex v0 = hR.getEnd();
        Vertex v1 = hR.getStart();

        if(v0.isRemoved()){
            printf("!!!error!!! removing removed vertex: %s\n", v0);
            return false;
        }
        if(v1.isRemoved()){
            printf("!!!error!!! removing removed vertex: %s\n", v1);
            return false;
        }

        HalfEdge he;// working variable 
        
        HalfEdge hL = hR.getTwin();        
        Face fL = hL.getLeft();
        Face fR = hR.getLeft();
        if(DEBUG)printf("fR: %s\nfL: %s", fR, fL);

        HalfEdge 
            hLp = hL.getPrev(),            
            hLpt = hLp.getTwin(),
            hLn = hL.getNext(),
            hLnt = hLn.getTwin(),
            hRn = hR.getNext(),
            hRnt = hRn.getTwin(),
            hRp = hR.getPrev(),
            hRpt = hRp.getTwin();


        Vertex 
            vR = hRp.getStart(),
            vL = hLn.getEnd();
        Edge 
            e1R = hRp.getEdge(),
            e1L = hLn.getEdge(),
            e0R = hRn.getEdge(),
            e0L = hLp.getEdge();

        //
        // check if collapse may cause surface pinch
        // the illeagal collapse is the following: 
        // if edge adjacent to v0 have common vertex with edge adjacent to v1 (except edges, which surround fR and fL )
        // then our collapse will cause surface pinch aong such edge 
        // 
        HashSet v1set = m_vset;
        v1set.clear();
        he = hLnt.getNext();
        
        while(he != hRpt){
            v1set.add(he.getEnd());
            he = he.getTwin().getNext();
        }
        // v1set has all the vertices conected to v1 
        he = hRnt.getNext();
        while(he != hLpt){
            if(v1set.contains(he.getEnd())){
                if(DEBUG)printf("!!!illegal collapse. Surface pinch detected!!!\n");
                ecr.returnCode = EdgeCollapseResult.FAILURE_SURFACE_PINCH;
                return false;
            }
            he = he.getTwin().getNext();
        }        
        
        //
        // if we are here - no surface pinch occurs. 
        // check if face flip occur 
        // face flip means, that face normals change direction to opposite after vertex was moved

        // check face flip of faces connected to v1 
        he = hLnt;
        Point3d pv1 = v1.getPoint();  
        while(he != hRp){
            Point3d p0 = he.getStart().getPoint();
            Point3d p1 = he.getNext().getEnd().getPoint();
            if(m_faceFlipChecker.checkFaceFlip(p0, p1, pv1, pos)){
                ecr.returnCode = EdgeCollapseResult.FAILURE_FACE_FLIP;
                return false;                
            }
            he = he.getNext().getTwin();
        }
        
        // check face flip of faces connected to v0 
        he = hRnt;
        Point3d pv0 = v0.getPoint();  
        while(he != hLp){

            Point3d p0 = he.getStart().getPoint();
            Point3d p1 = he.getNext().getEnd().getPoint();
            if(m_faceFlipChecker.checkFaceFlip(p0, p1, pv0, pos)){
                ecr.returnCode = EdgeCollapseResult.FAILURE_FACE_FLIP;
                return false;                
            }
            he = he.getNext().getTwin();
        }
        
        

        //
        //  Proseed with collapse.
        //
        // move v1 to new position 
        v1.getPoint().set(pos);        

        // remove all removable edges 
        removeEdge(e);
        removeEdge(e0R);
        removeEdge(e0L);

        if(DEBUG){
            printf("v0: %s, v1: %s\n",v0,v1);
            printf("vR: %s, vL: %s\n",vL, vR);
            printf("hL: %s,  hR: %s\n", hL, hR);
            printf("hLp:%s hLpt: %s\n", hLp, hLpt);
            printf("hLn:%s hLnt: %s\n", hLn, hLnt);
            printf("hRp:%s hRpt: %s\n", hRp, hRpt);
            printf("hRn:%s hRnt: %s\n", hRn, hRnt);                
        }
        //
        // relink all edges from v0 to v1
        //
        HalfEdge end = hLp;
        he = hRnt;        
        int maxcount = 30; // to avoid infinite cycle if cycle is broken 
        if(DEBUG)printf("moving v0-> v1\n"); 
        do{                 
            HalfEdge next = he.getNext();
            if(DEBUG)printf("  before he: %s; next: %s ", he, next);             
            he.setEnd(v1);
            next.setStart(v1);

            if(DEBUG)printf("   after  he: %s; next: %s\n", he, next);                         
            if(--maxcount < 0){
                printf("!!!!!error: maxcount exceeded!!!!! for vertex: %d\n", v0.getID());
                break;
            }            
            he = next.getTwin(); 

        } while(he != end);

        //
        // remove collapsed faces 
        //
        removeFace(fL);
        removeFace(fR);
        //
        // close outer sides of removed faces 
        betwin(hRnt, hRpt);
        betwin(hLpt, hLnt);
        hRnt.setEdge(e1R);  e1R.setHe(hRnt);
        hLpt.setEdge(e1L);  e1L.setHe(hLpt);
        //
        // reset link to HalfEdges on modified vertices 
        //
        vL.setLink(hLnt);
        vR.setLink(hRnt);
        v1.setLink(hRpt);

        // release pointers to removed edges 
        e.setHe(null);
        e0L.setHe(null);
        e0R.setHe(null);
        
        // remove the vertex 
        if(DEBUG)printf("removing vertex: %d\n", v0.getID());
        removeVertex(v0);
        
        // compose result 
        ecr.removedEdges.add(e);
        ecr.removedEdges.add(e0L);
        ecr.removedEdges.add(e0R);

        ecr.insertedVertex = v1;
        ecr.faceCount = 2;
        ecr.edgeCount = 3;
        ecr.vertexCount = 1;        
        ecr.returnCode = EdgeCollapseResult.SUCCESS;

        return true;        
    }

    

    /**
     * Check if a face is degenerate.  Does it goto burning man etc?  More formally is the area close to zero.
     *
     * @param f The face
     * @return true if the face is degenerate
     */
    private boolean isFaceDegenerate(Face f) {
        Vertex p1;
        Vertex p2;
        Vertex p3;

        p1 = f.getHe().getStart();
        p2 = f.getHe().getEnd();

        HalfEdge he = f.getHe().getNext();

        if (he.getStart() != p1 && he.getStart() != p2) {
            p3 = he.getStart();
        } else if (he.getEnd() != p1 && he.getEnd() != p2) {
            p3 = he.getEnd();
        } else {
            // Cannot find a unique third point
            return true;
        }


        svec1.x = p2.getPoint().x - p1.getPoint().x;
        svec1.y = p2.getPoint().y - p1.getPoint().y;
        svec1.z = p2.getPoint().z - p1.getPoint().z;

        svec2.x = p3.getPoint().x - p1.getPoint().x;
        svec2.y = p3.getPoint().y - p1.getPoint().y;
        svec2.z = p3.getPoint().z - p1.getPoint().z;

        svec1.cross(svec1, svec2);
        double area = svec1.length();

        if (DEBUG)
            System.out.println("Checking for degenerate: f: " + f.hashCode() + " v: " + p1.getID() + " " + p2.getID() + " " + p3.getID() + " p: " + p1.getPoint() + " p2: " + p2.getPoint() + " p3: " + p3.getPoint() + " area: " + area);

        double EPS = 1e-10;

        if (area < EPS) {
            return true;
        }
/*

System.out.println("Checking: f: " + f.hashCode() + " v: " + p1.getID() + " " + p2.getID() + " " + p3.getID() + " p: " + p1.getPoint() + " p2: " + p2.getPoint() + " p3: " + p3.getPoint());
        if (p1.getPoint().epsilonEquals(p2.getPoint(), EPS)) {
            return true;
        }
        if (p1.getPoint().epsilonEquals(p3.getPoint(), EPS)) {
            return true;
        }
        if (p2.getPoint().epsilonEquals(p3.getPoint(), EPS)) {
            return true;
        }
*/
        return false;
    }


    /**
     * Change a vertex reference from one vertex to another for a face.
     *
     * @param f      The face
     * @param vorig  The original vertex
     * @param vnew   The new vertex
     * @param hedges List of half edges involved
     */
    private void changeVertex(Face f, Vertex vorig, Vertex vnew, Set<Face> visited, Set<Edge> hedges) {
        if (visited.contains(f)) {
            return;
        }

        visited.add(f);

        if (DEBUG)
            System.out.println("ChangeVertex on face: " + f + " orig: " + vorig.getID() + " vnew: " + vnew.getID() + " start he: " + f.getHe().hashCode());
        HalfEdge he = f.getHe();
        HalfEdge start = he;

        while (he != null) {
            if (DEBUG) System.out.println("HalfEdge: " + he + " hc: " + he.hashCode());

            if (he.getStart() == vorig) {
                if (DEBUG) System.out.print("   Update vertex: " + he);
                hedges.add(he.getEdge());

                // remove old edgeMap entry
                HalfEdgeKey key = new HalfEdgeKey(he.getStart(), he.getEnd());
                edgeMap.remove(key);

                he.setStart(vnew);
                vnew.setLink(he);  // insure linkage is valid

                // readd edgeMap entry
                key.setStart(he.getStart());
                key.setEnd(he.getEnd());
                edgeMap.put(key, he);

                if (DEBUG) System.out.println("   to -->: " + he);

                HalfEdge twin = he.getTwin();

                if (twin != null) {
                    // Recurse into next face to find other vertex
                    changeVertex(twin.getLeft(), vorig, vnew, visited, hedges);
                }
            } else if (he.getEnd() == vorig) {
                if (DEBUG) System.out.print("   Update vertex: " + he);
                hedges.add(he.getEdge());

                // remove old edgeMap entry
                HalfEdgeKey key = new HalfEdgeKey(he.getStart(), he.getEnd());
                edgeMap.remove(key);

                he.setEnd(vnew);
                vnew.setLink(he);  // insure linkage is valid

                // readd edgeMap entry
                key.setStart(he.getStart());
                key.setEnd(he.getEnd());
                edgeMap.put(key, he);

                if (DEBUG) System.out.println("   to -->: " + he);

                HalfEdge twin = he.getTwin();

                if (twin != null) {
                        // Recurse into next face to find other vertex
                    changeVertex(twin.getLeft(), vorig, vnew, visited, hedges);
                }
            }

            he = he.getNext();

            if (he == start) {
                break;
            }
        }
    }

    /**
     * Change a vertex reference from one vertex to another for a face.  This a trial run that only calculates
     * the resultant faces for analysis.
     * <p/>
     *
     * @param f1      The first face to be deleted
     * @param f2      The second face to be deleted
     * @param vorig   The original vertex
     * @param vnew    The new vertex
     * @param tris    The resultant triangles created
     */
    private void changeVertexTrial(Face f1, Face f2, Vertex vorig, Vertex vnew, Set<Triangle> tris) {

        for(Triangle t : tris) {
            if (t.getV0() == vorig) {
                t.setV0(vnew);
            }
            if (t.getV1() == vorig) {
                t.setV1(vnew);
            }
            if (t.getV2() == vorig) {
                t.setV2(vnew);
            }
        }
    }

    /**
     * Change a vertex reference from one vertex to another for a face.  This a trial run that only calculates
     * the resultant faces for analysis.
     * <p/>
     * TODO:  Should use an object pool to reduce garbage generation?
     *
     * @param f       The face
     * @param f1      The first face to be deleted
     * @param f2      The second face to be deleted
     * @param vorig   The original vertex
     * @param vnew    The new vertex
     * @param visited The faces already visited
     * @param tris    The resultant triangles created
     */
    private void changeVertexTrial(Face f, Face f1, Face f2, Vertex vorig, Vertex vnew, Set<Face> visited, Set<Triangle> tris) {
        if (visited.contains(f)) {
            return;
        }

        visited.add(f);

        if (DEBUG)
            System.out.println("ChangeVertexTrial on face: " + f + " orig: " + vorig.getID() + " vnew: " + vnew.getID() + " start he: " + f.getHe().hashCode());
        HalfEdge he = f.getHe();
        HalfEdge start = he;

        int idx = 0;
        Vertex[] he_verts = new Vertex[6];

        while (he != null) {
            he_verts[idx * 2] = he.getStart();
            he_verts[idx * 2 + 1] = he.getEnd();

            if (DEBUG) System.out.println("HalfEdge: " + he + " hc: " + he.hashCode());
            if (he.getStart() == vorig) {
                he_verts[idx * 2] = vnew;
                // Recurse into next face to find other vertex
                changeVertexTrial(he.getTwin().getLeft(), f1, f2, vorig, vnew, visited, tris);
            } else if (he.getEnd() == vorig) {
                // Recurse into next face to find other vertex
                he_verts[idx * 2 + 1] = vnew;
                changeVertexTrial(he.getTwin().getLeft(), f1, f2, vorig, vnew, visited, tris);
            }

            he = he.getNext();
            idx++;

            if (he == start) {
                break;
            }
        }

        if (f != f1 && f != f2) {
            // don't test to be deleted faces
            Triangle tri = new Triangle(he_verts[0], he_verts[2], he_verts[4]);
            tris.add(tri);

            if (DEBUG) System.out.println("Adding tri: " + tri);
        }
        System.out.println("he_verts: " + java.util.Arrays.toString(he_verts));
    }

    public void writeOBJ(PrintStream out) {

        Face f;
        Vertex v;
        Edge e;
        int counter = 0;

        for (v = vertices; v != null; v = v.getNext()) {
            out.println("v " + v.getPoint() + " id: " + v.getID());
            v.setID(counter);
            counter++;
        }

        for (f = faces; f != null; f = f.getNext()) {

            out.print("f");
            HalfEdge he = f.getHe();
            do {
                out.print(" " + he.getStart().getID());
                he = he.getNext();
            } while (he != f.getHe());

            out.println(" hc: " + f.hashCode());
        }

        for (e = edges; e != null; e = e.getNext()) {

            out.print("e " + e.getHe().getStart().getID() + " " + e.getHe().getEnd().getID());
            if (e.getHe().isRemoved()) {
                out.print("HE DEAD!");
            }
            HalfEdge twin = e.getHe().getTwin();
            if (twin != null) {
                if (twin.isRemoved()) {
                    out.print("TWIN DEAD!");
                }
                out.print("  tw: " + twin.getStart().getID() + " " + e.getHe().getTwin().getEnd().getID());
            } else
                out.print("  tw: null");

            System.out.println(" hc: " + e.hashCode());
        }

        System.out.println("EdgeMap: ");
        for (Map.Entry<HalfEdgeKey, HalfEdge> entry : edgeMap.entrySet()) {
            System.out.println(entry.getKey() + " val: " + entry.getValue());
        }
    }

    Vertex buildVertex(Point3d p) {

        Vertex v = new Vertex();
        v.setPoint(p);
        addVertex(v);
        return v;
    }

    void addVertex(Vertex v) {

        //System.out.println("addVertex: " + v.p);
        // is the list empty?
        if (vertices == null) {
            lastVertex = v;
            vertices = v;
        } else {
            lastVertex.setNext(v);
            v.setPrev(lastVertex);
            lastVertex = v;
        }

        v.setID(vertexCount++);

        //System.out.println("index: " + v.index);
        // we probably don't need this map 
        //tvertices.put(v.getPoint(), v);

    }

    public void removeVertex(Vertex v) {
        if (DEBUG) {
            System.out.println("Removing vertex: " + v + " hc: " + v.hashCode());
        }
        Vertex prev = v.getPrev();

        if (prev != null) {
            prev.setNext(v.getNext());
        } else {
            // updating head
            vertices = v.getNext();
        }

        if (v == lastVertex) {
            lastVertex = prev;

            if (lastVertex == null) {
                // just removed last face?
            } else {
                lastVertex.setNext(null);
            }
        } else {
            v.getNext().setPrev(v.getPrev());
        }

        v.setLink(null);    // unlink from structure, force npe on users
        v.setNext(null);
        v.setRemoved(true);
        // do we need that tvertices
        //tvertices.remove(v);

        vertexCount--;
    }

    Face buildFace(HalfEdge hedges[]) {

        Face f = new Face();

        f.setHe((HalfEdge) hedges[0]);

        int size = hedges.length;
        for (int e = 0; e < size; e++) {

            HalfEdge he1 = hedges[e];
            he1.setLeft(f);
            HalfEdge he2 = (HalfEdge) hedges[(e + 1) % size];
            joinHalfEdges(he1, he2);

        }

        addFace(f);

        return f;

    }


    Face buildFaceV(List<Vertex> vert) {

        HalfEdge edges[] = new HalfEdge[vert.size()];

        int size = edges.length;

        for (int v = 0; v < size; v++) {

            Vertex v1 = (Vertex) vert.get(v);
            Vertex v2 = (Vertex) vert.get((v + 1) % size);
            edges[v] = buildHalfEdge(v1, v2);
        }

        return buildFace(edges);

    }

    Face buildFaceV(Vertex[] vert) {

        HalfEdge edges[] = new HalfEdge[vert.length];

        int size = edges.length;

        for (int v = 0; v < size; v++) {

            Vertex v1 = vert[v];
            Vertex v2 = vert[(v + 1) % size];
            edges[v] = buildHalfEdge(v1, v2);
        }

        return buildFace(edges);

    }

    /**
     *
     *
     *
     */
    public HalfEdge getTwin(Vertex head, Vertex tail) {

        //System.out.println("getTwin() " + head.index + "-" + tail.index);
        // return halfedge, which has corresponding head and tail
        HalfEdge he = head.getLink();
        do {
            //System.out.println(" ?twin? " + he);
            if ((he.getStart() == head) && (he.getEnd() == tail)) {
                return he;
            }
            he = he.getNext();
        } while (he != head.getLink());

        return null;
    }

    /**
     *
     *
     *
     */
    public Face addNewFace(Point3d coord[]) {

        Vertex vert[] = new Vertex[coord.length];

        for (int i = 0; i < vert.length; i++) {

            vert[i] = findVertex(coord[i]);
            if (vert[i] == null)
                vert[i] = buildVertex(coord[i]);
        }

        return addNewFace(vert);

    }


    /**
     *
     *
     *
     */
    public Face addNewFace(Vertex vert[]) {

        Face face = buildFaceV(vert);

        HalfEdge he = face.getHe();

        do {

            HalfEdge twin = getTwin(he.getEnd(), he.getStart());
            //System.out.println("he:" + he + " twin: " + twin);
            if (twin != null) {
                betwin(he, twin);
                buildEdge(he); // create the edge!
            }
            he = he.getNext();
        } while (he != face.getHe());


        return face;

    }


    Face buildFace(List<HalfEdge> vhedges) {

        Face f = new Face();

        f.setHe(vhedges.get(0));

        int size = vhedges.size();
        for (int e = 0; e < size; e++) {

            HalfEdge he1 = vhedges.get(e);
            he1.setLeft(f);

            HalfEdge he2 = vhedges.get((e + 1) % size);
            joinHalfEdges(he1, he2);

        }

        addFace(f);

        return f;

    }

    void joinHalfEdges(HalfEdge he1, HalfEdge he2) {

        he1.setNext(he2);
        he2.setPrev(he1);

    }


    /**
     * Remove a face from the mesh.  This method does not remove any edges or vertices.
     *
     * @param f The face to remove
     */
    public void removeFace(Face f) {

        if (DEBUG) {
            System.out.println("Removing face: " + f + " hc: " + f.hashCode());
        }
        Face prev = f.getPrev();

        if (prev != null) {
            prev.setNext(f.getNext());
        } else {
            // updating head
            faces = f.getNext();
        }

        if (f == lastFace) {
            lastFace = prev;

            if (lastFace == null) {
                // just removed last face?
            } else {
                lastFace.setNext(null);
            }
        } else {
            f.getNext().setPrev(f.getPrev());
        }
    }

    /**
     * Remove the half edges making up a face.
     *
     * @param f The face to remove
     */
    public void removeHalfEdges(Face f) {

        if (DEBUG) System.out.println("Removing half edges for face: " + f + " hc: " + f.hashCode());
        HalfEdge he = f.getHe();
        do {

            removeHalfEdge(he);

            he.setPrev(null);
            he = he.getNext();

        } while (he != f.getHe());

    }

    /**
     * Remove the half edges making up a face.
     *
     * @param f The face to remove
     */
    public void removeHalfEdges(Face f, Set<Edge> removedEdges) {

        if (DEBUG) System.out.println("Removing half edges for face: " + f + " hc: " + f.hashCode());
        HalfEdge he = f.getHe();
        do {

            removeHalfEdge(he, removedEdges);

            he.setPrev(null);
            he = he.getNext();

        } while (he != f.getHe());

    }

    /**
     * Remove an edge.  Does not remove any vertices or other related structures.
     *
     * @param e The edge to remove
     */
    public void removeEdge(Edge e) {

        if (DEBUG) System.out.println("removeEdge: " + e.getUserData());

        Edge prev = e.getPrev();

        if (prev != null) {
            prev.setNext(e.getNext());
        } else {
            edges = e.getNext();
        }

        if (e == lastEdge) {
            lastEdge = prev;

            if (lastEdge == null) {
                // just removed all but last edge?
            } else {
                lastEdge.setNext(null);
            }
        } else {
            Edge next = e.getNext();

            // its possible an edge can get removed twice, this would crash.
            if (next != null) {
                next.setPrev(e.getPrev());
            }
        }
    }

    void removeHalfEdge(HalfEdge he) {

        if (DEBUG) System.out.println("removeHalfEdge()" + he);

        HalfEdge twin = he.getTwin();
        Edge e = he.getEdge();

        if (e.getHe() == he) {
            // this he is at the head of he list
            // place another one at the head
            e.setHe(he.getTwin());
        }

        if (twin == null) {
            removeEdge(e);

        } else {
            if (DEBUG) System.out.println("Clearing twin: " + twin);
            twin.setTwin(null);
        }

        he.setRemoved(true);

        // TODO: causes problems, not sure why
        if (DEBUG) System.out.println("***Removing edgeMap: " + he);
        edgeMap.remove(new HalfEdgeKey(he.getStart(), he.getEnd()));
    }

    void removeHalfEdge(HalfEdge he, Set<Edge> removedEdges) {

        if (DEBUG) System.out.println("removeHalfEdge()" + he);

        HalfEdge twin = he.getTwin();
        Edge e = he.getEdge();

        if (e.getHe() == he) {
            // this he is at the head of he list
            // place another one at the head
            e.setHe(he.getTwin());
        }

        if (twin == null) {
            removeEdge(e);
            removedEdges.add(e);

        } else {
            if (DEBUG) System.out.println("Clearing twin: " + twin);
            twin.setTwin(null);
        }

        he.setRemoved(true);

        // TODO: causes problems, not sure why
        if(DEBUG) System.out.println("Removing edgeMap: " + he);
        edgeMap.remove(new HalfEdgeKey(he.getStart(), he.getEnd()));
    }

    private void addFace(Face f) {

        /* is the list empty? */
        if (faces == null) {

            lastFace = f;
            faces = f;

        } else {

            lastFace.setNext(f);
            f.setPrev(lastFace);
            lastFace = f;
        }

        //System.out.println("addFace(): " + f.next);

    }

    private void addEdge(Edge e) {

        /* is the list empty? */
        if (edges == null) {
            lastEdge = e;
            edges = e;
        } else {
            lastEdge.setNext(e);
            e.setPrev(lastEdge);
            lastEdge = e;
        }
    }


    private HalfEdge buildHalfEdge(Vertex start, Vertex end) {

        HalfEdge he = new HalfEdge();

        he.setEnd(end);
        he.setStart(start);

        if (start.getLink() == null){
            // vertex has no link - set it 
            start.setLink(he); // link to the end vertex of this HE
        }

        return he;

    }

/*
    private HalfEdge buildHalfEdge(Vertex start, Vertex end) {

        HalfEdge he = new HalfEdge();

        he.setEnd(end);
        if (end.getLink() == null)
            end.setLink(he); // link the tail vertex to this edge
        he.setStart(start);
        return he;

    }
  */

    Edge buildEdge(HalfEdge he) {

        Edge e = new Edge();
        e.setHe(he);
        he.setEdge(e);

        if (he.getTwin() != null) {
            he.getTwin().setEdge(e);
        }

        addEdge(e);

        return e;

    }

    void betwin(HalfEdge he1, HalfEdge he2) {

        if (he1.isRemoved() || he2.isRemoved()) {
            throw new IllegalArgumentException("Trying to betwin with a dead edge: " + he1 + " he2: " + he2);
        }
        he1.setTwin(he2);
        he2.setTwin(he1);

    }

    int getFaceCount() {

        int count = 0;
        Face f = faces;

        while (f != null) {

            count++;
            f = f.getNext();

        }
        return count;
    }

    int initVertexIndices() {

        int vc = 0;
        Vertex v = vertices;
        while (v != null) {
            v.setID(vc++);
            v = v.getNext();
        }
        return vc;

    }

    /**
     * Find a vertex using a Point3D value epsilon.
     *
     * @param p
     * @return
     */
    public Vertex findVertex(Point3d p, double eps) {

        Vertex v = vertices;
        while (v != null) {
            if (v.getPoint().distanceSquared(p) < eps) {
                return v;
            }
            v = v.getNext();
        }
        return null;
    }

    /**
     * Find a vertex using a point3d reference.
     *
     * @param v
     * @return
     */
    public Vertex findVertex(Point3d v) {
        return (Vertex) tvertices.get(v);
    }

    
    /**
       class to check face Flip 
   
     */
    public static class FaceFlipChecker {

        Vector3d 
            // p0 = new Vector3d(), // we move origin to p0
            m_p1 = new Vector3d(),
            m_v0 = new Vector3d(),
            m_v1 = new Vector3d(), 
            m_n0 = new Vector3d(), 
            m_n1 = new Vector3d();   

        /**
           return true if deforrming trinagle (p0, p1, v0) into (p0, p1, v1) will flip triangle normal 
           return false otherwise
        */
        public boolean checkFaceFlip(Point3d p0, Point3d p1, Point3d v0, Point3d v1){
            
            m_p1.set(p1);
            m_v0.set(v0);
            m_v1.set(v1);

            m_p1.sub(p0);
            m_v0.sub(p0);
            m_v1.sub(p0);
            
            m_n0.cross(m_p1, m_v0); 
            
            m_n1.cross(m_p1, m_v1);

            double dot = m_n0.dot(m_n1);
        
            if(dot < 0.) // face flip 
                return true;
            else 
                return false;
        }
        
    }// class FaceFlipChecker 



}
