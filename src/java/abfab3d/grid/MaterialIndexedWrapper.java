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
public class MaterialIndexedWrapper implements AttributeGridWrapper {
    /** Starting size of Sets per material */
    private static final int INDEX_SIZE = 1024;

    /** The wrapper grid */
    private AttributeGrid grid;

    /** The index */
    private HashMap<Integer, HashSet<VoxelCoordinate>> index;

    /** The optimized index */
    private HashMap<Integer, Voxel[]> optIndex;

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

        index = new HashMap<Integer, HashSet<VoxelCoordinate>>();
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
        if (wrap.index != null)
            this.index = (HashMap<Integer, HashSet<VoxelCoordinate>>) wrap.index.clone();
        if (wrap.optIndex != null)
            this.optIndex = (HashMap<Integer, Voxel[]>) wrap.optIndex.clone();
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

        index = new HashMap<Integer, HashSet<VoxelCoordinate>>();
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
     * Reassign a group of materials to a new materialID
     *
     * @param materials The new list of materials
     * @param matID The new materialID
     */
    public void reassignAttribute(int[] materials, int matID) {

        int len = materials.length;

        for(int i=0; i < len; i++) {
        	if (materials[i] == matID)
        		continue;
        	
            Integer orig = new Integer(materials[i]);

            HashSet<VoxelCoordinate> coords = index.get(orig);
            if (coords == null) {
                // Nothing to do
                continue;
            }

            HashSet<VoxelCoordinate> target = index.get(matID);

            if (target == null) {
                target = new HashSet<VoxelCoordinate>(INDEX_SIZE);
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
    public void removeAttribute(int mat) {
        Integer b = new Integer(mat);

        HashSet<VoxelCoordinate> coords = index.get(b);
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
     * Get the data for a voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public VoxelData getData(double x, double y, double z) {
        return grid.getData(x,y,z);
    }

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
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public void getData(double x, double y, double z,VoxelData vd) {
        grid.getData(x,y,z,vd);
    }

    /**
     * Get the state of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public VoxelData getData(int x, int y, int z) {
        return grid.getData(x,y,z);
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public byte getState(double x, double y, double z) {
        return grid.getState(x,y,z);
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
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public int getAttribute(double x, double y, double z) {
        return grid.getAttribute(x, y, z);
    }

    /**
     * Get the material of the voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public int getAttribute(int x, int y, int z) {
        return grid.getAttribute(x, y, z);
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
    public void setData(double x, double y, double z, byte state, int material) {
        Integer b = new Integer(material);

        HashSet<VoxelCoordinate> coords = index.get(b);
        if (coords == null) {
            coords = new HashSet<VoxelCoordinate>(INDEX_SIZE);
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
    public void setData(int x, int y, int z, byte state, int material) {
        Integer b = new Integer(material);

        HashSet<VoxelCoordinate> coords = index.get(b);
        if (coords == null) {
            coords = new HashSet<VoxelCoordinate>(INDEX_SIZE);
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
    public void setAttribute(int x, int y, int z, int material) {
        Integer b = new Integer(material);

        HashSet<VoxelCoordinate> coords = index.get(b);
        if (coords == null) {
            coords = new HashSet<VoxelCoordinate>(INDEX_SIZE);
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
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 materialID
     */
    public void setState(double x, double y, double z, byte state) {
        grid.setState(x,y,z,state);
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
    public int findCount(int mat) {
        int ret_val = 0;

        Integer b = new Integer(mat);

        HashSet<VoxelCoordinate> coords = index.get(b);
        if (coords == null) {
            return 0;
        }

        Iterator<VoxelCoordinate> itr = coords.iterator();
        int x,y,z;

        while(itr.hasNext()) {
            VoxelCoordinate vc = itr.next();
            x = vc.getX();
            y = vc.getY();
            z = vc.getZ();

            VoxelData vd = grid.getData(x,y,z);

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
    public void findAttribute(VoxelClasses vc, int mat, ClassAttributeTraverser t) {
        Integer b = new Integer(mat);

        HashSet<VoxelCoordinate> coords = index.get(b);

        if (coords == null) {
            return;
        }

        Iterator<VoxelCoordinate> itr = coords.iterator();
        int x,y,z;

        while(itr.hasNext()) {
            VoxelCoordinate coord = itr.next();
            x = coord.getX();
            y = coord.getY();
            z = coord.getZ();

            VoxelData vd = grid.getData(x,y,z);

            if (vd.getMaterial() != mat)
                continue;

            byte state;

            switch(vc) {
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
    public void findAttribute(int mat, ClassAttributeTraverser t) {
        Integer b = new Integer(mat);

        HashSet<VoxelCoordinate> coords = index.get(b);

        if (coords == null) {
            return;
        }

        Iterator<VoxelCoordinate> itr = coords.iterator();
        int x,y,z;

        while(itr.hasNext()) {
            VoxelCoordinate vc = itr.next();
            x = vc.getX();
            y = vc.getY();
            z = vc.getZ();

            VoxelData vd = grid.getData(x,y,z);

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
    public void findAttributeInterruptible(VoxelClasses vc, int mat, ClassAttributeTraverser t) {
        Integer b = new Integer(mat);

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
                    case MARKED:
                        state = vd.getState();
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            if (!t.foundInterruptible(x,y,z,vd))
                                break rloop;
                        }
                        break;
                    case EXTERIOR:
                        state = vd.getState();
                        if (state == Grid.EXTERIOR) {
                            if (!t.foundInterruptible(x,y,z,vd))
                                break rloop;
                        }
                        break;
                    case INTERIOR:
                        state = vd.getState();
                        if (state == Grid.INTERIOR) {
                            if (!t.foundInterruptible(x,y,z,vd))
                                break rloop;
                        }
                        break;
                }
            }

            return;
        }

        HashSet<VoxelCoordinate> coords = index.get(b);

        if (coords == null) {
            return;
        }

        Iterator<VoxelCoordinate> itr = coords.iterator();
        int x,y,z;

        loop:
        while(itr.hasNext()) {
            VoxelCoordinate coord = itr.next();
            x = coord.getX();
            y = coord.getY();
            z = coord.getZ();

            VoxelData vd = grid.getData(x,y,z);

            if (vd.getMaterial() != mat)
                continue;

            byte state;

            switch(vc) {
                case MARKED:
                    state = vd.getState();
                    if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                        if (!t.foundInterruptible(x,y,z,vd))
                            break loop;
                    }
                    break;
                case EXTERIOR:
                    state = vd.getState();
                    if (state == Grid.EXTERIOR) {
                        if (!t.foundInterruptible(x,y,z,vd))
                            break loop;
                    }
                    break;
                case INTERIOR:
                    state = vd.getState();
                    if (state == Grid.INTERIOR) {
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
    public void findAttributeInterruptible(int mat, ClassAttributeTraverser t) {
        Integer b = new Integer(mat);

// TODO: add optRead enhancements

        HashSet<VoxelCoordinate> coords = index.get(b);

        if (coords == null) {
            return;
        }

        Iterator<VoxelCoordinate> itr = coords.iterator();
        int x,y,z;

        while(itr.hasNext()) {
            VoxelCoordinate vc = itr.next();
            x = vc.getX();
            y = vc.getY();
            z = vc.getZ();

            VoxelData vd = grid.getData(x,y,z);

            if (vd.getState() != Grid.OUTSIDE) {
                if (!t.foundInterruptible(x,y,z,vd))
                    break;
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
    private void deoptimizeIndex(int mat) {
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
    private void optimizeIndex(int mat) {
        long startTime = System.currentTimeMillis();

        if (optIndex == null) {
            optIndex = new HashMap<Integer,Voxel[]>();
        }

        Integer b = new Integer(mat);

        if (optIndex.get(b) != null) {
            // Index is in place so reuse,
            return;
        }

        HashSet<VoxelCoordinate> coords = index.get(b);

        if (coords == null) {
            optIndex.put(b, new Voxel[0]);
            return;
        }

        Voxel[] voxels = new Voxel[coords.size()];

        Iterator<VoxelCoordinate> itr = coords.iterator();
        int idx = 0;

        while(itr.hasNext()) {
            VoxelCoordinate coord = itr.next();
            VoxelData vd = grid.getData(coord.getX(), coord.getY(), coord.getZ());

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
            findInterruptible(VoxelClasses.EXTERIOR, mat, ef);
        }
System.out.println("Speed unopt: " + (System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();

        optRead = true;
        for(int i=0; i < TIMES; i++) {
            findInterruptible(VoxelClasses.EXTERIOR, mat,ef);
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
    public boolean insideGrid(double wx, double wy, double wz) {
        return grid.insideGrid(wx,wy,wz);
    }

}

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
