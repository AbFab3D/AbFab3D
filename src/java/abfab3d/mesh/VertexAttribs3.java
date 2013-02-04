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

/**
 * Vertex defined by a point in space including 3 per-vertex attributes.
 *
 * @author Alan Hudson
 */
public class VertexAttribs3 extends Vertex {
    private static final boolean DEBUG = false;

    public static final StructDataDefinition DEFINITION = new VertexAttribs3();

    public static final int NUM_ATTRIBS = 3;
    public static final int FLOAT_DATA_SIZE = NUM_ATTRIBS*3;

    // float positions
    public static final int POS_ATTRIB1 = 0;
    public static final int POS_ATTRIB2 = 3;
    public static final int POS_ATTRIB3 = 6;

    public VertexAttribs3() {
    }

    public static int create(double x, double y, double z, int id, float[] attribs, StructMixedData dest) {

        int destIdx = Vertex.create(x, y, z, id, dest);

        int float_pos = destIdx * DOUBLE_DATA_SIZE;
        double[] float_data = dest.getDoubleData();

        float_data[float_pos] = attribs[0];
        float_data[float_pos++] = attribs[1];
        float_data[float_pos++] = attribs[2];
        float_data[float_pos++] = attribs[3];
        float_data[float_pos++] = attribs[4];
        float_data[float_pos++] = attribs[5];
        float_data[float_pos++] = attribs[6];
        float_data[float_pos++] = attribs[7];
        float_data[float_pos++] = attribs[8];

        return destIdx;
    }

    public static void getAttrib1(StructMixedData src, int srcIdx, float[] attrib) {
        int float_pos = srcIdx * FLOAT_DATA_SIZE;
        float[] float_data = src.getFloatData();

        attrib[0] = float_data[float_pos++];
        attrib[1] = float_data[float_pos++];
        attrib[2] = float_data[float_pos];
    }

    public static void getAttrib2(StructMixedData src, int srcIdx, float[] attrib) {
        int float_pos = srcIdx * FLOAT_DATA_SIZE + POS_ATTRIB2;
        float[] float_data = src.getFloatData();

        attrib[0] = float_data[float_pos++];
        attrib[1] = float_data[float_pos++];
        attrib[2] = float_data[float_pos];
    }

    public static void getAttrib3(StructMixedData src, int srcIdx, float[] attrib) {
        int float_pos = srcIdx * FLOAT_DATA_SIZE + POS_ATTRIB3;
        float[] float_data = src.getFloatData();

        attrib[0] = float_data[float_pos++];
        attrib[1] = float_data[float_pos++];
        attrib[2] = float_data[float_pos];
    }

    public static void getAttrib(int attribIdx, StructMixedData src, int srcIdx, float[] attrib) {
        int float_pos = srcIdx * FLOAT_DATA_SIZE + attribIdx * 3;
        float[] float_data = src.getFloatData();

        attrib[0] = float_data[float_pos++];
        attrib[1] = float_data[float_pos++];
        attrib[2] = float_data[float_pos];
    }

    public static void getAttribs(StructMixedData src, int srcIdx, float[] attribs) {
        int float_pos = srcIdx * FLOAT_DATA_SIZE;
        float[] float_data = src.getFloatData();

        attribs[0] = float_data[float_pos++];
        attribs[1] = float_data[float_pos++];
        attribs[2] = float_data[float_pos++];
        attribs[3] = float_data[float_pos++];
        attribs[4] = float_data[float_pos++];
        attribs[5] = float_data[float_pos++];
        attribs[6] = float_data[float_pos++];
        attribs[7] = float_data[float_pos++];
        attribs[8] = float_data[float_pos++];
    }

    public int getFloatDataSize() {
        return FLOAT_DATA_SIZE;
    }
}

