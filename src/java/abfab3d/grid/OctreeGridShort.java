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

/**
 * A grid backed by an Octree
 *
 * Much smaller memory footprint then Array* versions.  Linear access speed
 * is likely slower.  Iterators should be faster for sparse grids.
 *
 * Uses the X3D coordinate system.  Y-up.  Grid is located
 * on positive right side octant.
 *
 * @author Alan Hudson
 */
public class OctreeGridShort extends BaseGrid {
    protected OctreeCellInternalShort root;

    /**
     * Constructor.
     *
     * @param w The width in world coords
     * @param h The height in world coords
     * @param d The depth in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public OctreeGridShort(double w, double h, double d, double pixel, double sheight) {
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
    public OctreeGridShort(int w, int h, int d, double pixel, double sheight) {
        super(w,h,d,pixel,sheight);

        if (pixel != sheight)
            throw new IllegalArgumentException("Octree must be have equals voxel sizes");

        int size = w;
        if(h > size)
            size = h;
        if(d > size)
            size = d;

        if (size % 2 != 0)
            size++;

        int max_level = 0;
        int n = size;
        while(n > 1) {
            max_level++;
            n = n / 2;
        }

        if (Math.pow(2, max_level) < size) {
            max_level++;
        }

        size = (int) Math.pow(2, max_level);
        w = size;
        h = size;
        d = size;

        max_level++;  // Account for terminal leafs

        root = new OctreeCellInternalShort(1, max_level, 0,0,0,size, Grid.OUTSIDE, 0);
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
        Grid ret_val = new OctreeGridShort(w,h,d,pixel,sheight);

        return ret_val;
    }

    /**
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public OctreeGridShort(OctreeGridShort grid) {
        super(grid.getWidth(), grid.getHeight(), grid.getDepth(),
            grid.getVoxelSize(), grid.getSliceHeight());

        this.root = (OctreeCellInternalShort) grid.root.clone();
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
        return root.getData(x,y,z);
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

        return root.getData(s_x,slice,s_z);
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

        VoxelData vd = root.getData(s_x,slice,s_z);
        return vd.getState();
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
        VoxelData vd = root.getData(x,y,z);

        return vd.getState();
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param The voxel material
     */
    public int getMaterial(double x, double y, double z) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        VoxelData vd = root.getData(s_x,slice,s_z);
        return vd.getMaterial();
    }

    /**
     * Get the material of the voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param The voxel material
     */
    public int getMaterial(int x, int y, int z) {
        VoxelData vd = root.getData(x,y,z);

        return vd.getMaterial();
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
    public void setData(double x, double y, double z, byte state, int material) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        root.setData(null, s_x, slice, s_z, state,material);
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
    public void setData(int x, int y, int z, byte state, int material) {
//System.out.println("sd int: " + x);
        root.setData(null, x, y, z, state,material);
    }

    /**
     * Set the material value of a voxel.  Leaves the state unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param material The materialID
     */
    public void setMaterial(int x, int y, int z, int material) {
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
     * @param material The materialID
     */
    public void setState(int x, int y, int z, byte state) {
        // TODO: not implemented yet
        throw new IllegalArgumentException("Not Implemented");
    }

    /**
     * Clone the object.
     */
    public Object clone() {
        OctreeGridShort ret_val = new OctreeGridShort(this);

        return ret_val;
    }

    /**
     * Get the count of cell nodes in this structure.  This can be either
     * internal or leaf nodes.
     *
     * @return The count of nodes
     */
    public int getCellCount() {
        // traverse the graph and count all children
        return root.getCellCount();
    }

    /**
     * Print the contents of the tree.
     */
    public void printTree() {
        System.out.println("Printing tree: " + hashCode() + " root: " + root.hashCode());
        root.printTree();
    }

    /**
     * Traverse a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findInterruptible(int mat, ClassTraverser t) {
        ArrayList<OctreeCellInternalShort> list = new ArrayList();
        ArrayList<OctreeCellInternalShort> add_list = new ArrayList();

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalShort> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalShort cell = itr.next();

                if (!findInterruptible(cell, mat, t))
                    return;

                OctreeCellInternalShort[] children = cell.children;

                if (children != null) {
                    int len = children.length;

                    for(int i=0; i < len; i++) {
                        if (children[i] != null)
                            add_list.add(children[i]);
                    }
                }
            }

            list.clear();

            itr = add_list.iterator();
            while(itr.hasNext()) {
                list.add(itr.next());
            }

            add_list.clear();
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
        if (vc == VoxelClasses.ALL || vc == VoxelClasses.OUTSIDE) {
            // I can't see a reason to optimize this
            super.find(vc, t);
            return;
        }

        ArrayList<OctreeCellInternalShort> list = new ArrayList();
        ArrayList<OctreeCellInternalShort> add_list = new ArrayList();

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalShort> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalShort cell = itr.next();

                find(cell, vc, t);

                OctreeCellInternalShort[] children = cell.children;

                if (children != null) {
                    int len = children.length;

                    for(int i=0; i < len; i++) {
                        if (children[i] != null)
                            add_list.add(children[i]);
                    }
                }
            }

            list.clear();

            itr = add_list.iterator();
            while(itr.hasNext()) {
                list.add(itr.next());
            }

            add_list.clear();
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
        if (vc == VoxelClasses.ALL || vc == VoxelClasses.OUTSIDE) {
            // I can't see a reason to optimize this
            super.findInterruptible(vc, t);
            return;
        }

        ArrayList<OctreeCellInternalShort> list = new ArrayList();
        ArrayList<OctreeCellInternalShort> add_list = new ArrayList();

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalShort> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalShort cell = itr.next();

                if (!findInterruptible(cell, vc, t))
                    return;

                OctreeCellInternalShort[] children = cell.children;

                if (children != null) {
                    int len = children.length;

                    for(int i=0; i < len; i++) {
                        if (children[i] != null)
                            add_list.add(children[i]);
                    }
                }
            }

            list.clear();

            itr = add_list.iterator();
            while(itr.hasNext()) {
                list.add(itr.next());
            }

            add_list.clear();
        }
    }

    private void find(OctreeCellInternalShort cell, VoxelClasses vc, ClassTraverser t) {
//System.out.println("find: " + cell);
        if (cell.allState.getState() == cell.MIXED) {
            int len = cell.children.length;

            for(int i=0; i < len; i++) {
                if (cell.children[i] == null) {
                    // TODO: I think this is right
                    switch(vc) {
                        case ALL:
                            t.found(cell.vcx,cell.vcy,cell.vcz,BaseGrid.EMPTY_VOXEL);
                            break;
                        case OUTSIDE:
                            t.found(cell.vcx,cell.vcy,cell.vcz,BaseGrid.EMPTY_VOXEL);
                            break;
                    }
                }
            }
        } else {
            byte state;

            if (cell.level == cell.maxLevel) {
//System.out.println(" TERM.  State: " + cell.allState.getState() + " pos: " + cell.vcx + " " + cell.vcy + " " + cell.vcz);
                switch(vc) {
                    case ALL:
                        t.found(cell.vcx,cell.vcy,cell.vcz,cell.allState);
                        break;
                    case MARKED:
                        state = cell.allState.getState();
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            t.found(cell.vcx,cell.vcy,cell.vcz,cell.allState);
                        }
                        break;
                    case EXTERIOR:
                        state = cell.allState.getState();
                        if (state == Grid.EXTERIOR) {
                            t.found(cell.vcx,cell.vcy,cell.vcz,cell.allState);
                        }
                       break;
                    case INTERIOR:
                        state = cell.allState.getState();
                        if (state == Grid.INTERIOR) {
                            t.found(cell.vcx,cell.vcy,cell.vcz,cell.allState);
                        }
                        break;
                    case OUTSIDE:
                        state = cell.allState.getState();
                        if (state == Grid.OUTSIDE) {
                            t.found(cell.vcx,cell.vcy,cell.vcz,cell.allState);
                        }
                        break;
                }
            } else {
//System.out.println(" ALL: " + cell.allState.getState() + " cell.size: " + cell.size + " level: " + cell.level + " max_level: " + cell.maxLevel);
                // TODO: Not sure why this works might be dodgy
                if (cell.level > 2) {
                    switch(vc) {
                        case ALL:
                            for(int i=0; i < cell.size; i++) {
                                for(int j=0; j < cell.size; j++) {
                                    for(int k=0; k < cell.size; k++) {
                                        t.found(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState);
                                    }
                                }
                            }
                            break;
                        case MARKED:
                            state = cell.allState.getState();
                            if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                for(int i=0; i < cell.size; i++) {
                                    for(int j=0; j < cell.size; j++) {
                                        for(int k=0; k < cell.size; k++) {
                                            t.found(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState);
                                        }
                                    }
                                }
                            }
                            break;
                        case EXTERIOR:
                            state = cell.allState.getState();
                            if (state == Grid.EXTERIOR) {
                                for(int i=0; i < cell.size; i++) {
                                    for(int j=0; j < cell.size; j++) {
                                        for(int k=0; k < cell.size; k++) {
                                            t.found(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState);
                                        }
                                    }
                                }
                            }
                            break;
                        case INTERIOR:
                            state = cell.allState.getState();
                            if (state == Grid.INTERIOR) {
                                for(int i=0; i < cell.size; i++) {
                                    for(int j=0; j < cell.size; j++) {
                                        for(int k=0; k < cell.size; k++) {
                                            t.found(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState);
                                        }
                                    }
                                }
                            }
                            break;
                        case OUTSIDE:
                            state = cell.allState.getState();
                            if (state == Grid.OUTSIDE) {
                                for(int i=0; i < cell.size; i++) {
                                    for(int j=0; j < cell.size; j++) {
                                        for(int k=0; k < cell.size; k++) {
                                            t.found(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState);
                                        }
                                    }
                                }
                            }
                            break;
                    }
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
    public void find(VoxelClasses vc, int mat, ClassTraverser t) {
        if (vc == VoxelClasses.ALL || vc == VoxelClasses.OUTSIDE) {
            // I can't see a reason to optimize this
            super.find(vc,mat, t);
            return;
        }

        ArrayList<OctreeCellInternalShort> list = new ArrayList();
        ArrayList<OctreeCellInternalShort> add_list = new ArrayList();

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalShort> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalShort cell = itr.next();

                find(cell, vc, mat, t);

                OctreeCellInternalShort[] children = cell.children;

                if (children != null) {
                    int len = children.length;

                    for(int i=0; i < len; i++) {
                        if (children[i] != null)
                            add_list.add(children[i]);
                    }
                }
            }

            list.clear();

            itr = add_list.iterator();
            while(itr.hasNext()) {
                list.add(itr.next());
            }

            add_list.clear();
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
    public void findInterruptible(VoxelClasses vc, int mat, ClassTraverser t) {
        if (vc == VoxelClasses.ALL || vc == VoxelClasses.OUTSIDE) {
            // I can't see a reason to optimize this
            super.findInterruptible(vc, mat, t);
            return;
        }

        ArrayList<OctreeCellInternalShort> list = new ArrayList();
        ArrayList<OctreeCellInternalShort> add_list = new ArrayList();

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalShort> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalShort cell = itr.next();

                if (!findInterruptible(cell, vc, mat, t))
                    return;

                OctreeCellInternalShort[] children = cell.children;

                if (children != null) {
                    int len = children.length;

                    for(int i=0; i < len; i++) {
                        if (children[i] != null)
                            add_list.add(children[i]);
                    }
                }
            }

            list.clear();

            itr = add_list.iterator();
            while(itr.hasNext()) {
                list.add(itr.next());
            }

            add_list.clear();
        }
    }

    private boolean findInterruptible(OctreeCellInternalShort cell, VoxelClasses vc, ClassTraverser t) {
//System.out.println("find: " + cell);
        if (cell.allState.getState() == cell.MIXED) {
            int len = cell.children.length;

            for(int i=0; i < len; i++) {
                if (cell.children[i] == null) {
                    // TODO: I think this is right
                    switch(vc) {
                        case ALL:
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,BaseGrid.EMPTY_VOXEL))
                                return false;
                            break;
                        case OUTSIDE:
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,BaseGrid.EMPTY_VOXEL))
                                return false;
                            break;
                    }
                }
            }
        } else {
            byte state;

            if (cell.level == cell.maxLevel) {
//System.out.println(" TERM.  State: " + cell.allState.getState());
                switch(vc) {
                    case ALL:
                        if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,cell.allState))
                            return false;
                        break;
                    case MARKED:
                        state = cell.allState.getState();
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,cell.allState))
                                return false;
                        }
                        break;
                    case EXTERIOR:
                        state = cell.allState.getState();
                        if (state == Grid.EXTERIOR) {
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,cell.allState))
                                return false;
                        }
                        break;
                    case INTERIOR:
                        state = cell.allState.getState();
                        if (state == Grid.INTERIOR) {
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,cell.allState))
                                return false;
                        }
                        break;
                    case OUTSIDE:
                        state = cell.allState.getState();
                        if (state == Grid.OUTSIDE) {
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,cell.allState))
                                return false;
                        }
                        break;
                }
            } else {
//System.out.println(" ALL: " + cell.allState.getState() + " cell.size: " + cell.size + " level: " + cell.level + " max_level: " + cell.maxLevel);
                // TODO: Not sure why this works might be dodgy
                if (cell.level > 2) {
                    switch(vc) {
                        case ALL:
                            for(int i=0; i < cell.size; i++) {
                                for(int j=0; j < cell.size; j++) {
                                    for(int k=0; k < cell.size; k++) {
                                        if (!t.foundInterruptible(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState))
                                            return false;
                                    }
                                }
                            }
                            break;
                        case MARKED:
                            state = cell.allState.getState();
                            if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                for(int i=0; i < cell.size; i++) {
                                    for(int j=0; j < cell.size; j++) {
                                        for(int k=0; k < cell.size; k++) {
                                            if (!t.foundInterruptible(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState))
                                                return false;
                                        }
                                    }
                                }
                            }
                            break;
                        case EXTERIOR:
                            state = cell.allState.getState();
                            if (state == Grid.EXTERIOR) {
                                for(int i=0; i < cell.size; i++) {
                                    for(int j=0; j < cell.size; j++) {
                                        for(int k=0; k < cell.size; k++) {
                                            if (!t.foundInterruptible(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState))
                                                return false;
                                        }
                                    }
                                }
                            }
                            break;
                        case INTERIOR:
                            state = cell.allState.getState();
                            if (state == Grid.INTERIOR) {
                                for(int i=0; i < cell.size; i++) {
                                    for(int j=0; j < cell.size; j++) {
                                        for(int k=0; k < cell.size; k++) {
                                            if (!t.foundInterruptible(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState))
                                                return false;
                                        }
                                    }
                                }
                            }
                            break;
                        case OUTSIDE:
                            state = cell.allState.getState();
                            if (state == Grid.OUTSIDE) {
                                for(int i=0; i < cell.size; i++) {
                                    for(int j=0; j < cell.size; j++) {
                                        for(int k=0; k < cell.size; k++) {
                                            if (!t.foundInterruptible(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState))
                                                return false;
                                        }
                                    }
                                }
                            }
                            break;
                    }
                }
            }
        }

        return true;
    }


    /**
     * Traverse a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void find(int mat, ClassTraverser t) {
        ArrayList<OctreeCellInternalShort> list = new ArrayList();
        ArrayList<OctreeCellInternalShort> add_list = new ArrayList();

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalShort> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalShort cell = itr.next();

                find(cell, mat, t);

                OctreeCellInternalShort[] children = cell.children;

                if (children != null) {
                    int len = children.length;

                    for(int i=0; i < len; i++) {
                        if (children[i] != null)
                            add_list.add(children[i]);
                    }
                }
            }

            list.clear();

            itr = add_list.iterator();
            while(itr.hasNext()) {
                list.add(itr.next());
            }

            add_list.clear();
        }
    }

    private void find(OctreeCellInternalShort cell, int mat, ClassTraverser t) {
//System.out.println("find: " + cell);
        if (cell.allState.getState() == cell.MIXED) {
            int len = cell.children.length;

            for(int i=0; i < len; i++) {
                if (cell.children[i] == null) {
                    // OUTSIDE = 0 materialID
                    if (mat == 0)
                        t.found(cell.vcx,cell.vcy,cell.vcz,BaseGrid.EMPTY_VOXEL);
                }
            }
        } else {
            byte state;

            if (cell.level == cell.maxLevel) {
                if (cell.allState.getMaterial() == mat) {
                    t.found(cell.vcx,cell.vcy,cell.vcz,cell.allState);
                }
            } else {
//System.out.println(" ALL: " + cell.allState.getState() + " cell.size: " + cell.size + " level: " + cell.level + " max_level: " + cell.maxLevel);
                // TODO: Not sure why this works might be dodgy
                if (cell.level > 2) {
                    if (cell.allState.getMaterial() == mat) {
                        for(int i=0; i < cell.size; i++) {
                            for(int j=0; j < cell.size; j++) {
                                for(int k=0; k < cell.size; k++) {
                                    t.found(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean findInterruptible(OctreeCellInternalShort cell, int mat, ClassTraverser t) {
//System.out.println("find: " + cell);
        if (cell.allState.getState() == cell.MIXED) {
            int len = cell.children.length;

            for(int i=0; i < len; i++) {
                if (cell.children[i] == null) {
                    // OUTSIDE = 0 materialID
                    if (mat == 0)
                        if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,BaseGrid.EMPTY_VOXEL))
                            return false;
                }
            }
        } else {
            byte state;

            if (cell.level == cell.maxLevel) {
                if (cell.allState.getMaterial() == mat) {
                    if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,cell.allState))
                        return false;
                }
            } else {
//System.out.println(" ALL: " + cell.allState.getState() + " cell.size: " + cell.size + " level: " + cell.level + " max_level: " + cell.maxLevel);
                // TODO: Not sure why this works might be dodgy
                if (cell.level > 2) {
                    if (cell.allState.getMaterial() == mat) {
                        for(int i=0; i < cell.size; i++) {
                            for(int j=0; j < cell.size; j++) {
                                for(int k=0; k < cell.size; k++) {
                                    if (!t.foundInterruptible(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState))
                                        return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private void find(OctreeCellInternalShort cell, VoxelClasses vc, int mat, ClassTraverser t) {
//System.out.println("find: " + cell);
        if (cell.allState.getState() == cell.MIXED) {
            int len = cell.children.length;

            for(int i=0; i < len; i++) {
                if (cell.children[i] == null) {
                    // TODO: I think this is right
                    switch(vc) {
                        case ALL:
                            if (cell.allState.getMaterial() == mat)
                                t.found(cell.vcx,cell.vcy,cell.vcz,BaseGrid.EMPTY_VOXEL);
                            break;
                        case OUTSIDE:
                            if (cell.allState.getMaterial() == mat)
                                t.found(cell.vcx,cell.vcy,cell.vcz,BaseGrid.EMPTY_VOXEL);
                            break;
                    }
                }
            }
        } else {
            byte state;

            if (cell.level == cell.maxLevel) {
//System.out.println(" TERM.  State: " + cell.allState.getState());
                if (cell.allState.getMaterial() != mat)
                    return;

                switch(vc) {
                    case ALL:
                        t.found(cell.vcx,cell.vcy,cell.vcz,cell.allState);
                        break;
                    case MARKED:
                        state = cell.allState.getState();
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            t.found(cell.vcx,cell.vcy,cell.vcz,cell.allState);
                        }
                        break;
                    case EXTERIOR:
                        state = cell.allState.getState();
                        if (state == Grid.EXTERIOR) {
                            t.found(cell.vcx,cell.vcy,cell.vcz,cell.allState);
                        }
                        break;
                    case INTERIOR:
                        state = cell.allState.getState();
                        if (state == Grid.INTERIOR) {
                            t.found(cell.vcx,cell.vcy,cell.vcz,cell.allState);
                        }
                        break;
                    case OUTSIDE:
                        state = cell.allState.getState();
                        if (state == Grid.OUTSIDE) {
                            t.found(cell.vcx,cell.vcy,cell.vcz,cell.allState);
                        }
                        break;
                }
            } else {
                if (cell.allState.getMaterial() != mat)
                    return;

//System.out.println(" ALL: " + cell.allState.getState() + " cell.size: " + cell.size + " level: " + cell.level + " max_level: " + cell.maxLevel);
                // TODO: Not sure why this works might be dodgy
                if (cell.level > 2) {
                    switch(vc) {
                        case ALL:
                            for(int i=0; i < cell.size; i++) {
                                for(int j=0; j < cell.size; j++) {
                                    for(int k=0; k < cell.size; k++) {
                                        t.found(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState);
                                    }
                                }
                            }
                            break;
                        case MARKED:
                            state = cell.allState.getState();
                            if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                for(int i=0; i < cell.size; i++) {
                                    for(int j=0; j < cell.size; j++) {
                                        for(int k=0; k < cell.size; k++) {
                                            t.found(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState);
                                        }
                                    }
                                }
                            }
                            break;
                        case EXTERIOR:
                            state = cell.allState.getState();
                            if (state == Grid.EXTERIOR) {
                                for(int i=0; i < cell.size; i++) {
                                    for(int j=0; j < cell.size; j++) {
                                        for(int k=0; k < cell.size; k++) {
                                            t.found(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState);
                                        }
                                    }
                                }
                            }
                            break;
                        case INTERIOR:
                            state = cell.allState.getState();
                            if (state == Grid.INTERIOR) {
                                for(int i=0; i < cell.size; i++) {
                                    for(int j=0; j < cell.size; j++) {
                                        for(int k=0; k < cell.size; k++) {
                                            t.found(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState);
                                        }
                                    }
                                }
                            }
                            break;
                        case OUTSIDE:
                            state = cell.allState.getState();
                            if (state == Grid.OUTSIDE) {
                                for(int i=0; i < cell.size; i++) {
                                    for(int j=0; j < cell.size; j++) {
                                        for(int k=0; k < cell.size; k++) {
                                            t.found(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState);
                                        }
                                    }
                                }
                            }
                            break;
                    }
                }
            }
        }
    }


    private boolean findInterruptible(OctreeCellInternalShort cell, VoxelClasses vc, int mat, ClassTraverser t) {
//System.out.println("find: " + cell);
        if (cell.allState.getState() == cell.MIXED) {
            int len = cell.children.length;

            for(int i=0; i < len; i++) {
                if (cell.children[i] == null) {
                    // TODO: I think this is right
                    switch(vc) {
                        case ALL:
                            if (cell.allState.getMaterial() == mat)
                                if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,BaseGrid.EMPTY_VOXEL))
                                    return false;
                            break;
                        case OUTSIDE:
                            if (cell.allState.getMaterial() == mat)
                                if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,BaseGrid.EMPTY_VOXEL))
                                    return false;
                            break;
                    }
                }
            }
        } else {
            byte state;

            if (cell.level == cell.maxLevel) {
//System.out.println(" TERM.  State: " + cell.allState.getState());
                if (cell.allState.getMaterial() != mat)
                    return true;

                switch(vc) {
                    case ALL:
                        if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,cell.allState))
                            return false;
                        break;
                    case MARKED:
                        state = cell.allState.getState();
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,cell.allState))
                                return false;
                        }
                        break;
                    case EXTERIOR:
                        state = cell.allState.getState();
                        if (state == Grid.EXTERIOR) {
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,cell.allState))
                                return false;
                        }
                        break;
                    case INTERIOR:
                        state = cell.allState.getState();
                        if (state == Grid.INTERIOR) {
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,cell.allState))
                                return false;
                        }
                        break;
                    case OUTSIDE:
                        state = cell.allState.getState();
                        if (state == Grid.OUTSIDE) {
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,cell.allState))
                                return false;
                        }
                        break;
                }
            } else {
                if (cell.allState.getMaterial() != mat)
                    return true;

//System.out.println(" ALL: " + cell.allState.getState() + " cell.size: " + cell.size + " level: " + cell.level + " max_level: " + cell.maxLevel);
                // TODO: Not sure why this works might be dodgy
                if (cell.level > 2) {
                    switch(vc) {
                        case ALL:
                            for(int i=0; i < cell.size; i++) {
                                for(int j=0; j < cell.size; j++) {
                                    for(int k=0; k < cell.size; k++) {
                                        if (!t.foundInterruptible(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState))
                                            return false;
                                    }
                                }
                            }
                            break;
                        case MARKED:
                            state = cell.allState.getState();
                            if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                for(int i=0; i < cell.size; i++) {
                                    for(int j=0; j < cell.size; j++) {
                                        for(int k=0; k < cell.size; k++) {
                                            if (!t.foundInterruptible(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState))
                                                return false;
                                        }
                                    }
                                }
                            }
                            break;
                        case EXTERIOR:
                            state = cell.allState.getState();
                            if (state == Grid.EXTERIOR) {
                                for(int i=0; i < cell.size; i++) {
                                    for(int j=0; j < cell.size; j++) {
                                        for(int k=0; k < cell.size; k++) {
                                            if (!t.foundInterruptible(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState))
                                                return false;
                                        }
                                    }
                                }
                            }
                            break;
                        case INTERIOR:
                            state = cell.allState.getState();
                            if (state == Grid.INTERIOR) {
                                for(int i=0; i < cell.size; i++) {
                                    for(int j=0; j < cell.size; j++) {
                                        for(int k=0; k < cell.size; k++) {
                                            if (!t.foundInterruptible(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState))
                                                return false;
                                        }
                                    }
                                }
                            }
                            break;
                        case OUTSIDE:
                            state = cell.allState.getState();
                            if (state == Grid.OUTSIDE) {
                                for(int i=0; i < cell.size; i++) {
                                    for(int j=0; j < cell.size; j++) {
                                        for(int k=0; k < cell.size; k++) {
                                            if (!t.foundInterruptible(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState))
                                                return false;
                                        }
                                    }
                                }
                            }
                            break;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Remove all voxels associated with the Material.
     *
     * @param mat The aterialID
     */
    public void removeMaterial(int mat) {
        ArrayList<VoxelCoordinate> remove_list = new ArrayList(100);

        ArrayList<OctreeCellInternalShort> list = new ArrayList();
        ArrayList<OctreeCellInternalShort> add_list = new ArrayList();

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalShort> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalShort cell = itr.next();

                VoxelData vd = cell.allState;

                if (vd.getState() != OctreeCellInternalShort.MIXED) {
                    if (vd.getMaterial() == mat && vd.getState() != Grid.OUTSIDE) {
                        remove_list.add(new VoxelCoordinate(cell.vcx, cell.vcy, cell.vcz));
                    }
                }

                OctreeCellInternalShort[] children = cell.children;

                if (children != null) {
                    int len = children.length;

                    for(int i=0; i < len; i++) {
                        if (children[i] != null)
                            add_list.add(children[i]);
                    }
                }
            }

            list.clear();

            itr = add_list.iterator();
            while(itr.hasNext()) {
                list.add(itr.next());
            }

            add_list.clear();
        }

        Iterator<VoxelCoordinate> itr = remove_list.iterator();
        while(itr.hasNext()) {
            VoxelCoordinate vc = itr.next();
            root.setData(null, vc.getX(), vc.getY(), vc.getZ(), Grid.OUTSIDE, 0);
        }
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

        if (vc == VoxelClasses.ALL || vc == VoxelClasses.OUTSIDE) {
            return super.findCount(vc);
        }

        ArrayList<OctreeCellInternalShort> list = new ArrayList(128);
        ArrayList<OctreeCellInternalShort> add_list = new ArrayList(128);

        list.add(root);

        while(list.size() > 0) {
//System.out.println("list size: " + list.size());
            Iterator<OctreeCellInternalShort> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalShort cell = itr.next();


                if (cell.allState.getState() == cell.MIXED) {
                    int len = cell.children.length;

                    for(int i=0; i < len; i++) {
                        if (cell.children[i] == null) {
                            switch(vc) {
                                case ALL:
                                    ret_val++;
                                    break;
                                case OUTSIDE:
                                    ret_val++;
                                    break;
                            }
                        }
                    }
                } else {
                    byte state;

                    if (cell.level == cell.maxLevel) {
        //System.out.println(" TERM.  State: " + cell.allState.getState());
                        switch(vc) {
                            case ALL:
                                ret_val++;
                                break;
                            case MARKED:
                                state = cell.allState.getState();
                                if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                    ret_val++;
                                }
                                break;
                            case EXTERIOR:
                                state = cell.allState.getState();
                                if (state == Grid.EXTERIOR) {
                                    ret_val++;
                                }
                                break;
                            case INTERIOR:
                                state = cell.allState.getState();
                                if (state == Grid.INTERIOR) {
                                    ret_val++;
                                }
                                break;
                            case OUTSIDE:
                                state = cell.allState.getState();
                                if (state == Grid.OUTSIDE) {
                                    ret_val++;
                                }
                                break;
                        }
                    } else {
        //System.out.println(" ALL: " + cell.allState.getState() + " cell.size: " + cell.size + " level: " + cell.level + " max_level: " + cell.maxLevel);
                        // TODO: Not sure why this works might be dodgy
                        if (cell.level > 2) {
                            switch(vc) {
                                case ALL:
                                    ret_val += cell.size * cell.size * cell.size;
                                    break;
                                case MARKED:
                                    state = cell.allState.getState();
                                    if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                        ret_val += cell.size * cell.size * cell.size;
                                    }
                                    break;
                                case EXTERIOR:
                                    state = cell.allState.getState();
                                    if (state == Grid.EXTERIOR) {
                                        ret_val += cell.size * cell.size * cell.size;
                                    }
                                    break;
                                case INTERIOR:
                                    state = cell.allState.getState();
                                    if (state == Grid.INTERIOR) {
                                        ret_val += cell.size * cell.size * cell.size;
                                    }
                                    break;
                                case OUTSIDE:
                                    state = cell.allState.getState();
                                    if (state == Grid.OUTSIDE) {
                                        ret_val += cell.size * cell.size * cell.size;
                                    }
                                    break;
                            }
                        }
                    }
                }

                OctreeCellInternalShort[] children = cell.children;

                if (children != null) {
                    int len = children.length;

                    for(int i=0; i < len; i++) {
                        if (children[i] != null)
                            add_list.add(children[i]);
                    }
                }
            }

            list.clear();

            itr = add_list.iterator();
            while(itr.hasNext()) {
                list.add(itr.next());
            }

            add_list.clear();
        }

        return ret_val;
    }

    /**
     * Find the count of a material.
     *
     * @param mat The material to count
     */
    public int findCount(int mat) {
        int ret_val = 0;

        ArrayList<OctreeCellInternalShort> list = new ArrayList(128);
        ArrayList<OctreeCellInternalShort> add_list = new ArrayList(128);

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalShort> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalShort cell = itr.next();

                if (cell.level == cell.maxLevel) {
//System.out.println(" TERM.  State: " + cell.allState.getState() + " pos: " + cell.vcx + " " + cell.vcy + " " + cell.vcz);

                    if (cell.allState.getMaterial() == mat && cell.allState.getState() != Grid.OUTSIDE) {
                        ret_val++;
                    }

                } else {
//    System.out.println(" ALL: " + cell.allState.getState() + " cell.size: " + cell.size + " level: " + cell.level + " max_level: " + cell.maxLevel);
                    // TODO: Not sure why this works might be dodgy
                    if (cell.level > 2) {
                        byte state = cell.allState.getState();

                        if (cell.allState.getMaterial() == mat &&
                            state != Grid.OUTSIDE &&
                            state != OctreeCellInternalShort.MIXED) {
                            ret_val += cell.size * cell.size * cell.size;
                        }
                    }
                }

                OctreeCellInternalShort[] children = cell.children;

                if (children != null) {
                    int len = children.length;

                    for(int i=0; i < len; i++) {
                        if (children[i] != null)
                            add_list.add(children[i]);
                    }
                }
            }

            list.clear();

            itr = add_list.iterator();
            while(itr.hasNext()) {
                list.add(itr.next());
            }

            add_list.clear();
        }

        return ret_val;
    }

}

