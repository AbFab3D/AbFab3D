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

import org.web3d.vrml.sav.BinaryContentHandler;


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
public class OctreeAttributeGridByte extends BaseAttributeGrid implements OctreeCell {
    private static final boolean STATS = true;

    /** The maximum coords to put in a shape */
    private static final int MAX_TRIANGLES_SHAPE = 1300000;

    protected OctreeCellInternalByte root;

    protected HashMap<WorldCoordinate,Integer> coords;
    protected ArrayList<WorldCoordinate> thisSlice;
    protected ArrayList<Integer> indices;
    protected int coordIdx;

    /**
     * Constructor.
     *
     * @param w The width in world coords
     * @param h The height in world coords
     * @param d The depth in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public OctreeAttributeGridByte(double w, double h, double d, double pixel, double sheight) {
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
    public OctreeAttributeGridByte(int w, int h, int d, double pixel, double sheight) {
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

//System.out.println("input size: " + size);
        int max_level = 0;
        int n = size;
        while(n > 1) {
            max_level++;
            n = n / 2;
        }

//System.out.println("max_level: " + max_level);
        if (Math.pow(2, max_level) < size) {
            max_level++;
        }

        size = (int) Math.pow(2, max_level);
        w = size;
        h = size;
        d = size;

        max_level++;  // Account for terminal leafs

//System.out.println("final size: " + size + " max_level: " + max_level);
        root = new OctreeCellInternalByte(1, max_level, 0,0,0,size, Grid.OUTSIDE, 0);
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
        Grid ret_val = new OctreeAttributeGridByte(w,h,d,pixel,sheight);

        return ret_val;
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
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public OctreeAttributeGridByte(OctreeAttributeGridByte grid) {
        super(grid.getWidth(), grid.getHeight(), grid.getDepth(),
            grid.getVoxelSize(), grid.getSliceHeight());

        this.root = (OctreeCellInternalByte) grid.root.clone();
    }

    /**
     * Get the children of this cell.
     *
     * @return The children
     */
    public OctreeCell[] getChildren() {
        return new OctreeCell[] { root };
    }

    /**
     * Get the state of the voxel.  If its not MIXED then all cells below
     * this are also this value.
     *
     * @return The voxel state
     */
    public byte getState() {
        return root.getState();
    }

    /**
     * Get the origin and size of this cell in voxel coordinates.
     *
     * @param origin The origin, preallocated to 3
     * @param size The size, preallocated to 3
     */
    public void getRegion(int[] origin, int[] size) {
        root.getRegion(origin, size);
    }

    /**
     * Get the data of the voxel
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public void getData(int x, int y, int z,VoxelData vd) {
        VoxelData ans = root.getData(x,y,z);
        vd.setData(ans.getState(), ans.getMaterial());
    }

    /**
     * Get the state of the voxels specified in the area.
     *
     * @param x1 The starting x grid coordinate
     * @param x2 The ending x grid coordinate
     * @param y1 The starting y grid coordinate
     * @param y2 The ending y grid coordinate
     * @param z1 The starting z grid coordinate
     * @param z2 The ending z grid coordinate
     *
     * @param ret Returns the data at each position.  3 dim array represented as flat, must be preallocated
     */
    public void getData(int x1, int x2, int y1, int y2, int z1, int z2, VoxelData[] ret) {
        // not impl
    }

