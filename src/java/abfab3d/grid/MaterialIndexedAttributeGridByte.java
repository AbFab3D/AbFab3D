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

// Internal Imports

/**
 * A wrapper that keeps a mapping of material to voxel coordinates.
 * This allows for fast traversal/removal of material groups.
 *
 * This class trades memory for speed.  Given N voxels and E exterior
 * voxels.  Usually exterior voxels = 1% of total voxels.  This
 * class will use E * (sizeof(pointer) + sizeof(VoxelCoordinate)) == 20
 * bytes extra to index.
 *
 * Insert operations will be slower(~100%).  Removal and traversal operations
 * will be much faster(2E verses N^3).
 *
 * This class can told to optimize for read usage.  This means
 * that find operations will be much faster at the expense of change ops.
 *
 * @author Alan Hudson
 */
public class MaterialIndexedAttributeGridByte extends BaseAttributeGrid {
    /** Starting size of Sets per material */
    private static final int INDEX_SIZE = 10*1024;

    /** The data */
    private HashMap<Long, HashSet<Voxel>> data;

    /** Scratch var */
    private int[] gcoords;

    /**
     * Constructor.
     *
     * @param w The width in world coords
     * @param h The height in world coords
     * @param d The depth in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public MaterialIndexedAttributeGridByte(double w, double h, double d, double pixel, double sheight) {
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
    public MaterialIndexedAttributeGridByte(int w, int h, int d, double pixel, double sheight) {
        super(w,h,d,pixel,sheight);

        data = new HashMap<Long, HashSet<Voxel>>();
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
        Grid ret_val = new MaterialIndexedAttributeGridByte(w,h,d,pixel,sheight);

        return ret_val;
    }

    /**
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public MaterialIndexedAttributeGridByte(MaterialIndexedAttributeGridByte grid) {
        super(grid.getWidth(), grid.getHeight(), grid.getDepth(),
            grid.getVoxelSize(), grid.getSliceHeight());
        this.data = (HashMap<Long, HashSet<Voxel>>)grid.data.clone();
    }

    /**
     * Get a new instance of voxel data.  Returns this grids specific sized voxel data.
     *
     * @return The voxel data
     */
    public VoxelData getVoxelData() {
        return new VoxelDataByte();
    }

    /**
     * Remove all voxels associated with the Material.
     *
     * @param mat The aterialID
     */
    public void removeAttribute(long mat) {
        Long b = new Long(mat);

        data.remove(b);
    }

    //----------------------------------------------------------
    // Grid methods
    //----------------------------------------------------------

