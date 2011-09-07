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
 * A cell in an Octree structure.
 *
 *
 * @author Alan Hudson
 */
public interface OctreeCell {
    public static final byte MIXED = Grid.USER_DEFINED;

    /**
     * Get the children of this cell.
     *
     * @return The children
     */
    public OctreeCell[] getChildren();

    /**
     * Get the state of the voxel.  If its not MIXED then all cells below
     * this are also this value.
     *
     * @return The voxel state
     */
    public byte getState();

    /**
     * Get the origin and size of this cell in voxel coordinates.
     *
     * @param origin The origin, preallocated to 3
     * @param size The size, preallocated to 3
     */
    public void getRegion(int[] origin, int[] size);
}