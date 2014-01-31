package abfab3d.mesh;

import abfab3d.util.StructMixedData;
import abfab3d.util.TriangleCollector;
import abfab3d.util.TriangleProducer;

import javax.vecmath.Point3d;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public interface TriangleMesh extends TriangleProducer {
    int getVertexCount();

    int getTriangleCount();

    int getEdgeCount();

    int getFaceCount();

    int findVertex(double[] pnt, double eps);

    double[] getBounds();

    boolean getTriangles(TriangleCollector tc);

    /**
     * Get the edges
     *
     * @return A linked list of edges
     */
    public StructMixedData getEdges();

    /**
     * Get the half edges
     *
     * @return A linked list of edges
     */
    public StructMixedData getHalfEdges();

    /**
     * Get the faces
     *
     * @return A linked list of faces
     */
    public StructMixedData getFaces();

    /**
     * Collapse an edge.
     *
     * @param e   The edge to collapse
     * @param pos The position of the new common vertex
     */
    public boolean collapseEdge(int e, Point3d pos, EdgeCollapseParams ecp, EdgeCollapseResult ecr);

    public StructMixedData getVertices();

    public int[] getFaceIndexes();

    public int getStartEdge();

    public int getStartVertex();

    public int getStartFace();

    /**
     * Get the semantic definitions of the vertices
     * @return The definitions or null if none
     */
    public int[] getSemantics();

    /**
     * Get the color attrib channel.
     *
     * @return The channelID or -1 if not available
     */
    public int getColorChannel();
}
