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

    private Vertex start;
    private Vertex end;

    private HalfEdge next;
    private HalfEdge prev;
    private HalfEdge twin;

    private Edge edge;
    private Face left;

    // TODO: Debug var, remove
    private boolean removed;

    public String toString() {
        String s = super.toString();
        s = s.substring(s.indexOf("@"), s.length());
        String start_st = null;
        String end_st = null;


        if (start != null) {
            start_st = "" + start.getID();
        }

        if (end != null) {
            end_st = "" + end.getID();
        }

        String dead = "";
        if (removed) {
            dead = " DEAD";
        }
        return s + "(" + start_st + "->" + end_st + ")" + dead;
    }

    public HalfEdge getTwin() {
        return twin;
    }

    public void setTwin(HalfEdge twin) {
        if (DEBUG) System.out.println("Setting twin: " + this + " to: " + twin);
        this.twin = twin;
    }

    public Vertex getStart() {
        return start;
    }

    public void setStart(Vertex start) {
        if (DEBUG) System.out.println("Setting start: " + this + " to: " + start.getID());
        this.start = start;
    }

    public Vertex getEnd() {
        return end;
    }

    public void setEnd(Vertex end) {
        if (DEBUG) System.out.println("Setting end: " + this + " to: " + end.getID());
        this.end = end;
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

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }
}
