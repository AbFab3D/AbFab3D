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
public class SliceGrid extends BaseGrid {
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

        this((int) (w / pixel) + 1, (int) (h / sheight) + 1,
           (int) (d / pixel) + 1, pixel, sheight, useArray);
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

        super(w,h,d,pixel,sheight);

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

}
