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
 * @author Tony Wong
 */
public class SetDifference implements Operation {

    /** Grid A */
    private Grid inGrid;

    /** Grid B */
    private Grid notInGrid;

    /** Grid A */
    private AttributeGrid inGridAtt;

    /** Grid B */
    private AttributeGrid notInGridAtt;
    
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
        
        if (inGrid instanceof AttributeGrid) {
            this.inGridAtt = (AttributeGrid) inGrid;
        }

        if (notInGrid instanceof AttributeGrid) {
            this.notInGridAtt = (AttributeGrid) notInGrid;
        }

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
     * @param dest The dest grid
     * @return The new grid
     */
    public Grid execute(Grid dest) {
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

        Grid diffGrid = null;

        if (dest != null) {
            diffGrid = dest;
        } else {
            // Create the difference grid as an empty copy of inGrid
            diffGrid = inGrid.createEmpty(inGridWidth,
                                               inGridHeight,
                                               inGridDepth,
                                               inGrid.getVoxelSize(),
                                               inGrid.getSliceHeight());
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

                            if (notInGridState == Grid.OUTSIDE) {
                            	diffGrid.setState(x, y, z, inGridState);
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
                            	diffGrid.setState(x, y, z, inGridState);
                            }
                        }
                    }
                }
            }
        }

        return diffGrid;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The dest grid
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid dest) {
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

        AttributeGrid diffGrid = null;

        if (dest != null) {
            diffGrid = dest;
        } else {
            // Create the difference grid as an empty copy of inGrid
            diffGrid = (AttributeGrid) inGrid.createEmpty(inGridWidth,
                    inGridHeight,
                    inGridDepth,
                    inGrid.getVoxelSize(),
                    inGrid.getSliceHeight());
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
                            int originalMaterial = inGridAtt.getAttribute(x, y, z);

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