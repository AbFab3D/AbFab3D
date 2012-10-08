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
import java.io.Serializable;


/**
 * Base class implementation of Grids.  Includes common code that
 * may get overwritten by faster implementations.
 *
 * Likely better performance for memory access that is not slice aligned.
 *
 * Uses the X3D coordinate system.  Y-up.  Grid is located
 * on positive right side octant.
 *
 * @author Alan Hudson
 */
public abstract class BaseGrid implements Grid, Cloneable,Serializable {
    // Empty voxel data value
    protected static final VoxelData EMPTY_VOXEL;

    /** The width of the grid */
    protected int width;

    /** The height of the grid */
    protected int height;

    /** The depth of the grid */
    protected int depth;

    /** The horizontal voxel size */
    protected double pixelSize;

    /** Half the horizontal size */
    protected double hpixelSize;

    /** The slice height */
    protected double sheight;

    /** Half the slice height */
    protected double hsheight;

    /** The number of voxels in a slice */
    protected int sliceSize;

    static {
        EMPTY_VOXEL = new VoxelDataByte(Grid.OUTSIDE, NO_MATERIAL);
    }

    /**
     * Constructor.
     *
     * @param w The width in world coords
     * @param h The height in world coords
     * @param d The depth in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public BaseGrid(double w, double h, double d, double pixel, double sheight) {
        this((int) (w / pixel) + 1, (int) (h / sheight) + 1,
           (int) (d / pixel) + 1, pixel, sheight);
    }

    /**
     * Constructor.
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public BaseGrid(int w, int h, int d, double pixel, double sheight) {
        width = w;
        height = h;
        depth = d;
        this.pixelSize = pixel;
        this.hpixelSize = pixelSize / 2.0;
        this.sheight = sheight;
        this.hsheight = sheight / 2.0;

        sliceSize = w * d;
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void find(VoxelClasses vc, ClassTraverser t) {
        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    byte state = getState(x,y,z);

                    switch(vc) {
                        case ALL:
                            t.found(x,y,z,state);
                            break;
                        case MARKED:
                            state = getState(x,y,z);
                            if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                t.found(x,y,z,state);
                            }
                            break;
                        case EXTERIOR:
                            if (state == Grid.EXTERIOR) {
                                t.found(x,y,z,state);
                            }
                            break;
                        case INTERIOR:
                            if (state == Grid.INTERIOR) {
                                t.found(x,y,z,state);
                            }
                            break;
                        case OUTSIDE:
                            if (state == Grid.OUTSIDE) {
                                t.found(x,y,z,state);
                            }
                            break;
                    }
                }
            }
        }
    }


    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     */
    public int findCount(VoxelClasses vc) {
        int ret_val = 0;

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    VoxelData vd = getData(x,y,z);

                    byte state;

                    switch(vc) {
                        case ALL:
                            ret_val++;
                            break;
                        case MARKED:
                            state = vd.getState();
                            if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                ret_val++;
                            }
                            break;
                        case EXTERIOR:
                            state = vd.getState();
                            if (state == Grid.EXTERIOR) {
                                ret_val++;
                            }
                            break;
                        case INTERIOR:
                            state = vd.getState();
                            if (state == Grid.INTERIOR) {
                                ret_val++;
                            }
                            break;
                        case OUTSIDE:
                            state = vd.getState();
                            if (state == Grid.OUTSIDE) {
                                ret_val++;
                            }
                            break;
                    }
                }
            }
        }

        return ret_val;
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findInterruptible(VoxelClasses vc, ClassTraverser t) {
        loop:
        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    byte state = getState(x,y,z);

                    switch(vc) {
                        case ALL:
                            if (!t.foundInterruptible(x,y,z,state))
                                break loop;
                            break;
                        case MARKED:
                            if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                if (!t.foundInterruptible(x,y,z,state))
                                    break loop;
                            }
                            break;
                        case EXTERIOR:
                            if (state == Grid.EXTERIOR) {
                                if (!t.foundInterruptible(x,y,z,state))
                                    break loop;
                            }
                            break;
                        case INTERIOR:
                            if (state == Grid.INTERIOR) {
                                if (!t.foundInterruptible(x,y,z,state))
                                    break loop;
                            }
                            break;
                        case OUTSIDE:
                            if (state == Grid.OUTSIDE) {
                                if (!t.foundInterruptible(x,y,z,state))
                                    break loop;
                            }
                            break;
                    }
                }
            }
        }
    }

    /**
     * Get the grid coordinates for a world coordinate.
     *
     * @param x The x value in world coords
     * @param y The y value in world coords
     * @param z The z value in world coords
     * @param coords The ans is placed into this preallocated array(3).
     */
    public void getGridCoords(double x, double y, double z, int[] coords) {
        coords[0] = (int) (x / pixelSize);
        coords[1] = (int) (y / sheight);
        coords[2] = (int) (z / pixelSize);
    }

    /**
     * Get the world coordinates for a grid coordinate.
     *
     * @param x The x value in grid coords
     * @param y The y value in grid coords
     * @param z The z value in grid coords
     * @param coords The ans is placed into this preallocated array(3).
     */
    public void getWorldCoords(int x, int y, int z, double[] coords) {
        coords[0] = x * pixelSize + hpixelSize;
        coords[1] = y * sheight + hsheight;
        coords[2] = z * pixelSize + hpixelSize;
    }

    /**
     * Get the grid bounds in world coordinates.
     *
     * @param min The min coordinate
     * @param max The max coordinate
     */
    public void getGridBounds(double[] min, double[] max) {
        min[0] = 0;
        min[1] = 0;
        min[2] = 0;

        max[0] = width * pixelSize;
        max[1] = height * sheight;
        max[2] = depth * pixelSize;
    }

    /**
     * Determine if a voxel coordinate is inside the grid space.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return True if the coordinate is inside the grid space
     */
    public boolean insideGrid(int x, int y, int z) {
        if (x >= 0 && x < width &&
            y >= 0 && y < height &&
            z >= 0 && z < depth) {

            return true;
        }

        return false;
    }

    /**
     * Determine if a voxel coordinate is inside the grid space.
     *
     * @param wx The x world coordinate
     * @param wy The y world coordinate
     * @param wz The z world coordinate
     * @return True if the coordinate is inside the grid space
     */
    public boolean insideGrid(double wx, double wy, double wz) {

        int x = (int) (wx / pixelSize);
        int y = (int) (wy / sheight);
        int z = (int) (wz / pixelSize);

        if (x >= 0 && x < width &&
                y >= 0 && y < height &&
                z >= 0 && z < depth) {

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
     * Get the number of depth cells.
     *
     * @return the val
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Get the slice height.
     *
     * @return The value
     */
    public double getSliceHeight() {
        return sheight;
    }

    /**
     * Get the number of dots per meter.
     *
     * @return The value
     */
    public double getVoxelSize() {
        return pixelSize;
    }

    /**
     * Print out a slice of data.
     */
    public String toStringSlice(int y) {
        StringBuilder sb = new StringBuilder();

        for(int i=0; i < depth; i++) {
            for(int j=0; j < width; j++) {
                sb.append(getState(i,y,j));
                sb.append(" ");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public String toStringAll() {
        StringBuilder sb = new StringBuilder();

        sb.append("Grid:  height: ");
        sb.append(height);
        sb.append("\n");

        for(int i=0; i < height; i++) {
            sb.append(i);
            sb.append(":\n");
            sb.append(toStringSlice(i));
        }

        return sb.toString();
    }

    public abstract Object clone();
}