    /**
     * Get the data for a voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public void getData(double x, double y, double z, VoxelData vd) {
        // Slow method, must traverse all lists

        Iterator<HashSet<Voxel>> itr = data.values().iterator();

        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        VoxelCoordinate vc = new VoxelCoordinate(s_x,slice,s_z);
        Voxel tv = new Voxel(vc, null);

        while(itr.hasNext()) {
            HashSet<Voxel> set = itr.next();
            Iterator<Voxel> itr2 = set.iterator();

            // TODO: Does contains call make it faster?
            if (set.contains(tv)) {
                while(itr2.hasNext()) {
                    Voxel v = itr2.next();
                    VoxelCoordinate vc2 = v.getCoordinate();

                    if (vc.equals(vc2)) {
                        VoxelData ans = v.getData();
                        vd.setData(ans.getState(), ans.getMaterial());
                        return;
                    }
                }
            }
        }

        // Not found, that's OUTSIDE, material 0
        vd.setData(Grid.OUTSIDE, Grid.NO_MATERIAL);
    }

    /**
     * Get the state of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public void getData(int x, int y, int z, VoxelData vd) {
        // Slow method, must traverse all lists

        Iterator<HashSet<Voxel>> itr = data.values().iterator();

        VoxelCoordinate vc = new VoxelCoordinate(x,y,z);
        Voxel tv = new Voxel(vc, null);

        while(itr.hasNext()) {
            HashSet<Voxel> set = itr.next();
            Iterator<Voxel> itr2 = set.iterator();

            // TODO: Does contains call make it faster?
            if (set.contains(tv)) {
                while(itr2.hasNext()) {
                    Voxel v = itr2.next();
                    VoxelCoordinate vc2 = v.getCoordinate();

                    if (vc.equals(vc2)) {
                        VoxelData ans = v.getData();
                        vd.setData(ans.getState(), ans.getMaterial());
                        return;
                    }
                }
            }
        }

        // Not found, that's OUTSIDE, material 0
        vd.setData(Grid.OUTSIDE, Grid.NO_MATERIAL);
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public byte getState(double x, double y, double z) {
        // Slow method, must traverse all lists

        Iterator<HashSet<Voxel>> itr = data.values().iterator();

        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        VoxelCoordinate vc = new VoxelCoordinate(s_x,slice,s_z);

        while(itr.hasNext()) {
            HashSet<Voxel> set = itr.next();
            Iterator<Voxel> itr2 = set.iterator();

            while(itr2.hasNext()) {
                Voxel v = itr2.next();
                VoxelCoordinate vc2 = v.getCoordinate();

                if (vc.equals(vc2)) {
                    return v.getData().getState();
                }
            }
        }

        // Not found, that's OUTSIDE, material 0
        return EMPTY_VOXEL.getState();
    }

    /**
     * Get the state of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public byte getState(int x, int y, int z) {
        // Slow method, must traverse all lists

        Iterator<HashSet<Voxel>> itr = data.values().iterator();

        VoxelCoordinate vc = new VoxelCoordinate(x,y,z);

        while(itr.hasNext()) {
            HashSet<Voxel> set = itr.next();
            Iterator<Voxel> itr2 = set.iterator();

            while(itr2.hasNext()) {
                Voxel v = itr2.next();
                VoxelCoordinate vc2 = v.getCoordinate();

                if (vc.equals(vc2)) {
                    return v.getData().getState();
                }
            }
        }

        // Not found, that's OUTSIDE, material 0
        return EMPTY_VOXEL.getState();
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public long getAttribute(double x, double y, double z) {
        // Slow method, must traverse all lists

        Iterator<HashSet<Voxel>> itr = data.values().iterator();

        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        VoxelCoordinate vc = new VoxelCoordinate(s_x,slice,s_z);

        while(itr.hasNext()) {
            HashSet<Voxel> set = itr.next();
            Iterator<Voxel> itr2 = set.iterator();

            while(itr2.hasNext()) {
                Voxel v = itr2.next();
                VoxelCoordinate vc2 = v.getCoordinate();

                if (vc.equals(vc2)) {
                    return v.getData().getMaterial();
                }
            }
        }

        // Not found, that's OUTSIDE, material 0
        return EMPTY_VOXEL.getMaterial();
    }

    /**
     * Get the material of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public long getAttribute(int x, int y, int z) {
        // Slow method, must traverse all lists

        Iterator<HashSet<Voxel>> itr = data.values().iterator();

        VoxelCoordinate vc = new VoxelCoordinate(x,y,z);

        while(itr.hasNext()) {
            HashSet<Voxel> set = itr.next();
            Iterator<Voxel> itr2 = set.iterator();

            while(itr2.hasNext()) {
                Voxel v = itr2.next();
                VoxelCoordinate vc2 = v.getCoordinate();

                if (vc.equals(vc2)) {
                    return v.getData().getMaterial();
                }
            }
        }

        // Not found, that's OUTSIDE, material 0
        return EMPTY_VOXEL.getMaterial();
    }

    /**
     * Set the value of a voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 materialID
     * @param material The materialID
     */
    public void setData(double x, double y, double z, byte state, long material) {
        Long b = new Long(material);

        HashSet<Voxel> voxels = data.get(b);
        if (voxels == null) {
            voxels = new HashSet<Voxel>(INDEX_SIZE);
            data.put(b,voxels);
        }

        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);


