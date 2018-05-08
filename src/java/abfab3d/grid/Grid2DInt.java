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
 * A 2D grid backed by int array. 
 * 
 * @author Vladimir Bulatov
 */
public class Grid2DInt extends BaseGrid2D implements Grid2D {

    static final long INTMASK = 0xFFFFFFFFL;

    protected int[] data;

    /**
     * Constructor.
     *
     * @param width The number of voxels in width
     * @param height The number of voxels in height
     */
    public Grid2DInt(int width, int height){
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
    public Grid2DInt(int width, int height, double pixel) {
        super(width, height, pixel);
        allocateData();
    }

    public Grid2DInt(Bounds bounds, double pixel) {
        super(bounds, pixel);
        allocateData();
    }

    /**
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public Grid2DInt(Grid2DInt grid) {
        super(grid.getWidth(), grid.getHeight(), 1.);
        copyBounds(grid);
        this.data = grid.data.clone();
    }

    protected void allocateData(){
        if((long)height*width > Integer.MAX_VALUE)
            throw new RuntimeException(fmt("grid size: [%d x %d] exceeds maximum [46340 x 46340]", width, height));
        data = new int[height * width];
    }

    /**
     * Create an empty grid of the specified size.  Reuses
     * the grid type and material type(byte, short, int).
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param pixel The pixel size in meters
     */
    public Grid2D createEmpty(int w, int h, double pixel) {

        Grid2D ret_val = new Grid2DInt(w,h,pixel);
        
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
        return (data[idx] & INTMASK);
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
        data[idx] = (int) (attribute & INTMASK);
    }

    /**
       sets all values to given 
     */
    public void fill(long value){
        int v = (int)(value & 0xFFFFFFFF);        
        int len = data.length;
        for(int i = 0; i < len; i++){
            data[i] = v;
        }
    }

    /**
     * Clone the object.
     */
    public Object clone() {
        return new Grid2DInt(this);
    }
}
