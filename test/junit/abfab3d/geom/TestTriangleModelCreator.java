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

package abfab3d.geom;

// External Imports
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;
import org.j3d.geom.CylinderGenerator;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.export.X3DBinaryRetainedDirectExporter;
import org.web3d.vrml.export.X3DBinarySerializer;
import org.web3d.vrml.sav.BinaryContentHandler;

// Internal Imports
import abfab3d.grid.*;
import abfab3d.grid.Grid.VoxelClasses;
import abfab3d.io.output.BoxesX3DExporter;

/**
 * Tests the functionality of TriangleModelCreator.
 *
 * @author Tony Wong
 * @version
 */
public class TestTriangleModelCreator extends TestCase {

    /** Horizontal resolution of the printer in meters.  */
    public static final double HORIZ_RESOLUTION = 0.002;

    /** Vertical resolution of the printer in meters.  */
    public static final double VERT_RESOLUTION = 0.001;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTriangleModelCreator.class);
    }

    /**
     * Test the voxelization of a simple triangle cube.
     */
    public void testCube() {

        // Use 0.0999 instead of 0.1 voxelization is unpredictable when model
        // lines up exactly with a grid
        float width = 0.0999f;
        float height = 0.0999f;
        float depth = 0.0999f;
        int outerMaterial = 1;
        int innerMaterial = 1;

        Grid grid = createCubeInGrid(width, height, depth, outerMaterial, innerMaterial, GeometryData.TRIANGLES);

System.out.println("grid dimensions: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());

        int xVoxels = (int) Math.round(width / HORIZ_RESOLUTION);
        int yVoxels = (int) Math.round(height / VERT_RESOLUTION);
        int zVoxels = (int) Math.round(depth / HORIZ_RESOLUTION);
        int expectedMatCount = xVoxels * yVoxels * zVoxels;

        // Check the material count
        assertEquals("Material count is not " + expectedMatCount, expectedMatCount, grid.findCount(outerMaterial));

        int cubeStartIndex = 0;

        checkCubeVoxelStates(grid, cubeStartIndex, xVoxels, yVoxels, zVoxels);
    }

    /**
     * Test the voxelization of a simple indexed triangle cube.
     */
    public void testCubeIndexed() {

        // Use 0.0999 instead of 0.1 voxelization is unpredictable when model
        // lines up exactly with a grid
        float width = 0.0999f;
        float height = 0.0999f;
        float depth = 0.0999f;
        int outerMaterial = 1;
        int innerMaterial = 1;

        Grid grid = createCubeInGrid(width, height, depth, outerMaterial, innerMaterial, GeometryData.INDEXED_TRIANGLES);

System.out.println("grid dimensions: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());

        int xVoxels = (int) Math.round(width / HORIZ_RESOLUTION);
        int yVoxels = (int) Math.round(height / VERT_RESOLUTION);
        int zVoxels = (int) Math.round(depth / HORIZ_RESOLUTION);
        int expectedMatCount = xVoxels * yVoxels * zVoxels;

        // Check the material count
        assertEquals("Material count is not " + expectedMatCount, expectedMatCount, grid.findCount(outerMaterial));

        int cubeStartIndex = 0;

        checkCubeVoxelStates(grid, cubeStartIndex, xVoxels, yVoxels, zVoxels);
    }



    /**
     * Test the voxelization of a simple triangle cylinder.
     */
    public void testCylinder() {

        // Use 0.0999 instead of 0.1 voxelization is unpredictable when model
        // lines up exactly with a grid
        float radius = 0.0499f;
        float height = 0.0999f;
        int outerMaterial = 1;
        int innerMaterial = 1;

        Grid grid = createCylinderInGrid(height, radius, outerMaterial, innerMaterial, GeometryData.TRIANGLES);

//System.out.println("grid dimensions: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());

        int radiusInVoxels = (int) Math.round(radius / HORIZ_RESOLUTION);
        int areaInVoxels = (int) Math.round(Math.PI * (double) (radiusInVoxels * radiusInVoxels));
        int heightInVoxels = (int) Math.round(height / VERT_RESOLUTION);
        int expectedMatCount = areaInVoxels * heightInVoxels;

System.out.println("radiusInVoxels: " + radiusInVoxels);
System.out.println("areaInVoxels: " + areaInVoxels);
System.out.println("expectedMatCount: " + expectedMatCount);

        checkCylinder(grid, radius, innerMaterial);

    }

    /**
     * Creates a simple cube in a grid and returns the grid.
     *
     * @param cWidth The width of the cube
     * @param cHeight The height of the cube
     * @param cDepth The depth of the cube
     * @param outerMaterial The outer material
     * @param innerMaterial The inner material
     * @return The grid containing the cube
     */
    private static Grid createCubeInGrid(float cWidth, float cHeight, float cDepth,
            int outerMaterial, int innerMaterial, int geomType) {

        BoxGenerator bg = new BoxGenerator(cWidth, cHeight, cDepth);
        GeometryData geom = new GeometryData();
        geom.geometryType = geomType;
        bg.generate(geom);

        double bounds = findMaxBounds(geom);
System.out.println("geometry bounds: " + bounds);

        // twice the bounds (since centered at origin) plus a slight over allocate
        int gWidth = (int) (cWidth / HORIZ_RESOLUTION) + 10;
        int gHeight = (int) (cHeight / VERT_RESOLUTION) + 10;
        int gDepth = (int) (cDepth / HORIZ_RESOLUTION) + 10;

        Grid grid = new ArrayGridByte(gWidth, gHeight, gDepth, HORIZ_RESOLUTION, VERT_RESOLUTION);

        double x = bounds;
        double y = x;
        double z = x;

        double rx = 0,ry = 1,rz = 0,rangle = 0;

        TriangleModelCreator tmc = null;
        tmc = new TriangleModelCreator(geom,x,y,z,
            rx,ry,rz,rangle,outerMaterial,innerMaterial,true);

        tmc.generate(grid);

        return grid;
    }

    /**
     * Creates a simple cube in a grid and returns the grid.
     *
     * @param cWidth The width of the cube
     * @param cHeight The height of the cube
     * @param cDepth The depth of the cube
     * @param outerMaterial The outer material
     * @param innerMaterial The inner material
     * @return The grid containing the cube
     */
    private static Grid createCylinderInGrid(float height, float radius,
            int outerMaterial, int innerMaterial, int geomType) {

        CylinderGenerator cg = new CylinderGenerator(height, radius);
        GeometryData geom = new GeometryData();
        geom.geometryType = geomType;
        cg.generate(geom);

        double bounds = findMaxBounds(geom);
System.out.println("geometry bounds: " + bounds);

        // twice the bounds (since centered at origin) plus a slight over allocate
        int gWidth = (int) (2.0f * radius / HORIZ_RESOLUTION) + 10;
        int gHeight = (int) (height / VERT_RESOLUTION) + 10;
        int gDepth = gWidth;

System.out.println("grid dimensions: " + gWidth + " " + gHeight + " " + gDepth);

        Grid grid = new ArrayGridByte(gWidth, gHeight, gDepth, HORIZ_RESOLUTION, VERT_RESOLUTION);

        double x = bounds;
        double y = x;
        double z = x;

        double rx = 0,ry = 1,rz = 0,rangle = 0;

        TriangleModelCreator tmc = null;
        tmc = new TriangleModelCreator(geom,x,y,z,
            rx,ry,rz,rangle,outerMaterial,innerMaterial,true);

        tmc.generate(grid);

        return grid;
    }

    /**
     * Check all voxels for correctness of its state. This function assumes a voxelized
     * cube in the grid and the starting index is the same in all three axes. Works
     * only for cubes aligned with the axes.
     *
     * @param grid The grid
     * @param startIndex The starting index of the cube
     * @param xVoxels The number of cube voxels in the x axis
     * @param yVoxels The number of cube voxels in the y axis
     * @param zVoxels The number of cube voxels in the z axis
     */
    private void checkCubeVoxelStates(Grid grid, int startIndex, int xVoxels, int yVoxels, int zVoxels) {

System.out.println("cube interior count: " + grid.findCount(VoxelClasses.INTERIOR));
System.out.println("cube exterior count: " + grid.findCount(VoxelClasses.EXTERIOR));
System.out.println("total mat count: " + grid.findCount(1));

        // Check each voxel for state correctness
        for (int x=0; x<grid.getWidth(); x++) {
            for (int y=0; y<grid.getHeight(); y++) {
                for (int z=0; z<grid.getDepth(); z++) {
//System.out.println("[" + x + ", " + y + ", " + z + "]: " + grid.getState(x, y, z));

                    // If x or y or z is at the starting index of the cube voxels,
                    // and the other coordinates are less than the ending index
                    // of the cube voxels, it is an exterior voxel. If x or y or z
                    // is at the ending index and the other coordinates are less
                    // than the ending index, it is an exterior voxel.
                    //
                    // If the index is between the starting and ending cube voxels,
                    // exclusive, it is an interior voxel.
                    if ( (x == startIndex && y < (yVoxels) && z < (zVoxels) ) ||
                         (y == startIndex && x < (xVoxels) && z < (zVoxels) ) ||
                         (z == startIndex && x < (xVoxels) && y < (yVoxels) ) ||
                         (x == (xVoxels-1) && y < (yVoxels) && z < (zVoxels) ) ||
                         (y == (yVoxels-1) && x < (xVoxels) && z < (zVoxels) ) ||
                         (z == (zVoxels-1) && x < (xVoxels) && y < (yVoxels) ) ) {

                        assertEquals("State is not exterior", Grid.EXTERIOR, grid.getState(x, y, z));
                    } else if (x > startIndex && x < (xVoxels-1) &&
                               y > startIndex && y < (yVoxels-1) &&
                               z > startIndex && z < (zVoxels-1)) {

                        assertEquals("State is not interior", Grid.INTERIOR, grid.getState(x, y, z));
                    } else {
                        assertEquals("State is not outside", Grid.OUTSIDE, grid.getState(x, y, z));
                    }
                }
            }
        }
    }

    /**
     * Check all voxels for correctness of its state. This function assumes a voxelized
     * cube in the grid and the starting index is the same in all three axes. Works
     * only for cubes aligned with the axes.
     *
     * @param grid The grid
     * @param startIndex The starting index of the cube
     * @param xVoxels The number of cube voxels in the x axis
     * @param yVoxels The number of cube voxels in the y axis
     * @param zVoxels The number of cube voxels in the z axis
     */
    private void checkCylinder(Grid grid, float radius, int material) {
        int matCountPerSlice = 0;
        int matCountHeight = 0;
        int exteriorPerSlice = 0;
        int interiorPerSlice = 0;

        int radiusInVoxels = (int) Math.round(radius / HORIZ_RESOLUTION);
        byte state;

        // Material count per slice
        for (int x=0; x<grid.getWidth(); x++) {
            for (int z=0; z<grid.getDepth(); z++) {
                if (grid.getMaterial(x, 0, z) == material) {
                    matCountPerSlice++;
                }

                state = grid.getState(x, 0, z);
                if (state == Grid.INTERIOR) {
                    interiorPerSlice++;
                } else if (state == Grid.EXTERIOR) {
                    exteriorPerSlice++;
                }
            }
        }

        // Material count in one Y column
        for (int y=0; y<grid.getHeight(); y++) {
            if (grid.getMaterial(radiusInVoxels, y, radiusInVoxels) == material) {
                matCountHeight++;
            }
        }

System.out.println("matCountPerSlice: " + matCountPerSlice);
System.out.println("matCountHeight: " + matCountHeight);
System.out.println("grid.findCount(material): " + grid.findCount(material));
System.out.println("interiorPerSlice: " + interiorPerSlice);
System.out.println("exteriorPerSlice: " + exteriorPerSlice);

        int expectedMatCount = matCountPerSlice * matCountHeight;
        int expectedInteriorCount = interiorPerSlice * matCountHeight;
        int expectedExteriorCount = exteriorPerSlice * matCountHeight;

        assertEquals("Material count is not " + expectedMatCount,
                expectedMatCount, grid.findCount(material));

        assertEquals("Exterior count is not " + expectedExteriorCount,
                expectedExteriorCount, grid.findCount(VoxelClasses.EXTERIOR));

        assertEquals("Interior count is not " + expectedInteriorCount,
                expectedInteriorCount, grid.findCount(VoxelClasses.INTERIOR));
    }

    /**
     * Find the absolute maximum bounds of a geometry.
     *
     * @return The max
     */
    private static double findMaxBounds(GeometryData geom) {
        double max = Double.NEGATIVE_INFINITY;

        int len = geom.coordinates.length;

        for(int i=0; i < len; i++) {
            if (geom.coordinates[i] > max) {
                max = geom.coordinates[i];
            }
        }

        return Math.abs(max);
    }


    /**
     * Generate an X3D file
     * @param filename
     */
    public static void generate(String filename) {
        ErrorReporter console = new PlainTextErrorReporter();

        long stime = System.currentTimeMillis();

        // Use 0.0999 instead of 0.01 voxelization is unpredictable when model
        // lines up exactly with a grid
        float width = 0.0999f;
        float height = 0.0999f;
        float depth = 0.0999f;
        int outerMaterial = 1;
        int innerMaterial = 1;

        Grid grid = createCubeInGrid(width, height, depth, outerMaterial, innerMaterial, GeometryData.TRIANGLES);

System.out.println("grid dimensions: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());
System.out.println("mat count: " + grid.findCount(1));

        System.out.println("Gen time: " + (System.currentTimeMillis() - stime));

        System.out.println("Writing x3d");
        stime = System.currentTimeMillis();

        try {
            FileOutputStream fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".")+1);
            BoxesX3DExporter exporter = new BoxesX3DExporter(encoding, fos, console);

            HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
            colors.put(new Integer(Grid.INTERIOR), new float[] {0,1,0});
            colors.put(new Integer(Grid.EXTERIOR), new float[] {1,0,0});
            colors.put(new Integer(Grid.OUTSIDE), new float[] {0,0,1});

            HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
            transparency.put(new Integer(Grid.INTERIOR), new Float(0));
            transparency.put(new Integer(Grid.EXTERIOR), new Float(0.5));
            transparency.put(new Integer(Grid.OUTSIDE), new Float(0.98));

            exporter.writeDebug(grid, colors, transparency);
            exporter.close();

            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        generate("out.x3db");
    }
}
