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

// Internal Imports
import abfab3d.grid.*;

/**
 * Erode an object based on the erosion morphology technique.  The object should
 * decrease in size after erosion.  The eroding element is a cube.
 *
 * @author Tony Wong
 */
public class ErosionCube implements Operation, AttributeOperation {

    /** The distance from a voxel to erode */
    private int distance;

    public ErosionCube(int distance) {
        this.distance = distance;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid A.
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid grid) {

        // Nothing to do if distance is 0
        if (distance == 0) {
            return grid;
        }

        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        // Create an empty copy of the grid
        // TODO: Decide on whether we should decrease the size of the
        // eroded grid by the erosion distance
        //        Grid erodedGrid = grid.createEmpty(width, heigt, depth,
        //            grid.getVoxelSize(), grid.getSliceHeight());
        AttributeGrid erodedGrid = (AttributeGrid) grid.createEmpty(width - 2 * distance,
                                                                    height - 2 * distance,
                                                                    depth - 2 * distance,
                                                                    grid.getVoxelSize(),
                                                                    grid.getSliceHeight());

        // Voxels less than the distance from the grid edge can be ignored
        // A cube of length "distance" will never fit into the grid at those
        // voxel coordinates, so they will always be eroded away
        int xStart = 0 + distance;
        int xEnd = width - distance;
        int yStart = 0 + distance;
        int yEnd = height - distance;
        int zStart = 0 + distance;
        int zEnd = depth - distance;

        // Loop through grid and check each voxel for erosion
        // If the voxel cannot be eroded (safe), mark the voxel in the grid copy
        for(int y=yStart; y < yEnd; y++) {
            for(int x=xStart; x < xEnd; x++) {
                for(int z=zStart; z < zEnd; z++) {
                    byte state = grid.getState(x, y, z);

                    if (state != Grid.OUTSIDE) {
                        boolean safe = checkErosion(grid, x, y, z);

                        if (safe) {
                            long mat = grid.getAttribute(x, y, z);

                            // TODO: Decide on whether we should decrease the size of the
                            // eroded grid by the erosion distance
//                            erodedGrid.setData(x, y, z, state, mat);
                            erodedGrid.setData(x-distance, y-distance, z-distance, state, mat);
                        }
                    }
                }
            }
        }

        return erodedGrid;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid A.
     * @return The new grid
     */
    public Grid execute(Grid grid) {

        // Nothing to do if distance is 0
        if (distance == 0) {
            return grid;
        }

        int height = grid.getHeight();
        int width = grid.getWidth();
        int depth = grid.getDepth();

        // Create an empty copy of the grid
        // TODO: Decide on whether we should decrease the size of the
        // eroded grid by the erosion distance
//        Grid erodedGrid = grid.createEmpty(width, height, depth,
//            grid.getVoxelSize(), grid.getSliceHeight());
        Grid erodedGrid = grid.createEmpty(width - 2 * distance,
                                           height - 2 * distance,
                                           depth - 2 * distance,
                                           grid.getVoxelSize(),
                                           grid.getSliceHeight());

        // Voxels less than the distance from the grid edge can be ignored
        // A cube of length "distance" will never fit into the grid at those
        // voxel coordinates, so they will always be eroded away
        int xStart = 0 + distance;
        int xEnd = width - distance;
        int yStart = 0 + distance;
        int yEnd = height - distance;
        int zStart = 0 + distance;
        int zEnd = depth - distance;

        // Loop through grid and check each voxel for erosion
        // If the voxel cannot be eroded (safe), mark the voxel in the grid copy
        for(int y=yStart; y < yEnd; y++) {
            for(int x=xStart; x < xEnd; x++) {
                for(int z=zStart; z < zEnd; z++) {
                    byte state = grid.getState(x, y, z);

                    if (state != Grid.OUTSIDE) {
                        boolean safe = checkErosion(grid, x, y, z);

                        if (safe) {
                            // TODO: Decide on whether we should decrease the size of the
                            // eroded grid by the erosion distance
//                            erodedGrid.setData(x, y, z, state, mat);
                            erodedGrid.setState(x-distance, y-distance, z-distance, state);
                        }
                    }
                }
            }
        }

        return erodedGrid;
    }

    private boolean checkErosion(Grid grid, int xPos, int yPos, int zPos) {
        int xStart = xPos - distance;
        int xEnd = xPos + distance;
        int yStart = yPos - distance;
        int yEnd = yPos + distance;
        int zStart = zPos - distance;
        int zEnd = zPos + distance;

        for (int y=yStart; y<=yEnd; y++) {
            for (int x=xStart; x<=xEnd; x++) {
                for (int z=zStart; z<=zEnd; z++) {
                    if (grid.getState(x, y, z) == Grid.OUTSIDE) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}



