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
 * Edge defined by 2 half edges.  Doubly linked list structure.
 */
public class Edge {
    private HalfEdge he;

    private Edge next;
    private Edge prev;

    public String toString() {

        if (getHe() != null){
            return "edge: " + getHe() + ":" + ((getHe().getTwin() != null) ? getHe().getTwin().toString() : "null");
        } else {
            return "edge [null]";
        }

    }

    /** One half edge, get the other via twin */
    public HalfEdge getHe() {
        return he;
    }

    public void setHe(HalfEdge he) {
        this.he = he;
    }

    /** List of all edges */
    public Edge getNext() {
        return next;
    }

    public void setNext(Edge next) {
        this.next = next;
    }

    public Edge getPrev() {
        return prev;
    }

    public void setPrev(Edge prev) {
        this.prev = prev;
    }
}
