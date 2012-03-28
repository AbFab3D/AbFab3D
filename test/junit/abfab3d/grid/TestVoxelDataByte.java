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
 * Tests the functionality of a OccupiedWrapper
 *
 * @author Tony Wong
 * @version
 */
public class TestVoxelDataByte extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestVoxelDataByte.class);
    }

    /**
     * Test setGrid.
     */
    public void testConstructor() {
        byte state = Grid.EXTERIOR;
        int material = 125;

        VoxelDataByte vd = new VoxelDataByte(state, material);

        assertEquals(state, vd.getState());
        assertEquals(material, vd.getMaterial());
    }

    public void testSetData() {
        byte state = Grid.EXTERIOR;
        int material = 125;

        VoxelDataByte vd = new VoxelDataByte(Grid.INTERIOR, 1);
        vd.setData(state, material);

        assertEquals(state, vd.getState());
        assertEquals(material, vd.getMaterial());
    }

    public void testSetState() {
        byte state = Grid.EXTERIOR;
        int material = 125;

        VoxelDataByte vd = new VoxelDataByte(Grid.INTERIOR, material);
        vd.setState(state);

        assertEquals(state, vd.getState());
        assertEquals(material, vd.getMaterial());
    }

    public void testSetMaterial() {
        byte state = Grid.EXTERIOR;
        int material = 125;

        VoxelDataByte vd = new VoxelDataByte(state, 1);
        vd.setMaterial(material);

        assertEquals(state, vd.getState());
        assertEquals(material, vd.getMaterial());
    }

    public void testDataRange() {
        byte state = 0;
        int material = 0;

        VoxelDataByte vd = new VoxelDataByte(state, material);

        state = Byte.MIN_VALUE;
        material = Byte.MIN_VALUE;

        vd.setData(state, material);

        assertEquals(Byte.MIN_VALUE, vd.getState());
        assertEquals(Byte.MIN_VALUE, vd.getMaterial());

        state = Byte.MAX_VALUE;
        material = Byte.MAX_VALUE;

        vd.setData(state, material);

        assertEquals(Byte.MAX_VALUE, vd.getState());
        assertEquals(Byte.MAX_VALUE, vd.getMaterial());
    }

    public void testClone() {
        byte state = Grid.EXTERIOR;
        int material = 255;

        VoxelDataByte vd = new VoxelDataByte(state, material);
        VoxelDataByte vd2 = (VoxelDataByte) vd.clone();

        assertEquals(state, vd2.getState());
        assertEquals(material, vd2.getMaterial());
    }

    public void testEquals() {
        byte state = Grid.EXTERIOR;
        int material = 5;

        VoxelDataByte vd = new VoxelDataByte(state, material);
        VoxelDataByte vd2 = new VoxelDataByte((byte)2, 3);

        assertFalse(vd.equals(vd2));

        vd2.setData(state, 1000);

        assertFalse(vd.equals(vd2));

        vd2.setData((byte)2, material);

        assertFalse(vd.equals(vd2));

        vd2.setData(state, material);

        assertTrue(vd.equals(vd2));

        // test equals with VoxelDataByte as an Object
        Object vd3 = (Object) vd.clone();

        assertTrue(vd.equals(vd3));

        // test against different object type
        Object vd4 = (Object) new String("blah");

        assertFalse(vd.equals(vd4));
    }
}
