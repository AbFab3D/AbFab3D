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

import abfab3d.util.StructDataDefinition;
import abfab3d.util.StructMixedData;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;

import static abfab3d.util.Output.fmt;

/**
 * Vertex defined by a point in space.
 */
public class Vertex extends StructDataDefinition {
    private static final boolean DEBUG = true;

    public static final StructDataDefinition DEFINITION = new Vertex();

    public static final int DOUBLE_DATA_SIZE = 3;
    public static final int INT_DATA_SIZE = 1;
    public static final int POINTER_DATA_SIZE = 4;

    // double positions
    public static final int POS_X = 0;
    public static final int POS_Y = 1;
    public static final int POS_Z = 2;

    // int positions
    public static final int POS_ID = 0;

    // pointer
    public static final int POS_LINK = 0;  //associate each vertex with tail of _some_ edge(HalfEdgeID)
    public static final int POS_NEXT = 1;  // List of all vertices(VertexID)
    public static final int POS_PREV = 2;  // VertexID
    public static final int POS_USER_DATA = 3;

    // float positions
    public static final int POS_ATTRIB = 0;

    // TODO: need to add support for vertex attribs
    private int numAttribs;

    public Vertex() {
        this(0);
    }

    public Vertex(int numAttrbs) {
        this.numAttribs = numAttrbs;
    }

    public static int create(double x, double y, double z, int id, StructMixedData dest) {
        int destIdx = dest.addItem();

        int double_pos = destIdx * DOUBLE_DATA_SIZE;
        double[] double_data = dest.getDoubleData();
        int int_pos = destIdx * INT_DATA_SIZE;
        int[] int_data = dest.getIntData();

        double_data[double_pos + POS_X] = x;
        double_data[double_pos + POS_Y] = y;
        double_data[double_pos + POS_Z] = z;

        int_data[int_pos + POS_ID] = id;

        return destIdx;
    }

    public static int getID(StructMixedData src, int srcIdx) {
        int int_pos = srcIdx * INT_DATA_SIZE + POS_ID;
        int[] int_data = src.getIntData();

        return int_data[int_pos];
    }

    public static void setID(int id,StructMixedData dest, int destIdx) {
        int int_pos = destIdx * INT_DATA_SIZE;
        int[] int_data = dest.getIntData();

        int_data[int_pos + POS_ID] = id;
    }

    public static double getX(StructMixedData src, int srcIdx) {
        int double_pos = srcIdx * DOUBLE_DATA_SIZE;
        double[] double_data = src.getDoubleData();

        return double_data[double_pos + POS_X];
    }

    public static double getY(StructMixedData src, int srcIdx) {
        int double_pos = srcIdx * DOUBLE_DATA_SIZE;
        double[] double_data = src.getDoubleData();

        return double_data[double_pos + POS_Y];
    }

    public static double getZ(StructMixedData src, int srcIdx) {
        int double_pos = srcIdx * DOUBLE_DATA_SIZE;
        double[] double_data = src.getDoubleData();

        return double_data[double_pos + POS_Z];
    }

    public static void getPoint(StructMixedData src, int srcIdx, double[] pos) {
        int double_pos = srcIdx * DOUBLE_DATA_SIZE;
        double[] double_data = src.getDoubleData();

        pos[0] = double_data[double_pos + POS_X];
        pos[1] = double_data[double_pos + POS_Y];
        pos[2] = double_data[double_pos + POS_Z];
    }

    public static void getPoint(StructMixedData src, int srcIdx, Tuple3d pos) {
        int double_pos = srcIdx * DOUBLE_DATA_SIZE;
        double[] double_data = src.getDoubleData();

        pos.x = double_data[double_pos + POS_X];
        pos.y = double_data[double_pos + POS_Y];
        pos.z = double_data[double_pos + POS_Z];
    }

    public static void setPoint(double x, double y, double z, StructMixedData dest, int destIdx) {
        int double_pos = destIdx * DOUBLE_DATA_SIZE;
        double[] double_data = dest.getDoubleData();

        double_data[double_pos + POS_X] = x;
        double_data[double_pos + POS_Y] = y;
        double_data[double_pos + POS_Z] = z;
    }

    public static void setPoint(StructMixedData src, int srcIdx,StructMixedData dest, int destIdx) {
        int src_double_pos = srcIdx * DOUBLE_DATA_SIZE;
        double[] src_double_data = src.getDoubleData();

        double x = src_double_data[src_double_pos + POS_X];
        double y = src_double_data[src_double_pos + POS_Y];
        double z = src_double_data[src_double_pos + POS_Z];

        int double_pos = destIdx * DOUBLE_DATA_SIZE;
        double[] double_data = dest.getDoubleData();

        double_data[double_pos + POS_X] = x;
        double_data[double_pos + POS_Y] = y;
        double_data[double_pos + POS_Z] = z;
    }

    public static void setLink(int link, StructMixedData dest, int destIdx) {
        int pointer_pos = destIdx * POINTER_DATA_SIZE + POS_LINK;
        int[] pointer_data = dest.getPointerData();

        pointer_data[pointer_pos] = link;
    }

    public static int getLink(StructMixedData src, int srcIdx) {
        int pointer_pos = srcIdx * POINTER_DATA_SIZE + POS_LINK;
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

    public static String toString(StructMixedData src, int srcIdx){
        int userData = getUserData(src, srcIdx);
        double x = getX(src, srcIdx);
        double y = getY(src, srcIdx);
        double z = getZ(src, srcIdx);

        return fmt("%d (%10.7f,%10.7f,%10.7f)", userData, x,y,z);
    }

    public static void setUserData(int data, StructMixedData dest, int destIdx) {
        int pointer_pos = destIdx * POINTER_DATA_SIZE + POS_USER_DATA;
        int[] pointer_data = dest.getPointerData();

        pointer_data[pointer_pos] = data;
    }

    public static int getUserData(StructMixedData src, int srcIdx) {
        int pointer_pos = srcIdx * POINTER_DATA_SIZE + POS_USER_DATA;
        int[] pointer_data = src.getPointerData();

        return pointer_data[pointer_pos];
    }

    public int getDoubleDataSize() {
        return DOUBLE_DATA_SIZE;
    }

    public int getIntDataSize() {
        return INT_DATA_SIZE;
    }

    public int getPointerDataSize() {
        return POINTER_DATA_SIZE;
    }

    public int getFloatDataSize() {
        return numAttribs;
    }
}

