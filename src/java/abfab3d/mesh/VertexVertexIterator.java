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

import java.util.HashSet;
import java.util.Iterator;

/**
 * Iterate through all the neighboring vertices of a vertex.
 * <p/>
 * Traverse the face referenced by the vertex half-edge.  Recursively traverse the face of any half-edge that
 * references the vertex.
 *
 * @author Alan Hudson
 */
public class VertexVertexIterator implements Iterator<Vertex> {
    /**
     * The list of neighboring vertices
     */
    private HashSet<Vertex> list;
    private HashSet<Face> visited;
    private Iterator<Vertex> itr;

    protected VertexVertexIterator(WingedEdgeTriangleMesh mesh, Vertex v) {
        list = new HashSet<Vertex>();
        visited = new HashSet<Face>();

        findVertex(v.getLink().getLeft(), v);

        // Don't include initial vertex
        list.remove(v);

        itr = list.iterator();
    }

    private void findVertex(Face f, Vertex v) {
        if (visited.contains(f)) {
            return;
        }

        visited.add(f);

        HalfEdge he = f.getHe();
        HalfEdge start = he;

        while (he != null) {
            if (he.getStart() == v) {
                list.add(he.getEnd());

                HalfEdge twin = he.getTwin();
                if (twin != null) {
                    findVertex(twin.getLeft(), v);
                }
            } else if (he.getEnd() == v) {
                list.add(he.getStart());

                HalfEdge twin = he.getTwin();
                if (twin != null) {
                    findVertex(twin.getLeft(), v);
                }
            }
            he = he.getNext();

            if (he == start) {
                break;
            }
        }

        System.out.println("FindVertex: " + f + " v: " + v);
    }

    @Override
    public boolean hasNext() {
        return itr.hasNext();
    }

    @Override
    public Vertex next() {
        return itr.next();
    }

    @Override
    public void remove() {
        itr.remove();
    }
}
