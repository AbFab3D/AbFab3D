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
import java.util.*;
import java.io.*;
import org.web3d.vrml.sav.BinaryContentHandler;

/**
 * A grid composed of slices.
 *
 * Uses the X3D coordinate system.  Y-up.  Grid is located
 * on positive right side octant.
 *
 * Some operations might be easy to do as 2D ops of the slice
 * such as making solid objects hollow.
 *
 * Others might need the 3D rep, such as providing an exit for
 * material or creating varying structures over the length
 *
 * Objet:
 *     Connex500:
 *           xy 600 dpi(42 micron)
 *           z  1600 dpi(16 micron)
 *
 * @author Alan Hudson
 */
public class SliceGrid implements Grid {
    /** The maximum coords to put in a shape */
    private static final int MAX_TRIANGLES_SHAPE = 300000;

    protected int width;
    protected int height;
    protected int depth;
    protected double pixelSize;
    protected double hpixelSize;
    protected double sheight;
    protected double hsheight;
    protected Slice[] data;

    /**
     * Constructor.
     *
     * @param w The width in world coords
     * @param h The height in world coords
     * @param d The depth in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     * @param useArray Use an array.  false means use map
     */
    public SliceGrid(double w, double h, double d, double pixel, double sheight,
        boolean useArray) {

        this((int) Math.ceil(w / pixel), (int) Math.ceil(h / sheight),
           (int) Math.ceil(d / pixel), pixel, sheight, useArray);
    }

    /**
     * Constructor.
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     * @param useArray Use an array.  false means use map
     */
    public SliceGrid(int w, int h, int d, double pixel, double sheight,
        boolean useArray) {
        width = w;
        height = h;
        depth = d;
        this.pixelSize = pixel;
        this.hpixelSize = pixelSize / 2.0;
        this.sheight = sheight;
        this.hsheight = sheight / 2.0;

        data = new Slice[height];

        // use Array form when memory is not an issue
        // TODO: Should confirm array form is indeed faster

        if (useArray) {
            for(int i=0; i < height; i++) {
                data[i] = new SliceArray(width,depth);
            }
        } else {
            for(int i=0; i < height; i++) {
                data[i] = new SliceMap(width,depth);
            }
        }
    }

    /**
     * Get the data of the voxel
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param The voxel state
     */
    public VoxelData getData(int x, int y, int z) {
        return data[y].getData(x,z);
    }

    /**
     * Get the data of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param The voxel state
     */
    public VoxelData getData(double x, double y, double z) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        return data[slice].getData(s_x,s_z);
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param The voxel state
     */
    public byte getState(double x, double y, double z) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        return data[slice].getState(s_x,s_z);
    }

    /**
     * Get the state of the voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param The voxel state
     */
    public byte getState(int x, int y, int z) {
        return data[y].getState(x,z);
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param The voxel material
     */
    public byte getMaterial(double x, double y, double z) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        return data[slice].getMaterial(s_x,s_z);
    }

    /**
     * Get the material of the voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param The voxel material
     */
    public byte getMaterial(int x, int y, int z) {
        return data[y].getMaterial(x,z);
    }


    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public int findCount(VoxelClasses vc) {
        int ret_val = 0;

        for(int y=0; y < height; y++) {
            Slice slice = data[y];

            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    VoxelData vd = slice.getData(x,z);

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
    public void find(VoxelClasses vc, ClassTraverser t) {
        for(int y=0; y < height; y++) {
            Slice slice = data[y];

            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    VoxelData vd = slice.getData(x,z);

                    byte state;

                    switch(vc) {
                        case ALL:
                            t.found(x,y,z,vd);
                            break;
                        case MARKED:
                            state = vd.getState();
                            if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                t.found(x,y,z,vd);
                            }
                            break;
                        case EXTERIOR:
                            state = vd.getState();
                            if (state == Grid.EXTERIOR) {
                                t.found(x,y,z,vd);
                            }
                            break;
                        case INTERIOR:
                            state = vd.getState();
                            if (state == Grid.INTERIOR) {
                                t.found(x,y,z,vd);
                            }
                            break;
                        case OUTSIDE:
                            state = vd.getState();
                            if (state == Grid.OUTSIDE) {
                                t.found(x,y,z,vd);
                            }
                            break;
                    }
                }
            }
        }
    }

    /**
     * Set the value of a voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The voxel state
     * @param material The material
     */
    public void setData(double x, double y, double z, byte state, byte material) {
        // Determine slice

        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

//System.out.println("x: " + x + " y: " + y + " z: " + z);
//System.out.println("slice: " + slice + " s_x: " + s_x + " s_z: " + s_z);
        data[slice].setData(s_x,s_z,state,material);
    }

    /**
     * Set the value of a voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param state The voxel state
     * @param material The material
     */
    public void setData(int x, int y, int z, byte state, byte material) {
        // Determine slice

        data[y].setData(x,z,state,material);
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
    public String toStringSlice(int s) {
        return data[s].toStringSlice();
    }

    public String toStringAll() {
        StringBuilder sb = new StringBuilder();

        sb.append("Grid:  height: ");
        sb.append(height);
        sb.append("\n");

        for(int i=0; i < height; i++) {
            sb.append(i);
            sb.append(":\n");
            sb.append(data[i].toStringSlice());
        }

        return sb.toString();
    }
}

class Coordinate {
    public float x,y,z;

    public Coordinate(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int hashCode() {
        float val = x * 64 + y * 32 + z;

        return Float.floatToIntBits(val);
    }

    public boolean equals(Object o) {
        if (!(o instanceof Coordinate))
            return false;

        Coordinate coord = (Coordinate) o;
        if (x == coord.x &&
            y == coord.y &&
            z == coord.z) {

            return true;
        }

        return false;
    }

    public String toString() {
        return "Coordinate(" + hashCode() + ")" + x + " " + y + " " + z;
    }
}