    /**
     * Get the data of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public void getData(double x, double y, double z, VoxelData vd) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        VoxelData ans = root.getData(s_x,slice,s_z);
        vd.setData(ans.getState(), ans.getMaterial());
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
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
     */
    public int getAttribute(double x, double y, double z) {
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
     */
    public int getAttribute(int x, int y, int z) {
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
     * Set the material value of a voxel.  Leaves the state unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param material The materialID
     */
    public void setAttribute(int x, int y, int z, int material) {
        // TODO: not implemented yet
        throw new IllegalArgumentException("Not Implemented");
    }

    /**
     * Set the state value of a voxel.  Leaves the material unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value. 
     */
    public void setState(int x, int y, int z, byte state) {
        // TODO: This is not really right but is hard to deal with for BlockBased
        root.setData(null, x, y, z, state,Grid.NO_MATERIAL);
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
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        // TODO: This is not really right but is hard to deal with for BlockBased
        root.setData(null, s_x, slice, s_z, state,Grid.NO_MATERIAL);
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
     * Clone the object.
     */
    public Object clone() {
        OctreeAttributeGridByte ret_val = new OctreeAttributeGridByte(this);

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
    public void findAttribute(int mat, ClassAttributeTraverser t) {
        ArrayList<OctreeCellInternalByte> list = new ArrayList();
        ArrayList<OctreeCellInternalByte> add_list = new ArrayList();

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalByte> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalByte cell = itr.next();

                findAttribute(cell, mat, t);

                OctreeCellInternalByte[] children = cell.children;

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
     * Traverse a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttributeInterruptible(int mat, ClassAttributeTraverser t) {
        ArrayList<OctreeCellInternalByte> list = new ArrayList(128);
        ArrayList<OctreeCellInternalByte> add_list = new ArrayList(128);

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalByte> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalByte cell = itr.next();

                if (!findAttributeInterruptible(cell, mat, t))
                    return;

                OctreeCellInternalByte[] children = cell.children;

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

        ArrayList<OctreeCellInternalByte> list = new ArrayList();
        ArrayList<OctreeCellInternalByte> add_list = new ArrayList();

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalByte> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalByte cell = itr.next();

                find(cell, vc, t);

                OctreeCellInternalByte[] children = cell.children;

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
    public void findAttribute(VoxelClasses vc, ClassAttributeTraverser t) {
        if (vc == VoxelClasses.ALL || vc == VoxelClasses.OUTSIDE) {
            // I can't see a reason to optimize this
            super.findAttribute(vc, t);
            return;
        }

        ArrayList<OctreeCellInternalByte> list = new ArrayList();
        ArrayList<OctreeCellInternalByte> add_list = new ArrayList();

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalByte> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalByte cell = itr.next();

                findAttribute(cell, vc, t);

                OctreeCellInternalByte[] children = cell.children;

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

        ArrayList<OctreeCellInternalByte> list = new ArrayList();
        ArrayList<OctreeCellInternalByte> add_list = new ArrayList();

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalByte> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalByte cell = itr.next();

                if (!findInterruptible(cell, vc, t))
                    return;

                OctreeCellInternalByte[] children = cell.children;

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
    public void findAttributeInterruptible(VoxelClasses vc, ClassAttributeTraverser t) {
        if (vc == VoxelClasses.ALL || vc == VoxelClasses.OUTSIDE) {
            // I can't see a reason to optimize this
            super.findAttributeInterruptible(vc, t);
            return;
        }

        ArrayList<OctreeCellInternalByte> list = new ArrayList();
        ArrayList<OctreeCellInternalByte> add_list = new ArrayList();

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalByte> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalByte cell = itr.next();

                if (!findAttributeInterruptible(cell, vc, t))
                    return;

                OctreeCellInternalByte[] children = cell.children;

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

    private void find(OctreeCellInternalByte cell, VoxelClasses vc, ClassTraverser t) {
//System.out.println("find: " + cell);
        if (cell.allState.getState() == cell.MIXED) {
            int len = cell.children.length;

            for(int i=0; i < len; i++) {
                if (cell.children[i] == null) {
                    // TODO: I think this is right
                    switch(vc) {
                        case ALL:
                            t.found(cell.vcx,cell.vcy,cell.vcz, Grid.OUTSIDE);
                            break;
                        case OUTSIDE:
                            t.found(cell.vcx,cell.vcy,cell.vcz, Grid.OUTSIDE);
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
                        t.found(cell.vcx,cell.vcy,cell.vcz,cell.allState.getState());
                        break;
                    case MARKED:
                        state = cell.allState.getState();
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            t.found(cell.vcx,cell.vcy,cell.vcz,cell.allState.getState());
                        }
                        break;
                    case EXTERIOR:
                        state = cell.allState.getState();
                        if (state == Grid.EXTERIOR) {
                            t.found(cell.vcx,cell.vcy,cell.vcz,cell.allState.getState());
                        }
                        break;
                    case INTERIOR:
                        state = cell.allState.getState();
                        if (state == Grid.INTERIOR) {
                            t.found(cell.vcx,cell.vcy,cell.vcz,cell.allState.getState());
                        }
                        break;
                    case OUTSIDE:
                        state = cell.allState.getState();
                        if (state == Grid.OUTSIDE) {
                            t.found(cell.vcx,cell.vcy,cell.vcz,cell.allState.getState());
                        }
                        break;
                }
            } else {
//System.out.println(" ALL: " + cell.allState.getState() + " cell.size: " + cell.size + " level: " + cell.level + " max_level: " + cell.maxLevel);
                // TODO: Not sure why this works might be dodgy
                if (cell.level > 1) {
                    switch(vc) {
                        case ALL:
                            for(int i=0; i < cell.size; i++) {
                                for(int j=0; j < cell.size; j++) {
                                    for(int k=0; k < cell.size; k++) {
                                        t.found(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState.getState());
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
                                            t.found(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState.getState());
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
                                            t.found(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState.getState());
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
                                            t.found(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState.getState());
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
                                            t.found(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState.getState());
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

    private void findAttribute(OctreeCellInternalByte cell, VoxelClasses vc, ClassAttributeTraverser t) {
//System.out.println("find: " + cell);
        if (cell.allState.getState() == cell.MIXED) {
            int len = cell.children.length;

            for(int i=0; i < len; i++) {
                if (cell.children[i] == null) {
                    // TODO: I think this is right
                    switch(vc) {
                        case ALL:
                            t.found(cell.vcx,cell.vcy,cell.vcz, BaseAttributeGrid.EMPTY_VOXEL);
                            break;
                        case OUTSIDE:
                            t.found(cell.vcx,cell.vcy,cell.vcz, BaseAttributeGrid.EMPTY_VOXEL);
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
                if (cell.level > 1) {
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
    public void findAttribute(VoxelClasses vc, int mat, ClassAttributeTraverser t) {
        if (vc == VoxelClasses.ALL || vc == VoxelClasses.OUTSIDE) {
            // I can't see a reason to optimize this
            super.findAttribute(vc,mat, t);
            return;
        }

        ArrayList<OctreeCellInternalByte> list = new ArrayList();
        ArrayList<OctreeCellInternalByte> add_list = new ArrayList();

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalByte> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalByte cell = itr.next();

                findAttribute(cell, vc, mat, t);

                OctreeCellInternalByte[] children = cell.children;

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
    public void findAttributeInterruptible(VoxelClasses vc, int mat, ClassAttributeTraverser t) {
        if (vc == VoxelClasses.ALL || vc == VoxelClasses.OUTSIDE) {
            // I can't see a reason to optimize this
            super.findAttributeInterruptible(vc, mat, t);
            return;
        }

        ArrayList<OctreeCellInternalByte> list = new ArrayList();
        ArrayList<OctreeCellInternalByte> add_list = new ArrayList();

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalByte> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalByte cell = itr.next();

                if (!findAttributeInterruptible(cell, vc, mat, t))
                    return;

                OctreeCellInternalByte[] children = cell.children;

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
    
    private boolean findAttributeInterruptible(OctreeCellInternalByte cell, VoxelClasses vc, ClassAttributeTraverser t) {
//System.out.println("find: " + cell);
        if (cell.allState.getState() == cell.MIXED) {
            int len = cell.children.length;

            for(int i=0; i < len; i++) {
                if (cell.children[i] == null) {
                    // TODO: I think this is right
                    switch(vc) {
                        case ALL:
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz, BaseAttributeGrid.EMPTY_VOXEL))
                                return false;
                            break;
                        case OUTSIDE:
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz, BaseAttributeGrid.EMPTY_VOXEL))
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
                if (cell.level > 1) {
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

    private boolean findInterruptible(OctreeCellInternalByte cell, VoxelClasses vc, ClassTraverser t) {
//System.out.println("find: " + cell);
        if (cell.allState.getState() == cell.MIXED) {
            int len = cell.children.length;

            for(int i=0; i < len; i++) {
                if (cell.children[i] == null) {
                    // TODO: I think this is right
                    switch(vc) {
                        case ALL:
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz, BaseAttributeGrid.EMPTY_VOXEL.getState()))
                                return false;
                            break;
                        case OUTSIDE:
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz, BaseAttributeGrid.EMPTY_VOXEL.getState()))
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
                        if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,cell.allState.getState()))
                            return false;
                        break;
                    case MARKED:
                        state = cell.allState.getState();
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,cell.allState.getState()))
                                return false;
                        }
                        break;
                    case EXTERIOR:
                        state = cell.allState.getState();
                        if (state == Grid.EXTERIOR) {
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,cell.allState.getState()))
                                return false;
                        }
                        break;
                    case INTERIOR:
                        state = cell.allState.getState();
                        if (state == Grid.INTERIOR) {
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,cell.allState.getState()))
                                return false;
                        }
                        break;
                    case OUTSIDE:
                        state = cell.allState.getState();
                        if (state == Grid.OUTSIDE) {
                            if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz,cell.allState.getState()))
                                return false;
                        }
                        break;
                }
            } else {
//System.out.println(" ALL: " + cell.allState.getState() + " cell.size: " + cell.size + " level: " + cell.level + " max_level: " + cell.maxLevel);
                // TODO: Not sure why this works might be dodgy
                if (cell.level > 1) {
                    switch(vc) {
                        case ALL:
                            for(int i=0; i < cell.size; i++) {
                                for(int j=0; j < cell.size; j++) {
                                    for(int k=0; k < cell.size; k++) {
                                        if (!t.foundInterruptible(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState.getState()))
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
                                            if (!t.foundInterruptible(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState.getState()))
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
                                            if (!t.foundInterruptible(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState.getState()))
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
                                            if (!t.foundInterruptible(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState.getState()))
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
                                            if (!t.foundInterruptible(cell.vcx + i,cell.vcy + j,cell.vcz + k,cell.allState.getState()))
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

    private void findAttribute(OctreeCellInternalByte cell, int mat, ClassAttributeTraverser t) {
//System.out.println("find: " + cell);
        if (cell.allState.getState() == cell.MIXED) {
            int len = cell.children.length;

            for(int i=0; i < len; i++) {
                if (cell.children[i] == null) {
                    // OUTSIDE = 0 materialID
                    if (mat == 0)
                        t.found(cell.vcx,cell.vcy,cell.vcz, BaseAttributeGrid.EMPTY_VOXEL);
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
                if (cell.level > 1) {
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

    private boolean findAttributeInterruptible(OctreeCellInternalByte cell, int mat, ClassAttributeTraverser t) {
//System.out.println("find: " + cell);
        if (cell.allState.getState() == cell.MIXED) {
            int len = cell.children.length;

            for(int i=0; i < len; i++) {
                if (cell.children[i] == null) {
                    // OUTSIDE = 0 materialID
                    if (mat == 0)
                        if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz, BaseAttributeGrid.EMPTY_VOXEL))
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
                if (cell.level > 1) {
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

    private void findAttribute(OctreeCellInternalByte cell, VoxelClasses vc, int mat, ClassAttributeTraverser t) {
//System.out.println("find: " + cell);
        if (cell.allState.getState() == cell.MIXED) {
            int len = cell.children.length;

            for(int i=0; i < len; i++) {
                if (cell.children[i] == null) {
                    // TODO: I think this is right
                    switch(vc) {
                        case ALL:
                            if (cell.allState.getMaterial() == mat)
                                t.found(cell.vcx,cell.vcy,cell.vcz, BaseAttributeGrid.EMPTY_VOXEL);
                            break;
                        case OUTSIDE:
                            if (cell.allState.getMaterial() == mat)
                                t.found(cell.vcx,cell.vcy,cell.vcz, BaseAttributeGrid.EMPTY_VOXEL);
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
                if (cell.level > 1) {
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

    private boolean findAttributeInterruptible(OctreeCellInternalByte cell, VoxelClasses vc, int mat, ClassAttributeTraverser t) {
//System.out.println("find: " + cell);
        if (cell.allState.getState() == cell.MIXED) {
            int len = cell.children.length;

            for(int i=0; i < len; i++) {
                if (cell.children[i] == null) {
                    // TODO: I think this is right
                    switch(vc) {
                        case ALL:
                            if (cell.allState.getMaterial() == mat)
                                if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz, BaseAttributeGrid.EMPTY_VOXEL))
                                    return false;
                            break;
                        case OUTSIDE:
                            if (cell.allState.getMaterial() == mat)
                                if (!t.foundInterruptible(cell.vcx,cell.vcy,cell.vcz, BaseAttributeGrid.EMPTY_VOXEL))
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
                if (cell.level > 1) {
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
    public void removeAttribute(int mat) {
        ArrayList<VoxelCoordinate> remove_list = new ArrayList(100);

        ArrayList<OctreeCellInternalByte> list = new ArrayList();
        ArrayList<OctreeCellInternalByte> add_list = new ArrayList();

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalByte> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalByte cell = itr.next();

                VoxelData vd = cell.allState;

                if (vd.getState() != OctreeCellInternalByte.MIXED) {
                    if (vd.getMaterial() == mat && vd.getState() != Grid.OUTSIDE) {
                        remove_list.add(new VoxelCoordinate(cell.vcx, cell.vcy, cell.vcz));
                    }
                }

                OctreeCellInternalByte[] children = cell.children;

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
     */
    public int findCount(VoxelClasses vc) {
        int ret_val = 0;

        if (vc == VoxelClasses.ALL || vc == VoxelClasses.OUTSIDE) {
            return super.findCount(vc);
        }

        ArrayList<OctreeCellInternalByte> list = new ArrayList(128);
        ArrayList<OctreeCellInternalByte> add_list = new ArrayList(128);

        list.add(root);

        while(list.size() > 0) {
//System.out.println("list size: " + list.size());
            Iterator<OctreeCellInternalByte> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalByte cell = itr.next();


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
                        if (cell.level > 1) {
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

                OctreeCellInternalByte[] children = cell.children;

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

        ArrayList<OctreeCellInternalByte> list = new ArrayList(128);
        ArrayList<OctreeCellInternalByte> add_list = new ArrayList(128);

        list.add(root);

        while(list.size() > 0) {
            Iterator<OctreeCellInternalByte> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalByte cell = itr.next();

                if (cell.level == cell.maxLevel) {
//System.out.println(" TERM.  State: " + cell.allState.getState() + " pos: " + cell.vcx + " " + cell.vcy + " " + cell.vcz);

                    if (cell.allState.getMaterial() == mat && cell.allState.getState() != Grid.OUTSIDE) {
                        ret_val++;
                    }

                } else {
//    System.out.println(" ALL: " + cell.allState.getState() + " cell.size: " + cell.size + " level: " + cell.level + " max_level: " + cell.maxLevel);
                    // TODO: Not sure why this works might be dodgy
                    if (cell.level > 1) {
                        byte state = cell.allState.getState();

                        if (cell.allState.getMaterial() == mat &&
                            state != Grid.OUTSIDE &&
                            state != OctreeCellInternalByte.MIXED) {
                            ret_val += cell.size * cell.size * cell.size;
                        }
                    }
                }

                OctreeCellInternalByte[] children = cell.children;

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
     * Write a grid to the stream.
     *
     * Do not call this directly.  Made public so output package can
     * access.  I don't like this design but can't think of a better one
     * currently.  It needs internal knowledge to mafe effecient output.
     *
     * @param grid The grid to write
     * @param matColors Maps materials to colors
     */
    public void write(BinaryContentHandler writer, OctreeAttributeGridByte grid, Map<Integer, float[]> matColors) {
        double pixelSize = grid.getVoxelSize();
        double sheight = grid.getSliceHeight();
        float[] color = new float[] {0.8f,0.8f,0.8f};
        float transparency = 0;
        int idx = 0;
        long saved = 0;


System.out.println("Cell count: " + grid.getCellCount());


        coords = new HashMap<WorldCoordinate,Integer>();
        thisSlice = new ArrayList<WorldCoordinate>();
        indices = new ArrayList<Integer>();

        writer.startNode("Transform", null);
        writer.startField("translation");
        double tx,ty,tz;
        tx = grid.getWidth() / 2.0 * grid.getVoxelSize();
        ty = grid.getHeight() / 2.0 * grid.getSliceHeight();
        tz = grid.getDepth() / 2.0 * grid.getVoxelSize();
        writer.fieldValue(new float[] {(float)-tx,(float)-ty,(float)-tz}, 3);

        writer.startField("children");

        double[] wcoords = new double[3];

        ArrayList<OctreeCellInternalByte> list = new ArrayList();
        ArrayList<OctreeCellInternalByte> add_list = new ArrayList();

        list.add(root);

        long termCount = 0;
        byte cstate, state;

        while(list.size() > 0) {
            Iterator<OctreeCellInternalByte> itr = list.iterator();

            while(itr.hasNext()) {
                OctreeCellInternalByte cell = itr.next();

                state = cell.allState.getState();

                if (state != cell.MIXED) {
                    if (cell.level == cell.maxLevel) {
//System.out.println("cell: " + cell.vcx + " " + cell.vcy + " " + cell.vcz);
                        grid.getWorldCoords(cell.vcx, cell.vcy, cell.vcz, wcoords);

                        termCount++;

                        WorldCoordinate ubl_coord = new WorldCoordinate((float)(wcoords[0]),
                            (float)(wcoords[1] + sheight),(float)(wcoords[2]));
                        Integer ubl_pos = coords.get(ubl_coord);

//System.out.println("ubl: " + ubl_coord + " pos: " + ubl_pos);
                        if (ubl_pos == null) {
//    System.out.println("ubl added: " + coordIdx);
                            ubl_pos = new Integer(coordIdx++);
                            coords.put(ubl_coord, ubl_pos);
                            thisSlice.add(ubl_coord);
                        }

                        WorldCoordinate ubr_coord = new WorldCoordinate((float)(wcoords[0] + pixelSize),
                            (float)(wcoords[1] + sheight),(float)(wcoords[2]));
                        Integer ubr_pos = coords.get(ubr_coord);
//System.out.println("ubr: " + ubr_coord + " pos: " + ubr_pos);
                        if (ubr_pos == null) {
//    System.out.println("ubr added: " + coordIdx);
                            ubr_pos = new Integer(coordIdx++);
                            coords.put(ubr_coord, ubr_pos);
                            thisSlice.add(ubr_coord);
                        }

                        // Origin of the cell
                        WorldCoordinate lbl_coord = new WorldCoordinate((float)(wcoords[0]),
                            (float)(wcoords[1]),(float)(wcoords[2]));
                        Integer lbl_pos = coords.get(lbl_coord);
//System.out.println("lbl: " + lbl_coord + " pos: " + lbl_pos);
                        if (lbl_pos == null) {
//    System.out.println("lbl added: " + coordIdx);
                            lbl_pos = new Integer(coordIdx++);
                            coords.put(lbl_coord, lbl_pos);
                            thisSlice.add(lbl_coord);
                        }

                        WorldCoordinate lbr_coord = new WorldCoordinate((float)(wcoords[0] + pixelSize),
                            (float)(wcoords[1]),(float)(wcoords[2]));
                        Integer lbr_pos = coords.get(lbr_coord);
//System.out.println("lbr: " + lbr_coord + " pos: " + lbr_pos);
                        if (lbr_pos == null) {
//    System.out.println("lbr added: " + coordIdx);
                            lbr_pos = new Integer(coordIdx++);
                            coords.put(lbr_coord, lbr_pos);
                            thisSlice.add(lbr_coord);
                        }

                        WorldCoordinate ufl_coord = new WorldCoordinate((float)(wcoords[0]),
                            (float)(wcoords[1] + sheight),(float)(wcoords[2] + pixelSize));
                        Integer ufl_pos = coords.get(ufl_coord);
//System.out.println("ufl: " + ufl_coord + " pos: " + ufl_pos);
                        if (ufl_pos == null) {
//    System.out.println("ufl added: " + coordIdx);
                            ufl_pos = new Integer(coordIdx++);
                            coords.put(ufl_coord, ufl_pos);
                            thisSlice.add(ufl_coord);
                        }

                        WorldCoordinate ufr_coord = new WorldCoordinate((float)(wcoords[0] + pixelSize),
                            (float)(wcoords[1] + sheight),(float)(wcoords[2] + pixelSize));
                        Integer ufr_pos = coords.get(ufr_coord);
//System.out.println("ufr: " + ufr_coord + " pos: " + ufr_pos);
                        if (ufr_pos == null) {
//    System.out.println("ufr added: " + coordIdx);
                            ufr_pos = new Integer(coordIdx++);
                            coords.put(ufr_coord, ufr_pos);
                            thisSlice.add(ufr_coord);
                        }


                        WorldCoordinate lfl_coord = new WorldCoordinate((float)(wcoords[0]),
                            (float)(wcoords[1]),(float)(wcoords[2] + pixelSize));
                        Integer lfl_pos = coords.get(lfl_coord);
//System.out.println("lfl: " + lfl_coord + " pos: " + lfl_pos);

                        if (lfl_pos == null) {
//    System.out.println("lfl added: " + coordIdx);
                            lfl_pos = new Integer(coordIdx++);
                            coords.put(lfl_coord, lfl_pos);
                            thisSlice.add(lfl_coord);
                        }

                        WorldCoordinate lfr_coord = new WorldCoordinate((float)(wcoords[0] + pixelSize),
                            (float)(wcoords[1]),(float)(wcoords[2] + pixelSize));
                        Integer lfr_pos = coords.get(lfr_coord);
//System.out.println("lfr: " + lfr_coord + " pos: " + lfr_pos);

                        if (lfr_pos == null) {
//    System.out.println("lfr added: " + coordIdx);
                            lfr_pos = new Integer(coordIdx++);
                            coords.put(lfr_coord, lfr_pos);
                            thisSlice.add(lfr_coord);
                        }

                        // Create Box

                        boolean displayFront = true;

                        if (cell.vcz < depth - 1) {
                            cstate = grid.getState(cell.vcx,cell.vcy,cell.vcz+1);

                            if (cstate == state) {
//System.out.println("no front");
                                displayFront = false;
                                if (STATS) saved++;
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

                        if (cell.vcz > 0) {
                            cstate = grid.getState(cell.vcx,cell.vcy,cell.vcz-1);

                            if (cstate == state) {
//System.out.println("no back");

                                displayBack = false;
                                if (STATS) saved++;
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

                        if (cell.vcx < width - 1) {
                            cstate = grid.getState(cell.vcx+1,cell.vcy,cell.vcz);
                            if (cstate == state) {
//System.out.println("no right");
                                displayRight = false;
                                if (STATS) saved++;
                            } else {
//System.out.println("yes right: " + cstate + " pos: " + (cell.vcx+1) + " " + cell.vcy + " " + cell.vcz);
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


                        if (cell.vcx > 0) {
                            cstate = grid.getState(cell.vcx-1,cell.vcy,cell.vcz);
//System.out.println("no left");

                            if (cstate == state) {
                                displayLeft = false;
                                if (STATS) saved++;
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

                        if (cell.vcy < height - 1) {
                            cstate = grid.getState(cell.vcx,cell.vcy+1,cell.vcz);

                            if (cstate == state) {
//System.out.println("no top");

                                displayTop = false;
                                if (STATS) saved++;
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

                        if (cell.vcy > 0) {
                            cstate = grid.getState(cell.vcx,cell.vcy-1,cell.vcz);
                            if (cstate == state) {
//System.out.println("no bottom");

                                displayBottom = false;
                                if (STATS) saved++;
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

                        if (indices.size() / 3 >= MAX_TRIANGLES_SHAPE) {
                            ejectShape(writer, thisSlice, indices, color, transparency);
                        }
                    } else {
                        if (cell.level > 1) {
                            // How big should we go, really impacts speed
                            // 1 = 46 seconds
                            if (cell.size > 1) {
                                ejectBoxCollapse(writer, grid, cell, color, transparency);
                            } else {
                                ejectBox(writer, grid, cell, color, transparency);
                            }
                        }
                    }
                }


                // Traverse Children
                OctreeCellInternalByte[] children = cell.children;

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

        ejectShape(writer, thisSlice, indices, color, transparency);

System.out.println("Terminal nodes: " + termCount);
if (STATS) System.out.println("saved sides: " + saved);
        // End Centering Transform
        writer.endField();
        writer.endNode();
    }

    /**
     * Eject a shape into the stream.
     *
     * @param stream The stream to use
     * @param totalCoords The coords to use
     * @param indices The indices to use
     */
    private void ejectShape(BinaryContentHandler stream, List<WorldCoordinate> totalCoords,
        ArrayList<Integer> indices, float[] color, float transparency) {

System.out.println("eject:   coords: " + totalCoords.size() + " indices: " + indices.size());
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
//        int[] allIndices = new int[(int) (indices.size() * 4 / 3)];
        int[] allIndices = new int[(int) (indices.size())];
        for(int i=0; i < indices.size(); ) {
            allIndices[idx++] = indices.get(i++);
            allIndices[idx++] = indices.get(i++);
            allIndices[idx++] = indices.get(i++);
            //allIndices[idx++] = -1;
        }

        stream.startNode("Shape", null);
        stream.startField("appearance");
        stream.startNode("Appearance", null);
        stream.startField("material");
        stream.startNode("Material",null);
        stream.startField("diffuseColor");
//        stream.startField("emissiveColor");
        stream.fieldValue(color,3);
        stream.startField("transparency");
        stream.fieldValue(transparency);
        stream.endNode();  //  Material
        stream.endNode();  //  Appearance
        stream.startField("geometry");
//        stream.startNode("IndexedFaceSet", null);
        stream.startNode("IndexedTriangleSet", null);
        stream.startField("coord");
        stream.startNode("Coordinate", null);
        stream.startField("point");
        stream.fieldValue(allCoords, allCoords.length);
        stream.endNode();  // Coordinate
//        stream.startField("coordIndex");
        stream.startField("index");
        stream.fieldValue(allIndices, allIndices.length);
        stream.endNode();  // IndexedFaceSet
        stream.endNode();  // Shape

        coords.clear();
        indices.clear();
        thisSlice.clear();
        coordIdx = 0;
    }

    /**
     * Eject a shape into the stream.
     *
     * @param stream The stream to use
     */
    private void ejectBox(BinaryContentHandler stream, Grid grid, OctreeCellInternalByte cell,
        float[] color, float transparency) {

//System.out.println("ejectBox: " + cell.vcx + " " + cell.vcy + " " + cell.vcz + " size: " + cell.size);
        double pixelSize = cell.size * grid.getVoxelSize();
        double sheight = cell.size * grid.getSliceHeight();
        double hpixelSize = cell.size * grid.getVoxelSize() / 2.0;
        double hsheight = cell.size * grid.getSliceHeight() / 2.0;

        long saved = 0;

/*
TODO: Why would you do this?
        HashMap<WorldCoordinate,Integer> coords = new HashMap<WorldCoordinate,Integer>();
        ArrayList<WorldCoordinate> thisSlice = new ArrayList<WorldCoordinate>();
*/
        double[] wcoords = new double[3];

        grid.getWorldCoords(cell.vcx, cell.vcy, cell.vcz, wcoords);

        WorldCoordinate ubl_coord = new WorldCoordinate((float)(wcoords[0]),
            (float)(wcoords[1] + sheight),(float)(wcoords[2]));
        Integer ubl_pos = coords.get(ubl_coord);

//System.out.println("ubl: " + ubl_coord + " pos: " + ubl_pos);
        if (ubl_pos == null) {
//    System.out.println("ubl added: " + coordIdx);
            ubl_pos = new Integer(coordIdx++);
            coords.put(ubl_coord, ubl_pos);
            thisSlice.add(ubl_coord);
        }

        WorldCoordinate ubr_coord = new WorldCoordinate((float)(wcoords[0] + pixelSize),
            (float)(wcoords[1] + sheight),(float)(wcoords[2]));
        Integer ubr_pos = coords.get(ubr_coord);
//System.out.println("ubr: " + ubr_coord + " pos: " + ubr_pos);
        if (ubr_pos == null) {
//    System.out.println("ubr added: " + coordIdx);
            ubr_pos = new Integer(coordIdx++);
            coords.put(ubr_coord, ubr_pos);
            thisSlice.add(ubr_coord);
        }

        // Origin of the cell
        WorldCoordinate lbl_coord = new WorldCoordinate((float)(wcoords[0]),
            (float)(wcoords[1]),(float)(wcoords[2]));
        Integer lbl_pos = coords.get(lbl_coord);
//System.out.println("lbl: " + lbl_coord + " pos: " + lbl_pos);
        if (lbl_pos == null) {
//    System.out.println("lbl added: " + coordIdx);
            lbl_pos = new Integer(coordIdx++);
            coords.put(lbl_coord, lbl_pos);
            thisSlice.add(lbl_coord);
        }

        WorldCoordinate lbr_coord = new WorldCoordinate((float)(wcoords[0] + pixelSize),
            (float)(wcoords[1]),(float)(wcoords[2]));
        Integer lbr_pos = coords.get(lbr_coord);
//System.out.println("lbr: " + lbr_coord + " pos: " + lbr_pos);
        if (lbr_pos == null) {
//    System.out.println("lbr added: " + coordIdx);
            lbr_pos = new Integer(coordIdx++);
            coords.put(lbr_coord, lbr_pos);
            thisSlice.add(lbr_coord);
        }

        WorldCoordinate ufl_coord = new WorldCoordinate((float)(wcoords[0]),
            (float)(wcoords[1] + sheight),(float)(wcoords[2] + pixelSize));
        Integer ufl_pos = coords.get(ufl_coord);
//System.out.println("ufl: " + ufl_coord + " pos: " + ufl_pos);
        if (ufl_pos == null) {
//    System.out.println("ufl added: " + coordIdx);
            ufl_pos = new Integer(coordIdx++);
            coords.put(ufl_coord, ufl_pos);
            thisSlice.add(ufl_coord);
        }

        WorldCoordinate ufr_coord = new WorldCoordinate((float)(wcoords[0]  + pixelSize),
            (float)(wcoords[1] + sheight),(float)(wcoords[2] + pixelSize));
        Integer ufr_pos = coords.get(ufr_coord);
//System.out.println("ufr: " + ufr_coord + " pos: " + ufr_pos);
        if (ufr_pos == null) {
//    System.out.println("ufr added: " + coordIdx);
            ufr_pos = new Integer(coordIdx++);
            coords.put(ufr_coord, ufr_pos);
            thisSlice.add(ufr_coord);
        }


        WorldCoordinate lfl_coord = new WorldCoordinate((float)(wcoords[0]),
            (float)(wcoords[1]),(float)(wcoords[2] + pixelSize));
        Integer lfl_pos = coords.get(lfl_coord);
//System.out.println("lfl: " + lfl_coord + " pos: " + lfl_pos);

        if (lfl_pos == null) {
//    System.out.println("lfl added: " + coordIdx);
            lfl_pos = new Integer(coordIdx++);
            coords.put(lfl_coord, lfl_pos);
            thisSlice.add(lfl_coord);
        }

        WorldCoordinate lfr_coord = new WorldCoordinate((float)(wcoords[0] + pixelSize),
            (float)(wcoords[1]),(float)(wcoords[2] + pixelSize));
        Integer lfr_pos = coords.get(lfr_coord);
//System.out.println("lfr: " + lfr_coord + " pos: " + lfr_pos);

        if (lfr_pos == null) {
//    System.out.println("lfr added: " + coordIdx);
            lfr_pos = new Integer(coordIdx++);
            coords.put(lfr_coord, lfr_pos);
            thisSlice.add(lfr_coord);
        }

        // Create Box

        boolean displayFront = true;

/*
        if (cell.vcz < depth - 1) {
            cstate = grid.getState(cell.vcx,cell.vcy,cell.vcz+1);

            if (cstate == state) {
                displayFront = false;
                if (STATS) saved++;
            }
        }
*/
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

/*
        if (cell.vcz > 0) {
            cstate = grid.getState(cell.vcx,cell.vcy,cell.vcz-1);

            if (cstate == state) {
                displayBack = false;
                if (STATS) saved++;
            }
        }
*/

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

/*
        if (cell.vcx < width - 1) {
            cstate = grid.getState(cell.vcx+1,cell.vcy,cell.vcz);
System.out.println("check right  curr: " + state + " right: " + cstate);
            if (cstate == state) {
                displayRight = false;
                if (STATS) saved++;
            }
        }
*/
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

/*
        if (cell.vcx > 0) {
            cstate = grid.getState(cell.vcx-1,cell.vcy,cell.vcz);

            if (cstate == state) {
                displayLeft = false;
                if (STATS) saved++;
            }
        }
*/

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

/*
        if (cell.vcy < height - 1) {
            cstate = grid.getState(cell.vcx,cell.vcy+1,cell.vcz);

            if (cstate == state) {
                displayTop = false;
                if (STATS) saved++;
            }
        }
*/

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

/*
        if (cell.vcy > 0) {
            cstate = grid.getState(cell.vcx,cell.vcy-1,cell.vcz);

            if (cstate == state) {
                displayBottom = false;
                if (STATS) saved++;
            }
        }
*/

        if (displayBottom) {
            // Bottom Face
            indices.add(new Integer(lfr_pos));
            indices.add(new Integer(lbl_pos));
            indices.add(new Integer(lbr_pos));
            indices.add(new Integer(lfr_pos));
            indices.add(new Integer(lfl_pos));
            indices.add(new Integer(lbl_pos));
        }

        if (indices.size() / 3 >= MAX_TRIANGLES_SHAPE) {
            ejectShape(stream, thisSlice, indices, color, transparency);
        }
    }

    /**
     * Eject a shape into the stream.
     *
     * @param stream The stream to use
     */
    private void ejectBoxCollapse(BinaryContentHandler stream, Grid grid, OctreeCellInternalByte cell,
        float[] color, float transparency) {

//System.out.println("ejectBoxCollapse: " + cell.vcx + " " + cell.vcy + " " + cell.vcz + " size: " + cell.size);
        double pixelSize = cell.size * grid.getVoxelSize();
        double sheight = cell.size * grid.getSliceHeight();
        double hpixelSize = cell.size * grid.getVoxelSize() / 2.0;
        double hsheight = cell.size * grid.getSliceHeight() / 2.0;

        long saved = 0;
        byte cstate, state;

        double[] wcoords = new double[3];

        state = cell.allState.getState();

        if (state == Grid.OUTSIDE) {
System.out.println("Interior OUTSIDE!  size: " + cell.size);
        }

        grid.getWorldCoords(cell.vcx, cell.vcy, cell.vcz, wcoords);

        WorldCoordinate ubl_coord = new WorldCoordinate((float)(wcoords[0]),
            (float)(wcoords[1] + sheight),(float)(wcoords[2]));
        Integer ubl_pos = coords.get(ubl_coord);

//System.out.println("ubl: " + ubl_coord + " pos: " + ubl_pos);
        if (ubl_pos == null) {
//    System.out.println("ubl added: " + coordIdx);
            ubl_pos = new Integer(coordIdx++);
            coords.put(ubl_coord, ubl_pos);
            thisSlice.add(ubl_coord);
        }

        WorldCoordinate ubr_coord = new WorldCoordinate((float)(wcoords[0] + pixelSize),
            (float)(wcoords[1] + sheight),(float)(wcoords[2]));
        Integer ubr_pos = coords.get(ubr_coord);
//System.out.println("ubr: " + ubr_coord + " pos: " + ubr_pos);
        if (ubr_pos == null) {
//    System.out.println("ubr added: " + coordIdx);
            ubr_pos = new Integer(coordIdx++);
            coords.put(ubr_coord, ubr_pos);
            thisSlice.add(ubr_coord);
        }

        // Origin of the cell
        WorldCoordinate lbl_coord = new WorldCoordinate((float)(wcoords[0]),
            (float)(wcoords[1]),(float)(wcoords[2]));
        Integer lbl_pos = coords.get(lbl_coord);
//System.out.println("lbl: " + lbl_coord + " pos: " + lbl_pos);
        if (lbl_pos == null) {
//    System.out.println("lbl added: " + coordIdx);
            lbl_pos = new Integer(coordIdx++);
            coords.put(lbl_coord, lbl_pos);
            thisSlice.add(lbl_coord);
        }

        WorldCoordinate lbr_coord = new WorldCoordinate((float)(wcoords[0] + pixelSize),
            (float)(wcoords[1]),(float)(wcoords[2]));
        Integer lbr_pos = coords.get(lbr_coord);
//System.out.println("lbr: " + lbr_coord + " pos: " + lbr_pos);
        if (lbr_pos == null) {
//    System.out.println("lbr added: " + coordIdx);
            lbr_pos = new Integer(coordIdx++);
            coords.put(lbr_coord, lbr_pos);
            thisSlice.add(lbr_coord);
        }

        WorldCoordinate ufl_coord = new WorldCoordinate((float)(wcoords[0]),
            (float)(wcoords[1] + sheight),(float)(wcoords[2] + pixelSize));
        Integer ufl_pos = coords.get(ufl_coord);
//System.out.println("ufl: " + ufl_coord + " pos: " + ufl_pos);
        if (ufl_pos == null) {
//    System.out.println("ufl added: " + coordIdx);
            ufl_pos = new Integer(coordIdx++);
            coords.put(ufl_coord, ufl_pos);
            thisSlice.add(ufl_coord);
        }

        WorldCoordinate ufr_coord = new WorldCoordinate((float)(wcoords[0]  + pixelSize),
            (float)(wcoords[1] + sheight),(float)(wcoords[2] + pixelSize));
        Integer ufr_pos = coords.get(ufr_coord);
//System.out.println("ufr: " + ufr_coord + " pos: " + ufr_pos);
        if (ufr_pos == null) {
//    System.out.println("ufr added: " + coordIdx);
            ufr_pos = new Integer(coordIdx++);
            coords.put(ufr_coord, ufr_pos);
            thisSlice.add(ufr_coord);
        }


        WorldCoordinate lfl_coord = new WorldCoordinate((float)(wcoords[0]),
            (float)(wcoords[1]),(float)(wcoords[2] + pixelSize));
        Integer lfl_pos = coords.get(lfl_coord);
//System.out.println("lfl: " + lfl_coord + " pos: " + lfl_pos);

        if (lfl_pos == null) {
//    System.out.println("lfl added: " + coordIdx);
            lfl_pos = new Integer(coordIdx++);
            coords.put(lfl_coord, lfl_pos);
            thisSlice.add(lfl_coord);
        }

        WorldCoordinate lfr_coord = new WorldCoordinate((float)(wcoords[0] + pixelSize),
            (float)(wcoords[1]),(float)(wcoords[2] + pixelSize));
        Integer lfr_pos = coords.get(lfr_coord);
//System.out.println("lfr: " + lfr_coord + " pos: " + lfr_pos);

        if (lfr_pos == null) {
//    System.out.println("lfr added: " + coordIdx);
            lfr_pos = new Integer(coordIdx++);
            coords.put(lfr_coord, lfr_pos);
            thisSlice.add(lfr_coord);
        }

        // Create Box

        boolean displayFront = true;
        boolean allSame = true;

        if (cell.vcz < depth - cell.size) {
            for(int i=0; i < cell.size; i++) {
                for(int j=0; j < cell.size; j++) {
                    for(int k=0; k < cell.size; k++) {
                        cstate = grid.getState(cell.vcx+i,cell.vcy+j,cell.vcz+k+1);

                        if (cstate != state) {
                            allSame = false;
                            break;
                        }
                    }
                }
            }

            if (allSame) {
                displayFront = false;
                if (STATS) saved += cell.size * cell.size * cell.size;
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
        allSame = true;

        if (cell.vcz > cell.size - 1) {
            for(int i=0; i < cell.size; i++) {
                for(int j=0; j < cell.size; j++) {
                    for(int k=0; k < cell.size; k++) {
                        cstate = grid.getState(cell.vcx+i,cell.vcy+j,cell.vcz+k-2);

                        if (cstate != state) {
                            allSame = false;
                            break;
                        }
                    }
                }
            }

            if (allSame) {
                displayBack = false;
                if (STATS) saved += cell.size * cell.size * cell.size;
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
        allSame = true;

        if (cell.vcx < width - cell.size) {
            for(int i=0; i < cell.size; i++) {
                for(int j=0; j < cell.size; j++) {
                    for(int k=0; k < cell.size; k++) {
                        cstate = grid.getState(cell.vcx+i+1,cell.vcy+j,cell.vcz+k);

                        if (cstate != state) {
                            allSame = false;
                            break;
                        }
                    }
                }
            }

            if (allSame) {
                displayRight = false;
                if (STATS) saved += cell.size * cell.size * cell.size;
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
        allSame = true;

        if (cell.vcx > cell.size - 1) {
            for(int i=0; i < cell.size; i++) {
                for(int j=0; j < cell.size; j++) {
                    for(int k=0; k < cell.size; k++) {
                        cstate = grid.getState(cell.vcx+i-2,cell.vcy+j,cell.vcz+k);

                        if (cstate != state) {
                            allSame = false;
                            break;
                        }
                    }
                }
            }

            if (allSame) {
                displayLeft = false;
                if (STATS) saved += cell.size * cell.size * cell.size;
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
        allSame = true;

        if (cell.vcy < height - cell.size) {
            for(int i=0; i < cell.size; i++) {
                for(int j=0; j < cell.size; j++) {
                    for(int k=0; k < cell.size; k++) {
                        cstate = grid.getState(cell.vcx+i,cell.vcy+j+1,cell.vcz+k);

                        if (cstate != state) {
                            allSame = false;
                            break;
                        }
                    }
                }
            }

            if (allSame) {
                displayTop = false;
                if (STATS) saved += cell.size * cell.size * cell.size;
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
        allSame = true;

        if (cell.vcy > cell.size - 1) {
            for(int i=0; i < cell.size; i++) {
                for(int j=0; j < cell.size; j++) {
                    for(int k=0; k < cell.size; k++) {
                        cstate = grid.getState(cell.vcx+i,cell.vcy+j-2,cell.vcz+k);

                        if (cstate != state) {
                            allSame = false;
                            break;
                        }
                    }
                }
            }

            if (allSame) {
                displayBottom = false;
                if (STATS) saved += cell.size * cell.size * cell.size;
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

        if (indices.size() / 3 >= MAX_TRIANGLES_SHAPE) {
            ejectShape(stream, thisSlice, indices, color, transparency);
        }
    }
}

