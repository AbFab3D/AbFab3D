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

/**
 * Representation of half an edge.
 * <p/>
 * The twin of a half-edge is the opposite direction half-edge making up a typical edge.
 *
 * @author Vladimir Bulatov
 * @author Alan Hudson
 */
public class HalfEdge {
    private static final boolean DEBUG = false;

    private Vertex head;
    private Vertex tail;

    private HalfEdge next;
    private HalfEdge prev;
    private HalfEdge twin;

    private Edge edge;
    private Face left;

    public String toString() {
        String t = null;
        String h = null;

        if (tail != null) {
            t = "" + tail.getID();
        }

        if (head != null) {
            h = "" + head.getID();
        }

        return "(" + t + "->" + h + ")";
    }

    public HalfEdge getTwin() {
        return twin;
    }

    public void setTwin(HalfEdge twin) {
        if (DEBUG) System.out.println("Setting twin: " + this + " to: " + twin);
        this.twin = twin;
    }

    public Vertex getHead() {
        return head;
    }

    public void setHead(Vertex head) {
        if (DEBUG) System.out.println("Setting head: " + this + " to: " + head.getID());
        this.head = head;
    }

    public Vertex getTail() {
        return tail;
    }

    public void setTail(Vertex tail) {
        if (DEBUG) System.out.println("Setting tail: " + this + " to: " + tail.getID());
        this.tail = tail;
    }

    public HalfEdge getNext() {
        return next;
    }

    public void setNext(HalfEdge next) {
        this.next = next;
    }

    public HalfEdge getPrev() {
        return prev;
    }

    public void setPrev(HalfEdge prev) {
        this.prev = prev;
    }

    public Edge getEdge() {
        return edge;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }

    public Face getLeft() {
        return left;
    }

    public void setLeft(Face left) {
        this.left = left;
    }
}
