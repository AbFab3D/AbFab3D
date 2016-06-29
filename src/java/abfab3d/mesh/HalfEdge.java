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
 * Representation of half an edge.
 * <p/>
 * The twin of a half-edge is the opposite direction half-edge making up a typical edge.
 *
 * Will be an ordered list of items plus a hashmap.
 *
 * @author Vladimir Bulatov
 * @author Alan Hudson
 */
public class HalfEdge extends StructDataDefinition {
    public static final StructDataDefinition DEFINITION = new HalfEdge();

    public static final int POINTER_DATA_SIZE = 7;

    // int positions
    public static final int POS_START = 0;  // Vertex Start
    public static final int POS_END = 1; // Vertex end
    public static final int POS_NEXT = 2; // HalfEdge next
    public static final int POS_PREV = 3; // HalfEdge prev
    public static final int POS_TWIN = 4; // HalfEdge twin
    public static final int POS_EDGE = 5; // Edge edge
    public static final int POS_LEFT = 6; // Face left

    public static int create(StructMixedData dest) {
        int destIdx = dest.addItem();

        return destIdx;
    }

    public static int create(int start, int end, StructMixedData dest) {
        int destIdx = dest.addItem();

        int pointer_pos = destIdx * POINTER_DATA_SIZE;
        int[] pointer_data = dest.getPointerData();

        pointer_data[pointer_pos + POS_START] = start;
        pointer_data[pointer_pos + POS_END] = end;

        return destIdx;
    }

    public static String toString(StructMixedData src, int srcIdx) {
        //return toString1();
        return toString1(src, srcIdx);

    }

    public static void setStart(int start, StructMixedData dest, int destIdx) {
        int pointer_pos = destIdx * POINTER_DATA_SIZE + POS_START;
        int[] pointer_data = dest.getPointerData();

        pointer_data[pointer_pos] = start;
    }

    public static int getStart(StructMixedData src, int srcIdx) {
        int pointer_pos = srcIdx * POINTER_DATA_SIZE + POS_START;
        int[] pointer_data = src.getPointerData();

        return pointer_data[pointer_pos];
    }

    public static void setEnd(int end, StructMixedData dest, int destIdx) {
        int pointer_pos = destIdx * POINTER_DATA_SIZE + POS_END;
        int[] pointer_data = dest.getPointerData();

        pointer_data[pointer_pos] = end;
    }

    public static int getEnd(StructMixedData src, int srcIdx) {
        int pointer_pos = srcIdx * POINTER_DATA_SIZE + POS_END;
        int[] pointer_data = src.getPointerData();

        return pointer_data[pointer_pos];
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

    public static void setTwin(int twin, StructMixedData dest, int destIdx) {
        int pointer_pos = destIdx * POINTER_DATA_SIZE + POS_TWIN;
        int[] pointer_data = dest.getPointerData();

        pointer_data[pointer_pos] = twin;
    }

    public static int getTwin(StructMixedData src, int srcIdx) {
        int pointer_pos = srcIdx * POINTER_DATA_SIZE + POS_TWIN;
        int[] pointer_data = src.getPointerData();

        return pointer_data[pointer_pos];
    }

    public static void setEdge(int edge, StructMixedData dest, int destIdx) {
        int pointer_pos = destIdx * POINTER_DATA_SIZE + POS_EDGE;
        int[] pointer_data = dest.getPointerData();

        pointer_data[pointer_pos] = edge;
    }

    public static int getEdge(StructMixedData src, int srcIdx) {
        int pointer_pos = srcIdx * POINTER_DATA_SIZE + POS_EDGE;
        int[] pointer_data = src.getPointerData();

        return pointer_data[pointer_pos];
    }

    public static void setLeft(int twin, StructMixedData dest, int destIdx) {
        int pointer_pos = destIdx * POINTER_DATA_SIZE + POS_LEFT;
        int[] pointer_data = dest.getPointerData();

        pointer_data[pointer_pos] = twin;
    }

    public static int getLeft(StructMixedData src, int srcIdx) {
        int pointer_pos = srcIdx * POINTER_DATA_SIZE + POS_LEFT;
        int[] pointer_data = src.getPointerData();

        return pointer_data[pointer_pos];
    }

    private static String toString1(StructMixedData src, int srcIdx){

        String s = "" + srcIdx;
        String start_st = null;
        String end_st = null;


        int start = getStart(src, srcIdx);
        int end = getEnd(src, srcIdx);

        if (start != -1) {
            start_st = "" + start;
        }

        if (end != -1) {
            end_st = "" + end;
        }

       return s + "(" + start_st + " ->" + end_st + ")";
    }

/*
    private static String toString2(StructMixedData src, int srcIdx){

        int start = getStart(src, srcIdx);

        String ss,se;
        if(start != -1)
            ss = String.valueOf(Vertex.getUserData());
        else
            ss = "NULL";
        if(end != null)
            se = String.valueOf(end.getUserData());
        else
            se = "NULL";

        return fmt("[%4s->%4s]",ss, se);

    }
    */

    public int getPointerDataSize() {
        return POINTER_DATA_SIZE;
    }
}
