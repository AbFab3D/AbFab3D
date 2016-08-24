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

package abfab3d.grid;

// External Imports

import abfab3d.core.AttributeGrid;
import abfab3d.core.Grid;
import abfab3d.core.VoxelData;
import abfab3d.core.Bounds;
import java.util.Arrays;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;

/**
 * A sparse grid backed by two arrays.
 * blocks array of block offsets 
 * data  - array of actual data
 * data array is dynamically re-allocated as non zero attribute value is assigned 
 * blocks - array of offsets to current block (zero offset means empty block) 
 * @author Vladimir Bulatov
 */
public class SparseGridInt extends BaseAttributeGrid {

    static final boolean DEBUG = false;

    static final int SIZEOF_INT=4;
    static final long MAX_DATA_SIZE = Integer.MAX_VALUE;
    static final long DATA_MASK = 0xFFFFFFFFL;
    static final int DEFAULT_BLOCK_ORDER = 3;
    // offset to write new block 
    protected int m_nextOffset = 1;    
    // array to store actual data     
    protected int[] m_data;
    // array to store blocks offsets
    protected int m_blocks[]; 
    // block size = 1 << blockOrder;    
    protected int m_blockOrder;
    protected int m_blockOrder2;
    protected int m_blockSize;
    protected int m_blockMask;
    protected int m_blockMemorySize;
    int m_bx, m_by, m_bz, m_bxz;

    public SparseGridInt(Bounds bounds, double voxelSize) {
        this(bounds, DEFAULT_BLOCK_ORDER, voxelSize);
    }

    public SparseGridInt(Bounds bounds, int blockOrder, double voxelSize) {
        super(bounds, voxelSize, voxelSize);
        m_blockOrder = blockOrder;
        m_blockOrder2 = 2*blockOrder;
        m_blockSize = 1 << blockOrder;
        m_blockMask = m_blockSize-1;
        m_blockMemorySize = m_blockSize*m_blockSize*m_blockSize;
        allocateData();
    }

    protected void allocateData(){
        

        m_bx = (width + m_blockSize-1)/ m_blockSize;
        m_by = (height + m_blockSize-1)/ m_blockSize;
        m_bz = (depth + m_blockSize-1)/ m_blockSize;
        m_bxz = m_bx*m_bz;

        if((long)m_bx * m_by * m_bz > MAX_DATA_SIZE) 
            throw new RuntimeException(fmt("grid is too large: block dimension: (%d x %d x %d)",m_bx, m_by, m_bz));
        
        m_blocks = new int[m_bx * m_by * m_bz];
        
        if(DEBUG)printf("blocks: %d x %d x %d\n", m_bx, m_by, m_bz);
        if(DEBUG)printf("blockSize: %d\n", m_blockSize);
        if(DEBUG)printf("blockMask: 0x%x\n", m_blockMask);
        if(DEBUG)printf("blockOrder: %d\n", m_blockOrder);
        if(DEBUG)printf("blockOrder2: %d\n", m_blockOrder2);

        int initialDataSize = m_blockMemorySize+1;

        m_data = new int[initialDataSize];
    }
    
    /**
     * Create an empty grid of the specified size.  Reuses
     * the grid type and material type(byte, short, int).
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public Grid createEmpty(int w, int h, int d, double pixel, double sheight) {
        throw new RuntimeException("createEmpty() not implemented");
    }

    /**
     * Get the material of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public long getAttribute(int x, int y, int z) {

        int bx = (x >> m_blockOrder);
        int by = (y >> m_blockOrder);
        int bz = (z >> m_blockOrder);
        int blockIndex = by*m_bxz + bx * m_bz + bz;
        int blockOffset = m_blocks[blockIndex];
        if(blockOffset == 0){
            // empty block 
            return 0;
        }
        int xb = (x & m_blockMask);
        int yb = (y & m_blockMask);
        int zb = (z & m_blockMask);

        int dataOffset = blockOffset + (yb << m_blockOrder2) + (xb << m_blockOrder) + zb;
        return m_data[dataOffset];
    }

    /**
     * Set the material value of a voxel.  Leaves the state unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param attribute attribute value 
     */
    public void setAttribute(int x, int y, int z, long attribute) {
        if(DEBUG)printf("setAttribute(%d %d %d %d)\n", x,y,z,attribute);
        // block coordinates 
        int bx = (x >> m_blockOrder);
        int by = (y >> m_blockOrder);
        int bz = (z >> m_blockOrder);
        if(DEBUG)printf("block %d %d %d\n", bx, by, bz);
        int blockIndex = by*m_bxz + bx * m_bz + bz;
        int dataOffset = m_blocks[blockIndex];
        if(DEBUG)printf("blockIndex %d dataOffset: %d\n", blockIndex, dataOffset);
        if(dataOffset == 0){
            // allocate new block 
            if(DEBUG)printf("allocate new block\n");
            if(m_nextOffset + m_blockMemorySize > m_data.length){
                if(2L * m_data.length > MAX_DATA_SIZE) 
                    throw new RuntimeException(fmt("max data size exceeded: %d", MAX_DATA_SIZE));
                if(DEBUG)printf("realloc data: %d\n", 2 * m_data.length);
                
                int data[] = new int[2 * m_data.length];
                System.arraycopy(m_data, 0, data, 0, m_data.length);
                m_data = data;
                
            }
            m_blocks[blockIndex] = m_nextOffset;
            dataOffset = m_nextOffset;
            m_nextOffset += m_blockMemorySize;
        }
        // in-block coordinates 
        int xb = (x & m_blockMask);
        int yb = (y & m_blockMask);
        int zb = (z & m_blockMask);
        if(DEBUG)printf("xb: %d %d %d\n", xb, yb, zb);
        if(DEBUG)printf("dataOffset: %d\n", dataOffset);
        
        int dataIndex = dataOffset + (yb << m_blockOrder2) + (xb << m_blockOrder) + zb;
        if(DEBUG)printf("dataIndex %d\n", dataIndex);
        m_data[dataIndex] = (int)attribute;
    }

    /**
     * Clone the object.
     */
    public Object clone() {
        throw new RuntimeException("setState() not impelemente");
    }

    public int getDataSize(){
        return (m_data.length + m_blocks.length)*SIZEOF_INT;
    }
    
}

