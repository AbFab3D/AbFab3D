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
import abfab3d.path.Path;

/**
 * Determines whether an object specified by a materialID can move in the
 * direction specified.  Only cares about intersections with
 * targteted material.
 *
 * Might be able to optimize by moving the axis most version of
 * of something and then not having to calculate the rest in that
 * axis.  Might only work for axis aligned?  cast ray along negative
 * axis and say all those are ok if connected.

 *
 *
 * @author Alan Hudson
 */
public class CanMoveMaterialTargeted implements ClassAttributeTraverser {
    /** The material to remove */
    private long material;

    /** The target material */
    private long target;

    /** The path to use */
    private Path path;

    /** Did all the voxels escape */
    private boolean allEscaped;

    /** The gridAtt we are using */
    private AttributeGrid gridAtt;

    /** Coordinates that can be ignored */
    HashSet<VoxelCoordinate> ignoreSet;

    public CanMoveMaterialTargeted(long material, long target, Path path) {
        this.material = material;
        this.target = target;
        this.path = path;
    }

    /**
     * Can the specified material move along the path
     * to exit the voxel space.  Any intersection with
     * another materialID will cause failure.
     *
     * @param grid The gridAtt to use for gridAtt src
     * @return true if it can move to an exit.
     */
    public boolean execute(Grid grid) {
        allEscaped = true;
        this.gridAtt = (AttributeGrid) grid;

        this.ignoreSet = new HashSet<VoxelCoordinate>();

        // TODO: just use material and say class only moves external?
//        gridAtt.findInterruptible(VoxelClasses.INSIDE, material, this);
        gridAtt.findAttributeInterruptible(material, this);

        return allEscaped;
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x The x gridAtt coordinate
     * @param y The y gridAtt coordinate
     * @param z The z gridAtt coordinate
     * @param start The voxel data
     */
    public void found(int x, int y, int z, VoxelData start) {
        // ignore
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x The x gridAtt coordinate
     * @param y The y gridAtt coordinate
     * @param z The z gridAtt coordinate
     * @param start The voxel data
     */
    public boolean foundInterruptible(int x, int y, int z, VoxelData start) {
        // All should be exterior voxels.

//System.out.println("Move voxel: " + x + " " + y + " " + z);

        if (canIgnore(x,y,z)) {
            return true;
        }

        int[] pos = new int[] {x,y,z};

        // Move along path till edge or
        path.init(pos, gridAtt.getWidth(), gridAtt.getHeight(), gridAtt.getDepth());

        boolean escaped = true;

        VoxelData vd = gridAtt.getVoxelData();
        while(path.next(pos)) {
            gridAtt.getData(pos[0], pos[1], pos[2],vd);

//System.out.println(java.util.Arrays.toString(pos) + ": " + vd.getState() + "  " + vd.getAttribute());
            if (vd.getState() != Grid.OUTSIDE && vd.getMaterial() == target) {

//System.out.println("Collide: " + vd.getAttribute());
                // found another materials voxel
                escaped = false;
                break;
            }
        }

        if (!escaped) {
            allEscaped = false;
            return false;
        }

        // walk along negative path, stop at first outside
        addIgnoredVoxels(x, y, z);

        return true;
    }

    /**
     * Add voxels to be ignored for a given path as specified by ignoreSetIndex.
     *
     * @param x The X coordinate for the starting position
     * @param y The Y coordinate for the starting position
     * @param z The Z coordinate for the starting position
     */
    private void addIgnoredVoxels(int x, int y, int z) {
        int[] pos = new int[] {x, y, z};

        Path invertedPath = path.invertPath();
        invertedPath.init(pos, gridAtt.getWidth(), gridAtt.getHeight(), gridAtt.getDepth());

        while(invertedPath.next(pos)) {
            byte state = gridAtt.getState(pos[0], pos[1], pos[2]);

            // can optimize by ignoring interior voxels and only checking for exterior voxels
            if (state == Grid.OUTSIDE)
                break;

            if (state == Grid.INSIDE) {
//System.out.println("placing in ignore list: " + pos[0] + " " + pos[1] + " " + pos[2]);
                ignoreSet.add(new VoxelCoordinate(pos[0], pos[1], pos[2]));
            }
        }
    }

    /**
     * Checks if a voxel can be ignored.
     *
     * @param x The X coordinate of the voxel to check
     * @param y The Y coordinate of the voxel to check
     * @param z The Z coordinate of the voxel to check
     * @return True if the voxel can be ignored.
     */
    private boolean canIgnore(int x, int y, int z) {
//if (1==1) return false;

        if (ignoreSet.contains(new VoxelCoordinate(x, y, z))) {
//System.out.println("can ignore: " + x + " " + y + " " + z);
            return true;
        }

        return false;
    }

    public int getIgnoredCount() {
        return ignoreSet.size();
    }
}
