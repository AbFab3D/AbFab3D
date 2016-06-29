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
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports
import abfab3d.util.TrianglePrinter;
import abfab3d.io.output.STLWriter;

import static abfab3d.core.Output.printf;


/**
 * Tests the functionality of ParametricSurfaceMaker
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestParametricSurfaceMaker extends TestCase {

    static final double MM = 0.001;

    /**+
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestParametricSurfaceMaker.class);
    }

    public void testDumb(){
        //this test here is to make Test happy. 
    }

    /**
     * 
     */
    public static void makeTorus() throws IOException {
	
        double rin = 4*MM; 
        double rout = 6*MM; 
        
        ParametricSurfaces.Torus torus = new ParametricSurfaces.Torus(rin, rout);

        ParametricSurfaceMaker maker = new ParametricSurfaceMaker(torus, 0.0001*MM);

        TrianglePrinter printer = new TrianglePrinter();
        STLWriter stl = new STLWriter("/tmp/torus_4mm_6mm_0001.stl");
        maker.getTriangles(stl);
        //maker.getTriangles(printer);
        stl.close();              

    }

    /**
     * 
     */
    public static void makeSphere() throws IOException {
	
        double rad = 10*MM; 
        
        ParametricSurfaces.Sphere sphere = new ParametricSurfaces.Sphere(rad);

        ParametricSurfaceMaker maker = new ParametricSurfaceMaker(sphere, 0.001*MM);

        STLWriter stl = new STLWriter("/tmp/sphere_10mm_001.stl");
        maker.getTriangles(stl);
        stl.close();              

    }

    public static void main(String[] arg) throws Exception {

        makeTorus();
        //makeSphere();

    }
}
