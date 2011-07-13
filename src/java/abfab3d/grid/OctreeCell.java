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
    /**
     * Get the data located at the specified cell in voxel coordinates.
     *
     * @param x The x voxel coordinate
     * @param y The y voxel coordinate
     * @param z The z voxel coordinate
     *
     * @return The voxel data
     */
    public VoxelData getData(int x, int y, int z);
}