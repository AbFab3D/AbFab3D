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
 * Vertex defined by a point in space including 1 per-vertex attribute.
 *
 * @author Alan Hudson
 */
public class VertexAttribs1 extends Vertex {
    private static final boolean DEBUG = false;

    public static final StructDataDefinition DEFINITION = new VertexAttribs1();

    public static final int FLOAT_DATA_SIZE = 3;

    // float positions
    public static final int POS_ATTRIB1 = 0;

    public VertexAttribs1() {
    }

    public static int create(double x, double y, double z, int id, float[] attribs, StructMixedData dest) {

        int destIdx = Vertex.create(x,y,z,id,dest);

        int float_pos = destIdx * FLOAT_DATA_SIZE;
        float[] float_data = dest.getFloatData();

        float_data[float_pos++] = attribs[0];
        float_data[float_pos++] = attribs[1];
        float_data[float_pos] = attribs[2];

        return destIdx;
    }

    public static void getAttrib1(StructMixedData src, int srcIdx, float[] attrib) {
        int float_pos = srcIdx * FLOAT_DATA_SIZE;
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

    public int getFloatDataSize() {
        return FLOAT_DATA_SIZE;
    }
}

