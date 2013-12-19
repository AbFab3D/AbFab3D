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

import java.util.HashSet;
import java.util.Iterator;

// Internal Imports

/**
 * Base functionality for testing grids.  Only uses the Grid interface.
 *
 * @author Alan Hudson
 */
public class BaseTestAttributeGrid extends BaseTestGrid implements ClassAttributeTraverser {
    /**
     * Test creating an empty grid.
     */
    public void createEmpty(AttributeGrid grid) {
        grid.setData(5, 5, 5, Grid.INSIDE, 10);

        AttributeGrid grid2 = (AttributeGrid) grid.createEmpty(10, 11, 12, 0.002, 0.003);
        int gridSize = 10 * 11 * 12;

        assertEquals("Grid size is not " + gridSize, gridSize, grid2.getWidth()*grid2.getHeight()*grid2.getDepth());
        assertEquals("Grid voxel size is not 0.002", 0.002, grid2.getVoxelSize());
        assertEquals("Grid slice height is not 0.003", 0.003, grid2.getSliceHeight());

        // all voxels in empty grid should be OUTSIDE state and 0 material
        assertEquals("State is not OUTSIDE for (5, 5, 5)", Grid.OUTSIDE, grid2.getState(5, 5, 5));
        assertEquals("Material is not 0 for (5, 5, 5)", 0, grid2.getAttribute(5, 5, 5));
    }

    /**
     * Test that find voxels by material actually found the voxels in the correct coordinates
     */
    public void findMaterialIterator1(AttributeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        long mat1 = 1;
        long mat2 = 2;

        HashSet<VoxelCoordinate> vcSetMat1 = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetMat2 = new HashSet<VoxelCoordinate>();

        for (int x = 0; x < width; x++) {
            grid.setData(x, 2, 2, Grid.INSIDE, mat1);
            vcSetMat1.add(new VoxelCoordinate(x, 2, 2));

            grid.setData(x, 5, 6, Grid.INSIDE, mat2);
            vcSetMat2.add(new VoxelCoordinate(x, 5, 6));
        }

        FindAttributeIterateTester ft = new FindAttributeIterateTester(vcSetMat1);
        grid.findAttribute(mat1, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat1,
                ft.foundAllVoxels());

        ft = new FindAttributeIterateTester(vcSetMat2);
        grid.findAttribute(mat2, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat2,
                ft.foundAllVoxels());

        // make sure that finding a voxel not in the list returns false
        grid.setData(10, 6, 2, Grid.INSIDE, mat1);
        ft = new FindAttributeIterateTester(vcSetMat1);
        grid.findAttribute(mat1, ft);

        assertFalse("Found material iterator should return false",
                ft.foundAllVoxels());

        // make sure that not finding a voxel in the list returns false
        grid.setData(1, 5, 6, Grid.INSIDE, mat1);
        ft = new FindAttributeIterateTester(vcSetMat2);
        grid.findAttribute(mat1, ft);

        assertFalse("Found material iterator should return false",
                ft.foundAllVoxels());

    }

    /**
     * Test that find voxels by material actually found the voxels in the correct coordinates
     */
    public void findMaterialIterator2(AttributeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        long mat1 = 1;

        //-------------------------------------------------------
        // test on some random boundary coordinates
        int[][] coords = {
                {0, 0, 0},
                {width / 2, height / 2, depth / 2},
                {0, height - 1, depth - 1},
                {width - 1, 0, 0},
                {width - 1, height - 1, depth - 1}
        };

        grid = new ArrayAttributeGridByte(width, height, depth, 0.001, 0.001);
        HashSet<VoxelCoordinate> vcSetMat1 = new HashSet<VoxelCoordinate>();

        for (int i = 0; i < coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.INSIDE, mat1);
            vcSetMat1.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        FindAttributeIterateTester ft = new FindAttributeIterateTester(vcSetMat1);
        grid.findAttribute(mat1, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat1,
                ft.foundAllVoxels());

    }

    /**
     * Test that find voxels by material actually found the voxels in the correct coordinates
     */
    public void findInterruptablMaterialIterator1(AttributeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        long mat1 = 1;
        long mat2 = 2;

        HashSet<VoxelCoordinate> vcSetMat1 = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetMat2 = new HashSet<VoxelCoordinate>();

        for (int x = 0; x < width; x++) {
            grid.setData(x, 2, 2, Grid.INSIDE, mat1);
            grid.setData(x, 4, 4, Grid.INSIDE, mat1);
            vcSetMat1.add(new VoxelCoordinate(x, 2, 2));
            vcSetMat1.add(new VoxelCoordinate(x, 4, 4));

            grid.setData(x, 5, 6, Grid.INSIDE, mat2);
            vcSetMat2.add(new VoxelCoordinate(x, 5, 6));
        }

        FindAttributeIterateTester ft = new FindAttributeIterateTester(vcSetMat1);
        grid.findAttributeInterruptible(mat1, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat1,
                ft.foundAllVoxels());

        ft = new FindAttributeIterateTester(vcSetMat2);
        grid.findAttributeInterruptible(mat2, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat2,
                ft.foundAllVoxels());

        // make sure that findAttributeInterruptible stops interating when voxel is not found
        // do this by adding a new material voxel
        grid.setData(5, 2, 2, Grid.OUTSIDE, 0);
        grid.setData(1, 3, 3, Grid.INSIDE, mat1);
        ft = new FindAttributeIterateTester(vcSetMat1);
        grid.findAttributeInterruptible(mat1, ft);

        assertFalse("Found material interruptible iterator should return false",
                ft.foundAllVoxels());
        assertTrue("Found material interruptible did not get interrupted ",
                ft.getIterateCount() < vcSetMat1.size());

        // make sure that not finding a voxel in the list returns false
        // do this by changing one of the interior voxels to EXTERIOR state
        grid.setData(1, 5, 6, Grid.INSIDE, mat1);
        ft = new FindAttributeIterateTester(vcSetMat2);
        grid.findAttributeInterruptible(mat2, ft);

        assertFalse("Found material interruptible iterator should return false", ft.foundAllVoxels());
    }

