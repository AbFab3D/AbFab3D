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
public class TestDilationShapeMT extends TestCase {//BaseTestAttributeGrid {


    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDilationShapeMT.class);
    }

    static final int GRID_SHORTINTERVALS = 0, GRID_ARRAYBYTE = 1; 

    /**
     * Test basic operation
     */
    public void _testSingleVoxel() {

        int size = 31;
        int gridType = GRID_ARRAYBYTE;

        for(int k = 1; k < 10; k++){
            
            AttributeGrid grid = makeBlock(gridType, size, size, size, size/2);
            
            int voxelCount =  grid.findCount(0);
            //writeFile(grid, "/tmp/dilationSingleVoxel.x3d");
            DilationShapeMT dil = new DilationShapeMT();        
            dil.setThreadCount(4);
            dil.setVoxelShape(VoxelShapeFactory.getCross(k));            
            grid = dil.execute(grid);            
            writeFile(grid, fmt("/tmp/dilationSingleVoxelDilatedMT_%d.x3d", k));
            
            voxelCount =  grid.findCount(0);
            printf("dilated grid volume: %d\n",voxelCount);
            assertTrue("dilation of single voxel", voxelCount == (6*k + 1));

        }
    }


    public void _testCubeVoxel() {

        int size = 30;
        int gridType = GRID_SHORTINTERVALS;//GRID_ARRAYBYTE;

        for(int k = 1; k < 10; k++){
            
            AttributeGrid grid = makeBlock(gridType, size, size, size, 10);
            
            int origVolume =  grid.findCount(0);
            //writeFile(grid, "/tmp/dilationSingleVoxel.x3d");
            DilationShapeMT dil = new DilationShapeMT(); 
            dil.setThreadCount(4);
            dil.setVoxelShape(VoxelShapeFactory.getCross(k));            
            grid = dil.execute(grid);            
            writeFile(grid, fmt("/tmp/dilationBlockDilatedMT_%d.x3d", k));
            
            int dilatedVolume =  grid.findCount(0);

            printf("orig volume: %d dilated volume: %d\n",origVolume, dilatedVolume);
            assertTrue("volume of dilated block ", (dilatedVolume - origVolume) == k * 600);

        }
    }

    public void testDilationBall() {

        int size = 100;
        int gridType = GRID_ARRAYBYTE;
        //int gridType = GRID_SHORTINTERVALS;
        printf("testing block shape dilated by different size balls\n");

        int maxDilation = 20;

        for(int k = 1; k < maxDilation; k++){
            int s = size+2*(maxDilation+1);
            AttributeGrid grid = makeBlock(gridType,s,s,s, maxDilation);
            printf("dilation size: %d\n",k);
            DilationShapeMT dilm = new DilationShapeMT();        
            dilm.setVoxelShape(VoxelShapeFactory.getBall(k,0,0));            
            dilm.setThreadCount(1);
            dilm.setSliceSize(2);
            long t0 = time();
            grid = dilm.execute(grid);   
            printf("DilationShapeMT: %dms\n",(time() - t0));
            int dilatedVolumeMT =  grid.findCount(0);

            grid = makeBlock(gridType,s,s,s, maxDilation);
            
            DilationShape dil = new DilationShape();  
            dil.setVoxelShape(VoxelShapeFactory.getBall(k,0,0));            
            t0 = time();
            grid = dil.execute(grid);                        
            printf("DilationShape: %dms\n",(time() - t0));
            int dilatedVolume =  grid.findCount(0);

            printf("DilationShape volume: %d, DilationShapeMT: %d\n",dilatedVolume, dilatedVolumeMT);
            assertTrue("DilationShapeMT and DilationShape volumes test", (dilatedVolume == dilatedVolumeMT));

        }
    }

    public void _testSpeed() {

        int gridType = GRID_ARRAYBYTE;

        int size = 100;
        int nx = 100, ny = 200, nz = 300;
        //int nx = 500, ny = 500, nz = 500;
        int dilationSize = 4;

        VoxelShape shape = VoxelShapeFactory.getBall(dilationSize,0,0);

        printf("grid: %d x %d x %d\n", nx, ny, nz);
        printf("dilation size: %d\n", dilationSize);        

        AttributeGrid grid = makeBlock(gridType, nx, ny, nz, dilationSize+1);

        DilationShape dil = new DilationShape();        
        dil.setVoxelShape(shape);  
        long t0 = time();
        grid = dil.execute(grid);            
        printf("DilationShape time: %d ms\n", (time() - t0));
        //writeFile(grid, fmt("/tmp/dilationShape.x3d"));
        
        for(int t = 1; t <= 4; t++){

            grid = makeBlock(gridType, nx, ny, nz, dilationSize+1);
            DilationShapeMT dilm = new DilationShapeMT();        
            dilm.setVoxelShape(shape);  
            dilm.setThreadCount(t);
            t0 = time();
            grid = dilm.execute(grid);            
            printf("DilationShapeMT(%d) time: %d ms\n", t, (time() - t0));
            //writeFile(grid, fmt("/tmp/dilationShape.x3d"));
        }
        
        // test old sphere dilation
        grid = makeBlock(gridType, nx, ny, nz, dilationSize+1);
        DilationMask dils = new DilationMask(dilationSize,0);        
        t0 = time();
        dils.execute(grid);
        printf("DilationMask( ball) time: %d ms\n", (time() - t0));
        //writeFile(grid, fmt("/tmp/dilationSphere.x3d"));
        
    }


    private AttributeGrid makeBlock(int gridType, int nx, int ny, int nz, int offset) {
        
        AttributeGrid grid;
        switch(gridType){
        case GRID_SHORTINTERVALS: 
            grid = new GridShortIntervals(nx, ny, nz, 0.001, 0.001);
            break;
        default: 
        case GRID_ARRAYBYTE: 
            grid = new ArrayAttributeGridByte(nx, ny, nz, 0.001, 0.001);
            break;            
        }

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
