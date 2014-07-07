/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid;

// External Imports


import abfab3d.util.HashFunction;
import abfab3d.util.StructDataDefinition;
import abfab3d.util.StructMap;
import abfab3d.util.StructMixedData;

import static abfab3d.util.Output.fmt;

/**
 * Implementation of VectorIndexer as StructMap.  This implementation should
 * generate much less garbage.
 *
 * @author Alan Hudson
 */
public class VectorIndexerStructMap implements VectorIndexer {


    int nx, ny, nz; // dimensions of 3D grid
    int nxz;

    StructMixedData src;
    StructMap data;

    int key;

    /**
       creates 3d array of given sizes
     */
    public VectorIndexerStructMap(int nx, int ny, int nz){

        this.nx = nx;
        this.ny = ny;
        this.nz = nz;

        src = new StructMixedData(IndexValue.DEFINITION,(int) ((long) nx * ny * nz * 0.01));

        data = new StructMap(src, new LocationHashFunction());

        key = IndexValue.create(src);

    }

    /**
       @override
     */
    public final void set(int x, int y, int z, int value){
        int entry = IndexValue.create(x,y,z,value,src);

        data.put(entry,entry);
    }

    /**
       @override
     */
    public final int get(int x, int y, int z){
        IndexValue.setKey(x,y,z,src,key);

        int loc = data.get(key);
        if (loc != -1) {
            return IndexValue.getVal(src,loc);
        } else {
            return 0;   // TODO: do we need a different value?
        }
    }

    public VectorIndexer createEmpty(int nx, int ny, int nz){
        return new VectorIndexerStructMap(nx, ny, nz);
    }


}

/**
 * Index Definition
 */
class IndexValue extends StructDataDefinition {
    public static final StructDataDefinition DEFINITION = new IndexValue();

    public static final int INT_DATA_SIZE = 4;

    // int positions
    public static final int POS_X = 0;
    public static final int POS_Y = 1;
    public static final int POS_Z = 2;
    public static final int POS_VAL = 3;

    public static int create(StructMixedData dest) {
        int destIdx = dest.addItem();

        return destIdx;
    }

    public static int create(int x, int y, int z, int val, StructMixedData dest) {
        int destIdx = dest.addItem();

        int int_pos = destIdx * INT_DATA_SIZE;
        int[] int_data = dest.getIntData();

        int_data[int_pos + POS_X] = x;
        int_data[int_pos + POS_Y] = y;
        int_data[int_pos + POS_Z] = z;
        int_data[int_pos + POS_VAL] = val;

        return destIdx;
    }

    public static int getX(StructMixedData src, int srcIdx) {
        int int_pos = srcIdx * INT_DATA_SIZE;
        int[] int_data = src.getIntData();

        return int_data[int_pos + POS_X];
    }

    public static int getY(StructMixedData src, int srcIdx) {
        int int_pos = srcIdx * INT_DATA_SIZE;
        int[] int_data = src.getIntData();

        return int_data[int_pos + POS_Y];
    }

    public static int getZ(StructMixedData src, int srcIdx) {
        int int_pos = srcIdx * INT_DATA_SIZE;
        int[] int_data = src.getIntData();

        return int_data[int_pos + POS_Z];
    }

    public static int getVal(StructMixedData src, int srcIdx) {
        int int_pos = srcIdx * INT_DATA_SIZE;
        int[] int_data = src.getIntData();

        return int_data[int_pos + POS_VAL];
    }

    public static void setEntry(int x, int y, int z, int val, StructMixedData dest, int destIdx) {
        int int_pos = destIdx * INT_DATA_SIZE;
        int[] int_data = dest.getIntData();

        int_data[int_pos + POS_X] = x;
        int_data[int_pos + POS_Y] = y;
        int_data[int_pos + POS_Z] = z;
        int_data[int_pos + POS_VAL] = val;
    }
    public static void setKey(int x, int y, int z, StructMixedData dest, int destIdx) {
        int int_pos = destIdx * INT_DATA_SIZE;
        int[] int_data = dest.getIntData();

        int_data[int_pos + POS_X] = x;
        int_data[int_pos + POS_Y] = y;
        int_data[int_pos + POS_Z] = z;
    }

    public int getIntDataSize() {
        return INT_DATA_SIZE;
    }
}

class LocationHashFunction implements HashFunction {
    @Override
    public int calcHashCode(StructMixedData src, int srcIdx) {
        // TODO: we could add a getKey(int[]) method but not sure how to deal with allocation
        // to keep thread safe

        int x = IndexValue.getX(src,srcIdx);
        int y = IndexValue.getY(src, srcIdx);
        int z = IndexValue.getZ(src, srcIdx);

        return 31 * 31 * x + 31 * y + z;
    }

    @Override
    public boolean calcEquals(StructMixedData src, int a, int b) {
        int x1 = IndexValue.getX(src,a);
        int x2 = IndexValue.getX(src,b);

        if (x1 != x2) return false;

        int y1 = IndexValue.getY(src, a);
        int y2 = IndexValue.getY(src, b);

        if (y1 != y2) return false;

        int z1 = IndexValue.getZ(src, a);
        int z2 = IndexValue.getZ(src, b);

        if (z1 != z2) return false;

        return true;
    }
}