    /**
     * Test that find voxels by material actually found the voxels in the correct coordinates
     */
    public void findInterruptablMaterialIterator2(AttributeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        long mat1 = 1;

        //-------------------------------------------------------
        // test on some random coordinates
        int[][] coords = {
                {0, 0, 0},
                {width / 2, height / 2, depth / 2},
                {0, height - 1, depth - 1},
                {width - 1, 0, 0},
                {width - 1, height - 1, depth - 1}
        };

        HashSet<VoxelCoordinate> vcSetMat1 = new HashSet<VoxelCoordinate>();

        for (int i = 0; i < coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.INSIDE, mat1);
            vcSetMat1.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        FindAttributeIterateTester ft = new FindAttributeIterateTester(vcSetMat1);
        grid.findAttributeInterruptible(mat1, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat1,
                ft.foundAllVoxels());

    }

    /**
     * Test that find voxels by VoxelClass and material actually found the voxels in the correct coordinates
     */
    public void findMaterialAndVCIterator1(AttributeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        long mat1 = 1;
        long mat2 = 2;

        HashSet<VoxelCoordinate> vcSetIntMat1 = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetIntMat2 = new HashSet<VoxelCoordinate>();

        for (int x = 0; x < width; x++) {
            grid.setData(x, 2, 2, Grid.INSIDE, mat1);
            vcSetIntMat1.add(new VoxelCoordinate(x, 2, 2));

            grid.setData(x, 5, 6, Grid.INSIDE, mat2);
            vcSetIntMat2.add(new VoxelCoordinate(x, 5, 6));
        }

        FindAttributeIterateTester ft = new FindAttributeIterateTester(vcSetIntMat1);
        grid.findAttribute(Grid.VoxelClasses.INSIDE, mat1, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state and material " + mat1,
                ft.foundAllVoxels());

        ft = new FindAttributeIterateTester(vcSetIntMat2);
        grid.findAttribute(Grid.VoxelClasses.INSIDE, mat2, ft);

        assertTrue("Found state iterator did not find all voxels with INSIDE state and material " + mat2,
                ft.foundAllVoxels());

        // make sure that finding a voxel not in the list returns false
        grid.setData(10, 6, 2, Grid.INSIDE, mat1);
        ft = new FindAttributeIterateTester(vcSetIntMat1);
        grid.findAttribute(Grid.VoxelClasses.INSIDE, mat1, ft);

        assertFalse("Found state and material iterator should return false",
                ft.foundAllVoxels());

        // make sure that not finding a voxel in the list returns false
        grid.setData(1, 5, 6, Grid.INSIDE, mat1);
        ft = new FindAttributeIterateTester(vcSetIntMat2);
        grid.findAttribute(Grid.VoxelClasses.INSIDE, ft);

        assertFalse("Found state and material iterator should return false",
                ft.foundAllVoxels());

    }

    /**
     * Test that find voxels by VoxelClass and material actually found the voxels in the correct coordinates
     */
    public void findMaterialAndVCIterator2(AttributeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        long mat1 = 1;

        //-------------------------------------------------------
        // test on some random coordinates
        int[][] coords = {
                {0, 0, 0},
                {width / 2, height / 2, depth / 2},
                {0, height - 1, depth - 1},
                {width - 1, 0, 0},
                {width - 1, height - 1, depth - 1}
        };

        HashSet<VoxelCoordinate> vcSetIntMat1 = new HashSet<VoxelCoordinate>();

        for (int i = 0; i < coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.INSIDE, mat1);
            vcSetIntMat1.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        FindAttributeIterateTester ft = new FindAttributeIterateTester(vcSetIntMat1);
        grid.findAttribute(Grid.VoxelClasses.INSIDE, mat1, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state and material " + mat1,
                ft.foundAllVoxels());

    }

