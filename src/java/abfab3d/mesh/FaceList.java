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

public class FaceList extends StructDataDefinition {
    public static final StructDataDefinition DEFINITION = new FaceList();

    public static final int INT_DATA_SIZE = 3;

    // int positions
    public static final int POS_X = 0;
    public static final int POS_Y = 1;
    public static final int POS_Z = 2;

    public static int createEntry(int x, int y, int z, StructMixedData dest) {
        int destIdx = dest.addItem();
        set(x,y,z, dest, destIdx);

        return destIdx;
    }

    public static void set(int x, int y, int z, StructMixedData dest, int destIdx) {
        int int_pos = destIdx * INT_DATA_SIZE;
        int[] int_data = dest.getIntData();

        int_data[int_pos + POS_X] = x;
        int_data[int_pos + POS_Y] = y;
        int_data[int_pos + POS_Z] = z;
    }

    public static void get(StructMixedData src, int srcIdx, int[] face) {
        int int_pos = srcIdx * INT_DATA_SIZE;
        int[] int_data = src.getIntData();

        face[0] = int_data[int_pos + POS_X];
        face[1] = int_data[int_pos + POS_Y];
        face[2] = int_data[int_pos + POS_Z];
    }

    public static int[] toArray(StructMixedData src) {

        int[] ret_val = src.getIntData();

        // need to trim to count
        int len = src.getLength() * INT_DATA_SIZE;
        if (ret_val.length != len) {
            int[] vals = ret_val;
            ret_val = new int[len];

            System.arraycopy(vals,0,ret_val,0,len);
        }

        return ret_val;
    }

    public static int[] toArray(StructMixedData src, int array[]) {
        
        int len = src.getLength() * INT_DATA_SIZE;
        if(array == null || array.length < len)
            array = new int[len];
        
        int[] ret_val = array;
        int data[] = src.getIntData();
        
        System.arraycopy(data,0,ret_val,0,len);
        
        return ret_val;
        
    }

    public static int getCount(StructMixedData src) {
        return src.getLength();
    }


    public int getIntDataSize() {
        return INT_DATA_SIZE;
    }
}

