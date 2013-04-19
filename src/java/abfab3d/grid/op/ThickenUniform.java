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

package abfab3d.grid.op;

// External Imports

import abfab3d.grid.*;

import java.util.HashSet;
import java.util.Iterator;

// Internal Imports

/**
 * Thicken an objects exterior by expanding each exterior voxel
 * in all directions.  The object will grow in size.  See
 * ThickenInward for a size preserving thicken.
 *
 * @author Alan Hudson
 */
public class ThickenUniform implements Operation, AttributeOperation {
    /**
     * The material to use for new voxels
     */
    private int material;

    public ThickenUniform(int material) {
        this.material = material;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid A.
     * @return The new grid
     */
    public Grid execute(Grid grid) {
        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        Grid ret_val = grid.createEmpty(width + 1, height + 1, depth + 1,
                grid.getVoxelSize(), grid.getSliceHeight());

        //Copy
        // Guess that 1% of all voxels will be exterior
        int ext_guess = (int) Math.ceil(height * width * depth * 0.01f);

        HashSet<VoxelCoordinate> ext_voxels = new HashSet<VoxelCoordinate>(ext_guess);

        // Generate set of voxels to thicken

        VoxelData vd = grid.getVoxelData();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    grid.getData(x, y, z, vd);

                    byte state = vd.getState();

                    if (state == Grid.EXTERIOR) {
                        ext_voxels.add(new VoxelCoordinate(x, y, z));
                    }
                }
            }
        }

        Iterator<VoxelCoordinate> itr = ext_voxels.iterator();
        int x, y, z;

        while (itr.hasNext()) {
            VoxelCoordinate vc = itr.next();

            x = vc.getX();
            y = vc.getY();
            z = vc.getZ();

            // Vist all 27 neigbors and make them exterior voxels
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    for (int k = -1; k < 2; k++) {
                        grid.setState(x + i, y + j, z + k, Grid.EXTERIOR);
                    }
                }
            }
        }

        return grid;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid A.
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid grid) {
        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        Grid ret_val = grid.createEmpty(width + 1, height + 1, depth + 1,
                grid.getVoxelSize(), grid.getSliceHeight());

        //Copy
        // Guess that 1% of all voxels will be exterior
        int ext_guess = (int) Math.ceil(height * width * depth * 0.01f);

        HashSet<VoxelCoordinate> ext_voxels = new HashSet<VoxelCoordinate>(ext_guess);

        // Generate set of voxels to thicken

        VoxelData vd = grid.getVoxelData();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    grid.getData(x, y, z, vd);

                    byte state = vd.getState();

                    if (state == Grid.EXTERIOR) {
                        ext_voxels.add(new VoxelCoordinate(x, y, z));
                    }
                }
            }
        }

        Iterator<VoxelCoordinate> itr = ext_voxels.iterator();
        int x, y, z;

        while (itr.hasNext()) {
            VoxelCoordinate vc = itr.next();

            x = vc.getX();
            y = vc.getY();
            z = vc.getZ();

            // Vist all 27 neigbors and make them exterior voxels
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    for (int k = -1; k < 2; k++) {
                        grid.setData(x + i, y + j, z + k, Grid.EXTERIOR, material);
                    }
                }
            }
        }

        return grid;
    }
}
