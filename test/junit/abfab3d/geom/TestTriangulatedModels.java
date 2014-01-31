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

import javax.vecmath.Vector3d;

// Internal Imports
import abfab3d.util.TrianglePrinter;
import abfab3d.io.output.STLWriter;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Units.MM;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;


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


    public void testDumb(){
        //this test here is to make Test happy. 
    }

    /**
     * Test the generation of star
     */
    public void makeStar() throws IOException {
	
        //TriangulatedModels.Star star = new  TriangulatedModels.Star(100, 0.01, 0.12, 0.005, 2., 0.5);
        //TriangulatedModels.Star star = new  TriangulatedModels.Star(20, 1.9*MM, 0.1*MM, 0.1*MM, 20*MM, 10*MM);
        TriangulatedModels.Star star = new  TriangulatedModels.Star(24, 3*MM, 3*MM, 3*MM, 20*MM, 10*MM);
        TrianglePrinter tp = new  TrianglePrinter();
        STLWriter stl = new STLWriter("/tmp/star_24.stl");
        star.getTriangles(stl);
        star.getTriangles(tp);
        stl.close();              

    }

    public void _testParallelepiped() throws IOException {
	
        TriangulatedModels.Parallelepiped pp = new  TriangulatedModels.Parallelepiped(0.25, 0.25, 0.25, 0.75,  0.75,  0.75);
        //TriangulatedModels.Parallelepiped pp = new  TriangulatedModels.Parallelepiped(0.25, 0.25, 0.25, 0.35,  0.45,  0.55);
        STLWriter stl = new STLWriter("/tmp/parallelepiped.stl");
        pp.getTriangles(stl);
        stl.close();              

    }

    public void _testTetrahedronInParallelepiped() throws IOException {
	
        //TriangulatedModels.TetrahedronInParallelepiped pp = new  TriangulatedModels.TetrahedronInParallelepiped(0.05*MM, 0.05*MM, 0.05*MM, 0.45*MM,  0.45*MM,  0.45*MM,1);
        //TriangulatedModels.TetrahedronInParallelepiped pp = new  TriangulatedModels.TetrahedronInParallelepiped(0.55*MM, 0.55*MM, 0.55*MM, 0.95*MM,  0.95*MM,  0.95*MM,1);
        //TriangulatedModels.TetrahedronInParallelepiped pp = new  TriangulatedModels.TetrahedronInParallelepiped(0.05, 0.25, 0.25, 0.75,  0.75,  0.75,0);
        //TriangulatedModels.TetrahedronInParallelepiped pp = new  TriangulatedModels.TetrahedronInParallelepiped(0.01*MM, 0.01*MM, 0.01*MM, 0.24*MM,  0.24*MM, 0.24*MM,0);
        //TriangulatedModels.TetrahedronInParallelepiped pp1 = new  TriangulatedModels.TetrahedronInParallelepiped(0.25, 0.25, 0.25, 0.75,  0.75,  0.75,1);


        double eps = 1.e-6*MM;
        double size = 0.25*MM;
        double shiftx = 0*size;
        double shifty = 2*size;
        double shiftz = 0*size;
        //TriangulatedModels.Parallelepiped pp = new  TriangulatedModels.Parallelepiped(eps+shiftx, eps+shifty, eps+shiftz, size-eps+shiftx, size-eps+shifty, size-eps+shiftz);
        TriangulatedModels.TetrahedronInParallelepiped pp = 
            new  TriangulatedModels.TetrahedronInParallelepiped(eps+shiftx, eps+shifty, eps+shiftz, size-eps+shiftx, size-eps+shifty, size-eps+shiftz,0);

        TriangulatedModels.Star star = new TriangulatedModels.Star(400, 0.002, 0.001, 0.001, 0.6, 0.7);

        STLWriter stl = new STLWriter("/tmp/star_400.stl");
        star.getTriangles(stl);
        //pp1.getTriangles(stl);
        stl.close();              

    }

    public static void makeUnitSphere()throws Exception {
        
        STLWriter stl = new STLWriter("/tmp/sphere_10mm_10_1.stl");
        
        int n = 10;

        TriangulatedModels.Sphere s = new  TriangulatedModels.Sphere(10*MM, new Vector3d(0,0,0), 10, 0.1*MM);
        s.getTriangles(stl);

        stl.close();              

    }


    public static void makeChainOfSphere()throws Exception {
        
        STLWriter stl = new STLWriter("/tmp/chain_of_spheres.stl");
        
        int n = 10;

        for(int i = 0; i < 10; i++){

            double r = 10*MM;
            double rr = 30*MM;
            double phi = (2*Math.PI*i)/n;
            double xc = rr*cos(phi);
            double yc = rr*sin(phi);
            double zc = 0;

            TriangulatedModels.Sphere s = new  TriangulatedModels.Sphere(r, new Vector3d(xc, yc, zc), 6);
            s.getTriangles(stl);
        }

        stl.close();              
       
    }


    public static void main(String[] arg) throws Exception {

        makeUnitSphere();

    }
}
