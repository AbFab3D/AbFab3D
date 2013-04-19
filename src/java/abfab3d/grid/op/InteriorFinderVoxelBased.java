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
public class InteriorFinderVoxelBased implements AttributeOperation, ClassAttributeTraverser {
    private static final int OUTSIDE = 0;
    private static final int ENTERING = 1;
    private static final int EXITING = 2;
    private static final int INSIDE = 3;

    /** The material to process */
    protected int material;

    /** The material to use for new voxels */
    protected int innerMaterial;

    /** The grid we are operating on */
    private AttributeGrid gridOp;


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
    public AttributeGrid execute(AttributeGrid grid) {
        gridOp = grid;

System.out.println("Creating grid for Interior Finding");
        AttributeGrid result = (AttributeGrid) grid.createEmpty(grid.getWidth(),grid.getHeight(),grid.getDepth(),
            grid.getVoxelSize(), grid.getSliceHeight());

//System.out.println("Filling model");
        byte state;
        byte last = Grid.OUTSIDE;
        int status = OUTSIDE;  // 0 = outside, 1 == coming into exterior, 2 == coming out inside, 3 == inside

        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

System.out.println("Outer material: " + material);

        VoxelData vd = grid.getVoxelData();
        // Find interior voxels using in/out tests
        // March across XAXIS
        for(int y=0; y < height; y++) {
            for(int z=0; z < depth; z++) {
                status = OUTSIDE;

                for(int x=0; x < width; x++) {
                    grid.getData(x,y,z,vd);
                    state = vd.getState();

                    if (vd.getMaterial() != material && state != Grid.OUTSIDE) {
                        // ignore other materials completely
                        continue;
                    }

//System.out.println("test: " + x + " " + y + " " + z + " state: " + state + " status: " + status + " mat: " + vd.getAttribute());
                    if (status == OUTSIDE) {
                        if (state == Grid.EXTERIOR) {
//System.out.println("outside, found exterior, set to entering at: " + x + " " + y + " " + z);
                            status = ENTERING;
                        } else if (state == Grid.INTERIOR) {
                            // No exterior voxel found?
//System.out.println("outside, found interior, set to inside at: " + x + " " + y + " " + z);
                            status = INSIDE;
                        }
                    } else if (status == ENTERING) {
//                        if (state == Grid.OUTSIDE && hasMatchingExterior(grid, x, y, z, 'X')) {
                          if (state == Grid.OUTSIDE && isInterior(grid, x, y, z)) {
//System.out.println("entering, found outside, set to inside at: " + x + " " + y + " " + z);
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
                            status = INSIDE;
                            continue;
                        } else if (state == Grid.INTERIOR) {
//System.out.println("entering, found interior, set to inside at: " + x + " " + y + " " + z);
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
                            status = INSIDE;
                        }
                    } else if (status == EXITING) {
                        if (state == Grid.OUTSIDE) {
//System.out.println("exiting, found outside, set to outside at: " + x + " " + y + " " + z);
                        	if (isInterior(grid, x, y, z)) {
                        		status = INSIDE;
                        	} else {
                        		status = OUTSIDE;
                        	}
                        } else if (state == Grid.INTERIOR) {
//System.out.println("exiting, found interior, set to inside at: " + x + " " + y + " " + z);
                            status = INSIDE;
                        } else if (state == Grid.EXTERIOR) {
//System.out.println("exiting, found exterior, set to entering at: " + x + " " + y + " " + z);
                            status = ENTERING;
                        }
                    } else if (status == INSIDE) {
                        if (state == Grid.OUTSIDE) {
//System.out.println("inside, found outside, set to inside at: " + x + " " + y + " " + z);
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
                            continue;
                        } else if (state == Grid.INTERIOR) {
//System.out.println("inside, found interior, set to inside at: " + x + " " + y + " " + z);
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
                            continue;
                        } else if (state == Grid.EXTERIOR) {
//System.out.println("inside, found exterior, set to exiting at: " + x + " " + y + " " + z);
                            status = EXITING;
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
                status = OUTSIDE;
                for(int y=0; y < height; y++) {
                    grid.getData(x,y,z,vd);
                    state = vd.getState();

                    if (vd.getMaterial() != material && state != Grid.OUTSIDE) {
                        // ignore other materials completely
                        continue;
                    }


//System.out.println("test: " + x + " " + y + " " + z + " state: " + state + " status: " + status);

                    if (status == OUTSIDE) {
                        if (state == Grid.EXTERIOR) {
//System.out.println("Found exterior at: " + x + " " + y + " " + z);
                            status = ENTERING;
                        } else if (state == Grid.INTERIOR) {
                            // No exterior voxel found?
System.out.println("outside to inside at: " + x + " " + y + " " + z);
                            status = INSIDE;
                            continue;
                        }
                    } else if (status == ENTERING) {
//                        if (state == Grid.OUTSIDE && hasMatchingExterior(grid, x, y, z, 'Y')) {
                      	if (state == Grid.OUTSIDE && isInterior(grid, x, y, z)) {
//System.out.println("Found inside at1: " + x + " " + y + " " + z);
//                            result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
//                            status = INSIDE;
//                            continue;
                            if (result.getState(x,y,z) == Grid.INTERIOR) {
                                status = INSIDE;
                                continue;
                            }
                        } else if (state == Grid.INTERIOR) {
//System.out.println("Found inside at1: " + x + " " + y + " " + z);
//                            result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
//                            status = INSIDE;
                            if (result.getState(x,y,z) == Grid.INTERIOR) {
                                status = INSIDE;
                                continue;
                            }
                        }
                    } else if (status == EXITING) {
                        if (state == Grid.OUTSIDE) {
                        	if (isInterior(grid, x, y, z)) {
                        		status = INSIDE;
                        	} else {
                        		status = OUTSIDE;
                        	}
                        } else if (state == Grid.INTERIOR) {
                            status = INSIDE;
                            continue;
                        } else if (state == Grid.EXTERIOR) {
                            status = ENTERING;
                            continue;
                        }
                    } else if (status == INSIDE) {
                        if (state == Grid.OUTSIDE) {
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
                            continue;
                        } else if (state == Grid.INTERIOR) {
//                            result.setData(x,y,z,Grid.INTERIOR,innerMaterial);
                            continue;
                        } else if (state == Grid.EXTERIOR) {
//System.out.println("Exiting at1: " + x + " " + y + " " + z);
                            status = EXITING;
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
                status = OUTSIDE;
                for(int z=0; z < depth; z++) {
                    grid.getData(x,y,z,vd);
                    state = vd.getState();

                    if (vd.getMaterial() != material && state != Grid.OUTSIDE) {
                        // ignore other materials completely
                        continue;
                    }


//System.out.println("test: " + x + " " + y + " " + z + " state: " + state + " status: " + status);

                    if (status == OUTSIDE) {
                        if (state == Grid.EXTERIOR) {
//System.out.println("Found exterior at: " + x + " " + y + " " + z);
                            status = ENTERING;
                        } else if (state == Grid.INTERIOR) {
                            // No exterior voxel found?
//System.out.println("outside to inside at: " + x + " " + y + " " + z);
                            status = INSIDE;
                            continue;
                        }
                    } else if (status == ENTERING) {
//                        if (state == Grid.OUTSIDE && hasMatchingExterior(grid, x, y, z, 'Z')) {
                      	if (state == Grid.OUTSIDE && isInterior(grid, x, y, z)) {
//System.out.println("Found inside at1: " + x + " " + y + " " + z);
                            if (result.getState(x,y,z) == Grid.INTERIOR) {
                                status = INSIDE;
                                continue;
                            }
                        } else if (state == Grid.INTERIOR) {
//System.out.println("Found inside at1: " + x + " " + y + " " + z);
                            if (result.getState(x,y,z) == Grid.INTERIOR) {
                                status = INSIDE;
                                continue;
                            }
                        }
                    } else if (status == EXITING) {
                        if (state == Grid.OUTSIDE) {
                        	if (isInterior(grid, x, y, z)) {
                        		status = INSIDE;
                        	} else {
                        		status = OUTSIDE;
                        	}
                        } else if (state == Grid.INTERIOR) {
                            status = INSIDE;
                            continue;
                        } else if (state == Grid.EXTERIOR) {
                            status = ENTERING;
                            continue;
                        }
                    } else if (status == INSIDE) {
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
                            status = EXITING;
                        }
                    }

                    result.setData(x,y,z,Grid.OUTSIDE,(byte)0);
                }
            }
        }

System.out.println("ZAXIS Interior: " + result.findCount(Grid.VoxelClasses.INTERIOR));

        result.findAttribute(Grid.VoxelClasses.INTERIOR, this);
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

    /**
     * Determines if a voxel is interior by walking along the X, Y, and Z axis 
     * @param grid
     * @param xPos
     * @param yPos
     * @param zPos
     * @return
     */
    private boolean isInterior(Grid grid, int xPos, int yPos, int zPos) {
    	int width = grid.getWidth();
    	int height = grid.getHeight();
    	int depth = grid.getDepth();
    	byte state;
    	boolean xPosInterior = false;
    	boolean xNegInterior = false;
    	boolean yPosInterior = false;
    	boolean yNegInterior = false;
    	boolean zPosInterior = false;
    	boolean zNegInterior = false;

        VoxelData vd = grid.getVoxelData();
		for (int x=xPos+1; x<width; x++) {
            grid.getData(x, yPos, zPos,vd);
            state = vd.getState();

            if (state == Grid.EXTERIOR) {
            	xPosInterior = true;
            	break;
            }
		}
		
		if (!xPosInterior) {
			return false;
		}
		
		for (int x=xPos-1; x>=0; x--) {
            grid.getData(x, yPos, zPos,vd);
            state = vd.getState();

            if (state == Grid.EXTERIOR) {
            	xNegInterior = true;
            	break;
            }
		}
		
		if (!xNegInterior) {
			return false;
		}

		for (int y=yPos+1; y<height; y++) {
            grid.getData(xPos, y, zPos,vd);
            state = vd.getState();

            if (state == Grid.EXTERIOR) {
            	yPosInterior = true;
            	break;
            }
		}

		if (!yPosInterior) {
			return false;
		}

		for (int y=yPos-1; y>=0; y--) {
            grid.getData(xPos, y, zPos,vd);
            state = vd.getState();

            if (state == Grid.EXTERIOR) {
            	yNegInterior = true;
            	break;
            }
		}

		if (!yNegInterior) {
			return false;
		}
		
		for (int z=zPos+1; z<depth; z++) {
            grid.getData(xPos, yPos, z,vd);
            state = vd.getState();

            if (state == Grid.EXTERIOR) {
            	zPosInterior = true;
            	break;
            }
		}

		if (!zPosInterior) {
			return false;
		}
    	
		for (int z=zPos-1; z>=0; z--) {
            grid.getData(xPos, yPos, z,vd);
            state = vd.getState();

            if (state == Grid.EXTERIOR) {
            	zNegInterior = true;
            	break;
            }
		}

    	return zNegInterior;
    }

    private boolean hasMatchingExterior(Grid grid, int xPos, int yPos, int zPos, char dir) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        byte state;

        VoxelData vd = grid.getVoxelData();
        if (dir == 'Y') {
            for (int y=yPos+1; y<height; y++) {
                grid.getData(xPos, y, zPos,vd);
                state = vd.getState();

                if (state == Grid.EXTERIOR)
                    return true;
            }
        } else if (dir == 'Z') {
            for (int z=zPos+1; z<depth; z++) {
                grid.getData(xPos, yPos, z,vd);
                state = vd.getState();

                if (state == Grid.EXTERIOR)
                    return true;
            }
        } else {
            for (int x=xPos+1; x<width; x++) {
                grid.getData(x, yPos, zPos,vd);
                state = vd.getState();

                if (state == Grid.EXTERIOR)
                    return true;
            }
        }

        return false;
    }

    private void printGridStates(Grid grid) {
        int gridWidth = grid.getWidth();
        int gridHeight = grid.getHeight();
        int gridDepth = grid.getDepth();
        byte state;

        for (int y=0; y<gridHeight; y++) {

            for (int z=0; z<gridDepth; z++) {
                boolean rowHasState = false;
                String temp = "";

//              int y = 16;
//              int z = 15;
                for (int x=0; x<gridWidth; x++) {

                    state = grid.getState(x, y, z);
//System.out.println(x + ", " + y + ", " + z + ": " + state);

                    if (state == Grid.OUTSIDE) {
                        temp = temp + x + " " + y + " " + z + ": OUTSIDE\n";
                    } else if (state == Grid.EXTERIOR) {
                        temp = temp + x + " " + y + " " + z + ": =>EXTERIOR\n";
                        rowHasState = true;
                    } else if (state == Grid.INTERIOR) {
                        temp = temp + x + " " + y + " " + z + ": ==>INTERIOR\n";
                        rowHasState = true;
                    }
                }

                if (rowHasState) {
                    System.out.println(temp);
                }

            }
        }
    }
}