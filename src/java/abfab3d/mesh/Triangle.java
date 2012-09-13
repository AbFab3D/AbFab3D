package abfab3d.mesh;

import org.web3d.util.LongHashMap;

import javax.vecmath.Point3d;
import java.util.Set;

/**
 * A lightweight triangle description.  Planned usage is Set to remove duplicates so hashCode and equals
 * based on sorted vertices.
 *
 * @author Alan Hudson
 */
public class Triangle {
    private static final boolean DEBUG = false;

    private Vertex v0;
    private Vertex v1;
    private Vertex v2;

    public Triangle() {
    }

    public Triangle(Vertex v0, Vertex v1, Vertex v2) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;

        sortVertices();
    }

    /**
     * Twizzle sort vertices so lowest vertexID is in v0.
     */
    private void sortVertices() {
        // Swizzle to v0 having smallest vertex
        if (v0.getID() < v1.getID() && v0.getID() < v2.getID()) {
            // no op
        } else if (v1.getID() < v2.getID()) {
            Vertex t = v0;
            this.v0 = v1;
            this.v1 = v2;
            this.v2 = t;
        }  else {
            Vertex t = v0;
            this.v0 = v2;
            this.v2 = v1;
            this.v1 = t;
        }

    }

    public Vertex getV0() {
        return v0;
    }

    public Vertex getV1() {
        return v1;
    }

    public Vertex getV2() {
        return v2;
    }

    public void setV0(Vertex v0) {
        this.v0 = v0;
        sortVertices();
    }

    public void setV1(Vertex v1) {
        this.v1 = v1;
        sortVertices();
    }

    public void setV2(Vertex v2) {
        this.v2 = v2;
        sortVertices();
    }

    public int hashCode() {
        return v0.hashCode() + 31 * v1.hashCode() + 31 * 31 + v2.hashCode();
    }

    public boolean equals(Object obj) {
        Triangle tri = (Triangle) obj;
        return (tri.v0 == v0) && (tri.v1 == v1) && (tri.v2 == v2);

    }

    /**
     * Test whether a set of triangles is manifold
     *
     * @return
     */
    public static boolean isManifold(Set<Triangle> tris) {

        LongHashMap edgeCount = new LongHashMap();

        for (Triangle t : tris) {
            if (DEBUG) System.out.println("Count face: " + t.v0 + " " + t.v1 + " " + t.v2);
            processEdge(t.v0.getID(), t.v1.getID(), edgeCount);
            processEdge(t.v1.getID(), t.v2.getID(), edgeCount);
            processEdge(t.v2.getID(), t.v0.getID(), edgeCount);
        }

        long[] keys = edgeCount.keySet();

        for (int i = 0; i < keys.length; i++) {

            Integer count = (Integer) edgeCount.get(keys[i]);

            if (count != 2) {
                int index1 = (int) (keys[i] >> 32);
                int index2 = (int) (keys[i]);

                if (DEBUG) System.out.println("Invalid edge: " + index1 + "->" + index2 + " cnt: " + count);

                return false;
            }
        }

        return true;
    }

    /**
     * Test whether a set of triangles had edge counts > 2
     *
     * @return
     */
    public static boolean isManifoldOver(Set<Triangle> tris) {

        LongHashMap edgeCount = new LongHashMap();

        for (Triangle t : tris) {
            if (DEBUG) System.out.println("Count face: " + t.v0.getID() + " " + t.v1.getID() + " " + t.v2.getID());
            processEdge(t.v0.getID(), t.v1.getID(), edgeCount);
            processEdge(t.v1.getID(), t.v2.getID(), edgeCount);
            processEdge(t.v2.getID(), t.v0.getID(), edgeCount);
        }

        long[] keys = edgeCount.keySet();

        for (int i = 0; i < keys.length; i++) {

            Integer count = (Integer) edgeCount.get(keys[i]);

            if (count > 2) {
                int index1 = (int) (keys[i] >> 32);
                int index2 = (int) (keys[i]);

                if (DEBUG) System.out.println("Invalid edge: " + index1 + "->" + index2 + " cnt: " + count);

                return false;
            }
        }

        return true;
    }

    private static void processEdge(int index1, int index2, LongHashMap edgeCount) {

//System.out.println("Edges being processed: " + index1 + "," + index2);

        long edge;
        int count = 1;

        // place the smallest index first for
        // consistent lookup
        if (index1 > index2) {
            int temp = index1;
            index1 = index2;
            index2 = temp;
        }

        // put the larger of the 2 points into the long
        edge = index2;

        // shift the point to the left to make room for
        // the smaller point
        edge <<= 32;

        // bit OR the smaller point into the long
        edge |= index1;

        // add the edge to the count
        if (edgeCount.containsKey(edge)) {
            Integer val = (Integer)edgeCount.get(edge);
            count = val.intValue();
            count++;
        }

        edgeCount.put(edge, new Integer(count));
    }

    public String toString() {
        String s = super.toString();
        s = s.substring(s.indexOf("@"), s.length());
        String v0_st = null;
        String v1_st = null;
        String v2_st = null;


        v0_st = "" + v0.getID();
        v1_st = "" + v1.getID();
        v2_st = "" + v2.getID();

        return s + "(" + v0_st + "," + v1_st + "," + v2_st + ")";
    }

}
