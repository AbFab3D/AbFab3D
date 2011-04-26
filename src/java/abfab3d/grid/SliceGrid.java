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

System.out.println("width: " + width + " h: " + height + " d: " + depth);
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
     * Create an X3D file from the grid.  This should be an exact
     * repsentation of the grid values.
     *
     * @param stream The stream to write too
     * @param colors Maps materialID's to colors
     */
    public void toX3D(BinaryContentHandler stream, Map<Byte, float[]> colors) {
        HashMap<Coordinate,Integer> coords = new HashMap<Coordinate,Integer>();
        ArrayList<Integer> indices = new ArrayList<Integer>();
        ArrayList<Coordinate> lastSlice = new ArrayList<Coordinate>();
        ArrayList<Coordinate> thisSlice = new ArrayList<Coordinate>();
        ArrayList<Coordinate> totalCoords = new ArrayList<Coordinate>();

        // TODO: We might want to use a form of run length encoding to
        // reduce the number of boxes

        double x,y,z;
        int idx = 0;
        float[] color = new float[] {0.8f,0.8f,0.8f};
        float transparency = 0;

        for(int i=0; i < height; i++) {
            y = i * sheight;

            Slice s = data[i];

            int width = s.getWidth();
            int depth = s.getDepth();

            for(int j=0; j < width; j++) {
                x = j * pixelSize;

                for(int k=0; k < depth; k++) {
                    z = k * pixelSize;

                    byte state = s.getState(j,k);

                    if (state == Grid.OUTSIDE)
                        continue;



// TODO: Need to map material

//System.out.println("Live one: coord: " + j + " " + i + " " + k);
//System.out.println("   coord(wc): " + x + " " + y + " " + z);
                    Coordinate ubl_coord = new Coordinate((float)(x - hpixelSize),
                        (float)(y + hsheight),(float)(z - hpixelSize));
                    Integer ubl_pos = coords.get(ubl_coord);
                    if (ubl_pos == null) {
//System.out.println("ubl added: " + idx);
                        ubl_pos = new Integer(idx++);
                        coords.put(ubl_coord, ubl_pos);
                        thisSlice.add(ubl_coord);
                    }

                    Coordinate ubr_coord = new Coordinate((float)(x + hpixelSize),
                        (float)(y + hsheight),(float)(z - hpixelSize));
                    Integer ubr_pos = coords.get(ubr_coord);
                    if (ubr_pos == null) {
//System.out.println("ubr added: " + idx);
                        ubr_pos = new Integer(idx++);
                        coords.put(ubr_coord, ubr_pos);
                        thisSlice.add(ubr_coord);
                    }


                    Coordinate lbl_coord = new Coordinate((float)(x - hpixelSize),
                        (float)(y - hsheight),(float)(z - hpixelSize));
                    Integer lbl_pos = coords.get(lbl_coord);
                    if (lbl_pos == null) {
//System.out.println("lbl added: " + idx);
                        lbl_pos = new Integer(idx++);
                        coords.put(lbl_coord, lbl_pos);
                        thisSlice.add(lbl_coord);
                    }

                    Coordinate lbr_coord = new Coordinate((float)(x + hpixelSize),
                        (float)(y - hsheight),(float)(z - hpixelSize));
                    Integer lbr_pos = coords.get(lbr_coord);
                    if (lbr_pos == null) {
//System.out.println("lbr added: " + idx);
                        lbr_pos = new Integer(idx++);
                        coords.put(lbr_coord, lbr_pos);
                        thisSlice.add(lbr_coord);
                    }

                    Coordinate ufl_coord = new Coordinate((float)(x - hpixelSize),
                        (float)(y + hsheight),(float)(z + hpixelSize));
                    Integer ufl_pos = coords.get(ufl_coord);
                    if (ufl_pos == null) {
//System.out.println("ufl added: " + idx);
                        ufl_pos = new Integer(idx++);
                        coords.put(ufl_coord, ufl_pos);
                        thisSlice.add(ufl_coord);
                    }

                    Coordinate ufr_coord = new Coordinate((float)(x + hpixelSize),
                        (float)(y + hsheight),(float)(z + hpixelSize));
                    Integer ufr_pos = coords.get(ufr_coord);
                    if (ufr_pos == null) {
//System.out.println("ufr added: " + idx);
                        ufr_pos = new Integer(idx++);
                        coords.put(ufr_coord, ufr_pos);
                        thisSlice.add(ufr_coord);
                    }

                    Coordinate lfr_coord = new Coordinate((float)(x + hpixelSize),
                        (float)(y - hsheight),(float)(z + hpixelSize));
                    Integer lfr_pos = coords.get(lfr_coord);
                    if (lfr_pos == null) {
//System.out.println("lfr added: " + idx);
                        lfr_pos = new Integer(idx++);
                        coords.put(lfr_coord, lfr_pos);
                        thisSlice.add(lfr_coord);
                    }

                    Coordinate lfl_coord = new Coordinate((float)(x - hpixelSize),
                        (float)(y - hsheight),(float)(z + hpixelSize));
                    Integer lfl_pos = coords.get(lfl_coord);
                    if (lfl_pos == null) {
//System.out.println("lfl added: " + idx);
                        lfl_pos = new Integer(idx++);
                        coords.put(lfl_coord, lfl_pos);
                        thisSlice.add(lfl_coord);
                    }


                    // Create Box
                    // Front Face
                    indices.add(new Integer(lfr_pos));
                    indices.add(new Integer(ufr_pos));
                    indices.add(new Integer(ufl_pos));
                    indices.add(new Integer(lfr_pos));
                    indices.add(new Integer(ufl_pos));
                    indices.add(new Integer(lfl_pos));

                    // Back Face
                    indices.add(new Integer(lbr_pos));
                    indices.add(new Integer(ubl_pos));
                    indices.add(new Integer(ubr_pos));
                    indices.add(new Integer(lbr_pos));
                    indices.add(new Integer(lbl_pos));
                    indices.add(new Integer(ubl_pos));

                    // Right Face
                    indices.add(new Integer(lbr_pos));
                    indices.add(new Integer(ubr_pos));
                    indices.add(new Integer(ufr_pos));
                    indices.add(new Integer(lbr_pos));
                    indices.add(new Integer(ufr_pos));
                    indices.add(new Integer(lfr_pos));

                    // Left Face
                    indices.add(new Integer(lbl_pos));
                    indices.add(new Integer(ufl_pos));
                    indices.add(new Integer(ubl_pos));
                    indices.add(new Integer(lbl_pos));
                    indices.add(new Integer(lfl_pos));
                    indices.add(new Integer(ufl_pos));

                    // Top Face
                    indices.add(new Integer(ufr_pos));
                    indices.add(new Integer(ubr_pos));
                    indices.add(new Integer(ubl_pos));
                    indices.add(new Integer(ufr_pos));
                    indices.add(new Integer(ubl_pos));
                    indices.add(new Integer(ufl_pos));

                    // Bottom Face
                    indices.add(new Integer(lfr_pos));
                    indices.add(new Integer(lbl_pos));
                    indices.add(new Integer(lbr_pos));
                    indices.add(new Integer(lfr_pos));
                    indices.add(new Integer(lfl_pos));
                    indices.add(new Integer(lbl_pos));
                }
            }

            // Remove n - 2 coordinates
            Iterator<Coordinate> itr = lastSlice.iterator();
            while(itr.hasNext()) {
                coords.remove(itr.next());
            }

            totalCoords.addAll(thisSlice);
            lastSlice = thisSlice;
            thisSlice = new ArrayList<Coordinate>();

            if (indices.size() / 3 >= MAX_TRIANGLES_SHAPE) {
System.out.println("Coords: " + totalCoords.size() + " indices: " + indices.size());
                ejectShape(stream, totalCoords, indices, color, transparency);
                coords.clear();
                indices.clear();
                lastSlice.clear();
                totalCoords.clear();
                idx = 0;
            }
        }

        ejectShape(stream, totalCoords, indices, color, transparency);
    }

    /**
     * Create an X3D file from the grid.  This should be an exact
     * repsentation of the grid values.
     *
     * @param stream The stream to write too
     * @param colors Maps materialID's to colors
     */
    public void toX3DDebug(BinaryContentHandler stream, Map<Byte, float[]> colors, Map<Byte, Float> transparency) {
        float[] color = colors.get(Grid.OUTSIDE);
        Float transF = transparency.get(Grid.OUTSIDE);
        float trans = 1;

        if (color != null) {
            if (transF != null) {
                trans = transF;
            }
            outputState(stream, Grid.OUTSIDE, color, trans);
        }

        color = colors.get(Grid.EXTERIOR);
        transF = transparency.get(Grid.EXTERIOR);
        trans = 1;

        if (color != null) {
            if (transF != null) {
                trans = transF;
            }
            outputState(stream, Grid.EXTERIOR, color, trans);
        }

        color = colors.get(Grid.INTERIOR);
        transF = transparency.get(Grid.INTERIOR);
        trans = 1;

        if (color != null) {
            if (transF != null) {
                trans = transF;
            }
            outputState(stream, Grid.INTERIOR, color, trans);
        }

    }

    private void outputState(BinaryContentHandler stream, byte display, float[] color, float transparency) {
        HashMap<Coordinate,Integer> coords = new HashMap<Coordinate,Integer>();
        ArrayList<Integer> indices = new ArrayList<Integer>();
        ArrayList<Coordinate> lastSlice = new ArrayList<Coordinate>();
        ArrayList<Coordinate> thisSlice = new ArrayList<Coordinate>();
        ArrayList<Coordinate> totalCoords = new ArrayList<Coordinate>();

        // TODO: We might want to use a form of run length encoding to
        // reduce the number of boxes

        double x,y,z;
        int idx = 0;

        for(int i=0; i < height; i++) {
            y = i * sheight;

            Slice s = data[i];

            int width = s.getWidth();
            int depth = s.getDepth();

            for(int j=0; j < width; j++) {
                x = j * pixelSize;

                for(int k=0; k < depth; k++) {
                    z = k * pixelSize;

                    byte state = s.getState(j,k);

                    if (state != display)
                        continue;



// TODO: Need to map material

//System.out.println("Live one: coord: " + j + " " + i + " " + k);
//System.out.println("   coord(wc): " + x + " " + y + " " + z);
                    Coordinate ubl_coord = new Coordinate((float)(x - hpixelSize),
                        (float)(y + hsheight),(float)(z - hpixelSize));
                    Integer ubl_pos = coords.get(ubl_coord);
                    if (ubl_pos == null) {
//System.out.println("ubl added: " + idx);
                        ubl_pos = new Integer(idx++);
                        coords.put(ubl_coord, ubl_pos);
                        thisSlice.add(ubl_coord);
                    }

                    Coordinate ubr_coord = new Coordinate((float)(x + hpixelSize),
                        (float)(y + hsheight),(float)(z - hpixelSize));
                    Integer ubr_pos = coords.get(ubr_coord);
                    if (ubr_pos == null) {
//System.out.println("ubr added: " + idx);
                        ubr_pos = new Integer(idx++);
                        coords.put(ubr_coord, ubr_pos);
                        thisSlice.add(ubr_coord);
                    }


                    Coordinate lbl_coord = new Coordinate((float)(x - hpixelSize),
                        (float)(y - hsheight),(float)(z - hpixelSize));
                    Integer lbl_pos = coords.get(lbl_coord);
                    if (lbl_pos == null) {
//System.out.println("lbl added: " + idx);
                        lbl_pos = new Integer(idx++);
                        coords.put(lbl_coord, lbl_pos);
                        thisSlice.add(lbl_coord);
                    }

                    Coordinate lbr_coord = new Coordinate((float)(x + hpixelSize),
                        (float)(y - hsheight),(float)(z - hpixelSize));
                    Integer lbr_pos = coords.get(lbr_coord);
                    if (lbr_pos == null) {
//System.out.println("lbr added: " + idx);
                        lbr_pos = new Integer(idx++);
                        coords.put(lbr_coord, lbr_pos);
                        thisSlice.add(lbr_coord);
                    }

                    Coordinate ufl_coord = new Coordinate((float)(x - hpixelSize),
                        (float)(y + hsheight),(float)(z + hpixelSize));
                    Integer ufl_pos = coords.get(ufl_coord);
                    if (ufl_pos == null) {
//System.out.println("ufl added: " + idx);
                        ufl_pos = new Integer(idx++);
                        coords.put(ufl_coord, ufl_pos);
                        thisSlice.add(ufl_coord);
                    }

                    Coordinate ufr_coord = new Coordinate((float)(x + hpixelSize),
                        (float)(y + hsheight),(float)(z + hpixelSize));
                    Integer ufr_pos = coords.get(ufr_coord);
                    if (ufr_pos == null) {
//System.out.println("ufr added: " + idx);
                        ufr_pos = new Integer(idx++);
                        coords.put(ufr_coord, ufr_pos);
                        thisSlice.add(ufr_coord);
                    }

                    Coordinate lfr_coord = new Coordinate((float)(x + hpixelSize),
                        (float)(y - hsheight),(float)(z + hpixelSize));
                    Integer lfr_pos = coords.get(lfr_coord);
                    if (lfr_pos == null) {
//System.out.println("lfr added: " + idx);
                        lfr_pos = new Integer(idx++);
                        coords.put(lfr_coord, lfr_pos);
                        thisSlice.add(lfr_coord);
                    }

                    Coordinate lfl_coord = new Coordinate((float)(x - hpixelSize),
                        (float)(y - hsheight),(float)(z + hpixelSize));
                    Integer lfl_pos = coords.get(lfl_coord);
                    if (lfl_pos == null) {
//System.out.println("lfl added: " + idx);
                        lfl_pos = new Integer(idx++);
                        coords.put(lfl_coord, lfl_pos);
                        thisSlice.add(lfl_coord);
                    }


                    // Create Box
                    // Front Face
                    indices.add(new Integer(lfr_pos));
                    indices.add(new Integer(ufr_pos));
                    indices.add(new Integer(ufl_pos));
                    indices.add(new Integer(lfr_pos));
                    indices.add(new Integer(ufl_pos));
                    indices.add(new Integer(lfl_pos));

                    // Back Face
                    indices.add(new Integer(lbr_pos));
                    indices.add(new Integer(ubl_pos));
                    indices.add(new Integer(ubr_pos));
                    indices.add(new Integer(lbr_pos));
                    indices.add(new Integer(lbl_pos));
                    indices.add(new Integer(ubl_pos));

                    // Right Face
                    indices.add(new Integer(lbr_pos));
                    indices.add(new Integer(ubr_pos));
                    indices.add(new Integer(ufr_pos));
                    indices.add(new Integer(lbr_pos));
                    indices.add(new Integer(ufr_pos));
                    indices.add(new Integer(lfr_pos));

                    // Left Face
                    indices.add(new Integer(lbl_pos));
                    indices.add(new Integer(ufl_pos));
                    indices.add(new Integer(ubl_pos));
                    indices.add(new Integer(lbl_pos));
                    indices.add(new Integer(lfl_pos));
                    indices.add(new Integer(ufl_pos));

                    // Top Face
                    indices.add(new Integer(ufr_pos));
                    indices.add(new Integer(ubr_pos));
                    indices.add(new Integer(ubl_pos));
                    indices.add(new Integer(ufr_pos));
                    indices.add(new Integer(ubl_pos));
                    indices.add(new Integer(ufl_pos));

                    // Bottom Face
                    indices.add(new Integer(lfr_pos));
                    indices.add(new Integer(lbl_pos));
                    indices.add(new Integer(lbr_pos));
                    indices.add(new Integer(lfr_pos));
                    indices.add(new Integer(lfl_pos));
                    indices.add(new Integer(lbl_pos));
                }
            }

            // Remove n - 2 coordinates
            Iterator<Coordinate> itr = lastSlice.iterator();
            while(itr.hasNext()) {
                coords.remove(itr.next());
            }

            totalCoords.addAll(thisSlice);
            lastSlice = thisSlice;
            thisSlice = new ArrayList<Coordinate>();

            if (indices.size() / 3 >= MAX_TRIANGLES_SHAPE) {
                ejectShape(stream, totalCoords, indices, color, transparency);
                coords.clear();
                indices.clear();
                lastSlice.clear();
                totalCoords.clear();
                idx = 0;
            }
        }

        ejectShape(stream, totalCoords, indices, color, transparency);
    }

    /**
     * Eject a shape into the stream.
     *
     * @param stream The stream to use
     * @param totalCoords The coords to use
     * @param indices The indices to use
     */
    private void ejectShape(BinaryContentHandler stream, ArrayList<Coordinate> totalCoords,
        ArrayList<Integer> indices, float[] color, float transparency) {

        int idx = 0;
        float[] allCoords = new float[totalCoords.size() * 3];
        Iterator<Coordinate> itr = totalCoords.iterator();
        while(itr.hasNext()) {
            Coordinate c = itr.next();
            allCoords[idx++] = c.x;
            allCoords[idx++] = c.y;
            allCoords[idx++] = c.z;
        }

        idx = 0;
        int[] allIndices = new int[(int) (indices.size() * 4 / 3)];
        for(int i=0; i < indices.size(); ) {
            allIndices[idx++] = indices.get(i++);
            allIndices[idx++] = indices.get(i++);
            allIndices[idx++] = indices.get(i++);
            allIndices[idx++] = -1;
        }

        stream.startNode("Shape", null);
        stream.startField("appearance");
        stream.startNode("Appearance", null);
        stream.startField("material");
        stream.startNode("Material",null);
        stream.startField("diffuseColor");
        stream.fieldValue(color,3);
        stream.startField("transparency");
        stream.fieldValue(transparency);
        stream.endNode();  //  Material
        stream.endNode();  //  Appearance
        stream.startField("geometry");
        stream.startNode("IndexedFaceSet", null);
        stream.startField("coord");
        stream.startNode("Coordinate", null);
        stream.startField("point");
        stream.fieldValue(allCoords, allCoords.length);
        stream.endNode();  // Coordinate
        stream.startField("coordIndex");
        stream.fieldValue(allIndices, allIndices.length);
        stream.endNode();  // IndexedFaceSet
        stream.endNode();  // Shape\
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