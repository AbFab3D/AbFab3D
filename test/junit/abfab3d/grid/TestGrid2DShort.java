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

import abfab3d.util.ImageGray16;
import abfab3d.util.ImageUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Units.MM;

// Internal Imports

/**
 * Tests the functionality of a Grid2DShort
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestGrid2DShort extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestGrid2DShort.class);
    }

    public void testSmallGrid(){
        int nx = 100;
        int ny = 200;
        Grid2D grid = new Grid2DShort(nx, ny);
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                long att = (x*y) & 0xFFFF;
                grid.setAttribute(x,y,x*y);
                long a = grid.getAttribute(x,y);
                if(a != att) 
                    fail(fmt("%d != %d\n",a, att));
            }
        }
    }

    public void testLargeGrid(){
        int nx = 32000; // theoretical max grid supported is sqrt(2^31-1) = 46340
        int ny = 32000;
        Grid2D grid = new Grid2DShort(nx, ny);
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                long att = (x*y) & 0xFFFF;
                grid.setAttribute(x,y,x*y);
                long a = grid.getAttribute(x,y);
                if(a != att) 
                    fail(fmt("%d != %d\n",a, att));
            }
        }
    }

    /**
     * Test usage in Java images to Grids that orientation is correct
     */
    public void testImageOrientation() throws IOException  {
        String path = "test/images/LineLeftToRight.png";

        BufferedImage image = null;

        try {
            image = ImageIO.read(new File(path));

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

        Grid2D grid = Grid2DShort.convertImageToGrid(image, 0.1*MM);

        // test that 0,0 is not zero

        assertTrue("origin not zero", grid.getAttribute(0,0) != 0);

        Grid2DShort.write(grid, "/tmp/line.png");

    }


    public static void main(String arg[]){

        //new TestGrid2DShort().testSmallGrid();
        new TestGrid2DShort().testLargeGrid();

    }

}
