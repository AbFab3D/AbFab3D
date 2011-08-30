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

package abfab3d.grid.query;

// External Imports

// Internal Imports
import java.util.*;

import abfab3d.grid.*;
import abfab3d.grid.Grid.VoxelClasses;
import abfab3d.path.Path;

/**
 * Counts the number of materials in a grid.
 *
 * @author Alan Hudson
 */
public class CountMaterials implements ClassTraverser {
    /** The grid we are using */
    private Grid grid;

    /** The material count */
    private int count;

    /** The materials seen */
    private HashMap<Integer,Integer> seen;

    public CountMaterials() {
    }

    /**
     * Counts the number of materials present
     *
     * @param grid The grid to use for grid src
     * @return Material counts
     */
    public Map<Integer,Integer> execute(Grid grid) {
        seen = new HashMap<Integer,Integer>();

        grid.find(VoxelClasses.MARKED,this);

        return seen;
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param start The voxel data
     */
    public void found(int x, int y, int z, VoxelData start) {
        Integer i = new Integer(start.getMaterial());

        Integer cnt = seen.get(i);

        if (cnt == null) {
            seen.put(i, new Integer(1));
        } else {
            cnt = new Integer(cnt.intValue() + 1);
            seen.put(i, cnt);
        }
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param start The voxel data
     */
    public boolean foundInterruptible(int x, int y, int z, VoxelData start) {
        return true;
    }

}
