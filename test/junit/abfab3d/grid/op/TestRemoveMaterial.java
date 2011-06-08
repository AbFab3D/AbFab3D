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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Internal Imports
import abfab3d.grid.*;
import abfab3d.grid.op.RemoveMaterial;

/**
 * Tests the functionality of RemoveMaterial Operation
 *
 * @author Alan Hudson
 * @version
 */
public class TestRemoveMaterial extends BaseTestGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestRemoveMaterial.class);
    }

    /**
     * Test basic operation
     */
    public void testBasic() {
        int size = 11;

        Grid grid = new SliceGrid(size,size,size,0.001, 0.001, true);

        // Add Object 1
        int mat1_count = 5;

        grid.setData(0,0,0, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,0,1, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,0,2, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,1,0, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,1,1, Grid.EXTERIOR, (byte) 1);

        assertEquals("Material1 count wrong on insert",
            countMaterial(grid, (byte) 1),mat1_count);

        // Add Object 2
        int mat2_count = 6;

        grid.setData(6,5,0, Grid.EXTERIOR, (byte) 2);
        grid.setData(6,5,1, Grid.EXTERIOR, (byte) 2);
        grid.setData(6,5,2, Grid.EXTERIOR, (byte) 2);
        grid.setData(6,6,0, Grid.EXTERIOR, (byte) 2);
        grid.setData(6,6,1, Grid.EXTERIOR, (byte) 2);
        grid.setData(6,6,2, Grid.EXTERIOR, (byte) 2);

        assertEquals("Material2 count wrong after insert2",
            countMaterial(grid, (byte) 2),mat2_count);

        assertEquals("Material1 count wrong after insert2",
            countMaterial(grid,(byte) 1),mat1_count);

        Operation op = new RemoveMaterial((byte) 1);
        op.execute(grid);

        assertEquals("Material1 count wrong after removal",
            countMaterial(grid,(byte) 1),0);

        assertEquals("Material2 count wrong after removal",
            countMaterial(grid,(byte) 2),mat2_count);

        op = new RemoveMaterial((byte) 2);
        op.execute(grid);

        assertEquals("Material2 count wrong after removal",
            countMaterial(grid,(byte) 2),0);
    }

    private int countMaterial(Grid grid, byte mat) {
        int ret_val = 0;

        int width = grid.getWidth();
        int depth = grid.getDepth();
        int height = grid.getHeight();

        int state;

        VoxelData vd;

        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                for(int z=0; z < depth; z++) {
                    vd = grid.getData(x,y,z);

                    if (vd.getMaterial() == mat) {
                        ret_val++;
                    }
                }
            }
        }

        return ret_val;
    }
}
