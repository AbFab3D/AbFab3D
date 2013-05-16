/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
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

import abfab3d.util.HashFunction;
import abfab3d.util.StructMixedData;


 /**
 * Custom hash function for HalfEdges.
 *
 * @author Alan Hudson
 */
public class HalfEdgeHashFunction implements HashFunction {
    public int calcHashCode(StructMixedData src, int srcIdx) {
        int start = HalfEdge.getStart(src, srcIdx);
        int end = HalfEdge.getEnd(src, srcIdx);

  /*
        // Make sure hashcode will be the same regardless of order
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
   */
        int h = start + 119 * end;
        //System.out.println("Calc hash  start: " + start + " end: " + end + " h: " + h);

        return h;
/*
        // Doesn't seem to speed anything up

        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).

        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
*/
    }

    public boolean calcEquals(StructMixedData src, int a, int b) {
        int start_a = HalfEdge.getStart(src, a);
        int start_b = HalfEdge.getStart(src, b);
        int end_a = HalfEdge.getEnd(src, a);
        int end_b = HalfEdge.getEnd(src, b);

        //System.out.println("Calc equals: a: " + a + " b: " + b);

        return  ((start_a == start_b) && (end_a == end_b));
/*
        // hmmm, this kills everything why?
        return  ((start_a == start_b) && (end_a == end_b)) || (start_a == end_b) && (end_a == start_b);
*/
    }
}
