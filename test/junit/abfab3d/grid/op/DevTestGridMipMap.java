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
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.io.output.SlicesWriter;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;

/**
 *
 * @author Vladimir Bulatov
 * @version
 */
public class DevTestGridMipMap {
    
    static void testMipMapCreation() throws IOException{

        int nx = 250, ny = 200, nz = 50;
        int margin = 5;
        int maxAttributeValue = 255;
        double voxelSize = 20*MM/nx;
        
        AttributeGrid grid = makeBlock(new ArrayAttributeGridByte(1,1,1,voxelSize, voxelSize), nx, ny, nz, margin, maxAttributeValue);        
        grid.setAttribute(nx/2, ny/2, nz/2, maxAttributeValue);
        long a = grid.getAttribute(nx/2, ny/2, nz/2);
        printf("a: %d -> %d\n", maxAttributeValue, a);
        GridMipMap mm = new GridMipMap(grid);
        if(false){
            SlicesWriter slicer = new SlicesWriter();
            slicer.setFilePattern("/tmp/slices/slice_%03d.png");
            slicer.setCellSize(3);
            slicer.setVoxelSize(2);        
            slicer.setMaxAttributeValue(maxAttributeValue);
            slicer.writeSlices(grid);     
        }   
        
    }


    static AttributeGrid makeBlock(AttributeGrid g, int nx, int ny, int nz, int margin, int attributeValue) {

        AttributeGrid grid = (AttributeGrid)g.createEmpty(nx, ny, nz, g.getVoxelSize(), g.getSliceHeight());
        int xmin = margin;
        int xmax  = nx - xmin;
        int ymin = margin;
        int ymax  = ny - margin;
        int zmin = margin;
        int zmax  = nz - zmin;

        for (int y = ymin; y < ymax; y++) {
            for (int x = xmin; x < xmax; x++) {
                for (int z = zmin; z < zmax; z++) {
                    grid.setAttribute(x,y,z, attributeValue);
                }
            }
        }
        return grid;
    }


    public static void main(String[] args) throws Exception{
        
        testMipMapCreation();

    }
}