    /**
     * Test that find voxels by voxel class and material actually found the voxels in the correct coordinates
     */
    public void findInterruptablMaterialAndVCIterator1(AttributeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        long mat1 = 1;
        long mat2 = 2;

        HashSet<VoxelCoordinate> vcSetIntMat1 = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetIntMat2 = new HashSet<VoxelCoordinate>();

        for (int x = 0; x < width; x++) {
            grid.setData(x, 2, 2, Grid.INSIDE, mat1);
            vcSetIntMat1.add(new VoxelCoordinate(x, 2, 2));

            grid.setData(x, 4, 4, Grid.INSIDE, mat1);
            vcSetIntMat1.add(new VoxelCoordinate(x, 4, 4));

            grid.setData(x, 7, 7, Grid.INSIDE, mat1);
            vcSetIntMat1.add(new VoxelCoordinate(x, 7, 7));

            grid.setData(x, 5, 6, Grid.INSIDE, mat2);
            vcSetIntMat2.add(new VoxelCoordinate(x, 5, 6));
        }

        FindAttributeIterateTester ft = new FindAttributeIterateTester(vcSetIntMat1);
        grid.findAttributeInterruptible(Grid.VoxelClasses.INSIDE, mat1, ft);
        assertTrue("Found iterator did not find all voxels with EXTERIOR state and material " + mat1,
                ft.foundAllVoxels());

        ft = new FindAttributeIterateTester(vcSetIntMat1);
        grid.findAttributeInterruptible(Grid.VoxelClasses.INSIDE, mat1, ft);
        assertTrue("Found iterator did not find all voxels with INSIDE state and material " + mat1,
                ft.foundAllVoxels());

        ft = new FindAttributeIterateTester(vcSetIntMat2);
        grid.findAttributeInterruptible(Grid.VoxelClasses.INSIDE, mat2, ft);
        assertTrue("Found iterator did not find all voxels with EXTERIOR state and material " + mat2,
                ft.foundAllVoxels());

        // make sure that findAttributeInterruptible stops interating when voxel is not found
        // do this by adding a new material voxel
        grid.setData(5, 2, 2, Grid.OUTSIDE, 0);
        grid.setData(1, 3, 3, Grid.INSIDE, mat1);
        ft = new FindAttributeIterateTester(vcSetIntMat1);
        grid.findAttributeInterruptible(Grid.VoxelClasses.INSIDE, mat1, ft);

        assertFalse("Found state and material interruptible iterator should return false",
                ft.foundAllVoxels());
        assertTrue("Found state and material interruptible did not get interrupted ",
                ft.getIterateCount() < vcSetIntMat1.size());

        // make sure that not finding a voxel in the list returns false
        // do this by changing one of the interior and mat2 voxels
        grid.setData(1, 5, 6, Grid.OUTSIDE, mat1);
        ft = new FindAttributeIterateTester(vcSetIntMat2);
        grid.findAttributeInterruptible(Grid.VoxelClasses.INSIDE, mat2, ft);

        assertFalse("Found state and material iterator should return false", ft.foundAllVoxels());

    }

    /**
     * Test that find voxels by voxel class and material actually found the voxels in the correct coordinates
     */
    public void findInterruptablMaterialAndVCIterator2(AttributeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        long mat1 = 1;

        //-------------------------------------------------------
        // test on some random coordinates
        int[][] coords = {
                {0, 0, 0},
                {width / 2, height / 2, depth / 2},
                {0, height - 1, depth - 1},
                {width - 1, 0, 0},
                {width - 1, height - 1, depth - 1}
        };

        HashSet<VoxelCoordinate> vcSetIntMat1 = new HashSet<VoxelCoordinate>();

        for (int i = 0; i < coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.INSIDE, mat1);
            vcSetIntMat1.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        FindAttributeIterateTester ft = new FindAttributeIterateTester(vcSetIntMat1);
        grid.findAttributeInterruptible(Grid.VoxelClasses.INSIDE, mat1, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state and material " + mat1,
                ft.foundAllVoxels());
    }


