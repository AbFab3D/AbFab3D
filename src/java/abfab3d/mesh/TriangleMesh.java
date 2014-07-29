package abfab3d.mesh;

import abfab3d.util.StructMixedData;
import abfab3d.util.TriangleCollector;
import abfab3d.util.TriangleProducer;

import javax.vecmath.Point3d;

/**
 * Mesh description using triangles.
 *
 * @author Alan Hudson
 */
public interface TriangleMesh extends TriangleProducer {
    public static final int VA_NORMAL = 0;
    public static final int VA_COLOR = 1;
    public static final int VA_TEXCOORD0 = 2;
    public static final int VA_TEXCOORD1 = 3;
    public static final int VA_TEXCOORD2 = 4;
    public static final int VA_TEXCOORD3 = 5;
    public static final int VA_TEXCOORD4 = 6;
    public static final int VA_TEXCOORD5 = 7;
    public static final int VA_TEXCOORD6 = 8;
    public static final int VA_TEXCOORD7 = 9;

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

    /**
     * Get the attribute channel.
     *
     * @return The channelID or -1 if not available
     */
    public int getAttributeChannel(int channel);
}
