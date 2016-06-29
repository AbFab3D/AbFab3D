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
import abfab3d.core.AttributeGrid;
import abfab3d.core.Grid;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.*;
import org.j3d.geom.GeometryData;
import org.j3d.geom.TorusGenerator;
//import org.web3d.util.ErrorReporter;

// Internal Imports
import abfab3d.grid.*;
import abfab3d.geom.TriangleModelCreator;

/**
 * Tests the functionality of the BoxRegionFinder class
 *
 * @author Alan Hudson
 * @version
 */
public class TestBoxRegionFinder extends TestCase {
    /** Horizontal resolution of the printer in meters.  */
    public static final double HORIZ_RESOLUTION = 0.004;

    /** Vertical resolution of the printer in meters.  */
    public static final double VERT_RESOLUTION = 0.004;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestBoxRegionFinder.class);
    }

    public void testTorus() {
        float ir = 0.04f;
        float or = 0.16f;
        int facets = 64;
        byte outerMaterial = 1;
        byte innerMaterial = 2;

        AttributeGrid grid = createTorusInGrid(ir, or, facets, outerMaterial, innerMaterial,
                GeometryData.TRIANGLES, false, new int[] {0,1,0});

        FindRegionRecorder rc1 = new FindRegionRecorder();

        BoxRegionFinder rf = new BoxRegionFinder();
        Set<Region> regions = rf.execute(grid);

        Iterator<Region> itr = regions.iterator();
        int[] size = new int[3];

System.out.println("Regions for y: " + regions.size());
        while(itr.hasNext()) {
            Region r = itr.next();

            if (r instanceof BoxRegion) {
                BoxRegion br = (BoxRegion) r;
                br.getSize(size);

//System.out.println("size: " + java.util.Arrays.toString(size));
                br.traverse(rc1);
            }
        }

        grid = createTorusInGrid(ir, or, facets, outerMaterial, innerMaterial,
                GeometryData.TRIANGLES, false, new int[] {0,0,1});

        FindRegionRecorder rc2 = new FindRegionRecorder();

        rf = new BoxRegionFinder();
        regions = rf.execute(grid);

System.out.println("Regions for z: " + regions.size());

        itr = regions.iterator();
        size = new int[3];

        while(itr.hasNext()) {
            Region r = itr.next();

            if (r instanceof BoxRegion) {
                BoxRegion br = (BoxRegion) r;
                br.getSize(size);

//System.out.println("size: " + java.util.Arrays.toString(size));
                br.traverse(rc2);
            }
        }

        boolean equals = rc1.compare(rc2.getCoords());
        assertTrue("Regions Equal", equals);

        grid = createTorusInGrid(ir, or, facets, outerMaterial, innerMaterial,
                GeometryData.TRIANGLES, false, new int[] {1,0,0});

        FindRegionRecorder rc3 = new FindRegionRecorder();

        rf = new BoxRegionFinder();
        regions = rf.execute(grid);

System.out.println("Regions for x: " + regions.size());

        itr = regions.iterator();
        size = new int[3];

        while(itr.hasNext()) {
            Region r = itr.next();

            if (r instanceof BoxRegion) {
                BoxRegion br = (BoxRegion) r;
                br.getSize(size);

//System.out.println("size: " + java.util.Arrays.toString(size));
                br.traverse(rc3);
            }
        }

        equals = rc1.compare(rc3.getCoords());
        assertTrue("Regions Equal", equals);
    }

    /**
     * Test edge conditions
     */
    public void testYBreakIt() {
        AttributeGrid grid = new ArrayAttributeGridByte(4,6,5,0.1,0.1);

        HashSet<VoxelCoordinate> vcSetInt = new HashSet<VoxelCoordinate>();
        vcSetInt.add(new VoxelCoordinate(2,0,1));
        vcSetInt.add(new VoxelCoordinate(3,0,1));
        vcSetInt.add(new VoxelCoordinate(3,0,2));
        vcSetInt.add(new VoxelCoordinate(3,1,1));
        vcSetInt.add(new VoxelCoordinate(3,1,2));
        vcSetInt.add(new VoxelCoordinate(3,1,3));
        vcSetInt.add(new VoxelCoordinate(3,2,1));
        vcSetInt.add(new VoxelCoordinate(3,2,2));
        vcSetInt.add(new VoxelCoordinate(3,2,3));
        vcSetInt.add(new VoxelCoordinate(3,2,4));
        vcSetInt.add(new VoxelCoordinate(3,3,1));
        vcSetInt.add(new VoxelCoordinate(3,3,2));
        vcSetInt.add(new VoxelCoordinate(3,3,3));
        vcSetInt.add(new VoxelCoordinate(3,3,4));

        Iterator<VoxelCoordinate> vc_itr = vcSetInt.iterator();
        while(vc_itr.hasNext()) {
            VoxelCoordinate vc = vc_itr.next();
            grid.setData(vc.getX(), vc.getY(), vc.getZ(), Grid.INSIDE,1);
        }

        FindRegionTester ft = new FindRegionTester(vcSetInt);

        BoxRegionFinder rf = new BoxRegionFinder();
        Set<Region> regions = rf.execute(grid);

        assertNotNull("Regions", regions);

        int vol = 0;
        Iterator<Region> itr = regions.iterator();
        int[] size = new int[3];

        while(itr.hasNext()) {
            Region r = itr.next();

            if (r instanceof BoxRegion) {
                BoxRegion br = (BoxRegion) r;
                br.getSize(size);

System.out.println("size: " + java.util.Arrays.toString(size));
                vol += size[0] * size[1] * size[2];

                br.traverse(ft);
            }
        }

        assertTrue("Found iterator did not find all voxels with EXTERIOR state",
                ft.foundAllVoxels());

System.out.println("vol: " + vol);
        assertEquals("Correct Volume", 14, vol);

    }

    /**
     * Test edge conditions
     */
    public void testZBreakIt() {
        AttributeGrid grid = new ArrayAttributeGridByte(4,5,6,0.1,0.1);

        HashSet<VoxelCoordinate> vcSetInt = new HashSet<VoxelCoordinate>();
        vcSetInt.add(new VoxelCoordinate(2,0,1));
        vcSetInt.add(new VoxelCoordinate(3,0,1));
        vcSetInt.add(new VoxelCoordinate(3,0,2));
        vcSetInt.add(new VoxelCoordinate(3,1,1));
        vcSetInt.add(new VoxelCoordinate(3,1,2));
        vcSetInt.add(new VoxelCoordinate(3,1,3));
        vcSetInt.add(new VoxelCoordinate(3,2,1));
        vcSetInt.add(new VoxelCoordinate(3,2,2));
        vcSetInt.add(new VoxelCoordinate(3,2,3));
        vcSetInt.add(new VoxelCoordinate(3,2,4));
        vcSetInt.add(new VoxelCoordinate(3,3,1));
        vcSetInt.add(new VoxelCoordinate(3,3,2));
        vcSetInt.add(new VoxelCoordinate(3,3,3));
        vcSetInt.add(new VoxelCoordinate(3,3,4));

        Iterator<VoxelCoordinate> vc_itr = vcSetInt.iterator();
        while(vc_itr.hasNext()) {
            VoxelCoordinate vc = vc_itr.next();
            grid.setData(vc.getX(), vc.getY(), vc.getZ(), Grid.INSIDE,1);
        }

        FindRegionTester ft = new FindRegionTester(vcSetInt);

        BoxRegionFinder rf = new BoxRegionFinder();
        Set<Region> regions = rf.execute(grid);

        assertNotNull("Regions", regions);

        int vol = 0;
        Iterator<Region> itr = regions.iterator();
        int[] size = new int[3];

        while(itr.hasNext()) {
            Region r = itr.next();

            if (r instanceof BoxRegion) {
                BoxRegion br = (BoxRegion) r;
                br.getSize(size);

System.out.println("size: " + java.util.Arrays.toString(size));
                vol += size[0] * size[1] * size[2];

                br.traverse(ft);
            }
        }

        assertTrue("Found iterator did not find all voxels with EXTERIOR state",
                ft.foundAllVoxels());

System.out.println("vol: " + vol);
        assertEquals("Correct Volume", 14, vol);

    }

    /**
     * Test basic operation
     */
    public void testZCentric() {
        AttributeGrid grid = new ArrayAttributeGridByte(20,20,30,0.1,0.1);

        // Create a simple region
        grid.setData(5,5,5,Grid.INSIDE,1);
        grid.setData(5,5,6,Grid.INSIDE,1);
        grid.setData(5,5,7,Grid.INSIDE,1);
        grid.setData(5,6,5,Grid.INSIDE,1);
        grid.setData(5,6,6,Grid.INSIDE,1);
        grid.setData(5,6,7,Grid.INSIDE,1);

        BoxRegionFinder rf = new BoxRegionFinder();
        Set<Region> regions = rf.execute(grid);

        assertNotNull("Regions", regions);

        // Total volume should equal 6
        int vol = 0;
        Iterator<Region> itr = regions.iterator();
        int[] size = new int[3];

        while(itr.hasNext()) {
            Region r = itr.next();

            if (r instanceof BoxRegion) {
                BoxRegion br = (BoxRegion) r;
                br.getSize(size);

                vol += size[0] * size[1] * size[2];
            }
        }

        assertEquals("Correct Volume", 6, vol);
    }

    /**
     * Test basic operation
     */
    public void testYCentric() {
        AttributeGrid grid = new ArrayAttributeGridByte(20,30,20,0.1,0.1);

        // Create a simple region
        grid.setData(5,5,5,Grid.INSIDE,1);
        grid.setData(5,5,6,Grid.INSIDE,1);
        grid.setData(5,5,7, Grid.INSIDE,1);
        grid.setData(5,6,5,Grid.INSIDE,1);
        grid.setData(5,6,6,Grid.INSIDE,1);
        grid.setData(5,6,7,Grid.INSIDE,1);

        BoxRegionFinder rf = new BoxRegionFinder();
        Set<Region> regions = rf.execute(grid);

        assertNotNull("Regions", regions);

        // Total volume should equal 6
        int vol = 0;
        Iterator<Region> itr = regions.iterator();
        int[] size = new int[3];

        while(itr.hasNext()) {
            Region r = itr.next();

            if (r instanceof BoxRegion) {
                BoxRegion br = (BoxRegion) r;
                br.getSize(size);

                vol += size[0] * size[1] * size[2];
            }
        }

        assertEquals("Correct Volume", 6,vol);
    }

    /**
     * Test edge conditions
     */
    public void testEdgeEnd() {
        AttributeGrid grid = new ArrayAttributeGridByte(4,5,4,0.1,0.1);

        // Create a simple region
        grid.setData(3,3,1,Grid.INSIDE,1);
        grid.setData(3,3,2,Grid.INSIDE,1);
        grid.setData(3,3,3,Grid.INSIDE,1);
        grid.setData(3,4,1,Grid.INSIDE,1);
        grid.setData(3,4,2,Grid.INSIDE,1);
        grid.setData(3,4,3,Grid.INSIDE,1);

        BoxRegionFinder rf = new BoxRegionFinder();
        Set<Region> regions = rf.execute(grid);

        assertNotNull("Regions", regions);

        // Total volume should equal 6
        int vol = 0;
        Iterator<Region> itr = regions.iterator();
        int[] size = new int[3];

        while(itr.hasNext()) {
            Region r = itr.next();

            if (r instanceof BoxRegion) {
                BoxRegion br = (BoxRegion) r;
                br.getSize(size);

System.out.println("size: " + java.util.Arrays.toString(size));
                vol += size[0] * size[1] * size[2];
            }
        }

System.out.println("vol: " + vol);
        assertEquals("Correct Volume", 6, vol);
    }

    /**
     * Test edge conditions
     */
    public void testEdgeStart() {
        AttributeGrid grid = new ArrayAttributeGridByte(4,5,4,0.1,0.1);

        // Create a simple region
        grid.setData(3,0,0,Grid.INSIDE,1);
        grid.setData(3,0,1,Grid.INSIDE,1);
        grid.setData(3,0,2,Grid.INSIDE,1);
        grid.setData(3,1,0,Grid.INSIDE,1);
        grid.setData(3,1,1,Grid.INSIDE,1);
        grid.setData(3,1,2,Grid.INSIDE,1);

        BoxRegionFinder rf = new BoxRegionFinder();
        Set<Region> regions = rf.execute(grid);

        assertNotNull("Regions", regions);

        // Total volume should equal 6
        int vol = 0;
        Iterator<Region> itr = regions.iterator();
        int[] size = new int[3];

        while(itr.hasNext()) {
            Region r = itr.next();

            if (r instanceof BoxRegion) {
                BoxRegion br = (BoxRegion) r;
                br.getSize(size);

//System.out.println("size: " + java.util.Arrays.toString(size));
                vol += size[0] * size[1] * size[2];
            }
        }

//System.out.println("vol: " + vol);
        assertEquals("Correct Volume", 6, vol);
    }

    /**
     * Test edge conditions
     */
    public void testZCentricEdgeEnd() {
        AttributeGrid grid = new ArrayAttributeGridByte(4,4,5,0.1,0.1);

        // Create a simple region
        grid.setData(3,2,1,Grid.INSIDE,1);
        grid.setData(3,2,2,Grid.INSIDE,1);
        grid.setData(3,2,3,Grid.INSIDE,1);
        grid.setData(3,2,4,Grid.INSIDE,1);
        grid.setData(3,3,1,Grid.INSIDE,1);
        grid.setData(3,3,2,Grid.INSIDE,1);
        grid.setData(3,3,3,Grid.INSIDE,1);
        grid.setData(3,3,4,Grid.INSIDE,1);

        BoxRegionFinder rf = new BoxRegionFinder();
        Set<Region> regions = rf.execute(grid);

        assertNotNull("Regions", regions);

        // Total volume should equal 8
        int vol = 0;
        Iterator<Region> itr = regions.iterator();
        int[] size = new int[3];

        while(itr.hasNext()) {
            Region r = itr.next();

            if (r instanceof BoxRegion) {
                BoxRegion br = (BoxRegion) r;
                br.getSize(size);

System.out.println("size: " + java.util.Arrays.toString(size));
                vol += size[0] * size[1] * size[2];
            }
        }

System.out.println("vol: " + vol);
        assertEquals("Correct Volume", 8, vol);
    }

    /**
     * Test edge conditions
     */
    public void testZCentricEdgeStart() {
        AttributeGrid grid = new ArrayAttributeGridByte(4,4,5,0.1,0.1);

        // Create a simple region
        grid.setData(3,0,0,Grid.INSIDE,1);
        grid.setData(3,0,1,Grid.INSIDE,1);
        grid.setData(3,0,2,Grid.INSIDE,1);
        grid.setData(3,1,0,Grid.INSIDE,1);
        grid.setData(3,1,1,Grid.INSIDE,1);
        grid.setData(3,1,2,Grid.INSIDE,1);

        BoxRegionFinder rf = new BoxRegionFinder();
        Set<Region> regions = rf.execute(grid);

        assertNotNull("Regions", regions);

        // Total volume should equal 6
        int vol = 0;
        Iterator<Region> itr = regions.iterator();
        int[] size = new int[3];

        while(itr.hasNext()) {
            Region r = itr.next();

            if (r instanceof BoxRegion) {
                BoxRegion br = (BoxRegion) r;
                br.getSize(size);

//System.out.println("size: " + java.util.Arrays.toString(size));
                vol += size[0] * size[1] * size[2];
            }
        }

//System.out.println("vol: " + vol);
        assertEquals("Correct Volume", 6, vol);
    }

    /**
     * Creates a torus in a grid and returns the grid.
     *
     * @param ir The inside radius of the torus
     * @param or The outside radius of the grid
     * @param facets The tessellation accuracy
     * @param outerMaterial The outer material
     * @param innerMaterial The inner material
     * @param geomType The geometry type to use
     * @param fill Should the interior be filled or just a shell
     * @return The grid containing the cube
     */
    private static AttributeGrid createTorusInGrid(float ir, float or, int facets,
            byte outerMaterial, byte innerMaterial, int geomType, boolean fill, int[] add) {

        TorusGenerator tg = new TorusGenerator(ir, or, facets, facets);
        GeometryData geom = new GeometryData();
        geom.geometryType = geomType;
        tg.generate(geom);

        double bounds = TriangleModelCreator.findMaxBounds(geom);
//System.out.println("geometry bounds: " + bounds);

        int bufferVoxel = 4;
        int size = (int) (2.0 * bounds / HORIZ_RESOLUTION) + bufferVoxel;
//System.out.println("grid voxels per side: " + size);

        AttributeGrid grid = new ArrayAttributeGridByte(size + add[0], size + add[1], size + add[2], HORIZ_RESOLUTION, VERT_RESOLUTION);

        double x = bounds + bufferVoxel/2 * HORIZ_RESOLUTION;
        double y = x;
        double z = x;

        double rx = 0,ry = 1,rz = 0,rangle = 0;
//        double rx = 1,ry = 0,rz = 0,rangle = 1.57079633;

        TriangleModelCreator tmc = null;
        tmc = new TriangleModelCreator(geom, x, y, z,
            rx,ry,rz,rangle,innerMaterial,fill);

        tmc.generate(grid);

        return grid;
    }

}

