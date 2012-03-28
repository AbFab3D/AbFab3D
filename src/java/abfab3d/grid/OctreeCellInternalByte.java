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

/**
 * An internal cell in an Octree structure.
 *
 * Octant Structure:
 *
 *   -------
 *   | 1 2 |
 *   | 0 3 |
 *   -------
 *
 * @author Alan Hudson
 */
public class OctreeCellInternalByte implements OctreeCell, Cloneable {
    private static final boolean DEBUG = false;

    // Denotes the state of this cell is mixed
    protected static final byte MIXED = Grid.USER_DEFINED;

    /** The x voxel coordinate of the lower left corner */
    protected int vcx;

    /** The y voxel coordinate of the lower left corner */
    protected int vcy;

    /** The z voxel coordinate of the lower left corner */
    protected int vcz;

    /** The number of voxels in all directions */
    protected int size;

    /** The level of this cell, root = 1 */
    protected int level;

    /** The maximum level to support */
    protected int maxLevel;

    /** The children cells */
    protected OctreeCellInternalByte[] children;

    /** The state of this cell and all children or -1 if mixed */
    protected VoxelData allState;

    protected OctreeCellInternalByte() {
    }

    public OctreeCellInternalByte(int level, int maxLevel, int x, int y, int z, int size, byte state, int material) {
        this.level = level;
        this.maxLevel = maxLevel;

        this.vcx = x;
        this.vcy = y;
        this.vcz = z;

        this.size = size;

//System.out.println("Create grid: level: " + level + " size: " + size + " state: " + state + " orig: " + x + " " + y + " " + z + " hc: " + hashCode());
        // TODO: Need short/int versions
        allState = new VoxelDataByte(state, material);
    }

    /**
     * Get the children of this cell.
     *
     * @return The children
     */
    public OctreeCell[] getChildren() {
        return children;
    }

    /**
     * Get the state of the voxel.  If its not MIXED then all cells below
     * this are also this value.
     *
     * @return The voxel state
     */
    public byte getState() {
        return allState.getState();
    }

    /**
     * Get the origin and size of this cell in voxel coordinates.
     *
     * @param origin The origin, preallocated to 3
     * @param size The size, preallocated to 3
     */
    public void getRegion(int[] origin, int[] size) {
        int hsize = this.size >> 2;

        origin[0] = vcx;
        origin[1] = vcy;
        origin[2] = vcz;

        size[0] = this.size;
        size[1] = this.size;
        size[2] = this.size;
    }

    /**
     * Get the data located at the specified cell in voxel coordinates.
     *
     * @param x The x voxel coordinate
     * @param y The y voxel coordinate
     * @param z The z voxel coordinate
     *
     * @return The voxel data
     */
    public VoxelData getData(int x, int y, int z) {
//System.out.println("getData: cell: " + hashCode() + " pos: " + x + " " + y + " " + z + " size: " + size);
        if (allState.getState() == MIXED) {
            int oc = findOctant(x,y,z);
//System.out.println("  mixed, oct: " + oc);
            OctreeCellInternalByte child = children[oc];

            if (child == null)
                return BaseAttributeGrid.EMPTY_VOXEL;

            return child.getData(x,y,z);
        } else {
//System.out.println("  homo, hc: " + hashCode() + " state: " + allState.getState());
            return allState;
        }
    }

    /**
     * Set the data located at the specified cell in voxel coordinates.  Internal
     * set that just sets the value with no recursion.
     *
     * @param x The x voxel coordinate
     * @param y The y voxel coordinate
     * @param z The z voxel coordinate
     * @param state The voxel state
     * @param material The voxel material
     */
    private void setDataInternal(int x, int y, int z, byte state, int material) {
if (DEBUG) System.out.println("Setting dataInternal: " + x + " " + y + " " + z + " new_state: " + state + " new_mat: " + material);
        allState = new VoxelDataByte(state, material);
    }

