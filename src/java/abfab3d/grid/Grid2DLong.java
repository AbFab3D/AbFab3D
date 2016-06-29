/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011-2013
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

import abfab3d.core.Bounds;
import abfab3d.core.Grid2D;


import static abfab3d.core.Output.fmt;

/**
 * A 2D grid backed by arrays. 
 * it formally uses interface of AttributeGrid, but z param is ignored 
 * 
 * @author Vladimir Bulatov
 */
public class Grid2DLong extends BaseGrid2D implements Grid2D {
    protected long[] data;

    /**
     * Constructor.
     *
     * @param width The number of voxels in width
     * @param height The number of voxels in height
     */
    public Grid2DLong(int width, int height){
        super(width, height, 1.);
        allocateData();
    }

    /**
     * Constructor.
     *
     * @param width The number of voxels in width
     * @param height The number of voxels in height
     * @param pixel The size of the pixel in meters
     */
    public Grid2DLong(int width, int height, double pixel){
        super(width, height, pixel);
        allocateData();
    }

    /**
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public Grid2DLong(Grid2DLong grid) {
        super(grid.getWidth(), grid.getHeight(), grid.getVoxelSize());
        copyBounds(grid);
        this.data = grid.data.clone();
    }


    /**
       @param bounds grid bounds 
       @param pxiel size of grid pixel
     */
    public Grid2DLong(Bounds bounds, double pixel) {
        super(bounds, pixel);
        allocateData();
    }

    protected void allocateData() {
        if((long)height*width > Integer.MAX_VALUE)
            throw new RuntimeException(fmt("grid size: [%d x %d] exceeds maximum [46340 x 46340]", width, height));
        data = new long[height * width];
    }
    /**
     * Create an empty grid of the specified size.  Reuses
     * the grid type and material type(byte, short, int).
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param pixel The size of the pixels
     */
    public Grid2D createEmpty(int w, int h, double pixel) {

        Grid2D ret_val = new Grid2DByte(w,h,pixel);
        
        return ret_val;
    }

    /**
     * Get the attribute of the voxel, 2D version
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @return The voxel attribute 
     */
    public final long getAttribute(int x, int y) {

        int idx = y * width + x;
        return (data[idx] & 0xFF);
    }

    /**
     * Set the attribute of a voxel, 2D version 
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param attribute value of attribute 
     */
    public final void setAttribute(int x, int y, long attribute) {
        int idx = y * width + x;
        data[idx] = (byte) (attribute & 0xFF);
    }

    /**
     * Clone the object.
     */
    public Object clone() {
        return new Grid2DLong(this);
    }
}
