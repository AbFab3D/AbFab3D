package abfab3d.datasources;

import abfab3d.core.AttributeGrid;
import abfab3d.core.AttributePacker;
import abfab3d.core.Grid;
import abfab3d.core.GridDataDesc;
import abfab3d.core.Vec;
import abfab3d.grid.SparseGridInt;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;

/**
 * Utils for packing grids into primitive arrays
 *
 * @author Alan Hudson
 */
public class GridPackingUtils {
    private static final boolean DEBUG = true;

    static final int BYTE_BITS = 8;  // bits in byte
    static final int BYTE_MASK = 0xFF;

    static final int SHORT_BITS = 16;
    static final int SHORT_MASK = 0xFFFF;

    static final int INT_BITS = 32; // bits in int
    static final int INT_BYTES = 4; // bytes in int
    static final long INT_MASK = 0xFFFFFFFFL;
    static final int MAX_DATA_DIMENSION = 6;

    // mask for byte in int packing
    // complement mask for byte in int packing
    static final int BYTE_COMPLEMENT[] = new int[]{0xFFFFFF00,0xFFFF00FF,0xFF00FFFF,0x00FFFFFF};
    // bit shift for byte in int packing
    static final int BYTE_SHIFT[] = new int[]{0, 8, 16, 24};
    // complement mask for short in int packing
    static final int SHORT_COMPLEMENT[] = new int[]{0xFFFF0000,0x0000FFFF};
    // bit shift for short in int packing
    static final int SHORT_SHIFT[] = new int[]{0, 16};

    /**
     * returns size of buffer needed for specific grid data description
     */
    public static int getBufferSize(AttributeGrid grid) {

        GridDataDesc gdd = grid.getDataDesc();
        AttributePacker packer = gdd.getAttributePacker();
        AttributePacker unpacker = gdd.getAttributePacker();

        int outBits = packer.getBitCount();

        int outBytes = (outBits + BYTE_BITS - 1) / BYTE_BITS;

        if (grid instanceof SparseGridInt) {
            // TODO: Suspect this logic needs improving if datadesc are mismatched, allow to pass one in?

            int dataSize = ((SparseGridInt) grid).getDataSize();

            return dataSize;
        } else {
            int nx = grid.getWidth();
            int ny = grid.getHeight();
            int nz = grid.getDepth();

            long attCount = ((long) nx) * ny * nz;
            if (attCount > Integer.MAX_VALUE) {
                throw new RuntimeException(fmt("grid is too large to pack in array: [%d x %d x %d] -> 0x%x", nx, ny, nz, attCount));
            }

            int count = (int) attCount;
            switch (outBytes) {
                default:
                    throw new RuntimeException(fmt("unsupported bytes count:%d", outBytes));
                case 1:
                    return (count + 3) / 4;
                case 2:
                    return (count + 1) / 2;
                case 3:
                case 4:
                    return count;
            }
        }
    }

    /**
     return data in specific data format
     */
    public static void getBuffer(AttributeGrid grid, int data[], GridDataDesc bufferDataDesc) {

        AttributePacker packer = bufferDataDesc.getAttributePacker();

        AttributePacker unpacker = grid.getDataDesc().getAttributePacker();
        printf("*** packer: %s  unpacker: %s\n",packer,unpacker);
        int outBits = packer.getBitCount();
        int outBytes = (outBits + BYTE_BITS - 1) / BYTE_BITS;
        int outInts = (outBytes + INT_BYTES - 1) / INT_BYTES;
        switch (outBytes) {
            default:
                throw new RuntimeException(fmt("unsupported bytes count:%d", outBytes));
            case 1:
                getGridDataUByte(grid,data, unpacker, packer);
                break;
            case 2:
                getGridDataUShort(grid,data, unpacker, packer);
                break;
            case 3:
            case 4:
                getGridDataUInt(grid,data, unpacker, packer);
                break;
        }
    }

    /**
     return grid data as array of bytes packed into array of ints
     */
    public static void getGridDataUByte(AttributeGrid grid, int data[], AttributePacker unpacker, AttributePacker packer){

        if(DEBUG)printf("getGridDataUByte()  packer: %s  unpacker: %s\n",packer,unpacker);
        final int nx = grid.getWidth(), ny = grid.getHeight(), nz = grid.getDepth(), nxz = nx*nz;
        Vec value = new Vec(MAX_DATA_DIMENSION);

        if (unpacker != packer) {
            for (int y = 0; y < ny; y++) {
                for (int x = 0; x < nx; x++) {
                    for (int z = 0; z < nz; z++) {
                        long att = grid.getAttribute(x, y, z);
                        long outAtt = att;
                        unpacker.getData(att, value);
                        outAtt = packer.makeAttribute(value);
                        int ind = (x * nz + y * nxz + z);
                        int wordInd = ind >> 2;
                        int byteInWord = ind & 3;
                        data[wordInd] = (int) ((data[wordInd] & BYTE_COMPLEMENT[byteInWord]) | (outAtt << BYTE_SHIFT[byteInWord]));
                    }
                }
            }
        }  else {
            for (int y = 0; y < ny; y++) {
                for (int x = 0; x < nx; x++) {
                    for (int z = 0; z < nz; z++) {
                        long outAtt = grid.getAttribute(x, y, z);
                        int ind = (x * nz + y * nxz + z);
                        int wordInd = ind >> 2;
                        int byteInWord = ind & 3;
                        data[wordInd] = (int) ((data[wordInd] & BYTE_COMPLEMENT[byteInWord]) | (outAtt << BYTE_SHIFT[byteInWord]));
                    }
                }
            }
        }
    }

    /**
     return grid data as array of shorts packed into array of ints
     */
    public static void getGridDataUShort(AttributeGrid grid, int data[], AttributePacker unpacker, AttributePacker packer){
        if(DEBUG)printf("getGridDataUShort()\n");
        final int nx = grid.getWidth(), ny = grid.getHeight(), nz = grid.getDepth(), nxz = nx*nz;
        Vec value = new Vec(MAX_DATA_DIMENSION);
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    long att = grid.getAttribute(x, y, z);
                    long outAtt = att;
                    if(unpacker != packer){
                        unpacker.getData(att, value);
                        outAtt = packer.makeAttribute(value);
                    }

                    unpacker.getData(att, value);
                    int ind = (x*nz + y * nxz + z);
                    int wordInd = ind >> 1;
                    int shortInWord = ind & 1;
                    data[wordInd] = (int)((data[wordInd] & SHORT_COMPLEMENT[shortInWord]) | (outAtt << SHORT_SHIFT[shortInWord]));
                }
            }
        }
    }

    /**
     * @noRefGuide
    return grid data as array of ints
     */
    public static void getGridDataUInt(AttributeGrid grid, int data[], AttributePacker unpacker, AttributePacker packer){
        if(DEBUG)printf("getGridDataUInt()\n");
        final int nx = grid.getWidth(), ny = grid.getHeight(), nz = grid.getDepth(), nxz = nx*nz;
        Vec value = new Vec(MAX_DATA_DIMENSION);
        if(false)printf("--------------\n ");
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    long att = grid.getAttribute(x, y, z);
                    long outAtt = att;
                    if(unpacker != packer){
                        unpacker.getData(att, value);
                        outAtt = packer.makeAttribute(value);
                    }
                    if(false)printf("%8x ", outAtt);
                    int ind = (x*nz + y * nxz + z);
                    data[ind] = (int)(outAtt & INT_MASK);
                }
                if(false)printf("\n ");
            }
            if(false)printf("-----------\n ");
        }
    }

}
