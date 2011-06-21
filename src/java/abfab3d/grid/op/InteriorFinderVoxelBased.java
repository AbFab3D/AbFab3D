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
public class InteriorFinderVoxelBased implements Operation, ClassTraverser {
    /** The material to process */
    protected int material;

    /** The material to use for new voxels */
    protected int innerMaterial;

    /** The grid we are operating on */
    private Grid gridOp;


    /**
     * Constructor.
     *
     * @param material The materialID of exterior voxels
     * @param newMaterial The materialID to assign new interior voxels
     */
    public InteriorFinderVoxelBased(int material, int newMaterial) {
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

System.out.println("Creating grid for Interior Finding");
        Grid result = grid.createEmpty(grid.getWidth(),grid.getHeight(),grid.getDepth(),
            grid.getVoxelSize(), grid.getSliceHeight());

//System.out.println("Filling model");
        byte state;
        byte last = Grid.OUTSIDE;
        int status = 0;  // 0 = outside, 1 == coming into exterior, 2 == coming out inside, 3 == inside

        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

System.out.println("Outer material: " + material);

        // Find interior voxels using in/out tests
        // March across XAXIS
        for(int y=0; y < height; y++) {
            for(int z=0; z < depth; z++) {
                status = 0;
                for(int x=0; x < width; x++) {
                    VoxelData vd = grid.getData(x,y,z);
                    state = vd.getState();

                    if (vd.getMaterial() != material && state != Grid.OUTSIDE) {
                        // ignore other materials completely
                        continue;
                    }


//System.out.println("test: " + x + " " + y + " " + z + " state: " + state + " status: " + status + " mat: " + vd.getMaterial());
                    if (status == 0) {
                        if (state == Grid.EXTERIOR) {
//System.out.println("Found exterior at: " + x + " " + y + " " + z);
                            status = 1;
                        } else if (state == Grid.INTERIOR) {
                            // No exterior voxel found?
                            status = 3;
                        }
                    } else if (status == 1) {
                        if (state == Grid.OUTSIDE) {
//System.out.println("Found inside at1: " + x + " " + y + " " + z);
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
                            status = 3;
                            continue;
                        } else if (state == Grid.INTERIOR) {
//System.out.println("Found inside at2: " + x + " " + y + " " + z);
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
                            status = 3;
                        }
                    } else if (status == 2) {
                        if (state == Grid.OUTSIDE) {
                            status = 0;
                        } else if (state == Grid.INTERIOR) {
                            status = 3;
                        }
                    } else if (status == 3) {
                        if (state == Grid.OUTSIDE) {
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
                            continue;
                        } else if (state == Grid.INTERIOR) {
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
                            continue;
                        } else if (state == Grid.EXTERIOR) {
//System.out.println("Exiting at1: " + x + " " + y + " " + z);
                            status = 2;
                        }
                    }
                }
            }
        }

// TODO: I think the logic for each axis needs to compute state the same way
//  but y and z axis should flip non agreements on interior
//  or perhaps we need to go back to 3 axis calcs that are then ORed

System.out.println("XAXIS Interior: " + result.findCount(Grid.VoxelClasses.INTERIOR));
        // March across YAXIS
        for(int x=0; x < width; x++) {
            for(int z=0; z < depth; z++) {
                status = 0;
                for(int y=0; y < height; y++) {
                    VoxelData vd = grid.getData(x,y,z);
                    state = vd.getState();

                    if (vd.getMaterial() != material && state != Grid.OUTSIDE) {
                        // ignore other materials completely
                        continue;
                    }


//System.out.println("test: " + x + " " + y + " " + z + " state: " + state + " status: " + status);

                    if (status == 0) {
                        if (state == Grid.EXTERIOR) {
//System.out.println("Found exterior at: " + x + " " + y + " " + z);
                            status = 1;
                        } else if (state == Grid.INTERIOR) {
                            // No exterior voxel found?
                            status = 3;
                            continue;
                        }
                    } else if (status == 1) {
                        if (state == Grid.OUTSIDE) {
//System.out.println("Found inside at1: " + x + " " + y + " " + z);
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
                            status = 3;
                            continue;
                        } else if (state == Grid.INTERIOR) {
//System.out.println("Found inside at1: " + x + " " + y + " " + z);
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
                            status = 3;
                        }
                    } else if (status == 2) {
                        if (state == Grid.OUTSIDE) {
                            status = 0;
                        } else if (state == Grid.INTERIOR) {
                            status = 3;
                            continue;
                        }
                    } else if (status == 3) {
                        if (state == Grid.OUTSIDE) {
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
                            continue;
                        } else if (state == Grid.INTERIOR) {
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
                            continue;
                        } else if (state == Grid.EXTERIOR) {
//System.out.println("Exiting at1: " + x + " " + y + " " + z);
                            status = 2;
                        }
                    }

                    result.setData(x,y,z,Grid.OUTSIDE,(byte)0);
                }
            }
        }

System.out.println("YAXIS Interior: " + result.findCount(Grid.VoxelClasses.INTERIOR));

//System.out.println("*****");

        status = 0;
        // March across ZAXIS
        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                status = 0;
                for(int z=0; z < depth; z++) {
                    VoxelData vd = grid.getData(x,y,z);
                    state = vd.getState();

                    if (vd.getMaterial() != material && state != Grid.OUTSIDE) {
                        // ignore other materials completely
                        continue;
                    }


//System.out.println("test: " + x + " " + y + " " + z + " state: " + state + " status: " + status);

                    if (status == 0) {
                        if (state == Grid.EXTERIOR) {
//System.out.println("Found exterior at: " + x + " " + y + " " + z);
                            status = 1;
                        } else if (state == Grid.INTERIOR) {
                            // No exterior voxel found?
                            status = 3;
                        }
                    } else if (status == 1) {
                        if (state == Grid.OUTSIDE) {
//System.out.println("Found inside at1: " + x + " " + y + " " + z);
                            if (result.getState(x,y,z) == Grid.INTERIOR) {
                                status = 3;
                                continue;
                            }
                        } else if (state == Grid.INTERIOR) {
//System.out.println("Found inside at1: " + x + " " + y + " " + z);
                            if (result.getState(x,y,z) == Grid.INTERIOR) {
                                status = 3;
                            }
                        }
                    } else if (status == 2) {
                        if (state == Grid.OUTSIDE) {
                            status = 0;
                        } else if (state == Grid.INTERIOR) {
                            status = 3;
                            continue;
                        }
                    } else if (status == 3) {
                        if (state == Grid.OUTSIDE) {
                            if (result.getState(x,y,z) == Grid.INTERIOR) {
                                result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
                                continue;
                            }
                        } else if (state == Grid.INTERIOR) {
                            if (result.getState(x,y,z) == Grid.INTERIOR) {
                                result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
                                continue;
                            }
                        } else if (state == Grid.EXTERIOR) {
//System.out.println("Exiting at1: " + x + " " + y + " " + z);
                            status = 2;
                        }
                    }

                    result.setData(x,y,z,Grid.OUTSIDE,(byte)0);
                }
            }
        }

System.out.println("ZAXIS Interior: " + result.findCount(Grid.VoxelClasses.INTERIOR));

        result.find(Grid.VoxelClasses.INTERIOR, this);
        gridOp = null;

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