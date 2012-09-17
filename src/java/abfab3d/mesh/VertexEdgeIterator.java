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
 * Iterate through all incident edges
 * <p/>
 *
 * @author Alan Hudson
 */
public class VertexEdgeIterator implements Iterator<Edge> {
    private static final boolean DEBUG = false;

    /**
     * The list of neighboring vertices
     */
    private HashSet<Edge> edges;
    private HashSet<Face> visited;
    private Iterator<Edge> itr;

    protected VertexEdgeIterator(WingedEdgeTriangleMesh mesh, Vertex v) {
        edges = new HashSet<Edge>();
        visited = new HashSet<Face>();

        findEdges(v.getLink().getLeft(), v);

        if (DEBUG) {
            System.out.println("Edges for vertex: " + v);
            System.out.println(edges);
        }
        itr = edges.iterator();
    }

    private void findEdges(Face f, Vertex v) {
        if (visited.contains(f)) {
            return;
        }

        visited.add(f);

        HalfEdge he = f.getHe();
        HalfEdge start = he;

        while (he != null) {
            if (he.getStart() == v) {
                edges.add(he.getEdge());

                HalfEdge twin = he.getTwin();
                if (twin != null) {
                    findEdges(twin.getLeft(), v);
                }
            } else if (he.getEnd() == v) {
                edges.add(he.getEdge());

                HalfEdge twin = he.getTwin();
                if (twin != null) {
                    findEdges(twin.getLeft(), v);
                }
            }
            he = he.getNext();

            if (he == start) {
                break;
            }
        }
    }

    @Override
    public boolean hasNext() {
        return itr.hasNext();
    }

    @Override
    public Edge next() {
        return itr.next();
    }

    @Override
    public void remove() {
        itr.remove();
    }
}