    /**
     * Set the data located at the specified cell in voxel coordinates.
     *
     * @param x The x voxel coordinate
     * @param y The y voxel coordinate
     * @param z The z voxel coordinate
     * @param state The voxel state
     * @param material The voxel material
     */
    protected void setData(OctreeCellInternalByte parent, int x, int y, int z, byte state, int material) {
        byte astate = allState.getState();
        int amaterial = allState.getMaterial();

        if (level >= maxLevel) {
if (DEBUG) System.out.println("Setting TERM: cell: " + hashCode() + " " + x + " " + y + " " + z + " new_state: " + state + " new_mat: " + material);
if (DEBUG) System.out.println("   orig: " + vcx + " " + vcy + " " + vcz + " new_state: " + state + " new_mat: " + material);
/*
            vcx = x - vcx;
            vcy = y - vcy;
            vcz = z - vcz;
if (DEBUG) System.out.println("   pos: " + vcx + " " + vcy + " " + vcz + " new_state: " + state + " new_mat: " + material);
*/
            vcx = x;
            vcy = y;
            vcz = z;
            allState.setData(state, material);
            return;
        }

if (DEBUG) System.out.println("Setting data: " + x + " " + y + " " + z + " old_state: " + astate + " new_state: " + state + "old_mat: " + amaterial + " new_mat: " + material);
        if (astate != MIXED) {
            if (state == astate && amaterial == material) {
if (DEBUG) System.out.println("   Same state");
                // same, ignore
                return;
            } else {
                int oc = findOctant(x,y,z);

if (DEBUG) System.out.println("Splitting cell");

                split(oc, allState.getState(), allState.getMaterial());

                allState.setState(MIXED);

                children[oc].setData(this, x,y,z,state,material);
                // TODO: Can we leave the rest null to save memory
            }
        } else {
            int oc = findOctant(x,y,z);

if (DEBUG) System.out.println("   mixed, oc: " + oc);
            if (children[oc] == null) {
                createChild(oc, Grid.OUTSIDE, 0);  // Make outside to force split
            }
            children[oc].setData(this, x,y,z,state,material);

            collapse(state, material, parent);
        }
    }

    /**
     * Get the count of cell nodes in this structure and below.  This can be
     * either internal or leaf nodes.
     *
     * @return The count of nodes
     */
    protected int getCellCount() {
        int ret_val = 1;

        if (children == null)
            return ret_val;

        for(int i=0; i < children.length; i++) {
            if (children[i] != null)
                ret_val += children[i].getCellCount();
        }

        return ret_val;
    }

    /**
     * Print the contents of the tree.
     */
    protected void printTree() {
        String pad = getPadding();

        System.out.println(pad + "Cell: " + hashCode() + " level: " + level + " state: " + allState.getState() + " mat: " + allState.getMaterial() + " pos: " + vcx + " " + vcy + " " + vcz);
        if (children == null) {
            System.out.println(pad + " no children");

        } else {
            for(int i=0; i < children.length; i++) {
                if (children[i] == null) {
                    System.out.println(pad + " empty");
                } else {
                    children[i].printTree();
                }
            }
        }
    }

