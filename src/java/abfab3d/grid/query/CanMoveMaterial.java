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
import java.util.HashSet;

import abfab3d.grid.*;
import abfab3d.grid.Grid.VoxelClasses;
import abfab3d.path.Path;

/**
 * Determines whether an object specified by a materialID can move
 *
 * Might be able to optimize by moving the axis most version of
 * of something and then not having to calculate the rest in that
 * axis.  Might only work for axis aligned?  cast ray along negative
 * axis and say all those are ok if connected.

 *
 *
 * @author Alan Hudson
 */
public class CanMoveMaterial implements ClassTraverser {
    /** The material to remove */
    private byte material;

    /** The path to use */
    private Path path;

    /** Did all the voxels escape */
    private boolean allEscaped;

    /** The grid we are using */
    private Grid grid;

    /** Coordinates that can be ignored */
    HashSet<VoxelCoordinate> ignoreSet;

    public CanMoveMaterial(byte material,Path path) {
        this.material = material;
        this.path = path;
    }

    /**
     * Can the specified material move along the path
     * to exit the voxel space.  Any intersection with
     * another materialID will cause failure.
     *
     * @param grid The grid to use for grid src
     * @return true if it can move to an exit.
     */
    public boolean execute(Grid grid) {
        allEscaped = true;
        this.grid = grid;

        /** THIS CAUSE OUT OF MEMORY ERROR ON LARGE GRID SIZES **/
        this.ignoreSet = new HashSet<VoxelCoordinate>();

//        grid.find(material, this);
//        grid.find(VoxelClasses.EXTERIOR, material, this);
        grid.findInterruptible(VoxelClasses.EXTERIOR, material, this);

System.out.println("Final answer: " + allEscaped);

        return allEscaped;
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public void found(int x, int y, int z, VoxelData start) {
        // All should be exterior voxels.

//System.out.println("Move voxel: " + x + " " + y + " " + z);
        if (!allEscaped || canIgnore(x,y,z)) {
            // TODO: need some way to allow user termination of find?
            return;
        }

        int[] pos = new int[] {x,y,z};

        // Move along path till edge or
        path.init(pos, grid.getWidth(), grid.getHeight(), grid.getDepth());

        boolean escaped = true;

        while(path.next(pos)) {
//System.out.println("Pos: " + java.util.Arrays.toString(pos));
            VoxelData vd = grid.getData(pos[0], pos[1], pos[2]);
//System.out.println("moving: [" + x + " " + y + " " + z + "] to " + java.util.Arrays.toString(pos));
//System.out.println("VoxelData: " + vd.getState() + " " + vd.getMaterial());
            if (vd.getState() != Grid.OUTSIDE &&
                vd.getMaterial() != material) {

//System.out.println("Collide");
                // found another materials voxel
                escaped = false;
                break;
            }
        }

        if (!escaped)
            allEscaped = false;
/*
        // walk along negative path, stop at first outside
        ignoreSet.add(new VoxelCoordinate(x,y,z);
*/
        addIgnoredVoxels(x, y, z);
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public boolean foundInterruptible(int x, int y, int z, VoxelData start) {
        // All should be exterior voxels.

//System.out.println("Move voxel: " + x + " " + y + " " + z);
        if (canIgnore(x,y,z)) {
            return true;
        }

        int[] pos = new int[] {x,y,z};

        // Move along path till edge or
        path.init(pos, grid.getWidth(), grid.getHeight(), grid.getDepth());

        boolean escaped = true;

        while(path.next(pos)) {
//System.out.println("Pos: " + java.util.Arrays.toString(pos));
            VoxelData vd = grid.getData(pos[0], pos[1], pos[2]);
//System.out.println("moving: [" + x + " " + y + " " + z + "] to " + java.util.Arrays.toString(pos));
//System.out.println("VoxelData: " + vd.getState() + " " + vd.getMaterial());
            if (vd.getState() != Grid.OUTSIDE &&
                vd.getMaterial() != material) {

//System.out.println("Collide");
                // found another materials voxel
                escaped = false;
                break;
            }
        }

        if (!escaped) {
            allEscaped = false;
            return false;
        }
/*
        // walk along negative path, stop at first outside
        ignoreSet.add(new VoxelCoordinate(x,y,z);
*/
        addIgnoredVoxels(x, y, z);

        return true;
    }

    private void addIgnoredVoxels(int x, int y, int z) {
        int[] pos = new int[] {x, y, z};

        Path invertedPath = path.invertPath();
        invertedPath.init(pos, grid.getWidth(), grid.getHeight(), grid.getDepth());

        while(invertedPath.next(pos)) {
            VoxelData vd = grid.getData(pos[0], pos[1], pos[2]);

            // can optimize by ignoring interior voxels and only checking for exterior voxels
            if (vd.getState() == Grid.OUTSIDE)
                break;

//System.out.println("placing in ignore list: " + pos[0] + " " + pos[1] + " " + pos[2]);
            ignoreSet.add(new VoxelCoordinate(pos[0], pos[1], pos[2]));
        }
    }

    boolean canIgnore(int x, int y, int z) {
        if (ignoreSet.contains(new VoxelCoordinate(x, y, z))) {
//System.out.println("can ignore: " + x + " " + y + " " + z);
            return true;
        }

        return false;
    }
}
