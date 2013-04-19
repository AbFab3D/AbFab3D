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
public class CanMoveMaterialAllPaths implements ClassAttributeTraverser {
    /** The material to remove */
    private int material;

    /** The path to use */
    private Path[] paths;

    /** The gridAtt we are using */
    private AttributeGrid gridAtt;

    /** Coordinates that can be ignored */
    private HashSet<VoxelCoordinate>[] ignoreSet;

    /** The number bad paths **/
    private int badPathCount;

    private VoxelData vd;

    public CanMoveMaterialAllPaths(int material, Path[] paths) {
        this.material = material;
        this.paths = paths.clone();
        this.ignoreSet = new HashSet[paths.length];
    }

    /**
     * Can the specified material move along the path
     * to exit the voxel space.  Any intersection with
     * another materialID will cause failure.
     *
     * @param grid The gridAtt to use for gridAtt src
     * @return true if it can move to an exit.
     */
    public boolean execute(AttributeGrid grid) {
//        allEscaped = true;
        this.gridAtt = grid;
        vd = grid.getVoxelData();

        for (int i=0; i<ignoreSet.length; i++) {
            ignoreSet[i] = new HashSet<VoxelCoordinate>();
        }

//        gridAtt.find(VoxelClasses.EXTERIOR, material, this);
        grid.findAttributeInterruptible(material, this);

        return canEscape();
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
        // All should be exterior voxels.

//System.out.println("Move voxel: " + x + " " + y + " " + z);

        boolean[] badPaths = new boolean[paths.length];
        badPathCount = 0;

        // Iterate through each path
        for (int i=0; i<paths.length; i++) {
            if (canIgnore(i, x,y,z)) {
                continue;
            }

            int[] pos = new int[] {x,y,z};

            paths[i].init(pos, gridAtt.getWidth(), gridAtt.getHeight(), gridAtt.getDepth());

            while(paths[i].next(pos)) {
                gridAtt.getData(pos[0], pos[1], pos[2],vd);

//System.out.println(java.util.Arrays.toString(pos) + ": " + vd.getState() + "  " + vd.getAttribute());
                if (vd.getState() != Grid.OUTSIDE &&
                    vd.getMaterial() != material) {

//System.out.println("Collide");
                    // found another materials voxel
                    badPathCount++;
                    badPaths[i] = true;
                    break;
                }

            }

//System.out.println("badPaths " + i + ": " + badPaths[i]);

            // walk along opposite path, stop at first outside
            addIgnoredVoxels(i, x, y, z);
        }

        processBadPaths(badPaths);
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

        if (!canEscape())
            return false;

//System.out.println("Move voxel: " + x + " " + y + " " + z);

        boolean[] badPaths = new boolean[paths.length];
        badPathCount = 0;

        for (int i=0; i<paths.length; i++) {
            if (canIgnore(i, x,y,z)) {
                continue;
            }

            int[] pos = new int[] {x,y,z};

            paths[i].init(pos, gridAtt.getWidth(), gridAtt.getHeight(), gridAtt.getDepth());

            while(paths[i].next(pos)) {
                gridAtt.getData(pos[0], pos[1], pos[2],vd);

//System.out.println(java.util.Arrays.toString(pos) + ": " + vd.getState() + "  " + vd.getAttribute());
                if (vd.getState() != Grid.OUTSIDE &&
                    vd.getMaterial() != material) {

//System.out.println("Collide");
                    // found another materials voxel
                    badPathCount++;
                    badPaths[i] = true;
                    break;
                }
            }
//System.out.println("badPaths[" + i + "]: " + badPaths[i]);
            // walk along opposite path, stop at first outside
            addIgnoredVoxels(i, x, y, z);
        }

        processBadPaths(badPaths);

        return canEscape();
    }

    /**
     * Add voxels to be ignored for a given path as specified by ignoreSetIndex.
     *
     * @param ignoreSetIndex The index of the path array to add voxels to ignore
     * @param x The X coordinate for the starting position
     * @param y The Y coordinate for the starting position
     * @param z The Z coordinate for the starting position
     */
    private void addIgnoredVoxels(int ignoreSetIndex, int x, int y, int z) {
        int[] pos = new int[] {x, y, z};

        Path invertedPath = paths[ignoreSetIndex].invertPath();
        invertedPath.init(pos, gridAtt.getWidth(), gridAtt.getHeight(), gridAtt.getDepth());

        while(invertedPath.next(pos)) {
            gridAtt.getData(pos[0], pos[1], pos[2],vd);

            // can optimize by ignoring interior voxels and only checking for exterior voxels
            if (vd.getState() == Grid.OUTSIDE)
                break;

            if (vd.getState() == Grid.EXTERIOR) {
//System.out.println("placing in ignore list: " + pos[0] + " " + pos[1] + " " + pos[2]);
                ignoreSet[ignoreSetIndex].add(new VoxelCoordinate(pos[0], pos[1], pos[2]));
            }
        }
    }

    private boolean canIgnore(int ignoreSetIndex, int x, int y, int z) {
//System.out.println("checking can ignore path " + ignoreSetIndex + ": " + x + " " + y + " " + z);
        if (ignoreSet[ignoreSetIndex].contains(new VoxelCoordinate(x, y, z))) {
//System.out.println("can ignore path " + ignoreSetIndex + ": " + x + " " + y + " " + z);
            return true;
        }

        return false;
    }

    /**
     * Checks if all voxels can escape.
     *
     * @return True if all voxels can escape
     */
    private boolean canEscape() {
        return (paths.length > 0);
    }

    /**
     * Removes bad paths from the array of paths. A path is bad if any voxel
     * cannot move out of the gridAtt bounds in that path.
     *
     * @param badPaths An array of booleans where its value indicates whether
     *   the corresponding path by index is good or bad
     */
    private void processBadPaths(boolean[] badPaths) {
        if (badPathCount == 0)
            return;

        Path[] tempPaths = new Path[paths.length - badPathCount];
        HashSet<VoxelCoordinate>[] tempIgnoreSet = new HashSet[paths.length - badPathCount];

        int j = 0;

        for (int i=0; i<badPaths.length; i++) {
            if (!badPaths[i]) {
//System.out.println("path " + i + " is good");
                tempPaths[j] = paths[i];
                tempIgnoreSet[j] = ignoreSet[i];
                j++;
            }
        }

        paths = tempPaths;
        ignoreSet = tempIgnoreSet;
    }
}
