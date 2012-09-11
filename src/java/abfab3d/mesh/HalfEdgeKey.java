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

    private Vertex start, end;

    public HalfEdgeKey() {
    }

    public HalfEdgeKey(Vertex start, Vertex end) {
        this.start = start;
        this.end = end;
    }

    public int hashCode() {
        return start.hashCode() + 119 * end.hashCode();
    }

    public boolean equals(Object obj) {
        HalfEdgeKey hk = (HalfEdgeKey) obj;
        return (hk.start == start) && (hk.end == end);

    }

    public Vertex getStart() {
        return start;
    }

    public void setStart(Vertex start) {
        this.start = start;
    }

    public Vertex getEnd() {
        return end;
    }

    public void setEnd(Vertex end) {
        this.end = end;
    }

    public String toString() {
        return "HalfEdgeKey: " + (start != null ? (start.getID()) : "null") + "->" + (end != null ? (end.getID()) : "null");
    }
}
