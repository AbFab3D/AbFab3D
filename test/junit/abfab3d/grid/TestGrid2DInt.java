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
import junit.framework.TestSuite;
import junit.framework.TestCase;

// Internal Imports

import abfab3d.io.output.SVXWriter;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;

/**
 * Tests the functionality of a Grid2DByte
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestGrid2DInt extends TestCase {

    static final long INTMASK = 0xFFFFFFFFL;
    static final boolean DEBUG_WRITE = false;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestGrid2DInt.class);
    }

    /**
       writes and reads into 1024x1024 grid of integers 
     */
    public void testSmallGrid(){

        int nx = 1024;
        int ny = 1024;

        AttributeDesc attDesc = new AttributeDesc();
        //attDesc.addChannel(new AttributeChannel(AttributeChannel.COLOR, "color", 24, 0));
        //attDesc.addChannel(new AttributeChannel(AttributeChannel.DENSITY, "d1", 8, 0));
        //attDesc.addChannel(new AttributeChannel(AttributeChannel.DENSITY, "d2", 8, 8));
        //attDesc.addChannel(new AttributeChannel(AttributeChannel.DENSITY, "d3", 8, 16));
        //attDesc.addChannel(new AttributeChannel(AttributeChannel.DENSITY, "d4", 8, 24));
        attDesc.addChannel(new AttributeChannel(AttributeChannel.COLOR, "color", 24, 0));
        attDesc.addChannel(new AttributeChannel(AttributeChannel.DENSITY, "d", 8, 24));

        AttributeGrid grid = new Grid2DInt(nx, ny);
        grid.setAttributeDesc(attDesc);

        Grid2D g2 = (Grid2D)grid;

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                long xx  = x;
                long yy = y;
                long att = ( (((xx) & 0xFF) << 8) | (((yy) & 0xFF) << 24) | (((xx+yy) & 0xFF) << 16) | ((( xx-yy) & 0xFF ) ) )  & INTMASK;
                g2.setAttribute(x,y,att);
                long a = g2.getAttribute(x,y);
                if(a != att) 
                    assertTrue(fmt("%d != %d\n",a, att), a == att);
            }
        }
        if(DEBUG_WRITE){
            SVXWriter svx = new SVXWriter(2);
            svx.write(grid, "/tmp/testOut/testSmallGrid.svx");
        }
    }

    public void _testLargeGrid(){
        int nx = 32000; // theoretical max grid supported is sqrt(2^31-1) = 46340
        int ny = 32000;
        Grid2D grid = new Grid2DInt(nx, ny);
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){                
                long att = ((x) | (y << 8) | ((x+y)<< 16) | ((x-y) << 24))  & INTMASK;
                grid.setAttribute(x,y,att);
                long a = grid.getAttribute(x,y);
                if(a != att) 
                    assertTrue(fmt("%d != %d\n",a, att), a == att);
            }
        }
    }

    public static void main(String arg[]){

        new TestGrid2DInt().testSmallGrid();
        //new TestGrid2DInt()._testLargeGrid();

    }
}
