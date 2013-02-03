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

import abfab3d.util.StructDataDefinition;
import abfab3d.util.StructMixedData;

/**
 * Face defined by half edges.  Includes single linked list traversal.
 */
public class Face extends StructDataDefinition {
    public static final StructDataDefinition DEFINITION = new Face();

    public static final int POINTER_DATA_SIZE = 3;

    // int positions
    public static final int POS_HE = 0;  // HalfEdge linked list start
    public static final int POS_NEXT = 1; // Face linked list next
    public static final int POS_PREV = 2; // Face linked list prev

    public static int create(int he, StructMixedData dest) {
        int destIdx = dest.addItem();

        int pointer_pos = destIdx * POINTER_DATA_SIZE;
        int[] pointer_data = dest.getPointerData();

        pointer_data[pointer_pos + POS_HE] = he;

        return destIdx;
    }

    public String toString(StructMixedData faceSrc, int srcIdx, StructMixedData halfEdgeSrc, StructMixedData vertexSrc) {
        String st = "[";

        int the = getHe(faceSrc, srcIdx);
        int start = the;
        do {
            st += Vertex.getUserData(vertexSrc, HalfEdge.getStart(halfEdgeSrc, the)) + " ";
            the = HalfEdge.getNext(halfEdgeSrc, the);
        }while(the != start);
        st += "]";

        return "f: " + st;
    }

    public static int getHe(StructMixedData src, int srcIdx) {
        int pointer_pos = srcIdx * POINTER_DATA_SIZE + POS_HE;
        int[] pointer_data = src.getPointerData();

        return pointer_data[pointer_pos];
    }

    public static void setHe(int he,StructMixedData dest, int destIdx) {
        int pointer_pos = destIdx * POINTER_DATA_SIZE;
        int[] pointer_data = dest.getPointerData();

        pointer_data[pointer_pos + POS_HE] = he;
    }

    public static void setNext(int next, StructMixedData dest, int destIdx) {
        int pointer_pos = destIdx * POINTER_DATA_SIZE + POS_NEXT;
        int[] pointer_data = dest.getPointerData();

        pointer_data[pointer_pos] = next;
    }

    public static int getNext(StructMixedData src, int srcIdx) {
        int pointer_pos = srcIdx * POINTER_DATA_SIZE + POS_NEXT;
        int[] pointer_data = src.getPointerData();

        return pointer_data[pointer_pos];
    }

    public static void setPrev(int prev, StructMixedData dest, int destIdx) {
        int pointer_pos = destIdx * POINTER_DATA_SIZE + POS_PREV;
        int[] pointer_data = dest.getPointerData();

        pointer_data[pointer_pos] = prev;
    }

    public static int getPrev(StructMixedData src, int srcIdx) {
        int pointer_pos = srcIdx * POINTER_DATA_SIZE + POS_PREV;
        int[] pointer_data = src.getPointerData();

        return pointer_data[pointer_pos];
    }

    public int getPointerDataSize() {
        return POINTER_DATA_SIZE;
    }
}
