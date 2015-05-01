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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
 * @author Vladimir Bulatov
 */
public class MaterialIndexedWrapper implements AttributeGridWrapper {
    private static final boolean CONCURRENT = true;

    /** Starting size of Sets per material */
    private static final int INDEX_SIZE = 1024;

    /** The wrapper grid */
    private AttributeGrid grid;

    /** The index */
    private Map<Long, Set<VoxelCoordinate>> index;

    /** The optimized index */
    private HashMap<Long, Voxel[]> optIndex;

    /** Scratch var */
    private int[] gcoords;

    /** Optimize for read usage */
    private boolean optRead;

    /**
     * Constructor.
     *
     * @param grid The grid to wrap
     */
    public MaterialIndexedWrapper(AttributeGrid grid) {
        this.grid = grid;

        if (CONCURRENT) {
            index = new ConcurrentHashMap<Long, Set<VoxelCoordinate>>();
        } else {
            index = new HashMap<Long, Set<VoxelCoordinate>>();
        }
        gcoords = new int[3];
        optRead = false;
    }

    /**
     * Copy Constructor.
     *
     * @param wrap The wrapper to copy
     */
    public MaterialIndexedWrapper(MaterialIndexedWrapper wrap) {
        if (wrap == null) {
            setGrid(wrap);
            return;
        }

        if (wrap.grid != null)
            this.grid = (AttributeGrid) wrap.grid.clone();
        if (wrap.index != null) {
            if (CONCURRENT) {
                this.index = new ConcurrentHashMap<Long, Set<VoxelCoordinate>>(wrap.index);
            } else {
                index = new HashMap<Long, Set<VoxelCoordinate>>(wrap.index);
            }
        }
        if (wrap.optIndex != null)
            this.optIndex = (HashMap<Long, Voxel[]>) wrap.optIndex.clone();
        gcoords = new int[3];
        this.optRead = wrap.optRead;
    }

    /**
     * Constructor.
     *
     * @param grid The grid to wrap
     * @param optRead Optimize for read usage
     */
    public MaterialIndexedWrapper(AttributeGrid grid, boolean optRead) {
        this.grid = grid;
        this.optRead = optRead;

        if (CONCURRENT) {
            index = new ConcurrentHashMap<Long, Set<VoxelCoordinate>>();
        } else {
            index = new HashMap<Long, Set<VoxelCoordinate>>();
        }
        gcoords = new int[3];
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
        return grid.createEmpty(w,h,d,pixel,sheight);
    }

    /**
     * Get a new instance of voxel data.  Returns this grids specific sized voxel data.
     *
     * @return The voxel data
     */
    public VoxelData getVoxelData() {
        return grid.getVoxelData();
    }

    /**
     * Reassign a group of materials to a new materialID
     *
     * @param materials The new list of materials
     * @param matID The new materialID
     */
    public void reassignAttribute(long[] materials, long matID) {

        int len = materials.length;

        for(int i=0; i < len; i++) {
            if (materials[i] == matID)
                continue;

            Long orig = new Long(materials[i]);

            Set<VoxelCoordinate> coords = index.get(orig);
            if (coords == null) {
                // Nothing to do
                continue;
            }

            Set<VoxelCoordinate> target = index.get(matID);

            if (target == null) {
                if (CONCURRENT) {
                    target = Collections.newSetFromMap(new ConcurrentHashMap<VoxelCoordinate, Boolean>(INDEX_SIZE));
                } else {
                    target = new HashSet<VoxelCoordinate>(INDEX_SIZE);
                }

                index.put(matID, target);
            }

            Iterator<VoxelCoordinate> itr = coords.iterator();
            while(itr.hasNext()) {
                VoxelCoordinate vc = itr.next();
                grid.setAttribute(vc.getX(), vc.getY(), vc.getZ(), matID);
                target.add(vc);
            }

            index.remove(orig);

            if (optIndex != null)
                optIndex.remove(orig);
        }
    }

