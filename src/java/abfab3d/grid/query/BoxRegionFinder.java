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
import java.util.*;

// Internal Imports
import abfab3d.core.Grid;
import abfab3d.grid.*;

/**
 * Finds box shaped regions in a grid.
 *
 * Works best with a spatial divided grid such as an Octree.
 *
 * @author Alan Hudson
 */
public class BoxRegionFinder {
    private HashSet<Region> ret_val;

    // Scratch vars
    private int[] center;
    private int[] size;

    public BoxRegionFinder() {
        center = new int[3];
        size = new int[3];
    }

    /**
     * Find the regions.
     *
     * @param grid The grid to use for grid src
     * @return The region of voxels
     */
    public Set<Region> execute(Grid grid) {
        ret_val = new HashSet<Region>();

        //if (!(grid instanceof OctreeCell)) {
            walkGrid(grid);

            return ret_val;
        //}

        /*
        OctreeCell root = (OctreeCell) grid;

        OctreeCell[] children = root.getChildren();

        addChildren(children);

        return ret_val;
        */
    }
/*
    private void addChildren(OctreeCell[] cell) {
        int len = cell.length;
        for(int i=0; i < len; i++) {
            if (cell[i] == null)
                continue;

            byte state = cell[i].getState();

            if (state != OctreeCell.MIXED) {
                if (state == Grid.INSIDE) {
                    cell[i].getRegion(center,size);
                    BoxRegion box = new BoxRegion(center,size);

                    ret_val.add(box);
                }
            } else {
                addChildren(cell[i].getChildren());
            }
        }
    }
     */
    /**
     * Walk the grid and create boxes.
     *
     * @param grid The grid
     */
    private void walkGrid(Grid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

System.out.println("width: " + width + " height: " + height + " depth: " + depth);
        int start = 0;
        int end;
        int stateVal = 0;   // 0 outside, 1 = ext

        int[] origin = new int[3];
        int[] size = new int[3];
        byte state;

        // TODO: My guess:  This code should use the two planes that have they
        // largest values.  Or maybe not, perhaps its really looking for the answer
        // that gives the most same sized cubes.

        if ((depth >= width && depth >= height)) {
System.out.println("***depth");
            for(int i=0; i < width; i++) {
                for(int j=0; j < height; j++) {
                    for(int k=0; k < depth; k++) {
                        state = grid.getState(i,j,k);
//System.out.println(i + "," + j + "," + k + " state: " + state);

                        if (stateVal == 0) {
                            if (state == Grid.INSIDE) {
                                start = k;
                                stateVal = 1;
                            }
                        } else if (stateVal == 1) {
                            if (state == Grid.INSIDE) {
                                continue;
                            }

                            // End of run
                            end = k;

                            origin[0] = i;
                            origin[1] = j;
                            origin[2] = start;


                            size[0] = 1;
                            size[1] = 1;
                            size[2] = end - start;

//System.out.println("run origin: " + java.util.Arrays.toString(origin) + " size: " + java.util.Arrays.toString(size));

                            ret_val.add(new BoxRegion(origin, size));
                            stateVal = 0;
                        }
                    }

                    if (stateVal == 1) {
                        // End of run
                        end = depth;

                        origin[0] = i;
                        origin[1] = j;
                        origin[2] = start;


                        size[0] = 1;
                        size[1] = 1;
                        size[2] = end - start;

                        ret_val.add(new BoxRegion(origin, size));
                        stateVal = 0;

//System.out.println("run2 origin: " + java.util.Arrays.toString(origin) + " size: " + java.util.Arrays.toString(size));
                    }

                }
            }
        } else if ((height >= width && height >= depth)) {
System.out.println("***height");
            for(int k=0; k < depth; k++) {
                for(int i=0; i < width; i++) {
                    for(int j=0; j < height; j++) {
                        state = grid.getState(i,j,k);
//System.out.println(i + "," + j + "," + k + " state: " + state);
                        if (stateVal == 0) {
                            if (state == Grid.INSIDE) {
                                start = j;
                                stateVal = 1;
                            }
                        } else if (stateVal == 1) {
                            if (state == Grid.INSIDE) {
                                continue;
                            }

                            // End of run
                            end = j;

                            origin[0] = i;
                            origin[1] = start;
                            origin[2] = k;


                            size[0] = 1;
                            size[1] = end - start;
                            size[2] = 1;

//System.out.println("run origin: " + java.util.Arrays.toString(origin) + " size: " + java.util.Arrays.toString(size));

                            ret_val.add(new BoxRegion(origin, size));
                            stateVal = 0;
                        }
                    }

                    if (stateVal == 1) {
                        // End of run
                        end = height;

                        origin[0] = i;
                        origin[1] = start;
                        origin[2] = k;


                        size[0] = 1;
                        size[1] = end - start;
                        size[2] = 1;

                        ret_val.add(new BoxRegion(origin, size));
                        stateVal = 0;

//System.out.println("run2 origin: " + java.util.Arrays.toString(origin) + " size: " + java.util.Arrays.toString(size));
                    }
                }
            }
        } else if ((width >= height && width >= depth)) {
System.out.println("***width");
            for(int k=0; k < depth; k++) {
                for(int j=0; j < height; j++) {
                    for(int i=0; i < width; i++) {
                        state = grid.getState(i,j,k);
//System.out.println(i + "," + j + "," + k + " state: " + state);
                        if (stateVal == 0) {
                            if (state == Grid.INSIDE) {
                                start = i;
                                stateVal = 1;
                            }
                        } else if (stateVal == 1) {
                            if (state == Grid.INSIDE) {
                                continue;
                            }

                            // End of run
                            end = i;

                            origin[0] = start;
                            origin[1] = j;
                            origin[2] = k;


                            size[0] = end - start;
                            size[1] = 1;
                            size[2] = 1;

//System.out.println("run origin: " + java.util.Arrays.toString(origin) + " size: " + java.util.Arrays.toString(size));

                            ret_val.add(new BoxRegion(origin, size));
                            stateVal = 0;
                        }
                    }

                    if (stateVal == 1) {
                        // End of run
                        end = width;

                        origin[0] = start;
                        origin[1] = j;
                        origin[2] = k;


                        size[0] = end-start;
                        size[1] = 1;
                        size[2] = 1;

                        ret_val.add(new BoxRegion(origin, size));
                        stateVal = 0;

//System.out.println("run2 origin: " + java.util.Arrays.toString(origin) + " size: " + java.util.Arrays.toString(size));
                    }
                }
            }
        }
    }
}