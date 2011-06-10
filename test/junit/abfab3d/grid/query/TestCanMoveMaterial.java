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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Internal Imports
import abfab3d.grid.*;
import abfab3d.path.StraightPath;

/**
 * Tests the functionality of CanMoveMaterial Query.
 *
 * @author Alan Hudson
 * @version
 */
public class TestCanMoveMaterial extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestCanMoveMaterial.class);
    }

    /**
     * Test basic operation
     */
    public void testBasic() {
        int size = 12;

        Grid grid = new SliceGrid(size,size,size,0.001, 0.001, true);

        // Add Object 1
        int mat1_count = 5;

        grid.setData(0,0,0, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,0,1, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,0,2, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,0,3, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,0,4, Grid.EXTERIOR, (byte) 1);

        // Add Object 2
        int mat2_count = 6;

        grid.setData(0,2,0, Grid.EXTERIOR, (byte) 2);
        grid.setData(0,2,1, Grid.EXTERIOR, (byte) 2);
        grid.setData(0,2,2, Grid.EXTERIOR, (byte) 2);
        grid.setData(0,2,3, Grid.EXTERIOR, (byte) 2);
        grid.setData(0,2,4, Grid.EXTERIOR, (byte) 2);
        grid.setData(0,2,5, Grid.EXTERIOR, (byte) 2);

        StraightPath path = new StraightPath(new int[] {1,0,0});
        CanMoveMaterial query = new CanMoveMaterial((byte) 1, path);
        boolean escaped = query.execute(grid);

        assertTrue("X Axis move", escaped == true);
        System.out.println("Escaped: " + escaped);

        path = new StraightPath(new int[] {0,1,0});
        query = new CanMoveMaterial((byte) 1, path);
        escaped = query.execute(grid);

        assertTrue("Y Axis move", escaped == false);

    }
}
