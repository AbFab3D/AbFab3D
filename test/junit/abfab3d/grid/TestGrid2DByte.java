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
import abfab3d.core.Grid2D;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

// Internal Imports

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;

/**
 * Tests the functionality of a Grid2DByte
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestGrid2DByte extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestGrid2DByte.class);
    }

    public void testSmallGrid(){
        int nx = 100;
        int ny = 200;
        Grid2D grid = new Grid2DByte(nx, ny);
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                long att = (x*y) & 0xFF;
                grid.setAttribute(x,y,x*y);
                long a = grid.getAttribute(x,y);
                if(a != att) 
                    assertTrue(fmt("%d != %d\n",a, att), a == att);
            }
        }
    }

    public void testLargeGrid(){
        int nx = 32000; // theoretical max grid supported is sqrt(2^31-1) = 46340
        int ny = 32000;
        Grid2D grid = new Grid2DByte(nx, ny);
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                long att = (x*y) & 0xFF;
                grid.setAttribute(x,y,x*y);
                long a = grid.getAttribute(x,y);
                if(a != att) 
                    assertTrue(fmt("%d != %d\n",a, att), a == att);
            }
        }
    }

    public static void main(String arg[]){

        //new TestGrid2DByte().testSmallGrid();
        new TestGrid2DByte().testLargeGrid();

    }

}
