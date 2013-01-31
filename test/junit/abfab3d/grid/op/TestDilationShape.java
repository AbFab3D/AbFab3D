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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.j3d.geom.GeometryData;
import org.j3d.geom.TorusGenerator;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

// Internal Imports
import abfab3d.geom.TorusCreator;
import abfab3d.geom.TriangleModelCreator;
import abfab3d.grid.*;
import abfab3d.io.output.BoxesX3DExporter;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;

/**
 * Tests the functionality of DilationShape Operation
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestDilationShape extends TestCase {//BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDilationShape.class);
    }

    /**
     * Test basic operation
     */
    public void _testSingleVoxel() {

        int size = 31;

        for(int k = 1; k < 10; k++){
            
            AttributeGrid grid = makeBlock(size, size, size, size/2);
            
            int voxelCount =  grid.findCount(0);
            //writeFile(grid, "/tmp/dilationSingleVoxel.x3d");
            DilationShape dil = new DilationShape();        
            dil.setVoxelShape(VoxelShapeFactory.getCross(k));            
            grid = dil.execute(grid);            
            writeFile(grid, fmt("/tmp/dilationSingleVoxelDilated_%d.x3d", k));
            
            voxelCount =  grid.findCount(0);
            printf("dilated grid volume: %d\n",voxelCount);
            assertTrue("dilation of single voxel", voxelCount == (6*k + 1));

        }
    }


    public void _testCubeVoxel() {

        int size = 30;

        for(int k = 1; k < 10; k++){
            
            AttributeGrid grid = makeBlock(size, size, size, 10);
            
            int origVolume =  grid.findCount(0);
            //writeFile(grid, "/tmp/dilationSingleVoxel.x3d");
            DilationShape dil = new DilationShape();        
            dil.setVoxelShape(VoxelShapeFactory.getCross(k));            
            grid = dil.execute(grid);            
            writeFile(grid, fmt("/tmp/dilationBlockDilated_%d.x3d", k));
            
            int dilatedVolume =  grid.findCount(0);

            printf("orig volume: %d dilated volume: %d\n",origVolume, dilatedVolume);
            assertTrue("volume of dilated block ", (dilatedVolume - origVolume) == k * 600);

        }
    }

    public void _testDilationBall() {

        int size = 45;

        for(int k = 1; k < 20; k++){
            
            AttributeGrid grid = makeBlock(size, size, size, 20);
            
            int origVolume =  grid.findCount(0);
            DilationShape dil = new DilationShape();        
            dil.setVoxelShape(VoxelShapeFactory.getBall(k,0,0));            
            grid = dil.execute(grid);            
            writeFile(grid, fmt("/tmp/dilationBlockDilated_%02d.x3d", k));
            
            int dilatedVolume =  grid.findCount(0);

            printf("orig volume: %d dilated volume: %d\n",origVolume, dilatedVolume);
            //assertTrue("volume of dilated block ", (dilatedVolume - origVolume) == k * 600);

        }
    }

    public void testSpeed() {

        int size = 100;
        int nx = 300, ny = 300, nz = 300;
        int dilationSize = 6;

        printf("grid: %d x %d x %d\n", nx, ny, nz);
        printf("dilation size: %d\n", dilationSize);
        
        AttributeGrid grid = makeBlock(nx, ny, nz, dilationSize+1);

        DilationShape dil = new DilationShape();        
        dil.setVoxelShape(VoxelShapeFactory.getBall(dilationSize,0,0));            
        long t0 = time();
        grid = dil.execute(grid);            
        printf("ball dilation time: %d ms\n", (time() - t0));
        //writeFile(grid, fmt("/tmp/dilationShape.x3d"));

        
        // test iterative dilation 
        grid = makeBlock(nx, ny, nz, dilationSize+1);
        DilationMask dilm = new DilationMask(dilationSize);        
        t0 = time();
        dilm.execute(grid);
        printf("iterative dilation time: %d ms\n", (time() - t0));
        //writeFile(grid, fmt("/tmp/dilationIterative.x3d"));
        
        /*
        // test old sphere dilation
        grid = makeBlock(nx, ny, nz, dilationSize+1);
        DilationMask dils = new DilationMask(dilationSize,0);        
        t0 = time();
        dils.execute(grid);
        printf("old spherical dilation time: %d ms\n", (time() - t0));
        writeFile(grid, fmt("/tmp/dilationSphere.x3d"));
        */
    }


    private AttributeGrid makeBlock(int nx, int ny, int nz, int offset) {
        
        AttributeGrid grid = new GridShortIntervals(nx, ny, nz, 0.001, 0.001);
        //AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, 0.001, 0.001);

        for (int y = offset; y < ny - offset; y++) {
            for (int x = offset; x < nx - offset; x++) {
                for (int z = offset; z < nz - offset; z++) {
                    //printf("%d %d %d\n",x,y,z);
                    grid.setState(x,y,z, Grid.INTERIOR);
                }
            }
        }
        return grid;
    }

    private void writeFile(Grid grid, String filename) {

        try {

            ErrorReporter console = new PlainTextErrorReporter();

            FileOutputStream fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".")+1);
            BoxesX3DExporter exporter = new BoxesX3DExporter(encoding, fos, console);

            HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
            colors.put(new Integer(Grid.INTERIOR), new float[] {0,1,0});
            //colors.put(new Integer(Grid.EXTERIOR), new float[] {1,0,0});
            //colors.put(new Integer(Grid.OUTSIDE), new float[] {0,1,1});

            HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
            transparency.put(new Integer(Grid.INTERIOR), new Float(0));
            //transparency.put(new Integer(Grid.EXTERIOR), new Float(0.5));
            //transparency.put(new Integer(Grid.OUTSIDE), new Float(1));

            exporter.writeDebug(grid, colors, transparency);

            exporter.close();

            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    


    public static void main(String[] args) {

        TestDilationShape ec = new TestDilationShape();
        
        //ec.dilateCube();

    }
}
