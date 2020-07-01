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

import org.apache.commons.io.IOUtils;

import java.io.*;

import static abfab3d.core.Output.printf;
import static abfab3d.io.cli.CLISliceReader.bytesToHex;

/**
 * Base class for slice readers
 *
 * @author Alan Hudson
 */
public abstract class BaseSliceReader implements SliceReader {

    public static final String EXT_SLI = ".sli";
    public static final String EXT_CLI = ".cli";


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

    protected void hexDump(InputStream is, int max) throws IOException {
        byte[] tbytes = IOUtils.toByteArray(is);

        int len = Math.min(max,tbytes.length);
        byte[] bytes = new byte[len];

        System.arraycopy(tbytes,0,bytes,0,len);
        printf("%s\n",bytesToHex(bytes));
    }

    protected void asciiDump(InputStream is, int len, boolean label, int maxCol) throws IOException {
        int lines = len / maxCol;

        for(int n=0; n < lines; n++) {
            int start = n*maxCol;
            int end = start + maxCol;
            if (label && n==0) {
                for (int i = start; i < end; i++) {
                    printf("%3d ", i);
                }
                printf("\n");
            }
            for (int i = start; i < end; i++) {
                int b = is.read();
                printf("%3d ", b);
            }
            printf("\n");
        }
    }


    public static SliceReader readFile(String filePath) throws IOException {

        if(filePath.endsWith(EXT_SLI))
            return new SLISliceReader(filePath);
        else if(filePath.endsWith(EXT_CLI))
            return new CLISliceReader(filePath);
        else throw new IllegalArgumentException("unrecognized slice file type:\""+filePath + "\"");
        
    }

}
