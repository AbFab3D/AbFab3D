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
 * Determine the set union between an array of grids.  The first grid is the base grid.
 * EXTERIOR voxels of all other grids will be added to the first grid.
 *
 * (The union of two sets A and B is the collection of points which are in A or in B (or in both))
 *
 * @author Tony Wong
 */
public class SetUnion implements Operation {

    private Grid[] grids;

    private AttributeGrid[] attrGrids;

    /** The material for the set difference voxels */
    private long material;

    /**
     * Constructor.
     *
     */
    public SetUnion(Grid[] grids) {
        this(grids, -1);
    }

    /**
     * Constructor.
     *
     */
    public SetUnion(Grid[] grids, long material) {
        if (grids == null || grids.length == 0) {
            throw new IllegalArgumentException("Parameter is null or empty");
        }

        if (!validGrids(grids)) {
            throw new IllegalArgumentException("Grids are not the same size");
        }

        this.grids = grids;
        System.out.println("grids class: " + grids.getClass());
        if (grids instanceof AttributeGrid[]) {
            this.attrGrids = (AttributeGrid[]) grids;
        }

        this.material = material;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The dest grid
     * @return The new grid
     */
    public Grid execute(Grid dest) {
        // Return the first grid if number of grids is 1
        if (grids.length == 1) {
            return grids[0];
        }

        // TODO: What if the dest grid size is different from the size of the grids in array

        Grid finalGrid = null;
        int gridWidth = grids[0].getWidth();
        int gridHeight = grids[0].getHeight();
        int gridDepth = grids[0].getDepth();

        if (dest != null) {
            finalGrid = dest;
        } else {
            // Create the union grid as an empty copy of the first grid
            finalGrid = (AttributeGrid) grids[0].createEmpty(
                    gridWidth,
                    gridHeight,
                    gridDepth,
                    grids[0].getVoxelSize(),
                    grids[0].getSliceHeight());
        }

        for(int y=0; y < gridHeight; y++) {
            for(int x=0; x < gridWidth; x++) {
                for(int z=0; z < gridDepth; z++) {

                    // Interior state will stay interior after union.
                    // Exterior state may change to interior after union.
                    // - If the state is interior, mark the finalGrid voxel as interior and break
                    // - If the state is exterior, mark the finalGrid voxel as exterior, but continue loop
                    for (int i=0; i<grids.length; i++) {
                        if (grids[i].getState(x, y, z) == Grid.INTERIOR) {
                            finalGrid.setState(x, y, z, Grid.INTERIOR);
                            break;
                        } else if (grids[i].getState(x, y, z) == Grid.EXTERIOR) {
                            finalGrid.setState(x, y, z, Grid.EXTERIOR);
                        }
                    }
                }
            }
        }

        return finalGrid;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The dest grid
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid dest) {
        // Return the first grid if number of grids is 1
        if (attrGrids.length == 1) {
            return attrGrids[0];
        }

        // TODO: What if the dest grid size is different from the size of the grids in array

        AttributeGrid finalGrid = null;
        int gridWidth = attrGrids[0].getWidth();
        int gridHeight = attrGrids[0].getHeight();
        int gridDepth = attrGrids[0].getDepth();

        if (dest != null) {
            finalGrid = dest;
        } else {
            // Create the union grid as an empty copy of the first grid
            finalGrid = (AttributeGrid) grids[0].createEmpty(
                    gridWidth,
                    gridHeight,
                    gridDepth,
                    grids[0].getVoxelSize(),
                    grids[0].getSliceHeight());
        }

        // if material is -1, keep the original material of the grid for
        //   the set difference voxels
        // else use the new material for the set difference voxels
        if (material == -1) {
            for(int y=0; y < gridHeight; y++) {
                for(int x=0; x < gridWidth; x++) {
                    for(int z=0; z < gridDepth; z++) {

                        // Set state and material to that of the first grid
                        long originalMaterial = attrGrids[0].getAttribute(x, y, z);
                        finalGrid.setData(x, y, z, attrGrids[0].getState(x, y, z), originalMaterial);

                        // Interior state will stay interior after union.
                        // Exterior state may change to interior after union.
                        // - If the state is interior, mark the finalGrid voxel as interior and break
                        // - If the state is exterior, mark the finalGrid voxel as exterior, but continue loop
                        for (int i=1; i<attrGrids.length; i++) {
                            if (finalGrid.getState(x, y, z) == Grid.INTERIOR) {
                                continue;
                            }

                            if (attrGrids[i].getState(x, y, z) == Grid.INTERIOR) {
                                finalGrid.setData(x, y, z, Grid.INTERIOR, originalMaterial);
                                break;
                            } else if (attrGrids[i].getState(x, y, z) == Grid.EXTERIOR) {
                                finalGrid.setData(x, y, z, Grid.EXTERIOR, originalMaterial);
                            }
                        }
                    }
                }
            }
        } else {
//System.out.println("=========> blahblah");
            for(int y=0; y < gridHeight; y++) {
                for(int x=0; x < gridWidth; x++) {
                    for(int z=0; z < gridDepth; z++) {

                        // Set state to that of the first grid
                        finalGrid.setData(x, y, z, attrGrids[0].getState(x, y, z), material);

                        // Interior state will stay interior after union.
                        // Exterior state may change to interior after union.
                        // - If the state is interior, mark the finalGrid voxel as interior and break
                        // - If the state is exterior, mark the finalGrid voxel as exterior, but continue loop
                        for (int i=1; i<attrGrids.length; i++) {
                            if (finalGrid.getState(x, y, z) == Grid.INTERIOR) {
                                continue;
                            }

                            if (attrGrids[i].getState(x, y, z) == Grid.INTERIOR) {
                                finalGrid.setData(x, y, z, Grid.INTERIOR, material);
                                break;
                            } else if (attrGrids[i].getState(x, y, z) == Grid.EXTERIOR) {
                                finalGrid.setData(x, y, z, Grid.EXTERIOR, material);
                            }
                        }
                    }
                }
            }
        }

        return finalGrid;
    }

    /**
     * Verifies that the grids are the same size.
     *
     * @param grids The array of grids
     */
    private boolean validGrids(Grid[] grids) {
        if (grids.length < 2) {
            return true;
        }

        for (int i=1; i<grids.length; i++) {
            if (grids[i-1].getWidth() != grids[i].getWidth() ||
                grids[i-1].getHeight() != grids[i].getHeight() ||
                grids[i-1].getDepth() != grids[i].getDepth()) {
                return false;
            }
        }

        return true;
    }
}