package abfab3d.mesh;

import abfab3d.util.TriangleCollector;

import javax.vecmath.Point3d;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public interface TriangleMesh {
    int getVertexCount();

    int getTriangleCount();

    int getEdgeCount();

    Face addNewFace(Point3d coord[]);

    int getFaceCount();

    Vertex findVertex(Point3d p, double eps);

    Vertex findVertex(Point3d v);

    double[] getBounds();

    void getTriangles(TriangleCollector tc);

    /**
     * Get the edges
     *
     * @return A linked list of edges
     */
    public Edge getEdges();

    /**
     * Collapse an edge.
     *
     * @param e   The edge to collapse
     * @param pos The position of the new common vertex
     */
    public boolean collapseEdge(Edge e, Point3d pos, EdgeCollapseParams ecp, EdgeCollapseResult ecr);

    public Vertex getVertices();

    public Vertex[][] getFaceIndexes();

    /**
     * Get the color attrib channel.
     *
     * @return The channelID or -1 if not available
     */
    public int getColorChannel();
}
