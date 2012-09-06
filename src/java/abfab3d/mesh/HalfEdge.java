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

    public Vertex head;
    public Vertex tail;

    public HalfEdge next;
    public HalfEdge prev;
    public HalfEdge twin;

    public Edge edge;
    public Face left;

    public String toString() {
        return "(" + tail.id + "->" + head.id + ")";
    }
}
