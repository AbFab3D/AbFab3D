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

package abfab3d.grid;

import abfab3d.grid.Grid;
import abfab3d.grid.VoxelData;
import abfab3d.grid.VoxelDataByte;
import abfab3d.grid.ClassTraverser;
import abfab3d.grid.ClassAttributeTraverser;
import abfab3d.grid.AttributeGrid;

//import abfab3d.grid.util.BitIntervals;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;

/**
   grid array to represent on/off mask. 
   Implemented as 2D array of intervals

   @author Vladimir Bulatov
*/
public class GridBitIntervalsBlocks extends GridBitIntervals {
    
    static final int DEFAULT_BLOCK_ORDER = 5;

    int m_bx, m_by, m_bz; 
    
    Block m_bdata[];
    
    int m_blockMask;
    int m_blockOrder;

    /**
       create grid of given orientation 
     */
    public GridBitIntervalsBlocks(int nx, int ny, int nz){
        this(nx, ny, nz, ORIENTATION_Z, DEFAULT_BLOCK_ORDER);
    }

    public GridBitIntervalsBlocks(int nx, int ny, int nz, int orientation, int blockOrder ){
        
        m_nx = nx;
        m_ny = ny;
        m_nz = nz;

        m_blockOrder = blockOrder;
        m_blockMask = (1 << blockOrder) -1;
        
        m_bx = (m_nx + m_blockMask) >> blockOrder;
        m_by = (m_ny + m_blockMask) >> blockOrder;
        m_bz = (m_nz + m_blockMask) >> blockOrder;
        

        m_orientation = orientation;
        
        switch(m_orientation){
        default:
        case ORIENTATION_X:
            m_bdata = new Block[m_by * m_bz];
            break;
        case ORIENTATION_Y:
            m_bdata = new Block[m_bx * m_bz];
            break;
        case ORIENTATION_Z:
            m_bdata = new Block[m_bx * m_by];
            break;
        }
    }

    static int getBlockMask(int blockOrder){
        int m = 0;
        for(int i = 0; i < blockOrder; i++){
            m = m | (1 << i);
        }
        return m;
    }

    /**
       return bit at given point 
     */
    public long get(int x, int y, int z){
        
        switch(m_orientation){

        default:
        case ORIENTATION_X:
            {
                Block block = m_bdata[y >> m_blockOrder + m_by* (z >> m_blockOrder)];
                if(block == null)
                    return 0;

                BitIntervals interval = block.get(y & m_blockMask, z & m_blockMask);
                if(interval == null)
                    return 0;
                else 
                    return interval.get(x);                
            }
        case ORIENTATION_Y:
            {
                Block block = m_bdata[x >> m_blockOrder + m_bx* (z >> m_blockOrder)];
                if(block == null)
                    return 0;

                BitIntervals interval = block.get(x & m_blockMask, z & m_blockMask);
                if(interval == null)
                    return 0;
                else 
                    return interval.get(y);                
            }
        case ORIENTATION_Z:
            {
                Block block = m_bdata[x >> m_blockOrder + m_bx* (y >> m_blockOrder)];
                if(block == null)
                    return 0;

                BitIntervals interval = block.get(x & m_blockMask, y & m_blockMask);
                if(interval == null)
                    return 0;
                else 
                    return interval.get(z);                
            }
        }
        
    }
    
    /**
       set bit at given point 
     */
    public void set(int x, int y, int z, int value){

        switch(m_orientation){

        default:
        case ORIENTATION_X:
            {
                int by = y >> m_blockOrder;
                int bz = z >> m_blockOrder;
                int yy = y & m_blockMask;
                int zz = z & m_blockMask;
                
                int ind = by + m_by * bz;
                Block block = m_bdata[ind];
                
                if(block == null){
                    block = m_bdata[ind] = new Block(m_blockOrder);
                }
                BitIntervals bi = block.get(yy, zz);
                if(bi == null){
                    bi = new BitIntervals();
                    block.set(yy, zz, bi);
                }                    
                bi.set(x, value);
                break;
            }
        case ORIENTATION_Y:
            {
                int bx = x >> m_blockOrder;
                int bz = z >> m_blockOrder;
                int xx = x & m_blockMask;
                int zz = z & m_blockMask;
                
                int ind = bx + m_bx * bz;
                Block block = m_bdata[ind];
                
                if(block == null){
                    block = m_bdata[ind] = new Block(m_blockOrder);
                }
                BitIntervals bi = block.get(xx, zz);
                if(bi == null){
                    bi = new BitIntervals();
                    block.set(xx, zz, bi);
                }                    
                bi.set(y, value);

                break;
            }
        case ORIENTATION_Z:
            {
                int bx = x >> m_blockOrder;
                int by = y >> m_blockOrder;
                int xx = x & m_blockMask;
                int yy = y & m_blockMask;
                
                int ind = bx + m_bx * by;
                Block block = m_bdata[ind];
                
                if(block == null){
                    block = m_bdata[ind] = new Block(m_blockOrder);
                }
                BitIntervals bi = block.get(xx, yy);
                if(bi == null){
                    bi = new BitIntervals();
                    block.set(xx, yy, bi);
                }                    
                bi.set(z, value);

                break;
            }
        }

    }

    static class Block {
        
        BitIntervals m_data[];
        int nx;
        int blockOrder; 
        
        Block(int blockOrder){
            
            nx = (1 << blockOrder);
            m_data = new BitIntervals[nx*nx];
            
        }
        
        BitIntervals get(int x, int y){
            return m_data[x + nx*y];
        }
        
        void set(int x, int y, BitIntervals bi){
            m_data[x + nx*y] = bi;
        }
    } // class Block     
    
} // GridBitIntervalsBlocks

