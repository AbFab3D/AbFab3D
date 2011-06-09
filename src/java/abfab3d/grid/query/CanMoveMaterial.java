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
import abfab3d.grid.*;
import abfab3d.path.Path;

/**
 * Determines whether an object specified by a materialID can move
 *
 *
 * @author Alan Hudson
 */
public class CanMoveMaterial {
    /** The material to remove */
    private byte material;

    /** The path to use */
    private Path path;

    /** The grid we are using */

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
/*
        grid.findMaterial(material, this);

*/
        return false;
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
    public void found(int x, int y, int z, VoxelData vd) {
        // All should be exterior voxels.

        // Move along path till edge or
    }
}