    private void createChild(int pos, byte state, int material) {
        int new_size = size / 2;
        int new_level = level + 1;

//System.out.println("Created Child, used pos: " + vcx + " " + vcy + " " + vcz + " new_size: " + new_size + " new_level: " + new_level);

        switch(pos) {
            case 0:
                children[0] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy,vcz,
                    new_size, state, material);
                break;
            case 1:
                children[1] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy + new_size,vcz,
                    new_size, state, material);
                break;
            case 2:
                children[2] = new OctreeCellInternalByte(new_level,maxLevel,vcx + new_size,vcy + new_size,vcz,
                    new_size, state, material);
                break;
            case 3:
                children[3] = new OctreeCellInternalByte(new_level,maxLevel,vcx + new_size,vcy,vcz,
                    new_size, state, material);
                break;
            case 4:
                children[4] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy,vcz + new_size,
                    new_size, state, material);
                break;
            case 5:
                children[5] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy + new_size,vcz + new_size,
                    new_size, state, material);
                break;
            case 6:
                children[6] = new OctreeCellInternalByte(new_level,maxLevel,vcx + new_size,vcy + new_size,vcz + new_size,
                    new_size, state, material);
                break;
            case 7:
                children[7] = new OctreeCellInternalByte(new_level,maxLevel,vcx + new_size,vcy,vcz + new_size,
                    new_size, state, material);
                break;
        }
    }

    /**
     * Split this cell into 8 parts.
     */
    private void splitOld(int pos, byte oldState, int oldMaterial) {
        children = new OctreeCellInternalByte[8];

        // local calced values
        int new_size = size / 2;
        int new_level = level + 1;
        byte state;
        byte material;

        children[0] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy,vcz,
            new_size, oldState, oldMaterial);
        children[1] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy + new_size,vcz,
            new_size, oldState, oldMaterial);
        children[2] = new OctreeCellInternalByte(new_level,maxLevel,vcx + new_size,vcy + new_size,vcz,
            new_size, oldState, oldMaterial);
        children[3] = new OctreeCellInternalByte(new_level,maxLevel,vcx + new_size,vcy,vcz,
            new_size, oldState, oldMaterial);
        children[4] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy,vcz + new_size,
            new_size, oldState, oldMaterial);
        children[5] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy + new_size,vcz + new_size,
            new_size, oldState, oldMaterial);
        children[6] = new OctreeCellInternalByte(new_level,maxLevel,vcx + new_size,vcy + new_size,vcz + new_size,
            new_size, oldState, oldMaterial);
        children[7] = new OctreeCellInternalByte(new_level,maxLevel,vcx + new_size,vcy,vcz + new_size,
            new_size, oldState, oldMaterial);
    }

    /**
     * Split this cell into 8 parts.
     */
    private void split(int pos, byte oldState, int oldMaterial) {
        children = new OctreeCellInternalByte[8];

        // local calced values
        int new_size = size / 2;
        int new_level = level + 1;

//System.out.println("Split, would have used pos: " + vcx + " " + vcy + " " + vcz + " new_size: " + new_size + " new_level: " + new_level);
        if (oldState == Grid.OUTSIDE) {

            switch(pos) {
                case 0:
                    children[0] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy,vcz,
                        new_size, oldState, oldMaterial);
                    break;
                case 1:
                    children[1] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy + new_size,vcz,
                        new_size, oldState, oldMaterial);
                    break;
                case 2:
                    children[2] = new OctreeCellInternalByte(new_level,maxLevel,vcx + new_size,vcy + new_size,vcz,
                        new_size, oldState, oldMaterial);
                    break;
                case 3:
                    children[3] = new OctreeCellInternalByte(new_level,maxLevel,vcx + new_size,vcy,vcz,
                        new_size, oldState, oldMaterial);
                    break;
                case 4:
                    children[4] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy,vcz + new_size,
                        new_size, oldState, oldMaterial);
                    break;
                case 5:
                    children[5] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy + new_size,vcz + new_size,
                        new_size, oldState, oldMaterial);
                    break;
                case 6:
                    children[6] = new OctreeCellInternalByte(new_level,maxLevel,vcx + new_size,vcy + new_size,vcz + new_size,
                        new_size, oldState, oldMaterial);
                    break;
                case 7:
                    children[7] = new OctreeCellInternalByte(new_level,maxLevel,vcx + new_size,vcy,vcz + new_size,
                        new_size, oldState, oldMaterial);
                    break;

                }

