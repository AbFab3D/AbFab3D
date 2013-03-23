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
package abfab3d.util;

import java.util.Arrays;

/**
 * Struct like data structure for reducing garbage collection.  A mixed collection which contains
 * all primitive data types for convenience.
 *
 * @author Alan Hudson
 */
public class StructMixedData {
    private byte[] byteData;
    private short[] shortData;
    private int[] intData;
    private int[] pointerData;
    private long[] longData;
    private char[] charData;
    private double[] doubleData;
    private boolean[] booleanData;
    private float[] floatData;
    private Object[] objectData;

    private int size;
    private int items;
    private StructDataDefinition def;

    public StructMixedData(StructDataDefinition def, int items) {
        this.items = items;
        this.def = def;

        if (def.getFloatDataSize() != 0) floatData = new float[items * def.getFloatDataSize()];
        if (def.getDoubleDataSize() != 0) doubleData = new double[items * def.getDoubleDataSize()];
        if (def.getBooleanDataSize() != 0) booleanData = new boolean[items * def.getBooleanDataSize()];
        if (def.getObjectDataSize() != 0) objectData = new Object[items * def.getObjectDataSize()];
        if (def.getByteDataSize() != 0) byteData = new byte[items * def.getByteDataSize()];
        if (def.getShortDataSize() != 0) shortData = new short[items * def.getShortDataSize()];
        if (def.getIntDataSize() != 0) intData = new int[items * def.getIntDataSize()];
        if (def.getPointerDataSize() != 0) {
            pointerData = new int[items * def.getPointerDataSize()];
            Arrays.fill(pointerData, -1);
        }
        if (def.getLongDataSize() != 0) longData = new long[items * def.getLongDataSize()];
        if (def.getCharDataSize() != 0) charData = new char[items * def.getCharDataSize()];
    }

    /**
     * Get the number of objects in this struct
     * @return
     */
    public int getLength() {
        return size;
    }

    /**
     * Add an item and return its index.
     * @return
     */
    public int addItem() {
        size++;

        if (size > items) {
            resize(items * 2);
        }
        return size - 1;
    }

    public void resize(int newSize) {
        byte[] oldByteData = byteData;
        short[] oldShortData = shortData;
        int[] oldIntData = intData;
        int[] oldPointerData = pointerData;
        long[] oldLongData = longData;
        char[] oldCharData = charData;
        double[] oldDoubleData = doubleData;
        boolean[] oldBooleanData = booleanData;
        float[] oldFloatData = floatData;
        Object[] oldObjectData = objectData;


        if (oldByteData != null) {
            byteData = new byte[newSize * def.getByteDataSize()];
            System.arraycopy(oldByteData, 0, byteData, 0, oldByteData.length);
        }
        if (oldShortData != null) {
            shortData = new short[newSize * def.getShortDataSize()];
            System.arraycopy(oldShortData, 0, shortData, 0, oldShortData.length);
        }
        if (oldIntData != null) {
            intData = new int[newSize * def.getIntDataSize()];
            System.arraycopy(oldIntData, 0, intData, 0, oldIntData.length);
        }
        if (oldPointerData != null) {
            pointerData = new int[newSize * def.getPointerDataSize()];
            System.arraycopy(oldPointerData, 0, pointerData, 0, oldPointerData.length);
            
            // Clear pointers to -1
            Arrays.fill(pointerData,oldPointerData.length, pointerData.length,-1);
        }
        if (oldLongData != null) {
            longData = new long[newSize * def.getLongDataSize()];
            System.arraycopy(oldLongData, 0, longData, 0, oldLongData.length);
        }
        if (oldCharData != null) {
            charData = new char[newSize * def.getCharDataSize()];
            System.arraycopy(oldCharData, 0, charData, 0, oldCharData.length);
        }
        if (oldDoubleData != null) {
            doubleData = new double[newSize * def.getDoubleDataSize()];
            System.arraycopy(oldDoubleData, 0, doubleData, 0, oldDoubleData.length);
        }
        if (oldFloatData != null) {
            floatData = new float[newSize * def.getFloatDataSize()];
            System.arraycopy(oldFloatData, 0, floatData, 0, oldFloatData.length);
        }
        if (oldBooleanData != null) {
            booleanData = new boolean[newSize * def.getBooleanDataSize()];
            System.arraycopy(oldBooleanData, 0, booleanData, 0, oldBooleanData.length);
        }
        if (oldObjectData != null) {
            objectData = new Object[newSize * def.getObjectDataSize()];
            System.arraycopy(oldObjectData, 0, objectData, 0, oldObjectData.length);
        }

        items = newSize;
    }

    public void clear() {
        size = 0;

        if (byteData != null) {
            Arrays.fill(byteData,(byte)0);
        }
        if (shortData != null) {
            Arrays.fill(shortData,(short)0);
        }
        if (intData != null) {
            Arrays.fill(intData,(int)0);
        }
        if (pointerData != null) {
            Arrays.fill(pointerData,-1);
        }
        if (longData != null) {
            Arrays.fill(longData,(long)0);
        }
        if (charData != null) {
            Arrays.fill(charData,(char) 0);
        }
        if (doubleData != null) {
            Arrays.fill(doubleData,0.0);
        }
        if (floatData != null) {
            Arrays.fill(floatData,0.0f);
        }
        if (booleanData != null) {
            Arrays.fill(booleanData,false);
        }
        if (objectData != null) {
            Arrays.fill(objectData,null);
        }
    }

    public double[] getDoubleData() {
        return doubleData;
    }

    public boolean[] getBooleanData() {
        return booleanData;
    }

    public float[] getFloatData() {
        return floatData;
    }

    public Object[] getObjectData() {
        return objectData;
    }

    public byte[] getByteData() {
        return byteData;
    }

    public short[] getShortData() {
        return shortData;
    }

    public int[] getIntData() {
        return intData;
    }

    public int[] getPointerData() {
        return pointerData;
    }
    
    public long[] getLongData() {
        return longData;
    }

    public char[] getCharData() {
        return charData;
    }

}
