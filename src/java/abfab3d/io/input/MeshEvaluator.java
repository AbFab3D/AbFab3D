/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2019
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.io.input;

import abfab3d.core.Bounds;
import abfab3d.core.TriangleCollector;

import javax.vecmath.Vector3d;

import static abfab3d.core.Output.fmt;

/**
 * Collect stats about a mesh
 *
 * @author Alan Hudson
 */
public class MeshEvaluator implements TriangleCollector {
    private BoundsCalculator bc = new BoundsCalculator();
    private int inputTris = 0;
    private double totEdgeLen = 0;
    private double maxEdgeLenSq = 0;
    private double minEdgeLenSq = Double.MAX_VALUE;
    private boolean statsCalculated = false;
    private Stats stats;


    @Override
    public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2) {
        bc.addTri(v0,v1,v2);

        inputTris++;

        double elen = v0.lengthSquared();
        if (elen > maxEdgeLenSq) {
            maxEdgeLenSq = elen;
        }
        if (elen < minEdgeLenSq) {
            minEdgeLenSq = elen;
        }
        totEdgeLen += elen;

        elen = v1.lengthSquared();
        if (elen > maxEdgeLenSq) {
            maxEdgeLenSq = elen;
        }
        if (elen < minEdgeLenSq) {
            minEdgeLenSq = elen;
        }
        totEdgeLen += elen;

        elen = v2.lengthSquared();
        if (elen > maxEdgeLenSq) {
            maxEdgeLenSq = elen;
        }
        if (elen < minEdgeLenSq) {
            minEdgeLenSq = elen;
        }
        totEdgeLen += elen;

        return true;
    }

    public Stats getStats() {
        if (statsCalculated) return stats;

        statsCalculated = true;

        stats = new Stats();
        stats.bounds = bc.getBoundsObject();
        stats.inputTris = inputTris;
        stats.minTriEdgeSize = (float) Math.sqrt(minEdgeLenSq);
        stats.avgTriEdgeSize = (float) Math.sqrt((totEdgeLen / (inputTris * 3)));
        stats.maxTriEdgeSize = (float) Math.sqrt(maxEdgeLenSq);

        return stats;
    }

    public static class Stats {
        public Bounds bounds;
        public int inputTris;
        public float maxTriEdgeSize;
        public float avgTriEdgeSize;
        public float minTriEdgeSize;

        public String toString(double unit) {
            return fmt("Mesh Stats:\ninput tris: %d  \nminTriEdge: %6.2f avgTriEdge: %6.2f maxTriEdge: %6.2f\n",
                    inputTris,minTriEdgeSize/unit,avgTriEdgeSize/unit,maxTriEdgeSize/unit);

        }
    }

}
