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
import java.util.List;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.Vector3d;


// Internal Imports
import abfab3d.grid.op.GridMaker;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Cylinder;
import abfab3d.datasources.Subtraction;
import abfab3d.datasources.Union;
import abfab3d.util.DataSource;
import abfab3d.distance.DistanceData;
import abfab3d.distance.DistanceDataHalfSpace;

import abfab3d.io.output.SVXWriter;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Units.MM;

/**
 * Tests the functionality of a RegionCounter
 * 
 * NOTE: Filled voxels cannot be at the edge of the grid.
 *
 * @author Tony Wong
 * @author Vladimir Bulatov
 * @version
 */
public class TestRegionCounter extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestRegionCounter.class);
    }

    /**
     * Test countComponents. Connected components are 6-connected (face adjacent).
     */
    public void testCountComponentsMaterial() {
        // single voxel
    	AttributeGrid grid = new ArrayAttributeGridByte(10, 10, 10, 0.001, 0.001);
        grid.setData(2, 2, 2, (byte)1, 3);
        int count = RegionCounter.countComponents(grid, 3, 10, false);
        assertEquals("Region count is not 1", 1, count);
        
        // two side adjacent voxels (share a face)
        grid.setData(3, 2, 2, (byte)1, 3);
        count = RegionCounter.countComponents(grid, 3, 10, false);
        assertEquals("Region count is not 1", 1, count);
        
        // two diagonal adjacent voxels, same plane (share an edge)
        grid.setData(3, 2, 2, (byte)0, 0);
        grid.setData(3, 3, 2, (byte)1, 3);
        count = RegionCounter.countComponents(grid, 3, 10, false);
        assertEquals("Region count is not 2", 2, count);
        
        // two diagonal adjacent voxels, different plane (share a corner)
        grid.setData(3, 3, 2, (byte)0, 0);
        grid.setData(3, 3, 3, (byte)1, 3);
        count = RegionCounter.countComponents(grid, 3, 10, false);
        assertEquals("Region count is not 2", 2, count);
    }
    
    /**
     * Test setGrid.
     */
    public void testCountComponentsMulti() {
        // two voxels, not connected
    	AttributeGrid grid = new ArrayAttributeGridByte(10, 10, 10, 0.001, 0.001);
        grid.setData(2, 2, 2, (byte)1, 3);
        grid.setData(4, 2, 2, (byte)1, 3);
        int count = RegionCounter.countComponents(grid, 3, 10, false);
        assertEquals("Region count is not 2", 2, count);
        
        grid.setData(5, 2, 2, (byte)1, 3);
        count = RegionCounter.countComponents(grid, 3, 10, false);
        assertEquals("Region count is not 2", 2, count);
    }
    
    /**
     * Test setGrid.
     */
    public void testCountComponentsDifferentMaterial() {
        // two adjacent voxels of different material
    	AttributeGrid grid = new ArrayAttributeGridByte(10, 10, 10, 0.001, 0.001);
        grid.setData(2, 2, 2, (byte)1, 3);
        grid.setData(3, 2, 2, (byte)1, 4);
        
        int count = RegionCounter.countComponents(grid, 3, 10, false);
        assertEquals("Region count is not 1", 1, count);
        
        count = RegionCounter.countComponents(grid, 4, 10, false);
        assertEquals("Region count is not 1", 1, count);
        
        grid.setData(4, 2, 2, (byte)1, 3);
        
        count = RegionCounter.countComponents(grid, 3, 10, false);
        assertEquals("Region count is not 2", 2, count);
        
        count = RegionCounter.countComponents(grid, 4, 10, false);
        assertEquals("Region count is not 1", 1, count);
    }

    /**
     * Test countComponents. Connected components are 6-connected (face adjacent).
     */
    public void testCountComponentsState() {
        // single voxel
    	AttributeGrid grid = new ArrayAttributeGridByte(10, 10, 10, 0.001, 0.001);
        grid.setData(2, 2, 2, (byte)1, 3);
        int count = RegionCounter.countComponents((Grid) grid, (byte) 1, 10, false);
        assertEquals("Region count is not 1", 1, count);
        
        // two side adjacent voxels (share a face)
        grid.setData(3, 2, 2, (byte)1, 3);
        count = RegionCounter.countComponents((Grid) grid, (byte) 1, 10, false);
        assertEquals("Region count is not 1", 1, count);
        
        // two diagonal adjacent voxels, same plane (share an edge)
        grid.setData(3, 2, 2, (byte)0, 0);
        grid.setData(3, 3, 2, (byte)1, 3);
        count = RegionCounter.countComponents((Grid) grid, (byte) 1, 10, false);
        assertEquals("Region count is not 2", 2, count);
        
        // two diagonal adjacent voxels, different plane (share a corner)
        grid.setData(3, 3, 2, (byte)0, 0);
        grid.setData(3, 3, 3, (byte)1, 3);
        count = RegionCounter.countComponents((Grid) grid, (byte) 1, 10, false);
        assertEquals("Region count is not 2", 2, count);
    }
    
    /**
     * Test countComponents. Connected components are 6-connected (face adjacent).
     */
    public void testCountComponentsMultiState() {
        // two voxels, not connected
    	AttributeGrid grid = new ArrayAttributeGridByte(10, 10, 10, 0.001, 0.001);
        grid.setData(2, 2, 2, (byte)1, 3);
        grid.setData(4, 2, 2, (byte)1, 3);
        int count = RegionCounter.countComponents((Grid) grid, (byte) 1, 10, false);
        assertEquals("Region count is not 2", 2, count);
        
        grid.setData(5, 2, 2, (byte)1, 3);
        count = RegionCounter.countComponents((Grid) grid, (byte) 1, 10, false);
        assertEquals("Region count is not 2", 2, count);
    }
    
    /**
     * Test countComponents. Connected components are 6-connected (face adjacent).
     */
    public void testCountComponentsDifferentState() {
        // two adjacent voxels of different material
    	AttributeGrid grid = new ArrayAttributeGridByte(10, 10, 10, 0.001, 0.001);
        grid.setData(2, 2, 2, Grid.INSIDE, 3);

        int count = RegionCounter.countComponents((Grid) grid, Grid.INSIDE, 10, false);
        assertEquals("Region count is not 1", 1, count);
        
        grid.setData(4, 2, 2, Grid.INSIDE, 3);
        
        count = RegionCounter.countComponents((Grid) grid, Grid.INSIDE, 10, false);
        assertEquals("Region count is not 2", 2, count);
    }
    
    /**
     * Test countComponents. Connected components are 6-connected (face adjacent).
     */
    public void testgetComponentBoundsByVolumeMaterial() {
        // single voxel
    	AttributeGrid grid = new ArrayAttributeGridByte(10, 10, 10, 0.001, 0.001);
        grid.setData(2, 2, 2, (byte)1, 3);

        List<int[]> regionsBoundsList = RegionCounter.getComponentBoundsByVolume(grid, 3, 10, 1, false);
        assertEquals("Bounds count is not 1", 1, regionsBoundsList.size());
        
        int[] bounds = regionsBoundsList.get(0);
        assertTrue(bounds[0] == 2 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 2 && bounds[4] == 2 && bounds[5] == 2);
        
        // two side adjacent voxels (share a face)
        grid.setData(3, 2, 2, (byte)1, 3);
        
        regionsBoundsList = RegionCounter.getComponentBoundsByVolume(grid, 3, 10, 1, false);
        assertEquals("Bounds count is not 1", 1, regionsBoundsList.size());
        
        bounds = regionsBoundsList.get(0);
        assertTrue(bounds[0] == 2 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 3 && bounds[4] == 2 && bounds[5] == 2);
        
        // two diagonal adjacent voxels, same plane (share an edge)
        grid.setData(3, 2, 2, (byte)0, 0);
        grid.setData(3, 3, 2, (byte)1, 3);

        regionsBoundsList = RegionCounter.getComponentBoundsByVolume(grid, 3, 10, 1, false);
        assertEquals("Bounds count is not 2", 2, regionsBoundsList.size());
        
        // two diagonal adjacent voxels, different plane (share a corner)
        grid.setData(3, 3, 2, (byte)0, 0);
        grid.setData(3, 3, 3, (byte)1, 3);

        regionsBoundsList = RegionCounter.getComponentBoundsByVolume(grid, 3, 10, 1, false);
        assertEquals("Bounds count is not 2", 2, regionsBoundsList.size());
    }
    
    /**
     * Test getComponentBoundsByVolume.
     */
    public void testgetComponentBoundsByVolumeMaterial2() {
        // two adjacent voxels of different material
    	AttributeGrid grid = new ArrayAttributeGridByte(10, 10, 10, 0.001, 0.001);
        grid.setData(2, 2, 2, (byte)1, 3);
        grid.setData(3, 2, 2, (byte)1, 3);
        grid.setData(2, 3, 2, (byte)1, 3);
        grid.setData(2, 2, 3, (byte)1, 3);
        
        List<int[]> regionsBoundsList = RegionCounter.getComponentBoundsByVolume(grid, 3, 10, 1, false);
        assertEquals("Bounds count is not 1", 1, regionsBoundsList.size());
        
        int[] bounds = regionsBoundsList.get(0);
        assertTrue(bounds[0] == 2 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 3 && bounds[4] == 3 && bounds[5] == 3);

        grid.setData(5, 2, 2, (byte)1, 3);
        grid.setData(6, 2, 2, (byte)1, 3);
        grid.setData(5, 5, 2, (byte)1, 3);
        
        regionsBoundsList = RegionCounter.getComponentBoundsByVolume(grid, 3, 10, 1, false);
        assertEquals("Bounds count is not 3", 3, regionsBoundsList.size());
        
        bounds = regionsBoundsList.get(0);
        assertTrue(bounds[0] == 2 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 3 && bounds[4] == 3 && bounds[5] == 3);

        bounds = regionsBoundsList.get(1);
        assertTrue(bounds[0] == 5 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 6 && bounds[4] == 2 && bounds[5] == 2);
        
        bounds = regionsBoundsList.get(2);
        assertTrue(bounds[0] == 5 && bounds[1] == 5 && bounds[2] == 2 && bounds[3] == 5 && bounds[4] == 5 && bounds[5] == 2);
        
        // maxCount = 2
        regionsBoundsList = RegionCounter.getComponentBoundsByVolume(grid, 3, 2, 1, false);
        assertEquals("Bounds count is not 2", 2, regionsBoundsList.size());
        
        bounds = regionsBoundsList.get(0);
        assertTrue(bounds[0] == 2 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 3 && bounds[4] == 3 && bounds[5] == 3);

        bounds = regionsBoundsList.get(1);
        assertTrue(bounds[0] == 5 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 6 && bounds[4] == 2 && bounds[5] == 2);
        
        // minSize = 2
        regionsBoundsList = RegionCounter.getComponentBoundsByVolume(grid, 3, 10, 2, false);
        assertEquals("Bounds count is not 2", 2, regionsBoundsList.size());
        
        bounds = regionsBoundsList.get(0);
        assertTrue(bounds[0] == 2 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 3 && bounds[4] == 3 && bounds[5] == 3);

        bounds = regionsBoundsList.get(1);
        assertTrue(bounds[0] == 5 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 6 && bounds[4] == 2 && bounds[5] == 2);
    }
    
    /**
     * Test countComponents. Connected components are 6-connected (face adjacent).
     */
    public void testgetComponentBoundsByVolumeState() {
        // single voxel
    	AttributeGrid grid = new ArrayAttributeGridByte(10, 10, 10, 0.001, 0.001);
        grid.setData(2, 2, 2, (byte)1, 3);

        List<int[]> regionsBoundsList = RegionCounter.getComponentBoundsByVolume((Grid) grid, (byte) 1, 10, 1, false);
        assertEquals("Bounds count is not 1", 1, regionsBoundsList.size());
        
        int[] bounds = regionsBoundsList.get(0);
        assertTrue(bounds[0] == 2 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 2 && bounds[4] == 2 && bounds[5] == 2);
        
        // two side adjacent voxels (share a face)
        grid.setData(3, 2, 2, (byte)1, 3);
        
        regionsBoundsList = RegionCounter.getComponentBoundsByVolume((Grid) grid, (byte) 1, 10, 1, false);
        assertEquals("Bounds count is not 1", 1, regionsBoundsList.size());
        
        bounds = regionsBoundsList.get(0);
        assertTrue(bounds[0] == 2 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 3 && bounds[4] == 2 && bounds[5] == 2);
        
        // two diagonal adjacent voxels, same plane (share an edge)
        grid.setData(3, 2, 2, (byte)0, 0);
        grid.setData(3, 3, 2, (byte)1, 3);

        regionsBoundsList = RegionCounter.getComponentBoundsByVolume((Grid) grid, (byte) 1, 10, 1, false);
        assertEquals("Bounds count is not 2", 2, regionsBoundsList.size());
        
        // two diagonal adjacent voxels, different plane (share a corner)
        grid.setData(3, 3, 2, (byte)0, 0);
        grid.setData(3, 3, 3, (byte)1, 3);

        regionsBoundsList = RegionCounter.getComponentBoundsByVolume((Grid) grid, (byte) 1, 10, 1, false);
        assertEquals("Bounds count is not 2", 2, regionsBoundsList.size());
    }
    
    /**
     * Test getComponentBoundsByVolume.
     */
    public void testgetComponentBoundsByVolumeState2() {
        // two adjacent voxels of different material
    	AttributeGrid grid = new ArrayAttributeGridByte(10, 10, 10, 0.001, 0.001);
        grid.setData(2, 2, 2, (byte)1, 3);
        grid.setData(3, 2, 2, (byte)1, 3);
        grid.setData(2, 3, 2, (byte)1, 3);
        grid.setData(2, 2, 3, (byte)1, 3);
        
        List<int[]> regionsBoundsList = RegionCounter.getComponentBoundsByVolume((Grid) grid, (byte) 1, 10, 1, false);
        assertEquals("Bounds count is not 1", 1, regionsBoundsList.size());
        
        int[] bounds = regionsBoundsList.get(0);
        assertTrue(bounds[0] == 2 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 3 && bounds[4] == 3 && bounds[5] == 3);

        grid.setData(5, 2, 2, (byte)1, 3);
        grid.setData(6, 2, 2, (byte)1, 3);
        grid.setData(5, 5, 2, (byte)1, 3);
        
        regionsBoundsList = RegionCounter.getComponentBoundsByVolume((Grid) grid, (byte) 1, 10, 1, false);
        assertEquals("Bounds count is not 3", 3, regionsBoundsList.size());
        
        bounds = regionsBoundsList.get(0);
        assertTrue(bounds[0] == 2 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 3 && bounds[4] == 3 && bounds[5] == 3);

        bounds = regionsBoundsList.get(1);
        assertTrue(bounds[0] == 5 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 6 && bounds[4] == 2 && bounds[5] == 2);
        
        bounds = regionsBoundsList.get(2);
        assertTrue(bounds[0] == 5 && bounds[1] == 5 && bounds[2] == 2 && bounds[3] == 5 && bounds[4] == 5 && bounds[5] == 2);
        
        // maxCount = 2
        regionsBoundsList = RegionCounter.getComponentBoundsByVolume((Grid) grid, (byte) 1, 2, 1, false);
        assertEquals("Bounds count is not 2", 2, regionsBoundsList.size());
        
        bounds = regionsBoundsList.get(0);
        assertTrue(bounds[0] == 2 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 3 && bounds[4] == 3 && bounds[5] == 3);

        bounds = regionsBoundsList.get(1);
        assertTrue(bounds[0] == 5 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 6 && bounds[4] == 2 && bounds[5] == 2);
        
        // minSize = 2
        regionsBoundsList = RegionCounter.getComponentBoundsByVolume((Grid) grid, (byte) 1, 10, 2, false);
        assertEquals("Bounds count is not 2", 2, regionsBoundsList.size());
        
        bounds = regionsBoundsList.get(0);
        assertTrue(bounds[0] == 2 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 3 && bounds[4] == 3 && bounds[5] == 3);

        bounds = regionsBoundsList.get(1);
        assertTrue(bounds[0] == 5 && bounds[1] == 2 && bounds[2] == 2 && bounds[3] == 6 && bounds[4] == 2 && bounds[5] == 2);
    }

    public void _testFindComponents(){
        printf("testFindComponents()\n");
        double vs = 0.5*MM;
        double r1 = 10*MM;
        double r2 = 20*MM;
        double thickness = 3*MM;        
        double ss = r2 + vs;

        AttributeGrid grid = makeDoubleSphere(r1, r2, thickness, ss, vs);
        long mat = 0;

        //grid.setAttributeWorld(0., 0., 0., mat);
        //grid.setAttributeWorld(vs,vs,vs, mat);
        //int count = RegionCounter.countComponents(grid, new AttributeTesterRange(0, 0));
        int count = RegionCounter.countComponents(grid, new HalfSpaceAttributeTester((int)((ss+r2)/vs-1), 0));
        printf("components count: %d\n", count);
        //SVXWriter svx = new SVXWriter();
        //svx.write(grid, "/tmp/ss.svx");
    }

    public void _testConnectedComponent(){
        printf("testFindComponents()\n");
        double vs = 0.5*MM;
        double r1 = 10*MM;
        double r2 = 20*MM;
        double thickness = 3*MM;        
        double ss = vs*(int)((r2/Math.sqrt(2))/vs);

        AttributeGrid grid = makeDoubleSphere(r1, r2, thickness, ss, vs);
        long mat = 0;

        //grid.setAttributeWorld(0., 0., 0., mat);
        //grid.setAttributeWorld(vs,vs,vs, mat);
        //int count = RegionCounter.countComponents(grid, new AttributeTesterRange(0, 0));
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        printf("grid: %d x %d x %d \n", nx, ny, nz);
        
        SVXWriter svx = new SVXWriter();
        svx.write(grid, "/tmp/grid.svx");

        AttributeTester tester = new HalfSpaceAttributeTester(0, 0, 0);
        Vector<ConnectedComponent> comp = RegionCounter.findComponents(grid, tester);

        printf("components count: %d\n", comp.size());
        for(int i = 0; i < comp.size(); i++){
            ConnectedComponent cc = comp.get(i);
            int seed[] = cc.getSeed();
            printf("component seed(%d %d %d), volume: %d\n", seed[0], seed[1], seed[2],cc.getVolume()); 
            GridBitIntervals mask = new GridBitIntervals(nx,ny,nz);
            ConnectedComponent c = new ConnectedComponent(grid, mask, seed[0],seed[1],seed[2],tester, false);
            svx.write(mask, fmt("/tmp/mask_%02d.svx",i));
        }

    }

    public void _testWithBoundary(){
        printf("testWithBoundary()\n");
        double vs = 0.25*MM;
        double width = 20*MM;
        double height = 20*MM;
        double thicknessOut = 5*MM; 
        double thicknessIn = 2*MM; 
        double gridWidth = 30*MM;

        AttributeGrid grid = makeDoubleWalledGlass(width, height, thicknessIn, thicknessOut, gridWidth, vs);

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        printf("grid: %d x %d x %d \n", nx, ny, nz);
        
        SVXWriter svx = new SVXWriter(2);
        svx.write(grid, "/tmp/glass.svx");


        GridBitIntervals mask = new GridBitIntervals(nx,ny,nz);
        DistanceData halfSpace = new DistanceDataHalfSpace(new Vector3d(0,1,0),new Vector3d(0,ny*5/6,0));
        //tester of shape exterior
        AttributeTester testerOut = new HalfSpaceAttributeTester(halfSpace, 0, 0);
        //tester of shape interior
        AttributeTester testerIn = new HalfSpaceAttributeTester(halfSpace, 1, 255);
        ConnectedComponent cout = new ConnectedComponent(grid, mask, 0,0,0,testerOut, false);
        svx.write(mask, "/tmp/glass_bottom_out.svx");
        
        Vector<ConnectedComponent> comp = RegionCounter.findComponents(grid, testerIn);
        printf("component count: %d\n", comp.size());
        for(int i = 0; i < comp.size(); i++){
            ConnectedComponent cc = comp.get(i);
            int seed[] = cc.getSeed();
            printf("component seed(%d %d %d), volume: %d\n", seed[0], seed[1], seed[2],cc.getVolume()); 
            GridBitIntervals cmask = new GridBitIntervals(nx,ny,nz);
            ConnectedComponent c = new ConnectedComponent(grid, cmask, seed[0],seed[1],seed[2],testerIn, false);
            svx.write(cmask, fmt("/tmp/glass_%02d.svx",i));
        }

    }
    
    // bound the connected components by half space (x >= x0)
    static class HalfSpaceAttributeTester implements AttributeTester {
        
        int x0;
        long minValue, maxValue;
        DistanceData dd; 
        HalfSpaceAttributeTester(int x0, long value){
            this(x0, value, value);
        }
        HalfSpaceAttributeTester(DistanceData dd, long minValue, long maxValue){
            this.dd = dd;
            this.maxValue = maxValue;
            this.minValue = minValue;
        }
        HalfSpaceAttributeTester(int x0, long minValue, long maxValue){
            this.dd = new DistanceDataHalfSpace(new Vector3d(1,0,0),new Vector3d(x0,0,0));
            this.maxValue = maxValue;
            this.minValue = minValue;
        }
        public boolean test(int x,int y,int z,long attribute){
            // point is inside of shape and attribute is inside of range 
            return (dd.getDistance(x,y,z) < 0.) && (attribute >= minValue) &&  (attribute <= maxValue );
        }
    }

    static AttributeGrid makeDoubleWalledGlass(double width, double height, double thicknessIn, double thicknessOut, double gridWidth, double vs){
        double s = gridWidth/2;
        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(new Bounds(-s, s, -s, s, -s, s), vs, vs);
        DataSource gin = makeGlass(width, height, thicknessIn);
        DataSource gout = makeGlass(width, height, thicknessOut);
        DataSource glass = new Subtraction(gout, gin);
        GridMaker gm = new GridMaker();
        
        gm.setSource(glass);
        gm.makeGrid(grid);        
        return grid;
    }

    static DataSource makeGlass(double width, double height, double thickness){
        double y1 = height/2;
        double th2 = thickness/2;
        double rout = width/2 + thickness/2;
        double rin = rout-thickness;

        DataSource c1 = new Cylinder(new Vector3d(0, y1+th2, 0),new Vector3d(0, -(y1+th2), 0), rout);
        DataSource c2 = new Cylinder(new Vector3d(0, y1+2*th2, 0),new Vector3d(0, -(y1+2*th2), 0), rin);
        DataSource c3 = new Cylinder(new Vector3d(0, -y1-th2, 0),new Vector3d(0, -y1+th2, 0), rout);

        return new Union(new Subtraction(c1,c2),c3);
    }

    static AttributeGrid makeDoubleSphere(double minR, double maxR, double thickness, double s, double vs){

        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(new Bounds(-s, s, -s, s, -s, s), vs, vs);

        DataSource ss = new Union(new Subtraction(new Sphere(maxR),new Sphere(maxR-thickness)),
                                  new Subtraction(new Sphere(minR),new Sphere(minR-thickness)));
        GridMaker gm = new GridMaker();
        gm.setSource(ss);
        gm.makeGrid(grid);        
        return grid;

    }

    public static void main(String arg[]){
        //new TestRegionCounter()._testFindComponents();
        //new TestRegionCounter()._testConnectedComponent();
        new TestRegionCounter()._testWithBoundary();
    }
}
