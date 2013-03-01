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

import abfab3d.util.StructSet;
import abfab3d.util.StructMixedData;
import abfab3d.util.StructMap;
import abfab3d.util.TriangleCollector;
//import abfab3d.util.DefaultHashFunction;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.io.PrintStream;

import static abfab3d.util.Output.printf;

/**
 * 3D Triangle Mesh structure implemented using a WingedEdge datastructure.  Made for easy traversal of edges and dynamic in
 * nature for easy edge collapses.
 *
 * @author Vladimir Bulatov
 * @author Alan Hudson
 */
public class WingedEdgeTriangleMesh implements TriangleMesh {
    public static final int VA_NORMAL = 0;
    public static final int VA_COLOR = 1;
    public static final int VA_TEXCOORD0 = 2;
    public static final int VA_TEXCOORD1 = 3;
    public static final int VA_TEXCOORD2 = 4;
    public static final int VA_TEXCOORD3 = 5;
    public static final int VA_TEXCOORD4 = 6;
    public static final int VA_TEXCOORD5 = 7;
    public static final int VA_TEXCOORD6 = 8;
    public static final int VA_TEXCOORD7 = 9;

    static boolean DEBUG = false;

    private final StructMixedData vertices;  // of Vertex
    private int startVertex = -1;
    private int lastVertex = -1;
    private final StructMixedData edges; // of Edge
    private int startEdge = -1;
    private int lastEdge = -1;
    private final StructMixedData faces; // of Face
    private int startFace = -1;
    private int lastFace = -1;
    private final StructMixedData hedges; // of HalfEdge

    private StructMap edgeMap;

    private int vertexCount = 0;
    private int faceCount = 0;
    private int edgeCount = 0;

    /** The semantic definition of the attribute, 0-15 reserved for internal usage */
    private int[] semantics;


    public WingedEdgeTriangleMesh(double[] vertCoord, float[][] attribs, int[] semantics, int[] findex) {
        if (semantics != null) {
            this.semantics = semantics.clone();
        }

        int len = vertCoord.length / 3;

        edges = new StructMixedData(Edge.DEFINITION, findex.length);
        hedges = new StructMixedData(HalfEdge.DEFINITION, findex.length * 2 + 1);
        edgeMap = new StructMap((int) ((findex.length * 2 + 1) * 1.25), 0.75f, hedges,new HalfEdgeHashFunction());
        faces = new StructMixedData(Face.DEFINITION, findex.length / 3);
        int idx = 0;

        if (semantics == null || semantics.length == 0) {
            vertices = new StructMixedData(Vertex.DEFINITION,len);
            for (int nv = 0; nv < len; nv++) {
                int v = Vertex.create(vertCoord[idx++],vertCoord[idx++], vertCoord[idx++], nv, vertices);
                addVertex(v);
            }
        } else if (semantics.length == 1) {
            vertices = new StructMixedData(VertexAttribs1.DEFINITION,len);
            for (int nv = 0; nv < len; nv++) {
                int v = VertexAttribs1.create(vertCoord[idx++],vertCoord[idx++], vertCoord[idx++], nv, attribs[nv], vertices);
                addVertex(v);
            }
        } else if (semantics.length == 3) {
            vertices = new StructMixedData(VertexAttribs3.DEFINITION,len);

            for (int nv = 0; nv < len; nv++) {
                int v = VertexAttribs3.create(vertCoord[idx++],vertCoord[idx++], vertCoord[idx++], nv, attribs[nv], vertices);
                addVertex(v);
            }
        } else {
            throw new IllegalArgumentException("Unsupported number of vertex attributes: " + semantics.length);
        }

        int[] eface = new int[3];

        int[] ahedges = new int[findex.length];
        int ahedges_idx = 0;

        int[] face = new int[3];

        idx = 0;
        len = findex.length / 3;
        for (int i = 0; i < len; i++) {

            face[0] = findex[idx++];
            face[1] = findex[idx++];
            face[2] = findex[idx++];

            // Create the half edges for each face
            for (int j = 0; j < 3; j++) {

                int v1 = face[j];
                int v2 = face[(j + 1) % 3];
                //System.out.println("Build he: " + v1.getID() + " v2: " + v2.getID());
                int he = buildHalfEdge(v1, v2);
                edgeMap.put(he, he);
                ahedges[ahedges_idx++] = he;
                eface[j] = he;
            }

            // Create the face
            buildFace(eface);
        }

        boolean notifyNonManifold = true;

        int key = HalfEdge.create(hedges);

        len = ahedges.length;

        // Find the twins
        for (int i=0; i < len; i++) {
            int he1 = ahedges[i];
            //for (HalfEdge he1 : edgeMap.values()) {
            int twin = HalfEdge.getTwin(hedges, he1);
            if (twin == -1) {
                // get halfedge of _opposite_ direction

                HalfEdge.setStart(HalfEdge.getEnd(hedges, he1), hedges, key);
                HalfEdge.setEnd(HalfEdge.getStart(hedges, he1), hedges, key);

                int he2 = edgeMap.get(key);
                if (he2 != -1) {
                    betwin(he1, he2);
                    buildEdge(he1); // create the edge!
                } else {
                    if (DEBUG && notifyNonManifold) {
                        System.out.println("NonManifold hedge: " + he1 + " ? " + Vertex.getID(vertices, HalfEdge.getStart(hedges,he1)) + "->" + Vertex.getID(vertices, HalfEdge.getEnd(hedges,he1)));
                    }
                    // Null twin means its an outer edge on a non-manifold surface
                    buildEdge(he1);
                }
            }
        }
    }