    /**
     * Remove all voxels associated with the Material.
     *
     * @param mat The materialID
     */
    public void removeAttribute(long mat) {
        Long b = new Long(mat);

        Set<VoxelCoordinate> coords = index.get(b);
        if (coords == null) {
            // Nothing to do
            return;
        }

        Iterator<VoxelCoordinate> itr = coords.iterator();
        while(itr.hasNext()) {
            VoxelCoordinate vc = itr.next();
            grid.setData(vc.getX(), vc.getY(), vc.getZ(), Grid.OUTSIDE, 0);
        }

        index.remove(b);

        if (optIndex != null)
            optIndex.remove(b);
    }

    //----------------------------------------------------------
    // GridWrapper methods
    //----------------------------------------------------------

    /**
     * Sets the underlying grid to use.
     *
     * @param grid The grid or null to clear.
     */
    public void setGrid(AttributeGrid grid) {
        // TODO: This needs to recreate indexes
        this.grid = grid;
    }

    //----------------------------------------------------------
    // Grid methods
    //----------------------------------------------------------

    /**
     * Get the state of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public void getData(int x, int y, int z,VoxelData vd) {
        grid.getData(x,y,z,vd);
    }

    /**
     * Get the data for a voxel
     *  @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public void getDataWorld(double x, double y, double z, VoxelData vd) {
        grid.getDataWorld(x, y, z, vd);
    }

    /**
     * Get the state of the voxel
     *  @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public byte getStateWorld(double x, double y, double z) {
        return grid.getStateWorld(x, y, z);
    }

    /**
     * Get the state of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public byte getState(int x, int y, int z) {
        return grid.getState(x,y,z);
    }

    /**
     * Get the state of the voxel
     *  @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public long getAttributeWorld(double x, double y, double z) {
        return grid.getAttributeWorld(x, y, z);
    }
    public void setAttributeWorld(double x, double y, double z, long attribute) {
        grid.setAttributeWorld(x, y, z, attribute);
    }

    /**
     * Get the material of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public long getAttribute(int x, int y, int z) {
        return grid.getAttribute(x, y, z);
    }

    /**
     * Set the value of a voxel.
     *  @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 materialID
     * @param material The materialID
     */
    public void setDataWorld(double x, double y, double z, byte state, long material) {
        Long b = new Long(material);

        Set<VoxelCoordinate> coords = index.get(b);
        if (coords == null) {
            if (CONCURRENT) {
                coords = Collections.newSetFromMap(new ConcurrentHashMap<VoxelCoordinate, Boolean>(INDEX_SIZE));
            } else {
                coords = new HashSet<VoxelCoordinate>(INDEX_SIZE);
            }
            index.put(b, coords);
        }

        // TODO: this makes this method not thread safe.
        // Make synchronized?  Use thread local.  Just allocate each time?

        grid.getGridCoords(x,y,z,gcoords);

        VoxelCoordinate vc = new VoxelCoordinate(gcoords[0], gcoords[1], gcoords[2]);
        coords.add(vc);

        grid.setData(gcoords[0],gcoords[1],gcoords[2],state,material);

        optIndex = null;
    }

    /**
     * Set the value of a voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param material The value.  0 = nothing. > 0 materialID
     */
    public void setData(int x, int y, int z, byte state, long material) {
        // TODO: I think this method does not correctly remove the entry from the old coords.

        Long b = new Long(material);

        Set<VoxelCoordinate> coords = index.get(b);
        if (coords == null) {
            if (CONCURRENT) {
                coords = Collections.newSetFromMap(new ConcurrentHashMap<VoxelCoordinate, Boolean>(INDEX_SIZE));
            } else {
                coords = new HashSet<VoxelCoordinate>(INDEX_SIZE);
            }
            index.put(b, coords);
        }

        VoxelCoordinate vc = new VoxelCoordinate(x,y,z);
        coords.add(vc);

        grid.setData(x,y,z,state,material);

        optIndex = null;
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
        // TODO: I think this method does not correctly remove the entry from the old coords.

        Long b = new Long(material);

        Set<VoxelCoordinate> coords = index.get(b);
        if (coords == null) {
            if (CONCURRENT) {
                coords = Collections.newSetFromMap(new ConcurrentHashMap<VoxelCoordinate, Boolean>(INDEX_SIZE));
            } else {
                coords = new HashSet<VoxelCoordinate>(INDEX_SIZE);
            }
            index.put(b, coords);
        }

        VoxelCoordinate vc = new VoxelCoordinate(x,y,z);
        coords.add(vc);

        grid.setAttribute(x, y, z, material);

        optIndex = null;
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
        grid.setState(x,y,z,state);
    }

