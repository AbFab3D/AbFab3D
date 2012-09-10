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
 * Key used in maps for half edges.  Allows access via either direction in map searches.
 *
 */
public class HalfEdgeKey {

    private Vertex head, tail;

    public HalfEdgeKey() {
    }

    public HalfEdgeKey(Vertex head, Vertex tail) {
        this.head = head;
        this.tail = tail;
    }

    public int hashCode() {
        return head.hashCode() + 119 * tail.hashCode();
    }

    public boolean equals(Object obj) {
        HalfEdgeKey hk = (HalfEdgeKey) obj;
        return (hk.head == head) && (hk.tail == tail);

    }

    public Vertex getHead() {
        return head;
    }

    public void setHead(Vertex head) {
        this.head = head;
    }

    public Vertex getTail() {
        return tail;
    }

    public void setTail(Vertex tail) {
        this.tail = tail;
    }

    public String toString() {
        return "HalfEdgeKey: " + (head != null ? (head.getID()) : "null") + "->" + (tail != null ? (tail.getID()) : "null");
    }
}