    /**
     * Test findCount by material.
     */
    public void findCountByMat(AttributeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        long material0 = 2;
        long material1 = 5;
        long material2 = 12;
        int[] materialDepth = {10, 6, 1};
        long[] material = {material0, material1, material2};

        // set some material data
        for (int x = 0; x < material.length; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < materialDepth[x]; z++) {
//System.out.println(x + ", " + y + ", " + z + ": " + material[x]);
                    grid.setData(x, y, z, Grid.INSIDE, material[x]);
                }
            }
        }

        int[] expectedCount = new int[material.length];

        for (int j = 0; j < material.length; j++) {
            expectedCount[j] = materialDepth[j] * height;
//System.out.println("count: " + expectedCount[j]);
            assertEquals("Material count for " + material[j] + " is not " + expectedCount[j], expectedCount[j], grid.findCount(material[j]));
        }

        // test material 1
        long mat = 1;
        grid = new ArrayAttributeGridByte(width, height, depth, 0.05, 0.02);
        for (int x = 0; x < width; x++) {
            grid.setData(x, 0, 0, Grid.INSIDE, mat);
        }

        assertEquals("Material count is not " + width, width, grid.findCount(mat));

        grid = new ArrayAttributeGridByte(width, height, depth, 0.05, 0.02);
        for (int y = 0; y < height; y++) {
            grid.setData(0, y, 0, Grid.INSIDE, mat);
        }

        assertEquals("Material count is not " + height, height, grid.findCount(mat));

    }


    /**
     * Test set/get byte material range.
     */
    public void byteMaterialRange(AttributeGrid grid) {
        int width = grid.getWidth();
        int maxMaterial = (int) Math.pow(2,8) - 1;
        long mat, expectedMat;

        for (int x = 0; x < width; x++) {
            grid.setData(x, 0, 0, Grid.INSIDE, x+1);
        }

        for (int x = 0; x < width; x++) {
            mat = grid.getAttribute(x, 0, 0);
            expectedMat = (x+1) % maxMaterial;
//System.out.println("Material [" + x + ",0,0]: " + mat);
            assertEquals("Material [" + x + ",0,0] is not " + expectedMat, expectedMat, mat);
        }

        for (int x = 0; x < width; x++) {
            grid.setAttribute(x, 0, 0, maxMaterial - x);
        }

        for (int x = 0; x < width; x++) {
            mat = grid.getAttribute(x, 0, 0);
            expectedMat = maxMaterial - x;
//System.out.println("Material [" + x + ",0,0]: " + mat);
            assertEquals("Material [" + x + ",0,0] is not " + expectedMat, expectedMat, mat);
        }
    }

    /**
     * Test set/get short material range.
     */
    public void shortMaterialRange(AttributeGrid grid) {
        int width = grid.getWidth();
        int maxMaterial = (int) Math.pow(2, 16);

        grid.setData(0, 0, 0, Grid.INSIDE, 1);
        grid.setData(1, 0, 0, Grid.INSIDE, maxMaterial - 1);

        assertEquals("Material [0,0,0] is not 1", 1, grid.getAttribute(0, 0, 0));
        assertEquals("Material [1,0,0] is not " + (maxMaterial - 1), (maxMaterial - 1), grid.getAttribute(1, 0, 0));

        grid.setAttribute(0, 0, 0, maxMaterial - 1);
        grid.setAttribute(1, 0, 0, 1);

        assertEquals("Material [0,0,0] is not " + (maxMaterial - 1), maxMaterial - 1, grid.getAttribute(0, 0, 0));
        assertEquals("Material [1,0,0] is not 1", 1, grid.getAttribute(1, 0, 0));

    }

    /**
     * Test set/get long material range.
     */
    public void intMaterialRange(AttributeGrid grid) {
        int width = grid.getWidth();
        int maxMaterial = (int) Math.pow(2, 32);

        grid.setData(0, 0, 0, Grid.INSIDE, 1);
        grid.setData(1, 0, 0, Grid.INSIDE, maxMaterial - 1);

        assertEquals("Material [0,0,0] is not 1", 1, grid.getAttribute(0, 0, 0));
        assertEquals("Material [1,0,0] is not " + (maxMaterial - 1), (maxMaterial - 1), grid.getAttribute(1, 0, 0));

        grid.setAttribute(0, 0, 0, maxMaterial - 1);
        grid.setAttribute(1, 0, 0, 1);

        assertEquals("Material [0,0,0] is not " + (maxMaterial - 1), maxMaterial - 1, grid.getAttribute(0, 0, 0));
        assertEquals("Material [1,0,0] is not 1", 1, grid.getAttribute(1, 0, 0));
    }


    /**
     * Test setState.
     */
    public void setState(AttributeGrid grid) {
        double vs = 0.001;

        grid.setData(0, 0, 0, Grid.INSIDE, 1);
        grid.setData(9, 9, 9, Grid.INSIDE, 2);
        grid.setData(5, 0, 7, Grid.INSIDE, 3);

        grid.setState(0, 0, 0, Grid.INSIDE);
        grid.setState(9, 9, 9, Grid.INSIDE);
        grid.setState(5, 0, 7, Grid.INSIDE);
        grid.setState(6 * vs, 0, 7 * vs , Grid.INSIDE);

        // check that the state changed, but the material did not
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0, 0, 0));
        assertEquals("Material should be ", 1, grid.getAttribute(0, 0, 0));

        assertEquals("State should be ", Grid.INSIDE, grid.getState(9, 9, 9));
        assertEquals("Material should be ", 2, grid.getAttribute(9, 9, 9));

        assertEquals("State should be ", Grid.INSIDE, grid.getState(5, 0, 7));
        assertEquals("Material should be ", 3, grid.getAttribute(5, 0, 7));

        assertEquals("State should be ", Grid.INSIDE, grid.getState(6 * vs, 0, 7 * vs));
    }

    /**
     * Test reassignAttribute.
     */
    public void reassignMaterial(AttributeGrid grid) {
        // Fill voxels such that it looks like:
        //
        //      2  11111
        //      2
        //      2  33 33
        //
        setX(grid, 10, 10, Grid.INSIDE, 1, 8, 12);
        setX(grid, 8, 10, Grid.INSIDE, 3, 8, 12);
        grid.setState(10, 8, 10, Grid.OUTSIDE);
        setY(grid, 5, 10, Grid.INSIDE, 2, 8, 10);

        int newMaterial = 10;

        // reassign a non-existing material
        grid.reassignAttribute(new long[]{50}, newMaterial);
        assertEquals(0, grid.findCount(50));
        assertEquals(5, grid.findCount(1));
        assertEquals(3, grid.findCount(2));
        assertEquals(4, grid.findCount(3));

        // reassign a single existing material
        // check that the original material count is 0
        // check that the material has changed for the set positions
        grid.reassignAttribute(new long[]{1}, newMaterial);

        assertEquals(0, grid.findCount(1));

        for (int i = 8; i <= 12; i++) {
            assertEquals("State should be ", Grid.INSIDE, grid.getState(i, 10, 10));
            assertEquals("Material should be ", newMaterial, grid.getAttribute(i, 10, 10));
        }

        // reassign several material
        // check that the original material count is 0
        // check that the material has changed for the set positions
        newMaterial = 20;
        grid.reassignAttribute(new long[]{2, 3, 10}, newMaterial);

        assertEquals(0, grid.findCount(2));
        assertEquals(0, grid.findCount(3));
        assertEquals(0, grid.findCount(10));

        for (int i = 8; i <= 12; i++) {
            assertEquals("State should be ", Grid.INSIDE, grid.getState(i, 10, 10));
            assertEquals("Material should be ", newMaterial, grid.getAttribute(i, 10, 10));
        }

        for (int i = 8; i <= 9; i++) {
            assertEquals("State should be ", Grid.INSIDE, grid.getState(i, 8, 10));
            assertEquals("Material should be ", newMaterial, grid.getAttribute(i, 8, 10));
        }

        for (int i = 11; i <= 12; i++) {
            assertEquals("State should be ", Grid.INSIDE, grid.getState(i, 8, 10));
            assertEquals("Material should be ", newMaterial, grid.getAttribute(i, 8, 10));
        }

        for (int i = 8; i <= 10; i++) {
            assertEquals("State should be ", Grid.INSIDE, grid.getState(5, i, 10));
            assertEquals("Material should be ", newMaterial, grid.getAttribute(5, i, 10));
        }
    }


    /**
     * Test getAttribute by world coordinates.
     */
    public void getMaterialByCoord1(AttributeGrid grid) {
        // OUTSIDE voxels cannot contain attributes
        //grid.setData(0.0, 0.0, 0.0, Grid.OUTSIDE, (byte) 3);

        // set and test get on some random world coordinates
        grid.setData(0.95, 0.39, 0.45, Grid.INSIDE, (byte) 2);
        grid.setData(0.6, 0.1, 0.4, Grid.INSIDE, (byte) 1);
        //assertEquals("State should be ", 3, grid.getAttribute(0.0, 0.0, 0.0));
        assertEquals("State should be ", 2, grid.getAttribute(0.95, 0.39, 0.45));
        assertEquals("State should be ", 1, grid.getAttribute(0.6, 0.1, 0.4));

    }

    /**
     * Test getAttribute by world coordinates.
     */
    public void getMaterialByCoord2(AttributeGrid grid) {

        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid.setData(0.06, 0.07, 0.08, Grid.INSIDE, (byte) 2);
        assertEquals("State should be ", 2, grid.getAttribute(0.05, 0.06, 0.05));
        assertEquals("State should be ", 2, grid.getAttribute(0.0999, 0.06, 0.05));
        assertEquals("State should be ", 2, grid.getAttribute(0.05, 0.0799, 0.05));
        assertEquals("State should be ", 2, grid.getAttribute(0.05, 0.06, 0.0999));
        assertEquals("State should be ", 2, grid.getAttribute(0.0999, 0.0799, 0.0999));
        assertEquals("State should be ", 0, grid.getAttribute(0.0499, 0.06, 0.05));
        assertEquals("State should be ", 0, grid.getAttribute(0.05, 0.0599, 0.05));
        assertEquals("State should be ", 0, grid.getAttribute(0.05, 0.06, 0.0499));
        assertEquals("State should be ", 0, grid.getAttribute(0.1, 0.06, 0.05));
        assertEquals("State should be ", 0, grid.getAttribute(0.05, 0.08, 0.05));
        assertEquals("State should be ", 0, grid.getAttribute(0.05, 0.06, 0.1));

        // set data for beginning voxel 0,0,0 and test the bounds
        grid.setData(0.0, 0.0, 0.0, Grid.INSIDE, (byte) 5);
        assertEquals("State should be ", 5, grid.getAttribute(0.0, 0.0, 0.0));
        assertEquals("State should be ", 5, grid.getAttribute(0.0499, 0.0, 0.0));
        assertEquals("State should be ", 5, grid.getAttribute(0.0, 0.0199, 0.0));
        assertEquals("State should be ", 5, grid.getAttribute(0.0, 0.0, 0.0499));
        assertEquals("State should be ", 5, grid.getAttribute(0.0499, 0.0199, 0.0499));
        assertEquals("State should be ", 0, grid.getAttribute(0.05, 0.0, 0.0));
        assertEquals("State should be ", 0, grid.getAttribute(0.0, 0.02, 0.0));
        assertEquals("State should be ", 0, grid.getAttribute(0.0, 0.0, 0.05));

        // set data for last voxel 2,5,3 and test the bounds
        grid.setData(0.149, 0.119, 0.199, Grid.INSIDE, (byte) 12);
//        assertEquals("State should be ", 12, grid.getAttribute(0.1, 0.1, 0.15)); //failing because 0.15/0.05=2.999997
        assertEquals("State should be ", 12, grid.getAttribute(0.1499, 0.1, 0.1501));
        assertEquals("State should be ", 12, grid.getAttribute(0.1, 0.119, 0.1501));
        assertEquals("State should be ", 12, grid.getAttribute(0.1, 0.1, 0.199));
        assertEquals("State should be ", 12, grid.getAttribute(0.1499, 0.1199, 0.1999));
        assertEquals("State should be ", 0, grid.getAttribute(0.0999, 0.1, 0.1501));
        assertEquals("State should be ", 0, grid.getAttribute(0.1, 0.0999, 0.1501));
        assertEquals("State should be ", 0, grid.getAttribute(0.1, 0.1, 0.1499));
        assertEquals("State should be ", 0, grid.getAttribute(0.0999, 0.0999, 0.1499));
    }

    /**
     * Test setAttribute.
     */
    public void setAttribute(AttributeGrid grid) {
        grid.setData(0, 0, 0, Grid.INSIDE, 1);
        grid.setData(9, 9, 9, Grid.INSIDE, 2);
        grid.setData(5, 0, 7, Grid.INSIDE, 3);

        grid.setAttribute(0, 0, 0, 10);
        grid.setAttribute(9, 9, 9, 11);
        grid.setAttribute(5, 0, 7, 12);

        // check that the material changed, but the state did not
        assertEquals("Material should be ", 10, grid.getAttribute(0, 0, 0));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0, 0, 0));

        assertEquals("Material should be ", 11, grid.getAttribute(9, 9, 9));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(9, 9, 9));

        assertEquals("Material should be ", 12, grid.getAttribute(5, 0, 7));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(5, 0, 7));
    }


    /**
     * Test getAttribute by voxels.
     */
    public void getMaterialByVoxel(AttributeGrid grid) {
        // Removed outside as it doesn't have to carry state
        //grid.setData(0, 0, 0, Grid.OUTSIDE, (byte)3);
        grid.setData(9, 8, 7, Grid.INSIDE, (byte) 2);
        grid.setData(5, 0, 7, Grid.INSIDE, (byte) 1);

        assertEquals("State should be ", 2, grid.getAttribute(9, 8, 7));
        assertEquals("State should be ", 1, grid.getAttribute(5, 0, 7));

        // Index that are not set should default to 0
        assertEquals("State should be ", 0, grid.getAttribute(2, 2, 2));
    }


    /**
     * Test getData by voxels.
     */
    public void getDataByCoord(AttributeGrid grid) {
        grid.setData(0.0, 0.0, 0.0, Grid.OUTSIDE, 0);
        grid.setData(0.95, 0.39, 0.45, Grid.INSIDE, (byte) 1);

        VoxelData vd = grid.getVoxelData();
        grid.getData(0.0, 0.0, 0.0, vd);
        assertEquals("State should be ", Grid.OUTSIDE, vd.getState());
        grid.getData(0.95, 0.39, 0.45, vd);
        assertEquals("State should be ", Grid.INSIDE, vd.getState());
    }


    /**
     * Test getData by voxels.
     */
    public void getDataByVoxel(AttributeGrid grid) {
        grid.setData(0, 0, 0, Grid.OUTSIDE, 0);
        grid.setData(9, 8, 7, Grid.INSIDE, (byte) 1);

        VoxelData vd = grid.getVoxelData();

        grid.getData(0, 0, 0, vd);
        assertEquals("State should be ", Grid.OUTSIDE, vd.getState());
        grid.getData(9, 8, 7, vd);
        assertEquals("State should be ", Grid.INSIDE, vd.getState());

        // Index that are not set should default to 0
        assertEquals("State should be ", 0, grid.getState(2, 2, 2));
    }


    /**
     * Test that remove material removes all specified material
     */
    public void removeMaterialIterator(AttributeGrid grid) {
        int width = grid.getWidth();
        long mat1 = 1;
        long mat2 = 2;

        HashSet<VoxelCoordinate> vcSetMat1 = new HashSet<VoxelCoordinate>();
        HashSet<VoxelCoordinate> vcSetMat2 = new HashSet<VoxelCoordinate>();

        for (int x = 0; x < width; x++) {
            grid.setData(x, 2, 2, Grid.INSIDE, mat1);
            vcSetMat1.add(new VoxelCoordinate(x, 2, 2));

            grid.setData(x, 4, 4, Grid.INSIDE, mat1);
            vcSetMat1.add(new VoxelCoordinate(x, 4, 4));

            grid.setData(x, 5, 6, Grid.INSIDE, mat2);
            vcSetMat2.add(new VoxelCoordinate(x, 5, 6));
        }

        // make sure that all coordinates in list have been set to mat1 in grid
        FindAttributeIterateTester ft = new FindAttributeIterateTester(vcSetMat1);
        grid.findAttribute(mat1, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat1,
                ft.foundAllVoxels());

        // remove all mat1
        grid.removeAttribute(mat1);

        // check that find mat1 returns false and iterate count returns zero
        ft = new FindAttributeIterateTester(vcSetMat1);
        grid.findAttribute(mat1, ft);

        assertFalse("Found iterator did not return false after removing material " + mat1,
                ft.foundAllVoxels());

        assertEquals("Found iterate count is not 0 after removing material", 0, ft.getIterateCount());

        // make sure that all coordinates in list are no longer mat1 in grid
        // note: probably redundant since the above assertions have passed
        Iterator iter = vcSetMat1.iterator();

        while (iter.hasNext()) {
            VoxelCoordinate vc = (VoxelCoordinate) iter.next();
//          System.out.println(vc.getX() + ", " + vc.getY() + ", " + vc.getZ());
            assertEquals("Material is not 0 after removal for coordinate: " +
                    vc.getX() + ", " + vc.getY() + ", " + vc.getZ(),
                    0,
                    grid.getAttribute(vc.getX(), vc.getY(), vc.getZ()));
        }

        // make sure other material has not been removed
        ft = new FindAttributeIterateTester(vcSetMat2);
        grid.findAttribute(mat2, ft);

        assertTrue("Found iterator did not find all voxels with material " + mat2,
                ft.foundAllVoxels());

    }


    /**
     * Set and get all values of a grid using voxel coords
     *
     * @param grid The grid to test
     */
    public void setGetAllVoxelCoords(AttributeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    grid.setData(x, y, z, Grid.INSIDE, 1);
                }
            }
        }

        VoxelData vd = grid.getVoxelData();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    grid.getData(x, y, z, vd);
                    assertTrue("State wrong", vd.getState() == Grid.INSIDE);
                    assertTrue("Material wrong", vd.getMaterial() == 1);
                }
            }
        }
    }

    /**
     * Set and get all values of a grid using voxel coords using stripped
     * exterior/interior pattern.
     *
     * @param grid The grid to test
     */
    public void setGetAllVoxelCoordsStripped(AttributeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    if ((x % 2) == 0 && (y % 2) == 0 && (z % 2) == 0) {
                        grid.setData(x, y, z, Grid.INSIDE, 1);
                    } else {
                        grid.setData(x, y, z, Grid.INSIDE, 2);
                    }

                }
            }
        }

        VoxelData vd = grid.getVoxelData();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    grid.getData(x, y, z, vd);
