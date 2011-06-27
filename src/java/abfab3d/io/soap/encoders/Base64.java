/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.io.soap.encoders;

import abfab3d.io.soap.*;

/**
 * A static class used to encode and decode a Base64 byte array
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class Base64 {

    /**
     * Make this a static class
     */
    private Base64() {}

    /**
     * Encodes the byte array and returns an encoded string
     *
     * @param raw The byte array
     * @return The encode string
     */
    public static String encode(byte[] raw) {

        StringBuffer encoded = new StringBuffer();

        for (int i = 0; i < raw.length; i += 3) {
            encoded.append(encodeBlock(raw, i));
        }
        return encoded.toString();

    }

    /**
     * Encodes a block of bytes
     *
     * @param raw
     * @param offset
     * @return
     */
    private static char[] encodeBlock(byte[] raw, int offset) {

        int block = 0;
        int slack = raw.length - offset - 1;

        int end = (slack >= 2) ?
                2:
                slack;

        for (int i = 0; i <= end; i++) {

            byte b = raw[offset + i];
            int neuter = (b < 0) ?
                    b + 256 :
                    b;

            block += neuter << (8 * (2 - i));
        }

        char[] base64 = new char[4];

        for (int i = 0; i < 4; i++) {
            int sixbit = (block >>> (6 * (3 - i))) & 0x3f;
            base64[i] = getChar(sixbit);
        }

        if (slack < 1) {
            base64[2] = '=';
        }

        if (slack < 2) {
            base64[3] = '=';
        }

        return base64;
    }

    /**
     *
     * @param sixBit
     * @return
     */
    private static char getChar(int sixBit) {
        if (sixBit >= 0 && sixBit <= 25) {
            return (char)('A' + sixBit);
        }
        if (sixBit >= 26 && sixBit <= 51) {
            return (char)('a' + (sixBit - 26));
        }
        if (sixBit >= 52 && sixBit <= 61) {
            return (char)('0' + (sixBit - 52));
        }
        if (sixBit == 62) {
            return '+';
        }
        if (sixBit == 63) {
            return '/';
        }
        return '?';
    }

    /**
     * Decodes a Base64 encoded String and returns
     * a byte array. The encoded data is in a String
     * named base64
     *
     * @param base64 The encode string
     * @return The byte array
     */
    public static byte[] decode(String base64) throws Exception {

        // The first thing to do is to prepare a
        // list of valid characters that can go in
        // a base64 encoded String.

        byte[] codes = getValidBytes();
        char[] data = base64.toCharArray();

        int tempLen=data.length;
        for (int ix=0; ix<data.length; ix++) {

            // ignore non-valid chars and padding.
            if ( (data[ix]>255)||codes[data[ix]]<0) {
                --tempLen;
            }
        }

        // Calculate required length: 3 bytes for every
        // 4 valid base64 chars; plus 2 bytes if there are
        // 3 extra base64 chars; or plus 1 byte if ther are 2 extra

        int len = (tempLen / 4) * 3;
        if ((tempLen % 4) == 3) {
            len+=2;
        }

        if ((tempLen%4) == 2) {
            len+=1;
        }

        byte[] out = new byte[len];

        int shift=0;
        int accum=0;
        int index=0;

        for (int ix=0; ix<data.length; ix++) {

            int value = (data[ix] > 255)?
                    -1:
                    codes[data[ix]];

            if (value>=0) {

                accum<<=6;
                shift+=6;
                accum|=value;
                if (shift>=8) {
                    shift-=8;
                    out[index++]=(byte)((accum>>shift)&0xff);
                }
            }
        }

        if (index != out.length) {
            throw new Exception("Problems encountered while decoding Base64 string");
        }

        return out;
    }

    /**
     *
     * @return
     */
    private static byte[] getValidBytes() {

        byte[] codes=new byte[256];

        for (int i=0; i < 256; i++) {
            codes[i] = -1;
        }

        for (int i='A'; i <= 'Z'; i++) {
            codes[i] = (byte)(i - 'A');
        }

        for (int i='a'; i <= 'z'; i++) {
            codes[i] = (byte)(26 + i -'a');
        }

        for (int i='0'; i <= '9'; i++) {
            codes[i] = (byte)(52 + i -'0');
        }

        codes['+'] = 62;
        codes['/'] = 63;

        return codes;
    }

}
