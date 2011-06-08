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
 * A grid backed by arrays.
 *
 * Likely better performance for memory access that is not slice aligned.
 *
 * Uses the X3D coordinate system.  Y-up.  Grid is located
 * on positive right side octant.
 *
 * @author Alan Hudson
 */
public class ArrayGrid implements Grid {
    /** The maximum coords to put in a shape */
    private static final int MAX_TRIANGLES_SHAPE = 300000;

    protected int width;
    protected int height;
    protected int depth;
    protected double pixelSize;
    protected double hpixelSize;
    protected double sheight;
    protected double hsheight;
    protected byte[] data;
    protected int sliceSize;

    /**
     * Constructor.
     *
     * @param w The width in world coords
     * @param h The height in world coords
     * @param d The depth in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public ArrayGrid(double w, double h, double d, double pixel, double sheight) {
        this((int) Math.ceil(w / pixel), (int) Math.ceil(h / sheight),
           (int) Math.ceil(d / pixel), pixel, sheight);
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
    public ArrayGrid(int w, int h, int d, double pixel, double sheight) {
        width = w;
        height = h;
        depth = d;
        this.pixelSize = pixel;
        this.hpixelSize = pixelSize / 2.0;
        this.sheight = sheight;
        this.hsheight = sheight / 2.0;

        data = new byte[height * width * depth];

        sliceSize = w * d;
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
        int idx = y * sliceSize + x * depth + z;

        byte state = (byte) ((data[idx] & 0xFF) >> 6);
        byte mat = (byte) (0x3F & data[idx]);

        VoxelData vd = new VoxelData(state, mat);

        return vd;
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

        int idx = slice * sliceSize + s_x * depth + s_z;

        byte state = (byte) ((data[idx] & 0xFF) >> 6);
        byte mat = (byte) (0x3F & data[idx]);

        return new VoxelData(state, mat);
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

        int idx = slice * sliceSize + s_x * depth + s_z;

        byte state = (byte) ((data[idx] & 0xFF) >> 6);

        return state;
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
        int idx = y * sliceSize + x * depth + z;

        byte state = (byte) ((data[idx] & 0xFF) >> 6);

        return state;
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

        int idx = slice * sliceSize + s_x * depth + s_z;

        byte mat = (byte) (0x3F & data[idx]);

        return mat;
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
        int idx = y * sliceSize + x * depth + z;

        byte mat = (byte) (0x3F & data[idx]);

        return mat;
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
    public void find(VoxelClasses vc, ClassTraverser t) {
        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    VoxelData vd = getData(x,y,z);

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
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        int idx = slice * sliceSize + s_x * depth + s_z;

        data[idx] = (byte) (0xFF & (state << 6 | material));
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
        int idx = y * sliceSize + x * depth + z;

        data[idx] = (byte) (0xFF & (state << 6 | material));
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
        HashMap<WorldCoordinate,Integer> coords = new HashMap<WorldCoordinate,Integer>();
        ArrayList<Integer> indices = new ArrayList<Integer>();
        ArrayList<WorldCoordinate> lastSlice = new ArrayList<WorldCoordinate>();
        ArrayList<WorldCoordinate> thisSlice = new ArrayList<WorldCoordinate>();
        ArrayList<WorldCoordinate> totalCoords = new ArrayList<WorldCoordinate>();

        // TODO: We might want to use a form of run length encoding to
        // reduce the number of boxes

        double x,y,z;
        int idx = 0;
        float[] color = new float[] {0.8f,0.8f,0.8f};
        float transparency = 0;
        int saved = 0;

        for(int i=0; i < height; i++) {
            y = i * sheight;

            byte cstate;

            for(int j=0; j < width; j++) {
                x = j * pixelSize;

                for(int k=0; k < depth; k++) {
                    z = k * pixelSize;

                    byte state = getState(i,j,k);

                    if (state == Grid.OUTSIDE)
                        continue;



// TODO: Need to map material

//System.out.println("Live one: coord: " + j + " " + i + " " + k);
//System.out.println("   coord(wc): " + x + " " + y + " " + z);
                    WorldCoordinate ubl_coord = new WorldCoordinate((float)(x - hpixelSize),
                        (float)(y + hsheight),(float)(z - hpixelSize));
                    Integer ubl_pos = coords.get(ubl_coord);
                    if (ubl_pos == null) {
//System.out.println("ubl added: " + idx);
                        ubl_pos = new Integer(idx++);
                        coords.put(ubl_coord, ubl_pos);
                        thisSlice.add(ubl_coord);
                    }

                    WorldCoordinate ubr_coord = new WorldCoordinate((float)(x + hpixelSize),
                        (float)(y + hsheight),(float)(z - hpixelSize));
                    Integer ubr_pos = coords.get(ubr_coord);
                    if (ubr_pos == null) {
//System.out.println("ubr added: " + idx);
                        ubr_pos = new Integer(idx++);
                        coords.put(ubr_coord, ubr_pos);
                        thisSlice.add(ubr_coord);
                    }


                    WorldCoordinate lbl_coord = new WorldCoordinate((float)(x - hpixelSize),
                        (float)(y - hsheight),(float)(z - hpixelSize));
                    Integer lbl_pos = coords.get(lbl_coord);
                    if (lbl_pos == null) {
//System.out.println("lbl added: " + idx);
                        lbl_pos = new Integer(idx++);
                        coords.put(lbl_coord, lbl_pos);
                        thisSlice.add(lbl_coord);
                    }

                    WorldCoordinate lbr_coord = new WorldCoordinate((float)(x + hpixelSize),
                        (float)(y - hsheight),(float)(z - hpixelSize));
                    Integer lbr_pos = coords.get(lbr_coord);
                    if (lbr_pos == null) {
//System.out.println("lbr added: " + idx);
                        lbr_pos = new Integer(idx++);
                        coords.put(lbr_coord, lbr_pos);
                        thisSlice.add(lbr_coord);
                    }

                    WorldCoordinate ufl_coord = new WorldCoordinate((float)(x - hpixelSize),
                        (float)(y + hsheight),(float)(z + hpixelSize));
                    Integer ufl_pos = coords.get(ufl_coord);
                    if (ufl_pos == null) {
//System.out.println("ufl added: " + idx);
                        ufl_pos = new Integer(idx++);
                        coords.put(ufl_coord, ufl_pos);
                        thisSlice.add(ufl_coord);
                    }

                    WorldCoordinate ufr_coord = new WorldCoordinate((float)(x + hpixelSize),
                        (float)(y + hsheight),(float)(z + hpixelSize));
                    Integer ufr_pos = coords.get(ufr_coord);
                    if (ufr_pos == null) {
//System.out.println("ufr added: " + idx);
                        ufr_pos = new Integer(idx++);
                        coords.put(ufr_coord, ufr_pos);
                        thisSlice.add(ufr_coord);
                    }

                    WorldCoordinate lfr_coord = new WorldCoordinate((float)(x + hpixelSize),
                        (float)(y - hsheight),(float)(z + hpixelSize));
                    Integer lfr_pos = coords.get(lfr_coord);
                    if (lfr_pos == null) {
//System.out.println("lfr added: " + idx);
                        lfr_pos = new Integer(idx++);
                        coords.put(lfr_coord, lfr_pos);
                        thisSlice.add(lfr_coord);
                    }

                    WorldCoordinate lfl_coord = new WorldCoordinate((float)(x - hpixelSize),
                        (float)(y - hsheight),(float)(z + hpixelSize));
                    Integer lfl_pos = coords.get(lfl_coord);
                    if (lfl_pos == null) {
//System.out.println("lfl added: " + idx);
                        lfl_pos = new Integer(idx++);
                        coords.put(lfl_coord, lfl_pos);
                        thisSlice.add(lfl_coord);
                    }


                    // Create Box

                    boolean displayFront = true;

                    if (k < depth - 1) {
                        cstate = getState(i,j,k+1);

                        if (cstate == state) {
                            displayFront = false;
                            saved++;
                        }
                    }

                    if (displayFront) {
                        // Front Face
                        indices.add(new Integer(lfr_pos));
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(ufl_pos));
                        indices.add(new Integer(lfr_pos));
                        indices.add(new Integer(ufl_pos));
                        indices.add(new Integer(lfl_pos));
                    }

                    boolean displayBack = true;
                    if (k > 0) {
                        cstate = getState(i,j,k-1);

                        if (cstate == state) {
                            displayBack = false;
                            saved++;
                        }
                    }

                    if (displayBack) {
                        // Back Face
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(ubl_pos));
                        indices.add(new Integer(ubr_pos));
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(lbl_pos));
                        indices.add(new Integer(ubl_pos));
                    }

                    boolean displayRight = true;

                    if (j < width - 1) {
                        cstate = getState(i,j+1,k);

                        if (cstate == state) {
                            displayRight = false;
                            saved++;
                        }
                    }

                    if (displayRight) {
                        // Right Face
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(ubr_pos));
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(lfr_pos));
                    }

                    boolean displayLeft = true;

                    if (j > 0) {
                        cstate = getState(i,j-1,k);

                        if (cstate == state) {
                            displayLeft = false;
                            saved++;
                        }
                    }

                    if (displayLeft) {
                        // Left Face
                        indices.add(new Integer(lbl_pos));
                        indices.add(new Integer(ufl_pos));
                        indices.add(new Integer(ubl_pos));
                        indices.add(new Integer(lbl_pos));
                        indices.add(new Integer(lfl_pos));
                        indices.add(new Integer(ufl_pos));
                    }

                    boolean displayTop = true;

                    if (i < height - 1) {
                        cstate = getState(i+1,j,k);

                        if (cstate == state) {
                            displayTop = false;
                            saved++;
                        }
                    }

                    if (displayTop) {
                        // Top Face
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(ubr_pos));
                        indices.add(new Integer(ubl_pos));
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(ubl_pos));
                        indices.add(new Integer(ufl_pos));
                    }

                    boolean displayBottom = true;

                    if (i > 0) {
                        cstate = getState(i-1,j,k);

                        if (cstate == state) {
                            displayBottom = false;
                            saved++;
                        }
                    }

                    if (displayBottom) {
                        // Bottom Face
                        indices.add(new Integer(lfr_pos));
                        indices.add(new Integer(lbl_pos));
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(lfr_pos));
                        indices.add(new Integer(lfl_pos));
                        indices.add(new Integer(lbl_pos));
                    }
                }
            }

            // Remove n - 2 coordinates
            Iterator<WorldCoordinate> itr = lastSlice.iterator();
            while(itr.hasNext()) {
                coords.remove(itr.next());
            }

            totalCoords.addAll(thisSlice);
            lastSlice = thisSlice;
            thisSlice = new ArrayList<WorldCoordinate>();

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

System.out.println("Saved: " + saved);
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
        HashMap<WorldCoordinate,Integer> coords = new HashMap<WorldCoordinate,Integer>();
        ArrayList<Integer> indices = new ArrayList<Integer>();
        ArrayList<WorldCoordinate> lastSlice = new ArrayList<WorldCoordinate>();
        ArrayList<WorldCoordinate> thisSlice = new ArrayList<WorldCoordinate>();
        ArrayList<WorldCoordinate> totalCoords = new ArrayList<WorldCoordinate>();

        // TODO: We might want to use a form of run length encoding to
        // reduce the number of boxes

        double x,y,z;
        int idx = 0;
        int saved = 0;

        for(int i=0; i < height; i++) {
            y = i * sheight;

            byte cstate;

            for(int j=0; j < width; j++) {
                x = j * pixelSize;

                for(int k=0; k < depth; k++) {
                    z = k * pixelSize;

                    byte state = getState(i,j,k);

                    if (state != display)
                        continue;



// TODO: Need to map material

//System.out.println("Live one: coord: " + j + " " + i + " " + k);
//System.out.println("   coord(wc): " + x + " " + y + " " + z);
                    WorldCoordinate ubl_coord = new WorldCoordinate((float)(x - hpixelSize),
                        (float)(y + hsheight),(float)(z - hpixelSize));
                    Integer ubl_pos = coords.get(ubl_coord);
                    if (ubl_pos == null) {
//System.out.println("ubl added: " + idx);
                        ubl_pos = new Integer(idx++);
                        coords.put(ubl_coord, ubl_pos);
                        thisSlice.add(ubl_coord);
                    }

                    WorldCoordinate ubr_coord = new WorldCoordinate((float)(x + hpixelSize),
                        (float)(y + hsheight),(float)(z - hpixelSize));
                    Integer ubr_pos = coords.get(ubr_coord);
                    if (ubr_pos == null) {
//System.out.println("ubr added: " + idx);
                        ubr_pos = new Integer(idx++);
                        coords.put(ubr_coord, ubr_pos);
                        thisSlice.add(ubr_coord);
                    }


                    WorldCoordinate lbl_coord = new WorldCoordinate((float)(x - hpixelSize),
                        (float)(y - hsheight),(float)(z - hpixelSize));
                    Integer lbl_pos = coords.get(lbl_coord);
                    if (lbl_pos == null) {
//System.out.println("lbl added: " + idx);
                        lbl_pos = new Integer(idx++);
                        coords.put(lbl_coord, lbl_pos);
                        thisSlice.add(lbl_coord);
                    }

                    WorldCoordinate lbr_coord = new WorldCoordinate((float)(x + hpixelSize),
                        (float)(y - hsheight),(float)(z - hpixelSize));
                    Integer lbr_pos = coords.get(lbr_coord);
                    if (lbr_pos == null) {
//System.out.println("lbr added: " + idx);
                        lbr_pos = new Integer(idx++);
                        coords.put(lbr_coord, lbr_pos);
                        thisSlice.add(lbr_coord);
                    }

                    WorldCoordinate ufl_coord = new WorldCoordinate((float)(x - hpixelSize),
                        (float)(y + hsheight),(float)(z + hpixelSize));
                    Integer ufl_pos = coords.get(ufl_coord);
                    if (ufl_pos == null) {
//System.out.println("ufl added: " + idx);
                        ufl_pos = new Integer(idx++);
                        coords.put(ufl_coord, ufl_pos);
                        thisSlice.add(ufl_coord);
                    }

                    WorldCoordinate ufr_coord = new WorldCoordinate((float)(x + hpixelSize),
                        (float)(y + hsheight),(float)(z + hpixelSize));
                    Integer ufr_pos = coords.get(ufr_coord);
                    if (ufr_pos == null) {
//System.out.println("ufr added: " + idx);
                        ufr_pos = new Integer(idx++);
                        coords.put(ufr_coord, ufr_pos);
                        thisSlice.add(ufr_coord);
                    }

                    WorldCoordinate lfr_coord = new WorldCoordinate((float)(x + hpixelSize),
                        (float)(y - hsheight),(float)(z + hpixelSize));
                    Integer lfr_pos = coords.get(lfr_coord);
                    if (lfr_pos == null) {
//System.out.println("lfr added: " + idx);
                        lfr_pos = new Integer(idx++);
                        coords.put(lfr_coord, lfr_pos);
                        thisSlice.add(lfr_coord);
                    }

                    WorldCoordinate lfl_coord = new WorldCoordinate((float)(x - hpixelSize),
                        (float)(y - hsheight),(float)(z + hpixelSize));
                    Integer lfl_pos = coords.get(lfl_coord);
                    if (lfl_pos == null) {
//System.out.println("lfl added: " + idx);
                        lfl_pos = new Integer(idx++);
                        coords.put(lfl_coord, lfl_pos);
                        thisSlice.add(lfl_coord);
                    }


                    // Create Box
                    boolean displayFront = true;

                    if (k < depth - 1) {
                        cstate = getState(i,j,k+1);

                        if (cstate == state) {
                            displayFront = false;
                            saved++;
                        }
                    }

                    if (displayFront) {
                        // Front Face
                        indices.add(new Integer(lfr_pos));
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(ufl_pos));
                        indices.add(new Integer(lfr_pos));
                        indices.add(new Integer(ufl_pos));
                        indices.add(new Integer(lfl_pos));
                    }

                    boolean displayBack = true;
                    if (k > 0) {
                        cstate = getState(i,j,k-1);

                        if (cstate == state) {
                            displayBack = false;
                            saved++;
                        }
                    }

                    if (displayBack) {
                        // Back Face
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(ubl_pos));
                        indices.add(new Integer(ubr_pos));
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(lbl_pos));
                        indices.add(new Integer(ubl_pos));
                    }

                    boolean displayRight = true;

                    if (j < width - 1) {
                        cstate = getState(i,j+1,k);

                        if (cstate == state) {
                            displayRight = false;
                            saved++;
                        }
                    }

                    if (displayRight) {
                        // Right Face
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(ubr_pos));
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(lfr_pos));
                    }

                    boolean displayLeft = true;

                    if (j > 0) {
                        cstate = getState(i,j-1,k);

                        if (cstate == state) {
                            displayLeft = false;
                            saved++;
                        }
                    }

                    if (displayLeft) {
                        // Left Face
                        indices.add(new Integer(lbl_pos));
                        indices.add(new Integer(ufl_pos));
                        indices.add(new Integer(ubl_pos));
                        indices.add(new Integer(lbl_pos));
                        indices.add(new Integer(lfl_pos));
                        indices.add(new Integer(ufl_pos));
                    }

                    boolean displayTop = true;

                    if (i < height - 1) {
                        cstate = getState(i+1,j,k);

                        if (cstate == state) {
                            displayTop = false;
                            saved++;
                        }
                    }

                    if (displayTop) {
                        // Top Face
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(ubr_pos));
                        indices.add(new Integer(ubl_pos));
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(ubl_pos));
                        indices.add(new Integer(ufl_pos));
                    }

                    boolean displayBottom = true;

                    if (i > 0) {
                        cstate = getState(i-1,j,k);

                        if (cstate == state) {
                            displayBottom = false;
                            saved++;
                        }
                    }

                    if (displayBottom) {
                        // Bottom Face
                        indices.add(new Integer(lfr_pos));
                        indices.add(new Integer(lbl_pos));
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(lfr_pos));
                        indices.add(new Integer(lfl_pos));
                        indices.add(new Integer(lbl_pos));
                    }
                }
            }

            // Remove n - 2 coordinates
            Iterator<WorldCoordinate> itr = lastSlice.iterator();
            while(itr.hasNext()) {
                coords.remove(itr.next());
            }

            totalCoords.addAll(thisSlice);
            lastSlice = thisSlice;
            thisSlice = new ArrayList<WorldCoordinate>();

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
    private void ejectShape(BinaryContentHandler stream, ArrayList<WorldCoordinate> totalCoords,
        ArrayList<Integer> indices, float[] color, float transparency) {

        int idx = 0;
        float[] allCoords = new float[totalCoords.size() * 3];
        Iterator<WorldCoordinate> itr = totalCoords.iterator();
        while(itr.hasNext()) {
            WorldCoordinate c = itr.next();
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
        stream.startNode("WorldCoordinate", null);
        stream.startField("point");
        stream.fieldValue(allCoords, allCoords.length);
        stream.endNode();  // WorldCoordinate
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
    public String toStringSlice(int y) {
        StringBuilder sb = new StringBuilder();

        for(int i=0; i < depth; i++) {
            for(int j=0; j < width; j++) {
                int idx = y * sliceSize + i * width + j;

                sb.append(data[idx]);
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
}

