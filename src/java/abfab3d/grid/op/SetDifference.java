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
 * Determine the set difference between two grids.  The set difference between
 * grid A and grid B is the set of filled voxels in A, but not in B.
 *
 * TODO: May need to change contructors around to take in a inGrid only as
 * the parameter instead of both inGrid and notInGrid.  The inherited execute
 * function passes in the grid to operate on.
 *
 * @author Tony Wong
 */
public class SetDifference implements Operation {

    /** Grid A */
    private Grid inGrid;

    /** Grid B */
    private Grid notInGrid;

    /** The material for the set difference voxels */
    private int material;

    /**
     * Constructor.
     *
     * @param inGrid
     * @param notInGrid
     */
    public SetDifference(Grid inGrid, Grid notInGrid) {
        this.inGrid = inGrid;
        this.notInGrid = notInGrid;
        this.material = -1;
    }

    /**
     * Constructor.
     *
     * @param inGrid
     * @param notInGrid
     */
    public SetDifference(Grid inGrid, Grid notInGrid, int material) {
        this.inGrid = inGrid;
        this.notInGrid = notInGrid;
        this.material = material;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
	 * TODO: Ignore this function for now.  Use execute() instead.
	 *
     * @param grid The grid to use for grid A.
     * @return The new grid
     */
    public Grid execute(Grid grid) {
/*        int inGridWidth = inGrid.getWidth();
    	int inGridHeight = inGrid.getHeight();
        int inGridDepth = inGrid.getDepth();

        int notInGridWidth = notInGrid.getWidth();
        int notInGridHeight = notInGrid.getHeight();
        int notInGridDepth = notInGrid.getDepth();

        // TODO: What happens if the size of inGrid, notInGrid, and
        // the parameter grid are not the same??

        if (inGridWidth != notInGridWidth ||
        	inGridHeight != notInGridHeight ||
        	inGridDepth != notInGridDepth) {
        	return null;
        }

        if (inGridWidth != grid.getWidth() ||
            inGridHeight != grid.getHeight() ||
            inGridDepth != grid.getDepth()) {
            return null;
        }

        // if material is -1, keep the original material of the grid for
        //   the set difference voxels
        // else use the new material for the set difference voxels
        if (material == -1) {
            for(int y=0; y < inGridHeight; y++) {
                for(int x=0; x < inGridWidth; x++) {
                    for(int z=0; z < inGridDepth; z++) {
                        byte inGridState = inGrid.getState(x, y, z);

                        if (inGridState != Grid.OUTSIDE) {
                            byte notInGridState = notInGrid.getState(x, y, z);
                            int originalMaterial = inGrid.getMaterial(x, y, z);

                            if (notInGridState == Grid.OUTSIDE) {
                            	grid.setData(x, y, z, inGridState, originalMaterial);
                            }
                        }
                    }
                }
            }
        } else {
//System.out.println("=========> blahblah");
            for(int y=0; y < inGridHeight; y++) {
                for(int x=0; x < inGridWidth; x++) {
                    for(int z=0; z < inGridDepth; z++) {
                        byte inGridState = inGrid.getState(x, y, z);

                        if (inGridState != Grid.OUTSIDE) {
//System.out.println("(" + x + ", " + y + ", " + z + ") inGrid: not outside");
                            byte notInGridState = notInGrid.getState(x, y, z);

                            if (notInGridState == Grid.OUTSIDE) {
//System.out.println("(" + x + ", " + y + ", " + z + ") notInGrid: outside");
                            	grid.setData(x, y, z, inGridState, material);
                            }
                        }
                    }
                }
            }
        }
*/
        return grid;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid A.
     * @return The new grid
     */
    public Grid execute() {
        int inGridWidth = inGrid.getWidth();
    	int inGridHeight = inGrid.getHeight();
        int inGridDepth = inGrid.getDepth();

        int notInGridWidth = notInGrid.getWidth();
        int notInGridHeight = notInGrid.getHeight();
        int notInGridDepth = notInGrid.getDepth();

        if (inGridWidth != notInGridWidth ||
        	inGridHeight != notInGridHeight ||
        	inGridDepth != notInGridDepth) {
        	return null;
        }

        // Create the difference grid as an empty copy of inGrid
        Grid diffGrid = inGrid.createEmpty(inGridWidth,
        		                           inGridHeight,
        		                           inGridDepth,
        		                           inGrid.getVoxelSize(),
        		                           inGrid.getSliceHeight());

        // if material is -1, keep the original material of the grid for
        //   the set difference voxels
        // else use the new material for the set difference voxels
        if (material == -1) {
            for(int y=0; y < inGridHeight; y++) {
                for(int x=0; x < inGridWidth; x++) {
                    for(int z=0; z < inGridDepth; z++) {
                        byte inGridState = inGrid.getState(x, y, z);

                        if (inGridState != Grid.OUTSIDE) {
                            byte notInGridState = notInGrid.getState(x, y, z);
                            int originalMaterial = inGrid.getMaterial(x, y, z);

                            if (notInGridState == Grid.OUTSIDE) {
                            	diffGrid.setData(x, y, z, inGridState, originalMaterial);
                            }
                        }
                    }
                }
            }
        } else {
//System.out.println("=========> blahblah");
            for(int y=0; y < inGridHeight; y++) {
                for(int x=0; x < inGridWidth; x++) {
                    for(int z=0; z < inGridDepth; z++) {
                        byte inGridState = inGrid.getState(x, y, z);

                        if (inGridState != Grid.OUTSIDE) {
//System.out.println("(" + x + ", " + y + ", " + z + ") inGrid: not outside");
                            byte notInGridState = notInGrid.getState(x, y, z);

                            if (notInGridState == Grid.OUTSIDE) {
//System.out.println("(" + x + ", " + y + ", " + z + ") notInGrid: outside");
                            	diffGrid.setData(x, y, z, inGridState, material);
                            }
                        }
                    }
                }
            }
        }

        return diffGrid;
    }

}