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
import abfab3d.grid.VoxelClasses;
import abfab3d.path.Path;

/**
 * Counts the number of materials in a grid.
 *
 * @author Alan Hudson
 */
public class CountMaterials implements ClassAttributeTraverser {
    /** The material count */
    private int count;

    /** The materials seen */
    private HashMap<Long,Long> seen;

    public CountMaterials() {
    }

    /**
     * Counts the number of materials present
     *
     * @param grid The grid to use for grid src
     * @return Material counts
     */
    public Map<Long,Long> execute(Grid grid) {
        seen = new HashMap<Long,Long>();

        ((AttributeGrid)grid).findAttribute(VoxelClasses.INSIDE,this);

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
        Long i = new Long(start.getMaterial());

        Long cnt = seen.get(i);

        if (cnt == null) {
            seen.put(i, new Long(1));
        } else {
            cnt = new Long(cnt.longValue() + 1);
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

    public String printResults(Map<Long, Long> results) {
        StringBuilder ret_val = new StringBuilder();

        for(Map.Entry<Long, Long> entry : results.entrySet()) {
            ret_val.append("MAT: " + entry.getKey() + " Cnt: " + entry.getValue() + "\n");
        }
        return ret_val.toString();
    }

}
