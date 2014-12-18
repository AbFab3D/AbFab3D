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


import static abfab3d.util.Output.fmt;

/**
 * A 2D grid backed by array. 
 * it formally uses interface of AttributeGrid, but z param is ignored 
 * 
 * @author Vladimir Bulatov
 */
public class Grid2DInt extends BaseAttributeGrid implements Grid2D {

    static final long INTMASK = 0xFFFFFFFFL;

    protected int[] data;

    /**
     * Constructor.
     *
     * @param width The number of voxels in width
     * @param height The number of voxels in height
     */
    public Grid2DInt(int width, int height){
        super(width, height, 1, 1., 1., null);
        if((long)height*width > Integer.MAX_VALUE)
            throw new RuntimeException(fmt("grid size: [%d x %d] exceeds maximum [46340 x 46340]", width, height));
        data = new int[height * width];
    }

    /**
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public Grid2DInt(Grid2DInt grid) {
        super(grid.getWidth(), grid.getHeight(), 1, 1., 1., null);
        this.data = grid.data.clone();
        BaseGrid.copyBounds(grid, this);        
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

        Grid ret_val = new Grid2DInt(w,h);
        
        return ret_val;
    }

    /**
     * Get the attribute of the voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate (this param is ignored) 
     * @return The voxel material
     */
    public final long getAttribute(int x, int y, int z) {
        return getAttribute(x,y);
    }

    /**
     * Set the attribute of voxel 
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param attribute value of attribute 
     */
    public final void setAttribute(int x, int y, int z, long attribute) {
        setAttribute(x,y,attribute);
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
     * Clone the object.
     */
    public Object clone() {
        return new Grid2DInt(this);
    }
}
