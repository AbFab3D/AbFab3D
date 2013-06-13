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

// External Imports
import junit.framework.TestSuite;
import junit.framework.Test;

// Internal Imports
import abfab3d.grid.*;

/**
 * Grid Tests
 *
 * @author Alan Hudson
 * @version
 */
public class TestGridSuite extends TestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("Grid Test Suite");

        suite.addTest(TestBlockBasedAttributeGridByte.suite());
        suite.addTest(TestBlockBasedGridShort.suite());
        suite.addTest(TestArrayAttributeGridByte.suite());
        suite.addTest(TestMaterialIndexedGridByte.suite());

        suite.addTest(TestRangeCheckWrapper.suite());
        suite.addTest(TestMaterialIndexedWrapper.suite());

        suite.addTest(TestVoxelDataByte.suite());
        suite.addTest(TestVoxelDataShort.suite());
        suite.addTest(TestVoxelDataInt.suite());
        suite.addTest(TestWorldCoordinate.suite());

        return suite;
    }
}