//System.out.println("pos: " + pos + " cell: " + children[pos].hashCode() + " orig: " + children[pos].vcx + " " + children[pos].vcy + " " + children[pos].vcz);
/*
            // let null denote OUTSIDE
            children[pos] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy,vcz,
                new_size, oldState, oldMaterial);
*/
        } else {
            children[0] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy,vcz,
                new_size, oldState, oldMaterial);
            children[1] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy + new_size,vcz,
                new_size, oldState, oldMaterial);
            children[2] = new OctreeCellInternalByte(new_level,maxLevel,vcx + new_size,vcy + new_size,vcz,
                new_size, oldState, oldMaterial);
            children[3] = new OctreeCellInternalByte(new_level,maxLevel,vcx + new_size,vcy,vcz,
                new_size, oldState, oldMaterial);
            children[4] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy,vcz + new_size,
                new_size, oldState, oldMaterial);
            children[5] = new OctreeCellInternalByte(new_level,maxLevel,vcx,vcy + new_size,vcz + new_size,
                new_size, oldState, oldMaterial);
            children[6] = new OctreeCellInternalByte(new_level,maxLevel,vcx + new_size,vcy + new_size,vcz + new_size,
                new_size, oldState, oldMaterial);
            children[7] = new OctreeCellInternalByte(new_level,maxLevel,vcx + new_size,vcy,vcz + new_size,
                new_size, oldState, oldMaterial);
        }
    }

    /**
     * Collapse the cell into 1 part.
     */
    private void collapse(byte state, int material, OctreeCellInternalByte parent) {

        boolean allSame = true;

        int len = children.length;

        for(int i=0; i < len; i++) {
            if (children[i] == null) {
                if (state != Grid.OUTSIDE) {
                    allSame = false;
                    break;
                }
            } else if (children[i].allState.getState() != state ||
                children[i].allState.getMaterial() != material) {

                allSame = false;
                break;
            }
        }

        if (!allSame)
            return;

        OctreeCellInternalByte cell = null;

        for(int i=0; i < len; i++) {
            if (children[i] != null) {
                cell = children[i];
                break;
            }
        }

        allState = cell.allState;
        children = null;

/*   // TODO: Not sure this is working right
System.out.println("Collapsed: level: " + level + " parent: " + parent);
        // Partial rollup of collapse, would need full path to completely roll up
        if (parent != null) {
System.out.println("Going to parent: " + parent);
            parent.collapse(state, material, null);
        }
*/
    }

    /**
     * Find the octant of a voxel.  Numbered clockwise with 0 in lower left corner.
     *
     * @param x The x voxel coordinate
     * @param y The y voxel coordinate
     * @param z The z voxel coordinate
     *
     * @return The octant or -1 if not found
     */
    protected int findOctant(int x, int y, int z) {


        int hsize = size / 2;

        int dx = (x - vcx) / hsize;
        int dy = (y - vcy) / hsize;
        int dz = (z - vcz) / hsize;

//System.out.println("Find Octant: size: " + size + " hsize: " + hsize + " x: " + x + " " + y + " " + z);
//System.out.println("  orig: " + vcx + " " + vcy + " " + vcz);
//System.out.println("   dx: " + dx + " dy: " + dy + " dz: " + dz);

        int ret_val = -1;

        if (dx == 0) {
            if (dy == 0) {
                if (dz == 0) {
                    ret_val = 0;
                } else {
                    ret_val = 4;
                }
            } else {
                if (dz == 0) {
                    ret_val = 1;
                } else {
                    ret_val = 5;
                }
            }
        } else {
            if (dy == 0) {
                if (dz == 0) {
                    ret_val = 3;
                } else {
                    ret_val = 7;
                }
            } else {
                if (dz == 0) {
                    ret_val = 2;
                } else {
                    ret_val = 6;
                }
            }
        }

//System.out.println("   ret oct: " + ret_val);
        return ret_val;
    }


    /**
     * Find the octant of a voxel.  Numbered clockwise with 0 in lower left corner.
     *
     * @param x The x voxel coordinate
     * @param y The y voxel coordinate
     * @param z The z voxel coordinate
     *
     * @return The octant or -1 if not found
     */
    protected int findOctantOld(int x, int y, int z) {
        int dx = x - vcx;
        int dy = y - vcy;
        int dz = z - vcz;
        int hsize = size / 2;

System.out.println("Find Octant: size: " + size + " hsize: " + hsize + " x: " + x + " " + y + " " + z);
System.out.println("  orig: " + vcx + " " + vcy + " " + vcz);
System.out.println("   dx: " + dx + " dy: " + dy + " dz: " + dz);

        int ret_val = -1;

// TODO: Check not really necessary?
        if (dx > hsize || dy > hsize || dz > hsize) {
System.out.println("***Outside octant");
            return -1;
        }

        if (dx < hsize) {
            if (dy < hsize) {
                if (dz < hsize) {
                    ret_val = 0;
                } else {
                    ret_val = 4;
                }
            } else {
                if (dz < hsize) {
                    ret_val = 1;
                } else {
                    ret_val = 5;
                }
            }
        } else {
            if (dy < hsize) {
                if (dz < hsize) {
                    ret_val = 3;
                } else {
                    ret_val = 7;
                }
            } else {
                if (dz < hsize) {
                    ret_val = 2;
                } else {
                    ret_val = 6;
                }
            }
        }

System.out.println("   ret oct: " + ret_val);
        return ret_val;
    }

    /**
     * Clone the object.
     */
    public Object clone() {
        OctreeCellInternalByte ret_val = new OctreeCellInternalByte();
        ret_val.vcx = vcx;
        ret_val.vcy = vcy;
        ret_val.vcz = vcz;
        ret_val.size = size;
        ret_val.level = level;
        ret_val.maxLevel = maxLevel;

        if (children != null) {
            int len = children.length;
            ret_val.children = new OctreeCellInternalByte[len];
            for(int i=0; i < len; i++) {
                ret_val.children[i] = (OctreeCellInternalByte) ((OctreeCellInternalByte)children[i]).clone();
            }
        }

        ret_val.allState = (VoxelDataByte) allState.clone();

        return ret_val;
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    protected void find(Grid.VoxelClasses vc, ClassTraverser t) {
        if (allState.getState() == MIXED) {
            int len = children.length;

            for(int i=0; i < len; i++) {
                if (children[i] != null) {
                    children[i].find(vc, t);
                } else {
                    // TODO: I think this is right
                    switch(vc) {
                        case ALL:
                            t.found(vcx,vcy,vcz, BaseAttributeGrid.EMPTY_VOXEL.getState());
                            break;
                        case OUTSIDE:
                            t.found(vcx,vcy,vcz, BaseAttributeGrid.EMPTY_VOXEL.getState());
                            break;
                    }
                }
            }
        } else {
            byte state;

            if (level == maxLevel) {
//System.out.println(pad + " TERM.  State: " + allState.getState());
                switch(vc) {
                    case ALL:
                        t.found(vcx,vcy,vcz,allState.getState());
                        break;
                    case MARKED:
                        state = allState.getState();
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            t.found(vcx,vcy,vcz,allState.getState());
                        }
                        break;
                    case EXTERIOR:
                        state = allState.getState();
                        if (state == Grid.EXTERIOR) {
                            t.found(vcx,vcy,vcz,allState.getState());
                        }
                        break;
                    case INTERIOR:
                        state = allState.getState();
                        if (state == Grid.INTERIOR) {
                            t.found(vcx,vcy,vcz,allState.getState());
                        }
                        break;
                    case OUTSIDE:
                        state = allState.getState();
                        if (state == Grid.OUTSIDE) {
                            t.found(vcx,vcy,vcz,allState.getState());
                        }
                        break;
                }
            } else {
//System.out.println(pad + " ALL: " + allState.getState() + " size: " + size + " level: " + level + " max_level: " + maxLevel);
                // TODO: Not sure why this works might be dodgy
                if (level > 2) {
                    switch(vc) {
                        case ALL:
                            for(int i=0; i < size; i++) {
                                for(int j=0; j < size; j++) {
                                    for(int k=0; k < size; k++) {
                                        t.found(vcx + i,vcy + j,vcz + k,allState.getState());
                                    }
                                }
                            }
                            break;
                        case MARKED:
                            state = allState.getState();
                            if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                for(int i=0; i < size; i++) {
                                    for(int j=0; j < size; j++) {
                                        for(int k=0; k < size; k++) {
                                            t.found(vcx + i,vcy + j,vcz + k,allState.getState());
                                        }
                                    }
                                }
                            }
                            break;
                        case EXTERIOR:
                            state = allState.getState();
                            if (state == Grid.EXTERIOR) {
                                for(int i=0; i < size; i++) {
                                    for(int j=0; j < size; j++) {
                                        for(int k=0; k < size; k++) {
                                            t.found(vcx + i,vcy + j,vcz + k,allState.getState());
                                        }
                                    }
                                }
                            }
                            break;
                        case INTERIOR:
                            state = allState.getState();
                            if (state == Grid.INTERIOR) {
                                for(int i=0; i < size; i++) {
                                    for(int j=0; j < size; j++) {
                                        for(int k=0; k < size; k++) {
                                            t.found(vcx + i,vcy + j,vcz + k,allState.getState());
                                        }
                                    }
                                }
                            }
                            break;
                        case OUTSIDE:
                            state = allState.getState();
                            if (state == Grid.OUTSIDE) {
                                for(int i=0; i < size; i++) {
                                    for(int j=0; j < size; j++) {
                                        for(int k=0; k < size; k++) {
                                            t.found(vcx + i,vcy + j,vcz + k,allState.getState());
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
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    protected void findInterruptible(Grid.VoxelClasses vc, ClassTraverser t) {
        //String pad = getPadding();

        if (allState.getState() == MIXED) {
            int len = children.length;

            for(int i=0; i < len; i++) {
                if (children[i] != null) {
                    children[i].find(vc, t);
                } else {
                    // TODO: I think this is right
                    switch(vc) {
                        case ALL:
                            t.found(vcx,vcy,vcz, BaseAttributeGrid.EMPTY_VOXEL.getState());
                            break;
                        case OUTSIDE:
                            t.found(vcx,vcy,vcz, BaseAttributeGrid.EMPTY_VOXEL.getState());
                            break;
                    }
                }
            }
        } else {
            byte state;

            if (level == maxLevel) {
//System.out.println(pad + " TERM.  State: " + allState.getState());
                switch(vc) {
                    case ALL:
                        t.found(vcx,vcy,vcz,allState.getState());
                        break;
                    case MARKED:
                        state = allState.getState();
                        if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                            t.found(vcx,vcy,vcz,allState.getState());
                        }
                        break;
                    case EXTERIOR:
                        state = allState.getState();
                        if (state == Grid.EXTERIOR) {
                            t.found(vcx,vcy,vcz,allState.getState());
                        }
                        break;
                    case INTERIOR:
                        state = allState.getState();
                        if (state == Grid.INTERIOR) {
                            t.found(vcx,vcy,vcz,allState.getState());
                        }
                        break;
                    case OUTSIDE:
                        state = allState.getState();
                        if (state == Grid.OUTSIDE) {
                            t.found(vcx,vcy,vcz,allState.getState());
                        }
                        break;
                }
            } else {
//System.out.println(pad + " ALL: " + allState.getState() + " size: " + size + " level: " + level + " max_level: " + maxLevel);
                // TODO: Not sure why this works might be dodgy
                if (level > 2) {
                    switch(vc) {
                        case ALL:
                            for(int i=0; i < size; i++) {
                                for(int j=0; j < size; j++) {
                                    for(int k=0; k < size; k++) {
                                        t.found(vcx + i,vcy + j,vcz + k,allState.getState());
                                    }
                                }
                            }
                            break;
                        case MARKED:
                            state = allState.getState();
                            if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                for(int i=0; i < size; i++) {
                                    for(int j=0; j < size; j++) {
                                        for(int k=0; k < size; k++) {
                                            t.found(vcx + i,vcy + j,vcz + k,allState.getState());
                                        }
                                    }
                                }
                            }
                            break;
                        case EXTERIOR:
                            state = allState.getState();
                            if (state == Grid.EXTERIOR) {
                                for(int i=0; i < size; i++) {
                                    for(int j=0; j < size; j++) {
                                        for(int k=0; k < size; k++) {
                                            t.found(vcx + i,vcy + j,vcz + k,allState.getState());
                                        }
                                    }
                                }
                            }
                            break;
                        case INTERIOR:
                            state = allState.getState();
                            if (state == Grid.INTERIOR) {
                                for(int i=0; i < size; i++) {
                                    for(int j=0; j < size; j++) {
                                        for(int k=0; k < size; k++) {
                                            t.found(vcx + i,vcy + j,vcz + k,allState.getState());
                                        }
                                    }
                                }
                            }
                            break;
                        case OUTSIDE:
                            state = allState.getState();
                            if (state == Grid.OUTSIDE) {
                                for(int i=0; i < size; i++) {
                                    for(int j=0; j < size; j++) {
                                        for(int k=0; k < size; k++) {
                                            t.found(vcx + i,vcy + j,vcz + k,allState.getState());
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
     * Get a padded string for printing levels.
     */
    private String getPadding() {
        String pad = "";

        for(int i=0; i < level; i++) {
            pad = pad + "   ";
        }
        return pad;
    }
}