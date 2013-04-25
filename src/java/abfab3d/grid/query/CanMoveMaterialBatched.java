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
import abfab3d.path.*;

/**
 * Determines whether an object specified by a materialID can move
 *
 * This version uses the getData region version to reduce call
 * overhead.
 *
 * @author Alan Hudson
 */
public class CanMoveMaterialBatched implements ClassAttributeTraverser {
    /** The material to remove */
    private int material;

    /** The path to use */
    private StraightPath path;

    /** Did all the voxels escape */
    private boolean allEscaped;

    /** The grid we are using */
    private AttributeGrid gridAtt;

    /** Coordinates that can be ignored */
    private HashSet<VoxelCoordinate> ignoreSet;
    private VoxelData vd;

    public CanMoveMaterialBatched(int material,StraightPath path) {
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
        this.gridAtt = (AttributeGrid)grid;
        vd = grid.getVoxelData();

        this.ignoreSet = new HashSet<VoxelCoordinate>();

        // TODO: just use material and say class only moves external?
        gridAtt.findAttributeInterruptible(material, this);

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
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param start The voxel data
     */
    public boolean foundInterruptible(int x, int y, int z, VoxelData start) {
        // All should be exterior voxels.

//System.out.println("Move voxel: " + x + " " + y + " " + z);

        if (canIgnore(x,y,z)) {
            return true;
        }

        int[] pos = new int[3];

        // Move along path till edge or
//System.out.println("pos: " + java.util.Arrays.toString(pos) + " w: " + grid.getWidth());
        path.init(x,y,z, gridAtt.getWidth(), gridAtt.getHeight(), gridAtt.getDepth());

        boolean escaped = true;

        int aligned_count = 0;
//System.out.println("extents: " + java.util.Arrays.toString(extents));
        if (!path.isAxisAligned()) {

if (1==1) {
System.out.println("Not aligned: " + path);
}
            // Diagonal path, process unbatched
            while(path.next(pos)) {
                gridAtt.getData(pos[0], pos[1], pos[2],vd);

    //System.out.println(java.util.Arrays.toString(pos) + ": " + vd.getState() + "  " + vd.getAttribute());
                if (vd.getState() != Grid.OUTSIDE &&
                    vd.getMaterial() != material) {

    //System.out.println("Collide");
                    // found another materials voxel
                    escaped = false;
                    break;
                }
            }
        } else {
            int[] extents = new int[6];
            path.getExtents(extents);
            int len = (extents[1] - extents[0] + 1) * (extents[3] - extents[2] + 1) * (extents[5] - extents[4] + 1);

            VoxelData[] voxels = new VoxelData[len];

System.out.println("getData method not implemented");
//            grid.getData(extents[0], extents[1], extents[2], extents[3], extents[4], extents[5], voxels);

//System.out.println("len: " + len);
//System.out.println(java.util.Arrays.toString(voxels));
            for(int i=0; i < len; i++) {
                VoxelData vd = voxels[i];

    //System.out.println("i: " + i + " vd: " + vd);
    //System.out.println(java.util.Arrays.toString(pos) + ": " + vd.getState() + "  " + vd.getAttribute());
                if (vd.getState() != Grid.OUTSIDE &&
                    vd.getMaterial() != material) {

    //System.out.println("Collide");
                    // found another materials voxel
                    escaped = false;
                    break;
                }
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

            if (state == Grid.EXTERIOR) {
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
