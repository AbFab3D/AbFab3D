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

import javax.vecmath.Tuple3d;

import static abfab3d.core.Output.fmt;

/**
 * Stuct version of Tuple3D
 *
 * @author Alan Hudson
 */
public class StructTuple3d extends StructDataDefinition {
    private static final boolean DEBUG = false;

    public static final StructDataDefinition DEFINITION = new StructTuple3d();

    public static final int DOUBLE_DATA_SIZE = 3;

    // double positions
    public static final int POS_X = 0;
    public static final int POS_Y = 1;
    public static final int POS_Z = 2;

    public static int create(StructMixedData dest) {
        int destIdx = dest.addItem();

        return destIdx;
    }

    public static int create(double x, double y, double z, StructMixedData dest) {
        int destIdx = dest.addItem();

        int double_pos = destIdx * DOUBLE_DATA_SIZE;
        double[] double_data = dest.getDoubleData();

        double_data[double_pos + POS_X] = x;
        double_data[double_pos + POS_Y] = y;
        double_data[double_pos + POS_Z] = z;

        return destIdx;
    }

    public static int create(Tuple3d vec, StructMixedData dest) {
        int destIdx = dest.addItem();

        int double_pos = destIdx * DOUBLE_DATA_SIZE;
        double[] double_data = dest.getDoubleData();

        double_data[double_pos + POS_X] = vec.x;
        double_data[double_pos + POS_Y] = vec.y;
        double_data[double_pos + POS_Z] = vec.z;

        return destIdx;
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

    public static void getPoint(StructMixedData src, int srcIdx, javax.vecmath.Tuple3d pos) {
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

    public static String toString(StructMixedData src, int srcIdx){
        double x = getX(src, srcIdx);
        double y = getY(src, srcIdx);
        double z = getZ(src, srcIdx);

        return fmt("(%10.7f,%10.7f,%10.7f)", x,y,z);
    }

    /**
     * Add a vector to another vector, places result into dest.
     * @param src
     * @param srcIdx
     * @param dest
     * @param destIdx
     */
    public static void add(StructMixedData src, int srcIdx, StructMixedData dest, int destIdx) {
        int src_double_pos = srcIdx * DOUBLE_DATA_SIZE;
        double[] src_double_data = src.getDoubleData();

        double x = src_double_data[src_double_pos + POS_X];
        double y = src_double_data[src_double_pos + POS_Y];
        double z = src_double_data[src_double_pos + POS_Z];

        int dest_double_pos = destIdx * DOUBLE_DATA_SIZE;
        double[] dest_double_data = dest.getDoubleData();

        dest_double_data[dest_double_pos + POS_X] += x;
        dest_double_data[dest_double_pos + POS_Y] += y;
        dest_double_data[dest_double_pos + POS_Z] += z;
    }

    /**
     * Scale a vector places result into dest.
     * @param s The scale
     * @param dest
     * @param destIdx
     */
    public static void scale(double s, StructMixedData dest, int destIdx) {
        int dest_double_pos = destIdx * DOUBLE_DATA_SIZE;
        double[] dest_double_data = dest.getDoubleData();

        dest_double_data[dest_double_pos + POS_X] *= s;
        dest_double_data[dest_double_pos + POS_Y] *= s;
        dest_double_data[dest_double_pos + POS_Z] *= s;
    }


    public int getDoubleDataSize() {
        return DOUBLE_DATA_SIZE;
    }

}
