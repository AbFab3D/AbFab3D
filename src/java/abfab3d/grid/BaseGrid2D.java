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

import abfab3d.util.Bounds;

import java.io.Serializable;
import static abfab3d.util.Output.printf;

/**
 * Base class implementation of Grids.  Includes common code that
 * may get overwritten by faster implementations.
 * <p/>
 * Likely better performance for memory access that is not slice aligned.
 * <p/>
 * Uses the X3D coordinate system.  Y-up.  Grid is located
 * on positive right side octant.
 *
 * @author Alan Hudson
 */
public abstract class BaseGrid2D implements Grid2D, Cloneable, Serializable {

    protected static final boolean DEBUG = false;
    protected static final boolean STATS = false;

    /**
     * The width of the grid
     */
    protected int width;

    /**
     * The depth of the grid
     */
    protected int height;

    /**
     * The voxel size
     */
    protected double pixelSize;

    /**
     * Half the horizontal size
     */
    protected double hpixelSize;

    /**
     * location of the grid corner
     */
    protected double xorig = 0.;
    protected double yorig = 0.;
    protected double zorig = 0.;  

    // attribute descriptor used for this grid
    protected AttributeDesc m_attributeDesc = AttributeDesc.getDefaultAttributeDesc(8);

    /**
     * Constructor.
     *
     * @param w       The number of voxels in width
     * @param h       The number of voxels in depth
     * @param pixel   The size of the pixels
     */
    public BaseGrid2D(int w, int h, double pixel) {
        width = w;
        height = h;
        this.pixelSize = pixel;
        this.hpixelSize = pixelSize / 2.0;

        if (STATS) {
            System.out.println("WARNING:  BaseGrid started in STATS mode");
        }
    }

    /**
     * Constructor.
     *
     * @param bounds the bounds of the grid
     * @param pixel The size of the pixels
     */
    public BaseGrid2D(Bounds bounds, double pixel) {
        
        width = bounds.getWidthVoxels(pixel);
        height = bounds.getHeightVoxels(pixel);
        this.pixelSize = pixel;
        this.hpixelSize = pixelSize / 2.0;
        this.xorig = bounds.xmin;
        this.yorig = bounds.ymin;

        if (STATS) {
            System.out.println("WARNING:  BaseGrid started in STATS mode");
        }
    }

    /**
     * Get a new instance of voxel data.  Returns this grids specific sized voxel data.
     *
     * @return The voxel data
     */
    //public VoxelData getVoxelData() {
        // This is a default impl.  For larger sizes the grid is expected to implement.
    //    return new VoxelDataByte();
    //}

    /**
     * Get the grid coordinates for a world coordinate.
     *
     * @param x      The x value in world coords
     * @param y      The y value in world coords
     * @param coords The ans is placed into this preallocated array(3).
     */
    //public void getGridCoords(double x, double y, int[] coords) {
    //    coords[0] = (int) ((x - xorig) / pixelSize);
    //    coords[1] = (int) ((y - yorig) / pixelSize);
    //}

    /**
     * Get the world coordinates of center of grid voxel 
     * 
     * @param x      The x value in grid coords
     * @param y      The y value in grid coords
     * @param coords The ans is placed into this preallocated array(3).
     */
    //public void getWorldCoords(int x, int y, double[] coords) {

    //        coords[0] = x * pixelSize + hpixelSize + xorig;
    //    coords[1] = y * pixelSize + hpixelSize + yorig;
    //}

    /**
     * Get the grid bounds in world coordinates.
     */
    public Bounds getGridBounds() {

        Bounds bounds = new Bounds(xorig, xorig + width * pixelSize,
                                   yorig, yorig + height * pixelSize,
                                   zorig, zorig + pixelSize);
        bounds.setGridSize(width, height, 1);
        return bounds;
    }

    /**
     * Set the grid bounds in world coordinates.
     *
     * @param bounds grid bounds 
     */
    public void setGridBounds(Bounds bounds) {

        if(DEBUG)printf("setGridBounds(%s)\n", bounds);

        xorig = bounds.xmin;
        yorig = bounds.ymin;
        zorig = bounds.zmin;
        pixelSize = (bounds.xmax - bounds.xmin) / width;

        if(DEBUG)printf("getGridBounds() returns: %s\n", getGridBounds());
    }

    /**
     * Set the grid bounds in world coordinates.
     *
     * @param bounds array {xmin, xmax, zmin, ymax, zmin, zmax}
     */
    //public void setGridBounds(double[] bounds) {
    //    setGridBounds(new Bounds(bounds));
    //}

    /**
     * Determine if a voxel coordinate is inside the grid space.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @return True if the coordinate is inside the grid space
     */
    public boolean insideGrid(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height ) {

            return true;
        }

        return false;
    }

    /**
     * Get the number of height cells.
     *
     * @return the val
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the number of width cells.
     *
     * @return the val
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the number of dots per meter.
     *
     * @return The value
     */
    public double getVoxelSize() {
        return pixelSize;
    }

    public abstract Object clone();

    /**
     assign to the grid a description of a voxel attributes
     @param description The attirbute description
     @override
     */
    public void setAttributeDesc(AttributeDesc description){
        m_attributeDesc = description;
    }

    /**
     @return voxel attribute description assigned to the grid
     @override
     */
    public AttributeDesc getAttributeDesc(){
        return m_attributeDesc;
    }

}

