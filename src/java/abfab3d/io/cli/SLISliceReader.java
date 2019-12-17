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
import static abfab3d.core.Units.MM;

/**
 * SLI Format used by some SLS vendors.  Basically CLI format with an index at the end
 *
 * @author Alan Hudson
 */
public class SLISliceReader extends BaseSliceReader {
    private static final boolean DEBUG = false;
    private Bounds bounds;
    private double units;
    private SliceLayer[] slices;
    private int[] index;

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
        //asciiDump(is,24000, true,60);

        DataInputStream dis = new DataInputStream(is);
        SliceLayer current = new SliceLayer();

        try {
            byte[] header = new byte[40];

            dis.read(header,0,40);
            //printf("Header: %s\n",new String(header));

            int version = parseUnsignedIntegerBinary(dis);
            int unknown1 = parseUnsignedIntegerBinary(dis);
            int headerSize = parseLongBinary(dis);
            int unknown2 = parseLongBinary(dis);
            int unknown3 = parseLongBinary(dis);
            int fileSliceDataOffset = parseLongBinary(dis);
            int fileIndexPos = parseLongBinary(dis);
            byte[] creator = new byte[40];
            dis.read(creator,0,40);
            int layerCount = parseLongBinary(dis);
            int polyLineCount = parseLongBinary(dis);
            long unknown4 = parseLongBinary(dis);

            byte[] unknown = new byte[32];
            dis.read(unknown,0,32);

            units = parseRealBinary(dis);
            double x0 = parseRealBinary(dis);
            double x1 = parseRealBinary(dis);
            double y0 = parseRealBinary(dis);
            double y1 = parseRealBinary(dis);
            double z0 = parseRealBinary(dis);
            double z1 = parseRealBinary(dis);  // 172 bytes to here
            bounds = new Bounds(x0,x1,y0,y1,z0,z1);


            if (DEBUG) {
                printf("version: %d  u1: %d  header: %d  u2: %d u3: %4d lc: %d plc: %d u4: %d fsdo: %6d fip: %6s creator: %s\n",
                        version, unknown1, headerSize, unknown2, unknown3, layerCount, polyLineCount, unknown4, fileSliceDataOffset, fileIndexPos, new String(creator));
                printf("layers: %d  polylines: %d  scale: %8.4f\n", layerCount, polyLineCount, units);
                printf("bounds: %7.3f %7.3f %7.3f %7.3f %7.3f %7.3f\n", x0, x1, y0, y1, z0, z1);
            }

            int skip = (int)(fileSliceDataOffset - (172 - 44) );  // headersize - 44?  strange
            //printf("Skipping bytes: %d",skip);
            dis.skipBytes(skip);

            slices = new SliceLayer[layerCount];

            for(int i=0; i < layerCount; i++) {
                //asciiDump(is,60, true,60);
                slices[i] = loadSliceData(dis, units);
            }

            //printf("index table start: %d\n",(fileIndexPos));
            //asciiDump(dis,12,true,12);
            //dis.skipBytes((int) (fileIndexPos-2-21*6));  // Shouldnt be necessary now
            index = new int[layerCount];
            for(int i=0; i < layerCount; i++) {
                int z = parseUnsignedIntegerBinary(dis);
                double layerPos = z * units;
                int off = parseLongBinary(dis);
                index[i] = off;
                int layerOff = off + headerSize;
                if (DEBUG) printf("Layer: %d  z: %d layerPos: %8.4f off: %d  idx: %d\n",i,z,layerPos,off,layerOff);
            }
        } catch(EOFException e) {
            // expected
        }

    }

    private SliceLayer loadSliceData(DataInputStream dis, double scale) throws IOException {
        SliceLayer ret = new SliceLayer();

        int otype = 0;

        while(otype != 2) {
            otype = dis.readUnsignedByte();
            switch(otype) {
                case 1:  // Start Layer
                    int  layerPos = parseUnsignedIntegerBinary(dis);
                    int unknown1 = parseLongBinary(dis);
                    int unknown2 = parseLongBinary(dis);

                    int padding = dis.readUnsignedByte();
                    if (DEBUG) printf("Start layer.  pos: %8.4f mm  %d %d %d\n",layerPos*scale,unknown1,unknown2,padding);
                    ret.setLayerHeight(layerPos*scale * MM);  // Convert to meters for internal storage

                    break;
                case 2:  // End data
                    break;
                case 3:  // Start Polyline
                    //asciiDump(dis,24,true,24);
                    int dir = dis.readUnsignedByte();
                    int n = parseUnsignedIntegerBinary(dis);

                    if (DEBUG) printf("Start PolyLine.  dir: %d  n: %d\n",dir,n);
                    double[] points = new double[n*2];

                    for(int i=0; i < n * 2; i++) {
                        int fp = parseUnsignedIntegerBinary(dis);
                        points[i] = fp * scale * MM; // Convert to meters for internal storage
                        //printf("%f,",coord);
                    }
                    PolyLine pl = new PolyLine(0,dir,points);  // TODO: Do we need to implement an internal unique id?
                    ret.addPolyLine(pl);
                    printf("\n");
                    break;
                case 4:
                    if (DEBUG) printf("Start Hatch\n");
                    break;
                default:
                    printf("Unknown cmd: %d\n",otype);
            }
        }

        //printf("End of slice data\n");

        return ret;
    }

    @Override
    public SliceLayer getSlice(int idx) {
        return slices[idx];
    }

    @Override
    public SliceLayer[] getSlices() {
        return slices;
    }

    @Override
    public int getNumSlices() {
        return slices.length;
    }

    @Override
    public double getUnits() {
        return units;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    public int[] getIndex() {
        return index;
    }
}
