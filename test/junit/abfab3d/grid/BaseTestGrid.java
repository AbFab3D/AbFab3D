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

package abfab3d.grid;

// External Imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Internal Imports

/**
 * Base functionality for testing grids.  Only uses the Grid interface.
 *
 * @author Alan Hudson
 * @version
 */
public class BaseTestGrid extends TestCase {
    /**
     * Set and get all values of a grid using voxel coords
     *
     * @param grid The grid to test
     */
    public void setGetAllVoxelCoords(Grid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                for(int z=0; z < depth; z++) {
                    grid.setData(x,y,z,Grid.EXTERIOR, (byte)1);
                }
            }
        }

        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                for(int z=0; z < depth; z++) {
                    VoxelData vd = grid.getData(x,y,z);

                    assertTrue("State wrong", vd.getState() == Grid.EXTERIOR);
                    assertTrue("Material wrong", vd.getMaterial() == 1);
                }
            }
        }
    }
}
