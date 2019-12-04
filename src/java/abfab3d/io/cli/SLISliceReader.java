/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.io.cli;


import abfab3d.core.Bounds;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import static abfab3d.core.Output.printf;

/**
 * SLI Format used by some SLS vendors.  Basically CLI format with an index at the end
 *
 * @author Alan Hudson
 */
public class SLISliceReader extends BaseSliceReader {

    public SLISliceReader() {

    }
    public SLISliceReader(InputStream is) throws IOException {
        load(is);
    }

    public SLISliceReader(String path) throws IOException {
        load(path);
    }

    @Override
    public void load(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        SliceLayer current = new SliceLayer();

        try {
            byte[] header = new byte[40];

            dis.read(header,0,40);
            //printf("Header: %s\n",new String(header));

            int version = parseUnsignedIntegerBinary(dis);
            int unknown1 = parseUnsignedIntegerBinary(dis);
            long headerSize = parseLongBinary(dis);
            long unknown2 = parseLongBinary(dis);
            long unknown3 = parseLongBinary(dis);
            long fileSliceDataOffset = parseLongBinary(dis);
            long fileIndexPos = parseLongBinary(dis);
            byte[] creator = new byte[40];
            dis.read(creator,0,40);
            long layerCount = parseLongBinary(dis);
            long polyLineCount = parseLongBinary(dis);
            long unknown4 = parseLongBinary(dis);

            byte[] unknown = new byte[32];
            dis.read(unknown,0,32);

            double scale = parseRealBinary(dis);
            double x0 = parseRealBinary(dis);
            double x1 = parseRealBinary(dis);
            double y0 = parseRealBinary(dis);
            double y1 = parseRealBinary(dis);
            double z0 = parseRealBinary(dis);
            double z1 = parseRealBinary(dis);


            printf("version: %d  u1: %d  header: %d  u2: %d u3: %4d fsdo: %6d fip: %6s creator: %s\n",
                    version,unknown1,headerSize,unknown2,unknown3,fileSliceDataOffset,fileIndexPos,new String(creator));
            printf("layers: %d  polylines: %d  scale: %8.4f\n",layerCount,polyLineCount,scale);
            printf("bounds: %7.3f %7.3f %7.3f %7.3f %7.3f %7.3f\n",x0,x1,y0,y1,z0,z1);

            // Load the index table

            printf("index table start: %d\n",(fileIndexPos));
            dis.skipBytes((int) (fileIndexPos-2-21*6));
            for(int i=0; i < layerCount; i++) {
                int z = parseUnsignedIntegerBinary(dis);
                double layerPos = z * scale;
                long off = parseLongBinary(dis);
                long layerOff = off + headerSize;
                printf("Layer: %d  z: %d layerPos: %8.4f off: %d  idx: %d\n",i,z,layerPos,off,layerOff);
            }
        } catch(EOFException e) {
            printf("End of file\n");
            // expected
        }

    }

    @Override
    public SliceLayer getSlice(int idx) {
        return null;
    }

    @Override
    public SliceLayer[] getSlices() {
        return new SliceLayer[0];
    }

    @Override
    public int getNumSlices() {
        return 0;
    }

    @Override
    public double getUnits() {
        return 0;
    }

    @Override
    public Bounds getBounds() {
        return null;
    }
}
