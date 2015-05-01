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

import abfab3d.util.Bounds;

import java.io.Serializable;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
// External Imports

/**
 * Base class implementation of Grids.  Includes common code that
 * may get overwritten by faster implementations.
 *
 * Likely better performance for memory access that is not slice aligned.
 *
 * Uses the X3D coordinate system.  Y-up.  Grid is located
 * on positive right side octant.
 *
 * @author Alan Hudson
 */
public abstract class BaseAttributeGrid extends BaseGrid implements AttributeGrid, Cloneable, Serializable {

    protected InsideOutsideFunc ioFunc;

    // attribute descriptor used for this grid
    protected AttributeDesc m_attributeDesc = AttributeDesc.getDefaultAttributeDesc(8);

    /**
     * Constructor.
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public BaseAttributeGrid(int w, int h, int d, double pixel, double sheight, InsideOutsideFunc ioFunc) {
        super(w,h,d,pixel,sheight);

        if (ioFunc == null) {
            this.ioFunc = new DefaultInsideOutsideFunc();
        } else {
            this.ioFunc = ioFunc;
        }
    }
    
    
    /**
     * Constructor.
     *
     * @param bounds the bounds of the grid
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public BaseAttributeGrid(Bounds bounds, double pixel, double sheight) {
        super(bounds, pixel,sheight);

        if (ioFunc == null) {
            this.ioFunc = new DefaultInsideOutsideFunc();
        } else {
            this.ioFunc = ioFunc;
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

        VoxelData vd = getVoxelData();

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {

                    getData(x,y,z,vd);

                    if (vd.getMaterial() == mat && vd.getState() != Grid.OUTSIDE) {
                        t.found(x,y,z,vd);
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
     * @param t The traverser to call for each voxel
     */
    public void findAttribute(VoxelClasses vc, long mat, ClassAttributeTraverser t) {

        VoxelData vd = getVoxelData();

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    getData(x,y,z,vd);

                    if (vd.getMaterial() != mat) {
                        continue;
                    }

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
        VoxelData vd = getVoxelData();

        loop:
        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    getData(x,y,z,vd);

                    if (vd.getMaterial() == mat && vd.getState() != Grid.OUTSIDE) {
                        if (!t.foundInterruptible(x,y,z,vd))
                            break loop;
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
    public void findAttributeInterruptible(VoxelClasses vc, long mat, ClassAttributeTraverser t) {
        VoxelData vd = getVoxelData();

        loop:
        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    getData(x,y,z,vd);

                    if (vd.getMaterial() != mat) {
                        continue;
                    }

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
        }
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

        VoxelData vd = getVoxelData();

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    getData(x,y,z,vd);

                    if (vd.getMaterial() == mat && vd.getState() != Grid.OUTSIDE) {
                        ret_val++;
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
    public void findAttribute(VoxelClasses vc, ClassAttributeTraverser t) {
        VoxelData vd = getVoxelData();

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    getData(x,y,z,vd);

                    byte state;

                    switch(vc) {
                        case ALL:
                            t.found(x,y,z,vd);
                            break;
                        case INSIDE:
                            state = vd.getState();
                            if (state == Grid.INSIDE) {
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
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttributeInterruptible(VoxelClasses vc, ClassAttributeTraverser t) {
        VoxelData vd = getVoxelData();

        loop:
        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    getData(x,y,z,vd);
                    byte state = getState(x,y,z);

                    switch(vc) {
                        case ALL:
                            if (!t.foundInterruptible(x,y,z,vd))
                                break loop;
                            break;
                        case INSIDE:
                            if (state == Grid.INSIDE) {
                                if (!t.foundInterruptible(x,y,z,vd))
                                    break loop;
                            }
                            break;
                        case OUTSIDE:
                            if (state == Grid.OUTSIDE) {
                                if (!t.foundInterruptible(x,y,z,vd))
                                    break loop;
                            }
                            break;
                    }
                }
            }
        }
    }

    /**
     * Traverse a class of voxels types over given rectangle in xy plane.
     * May be much faster then full grid traversal for some implementations.
     *
     * @param vc   The class of voxels to traverse
     * @param t    The traverer to call for each voxel
     * @param xmin - minimal x - coordinate of voxels
     * @param xmax - maximal x - coordinate of voxels
     * @param ymin - minimal y - coordinate of voxels
     * @param ymax - maximal y - coordinate of voxels
     */
    public void findAttribute(VoxelClasses vc, ClassAttributeTraverser t, int xmin, int xmax, int ymin, int ymax) {
        VoxelData vd = getVoxelData();

        switch (vc) {
            case ALL:
                for (int y = ymin; y <= ymax; y++) {
                    for (int x = xmin; x <= xmax; x++) {
                        for (int z = 0; z < depth; z++) {
                            getData(x, y, z, vd);
                            t.found(x, y, z, vd);
                        }
                    }
                }
                break;
            case INSIDE:
                for (int y = ymin; y <= ymax; y++) {
                    for (int x = xmin; x <= xmax; x++) {
                        for (int z = 0; z < depth; z++) {
                            byte state = getState(x, y, z);

                            if (state == Grid.INSIDE) {
                                getData(x, y, z,vd);
                                t.found(x, y, z, vd);
                            }
                        }
                    }
                }
                break;
            case OUTSIDE:
                for (int y = ymin; y <= ymax; y++) {
                    for (int x = xmin; x <= xmax; x++) {
                        for (int z = 0; z < depth; z++) {
                            byte state = getState(x, y, z);

                            if (state == Grid.OUTSIDE) {
                                getData(x, y, z,vd);
                                t.found(x, y, z, vd);
                            }
                        }
                    }
                }
                break;
        }
    }

    /**
     * Reassign a group of materials to a new materialID
     *
     * @param materials The new list of materials
     */
    public void reassignAttribute(final long[] materials, long matID) {
        // assume unindexed if we got here.  Best to traverse
        // whole structure

        final int len = materials.length;
        VoxelData vd = getVoxelData();

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    getData(x,y,z,vd);

                    long mat;
                    byte state = vd.getState();

                    if (state != Grid.OUTSIDE) {
                        mat = vd.getMaterial();

                        for(int i=0; i < len; i++) {
                            if (mat == materials[i]) {
                                setData(x,y,z, state, matID);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Remove all voxels associated with the Material.
     *
     * @param mat The aterialID
     */
    public void removeAttribute(long mat) {
        VoxelData vd = getVoxelData();

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    getData(x,y,z,vd);

                    if (vd.getMaterial() == mat && vd.getState() != Grid.OUTSIDE) {
                        setData(x,y,z, Grid.OUTSIDE, 0);
                    }
                }
            }
        }
    }

    /**
     * Print out a slice of data.
     */
    public String toStringSlice(int y) {
        StringBuilder sb = new StringBuilder();

        for(int z=depth-1; z >= 0; z--) {
            for(int x=0; x < width; x++) {
                sb.append(getState(x,y,z));
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

    /**
       print given crossection of attributes
     */
    public String toStringAttributesSectionZ(int z) {

        StringBuilder sb = new StringBuilder();

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                long att = getAttribute(x, y, z);
                sb.append((char)('A' + att));
            }
            sb.append('\n');
        }

        return sb.toString();
    }


    public abstract Object clone();



    /**
       assign to the grid a description of a voxel attributes
       @param description The attirbute description 
       @override 
    */
    public void setAttributeDesc(AttributeDesc description){
        m_attributeDesc = description;
    }

    /**
       @return voxel attribute description assigned to the grid
       @override 
    */
    public AttributeDesc getAttributeDesc(){
        return m_attributeDesc; 
    }


    /**
       deprecated method 
     */
    public void setStateWorld(double x, double y, double z, byte state) {
        setState((int)((x-xorig) / pixelSize), 
                 (int)((y-yorig) / sheight),
                 (int)((z-zorig) / pixelSize),
                 state);
    }
    
    /**
       deprecated method 
     */
    public void setDataWorld(double x, double y, double z, byte state, long attribute) {
        setData((int)((x-xorig) / pixelSize), 
                (int)((y-yorig) / sheight),
                (int)((z-zorig) / pixelSize),
                state,
                attribute);
        
    }

    /**
     */
    public long getAttributeWorld(double x, double y, double z) {
        return getAttribute((int)((x-xorig) / pixelSize), 
                            (int)((y-yorig) / sheight),
                            (int)((z-zorig) / pixelSize)); 

    }
    /**
     */
    public void setAttributeWorld(double x, double y, double z, long attribute) {
        setAttribute((int)((x-xorig) / pixelSize), 
                     (int)((y-yorig) / sheight),
                     (int)((z-zorig) / pixelSize), attribute); 
        
    }

    /**
       deprecated method 
     */
    public byte getStateWorld(double x, double y, double z) {
        return getState((int)((x-xorig) / pixelSize), 
                        (int)((y-yorig) / sheight),
                        (int)((z-zorig) / pixelSize));         
    }

    /**
       deprecated method 
     */
    public void getDataWorld(double x, double y, double z, VoxelData vd) {
        getData((int)((x-xorig) / pixelSize), 
                 (int)((y-yorig) / sheight),
                 (int)((z-zorig) / pixelSize),
                 vd);         
        
    }
    /**
     */
    public void getData(int x, int y, int z, VoxelData vd) {
        throw new RuntimeException(fmt("getData() not implemented in %s", this));
    }
    /**
     */
    public byte getState(int x, int y, int z) {
        throw new RuntimeException(fmt("getState() not implemented in %s",this));        
    }

    /**
     */
    public void setData(int x, int y, int z, byte state, long material) {
        throw new RuntimeException(fmt("setData() not implemented in %s",this));        
    }

    /**
     */
    public void setState(int x, int y, int z, byte state) {
        throw new RuntimeException(fmt("setState() not implemented n %s",this));                
    }

    /**
     *
     */
    public VoxelData getVoxelData() {
        throw new RuntimeException(fmt("getVoxelData() not implemented in %s", this));
    }

}

