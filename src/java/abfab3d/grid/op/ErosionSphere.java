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
import abfab3d.util.MathUtil;

/**
 * Erode an object based on the erosion morphology technique.  The object should
 * decrease in size after erosion.  The eroding element is a sphere.
 *
 * @author Tony Wong
 */
public class ErosionSphere implements Operation, AttributeOperation {

    /** The distance from a voxel to erode */
    private int radius;

    public ErosionSphere(int radius) {
        this.radius = radius;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid A.
     * @return The new grid
     */
    public Grid execute(Grid dest) {

        // Nothing to do if radius is 0
        if (radius == 0) {
            return dest;
        }

        int height = dest.getHeight();
        int width = dest.getWidth();
        int depth = dest.getDepth();

        // Create an empty copy of the grid
        // TODO: Decide on whether we should decrease the size of the
        // eroded grid by the erosion distance
//        Grid erodedGrid = grid.createEmpty(width, height, depth,
//            grid.getVoxelSize(), grid.getSliceHeight());
        Grid erodedGrid = dest.createEmpty(width - 2 * radius,
                                           height - 2 * radius,
                                           depth - 2 * radius,
                                           dest.getVoxelSize(),
                                           dest.getSliceHeight());

        // Voxels less than the radius from the grid edge can be ignored
        // A sphere of radius "radius" will never fit into the grid at
        // those voxel coordinates, so they will always be eroded away
        int xStart = 0 + radius;
        int xEnd = width - radius;
        int yStart = 0 + radius;
        int yEnd = height - radius;
        int zStart = 0 + radius;
        int zEnd = depth - radius;

        // Loop through grid and check each voxel for erosion
        // If the voxel cannot be eroded (safe), mark the voxel in the grid copy
        for(int y=yStart; y < yEnd; y++) {
            for(int x=xStart; x < xEnd; x++) {
                for(int z=zStart; z < zEnd; z++) {
                    byte state = dest.getState(x, y, z);

                    if (state != Grid.OUTSIDE) {
                        boolean safe = checkErosion(dest, x, y, z);

                        if (safe) {
                            // TODO: Decide on whether we should decrease the size of the
                            // eroded grid by the erosion distance
//                            erodedGrid.setData(x, y, z, state, mat);
                            erodedGrid.setState(x-radius, y-radius, z-radius, state);
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
     * @param dest The grid to use for grid A.
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid dest) {

        // Nothing to do if radius is 0
        if (radius == 0) {
            return dest;
        }

        int height = dest.getHeight();
        int width = dest.getWidth();
        int depth = dest.getDepth();

        // Create an empty copy of the grid
        // TODO: Decide on whether we should decrease the size of the
        // eroded grid by the erosion distance
//        Grid erodedGrid = grid.createEmpty(width, depth, height,
//            grid.getVoxelSize(), grid.getSliceHeight());
        AttributeGrid erodedGrid = (AttributeGrid) dest.createEmpty(width - 2 * radius,
                                                                    height - 2 * radius,
                                                                    depth - 2 * radius,
                                                                    dest.getVoxelSize(),
                                                                    dest.getSliceHeight());

        // Voxels less than the radius from the grid edge can be ignored
        // A sphere of radius "radius" will never fit into the grid at
        // those voxel coordinates, so they will always be eroded away
        int xStart = 0 + radius;
        int xEnd = width - radius;
        int yStart = 0 + radius;
        int yEnd = height - radius;
        int zStart = 0 + radius;
        int zEnd = depth - radius;

        // Loop through grid and check each voxel for erosion
        // If the voxel cannot be eroded (safe), mark the voxel in the grid copy
        for(int y=yStart; y < yEnd; y++) {
            for(int x=xStart; x < xEnd; x++) {
                for(int z=zStart; z < zEnd; z++) {
                    byte state = dest.getState(x, y, z);

                    if (state != Grid.OUTSIDE) {
                        boolean safe = checkErosion(dest, x, y, z);

                        if (safe) {
                            long mat = dest.getAttribute(x, y, z);

                            // TODO: Decide on whether we should decrease the size of the
                            // eroded grid by the erosion distance
//                            erodedGrid.setData(x, y, z, state, mat);
                            erodedGrid.setData(x-radius, y-radius, z-radius, state, mat);
                        }
                    }
                }
            }
        }

        return erodedGrid;
    }

    private boolean checkErosion(Grid grid, int xPos, int yPos, int zPos) {
        int[] origin = {xPos, yPos, zPos};

        int xStart = xPos - radius;
        int xEnd = xPos + radius;
        int yStart = yPos - radius;
        int yEnd = yPos + radius;
        int zStart = zPos - radius;
        int zEnd = zPos + radius;

        for (int y=yStart; y<=yEnd; y++) {
            for (int x=xStart; x<=xEnd; x++) {
                for (int z=zStart; z<=zEnd; z++) {
                    int[] pos = {x, y, z};

                    if (MathUtil.getDistance(origin, pos) <= radius) {
                        if (grid.getState(x, y, z) == Grid.OUTSIDE) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

}
