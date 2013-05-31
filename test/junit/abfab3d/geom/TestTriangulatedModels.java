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


// Internal Imports
import abfab3d.util.TrianglePrinter;
import abfab3d.io.output.STLWriter;

/**
 * Tests the functionality of TriangleModelCreator.
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestTriangulatedModels extends TestCase {

    static final double MM = 0.001;

    /**+
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTriangulatedModels.class);
    }

    /**
     * Test the generation of star
     */
    public void testStar() throws IOException {
	
        //TriangulatedModels.Star star = new  TriangulatedModels.Star(100, 0.01, 0.12, 0.005, 2., 0.5);
        //TriangulatedModels.Star star = new  TriangulatedModels.Star(20, 1.9*MM, 0.1*MM, 0.1*MM, 20*MM, 10*MM);
        TriangulatedModels.Star star = new  TriangulatedModels.Star(24, 3*MM, 3*MM, 3*MM, 20*MM, 10*MM);
        TrianglePrinter tp = new  TrianglePrinter();
        STLWriter stl = new STLWriter("/tmp/star_24.stl");
        star.getTriangles(stl);
        star.getTriangles(tp);
        stl.close();              

    }

    
}
