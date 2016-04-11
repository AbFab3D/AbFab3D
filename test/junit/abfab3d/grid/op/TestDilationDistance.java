/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014
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

import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.GridDataDesc;
import abfab3d.grid.GridDataChannel;
import abfab3d.grid.BaseTestAttributeGrid;

import abfab3d.io.output.SVXWriter;

import junit.framework.Test;
import junit.framework.TestSuite;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;

// Internal Imports

/**
 * Tests the functionality of DilationDistance
 *
 * @author Alan Hudson
 * @version
 */
public class TestDilationDistance extends BaseTestAttributeGrid {
    
    static final boolean DEBUG = true;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDilationDistance.class);
    }

    public void testNothing() {
        // sad but true, needed to place hold
    }
    
    public void testBox() {

        printf("dilateSphere()\n");

        String outDir = "/tmp/";
        SVXWriter writer = new SVXWriter();

        int gridWidth = 20;
        int boxWidth = 1;
        int subvoxelResolution = 255;

        double vs = 1.;
        int cx = gridWidth/2;
        int cy = cx;
        int cz = cx;
        double distance = cx/4;
      
        double s = gridWidth/2;

        GridDataDesc attDesc = new GridDataDesc();
        attDesc.addChannel(new GridDataChannel(GridDataChannel.DENSITY, "dens", 8,0));

        double bounds[] = new double[]{-s,s, -s, s, -s,s };

        AttributeGrid grid = new ArrayAttributeGridByte(gridWidth,gridWidth,gridWidth,vs, vs);
        grid.setGridBounds(bounds);
        int width = 15;
        int box[]= new int[]{cx-boxWidth/2,cx-boxWidth/2+boxWidth,cy,cy+1,cz,cz+1};

        fillBox(grid, box,  subvoxelResolution);
        double vorig = getVolume(grid, subvoxelResolution);
        
        grid.setDataDesc(attDesc);
        if(DEBUG) writer.write(grid, outDir + "/boxx_orig.svx");
        
        DilationDistance dd = new DilationDistance(distance,subvoxelResolution);
        dd.setThreadCount(1);
        AttributeGrid dilatedGridST = dd.execute(grid);

        if(DEBUG) {
            dilatedGridST.setDataDesc(attDesc);
            writer.write(dilatedGridST, outDir + "/boxx_dilatedST.svx");
        }

        AttributeGrid grid1 = new ArrayAttributeGridByte(gridWidth,gridWidth,gridWidth,vs, vs);
        grid1.setGridBounds(bounds);
        fillBox(grid1, box,  subvoxelResolution);

        dd.setThreadCount(1);
        AttributeGrid dilatedGridMT = dd.execute(grid1);
        if(DEBUG) {
            dilatedGridMT.setDataDesc(attDesc);
            writer.write(dilatedGridMT, outDir + "/boxx_dilatedMT.svx");
        }
        double vst = getVolume(dilatedGridST, subvoxelResolution);
        double vmt = getVolume(dilatedGridMT, subvoxelResolution);
        printf("dilated grid volume orig: %10.2f ST: %10.2f M: %10.2f\n", vorig, vst, vmt);

        assertTrue(fmt("volume of ST and MT operations differs: %8.2f != %8.2f",vst, vmt),(vst == vmt));
        
    }

    static double getVolume(AttributeGrid grid, double svr) {

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        long v = 0;
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    v += grid.getAttribute(x,y,z);
                }
            }
        }
        return v/(svr);
    }
    
    static void fillBox(AttributeGrid grid, int box[], long value) {
        int xmi = box[0];
        int xma = box[1];
        int ymi = box[2];
        int yma = box[3];
        int zmi = box[4];
        int zma = box[5];

        for(int y = ymi; y < yma; y++){
            for(int x = xmi; x < xma; x++){
                for(int z = zmi; z < zma; z++){ 
                    grid.setAttribute(x,y,z,value);
                }
            }
        }
    }

    public static void main(String[] args) {
        //new TestDilationDistance().dilateSphere();
        new TestDilationDistance().testBox();
//        ec.dilateCube();
//        ec.dilateTorus();
    }
}