    public WingedEdgeTriangleMesh(double[] vertCoord, int[] findex) {

        //printf("Creating new WE mesh from new code\n");

        int len = vertCoord.length / 3;

        vertices = new StructMixedData(Vertex.DEFINITION,len);
        edges = new StructMixedData(Edge.DEFINITION, findex.length);
        hedges = new StructMixedData(HalfEdge.DEFINITION, findex.length * 2 + 1);
        edgeMap = new StructMap((int) ((findex.length * 2 + 1) * 1.25), 0.75f, hedges,new HalfEdgeHashFunction());
        faces = new StructMixedData(Face.DEFINITION, findex.length / 3);
        int idx = 0;

        for (int nv = 0; nv < len; nv++) {
            int v = Vertex.create(vertCoord[idx++],vertCoord[idx++], vertCoord[idx++], nv, vertices);
            addVertex(v);
        }

        int[] eface = new int[3];

        int[] ahedges = new int[findex.length];
        int ahedges_idx = 0;

        int[] face = new int[3];

        idx = 0;
        len = findex.length / 3;
        for (int i = 0; i < len; i++) {

            face[0] = findex[idx++];
            face[1] = findex[idx++];
            face[2] = findex[idx++];

            // Create the half edges for each face
            for (int j = 0; j < 3; j++) {

                int v1 = face[j];
                int v2 = face[(j + 1) % 3];
                //System.out.println("Build he: " + v1.getID() + " v2: " + v2.getID());
                int he = buildHalfEdge(v1, v2);
                edgeMap.put(he, he);
                ahedges[ahedges_idx++] = he;
                eface[j] = he;
            }

            // Create the face
            buildFace(eface);
        }

        boolean notifyNonManifold = true;

        int key = HalfEdge.create(hedges);

        len = ahedges.length;

        // Find the twins
        for (int i=0; i < len; i++) {
            int he1 = ahedges[i];
            //for (HalfEdge he1 : edgeMap.values()) {
            int twin = HalfEdge.getTwin(hedges, he1);
            if (twin == -1) {
                // get halfedge of _opposite_ direction

                HalfEdge.setStart(HalfEdge.getEnd(hedges, he1), hedges, key);
                HalfEdge.setEnd(HalfEdge.getStart(hedges, he1), hedges, key);

                int he2 = edgeMap.get(key);
                if (he2 != -1) {
                    betwin(he1, he2);
                    buildEdge(he1); // create the edge!
                } else {
                    if (DEBUG && notifyNonManifold) {
                        System.out.println("NonManifold hedge: " + he1 + " ? " + Vertex.getID(vertices, HalfEdge.getStart(hedges,he1)) + "->" + Vertex.getID(vertices, HalfEdge.getEnd(hedges,he1)));
                    }
                    // Null twin means its an outer edge on a non-manifold surface
                    buildEdge(he1);
                }
            }
        }
    }

