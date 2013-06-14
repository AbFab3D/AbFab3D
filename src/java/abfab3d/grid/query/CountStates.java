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

import abfab3d.grid.*;
import abfab3d.grid.Grid.VoxelClasses;

import java.util.HashMap;
import java.util.Map;

/**
 * Counts the number of voxels of each state
 *
 * @author Alan Hudson
 */
public class CountStates implements ClassTraverser {
    private long inside;

    public CountStates() {
    }

    /**
     * Counts the number of materials present
     *
     * @param grid The grid to use for grid src
     * @return Material counts
     */
    public Map<Byte,Long> execute(Grid grid) {
        grid.find(VoxelClasses.INSIDE, this);

        Map<Byte,Long> ret_val = new HashMap<Byte, Long>();
        ret_val.put(new Byte(Grid.INSIDE), inside);
        long tot_voxels = (long) grid.getWidth() * grid.getHeight() * grid.getDepth();
        ret_val.put(new Byte(Grid.OUTSIDE), tot_voxels - inside);

        return ret_val;
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param state The voxel data
     */
    public void found(int x, int y, int z, byte state) {
        switch(state) {
            case Grid.INSIDE:
                inside++;
                break;
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
     * @param state The voxel data
     */
    public boolean foundInterruptible(int x, int y, int z, byte state) {
        return true;
    }

}
