/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2019
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.io.cli;

import java.io.*;

/**
 * Base class for slice readers
 *
 * @author Alan Hudson
 */
public abstract class BaseSliceReader implements SliceReader {
    public void load(String file) throws IOException {
        try (
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis)
        ) {
            load(bis);
        }
    }
    
    protected int parseUnsignedIntegerBinary(DataInputStream dis) throws IOException {
        int b1 = dis.readUnsignedByte();
        int b2 = dis.readUnsignedByte();

        int bits = ((b2 << 8) + (b1 << 0));
        return bits;
    }

    protected double parseRealBinary(DataInputStream dis) throws IOException {
        int b1 = dis.readUnsignedByte();
        int b2 = dis.readUnsignedByte();
        int b3 = dis.readUnsignedByte();
        int b4 = dis.readUnsignedByte();

        int bits = (b4 << 24) + (b3 << 16) + (b2 << 8) + (b1 << 0);
        return Float.intBitsToFloat(bits);
    }

    /**
     * Parse 32 bit value, known as long in CLI spec, its an int in Java
     * @param dis
     * @return
     * @throws IOException
     */
    protected int parseLongBinary(DataInputStream dis) throws IOException {
        int b1 = dis.readUnsignedByte();
        int b2 = dis.readUnsignedByte();
        int b3 = dis.readUnsignedByte();
        int b4 = dis.readUnsignedByte();

        int bits = (b4 << 24) + (b3 << 16) + (b2 << 8) + (b1 << 0);
        return bits;
    }


}