//System.out.println(x + ", " + y + ", " + z + ": " + vd.getState());
                    if ((x % 2) == 0 && (y % 2) == 0 && (z % 2) == 0) {
                        assertTrue("State wrong", vd.getState() == Grid.INSIDE);
                        assertTrue("Material wrong", vd.getMaterial() == 1);
                    } else {
                        assertTrue("State wrong", vd.getState() == Grid.INSIDE);
                        assertTrue("Material wrong", vd.getMaterial() == 2);
                    }
                }
            }
        }
    }

    /**
     * Set and get all values of a grid using voxel coords using stripped
     * exterior/interior pattern.
     *
     * @param grid The grid to test
     */
    public void setGetAllVoxelCoordsDiagonal(AttributeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    if (x == y && y == z) {
                        grid.setData(x, y, z, Grid.INSIDE, 1);
                    }
                }
            }
        }

        VoxelData vd = grid.getVoxelData();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    grid.getData(x, y, z, vd);
//System.out.println(x + ", " + y + ", " + z + ": " + vd.getState());
                    if (x == y && y == z) {
                        assertTrue("State wrong", vd.getState() == Grid.INSIDE);
                        assertTrue("Material wrong", vd.getMaterial() == 1);
                    }
                }
            }
        }
    }

    /**
     * Set and get all values of a grid using world coords
     *
     * @param grid The grid to test
     */
    public void setGetAllVoxelByWorldCoords(AttributeGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        double voxelSize = grid.getVoxelSize();
        double sliceHeight = grid.getSliceHeight();

        double xcoord, ycoord, zcoord;

        for (int x = 0; x < width; x++) {
            xcoord = (double) (x) * voxelSize + voxelSize / 2.0;
            for (int y = 0; y < height; y++) {
                ycoord = (double) (y) * sliceHeight + sliceHeight / 2.0;
                for (int z = 0; z < depth; z++) {
                    zcoord = (double) (z) * voxelSize + voxelSize / 2.0;
                    grid.setData(xcoord, ycoord, zcoord, Grid.INSIDE, (byte) 1);
                }
            }
        }

        VoxelData vd = grid.getVoxelData();

        for (int x = 0; x < width; x++) {
            xcoord = (double) (x) * voxelSize + voxelSize / 2.0;
            for (int y = 0; y < height; y++) {
                ycoord = (double) (y) * sliceHeight + sliceHeight / 2.0;
                for (int z = 0; z < depth; z++) {
                    zcoord = (double) (z) * voxelSize + voxelSize / 2.0;
                    grid.getData(xcoord, ycoord, zcoord, vd);
//System.out.println(x + ", " + y + ", " + z + ": " + vd.getState());
                    assertTrue("State wrong", vd.getState() == Grid.INSIDE);
                    assertTrue("Material wrong", vd.getMaterial() == 1);
                }
            }
        }
    }

    /**
     * Set the X values of a grid with a given Y and Z to the given state and material.
     *
     * @param state      The new state
     * @param mat        The new material
     * @param startIndex The starting X index
     * @param endIndex   The ending X Index
     */
    protected static void setX(AttributeGrid grid, int y, int z, byte state, long mat, int startIndex, int endIndex) {
        for (int x = startIndex; x <= endIndex; x++) {
            grid.setData(x, y, z, state, mat);
        }
    }

    /**
     * Set the X values of a grid.
     *
     * @param state The new state
     * @param mat   The new material
     */
    protected static void setX(AttributeGrid grid, int y, int z, byte state, byte mat, int startIndex, int endIndex) {
        for (int x = startIndex; x <= endIndex; x++) {
            grid.setData(x, y, z, state, mat);
        }
    }


    /**
     * Set the Y values of a grid with a given X and Z to the given state and material.
     *
     * @param state      The new state
     * @param mat        The new material
     * @param startIndex The starting Y index
     * @param endIndex   The ending Y Index
     */
    protected static void setY(AttributeGrid grid, int x, int z, byte state, long mat, int startIndex, int endIndex) {
        for (int y = startIndex; y <= endIndex; y++) {
            grid.setData(x, y, z, state, mat);
        }
    }

    /**
     * Set the Z values of a grid with a given X and Y to the given state and material.
     *
     * @param state      The new state
     * @param mat        The new material
     * @param startIndex The starting Z index
     * @param endIndex   The ending Z Index
     */
    protected static void setZ(AttributeGrid grid, int x, int y, byte state, long mat, int startIndex, int endIndex) {
        for (int z = startIndex; z <= endIndex; z++) {
            grid.setData(x, y, z, state, mat);
        }
    }


    /**
     * Set the data for an X plane.
     *
     * @param grid     The grid to set
     * @param x        The X plane to set
     * @param state    The new state
     * @param material The new material
     */
    protected static void setPlaneX(AttributeGrid grid, int x, byte state, long material) {
        int height = grid.getHeight();
        int depth = grid.getDepth();

        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                grid.setData(x, y, z, state, material);
            }
        }
    }

    /**
     * Set the data for a Y plane.
     *
     * @param grid     The grid to set
     * @param y        The Y plane to set
     * @param state    The new state
     * @param material The new material
     */
    protected static void setPlaneY(AttributeGrid grid, int y, byte state, long material) {
        int width = grid.getWidth();
        int depth = grid.getDepth();

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                grid.setData(x, y, z, state, material);
            }
        }
    }

    /**
     * Set the data for a Z plane.
     *
     * @param grid     The grid to set
     * @param z        The Z plane to set
     * @param state    The new state
     * @param material The new material
     */
    protected static void setPlaneZ(AttributeGrid grid, int z, byte state, long material) {
        int width = grid.getWidth();
        int height = grid.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid.setData(x, y, z, state, material);
            }
        }
    }

    /**
     * A voxel of the class requested has been found.
     *
     * @param x  The x grid coordinate
     * @param y  The y grid coordinate
     * @param z  The z grid coordinate
     * @param vd The voxel data
     */
    public void found(int x, int y, int z, VoxelData vd) {
        allCount++;

        if (vd.getState() == Grid.INSIDE) {
            mrkCount++;
            extCount++;
        } else if (vd.getState() == Grid.INSIDE) {
            mrkCount++;
            intCount++;
        } else {
            outCount++;
        }

    }

    /**
     * A voxel of the class requested has been found.
     *
     * @param x  The x grid coordinate
     * @param y  The y grid coordinate
     * @param z  The z grid coordinate
     * @param vd The voxel data
     */
    public boolean foundInterruptible(int x, int y, int z, VoxelData vd) {
        // ignore
        return true;
    }
}

