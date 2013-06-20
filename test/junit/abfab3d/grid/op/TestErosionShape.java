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

import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

// Internal Imports
import abfab3d.grid.*;
import abfab3d.io.output.BoxesX3DExporter;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;

/**
 * Tests the functionality of ErosinShape Operation
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestErosionShape extends TestCase {//BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestErosionShape.class);
    }

    public void testBlockErosion() {

        int nx = 30, ny = 35, nz = 40;
        int kmax = 4;
        int offset = 10;

        for(int k = 1; k <= kmax; k++){

            AttributeGrid grid = makeBlock(nx+2*offset, ny+2*offset, nz+2*offset, offset);

            int origVolume =  grid.findCount(Grid.INSIDE);
            ErosionShape dil = new ErosionShape();
            dil.setVoxelShape(VoxelShapeFactory.getBall(k,0,0));
            grid = dil.execute(grid);
            writeFile(grid, fmt("/tmp/erosionBlockEroded_%d.x3d", k));

            int erodedVolume =  grid.findCount(Grid.INSIDE);
            int exactVolume = (nx-2*k)*(ny-2*k)*(nz-2*k);
            printf("orig volume: %d eroded volume: %d exactVolume: %d\n",origVolume, erodedVolume, exactVolume);
            assertTrue("test of volume of eroded block ", (erodedVolume == exactVolume));

        }
    }

    public void _testSpeed() {

        int size = 100;
        int nx = 100, ny = 200, nz = 300;
        int dilationSize = 4;

        printf("grid: %d x %d x %d\n", nx, ny, nz);
        printf("dilation size: %d\n", dilationSize);

        AttributeGrid grid = makeBlock(nx, ny, nz, dilationSize+1);

        ErosionShape op = new ErosionShape();
        op.setVoxelShape(VoxelShapeFactory.getBall(dilationSize,0,0));
        long t0 = time();
        grid = op.execute(grid);
        printf("ball erosion time: %d ms\n", (time() - t0));
        //writeFile(grid, fmt("/tmp/dilationShape.x3d"));

        // test iterative dilation
        grid = makeBlock(nx, ny, nz, dilationSize+1);
        ErosionMask opm = new ErosionMask(dilationSize);
        t0 = time();
        opm.execute(grid);
        printf("iterative erosion time: %d ms\n", (time() - t0));
        //writeFile(grid, fmt("/tmp/dilationIterative.x3d"));

        // test old sphere dilation
        grid = makeBlock(nx, ny, nz, dilationSize+1);
        ErosionMask ops = new ErosionMask(dilationSize,0);
        t0 = time();
        ops.execute(grid);
        printf("old spherical erosion time: %d ms\n", (time() - t0));
        //writeFile(grid, fmt("/tmp/dilationSphere.x3d"));

    }

    private AttributeGrid makeBlock(int nx, int ny, int nz, int offset) {

        AttributeGrid grid = new GridShortIntervals(nx, ny, nz, 0.001, 0.001);
        //AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, 0.001, 0.001);

        for (int y = offset; y < ny - offset; y++) {
            for (int x = offset; x < nx - offset; x++) {
                for (int z = offset; z < nz - offset; z++) {
                    //printf("%d %d %d\n",x,y,z);
                    grid.setState(x,y,z, Grid.INSIDE);
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
            colors.put(new Integer(Grid.INSIDE), new float[] {0,1,0});
            //colors.put(new Integer(Grid.INSIDE), new float[] {1,0,0});
            //colors.put(new Integer(Grid.OUTSIDE), new float[] {0,1,1});

            HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
            transparency.put(new Integer(Grid.INSIDE), new Float(0));
            //transparency.put(new Integer(Grid.INSIDE), new Float(0.5));
            //transparency.put(new Integer(Grid.OUTSIDE), new Float(1));

            exporter.writeDebug(grid, colors, transparency);

            exporter.close();

            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }




    public static void main(String[] args) {


    }
}