    /**
     * Get the semantic definitions of the vertices
     * @return The definitions or null if none
     */
    public int[] getSemantics() {
        return semantics.clone();
    }

    /**
     * Get the color attrib channel.
     *
     * @return The channelID or -1 if not available
     */
    public int getColorChannel() {
        if (semantics == null) {
            return -1;
        }
        for(int i=0; i < semantics.length; i++) {
            if (semantics[i] == VA_COLOR) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get the edges
     *
     * @return A linked list of edges
     */
    public final StructMixedData getEdges() {
        return edges;
    }

    public int getStartEdge() {
        return startEdge;
    }

    public int getLastEdge() {
        return lastEdge;
    }

    public final StructMixedData getVertices() {
        return vertices;
    }

    public int getStartVertex() {
        return startVertex;
    }

    public int getLastVertex() {
        return lastVertex;
    }

    public final StructMixedData getFaces() {
        return faces;
    }

    public int getStartFace() {
        return startFace;
    }

    public int getLastFace() {
        return lastFace;
    }

    public final StructMixedData getHalfEdges() {
        return hedges;
    }

    /**
     * Get the indices into the vertex array for each face.
     * @return Flat array of triangle indices
     */
    public int[] getFaceIndexes() {

        // init vert indices
        initVertexIndices();

        int[] findex = new int[faceCount*3];

        int fidx = 0;

        for (int f = startFace; f != -1; f = Face.getNext(faces, f)) {

            int startHe = Face.getHe(faces, f);
            int he = startHe;
            do {
                findex[fidx++] = HalfEdge.getStart(hedges, he);
                he = HalfEdge.getNext(hedges,he);
            } while (he != startHe);
        }

        return findex;

    }

    /**
     * Get the count of vertices in this mesh.  Local variable kept during upkeep so it's a fast operation.
     *
     * @return The count
     */
    @Override
    public int getVertexCount() {
        return vertexCount;
    }

    /**
     * Get the count of triangles in this mesh.
     *
     * @return The count
     */
    @Override
    public int getTriangleCount() {
        return faceCount;
    }

    /**
     * Get the count of edges in this mesh.
     *
     * @return The count
     */
    @Override
    public int getEdgeCount() {
        return edgeCount;
    }


    // debug
    //int collapseCnt = 0;

    /**
     * Collapse an edge.
     *
     * @param e   The edge to collapse
     * @param pos The position of the new common vertex
     */
    public boolean collapseEdge(int e, Point3d pos, EdgeCollapseParams ecp, EdgeCollapseResult ecr) {
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

        // debug
        //collapseCnt++;

        int hR = Edge.getHe(edges, e);
        int v0 = HalfEdge.getEnd(hedges,hR);
        int v1 = HalfEdge.getStart(hedges,hR);

        int he;// working variable

        int hL = HalfEdge.getTwin(hedges, hR);
        int fL = HalfEdge.getLeft(hedges, hL);
        int fR = HalfEdge.getLeft(hedges, hR);

        if (DEBUG) printf("fR: %s\nfL: %s", fR, fL);

        int
                hLp = HalfEdge.getPrev(hedges, hL),
                hLpt = HalfEdge.getTwin(hedges,hLp),
                hLn = HalfEdge.getNext(hedges,hL),
                hLnt = HalfEdge.getTwin(hedges, hLn),
                hRn = HalfEdge.getNext(hedges, hR),
                hRnt = HalfEdge.getTwin(hedges,hRn),
                hRp = HalfEdge.getPrev(hedges,hR),
                hRpt = HalfEdge.getTwin(hedges,hRp);


        int
                vR = HalfEdge.getStart(hedges, hRp),
                vL = HalfEdge.getEnd(hedges, hLn);
        int
                e1R = HalfEdge.getEdge(hedges, hRp),
                e1L = HalfEdge.getEdge(hedges, hLn),
                e0R = HalfEdge.getEdge(hedges, hRn),
                e0L = HalfEdge.getEdge(hedges, hLp);

        //
        // check if collapse may cause surface pinch
        // the illeagal collapse is the following:
        // if edge adjacent to v0 have common vertex with edge adjacent to v1 (except edges, which surround fR and fL )
        // then our collapse will cause surface pinch aong such edge
        //
        if(ecp.testSurfacePinch){
            StructSet v1set = ecr.v1set;
            v1set.clear();
            he = HalfEdge.getNext(hedges,hLnt);
            
            // make cycle about v1
            while (he != hRpt) {
                
                v1set.add(HalfEdge.getEnd(hedges,he));
                he = HalfEdge.getNext(hedges, HalfEdge.getTwin(hedges, he));
            }
            // v1set has all the vertices connected to v1
            
            // make cycle about v0
            he = HalfEdge.getNext(hedges,hRnt);
            
            while (he != hLpt) {
                
                if (v1set.contains(HalfEdge.getEnd(hedges,he))) {
                    if (DEBUG) printf("!!!illegal collapse. Surface pinch detected!!!\n");
                    ecr.returnCode = EdgeCollapseResult.FAILURE_SURFACE_PINCH;
                    return false;
                }
                
                he = HalfEdge.getNext(hedges, HalfEdge.getTwin(hedges, he));
            }
            //
            // if we are here - no surface pinch occurs.
        }

        // get scratch variables 
        Point3d p0 = ecr.p0, p1 = ecr.p1, pv0 = ecr.pv0, pv1 = ecr.pv1;

        // check if face flip occur
        // face flip means, that face normals change direction to opposite after vertex was moved
        // check face flip of faces connected to v1
        he = hLnt;

        FaceFlipChecker faceFlipChecker = ecr.faceFlipChecker;
        Vertex.getPoint(vertices, v1,pv1);

        if (ecp.maxEdgeLength2 > 0.) {
            while (he != hRp) {
                int start = HalfEdge.getStart(hedges, he);
                int next = HalfEdge.getNext(hedges, he);
                Vertex.getPoint(vertices,start,p0);
                Vertex.getPoint(vertices,HalfEdge.getEnd(hedges,next),p1);

                // we need to check for max edge
                if (pos.distanceSquared(ecr.p0) > ecp.maxEdgeLength2) {
                    ecr.returnCode = EdgeCollapseResult.FAILURE_LONG_EDGE;
                    return false;
                }

                if (faceFlipChecker.checkFaceFlip(p0, p1, pv1, pos)) {
                    ecr.returnCode = EdgeCollapseResult.FAILURE_FACE_FLIP;
                    return false;
                }

                he = HalfEdge.getTwin(hedges, next);
            }
        } else {
            // Avoid per edge check of max edgeLength
            while (he != hRp) {
                int start = HalfEdge.getStart(hedges, he);
                int next = HalfEdge.getNext(hedges, he);
                Vertex.getPoint(vertices,start,p0);
                Vertex.getPoint(vertices,HalfEdge.getEnd(hedges,next),p1);

                if (faceFlipChecker.checkFaceFlip(p0, p1, pv1, pos)) {
                    ecr.returnCode = EdgeCollapseResult.FAILURE_FACE_FLIP;
                    return false;
                }

                he = HalfEdge.getTwin(hedges, next);
            }
        }

        // check face flip of faces connected to v0
        he = hRnt;
        Vertex.getPoint(vertices,v0,pv0);

        if (ecp.maxEdgeLength2 > 0.) {
            while (he != hLp) {
                int start = HalfEdge.getStart(hedges, he);
                int next = HalfEdge.getNext(hedges, he);
                Vertex.getPoint(vertices,start,p0);
                Vertex.getPoint(vertices,HalfEdge.getEnd(hedges,next),p1);

                // we need to check for max edge
                if (pos.distanceSquared(p0) > ecp.maxEdgeLength2) {
                    ecr.returnCode = EdgeCollapseResult.FAILURE_LONG_EDGE;
                    return false;
                }

                if (faceFlipChecker.checkFaceFlip(p0, p1, pv0, pos)) {
                    ecr.returnCode = EdgeCollapseResult.FAILURE_FACE_FLIP;
                    return false;
                }
                he = HalfEdge.getTwin(hedges, next);
            }
        } else {
            // Avoid per edge check of max edgeLength
            while (he != hLp) {
                int start = HalfEdge.getStart(hedges, he);
                int next = HalfEdge.getNext(hedges, he);
                Vertex.getPoint(vertices,start,p0);
                Vertex.getPoint(vertices,HalfEdge.getEnd(hedges,next),p1);

                if (faceFlipChecker.checkFaceFlip(p0, p1, pv0, pos)) {
                    ecr.returnCode = EdgeCollapseResult.FAILURE_FACE_FLIP;
                    return false;
                }
                he = HalfEdge.getTwin(hedges, next);
            }
        }

        //
        //  Proceed with collapse.
        //
        // move v1 to new position
        Vertex.setPoint(pos.x, pos.y, pos.z, vertices, v1);

        // remove all removable edges
        removeEdge(e);
        removeEdge(e0R);
        removeEdge(e0L);

        if (DEBUG) {
            printf("v0: %s, v1: %s\n", v0, v1);
            printf("vR: %s, vL: %s\n", vL, vR);
            printf("hL: %s,  hR: %s\n", hL, hR);
            printf("hLp:%s hLpt: %s\n", hLp, hLpt);
            printf("hLn:%s hLnt: %s\n", hLn, hLnt);
            printf("hRp:%s hRpt: %s\n", hRp, hRpt);
            printf("hRn:%s hRnt: %s\n", hRn, hRnt);
        }
        //
        // relink all edges from v0 to v1
        //
        int end = hLp;
        he = hRnt;
        int maxcount = 100; // to avoid infinite cycle if cycle is broken
        if (DEBUG) printf("moving v0-> v1\n");
        do {
            int next = HalfEdge.getNext(hedges,he);
            if (DEBUG) printf("  before he: %s; next: %s ", he, next);
            HalfEdge.setEnd(v1, hedges, he);
            HalfEdge.setStart(v1, hedges,next);

            if (DEBUG) printf("   after  he: %s; next: %s\n", he, next);
            if (--maxcount < 0) {
                printf("!!!!!error: maxcount exceeded!!!!! for vertex: %d\n", Vertex.getID(vertices, v0));
                break;
            }
            he = HalfEdge.getTwin(hedges,next);
        } while (he != end);

        //
        // remove collapsed faces
        //
        removeFace(fL);
        removeFace(fR);
        //
        // close outer sides of removed faces
        betwin(hRnt, hRpt);
        betwin(hLpt, hLnt);
        HalfEdge.setEdge(e1R, hedges, hRnt);
        Edge.setHe(hRnt, edges, e1R);
        HalfEdge.setEdge(e1L, hedges, hLpt);
        Edge.setHe(hLpt, edges, e1L);
        //
        // reset link to HalfEdges on modified vertices
        //
        Vertex.setLink(hLnt, vertices, vL);
        Vertex.setLink(hRnt, vertices, vR);
        Vertex.setLink(hRpt, vertices, v1);

        // release pointers to removed edges
        Edge.setHe(-1, edges, e);
        Edge.setHe(-1, edges, e0L);
        Edge.setHe(-1, edges, e0R);

        // remove the vertex
        if (DEBUG) printf("removing vertex: %d\n", Vertex.getID(vertices, v0));
        removeVertex(v0);

        // compose result
        ecr.removedEdges[0] = e;
        ecr.removedEdges[1] = e0L;
        ecr.removedEdges[2] = e0R;

        ecr.insertedVertex = v1;
        ecr.faceCount = 2;
        ecr.edgeCount = 3;
        ecr.vertexCount = 1;
        ecr.returnCode = EdgeCollapseResult.SUCCESS;

        return true;

    } // collapseEdge()


    public void writeOBJ(PrintStream out) {

        int f;
        int v;
        int e;

        int vc = 0;
        double[] pnt = new double[3];

        v = startVertex;
        while (v != -1) {
            Vertex.getPoint(vertices, v, pnt);
            out.println("v (" + pnt[0] + ", " + pnt[1] + ", " + pnt[2] + ") id: " + Vertex.getID(vertices,v));
            Vertex.setID(vc++, vertices, v);
            v = Vertex.getNext(vertices, v);
        }

        for (f = startFace; f != -1; f = Face.getNext(faces,f)) {

            out.print("f");
            int startHe = Face.getHe(faces,f);
            int he = startHe;

            do {
                int startv = HalfEdge.getStart(hedges,he);
                out.print(" " + Vertex.getID(vertices,startv));
                he = HalfEdge.getNext(hedges,he);
            } while (he != startHe);

            out.println();
        }

        for (e = startEdge; e != -1; e = Edge.getNext(edges, e)) {

            int he = Edge.getHe(edges, e);
            int start = HalfEdge.getStart(hedges, he);
            int end = HalfEdge.getEnd(hedges, he);

            out.print("e " + Vertex.getID(vertices, start) + " " + Vertex.getID(vertices, end));

            int twin = HalfEdge.getTwin(hedges, he);
            if (twin != -1) {
                start = HalfEdge.getStart(hedges, twin);
                end = HalfEdge.getEnd(hedges, twin);
                out.print("  tw: " + Vertex.getID(vertices, start) + " " + Vertex.getID(vertices, end));
            } else
                out.print("  tw: null");

            System.out.println();
        }

        int[] entries = edgeMap.entrySet();
        int len = entries.length / 2;

        System.out.println("EdgeMap: ");
        for (int i=0; i < len; i++) {
            System.out.println(HalfEdge.toString(hedges, entries[i*2]) + " val: " + HalfEdge.toString(hedges, entries[i*2+1]));
        }
    }

    private void addVertex(int v) {

        /* is the list empty? */
        if (startVertex == -1) {
            startVertex = v;
            lastVertex = v;
        } else {
            Vertex.setNext(v, vertices, lastVertex);
            Vertex.setPrev(lastVertex, vertices, v);
            lastVertex = v;
        }

        vertexCount++;
    }

    private int buildFace(int[] hedges) {

        int f = Face.create(hedges[0],faces);

        int size = hedges.length;
        for (int e = 0; e < size; e++) {

            int he1 = hedges[e];

            HalfEdge.setLeft(f, this.hedges, he1);

            int he2 = hedges[(e + 1) % size];
            joinHalfEdges(he1, he2);

        }

        addFace(f);

        return f;

    }

    private void joinHalfEdges(int he1, int he2) {
        HalfEdge.setNext(he2, hedges, he1);
        HalfEdge.setPrev(he1, hedges, he2);
    }

    /**
     * Remove a face from the mesh.  This method does not remove any edges or vertices.
     *
     * @param f The face to remove
     */
    public void removeFace(int f) {
        if (Face.getHe(faces,f) == -1) {
            // already removed, ignore
            return;
        }

        if (DEBUG) {
            System.out.println("Removing face: " + f);
        }
        int prev = Face.getPrev(faces,f);

        if (prev != -1) {
            Face.setNext(Face.getNext(faces,f), faces, prev);
        } else {
            // updating head
            startFace = Face.getNext(faces,f);
        }

        if (f == lastFace) {
            lastFace = prev;

            if (lastFace == -1) {
                // just removed last face?
            } else {
                Face.setNext(-1,faces,lastFace);
            }
        } else {
            int next = Face.getNext(faces,f);
            Face.setPrev(prev, faces, next);
        }

        Face.setHe(-1,faces,f);
        faceCount--;
    }

    /**
     * Remove an edge.  Does not remove any vertices or other related structures.
     *
     * @param e The edge to remove
     */
    public void removeEdge(int e) {
        if (Edge.getHe(edges,e) == -1) {
            // already removed, ignore
            return;
        }

        if (DEBUG) {
            System.out.println("Removing Edge: " + e);
        }
        int prev = Edge.getPrev(edges,e);

        if (prev != -1) {
            Edge.setNext(Edge.getNext(edges,e), edges, prev);
        } else {
            // updating head
            startEdge = Edge.getNext(edges,e);
        }

        if (e == lastEdge) {
            lastEdge = prev;

            if (lastEdge == -1) {
                // just removed last edge?
            } else {
                Edge.setNext(-1,edges,lastEdge);
            }
        } else {
            int next = Edge.getNext(edges,e);

            // its possible an edge can get removed twice, this would crash.
            if (next != -1) {
                Edge.setPrev(prev, edges, next);
            } else {
                return; // Do not alter edgeCount
            }
        }

        Edge.setHe(-1,edges,e);
        edgeCount--;
    }

    public void removeVertex(int v) {
        if (Vertex.getLink(vertices,v) == -1) {
            // already removed, ignore
            return;
        }

        if (DEBUG) {
            System.out.println("Removing vertex: " + v);
        }
        int prev = Vertex.getPrev(vertices, v);

        if (prev != -1) {
            Vertex.setNext(Vertex.getNext(vertices, v), vertices, prev);
        } else {
            // updating head
            startVertex = Vertex.getNext(vertices, v);
        }

        if (v == lastVertex) {
            lastVertex = prev;

            if (lastVertex == -1) {
                // just removed last face?
            } else {
                Vertex.setNext(-1, vertices,lastVertex);
            }
        } else {
            int next = Vertex.getNext(vertices,v);
            Vertex.setPrev(prev, vertices, next);
        }

        Vertex.setLink(-1, vertices, v);
        Vertex.setNext(-1, vertices, v);

        vertexCount--;
    }


    private void addFace(int f) {

        /* is the list empty? */
        if (startFace == -1) {
            startFace = f;
            lastFace = f;
        } else {
            Face.setNext(f, faces, lastFace);
            Face.setPrev(lastFace, faces, f);
            lastFace = f;
        }

        faceCount++;
    }

    private void addEdge(int e) {
        /* is the list empty? */
        if (startEdge == -1) {
            startEdge = e;
            lastEdge = e;
        } else {
            Edge.setNext(e, edges, lastEdge);
            Edge.setPrev(lastEdge, edges, e);
            lastEdge = e;
        }

        edgeCount++;
    }

    private int buildHalfEdge(int start, int end) {

        int he = HalfEdge.create(start,end, hedges);

        int link = Vertex.getLink(vertices, start);

        if (link == -1) {
            // vertex has no link - set it
            Vertex.setLink(he, vertices, start);  // link to the end vertex of this HE
        }

        return he;

    }

    int buildEdge(int he) {

        int e = Edge.create(he, edges);
        HalfEdge.setEdge(e, hedges, he);
        int twin = HalfEdge.getTwin(hedges, he);
        if (twin != -1) {
            HalfEdge.setEdge(e, hedges, twin);
        }

        addEdge(e);

        return e;
    }

    private void betwin(int he1, int he2) {

        HalfEdge.setTwin(he2, hedges, he1);
        HalfEdge.setTwin(he1, hedges, he2);
    }

    @Override
    public int getFaceCount() {
        return faceCount;
    }

    /**
     * Insure vertex indices are continuous.  When vertices are removed they make holes in the ID numbers.
     * @return
     */
    private void initVertexIndices() {

        int vc = 0;
        int v = startVertex;

        while (v != -1) {
            Vertex.setID(vc++, vertices, v);
            v = Vertex.getNext(vertices, v);
        }
    }

    /**
     * Find a vertex using a Point3D value epsilon.
     *
     * @param p
     * @return The vertexID or -1
     */
    @Override
    public int findVertex(double[] p, double eps) {

        int v = startVertex;
        double[] pnt = new double[3];

        while (v != -1) {
            Vertex.getPoint(vertices, v, pnt);

            if (distanceSquared(pnt,p) < eps) {
                return v;
            }
            v = Vertex.getNext(vertices,v);
        }
        return -1;
    }

    private double distanceSquared(final double[] p0, final double[] p1) {
        double dx, dy, dz;
        dx = p0[0]-p1[0];

        dy = p0[1]-p1[1];

        dz = p0[2]-p1[2];

        return (dx*dx+dy*dy+dz*dz);
    }

    /**
     * return bounds of the mesh
     * as double[]{xmin,xmax, ymin, ymax, zmin, zmax}
     */
    @Override
    public double[] getBounds() {

        double
                xmin = Double.MAX_VALUE, xmax = Double.MIN_VALUE,
                ymin = Double.MAX_VALUE, ymax = Double.MIN_VALUE,
                zmin = Double.MAX_VALUE, zmax = Double.MIN_VALUE;

        double[] pnt = new double[3];
        
        for(int v = startVertex; v != 1; v = Vertex.getNext(vertices, v)) {

            Vertex.getPoint(vertices, v, pnt);

            if (pnt[0] < xmin) xmin = pnt[0];
            if (pnt[0] > xmax) xmax = pnt[0];

            if (pnt[1] < ymin) ymin = pnt[1];
            if (pnt[1] > ymax) ymax = pnt[1];

            if (pnt[2] < zmin) zmin = pnt[2];
            if (pnt[2] > zmax) zmax = pnt[2];

        }

        return new double[]{xmin, xmax, ymin, ymax, zmin, zmax};

    }

    /**
     * feeds all triangular faces into TriangleCollector
     */
    @Override
    public void getTriangles(TriangleCollector tc) {
        Vector3d
                p0 = new Vector3d(),
                p1 = new Vector3d(),
                p2 = new Vector3d();

        double[] pnt = new double[3];

        int f = startFace;
        while(f != -1) {
            int he = Face.getHe(faces, f);
            int he1 = HalfEdge.getNext(hedges,he);
            int
                    v0 = HalfEdge.getStart(hedges, he),
                    v1 = HalfEdge.getEnd(hedges, he),
                    v2 = HalfEdge.getEnd(hedges, he1);

            Vertex.getPoint(vertices, v0, pnt);
            p0.set(pnt);
            Vertex.getPoint(vertices, v1, pnt);
            p1.set(pnt);
            Vertex.getPoint(vertices, v2, pnt);
            p2.set(pnt);

            tc.addTri(p0, p1, p2);

            f = Face.getNext(faces, f);
        }
    }


    /**
     * Verify that the mesh structure is correct.  Chase as many pointers and references as we can to confirm that
     * nothing is messed up.
     *
     * @param mesh
     * @return
     */
    public boolean verifyCounts(TriangleMesh mesh) {
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

            e = Edge.getNext(edges,e);
        }

        System.out.println("Edges:  class: " + mesh.getEdgeCount() + " cnt: " + edgeCount);

        // Make sure all faces have three half edges
        // Make sure all edge and face references in halfedge are valid
        // Make sure forward traversal(next) around face is same as backwards(prev)

        int f = startFace;
        int faceCount = 0;
        while(f != -1) {
            faceCount++;

            f = Face.getNext(faces,f);
        }

        System.out.println("Faces:  class: " + mesh.getFaceCount() + " cnt: " + faceCount);

        int v = startVertex;
        int vertexCount = 0;
        while(v != -1) {
            vertexCount++;
            v = Vertex.getNext(vertices,v);
        }
        System.out.println("Vertices:  class: " + mesh.getVertexCount() + " cnt: " + vertexCount);


        return true;
    }


    /**
     * Verify that the mesh structure is correct.  Chase as many pointers and references as we can to confirm that
     * nothing is messed up.
     *
     * @param mesh
     */
    public void verifyEdgeCount(TriangleMesh mesh, int collapse, int collapseCnt) {
        StructMixedData edges = mesh.getEdges();
        int startEdge = mesh.getStartEdge();

        int e = startEdge;

        int edgeCount = 0;

        while(e != -1) {
            edgeCount++;
            e = Edge.getNext(edges,e);
        }

        if (mesh.getEdgeCount() != edgeCount) {
            System.out.println("Collapse: " + collapseCnt + " Bad edge: " + collapse + " count: " + edgeCount + " expected: " + mesh.getEdgeCount());
            throw new IllegalArgumentException("Counts don't match");
        }
    }

    // area of small trinagle to be rejected as face flip  (in m^2)
    static final double FACE_FLIP_EPSILON = 1.e-20;

}

