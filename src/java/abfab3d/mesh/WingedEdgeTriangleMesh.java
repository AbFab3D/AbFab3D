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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 3D Triangle Mesh structure implemented using a WingedEdge datastructure.  Made for easy traversal of edges and dynamic in
 * nature for easy edge collapses.
 *
 * @author Vladimir Bulatov
 * @author Alan Hudson
 */
public class WingedEdgeTriangleMesh {

    private Face faces;
    private Face lastFace;

    private Vertex vertices;
    private Vertex lastVertex;

    private HashMap<Point3d, Vertex> tvertices = new HashMap<Point3d, Vertex>();

    private Edge edges;
    private Edge lastEdge;

    private int vertexCount = 0;

    public WingedEdgeTriangleMesh(Point3d vertCoord[], int[][] findex) {

        Vertex V[] = new Vertex[vertCoord.length];

        for (int nv = 0; nv < V.length; nv++) {

            V[nv] = new Vertex();
            V[nv].p = new Point3d(vertCoord[nv]);
            V[nv].id = nv;
        }

        ArrayList eface = new ArrayList(findex.length * 3);

        HashMap<HalfEdgeKey, HalfEdge> tedges = new HashMap<HalfEdgeKey, HalfEdge>();

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
                tedges.put(new HalfEdgeKey(he.tail, he.head), he);
                eface.add(he);
            }

