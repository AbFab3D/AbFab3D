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

package abfab3d.io.input;

// External Imports


// external imports
import abfab3d.util.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports
        import abfab3d.core.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridByte;

        import abfab3d.io.output.SlicesWriter;


        import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;

/**
 * Tests the functionality of SlicesReader
 *
 * @version
 */
public class TestSlicesReader extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestSlicesReader.class);
    }


    /**
       make test suit happy 
     */
    public void testNothing(){
        
    }
    
    /**
       reads test set of slices
     */
    public void readSlices() throws Exception {

        double vs = 0.1*MM;
        int nx = 101;  int ny = 316; int nz = 132; 

        //int subvoxelResolution = 255;

        AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, vs, vs);        

        SlicesReader reader = new SlicesReader();
        reader.readSlices(grid, "/tmp/slices/layers/image%04d.png", 0, 0, nz, 2);
        
        SlicesWriter writer = new SlicesWriter();
        writer.writeSlices(grid, "/tmp/slices/dens/slicez%04d.png", 0, 0, nz, 2, 8, new DefaultLongConverter());
        
        

    }
        
    public static void main(String[] args) throws Exception{

        new TestSlicesReader().readSlices();

    }
}
