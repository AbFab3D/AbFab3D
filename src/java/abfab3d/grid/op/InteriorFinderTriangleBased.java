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
import java.util.*;
import java.io.*;
import org.web3d.vrml.sav.ContentHandler;
import org.j3d.geom.*;
import org.web3d.util.spatial.Triangle;
import javax.vecmath.*;

// Internal Imports
import abfab3d.grid.*;

/**
 * Find the interior voxels of a grid.  Walks the model from each axis,
 * when it finds an exterior voxel it assumes its entered the model.
 *
 * I suspect this method will be error prone.  But it should be fast.
 *
 * @author Alan Hudson
 */
public class InteriorFinderTriangleBased implements Operation, ClassTraverser {
    private static final int OUTSIDE = 0;
    private static final int ENTERING = 1;
    private static final int EXITING = 2;
    private static final int INSIDE = 3;

    /** The material to process */
    protected int material;

    /** The material to use for new voxels */
    protected int innerMaterial;

    /** The grid we are operating on */
    private Grid gridOp;

    /** The triangle geometry */
    private GeometryData geom;

    /**
     * Constructor.
     *
     * @param material The materialID of exterior voxels
     * @param newMaterial The materialID to assign new interior voxels
     */
    public InteriorFinderTriangleBased(GeometryData geom, int material, int newMaterial) {
        this.geom = geom;
        this.material = material;
        this.innerMaterial = newMaterial;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid A.
     * @return The new grid
     */
    public Grid execute(Grid grid) {
        gridOp = grid;

/*
        // Copies results back
        result.find(Grid.VoxelClasses.INTERIOR, this);
        gridOp = null;
*/
        return grid;
    }

    /**
     * A voxel of the class requested has been found.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public void found(int x, int y, int z, VoxelData vd) {
        gridOp.setData(x,y,z,Grid.INTERIOR, innerMaterial);

// TODO: change back
        //gridOp.setData(x,y,z,Grid.EXTERIOR, innerMaterial);
    }

    /**
     * A voxel of the class requested has been found.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public boolean foundInterruptible(int x, int y, int z, VoxelData vd) {
        // ignore
        return true;
    }
}
