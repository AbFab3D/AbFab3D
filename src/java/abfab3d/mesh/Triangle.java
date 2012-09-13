package abfab3d.mesh;

import org.web3d.util.LongHashMap;

import javax.vecmath.Point3d;
import java.util.Set;

/**
 * A lightweight triangle description.
 *
 * @author Alan Hudson
 */
public class Triangle {
    private static final boolean DEBUG = true;

    private Vertex v0;
    private Vertex v1;
    private Vertex v2;

    public Triangle() {
    }

    public void setPoint(int idx, Vertex v) {
        switch (idx) {
            case 0:
                v0 = v;
                break;
            case 1:
                v1 = v;
                break;
            case 2:
                v2 = v;
                break;
            default:
                throw new IllegalArgumentException("Invalid point index: " + idx);
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

    /**
     * Test whether a set of triangles is manifold
     *
     * @return
     */
    public static boolean isManifold(Set<Triangle> tris) {

        LongHashMap edgeCount = new LongHashMap();

        for (Triangle t : tris) {
            System.out.println("Count face: " + t.v0 + " " + t.v1 + " " + t.v2);
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
            System.out.println("Count face: " + t.v0 + " " + t.v1 + " " + t.v2);
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

}