class FindRegionTester implements RegionTraverser {
    private boolean foundCorrect;
    private HashSet<VoxelCoordinate> vcSet;
    private int iterateCount;
    private int vcSetCount;

    /**
     * Constructor that takes in a HashSet of VoxelCoordinates known to be
     * in the VoxelClass to find
     * @param vc
     */
    public FindRegionTester(HashSet<VoxelCoordinate> vc) {
        this.vcSet = (HashSet<VoxelCoordinate>)vc.clone();
        foundCorrect = true;
        iterateCount = 0;
        vcSetCount = vcSet.size();
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public void found(int x, int y, int z) {
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
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     *
     * @return True to continue, false stops the traversal.
     */
    public boolean foundInterruptible(int x, int y, int z) {
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

class FindRegionRecorder implements RegionTraverser {
    private HashSet<VoxelCoordinate> vcSet;

    /**
     * Constructor that takes in a HashSet of VoxelCoordinates known to be
     * in the VoxelClass to find
     */
    public FindRegionRecorder() {
        this.vcSet = new HashSet<VoxelCoordinate>();
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public void found(int x, int y, int z) {
        VoxelCoordinate c = new VoxelCoordinate(x, y, z);

        if (vcSet.contains(c)) {
            System.out.println("*** Duplicate coordinate returned");
        }

        vcSet.add(c);
    }

    /**
     * A voxel of the class requested has been found.
     * VoxelData classes may be reused so clone the object
     * if you keep a copy.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     *
     * @return True to continue, false stops the traversal.
     */
    public boolean foundInterruptible(int x, int y, int z) {
        VoxelCoordinate c = new VoxelCoordinate(x, y, z);

        if (vcSet.contains(c)) {
            System.out.println("*** Duplicate coordinate returned");
        }

        vcSet.add(c);

        return true;
    }

    public Set<VoxelCoordinate> getCoords() {
        return vcSet;
    }

    /**
     * Compare the coords in this recorder to another set.
     *
     * @param coords The coords to test
     * @return Whether they are equal
     */
    public boolean compare(Set<VoxelCoordinate> coords) {
        if (coords.size() != vcSet.size())
            return false;

        Iterator<VoxelCoordinate> itr = coords.iterator();

        while(itr.hasNext()) {
            VoxelCoordinate vc = itr.next();

            if (vcSet.contains(vc)) {
                continue;
            } else {
                return false;
            }
        }

        return true;
    }
}