            // Create the face
            buildFace(eface);

        }

        // Find the twins
        for (HalfEdge he1 : tedges.values()) { //for(int i = 0; i < hesize-1; i++){
            if (he1.twin == null) {
                // get halfedge of _opposite_ direction
                HalfEdge he2 = (HalfEdge) tedges.get(new HalfEdgeKey(he1.head, he1.tail));
                if (he2 != null) {
                    betwin(he1, he2);
                    buildEdge(he1); // create the edge!
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

    public Vertex getVertices() {
        return vertices;
    }

    public Vertex[][] getFaces() {

        // init vert indices
        initVertexIndices();

        int faceCount = getFaceCount();

        Vertex[][] findex = new Vertex[faceCount][];
        int fcount = 0;

        for (Face f = faces; f != null; f = f.next) {

            Vertex[] face = new Vertex[3];
            findex[fcount++] = face;
            int v = 0;
            HalfEdge he = f.he;
            do {
                face[v++] = he.head;
                he = he.next;
            } while (he != f.he);
        }

        return findex;

    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void writeOBJ(PrintStream out) {

        Face f;
        Vertex v;
        Edge e;
        int counter = 0;

        for (v = vertices; v != null; v = v.next) {
            out.println("v " + v.p);
            v.id = counter;
            counter++;
        }

        for (f = faces; f != null; f = f.next) {

            out.print("f");
            HalfEdge he = f.he;
            do {
                out.print(" " + he.head.id);
                he = he.next;
            } while (he != f.he);

            out.println();
        }

        for (e = edges; e != null; e = e.next) {

            out.print("e " + e.he.head.id + " " + e.he.tail.id);
            HalfEdge twin = e.he.twin;
            if (twin != null)
                out.println("  tw: " + twin.head.id + " " + e.he.twin.tail.id);
            else
                out.println("  tw: null");

        }
    }

    Vertex buildVertex(Point3d p) {

        Vertex v = new Vertex();
        v.p = p;
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

            lastVertex.next = v;
            lastVertex = v;

        }

        v.id = vertexCount++;

        //System.out.println("index: " + v.index);

        tvertices.put(v.p, v);

    }

    public void removeVertex(Vertex v) {

        Vertex prev = getPreviousVertex(v);
        if (prev != null) {
            prev.next = v.next;
            if (v == lastVertex) {
                lastVertex = prev;
                lastVertex.next = null;
            }
        }


        tvertices.remove(v);
        vertexCount--;

    }


    Face buildFace(HalfEdge hedges[]) {

        Face f = new Face();

        f.he = (HalfEdge) hedges[0];

        int size = hedges.length;
        for (int e = 0; e < size; e++) {

            HalfEdge he1 = hedges[e];
            he1.left = f;
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
        HalfEdge he = head.link;
        do {
            //System.out.println(" ?twin? " + he);
            if ((he.head == head) && (he.tail == tail)) {
                return he;
            }
            he = he.next;
        } while (he != head.link);

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

        HalfEdge he = face.he;

        do {

            HalfEdge twin = getTwin(he.tail, he.head);
            //System.out.println("he:" + he + " twin: " + twin);
            if (twin != null) {
                betwin(he, twin);
                buildEdge(he); // create the edge!
            }
            he = he.next;
        } while (he != face.he);


        return face;

    }


    Face buildFace(List<HalfEdge> vhedges) {

        Face f = new Face();

        f.he = vhedges.get(0);

        int size = vhedges.size();
        for (int e = 0; e < size; e++) {

            HalfEdge he1 = vhedges.get(e);
            he1.left = f;

            HalfEdge he2 = vhedges.get((e + 1) % size);
            joinHalfEdges(he1, he2);

        }

        addFace(f);

        return f;

    }

    void joinHalfEdges(HalfEdge he1, HalfEdge he2) {

        he1.next = he2;
        he2.prev = he1;

    }


    public Vertex getPreviousVertex(Vertex vert) {

        for (Vertex v = vertices; v != null; v = v.next) {

            if (v.next == vert)
                return v;
        }

        return null;

    }

    public void removeFace(Face f) {

        Face prev = getPreviousFace(f);

        if (prev != null)
            prev.next = f.next;

        if (f == lastFace) {
            lastFace = prev;
            lastFace.next = null;
        }

    }

    public void removeHalfEdges(Face f) {

        HalfEdge he = f.he;
        do {

            removeHalfEdge(he);

            he.prev = null;
            he = he.next;

        } while (he != f.he);

    }

    public void removeEdge(Edge e) {

        //System.out.println("removeEdge: " + e);
        Edge prev = getPreviousEdge(e);
        if (prev != null)
            prev.next = e.next;
        if (e == lastEdge) {
            lastEdge = prev;
            lastEdge.next = null;
        }
    }

    public Edge getPreviousEdge(Edge edge) {

        for (Edge e = edges; e != null; e = e.next) {

            if (e.next == edge)
                return e;
        }

        return null;

    }

    void removeHalfEdge(HalfEdge he) {

        //System.out.println("removeHalfEdge()" + he);

        HalfEdge twin = he.twin;
        Edge e = he.edge;

        if (e.he == he) {
            // this he is at the head of he list
            // place another one at the head
            e.he = he.twin;
        }

        //he.twin = null;

        if (twin == null) {

            removeEdge(e);

        } else {

            twin.twin = null;

        }


    }

    public Face getPreviousFace(Face face) {

        for (Face f = faces; f != null; f = f.next) {

            if (f.next == face)
                return f;
        }

        return null;

    }

    private void addFace(Face f) {

        /* is the list empty? */
        if (faces == null) {

            lastFace = f;
            faces = f;

        } else {

            lastFace.next = f;
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

            lastEdge.next = e;
            lastEdge = e;

        }
    }

    private HalfEdge buildHalfEdge(Vertex tail, Vertex head) {

        HalfEdge he = new HalfEdge();

        he.tail = tail;
        if (tail.link == null)
            tail.link = he; // link the tail vertex to this edge
        he.head = head;
        return he;

    }

    Edge buildEdge(HalfEdge he) {

        Edge e = new Edge();
        e.he = he;
        he.edge = e;
        he.twin.edge = e;
        addEdge(e);

        return e;

    }

    void betwin(HalfEdge he1, HalfEdge he2) {

        he1.twin = he2;
        he2.twin = he1;

    }

    int getFaceCount() {

        int count = 0;
        Face f = faces;

        while (f != null) {

            count++;
            f = f.next;

        }
        return count;
    }

    int initVertexIndices() {

        int vc = 0;
        Vertex v = vertices;
        while (v != null) {
            v.id = vc++;
            v = v.next;
        }
        return vc;

    }

    public Vertex findVertexSlow(Point3d p) {

        Vertex v = vertices;
        double EPS2 = 1.e-10;
        while (v != null) {
//      if(v.p.dist2(p) < EPS2){
            if (v.p.distanceSquared(p) < EPS2) {
                return v;
            }
            v = v.next;
        }
        return null;
    }

    public Vertex findVertex(Point3d v) {

        return (Vertex) tvertices.get(v);

    }


    /**
     * search for pyramid with p as a head vertex and replaces it with prims made on the base of
     * of that pyramid
     */
    /*
      public void addPrism(Point3d p, double capHeight, double capRadius, double capTopScale,
                           boolean straightenBase, double baseRadius){

        //System.out.println("addPrism()");
        initVertexIndices(); // for debug

        Vertex headVertex = findVertex(p);
        if(headVertex == null){
          headVertex = findVertexSlow(p);
          if(headVertex == null){
            System.out.println("vertex: " + p);
            System.out.println("WingedEdge.addPrism(): failed to find Vertex");
            Thread.dumpStack();
            return;
          }
          //return;
        }

        //System.out.println("vertex found: " + headVertex.p);

        HalfEdge he = headVertex.link;

        Vector baseV = new Vector();
        do{
          //
          // traverse all faces adjacent to the vertex, and collect all vertices, which remains after
          // removing that face
          //
          //System.out.println("edge :" + he.tail.index + "-" + he.head.index);
          baseV.add(he.head);
          removeFace(he.left);
          removeHalfEdges(he.left);

          //facesToRemove.add(he.left);

          if(he.twin == null)
            break;
          he = he.twin.next;
        }while(he != headVertex.link);

        removeVertex(headVertex);


        int baseSize = baseV.size();
        Vertex base[] = new Vertex[baseSize];
        baseV.toArray(base);

        // reverse array of vertices
        for(int i = 0; i < baseSize/2; i++){

          int i2 = baseSize-i-1;
          Vertex t = base[i2];
          base[i2] = base[i];
          base[i] = t;
        }

        Point3d baseCenter = new Point3d();
        for(int i = 0; i < baseSize; i++){
          baseCenter.addSet(base[i].p);
        }
        baseCenter.mulSet(1./baseSize);

        Point3d spine = headVertex.p.sub(baseCenter);
        Point3d axis = new Point3d(spine);
        axis.normalize();

        Point3d basep[] = new Point3d[base.length];
        for(int i=0; i < basep.length; i++){
          basep[i] = base[i].p;
        }

        Point3d dbase[] = PointSimulator.distrubute_points_on_circle(basep, baseCenter, axis);

        Vertex upperBase[] = new Vertex[baseSize*2];

        for(int i = 0; i < baseSize; i++){

          if(straightenBase){
            // make some scaling of the base
            base[i].p.set(baseCenter.add(base[i].p.sub(baseCenter).mul(baseRadius)));

          }

          Point3d bp1 = dbase[i].sub(baseCenter);
          Point3d bp2 = dbase[(i+1)%baseSize].sub(baseCenter);
          bp2 = bp2.add(bp1);

          // we are making upper base with double count of vertices
          // make bp orthogonal to axis  //bp.subSet(axis.mul(bp.dot(axis)));
          bp1.normalize();
          bp2.normalize();

          Point3d vp1 = bp1.mulSet(capRadius).add(baseCenter).add(spine);
          upperBase[2*i] = buildVertex(vp1);
          Point3d vp2 = bp2.mulSet(capRadius).add(baseCenter).add(spine);
          upperBase[2*i+1] = buildVertex(vp2);

        }


        //writeOBJ(System.out);

        Vertex[] side = new Vertex[3];

        for(int i = 0; i < base.length; i++){

          side[0] = upperBase[(2*i+1)];
          side[1] = upperBase[2*i];
          side[2] = base[i];
          addNewFace(side);

          side[0] = upperBase[(2*i+1)];
          side[1] = base[i];
          side[2] = base[(i+1)%base.length];
          addNewFace(side);
          side[0] = upperBase[(2*i+2)%upperBase.length];
          side[1] = upperBase[(2*i+1)];
          side[2] = base[(i+1)%base.length];
          addNewFace(side);

        }


        Point3d capTopCenter = baseCenter.add(spine.add(axis.mul(capRadius*capHeight)));
        Vertex capTop[] = new Vertex[3];
        for(int i=0; i < capTop.length; i++){

          Point3d capVert = dbase[i].sub(baseCenter);
          capVert.normalize();
          capVert.mulSet(capRadius*capTopScale);
          capTop[i] = buildVertex(capTopCenter.add(capVert));
        }

        side[0] = capTop[0];
        side[1] = capTop[1];
        side[2] = capTop[2];
        addNewFace(side);

        for(int i = 0; i < capTop.length; i++){


          side[0] = upperBase[(2*i+1)];
          side[1] = capTop[i];
          side[2] = upperBase[2*i];
          addNewFace(side);

          side[0] = capTop[i];
          side[1] = upperBase[(2*i+1)];
          side[2] = capTop[(i+1)%base.length];
          addNewFace(side);
          side[0] = upperBase[(2*i+2)%upperBase.length];
          side[1] = capTop[(i+1)%base.length];
          side[2] = upperBase[(2*i+1)];
          addNewFace(side);

        }


        //writeOBJ(System.out);

      }

    */

    //
    //  returns Voronoy polygon around given vertex
    //
    //
/*
  public Point3d[] getVoronoyCell(Point3d center){
    
    Vertex vert = findVertex(center);

    HalfEdge he = vert.link;
    if(he == null){
      System.out.println("bad vertex without link!");
      Thread.dumpStack();
      System.out.println("");
      return new Point3d[0];
    }

    Vector vv = new Vector();

    do{
      vv.add(he.head); 
      //CCW order
      if(he.prev.twin == null) // it is boundary vertex - no Voronoy cell for it
        return null;
      he = he.prev.twin;        
      // CW order of vertices 
      //if(he.twin == null) // it is boundary vertex - no Voronoy cell for it
      //  return null;      
      //he = he.twin.next;      

    }while(he != vert.link);
    
    Point3d[] vout = new Point3d[vv.size()];
    for(int i=0; i < vout.length; i++){
      Point3d 
        v0 = vert.p, 
        v1 = ((Vertex)vv.elementAt(i)).p,
        v2 = ((Vertex)vv.elementAt((i+1)%vout.length)).p;

      double circle[] = Qhull.circumcenter(v0.x, v0.y, v1.x, v1.y, v2.x, v2.y);
      vout[i] = new Point3d(circle[0], circle[1], 0);
    }
    return vout;
  }
*/
/*
  public Point3d getFaceCenter(HalfEdge he){

    int count = 1;
    Point3d center = new Point3d(he.head.p);
    HalfEdge start = he;
    while(he.next != start){
      he = he.next;
      center.addSet(he.head.p);
      count++;
    }
    center.mulSet(1./count);

    return center;
  }

  */
}
