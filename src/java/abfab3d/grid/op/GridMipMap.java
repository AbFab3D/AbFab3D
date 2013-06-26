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
package abfab3d.grid.op;

import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;

import static abfab3d.util.Output.printf;

/**
   provides accress to a grid on several levels of details. 
   similar to mipmapping techniques used for texture mapping
 */
public class GridMipMap {
    
    static final boolean DEBUG = true;

    AttributeGrid grids[];

    public GridMipMap(AttributeGrid grid){
        createMipMap(grid);
    }
    
    protected void createMipMap(AttributeGrid grid){
        
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        
        if(DEBUG)
            printf("createMipMap()\n grid [%d x %d x %d]\n", nx, ny, nz);
        int levelCount = 1;
        while(nx > 1 || ny > 1 ||nz > 1){
            nx = (nx+1)/2;
            ny = (ny+1)/2;
            nz = (nz+1)/2;
            if(DEBUG)
                printf("  mipmap level [%d x %d x %d]\n", nx, ny, nz);
            levelCount++;
        }
        printf("  levelCount: %d\n", levelCount);
    }

}