        VoxelCoordinate vc = new VoxelCoordinate(s_x, slice, s_z);
        VoxelDataByte vd = new VoxelDataByte(state, material);
        Voxel v = new Voxel(vc, vd);

        // Must remove as data may have changed
        voxels.remove(v);
        voxels.add(v);
    }

    /**
     * Set the value of a voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The state
     * @param material The material
     */
    public void setData(int x, int y, int z, byte state, long material) {
        Long b = new Long(material);

        HashSet<Voxel> voxels = data.get(b);
        if (voxels == null) {
            voxels = new HashSet<Voxel>(INDEX_SIZE);
            data.put(b,voxels);
        }

        VoxelCoordinate vc = new VoxelCoordinate(x, y, z);
        VoxelDataByte vd = new VoxelDataByte(state, material);
        Voxel v = new Voxel(vc, vd);

        // Must remove as data may have changed
        voxels.remove(v);
        voxels.add(v);
    }

    /**
     * Set the material value of a voxel.  Leaves the state unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param material The materialID
     */
    public void setAttribute(int x, int y, int z, long material) {
        // TODO: not implemented yet
        throw new IllegalArgumentException("Not Implemented");
    }

    /**
     * Set the state value of a voxel.  Leaves the material unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 materialID
     */
    public void setState(int x, int y, int z, byte state) {
        // TODO: not implemented yet
        throw new IllegalArgumentException("Not Implemented");
    }

    /**
     * Set the state value of a voxel.  Leaves the material unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 materialID
     */
    public void setState(double x, double y, double z, byte state) {
        // TODO: not implemented yet
        throw new IllegalArgumentException("Not Implemented");
    }

    /**
     * Count a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @return The number
     */
    public int findCount(VoxelClasses vc) {
        // Slow method, must traverse all lists
        int ret_val = 0;

        if (vc == VoxelClasses.ALL) {
            return width * height * depth;
        } else if (vc == VoxelClasses.OUTSIDE) {
            int all = width * height * depth - findCount(VoxelClasses.MARKED);

            return all;
        }

        Iterator<HashSet<Voxel>> itr = data.values().iterator();

        byte state;

        while(itr.hasNext()) {
            HashSet<Voxel> set = itr.next();
            Iterator<Voxel> itr2 = set.iterator();

            while(itr2.hasNext()) {
                Voxel v = itr2.next();

                switch(vc) {
                    case MARKED:
                        state = v.getData().getState();
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            ret_val++;
                        }
                        break;
                    case EXTERIOR:
                        state = v.getData().getState();
                        if (state == Grid.EXTERIOR) {
                            ret_val++;
                        }
                        break;
                    case INTERIOR:
                        state = v.getData().getState();
                        if (state == Grid.INTERIOR) {
                            ret_val++;
                        }
                        break;
                }
            }
        }

        return ret_val;
    }

    /**
     * Count a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The class of material to traverse
     * @return The number
     */
    public int findCount(long mat) {
        int ret_val = 0;

        Long b = new Long(mat);

        HashSet<Voxel> voxels = data.get(b);
        if (voxels == null) {
            return 0;
        }

        Iterator<Voxel> itr = voxels.iterator();
        int x,y,z;

        while(itr.hasNext()) {
            Voxel v = itr.next();
            VoxelData vd = v.getData();

            if (vd.getMaterial() == mat && vd.getState() != Grid.OUTSIDE) {
                ret_val++;
            }
        }

        return ret_val;
    }

    /**
     * Traverse a class of voxel and material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttribute(VoxelClasses vc, long mat, ClassAttributeTraverser t) {
        if (vc == VoxelClasses.ALL) {
            VoxelData vd = getVoxelData();

            for(int x=0; x < width; x++) {
                for(int y=0; y < height; y++) {
                    for(int z=0; z < depth; z++) {
                        getData(x,y,z,vd);
                        if (mat == vd.getMaterial()) {
                            t.found(x,y,z,vd);
                        }
                    }
                }
            }

            return;
        }

        Long b = new Long(mat);

        HashSet<Voxel> voxels = data.get(b);

        if (voxels == null) {
            return;
        }

        Iterator<Voxel> itr = voxels.iterator();

        while(itr.hasNext()) {
            Voxel v = itr.next();
            VoxelCoordinate vcoord = v.getCoordinate();
            VoxelData vd = v.getData();

            if (vd.getMaterial() != mat)
                continue;

            byte state;

            int x,y,z;


            switch(vc) {
                case MARKED:
                    state = vd.getState();
                    if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                        x = vcoord.getX();
                        y = vcoord.getY();
                        z = vcoord.getZ();

                        t.found(x,y,z,vd);
                    }
                    break;
                case EXTERIOR:
                    state = vd.getState();
                    if (state == Grid.EXTERIOR) {
                        x = vcoord.getX();
                        y = vcoord.getY();
                        z = vcoord.getZ();

                        t.found(x,y,z,vd);
                    }
                    break;
                case INTERIOR:
                    state = vd.getState();
                    if (state == Grid.INTERIOR) {
                        x = vcoord.getX();
                        y = vcoord.getY();
                        z = vcoord.getZ();

                        t.found(x,y,z,vd);
                    }
                    break;
            }
        }
    }

    /**
     * Traverse a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttribute(long mat, ClassAttributeTraverser t) {
        Long b = new Long(mat);

        HashSet<Voxel> voxels = data.get(b);
        if (voxels == null) {
            return;
        }

        Iterator<Voxel> itr = voxels.iterator();

        while(itr.hasNext()) {
            Voxel v = itr.next();
            VoxelData vd = v.getData();

            if (vd.getMaterial() == mat && vd.getState() != Grid.OUTSIDE) {
                VoxelCoordinate vcoord = v.getCoordinate();

                int x = vcoord.getX();
                int y = vcoord.getY();
                int z = vcoord.getZ();

                t.found(x,y,z,vd);
            }
        }
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void find(VoxelClasses vc, ClassTraverser t) {
        // Slow method, must traverse all lists
        if (vc == VoxelClasses.ALL) {
            for(int x=0; x < width; x++) {
                for(int y=0; y < height; y++) {
                    for(int z=0; z < depth; z++) {
                        byte vd = getState(x,y,z);
                        t.found(x,y,z,vd);
                    }
                }
            }

            return;
        } else if (vc == VoxelClasses.OUTSIDE) {
            for(int x=0; x < width; x++) {
                for(int y=0; y < height; y++) {
                    for(int z=0; z < depth; z++) {
                        byte vd = getState(x,y,z);
                        if (vd == Grid.OUTSIDE) {
                            t.found(x,y,z,vd);
                        }
                    }
                }
            }

            return;
        }


        Iterator<HashSet<Voxel>> itr = data.values().iterator();

        byte state;

        while(itr.hasNext()) {
            HashSet<Voxel> set = itr.next();
            Iterator<Voxel> itr2 = set.iterator();

            while(itr2.hasNext()) {
                Voxel v = itr2.next();
                VoxelCoordinate vcoord = v.getCoordinate();
                int x = vcoord.getX();
                int y = vcoord.getY();
                int z = vcoord.getZ();

                switch(vc) {
                    case ALL:
                        t.found(x,y,z,v.getData().getState());
                        break;
                    case MARKED:
                        state = v.getData().getState();
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            t.found(x,y,z,state);
                        }
                        break;
                    case EXTERIOR:
                        state = v.getData().getState();
                        if (state == Grid.EXTERIOR) {
                            t.found(x,y,z,state);
                        }
                        break;
                    case INTERIOR:
                        state = v.getData().getState();
                        if (state == Grid.INTERIOR) {
                            t.found(x,y,z,state);
                        }
                        break;
                    case OUTSIDE:
                        state = v.getData().getState();
                        if (state == Grid.OUTSIDE) {
                            t.found(x,y,z,state);
                        }
                        break;
                }
            }
        }
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttribute(VoxelClasses vc, ClassAttributeTraverser t) {
        // Slow method, must traverse all lists
        if (vc == VoxelClasses.ALL) {
            VoxelData vd = getVoxelData();

            for(int x=0; x < width; x++) {
                for(int y=0; y < height; y++) {
                    for(int z=0; z < depth; z++) {
                        getData(x,y,z,vd);
                        t.found(x,y,z,vd);
                    }
                }
            }

            return;
        } else if (vc == VoxelClasses.OUTSIDE) {
            VoxelData vd = getVoxelData();
            for(int x=0; x < width; x++) {
                for(int y=0; y < height; y++) {
                    for(int z=0; z < depth; z++) {
                        getData(x,y,z,vd);
                        if (vd.getState() == Grid.OUTSIDE) {
                            t.found(x,y,z,vd);
                        }
                    }
                }
            }

            return;
        }


        Iterator<HashSet<Voxel>> itr = data.values().iterator();

        byte state;

        while(itr.hasNext()) {
            HashSet<Voxel> set = itr.next();
            Iterator<Voxel> itr2 = set.iterator();

            while(itr2.hasNext()) {
                Voxel v = itr2.next();
                VoxelCoordinate vcoord = v.getCoordinate();
                int x = vcoord.getX();
                int y = vcoord.getY();
                int z = vcoord.getZ();

                switch(vc) {
                    case ALL:
                        t.found(x,y,z,v.getData());
                        break;
                    case MARKED:
                        state = v.getData().getState();
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            t.found(x,y,z,v.getData());
                        }
                        break;
                    case EXTERIOR:
                        state = v.getData().getState();
                        if (state == Grid.EXTERIOR) {
                            t.found(x,y,z,v.getData());
                        }
                        break;
                    case INTERIOR:
                        state = v.getData().getState();
                        if (state == Grid.INTERIOR) {
                            t.found(x,y,z,v.getData());
                        }
                        break;
                    case OUTSIDE:
                        state = v.getData().getState();
                        if (state == Grid.OUTSIDE) {
                            t.found(x,y,z,v.getData());
                        }
                        break;
                }
            }
        }
    }

    /**
     * Traverse a class of voxel and material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttributeInterruptible(VoxelClasses vc, long mat, ClassAttributeTraverser t) {
        Long b = new Long(mat);

        HashSet<Voxel> voxels = data.get(b);

        if (voxels == null) {
            return;
        }

        Iterator<Voxel> itr = voxels.iterator();

        rloop: while(itr.hasNext()) {
            Voxel v = itr.next();
            VoxelData vd = v.getData();

            if (vd.getMaterial() != mat)
                continue;

            byte state;

            VoxelCoordinate vcoord = v.getCoordinate();
            int x = vcoord.getX();
            int y = vcoord.getY();
            int z = vcoord.getZ();

            switch(vc) {
                case MARKED:
                    state = vd.getState();
                    if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                        if (!t.foundInterruptible(x,y,z,vd)) {
                            break rloop;
                        }
                    }
                    break;
                case EXTERIOR:
                    state = vd.getState();
                    if (state == Grid.EXTERIOR) {
                        if (!t.foundInterruptible(x,y,z,vd)) {
                            break rloop;
                        }
                    }
                    break;
                case INTERIOR:
                    state = vd.getState();
                    if (state == Grid.INTERIOR) {
                        if (!t.foundInterruptible(x,y,z,vd)) {
                            break rloop;
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Traverse a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttributeInterruptible(long mat, ClassAttributeTraverser t) {
        Long b = new Long(mat);

        HashSet<Voxel> voxels = data.get(b);

        if (voxels == null) {
            return;
        }

        Iterator<Voxel> itr = voxels.iterator();

        rloop: while(itr.hasNext()) {
            Voxel v = itr.next();
            VoxelData vd = v.getData();

/*
            if (vd.getAttribute() != mat)
                continue;
*/

            VoxelCoordinate vcoord = v.getCoordinate();
            int x = vcoord.getX();
            int y = vcoord.getY();
            int z = vcoord.getZ();

            if (!t.foundInterruptible(x,y,z,vd)) {
                break rloop;
            }
        }
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttributeInterruptible(VoxelClasses vc, ClassAttributeTraverser t) {
        // Slow method, must traverse all lists
        int ret_val = 0;

        Iterator<HashSet<Voxel>> itr = data.values().iterator();

        byte state;

        loop: while(itr.hasNext()) {
            HashSet<Voxel> set = itr.next();
            Iterator<Voxel> itr2 = set.iterator();

            while(itr2.hasNext()) {
                Voxel v = itr2.next();
                VoxelCoordinate vcoord = v.getCoordinate();
                int x = vcoord.getX();
                int y = vcoord.getY();
                int z = vcoord.getZ();

                switch(vc) {
                    case ALL:
                        t.found(x,y,z,v.getData());
                        break;
                    case MARKED:
                        state = v.getData().getState();
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            if (!t.foundInterruptible(x,y,z,v.getData())) {
                                break loop;
                            }
                        }
                        break;
                    case EXTERIOR:
                        state = v.getData().getState();
                        if (state == Grid.EXTERIOR) {
                            if (!t.foundInterruptible(x,y,z,v.getData())) {
                                break loop;
                            }
                        }
                        break;
                    case INTERIOR:
                        state = v.getData().getState();
                        if (state == Grid.INTERIOR) {
                            if (!t.foundInterruptible(x,y,z,v.getData())) {
                                break loop;
                            }
                        }
                        break;
                    case OUTSIDE:
                        state = v.getData().getState();
                        if (state == Grid.OUTSIDE) {
                            if (!t.foundInterruptible(x,y,z,v.getData())) {
                                break loop;
                            }
                        }
                        break;
                }
            }
        }
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findInterruptible(VoxelClasses vc, ClassTraverser t) {
        // Slow method, must traverse all lists
        int ret_val = 0;

        Iterator<HashSet<Voxel>> itr = data.values().iterator();

        byte state;

        loop: while(itr.hasNext()) {
            HashSet<Voxel> set = itr.next();
            Iterator<Voxel> itr2 = set.iterator();

            while(itr2.hasNext()) {
                Voxel v = itr2.next();
                VoxelCoordinate vcoord = v.getCoordinate();
                int x = vcoord.getX();
                int y = vcoord.getY();
                int z = vcoord.getZ();

                switch(vc) {
                    case ALL:
                        t.found(x,y,z,v.getData().getState());
                        break;
                    case MARKED:
                        state = v.getData().getState();
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            if (!t.foundInterruptible(x,y,z,state)) {
                                break loop;
                            }
                        }
                        break;
                    case EXTERIOR:
                        state = v.getData().getState();
                        if (state == Grid.EXTERIOR) {
                            if (!t.foundInterruptible(x,y,z,state)) {
                                break loop;
                            }
                        }
                        break;
                    case INTERIOR:
                        state = v.getData().getState();
                        if (state == Grid.INTERIOR) {
                            if (!t.foundInterruptible(x,y,z,state)) {
                                break loop;
                            }
                        }
                        break;
                    case OUTSIDE:
                        state = v.getData().getState();
                        if (state == Grid.OUTSIDE) {
                            if (!t.foundInterruptible(x,y,z,state)) {
                                break loop;
                            }
                        }
                        break;
                }
            }
        }
    }

    /**
     * Clone this object.
     */
    public Object clone() {
        MaterialIndexedAttributeGridByte grid = new MaterialIndexedAttributeGridByte(this);

        return grid;
    }
}