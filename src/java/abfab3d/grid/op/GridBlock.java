/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2017
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/
package abfab3d.grid.op;

import abfab3d.core.Grid;
import abfab3d.core.Bounds;

import java.util.Vector;

import static java.lang.Math.min;
import static abfab3d.core.Output.printf;

/**
   describes a block of a grid with given bounds 
   
 */
public class GridBlock {
    
    static final boolean DEBUG=false;
    
    // min and max coordinates of blocks (inclusive)
    public int 
        xmin, xmax, ymin, ymax, zmin, zmax;
    
    Bounds m_gridBounds;
    double m_voxelSize;

    public GridBlock(int xmin, int xmax, int ymin, int ymax, int zmin, int zmax, Bounds bounds, double voxelSize){
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
        this.zmin = zmin;
        this.zmax = zmax;
        m_gridBounds = bounds;
        m_voxelSize = voxelSize;
       
    }

    
    public Bounds getBounds(){
        
        Bounds bounds = new Bounds();
        return getBounds(bounds);
    }

    
    public Bounds getBounds(Bounds bounds){

        bounds.xmin = m_gridBounds.xmin + m_voxelSize*xmin;
        bounds.xmax = m_gridBounds.xmin + m_voxelSize*(xmax+1);

        bounds.ymin = m_gridBounds.ymin + m_voxelSize*ymin;
        bounds.ymax = m_gridBounds.ymin + m_voxelSize*(ymax+1);

        bounds.zmin = m_gridBounds.zmin + m_voxelSize*zmin;
        bounds.zmax = m_gridBounds.zmin + m_voxelSize*(zmax+1);

        return bounds;
        
    }

    /**
       @param gridBounds the grid bounds from which the block is taken 
       @return physical bounds of the block based on bounds of grid 
     */
    private Bounds _getBounds(Bounds gridBounds, double vs){
        
        return new Bounds(gridBounds.xmin + vs*xmin, gridBounds.xmin + vs*(xmax+1),
                          gridBounds.ymin + vs*ymin, gridBounds.ymin + vs*(ymax+1),
                          gridBounds.zmin + vs*zmin, gridBounds.zmin + vs*(zmax+1)
                          );
        
    }


    /**
       generates set of blocks for procesing 
       @param bounds grid bounds
       @param vs grid voxel size
       
       @param blockSize max size of block 
       @param overlap overlap between blocks 
     */
    public static Vector<GridBlock> makeBlocks(Bounds bounds, double vs, int blockSize, int overlap){

        return makeBlocks(bounds.getGridWidth(vs),bounds.getGridHeight(vs),bounds.getGridDepth(vs), blockSize, overlap, bounds, vs);

    }
    /**
       generates set of blocks for procesing 
       @param nx - width of grid 
       @param ny - height of grid 
       @param nz - depth of grid 
       @param blockSize max size of block 
       @param overlap overlap between blocks 
     */
    static private Vector<GridBlock> makeBlocks(int nx, int ny, int nz, int blockSize, int overlap, Bounds gridBounds, double voxelSize){

        if(DEBUG) printf("makeBlocks(%d %d %d bockSize:%d overlap: %d\n", nx, ny, nz, blockSize, overlap);
        int rSize = blockSize - overlap;
        int maxX = nx-1;
        int maxY = ny-1;
        int maxZ = nz-1;

        Vector<GridBlock> blocks = new Vector<GridBlock>();

        for(int y0 = 0; y0 < maxY; y0 += rSize){
            int y1 = min(y0 + blockSize-1, maxY);

            for(int x0 = 0; x0 < maxX; x0 += rSize){
                int x1 = min(x0 + blockSize-1, maxX);

                for(int z0 = 0; z0 < maxZ; z0 += rSize){
                    int z1 = min(z0 + blockSize-1, maxZ);
                    if(DEBUG) printf("block[%2d %2d; %2d %2d; %2d %2d]\n", x0, x1, y0, y1, z0, z1);
                    blocks.add(new GridBlock(x0, x1, y0, y1, z0, z1,gridBounds, voxelSize));
                }
            }
        }
        return blocks;
    }
}