    /**
     * Set the state value of a voxel.  Leaves the material unchanged.
     *  @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 materialID
     */
    public void setStateWorld(double x, double y, double z, byte state) {
        grid.setStateWorld(x, y, z, state);
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
        grid.getGridCoords(x,y,z,coords);
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
        grid.getWorldCoords(x,y,z,coords);
    }

    /**
     * Get the grid bounds in world coordinates.
     *
     * @param min The min coordinate
     * @param max The max coordinate
     */
    public void getGridBounds(double[] min, double[] max) {
        grid.getGridBounds(min,max);
    }

    /**
     * Get the grid bounds in world coordinates.
     *  @param bounds array {xmin, xmax, ymin, ymax, zmin, zmax}
     */
    public void getGridBounds(double[] bounds){

        grid.getGridBounds(bounds);

    }

    public Bounds getGridBounds(){

        return grid.getGridBounds();

    }

    /**
     * Set the grid bounds in world coordinates.
     *  @param bounds array {xmin, xmax, ymin, ymax, zmin, zmax}
     */
    public void setGridBounds(double[] bounds){

        grid.setGridBounds(bounds);

    }

    public void setGridBounds(Bounds bounds){

        grid.setGridBounds(bounds);

    }


    /**
     * Count a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @return The number
     */
    public int findCount(VoxelClasses vc) {
        return grid.findCount(vc);
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

        Set<VoxelCoordinate> coords = index.get(b);
        if (coords == null) {
            return 0;
        }

        Iterator<VoxelCoordinate> itr = coords.iterator();
        int x,y,z;

        VoxelData vd = grid.getVoxelData();

        while(itr.hasNext()) {
            VoxelCoordinate vc = itr.next();
            x = vc.getX();
            y = vc.getY();
            z = vc.getZ();

            grid.getData(x,y,z,vd);

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
        Long b = new Long(mat);

        Set<VoxelCoordinate> coords = index.get(b);

        if (coords == null) {
            return;
        }

        Iterator<VoxelCoordinate> itr = coords.iterator();
        int x,y,z;

        VoxelData vd = grid.getVoxelData();

        while(itr.hasNext()) {
            VoxelCoordinate coord = itr.next();
            x = coord.getX();
            y = coord.getY();
            z = coord.getZ();

            grid.getData(x,y,z,vd);

            if (vd.getMaterial() != mat)
                continue;

            byte state;

            switch(vc) {
                case INSIDE:
                    state = vd.getState();
                    if (state == Grid.INSIDE) {
                        t.found(x,y,z,vd);
                    }
                    break;
            }
        }
    }

    /**
     * Traverse a class of voxels types over given rectangle in xy plane.
     * May be much faster then full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     * @param xmin - minimal x - coordinate of voxels
     * @param xmax - maximal x - coordinate of voxels
     * @param ymin - minimal y - coordinate of voxels
     * @param ymax - maximal y - coordinate of voxels
     */
    public void findAttribute(VoxelClasses vc, ClassAttributeTraverser t, int xmin, int xmax, int ymin, int ymax) {
        grid.findAttribute(vc,t,xmin,xmax,ymin,ymax);
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

        Set<VoxelCoordinate> coords = index.get(b);

        if (coords == null) {
            return;
        }

        Iterator<VoxelCoordinate> itr = coords.iterator();
        int x,y,z;

        VoxelData vd = grid.getVoxelData();

        while(itr.hasNext()) {
            VoxelCoordinate vc = itr.next();
            x = vc.getX();
            y = vc.getY();
            z = vc.getZ();

            grid.getData(x,y,z,vd);

            if (vd.getMaterial() == mat && vd.getState() != Grid.OUTSIDE) {
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
        grid.find(vc, t);
    }

    /**
     * Traverse a class of voxels types over given rectangle in xy plane.
     * May be much faster then full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     * @param xmin - minimal x - coordinate of voxels
     * @param xmax - maximal x - coordinate of voxels
     * @param ymin - minimal y - coordinate of voxels
     * @param ymax - maximal y - coordinate of voxels
     */
    public void find(VoxelClasses vc, ClassTraverser t, int xmin, int xmax, int ymin, int ymax){

        grid.find(vc, t, xmin, xmax, ymin, ymax);

    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttribute(VoxelClasses vc, ClassAttributeTraverser t) {
        grid.findAttribute(vc, t);
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

        if (optRead) {
            if (optIndex == null) {
                // TODO: in theory we could rebuild during the traversal and then
                // keep last processed position to pickup the rebuild.
                optimizeIndex(b);
            }

            Voxel[] voxels = optIndex.get(b);

            if (voxels == null) {
                optimizeIndex(b);
                voxels = optIndex.get(b);
            }

            int len = voxels.length;

//System.out.println("find: " + b + " ext voxels: " + len);


            rloop: for(int i=0; i < len; i++) {
                Voxel voxel = voxels[i];
                VoxelCoordinate coord = voxel.getCoordinate();
                VoxelData vd = voxel.getData();

                int x = coord.getX();
                int y = coord.getY();
                int z = coord.getZ();

                if (vd.getMaterial() != mat)
                    continue;

                byte state;

                switch(vc) {
                    case INSIDE:
                        state = vd.getState();
                        if (state == Grid.INSIDE) {
                            if (!t.foundInterruptible(x,y,z,vd))
                                break rloop;
                        }
                        break;
                }
            }

            return;
        }

        Set<VoxelCoordinate> coords = index.get(b);

        if (coords == null) {
            return;
        }

        Iterator<VoxelCoordinate> itr = coords.iterator();
        int x,y,z;

        VoxelData vd = grid.getVoxelData();

        loop:
        while(itr.hasNext()) {
            VoxelCoordinate coord = itr.next();
            x = coord.getX();
            y = coord.getY();
            z = coord.getZ();

            grid.getData(x,y,z,vd);

            if (vd.getMaterial() != mat)
                continue;

            byte state;

            switch(vc) {
                case INSIDE:
                    state = vd.getState();
                    if (state == Grid.INSIDE) {
                        if (!t.foundInterruptible(x,y,z,vd))
                            break loop;
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

// TODO: add optRead enhancements

        Set<VoxelCoordinate> coords = index.get(b);

        if (coords == null) {
            return;
        }

        Iterator<VoxelCoordinate> itr = coords.iterator();
        int x,y,z;

        VoxelData vd = grid.getVoxelData();
        while(itr.hasNext()) {
            VoxelCoordinate vc = itr.next();
            x = vc.getX();
            y = vc.getY();
            z = vc.getZ();

            grid.getData(x,y,z,vd);

            if (vd.getState() != Grid.OUTSIDE) {
                if (!t.foundInterruptible(x,y,z,vd))
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
    public void findAttributeInterruptibleSampled(long mat, int skip,ClassAttributeTraverser t) {
        Long b = new Long(mat);

// TODO: add optRead enhancements

        Set<VoxelCoordinate> coords = index.get(b);

        if (coords == null) {
            return;
        }


        int factor = 250;
        // Never allow skip to be > 1/10 of the # coordinates
        if (skip > 1 && skip > coords.size() / factor) {
            skip = coords.size() / factor;

            if (skip < 1) skip = 1;
        }

        Iterator<VoxelCoordinate> itr = coords.iterator();
        int x,y,z;

        VoxelData vd = grid.getVoxelData();
        long cnt = 0;
        while(itr.hasNext()) {
            VoxelCoordinate vc = itr.next();

            cnt++;

            if (cnt % skip == 0) {
                x = vc.getX();
                y = vc.getY();
                z = vc.getZ();

                grid.getData(x, y, z, vd);

                if (vd.getState() != Grid.OUTSIDE) {
                    if (!t.foundInterruptible(x, y, z, vd))
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
        grid.findInterruptible(vc, t);
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttributeInterruptible(VoxelClasses vc, ClassAttributeTraverser t) {
        grid.findAttributeInterruptible(vc, t);
    }

    /**
     * Get the number of height cells.
     *
     * @return the val
     */
    public int getHeight() {
        return grid.getHeight();
    }

    /**
     * Get the number of width cells.
     *
     * @return the val
     */
    public int getWidth() {
        return grid.getWidth();
    }

    /**
     * Get the number of depth cells.
     *
     * @return the val
     */
    public int getDepth() {
        return grid.getDepth();
    }

    /**
     * Get the voxel size in xz.
     *
     * @return The value
     */
    public double getVoxelSize() {
        return grid.getVoxelSize();
    }

    /**
     * Get the slice height.
     *
     * @return The value
     */
    public double getSliceHeight() {
        return grid.getSliceHeight();
    }

    /**
     * Print out a slice of data.
     */
    public String toStringSlice(int s) {
        return grid.toStringSlice(s);
    }

    /**
     * Print out all slices.
     */
    public String toStringAll() {
        return grid.toStringAll();
    }

    /**
     * Return the optimized form to the insert friendly form.
     *
     * @param mat The material
     */
    private void deoptimizeIndex(long mat) {
        throw new UnsupportedOperationException("deoptimizeIndex not implemented");

        // TODO:  to save memory we should be able to go back and forth from
        // forms instead of creating two.

        // TODO:  or maybe we should just create a MaterialIndexedGrid directly.
    }

    /**
     * Optimize the indexes for traversal.
     *
     * @mat The material to optimize
     */
    private void optimizeIndex(long mat) {
        if (optIndex == null) {
            optIndex = new HashMap<Long,Voxel[]>();
        }

        Long b = new Long(mat);

        if (optIndex.get(b) != null) {
            // Index is in place so reuse,
            return;
        }

        Set<VoxelCoordinate> coords = index.get(b);

        if (coords == null) {
            optIndex.put(b, new Voxel[0]);
            return;
        }

        Voxel[] voxels = new Voxel[coords.size()];

        Iterator<VoxelCoordinate> itr = coords.iterator();
        int idx = 0;

        VoxelData vd = grid.getVoxelData();
        while(itr.hasNext()) {
            VoxelCoordinate coord = itr.next();
            grid.getData(coord.getX(), coord.getY(), coord.getZ(),vd);

            voxels[idx++] = new Voxel(coord, vd);
        }

        optIndex.put(b, voxels);


//System.out.println("Speed Optimize Index: " + mat + " time: " + (System.currentTimeMillis() - startTime));
/*
        int TIMES = 26;
        startTime = System.currentTimeMillis();

        optRead = false;
        EmptyFound ef = new EmptyFound();
        for(int i=0; i < TIMES; i++) {
            findInterruptible(VoxelClasses.INSIDE, mat, ef);
        }
System.out.println("Speed unopt: " + (System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();

        optRead = true;
        for(int i=0; i < TIMES; i++) {
            findInterruptible(VoxelClasses.INSIDE, mat,ef);
        }
System.out.println("Speed opt: " + (System.currentTimeMillis() - startTime));
*/
    }

    /**
     * Clone this object.
     */
    public Object clone() {
        MaterialIndexedWrapper new_wrapper = new MaterialIndexedWrapper(this);

        return new_wrapper;
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
        return grid.insideGrid(x,y,z);
    }

    /**
     * Determine if a voxel coordinate is inside the grid space.
     *
     * @param wx The x world coordinate
     * @param wy The y world coordinate
     * @param wz The z world coordinate
     * @return True if the coordinate is inside the grid space
     */
    public boolean insideGridWorld(double wx, double wy, double wz) {
        return grid.insideGridWorld(wx, wy, wz);
    }

    /**
       assign to the grid a description of a voxel attributes
       @param description The attirbute description 
       @override 
    */
    public void setAttributeDesc(AttributeDesc description){
        grid.setAttributeDesc(description);
    }

    /**
       @return voxel attribute description assigned to the grid
       @override 
    */
    public AttributeDesc getAttributeDesc(){
        return grid.getAttributeDesc(); 
    }

} // MaterialIndexerWrapper 

class EmptyFound implements ClassAttributeTraverser {
    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public void found(int x, int y, int z, VoxelData vd) {
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     *
     * @return True to continue, false stops the traversal.
     */
    public boolean foundInterruptible(int x, int y, int z, VoxelData vd) {
        return true;
    }

}