/**
 * Class to test that the find methods actually found the voxel states in the correct coordinate.
 *
 * @author Tony
 */
class FindAttributeIterateTester implements ClassAttributeTraverser {
    private boolean foundCorrect;
    private HashSet<VoxelCoordinate> vcSet;
    private int iterateCount;
    private int vcSetCount;

    /**
     * Constructor that takes in a HashSet of VoxelCoordinates known to be
     * in the VoxelClass to find
     *
     * @param vc
     */
    public FindAttributeIterateTester(HashSet<VoxelCoordinate> vc, boolean threadSafe) {
        this.vcSet = (HashSet<VoxelCoordinate>) vc.clone();
        foundCorrect = true;
        iterateCount = 0;
        vcSetCount = vcSet.size();
    }

    /**
     * Constructor that takes in a HashSet of VoxelCoordinates known to be
     * in the VoxelClass to find
     *
     * @param vc
     */
    public FindAttributeIterateTester(HashSet<VoxelCoordinate> vc) {
        this(vc,false);
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x  The x grid coordinate
     * @param y  The y grid coordinate
     * @param z  The z grid coordinate
     * @param vd The voxel data
     */
    public void found(int x, int y, int z, VoxelData vd) {
        VoxelCoordinate c = new VoxelCoordinate(x, y, z);
//System.out.println(x + ", " + y + ", " + z);
        if (!inCoordList(c)) {
//System.out.println("not in cood list: " + x + ", " + y + ", " + z);
            foundCorrect = false;
        }

        iterateCount++;
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x  The x grid coordinate
     * @param y  The y grid coordinate
     * @param z  The z grid coordinate
     * @param vd The voxel data
     * @return True to continue, false stops the traversal.
     */
    public boolean foundInterruptible(int x, int y, int z, VoxelData vd) {
        VoxelCoordinate c = new VoxelCoordinate(x, y, z);
//System.out.println(x + ", " + y + ", " + z);
        if (!inCoordList(c)) {
//System.out.println("not in cood list: " + x + ", " + y + ", " + z);
            foundCorrect = false;
            return false;
        }

        iterateCount++;
        return true;
    }

    /**
     * Returns whether all voxels have been found, and that the number of
     * times iterated through the grid is equal to the expected value.
     *
     * @return True if voxels were found correctly
     */
    public boolean foundAllVoxels() {
//System.out.println("iterateCount: " + iterateCount);
//System.out.println("vcSetCount: " + vcSetCount);
        return (foundCorrect && (iterateCount == vcSetCount));
    }

    /**
     * Returns the number of times voxels of the correct state was found.
     *
     * @return count of the times voxels of the correct state was found\
     */
    public int getIterateCount() {
        return iterateCount;
    }

    /**
     * Check if the VoxelCoordinate is in the known list, and removes
     * it from the list if found.
     *
     * @param c The voxel coordinate
     * @return True if the voxel coordinate is in the know list
     */
    private boolean inCoordList(VoxelCoordinate c) {
        if (vcSet.contains(c)) {
            vcSet.remove(c);
            return true;
        }

        return false;
    }
}

